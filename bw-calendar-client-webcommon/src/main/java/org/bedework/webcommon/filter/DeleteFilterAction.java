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
package org.bedework.webcommon.filter;

import org.bedework.appcommon.ClientError;
import org.bedework.calfacade.exc.CalFacadeException;
import org.bedework.calfacade.exc.ValidationError;
import org.bedework.client.rw.RWClient;
import org.bedework.webcommon.BwAbstractAction;
import org.bedework.webcommon.BwActionFormBase;
import org.bedework.webcommon.BwRequest;

/** Delete a filter.
 *
 * <p>Parameters are:<ul>
 *      <li>"name"             Name of filter</li>
 * </ul>
 *
 * <p>Forwards to:<ul>
 *      <li>"error"        some form of fatal error.</li>
 *      <li>"noAccess"     user not authorised.</li>
 *      <li>"noAction"     duplicate or bad name.</li>
 *      <li>"success"      created ok.</li>
 * </ul>
 *
 * @author Mike Douglass
 */
public class DeleteFilterAction extends BwAbstractAction {
  @Override
  public int doAction(final BwRequest request,
                      final BwActionFormBase form) throws Throwable {
    if (request.isGuest()) {
      return forwardNoAccess; // First line of defense
    }

    final RWClient cl = (RWClient)request.getClient();

    final String name = request.getReqPar("name");

    if (name == null) {
      form.getErr().emit(ValidationError.missingName);
      return forwardNoAction;
    }

    try {
      cl.deleteFilter(name);
    } catch (final CalFacadeException cfe) {
      if (cfe.getMessage().equals(CalFacadeException.unknownFilter)) {
        form.getErr().emit(ClientError.unknownFilter, name);
        return forwardNotFound;
      }

      form.getErr().emit(cfe);
      return forwardNoAction;
    } catch (final Throwable t) {
      form.getErr().emit(t);
      return forwardNoAction;
    }

    form.assignReloadRequired(true);

    return forwardSuccess;
  }
}
