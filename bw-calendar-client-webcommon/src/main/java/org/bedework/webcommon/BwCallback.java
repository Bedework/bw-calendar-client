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

import org.bedework.util.webaction.Request;

import java.io.Serializable;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/** Abstract class an instance of which is used to signal open and close
 * events to the web application.
 *
 * @author Mike Douglass douglm rpi.edu
 */
public abstract class BwCallback implements Serializable {
  /** This callback should be installed in the session as an attribute with
   this name. Then either a filter or request listener (but not both) will
   be able to use the callback to open/close the backend.
   */
  public static final String cbAttrName =
      "org.bedework.webcommon.SvciFilter";

  /** Called when the response is on its way in.
   *
   * @return int HttpServletResponse status
   */
  public abstract int in(Request req);

  /** Called when the response is on its way out.
   *
   * @param hreq HttpServletRequest
   */
  public abstract void out(HttpServletRequest hreq);

  /** Flag an error
   *
   * @param hreq HttpServletRequest
   * @param hresp HttpServletResponse
   * @param t the error
   */
  public abstract void error(HttpServletRequest hreq,
                             HttpServletResponse hresp,
                             Throwable t);

  /** Called after the above to allow close etc.
   *
   * @param cleanUp  true if we are cleaning up for id switch etc
   */
  public abstract void close(HttpServletRequest hreq,
                             boolean cleanUp);

  public static BwCallback getCb(final Request request,
                                 final BwActionFormBase form) {
    final HttpSession hsess = request.getRequest().getSession();
    BwCallback cb = (BwCallback)hsess.getAttribute(BwCallback.cbAttrName);
    if (cb == null) {
      /* create a call back object for the filter */

      cb = new BwCallbackImpl(form);
      hsess.setAttribute(BwCallback.cbAttrName, cb);
    }

    final BwCallbackImpl impl = (BwCallbackImpl)cb;
    if (impl.debug()) {
      impl.debug("checkSvci-- set req in cb - form action path = " +
                         request.getActionPath() +
                         " conv-type = " + request.getConversationType());
    }

    return cb;
  }
}
