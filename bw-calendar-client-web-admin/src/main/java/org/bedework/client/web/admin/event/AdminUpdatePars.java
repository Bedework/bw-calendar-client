/* ********************************************************************
    Appropriate copyright notice
*/
package org.bedework.client.web.admin.event;

import org.bedework.calfacade.BwCategory;
import org.bedework.calfacade.BwEvent;
import org.bedework.calfacade.exc.ValidationError;
import org.bedework.client.rw.RWClient;
import org.bedework.client.web.rw.event.UpdatePars;
import org.bedework.util.misc.Util;
import org.bedework.webcommon.BwRequest;

import java.util.List;
import java.util.Set;

import static org.bedework.client.web.rw.EventCommon.addError;
import static org.bedework.client.web.rw.EventCommon.validateEvent;

/**
 * User: mike Date: 11/21/22 Time: 22:51
 */
public class AdminUpdatePars extends UpdatePars {
  String submitterEmail;

  final boolean publishEvent;
  final boolean updateSubmitEvent;
  final boolean approveEvent;

  final String submissionsRoot;
  final String workflowRoot;

  // TODO - set this based on an x-prop or a request param
  boolean awaitingApprovalEvent;

  List<BwEvent.SuggestedTo> suggestedTo;

  // Set when we realias or approve/publish
  Set<BwCategory> categories;

  AdminUpdatePars(final BwRequest request,
                  final RWClient cl) {
    super(request, cl);

    publishEvent = request.present("publishEvent");
    updateSubmitEvent = request.present("updateSubmitEvent");
    approveEvent = request.present("approveEvent");

    submissionsRoot = cl.getSystemProperties().getSubmissionRoot();
    workflowRoot = cl.getSystemProperties().getWorkflowRoot();

    categories = ev.getCategories();
  }

  @Override
  public List<ValidationError> validate() {
    List<ValidationError> ves =
            validateEvent(cl,
                          cl.getAuthProperties()
                            .getMaxPublicDescriptionLength(),
                          false,
                          ev);

    if (!Util.isEmpty(ves)) {
      return ves;
    }

    if (!updateSubmitEvent) {
      /* -------------------------- Location ------------------------------ */

      if (ev.getLocation() == null) {
        ves = addError(ves, ValidationError.missingLocation);
      }

      /* -------------------------- Contact ------------------------------ */

      if (ev.getContact() == null) {
        ves = addError(ves, ValidationError.missingContact);
      }
    }

    /* ------- col path related -------- */

    if (updateSubmitEvent) {
      /* Event MUST be in a submission calendar */
      if (!ev.getColPath().startsWith(submissionsRoot)) {
        ves = addError(ves, ValidationError.notSubmissionsCalendar);
        cl.rollback();
        return ves;
      }
    } else if ((workflowRoot != null) &&
            ev.getColPath().startsWith(workflowRoot)) {
      awaitingApprovalEvent = adding;
    }

    /* ------- topical areas -------- */

    if (!updateSubmitEvent && Util.isEmpty(categories)) {
      ves = addError(ves, ValidationError.missingTopic);
    }

    return ves;
  }
}
