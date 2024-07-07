/* ********************************************************************
    Appropriate copyright notice
*/
package org.bedework.client.web.admin.event;

import org.bedework.client.web.admin.AdminUtil;
import org.bedework.client.web.admin.BwAdminWebGlobals;
import org.bedework.client.web.rw.event.InitAddEventAction;
import org.bedework.webcommon.BwActionFormBase;
import org.bedework.webcommon.BwRequest;
import org.bedework.webcommon.BwSession;

/**
 * User: mike Date: 3/10/21 Time: 21:26
 */
public class AdminInitAddEventAction extends InitAddEventAction {
  @Override
  public int doAction(final BwRequest request,
                      final BwActionFormBase form) {
    final int fwd = super.doAction(request, form);

    final BwSession sess = request.getSess();

    sess.embedContactCollection(request, BwSession.preferredEntity);
    sess.embedLocations(request, BwSession.preferredEntity);

    if (((BwAdminWebGlobals)request.getBwGlobals()).getSuggestionEnabled()) {
      AdminUtil.embedPreferredAdminGroups(request);
      AdminUtil.embedCalsuiteAdminGroups(request);
    }

    return fwd;
  }
}
