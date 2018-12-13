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
import org.bedework.util.logging.Logged;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/** This class must be installed as a filter for a Bedework web application.
 *
 * <p>We assume that any CalSvci object must remain open until after the jsp
 * has done its stuff, i.e. after the action returns but before we finally
 * deliver the response. This filter uses a callback object stored as an
 * attribute in the session.
 *
 * @author Mike Douglass douglm rpi.edu
 */
public class BwSvciFilter implements Filter, Logged {
  protected ServletContext ctx;

  public void init(final FilterConfig cnfg) throws ServletException {
    ctx = cnfg.getServletContext();
  }

  public void doFilter(final ServletRequest req,
                       final ServletResponse resp,
                       final FilterChain chain)
          throws IOException, ServletException {
    HttpServletRequest hreq = (HttpServletRequest)req;
    HttpServletResponse hresp = (HttpServletResponse)resp;
    HttpSession sess = hreq.getSession();
    BwCallback cb = null;

    Throwable thr = null;

    try {
      /* cb is not set up at this point
      cb = getCb(sess, "in");

      if (cb != null) {
        int status = cb.in();
        if (status != HttpServletResponse.SC_OK) {
          hresp.setStatus(status);
          error("Callback.in status=" + status);
          return;
        }
      }
      */

      /* We need this to force utf encoding.
       * Not sure whether this is always correct
       */
      req.setCharacterEncoding("UTF-8");

      try {
        chain.doFilter(req, resp);
      } catch (Throwable dft) {
        error("Exception in filter: ", dft);
        thr = dft;
        try {
          cb.error(hreq, hresp, dft);
        } catch (Throwable t1) {
          error("Callback exception: ", t1);
        }
      }

      cb = getCb(sess, "out");
      if (cb != null) {
        cb.out(hreq);
      }
    } catch (Throwable t) {
      error("Callback exception: ", t);
      thr = t;
      try {
        cb.error(hreq, hresp, t);
      } catch (Throwable t1) {
        error("Callback exception: ", t1);
      }
    } finally {
      try {
        cb = getCb(sess, "close");

        if (cb != null) {
          cb.close(hreq, false);
        }
      } catch (Throwable t) {
        error("Callback exception: ", t);
        thr = t;
      }

      if (thr != null) {
        if (thr instanceof ServletException) {
          throw (ServletException)thr;
        }
        throw new ServletException(thr);
      }
    }
  }

  public void destroy() {
  }

  private BwCallback getCb(final HttpSession sess,
                              final String tracer) throws Throwable {
    if (sess == null) {
      if (debug()) {
        debug(tracer + " no session object");
      }
      return null;
    }

    try {
      BwCallback cb = (BwCallback)sess.getAttribute(BwCallback.cbAttrName);
      if (debug()) {
        if (cb != null) {
          debug(tracer + " Obtained BwCallback object");
        } else {
          debug(tracer + " no BwCallback available");
        }
      }

      return cb;
    } catch (IllegalStateException ise) {
      // Invalidated session - assume logged out
      if (debug()) {
        debug(tracer + " Invalidated session - assume logged out");
      }
      return null;
    }
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


