/* ********************************************************************
    Appropriate copyright notice
*/
package org.bedework.client.web.admin;

import org.bedework.calfacade.indexing.BwIndexer;
import org.bedework.client.admin.AdminClient;
import org.bedework.webcommon.BwActionFormBase;
import org.bedework.webcommon.BwRequest;
import org.bedework.webcommon.RenderMainAction;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * User: mike Date: 3/10/21 Time: 21:17
 */
public class ShowCurrentTabAction extends RenderMainAction {
  private final static Map<String, Integer> forwardsTab =
          new HashMap<>();
  static {
    forwardsTab.put("main", forwardMainTab);
    forwardsTab.put("approvalQueue", forwardApprovalQTab);
    forwardsTab.put("pending", forwardPendingQTab);
    forwardsTab.put("suggestionQueue", forwardSuggestionQTab);
    forwardsTab.put("users", forwardUsersTab);
    forwardsTab.put("calsuite", forwardCalsuiteTab);
    forwardsTab.put("system", forwardSystemTab);
  }

  private final List<Integer> eventSearchTab =
          Arrays.asList(forwardMainTab,
                        forwardApprovalQTab,
                        forwardPendingQTab,
                        forwardSuggestionQTab);

  @Override
  public int doAction(final BwRequest request,
                      final BwActionFormBase form) {

    final BwAdminActionForm aform = (BwAdminActionForm)form;
//    if (form.getNewSession()) {
      request.refresh();
//    }

    final var cl = (AdminClient)request.getClient();

    if (!cl.isApprover()) {
      // Force to approval q
      aform.assignCurrentTab("approvalQueue");
    }

    final var fwd = forwardsTab.get(aform.getCurrentTab());
    if (fwd == null) {
      throw new RuntimeException("No forward - tab not defined: " +
              aform.getCurrentTab());
    }

    if (eventSearchTab.contains(fwd)) {
      final var sfwd = super.doAction(request, form);
      if (sfwd != forwardSuccess) {
        return sfwd;
      }
      request.setRequestAttr(BwRequest.bwSearchListName,
                             cl.getSearchResult(BwIndexer.Position.current));
    }

    return fwd;
  }
}
