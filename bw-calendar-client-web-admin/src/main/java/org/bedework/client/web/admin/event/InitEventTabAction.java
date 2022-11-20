/* ********************************************************************
    Appropriate copyright notice
*/
package org.bedework.client.web.admin.event;

import org.bedework.client.web.admin.BwAdminActionForm;
import org.bedework.webcommon.BwActionFormBase;
import org.bedework.webcommon.BwRequest;
import org.bedework.webcommon.search.SearchParamsAction;

/**
 * User: mike Date: 3/10/21 Time: 21:17
 */
public class InitEventTabAction extends SearchParamsAction {
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
