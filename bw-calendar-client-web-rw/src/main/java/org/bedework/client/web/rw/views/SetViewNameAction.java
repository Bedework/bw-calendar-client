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

package org.bedework.client.web.rw.views;

import org.bedework.calfacade.exc.ValidationError;
import org.bedework.client.rw.RWClient;
import org.bedework.client.web.rw.BwRWActionForm;
import org.bedework.client.web.rw.RWActionBase;
import org.bedework.webcommon.BwRequest;

/** Set a view name for update/display/delete.
 *
 * <p>Parameters are:<ul>
 *      <li>"name"             Name of view</li>
 *      <li>"delete"           If fetch for delete</li>
 * </ul>
 *
 * <p>Forwards to:<ul>
 *   <li>"delete"       probably to confirm.</li>
 *   <li>"retry"        try again.</li>
 *   <li>"success"      ok.</li>
 * </ul>
 *
 * @author Mike Douglass
 */
public class SetViewNameAction extends RWActionBase {
  @Override
  public int doAction(final BwRequest request,
                      final RWClient cl,
                      final BwRWActionForm form) {
    final String name = request.getReqPar("name");

    if (name == null) {
      request.error(ValidationError.missingName);
      return forwardRetry;
    }

    form.setViewName(name);

    if (request.getReqPar("delete") != null) {
      return forwardDelete;
    }

    return forwardSuccess;
  }
}
