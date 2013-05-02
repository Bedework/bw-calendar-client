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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.servlet.ServletContext;
import javax.servlet.ServletRequestEvent;
import javax.servlet.ServletRequestListener;
import org.apache.log4j.Logger;

/** This class was installed as a request listener for a bedework web application.
 *
 * NOT USED - this has been replaced by the filter.
 *
 * <p>We assume that any CalSvci object must remain open until after the jsp
 * has done its stuff, i.e. after the action returns but before we finally
 * deliver the response. This listener uses a callback object stored as an
 * attribute in the session.
 *
 * @author Mike Douglass douglm@rpi.edu
 */
public class BwRequestListener implements ServletRequestListener {
  /* Set as a config parameter */
  private String attrName;

  protected boolean debug = false;

  public void requestInitialized(ServletRequestEvent sre) {
    HttpServletRequest req = (HttpServletRequest)sre.getServletRequest();
    HttpSession sess = req.getSession();

    if (attrName == null) {
      init(sess);
    }

    BwCallback cb = null;

    try {
      cb = getCb(sess, "in");

      if (cb != null) {
        cb.in();
      }
    } catch (Throwable t) {
      getLogger().error("Callback exception: ", t);
      throw new RuntimeException(t);
    }
  }

  public void requestDestroyed(ServletRequestEvent sre) {
    HttpServletRequest req = (HttpServletRequest)sre.getServletRequest();
    HttpSession sess = req.getSession();

    if (attrName == null) {
      init(sess);
    }

    BwCallback cb = null;

    try {
      cb = getCb(sess, "in");

      if (cb != null) {
        cb.out();
      }
    } catch (Throwable t) {
      getLogger().error("Callback exception: ", t);
      throw new RuntimeException(t);
    } finally {
      cb = getCb(sess, "close");

      if (cb != null) {
        try {
          cb.close();
        } catch (Throwable t) {
          getLogger().error("Callback exception: ", t);
          throw new RuntimeException(t);
        }
      }
    }
  }

  private void init(HttpSession sess) {
    ServletContext ctx = sess.getServletContext();
    String temp = ctx.getInitParameter("debug");

    try {
      int debugVal = Integer.parseInt(temp);

      debug = (debugVal > 2);
    } catch (Exception e) {}

    attrName = ctx.getInitParameter("org.uwcal.svcicb.sessionAttrName");
    if (attrName == null) {
      throw new RuntimeException("Must supply sessionAttrName for request listener");
    }

  }

  private BwCallback getCb(HttpSession sess,
                              String tracer) {
    if (sess == null) {
      if (debug) {
        getLogger().debug(tracer + " no session object");
      }
      return null;
    }

    try {
      BwCallback cb = (BwCallback)sess.getAttribute(attrName);
      if (debug) {
        if (cb != null) {
          getLogger().debug(tracer + " Obtained BWCalCallback object");
        } else {
          getLogger().debug(tracer + " no BWCalCallback available");
        }
      }

      return cb;
    } catch (IllegalStateException ise) {
      // Invalidated session - assume logged out
      if (debug) {
        getLogger().debug(tracer + " Invalidated session - assume logged out");
      }
      return null;
    }
  }

  private Logger getLogger() {
    return Logger.getLogger(this.getClass());
  }
}


