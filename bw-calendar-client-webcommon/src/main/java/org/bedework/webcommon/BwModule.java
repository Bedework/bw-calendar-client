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

import org.bedework.appcommon.client.Client;
import org.bedework.util.struts.Request;

import org.apache.log4j.Logger;

import java.io.Serializable;

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
public class BwModule implements Serializable {
  protected boolean debug = false;

  private transient Logger log;

  /** */
  public static final String defaultModuleName = "default";

  private String moduleName;
  private Client cl;
  private BwModuleState state;

  /* ..................... fields associated with locking ............... */

  /** Requests waiting */
  private int waiters;

  private boolean inuse;

  private long timeIn;

  private Request currentReq;

  public BwModule(String moduleName,
                  Client cl) {
    debug = getLogger().isDebugEnabled();
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
   * @param val
   */
  public void setClient(Client val) {
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
   * @param val
   */
  public void setRequest(Request val) {
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
   * @param val
   */
  public void setInuse(boolean val) {
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
    while (getInuse()) {
      if (debug) {
        debugMsg("Module " + getModuleName() +
                         " in use by " + getWaiters());
      }
      // double-clicking on our links eh?
      if (getWaiters() > 10) {
        return false;
      }
      incWaiters();
      try {
        wait();
      } catch (InterruptedException e) {
        return false;
      } finally {
        decWaiters();
      }
    }

    setInuse(true);

    return true;
  }

  public void requestIn() throws Throwable {
    if (getClient() == null) {
      return;
    }

//    decWaiters();
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
   * @throws Throwable
   */
  public void close(final boolean cleanUp) throws Throwable {
    int convType = currentReq.getConversationType();

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
  }

  private void closeNow() throws Throwable {
    Throwable t = null;

    try {
      Client cl = getClient();
      if (cl != null) {
        cl.close();
      }
    } catch (Throwable t1) {
      t = t1;
    }

    if (t != null) {
      throw t;
    }
  }

  /** Get a logger for messages
   *
   * @return Logger
   */
  private Logger getLogger() {
    if (log == null) {
      log = Logger.getLogger(this.getClass());
    }

    return log;
  }

  /**
   * @param msg
   */
  private void debugMsg(final String msg) {
    getLogger().debug(msg);
  }
}

