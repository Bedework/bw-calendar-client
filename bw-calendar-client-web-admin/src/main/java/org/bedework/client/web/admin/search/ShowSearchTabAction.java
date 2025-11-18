package org.bedework.client.web.admin.search;

import org.bedework.calfacade.indexing.BwIndexer;
import org.bedework.client.admin.AdminClient;
import org.bedework.client.web.admin.BwAdminWebGlobals;
import org.bedework.webcommon.BwRequest;
import org.bedework.webcommon.RenderMainAction;

import org.apache.struts2.interceptor.parameter.StrutsParameter;

public class ShowSearchTabAction extends RenderMainAction {
  private String currentTab;

  @Override
  public int doAction(final BwRequest request) {
    final var globals = (BwAdminWebGlobals)request.getBwGlobals();
    final var cl = (AdminClient)request.getClient();
    globals.assignCurrentTab(currentTab);
    request.refresh();

    final var fwd = super.doAction(request);
    if (fwd == forwardSuccess) {
      request.setRequestAttr(BwRequest.bwSearchListName,
                             cl.getSearchResult(BwIndexer.Position.current));
    }

    return fwd;
  }

  @StrutsParameter
  public void setCurrentTab(final String val) {
    currentTab = val;
  }
}
