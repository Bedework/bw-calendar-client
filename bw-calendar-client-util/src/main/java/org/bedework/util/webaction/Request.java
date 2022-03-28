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

import org.bedework.util.servlet.MessageEmit;
import org.bedework.util.servlet.ReqUtil;
import org.bedework.util.struts.UtilActionForm;

import org.apache.struts.action.ActionMapping;

import java.util.Collection;
import java.util.HashMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/** Class to handle the incoming request.
 *
 * @author Mike Douglass
 */
public class Request extends ReqUtil {
  protected UtilActionForm form;
  protected ActionMapping mapping;

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

  /** action mapping keys - note the "=" */
  public static final String actionTypeKey = "actionType=";
  /** */
  public static final String conversationKey = "conversation=";
  /** */
  public static final String refreshIntervalKey = refreshIntervalReqPar + "=";
  /** */
  public static final String refreshActionKey = "refaction=";
    /** */
    public static final String moduleNameKey = "mdl=";

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

  /** May be specified as an action parameter or overriddem by the
   * request parameter.
   */
  protected String moduleName;

  /**
   * @param request the http request
   * @param response the response
   * @param form form object
   * @param mapping  and the mapping
   */
  public Request(final HttpServletRequest request,
                 final HttpServletResponse response,
                 final UtilActionForm form,
                 final ActionMapping mapping) {
    super(request, response);
    this.form = form;
    this.mapping = mapping;

    final String at = getStringActionPar(actionTypeKey);
    if (at != null) {
      for (int ati = 0; ati < actionTypes.length; ati++) {
        if (Request.actionTypes[ati].equals(at)) {
          actionType = ati;
          break;
        }
      }
    }

    final String convType = getStringActionPar(conversationKey);
    if (convType != null) {
      for (int ati = 0; ati < Request.conversationTypes.length; ati++) {
        if (Request.conversationTypes[ati].equals(convType)) {
          conversationType = ati;
          break;
        }
      }
    }

    moduleName = getStringActionPar(Request.moduleNameKey); // <-- Note it's key for the "="
  }

  /**
   * @return UtilActionForm
   */
  public UtilActionForm getForm() {
    return form;
  }

  /**
   * @return ActionMapping
   */
  public ActionMapping getMapping() {
    return mapping;
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
    return mapping.getPath();
  }

  /**
   * @return the action parameter if any.
   */
  public String getActionParameter() {
    return mapping.getParameter();
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

  /**
   * @param name of parameter
   * @return value of given parameter or null
   */
  public Integer getIntActionPar(final String name) {
    return getIntActionPar(name, getActionParameter());
  }

  /**
   * @param name of parameter
   * @return value of given parameter or null
   */
  public String getStringActionPar(final String name) {
    return getStringActionPar(name, getActionParameter());
  }

  protected Integer getIntActionPar(final String name,
                                    final String par) {
    if (par == null) {
      return null;
    }

    try {
      int pos = par.indexOf(name);
      if (pos < 0) {
        return null;
      }

      pos += name.length();
      int epos = par.indexOf(";", pos);
      if (epos < 0) {
        epos = par.length();
      }

      return Integer.valueOf(par.substring(pos, epos));
    } catch (final Throwable t) {
      form.getErr().emit("edu.rpi.bad.actionparameter", par);
      return null;
    }
  }

  public String getStringActionPar(final String name,
                                   final String par) {
    if (par == null) {
      return null;
    }

    try {
      int pos = par.indexOf(name);
      if (pos < 0) {
        return null;
      }

      pos += name.length();
      int epos = par.indexOf(";", pos);
      if (epos < 0) {
        epos = par.length();
      }

      return par.substring(pos, epos);
    } catch (final Throwable t) {
      form.getErr().emit("edu.rpi.bad.actionparameter", par);
      return null;
    }
  }

  /* ====================================================================
   *                  Application variable methods
   * ==================================================================== */

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
}