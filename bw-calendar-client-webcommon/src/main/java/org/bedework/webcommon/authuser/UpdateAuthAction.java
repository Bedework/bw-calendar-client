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
package org.bedework.webcommon.authuser;

import org.bedework.appcommon.ClientMessage;
import org.bedework.calfacade.BwAuthUser;
import org.bedework.calsvci.CalSvcI;
import org.bedework.webcommon.BwAbstractAction;
import org.bedework.webcommon.BwActionFormBase;
import org.bedework.webcommon.BwRequest;

/** This action updates an authorised user
 *
 * <p>Forwards to:<ul>
 *      <li>forwardNoAccess     user not authorised.</li>
 *      <li>forwardRetry        invalid entitly.</li>
 *      <li>forwardContinue     continue on to update page.</li>
 * </ul>
 *
 * @author Mike Douglass   douglm@rpi.edu
 */
public class UpdateAuthAction extends BwAbstractAction {
  /* (non-Javadoc)
   * @see org.bedework.webcommon.BwAbstractAction#doAction(org.bedework.webcommon.BwRequest, org.bedework.webcommon.BwActionFormBase)
   */
  public int doAction(BwRequest request,
                      BwActionFormBase form) throws Throwable {
    /** Check access
     */
    if (!form.getCurUserSuperUser()) {
      return forwardNoAccess;
    }

    CalSvcI svci = form.fetchSvci();

    /** We are just updating from the current form values.
     */
    BwAuthUser au = validateAuthUser(form);
    if (au == null) {
      return forwardRetry;
    }

    if (debug) {
      debugMsg("Update authUser " + au);
    }

    svci.getUserAuth().updateUser(au);

    /** Refetch the list
     */
    form.setAuthUsers(form.fetchSvci().getUserAuth().getAll());

    form.getMsg().emit(ClientMessage.updatedAuthuser);

    return forwardContinue;
  }

  /**
   *
   * @param form
   * @return BwAuthUser  null means something wrong, message emitted
   * @throws Throwable
   */
  public BwAuthUser validateAuthUser(BwActionFormBase form) throws Throwable {
    BwAuthUser au = form.getEditAuthUser();

    au.setUsertype(form.getEditAuthUserType());

    return au;
  }
}

