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
package org.bedework.webcommon.resources;

import org.bedework.appcommon.client.Client;
import org.bedework.calfacade.BwResource;
import org.bedework.calfacade.exc.CalFacadeException;
import org.bedework.webcommon.BwActionFormBase;
import org.bedework.webcommon.BwRequest;
import org.bedework.webcommon.CalSuiteResource;
import org.bedework.webcommon.RenderAction;

import java.util.ArrayList;
import java.util.List;

/**
 * Render action used to display the list of resources for a calendar suite.
 *
 * @author eric.wittmann@redhat.com
 */
public class RenderResourcesAction extends RenderAction {
  /**
   * Constructor.
   */
  public RenderResourcesAction() {
  }

  /* (non-Javadoc)
   * @see org.bedework.webcommon.RenderAction#doAction(org.bedework.webcommon.BwRequest, org.bedework.webcommon.BwActionFormBase)
   */
  @Override
  public int doAction(BwRequest request, BwActionFormBase form) throws Throwable {
    if (form.getNewSession()) {
      form.refreshIsNeeded();
      return forwardGotomain;
    }

    List<CalSuiteResource> resources = getResources(form);

    // TODO: add admin-only suite resources if logged-in user is a superadmin
    form.setCalSuiteResources(resources);

    return forwardSuccess;
  }

  /**
   * Gets the resources to be displayed in the UI.
   * @param form
   * @throws CalFacadeException
   */
  protected List<CalSuiteResource> getResources(BwActionFormBase form)
      throws CalFacadeException {
    Client cl = form.fetchClient();

    List<CalSuiteResource> resources = new ArrayList<>();
    List<BwResource> bres = cl.getCSResources(form.getCurrentCalSuite(),
                                              "calsuite");
    if (bres != null) {
      for (BwResource r: bres) {
        resources.add(new CalSuiteResource(r,
                                           "calsuite"));
      }
    }

    if (form.getCurUserSuperUser()) {
      bres = cl.getCSResources(form.getCurrentCalSuite(),
                               "admin");
      if (bres != null) {
        for (BwResource r: bres) {
          resources.add(new CalSuiteResource(r, "admin"));
        }
      }
    }
    return resources;
  }
}
