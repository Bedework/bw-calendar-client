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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

/** This class allows informational message generation.
 *
 * @author Mike Douglass douglm@rpi.edu
 * @version 1.0
 */
public class MessageEmitSvlt extends ErrorEmitSvlt {
  public static final String messageObjAttrName =
          "org.bedework.client.messageobj";

  /** Get the message object. If we haven't already got one and
   * getMessageObjAttrName returns non-null create one and implant it in
   * the session.
   *
   * @param request  Needed to locate session
   * @param id       An identifying name
   * @param exceptionPname Property name for exceptions
   * @param clear clear list if true
   * @return MessageEmitSvlt
   */
  public static MessageEmitSvlt getMessageObj(
          final HttpServletRequest request,
          final String id,
          final String exceptionPname,
          final boolean clear) {
    final HttpSession sess = request.getSession(false);

    if (sess == null) {
      throw new NoSessionException();
    }

    final Object o = sess.getAttribute(messageObjAttrName);
    MessageEmitSvlt msg = null;

    // Ensure it's initialised correctly
    if (o instanceof MessageEmitSvlt) {
      msg = (MessageEmitSvlt)o;
    }

    if (msg == null) {
      msg = new MessageEmitSvlt();
    }

    msg.reinit(id, exceptionPname, clear);

    // Implant in session

    sess.setAttribute(messageObjAttrName, msg);

    return msg;
  }

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
   * @param exceptionPname Property name for exceptions
   * @param clear clear list if true
   */
  public void reinit(final String id,
                     final String exceptionPname,
                     final boolean clear) {
    super.reinit(id, exceptionPname, clear);
  }

  @Override
  public void emit(final String pname) {
    if (debug()) {
      debugMsg(pname, null, null);
    }

    msgList.add(new Msg(pname));
  }

  @Override
  public void emit(final String pname, final Object o){
    if (debug()) {
      debugMsg(pname, "object", String.valueOf(o));
    }

    msgList.add(new Msg(pname, o));
  }

  @Override
  public void emit(final String pname, final Object o1, final Object o2){
    if (debug()) {
      debugMsg(pname, "2objects",
               o1 + "; " +
                       o2);
    }

    msgList.add(new Msg(pname, o1, o2));
  }

  @Override
  public void emit(final String pname, final Object o1, final Object o2, final Object o3){
    if (debug()) {
      debugMsg(pname, "2objects",
               o1 + "; " +
                       o2 + "; " +
                       o3);
    }

    msgList.add(new Msg(pname, o1, o2, o3));
  }

  @Override
  public boolean messagesEmitted() {
    return !msgList.isEmpty();
  }

  @Override
  protected String className() {
    return "MessageEmitSvlt";
  }
}

