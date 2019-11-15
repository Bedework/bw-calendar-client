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

import org.apache.struts.action.ActionMessage;
import org.apache.struts.action.ActionMessages;
import org.apache.struts.util.MessageResources;

/** This class allows informational message generation in the struts world.
 *
 * @author Mike Douglass douglm@rpi.edu
 * @version 1.0
 */
public class MessageEmitSvlt extends ErrorEmitSvlt {
  transient private ActionMessages msgs;

  /**
   *
   */
  public MessageEmitSvlt() {
  }

  /** Generation of errors in the servlet world means adding them to the
   *  errors object. We need to call this routine on every entry to the
   *  application
   *
   * @param id       An identifying name
   * @param caller   Used for log identification
   * @param messages Resources
   * @param msgs     Error message will be appended on failure.
   * @param exceptionPname Property name for exceptions
   * @param clear clear list if true
   */
  public void reinit(final String id,
                     final Object caller,
                     final MessageResources messages,
                     final ActionMessages msgs,
                     final String exceptionPname,
                     final boolean clear) {
    super.reinit(id, caller, messages, null, exceptionPname, clear);
    this.msgs = msgs;
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
      msgs.add(id, new ActionMessage(pname));
    } catch (Throwable t) {
      error(className() + ": exception adding Action message", t);
    }
  }

  @Override
  public void emit(final String pname, final Object o){
    if (debug()) {
      debugMsg(pname, "object", String.valueOf(o));
    }

    msgList.add(new Msg(messages, pname, o));

    if ((messages == null) || !haveOutputObject()) {
      return;
    }

    try {
      msgs.add(id, new ActionMessage(pname, o));
    } catch (Throwable t) {
      error(className() + ": exception adding Action message", t);
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
      msgs.add(id, new ActionMessage(pname, o1, o2));
    } catch (Throwable t) {
      error(className() + ": exception adding Action message", t);
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
      msgs.add(id, new ActionMessage(pname, o1, o2, o3));
    } catch (Throwable t) {
      error(className() + ": exception adding Action message", t);
    }
  }

  /* (non-Javadoc)
   * @see edu.rpi.sss.util.log.MessageEmit#messagesEmitted()
   */
  @Override
  public boolean messagesEmitted() {
    return !msgs.isEmpty();
  }

  /**
   * @return messages
   */
  public ActionMessages getMessages() {
    return msgs;
  }

  @Override
  protected String className() {
    return "MessageEmitSvlt";
  }

  @Override
  protected boolean haveOutputObject() {
    return msgs != null;
  }
}

