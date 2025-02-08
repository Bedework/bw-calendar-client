/* ********************************************************************
    Appropriate copyright notice
*/
package org.bedework.client.web.admin.event;

import org.bedework.client.web.admin.BwAdminWebGlobals;
import org.bedework.webcommon.BwRequest;
import org.bedework.webcommon.search.SearchParamsAction;

import org.apache.struts2.interceptor.parameter.StrutsParameter;

/**
 * User: mike Date: 3/10/21 Time: 21:17
 */
public class InitEventTabAction extends SearchParamsAction {
  private String currentTab;

  @Override
  public int doAction(final BwRequest request) {
    final var globals = (BwAdminWebGlobals)request.getBwGlobals();
    globals.assignCurrentTab(currentTab);

    return super.doAction(request);
  }

  @StrutsParameter
  public void setCurrentTab(final String val) {
    currentTab = val;
  }
}
