/* ********************************************************************
    Appropriate copyright notice
*/
package org.bedework.client.web.admin;

import org.bedework.webcommon.BwActionFormBase;
import org.bedework.webcommon.BwRequest;
import org.bedework.webcommon.RenderAction;

/**
 * User: mike Date: 3/10/21 Time: 21:17
 */
public class ShowTabAction extends RenderAction {
  private String currentTab;

  @Override
  public int doAction(final BwRequest request,
                      final BwActionFormBase form) throws Throwable {
    ((BwAdminActionForm)form).assignCurrentTab(currentTab);

    return super.doAction(request, form);
  }

  public void setCurrentTab(final String val) {
    currentTab = val;
  }
}
