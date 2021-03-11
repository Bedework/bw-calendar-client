/* ********************************************************************
    Appropriate copyright notice
*/
package org.bedework.client.web.admin;

import org.bedework.client.admin.AdminClient;
import org.bedework.webcommon.BwAbstractAction;
import org.bedework.webcommon.BwActionFormBase;
import org.bedework.webcommon.BwRequest;

/**
 * User: mike Date: 3/9/21 Time: 22:37
 */
public abstract class AdminActionBase extends BwAbstractAction {
  @Override
  public int doAction(final BwRequest request,
                      final BwActionFormBase form) throws Throwable {
    return doAction(request,
                    (AdminClient)request.getClient(),
                    (BwAdminActionForm)form);
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
}
