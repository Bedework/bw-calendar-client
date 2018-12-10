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

import org.bedework.util.struts.Request;

import org.apache.log4j.Logger;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.util.RequestUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/** Abstract class an instance of which is used to signal open and close
 * events to the web application.
 *
 * @author Mike Douglass douglm@rpi.edu
 */
public class BwCallbackImpl extends BwCallback {
  protected boolean debug = false;

  private transient Logger log;

  final BwActionFormBase form;
  ActionForward errorForward;

  BwCallbackImpl(final BwActionFormBase form,
                 final ActionMapping mapping) {
    this.form = form;
    debug = getLogger().isDebugEnabled();
    errorForward = mapping.findForward("error");
    if (errorForward == null) {
      throw new RuntimeException("Forward \"error\" must be defined in struts-comfig");
    }
  }

  @Override
  public int in(final Request req) throws Throwable {
      /* On the way in we set up the client from the default client
         embedded in the form.
       */
    //synchronized (form) {
      final BwModule module = form.fetchModule(req.getModuleName());
      if (debug) {
        getLogger().debug("About to claim module " + module.getModuleName());
      }

      if (!module.claim()) {
        return HttpServletResponse.SC_SERVICE_UNAVAILABLE;
      }

      req.setRequestAttr(Request.moduleNamePar, module.getModuleName());
      module.setRequest(req);
      module.requestIn();

      return HttpServletResponse.SC_OK;
    //}
  }

  @Override
  public void out(final HttpServletRequest hreq) throws Throwable {
    final BwModule module = form.fetchModule(
            (String)hreq.getAttribute(Request.moduleNamePar));

    if (debug) {
      getLogger().debug("Request out for module " + module.getModuleName());
    }

    module.requestOut();
  }

  @Override
  public void close(final HttpServletRequest hreq,
                    final boolean cleanUp) throws Throwable {
    final BwModule module = form.fetchModule(
            (String)hreq.getAttribute(Request.moduleNamePar));

    if (debug) {
      getLogger().debug("Close for module " + module.getModuleName());
    }

    module.close(cleanUp);
  }

  @Override
  public void error(final HttpServletRequest hreq,
                    final HttpServletResponse hresp,
                    final Throwable t) throws Throwable {
    form.getErr().emit(t);

      /* Redirect to an error action
       */

    final String forwardPath = errorForward.getPath();
    String uri;

    // paths not starting with / should be passed through without any processing
    // (ie. they're absolute)
    if (forwardPath.startsWith("/")) {
      uri = RequestUtils.forwardURL(hreq, errorForward, null);    // get module relative uri
    } else {
      uri = forwardPath;
    }

    // only prepend context path for relative uri
    if (uri.startsWith("/")) {
      uri = hreq.getContextPath() + uri;
    }
    try {
      hresp.sendRedirect(hresp.encodeRedirectURL(uri));
    } catch (final Throwable ignored) {
      // Presumably illegal state
    }
  }

  /** Get a logger for messages
   *
   * @return Logger
   */
  public Logger getLogger() {
    if (log == null) {
      log = Logger.getLogger(this.getClass());
    }

    return log;
  }
}
