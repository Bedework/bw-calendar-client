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
package org.bedework.appcommon.client;

import org.bedework.calfacade.BwCalendar;
import org.bedework.calfacade.exc.CalFacadeException;

import org.apache.log4j.Logger;

import java.io.Serializable;

/** This class represents the current state of a bedework client - most probably
 * the web clients.
 *
 * @author Mike Douglass
 *
 */
public class ClientState implements Serializable {
  private transient Logger log;

  private boolean debug;

  private SearchParams searchParams;

  private String currentDate;

  private Client cl;

  /**
   * @param cl-the client object
   */
  public ClientState(final Client cl) {
    this.cl = cl;
    debug = getLogger().isDebugEnabled();
  }

  /* ====================================================================
   *                   Current selection
   * This defines how we select events to display.
   * ==================================================================== */

  /** Event key for next action
   *
   * @param val
   */
  public void setSearchParams(final SearchParams val) {
    searchParams = val;
  }

  /**
   * @return SearchParams
   */
  public SearchParams getSearchParams() {
    return searchParams;
  }

  /**
   * @return BwCalendar
   * @throws CalFacadeException
   */
  public BwCalendar getCurrentCollection() throws CalFacadeException {
    return null; //cc currentCollection;
  }

  /* ====================================================================
   *                   Private methods
   * ==================================================================== */

  /* Get a logger for messages
   */
  private Logger getLogger() {
    if (log == null) {
      log = Logger.getLogger(this.getClass());
    }

    return log;
  }

  protected void trace(final String msg) {
    getLogger().debug("trace: " + msg);
  }
}
