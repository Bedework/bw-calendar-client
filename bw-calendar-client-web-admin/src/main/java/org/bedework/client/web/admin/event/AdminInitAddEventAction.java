/* ********************************************************************
    Appropriate copyright notice
*/
package org.bedework.client.web.admin.event;

import org.bedework.client.web.admin.AdminUtil;
import org.bedework.client.web.admin.BwAdminWebGlobals;
import org.bedework.client.web.rw.event.InitAddEventAction;
import org.bedework.webcommon.BwRequest;
import org.bedework.webcommon.BwSession;

import org.apache.struts2.interceptor.parameter.StrutsParameter;

/**
 * User: mike Date: 3/10/21 Time: 21:26
 */
public class AdminInitAddEventAction extends InitAddEventAction {
  private String currentTab;

  @Override
  public int doAction(final BwRequest request) {
    final var fwd = super.doAction(request);
    final var sess = request.getSess();
    final var globals = (BwAdminWebGlobals)request.getBwGlobals();

    globals.assignCurrentTab(currentTab);
    sess.embedContactCollection(request, BwSession.preferredEntity);
    sess.embedLocations(request, BwSession.preferredEntity);

    if (globals.getSuggestionEnabled()) {
      AdminUtil.embedPreferredAdminGroups(request);
      AdminUtil.embedCalsuiteAdminGroups(request);
    }

    return fwd;
  }

  @StrutsParameter
  public void setCurrentTab(final String val) {
    currentTab = val;
  }
}
