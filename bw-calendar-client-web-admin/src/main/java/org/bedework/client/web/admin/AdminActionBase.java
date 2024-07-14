/* ********************************************************************
    Appropriate copyright notice
*/
package org.bedework.client.web.admin;

import org.bedework.calfacade.svc.BwAdminGroup;
import org.bedework.client.admin.AdminClient;
import org.bedework.client.rw.RWClient;
import org.bedework.client.web.rw.RWActionBase;
import org.bedework.webcommon.BwRequest;

/**
 * User: mike Date: 3/9/21 Time: 22:37
 */
public abstract class AdminActionBase extends RWActionBase {
  @Override
  public int doAction(final BwRequest request) {
    final AdminClient cl = (AdminClient)request.getClient();

    /* Check access
     */
    if (cl.isGuest()) {
      return forwardNoAccess; // First line of defence
    }

    return doAction(request, cl);
  }

  public int doAction(final BwRequest request,
                      final RWClient cl) {
    throw new RuntimeException("Should never be called");
  }

  /** This is the routine which does the work.
   *
   * @param request   For request pars and BwSession
   * @return int      forward index
   */
  public abstract int doAction(BwRequest request,
                               AdminClient cl);

  public BwAdminActionForm getAdminForm() {
    return (BwAdminActionForm)getForm();
  }

  @SuppressWarnings("UnusedDeclaration")
  public BwAdminGroup getUpdAdminGroup() {
    return getAdminForm().getUpdAdminGroup();
  }

  /**
   * @param val true for show group members
   */
  @SuppressWarnings("UnusedDeclaration")
  public void setShowAgMembers(final boolean val) {
    getAdminForm().setShowAgMembers(val);
  }

  /**
   * @return true for show group members
   */
  @SuppressWarnings("UnusedDeclaration")
  public boolean getShowAgMembers() {
    return getAdminForm().getShowAgMembers();
  }
}
