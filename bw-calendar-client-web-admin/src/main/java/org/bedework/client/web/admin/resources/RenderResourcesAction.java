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

import org.bedework.appcommon.CalSuiteResource;
import org.bedework.calfacade.BwResource;
import org.bedework.calfacade.exc.CalFacadeException;
import org.bedework.calfacade.svc.BwCalSuite;
import org.bedework.client.admin.AdminClient;
import org.bedework.client.web.admin.AdminActionBase;
import org.bedework.client.web.admin.BwAdminActionForm;
import org.bedework.webcommon.BwRequest;

import java.util.ArrayList;
import java.util.List;

/** Render action used to display the list of resources for a calendar suite.
 * ADMIN ONLY
 *
 * @author eric.wittmann@redhat.com
 */
public class RenderResourcesAction extends AdminActionBase {
  @Override
  public int doAction(final BwRequest request,
                      final AdminClient cl) {
    final var form = getAdminForm();
    final var globals = request.getBwGlobals();
    if (request.isNewSession()) {
      request.refresh();
      return forwardGotomain;
    }

    final List<CalSuiteResource> resources =
            getResources(cl,
                         globals.getCurrentCalSuite());

    // TODO: add admin-only suite resources if logged-in user is a superadmin
    form.setCalSuiteResources(resources);

    return forwardSuccess;
  }

  /**
   * Gets the resources to be displayed in the UI.
   * @param cl the client
   * @param currentCalSuite calendar suite
   * @throws CalFacadeException on errors
   */
  protected List<CalSuiteResource> getResources(final AdminClient cl,
                                                final BwCalSuite currentCalSuite)
      throws CalFacadeException {
    final List<CalSuiteResource> resources = new ArrayList<>();
    List<BwResource> bres = cl.getCSResources(currentCalSuite,
                                              CalSuiteResource.resourceClassCalSuite);
    if (bres != null) {
      for (final BwResource r: bres) {
        resources.add(new CalSuiteResource(r,
                                           CalSuiteResource.resourceClassCalSuite));
      }
    }

    if (cl.isSuperUser()) {
      bres = cl.getCSResources(currentCalSuite,
                               CalSuiteResource.resourceClassAdmin);
      if (bres != null) {
        for (final BwResource r: bres) {
          resources.add(new CalSuiteResource(r,
                                             CalSuiteResource.resourceClassAdmin));
        }
      }
    }

    return resources;
  }

  @Override
  public boolean clearMessagesOnEntry() {
    return false;
  }
}
