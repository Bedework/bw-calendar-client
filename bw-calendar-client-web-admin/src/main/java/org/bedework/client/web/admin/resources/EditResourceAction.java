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

package org.bedework.client.web.admin.resources;

import org.bedework.client.admin.AdminClient;
import org.bedework.client.web.admin.AdminActionBase;
import org.bedework.client.web.admin.BwAdminActionForm;
import org.bedework.webcommon.BwRequest;

/** Shows a single resource, for editing purposes.
 * ADMIN ONLY
 *
 * <p>Forwards to:<ul>
 *      <li>"success"      show edit form.</li>
 *      <li>"retry"        back to resource list.</li>
 * </ul>
 *
 * @author eric.wittmann@redhat.com
 */
public class EditResourceAction extends AdminActionBase {
  @Override
  public int doAction(final BwRequest request,
                      final AdminClient cl,
                      final BwAdminActionForm form) throws Throwable {
    final String name = request.getReqPar("name");
    final String class_ = request.getReqPar("class");

    form.setResourceName(name);
    form.setResourceClass(class_);
    form.assignAddingResource(false);

    return forwardSuccess;
  }
}
