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
import org.bedework.util.webaction.ErrorEmitSvlt;
import org.bedework.util.webaction.MessageEmitSvlt;
import org.bedework.util.webaction.Request;
import org.bedework.util.webaction.WebActionForm;

import com.opensymphony.xwork2.ActionSupport;
import com.opensymphony.xwork2.config.entities.Parameterizable;
import org.apache.struts2.interceptor.ServletRequestAware;
import org.apache.struts2.interceptor.ServletResponseAware;
import org.apache.struts2.interceptor.SessionAware;

import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import static java.lang.String.format;

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
public abstract class UtilAbstractAction extends ActionSupport
         implements Parameterizable,
                    ServletRequestAware, ServletResponseAware,
                    SessionAware, HttpAppLogger, Logged {
  private transient String logPrefix;

  private HttpServletRequest request;
  private HttpServletResponse response;

  private Map<String, Object> session;
  private Map<String, String> params;

  protected UtilActionForm form;

  @Override
  public void setServletResponse(final HttpServletResponse val) {
    response = val;
  }

  public HttpServletResponse getServletResponse() {
    return response;
  }

  //protected boolean isPortlet;

  @Override
  public void setServletRequest(final HttpServletRequest val) {
    request = val;
  }

  public HttpServletRequest getServletRequest() {
    return request;
  }

  public void setSession(final Map<String, Object> val) {
    session = val;
    form = (UtilActionForm)session.get("calForm");
    if (form == null) {
      if (debug()) {
        debug("No form in session - creating " +
                      getFormClass(request));
      }
      form = (UtilActionForm)Util.getObject(
              getFormClass(request),
              UtilActionForm.class);
      session.put("calForm", form);
    } else if (debug()) {
      debug("Found form in session with class " +
                    form.getClass());
    }
  }

  public void addParam(final String name,
                       final String value) {
    if (debug()) {
      debug("addParam " + name + " = " + value);
    }
//    params.put(name, value);
  }

  public void setParams(final Map<String, String> val) {
    params = val;
    if (debug()) {
      dumpParams(params);
    }
  }

  public Map<String, String> getParams() {
    return params;
  }

  public UtilActionForm getForm() {
    return form;
  }

  /** This is the routine which does the work.
   *
   * @param request  Provide http request/response and form
   * @return String  forward name
   * @throws Throwable on fatal error
   */
  public abstract String performAction(Request request)
               throws Throwable;

  public String execute() {
    final ErrorEmitSvlt err;
    final MessageEmitSvlt msg;

    String forward;

    //isPortlet = isPortletRequest(request);

    try {
      err = ErrorEmitSvlt.getErrorObj(request, getId(),
                                      getErrorObjErrProp(),
                                      clearMessagesOnEntry());
      msg = MessageEmitSvlt.getMessageObj(request, getId(),
                                          getErrorObjErrProp(),
                                          clearMessagesOnEntry());
    } catch (final ErrorEmitSvlt.NoSessionException nse) {
      warn("No session exception");
      return "errpr";
    }

    /* Log the request - virtual domains can make it difficult to
       *  distinguish applications.
       */
    logRequest(request);

    if (debug()) {
      debug("entry");
      debug("================================");
      debug("================================");
    }

    try {
      final Request req =
              getRequest(request, response,
                         params,
                         request.getPathTranslated(),
                         err,
                         msg,
                         form);

      final String contentName = getContentName(req);

      if (contentName != null) {
        /* Indicate we have a file attachment with the given name
         */

        response.setHeader("Content-Disposition",
                           "Attachment; Filename=\"" + contentName + "\"");
      }

      /* ----------------------------------------------------------
         Everything is set up and ready to go. Execute something
         ---------------------------------------------------------- */

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
        err.emit("org.bedework.client.nullforward");
        forward = "error";
      } else if (forward.equals("FORWARD-NULL")) {
        forward = null;
      }

      if (debug()) {
        debug(format("exit to %s, %d errors %d messages",
                     forward,
                     err.getMsgList().size(),
                     msg.getMsgList().size()));
      }
    } catch (final Throwable t) {
      if (debug()) {
        error("Action exception: ", t);
      }

      err.emit(t);
      forward = "error";
    }

    return forward;
  }

  /**
   *
   * @param request the http request
   * @param response the response
   * @param params action parameters
   * @param actionPath from mapping
   * @param form form object
   * @return a Request object
   */
  protected Request getRequest(final HttpServletRequest request,
                               final HttpServletResponse response,
                               final Map<String, String> params,
                               final String actionPath,
                               final ErrorEmitSvlt err,
                               final MessageEmitSvlt msg,
                               final WebActionForm form) {
    return new Request(request, response, params, actionPath, err, msg, form);
  }

  /** Override this to get the contentName from different sources
   *
   * @param req the Request object
   * @return String name of content
   */
  public String getContentName(final Request req) {
    return req.getContentName();
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
  public boolean clearMessagesOnEntry() {
    return true;
  }

  /** Override to return a different name for the error exception property.
   * This must return non-null if getErrorObjAttrName returns a value.
   *
   * @return error exception property name
   */
  public String getErrorObjErrProp() {
    return "org.bedework.util.error.exc";
  }

  public String getFormClass(final HttpServletRequest request) {
    final HttpSession session = request.getSession();
    final ServletContext sc = session.getServletContext();

    return sc.getInitParameter("formClass");
  }

  public void setB(final String val) {
    // We use b=de as a dummay param to make url building easy
    if (!"de".equals(val)) {
      throw new RuntimeException("'b' is reserved as a bedework parameter");
    }
  }

  /* ============================================================
   *                 Request parameters
   * ============================================================ */

  public void setSetappvar(final String val) {
    // Handled elsewhere.
  }

  public void setSkinNameSticky(final String val) {
    // Handled elsewhere.
  }

  /* ============================================================
   *                 Params that show up as method calls
   * ============================================================ */

  public void setActionType(final String val) {
    // Handled by Request class.
  }

  public void setConversation(final String val) {
    // Handled by Request class.
  }

  public void setMdl(final String val) {
    // Handled by Request class.
  }

  public void setRefaction(final String val) {
    // Handled by Request class.
  }

  /* ============================================================
   *                        Log request
   * ============================================================ */

  @Override
  public String getLogPrefix(final HttpServletRequest request) {
    if (logPrefix == null) {
      final HttpSession session = request.getSession();
      final ServletContext sc = session.getServletContext();

      logPrefix = sc.getInitParameter("bwappname");
    }

    return logPrefix;
  }

  private void dumpParams(final Map<String, String> params) {
    debug("============== params ============");
    if (params == null) {
      return;
    }

    for (final var k: params.keySet()) {
      debug(k + ": " + params.get(k));
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
