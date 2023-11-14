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
package org.bedework.client.web.rw.event;

import org.bedework.access.Acl;
import org.bedework.appcommon.AccessXmlUtil;
import org.bedework.appcommon.ClientError;
import org.bedework.appcommon.ClientMessage;
import org.bedework.appcommon.ImageProcessing;
import org.bedework.appcommon.Images;
import org.bedework.appcommon.TimeView;
import org.bedework.appcommon.client.Client;
import org.bedework.appcommon.client.IcalCallbackcb;
import org.bedework.calfacade.BwAttendee;
import org.bedework.calfacade.BwCalendar;
import org.bedework.calfacade.BwCategory;
import org.bedework.calfacade.BwDateTime;
import org.bedework.calfacade.BwEvent;
import org.bedework.calfacade.BwEventObj;
import org.bedework.calfacade.BwResource;
import org.bedework.calfacade.BwXproperty;
import org.bedework.calfacade.base.StartEndComponent;
import org.bedework.calfacade.exc.CalFacadeException;
import org.bedework.calfacade.exc.ValidationError;
import org.bedework.calfacade.svc.EventInfo;
import org.bedework.calfacade.svc.EventInfo.UpdateResult;
import org.bedework.calfacade.svc.RealiasResult;
import org.bedework.calfacade.util.CalFacadeUtil;
import org.bedework.calfacade.util.ChangeTableEntry;
import org.bedework.calsvci.EventsI;
import org.bedework.client.rw.RWClient;
import org.bedework.client.web.rw.Attendees;
import org.bedework.client.web.rw.BwRWActionForm;
import org.bedework.client.web.rw.RWActionBase;
import org.bedework.convert.IcalTranslator;
import org.bedework.convert.Icalendar;
import org.bedework.util.calendar.IcalDefs;
import org.bedework.util.calendar.PropertyIndex.PropertyInfoIndex;
import org.bedework.util.misc.Util;
import org.bedework.util.misc.response.GetEntityResponse;
import org.bedework.util.misc.response.Response;
import org.bedework.util.timezones.DateTimeUtil;
import org.bedework.util.timezones.Timezones;
import org.bedework.util.webaction.UploadFileInfo;
import org.bedework.webcommon.BwActionFormBase;
import org.bedework.webcommon.BwRequest;
import org.bedework.webcommon.DurationBean;
import org.bedework.webcommon.TimeDateComponents;

import net.fortuna.ical4j.model.Recur;

import java.io.StringReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import static org.bedework.client.web.rw.EventCommon.emitScheduleStatus;
import static org.bedework.client.web.rw.EventCommon.initMeeting;
import static org.bedework.client.web.rw.EventCommon.setEntityCategories;
import static org.bedework.client.web.rw.EventCommon.setEventContact;
import static org.bedework.client.web.rw.EventCommon.setEventLocation;
import static org.bedework.client.web.rw.EventCommon.setEventText;
import static org.bedework.client.web.rw.EventCommon.validateEventDates;
import static org.bedework.util.misc.response.Response.Status.ok;

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
public class UpdateEventAction extends RWActionBase {
  @Override
  public int doAction(final BwRequest request,
                      final RWClient cl,
                      final BwRWActionForm form) throws Throwable {
    return doUpdate(request, cl, form,
                    new UpdatePars(request, cl, form));
  }

  public int doUpdate(final BwRequest request,
                      final RWClient cl,
                      final BwRWActionForm form,
                      final UpdatePars pars) throws Throwable {
    if (request.present("access")) {
      // Fail this to stop someone screwing around with the access
      cl.rollback();
      return forwardNoAccess;
    }

    EventInfo ei = pars.ei;
    BwEvent ev = pars.ev;
    if (ev == null) {
      return forwardNoAction;
    }

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

    /* ------------------- Change attendee list ------------------- */

    if (request.present("editEventAttendees")) {
      final int res = initMeeting(request, form, true);

      if (res != forwardSuccess) {
        return res;
      }

      return forwardEditEventAttendees;
    }

    /* ----------------- Turn event into meeting ------------------ */

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
      ev.setDtend(ev.getDtstart().addDur(ev.getDuration()));
      ev.setEndType(StartEndComponent.endTypeDuration);

      form.getEventDates().setFromEvent(ev);

      return forwardEventDatesInited;
    }

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
        pars.changes.changed(PropertyInfoIndex.DELETED,
                             true,
                             false);
        ev.setDeleted(false);
      }
    }

    /* ------------------------ Text fields ------------------------------ */

    setEventText(request, ev, pars.adding, pars.changes);

    /* ---------------------- Uploaded image ----------------------------- */

    boolean retry = false;
    boolean validationError = false;

    final List<BwXproperty> extras = new ArrayList<>();
    final UploadFileInfo ff = form.getImageUploadInfo();

    if ((ff != null) && (ff.getLength() > 0)) {
      final ProcessedImage pi = processImage(request, ff);

      if (!pi.OK) {
        if (!pi.retry) {
          cl.rollback();
          validationError = true;
        }

        retry = true;
      } else {
        BwXproperty imageXp = new BwXproperty();
        imageXp.setName(BwXproperty.bedeworkImage);
        imageXp.setValue(
                pi.image.getColPath() + "/" + pi.image.getName());
        extras.add(imageXp);

        imageXp = new BwXproperty();
        imageXp.setName(BwXproperty.bedeworkThumbImage);
        imageXp.setValue(
                pi.thumbnail.getColPath() + "/" + pi.thumbnail
                        .getName());
        extras.add(imageXp);
      }
    }

    /* ----------------------- X-properties ------------------------------ */

    int res = processXprops(pars, extras);
    if (res == forwardValidationError) {
      cl.rollback();
      validationError = true;
    }

    /* -------------------------- Dates ------------------------------ */

    res = form.getEventDates().updateEvent(ei);
    if (res == forwardValidationError) {
      cl.rollback();
      validationError = true;
    }

    if (!validateEventDates(request, ei)) {
      retry = true;
    }

    /* -------------------------- CalSuite ------------------------------ */

    ev.setCalSuite(form.getCalSuiteName());

    /* -------------------------- Location ------------------------------ */

    if (!setLocation(pars)) {
      retry = true;
    }

    /* -------------------------- Contact ------------------------------ */


    if (!setContact(pars)) {
      retry = true;
    }

    /* -------------------------- Aliases ------------------------------ */

    final var aliasGer = doAliases(pars);
    if (!aliasGer.isOk()) {
      retry = true;
    }
    final Set<BwCategory> cats = aliasGer.getEntity();

    /* -------------------------- Categories ------------------------------ */

    final EventsI.SetEntityCategoriesResult secr =
            setEntityCategories(request,
                                cats,
                                ev, pars.changes);
    if (secr.rcode != forwardSuccess) {
      cl.rollback();
      return secr.rcode;
    }

    /* ------------------------- Link ---------------------------- */

    final String link = Util.checkNull(request.getReqPar("eventLink"));
    if ((link != null) && (Util.validURI(link) == null)) {
      form.getErr().emit(ValidationError.invalidUri);
      retry = true;
    }

    if (!Util.equalsString(ev.getLink(), link)) {
      pars.changes.changed(PropertyInfoIndex.URL, ev.getLink(), link);
      ev.setLink(link);
    }

    /* ------------------------- Cost ---------------------------- */

    final String cost = Util.checkNull(request.getReqPar("eventCost"));
    if (!Util.equalsString(ev.getCost(), cost)) {
      pars.changes.changed(PropertyInfoIndex.COST, ev.getCost(), cost);
      ev.setCost(cost);
    }

    /* ------------------------- Transparency ---------------------------- */

    final String transp = Util.checkNull(request.getReqPar("transparency"));
    if (!Util.equalsString(ev.getTransparency(), transp)) {
      pars.changes.changed(PropertyInfoIndex.TRANSP, ev.getTransparency(),
                           transp);
      ev.setTransparency(transp);
    }


    /* ------------------------- Status ---------------------------- */

    /* Set the status for the event, confirmed, tentative or canceled
     */
    final String fStatus = request.getReqPar("eventStatus");

    if (!Util.equalsString(fStatus, ev.getStatus())) {
      /* Changing the status */
//      if (BwEvent.statusCancelled.equals(fStatus)) {
      //      canceling = true;
      //  }

      pars.changes.changed(PropertyInfoIndex.STATUS, ev.getStatus(),
                           fStatus);
      ev.setStatus(fStatus);
    }

    /* ------------------------- Scheduling ---------------------------- */

    /* -------------------------- Attendees ---------------------------- */

    final Attendees atts = form.getAttendees();

    ChangeTableEntry cte =
            pars.changes.getEntry(PropertyInfoIndex.ATTENDEE);

    if (pars.adding) {
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
      retry = true;
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
        validationError = true;
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

          cte = pars.changes.getEntry(PropertyInfoIndex.RRULE);
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
        cte = pars.changes.getEntry(PropertyInfoIndex.RRULE);
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

    updateRExdates(pars, evDateOnly);

    /* ---------------- Suggested to a group? --------------------- */

    if (!doAdditional(pars)) {
      retry = true;
    }

    /* ------------------ final validation -------------------------- */

    /* If we're updating but not publishing a submitted event, treat it as
     * if it were the submit app.
     */
    final List<ValidationError> ves = pars.validate();

    if (ves != null) {
      for (final ValidationError ve: ves) {
        request.error(ve.getErrorCode(), ve.getExtra());
      }
      retry = true;
    }

    /* --------------------- Must have a calendar ------------------ */

    if (ev.getColPath() == null) {
      request.error(ValidationError.missingCalendar);
      cl.rollback();
      retry = true;
    }

    if (retry || validationError) {
      return forwardRetry;
    }

    /* --------------------- Add or update the event ------------------------ */

    final var fwd = update(pars);
    if (fwd != forwardSuccess) {
      return fwd;
    }

    if (pars.adding) {
      ev = new BwEventObj();
      form.getEventDates().setNewEvent(ev);

      final TimeView tv = request.getSess().getCurTimeView(request);

      form.getEventStartDate().setDateTime(tv.getCurDayFmt().getDateTimeString());
      form.getEventEndDate().setDateTime(tv.getCurDayFmt().getDateTimeString());
      ei = new EventInfo(ev);

      form.setEventInfo(ei, true);

      if (ev.getEntityType() == IcalDefs.entityTypeTodo) {
        request.message(ClientMessage.addedTasks, 1);
      } else {
        request.message(ClientMessage.addedEvents, 1);
      }
    } else if (ev.getEntityType() == IcalDefs.entityTypeTodo) {
      request.message(ClientMessage.updatedTask);
    } else {
      request.message(ClientMessage.updatedEvent);
    }

    cl.clearSearchEntries();

    return forwardSuccess;
  }

  protected boolean isOwner(final UpdatePars pars) {
    return true;
  }

  protected boolean setLocation(final UpdatePars pars) throws Throwable {
    setEventLocation(pars.request, pars.ei, pars.form, pars.submitApp);
    return true;
  }

  /* -------------------------- Contact ------------------------------ */

  protected boolean setContact(final UpdatePars pars) {
    if (!setEventContact(pars.request, pars.submitApp)) {
      return false;
    }

    return true;
  }

  protected GetEntityResponse<Set<BwCategory>> doAliases(final UpdatePars pars) {
    final var ger = new GetEntityResponse<Set<BwCategory>>();

    if (!pars.submitApp) {
      return ger;
    }

    final RealiasResult resp = pars.cl.reAlias(pars.ev);
    if (resp.getStatus() != ok) {
      if (debug()) {
        debug("Failed to get topical areas? " + resp);
      }
      pars.cl.rollback();
      pars.request.error(ValidationError.missingTopic);
      return Response.error(ger, ValidationError.missingTopic);
    }

    ger.setEntity(resp.getCats());
    return ger;
  }

  protected boolean doAdditional(final UpdatePars pars) throws Throwable {
    return true;
  }

  protected int update(final UpdatePars pars) throws Throwable {
    final var fwd = doUpdate(pars);
    if (fwd != forwardSuccess) {
      return fwd;
    }

    pars.request.refresh();

    return forwardSuccess;
  }

  protected int doUpdate(final UpdatePars pars) throws Throwable {

    if (debug()) {
      debug(pars.changes.toString());
    }

    try {
      final UpdateResult ur;
      pars.ei.setNewEvent(pars.adding);

      if (pars.adding) {
        ur = pars.cl.addEvent(pars.ei,
                              !pars.sendInvitations,
                              true);
      } else {
        ur = pars.cl.updateEvent(pars.ei, !pars.sendInvitations,
                                 null, false);
      }
      if (!ur.isOk()) {
        pars.request.error(ur.getMessage());
        return forwardError;
      }

      if (ur.schedulingResult != null) {
        emitScheduleStatus(pars.form, ur.schedulingResult, false);
      }

      pars.form.assignAddingEvent(false);

      /* -------------------------- Access ------------------------------ */

      final String aclStr = pars.request.getReqPar("acl");
      if (aclStr != null) {
        final Acl acl =
                new AccessXmlUtil(null, pars.cl).getAcl(aclStr, true);

        pars.cl.changeAccess(pars.ev, acl.getAces(), true);
      }
    } catch (final CalFacadeException cfe) {
      pars.cl.rollback();

      if (CalFacadeException.noRecurrenceInstances.equals(cfe.getMessage())) {
        pars.request.error(ClientError.noRecurrenceInstances,
                           pars.ev.getUid());
        return forwardValidationError;
      }

      if (CalFacadeException.duplicateGuid.equals(cfe.getMessage())) {
        pars.request.error(ClientError.duplicateUid);
        return forwardDuplicate;
      }

      throw cfe;
    }

    return forwardSuccess;
  }

  /** Update rdates and exdates.
   *
   * @param pars update parameters
   * @param evDateOnly true for date only
   * @return boolean  true if changed.
   * @throws Throwable on fatal error
   */
  private boolean updateRExdates(final UpdatePars pars,
                                 final boolean evDateOnly) throws Throwable {
    Collection<BwDateTime> reqDates = pars.request.getRdates(evDateOnly);
    final BwEvent event = pars.ev;

    final Collection<BwDateTime> evDates = new TreeSet<>();
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
      for (final BwDateTime dt: reqDates) {
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
      final ChangeTableEntry cte =
              pars.changes.getEntry(PropertyInfoIndex.RDATE);
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

    reqDates = pars.request.getExdates(evDateOnly);

    if (Util.isEmpty(reqDates)) {
      if (!Util.isEmpty(evDates)) {
        removed = evDates;
        event.getExdates().clear();
        exdChanged = true;
      }
    } else if (Util.isEmpty(evDates)) {
      for (final BwDateTime dt: reqDates) {
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
      final ChangeTableEntry cte =
              pars.changes.getEntry(PropertyInfoIndex.EXDATE);
      cte.setChanged(evDates, event.getExdates());
      cte.setRemovedValues(removed);
      cte.setAddedValues(added);
    }

    return rdChanged || exdChanged;
  }

  private static final String alwaysRemove1 =
          "BEGIN:VEVENT\r\nUID:123456\r\nDTSTART;VALUE=DATE:20080212\r\n";

  private static final String alwaysRemove2 =
          "BEGIN:VEVENT\nUID:123456\nDTSTART;VALUE=DATE:20080212\n";

  /* return forwardNoAction for no change
   * forward success for change otherwise error.
   */
  protected int processXprops(final UpdatePars pars,
                              final List<BwXproperty> extras) throws Throwable {
    final ChangeTableEntry cte =
            pars.changes.getEntry(PropertyInfoIndex.XPROP);
    final List<String> unparsedxps =
            pars.request.getReqPars("xproperty");

    final List<BwXproperty> added;
    List<BwXproperty> removed = null;

    List<BwXproperty> xprops = null;

    final List<BwXproperty> evxprops = new ArrayList<>();

    if (!Util.isEmpty(pars.ev.getXproperties())) {
      evxprops.addAll(pars.ev.getXproperties());
    }

    /* When the xproperties get emitted we don't emit the set marked for skipping
     * We have to preserve those now.
     */

    if (unparsedxps != null) {
      xprops = parseXprops(pars, /*publishEvent*/false);
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
        cte.setChanged(evxprops, pars.ev.getXproperties());
        cte.setRemovedValues(removed);
      }

      return forwardSuccess;
    }

    if (Util.isEmpty(evxprops)) {
      if (pars.ev.getXproperties() == null) {
        pars.ev.setXproperties(new ArrayList<>(xprops));
      } else {
        pars.ev.getXproperties().addAll(xprops);
      }

      cte.setChanged(evxprops, new ArrayList<>(pars.ev.getXproperties()));
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
                                       pars.ev.getXproperties(), // change this (to)
                                       added, removed)) {
      cte.setChanged(evxprops, new ArrayList<>(pars.ev.getXproperties()));
      cte.setAddedValues(added);
      cte.setRemovedValues(removed);

      return forwardSuccess;
    }

    return forwardNoAction;
  }

  /** Create resource entities based on the uploaded file.
   *
   * @param request BwRequest object
   * @param file - uploaded
   * @return never null.
   */
  protected ProcessedImage processImage(final BwRequest request,
                                        final UploadFileInfo file) {
    final ProcessedImage pi = new ProcessedImage();
    final RWClient cl = (RWClient)request.getClient();

    try {
      final long maxSize = cl.getUserMaxEntitySize();

      if (file.getLength() > maxSize) {
        request.getErr().emit(ValidationError.tooLarge, file.getLength(), maxSize);
        pi.retry = true;
        return pi;
      }

      /* If the user has set a default images directory preference it must exist.
       * Otherwise we use a system default. For the moment we
       * try to create a folder called "Images"
       */

      BwCalendar imageCol;

      String imagecolPath = cl.getPreferences().getDefaultImageDirectory();
      if (imagecolPath == null) {
        final BwCalendar home = cl.getHome();

        final String imageColName = "Images";

        imagecolPath = Util.buildPath(false, home.getPath(), "/",
                                      imageColName);

//        for (BwCalendar col: cl.getChildren(home)) {
//          if (col.getName().equals(imageColName)) {
//            imageCol = col;
//            break;
//          }
//        }

        imageCol = cl.getCollection(imagecolPath);

        if (imageCol == null) {
          imageCol = new BwCalendar();

          imageCol.setSummary(imageColName);
          imageCol.setName(imageColName);
          imageCol = cl.addCollection(imageCol, home.getPath());
        }
      } else {
        imageCol = cl.getCollection(imagecolPath);
        if (imageCol == null) {
          request.getErr().emit(ClientError.missingImageDirectory);
          return pi;
        }
      }

      final String thumbType = "png";
      final Filenames fns = makeFilenames(file.getFileName(),
                                          thumbType);

      /* See if the resource exists already */

      boolean replace = false;
      boolean replaceThumb = false;

      pi.image = cl.getResource(
              Util.buildPath(false, imageCol.getPath(), "/", fns.fn));

      if (pi.image != null) {
        if (!request.getBooleanReqPar("replaceImage", false)) {
          request.getErr().emit(ClientError.duplicateImage);
          pi.retry = true;
          return pi;
        }

        replace = true;

        if (!cl.getResourceContent(pi.image)) {
          request.getErr().emit("Missing content for " +
                                        imageCol.getPath() + "/" + fns.fn);
          pi.retry = true;
          return pi;
        }
      } else {
        pi.image = new BwResource();
        pi.image.setColPath(imagecolPath);
        pi.image.setName(fns.fn);
      }

      final Images images;

      try {
        images = ImageProcessing.createImages(
                file.getContentStream(),
                fns.fnSuffix,
                thumbType, 160);
      } catch (final Throwable t) {
        /* Probably an image type we can't process or maybe not an image at all
         */
        if (debug()) {
          error(t);
        }

        request.getErr().emit(ClientError.imageError);
        pi.retry = true;
        return pi;
      }

      cl.setResourceValue(pi.image,
                          file.getContentStream(),
                          file.getLength());
      pi.image.setContentType(file.getContentType());

      /* Make a thumbnail */

      pi.thumbnail = cl.getResource(
              Util.buildPath(false, imageCol.getPath(), "/",
                             fns.thumbFn));

      if (pi.thumbnail != null) {
        replaceThumb = true;
        if (!cl.getResourceContent(pi.image)) {
          request.getErr().emit("Missing content for " +
                                        imageCol.getPath() + "/" +
                                        fns.thumbFn);
          pi.retry = true;
          return pi;
        }
      } else {
        pi.thumbnail = new BwResource();
        pi.thumbnail.setName(fns.thumbFn);
        pi.thumbnail.setColPath(imagecolPath);
      }

      pi.thumbnail.setContentType("image/" + thumbType);

      cl.setResourceValue(pi.thumbnail,
                          images.getThumbInputStream(),
                          images.getThumbLength());

      if (!replace) {
        cl.saveResource(pi.image);
      } else {
        cl.updateResource(pi.image, true);
      }

      if (!replaceThumb) {
        cl.saveResource(pi.thumbnail);
      } else {
        cl.updateResource(pi.thumbnail, true);
      }

      pi.OK = true;
    } catch (final Throwable t) {
      if (debug()) {
        error(t);
        request.getErr().emit(t);
      }
    }

    return pi;
  }

  private static class Filenames {
    String fn;
    String fnSuffix;
    String thumbFn;
  }

  /**
   *
   * @param imageName from upload
   * @param thumbType "png" etc
   * @return datestamped names
   */
  private Filenames makeFilenames(final String imageName,
                                  final String thumbType) {
    final int dotPos = imageName.lastIndexOf('.');
    final Filenames fns = new Filenames();

    final String dt = new SimpleDateFormat("yyyyMMddhhmm").format(new Date());

    if (dotPos < 0) {
      fns.fn = imageName + "-" + dt;
      fns.fnSuffix = null;
      fns.thumbFn = fns.fn + "-thumb" + "." + thumbType;
    } else {
      final String namePart = imageName.substring(0, dotPos) + "-" + dt;
      fns.fnSuffix = imageName.substring(dotPos + 1);
      fns.fn = namePart + imageName.substring(dotPos);
      fns.thumbFn =  namePart + "-thumb" + "." + thumbType;
    }

    return fns;
  }

  /* This is a bit bogus but it will get us going. Make up a fake calendar,
   * parse it with ical4j and then extract the xproperties.
   *
   * We'll be rebuilding all of this anyway.
   */
  private List<BwXproperty> parseXprops(final UpdatePars pars,
                                        final boolean publishEvent) throws Throwable {
    final Collection<String> unparsedxps =
            pars.request.getReqPars("xproperty");

    if (unparsedxps == null) {
      return null;
    }

    final StringBuilder sb = new StringBuilder();
    sb.append("BEGIN:VCALENDAR\n" +
                      "VERSION:2.0\n" +
                      "PRODID:-//xyz.com//EN\n" +
                      "BEGIN:VEVENT\n" +
                      "UID:123456\n" +
                      "DTSTART;VALUE=DATE:20080212\n");

    for (final String unparsedxp: unparsedxps) {
      /* Probably not the most efficient way */
      String val = unparsedxp.replace("\r\n", "\\n");
      val = val.replace("\n", "\\n");
      val = val.replace("\r", "\\n");

      sb.append(val);
      sb.append("\n");
    }

    sb.append("END:VEVENT\n" +
                      "END:VCALENDAR\n");

    final Client cl = pars.request.getClient();
    final IcalTranslator trans = new IcalTranslator(new IcalCallbackcb(cl));
    final Icalendar c = trans.fromIcal(null, new StringReader(sb.toString()));

    final BwEvent ev = c.getEventInfo().getEvent();

    final List<BwXproperty> xprops = ev.getXproperties();

    final List<BwXproperty> strippedXprops = new ArrayList<>();

    if (!publishEvent) {
      for (final BwXproperty xp: xprops) {
        if (xp.getSkipJsp()) {
          // Should not be here
          continue;
        }

        if (xp.getName().equals(BwXproperty.bedeworkSuggestedTo)) {
          // We output it but don't want it back
          continue;
        }

        // Remove empty ones.
        if ((xp.getValue() == null) || (xp.getValue().length() == 0)) {
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

    final Map<String, String> plistMap = new HashMap<>();
    for (final String plistName: pars.request.getReqPars("xprop-preserve")) {
      plistMap.put(plistName, plistName);
    }

    for (final BwXproperty xp: xprops) {
      if (plistMap.get(xp.getName()) != null) {
        strippedXprops.add(xp);
      }
    }

    return strippedXprops;
  }

  private static final HashMap<String, String> validFreq = new HashMap<>();
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
    final StringBuilder rrule = new StringBuilder();
    final String freq = request.getReqPar("freq");
    if ((freq == null) || ("NONE".equals(freq)) ){
      return null;
    }

    if (validFreq.get(freq) == null) {
      request.error("org.bedework.bad.request", "freq");
      return null;
    }

    rrule.append("FREQ=");
    rrule.append(freq);

    final Integer interval = request.getIntReqPar("interval",
                                                  ValidationError.invalidRecurInterval);
    if (interval != null) {
      rrule.append(";INTERVAL=");
      rrule.append(interval);
    }

    final Integer count =
            request.getIntReqPar("count",
                                 ValidationError.invalidRecurCount);
    String until = request.getReqPar("until");

    if ((count != null) && (until != null)) {
      request.error(ValidationError.invalidRecurCountAndUntil);
      return null;
    }

    if (until != null) {
      // RECUR This needs fixing when we get the widgets sorted out.
      /* For the time being, if the start is a date/time and we get date only for
       * until, add the time part of the start.
       *
       *  Also use the start timezone for until.
       */
      final TimeDateComponents start = form.getEventDates().getStartDate();

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
        request.error(ValidationError.invalidRecurUntil, "until");
        return null;
      }

      rrule.append(";UNTIL=");
      rrule.append(until);
    }

    if (count != null) {
      if (count < 0) {
        request.error(ValidationError.invalidRecurCount, count);
        return null;
      }
      rrule.append(";COUNT=");

      rrule.append(count);
    }

    if (!"DAILY".equals(freq)) {
      final String byday = request.getReqPar("byday");

      if (byday != null) {
        rrule.append(";BYDAY=");
        rrule.append(byday);
      }

      if (!"WEEKLY".equals(freq)) {
        final String bymonthday = request.getReqPar("bymonthday");
        if (bymonthday != null) {
          rrule.append(";BYMONTHDAY=");
          rrule.append(bymonthday);
        }

        if (!"MONTHLY".equals(freq)) {
          final String bymonth = request.getReqPar("bymonth");
          if (bymonth != null) {
            rrule.append(";BYMONTH=");
            rrule.append(bymonth);
          }

          final String byyearday = request.getReqPar("byyearday");
          if (byyearday != null) {
            rrule.append(";BYYEARDAY=");
            rrule.append(byyearday);
          }
        }
      }
    }

    final String rruleStr = rrule.toString();

    try {
      new Recur(rruleStr);
    } catch (final Throwable t) {
      request.error(ValidationError.invalidRecurRule);
      return null;
    }

    if (debug()) {
      debug(rruleStr);
    }

    return rruleStr;
  }

  // ============== event form elements ==============

  public void setDescription(final String val) {
    getRwForm().setDescription(val);
  }

  public String getDescription() {
    return getRwForm().getDescription();
  }

  public void setSummary(final String val) {
    getRwForm().setSummary(val);
  }

  public String getSummary() {
    return getRwForm().getSummary();
  }

  // ========= time and date form elements ===========

  public TimeDateComponents getEventStartDate() {
    return getRwForm().getEventStartDate();
  }

  public TimeDateComponents getEventEndDate() {
    return getRwForm().getEventEndDate();
  }

  public DurationBean getEventDuration() {
    return getRwForm().getEventDates().getDuration();
  }

  // ============ contact form elements ==============

  public void setAllContactId(final String val) {
    getRwForm().setAllContactId(val);
  }

  public String getAllContactId() {
    return getRwForm().getAllContactId();
  }

  public void setPrefContactId(final String val) {
    getRwForm().setPrefContactId(val);
  }

  public String getPrefContactId() {
    return getRwForm().getPrefContactId();
  }

  // ===================== location form elements ====================

  public void setAllLocationId(final String val) {
    getRwForm().setAllLocationId(val);
  }

  public String getAllLocationId() {
    return getRwForm().getAllLocationId();
  }

  public void setPrefLocationId(final String val) {
    getRwForm().setPrefLocationId(val);
  }

  public String getPrefLocationId() {
    return getRwForm().getPrefLocationId();
  }
}
