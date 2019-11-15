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
import org.bedework.util.servlet.HttpServletUtils;
import org.bedework.util.servlet.MessageEmit;

import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionMessages;
import org.apache.struts.util.MessageResources;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

/**
 * This class provides some convenience methods for use by ActionForm objects.
 * It shoudl really be called StrutsUtil
 *
 * @author Mike Douglass
 */
public class StrutsUtil extends HttpServletUtils {
  private static BwLogger logger =
          new BwLogger().setLoggedClass(HttpServletUtils.class);

  private StrutsUtil() throws Exception {} // No instantiation

  /* ================ Properties methods ============== */

  /** Return a property value or the default
   *
   * @param msg      MessageResources object
   * @param pname    String name of the property
   * @param def      String default value
   * @return String  property value or default (may be null)
   * @throws Throwable on error
   */
  public static String getProperty(final MessageResources msg,
                                   final String pname,
                                   final String def) throws Throwable {
    String p = msg.getMessage(pname);
    if (p == null) {
      return def;
    }

    return p;
  }

  /** Return a required property value
   *
   * @param msg      MessageResources object
   * @param pname    name of the property
   * @return String  property value
   * @throws Throwable on error
   */
  public static String getReqProperty(final MessageResources msg,
                                      final String pname) throws Throwable {
    String p = getProperty(msg, pname, null);
    if (p == null) {
      logger.error("No definition for property " + pname);

      throw new Exception(": No definition for property " + pname);
    }

    return p;
  }

  /** Return a boolean property value or the default
   *
   * @param msg      MessageResources object
   * @param pname    String name of the property
   * @param def      boolean default value
   * @return boolean property value or default
   * @throws Throwable on error
   */
  public static boolean getBoolProperty(final MessageResources msg,
                                        final String pname,
                                        final boolean def) throws Throwable {
    String p = msg.getMessage(pname);
    if (p == null) {
      return def;
    }

    return Boolean.valueOf(p);
  }

  /** Return an int property value or the default
   *
   * @param msg      MessageResources object
   * @param pname    String name of the property
   * @param def      int default value
   * @return int     property value or default
   * @throws Throwable on error
   */
  public static int getIntProperty(final MessageResources msg,
                                   final String pname,
                                   final int def) throws Throwable {
    String p = msg.getMessage(pname);
    if (p == null) {
      return def;
    }

    return Integer.valueOf(p);
  }

  /* ==================================================================
                      Error object
     ================================================================== */

  /** Get the error object. If we haven't already got one and
   * getErrorObjAttrName returns non-null create one and implant it in
   * the session.
   *
   * @param id       Identifying string for messages
   * @param caller   Used for log identification
   * @param request  Needed to locate session
   * @param messages MessageResources object for creating new object
   * @param errorObjAttrName  name of session attribute
   * @param errProp  name of exception message property
   * @param noActionErrors true/false
   * @param clear list
   * @return MessageEmit null on failure
   */
  public static MessageEmit getErrorObj(final String id,
                                        final Object caller,
                                        final HttpServletRequest request,
                                        final MessageResources messages,
                                        final String errorObjAttrName,
                                        final String errProp,
                                        final boolean noActionErrors,
                                        final boolean clear) {
    if (errorObjAttrName == null) {
      // don't set
      return null;
    }

    HttpSession sess = request.getSession(false);

    if (sess == null) {
      logger.error("No session!!!!!!!");
      return null;
    }

    Object o = sess.getAttribute(errorObjAttrName);
    MessageEmit err = null;

    // Ensure it's initialised correctly
    if ((o != null) && (o instanceof ErrorEmitSvlt)) {
      if (noActionErrors || (((ErrorEmitSvlt)o).getErrors() != null)) {
        err = (MessageEmit)o;
      }
    }

    if (err == null) {
      err = new ErrorEmitSvlt();
    }

    ActionErrors ae = null;
    if (!noActionErrors) {
      ae = new ActionErrors();
    }

    ((ErrorEmitSvlt)err).reinit(id, caller, messages, ae, errProp, clear);

    // Implant in session

    sess.setAttribute(errorObjAttrName, err);

    return err;
  }

  /** Get the existing error object from the session or null.
   *
   * @param request  Needed to locate session
   * @param errorObjAttrName  name of session attribute
   * @return MessageEmit null on none found
   */
  public static MessageEmit getErrorObj(final HttpServletRequest request,
                                        final String errorObjAttrName) {
    if (errorObjAttrName == null) {
      // don't set
      return null;
    }

    HttpSession sess = request.getSession(false);

    if (sess == null) {
      logger.error("No session!!!!!!!");
      return null;
    }

    Object o = sess.getAttribute(errorObjAttrName);
    if ((o != null) && (o instanceof MessageEmit)) {
      return (MessageEmit)o;
    }

    return null;
  }

  /* ==================================================================
                      Message object
     ================================================================== */

  /** Get the message object. If we haven't already got one and
   * getMessageObjAttrName returns non-null create one and implant it in
   * the session.
   *
   * @param id                Identifying string for messages
   * @param caller            Used for log identification
   * @param request           Needed to locate session
   * @param messages           MessageResources object for creating new object
   * @param messageObjAttrName  name of session attribute
   * @param errProp           name of exception message property
   * @param clear             the list
   * @return MessageEmit      null on failure
   */
  public static MessageEmit getMessageObj(final String id,
                                          final Object caller,
                                          final HttpServletRequest request,
                                          final MessageResources messages,
                                          final String messageObjAttrName,
                                          final String errProp,
                                          final boolean clear) {
    if (messageObjAttrName == null) {
      // don't set
      return null;
    }

    HttpSession sess = request.getSession(false);

    if (sess == null) {
      logger.error("No session!!!!!!!");
      return null;
    }

    Object o = sess.getAttribute(messageObjAttrName);
    MessageEmit msg = null;

    // Ensure it's initialised correctly
    if ((o != null) && (o instanceof MessageEmitSvlt)) {
      if (((MessageEmitSvlt)o).getMessages() != null) {
        msg = (MessageEmit)o;
      }
    }

    if (msg == null) {
      msg = new MessageEmitSvlt();
    }

    ((MessageEmitSvlt)msg).reinit(id, caller, messages, new ActionMessages(),
                                  errProp, clear);

    // Implant in session

    sess.setAttribute(messageObjAttrName, msg);

    return msg;
  }

  /** Get the existing message object from the session or null.
   *
   * @param request  Needed to locate session
   * @param messageObjAttrName  name of session attribute
   * @return MessageEmit null on none found
   */
  public static MessageEmit getMessageObj(final HttpServletRequest request,
                                          final String messageObjAttrName) {
    if (messageObjAttrName == null) {
      // don't set
      return null;
    }

    HttpSession sess = request.getSession(false);

    if (sess == null) {
      logger.error("No session!!!!!!!");
      return null;
    }

    Object o = sess.getAttribute(messageObjAttrName);
    if ((o != null) && (o instanceof MessageEmit)) {
      return (MessageEmit)o;
    }

    return null;
  }
}

