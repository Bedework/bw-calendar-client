/* ********************************************************************
    Appropriate copyright notice
*/
package org.bedework.client.web.admin;

import org.bedework.appcommon.client.Client;
import org.bedework.client.admin.AdminClient;
import org.bedework.client.web.rw.BwRWWebGlobals;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * User: mike Date: 7/4/24 Time: 12:31
 */
public class BwAdminWebGlobals extends BwRWWebGlobals {
  private boolean superUser;

  private String currentTab = "main";

  private boolean suggestionEnabled;
  private boolean workflowEnabled;
  private String workflowRoot;
  private String encodedWorkflowRoot;

  private boolean oneGroup;

  private String adminGroupName;

  private boolean userMaintOK;

  private boolean adminGroupMaintOK;

  /* Settings for current authenticated user */
  private boolean curUserContentAdminUser;
  private boolean curUserApproverUser;

  public boolean isSuperUser() {
    return superUser;
  }

  /**
   * @param val admin group name
   */
  public void assignCurrentTab(final String val) {
    currentTab = val;
  }

  /**
   * @return String admin group name
   */
  public String getCurrentTab() {
    return currentTab;
  }

  public boolean getSuggestionEnabled() {
    return suggestionEnabled;
  }

  public boolean getWorkflowEnabled() {
    return workflowEnabled;
  }

  /** Return the unencoded root of the workflow collections
   *
   * @return String path.
   */
  public String getWorkflowRoot() {
    return workflowRoot;
  }

  /** Return the encoded root of the workflow collections
   *
   * @return String path.
   */
  public String getEncodedWorkflowRoot() {
    return encodedWorkflowRoot;
  }

  /**
   * @return true if there is only one group
   */
  public boolean getOneGroup() {
    return oneGroup;
  }

  /**
   * @return String admin group name
   */
  public String getAdminGroupName() {
    return adminGroupName;
  }

  /** Show whether user entries can be displayed or modified with this
   * class. Some sites may use other mechanisms.
   *
   * @return boolean    true if user maintenance is implemented.
   */
  public boolean getUserMaintOK() {
    return userMaintOK;
  }

  /** Show whether admin group maintenance is available.
   * Some sites may use other mechanisms.
   *
   * @return boolean    true if admin group maintenance is implemented.
   */
  public boolean getAdminGroupMaintOK() {
    return adminGroupMaintOK;
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

  public void reset(final Client cl) {
    final AdminClient adcl = (AdminClient)cl;
    superUser = cl.isSuperUser();
    super.reset(cl);

    final var sysprops = cl.getSystemProperties();

    suggestionEnabled = sysprops.getSuggestionEnabled();
    workflowEnabled = sysprops.getWorkflowEnabled();
    workflowRoot = sysprops.getWorkflowRoot();
    if (workflowRoot != null) {
      encodedWorkflowRoot = URLEncoder.encode(workflowRoot,
                                              StandardCharsets.UTF_8);
    }

    oneGroup = adcl.getOneGroup();
    adminGroupName = adcl.getAdminGroupName();

    userMaintOK = adcl.getUserMaintOK();
  }
}
