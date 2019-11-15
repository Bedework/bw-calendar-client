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
import org.bedework.util.servlet.MessageEmit;

import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionMessage;
import org.apache.struts.util.MessageResources;

import java.io.Serializable;
import java.util.ArrayList;

/** This class allows error message generation in the servlet world.
 *
 * @author Mike Douglass douglm@rpi.edu
 * @version 1.0
 */
public class ErrorEmitSvlt implements MessageEmit {
  transient protected String id;
  transient protected Object caller;
  transient protected MessageResources messages;
  transient protected ActionErrors errors;
  transient protected String exceptionPname;

  /** We save the message property and the parameters in the following
   * class which we can return as an alternative to the struts message
   * generation.
   */
  @SuppressWarnings("unused")
  public class Msg implements Serializable {
    private ArrayList<Object> params = new ArrayList<>();
    private Object p1;
    private Object p2;
    private Object p3;

    private String msgId;
    protected MessageResources messages;

    /**
     * @param messages MessageResources object
     * @param msgId id
     */
    public Msg(final MessageResources messages,
               final String msgId) {
      this.messages = messages;
      this.msgId = msgId;
    }

    /**
     * @param messages MessageResources object
     * @param msgId id
     * @param o object to output
     */
    public Msg(final MessageResources messages,
               final String msgId, final Object o) {
      this.messages = messages;
      this.msgId = msgId;
      addParam(o);
      p1 = o;
    }

    /**
     * @param messages MessageResources object
     * @param msgId id
     * @param o1 object to output
     * @param o2 object to output
     */
    public Msg(final MessageResources messages,
               final String msgId, final Object o1, final Object o2) {
      this.messages = messages;
      this.msgId = msgId;
      addParam(o1);
      addParam(o2);
      p1 = o1;
      p2 = o2;
    }

    /**
     * @param messages MessageResources object
     * @param msgId id
     * @param o1 object to output
     * @param o2 object to output
     * @param o3 object to output
     */
    public Msg(final MessageResources messages,
               final String msgId, final Object o1, final Object o2, final Object o3) {
      this.messages = messages;
      this.msgId = msgId;
      addParam(o1);
      addParam(o2);
      addParam(o3);
      p1 = o1;
      p2 = o2;
      p3 = o3;
    }

    /**
     * @return String message id
     */
    public String getMsgId() {
      return msgId;
    }

    /**
     * @return params
     */
    public ArrayList getParams() {
      return params;
    }

    /**
     * @return expanded message
     */
    public String getMsg() {
      if (messages == null) {
        return "";
      }

      return messages.getMessage(msgId, p1, p2, p3);
    }

    private void addParam(final Object o) {
      if (o != null) {
        params.add(o);
      }
    }
  }

  protected ArrayList<Msg> msgList = new ArrayList<>();

  /**
   *
   */
  public ErrorEmitSvlt() {
  }

  /** Generation of errors in the servlet world means adding them to the
   *  errors object. We need to call this routine on every entry to the
   *  application
   *
   * @param id       An identifying name
   * @param caller   Used for log identification
   * @param messages Resources
   * @param errors   Error message will be appended on failure.
   * @param exceptionPname Property name for exceptions
   * @param clear true to clear list
   */
  public void reinit(final String id,
                     final Object caller,
                     final MessageResources messages,
                     final ActionErrors errors,
                     final String exceptionPname,
                     final boolean clear) {
    this.id = id;
    this.caller = caller;
    this.messages = messages;
    this.errors = errors;
    this.exceptionPname = exceptionPname;

    if (clear) {
      msgList.clear();
    }
  }

  /**
   * @return msg list
   */
  public ArrayList<Msg> getMsgList() {
    return msgList;
  }

  @Override
  public void emit(final String pname) {
    if (debug()) {
      debugMsg(pname, null, null);
    }

    msgList.add(new Msg(messages, pname));

    if ((messages == null) || !haveOutputObject()) {
      return;
    }

    try {
      errors.add(id, new ActionMessage(pname));
    } catch (Throwable t) {
      error(className() + ": exception adding Action message", t);
    }
  }

  @Override
  public void emit(final String pname, final int num) {
    if (debug()) {
      debugMsg(pname, "int", String.valueOf(num));
    }

    emit(pname, new Integer(num));
  }

  @Override
  public void setExceptionPname(final String pname) {
    exceptionPname = pname;
  }

  @Override
  public void emit(final Throwable t) {
    if (debug()) {
      debugMsg(exceptionPname, "Throwable", String.valueOf(t.getMessage()));
    }

    String msg = t.getMessage();
    if (msg == null) {
      msg = "<No-message>";
    }

    error(msg, t);

    emit(exceptionPname, t.getMessage());
  }

  @Override
  public void emit(final String pname, final Object o){
    if (debug()) {
      if (o == null) {
        debugMsg(pname, "null object", "null");
      } else {
        debugMsg(pname, o.getClass().getName(), String.valueOf(o));
      }
    }

    if (o == null) {
      msgList.add(new Msg(messages, pname));
    } else {
      msgList.add(new Msg(messages, pname, o));
    }

    if ((messages == null) || !haveOutputObject()) {
      return;
    }

    try {
      errors.add(id, new ActionMessage(pname, o));
    } catch (Throwable t) {
      error(className() + ": exception adding Action error", t);
    }
  }

  @Override
  public void emit(final String pname, final Object o1, final Object o2){
    if (debug()) {
      debugMsg(pname, "2objects",
               String.valueOf(o1) + "; " +
               String.valueOf(o2));
    }

    msgList.add(new Msg(messages, pname, o1, o2));

    if ((messages == null) || !haveOutputObject()) {
      return;
    }

    try {
      errors.add(id, new ActionMessage(pname, o1, o2));
    } catch (Throwable t) {
      error(className() + ": exception adding Action error", t);
    }
  }

  @Override
  public void emit(final String pname, final Object o1, final Object o2, final Object o3){
    if (debug()) {
      debugMsg(pname, "2objects",
               String.valueOf(o1) + "; " +
               String.valueOf(o2) + "; " +
               String.valueOf(o3));
    }

    msgList.add(new Msg(messages, pname, o1, o2, o3));

    if ((messages == null) || !haveOutputObject()) {
      return;
    }

    try {
      errors.add(id, new ActionMessage(pname, o1, o2, o3));
    } catch (Throwable t) {
      error(className() + ":exception adding Action error" + pname, t);
    }
  }

  /** Indicate no messages emitted. Null in this implementation.
   */
  @Override
  public void clear() {}

  /** @return true if any messages emitted
   */
  @Override
  public boolean messagesEmitted() {
    return !msgList.isEmpty();
  }

  /**
   * @return errors
   */
  public ActionErrors getErrors() {
    return errors;
  }

  protected boolean haveOutputObject() {
    return errors != null;
  }

  protected String className() {
    return "ErrorEmitSvlt";
  }

  protected void debugMsg(final String pname, 
                          final String ptype, 
                          final String pval) {
    debug("Emitted: property=" + pname +
                  " ptype=" + ptype +
                  " val(s)=" + pval);
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

