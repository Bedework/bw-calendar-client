/* ********************************************************************
    Appropriate copyright notice
*/
package org.bedework.client.web.admin;

import org.bedework.calfacade.svc.BwAdminGroup;
import org.bedework.calfacade.svc.BwAuthUser;
import org.bedework.calfacade.svc.UserAuth;
import org.bedework.webcommon.BwActionFormBase;
import org.bedework.webcommon.BwModule;

import org.apache.struts.action.ActionMapping;

import javax.servlet.http.HttpServletRequest;

/**
 * User: mike Date: 3/9/21 Time: 22:25
 */
public class BwAdminActionForm extends BwActionFormBase {
  /* ..............................................................
   *                   Admin group fields
   * .............................................................. */

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

  /** Value built out of checked boxes.
   */
  private int editAuthUserType;

  /** User we want to fetch or modify
   */
  private String editAuthUserId;

  /** User object we are creating or modifying
   */
  private BwAuthUser editAuthUser;

  /* ====================================================================
   *                   Admin groups
   * ==================================================================== */

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

  /* ....................................................................
   *                       Modules
   * .................................................................... */

  public BwModule newModule(final String name) {
    return new AdminBwModule(name);
  }
}
