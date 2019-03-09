/* ********************************************************************
    Licensed to Jasig under one or more contributor license
    agreements. See the NOTICE file distributed with this work
    for additional information regarding copyright ownership.
    Jasig licenses this file to you under the Apache License,
    Version 2.0 (the "License"); you may not use this file
    except in compliance with the License. You may obtain a
    copy of the License at:

    http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing,
    software distributed under the License is distributed on
    an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
    KIND, either express or implied. See the License for the
    specific language governing permissions and limitations
    under the License.
*/
package org.bedework.webcommon.event;

import org.bedework.access.Acl;
import org.bedework.appcommon.AccessXmlUtil;
import org.bedework.appcommon.AdminConfig;
import org.bedework.appcommon.ClientError;
import org.bedework.appcommon.ClientMessage;
import org.bedework.appcommon.TimeView;
import org.bedework.appcommon.client.Client;
import org.bedework.appcommon.client.IcalCallbackcb;
import org.bedework.calfacade.BwAttendee;
import org.bedework.calfacade.BwCategory;
import org.bedework.calfacade.BwContact;
import org.bedework.calfacade.BwDateTime;
import org.bedework.calfacade.BwEvent;
import org.bedework.calfacade.BwEvent.SuggestedTo;
import org.bedework.calfacade.BwEventObj;
import org.bedework.calfacade.BwLocation;
import org.bedework.calfacade.BwXproperty;
import org.bedework.calfacade.base.StartEndComponent;
import org.bedework.calfacade.exc.CalFacadeException;
import org.bedework.calfacade.exc.ValidationError;
import org.bedework.calfacade.svc.BwAdminGroup;
import org.bedework.calfacade.svc.BwCalSuite;
import org.bedework.calfacade.svc.EventInfo;
import org.bedework.calfacade.svc.EventInfo.UpdateResult;
import org.bedework.calfacade.util.CalFacadeUtil;
import org.bedework.calfacade.util.ChangeTable;
import org.bedework.calfacade.util.ChangeTableEntry;
import org.bedework.calsvci.EventsI.RealiasResult;
import org.bedework.icalendar.IcalTranslator;
import org.bedework.icalendar.Icalendar;
import org.bedework.sysevents.events.SysEventBase;
import org.bedework.sysevents.events.publicAdmin.EntityApprovalNeededEvent;
import org.bedework.sysevents.events.publicAdmin.EntityApprovalResponseEvent;
import org.bedework.sysevents.events.publicAdmin.EntitySuggestedEvent;
import org.bedework.util.calendar.IcalDefs;
import org.bedework.util.calendar.PropertyIndex.PropertyInfoIndex;
import org.bedework.util.misc.Util;
import org.bedework.util.timezones.DateTimeUtil;
import org.bedework.util.timezones.Timezones;
import org.bedework.webcommon.Attendees;
import org.bedework.webcommon.BwActionFormBase;
import org.bedework.webcommon.BwRequest;
import org.bedework.webcommon.BwWebUtil;
import org.bedework.webcommon.TimeDateComponents;

import net.fortuna.ical4j.model.Dur;
import net.fortuna.ical4j.model.Recur;
import org.apache.struts.upload.FormFile;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import static org.bedework.calfacade.responses.Response.Status.ok;

/** Action to add or modify an Event. The form has an addingEvent property to
 * distinguish.
 *
 * <p>Form should contain an initialized BwEvent object.
 *
 * <p>Optional request parameters:<ul>
 *      <li>  submitAndSend present to send scheduling request</li>
 *      <li>  saveState:   yes - to save state in form but suppress
 *                         validation and db update</li>
 *      <li>  subname:     Name of a subscription to an external calendar</li>
 *      <li>  calPath:     Path to a (writeable) calendar collection</li>
 *      <li>  schedule:    request or publish</li>
 *      <li>  category:    multi-valued categories to add to event</li>
 *      <li>  delcategory: multi-valued categories to remove from event</li>
 * </ul>
 *
 * <p>Forwards to:<ul>
 *      <li>"noAction"          input error or we want to ignore the request.</li>
 *      <li>forwardNoAccess     user not authorized.</li>
 *      <li>forwardRetry        invalid entity.</li>
 *      <li>"copy"              User wants to duplicate event.</li>
 *      <li>"delete"            User wants to delete event.</li>
 *      <li>"success"           added/updated ok.</li>
 * </ul>
 *
 * @author Mike Douglass
 */
public class UpdateEventAction extends EventActionBase {
  @Override
  public int doAction(final BwRequest request,
                      final BwActionFormBase form) throws Throwable {
    final Client cl = request.getClient();

    final boolean publicAdmin = cl.getPublicAdmin();
    final boolean submitApp = form.getSubmitApp();

    String submitterEmail = null;
    //String submitter = null;

    final boolean sendInvitations = request.present("submitAndSend");
    final boolean publishEvent = request.present("publishEvent");
    final boolean updateSubmitEvent = request.present(
            "updateSubmitEvent");
    final boolean approveEvent = request.present("approveEvent");

    // TODO - set this based on an x-prop or a request param
    boolean awaitingApprovalEvent = false;

    if ((publicAdmin && !form.getAuthorisedUser()) ||
        form.getGuest()) {
      cl.rollback();
      return forwardNoAccess;
    }

    if (approveEvent && !form.getCurUserApproverUser()) {
      cl.rollback();
      return forwardNoAccess;
    }

    if (request.present("access")) {
      // Fail this to stop someone screwing around with the access
      cl.rollback();
      return forwardNoAccess;
    }

    EventInfo ei = form.getEventInfo();
    BwEvent ev = ei.getEvent();
    if (ev == null) {
      return forwardNoAction;
    }

    final boolean eventOwner;

    if (!publicAdmin) {
      eventOwner = true;
    } else {
      eventOwner = form.getAddingEvent() || cl.isCalSuiteEntity(ev);
    }

    if ((publishEvent || approveEvent) && (ev.getRecurrenceId() != null)) {
      // Cannot publish/approve an instance - only the master
      cl.rollback();
      return forwardError;
    }

    /* This should be done by a wrapper */
    final ChangeTable changes = ei.getChangeset(
            cl.getCurrentPrincipalHref());

    /*
    BwEventAnnotation ann = null;
    if (ev instanceof BwEventProxy) {
      ann = ((BwEventProxy)ev).getRef();
      svci.reAttach(ann);
    } else {
      svci.reAttach(ev);
    }
    if (ev instanceof BwEventProxy) {
      final BwEventAnnotation ann = ((BwEventProxy)ev).getRef();
      cl.reAttach(ann.getTarget());
    }
    */

    if (request.hasCopy()) {
      request.getErr().emit("copy no longer supported - use fetch action");

      return forwardError;
    }

    /* ----------------------- Change attendee list ------------------------- */

    if (request.present("editEventAttendees")) {
      final int res = initMeeting(request, form, true);

      if (res != forwardSuccess) {
        return res;
      }

      return forwardEditEventAttendees;
    }

    /* -------------------- Turn event into meeting ------------------------- */

    if (request.present("makeEventIntoMeeting")) {
      final int res = initMeeting(request, form, true);

      if (res != forwardSuccess) {
        return res;
      }

      return forwardEventIntoMeeting;
    }

    if (request.present("initDates")) {
      ev.setDtstart(request.getDateTime("meetingStartdt",
                                        "meetingStartTzid"));
      ev.setDuration("P" + request.getReqPar("meetingDuration") + "M");
      ev.setDtend(ev.getDtstart().addDur(new Dur(ev.getDuration())));
      ev.setEndType(StartEndComponent.endTypeDuration);

      form.getEventDates().setFromEvent(ev);

      return forwardEventDatesInited;
    }

    final boolean adding = form.getAddingEvent();

    if (request.hasDelete()) {
      // Delete button in form
      form.assignMarkDeleted(false);
      return forwardDelete;
    }

    if (request.hasMarkDeleted()) {
      // Mark Deleted button in form
      form.assignMarkDeleted(true);
      return forwardDelete;
    }

    if (cl.isSuperUser() && ev.getDeleted()) {
      if (!request.present("deleted")) {
        changes.changed(PropertyInfoIndex.DELETED,
                        true,
                        false);
        ev.setDeleted(false);
      }
    }

    /* -------------------------- Collection ------------------------------ */

    /* TODO - If we are publishing this we should do it as a MOVE
              Allows the back end to remove the index entry from the old
              location. For the moment do it explicitly here
     */

    final String submissionsRoot = form.getConfig().getSubmissionRoot();
    final String workflowRoot = cl.getSystemProperties().getWorkflowRoot();
    String unindexLocation = null;

    /*
    if (publishEvent | approveEvent) {
      // Will need to unindex from old location
//      cl.unindex(ei.getEvent().getHref());
      unindexLocation = ev.getHref();
    } else {
      unindexLocation = null;
    }
    */
    if (!adding) {
      unindexLocation = ev.getHref();
    }

    /* We have a problem with roll back in the case of errors.
     * Hibernate will actually update the event as we change fields and
     * really we should do a roll back on any failure.
     *
     * However, this causes a problem with the UI. All this should be
     * resolved with the newer client approach which doesn't update
     * this way. For the moment preserve some important value(s).
     */

    final String preserveColPath = ev.getColPath();

    /* ------------------------ Text fields ------------------------------ */

    setEventText(request, ev, adding, changes);

    /* ---------------------- Uploaded image ----------------------------- */

    final List<BwXproperty> extras = new ArrayList<>();
    final FormFile ff = form.getEventImageUpload();

    if ((ff != null) && (ff.getFileSize() > 0)) {
      final ProcessedImage pi = processImage(request, ff);

      if (!pi.OK) {
        if (!pi.retry) {
          cl.rollback();
          return forwardValidationError;
        }

        restore(ev, preserveColPath);
        return forwardRetry;
      }

      BwXproperty imageXp = new BwXproperty();
      imageXp.setName(BwXproperty.bedeworkImage);
      imageXp.setValue(
              pi.image.getColPath() + "/" + pi.image.getName());
      extras.add(imageXp);

      imageXp = new BwXproperty();
      imageXp.setName(BwXproperty.bedeworkThumbImage);
      imageXp.setValue(pi.thumbnail.getColPath() + "/" + pi.thumbnail
              .getName());
      extras.add(imageXp);
    }

    /* ----------------------- X-properties ------------------------------ */

    int res = processXprops(request, ev, extras,
                            publishEvent,
                            changes);
    if (res == forwardValidationError) {
      cl.rollback();
      return res;
    }

    if ((publishEvent || updateSubmitEvent)) {
      // We might need the submitters info */

      final List<BwXproperty> xps =
              ev.getXproperties(BwXproperty.bedeworkSubmitterEmail);

      if (!Util.isEmpty(xps)) {
        submitterEmail = xps.get(0).getValue();
      }
    }

    /* ---------------- Suggested to a group? --------------------- */

    List<SuggestedTo> suggestedTo = null;

    if (eventOwner) {
      suggestedTo = doSuggested(request, ev, changes);
    }

    /* -------------------------- Dates ------------------------------ */

    res = form.getEventDates().updateEvent(ei);
    if (res == forwardValidationError) {
      cl.rollback();
      return res;
    }

    if (!BwWebUtil.validateEventDates(request, ei)) {
      restore(ev, preserveColPath);
      return forwardRetry;
    }

    /* -------------------------- CalSuite ------------------------------ */
    
    ev.setCalSuite(form.getCalSuiteName());

    /* -------------------------- Location ------------------------------ */

    if (publicAdmin) {
      if (!adminEventLocation(request, ei)) {
        restore(ev, preserveColPath);
        return forwardRetry;
      }
    } else {
      setEventLocation(request, ei, form, submitApp);
        // RFC says maybe for this.
        //incSequence = true;
    }

    /* -------------------------- Contact ------------------------------ */


    try {
      if (!setEventContact(request, submitApp)) {
        restore(ev, preserveColPath);
        return forwardRetry;
      }
    } catch (final CalFacadeException cfe) {
      cl.rollback();
      request.getErr().emit(ClientError.badRequest, cfe.getExtra());
      return forwardRetry;
    }

    /* -------------------------- Aliases ------------------------------ */

    Set<BwCategory> cats = null;
    if (cl.getPublicAdmin() ||
            request.getBwForm().getSubmitApp()) {
        final RealiasResult resp = cl.reAlias(ev);
      if (resp.getStatus() != ok) {
        if (debug()) {
          debug("Failed to get topical areas? " + resp);
        }
        cl.rollback();
        form.getErr().emit(ValidationError.missingTopic);
        restore(ev, preserveColPath);
        return forwardRetry;
      }

      cats = resp.getCats();

      if (publicAdmin && !updateSubmitEvent && Util.isEmpty(cats)) {
        if (debug()) {
          debug("No topical areas? " + resp);
        }
        form.getErr().emit(ValidationError.missingTopic);
        restore(ev, preserveColPath);
        return forwardRetry;
      }
    }

    /* -------------------------- Categories ------------------------------ */

    final SetEntityCategoriesResult secr = 
            setEntityCategories(request, 
                                cats,
                                ev, changes);
    if (secr.rcode != forwardSuccess) {
      cl.rollback();
      return secr.rcode;
    }

    /* ------------------------- Link ---------------------------- */

    final String link = Util.checkNull(form.getEventLink());
    if ((link != null) && (Util.validURI(link) == null)) {
      form.getErr().emit(ValidationError.invalidUri);
      restore(ev, preserveColPath);
      return forwardRetry;
    }

    if (!Util.equalsString(ev.getLink(), link)) {
      changes.changed(PropertyInfoIndex.URL, ev.getLink(), link);
      ev.setLink(link);
    }

    /* ------------------------- Cost ---------------------------- */

    final String cost = Util.checkNull(form.getEventCost());
    if (!Util.equalsString(ev.getCost(), cost)) {
      changes.changed(PropertyInfoIndex.COST, ev.getCost(), cost);
      ev.setCost(cost);
    }

    /* ------------------------- Transparency ---------------------------- */

    final String transp = Util.checkNull(form.getTransparency());
    if (!Util.equalsString(ev.getTransparency(), transp)) {
      changes.changed(PropertyInfoIndex.TRANSP, ev.getTransparency(),
                      transp);
      ev.setTransparency(transp);
    }

    /* ------------------------- publick ---------------------------- */

    ev.setPublick(publicAdmin);

    /* ------------------------- Status ---------------------------- */

    /* Set the status for the event, confirmed, tentative or canceled
     */
    final String fStatus = form.getEventStatus();

    if (!Util.equalsString(fStatus, ev.getStatus())) {
      /* Changing the status */
//      if (BwEvent.statusCancelled.equals(fStatus)) {
      //      canceling = true;
      //  }

      changes.changed(PropertyInfoIndex.STATUS, ev.getStatus(),
                      fStatus);
      ev.setStatus(fStatus);
    }

    /* ------------------------- Scheduling ---------------------------- */

    /* -------------------------- Attendees ---------------------------- */

    final Attendees atts = form.getAttendees();

    ChangeTableEntry cte = changes.getEntry(PropertyInfoIndex.ATTENDEE);

    if (adding) {
      ev.setAttendees(atts.getAttendees());
    }

    if (atts.getAddedAttendees() != null) {
      for (final BwAttendee att: atts.getAddedAttendees()) {
        cte.addAddedValue(att);
      }
    }

    if (atts.getDeletedAttendees() != null) {
      for (final BwAttendee att: atts.getDeletedAttendees()) {
        cte.addRemovedValue(att);
      }
    }

    if (request.getErrFlag()) {
      restore(ev, preserveColPath);
      return forwardRetry;
    }

    /* -------------------------- Recurrences ------------------------------ */
    if (request.getBooleanReqPar("recurring", false)) {
      /* recurring turned on - if "freq" is null or "NONE" we get a null rrule
       * which mean leave the recurrences alone.
       */
      final String rrule = getRrule(request, form);
      Collection<String> oldRrules = null;

      if (form.getErrorsEmitted()) {
        // Unrecoverable? Error in form.
        cl.rollback();
        return forwardValidationError;
      }

      if (rrule != null) {
        final Collection<String> rrules = ev.getRrules();

        final boolean rruleChanged;

        if (rrules == null) {
          final Set<String> newRrules = new TreeSet<>();
          newRrules.add(rrule);
          ev.setRrules(newRrules);
          rruleChanged = true;
        } else {
          if (rrules.size() == 0) {
            rruleChanged = true;
          } else if (rrules.size() > 1) {
            oldRrules = new ArrayList<>(rrules);
            rrules.clear();
            rruleChanged = true;
          } else {
            final String oldRrule = rrules.iterator().next();
            rruleChanged = !rrule.equals(oldRrule);

            if (rruleChanged) {
              oldRrules = new ArrayList<>(rrules);
              rrules.clear();
            }
          }
        }

        if (rruleChanged) {
          ev.addRrule(rrule);

          cte = changes.getEntry(PropertyInfoIndex.RRULE);
          cte.setChanged(oldRrules, ev.getRrules());
          cte.setRemovedValues(oldRrules);
          cte.setAddedValues(new TreeSet<>(ev.getRrules()));
        }
      }

      ev.setRecurring(ev.isRecurringEntity());
    } else if (ev.getRecurring() == null) {
      // Adding non-recurring
      ev.setRecurring(false);
    } else if (ev.testRecurring()) {
      // Turned recurring off
      /* Clear out any recurrence info */

      final Collection<String> rrules = ev.getRrules();

      if (!Util.isEmpty(rrules)) {
        cte = changes.getEntry(PropertyInfoIndex.RRULE);
        cte.setChanged(rrules, null);
        cte.setRemovedValues(new ArrayList<>(rrules));

        rrules.clear();
      }

      // Rdates and exdates should get handled below
      //ev.setRecurring(false);
    }

    /* ------------------------ Rdates and exdates -------------------------- */

    boolean evDateOnly = false;
    if (!form.getAddingEvent()) {
      evDateOnly = ev.getDtstart().getDateType();
    } else if (!request.present("initDates")) {
      evDateOnly = form.getEventDates().getStartDate().getDateOnly();
    }

    updateRExdates(request, ev, evDateOnly, changes);

    /* ------------------ final validation -------------------------- */

    /* If we're updating but not publishing a submitted event, treat it as
     * if it were the submit app.
     */
    final List<ValidationError> ves =
            BwWebUtil.validateEvent(cl,
                                    updateSubmitEvent || submitApp,
                                    publicAdmin,
                                    ev);

    if (ves != null) {
      for (final ValidationError ve: ves) {
        form.getErr().emit(ve.getErrorCode(), ve.getExtra());
      }
      restore(ev, preserveColPath);
      return forwardRetry;
    }

    /* ------------- web submit - copy entities and change owner ------------ */

    if (!request.setEventCalendar(ei, changes)) {
      return forwardRetry;
    }

    final String colPath = ev.getColPath();

    if (publishEvent) {
      /* Event MUST NOT be in a submission calendar */
      if (colPath.startsWith(submissionsRoot)) {
        form.getErr().emit(ValidationError.inSubmissionsCalendar);
        cl.rollback();
        return forwardValidationError;
      }
    } else if (approveEvent) {
      /* Event MUST NOT be in a workflow calendar */
      if (colPath.startsWith(workflowRoot)) {
        form.getErr().emit(ValidationError.inSubmissionsCalendar);
        restore(ev, preserveColPath);
        cl.rollback();
        return forwardValidationError;
      }

      // See if colpath changed and if so change the overrides
      if (ev.getRecurring() &&
              !preserveColPath.equals(colPath) &&
              (ei.getOverrideProxies() != null)) {
        for (final BwEvent oev: ei.getOverrideProxies()) {
          oev.setColPath(ev.getColPath());
        }
      }

    } else if (updateSubmitEvent) {
      /* Event MUST be in a submission calendar */
      if (!colPath.startsWith(submissionsRoot)) {
        form.getErr().emit(ValidationError.notSubmissionsCalendar);
        cl.rollback();
        return forwardValidationError;
      }
    } else if ((workflowRoot != null) && colPath.startsWith(workflowRoot)) {
      awaitingApprovalEvent = adding;
    }

    if (publishEvent) {
      copyEntities(ev);
      changeOwner(ev, cl);
      changes.changed(PropertyInfoIndex.CREATOR, null,
                      ev.getCreatorHref());

      // Do the same for any overrides

      if (ev.getRecurring() &&
              (ei.getOverrideProxies() != null)) {
        for (final BwEvent oev: ei.getOverrideProxies()) {
          copyEntities(oev);
          changeOwner(oev, cl);
          oev.setColPath(ev.getColPath());
        }
      }
    }

    /* --------------------- Must have a calendar ------------------ */

    if (ev.getColPath() == null) {
      form.getErr().emit(ValidationError.missingCalendar);
      cl.rollback();
      return forwardValidationError;
    }

    /* --------------------- Add or update the event ------------------------ */

    if (debug()) {
      debug(changes.toString());
    }

    boolean clearForm = false;

    try {
      final UpdateResult ur;
      ei.setNewEvent(adding);

      if (adding) {
        ur = cl.addEvent(ei,
                         !sendInvitations,
                         true);
      } else {
        ur = cl.updateEvent(ei, !sendInvitations, null);
      }

      if ((ur != null) && (ur.schedulingResult != null)) {
        emitScheduleStatus(form, ur.schedulingResult, false);
      }

      form.assignAddingEvent(false);

      if (cl.getPublicAdmin()) {
        final String clearFormPref = cl.getCalsuitePreferences().
                getClearFormsOnSubmit();

        if (clearFormPref == null) {
          clearForm = ((AdminConfig)form.getConfig())
                  .getDefaultClearFormsOnSubmit();
        } else {
          clearForm = Boolean.valueOf(clearFormPref);
        }

        if (clearForm) {
          form.setLocation(null);
          form.setContact(null);
          form.resetSelectIds();
        }
      }

      /* -------------------------- Access ------------------------------ */

      final String aclStr = request.getReqPar("acl");
      if (aclStr != null) {
        final Acl acl = new AccessXmlUtil(null, cl).getAcl(aclStr, true);

        cl.changeAccess(ev, acl.getAces(), true);
      }

      if (!adding && !unindexLocation.equals(ev.getHref())) {
        cl.unindex(unindexLocation);
      }
    } catch (final CalFacadeException cfe) {
      cl.rollback();

      if (CalFacadeException.noRecurrenceInstances.equals(cfe.getMessage())) {
        form.getErr().emit(ClientError.noRecurrenceInstances,
                           ev.getUid());
        return forwardValidationError;
      }

      if (CalFacadeException.duplicateGuid.equals(cfe.getMessage())) {
        form.getErr().emit(ClientError.duplicateUid);
        return forwardDuplicate;
      }

      throw cfe;
    }

    if ((publishEvent || updateSubmitEvent) &&
        request.getBooleanReqPar("submitNotification", false)) {
      notifySubmitter(request, ei, submitterEmail);
    }

    if (approveEvent || awaitingApprovalEvent) {
      /* Post an event flagging the approval.
         The change notification processor will add the
         notification(s).
        */

      final BwCalSuite cs = cl.getCalSuite();
      final String csHref;

      if (cs != null) {
        csHref = cs.getGroup().getOwnerHref();
      } else {
        csHref = null;
      }

      final SysEventBase sev;

      if (approveEvent) {
        sev = new EntityApprovalResponseEvent(
                SysEventBase.SysCode.APPROVAL_STATUS,
                cl.getCurrentPrincipalHref(),
                ev.getCreatorHref(),
                ev.getHref(),
                null,
                true,
                null,
                csHref);
      } else {
        sev = new EntityApprovalNeededEvent(
                SysEventBase.SysCode.APPROVAL_NEEDED,
                cl.getCurrentPrincipalHref(),
                ev.getCreatorHref(),
                ev.getHref(),
                null,
                null,
                csHref);
      }
      cl.postNotification(sev);
    }

    if (!Util.isEmpty(suggestedTo)) {
      for (final SuggestedTo st: suggestedTo) {
        final BwAdminGroup grp =
                (BwAdminGroup)cl.getAdminGroup(st.getGroupHref());

        if (grp == null) {
          warn("Unable to locate group " + st.getGroupHref());
          continue;
        }

        final EntitySuggestedEvent ese =
                new EntitySuggestedEvent(
                        SysEventBase.SysCode.SUGGESTED,
                        cl.getCurrentPrincipalHref(),
                        ev.getCreatorHref(),
                        ev.getHref(),
                        null,
                        grp.getOwnerHref());
        cl.postNotification(ese);
      }
    }

    if (publicAdmin) {
      /* See if we need to notify event registration system for add/update */
      final BwXproperty evregprop =
              ev.findXproperty(BwXproperty.bedeworkEventRegStart);

      if (evregprop != null) {
        // Registerable event
        notifyEventReg(request, ei);
      }

      resetEvent(request, clearForm);
    } else {
      request.refresh();
    }

    if (adding) {
      ev = new BwEventObj();
      form.getEventDates().setNewEvent(ev);

      final TimeView tv = request.getSess().getCurTimeView(request);

      form.getEventStartDate().setDateTime(tv.getCurDayFmt().getDateTimeString());
      form.getEventEndDate().setDateTime(tv.getCurDayFmt().getDateTimeString());
      ei = new EventInfo(ev);

      form.setEventInfo(ei, true);

      if (ev.getEntityType() == IcalDefs.entityTypeTodo) {
        form.getMsg().emit(ClientMessage.addedTasks, 1);
      } else {
        form.getMsg().emit(ClientMessage.addedEvents, 1);
      }
    } else  if (ev.getEntityType() == IcalDefs.entityTypeTodo) {
      form.getMsg().emit(ClientMessage.updatedTask);
    } else {
      form.getMsg().emit(ClientMessage.updatedEvent);
    }

    cl.clearSearchEntries();

    return forwardSuccess;
  }

  private void restore(final BwEvent ev,
                       final String preserveColPath) throws Throwable {
    if (preserveColPath == null) {
      return;
    }

    if (!preserveColPath.equals(ev.getColPath())) {
      ev.setColPath(preserveColPath);
    }
  }

  private List<SuggestedTo> doSuggested(final BwRequest request,
                                        final BwEvent ev,
                                        final ChangeTable changes) throws Throwable {
    final Client cl = request.getClient();

    if (!cl.getPublicAdmin() ||
            !cl.getSystemProperties().getSuggestionEnabled()) {
      return null;
    }

    /* If so we adjust x-properties to match */

    List<SuggestedTo> suggestedTo = null;

    final ChangeTableEntry xcte =
            changes.getEntry(PropertyInfoIndex.XPROP);

    final List<String> groupHrefs = request.getReqPars("groupHref");

    final List<BwXproperty> alreadySuggested =
            ev.getXproperties(BwXproperty.bedeworkSuggestedTo);

    final BwCalSuite cs = cl.getCalSuite();

    final String csHref = cs.getGroup().getPrincipalRef();
    if (groupHrefs == null) {
      if (!Util.isEmpty(alreadySuggested)) {
        for (final BwXproperty xp : alreadySuggested) {
          ev.removeXproperty(xp);
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
        ev.addXproperty(grpXp);
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
        ev.removeXproperty(xp);
        xcte.addRemovedValue(xp);
      }
    }

    return suggestedTo;
  }

  private void changeOwner(final BwEvent ev,
                           final Client cl) throws CalFacadeException {
    cl.claimEvent(ev);
    ev.setCreatorHref(cl.getCurrentPrincipalHref());
  }

  private void copyEntities(final BwEvent ev) throws CalFacadeException {
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

  /** Update rdates and exdates.
   *
   * @param request object
   * @param event to be updated
   * @param evDateOnly true for date only
   * @param changes to record changes
   * @return boolean  true if changed.
   * @throws Throwable on fatal error
   */
  private boolean updateRExdates(final BwRequest request,
                                 final BwEvent event,
                                 final boolean evDateOnly,
                                 final ChangeTable changes) throws Throwable {
    Collection<BwDateTime> reqDates = request.getRdates(evDateOnly);
    Collection<BwDateTime> evDates = new TreeSet<>();
    if (event.getRdates() != null) {
      evDates.addAll(event.getRdates());
    }

    Collection<BwDateTime> added = null;
    Collection<BwDateTime> removed = null;

    boolean rdChanged = false;

    if (reqDates.isEmpty()) {
      if (!Util.isEmpty(evDates)) {
        removed = evDates;
        event.getRdates().clear();
        rdChanged = true;
      }
    } else if (Util.isEmpty(evDates)) {
      for (BwDateTime dt: reqDates) {
        event.addRdate(dt);
      }

      added = reqDates;
      event.setRecurring(true);
      rdChanged = true;
    } else {
      added = new TreeSet<>();
      removed = new TreeSet<>();
      rdChanged = CalFacadeUtil.updateCollection(false, reqDates,
                                                 event.getRdates(),
                                                 added, removed);
    }

    if (rdChanged) {
      ChangeTableEntry cte = changes.getEntry(PropertyInfoIndex.RDATE);
      cte.setChanged(evDates, event.getRdates());
      cte.setRemovedValues(removed);
      cte.setAddedValues(added);
    }

    boolean exdChanged = false;

    evDates.clear();
    if (event.getExdates() != null) {
      evDates.addAll(event.getExdates());
    }
    added = null;
    removed = null;

    reqDates = request.getExdates(evDateOnly);

    if (Util.isEmpty(reqDates)) {
      if (!Util.isEmpty(evDates)) {
        removed = evDates;
        event.getExdates().clear();
        exdChanged = true;
      }
    } else if (Util.isEmpty(evDates)) {
      for (BwDateTime dt: reqDates) {
        event.addExdate(dt);
      }

      added = reqDates;
      exdChanged = true;
    } else {
      added = new TreeSet<>();
      removed = new TreeSet<>();
      exdChanged = CalFacadeUtil.updateCollection(false, reqDates,
                                                  event.getExdates(),
                                                  added, removed);
    }

    if (exdChanged) {
      ChangeTableEntry cte = changes.getEntry(PropertyInfoIndex.EXDATE);
      cte.setChanged(evDates, event.getExdates());
      cte.setRemovedValues(removed);
      cte.setAddedValues(added);
    }

    return rdChanged || exdChanged;
  }

  private static final String alwaysRemove1 =
      "BEGIN:VEVENT\r\nUID:123456\r\nDTSTART;TYPE=DATE:20080212T000000\r\n";

  private static final String alwaysRemove2 =
      "BEGIN:VEVENT\nUID:123456\nDTSTART;TYPE=DATE:20080212T000000\n";

  /* return forwardNoAction for no change
   * forward success for change otherwise error.
   */
  private int processXprops(final BwRequest request,
                            final BwEvent event,
                            final List<BwXproperty> extras,
                            final boolean publishEvent,
                            final ChangeTable changes) throws Throwable {
    final ChangeTableEntry cte = changes.getEntry(PropertyInfoIndex.XPROP);
    final List<String> unparsedxps = request.getReqPars("xproperty");

    List<BwXproperty> added = null;
    List<BwXproperty> removed = null;

    List<BwXproperty> xprops = null;

    final List<BwXproperty> evxprops = new ArrayList<>();

    if (!Util.isEmpty(event.getXproperties())) {
      evxprops.addAll(event.getXproperties());
    }

    /* When the xproperties get emitted we don't emit the set marked for skipping
     * We have to preserve those now.
     */

    if (unparsedxps != null) {
      xprops = parseXprops(request, /*publishEvent*/false);
    }

    if (!Util.isEmpty(extras)) {
      if (xprops == null) {
        xprops = extras;
      } else {
        xprops.addAll(extras);
      }
    }

    if (Util.isEmpty(xprops)) {
      /* No xprops in the request. Remove everything except certain
         special values.
         We also need to add in anything in extras
       */
      if (Util.isEmpty(evxprops)) {
        return forwardNoAction;
      }

      for (final BwXproperty xp:  evxprops) {
        if (xp.getSkipJsp()) {
          if (!xp.getName().equals(BwXproperty.bedeworkIcal) ||
              (xp.getValue() == null)) {
            continue;
          }

          if (!xp.getValue().startsWith(alwaysRemove1) &&
              !xp.getValue().startsWith(alwaysRemove2)) {
            continue;
          }
        }

        // Remove this one
        if (removed == null) {
          removed = new ArrayList<>();
        }

        removed.add(xp);
      }

      if (removed != null) {
        cte.setChanged(evxprops, event.getXproperties());
        cte.setRemovedValues(removed);
      }

      return forwardSuccess;
    }

    if (Util.isEmpty(evxprops)) {
      if (event.getXproperties() == null) {
        event.setXproperties(new ArrayList<>(xprops));
      } else {
        event.getXproperties().addAll(xprops);
      }

      cte.setChanged(evxprops, new ArrayList<>(event.getXproperties()));
      cte.setAddedValues(xprops);

      if (removed != null) {
        cte.setRemovedValues(removed);
      }

      return forwardSuccess;
    }

    // Add the skipped set to the 'new' set

    for (final BwXproperty xp:  evxprops) {
      if (xp.getSkipJsp()) {
        if (xp.getName().equals(BwXproperty.bedeworkIcal)) {
          if ((xp.getValue() != null) &&
              (xp.getValue().startsWith(alwaysRemove1) ||
               xp.getValue().startsWith(alwaysRemove2))) {
            continue; // Bogus x-prop - remove it
          }
        }

        xprops.add(xp);
      }
    }

    added = new ArrayList<>();
    if (removed == null) {
      removed = new ArrayList<>();
    }

    if (CalFacadeUtil.updateCollection(true, // clone them
                                       xprops, // make it look like this
                                       event.getXproperties(), // change this (to)
                                       added, removed)) {
      cte.setChanged(evxprops, new ArrayList<>(event.getXproperties()));
      cte.setAddedValues(added);
      cte.setRemovedValues(removed);

      return forwardSuccess;
    }

    return forwardNoAction;
  }

  /* This is a bit bogus but it will get us going. Make up a fake calendar,
   * parse it with ical4j and then extract the xproperties.
   *
   * We'll be rebuilding all of this anyway.
   */
  private List<BwXproperty> parseXprops(final BwRequest request,
                                        final boolean publishEvent) throws Throwable {
    Collection<String> unparsedxps = request.getReqPars("xproperty");

    if (unparsedxps == null) {
      return null;
    }

    StringBuilder sb = new StringBuilder();
    sb.append("BEGIN:VCALENDAR\n" +
              "VERSION:2.0\n" +
              "PRODID:-//xyz.com//EN\n" +
              "BEGIN:VEVENT\n" +
              "UID:123456\n" +
              "DTSTART;TYPE=DATE:20080212\n");

    for (String unparsedxp: unparsedxps) {
      /* Probably not the most efficient way */
      String val = unparsedxp.replace("\r\n", "\\n");
      val = val.replace("\n", "\\n");
      val = val.replace("\r", "\\n");

      sb.append(val);
      sb.append("\n");
    }

    sb.append("END:VEVENT\n" +
              "END:VCALENDAR\n");

    Client cl = request.getClient();
    IcalTranslator trans = new IcalTranslator(new IcalCallbackcb(cl));
    Icalendar c = trans.fromIcal(null, new StringReader(sb.toString()));

    BwEvent ev = c.getEventInfo().getEvent();

    List<BwXproperty> xprops = ev.getXproperties();

    List<BwXproperty> strippedXprops = new ArrayList<>();

    if (!publishEvent) {
      for (BwXproperty xp: xprops) {
        if (xp.getSkipJsp()) {
          // Should not be here
          continue;
        }

        if (xp.getName().equals(BwXproperty.bedeworkSuggestedTo)) {
          // We output it but don't want it back
          continue;
        }
        strippedXprops.add(xp);
      }
      return strippedXprops;
    }

    /* Some x-properties added by the submission client contain private
     * information. We will remove all except a known set. Any that need to be
     * retained should be added to the set.
     *
     * The request parameter xprop-preserve names a property to save.
     */

    Map<String, String> plistMap = new HashMap<>();
    for (String plistName: request.getReqPars("xprop-preserve")) {
      plistMap.put(plistName, plistName);
    }

    for (BwXproperty xp: xprops) {
      if (plistMap.get(xp.getName()) != null) {
        strippedXprops.add(xp);
      }
    }

    return strippedXprops;
  }

  private static HashMap<String, String> validFreq = new HashMap<>();
  static {
    /* Block use of over-frequent recurrences */
    validFreq.put("HOURLY", "HOURLY");
    validFreq.put("DAILY", "DAILY");
    validFreq.put("WEEKLY", "WEEKLY");
    validFreq.put("MONTHLY", "MONTHLY");
    validFreq.put("YEARLY", "YEARLY");
  }

  /** Create a recurrence rule from the incoming request.
   *
   * <p>Incoming parameters are the following:</br>
   * <em>freq</em>HOURLY, DAILY, WEEKLY, MONTHLY, YEARLY</br>
   * <em>interval</em>Optional, default 1</br>
   * <em>count</em>Optional but only count OR until</br>
   * <em>until</em>Optional date, yyyyMMdd or UTC date/time yyyyMMddThhmmssZ</br>
   *
   * <p>If freq=WEEKLY, MONTHLY or YEARLY the following can be used</br>
   * <em>weekdays</em>Any value, same as byday=MO,TU,WE,TH,FR</br>
   * <em>weekends</em>Any value, same as byday=SA,SU</br>
   * <em>byday</em>List of days from SA,SU,MO,TU,WE,TH,FR</br>
   *
   * <p>If freq=MONTHLY or YEARLY the following can be used</br>
   * <em>bymonthday</em>Comma separated list of integer -31...31</br>
   *
   * <p>If freq=YEARLY the following can be used</br>
   * <em>bymonth</em>Comma separated list of integer 1...12</br>
   * <em>byyearday</em>Comma separated list of integer -366...366</br>
   *
   * <p>e.g. Every Friday the 13th,</br>
   *  RRULE:FREQ=MONTHLY;BYDAY=FR;BYMONTHDAY=13
   *
   * @param request object
   * @param form object
   * @return String or null
   * @throws Throwable on fatal error
   */
  public String getRrule(final BwRequest request,
                         final BwActionFormBase form) throws Throwable {
    StringBuilder rrule = new StringBuilder();
    String freq = request.getReqPar("freq");
    if ((freq == null) || ("NONE".equals(freq)) ){
      return null;
    }

    if (validFreq.get(freq) == null) {
      form.getErr().emit("org.bedework.bad.request", "freq");
      return null;
    }

    rrule.append("FREQ=");
    rrule.append(freq);

    Integer interval = request.getIntReqPar("interval",
                                            ValidationError.invalidRecurInterval);
    if (interval != null) {
      rrule.append(";INTERVAL=");
      rrule.append(interval);
    }

    Integer count = request.getIntReqPar("count",
                                         ValidationError.invalidRecurCount);
    String until = request.getReqPar("until");

    if ((count != null) && (until != null)) {
      form.getErr().emit(ValidationError.invalidRecurCountAndUntil);
      return null;
    }

    if (until != null) {
      // RECUR This needs fixing when we get the widgets sorted out.
      /* For the time being, if the start is a date/time and we get date only for
       * until, add the time part of the start.
       *
       *  Also use the start timezone for until.
       */
      TimeDateComponents start = form.getEventDates().getStartDate();

      if (DateTimeUtil.isISODate(until)) {
        if (!start.getDateOnly()) {
          // Append start time.
          until += "T" + start.getDateTime().getDtval().substring(8);
        }
      }
      if (DateTimeUtil.isISODateTime(until)) {
        // Floating - convert to UTC using start timezone.
        until = Timezones.getUtc(until, start.getTzid());
      } else if (!DateTimeUtil.isISODateTimeUTC(until)) {
        form.getErr().emit(ValidationError.invalidRecurUntil, "until");
        return null;
      }

      rrule.append(";UNTIL=");
      rrule.append(until);
    }

    if (count != null) {
      if (count < 0) {
        form.getErr().emit(ValidationError.invalidRecurCount, count);
        return null;
      }
      rrule.append(";COUNT=");

      rrule.append(count);
    }

    if (!"DAILY".equals(freq)) {
      String byday = request.getReqPar("byday");

      if (byday != null) {
        rrule.append(";BYDAY=");
        rrule.append(byday);
      }

      if (!"WEEKLY".equals(freq)) {
        String bymonthday = request.getReqPar("bymonthday");
        if (bymonthday != null) {
          rrule.append(";BYMONTHDAY=");
          rrule.append(bymonthday);
        }

        if (!"MONTHLY".equals(freq)) {
          String bymonth = request.getReqPar("bymonth");
          if (bymonth != null) {
            rrule.append(";BYMONTH=");
            rrule.append(bymonth);
          }

          String byyearday = request.getReqPar("byyearday");
          if (byyearday != null) {
            rrule.append(";BYYEARDAY=");
            rrule.append(byyearday);
          }
        }
      }
    }

    String rruleStr = rrule.toString();

    try {
      new Recur(rruleStr);
    } catch (Throwable t) {
      form.getErr().emit(ValidationError.invalidRecurRule);
      return null;
    }

    if (debug()) {
      debug(rruleStr);
    }

    return rruleStr;
  }
}
