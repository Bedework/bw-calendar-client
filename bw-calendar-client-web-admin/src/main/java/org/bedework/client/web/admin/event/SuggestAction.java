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
package org.bedework.client.web.admin.event;

import org.bedework.appcommon.ClientError;
import org.bedework.calfacade.BwCategory;
import org.bedework.calfacade.BwEvent;
import org.bedework.calfacade.BwXproperty;
import org.bedework.calfacade.RecurringRetrievalMode.Rmode;
import org.bedework.calfacade.svc.BwAdminGroup;
import org.bedework.calfacade.svc.EventInfo;
import org.bedework.calfacade.util.ChangeTable;
import org.bedework.calfacade.util.ChangeTableEntry;
import org.bedework.client.admin.AdminClient;
import org.bedework.client.web.admin.AdminActionBase;
import org.bedework.client.web.admin.BwAdminWebGlobals;
import org.bedework.sysevents.events.SysEventBase;
import org.bedework.sysevents.events.publicAdmin.EntitySuggestedResponseEvent;
import org.bedework.util.calendar.PropertyIndex;
import org.bedework.util.misc.Util;
import org.bedework.webcommon.BwRequest;

import java.util.List;
import java.util.Set;

import jakarta.servlet.http.HttpServletResponse;

import static org.bedework.webcommon.event.EventUtil.findEvent;

/**Action to update suggest status for an event
 * ADMIN ONLY
 *
 * <p>Request parameters:<ul>
 *      <li>  colPath    - collection href</li>.
 *      <li>  eventName  - name for event</li>.
 *      <li>  accept | reject  - only one must be present </li>.
 * </ul>
 * <p>Errors:<ul>
 *      <li>org.bedework.error.noaccess </li>
 *      <li>org.bedework.error.not.suggested - when
 *            not a suggested event</li>
 * </ul>
 */
public class SuggestAction extends AdminActionBase {
  @Override
  public int doAction(final BwRequest request,
                      final AdminClient cl) {
    final var globals = (BwAdminWebGlobals)request.getGlobals();
    final HttpServletResponse response = request.getResponse();

    /* Check access
     */
    if (!globals.getCurUserApproverUser()) {
      response.setStatus(HttpServletResponse.SC_FORBIDDEN);
      return forwardNull;
    }

    final EventInfo ei = findEvent(request, Rmode.overrides, getLogger());

    if (ei == null) {
      // Do nothing
      response.setStatus(HttpServletResponse.SC_NOT_FOUND);
      return forwardNull;
    }

    final BwEvent ev = ei.getEvent();

    final boolean accept = request.present("accept");
    final boolean reject = request.present("reject");

    if ((reject && accept) || (!reject && !accept)) {
      request.error(ClientError.badRequest);
      response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      return forwardNull;
    }

    final String csHref = globals.getCurrentCalSuite().getGroup().getPrincipalRef();

    final List<BwXproperty> props =
            ev.getXproperties(BwXproperty.bedeworkSuggestedTo);

    if (Util.isEmpty(props)) {
      request.error(ClientError.notSuggested);
      response.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
      return forwardNull;
    }

    BwXproperty theProp = null;
    BwEvent.SuggestedTo st = null;

    for (final BwXproperty prop: props) {
      st = new BwEvent.SuggestedTo(prop.getValue());
      if (st.getGroupHref().equals(csHref)) {
        theProp = prop;
        break;
      }
    }

    if (theProp == null) {
      request.error(ClientError.notSuggested);
      response.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
      return forwardNull;
    }

    final ChangeTable changes = ei.getChangeset(
            cl.getCurrentPrincipalHref());
    final char newStatus;

    if (accept) {
      newStatus = 'A';
    } else {
      newStatus = 'R';
    }

    final String newSt =
            new BwEvent.SuggestedTo(newStatus,
                                    st.getGroupHref(),
                                    st.getSuggestedByHref()).toString();
    /* TODO - reenable after data fixed
    if (newSt.equals(st.toString())) {
      response.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
      return forwardNull;
    }
    */

    theProp.setValue(newSt);

    final ChangeTableEntry xpCte = changes.getEntry(
            PropertyIndex.PropertyInfoIndex.XPROP);
    xpCte.addChangedValue(theProp);

    final Set<String> catuids = cl.getCalsuitePreferences().getDefaultCategoryUids();
    final ChangeTableEntry catCte = changes.getEntry(
            PropertyIndex.PropertyInfoIndex.CATEGORIES);

    if (debug()) {
      debug("About to add " + catuids.size() + " categories");
    }

    for (final String uid: catuids) {
      final BwCategory cat = cl.getPersistentCategory(uid);

      if (cat != null) {
        if (debug()) {
          debug("Found " + cat.getWordVal());
        }

        if (accept) {
          ev.addCategory(cat);
          catCte.addAddedValue(cat);
        } else {
          ev.removeCategory(cat);
          catCte.addRemovedValue(cat);
        }
      } else {
        warn("Default category uid " + uid + " is missing");
      }
    }

    if (!cl.updateEvent(ei, true, null,
                        true).isOk()) {
      cl.rollback();
      return forwardError;
    }

    final BwAdminGroup grp =
            (BwAdminGroup)cl.getAdminGroup(st.getSuggestedByHref());

    if (grp == null) {
      warn("Unable to locate group " + st.getSuggestedByHref());
    } else {
      final EntitySuggestedResponseEvent esre =
              new EntitySuggestedResponseEvent(
                      SysEventBase.SysCode.SUGGESTED_RESPONSE,
                      cl.getCurrentPrincipalHref(),
                      ev.getCreatorHref(),
                      ev.getHref(),
                      null,
                      grp.getOwnerHref(),
                      accept);
      cl.postNotification(esre);
    }

    response.setStatus(HttpServletResponse.SC_NO_CONTENT);
    response.setContentLength(0);

    return forwardNull;
  }
}
