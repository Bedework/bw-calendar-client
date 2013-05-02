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
package org.bedework.webcommon.misc;

import java.util.Collection;

import org.bedework.calfacade.BwStats;
import org.bedework.calfacade.BwStats.StatsEntry;
import org.bedework.calsvci.CalSvcI;
import org.bedework.webcommon.BwAbstractAction;
import org.bedework.webcommon.BwActionFormBase;
import org.bedework.webcommon.BwRequest;

/**
 * Action to display statistics.
 *
 * <p>Parameters are one of:<ul>
 *      <li>"enable"            Enable stats</li>
 *      <li>"disable"           Disable stats</li>
 *      <li>"dump"              Dump stats</li>
 *      <li>"fetch"             fetch stats</li>
 * </ul>
 *
 * <p>Forwards to:<ul>
 *      <li>"retry"        email options still not valid.</li>
 *      <li>"noEvent"      no event was selected.</li>
 *      <li>"success"      mailed (or queued) ok.</li>
 * </ul>
 */
public class StatisticsAction extends BwAbstractAction {
  /* (non-Javadoc)
   * @see org.bedework.webcommon.BwAbstractAction#doAction(org.bedework.webcommon.BwRequest, org.bedework.webcommon.BwActionFormBase)
   */
  public int doAction(BwRequest request,
                      BwActionFormBase form) throws Throwable {
    /*if (form.getGuest()) {
      return forwardNoAccess; // First line of defence
    }*/

    CalSvcI svci = form.fetchSvci();

    if (request.getReqPar("enable") != null) {
      svci.setDbStatsEnabled(true);
    } else if (request.getReqPar("disable") != null) {
      svci.setDbStatsEnabled(false);
    } else if (request.getReqPar("dump") != null) {
      svci.dumpDbStats();
    } else if (request.getReqPar("fetch") != null) {
      BwStats stats = svci.getStats();

      Collection<StatsEntry> c = stats.getStats();

      c.addAll(svci.getDbStats());

      form.assignSysStats(c);

      return forwardContinue;
    }

    return forwardSuccess;
  }
}
