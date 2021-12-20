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
package org.bedework.webcommon;

import org.bedework.util.logging.BwLogger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

/** Useful shared web utility routines
 *
 * @author  Mike Douglass  douglm    rpi.edu
 */
public class BwWebUtil {
  /** Name of the session attribute holding our session state
   */
  public static final String sessStateAttr = "org.bedework.sessstate";

  /** Try to get the session state object  embedded in
   *  the current session.
   *
   * @param request  Needed to locate session
   * @return BwSession null on failure
   */
  public static BwSession getState(final HttpServletRequest request) {
    final HttpSession sess = request.getSession(false);

    if (sess != null) {
      final Object o = sess.getAttribute(sessStateAttr);
      if ((o instanceof BwSession)) {
        return (BwSession)o;
      }
    } else {
      noSession();
    }

    return null;
  }

  /** Drop the session state object embedded in
   *  the current session.
   *
   * @param request  Needed to locate session
   */
  public static void dropState(final HttpServletRequest request) {
    final HttpSession sess = request.getSession(false);

    if (sess == null) {
      return;
    }

    sess.removeAttribute(sessStateAttr);
  }

  /** Set the session state object into the current session.
   *
   * @param request  HttpServletRequest Needed to locate session
   * @param s        BwSession session object
   */
  public static void setState(final HttpServletRequest request,
                              final BwSession s) {
    final HttpSession sess = request.getSession(false);

    if (sess != null) {
      sess.setAttribute(sessStateAttr, s);
    } else {
      noSession();
    }
  }

  private static void noSession() {
    new BwLogger().
            setLoggedClass(BwWebUtil.class).
            warn("No session!!!!!!!");
  }
}

