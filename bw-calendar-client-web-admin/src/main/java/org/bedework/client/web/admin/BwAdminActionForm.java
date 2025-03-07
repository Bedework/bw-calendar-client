/* ********************************************************************
    Appropriate copyright notice
*/
package org.bedework.client.web.admin;

import org.bedework.appcommon.CalSuiteResource;
import org.bedework.calfacade.svc.BwAdminGroup;
import org.bedework.calfacade.svc.BwAuthUser;
import org.bedework.calfacade.svc.BwCalSuite;
import org.bedework.calfacade.svc.UserAuth;
import org.bedework.calfacade.svc.wrappers.BwCalSuiteWrapper;
import org.bedework.client.web.rw.BwRWActionForm;

import jakarta.servlet.http.HttpServletRequest;
import org.apache.struts2.interceptor.parameter.StrutsParameter;

import java.util.Collection;
import java.util.List;

/**
 * User: mike Date: 3/9/21 Time: 22:25
 */
public class BwAdminActionForm extends BwRWActionForm {
  /* ..............................................................
   *                   Admin group fields
   * .............................................................. */

  /** True if we are adding a new administrative group
   */
  private boolean addingAdmingroup;

  /** True to show members in list
   */
  private boolean showAgMembers;

  private BwAdminGroup updAdminGroup;

  /** Group owner and group event owner */
  private String adminGroupGroupOwner;
  private String adminGroupEventOwner;

  /* ..............................................................
   *                   Authorised user fields
   * .............................................................. */

  /** Value built out of checked boxes.
   */
  private int editAuthUserType;

  /** User object we are creating or modifying
   */
  private BwAuthUser editAuthUser;

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

  /* ==============================================================
   *                   Admin groups
   * ============================================================== */

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
   * @param val the group or null for a new one
   */
  public void setUpdAdminGroup(final BwAdminGroup val) {
    if (val == null) {
      updAdminGroup = new BwAdminGroup();
    } else {
      updAdminGroup = val;
    }

    String href = updAdminGroup.getGroupOwnerHref();

    if (href != null) {
      setAdminGroupGroupOwner(href);
    }

    href = updAdminGroup.getOwnerHref();

    if (href != null) {
      setAdminGroupEventOwner(href);
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
  @StrutsParameter
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
  @StrutsParameter
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
   * @return null. Only here to satisfy jsp
   */
  public String getUpdGroupMember() {
    return null;
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

  /* ==============================================================
   *                   Calendar suites
   * ============================================================== */

  /** Name of CalSuite to edit/add/delete.
   *
   * @param val
   */
  @StrutsParameter
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
  @StrutsParameter
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
  @StrutsParameter
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
  @StrutsParameter
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

  @Override
  public void reset(final HttpServletRequest request) {
    super.reset(request);

    editAuthUserType = 0;
  }
}
