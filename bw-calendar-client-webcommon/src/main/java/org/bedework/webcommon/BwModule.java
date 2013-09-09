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

import java.io.Serializable;

/** A module represents a client and its associated state. A module
 * or its subclass MUST NOT be exposed to jsp. It MAY hold an object
 * which is exposed via the form and delivered via the moduleState
 * method.
 *
 * <p>A module will be single threaded with respect to requests. The
 * filter enforces this by checking to see if the module is in use.</p>
 *
 * @author Mike Douglass   douglm  bedework.edu
 */
public class BwModule implements Serializable {
  /** This class will be exposed to JSP via the form. Do not expose the
   * client indirectly through this.
   *
   */
  public class ModuleState implements Serializable {
  }

  /** */
  public static final String defaultModuleName = "default";

  private String moduleName;
  private Client cl;
  private ModuleState state;

  /* ..................... fields associated with locking ............... */

  /** Requests waiting */
  private int waiters;

  private boolean inuse;

  private long timeIn;

  private Request currentReq;

  public BwModule(String moduleName,
                  Client cl) {
    this.moduleName = moduleName;
    this.cl = cl;
  }

  /**
   * @param val
   */
  public void setModuleName(final String val) {
    moduleName = val;
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

  /** The current state of this module.
   *
   * @param val
   */
  public void setState(ModuleState val) {
    state = val;
  }

  /**
   *
   * @return the state object
   */
  public ModuleState getState() {
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

  public void requestIn() throws Throwable {
    if (getClient() == null) {
      return;
    }

    decWaiters();
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
   * @throws Throwable
   */
  public void close() throws Throwable {
    boolean unlocked = false;
    int convType = currentReq.getConversationType();

    try {
      if (convType == Request.conversationTypeUnknown) {
        if (currentReq.getActionType() != Request.actionTypeAction) {
          closeNow();
          unlocked = true;
        }
      } else {
        // Conversations
        if ((convType == Request.conversationTypeEnd) ||
                (convType == Request.conversationTypeOnly)) {
          closeNow();
          unlocked = true;
        }
      }
    } finally {
      if (!unlocked) {
        synchronized (this) {
          setInuse(false);
          notify();
        }
      }
    }
  }

  public void closeNow() throws Throwable {
    Throwable t = null;

    try {
      Client cl = getClient();
      if (cl != null) {
        cl.close();
      }
    } catch (Throwable t1) {
      t = t1;
    } finally {
      synchronized (this) {
        setInuse(false);
        notify();
      }
    }

    if (t != null) {
      throw t;
    }
  }
}

