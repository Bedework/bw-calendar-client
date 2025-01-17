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
package org.bedework.client.web.rw.filter;

import org.bedework.appcommon.ClientError;
import org.bedework.base.exc.BedeworkException;
import org.bedework.calfacade.exc.CalFacadeErrorCode;
import org.bedework.calfacade.exc.ValidationError;
import org.bedework.client.rw.RWClient;
import org.bedework.client.web.rw.RWActionBase;
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
public class DeleteFilterAction extends RWActionBase {
  @Override
  public int doAction(final BwRequest request,
                      final RWClient cl) {
    final String name = request.getReqPar("name");

    if (name == null) {
      request.error(ValidationError.missingName);
      return forwardNoAction;
    }

    try {
      cl.deleteFilter(name);
    } catch (final BedeworkException be) {
      if (be.getMessage().equals(CalFacadeErrorCode.unknownFilter)) {
        request.error(ClientError.unknownFilter, name);
        return forwardNotFound;
      }

      request.error(be);
      return forwardNoAction;
    } catch (final Throwable t) {
      request.error(t);
      return forwardNoAction;
    }

    return forwardSuccess;
  }
}
