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

  private boolean suggestionEnabled;
  private boolean workflowEnabled;
  private String workflowRoot;
  private String encodedWorkflowRoot;

  private boolean oneGroup;

  private String adminGroupName;

  public boolean isSuperUser() {
    return superUser;
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
  }
}
