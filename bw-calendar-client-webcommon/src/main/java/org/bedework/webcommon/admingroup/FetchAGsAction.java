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
package org.bedework.webcommon.admingroup;

import org.bedework.appcommon.client.Client;
import org.bedework.calfacade.BwGroup;
import org.bedework.webcommon.BwAbstractAction;
import org.bedework.webcommon.BwActionFormBase;
import org.bedework.webcommon.BwRequest;

import java.util.ArrayList;
import java.util.Collection;

/** This action fetches all admin groups
 *
 * <p>Forwards to:<ul>
 *      <li>forwardSuccess     OK.</li>
 * </ul>
 *
 * @author Mike Douglass   douglm@rpi.edu
 */
public class FetchAGsAction extends BwAbstractAction {
  /* The list of admin groups displayed for the use of the user client
   */
  private static Collection<BwGroup> adminGroupsInfo;

  private static long lastAdminGroupsInfoRefresh;
  private static long adminGroupsInfoRefreshInterval = 1000 * 60 * 5;

  private static volatile Object locker = new Object();

  static void forceRefresh() {
    lastAdminGroupsInfoRefresh = 0;
  }

  @Override
  public int doAction(BwRequest request,
                      BwActionFormBase form) throws Throwable {
    Client cl = request.getClient();

    refreshAdminGroupsInfo: {
      if (System.currentTimeMillis() > (lastAdminGroupsInfoRefresh +
                                                adminGroupsInfoRefreshInterval)) {
        synchronized (locker) {
          if (System.currentTimeMillis() < (lastAdminGroupsInfoRefresh +
                                                    adminGroupsInfoRefreshInterval)) {
            // Somebody else got there
            break refreshAdminGroupsInfo;
          }

          adminGroupsInfo = new ArrayList<>();

          Collection<BwGroup> ags = cl.getAdminGroups(true);

          for (BwGroup g: ags) {
            BwGroup cg = (BwGroup)g.clone();

            Collection<BwGroup> mgs = cl.getAllAdminGroups(g);

            for (BwGroup mg: mgs) {
              BwGroup cmg = (BwGroup)mg.clone();

              cg.addGroup(cmg);
            }

            adminGroupsInfo.add(cg);
          }

          lastAdminGroupsInfoRefresh = System.currentTimeMillis();
        }
      }
    }

    request.setSessionAttr(BwRequest.bwAdminGroupsInfoName,
                           adminGroupsInfo);

    return forwardSuccess;
  }
}

