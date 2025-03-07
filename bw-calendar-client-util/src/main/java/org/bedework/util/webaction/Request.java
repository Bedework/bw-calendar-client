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
package org.bedework.util.webaction;

import org.bedework.util.logging.BwLogger;
import org.bedework.util.logging.Logged;
import org.bedework.util.misc.Util;
import org.bedework.util.servlet.HttpServletUtils;
import org.bedework.util.servlet.MessageEmit;
import org.bedework.util.servlet.ReqUtil;
import org.bedework.util.servlet.filters.PresentationState;

import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.bedework.util.servlet.ActionTypes.actionTypes;
import static org.bedework.util.servlet.ConversationTypes.conversationTypes;

/** Class to handle the incoming request.
 *
 * @author Mike Douglass
 */
public class Request extends ReqUtil implements Logged {
  protected WebActionForm form;
  protected final Map<String, String> params;
  private final String actionPath;

  public final static String appvarsAttrName =
          "org.bedework.client.appvars";

  public final static String urlPrefixAttrName =
          "org.bedework.client.urlprefix";

  protected int actionType;

  protected boolean noCache;

  /* request parameter names */

  /** */
  public static final String refreshIntervalReqPar = "refinterval";

  /** action mapping keys */
  public static final String actionTypeKey = "actionType";
  /** */
  public static final String conversationKey = "conversation";
  /** */
  public static final String refreshIntervalKey = refreshIntervalReqPar;
  /** */
  public static final String refreshActionKey = "refaction";
    /** */
    public static final String moduleNameKey = "mdl";

  protected int conversationType;

  /** Request parameter to specify which module */
  public final static String moduleNamePar = "mdl";

  public final String requestLogout = "logout";

  /** Forward to here for logged out
   */
  public final String forwardLoggedOut = "loggedOut";

  /** May be specified as an action parameter or overriddem by the
   * request parameter.
   */
  protected String moduleName;

  private final String errorForward;

  private final ErrorEmitSvlt err;
  private final MessageEmitSvlt msg;

  /**
   * @param request the http request
   * @param response the response
   * @param params action parameters
   * @param actionPath from mapping
   * @param form form object
   */
  public Request(final HttpServletRequest request,
                 final HttpServletResponse response,
                 final Map<String, String> params,
                 final String actionPath,
                 final ErrorEmitSvlt err,
                 final MessageEmitSvlt msg,
                 final WebActionForm form) {
    super(request, response);
    this.params = params;
    this.actionPath = actionPath;
    this.err = err;
    this.msg = msg;
    this.form = form;

    final String at = params.get(actionTypeKey);
    if (at != null) {
      for (int ati = 0; ati < actionTypes.length; ati++) {
        if (actionTypes[ati].equals(at)) {
          actionType = ati;
          break;
        }
      }
    }

    final String convType = params.get(conversationKey);
    if (convType != null) {
      for (int ati = 0; ati < conversationTypes.length; ati++) {
        if (conversationTypes[ati].equals(convType)) {
          conversationType = ati;
          break;
        }
      }
    }

    moduleName = getStringActionPar(moduleNameKey);

    if (debug()) {
      dumpRequest();
    }

    setRequestAttr(urlPrefixAttrName,
                   HttpServletUtils.getURLPrefix(request));

    final HttpSession session = request.getSession();
    final ServletContext sc = session.getServletContext();

    errorForward = sc.getInitParameter("errorForward");
    if (errorForward == null) {
      throw new RuntimeException("errorForward not set");
    }

    checkNocache();

    getGlobals().reset(this);
  }

  public WebGlobals getGlobals() {
    var globals = (WebGlobals)getSessionAttr(WebGlobals.webGlobalsAttrName);

    if (globals == null) {
      globals = new WebGlobals();
      setSessionAttr(WebGlobals.webGlobalsAttrName, globals);
    }

    return globals;
  }

  public Map<String, String> getParams() {
    return params;
  }

  /** Returns the scheme + host + port part of the url together with the
   *  path up to the servlet path. This allows us to append a new action to
   *  the end.
   *
   *  @return  String   the URL prefix
   */
  public String getUrlPrefix() {
    return (String)getRequestAttr(urlPrefixAttrName);
  }

  public String getErrorForward() {
    return errorForward;
  }

  /**
   * @return UtilActionForm
   */
  public WebActionForm getForm() {
    return form;
  }

  /** Emit message with given throwable
   *
   * @param val - exception object
   */
  public void error(final Throwable val) {
    getErr().emit(val);
  }

  /** Emit message with given property name
   *
   * @param pname - property name
   */
  public void error(final String pname) {
    getErr().emit(pname);
  }

  /** Emit message with given property name and Object value
   *
   * @param pname - property name
   * @param o - object to display
   */
  public void error(final String pname,
                    final Object o) {
    getErr().emit(pname, o);
  }

  /** Emit message with given property name and Object values
   *
   * @param pname - property name
   * @param o1 - object to display
   * @param o2 - object to display
   */
  public void error(final String pname,
                    final Object o1, final Object o2) {
    getErr().emit(pname, o1, o2);
  }

  /**
   * @return MessageEmit
   */
  public MessageEmit getErr() {
    return err;
  }

  /** Emit message with given property name
   *
   * @param pname - property name
   */
  public void message(final String pname) {
    getMsg().emit(pname);
  }

  /** Emit message with given property name and Object value
   *
   * @param pname - property name
   * @param o - object to display
   */
  public void message(final String pname,
                      final Object o) {
    getMsg().emit(pname, o);
  }

  /**
   * @return MessageEmit
   */
  public MessageEmit getMsg() {
    return msg;
  }

  /**
   * @return boolean
   */
  public boolean getErrorsEmitted() {
    return errFlag || getErr().messagesEmitted();
  }

  /**
   * @return int
   */
  public int getActionType() {
    return actionType;
  }

  /**
   * @return the part of the URL that identifies the action.
   */
  public String getActionPath() {
    return actionPath;
  }

  /**
   * @return int
   */
  public int getConversationType() {
    return conversationType;
  }

  /**
   * @return String
   */
  public String getModuleName() {
    String nm = getReqPar(moduleNamePar);

    if (nm == null) {
      nm = moduleName;
    }

    return nm;
  }

  /** Check for action forwarding
   * We expect the request parameter to be of the form<br/>
   * forward=name<p>.
   *
   * @return String  forward to here. null if no forward found.
   */
  public String checkForwardto() {
    return request.getParameter("forwardto");
  }

  public Integer getRefreshInt() {
    try {
      final Integer res = super.getIntReqPar(refreshIntervalReqPar);
      if (res != null) {
        return res;
      }
    } catch (final Throwable ignored) {
    }

    return getIntActionPar(refreshIntervalKey);
  }

  public String getRefreshAction() {
    return getStringActionPar(refreshActionKey);
  }

  /**
   */
  public void dumpRequest() {
    final HttpServletRequest req = getRequest();

    final String title = "Request parameters";

    debug(title + " - global info and uris");
    debug("getRequestURI = " + req.getRequestURI());
    debug("getRemoteUser = " + req.getRemoteUser());
    if (req.getUserPrincipal() == null) {
      debug("getUserPrincipal = null");
    } else {
      debug("getUserPrincipal.name = " + req.getUserPrincipal()
                                            .getName());
    }
    debug("getRequestedSessionId = " + req.getRequestedSessionId());
    debug("HttpUtils.getRequestURL(req) = " + req.getRequestURL());
    debug("query=" + req.getQueryString());
    debug("contentlen=" + req.getContentLength());
    debug("request=" + req);
    debug("host=" + req.getHeader("host"));
    debug("parameters:");

    debug(title);

    final Enumeration<String> names = req.getParameterNames();
    final List<String> snames = Collections.list(names);
    Collections.sort(snames);

    for (final String key: snames) {
      final String[] vals = req.getParameterValues(key);
      for (final String val: vals) {
        debug("  " + key + " = \"" + val + "\"");
      }
    }
  }

  /** Get an Integer request parameter or null. Emit error for non-null and
   * non integer
   *
   * @param name    name of parameter
   * @param errProp error to emit
   * @return  Integer   value or null
   */
  public Integer getIntReqPar(final String name,
                              final String errProp) {
    try {
      return super.getIntReqPar(name);
    } catch (final Throwable t) {
      getErr().emit(errProp, getReqPar(name));
      return null;
    }
  }

  public Integer getIntActionPar(final String name) {
    final String par = params.get(name);
    if (par == null) {
      return null;
    }

    try {
      return Integer.valueOf(par);
    } catch (final Throwable t) {
      getErr().emit("org.bedework.bad.actionparameter", par);
      return null;
    }
  }

  /**
   * @param name of parameter
   * @return value of given parameter or null
   */
  public String getStringActionPar(final String name) {
    return params.get(name);
  }

  /* We handle our own nocache headers but, if we are running
   * with nocache, we need to be able to disable it for the
   * occasional response.
   *
   * <p>This gets around an IE problem when attempting to
   * deliver files. IE requires caching on, or it is unable to
   * locate the file it is supposed to be delivering.
   */
  public void checkNocache() {
    String reqpar = getReqPar("nocacheSticky");

    if (reqpar != null) {
      /* (re)set the default */
      setNocache(reqpar.equals("yes"));
    }

    /* Look for a one-shot setting
     */

    reqpar = getReqPar("nocache");

    if ((reqpar == null) && (!getNocache())) {
      return;
    }

    /* If we got a request parameter it overrides the default
     */
    boolean nc = getNocache();

    if (reqpar != null) {
      nc = reqpar.equals("yes");
    }

    if (nc) {
      response.setHeader("Pragma", "No-cache");
      //response.setHeader("Cache-Control", "no-cache");
      response.setHeader("Cache-Control", "no-store, no-cache, must-revalidate");
      response.setDateHeader("Expires", 1);
    }
  }

  public void setNocache(final boolean val) {
    noCache = val;
  }

  public boolean getNocache() {
    return noCache;
  }

  public void setContentName(final String val) {
    getPresentationState().setContentName(val);
  }

  /**
   * @return contentName if present or set
   */
  public String getContentName() {
    return getPresentationState().getContentName();
  }

  /** Check for logout request or force logout if user changed.
   *
   * @return null for continue, forwardLoggedOut to end session.
   */
  public String checkLogOut() {
    final String reqUser = HttpServletUtils.remoteUser(request);

    final boolean forceLogout =
            !Util.equalsString(reqUser,
                               getGlobals().getCurrentUser());

    if (forceLogout || (request.getParameter(requestLogout) != null)) {
      final HttpSession sess = request.getSession(false);

      if ((sess != null) && logOutCleanup()) {
        sess.invalidate();
      }
      return forwardLoggedOut;
    }

    return null;
  }

  public String getCurrentUser() {
    return getGlobals().getCurrentUser();
  }

  public void clearCurrentUser() {
    getGlobals().currentUser = null;
  }

  /** Clean up - we're about to logout
   *
   * @return boolean true for OK to log out. False - not allowed - ignore it.
   */
  protected boolean logOutCleanup() {
    return true;
  }

  /* ========================================================
   *                  Application variable methods
   * ======================================================== */

  /**
   * @return app vars
   */
  @SuppressWarnings("unchecked")
  public HashMap<String, String> getAppVars() {
    Object o = getSessionAttr(appvarsAttrName);
    if (!(o instanceof HashMap)) {
      o = new HashMap<String, String>();
      setSessionAttr(appvarsAttrName, o);
    }

    return (HashMap<String, String>)o;
  }

  private static final int maxAppVars = 50; // Stop screwing around.

  /** Check for action setting a variable
   * We expect the request parameter to be of the form<br/>
   * setappvar=name(value) or <br/>
   * setappvar=name{value}<p>.
   *  Currently, we're not escaping characters so if you want both right
   *  terminators in the value you're out of luck - actually we cheat a bit
   *  We just look at the last char and then look for that from the start.
   *
   * @return String  forward to here. null if no error found.
   */
  public String checkVarReq() {
    final Collection<String> avs = getReqPars("setappvar");
    if (avs == null) {
      return null;
    }

    final HashMap<String, String> appVars = getAppVars();

    for (final String reqpar: avs) {
      final int start;

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

      final String varName = reqpar.substring(0, start);
      String varVal = reqpar.substring(start + 1, reqpar.length() - 1);

      if (varVal.isEmpty()) {
        varVal = null;
      }

      if (!setAppVar(varName, varVal, appVars)) {
        return "badRequest";
      }
    }

    return null;
  }

  /** Called to set an application variable to a value
   *
   * @param   name     name of variable
   * @param   val      new value of variable - null means remove.
   * @param appVars    application variables map
   * @return  boolean  True if ok - false for too many vars
   */
  public boolean setAppVar(final String name,
                           final String val,
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

  /* ========================================================
   *               Presentation state methods
   * ======================================================== */

  /**
   * @return PresentationState
   */
  public PresentationState getPresentationState() {
    final String attrName = PresentationState.presentationAttrName;

    final Object o = getRequestAttr(attrName);
    final PresentationState ps;

    if (!(o instanceof PresentationState)) {
      ps = new PresentationState().reinit(getRequest());
    } else {
      ps = (PresentationState)o;
    }

    setRequestAttr(attrName, ps);

    return  ps;
  }

  /* ============================================================
   *                   Logged methods
   * ============================================================ */

  private final BwLogger logger = new BwLogger();

  @Override
  public BwLogger getLogger() {
    if ((logger.getLoggedClass() == null) && (logger.getLoggedName() == null)) {
      logger.setLoggedClass(getClass());
    }

    return logger;
  }
}
