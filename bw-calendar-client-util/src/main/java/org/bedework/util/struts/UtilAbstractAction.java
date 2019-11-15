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
import org.bedework.util.misc.Util;
import org.bedework.util.servlet.HttpAppLogger;
import org.bedework.util.servlet.HttpServletUtils;
import org.bedework.util.servlet.filters.PresentationState;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessages;
import org.apache.struts.config.ActionConfig;
import org.apache.struts.util.MessageResources;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;

import javax.servlet.ServletException;
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
  transient MessageResources messages;

  private transient String logPrefix;

  protected String requestLogout = "logout";

  /** Forward to here for logged out
   */
  public final String forwardLoggedOut = "loggedOut";

  private boolean noActionErrors = false;

  protected boolean isPortlet;

  /** This is the routine which does the work.
   *
   * @param request  Provide http request/response and form
   * @param messages Resources
   * @return String  forward name
   * @throws Throwable
   */
  public abstract String performAction(Request request,
                                       MessageResources messages)
               throws Throwable;

  @Override
  public ActionForward execute(final ActionMapping mapping,
                               final ActionForm frm,
                               final HttpServletRequest request,
                               final HttpServletResponse response)
                               throws IOException, ServletException {
    ErrorEmitSvlt err = null;
    MessageEmitSvlt msg = null;

    String forward = "success";
    UtilActionForm form = (UtilActionForm)frm;

    try {
      messages = getResources(request);

      isPortlet = isPortletRequest(request);

      noActionErrors = StrutsUtil.getProperty(messages,
                                              "edu.rpi.sss.util.action.noactionerrors",
                                              "no").equals("yes");

      err = getErrorObj(request, messages);
      msg = getMessageObj(request, messages);

      /* Log the request - virtual domains can make it difficult to
       *  distinguish applications.
       */
      logRequest(request);

      if (debug()) {
        debug("entry");
        debug("================================");
        debug("isPortlet=" + isPortlet);

        Enumeration en = servlet.getInitParameterNames();

        while (en.hasMoreElements()) {
          debug("attr name=" + en.nextElement());
        }
        debug("================================");

        dumpRequest(request);
      }

      if (!form.getInitialised()) {
        // Do one time settings
        form.setNocache(
                StrutsUtil.getProperty(messages,
                                       "edu.rpi.sss.util.action.nocache",
                                       "no").equals("yes"));

        form.setInitialised(true);
      }

      form.setMres(messages);
      form.setBrowserType(StrutsUtil.getBrowserType(request));
      form.assignCurrentUser(HttpServletUtils.remoteUser(request));
      form.setUrl(StrutsUtil.getUrl(request));
      form.setSchemeHostPort(StrutsUtil.getURLshp(request));
      form.setContext(StrutsUtil.getContext(request));
      form.setUrlPrefix(StrutsUtil.getURLPrefix(request));
      form.setErr(err);
      form.setMsg(msg);
      form.assignSessionId(getSessionId(request));

      checkNocache(request, response, form);

      String defaultContentType =
          StrutsUtil.getProperty(messages,
                                 "edu.rpi.sss.util.action.contenttype",
                                 "text/html");

      Request req = new Request(request, response, form,
                                this, mapping);

      /** Set up presentation values from request
       */
      doPresentation(req);

      String contentName = getContentName(req);

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

      if (!isPortlet) {
        forward = checkLogOut(request, form);
      } else {
        forward = null;
      }

      if (forward != null) {
        // Disable xslt filters
        response.setContentType("text/html");
      } else {
        if (!isPortlet) {
          response.setContentType(defaultContentType);
        }
        forward = checkVarReq(req, form);

        if (forward == null) {
          forward = checkForwardto(request);
        }

        if (forward == null) {
          forward = performAction(req, messages);
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
        if (noActionErrors) {
        } else {
          ActionErrors aes = err.getErrors();
          saveErrors(request, aes);
        }

        if (debug()) {
          debug(err.getMsgList().size() + " errors emitted");
        }
      } else if (debug()) {
        debug("No errors emitted");
      }

      if (msg == null) {
        warn("No messages object");
      } else if (msg.messagesEmitted()) {
        ActionMessages ams = msg.getMessages();
        saveMessages(request, ams);

        if (debug()) {
          debug(ams.size() + " messages emitted");
        }
      } else if (debug()) {
        debug("No messages emitted");
      }

      if (debug()) {
        debug("exit to " + forward);
      }
    } catch (Throwable t) {
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

  protected void traceConfig(final Request req) {
    if (!debug()) {
      return;
    }

    ActionConfig[] actions = req.getMapping().getModuleConfig().findActionConfigs();

    debug("========== Action configs ===========");

    for (ActionConfig aconfig: actions) {
      StringBuilder sb = new StringBuilder();

      sb.append(aconfig.getPath());

      String param = aconfig.getParameter();

      boolean noActionType = traceConfigParam(req, sb,
                                              Request.actionTypeKey,
                                              param) == null;
      traceConfigParam(req, sb, Request.conversationKey, param);

      traceConfigParam(req, sb, Request.refreshIntervalKey, param);
      traceConfigParam(req, sb, Request.refreshActionKey, param);

      debug(sb.toString());

      if (noActionType) {
        debug("***** Warning: no action type specified ****");
      }
    }
  }

  private String traceConfigParam(final Request req,
                                  final StringBuilder sb,
                                  final String name,
                                  final String param) {
    String res = req.getStringActionPar(name, param);
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
  public String getContentName(final Request req) throws Throwable {
    UtilActionForm form = req.getForm();
    PresentationState ps = getPresentationState(req);
    String contentName = ps.getContentName();

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

  /** Override to return the name of the error object session attribute.
   *
   * @return String   request attribute name. Null to suppress.
   */
  public String getErrorObjAttrName() {
    return "edu.rpi.sss.util.errorobj";
  }

  /** Override to return the name of the messages object session attribute.
   *
   * @return String   request attribute name. Null to suppress.
   */
  public String getMessageObjAttrName() {
    return "edu.rpi.sss.util.messageobj";
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

  /**
   * @return message resources
   */
  public MessageResources getMessages() {
    return messages;
  }

  /**
   * @param name
   * @return message identified by name
   */
  public String getMessage(final String name) {
    return messages.getMessage(name);
  }

  /* ====================================================================
   *               Log request
   * ==================================================================== */

  @Override
  public String getLogPrefix(final HttpServletRequest request) {
    try {
      if (logPrefix == null) {
        logPrefix = StrutsUtil.getProperty(getMessages(),
                                           "edu.rpi.sss.util.action.logprefix",
                                           "unknown");
      }

      return logPrefix;
    } catch (Throwable t) {
      error(t);
      return "LOG-PREFIX-EXCEPTION";
    }
  }

  /* ====================================================================
   *               Check logout
   * ==================================================================== */

  /** Clean up - we're about to logout
   *
   * @param request    HttpServletRequest
   * @param form
   * @return boolean true for OK to log out. False - not allowed - ignore it.
   */
  protected boolean logOutCleanup(final HttpServletRequest request,
                                  final UtilActionForm form) {
    return true;
  }

  /** Check for logout request.
   *
   * @param request    HttpServletRequest
   * @param form
   * @return null for continue, forwardLoggedOut to end session.
   * @throws Throwable
   */
  protected String checkLogOut(final HttpServletRequest request,
                               final UtilActionForm form)
               throws Throwable {
    final String reqUser = HttpServletUtils.remoteUser(request);

    final boolean forceLogout =
            !Util.equalsString(reqUser, form.getCurrentUser());

    final String temp = request.getParameter(requestLogout);


    if (forceLogout || (temp != null)) {
      final HttpSession sess = request.getSession(false);

      if ((sess != null) && logOutCleanup(request, form)) {
        sess.invalidate();
      }
      return forwardLoggedOut;
    }

    return null;
  }

  /* ====================================================================
   *               Check nocache
   * ==================================================================== */

  /* We handle our own nocache headers instead of letting struts do it.
   * Struts does it on every response but, if we are running with nocache,
   * we need to be able to disable it for the occassional response.
   *
   * <p>This gets around an IE problem when attempting to deliver files.
   * IE requires caching on or it is unable to locate the file it is
   * supposed to be delivering.
   *
   */
  private void checkNocache(final HttpServletRequest request,
                            final HttpServletResponse response,
                            final UtilActionForm form) {
    String reqpar = request.getParameter("nocacheSticky");

    if (reqpar != null) {
      /* (re)set the default */
      form.setNocache(reqpar.equals("yes"));
    }

    /** Look for a one-shot setting
     */

    reqpar = request.getParameter("nocache");

    if ((reqpar == null) && (!form.getNocache())) {
      return;
    }

    /** If we got a request parameter it overrides the default
     */
    boolean nocache = form.getNocache();

    if (reqpar != null) {
      nocache = reqpar.equals("yes");
    }

    if (nocache) {
      response.setHeader("Pragma", "No-cache");
      //response.setHeader("Cache-Control", "no-cache");
      response.setHeader("Cache-Control", "no-store, no-cache, must-revalidate");
      response.setDateHeader("Expires", 1);
    }
  }

  /* ====================================================================
   *               Check serialization
   * ==================================================================== */

  /* Debugging routine to see if we can serialize the session.
   * We see session serialization errors in the web container if an
   * unserializable object class gets embedded in the session somewhere
   */
  private void checkSerialize(final HttpServletRequest request) {
    String reqpar = request.getParameter("serialize");

    if (reqpar == null) {
      return;
    }

    HttpSession sess = request.getSession(false);
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
   *                       Response methods
   * ==================================================================== */

  /** Check request for refresh interval
   *
   * @param request
   * @param response
   * @param refreshInterval
   * @param refreshAction
   * @param form
   */
  public void setRefreshInterval(final HttpServletRequest request,
                                 final HttpServletResponse response,
                                 final int refreshInterval,
                                 final String refreshAction,
                                 final UtilActionForm form) {
    if (refreshInterval != 0) {
      StringBuilder sb = new StringBuilder(250);

      sb.append(refreshInterval);
      sb.append("; URL=");
      sb.append(form.getUrlPrefix());
      if (!refreshAction.startsWith("/")) {
        sb.append("/");
      }
      sb.append(refreshAction);
      response.setHeader("Refresh", sb.toString());
    }
  }

  /* ====================================================================
   *                  Application variable methods
   * ==================================================================== */

  /**
   * @param request
   * @return app vars
   */
  @SuppressWarnings("unchecked")
  public HashMap<String, String> getAppVars(final Request request) {
    Object o = request.getSessionAttr("edu.rpi.sss.util.UtilAbstractAction.appVars");
    if ((o == null) || (!(o instanceof HashMap))) {
      o = new HashMap<String, String>();
      request.setSessionAttr("edu.rpi.sss.util.UtilAbstractAction.appVars", o);
    }

    return (HashMap<String, String>)o;
  }

  private static final int maxAppVars = 50; // Stop screwing around.

  /** Check for action setting a variable
   * We expect the request parameter to be of the form<br/>
   * setappvar=name(value) or <br/>
   * setappvar=name{value}<p>.
   *  Currently we're not escaping characters so if you want both right
   *  terminators in the value you're out of luck - actually we cheat a bit
   *  We just look at the last char and then look for that from the start.
   *
   * @param request  Needed to locate session
   * @param form     Action form
   * @return String  forward to here. null if no error found.
   * @throws Throwable
   */
  private String checkVarReq(final Request request,
                             final UtilActionForm form) throws Throwable {
    Collection<String> avs = request.getReqPars("setappvar");
    if (avs == null) {
      return null;
    }

    HashMap<String, String> appVars = getAppVars(request);

    for (String reqpar: avs) {
      int start;

      if (reqpar.endsWith("}")) {
        start = reqpar.indexOf('{');
      } else if (reqpar.endsWith(")")) {
        start = reqpar.indexOf('(');
      } else {
        return "badRequest";
      }

      if (start < 0) {
        return "badRequest";
      }

      String varName = reqpar.substring(0, start);
      String varVal = reqpar.substring(start + 1, reqpar.length() - 1);

      if (varVal.length() == 0) {
        varVal = null;
      }

      if (!setAppVar(varName, varVal, appVars)) {
        return "badRequest";
      }
    }

    form.setAppVarsTbl(appVars);

    return null;
  }

  /** Called to set an application variable to a value
   *
   * @param   name     name of variable
   * @param   val      new value of variable - null means remove.
   * @param appVars
   * @return  boolean  True if ok - false for too many vars
   */
  public boolean setAppVar(final String name, final String val,
                           final HashMap<String, String> appVars) {
    if (val == null) {
      appVars.remove(name);
      return true;
    }

    if (appVars.size() > maxAppVars) {
      return false;
    }

    appVars.put(name, val);
    return true;
  }

  /* ====================================================================
   *               Forward methods
   * ==================================================================== */

  /** Check for action forwarding
   * We expect the request parameter to be of the form<br/>
   * forward=name<p>.
   *
   * @param request  Needed to locate session
   * @return String  forward to here. null if no forward found.
   * @throws Throwable
   */
  private String checkForwardto(final HttpServletRequest request) throws Throwable {
    String reqpar = request.getParameter("forwardto");
    return reqpar;
  }

  /* ====================================================================
   *               Confirmation id methods
   * ==================================================================== */

  /** Check for a confirmation id. This is a random string embedded
   * in some requests to confirm that the incoming request came from a page
   * we generated. Not all pages will have such an id but if we do it must
   * match.
   *
   * We expect the request parameter to be of the form<br/>
   * confirmationid=id<p>.
   *
   * @param request  Needed to locate session
   * @param form
   * @return String  forward to here on error. null for OK.
   * @throws Throwable
   */
  protected String checkConfirmationId(final HttpServletRequest request,
                                       final UtilActionForm form)
          throws Throwable {
    String reqpar = request.getParameter("confirmationid");

    if (reqpar == null) {
      return null;
    }

    if (!reqpar.equals(form.getConfirmationId())) {
      return "badConformationId";
    }

    return null;
  }

  /** Require a confirmation id. This is a random string embedded
   * in some requests to confirm that the incoming request came from a page
   * we generated. Not all pages will have such an id but if we do it must
   * match.
   *
   * We expect the request parameter to be of the form<br/>
   * confirmationid=id<p>.
   *
   * @param request  Needed to locate session
   * @param form
   * @return String  forward to here on error. null for OK.
   * @throws Throwable
   */
  protected String requireConfirmationId(final HttpServletRequest request,
                                         final UtilActionForm form)
          throws Throwable {
    String reqpar = request.getParameter("confirmationid");

    if (reqpar == null) {
      return "missingConformationId";
    }

    if (!reqpar.equals(form.getConfirmationId())) {
      return "badConformationId";
    }

    return null;
  }

  /* ====================================================================
   *               Presentation state methods
   * ==================================================================== */

  /**
   * @param request
   */
  public void doPresentation(final Request request) throws Throwable {
    PresentationState ps = getPresentationState(request);

    if (ps == null) {
      if (debug()) {
        debug("No presentation state");
      }
      return;
    }

    if (debug()) {
      debug("Set presentation state");
    }

    HttpServletRequest req = request.getRequest();

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
  public PresentationState getPresentationState(final Request request) throws Throwable {
    String attrName = getPresentationAttrName();

    if ((attrName == null) || (attrName.equals("NONE"))) {
      return null;
    }

    Object o = request.getSessionAttr(attrName);
    PresentationState ps;

    if ((o == null) || (!(o instanceof PresentationState))) {
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
                                       PresentationState ps) {
    UtilActionForm form = request.getForm();

    ps.setBrowserType(form.getBrowserType());

    try {
      ps.setNoXSLTSticky(StrutsUtil.getProperty(messages,
                                                "edu.rpi.sss.util.action.noxslt",
                                                "no").equals("yes"));
    } catch (Throwable t) {
      t.printStackTrace();
    }
  }

  /* ==================================================================
                Various utility methods
     ================================================================== */

  /* * Set the value of a named session attribute.
   *
   * @param request     Needed to locate session
   * @param attrName    Name of the attribute
   * @param val         Object
   * /
  public void setSessionAttr(HttpServletRequest request,
                             String attrName,
                             Object val) {
    HttpSession sess = request.getSession(false);

    if (sess == null) {
      return;
    }

    sess.setAttribute(attrName, val);
  }

  /* * Return the value of a named session attribute.
   *
   * @param request     Needed to locate session
   * @param attrName    Name of the attribute
   * @return Object     Attribute value or null
   * /
  public Object getSessionAttr(HttpServletRequest request,
                               String attrName) {
    HttpSession sess = request.getSession(false);

    if (sess == null) {
      return null;
    }

    return sess.getAttribute(attrName);
  } */

  /** Return the value of a required named resource.
   *
   * @param resName     Name of the property
   * @return String     Resource value or null
   * @throws Throwable
   */
  public String getReqRes(final String resName) throws Throwable {
    return StrutsUtil.getReqProperty(messages, resName);
  }

  /**
   * @param req
   * @return boolean true for portlet
   */
  public boolean isPortletRequest(final HttpServletRequest req) {
    // JSR 168 requires this attribute be present
    return req.getAttribute("javax.portlet.request") != null;
  }

  /** Get a request parameter stripped of white space. Return null for zero
   * length.
   *
   * @param req
   * @param name    name of parameter
   * @return  String   value
   * @throws Throwable
   */
  protected String getReqPar(final HttpServletRequest req, final String name) throws Throwable {
    return Util.checkNull(req.getParameter(name));
  }

  /** Get a multi-valued request parameter stripped of white space.
   * Return null for zero length.
   *
   * @param req
   * @param name    name of parameter
   * @return  Collection<String> or null
   * @throws Throwable
   */
  protected Collection<String> getReqPars(final HttpServletRequest req,
                                          final String name) throws Throwable {
    String[] s = req.getParameterValues(name);
    ArrayList<String> res = null;

    if ((s == null) || (s.length == 0)) {
      return null;
    }

    for (String par: s) {
      par = Util.checkNull(par);
      if (par != null) {
        if (res == null) {
          res = new ArrayList<String>();
        }

        res.add(par);
      }
    }

    return res;
  }

  /* * Get an Integer request parameter or null.
   *
   * @param req
   * @param name    name of parameter
   * @return  Integer   value or null
   * @throws Throwable
   * /
  protected Integer getIntReqPar(HttpServletRequest req,
                                 String name) throws Throwable {
    String reqpar = getReqPar(req, name);

    if (reqpar == null) {
      return null;
    }

    return Integer.valueOf(reqpar);
  }

  /* * Get an Integer request parameter or null. Emit error for non-null and
   * non integer
   *
   * @param req
   * @param name    name of parameter
   * @param err     name of parameter
   * @param errProp
   * @return  Integer   value or null
   * @throws Throwable
   * /
  protected Integer getIntReqPar(HttpServletRequest req,
                                 String name,
                                 MessageEmit err,
                                 String errProp) throws Throwable {
    String reqpar = getReqPar(req, name);

    if (reqpar == null) {
      return null;
    }

    try {
      return Integer.valueOf(reqpar);
    } catch (Throwable t) {
      err.emit(errProp, reqpar);
      return null;
    }
  }

  /* * Get an integer valued request parameter.
   *
   * @param req
   * @param name    name of parameter
   * @param defaultVal
   * @return  int   value
   * @throws Throwable
   * /
  protected int getIntReqPar(HttpServletRequest req, String name,
                             int defaultVal) throws Throwable {
    String reqpar = req.getParameter(name);

    if (reqpar == null) {
      return defaultVal;
    }

    try {
      return Integer.parseInt(reqpar);
    } catch (Throwable t) {
      return defaultVal; // XXX exception?
    }
  }

  /* * Get a Long request parameter or null.
   *
   * @param req
   * @param name    name of parameter
   * @return  Long   value or null
   * @throws Throwable
   * /
  protected Long getLongReqPar(HttpServletRequest req,
                               String name) throws Throwable {
    String reqpar = getReqPar(req, name);

    if (reqpar == null) {
      return null;
    }

    return Long.valueOf(reqpar);
  }

  /* * Get an long valued request parameter.
   *
   * @param req
   * @param name    name of parameter
   * @param defaultVal
   * @return  long  value
   * @throws Throwable
   * /
  protected long getLongReqPar(HttpServletRequest req, String name,
                             long defaultVal) throws Throwable {
    String reqpar = req.getParameter(name);

    if (reqpar == null) {
      return defaultVal;
    }

    try {
      return Long.parseLong(reqpar);
    } catch (Throwable t) {
      return defaultVal; // XXX exception?
    }
  }

  /* * Get a boolean valued request parameter.
   *
   * @param req
   * @param name    name of parameter
   * @return  Boolean   value or null for absent parameter
   * @throws Throwable
   * /
  protected Boolean getBooleanReqPar(HttpServletRequest req, String name)
               throws Throwable {
    String reqpar = req.getParameter(name);

    if (reqpar == null) {
      return null;
    }

    try {
      return Boolean.valueOf(reqpar);
    } catch (Throwable t) {
      return null; // XXX exception?
    }
  }

  /* * Get a boolean valued request parameter giving a default value.
   *
   * @param req
   * @param name    name of parameter
   * @param defVal default value for absent parameter
   * @return  boolean   value
   * @throws Throwable
   * /
  protected boolean getBooleanReqPar(HttpServletRequest req, String name,
                                     boolean defVal) throws Throwable {
    boolean val = defVal;
    Boolean valB = getBooleanReqPar(req, name);
    if (valB != null) {
      val = valB.booleanValue();
    }

    return val;
  }
  */

  /* ==================================================================
                Private methods
     ================================================================== */

  /** Get the error object. If we haven't already got one and
   * getErrorObjAttrName returns non-null create one and implant it in
   * the session.
   *
   * @param request  Needed to locate session
   * @param messages Resources
   * @return ErrorEmitSvlt
   */
  private ErrorEmitSvlt getErrorObj(final HttpServletRequest request,
                                    final MessageResources messages) {
    return (ErrorEmitSvlt)StrutsUtil.getErrorObj(getId(), this,
                                                 request,
                                                 messages,
                                                 getErrorObjAttrName(),
                                                 getErrorObjErrProp(),
                                                 noActionErrors,
                                                 clearMessages());
  }

  /** Get the message object. If we haven't already got one and
   * getMessageObjAttrName returns non-null create one and implant it in
   * the session.
   *
   * @param request  Needed to locate session
   * @param messages Resources
   * @return MessageEmitSvlt
   */
  private MessageEmitSvlt getMessageObj(final HttpServletRequest request,
                                        final MessageResources messages) {
    return (MessageEmitSvlt)StrutsUtil
            .getMessageObj(getId(), this, request,
                           messages,
                           getMessageObjAttrName(),
                           getErrorObjErrProp(),
                           clearMessages());
  }

  /**
   * @param req
   */
  public void dumpRequest(final HttpServletRequest req) {
    try {
      Enumeration names = req.getParameterNames();

      String title = "Request parameters";

      debug(title + " - global info and uris");
      debug("getRequestURI = " + req.getRequestURI());
      debug("getRemoteUser = " + req.getRemoteUser());
      debug("getRequestedSessionId = " + req.getRequestedSessionId());
      debug("HttpUtils.getRequestURL(req) = " + req.getRequestURL());
      debug("query=" + req.getQueryString());
      debug("contentlen=" + req.getContentLength());
      debug("request=" + req);
      debug("host=" + req.getHeader("host"));
      debug("parameters:");

      debug(title);

      while (names.hasMoreElements()) {
        String key = (String)names.nextElement();
        String[] vals = req.getParameterValues(key);
        for (String val: vals) {
          debug("  " + key + " = \"" + val + "\"");
        }
      }
    } catch (final Throwable ignored) {
    }
  }

  /* ====================================================================
   *                   Logged methods
   * ==================================================================== */

  private BwLogger logger = new BwLogger();

  @Override
  public BwLogger getLogger() {
    if ((logger.getLoggedClass() == null) && (logger.getLoggedName() == null)) {
      logger.setLoggedClass(getClass());
    }

    return logger;
  }
}
