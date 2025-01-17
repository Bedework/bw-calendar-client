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
package org.bedework.client.web.admin;

import org.bedework.appcommon.ClientError;
import org.bedework.base.exc.BedeworkAccessException;
import org.bedework.calfacade.BwGroup;
import org.bedework.calfacade.svc.BwAdminGroup;
import org.bedework.calfacade.svc.wrappers.BwCalSuiteWrapper;
import org.bedework.client.admin.AdminClient;
import org.bedework.util.logging.BwLogger;
import org.bedework.util.webaction.Request;
import org.bedework.webcommon.BwRequest;
import org.bedework.webcommon.ForwardDefs;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * @author Mike Douglass
 *
 */
public class AdminUtil implements ForwardDefs {
  private static final BwLogger logger =
          new BwLogger().setLoggedClass(AdminUtil.class);

  public static void embedPreferredAdminGroups(final BwRequest request) {
    final AdminClient cl = (AdminClient)request.getClient();

    final Set<String> prefGroupHrefs = cl.getPreferences().getPreferredGroups();

    final List<BwGroup<?>> prefGroups = new ArrayList<>(prefGroupHrefs.size());

    for (final String href: prefGroupHrefs) {
      final BwGroup<?> group = cl.getAdminGroup(href);

      if (group == null) {
        continue;
      }

      prefGroups.add(group);
    }

    request.setSessionAttr(BwRequest.bwPreferredAdminGroupsInfoName,
                           prefGroups);
  }

  public static void embedCalsuiteAdminGroups(final BwRequest request) {
    final AdminClient cl = (AdminClient)request.getClient();

    request.setSessionAttr(BwRequest.bwCsAdminGroupsInfoName,
                           cl.getCalsuiteAdminGroups());
  }

  /** For an administrative user this is how we determine the calendar suite
   * they are administering. We see if there is a suite associated with the
   * administrative group. If so, return.
   *
   * <p>If not we call ourselves for each parent group.
   *
   * <p>If none is found we return null.
   *
   * @param request the request object
   * @param cl client
   * @return calendar suite wrapper
   */
  public static BwCalSuiteWrapper findCalSuite(
          final Request request,
          final AdminClient cl) {
    final String groupName = cl.getAdminGroupName();
    if (groupName == null) {
      return null;
    }

    final BwAdminGroup adg = (BwAdminGroup)cl.findGroup(groupName);

    return findCalSuite(request, cl, adg);
  }

  /** For an administrative user this is how we determine the calendar suite
   * they are administering. We see if there is a suite associated with the
   * administrative group. If so, return.
   *
   * <p>If not we call ourselves for each parent group.
   *
   * <p>If none is found we return null.
   *
   * @param request the request object
   * @param cl client
   * @param adg admin group
   * @return calendar suite wrapper
   */
  private static BwCalSuiteWrapper findCalSuite(
          final Request request,
          final AdminClient cl,
          final BwAdminGroup adg) {
    if (adg == null) {
      return null;
    }

    /* At this point we still require at least authenticated read access to
     * the target calendar suite
     */

    try {
      BwCalSuiteWrapper cs = cl.getCalSuite(adg);
      if (cs != null) {
        return cs;
      }

      for (final BwGroup<?> parent: cl.findGroupParents(adg)) {
        cs = findCalSuite(request, cl, (BwAdminGroup)parent);
        if (cs != null) {
          return cs;
        }
      }
    } catch (final BedeworkAccessException ignored) {
      // Access is set incorrectly
      request.error(ClientError.noCalsuiteAccess, adg.getPrincipalRef());
    }

    return null;
  }
}
