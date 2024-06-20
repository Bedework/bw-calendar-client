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

import org.bedework.calfacade.exc.CalFacadeException;
import org.bedework.util.logging.BwLogger;
import org.bedework.util.servlet.HttpAppLogger;
import org.bedework.util.webaction.Request;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/** Class an instance of which is used to signal open and close
 * events to the web application.
 *
 * @author Mike Douglass douglm
 */
public class BwCallbackImpl extends BwCallback implements
        HttpAppLogger {
  final BwActionFormBase form;
  final String errorForward;

  BwCallbackImpl(final BwActionFormBase form) {
    this.form = form;
    final var ef = form.getErrorForward();
    if (ef == null) {
      throw new CalFacadeException("\"errorForward\" must be defined for servlet context");
    }

    if (!ef.startsWith("/")) {
      errorForward = "/" + ef;
    } else {
      errorForward = ef;
    }
  }

  @Override
  public int in(final Request req) {
      /* On the way in we set up the client from the default client
         embedded in the form.
       */
    //synchronized (form) {
      final BwModule module = form.fetchModule(req.getModuleName());
      if (debug()) {
        debug("About to claim module " + module.getModuleName());
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
  public void out(final HttpServletRequest hreq) {
    final BwModule module = form.fetchModule(
            (String)hreq.getAttribute(Request.moduleNamePar));

    if (debug()) {
      debug("Request out for module " + module.getModuleName());
    }

    module.requestOut();
  }

  @Override
  public void close(final HttpServletRequest hreq,
                    final boolean cleanUp) {
    final BwModule module = form.fetchModule(
            (String)hreq.getAttribute(Request.moduleNamePar));

    if (debug()) {
      debug("Close for module " + module.getModuleName());
    }

    if (!cleanUp) {
      logRequestOut(hreq);
    }

    module.close(cleanUp);
  }

  @Override
  public void error(final HttpServletRequest hreq,
                    final HttpServletResponse hresp,
                    final Throwable t) {
    error(t);

    /* Redirect to an error action
       */

    try {
      hresp.sendRedirect(hresp.encodeRedirectURL(
              hreq.getContextPath() + errorForward));
    } catch (final Throwable ignored) {
      // Presumably illegal state
    }
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

  @Override
  public String getLogPrefix(final HttpServletRequest request) {
    return "bwclientcb";
  }
}
