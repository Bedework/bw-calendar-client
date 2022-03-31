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

import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

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

  /** */
  public final static int actionTypeUnknown = 0;
  /** */
  public final static int actionTypeRender = 1;
  /** */
  public final static int actionTypeAction = 2;
  /** */
  public final static int actionTypeResource = 3;

  /** */
  public final static String[] actionTypes = {"unknown",
                                              "render",
                                              "action",
                                              "resource"};

  protected int actionType;

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

  /** In the absence of a conversation parameter we assume that a conversation
   * starts with actionType=action and ends with actionType=render.
   *
   * Conversations are related to the persistence framework and allow us to keep
   * a persistence engine session running until the sequence of actions is
   * completed.
   */
  public final static int conversationTypeUnknown = 0;

  /** start of a multi-request conversation */
  public final static int conversationTypeStart = 1;

  /** part-way through a multi-request conversation */
  public final static int conversationTypeContinue = 2;

  /** end of a multi-request conversation */
  public final static int conversationTypeEnd = 3;

  /** if a conversation is started, end it on entry with no
   * processing of changes. Start a new conversation which we will end on exit.
   */
  public final static int conversationTypeOnly = 4;

  /** If a conversation is already started on entry, process changes and end it.
   * Start a new conversation which we will end on exit.
   */
  public final static int conversationTypeProcessAndOnly = 5;

  /** */
  public final static String[] conversationTypes = {"unknown",
                                                    "start",
                                                    "continue",
                                                    "end",
                                                    "only",
                                                    "processAndOnly"};

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
                 final WebActionForm form) {
    super(request, response);
    this.params = params;
    this.actionPath = actionPath;
    this.form = form;

    final String at = params.get(actionTypeKey);
    if (at != null) {
      for (int ati = 0; ati < actionTypes.length; ati++) {
        if (Request.actionTypes[ati].equals(at)) {
          actionType = ati;
          break;
        }
      }
    }

    final String convType = params.get(conversationKey);
    if (convType != null) {
      for (int ati = 0; ati < Request.conversationTypes.length; ati++) {
        if (Request.conversationTypes[ati].equals(convType)) {
          conversationType = ati;
          break;
        }
      }
    }

    moduleName = getStringActionPar(moduleNameKey);
  }

  public Map<String, String> getParams() {
    return params;
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

  /**
   * @return MessageEmit
   */
  public MessageEmit getErr() {
    errFlag = true;
    return form.getErr();
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
    return form.getMsg();
  }

  /**
   * @return boolean
   */
  public boolean getErrorsEmitted() {
    return errFlag || form.getErrorsEmitted();
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
    debug("getUserPrincipal.name = " + req.getUserPrincipal().getName());
    debug("getRequestedSessionId = " + req.getRequestedSessionId());
    debug("HttpUtils.getRequestURL(req) = " + req.getRequestURL());
    debug("query=" + req.getQueryString());
    debug("contentlen=" + req.getContentLength());
    debug("request=" + req);
    debug("host=" + req.getHeader("host"));
    debug("parameters:");

    debug(title);

    final Enumeration<String> names = req.getParameterNames();
    while (names.hasMoreElements()) {
      final String key = names.nextElement();
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
   * @throws Throwable on error
   */
  public Integer getIntReqPar(final String name,
                              final String errProp) throws Throwable {
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

  /* We handle our own nocache headers but, if we are running with nocache,
   * we need to be able to disable it for the occasional response.
   *
   * <p>This gets around an IE problem when attempting to deliver files.
   * IE requires caching on or it is unable to locate the file it is
   * supposed to be delivering.
   */
  public void checkNocache() {
    String reqpar = getReqPar("nocacheSticky");

    if (reqpar != null) {
      /* (re)set the default */
      form.setNocache(reqpar.equals("yes"));
    }

    /* Look for a one-shot setting
     */

    reqpar = getReqPar("nocache");

    if ((reqpar == null) && (!form.getNocache())) {
      return;
    }

    /* If we got a request parameter it overrides the default
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

  /** Check for logout request or force logout if user changed.
   *
   * @return null for continue, forwardLoggedOut to end session.
   */
  public String checkLogOut() {
    final String reqUser = HttpServletUtils.remoteUser(request);

    final boolean forceLogout =
            !Util.equalsString(reqUser, form.getCurrentUser());

    if (forceLogout || (request.getParameter(requestLogout) != null)) {
      final HttpSession sess = request.getSession(false);

      if ((sess != null) && logOutCleanup()) {
        sess.invalidate();
      }
      return forwardLoggedOut;
    }

    return null;
  }

  /** Clean up - we're about to logout
   *
   * @return boolean true for OK to log out. False - not allowed - ignore it.
   */
  protected boolean logOutCleanup() {
    return true;
  }

  /* ==============================================================
   *                  Application variable methods
   * ============================================================== */

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

      if (varVal.length() == 0) {
        varVal = null;
      }

      if (!setAppVar(varName, varVal, appVars)) {
        return "badRequest";
      }
    }

    getForm().setAppVarsTbl(appVars);

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

  /* ====================================================================
   *               Presentation state methods
   * ==================================================================== */

  /**
   */
  public void doPresentation() {
    final PresentationState ps = getPresentationState();

    if (ps == null) {
      if (debug()) {
        debug("No presentation state");
      }
      return;
    }

    if (debug()) {
      debug("Set presentation state");
    }

    final HttpServletRequest req = getRequest();

    ps.checkBrowserType(req);
    ps.checkContentType(req);
    ps.checkContentName(req);
    ps.checkNoXSLT(req);
    ps.checkRefreshXslt(req);
    ps.checkSkinName(req);

    setRequestAttr(PresentationState.presentationAttrName, ps);

    if (debug()) {
      ps.debugDump("action");
    }
  }

  /**
   * @return PresentationState
   */
  public PresentationState getPresentationState() {
    final String attrName = PresentationState.presentationAttrName;

    final Object o = getSessionAttr(attrName);
    final PresentationState ps;

    if (!(o instanceof PresentationState)) {
      ps = new PresentationState();
      initPresentationState(ps);

      setSessionAttr(attrName, ps);
    } else {
      ps = (PresentationState)o;
    }

    return  ps;
  }

  /**
   * @param ps PresentationState
   */
  protected void initPresentationState(final PresentationState ps) {

    ps.setBrowserType(getForm().getBrowserType());
    ps.setNoXSLTSticky(false);
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
