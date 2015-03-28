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
package org.bedework.webcommon.calendars;

import org.bedework.appcommon.AccessXmlUtil;
import org.bedework.appcommon.ClientError;
import org.bedework.appcommon.ClientMessage;
import org.bedework.appcommon.client.Client;
import org.bedework.calfacade.BwCalendar;
import org.bedework.calfacade.BwFilterDef;
import org.bedework.calfacade.CalFacadeDefs;
import org.bedework.calfacade.exc.CalFacadeException;
import org.bedework.calfacade.exc.ValidationError;
import org.bedework.util.misc.Util;
import org.bedework.util.xml.tagdefs.WebdavTags;
import org.bedework.webcommon.BwAbstractAction;
import org.bedework.webcommon.BwActionFormBase;
import org.bedework.webcommon.BwRequest;

import org.bedework.access.Acl;

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
public class UpdateCalendarAction extends BwAbstractAction {
  @Override
  public int doAction(final BwRequest request,
                      final BwActionFormBase form) throws Throwable {
    final Client cl = request.getClient();

    if (cl.isGuest()) {
      return forwardNoAccess; // First line of defence
    }

    if (request.present("access")) {
      // Fail this to stop someone screwing around with the access
      return forwardNoAccess;
    }

    final String reqpar = request.getReqPar("delete");

    if (reqpar != null) {
      return forwardDelete;
    }

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
          form.getErr().emit(ClientError.badRequest);
          return forwardContinue;
        }

        if (newCal.getCalendarCollection() ||
            !newCal.getCollectionInfo().childrenAllowed) {
          // Cannot be part of a calendar collection
          form.getErr().emit(ClientError.badRequest);
          return forwardContinue;
        }

        cl.moveCollection(cal, newCal);
        form.getMsg().emit(ClientMessage.updatedCalendar);

        return forwardContinue;
      }
    }

    /** We are just adding or updating from the current form values.
     */

    final Boolean bool = request.getBooleanReqPar("unremoveable");
    if (bool != null) {
      if (!form.getCurUserSuperUser()) {
        return forwardNoAccess; // Only super user for that flag
      }

      if (cal.getUnremoveable() != bool) {
        cal.setUnremoveable(bool);
      }
    }

    final BwFilterDef fd = request.getFilterDef();

    if (request.getErrorsEmitted()) {
      return forwardRetry;
    } else if (fd != null) {
      final String fdef = fd.getDefinition();

      if (!Util.equalsString(fdef, cal.getFilterExpr())) {
        cal.setFilterExpr(fdef);
      }
    } else if (cal.getFilterExpr() != null) {
      cal.setFilterExpr(null);
    }

    /* -------------------------- Categories ------------------------------ */
    final SetEntityCategoriesResult secr =
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
      } catch (final CalFacadeException cfe) {
        if (cfe.getMessage().equals(CalFacadeException.duplicateCalendar)) {
          form.getErr().emit(CalFacadeException.duplicateCalendar,
                             cal.getName());
          return forwardRetry;
        }
        throw cfe;
      }
      form.assignAddingCalendar(false);
    } else {
      cl.updateCollection(cal);
    }

    /* -------------------------- Access ------------------------------ */

    final String aclStr = request.getReqPar("acl");
    if (aclStr != null) {
      final AccessXmlUtil axu = new AccessXmlUtil(null, cl);

      final Acl acl = axu.getAcl(aclStr, true);

      if (axu.getErrorTag() != null) {
        if (axu.getErrorTag().equals(WebdavTags.recognizedPrincipal)) {
          form.getErr().emit(CalFacadeException.principalNotFound,
                             axu.getErrorMsg());
        } else {
          form.getErr().emit(CalFacadeException.badRequest, axu.getErrorTag());
        }
        return forwardRetry;
      }

      cl.changeAccess(cal, acl.getAces(), true);
    }

    form.setParentCalendarPath(null);

    if (cal.getCalendarCollection()) {
      if (add) {
        form.getMsg().emit(ClientMessage.addedCalendar);
      } else {
        form.getMsg().emit(ClientMessage.updatedCalendar);
      }
    } else {
      if (add) {
        form.getMsg().emit(ClientMessage.addedFolder);
      } else {
        form.getMsg().emit(ClientMessage.updatedFolder);
      }
    }

    /* redo filters */
    cl.flushState();

    return forwardContinue;
  }

  /* Set the collection type and alias
   */
  private void typeAndAlias(final BwRequest request,
                            final boolean add) throws Throwable {
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
    }

    // default to 15 mins refresh then turn to seconds
    final int refreshRate = request.getIntReqPar("refinterval", 15) * 60;

    if (cal.getRefreshRate() != refreshRate) {
      cal.setRefreshRate(refreshRate);
    }

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

    /* Set the  etag and last refresh to null to force a refresh */
//    cal.setLastRefresh(null);
  //  cal.setLastEtag(null);
  }

  /* Validate a calendar - we do not create these as a side effect.
   *
   * @return boolean  false means something wrong, message emitted
   */
  private boolean validateCalendar(final BwRequest request) throws Throwable {
    final BwActionFormBase form = request.getBwForm();
    boolean ok = true;

    final BwCalendar cal = form.getCalendar();

    if (cal.getName() == null) {
      form.getErr().emit(ValidationError.missingName);
      ok = false;
    }

    return ok;
  }
}

