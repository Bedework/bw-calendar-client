/* ********************************************************************
    Appropriate copyright notice
*/
package org.bedework.client.web.admin.event;

import org.bedework.client.rw.RWClient;
import org.bedework.client.web.admin.AdminUtil;
import org.bedework.client.web.admin.BwAdminWebGlobals;
import org.bedework.client.web.rw.event.FetchEventAction;
import org.bedework.webcommon.BwRequest;
import org.bedework.webcommon.BwSession;

/**
 * User: mike Date: 3/10/21 Time: 21:17
 */
public class AdminFetchEventAction extends FetchEventAction {
  @Override
  public String doAction(final BwRequest request,
                         final RWClient cl) {
    final var fwd = doTheAction(request, cl);

    if (((BwAdminWebGlobals)request.getBwGlobals()).getSuggestionEnabled()) {
      AdminUtil.embedPreferredAdminGroups(request);
      AdminUtil.embedCalsuiteAdminGroups(request);
    }

    final var sess = request.getSess();

    sess.embedContactCollection(request, BwSession.preferredEntity);
    sess.embedLocations(request, BwSession.preferredEntity);

    return fwd;
  }
}
