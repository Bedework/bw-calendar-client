/* ********************************************************************
    Appropriate copyright notice
*/
package org.bedework.client.web.admin;

import org.bedework.appcommon.ConfigCommon;
import org.bedework.calfacade.BwGroup;
import org.bedework.calfacade.BwPrincipal;
import org.bedework.calfacade.exc.CalFacadeException;
import org.bedework.calfacade.svc.BwAdminGroup;
import org.bedework.calfacade.svc.BwAuthUser;
import org.bedework.calfacade.svc.prefs.BwAuthUserPrefs;
import org.bedework.calfacade.svc.wrappers.BwCalSuiteWrapper;
import org.bedework.client.admin.AdminClient;
import org.bedework.client.admin.AdminClientImpl;
import org.bedework.client.admin.AdminConfig;
import org.bedework.client.web.rw.RwBwModule;
import org.bedework.util.struts.Request;
import org.bedework.webcommon.BwActionFormBase;
import org.bedework.webcommon.BwCallback;
import org.bedework.webcommon.BwModuleState;
import org.bedework.webcommon.BwRequest;
import org.bedework.webcommon.BwSession;
import org.bedework.webcommon.BwSessionImpl;

import java.util.Collection;
import java.util.List;

import static org.bedework.webcommon.ForwardDefs.forwardChooseGroup;
import static org.bedework.webcommon.ForwardDefs.forwardError;
import static org.bedework.webcommon.ForwardDefs.forwardNoAccess;
import static org.bedework.webcommon.ForwardDefs.forwardNoAction;
import static org.bedework.webcommon.ForwardDefs.forwardNoGroupAssigned;

/**
 * User: mike Date: 3/10/21 Time: 23:01
 */
public class AdminBwModule extends RwBwModule {
  public AdminBwModule(final String moduleName) {
    super(moduleName);
  }

  /** Overridden for the admin client.
   *
   * <p>For an admin client with a super user we may switch to a different
   * user to administer their events.
   *
   * @param request       for pars
   * @param user          String user we want to be
   * @param canSwitch     true if we should definitely allow user to switch
   *                      this allows a user to switch between and into
   *                      groups of which they are a member
   * @return boolean      false for problems.
   * @throws Throwable on fatal error
   */
  @Override
  public boolean checkClient(final Request request,
                             final BwSession sess,
                             final String user,
                             boolean canSwitch,
                             final ConfigCommon conf) throws Throwable {
    if (!conf.getPublicAdmin()) {
      throw new RuntimeException("Admin client called for non admin app");
    }

    final BwAdminActionForm form = (BwAdminActionForm)request.getForm();
    final BwModuleState mstate = getState();
    AdminClient client = (AdminClient)getClient();
    final BwCallback cb = BwCallback.getCb(request, form);
    String calSuiteName = null;

    if (client != null) {
      /* Calendar suite we are administering is the one we find attached to a
       * group as we proceed up the tree
       */

      /* Note that we redo this once we have a group set. The first call
         (before we have any client) has no group name set in the form
       */
      final BwCalSuiteWrapper cs =
              AdminUtil.findCalSuite(request,
                                     client);
      form.setCurrentCalSuite(cs);

      if (cs != null) {
        calSuiteName = cs.getName();
        client.setCalSuite(cs);

        // Use preferences to set approver
        final List<String> approvers =
                client.getCalsuitePreferences().getCalsuiteApproversList();

        if (approvers.contains(form.getCurrentUser())) {
          form.assignCurUserApproverUser(true);
        }

        // If membership of an admin group implies approver - use that
        final boolean adminGroupImpliesApprover =
                ((AdminConfig)form.getConfig()).getAdminGroupApprovers();

        if (adminGroupImpliesApprover &&
                (cs.getGroup() != null) &&
                cs.getGroup().getAccount().equals(form.getAdminGroupName())) {
          form.assignCurUserApproverUser(true);
        }
      }

      form.setCalSuiteName(calSuiteName);

      if (debug()) {
        if (cs != null) {
          debug("Found calSuite " + cs);
        } else {
          debug("No calsuite found");
        }
      }
    }

    boolean reinitClient = false;

    try {
      /* Make some checks to see if this is an old - restarted session.
        If so discard the svc interface
       */
      if (client != null) {
        /* Not the first time through here so for a public admin client we
         * already have the authorised user's rights set in the form.
         */

        final BwPrincipal pr = client.getCurrentPrincipal();
        if (pr == null) {
          throw new CalFacadeException("Null user for public admin.");
        }

        canSwitch = canSwitch || form.getCurUserContentAdminUser() ||
                form.getCurUserSuperUser();

        final String curUser = pr.getAccount();

        if (!user.equals(curUser)) {
          if (!canSwitch) {
            /* Trying to switch but not allowed */
            return false;
          }

          /* Switching user */
          client.endTransaction();
          client.close();
          reinitClient = true;
          sess.reset(request);
          cb.close(request.getRequest(), true); // So we're not waiting for ourself
        }

        /* Already there and already opened */
        if (debug()) {
          debug("Client interface -- Obtained from session for user " +
                        client.getCurrentPrincipalHref());
        }

        if (reinitClient) {
          // We did a module close will need to reclaim - always public admin
          if (debug()) {
            debug("Client-- reinit for user " + user);
          }

          form.flushModules(request.getModuleName());

          ((AdminClientImpl)client).reinit(form.getCurrentUser(),
                                           user,
                                           calSuiteName);

          cb.in(request);
          //client.requestIn(request.getConversationType());
          mstate.setRefresh(true);
        }
      } else {
        if (debug()) {
          debug("Client-- getResource new object for user " + user);
        }

        client = new AdminClientImpl(conf,
                                     getModuleName(),
                                     form.getCurrentUser(),
                                     user,
                                     calSuiteName);

        setClient(client);
        setRequest(request);

        // Didn't release module - just reflag entry
        requestIn();
        mstate.setRefresh(true);
        sess.reset(request);
      }

      final BwPrincipal pr = client.getCurrentPrincipal();
      form.assignCurrentAdminUser(pr.getAccount());
    } catch (final CalFacadeException cfe) {
      throw cfe;
    } catch (final Throwable t) {
      throw new CalFacadeException(t);
    }

    return true;
  }

  /** Called just before action.
   *
   * @param request wrapper
   * @return int foward index
   * @throws Throwable on fatal error
   */
  protected int actionSetup(final BwRequest request) throws Throwable {
    final BwAdminActionForm form = (BwAdminActionForm)request.getBwForm();
    final AdminClient cl = (AdminClient)request.getClient();

    final BwAuthUser au = cl.getAuthUser(cl.getAuthPrincipal());

    if ((au == null) || au.isUnauthorized()) {
      return forwardNoAccess;
    }

    form.assignOneGroup(cl.getOneGroup());
    form.assignAdminGroupMaintOK(cl.getAdminGroupMaintOK());

    form.assignSuggestionEnabled(cl.getSystemProperties().getSuggestionEnabled());
    form.assignWorkflowEnabled(cl.getSystemProperties().getWorkflowEnabled());
    form.assignWorkflowRoot(cl.getSystemProperties().getWorkflowRoot());
    form.assignUserMaintOK(cl.getUserMaintOK());

    // Refresh current auth user prefs.
    final BwAuthUserPrefs prefs = au.getPrefs();

    ((BwSessionImpl)request.getSess()).setCurAuthUserPrefs(prefs);
    form.setCurAuthUserPrefs(prefs);

    if (!cl.getGroupSet()) {
      // Set default access rights.

      form.assignCurUserContentAdminUser(au.isContentAdminUser());
      form.assignCurUserApproverUser(au.isApproverUser());
    }

    if (debug()) {
      info("form.getGroupSet()=" + cl.getGroupSet());
      info("-------- isSuperUser: " + form.getCurUserSuperUser());
    }

    final int temp = checkGroup(request, true);

    if (temp != forwardNoAction) {
      if (debug()) {
        info("form.getGroupSet()=" + cl.getGroupSet());
      }
      return temp;
    }

    return forwardNoAction;
  }

  @Override
  protected boolean shouldCheckNotifications(final BwRequest req) {
    final AdminClient cl = (AdminClient)req.getClient();

    return cl.getGroupSet();
  }

  /** Return no action if group is chosen else return a forward index.
   *
   * @param request   for pars
   * @param initCheck true if this is a check to see if we're initialised,
   *                  otherwise this is an explicit request to change group.
   * @return int   forward index
   */
  public int checkGroup(final BwRequest request,
                        final boolean initCheck) {
    final BwAdminActionForm form = (BwAdminActionForm)request.getForm();

    final AdminClient cl = (AdminClient)request.getClient();

    if (cl.getGroupSet()) {
      return forwardNoAction;
    }

    try {
      if (cl.getChoosingGroup()) {
        /* This should be the response to presenting a list of groups.
            We handle it here rather than in a separate action to ensure our
            client is not trying to bypass the group setting.
         */

        final String reqpar = request.getReqPar("adminGroupName");
        if (reqpar == null) {
          // Make them do it again.

          form.assignCalSuites(cl.getContextCalSuites());
//          request.setSessionAttr(BwRequest.bwAdminGroupsInfoName,
//                                 request.getClient().getAdminGroups());

          return forwardChooseGroup;
        }

        final BwAdminGroup adg = (BwAdminGroup)cl.findGroup(reqpar);
        if (adg == null) {
          if (debug()) {
            info("No user admin group with name " + reqpar);
          }

          form.assignCalSuites(cl.getContextCalSuites());
//          request.setSessionAttr(BwRequest.bwAdminGroupsInfoName,
//                                 request.getClient().getAdminGroups());
          // We require a group
          return forwardChooseGroup;
        }

        return setGroup(request, adg);
      }

      /* If the user is in no group or in one group we just go with that,
          otherwise we ask them to select the group
       */

      final Collection<BwGroup> adgs;

      final BwPrincipal p = cl.getAuthPrincipal();
      if (p == null) {
        return forwardNoAccess;
      }

      if (initCheck || !form.getCurUserSuperUser()) {
        // Always restrict to groups of which we are a member
        adgs = cl.getGroups(p);
      } else {
        adgs = cl.getAllGroups(false);
      }

      if (adgs.isEmpty()) {
        /* If we require that all users be in a group we return to an error
            page. The only exception will be superUser.
         */

        final boolean noGroupAllowed =
                ((AdminConfig)form.getConfig()).getNoGroupAllowed();
        if (cl.isSuperUser() || noGroupAllowed) {
          cl.setAdminGroupName(null);
          cl.setGroupSet(true);
          return forwardNoAction;
        }

        return forwardNoGroupAssigned;
      }

      cl.setOneGroup(false);
      if (adgs.size() == 1) {
        cl.setOneGroup(true);
        return setGroup(request,
                        (BwAdminGroup)adgs.iterator().next());
      }

      /* Go ahead and present the possible groups
       */
      request.setSessionAttr(BwRequest.bwUserAdminGroupsInfoName,
                             adgs);

      form.assignCalSuites(cl.getContextCalSuites());
      cl.setChoosingGroup(true); // reset

      return forwardChooseGroup;
    } catch (final Throwable t) {
      form.getErr().emit(t);
      return forwardError;
    }
  }

  private int setGroup(final BwRequest request,
                       final BwAdminGroup adg) throws Throwable {
    final BwAdminActionForm form = (BwAdminActionForm)request.getBwForm();
    final AdminClient cl = (AdminClient)request.getClient();

    cl.getMembers(adg);

    if (debug()) {
      info("Set admin group to " + adg);
    }

    cl.setAdminGroupName(adg.getAccount());
    form.assignAdminGroupName(adg.getAccount());
    cl.setGroupSet(true);

    //int access = getAccess(request, getMessages());

    final BwPrincipal p = cl.getPrincipal(adg.getOwnerHref());

    if ((p == null) ||
            !form.fetchModule(request.getModuleName())
                 .checkClient(request,
                              request.getSess(),
                              p.getAccount(),
                              isMember(adg, form),
                              cl.getConf())) {
      return forwardNoAccess;
    }

    return forwardNoAction;
  }

  private boolean isMember(final BwAdminGroup ag,
                           final BwActionFormBase form) {
    return ag.isMember(String.valueOf(form.getCurrentUser()), false);
  }
}
