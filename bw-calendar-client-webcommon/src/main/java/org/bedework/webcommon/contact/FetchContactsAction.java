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

package org.bedework.webcommon.contact;

import org.bedework.calfacade.BwContact;
import org.bedework.calfacade.responses.ContactsResponse;
import org.bedework.calfacade.responses.EventPropertiesResponse;
import org.bedework.base.response.GetEntitiesResponse;
import org.bedework.webcommon.BwRequest;
import org.bedework.webcommon.BwSession;
import org.bedework.webcommon.FetchEventPropertiesAction;

import java.util.Collection;

/** This action fetches contacts and writes them as a json object.
 *
 * <p>Forwards to:<ul>
 *      <li>null     always.</li>
 * </ul>
 *
 * <p>Request param is kind: value may be <ul>
 *      <li>"owners"       all owners locations.</li>
 *      <li>"editable"     the locations we can edit.</li>
 *      <li>"preferred"    our preferred locations.</li>
 * </ul>
 *
 * Default is "owners"</p>
 *
 * @author Mike Douglass   douglm   rpi.edu
 */
public class FetchContactsAction
        extends FetchEventPropertiesAction<BwContact> {
  @Override
  protected Collection<BwContact> getEProps(final BwRequest request,
                                         final int kind) {
    final BwSession sess = request.getSess();
    final var includeArchived =
            request.getBooleanReqPar("includeArchived", false);

    return sess.getContacts(request, BwSession.ownersEntity,
                            includeArchived, false);
  }

  @Override
  protected GetEntitiesResponse<BwContact> search(
          final BwRequest request,
          final String fexpr) {
    return request.getClient().getContacts(
            fexpr,
            request.getIntReqPar("from", 0),
            request.getIntReqPar("size", 10));
  }

  @Override
  protected EventPropertiesResponse makeResponse(
          final Collection<BwContact> eprops) {
    return new ContactsResponse().setContacts(eprops);
  }
}
