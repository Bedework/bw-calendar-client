/* ********************************************************************
    Appropriate copyright notice
*/
package org.bedework.client.web.admin;

import org.bedework.webcommon.BwRequest;
import org.bedework.webcommon.RenderAction;

import org.apache.struts2.interceptor.parameter.StrutsParameter;

/**
 * User: mike Date: 3/10/21 Time: 21:17
 */
public class ShowTabAction extends RenderAction {
  private String currentTab;

  @Override
  public String doAction(final BwRequest request) {
    final var globals = (BwAdminWebGlobals)request.getBwGlobals();
    globals.assignCurrentTab(currentTab);

    return super.doAction(request);
  }

  @StrutsParameter
  public void setCurrentTab(final String val) {
    currentTab = val;
  }
}
