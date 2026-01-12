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

import org.bedework.base.response.Response;
import org.bedework.client.rw.RWClient;
import org.bedework.client.web.rw.RWActionBase;
import org.bedework.webcommon.BwRequest;

/** Set the current filter.
 *
 * <p>Parameters are:<ul>
 *   <li>"filterName"            Name of filter</li>
 *   <li>"fexpr"                 Filter expression</li>
 * </ul>
 *
 * <p>If both absent unset any filter.
 *
 * <p>Forwards to:<ul>
 *      <li>"notFound"     filter not found.</li>
 *      <li>"success"      created ok.</li>
 * </ul>
 *
 * @author Mike Douglass   douglm     rpi.edu
 */
public class SetFilterAction extends RWActionBase {
  @Override
  public String doAction(final BwRequest request,
                         final RWClient cl) {
    final var gfdr = request.getFilterDef();

    if (gfdr.getStatus() == Response.Status.notFound) {
      return forwardNotFound;
    }

    if (gfdr.getStatus() != Response.Status.ok) {
      return forwardError;
    }
    
    getRwForm().setCurrentFilter(gfdr.getFilterDef());
    request.refresh();
    return forwardSuccess;
  }
}
