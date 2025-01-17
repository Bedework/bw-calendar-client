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
import org.bedework.util.logging.BwLogger;
import org.bedework.util.logging.Logged;

import java.io.Serializable;

/** This class represents the current state of a bedework client - most probably
 * the web clients.
 *
 * @author Mike Douglass
 *
 */
public class ClientState implements Logged, Serializable {
  private SearchParams searchParams;

  private String currentDate;

  private Client cl;

  /**
   * @param cl-the client object
   */
  public ClientState(final Client cl) {
    this.cl = cl;
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
) {   */
  public BwCalendar getCurrentCollection() {
    return null; //cc currentCollection;
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
