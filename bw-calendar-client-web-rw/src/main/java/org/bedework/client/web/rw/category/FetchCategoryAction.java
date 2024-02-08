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
package org.bedework.client.web.rw.category;

import org.bedework.appcommon.ClientError;
import org.bedework.calfacade.BwCategory;
import org.bedework.client.rw.RWClient;
import org.bedework.client.web.rw.BwRWActionForm;
import org.bedework.client.web.rw.RWActionBase;
import org.bedework.webcommon.BwRequest;

/** This action fetches a category for update.
 *
 * <p>Request parameters<ul>
 *      <li>catUid           The key.</li>
 * </ul>
 *.
 * <p>Forwards to:<ul>
 *      <li>"noAccess"     user not authorised.</li>
 *      <li>"notFound"     no such event.</li>
 *      <li>"continue"     continue on to update page.</li>
 * </ul>
 *
 * @author Mike Douglass   douglm  rpi.edu
 */
public class FetchCategoryAction extends RWActionBase {
  @Override
  public int doAction(final BwRequest request,
                      final RWClient cl,
                      final BwRWActionForm form) {
    /* User requested a category from the list. Retrieve it, embed it in
     * the form so we can display the page
     */
    final String catUid = request.getReqPar("catUid");

    if (catUid == null) {
      request.error(ClientError.unknownCategory, "null");
      return forwardNotFound;
    }

    final BwCategory category = request.getClient().getCategoryByUid(catUid);

    if (debug()) {
      if (category == null) {
        info("No category with uid " + catUid);
      } else {
        info("Retrieved category " + catUid);
      }
    }

    form.setCategory(category);
    if (category == null) {
      request.error(ClientError.unknownCategory, catUid);
      return forwardNotFound;
    }


    form.assignAddingCategory(false);
    return forwardContinue;
  }
}
