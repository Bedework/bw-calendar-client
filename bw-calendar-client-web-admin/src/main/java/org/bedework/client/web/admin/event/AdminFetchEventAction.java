/* ********************************************************************
    Appropriate copyright notice
*/
package org.bedework.client.web.admin.event;

import org.bedework.client.web.admin.AdminUtil;
import org.bedework.webcommon.BwActionFormBase;
import org.bedework.webcommon.BwRequest;
import org.bedework.webcommon.BwSession;
import org.bedework.webcommon.event.FetchEventAction;

/**
 * User: mike Date: 3/10/21 Time: 21:17
 */
public class AdminFetchEventAction extends FetchEventAction {
  @Override
  public int doAction(final BwRequest request,
                      final BwActionFormBase form) throws Throwable {
    if (!form.getAuthorisedUser()) {
      return forwardNoAccess;
    }

    final int fwd = doAction(request, request.getClient(), form);

    if (form.getSuggestionEnabled()) {
      AdminUtil.embedPreferredAdminGroups(request);
      AdminUtil.embedCalsuiteAdminGroups(request);
    }

    final BwSession sess = request.getSess();

    sess.embedContactCollection(request, BwSession.preferredEntity);
    sess.embedLocations(request, BwSession.preferredEntity);

    return fwd;
  }
}
