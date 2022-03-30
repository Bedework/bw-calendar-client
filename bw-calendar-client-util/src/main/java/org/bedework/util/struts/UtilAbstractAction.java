/* ********************************************************************
    Licensed to Jasig under one or more contributor license
    agreements. See the NOTICE file distributed with this work
    for additional information regarding copyright ownership.
    Jasig licenses this file to you under the Apache License,
    Version 2.0 (the "License"); you may not use this file
    except in compliance with the License. You may obtain a
    copy of the License at:

    http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing,
    software distributed under the License is distributed on
    an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
    KIND, either express or implied. See the License for the
    specific language governing permissions and limitations
    under the License.
*/
package org.bedework.util.struts;

import org.bedework.util.logging.BwLogger;
import org.bedework.util.logging.Logged;
import org.bedework.util.servlet.HttpAppLogger;
import org.bedework.util.servlet.HttpServletUtils;
import org.bedework.util.servlet.filters.PresentationState;
import org.bedework.util.webaction.ErrorEmitSvlt;
import org.bedework.util.webaction.MessageEmitSvlt;
import org.bedework.util.webaction.Request;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.config.ActionConfig;

import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * Abstract implementation of <strong>Action</strong> which sets up frequently
 * required fields then calls an abstract method to do the work.
 *
 * <p>If the abstract action method returns null or throws an exception the
 * struts action method will forward to "error".
 *
 * <p>An invalid form of request parameter (for those recognized by the
 * abstract action) will cause a forward to "badRequest"..
 *
 * <p>Otherwise we forward to the result from the abstract action.
 *
 * <p>This action also checks for a number of request parameters, mostly
 * related to presentation of data but some related to debugging.
 *
 * <p>Debugging actions:<ul>
 * <li><em>debug=yes|no</em> Set the debugging mode</li>
 * <li><em>seralize=anything</em> Try to serialize all session attributes
 *      and log the result. Only when debugging on.</li>
 * </ul>
 * <p>The following are related to presentation of data. They are often
 * useful when debugging xslt but can be used to force content types.
 * <ul>
 * <li><em>browserType=!</em> Reset to normal dynamic behaviour,
 *                            that is browser type reset every request.</li>
 * <li><em>browserType=string</em> Set the browser type for one request</li>
 * <li><em>browserTypeSticky=!</em> Reset to normal dynamic behaviour,
 *                                  that is reset browser type every request.
 * </li>
 * <li><em>browserTypeSticky=string</em> Set the browser type permanently
 *                                          - browser type set until
 *                                          next explicit setting.</li>
 * </ul><p>
 * Set the 'skinName' - maybe change the look and feel or e.g. provide
 * printer friendly output:
 * <p><ul>
 * <li><em>skinName=!</em> Reset to normal dynamic behaviour,
 *                            that is reset every request.
 * </li>
 * <li><em>skinName=string</em> Set the skin name for one request
 * </li>
 * <li><em>skinNameSticky=!</em> Reset to normal dynamic behaviour,
 *                                  that is reset every request.
 * </li>
 * <li><em>skinNameSticky=string</em> Set the skin name permanently
 * </li>
 * </ul><p>
 * Allow user to set the content type explicitly. Used mainly for
 * debugging. The incoming request may contain the following:
 * <p><ul>
 * <li><em>contentType=!</em> Reset to normal dynamic behaviour,
 *                            that is reset every request.
 * </li>
 * <li><em>contentType=string</em> Set the Content type
 * </li>
 * <li><em>contentTypeSticky=!</em> Reset to normal dynamic behaviour,
 *                                  that is reset every request.
 * </li>
 * <li><em>contentTypeSticky=string</em> Set the Content type permanently
 * </li>
 * </ul><p>
 * Allow user to indicate if we should refresh the xslt once, every time
 * or only when something changes. Used mainly for
 * debugging. The incoming request may contain the following:
 * <p><ul>
 * <li><em>refreshXslt=!</em> Reset to normal dynamic behaviour,
 *                            that is reset every request.
 * </li>
 * <li><em>refreshXslt=yes</em> One shot refresh
 * </li>
 * <li><em>refreshxslt=always</em> Refresh every request.
 * </li>
 * </ul><p>
 * Some misc actions:
 * <p><ul>
 * <li><em>forwardto=name</em> Just does a forward to that name. Used to
 *                            provide a null action with arbitrary forward
 *                            configured in struts-config.xml
 * <li><em>noxslt=anything</em> Suppress XSLT transform for one request.
 *                              Used for debugging - provides the raw xml.
 * </li>
 *
 * </ul>
 * <p>A combination of the above can allow us to dump the output in a file,
 * <br /><em>contentType=text/text&amp;noxslt=yes</em>
 */
public abstract class UtilAbstractAction extends Action
         implements HttpAppLogger, Logged {
  private static boolean configTraced;

  private transient String logPrefix;

  //protected boolean isPortlet;

  /** This is the routine which does the work.
   *
   * @param request  Provide http request/response and form
   * @return String  forward name
   * @throws Throwable
   */
  public abstract String performAction(Request request)
               throws Throwable;

  @Override
  public ActionForward execute(final ActionMapping mapping,
                               final ActionForm frm,
                               final HttpServletRequest request,
                               final HttpServletResponse response) {
    ErrorEmitSvlt err = null;
    MessageEmitSvlt msg = null;

    String forward = "success";
    final UtilActionForm form = (UtilActionForm)frm;

    try {
      //isPortlet = isPortletRequest(request);

      err = ErrorEmitSvlt.getErrorObj(request, getId(),
                                      getErrorObjErrProp(),
                                      clearMessages());
      msg = MessageEmitSvlt.getMessageObj(request, getId(),
                                          getErrorObjErrProp(),
                                          clearMessages());

      /* Log the request - virtual domains can make it difficult to
       *  distinguish applications.
       */
      logRequest(request);

      if (debug()) {
        debug("entry");
        debug("================================");
        //debug("isPortlet=" + isPortlet);

        final Enumeration<?> en = servlet.getInitParameterNames();

        while (en.hasMoreElements()) {
          debug("attr name=" + en.nextElement());
        }
        debug("================================");

        dumpRequest(request);
      }

      if (!form.getInitialised()) {
        // Do one time settings
        form.setNocache(false);

        form.setInitialised(true);

        if (debug() && !configTraced) {
          traceConfig(mapping);
          configTraced = true;
        }
      }

      getErrorForward(request, form);
      form.setBrowserType(HttpServletUtils.getBrowserType(request));
      form.assignCurrentUser(HttpServletUtils.remoteUser(request));
      form.setUrl(HttpServletUtils.getUrl(request));
      form.setSchemeHostPort(HttpServletUtils.getURLshp(request));
      form.setContext(HttpServletUtils.getContext(request));
      form.setUrlPrefix(HttpServletUtils.getURLPrefix(request));
      form.setErr(err);
      form.setMsg(msg);
      form.assignSessionId(getSessionId(request));

      final Request req =
              getRequest(request, response,
                         getActionParams(mapping),
                         mapping.getPath(),
                         form);

      req.checkNocache();

      /* Set up presentation values from request
       */
      doPresentation(req);

      final String contentName = getContentName(req);

      if (contentName != null) {
        /* Indicate we have a file attachment with the given name
         */

        response.setHeader("Content-Disposition",
                           "Attachment; Filename=\"" + contentName + "\"");
      }

      // Debugging action to test session serialization
      if (debug()) {
        checkSerialize(request);
      }

      /* ----------------------------------------------------------------
         Everything is set up and ready to go. Execute something
         ---------------------------------------------------------------- */

      /* NO portlet
      if (!isPortlet) {
        forward = checkLogOut(request, form);
      } else {
        forward = null;
      }
      */

      forward = req.checkLogOut();
      response.setContentType("text/html");

      if (forward == null) {
        //if (!isPortlet) {
        //  response.setContentType(defaultContentType);
        //}
        forward = req.checkVarReq();

        if (forward == null) {
          forward = req.checkForwardto();
        }

        if (forward == null) {
          forward = performAction(req);
        }
      }

      if (forward == null) {
        warn("Forward = null");
        err.emit("edu.rpi.sss.util.nullforward");
        forward = "error";
      } else if (forward.equals("FORWARD-NULL")) {
        forward = null;
      }

      if (err == null) {
        warn("No errors object");
      } else if (err.messagesEmitted()) {
        if (debug()) {
          debug(err.getMsgList().size() + " errors emitted");
        }
      } else if (debug()) {
        debug("No errors emitted");
      }

      if (msg == null) {
        warn("No messages object");
      } else if (msg.messagesEmitted()) {
        if (debug()) {
          debug(msg.getMsgList().size() + " messages emitted");
        }
      } else if (debug()) {
        debug("No messages emitted");
      }

      if (debug()) {
        debug("exit to " + forward);
      }
    } catch (final Throwable t) {
      if (debug()) {
        error("Action exception: ", t);
      }

      err.emit(t);
      forward = "error";
    }

    if (forward == null) {
      return null;
    }

    return (mapping.findForward(forward));
  }

  /**
   *
   * @param request the http request
   * @param response the response
   * @param params action parameters
   * @param actionPath from mapping
   * @param form form object
   * @return
   */
  protected Request getRequest(final HttpServletRequest request,
                               final HttpServletResponse response,
                               final Map<String, String> params,
                               final String actionPath,
                               final UtilActionForm form) {
    return new Request(request, response, params, actionPath, form);
  }

  private Map<String, String> getActionParams(final ActionConfig config) {
    final Map<String, String> res = new HashMap<>();
    final String paramsStr = config.getParameter();

    if (paramsStr == null) {
      return res;
    }

    for (final String param: paramsStr.split(";")) {
      final String[] nv = param.split("=");
      if (nv.length == 1) {
//        debug(format("param %s", nv[0]));
        res.put(nv[0], "");
      } else {
//        debug(format("param %s=\"%s\"", nv[0], nv[1]));
        res.put(nv[0], nv[1]);
      }
    }

    return res;
  }

  protected void traceConfig(final ActionMapping mapping) {
    if (!debug()) {
      return;
    }

    final ActionConfig[] actions = mapping.getModuleConfig()
                                          .findActionConfigs();

    debug("========== Action configs ===========");

    for (final ActionConfig aconfig: actions) {
      final StringBuilder sb = new StringBuilder();

      sb.append(aconfig.getPath());

      final Map<String, String> params =
              getActionParams(aconfig);

      final boolean noActionType = traceConfigParam(sb,
                                                    Request.actionTypeKey,
                                                    params) == null;
      traceConfigParam(sb, Request.conversationKey, params);

      traceConfigParam(sb, Request.refreshIntervalKey, params);
      traceConfigParam(sb, Request.refreshActionKey, params);

      debug(sb.toString());

      if (noActionType) {
        debug("***** Warning: no action type specified ****");
      }
    }
  }

  private String traceConfigParam(final StringBuilder sb,
                                  final String name,
                                  final Map<String, String> params) {
    String res = params.get(name);
    if (res == null) {
      return null;
    }

    sb.append(",\t");
    sb.append(name);
    sb.append(res);

    return res;
  }

  /** Override this to get the contentName from different sources
   *
   * @param req
   * @return String name of content
   * @throws Throwable
   */
  public String getContentName(final Request req) {
    final UtilActionForm form = req.getForm();
    final PresentationState ps = getPresentationState(req);
    final String contentName = ps.getContentName();

    form.setContentName(contentName);

    return contentName;
  }

  /** Set the global id to some name for logging
   *
   * @return String id
   */
  public abstract String getId();

  /** In a portlet environment a render action should override this to return
   * false to preserve messages.
   *
   * @return boolean   true to clear messages
   */
  public boolean clearMessages() {
    return true;
  }

  /** Override to return a different name for the error exception property.
   * This must return non-null if getErrorObjAttrName returns a value.
   *
   * @return error exception property name
   */
  public String getErrorObjErrProp() {
    return "edu.rpi.sss.util.error.exc";
  }

  /** Overide this to set the value or turn off presentation support
   * by returning null or the value "NONE".
   *
   * @return presentation attr name
   */
  public String getPresentationAttrName() {
    return PresentationState.presentationAttrName;
  }

  public void getErrorForward(final HttpServletRequest request,
                                final UtilActionForm form) {
    if (form.getErrorForward() != null) {
      return;
    }

    final HttpSession session = request.getSession();
    final ServletContext sc = session.getServletContext();

    form.assignErrorForward(sc.getInitParameter("errorForward"));
  }

  /* ====================================================================
   *               Log request
   * ==================================================================== */

  @Override
  public String getLogPrefix(final HttpServletRequest request) {
    if (logPrefix == null) {
      final HttpSession session = request.getSession();
      final ServletContext sc = session.getServletContext();

      logPrefix = sc.getInitParameter("bwappname");
    }

    return logPrefix;
  }

  /* ====================================================================
   *               Check serialization
   * ==================================================================== */

  /* Debugging routine to see if we can serialize the session.
   * We see session serialization errors in the web container if an
   * unserializable object class gets embedded in the session somewhere
   */
  private void checkSerialize(final HttpServletRequest request) {
    final String reqpar = request.getParameter("serialize");

    if (reqpar == null) {
      return;
    }

    HttpSession sess = request.getSession(false);
    if (sess == null) {
      info("No session to test");
    }
    Enumeration en = sess.getAttributeNames();

    while (en.hasMoreElements()) {
      String attrname = (String)en.nextElement();
      ObjectOutputStream oo = null;

      info("Attempt to serialize attr " + attrname);
      Object o = sess.getAttribute(attrname);

      try {
        ByteArrayOutputStream bo = new ByteArrayOutputStream();
        oo = new ObjectOutputStream(bo);
        oo.writeObject(o);
        oo.flush();

        info("Serialized object " + attrname + " has size: " + bo.size());
      } catch (Throwable t) {
        t.printStackTrace();
      } finally {
        if (oo != null) {
          try {
            oo.close();
          } catch (Throwable t) {}
        }
      }
    }
  }

  /* ====================================================================
   *               Presentation state methods
   * ==================================================================== */

  /**
   * @param request
   */
  public void doPresentation(final Request request) {
    final PresentationState ps = getPresentationState(request);

    if (ps == null) {
      if (debug()) {
        debug("No presentation state");
      }
      return;
    }

    if (debug()) {
      debug("Set presentation state");
    }

    final HttpServletRequest req = request.getRequest();

    ps.checkBrowserType(req);
    ps.checkContentType(req);
    ps.checkContentName(req);
    ps.checkNoXSLT(req);
    ps.checkRefreshXslt(req);
    ps.checkSkinName(req);

    request.setRequestAttr(getPresentationAttrName(), ps);

    if (debug()) {
      ps.debugDump("action");
    }
  }

  /**
   * @param request
   * @return PresentationState
   */
  public PresentationState getPresentationState(final Request request) {
    final String attrName = getPresentationAttrName();

    if ((attrName == null) || (attrName.equals("NONE"))) {
      return null;
    }

    Object o = request.getSessionAttr(attrName);
    final PresentationState ps;

    if (!(o instanceof PresentationState)) {
      ps = new PresentationState();
      initPresentationState(request, ps);

      request.setSessionAttr(attrName, ps);
    } else {
      ps = (PresentationState)o;
    }

    return  ps;
  }

  /**
   * @param request
   * @return PresentationState
   */
  protected void initPresentationState(final Request request,
                                       final PresentationState ps) {

    ps.setBrowserType(request.getForm().getBrowserType());
    ps.setNoXSLTSticky(false);
  }

  /* ==================================================================
                Various utility methods
     ================================================================== */

  /**
   * @param req the http request
   */
  public void dumpRequest(final HttpServletRequest req) {
    try {
      final Enumeration<String> names = req.getParameterNames();

      final String title = "Request parameters";

      debug(title + " - global info and uris");
      debug("getRequestURI = " + req.getRequestURI());
      debug("getRemoteUser = " + req.getRemoteUser());
      debug("getUserPrincipal.name = " + req.getUserPrincipal().getName());
      debug("getRequestedSessionId = " + req.getRequestedSessionId());
      debug("HttpUtils.getRequestURL(req) = " + req.getRequestURL());
      debug("query=" + req.getQueryString());
      debug("contentlen=" + req.getContentLength());
      debug("request=" + req);
      debug("host=" + req.getHeader("host"));
      debug("parameters:");

      debug(title);

      while (names.hasMoreElements()) {
        final String key = names.nextElement();
        final String[] vals = req.getParameterValues(key);
        for (final String val: vals) {
          debug("  " + key + " = \"" + val + "\"");
        }
      }
    } catch (final Throwable ignored) {
    }
  }

  /* ====================================================================
   *                   Logged methods
   * ==================================================================== */

  private final BwLogger logger = new BwLogger();

  @Override
  public BwLogger getLogger() {
    if ((logger.getLoggedClass() == null) && (logger.getLoggedName() == null)) {
      logger.setLoggedClass(getClass());
    }

    return logger;
  }
}
