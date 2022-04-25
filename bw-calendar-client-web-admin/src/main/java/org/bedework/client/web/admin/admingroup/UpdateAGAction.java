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
package org.bedework.client.web.admin.admingroup;

import org.bedework.appcommon.ClientError;
import org.bedework.appcommon.ClientMessage;
import org.bedework.calfacade.BwPrincipal;
import org.bedework.calfacade.DirectoryInfo;
import org.bedework.calfacade.exc.CalFacadeException;
import org.bedework.calfacade.exc.ValidationError;
import org.bedework.calfacade.svc.BwAdminGroup;
import org.bedework.calfacade.svc.BwAuthUser;
import org.bedework.calfacade.svc.UserAuth;
import org.bedework.client.admin.AdminClient;
import org.bedework.client.web.admin.AdminActionBase;
import org.bedework.client.web.admin.BwAdminActionForm;
import org.bedework.webcommon.BwActionFormBase;
import org.bedework.webcommon.BwRequest;

import static org.bedework.util.misc.Util.checkNull;

/** This action updates an admin group
 * ADMIN ONLY
 *
 * <p>Parameters are:<ul>
 *      <li>"delete"           Delete current admin group</li>
 *      <li>"addGroupMember"   Add member to current group</li>
 *      <li>"removeGroupMember"  Remove member from current group.</li>
 *      <li>"kind"             Kind of member, "group" or "user".</li>
 *      <li>"view"             Optional name of view to which we add subscription.</li>
 *      <li>"addtodefaultview" Optional y/n to add to default view.</li>
 * </ul>
 *
 * <p>Forwards to:<ul>
 *      <li>forwardNoAccess     user not authorised.</li>
 *      <li>forwardNotFound     no such event.</li>
 *      <li>forwardContinue     continue on to update page.</li>
 * </ul>
 *
 * @author Mike Douglass   douglm rpi.edu
 */
public class UpdateAGAction extends AdminActionBase {
  @Override
  public int doAction(final BwRequest request,
                      final AdminClient cl,
                      final BwAdminActionForm form) throws Throwable {

    /* Check access
     */
    if (!cl.isSuperUser()) {
      return forwardNoAccess;
    }

    if (request.getReqPar("delete") != null) {
      return forwardDelete;
    }

    cl.setChoosingGroup(false); // reset
    final boolean add = form.getAddingAdmingroup();

    final BwAdminGroup updgrp = form.getUpdAdminGroup();

    if (updgrp == null) {
      // That's not right
      return forwardDone;
    }

    try {
      if (request.getReqPar("addGroupMember") != null) {
        /* Add a user to the group we are updating.
         */
        final String mbr = checkNull(request.getReqPar("updGroupMember"));
        if (mbr == null) {
          return forwardContinue;
        }

        final String kind = request.getReqPar("kind");
        if (!validateKind(kind, form)) {
          return forwardRetry;
        }

        if (updgrp.isMember(mbr, "group".equals(kind))) {
          request.error(ClientError.alreadyMember, mbr);
          return forwardRetry;
        }

        final BwPrincipal newMbr;

        if ("user".equals(kind)) {
          BwPrincipal p = cl.getUser(mbr);

          if (p == null) {
            cl.addUser(mbr);
            p = cl.getUser(mbr);
          }

          /* Ensure the authorised user exists - create an entry if not
           *
           * @param val      BwUser account
           */

          BwAuthUser au = cl.getAuthUser(p);

          if ((au != null) && au.isUnauthorized()) {
            return forwardNotAllowed;
          }

          if (au == null) {
            au = BwAuthUser.makeAuthUser(p.getPrincipalRef(),
                                         UserAuth.publicEventUser);
            cl.addAuthUser(au);
          }

          newMbr = p;
        } else {
          // group
          newMbr = cl.findGroup(mbr);

          if (newMbr == null) {
            request.error(ClientError.unknownGroup, mbr);
            return forwardRetry;
          }
        }

        cl.addAdminGroupMember(updgrp, newMbr);
        updgrp.addGroupMember(newMbr);
      } else if (request.getReqPar("removeGroupMember") != null) {
        /* Remove a user or group from the group we are updating.
         */
        final String mbr = request.getReqPar("removeGroupMember");

        final String kind = request.getReqPar("kind");
        if (!validateKind(kind, form)) {
          return forwardRetry;
        }

        final BwPrincipal oldMbr;

        if ("user".equals(kind)) {
          oldMbr = cl.getUser(mbr);
        } else {
          // group
          oldMbr = cl.findAdminGroup(mbr);
        }

        if (oldMbr != null) {
          cl.removeAdminGroupMember(updgrp, oldMbr);
          updgrp.removeGroupMember(oldMbr);
        }
      } else if (add) {
        if (!validateNewAdminGroup(request, cl)) {
          return forwardRetry;
        }

        cl.addAdminGroup(updgrp);

        form.assignAddingAdmingroup(false);
      } else {
        if (!validateAdminGroup(cl, form)) {
          return forwardRetry;
        }

        if (debug()) {
          debug("About to update " + updgrp);
        }
        cl.updateAdminGroup(updgrp);
      }

      cl.refreshAdminGroups();
    } catch (final CalFacadeException cfe) {
      final String msg = cfe.getMessage();

      switch (msg) {
        case CalFacadeException.duplicateAdminGroup:
          request.error(ClientError.duplicateGroup,
                        updgrp.getAccount());
          return forwardRetry;
        case CalFacadeException.alreadyOnGroupPath:
          request.error(ClientError.onGroupPath,
                        updgrp.getAccount());
          return forwardRetry;
        default:
          throw cfe;
      }
    }

    /* Refetch the group
     * /

    updgrp = (BwAdminGroup)adgrps.findGroup(updgrp.getAccount());

    adgrps.getMembers(updgrp);

    form.setUpdAdminGroup(updgrp);
    */

    request.message(ClientMessage.updatedGroup);
    return forwardContinue;
  }

  /**
   * @param val Admin group group owner
   */
  public void setAdminGroupGroupOwner(final String val) {
    getAdminForm().setAdminGroupGroupOwner(val);
  }

  /**
   * @return group owner
   */
  public String getAdminGroupGroupOwner() {
    return getAdminForm().getAdminGroupGroupOwner();
  }

  /**
   * @param val event owner
   */
  public void setAdminGroupEventOwner(final String val) {
    getAdminForm().setAdminGroupEventOwner(val);
  }

  /**
   * @return owner
   */
  public String getAdminGroupEventOwner() {
    return  getAdminForm().getAdminGroupEventOwner();
  }

  private boolean validateNewAdminGroup(final BwRequest request,
                                        final AdminClient cl) {
    boolean ok = true;

    final BwAdminGroup updAdminGroup = getAdminForm().getUpdAdminGroup();

    if (updAdminGroup == null) {
      // bogus call.
      return false;
    }

    updAdminGroup.setAccount(checkNull(updAdminGroup.getAccount()));

    if (updAdminGroup.getAccount() == null) {
      request.error(ValidationError.missingName);
      ok = false;
    } else {
      final DirectoryInfo di =  cl.getDirectoryInfo();
      String href = di.getBwadmingroupPrincipalRoot();
      if (!href.endsWith("/")) {
        href += "/";
      }

      updAdminGroup.setPrincipalRef(href + updAdminGroup.getAccount());
    }

    updAdminGroup.setDescription(checkNull(updAdminGroup.getDescription()));

    if (updAdminGroup.getDescription() == null) {
      request.error(ValidationError.missingDescription);
      ok = false;
    }

    final String adminGroupGroupOwner = checkNull(getAdminForm().getAdminGroupGroupOwner());
    if (adminGroupGroupOwner == null) {
      request.error(ValidationError.missingGroupOwner);
      ok = false;
    } else {
      updAdminGroup.setGroupOwnerHref(cl.getUserAlways(adminGroupGroupOwner).getPrincipalRef());
    }

    String adminGroupEventOwner = checkNull(getAdminForm().getAdminGroupEventOwner());
    if (adminGroupEventOwner == null) {
      adminGroupEventOwner = updAdminGroup.getAccount();
    }
    if (adminGroupEventOwner == null) {
      request.error(ValidationError.missingEventOwner);
      ok = false;
    } else {
      final String prefix = cl.getAdminGroupsIdPrefix();

      if (cl.isPrincipal(adminGroupEventOwner)) {
        // XXX For the moment just strip down to the account
        adminGroupEventOwner = adminGroupEventOwner.substring(adminGroupEventOwner.lastIndexOf("/") + 1);
      }

      if (!adminGroupEventOwner.startsWith(prefix)) {
        adminGroupEventOwner = prefix + adminGroupEventOwner;
      }

      updAdminGroup.setOwnerHref(cl.getUserAlways(adminGroupEventOwner).getPrincipalRef());
    }

    return ok;
  }

  private boolean validateAdminGroup(final AdminClient cl,
                                     final BwAdminActionForm form) throws Throwable {
    boolean ok = true;

    final BwAdminGroup updAdminGroup = form.getUpdAdminGroup();

    if (updAdminGroup == null) {
      // bogus call.
      return false;
    }

    /* We should see if somebody tried to change the name of the group */

    updAdminGroup.setDescription(checkNull(updAdminGroup.getDescription()));

    if (updAdminGroup.getDescription() == null) {
      form.getErr().emit(ValidationError.missingDescription);
      ok = false;
    }

    final String adminGroupGroupOwner = checkNull(form.getAdminGroupGroupOwner());
    final BwPrincipal updAgowner = cl.getPrincipal(updAdminGroup.getGroupOwnerHref());

    if ((adminGroupGroupOwner != null) &&
        (!adminGroupGroupOwner.equals(updAgowner.getAccount()))) {
      final BwPrincipal aggo = cl.getUser(adminGroupGroupOwner);

      if (aggo == null) {
        form.getErr().emit(ClientError.unknownUser, adminGroupGroupOwner);
        return false;
      }

      updAdminGroup.setGroupOwnerHref(aggo.getPrincipalRef());
    }

    String adminGroupEventOwner = checkNull(form.getAdminGroupEventOwner());
    if (adminGroupEventOwner == null) {
      // no change
      return ok;
    }

    final BwPrincipal ageo = cl.getUser(adminGroupEventOwner);

    final String prefix = cl.getAdminGroupsIdPrefix();

    if (!adminGroupEventOwner.startsWith(prefix)) {
      adminGroupEventOwner = prefix + adminGroupEventOwner;
    }

    if (ageo == null) {
      form.getErr().emit(ClientError.unknownUser, adminGroupEventOwner);
      return false;
    }

    if (ageo.getPrincipalRef().equals(updAdminGroup.getOwnerHref())) {
      // no change
      return ok;
    }

    updAdminGroup.setOwnerHref(ageo.getPrincipalRef());

    return ok;
  }

  private boolean validateKind(final String kind, final BwActionFormBase form) {
    if (kind == null) {
      form.getErr().emit(ClientError.missingRequestPar, "kind");
      return false;
    }

    if ("group".equals(kind) || "user".equals(kind)) {
      return true;
    }

    form.getErr().emit(ClientError.badRequest, kind);
    return false;
  }
}

