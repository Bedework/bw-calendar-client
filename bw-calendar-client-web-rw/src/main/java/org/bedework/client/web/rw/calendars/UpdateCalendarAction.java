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
package org.bedework.client.web.rw.calendars;

import org.bedework.access.Acl;
import org.bedework.appcommon.AccessXmlUtil;
import org.bedework.appcommon.ClientError;
import org.bedework.appcommon.ClientMessage;
import org.bedework.base.exc.BedeworkException;
import org.bedework.calfacade.BwCalendar;
import org.bedework.calfacade.CalFacadeDefs;
import org.bedework.calfacade.exc.CalFacadeErrorCode;
import org.bedework.calfacade.exc.ValidationError;
import org.bedework.calfacade.responses.GetFilterDefResponse;
import org.bedework.calsvci.EventsI;
import org.bedework.client.rw.RWClient;
import org.bedework.client.web.rw.RWActionBase;
import org.bedework.util.misc.Util;
import org.bedework.util.misc.response.Response;
import org.bedework.util.xml.tagdefs.WebdavTags;
import org.bedework.webcommon.BwActionFormBase;
import org.bedework.webcommon.BwRequest;

import static org.bedework.client.web.rw.EventCommon.setEntityCategories;

/** This action updates a calendar.
 *
 * <p>Parameters are:<ul>
 *      <li>"calendar.summary"            Summary for calendar</li>
 *      <li>"calendar.description"        Description for calendar</li>
 *      <li>"calendar.calendarCollection" Calendar/Folder flag   true/false</li>
 *      <li>"calendar.affectsFreeBusy"    </li>
 *      <li>"calendar.display"    </li>
 *      <li>"unremoveable"   admin only</li>
 *      <li>"aliasUri"       if present creates an alias</li>
 *      <li>"remoteId"       if needed an id for an external subscription"  </li>
 *      <li>"remotePw"       if needed a pw for an external subscription"  </li>
 *      <li>"fexpr"          if the user wants filtering"  </li>
 * </ul>
 *
 * <p>Forwards to:<ul>
 *      <li>"noAccess"     user not authorised.</li>
 *      <li>"error"        for problems.</li>
 *      <li>"notFound"     no such calendar.</li>
 *      <li>"continue"     continue on to update page.</li>
 *      <li>"delete"       for confirmation.</li>
 * </ul>
 *
 * @author Mike Douglass   douglm@rpi.edu
 */
public class UpdateCalendarAction extends RWActionBase {
  @Override
  public int doAction(final BwRequest request,
                      final RWClient cl) {
    if (request.present("access")) {
      // Fail this to stop someone screwing around with the access
      return forwardNoAccess;
    }

    final String reqpar = request.getReqPar("delete");

    if (reqpar != null) {
      return forwardDelete;
    }

    final var form = getRwForm();
    final boolean add = form.getAddingCalendar();

    final BwCalendar cal = form.getCalendar();

    if (!add) {
      // See if we're moving

      final BwCalendar newCal = request.getNewCal(false);
      if (newCal != null) {
        if (newCal.getPath().equals(cal.getColPath())) {
          // Null move
          return forwardContinue;
        }

        if (newCal.equals(cal)) {
          // Cannot be your own parent (except in Red Dwarf)
          request.error(ClientError.badRequest);
          return forwardContinue;
        }

        if (newCal.getCalendarCollection() ||
            !newCal.getCollectionInfo().childrenAllowed) {
          // Cannot be part of a calendar collection
          request.error(ClientError.badRequest);
          return forwardContinue;
        }

        if (cal.getPublick()) {
          request.getSess().flushPublicCache();
        }
        cl.moveCollection(cal, newCal);
        request.message(ClientMessage.updatedCalendar);

        return forwardContinue;
      }
    }

    if (!add && !request.empty("refresh")) {
      final var response = cl.refreshSubscription(cal);
      if (response.isOk()) {
        request.message(ClientMessage.refreshedCalendar);

        return forwardContinue;
      }

      request.error(ClientError.refreshCalendarFailed,
                    response.getMessage());
      return forwardContinue;
    }

    /* We are just adding or updating from the current form values.
     */

    final Boolean bool = request.getBooleanReqPar("unremoveable");
    if (bool != null) {
      if (!cl.isSuperUser()) {
        return forwardNoAccess; // Only superuser for that flag
      }

      if (cal.getUnremoveable() != bool) {
        cal.setUnremoveable(bool);
      }
    }

    // Checkboxes
    cal.setPrimaryCollection(
            request.getBooleanReqPar("calendar.primaryCollection", false));

    cal.setDisplay(
            request.getBooleanReqPar("calendar.display",
                                     false));

    final GetFilterDefResponse gfdr = request.getFilterDef();

    if (gfdr.getStatus() == Response.Status.notFound) {
      cal.setFilterExpr(null);
    } else if (gfdr.getStatus() != Response.Status.ok) {
      return forwardRetry;
    } else if (gfdr.getFilterDef() == null) {
      cal.setFilterExpr(null);
    } else {
      final String fdef = gfdr.getFilterDef().getDefinition();

      if (!Util.equalsString(fdef, cal.getFilterExpr())) {
        cal.setFilterExpr(fdef);
      }
    }

    /* -------------------------- Categories ------------------------------ */
    final EventsI.SetEntityCategoriesResult secr =
            setEntityCategories(request, null,
                                cal, null);
    if (secr.rcode != forwardSuccess) {
      return secr.rcode;
    }

    typeAndAlias(request, add);

    cal.setSummary(Util.checkNull(cal.getSummary()));
    cal.setDescription(Util.checkNull(cal.getDescription()));

    if (!validateCalendar(request)) {
      if (add) {
        return forwardNotAdded;
      } else {
        return forwardRetry;
      }
    }

    if (add) {
      final String parentPath = form.getParentCalendarPath();

      if (parentPath == null) {
        return forwardRetry;
      }

      try {
        form.setCalendar(cl.addCollection(cal, parentPath));
      } catch (final BedeworkException be) {
        if (be.getMessage().equals(CalFacadeErrorCode.duplicateCalendar)) {
          request.error(CalFacadeErrorCode.duplicateCalendar,
                        cal.getName());
          return forwardRetry;
        }
        throw be;
      }
      form.assignAddingCalendar(false);
    } else {
      cl.updateCollection(cal);
    }

    /* -------------------------- Access ------------------------------ */

    final String aclStr = request.getReqPar("acl");
    if (aclStr != null) {
      final AccessXmlUtil axu = new AccessXmlUtil(null, cl);

      try {
        final Acl acl = axu.getAcl(aclStr, true);

        if (axu.getErrorTag() != null) {
          if (axu.getErrorTag()
                 .equals(WebdavTags.recognizedPrincipal)) {
            request.error(CalFacadeErrorCode.principalNotFound,
                          axu.getErrorMsg());
          } else {
            request.error(CalFacadeErrorCode.badRequest,
                          axu.getErrorTag());
          }
          return forwardRetry;
        }

        cl.changeAccess(cal, acl.getAces(), true);
      } catch (final Throwable t) {
        throw new BedeworkException(t);
      }
    }

    form.setParentCalendarPath(null);

    if (cal.getCalendarCollection()) {
      if (add) {
        request.message(ClientMessage.addedCalendar);
      } else {
        request.message(ClientMessage.updatedCalendar);
      }
    } else {
      if (add) {
        request.message(ClientMessage.addedFolder);
      } else {
        request.message(ClientMessage.updatedFolder);
      }
    }

    request.getSess().flushPublicCache();

    /* redo filters */
    cl.flushState();

    return forwardContinue;
  }

  /* Set the collection type and alias
   */
  private void typeAndAlias(final BwRequest request,
                            final boolean add) {
    final BwActionFormBase form = request.getBwForm();

    final BwCalendar cal = form.getCalendar();
    boolean extSub = false;

    final String aliasUri = request.getReqPar("aliasUri");

    if (add) {
      final int calType;
      // See what type we are creating
      final boolean cc = request.getBooleanReqPar("calendarCollection", false);

      if (aliasUri != null) {
        final boolean internal = aliasUri.startsWith(CalFacadeDefs.bwUriPrefix);
        if (internal) {
          calType = BwCalendar.calTypeAlias;
        } else {
          calType = BwCalendar.calTypeExtSub;
          extSub = true;
        }
      } else if (cc) {
        calType = BwCalendar.calTypeCalendarCollection;
      } else {
        calType = BwCalendar.calTypeFolder;
      }

      cal.setName(Util.checkNull(cal.getName()));

      // This only works because the calendar is not wrapped
      cal.setCalType(calType);
    }

    /* If non-null will set the calType */
    if (!Util.equalsString(aliasUri, cal.getAliasUri())) {
      cal.setAliasUri(aliasUri);
    }

    if (extSub) {
      cal.setSynchAdminCreateEprops(request.present("adminAllowCreateEprops"));
      cal.setSynchXlocXcontacts(request.present("xlocxcontact"));
      cal.setSynchXcategories(request.present("xcategories"));
      cal.setSynchDeleteSuppressed(request.present("deleteSuppressed"));
    }

    // default to 15 mins refresh then turn to seconds
    final int refreshRate = request.getIntReqPar("refinterval", 15) * 60;

    if (cal.getRefreshRate() != refreshRate) {
      cal.setRefreshRate(refreshRate);
    }

    // Mozilla autofills hidden fields. Don't do this for public subscriptions.
    if (!"public".equals(request.getReqPar("subType"))) {
      final String remoteId = request.getReqPar("remoteId");
      if (!Util.equalsString(remoteId, cal.getRemoteId())) {
        cal.setRemoteId(remoteId);
      }

      String remotePw = request.getReqPar("remotePw");

      if (remotePw != null) {
      /* I really want to do this
      remotePw = form.fetchSvci().getEncrypter().encrypt(remotePw);
      */
        cal.setRemotePw(remotePw);
        cal.setPwNeedsEncrypt(true);
        //noinspection UnusedAssignment
        remotePw = null;
      }
    }

    final boolean campusGroups = request.present("campusGroups");
    if (campusGroups) {
      cal.setSubscriptionTargetType("campusGroups");
    } else {
      final boolean orgSyncV2 = request.present("orgSyncV2");
      if (orgSyncV2) {
        cal.setSubscriptionTargetType("orgSyncV2");
        cal.setOrgSyncPublicOnly(request.present("orgSyncPublicOnly"));
      } else {
        cal.setSubscriptionTargetType("read-only-file");
      }
    }

    cal.setLocationKey(request.getReqPar("locKey"));

    /* Set the  etag and last refresh to null to force a refresh */
//    cal.setLastRefresh(null);
  //  cal.setLastEtag(null);
  }

  /* Validate a calendar - we do not create these as a side effect.
   *
   * @return boolean  false means something wrong, message emitted
   */
  private boolean validateCalendar(final BwRequest request) {
    final BwActionFormBase form = request.getBwForm();
    boolean ok = true;

    final BwCalendar cal = form.getCalendar();

    if (cal.getName() == null) {
      request.error(ValidationError.missingName);
      ok = false;
    }

    return ok;
  }
}

