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
import org.bedework.appcommon.client.Client;
import org.bedework.util.misc.Logged;
import org.bedework.util.misc.Util;
import org.bedework.util.struts.Request;

import java.io.Serializable;

import javax.servlet.http.HttpSession;

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
public class BwModule extends Logged implements Serializable {
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

  public BwModule(final String moduleName,
                  final Client cl) {
    this.moduleName = moduleName;
    this.cl = cl;
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
      if (debug) {
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

  public void requestIn() throws Throwable {
    if (getClient() == null) {
      return;
    }

//    decWaiters();
    assert(getInuse());
    setInuse(true);
    timeIn = System.currentTimeMillis();

    getClient().requestIn(currentReq.getConversationType());
  }

  public void requestOut() throws Throwable {
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
   * @throws Throwable on fatal error
   */
  public void close(final boolean cleanUp) throws Throwable {
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

  private void closeNow() throws Throwable {
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
      throw t;
    }
  }
}

