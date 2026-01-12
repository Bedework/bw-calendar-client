/* ********************************************************************
    Appropriate copyright notice
*/
package org.bedework.client.web.admin.event;

import org.bedework.base.response.GetEntityResponse;
import org.bedework.calfacade.BwCategory;
import org.bedework.calfacade.BwEvent.SuggestedTo;
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
import org.bedework.client.web.rw.event.UpdateEventAction;
import org.bedework.client.web.rw.event.UpdatePars;
import org.bedework.sysevents.events.SysEventBase;
import org.bedework.sysevents.events.publicAdmin.EntityApprovalNeededEvent;
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

import static org.bedework.base.response.Response.Status.ok;
import static org.bedework.client.web.rw.EventCommon.adminEventLocation;
import static org.bedework.client.web.rw.EventCommon.notifySubmitter;
import static org.bedework.client.web.rw.EventCommon.resetEvent;

/**
 * User: mike Date: 3/10/21 Time: 21:26
 */
public class AdminUpdateEventAction extends UpdateEventAction {
  @Override
  public String doAction(final BwRequest request,
                      final RWClient cl) {
    if (getRwForm().getEventInfo() == null) {
      // Session timed out and lost state?
      return forwardError;
    }

    final var pars = new AdminUpdatePars(request, cl);

    if (pars.publishEvent || pars.approveEvent) {
      // Cannot publish/approve while updating.
      cl.rollback();
      return forwardError;
    }

    final var fwd = super.doUpdate(request, cl, pars);

    pars.ev.setPublick(true);

    return fwd;
  }

  @Override
  protected boolean isOwner(final UpdatePars pars) {
    return pars.adding ||
            ((AdminClient)pars.cl).isCalSuiteEntity(pars.ev);
  }

  @Override
  protected boolean setLocation(final UpdatePars pars) {
    if (!adminEventLocation(pars.request, pars.ei)) {
      return false;
    }

    return true;
  }

  @Override
  protected GetEntityResponse<Set<BwCategory>> doAliases(final UpdatePars pars) {
    final var ger = new GetEntityResponse<Set<BwCategory>>();
    final AdminUpdatePars adPars = (AdminUpdatePars)pars;

    final RealiasResult resp = pars.cl.reAlias(pars.ev);
    if (resp.getStatus() != ok) {
      if (debug()) {
        debug("Failed to get topical areas? " + resp);
      }
      pars.cl.rollback();
      pars.request.error(ValidationError.missingTopic);
      return ger.error(ValidationError.missingTopic);
    }

    adPars.categories = resp.getCats(); // For validation later

    ger.setEntity(resp.getCats());
    return ger;
  }

  @Override
  protected boolean doAdditional(final UpdatePars pars) {
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
  protected String processXprops(final UpdatePars pars,
                                 final List<BwXproperty> extras) {
    final AdminUpdatePars adPars = (AdminUpdatePars)pars;

    if (adPars.updateSubmitEvent) {
      // We might need the submitters info */

      final List<BwXproperty> xps =
              adPars.ev.getXproperties(BwXproperty.bedeworkSubmitterEmail);

      if (!Util.isEmpty(xps)) {
        adPars.submitterEmail = xps.getFirst().getValue();
      }
    }

    return super.processXprops(pars, extras);
  }

  @Override
  protected String update(final UpdatePars pars) {
    final var adPars = (AdminUpdatePars)pars;
    final var ev = pars.ev;
    final var adcl = (AdminClient)pars.cl;
    final var form = (BwAdminActionForm)pars.request.getBwForm();

    final var fwd = super.doUpdate(pars);
    if (!forwardSuccess.equals(fwd)) {
      return fwd;
    }

    final boolean clearForm;

    final String clearFormPref = adcl.getCalsuitePreferences().
            getClearFormsOnSubmit();

    if (clearFormPref == null) {
      clearForm = ((AdminConfig)pars.request.getConfig())
              .getDefaultClearFormsOnSubmit();
    } else {
      clearForm = Boolean.parseBoolean(clearFormPref);
    }

    if (clearForm) {
      form.setLocation(null);
      form.setContact(null);
      form.resetSelectIds();
    }

    if (adPars.updateSubmitEvent &&
            pars.request.getBooleanReqPar("submitNotification", false)) {
      notifySubmitter(pars.request, pars.ei, adPars.submitterEmail);
    }

    if (adPars.awaitingApprovalEvent) {
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

      sev = new EntityApprovalNeededEvent(
              SysEventBase.SysCode.APPROVAL_NEEDED,
              adcl.getCurrentPrincipalHref(),
              ev.getCreatorHref(),
              ev.getHref(),
              null,
              null,
              csHref);

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
      // Registrable event
      EventregNotifier.notify(pars.request, pars.ei);
    }

    resetEvent(pars.request, clearForm);

    return forwardSuccess;
  }

  private List<SuggestedTo> doSuggested(final AdminUpdatePars pars) {
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
