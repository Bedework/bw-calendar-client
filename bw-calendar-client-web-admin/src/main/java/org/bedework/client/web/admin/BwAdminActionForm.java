/* ********************************************************************
    Appropriate copyright notice
*/
package org.bedework.client.web.admin;

import org.bedework.appcommon.CalSuiteResource;
import org.bedework.calfacade.BwCalendar;
import org.bedework.calfacade.svc.BwAdminGroup;
import org.bedework.calfacade.svc.BwAuthUser;
import org.bedework.calfacade.svc.BwCalSuite;
import org.bedework.calfacade.svc.UserAuth;
import org.bedework.calfacade.svc.prefs.BwAuthUserPrefs;
import org.bedework.calfacade.svc.wrappers.BwCalSuiteWrapper;
import org.bedework.client.web.rw.BwRWActionForm;
import org.bedework.webcommon.BwModule;

import org.apache.struts.action.ActionMapping;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

/**
 * User: mike Date: 3/9/21 Time: 22:25
 */
public class BwAdminActionForm extends BwRWActionForm {
  /* ..............................................................
   *                   Admin group fields
   * .............................................................. */

  private boolean oneGroup;
  private String adminGroupName;

  /** True if we are adding a new administrative group
   */
  private boolean addingAdmingroup;

  /** True to show members in list
   */
  private boolean showAgMembers;

  private boolean adminGroupMaintOK;

  private BwAdminGroup updAdminGroup;

  /** Group owner and group event owner */
  private String adminGroupGroupOwner;
  private String adminGroupEventOwner;

  /** Group member to add/delete
   */
  private String updGroupMember;

  /* ..............................................................
   *                   Authorised user fields
   * .............................................................. */

  private boolean userMaintOK;

  /** Value built out of checked boxes.
   */
  private int editAuthUserType;

  /** User we want to fetch or modify
   */
  private String editAuthUserId;

  /** User object we are creating or modifying
   */
  private BwAuthUser editAuthUser;

  /* Settings for current authenticated user */
  private boolean curUserContentAdminUser;
  private boolean curUserApproverUser;

  /** Auth prefs for the currently logged in user
   */
  private BwAuthUserPrefs curAuthUserPrefs;

  /* ..............................................................
   *                       Calendar suites
   * .............................................................. */

  private String editCalSuiteName;

  private BwCalSuiteWrapper calSuite;

  private Collection<BwCalSuite> calSuites;

  /* ..............................................................
   *                     Resources
   * .............................................................. */

  private String resourceName;

  private String resourceClass;

  private boolean addingResource;

  private CalSuiteResource calSuiteResource;

  private List<CalSuiteResource> calSuiteResources;

  /* ..............................................................
   *                       Misc
   * .............................................................. */

  private boolean suggestionEnabled;
  private boolean workflowEnabled;
  private String workflowRoot;

  /* ==============================================================
   *                   Admin groups
   * ============================================================== */

  /**
   * @param val true if there is only one group
   */
  public void assignOneGroup(final boolean val) {
    oneGroup = val;
  }

  /**
   * @return true if there is only one group
   */
  public boolean getOneGroup() {
    return oneGroup;
  }

  /**
   * @param val admin group name
   */
  public void assignAdminGroupName(final String val) {
    adminGroupName = val;
  }

  /**
   * @return String admin group name
   */
  public String getAdminGroupName() {
    return adminGroupName;
  }

  /** Not set - invisible to jsp
   *
   * @param val true if adding admin group
   */
  public void assignAddingAdmingroup(final boolean val) {
    addingAdmingroup = val;
  }

  /**
   * @return adding group
   */
  public boolean getAddingAdmingroup() {
    return addingAdmingroup;
  }

  /**
   * @param val true for show group members
   */
  public void setShowAgMembers(final boolean val) {
    showAgMembers = val;
  }

  /**
   * @return true for show group members
   */
  public boolean getShowAgMembers() {
    return showAgMembers;
  }

  /**
   * @param val true for admin group maint allowed
   */
  public void assignAdminGroupMaintOK(final boolean val) {
    adminGroupMaintOK = val;
  }

  /** Show whether admin group maintenance is available.
   * Some sites may use other mechanisms.
   *
   * @return boolean    true if admin group maintenance is implemented.
   */
  public boolean getAdminGroupMaintOK() {
    return adminGroupMaintOK;
  }

  /**
   * @param val the group or null for a new one
   */
  public void setUpdAdminGroup(final BwAdminGroup val) {
    if (val == null) {
      updAdminGroup = new BwAdminGroup();
    } else {
      updAdminGroup = val;
    }

    try {
      String href = updAdminGroup.getGroupOwnerHref();

      if (href != null) {
        setAdminGroupGroupOwner(href);
      }

      href = updAdminGroup.getOwnerHref();

      if (href != null) {
        setAdminGroupEventOwner(href);
      }
    } catch (final Throwable t) {
      err.emit(t);
    }
  }

  /**
   * @return group
   */
  public BwAdminGroup getUpdAdminGroup() {
    if (updAdminGroup == null) {
      updAdminGroup = new BwAdminGroup();
    }

    return updAdminGroup;
  }

  /**
   * @param val Admin group group owner
   */
  public void setAdminGroupGroupOwner(final String val) {
    adminGroupGroupOwner = val;
  }

  /**
   * @return group owner
   */
  public String getAdminGroupGroupOwner() {
    return adminGroupGroupOwner;
  }

  /**
   * @param val event owner
   */
  public void setAdminGroupEventOwner(final String val) {
    adminGroupEventOwner = val;
  }

  /**
   * @return owner
   */
  public String getAdminGroupEventOwner() {
    return  adminGroupEventOwner;
  }

  /**
   * @param val member
   */
  public void setUpdGroupMember(final String val) {
    updGroupMember = val;
  }

  /**
   * @return group member
   */
  public String getUpdGroupMember() {
    return updGroupMember;
  }

  /* ==============================================================
   *                   Authorised user maintenance
   * ============================================================== */

  /**
   * @param val
   */
  public void assignUserMaintOK(final boolean val) {
    userMaintOK = val;
  }

  /** Show whether user entries can be displayed or modified with this
   * class. Some sites may use other mechanisms.
   *
   * @return boolean    true if user maintenance is implemented.
   */
  public boolean getUserMaintOK() {
    return userMaintOK;
  }

  /** Only called if the flag is set - it's a checkbox.
   *
   * @param val always true
   */
  public void setEditAuthUserPublicEvents(final boolean val) {
    editAuthUserType |= UserAuth.publicEventUser;
  }

  /** Only called if the flag is set - it's a checkbox.
   *
   * @param val always true
   */
  public void setEditAuthUserContentAdmin(final boolean val) {
    editAuthUserType |= UserAuth.contentAdminUser;
  }

  /** Only called if the flag is set - it's a checkbox.
   *
   * @param val always true
   */
  public void setEditAuthUserApprover(final boolean val) {
    editAuthUserType |= UserAuth.approverUser;
  }

  /**
   *
   * @return boolean
   */
  public boolean getEditAuthUserPublicEvents() {
    return editAuthUser.isPublicEventUser();
  }

  /**
   *
   * @return boolean
   */
  public boolean getEditAuthUserContentAdmin() {
    return editAuthUser.isContentAdminUser();
  }

  /**
   *
   * @return boolean
   */
  public boolean getEditAuthUserApprover() {
    return editAuthUser.isApproverUser();
  }

  /** New auth user rights
   *
   * @return int rights
   */
  public int getEditAuthUserType() {
    return editAuthUserType;
  }

  /**
   * @param val id
   */
  public void setEditAuthUserId(final String val) {
    editAuthUserId = val;
  }

  /**
   * @return id
   */
  public String getEditAuthUserId() {
    return editAuthUserId;
  }

  /**
   * @param val auth user object
   */
  public void setEditAuthUser(final BwAuthUser val) {
    editAuthUser = val;
    editAuthUserType = 0;
  }

  /**
   * @return auth user object
   */
  public BwAuthUser getEditAuthUser() {
    return editAuthUser;
  }

  /** True for contentAdminUser
   *
   * @param val boolean
   */
  public void assignCurUserContentAdminUser(final boolean val) {
    curUserContentAdminUser = val;
  }

  /** True for contentAdminUser
   *
   * @return boolean
   */
  public boolean getCurUserContentAdminUser() {
    return curUserContentAdminUser;
  }

  /** True for approver
   *
   * @param val boolean
   */
  public void assignCurUserApproverUser(final boolean val) {
    curUserApproverUser = val;
  }

  /** True for approver
   *
   * @return boolean
   */
  public boolean getCurUserApproverUser() {
    return curUserApproverUser;
  }

  /**
   * @param val
   */
  public void setCurAuthUserPrefs(final BwAuthUserPrefs val) {
    curAuthUserPrefs = val;
  }

  /**
   * @return auth user prefs
   */
  public BwAuthUserPrefs getCurAuthUserPrefs() {
    return curAuthUserPrefs;
  }

  /* ==============================================================
   *                   Calendar suites
   * ============================================================== */

  /** Name of CalSuite to edit/add/delete.
   *
   * @param val
   */
  public void setEditCalSuiteName(final String val) {
    editCalSuiteName = val;
  }

  /** name of CalSuite to edit/add/delete.
   *
   * @return String
   */
  public String getEditCalSuiteName() {
    return editCalSuiteName;
  }

  /** CalSuite we are editing or creating.
   *
   * @param val
   */
  public void setCalSuite(final BwCalSuiteWrapper val) {
    calSuite = val;
  }

  /** CalSuite we are editing or creating.
   *
   * @return BwCalSuiteWrapper
   */
  public BwCalSuiteWrapper getCalSuite() {
    return calSuite;
  }

  /**
   *
   * @param val the collection of cal suites
   */
  public void assignCalSuites(final Collection<BwCalSuite> val) {
    calSuites = val;
  }

  /** Return the collection of cal suites
   *
   * @return Calendar suites
   */
  public Collection<BwCalSuite> getCalSuites() {
    return calSuites;
  }

  /* ==============================================================
   *                   Resources
   * ============================================================== */

  /** Not set - invisible to jsp
   *
   * @param val
   */
  public void assignAddingResource(final boolean val) {
    addingResource = val;
  }

  /**
   * @return bool
   */
  public boolean getAddingResource() {
    return addingResource;
  }

  /**
   * @return the resource name
   */
  public String getResourceName() {
    return resourceName;
  }

  /**
   * Sets the resource name.
   * @param name
   */
  public void setResourceName(final String name) {
    this.resourceName = name;
  }

  /**
   * @return the resource class
   */
  public String getResourceClass() {
    return resourceClass;
  }

  /**
   * Sets the resource class.
   * @param resourceClass
   */
  public void setResourceClass(final String resourceClass) {
    this.resourceClass = resourceClass;
  }

  /** Current resource fetched
   *
   * @param val
   */
  public void setCalSuiteResource(final CalSuiteResource val) {
    calSuiteResource = val;
  }

  /**
   * @return resource or null
   */
  public CalSuiteResource getCalSuiteResource() {
    return calSuiteResource;
  }

  /** Current resources fetched
   *
   * @param val
   */
  public void setCalSuiteResources(final List<CalSuiteResource> val) {
    calSuiteResources = val;
  }

  /**
   * @return list or null
   */
  public List<CalSuiteResource> getCalSuiteResources() {
    return calSuiteResources;
  }

  /* ==============================================================
   * ============================================================== */

  /**
   * Reset properties to their default values.
   *
   * @param mapping The mapping used to select this instance
   * @param request The servlet request we are processing
   */
  @Override
  public void reset(final ActionMapping mapping,
                    final HttpServletRequest request) {
    super.reset(mapping, request);

    editAuthUserType = 0;
  }

  /* ====================================================================
   *                       Modules
   * ==================================================================== */

  public BwModule newModule(final String name) {
    return new AdminBwModule(name);
  }

  /* ====================================================================
   *                   Calendars
   * ==================================================================== */

  /** Get the preferred calendars for the current user
   *
   * @return Collection  preferred calendars
   */
  public Collection<BwCalendar> getPreferredCalendars() {
    return getCurAuthUserPrefs().getCalendarPrefs().getPreferred();
  }

  /* ====================================================================
   *                   Misc
   * ==================================================================== */

  /** Return the encoded root of the workflow collections
   *
   * @return String path.
   */
  public String getEncodedWorkflowRoot() {
    final String appType = getAppType();

    if (appTypeWebadmin.equals(appType)) {
      if (getWorkflowRoot() == null) {
        return "";
      }

      try {
        return URLEncoder.encode(getWorkflowRoot(),
                                 StandardCharsets.UTF_8);
      } catch (final Throwable t) {
        getErr().emit(t);
      }
    }

    return "";
  }

  /**
   *
   * @param val root of the workflow collections
   */
  public void assignWorkflowRoot(final String val) {
    workflowRoot = val;
  }

  /** Return the unencoded root of the workflow collections
   *
   * @return String path.
   */
  public String getWorkflowRoot() {
    return workflowRoot;
  }

  public void assignSuggestionEnabled(final boolean val) {
    suggestionEnabled = val;
  }

  public boolean getSuggestionEnabled() {
    return suggestionEnabled;
  }

  public void assignWorkflowEnabled(final boolean val) {
    workflowEnabled = val;
  }

  public boolean getWorkflowEnabled() {
    return workflowEnabled;
  }
}
