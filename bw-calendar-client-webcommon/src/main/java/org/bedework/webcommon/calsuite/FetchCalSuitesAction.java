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
package org.bedework.webcommon.calsuite;

import org.bedework.calfacade.responses.CalSuitesResponse;
import org.bedework.calfacade.svc.BwCalSuite;
import org.bedework.webcommon.BwAbstractAction;
import org.bedework.webcommon.BwRequest;

import jakarta.servlet.http.HttpServletResponse;

import java.util.Collection;

import static org.bedework.base.response.Response.Status.ok;

/** This action fetches all calendar suites.
 * READONLY + ADMIN
 *
 * <p>Forwards to:<ul>
 *      <li>"success"      ok.</li>
 * </ul>
 *
 * @author Mike Douglass   douglm@rpi.edu
 */
public class FetchCalSuitesAction extends BwAbstractAction {
  @Override
  public int doAction(final BwRequest request) {
    // Return as json list for widgets
    final Collection<BwCalSuite> vals =
            request.getClient().getContextCalSuites();

    // Do this if ws get a filename?
    //resp.setHeader("Content-Disposition",
    //               "Attachment; Filename=\"calSuites.json\"");

    final CalSuitesResponse csr = new CalSuitesResponse();
    csr.setCalSuites(vals);
    csr.setStatus(ok);

    final HttpServletResponse resp = request.getResponse();
    outputJson(resp, null, null, csr);

    return forwardNull;
  }
}
