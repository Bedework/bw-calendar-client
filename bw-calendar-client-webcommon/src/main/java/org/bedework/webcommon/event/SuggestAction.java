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
package org.bedework.webcommon.event;

import org.bedework.appcommon.ClientError;
import org.bedework.appcommon.client.Client;
import org.bedework.calfacade.BwCategory;
import org.bedework.calfacade.BwEvent;
import org.bedework.calfacade.BwXproperty;
import org.bedework.calfacade.RecurringRetrievalMode.Rmode;
import org.bedework.calfacade.exc.CalFacadeException;
import org.bedework.calfacade.svc.BwAdminGroup;
import org.bedework.calfacade.svc.EventInfo;
import org.bedework.calfacade.util.ChangeTable;
import org.bedework.calfacade.util.ChangeTableEntry;
import org.bedework.sysevents.events.SysEventBase;
import org.bedework.sysevents.events.publicAdmin.EntitySuggestedResponseEvent;
import org.bedework.util.calendar.PropertyIndex;
import org.bedework.util.misc.Util;
import org.bedework.webcommon.BwActionFormBase;
import org.bedework.webcommon.BwRequest;

import java.util.List;
import java.util.Set;

import javax.servlet.http.HttpServletResponse;

/**
 * Action to update suggest status for an event
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
public class SuggestAction extends EventActionBase {
  @Override
  public int doAction(final BwRequest request,
                      final BwActionFormBase form) throws Throwable {
    final Client cl = request.getClient();
    final HttpServletResponse response = request.getResponse();

    /* Check access
     */
    final boolean publicAdmin = cl.getPublicAdmin();
    if (cl.isGuest() || !publicAdmin || !form.getCurUserApproverUser()) {
      response.setStatus(HttpServletResponse.SC_FORBIDDEN);
      return forwardNull;
    }

    final EventInfo ei = findEvent(request, Rmode.overrides);

    if (ei == null) {
      // Do nothing
      response.setStatus(HttpServletResponse.SC_NOT_FOUND);
      return forwardNull;
    }

    final BwEvent ev = ei.getEvent();

    final boolean accept = request.present("accept");
    final boolean reject = request.present("reject");

    if ((reject && accept) || (!reject && !accept)) {
      form.getErr().emit(ClientError.badRequest);
      response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      return forwardNull;
    }

    final String csHref = form.getCurrentCalSuite().getGroup().getPrincipalRef();

    final List<BwXproperty> props =
            ev.getXproperties(BwXproperty.bedeworkSuggestedTo);

    if (Util.isEmpty(props)) {
      form.getErr().emit(ClientError.notSuggested);
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
      form.getErr().emit(ClientError.notSuggested);
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

    final String newSt = new BwEvent.SuggestedTo(newStatus,
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

    try {
      cl.updateEvent(ei, true, null,
                     true); // TODO - set this back to false after data is fixed
    } catch (final CalFacadeException cfe) {
      cl.rollback();
      throw cfe;
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
