/* ********************************************************************
    Appropriate copyright notice
*/
package org.bedework.client.web.admin;

import org.bedework.calfacade.svc.BwAuthUser;
import org.bedework.calfacade.svc.UserAuth;
import org.bedework.webcommon.BwActionFormBase;

import org.apache.struts.action.ActionMapping;

import javax.servlet.http.HttpServletRequest;

/**
 * User: mike Date: 3/9/21 Time: 22:25
 */
public class BwAdminActionForm extends BwActionFormBase {
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
   * @param val
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
   * @param val
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
}
