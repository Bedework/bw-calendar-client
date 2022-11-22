/* ********************************************************************
    Appropriate copyright notice
*/
package org.bedework.client.web.admin.event;

import org.bedework.calfacade.BwContact;
import org.bedework.calfacade.BwEvent;
import org.bedework.calfacade.BwLocation;
import org.bedework.calfacade.BwXproperty;
import org.bedework.calfacade.exc.ValidationError;
import org.bedework.calfacade.svc.BwCalSuite;
import org.bedework.calfacade.svc.EventInfo;
import org.bedework.client.admin.AdminClient;
import org.bedework.client.rw.RWClient;
import org.bedework.client.web.admin.AdminActionBase;
import org.bedework.client.web.admin.BwAdminActionForm;
import org.bedework.client.web.rw.event.UpdatePars;
import org.bedework.sysevents.events.SysEventBase;
import org.bedework.sysevents.events.publicAdmin.EntityApprovalResponseEvent;
import org.bedework.util.calendar.PropertyIndex.PropertyInfoIndex;
import org.bedework.util.misc.Util;
import org.bedework.webcommon.BwRequest;

import java.util.List;

import static org.bedework.client.web.rw.EventCommon.addError;
import static org.bedework.client.web.rw.EventCommon.notifyEventReg;
import static org.bedework.client.web.rw.EventCommon.notifySubmitter;

/**
 * User: mike Date: 3/10/21 Time: 21:26
 */
public class ApprovePublishAction extends AdminActionBase {
  @Override
  public int doAction(final BwRequest request,
                      final AdminClient cl,
                      final BwAdminActionForm form) throws Throwable {
    final AdminUpdatePars pars = new AdminUpdatePars(request, cl, form);

    final AdminClient adcl = (AdminClient)pars.cl;

    final BwEvent ev = pars.ev;

    if (pars.approveEvent &&
            !form.getCurUserApproverUser()) {
      cl.rollback();
      return forwardNoAccess;
    }

    if ((pars.publishEvent || pars.approveEvent) &&
            (ev.getRecurrenceId() != null)) {
      // Cannot publish/approve an instance - only the master
      cl.rollback();
      return forwardError;
    }

    if (pars.publishEvent) {
      // We might need the submitters info */

      final List<BwXproperty> xps =
              ev.getXproperties(BwXproperty.bedeworkSubmitterEmail);

      if (!Util.isEmpty(xps)) {
        pars.submitterEmail = xps.get(0).getValue();
      }
    }

    /* ------------------ validation -------------------------- */

    final List<ValidationError> ves = validate(pars);

    if (ves != null) {
      for (final ValidationError ve: ves) {
        request.error(ve.getErrorCode(), ve.getExtra());
      }
      return forwardValidationError;
    }

    if (pars.publishEvent) {
      /* ------- web submit - copy entities and change owner ------ */

      copyEntities(ev);
      changeOwner(ev, pars.cl);
      pars.changes.changed(PropertyInfoIndex.CREATOR, null,
                           ev.getCreatorHref());

      // Do the same for any overrides

      if (ev.getRecurring() &&
              (pars.ei.getOverrideProxies() != null)) {
        for (final BwEvent oev: pars.ei.getOverrideProxies()) {
          copyEntities(oev);
          changeOwner(oev, pars.cl);
          oev.setColPath(ev.getColPath());
        }
      }

      final EventInfo.UpdateResult ur =
              pars.cl.updateEvent(pars.ei, !pars.sendInvitations,
                                  null, false);

      if (!ur.isOk()) {
        pars.request.error(ur.getMessage());
        pars.cl.rollback();
        return forwardError;
      }

      if (request.getBooleanReqPar("submitNotification", false)) {
        notifySubmitter(pars.request, pars.ei, pars.submitterEmail);
      }
    }

    /* --------------- Move into public calendar collection ------- */

    final var resp = pars.cl.moveEvent(pars.ei,
                                       cl.getPrimaryPublicPath());
    if (!resp.isOk()) {
      pars.request.error(resp.getMessage());
      pars.cl.rollback();
      return forwardError;
    }

    if (pars.approveEvent) {
      /* Post an event flagging the approval.
         The change notification processor will add the
         notification(s).
        */

      final BwCalSuite cs = adcl.getCalSuite();
      final String csHref;

      if (cs != null) {
        csHref = cs.getGroup().getOwnerHref();
      } else {
        csHref = null;
      }

      final SysEventBase sev;

      sev = new EntityApprovalResponseEvent(
              SysEventBase.SysCode.APPROVAL_STATUS,
              adcl.getCurrentPrincipalHref(),
              ev.getCreatorHref(),
              ev.getHref(),
              null,
              true,
              null,
              csHref);

      adcl.postNotification(sev);
    }

    /* See if we need to notify event registration system for add/update */
    final BwXproperty evregprop =
            ev.findXproperty(BwXproperty.bedeworkEventRegStart);

    if (evregprop != null) {
      // Registerable event
      notifyEventReg(pars.request, pars.ei);
    }

    pars.ev.setPublick(true);

    return forwardSuccess;
  }

  protected List<ValidationError> validate(final UpdatePars pars) {
    final AdminUpdatePars adPars = (AdminUpdatePars)pars;
    final var cl = pars.cl;

    final BwEvent ev = pars.ev;

    List<ValidationError> ves = adPars.validate();

    if (!Util.isEmpty(ves)) {
      return ves;
    }

    final String colPath = ev.getColPath();

    if (adPars.publishEvent) {
      /* Event MUST be in a submission calendar */
      if (!colPath.startsWith(adPars.submissionsRoot)) {
        ves = addError(ves, ValidationError.notInSubmissionsCalendar);
        return ves;
      }
    } else if (adPars.approveEvent) {
      /* Event MUST be in a workflow calendar */
      if (!colPath.startsWith(adPars.workflowRoot)) {
        ves = addError(ves, ValidationError.notInWorkflowCalendar);
        return ves;
      }
    }

    return ves;
  }

  private void changeOwner(final BwEvent ev,
                           final RWClient cl) {
    cl.claimEvent(ev);
    ev.setCreatorHref(cl.getCurrentPrincipalHref());
  }

  private void copyEntities(final BwEvent ev) {
    /* Copy event entities */
    BwLocation loc = ev.getLocation();
    if ((loc != null) && !loc.getPublick()) {
      loc = (BwLocation)loc.clone();
      loc.setOwnerHref(null);
      loc.setCreatorHref(null);
      loc.setPublick(true);
      ev.setLocation(loc);
    }

    BwContact contact = ev.getContact();
    if ((contact != null)  && !contact.getPublick()) {
      contact = (BwContact)contact.clone();
      contact.setOwnerHref(null);
      contact.setCreatorHref(null);
      contact.setPublick(true);
      ev.setLocation(loc);
      ev.setContact(contact);
    }
  }
}
