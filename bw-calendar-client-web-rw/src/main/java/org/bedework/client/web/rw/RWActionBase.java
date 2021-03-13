/* ********************************************************************
    Appropriate copyright notice
*/
package org.bedework.client.web.rw;

import org.bedework.client.rw.RWClient;
import org.bedework.webcommon.BwAbstractAction;
import org.bedework.webcommon.BwActionFormBase;
import org.bedework.webcommon.BwRequest;

import javax.servlet.http.HttpServletResponse;

/**
 * User: mike Date: 3/9/21 Time: 22:37
 */
public abstract class RWActionBase extends BwAbstractAction {
  @Override
  public int doAction(final BwRequest request,
                      final BwActionFormBase form) throws Throwable {
    final RWClient cl = (RWClient)request.getClient();

    /* Check access
     */
    if (cl.isGuest()) {
      if (actionIsWebService()) {
        request.getResponse().sendError(HttpServletResponse.SC_FORBIDDEN);

        return forwardNull;
      }

      return forwardNoAccess; // First line of defence
    }

    return doAction(request,
                    cl,
                    (BwRWActionForm)form);
  }

  /**
   * @return true if we should set status and return null for guest.
   */
  protected boolean actionIsWebService() {
    return false;
  }

  /** This is the routine which does the work.
   *
   * @param request   For request pars and BwSession
   * @param form       Admin action form
   * @return int      forward index
   * @throws Throwable on fatal error
   */
  public abstract int doAction(BwRequest request,
                               RWClient cl,
                               BwRWActionForm form) throws Throwable;
}
