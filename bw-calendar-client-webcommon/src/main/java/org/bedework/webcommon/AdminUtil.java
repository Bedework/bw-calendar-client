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
package org.bedework.webcommon;

import org.bedework.appcommon.ClientError;
import org.bedework.calfacade.BwAuthUser;
import org.bedework.calfacade.BwGroup;
import org.bedework.calfacade.BwPrincipal;
import org.bedework.calfacade.configs.AdminConfig;
import org.bedework.calfacade.exc.CalFacadeAccessException;
import org.bedework.calfacade.ifs.Directories;
import org.bedework.calfacade.svc.AdminGroups;
import org.bedework.calfacade.svc.BwAdminGroup;
import org.bedework.calfacade.svc.UserAuth;
import org.bedework.calfacade.svc.prefs.BwAuthUserPrefs;
import org.bedework.calfacade.svc.wrappers.BwCalSuiteWrapper;
import org.bedework.calsvci.CalSvcI;

import edu.rpi.sss.util.jsp.Request;

import org.apache.log4j.Logger;

import java.util.Collection;

/**
 * @author Mike Douglass
 *
 */
public class AdminUtil implements ForwardDefs {
  /** Called just before action.
   *
   * @param request
   * @return int foward index
   * @throws Throwable
   */
  public static int actionSetup(final Request request) throws Throwable {
    boolean debug = getLogger().isDebugEnabled();

    BwActionFormBase form = (BwActionFormBase)request.getForm();
    CalSvcI svc = form.fetchSvci();

    UserAuth ua = svc.getUserAuth();
    BwAuthUser au = ua.getUser(form.getCurrentUser());

    if (au == null) {
      // No authuser entry for this user.
      if (!form.getCurUserSuperUser()) {
        return forwardNoAccess;
      }
      au = BwAuthUser.makeAuthUser(svc.getPrincipal().getPrincipalRef(),
                                   UserAuth.publicEventUser);
      ua.updateUser(au);
    }

    // Refresh current auth user prefs.
    BwAuthUserPrefs prefs = au.getPrefs();

    form.setCurAuthUserPrefs(prefs);
    if (form.getNewSession()) {
      // First time through here for this session. svci is still set up for the
      // authenticated user. Set access rights.

      int rights = au.getUsertype();

      form.assignCurUserPublicEvents((rights & UserAuth.publicEventUser) != 0);
      form.assignCurUserContentAdminUser((rights & UserAuth.contentAdminUser) != 0);

      form.assignAuthorisedUser(rights != UserAuth.noPrivileges);
    }

    if (debug) {
      logIt("form.getGroupSet()=" + form.getGroupSet());
    }

    /** Show the owner we are administering */
    form.setAdminUserId(form.fetchSvci().getPrincipal().getAccount());

    if (debug) {
      logIt("-------- isSuperUser: " + form.getCurUserSuperUser());
    }

    int temp = checkGroup(request, true);

    if (temp != forwardNoAction) {
      if (debug) {
        logIt("form.getGroupSet()=" + form.getGroupSet());
      }
      return temp;
    }

    if (!form.getAuthorisedUser()) {
      return forwardNoAccess;
    }

    return forwardNoAction;
  }

  /** Return no action if group is chosen else return a forward index.
   *
   * @param request   for pars
   * @param initCheck true if this is a check to see if we're initialised,
   *                  otherwise this is an explicit request to change group.
   * @return int   forward index
   * @throws Throwable
   */
  public static int checkGroup(final Request request,
                               final boolean initCheck) throws Throwable {
    BwActionFormBase form = (BwActionFormBase)request.getForm();
    if (form.getGroupSet()) {
      return forwardNoAction;
    }

    CalSvcI svci = form.fetchSvci();

    try {
      Directories adgrps = svci.getDirectories();

      if (form.retrieveChoosingGroup()) {
        /** This should be the response to presenting a list of groups.
            We handle it here rather than in a separate action to ensure our
            client is not trying to bypass the group setting.
         */

        String reqpar = request.getReqPar("adminGroupName");
        if (reqpar == null) {
          // Make them do it again.

          return forwardChooseGroup;
        }

        BwAdminGroup adg = (BwAdminGroup)adgrps.findGroup(reqpar);
        if (adg == null) {
          if (getLogger().isDebugEnabled()) {
            logIt("No user admin group with name " + reqpar);
          }
          // We require a group
          return forwardChooseGroup;
        }

        return setGroup(request, adg);
      }

      /** If the user is in no group or in one group we just go with that,
          otherwise we ask them to select the group
       */

      Collection<BwGroup> adgs;

      BwPrincipal p = svci.getUsersHandler().getUser(form.getCurrentUser());
      if (p == null) {
        return forwardNoAccess;
      }

      if (initCheck || !form.getCurUserSuperUser()) {
        // Always restrict to groups of which we are a member
        adgs = adgrps.getGroups(p);
      } else {
        adgs = adgrps.getAll(false);
      }

      if (adgs.isEmpty()) {
        /** If we require that all users be in a group we return to an error
            page. The only exception will be superUser.
         */

        boolean noGroupAllowed =
          ((AdminConfig)form.getConfig()).getNoGroupAllowed();
        if (svci.getSuperUser() || noGroupAllowed) {
          form.assignAdminGroupName(null);
          return forwardNoAction;
        }

        return forwardNoGroupAssigned;
      }

      form.setOneGroup(false);
      if (adgs.size() == 1) {
        form.setOneGroup(true);
        return setGroup(request,
                        (BwAdminGroup)adgs.iterator().next());
      }

      /** Go ahead and present the possible groups
       */
      form.setUserAdminGroups(adgs);
      form.assignChoosingGroup(true); // reset

      return forwardChooseGroup;
    } catch (Throwable t) {
      form.getErr().emit(t);
      return forwardError;
    }
  }

  private static int setGroup(final Request request,
                              final BwAdminGroup adg) throws Throwable {
    BwActionFormBase form = (BwActionFormBase)request.getForm();
    CalSvcI svci = form.fetchSvci();
    Directories adgrps = svci.getDirectories();
    boolean debug = getLogger().isDebugEnabled();

    adgrps.getMembers(adg);

    if (debug) {
      logIt("Set admin group to " + adg);
    }

    form.assignAdminGroupName(adg.getAccount());

    //int access = getAccess(request, getMessages());

    BwPrincipal p = svci.getUsersHandler().getPrincipal(adg.getOwnerHref());

    if ((p == null) ||
        !((BwAbstractAction)request.getAction()).checkSvci(request,
                                                           p.getAccount(),
                                                           isMember(adg, form))) {
      return forwardNoAccess;
    }

    form.setAdminUserId(form.fetchSvci().getPrincipal().getAccount());

    return forwardNoAction;
  }

  private static boolean isMember(final BwAdminGroup ag,
                                  final BwActionFormBase form) throws Throwable {
    return ag.isMember(String.valueOf(form.getCurrentUser()), false);
  }

  /** For an administrative user this is how we determine the calendar suite
   * they are administering. We see if there is a suite associated with the
   * administrative group. If so, return.
   *
   * <p>If not we call ourself for each parent group.
   *
   * <p>If none is found we return null.
   *
   * @param form
   * @param groupName
   * @return calendar suite wrapper
   * @throws Throwable
   */
  public static BwCalSuiteWrapper findCalSuite(final BwActionFormBase form,
                                               final String groupName) throws Throwable {
    if (groupName == null) {
      return null;
    }

    CalSvcI svci = form.fetchSvci();
    Directories adgrps = svci.getDirectories();
    BwAdminGroup adg = (BwAdminGroup)adgrps.findGroup(groupName);

    return findCalSuite(form, adg);
  }

  /** For an administrative user this is how we determine the calendar suite
   * they are administering. We see if there is a suite associated with the
   * administrative group. If so, return.
   *
   * <p>If not we call ourself for each parent group.
   *
   * <p>If none is found we return null.
   *
   * @param form
   * @param adg
   * @return calendar suite wrapper
   * @throws Throwable
   */
  private static BwCalSuiteWrapper findCalSuite(final BwActionFormBase form,
                                                final BwAdminGroup adg) throws Throwable {
    if (adg == null) {
      return null;
    }

    CalSvcI svci = form.fetchSvci();
    Directories adgrps = svci.getDirectories();
    /* At this point we still require at least authenticated read access to
     * the target calendar suite
     */

    try {
      BwCalSuiteWrapper cs = svci.getCalSuitesHandler().get(adg);
      if (cs != null) {
        return cs;
      }

      for (BwGroup parent: ((AdminGroups)adgrps).findGroupParents(adg)) {
        cs = findCalSuite(form, (BwAdminGroup)parent);
        if (cs != null) {
          return cs;
        }
      }
    } catch (CalFacadeAccessException cfe) {
      // Access is set incorrectly
      form.getErr().emit(ClientError.noCalsuiteAccess, adg.getPrincipalRef());
    }

    return null;
  }

  /**
   * @param msg
   */
  public static void debugOut(final String msg) {
    getLogger().debug(msg);
  }

  /**
   * @param msg
   */
  public static void logIt(final String msg) {
    getLogger().info(msg);
  }

  /**
   * @return Logger
   */
  public static Logger getLogger() {
    return Logger.getLogger(AdminUtil.class);
  }

  /** Info message
   *
   * @param msg
   */
  public static void info(final String msg) {
    getLogger().info(msg);
  }

  /** Warning message
   *
   * @param msg
   */
  public void warn(final String msg) {
    getLogger().warn(msg);
  }

  /**
   * @param msg
   */
  public void debugMsg(final String msg) {
    getLogger().debug(msg);
  }

  /**
   * @param t
   */
  public void error(final Throwable t) {
    getLogger().error(this, t);
  }

}
