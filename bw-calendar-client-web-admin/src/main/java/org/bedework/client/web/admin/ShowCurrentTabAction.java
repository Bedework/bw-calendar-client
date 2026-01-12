/* ********************************************************************
    Appropriate copyright notice
*/
package org.bedework.client.web.admin;

import org.bedework.base.exc.BedeworkException;
import org.bedework.calfacade.indexing.BwIndexer;
import org.bedework.client.admin.AdminClient;
import org.bedework.webcommon.BwRequest;
import org.bedework.webcommon.RenderMainAction;

import org.apache.struts2.interceptor.parameter.StrutsParameter;

import java.util.Arrays;
import java.util.List;

/**
 * User: mike Date: 3/10/21 Time: 21:17
 */
public class ShowCurrentTabAction extends RenderMainAction {
  private List<String> validForwards;

  private List<String> eventSearches;

  @Override
  public String doAction(final BwRequest request) {
    final var globals = (BwAdminWebGlobals)request.getBwGlobals();

    //    if (request.isNewSession()) {
      request.refresh();
//    }

    final var cl = (AdminClient)request.getClient();

    if (!cl.isApprover()) {
      // Force to approval q
      globals.assignCurrentTab("approvalQueue");
    }

    final var fwd = globals.getCurrentTab();
    if (!validForwards.contains(fwd)) {
      throw new BedeworkException(
              "No forward - tab not defined: " + fwd);
    }

    if (eventSearches.contains(fwd)) {
      final var sfwd = super.doAction(request);
      if (!forwardSuccess.equals(sfwd)) {
        return sfwd;
      }

      request.embedAdminGroups();
      request.setRequestAttr(BwRequest.bwSearchListName,
                             cl.getSearchResult(BwIndexer.Position.current));
    }

    return fwd;
  }

  @StrutsParameter
  public void setValidForwards(final String val) {
    validForwards = Arrays.asList(val.split("\\s*,\\s*"));
  }

  @StrutsParameter
  public void setEventSearches(final String val) {
    eventSearches = Arrays.asList(val.split("\\s*,\\s*"));
  }
}
