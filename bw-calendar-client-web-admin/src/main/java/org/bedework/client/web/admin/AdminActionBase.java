/* ********************************************************************
    Appropriate copyright notice
*/
package org.bedework.client.web.admin;

import org.bedework.calfacade.svc.BwAdminGroup;
import org.bedework.client.admin.AdminClient;
import org.bedework.client.rw.RWClient;
import org.bedework.client.web.rw.BwRWActionForm;
import org.bedework.client.web.rw.RWActionBase;
import org.bedework.webcommon.BwActionFormBase;
import org.bedework.webcommon.BwRequest;

/**
 * User: mike Date: 3/9/21 Time: 22:37
 */
public abstract class AdminActionBase extends RWActionBase {
  @Override
  public int doAction(final BwRequest request,
                      final BwActionFormBase form) throws Throwable {
    final AdminClient cl = (AdminClient)request.getClient();

    /* Check access
     */
    if (cl.isGuest()) {
      return forwardNoAccess; // First line of defence
    }

    return doAction(request,
                    cl,
                    (BwAdminActionForm)form);
  }

  public int doAction(final BwRequest request,
                      final RWClient cl,
                      final BwRWActionForm form) {
    throw new RuntimeException("Should never be called");
  }

  /** This is the routine which does the work.
   *
   * @param request   For request pars and BwSession
   * @param form       Admin action form
   * @return int      forward index
   * @throws Throwable on fatal error
   */
  public abstract int doAction(BwRequest request,
                               AdminClient cl,
                               BwAdminActionForm form) throws Throwable;

  public BwAdminActionForm getAdminForm() {
    return (BwAdminActionForm)getForm();
  }

  public BwAdminGroup getUpdAdminGroup() {
    return getAdminForm().getUpdAdminGroup();
  }
}
