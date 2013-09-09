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

/** This is a no-op action
 *
 * @author Mike Douglass  douglm - bedework.edu
 */
public class NoopAction extends BwAbstractAction {
  /* (non-Javadoc)
   * @see org.bedework.webcommon.BwAbstractAction#doAction(org.bedework.webcommon.BwRequest, org.bedework.webcommon.BwActionFormBase)
   */
  @Override
  public int doAction(final BwRequest request,
                      final BwActionFormBase form) throws Throwable {
    if (form.getNewSession()) {
      form.refreshIsNeeded();

      Client cl = request.getClient();

      String defViewMode = cl.getPreferences().getDefaultViewMode();

      if ("list".equals(defViewMode)) {
        /* Set up the event list parameters */

        EventListPars elpars = new EventListPars();
        int forward = setEventListPars(request, elpars);

        if (forward != forwardSuccess) {
          return forward;
        }

        form.setEventListPars(elpars);

        return forwardListEvents;
      }
    }

    return forwardSuccess;
  }
}
