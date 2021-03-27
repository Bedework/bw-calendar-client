/* ********************************************************************
    Appropriate copyright notice
*/
package org.bedework.client.web.admin.event;

import org.bedework.calfacade.BwCategory;
import org.bedework.calfacade.BwContact;
import org.bedework.calfacade.BwEvent;
import org.bedework.calfacade.BwEvent.SuggestedTo;
import org.bedework.calfacade.BwLocation;
import org.bedework.calfacade.BwXproperty;
import org.bedework.calfacade.exc.ValidationError;
import org.bedework.calfacade.svc.BwAdminGroup;
import org.bedework.calfacade.svc.BwCalSuite;
import org.bedework.calfacade.svc.RealiasResult;
import org.bedework.calfacade.util.ChangeTableEntry;
import org.bedework.client.admin.AdminClient;
import org.bedework.client.admin.AdminConfig;
import org.bedework.client.rw.RWClient;
import org.bedework.client.web.admin.BwAdminActionForm;
import org.bedework.client.web.rw.BwRWActionForm;
import org.bedework.client.web.rw.event.UpdateEventAction;
import org.bedework.sysevents.events.SysEventBase;
import org.bedework.sysevents.events.publicAdmin.EntityApprovalNeededEvent;
import org.bedework.sysevents.events.publicAdmin.EntityApprovalResponseEvent;
import org.bedework.sysevents.events.publicAdmin.EntitySuggestedEvent;
import org.bedework.util.calendar.PropertyIndex.PropertyInfoIndex;
import org.bedework.util.misc.Util;
import org.bedework.webcommon.BwRequest;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import static org.bedework.client.web.rw.EventCommon.addError;
import static org.bedework.client.web.rw.EventCommon.adminEventLocation;
import static org.bedework.client.web.rw.EventCommon.notifyEventReg;
import static org.bedework.client.web.rw.EventCommon.notifySubmitter;
import static org.bedework.client.web.rw.EventCommon.resetEvent;
import static org.bedework.client.web.rw.EventCommon.validateEvent;
import static org.bedework.util.misc.response.Response.Status.ok;

/**
 * User: mike Date: 3/10/21 Time: 21:26
 */
public class AdminUpdateEventAction extends UpdateEventAction {
  public static class AdminUpdatePars extends UpdatePars {
    String submitterEmail;

    final boolean publishEvent;
    final boolean updateSubmitEvent;
    final boolean approveEvent;

    final String submissionsRoot;
    final String workflowRoot;

    // TODO - set this based on an x-prop or a request param
    boolean awaitingApprovalEvent;

    List<SuggestedTo> suggestedTo;

    AdminUpdatePars(final BwRequest request,
                    final RWClient cl,
                    final BwRWActionForm form) {
      super(request, cl, form);

      publishEvent = request.present("publishEvent");
      updateSubmitEvent = request.present("updateSubmitEvent");
      approveEvent = request.present("approveEvent");

      submissionsRoot = form.getConfig().getSubmissionRoot();
      workflowRoot = cl.getSystemProperties().getWorkflowRoot();
    }
  }

  @Override
  public int doAction(final BwRequest request,
                      final RWClient cl,
                      final BwRWActionForm form) throws Throwable {
    final AdminUpdatePars pars = new AdminUpdatePars(request, cl, form);

    if (pars.approveEvent &&
            !((BwAdminActionForm)form).getCurUserApproverUser()) {
      cl.rollback();
      return forwardNoAccess;
    }

    if ((pars.publishEvent || pars.approveEvent) &&
            (pars.ev.getRecurrenceId() != null)) {
      // Cannot publish/approve an instance - only the master
      cl.rollback();
      return forwardError;
    }

    final int fwd = super.doUpdate(request, cl, form, pars);

    pars.ev.setPublick(true);

    return fwd;
  }

  @Override
  protected boolean isOwner(final UpdatePars pars) {
    return pars.form.getAddingEvent() ||
            ((AdminClient)pars.cl).isCalSuiteEntity(pars.ev);
  }

  @Override
  protected boolean setLocation(final UpdatePars pars) throws Throwable {
    if (!adminEventLocation(pars.request, pars.ei)) {
      restore(pars);
      return false;
    }

    return true;
  }

  @Override
  protected Set<BwCategory> doAliases(final UpdatePars pars) {
    final AdminUpdatePars adPars = (AdminUpdatePars)pars;

    final RealiasResult resp = pars.cl.reAlias(pars.ev);
    if (resp.getStatus() != ok) {
      if (debug()) {
        debug("Failed to get topical areas? " + resp);
      }
      pars.cl.rollback();
      pars.request.error(ValidationError.missingTopic);
      restore(pars);
      return null;
    }

    if (!adPars.updateSubmitEvent && Util.isEmpty(resp.getCats())) {
      if (debug()) {
        debug("No topical areas? " + resp);
      }
      pars.request.error(ValidationError.missingTopic);
      restore(pars);
      return null;
    }

    return resp.getCats();
  }

  @Override
  protected boolean doAdditional(final UpdatePars pars) throws Throwable {
    final AdminUpdatePars adPars = (AdminUpdatePars)pars;

    if (isOwner(pars)) {
      adPars.suggestedTo = doSuggested(adPars);
    }

    return true;
  }

  /* return forwardNoAction for no change
   * forward success for change otherwise error.
   */
  @Override
  protected int processXprops(final UpdatePars pars,
                            final List<BwXproperty> extras) throws Throwable {
    final AdminUpdatePars adPars = (AdminUpdatePars)pars;

    if ((adPars.publishEvent || adPars.updateSubmitEvent)) {
      // We might need the submitters info */

      final List<BwXproperty> xps =
              adPars.ev.getXproperties(BwXproperty.bedeworkSubmitterEmail);

      if (!Util.isEmpty(xps)) {
        adPars.submitterEmail = xps.get(0).getValue();
      }
    }

    return super.processXprops(pars, extras);
  }

  @Override
  protected List<ValidationError> validate(final UpdatePars pars) throws Throwable {
    final AdminUpdatePars adPars = (AdminUpdatePars)pars;

    final boolean prePublish = adPars.updateSubmitEvent;

    final BwEvent ev = pars.ev;

    List<ValidationError> ves =
            validateEvent(pars.cl,
                          pars.cl.getAuthProperties()
                                 .getMaxPublicDescriptionLength(),
                          false,
                          ev);

    if (!Util.isEmpty(ves)) {
      return ves;
    }

    if (!prePublish) {
      /* -------------------------- Location ------------------------------ */

      if (ev.getLocation() == null) {
        ves = addError(ves, ValidationError.missingLocation);
      }

      /* -------------------------- Contact ------------------------------ */

      if (ev.getContact() == null) {
        ves = addError(ves, ValidationError.missingContact);
      }
    }

    /* ------- web submit - copy entities and change owner -------- */

    final String colPath = ev.getColPath();

    if (adPars.publishEvent) {
      /* Event MUST NOT be in a submission calendar */
      if (colPath.startsWith(adPars.submissionsRoot)) {
        ves = addError(ves, ValidationError.inSubmissionsCalendar);
        pars.cl.rollback();
        return ves;
      }
    } else if (adPars.approveEvent) {
      /* Event MUST NOT be in a workflow calendar */
      if (colPath.startsWith(adPars.workflowRoot)) {
        ves = addError(ves, ValidationError.inSubmissionsCalendar);
        restore(pars);
        pars.cl.rollback();
        return ves;
      }

      // See if colpath changed and if so change the overrides
      if (ev.getRecurring() &&
              !pars.preserveColPath.equals(colPath) &&
              (pars.ei.getOverrideProxies() != null)) {
        for (final BwEvent oev: pars.ei.getOverrideProxies()) {
          oev.setColPath(ev.getColPath());
        }
      }

    } else if (adPars.updateSubmitEvent) {
      /* Event MUST be in a submission calendar */
      if (!colPath.startsWith(adPars.submissionsRoot)) {
        ves = addError(ves, ValidationError.notSubmissionsCalendar);
        pars.cl.rollback();
        return ves;
      }
    } else if ((adPars.workflowRoot != null) &&
            colPath.startsWith(adPars.workflowRoot)) {
      adPars.awaitingApprovalEvent = pars.adding;
    }

    if (adPars.publishEvent) {
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
    }

    return ves;
  }

  @Override
  protected int update(final UpdatePars pars) throws Throwable {
    final AdminUpdatePars adPars = (AdminUpdatePars)pars;

    final BwEvent ev = pars.ev;

    final AdminClient adcl = (AdminClient)pars.cl;

    final BwAdminActionForm form = (BwAdminActionForm)pars.form;

    final var fwd = super.doUpdate(pars);
    if (fwd != forwardSuccess) {
      return fwd;
    }

    boolean clearForm = false;

    final String clearFormPref = adcl.getCalsuitePreferences().
            getClearFormsOnSubmit();

    if (clearFormPref == null) {
      clearForm = ((AdminConfig)form.getConfig())
              .getDefaultClearFormsOnSubmit();
    } else {
      clearForm = Boolean.parseBoolean(clearFormPref);
    }

    if (clearForm) {
      form.setLocation(null);
      form.setContact(null);
      form.resetSelectIds();
    }

    if ((adPars.publishEvent || adPars.updateSubmitEvent) &&
            pars.request.getBooleanReqPar("submitNotification", false)) {
      notifySubmitter(pars.request, pars.ei, adPars.submitterEmail);
    }

    if (adPars.approveEvent || adPars.awaitingApprovalEvent) {
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

      if (adPars.approveEvent) {
        sev = new EntityApprovalResponseEvent(
                SysEventBase.SysCode.APPROVAL_STATUS,
                adcl.getCurrentPrincipalHref(),
                ev.getCreatorHref(),
                ev.getHref(),
                null,
                true,
                null,
                csHref);
      } else {
        sev = new EntityApprovalNeededEvent(
                SysEventBase.SysCode.APPROVAL_NEEDED,
                adcl.getCurrentPrincipalHref(),
                ev.getCreatorHref(),
                ev.getHref(),
                null,
                null,
                csHref);
      }
      adcl.postNotification(sev);
    }

    if (!Util.isEmpty(adPars.suggestedTo)) {
      for (final SuggestedTo st: adPars.suggestedTo) {
        final BwAdminGroup grp =
                (BwAdminGroup)adcl.getAdminGroup(st.getGroupHref());

        if (grp == null) {
          warn("Unable to locate group " + st.getGroupHref());
          continue;
        }

        final EntitySuggestedEvent ese =
                new EntitySuggestedEvent(
                        SysEventBase.SysCode.SUGGESTED,
                        adcl.getCurrentPrincipalHref(),
                        ev.getCreatorHref(),
                        ev.getHref(),
                        null,
                        grp.getOwnerHref());
        adcl.postNotification(ese);
      }
    }

    /* See if we need to notify event registration system for add/update */
    final BwXproperty evregprop =
            ev.findXproperty(BwXproperty.bedeworkEventRegStart);

    if (evregprop != null) {
      // Registerable event
      notifyEventReg(pars.request, pars.ei);
    }

    resetEvent(pars.request, clearForm);

    return forwardSuccess;
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

  private List<SuggestedTo> doSuggested(final AdminUpdatePars pars) throws Throwable {
    final AdminClient cl = (AdminClient)pars.cl;

    if (!cl.getSystemProperties().getSuggestionEnabled()) {
      return null;
    }

    /* If so we adjust x-properties to match */

    List<SuggestedTo> suggestedTo = null;

    final ChangeTableEntry xcte =
            pars.changes.getEntry(PropertyInfoIndex.XPROP);

    final List<String> groupHrefs = pars.request.getReqPars("groupHref");

    final List<BwXproperty> alreadySuggested =
            pars.ev.getXproperties(BwXproperty.bedeworkSuggestedTo);

    final BwCalSuite cs = cl.getCalSuite();

    final String csHref = cs.getGroup().getPrincipalRef();
    if (groupHrefs == null) {
      if (!Util.isEmpty(alreadySuggested)) {
        for (final BwXproperty xp : alreadySuggested) {
          pars.ev.removeXproperty(xp);
          xcte.addRemovedValue(xp);
        }
      }

      return suggestedTo;
    }

    // Add each suggested group to the event and update preferred groups.

    final Set<String> hrefsPresent = new TreeSet<>();
    final Map<String, BwXproperty> toRemove =
            new HashMap<>(alreadySuggested.size());

    /* List those present and populate the toRemove map -
           we'll remove entries from toRemove as we process them later
         */
    for (final BwXproperty as: alreadySuggested) {
      final String href = new SuggestedTo(as.getValue()).getGroupHref();
      hrefsPresent.add(href);
      toRemove.put(href, as);
    }

    final Set<String> deDuped = new TreeSet<>(groupHrefs);

    for (final String groupHref: deDuped) {
      if (!hrefsPresent.contains(groupHref)) {
        final SuggestedTo sto =
                new SuggestedTo(SuggestedTo.pending, groupHref,
                                csHref);
        final BwXproperty grpXp =
                new BwXproperty(BwXproperty.bedeworkSuggestedTo,
                                null,
                                sto.toString());
        pars.ev.addXproperty(grpXp);
        xcte.addAddedValue(grpXp);
        if (suggestedTo == null) {
          suggestedTo = new ArrayList<>();
        }

        suggestedTo.add(sto);
      } else {
        toRemove.remove(groupHref);
      }

      // Add to preferred list
      cl.getPreferences().addPreferredGroup(groupHref);
    }

    /* Anything left in toRemove wasn't in the list. Remove
     * those entries
     */

    if (!Util.isEmpty(toRemove.values())) {
      for (final BwXproperty xp: toRemove.values()) {
        pars.ev.removeXproperty(xp);
        xcte.addRemovedValue(xp);
      }
    }

    return suggestedTo;
  }
}
