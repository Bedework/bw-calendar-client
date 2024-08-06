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

import org.bedework.appcommon.BedeworkDefs;
import org.bedework.appcommon.ConfigCommon;
import org.bedework.appcommon.client.Client;
import org.bedework.appcommon.client.ROClientImpl;
import org.bedework.caldav.util.filter.FilterBase;
import org.bedework.calfacade.exc.CalFacadeException;
import org.bedework.client.rw.RWClientImpl;
import org.bedework.util.logging.BwLogger;
import org.bedework.util.logging.Logged;
import org.bedework.util.misc.Util;
import org.bedework.util.webaction.Request;

import java.io.Serializable;

import javax.servlet.http.HttpSession;

import static org.bedework.webcommon.ForwardDefs.forwardNoAction;

/** A module represents a client and its associated state. A module
 * or its subclass MUST NOT be exposed to jsp. It MAY hold an object
 * which is exposed via the form and delivered via the moduleState
 * method.
 *
 * <p>A module will be single threaded with respect to requests. The
 * filter enforces this by checking to see if the module is in use.</p>
 *
 * @author Mike Douglass   douglm  rpi.edu
 */
public class BwModule implements Logged, Serializable {
  /** */
  public static final String defaultModuleName = "default";

  private final String moduleName;
  private Client cl;
  private final BwModuleState state;
  private String whenClaimed;

  /* ..................... fields associated with locking ............... */

  /** Requests waiting */
  private int waiters;

  private boolean inuse;

  private long timeIn;

  private Request currentReq;

  public BwModule(final String moduleName) {
    this.moduleName = moduleName;
    state = new BwModuleState(moduleName);
  }

  /**
   * @return String
   */
  public String getModuleName() {
    return moduleName;
  }

  /** The current client object for this module.
   *
   * @param val the client
   */
  public void setClient(final Client val) {
    cl = val;
  }

  /**
   *
   * @return the client object
   */
  public Client getClient() {
    return cl;
  }

  /**
   *
   * @return the state object
   */
  public BwModuleState getState() {
    return state;
  }

  /** The current request for this module.
   *
   * @param val the request
   */
  public void setRequest(final Request val) {
    currentReq = val;
  }

  /**
   *
   * @return the request object
   */
  public Request getRequest() {
    return currentReq;
  }

  /** Inc waiting for resource
   *
   */
  public void incWaiters() {
    waiters++;
  }

  /** Dec waiting for resource
   *
   */
  public void decWaiters() {
    waiters--;
  }

  /** Get waiting for resource
   *
   * @return num waiting for resource
   */
  public int getWaiters() {
    return waiters;
  }

  /** Set inuse flag
   *
   * @param val inuse flag
   */
  public void setInuse(final boolean val) {
    inuse = val;
  }

  /**
   * @return boolean value of inuse flag
   */
  public boolean getInuse() {
    return inuse;
  }

  /**
   *
   * @return true if we succeeded - false if interrupted or too busy
   */
  public synchronized boolean claim() {
    int attempts = 0;
    while (getInuse()) {
      if (debug()) {
        debug("Module " + getModuleName() +
                      " in use by " + getWaiters() +
                      " Timestamp: " + whenClaimed);
      }
      // double-clicking on our links eh?
      if ((getWaiters() > 10) || (attempts > 3)) {
        return false;
      }
      incWaiters();
      try {
        wait(5000);
        attempts++;
      } catch (final InterruptedException e) {
        return false;
      } finally {
        decWaiters();
      }
    }

    setInuse(true);
    whenClaimed = Util.icalUTCTimestamp();

    return true;
  }

  public void requestIn() {
    if (getClient() == null) {
      return;
    }

//    decWaiters();
    assert(getInuse());
    setInuse(true);
    timeIn = System.currentTimeMillis();

    getClient().requestIn(currentReq.getConversationType());
  }

  public void requestOut() {
    if (getClient() == null) {
      return;
    }

    cl.requestOut(currentReq.getConversationType(),
                  currentReq.getActionType(),
                  System.currentTimeMillis() - timeIn);
  }

  /** Close the session.
   *
   * @param cleanUp  true if we are cleaning up for id switch etc
   */
  public void close(final boolean cleanUp) {
    final int convType = currentReq.getConversationType();

    try {
      if (cleanUp) {
        closeNow();
      } else if (convType == Request.conversationTypeUnknown) {
        if (currentReq.getActionType() != Request.actionTypeAction) {
          closeNow();
        }
      } else {
        if ((convType == Request.conversationTypeEnd) ||
                (convType == Request.conversationTypeOnly)) {
          closeNow();
        }
      }
    } finally {
      synchronized (this) {
        setInuse(false);
        notify();
      }
    }

    // Kill the session if it's the feeder
    if ((getClient() == null) || BedeworkDefs.appTypeFeeder.equals(getClient().getAppType())) {
      final HttpSession sess = currentReq.getRequest().getSession(false);
      if (sess != null) {
        sess.invalidate();
      }
    }

  }

  /** Ensure we have a client object for the given user.
   *
   * <p>This method must only be called for a non admin client.
   * It it overridden by the admin version.
   *
   * @param request       for pars
   * @param requestedUser user we want to be
   * @param canSwitch     true if we should definitely allow user to switch
   *                      this allows a user to switch between and into
   *                      groups of which they are a member
   * @return boolean      false for problems.
   */
  public boolean checkClient(final BwRequest request,
                             final BwSession sess,
                             final String requestedUser,
                             final boolean canSwitch,
                             final ConfigCommon conf) {
    if (conf.getPublicAdmin()) {
      throw new CalFacadeException("Non-admin client called for admin app");
    }

    final boolean readWrite = conf.getReadWrite();
    final boolean guestMode = !readWrite && conf.getGuestMode();
    String calSuiteName = null;

    final BwModuleState mstate = getState();

//    Client client = BwWebUtil.getClient(request.getRequest());
    Client client = getClient();

    if (BedeworkDefs.appTypeFeeder.equals(conf.getAppType())) {
      calSuiteName = request.getReqPar("cs", conf.getCalSuite());
    } else if (guestMode ||
            BedeworkDefs.appTypeWebpublicauth.equals(conf.getAppType())) {
      // A guest user using the public clients. Get the calendar suite from the
      // configuration
      calSuiteName = conf.getCalSuite();
    } else if (!requestedUser.equals(request.getCurrentUser())) {
      /* !publicAdmin: We're never allowed to switch identity as a user client.
       */
      return false;
    }

    /* Make some checks to see if this is an old - restarted session.
     */
    if (client != null) {
      /* Already there and already opened */
      if (debug()) {
        debug("Client interface -- Obtained from session for user " +
                      client.getCurrentPrincipalHref());
      }

      return true;
    }

    if (debug()) {
      debug("Client-- getResource new object for user " + requestedUser);
    }

    if (readWrite) {
      client = new RWClientImpl(conf,
                                getModuleName(),
                                request.getCurrentUser(),
                                requestedUser,
                                request.getConfig().getAppType());
    } else {
      client = new ROClientImpl(conf,
                                getModuleName(),
                                request.getCurrentUser(),
                                requestedUser,
                                calSuiteName,
                                request.getConfig().getAppType(),
                                true);
    }

    setClient(client);
    setRequest(request);

    // Didn't release module - just reflag entry
    requestIn();
    mstate.setRefresh(true);
    sess.reset(request);

    return true;
  }

  /** Called just before action.
   *
   * @param request wrapper
   * @return int foward index
   */
  protected int actionSetup(final BwRequest request) {
    // Not public admin.

    final ConfigCommon conf = request.getConfig();

    String refreshAction = request.getRefreshAction();
    Integer refreshInt = request.getRefreshInt();

    if (refreshAction == null) {
      refreshAction = conf.getRefreshAction();
    }

    if (refreshAction == null) {
      refreshAction = request.getActionPath();
    }

    if (refreshAction != null) {
      if (refreshInt == null) {
        refreshInt =  conf.getRefreshInterval();
      }

      setRefreshInterval(request,
                         refreshInt, refreshAction);

      /* Ensure the session timeout interval is longer than our refresh period
       */

      if (refreshInt > 0) {
        final HttpSession sess = request.getRequest().getSession(false);
        final int timeout = sess.getMaxInactiveInterval();

        if (timeout <= refreshInt) {
          // An extra minute should do it.
          debug("@+@+@+@+@+ set timeout to " + (refreshInt + 60));
          sess.setMaxInactiveInterval(refreshInt + 60);
        }
      }
    }

    //if (debug()) {
    //  log.debug("curTimeView=" + form.getCurTimeView());
    //}

    return forwardNoAction;
  }

  protected void checkMessaging(final BwRequest req) {
    // Nothing to do for read-only
  }

  /** Called if no filter is set. May be used to exclude unwanted items
   *
   * @param req current request
   * @return filter or null
   */
  public FilterBase defaultSearchFilter(final BwRequest req) {
    return null;
  }

  /** Check request for refresh interval
   *
   * @param request bedework request object
   * @param refreshInterval seconds
   * @param refreshAction action to call
   */
  public void setRefreshInterval(final BwRequest request,
                                 final int refreshInterval,
                                 final String refreshAction) {
    if (refreshInterval != 0) {
      final StringBuilder sb = new StringBuilder(250);

      sb.append(refreshInterval);
      sb.append("; URL=");
      sb.append(request.getUrlPrefix());
      if (!refreshAction.startsWith("/")) {
        sb.append("/");
      }
      sb.append(refreshAction);
      request.getResponse().setHeader("Refresh", sb.toString());
    }
  }

  private void closeNow() {
    Throwable t = null;

    try {
      final Client cl = getClient();
      if (cl != null) {
        cl.close();
      }
    } catch (final Throwable t1) {
      t = t1;
    }

    if (t != null) {
      throw new CalFacadeException(t);
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
}

