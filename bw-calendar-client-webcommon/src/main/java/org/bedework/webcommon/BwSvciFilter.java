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
import org.bedework.util.misc.Util;

import java.io.IOException;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

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

  protected boolean headDisallowed;

  public void init(final FilterConfig cnfg) {
    ctx = cnfg.getServletContext();

    headDisallowed =
            Boolean.parseBoolean(cnfg.getInitParameter("headDisallowed"));
  }

  public void doFilter(final ServletRequest req,
                       final ServletResponse resp,
                       final FilterChain chain)
          throws ServletException {
    final HttpServletRequest hreq = (HttpServletRequest)req;
    final HttpSession sess = hreq.getSession();
    BwCallback cb;

    Throwable thr = null;

    try {
      if (headDisallowed &&
              "HEAD".equals(hreq.getMethod())) {
        ((HttpServletResponse)resp).setStatus(HttpServletResponse.SC_OK);
        return;
      }

      /* We need this to force utf encoding.
       * Not sure whether this is always correct
       */
      req.setCharacterEncoding("UTF-8");

      try {
        chain.doFilter(req, resp);
      } catch (final Throwable dft) {
        final Throwable cause = Util.getRootCause(dft);
        if ((cause instanceof IOException) &&
                (cause.getMessage().startsWith("Connection reset by peer"))) {
          // Just warn
          warn("Connection reset by peer");
        } else {
          error("Exception in filter: ", dft);
          thr = dft;
        }
      }

      cb = getCb(sess, "out");
      if (cb != null) {
        cb.out(hreq);
      }
    } catch (final Throwable t) {
      error("Callback exception: ", t);
      thr = t;
    } finally {
      try {
        cb = getCb(sess, "close");

        if (cb != null) {
          cb.close(hreq, false);
        }
      } catch (final Throwable t) {
        error("Callback exception: ", t);
        thr = t;
      }
    }

    if (thr != null) {
      if (thr instanceof ServletException) {
        throw (ServletException)thr;
      }
      throw new ServletException(thr);
    }
  }

  public void destroy() {
  }

  private BwCallback getCb(final HttpSession sess,
                              final String tracer) {
    if (sess == null) {
      if (debug()) {
        debug(tracer + " no session object");
      }
      return null;
    }

    try {
      final BwCallback cb =
              (BwCallback)sess.getAttribute(BwCallback.cbAttrName);
      if (debug()) {
        if (cb != null) {
          debug(tracer + " Obtained BwCallback object");
        } else {
          debug(tracer + " no BwCallback available");
        }
      }

      return cb;
    } catch (final IllegalStateException ise) {
      // Invalidated session - assume logged out
      if (debug()) {
        debug(tracer + " Invalidated session - assume logged out");
      }
      return null;
    }
  }

  /* ==============================================================
   *                   Logged methods
   * ============================================================== */

  private final BwLogger logger = new BwLogger();

  @Override
  public BwLogger getLogger() {
    if ((logger.getLoggedClass() == null) &&
            (logger.getLoggedName() == null)) {
      logger.setLoggedClass(getClass());
    }

    return logger;
  }
}
