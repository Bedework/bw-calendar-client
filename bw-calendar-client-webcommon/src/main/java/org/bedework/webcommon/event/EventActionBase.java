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

import org.bedework.appcommon.ClientError;
import org.bedework.appcommon.DateTimeFormatter;
import org.bedework.appcommon.EventFormatter;
import org.bedework.appcommon.SelectId;
import org.bedework.caldav.util.filter.FilterBase;
import org.bedework.calfacade.BwAttendee;
import org.bedework.calfacade.BwCalendar;
import org.bedework.calfacade.BwContact;
import org.bedework.calfacade.BwDateTime;
import org.bedework.calfacade.BwEvent;
import org.bedework.calfacade.BwLocation;
import org.bedework.calfacade.BwLongString;
import org.bedework.calfacade.BwOrganizer;
import org.bedework.calfacade.BwString;
import org.bedework.calfacade.CalFacadeDefs;
import org.bedework.calfacade.RecurringRetrievalMode;
import org.bedework.calfacade.RecurringRetrievalMode.Rmode;
import org.bedework.calfacade.base.BwStringBase;
import org.bedework.calfacade.exc.ValidationError;
import org.bedework.calfacade.mail.Message;
import org.bedework.calfacade.svc.EventInfo;
import org.bedework.calfacade.util.ChangeTable;
import org.bedework.calfacade.util.ChangeTableEntry;
import org.bedework.calsvc.indexing.BwIndexer;
import org.bedework.calsvci.CalSvcI;
import org.bedework.icalendar.IcalTranslator;
import org.bedework.icalendar.RecurRuleComponents;
import org.bedework.webcommon.Attendees;
import org.bedework.webcommon.BwAbstractAction;
import org.bedework.webcommon.BwActionFormBase;
import org.bedework.webcommon.BwRequest;
import org.bedework.webcommon.BwWebUtil;
import org.bedework.webcommon.BwWebUtil.ValidateResult;
import org.bedework.webcommon.EventKey;
import org.bedework.webcommon.EventListPars;

import edu.rpi.cmt.calendar.IcalDefs;
import edu.rpi.cmt.calendar.PropertyIndex.PropertyInfoIndex;
import edu.rpi.cmt.calendar.ScheduleMethods;
import edu.rpi.sss.util.Util;

import net.fortuna.ical4j.model.parameter.Role;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import javax.xml.ws.Holder;

/** Common base class fro some or all event actions.
 *
 * @author douglm
 *
 */
public abstract class EventActionBase extends BwAbstractAction {
  /** Get events based on EventListPars
   *
   * @param form
   * @return Collection of events or nul
   * @throws Throwable
   */
  public Collection<EventInfo> getEventsList(final BwActionFormBase form) throws Throwable {
    form.assignAddingEvent(false);
    CalSvcI svci = form.fetchSvci();

    EventListPars elpars = form.getEventListPars();
    if (elpars == null) {
      return null;
    }

    /* See if we can use the indexed version */

    boolean publicIndex = getPublicAdmin(form) | form.getPublicView();
    BwIndexer idx = svci.getIndexingHandler().getIndexer(publicIndex,
                                                         svci.getPrincipal().getPrincipalRef());

    FilterBase f = FilterBase.addAndChild(elpars.getFilter(),
                                          svci.getClientState().getViewFilter(elpars.getCollection()));

    if (!elpars.getUseDbSearch() && !elpars.getForExport() && idx.isFetchEnabled()) {
      // Fetch from the index.

      String from = null;
      String to = null;

      if (elpars.getFromDate() !=null) {
        from = elpars.getFromDate().getDate();
      }

      if (elpars.getToDate() !=null) {
        to = elpars.getToDate().getDate();
      }

      Holder<Integer> resSz = new Holder<Integer>();

      /*
       *         form.setResultStart(0);
        form.setResultCt(pageSize);
       */

      int pos = 0;
      int pageSize = -1;    // Give me all

      if (elpars.getPaged()) {
        pos = elpars.getCurPage() - 1;
        if (pos < 0) {
          pos = 0;
        }

        pageSize = elpars.getPageSize();
        pos *= pageSize;
      }

      Collection<EventInfo> events = idx.fetch(f,
                                               from,
                                               to,
                                               resSz,
                                               pos,
                                               pageSize);

      if (resSz.value != null) {
        elpars.setResultSize(resSz.value);
        elpars.setNumPages((((resSz.value + pageSize) - 1) / pageSize));
      } else if (!Util.isEmpty(events)) {
        elpars.setResultSize(resSz.value);
        elpars.setNumPages(0);
      } else {
        elpars.setResultSize(resSz.value);
        elpars.setNumPages(0);
      }

      form.setResultSize(elpars.getResultSize());
      form.setNumPages(elpars.getNumPages());

      int count = 0;
      if (!Util.isEmpty(events)) {
        count = events.size();
      }

      /* We need to implant categories etc */

      svci.getEventsHandler().implantEntities(events);

      if (debug) {
        debugMsg("Used indexer to retrieve " + count + " events");
      }

      return events;
    }

    elpars.setPaged(false);

    RecurringRetrievalMode rrm;
    if (elpars.getForExport()) {
      rrm = new RecurringRetrievalMode(Rmode.overrides);
    } else {
      rrm = new RecurringRetrievalMode(Rmode.expanded);
    }

    return svci.getEventsHandler().getEvents((BwCalendar)null,
                                             f,
                                             elpars.getFromDate(),
                                             elpars.getToDate(),
                                             null, // retrieveList
                                             rrm);
  }

  /** Given the EventInfo object refresh the information in the form.
   *
   * @param ei
   * @param request
   * @return int forward.
   * @throws Throwable
   */
  protected int refreshEvent(final EventInfo ei,
                             final BwRequest request) throws Throwable {
    BwActionFormBase form = request.getBwForm();

    if (ei == null) {
      form.getErr().emit(ClientError.unknownEvent);
      return forwardNotFound;
    }

    CalSvcI svc = form.fetchSvci();
    BwEvent ev = ei.getEvent();

    form.setEventInfo(ei);
    form.assignSavedEvent((BwEvent)ev.clone());
    form.assignAddingEvent(false);

    String str = null;
    BwStringBase bstr = ev.findDescription(null);
    if (bstr != null) {
      str = bstr.getValue();
    }
    form.setDescription(str);

    bstr = ev.findSummary(null);
    if (bstr != null) {
      str = bstr.getValue();
    } else {
      str = null;
    }
    form.setSummary(str);

    form.setEventStatus(ev.getStatus());

    if (!request.setEventCalendar(ei,
                                  ei.getChangeset(svc.getPrincipal().getPrincipalRef()))) {
      return forwardNoAction;
    }

    BwLocation loc = ev.getLocation();

    if (debug) {
      if (loc == null) {
        debugMsg("Set event with null location");
      } else {
        debugMsg("Set event with location " + loc);
      }
    }

    form.setLocation(null);

    if (loc != null) {
      form.setLocationUid(loc.getUid());
    } else {
      form.setLocationUid(null);
    }

    // Not export - just set up for display

    if (ev.getRrules() != null) {
      Collection<RecurRuleComponents> rrcs = RecurRuleComponents.fromEventRrules(ev);

      form.setRruleComponents(rrcs);
    } else {
      form.setRruleComponents(null);
    }

    EventFormatter ef = new EventFormatter(svc,
                                           new IcalTranslator(svc.getIcalCallback()),
                                           ei);

    form.setCurEventFmt(ef);

    if (ev.getScheduleMethod() != ScheduleMethods.methodTypeNone) {
      // Assume we need a list of event calendars
      form.setMeetingCal(svc.getCalendarsHandler().get(ev.getColPath()));
    }

    Collection<BwDateTime> dates = ev.getRdates();
    Collection<DateTimeFormatter> frdates = null;
    if ((dates != null) && (!dates.isEmpty())) {
      frdates = new TreeSet<DateTimeFormatter>();

      for (BwDateTime date: dates) {
        frdates.add(new DateTimeFormatter(date));
      }
    }

    form.setFormattedRdates(frdates);

    dates = ev.getExdates();
    frdates = null;
    if ((dates != null) && (!dates.isEmpty())) {
      frdates = new TreeSet<DateTimeFormatter>();

      for (BwDateTime date: dates) {
        frdates.add(new DateTimeFormatter(date));
      }
    }

    form.setFormattedExdates(frdates);

    return forwardContinue;
  }

  /* Create a copy of the event.
   *
   * @return false for not found
   */
  protected void copyEvent(final BwEvent ev,
                           final BwActionFormBase form) throws Throwable {
    /* Refetch the event and switch it for a cloned copy.
     * guid and name must be set to null to avoid dup guid.
     */
    EventInfo ei = fetchEvent(ev, form);
    BwEvent evcopy = (BwEvent)ei.getEvent().clone();

    /* Ensure new attendee set is in form */
    form.setAttendees(new Attendees());
    form.getAttendees().setAttendees(evcopy.getAttendees());

    evcopy.setId(CalFacadeDefs.unsavedItemKey);
    evcopy.setUid(null);
    evcopy.setName(null);

    ei = new EventInfo(evcopy);

    form.setEventInfo(ei);
    resetEvent(form);
    form.assignAddingEvent(true);
  }

  /* Mostly for admin use at the moment. */
  protected void resetEvent(final BwActionFormBase form) throws Throwable {
    //form.setEventInfo(null);

    BwEvent event = form.getEvent();

    /* A is the All box, B is the user preferred values. * /
    form.retrieveCategoryKey().reset(null, IntSelectId.AHasPrecedence);
    */

    BwContact s = event.getContact();
    String uid = null;
    if (s != null) {
      uid = s.getUid();
      form.setContact(s);
    }

    form.retrieveCtctId().reset(uid, SelectId.AHasPrecedence);

    BwLocation l = event.getLocation();
    uid = null;
    if (l != null) {
      uid = l.getUid();
      form.setLocation(l);
    }

    form.retrieveLocId().reset(uid, SelectId.AHasPrecedence);

    BwCalendar c = form.fetchSvci().getCalendarsHandler().get(event.getColPath());
    String path = null;
    if (c != null) {
      path = c.getPath();
      form.setCalendar(c);
    }

    form.retrieveCalendarId().reset(path, SelectId.AHasPrecedence);
  }

  protected EventInfo findEvent(final EventKey ekey,
                                final BwActionFormBase form) throws Throwable {
    CalSvcI svci = form.fetchSvci();
    EventInfo ev = null;
    BwCalendar cal = null;

    if (ekey.getColPath() == null) {
      // bogus request
      form.getErr().emit(ValidationError.missingCalendarPath);
      return null;
    }

    cal = svci.getCalendarsHandler().get(ekey.getColPath());

    if (cal == null) {
      // Assume no access
      form.getErr().emit(ClientError.noAccess);
      return null;
    }

    String key = null;

    if (ekey.getGuid() != null) {
      if (debug) {
        debugMsg("Get event by guid");
      }
      key = ekey.getGuid();
      String rid = ekey.getRecurrenceId();
      // DORECUR is this right?
      RecurringRetrievalMode rrm;
      if (ekey.getForExport()) {
        rrm = new RecurringRetrievalMode(Rmode.overrides);
        rid = null;
      } else {
        rrm = new RecurringRetrievalMode(Rmode.expanded);
      }
      Collection<EventInfo> evs = svci.getEventsHandler().get(cal.getPath(),
                                                              ekey.getGuid(),
                                                              rid, rrm,
                                                              false);
      if (debug) {
        debugMsg("Get event by guid found " + evs.size());
      }

      if (evs.size() == 1) {
        ev = evs.iterator().next();
      } else {
        // XXX this needs dealing with
      }
    } else if (ekey.getName() != null) {
      if (debug) {
        debugMsg("Get event by name");
      }
      key = ekey.getName();

      RecurringRetrievalMode rrm =
        new RecurringRetrievalMode(Rmode.overrides);
      ev = svci.getEventsHandler().get(cal.getPath(), ekey.getName(), rrm);
    }

    if (ev == null) {
      form.getErr().emit(ClientError.unknownEvent, key);
      return null;
    } else if (debug) {
      debugMsg("Get event found " + ev.getEvent());
    }

    return ev;
  }

  /** Refetch an event given a copy of that event. Collection path, uid and possibly
   * recurrence id must be set.
   *
   * @param event   BwEvent to refetch
   * @param form
   * @return EventInfo or null if not found
   * @throws Throwable
   */
  protected EventInfo fetchEvent(final BwEvent event,
                                 final BwActionFormBase form) throws Throwable {
    CalSvcI svci = form.fetchSvci();
    EventInfo ei = null;

    BwCalendar cal = svci.getCalendarsHandler().get(event.getColPath());

    if (cal == null) {
      // Assume no access
      form.getErr().emit(ClientError.noAccess);
      return null;
    }

    // DORECUR is this right?
    RecurringRetrievalMode rrm = new RecurringRetrievalMode(Rmode.overrides);
    String key;

    if (!cal.getCollectionInfo().uniqueKey) {
      /* Use name */
      key = event.getName();
      ei = svci.getEventsHandler().get(cal.getPath(), key, rrm);
    } else {
      /* Use uid */
      String uid = event.getUid();
      key = uid;

      if (uid == null) {
        // Assume no access
        form.getErr().emit(ClientError.noAccess);
        return null;
      }

      String rid = event.getRecurrenceId();

      Collection<EventInfo> evs = svci.getEventsHandler().get(cal.getPath(),
                                                              uid, rid, rrm,
                                                              false);
      if (debug) {
        debugMsg("Get event by guid found " + evs.size());
      }

      if (evs.size() == 1) {
        ei = evs.iterator().next();
      } else {
        // XXX this needs dealing with
      }
    }

    if (ei == null) {
      form.getErr().emit(ClientError.unknownEvent, key);
      return null;
    } else if (debug) {
      debugMsg("Fetch event found " + ei.getEvent());
    }

    return ei;
  }

  protected EventInfo refetchEvent(final BwActionFormBase form) throws Throwable {
    EventInfo ei = form.getEventInfo();

    if (ei == null) {
      return null;
    }

    // Refetch
    return fetchEvent(ei.getEvent(), form);
  }

  /** Set the event text fields
   *
   * @param request
   * @param ev
   * @param skipNull - don't update for null values.
   * @param changes
   * @throws Throwable
   */
  protected void setEventText(final BwRequest request,
                              final BwEvent ev,
                              final boolean skipNull,
                              final ChangeTable changes) throws Throwable {
    setEventSummary(request.getReqPar("summaryLang"),
                    request.getReqPar("summary"),
                    ev,
                    skipNull,
                    changes);

    setEventDescription(request.getReqPar("descriptionLang"),
                        request.getReqPar("description"),
                        ev,
                        skipNull,
                        changes);

    /* Now try it with localized values */

    List<String> vals = request.getReqPars("localizedSummary");
    if (!Util.isEmpty(vals)) {
      for (String s: vals) {
        int pos = s.indexOf(":");

        String text = null;
        String lang = null;

        if (pos > 0) {
          lang = s.substring(0, pos);
        }

        if (pos >= 0) {
          text = s.substring(pos + 1);
        } else {
          text = s;
        }

        setEventSummary(lang, text, ev, skipNull, changes);
      }
    }

    vals = request.getReqPars("localizedDescription");
    if (!Util.isEmpty(vals)) {
      for (String s: vals) {
        int pos = s.indexOf(":");

        String text = null;
        String lang = null;

        if (pos > 0) {
          lang = s.substring(0, pos);
        }

        if (pos >= 0) {
          text = s.substring(pos + 1);
        } else {
          text = s;
        }

        setEventDescription(lang, text, ev, skipNull, changes);
      }
    }
  }

  private void setEventSummary(final String lang,
                               final String text,
                               final BwEvent ev,
                               final boolean skipNull,
                               final ChangeTable changes) {
    BwStringBase evText = ev.findSummary(lang);

    if (!eventTextChanged(lang, text, evText, skipNull)) {
      return;
    }

    Set<BwString> oldSums = new TreeSet<BwString>();
    if (ev.getSummaries() != null) {
      oldSums.addAll(ev.getSummaries());
    }

    ev.updateSummaries(lang, text);

    changes.changed(PropertyInfoIndex.SUMMARY.getPname(),
                    oldSums, ev.getSummaries());
  }

  private void setEventDescription(final String lang,
                                   final String text,
                                   final BwEvent ev,
                                   final boolean skipNull,
                                   final ChangeTable changes) {
    BwStringBase evText = ev.findDescription(lang);

    if (!eventTextChanged(lang, text, evText, skipNull)) {
      return;
    }

    Set<BwLongString> oldDescs = new TreeSet<BwLongString>();
    if (ev.getDescriptions() != null) {
      oldDescs.addAll(ev.getDescriptions());
    }
    ev.updateDescriptions(lang, text);

    changes.changed(PropertyInfoIndex.DESCRIPTION.getPname(),
                    oldDescs,
                    ev.getDescriptions());
  }

  private boolean eventTextChanged(final String lang, final String text,
                                   final BwStringBase evText,
                                   final boolean skipNull) {
    if ((text == null) && skipNull) {
      return false;
    }

    if ((text == null) && (evText == null)) {
      return false;
    }

    if ((text == null) || (evText == null)) {
      return true;
    }

    return !text.equals(evText.getValue());
  }

  /** Validate the location provided for an event and embed it in the event and
   * the form.
   *
   * @param form
   * @param event
   * @return boolean  true OK, false not OK and message(s) emitted.
   * @throws Throwable
   */
  protected boolean adminEventLocation(final BwActionFormBase form,
                                       final EventInfo ei) throws Throwable {
    CalSvcI svci = form.fetchSvci();
    BwEvent event = ei.getEvent();
    ChangeTable changes = ei.getChangeset(svci.getPrincipal().getPrincipalRef());

    if (!form.retrieveLocId().getChanged()) {
      /* The location id from the form didn't change so they didn't select from
       * the list. If we allow auto create - did they provide a new location
       */
      if (form.retrieveConfig().getAutoCreateLocations()) {
        BwLocation l = form.getLocation();

        ValidateResult vr = BwWebUtil.validateLocation(form);
        if (!vr.ok) {
          return false;
        }

        l = svci.getLocationsHandler().ensureExists(l, svci.getPrincipal().getPrincipalRef()).entity;

        if (changes.changed(PropertyInfoIndex.LOCATION.getPname(),
                            event.getLocation(), l)) {
          event.setLocation(l);
        }
      }

      return true;
    }

    // The user selected one from the list

    try {
      String uid = form.retrieveLocId().getVal();
      BwLocation loc = svci.getLocationsHandler().get(uid);
      BwLocation eloc = event.getLocation();

      if ((loc == null) || !loc.getPublick()) {
        // Somebody's faking
        return false;
      }

      if ((eloc == null) || !loc.equals(eloc)) {
        event.setLocation(loc);
        form.setLocation(loc);
        changes.changed(PropertyInfoIndex.LOCATION.getPname(),
                        eloc, loc);
      }

      return true;
    } catch (Throwable t) {
      form.getErr().emit(t);
      return false;
    }
  }

  protected boolean setEventLocation(final EventInfo ei,
                                     final BwActionFormBase form,
                                     final boolean webSubmit) throws Throwable {
    CalSvcI svci = form.fetchSvci();
    BwEvent ev = ei.getEvent();
    ChangeTable changes = ei.getChangeset(svci.getPrincipal().getPrincipalRef());

    BwLocation loc = getLocation(form, ev.getOwnerHref(), webSubmit);
    BwLocation eloc = ev.getLocation();

    if ((eloc == null) && (loc == null)) {
      return false;
    }

    if ((eloc == null) || (loc == null) || !loc.equals(eloc)) {
      changes.changed(PropertyInfoIndex.LOCATION.getPname(),
                      eloc, loc);
      ev.setLocation(loc);
      return true;
    }

    return false;
  }

  /* Validate the contact(s) provided for an event and embed it in the event and
   * the form.
   *
   * <p>XXX We need to change this to handle multiple contacts.
   *
   * @param form
   * @param svci
   * @param event
   * @return boolean  true OK, false not OK and message(s) emitted.
   * @throws Throwable
   */
  protected boolean setEventContact(final BwActionFormBase form,
                                    final boolean webSubmit) throws Throwable {
    CalSvcI svci = form.fetchSvci();
    EventInfo ei = form.getEventInfo();
    BwEvent event = ei.getEvent();
    ChangeTable changes = ei.getChangeset(svci.getPrincipal().getPrincipalRef());

    BwContact c = null;
    String owner = event.getOwnerHref();

    if (owner == null) {
      owner = svci.getPrincipal().getPrincipalRef();
    }

    if (!form.retrieveCtctId().getChanged()) {
      /* Didn't select from list. Do  we allow auto-creat */
      if (form.retrieveConfig().getAutoCreateContacts()) {
        c = form.getContact();

        ValidateResult vr = BwWebUtil.validateContact(form);
        if (!vr.ok) {
          return false;
        }

        c = svci.getContactsHandler().ensureExists(c, owner).entity;


        if (changes.changed(PropertyInfoIndex.CONTACT.getPname(),
                            event.getContact(), c)) {
          event.setContact(c);
        }

        form.setContact(c);
      }
    }

    if (c != null) {
      return true;
    }

    // Did the user select one from the list
    String uid = form.retrieveCtctId().getVal();

    if (uid == null) {
      uid = Util.checkNull(form.getContactUid());
    }

    if (uid != null) {
      try {
        c = svci.getContactsHandler().get(uid);
      } catch (Throwable t) {
        form.getErr().emit(t);
        return false;
      }
    }

    BwContact evc = event.getContact();

    if (Util.cmpObjval(evc, c) != 0) {
      changes.changed(PropertyInfoIndex.CONTACT.getPname(),
                      event.getContact(), c);
      event.setContact(c);
      form.setContact(c);
    }

    return true;
  }

  /** XXX This should be changed so that we manipulate our list of attendees
   * then update the event list when we're finished. This may be triggering
   * multiple db updates.
   *
   * @param form
   * @param delete
   * @param update
   * @param recipient
   * @param attendee
   * @param initializing
   * @param partstat
   * @param role
   * @param uri
   * @param cn
   * @param lang
   * @param cutype
   * @param dir
   * @return int
   * @throws Throwable
   */
  public int doAttendee(final BwActionFormBase form,
                        final boolean delete,
                        final boolean update,
                        final boolean recipient,
                        final boolean attendee,
                        final boolean initializing,
                        final String partstat,
                        final String role,
                        final String uri,
                        final String cn,
                        final String lang,
                        final String cutype,
                        final String dir) throws Throwable {
    CalSvcI svc = form.fetchSvci();

    if (uri == null) {
      form.getErr().emit(ValidationError.invalidUser, "null");
      return forwardNoAction;
    }

    // XXX This is to reattach a collection. May not be needed with conversations
    EventInfo ei = form.getEventInfo();
    BwEvent ev = ei.getEvent();
    if (ev == null) {
      return forwardNoAction;
    }

    /*
    BwEventAnnotation ann = null;
    if (ev instanceof BwEventProxy) {
      ann = ((BwEventProxy)ev).getRef();
      svc.reAttach(ann);
    } else {
      svc.reAttach(ev);
    }*/

    if (!initializing &&
        ((ev.getOrganizer() == null) ||
        (form.getAttendees() == null))) {
      initMeeting(form, false);
    }

    Attendees atts = form.getAttendees();

    if (!form.getAddingEvent()) {
      atts.setAttendees(ev.getAttendees());
      atts.setRecipients(ev.getRecipients());
    }

    String calAddr = svc.getDirectories().uriToCaladdr(uri);
    if (calAddr == null) {
      form.getErr().emit(ValidationError.invalidUser, uri);
      return forwardNoAction;
    }

    if (recipient && !delete) {
      atts.addRecipient(calAddr);
    }

    if (!attendee) {
      return forwardSuccess;
    }

    BwAttendee att = new BwAttendee();

    att.setAttendeeUri(calAddr);

    if (delete) {
      atts.removeRecipient(calAddr);
      atts.removeAttendee(att);
      return forwardSuccess;
    }

    // Add or update
    if (update) {
      boolean found = false;
      if (atts.getNumAttendees() > 0) {
        for (BwAttendee att1: atts.getAttendees()) {
          if (att1.equals(att)) {
            att = att1;
            found = true;
            break;
          }
        }
      }

      if (!found) {
        form.getErr().emit(ClientError.unknownAttendee, calAddr);
        return forwardNoAction;
      }
    }

    int maxAttendees = svc.getSysparsHandler().get().getMaxAttendees();

    if ((atts.getAttendees() != null) &&
        (atts.getAttendees().size() == maxAttendees)) {
      form.getErr().emit(ValidationError.tooManyAttendees, uri);
      return forwardNoAction;
    }

    att.setRole(role);

    if ((partstat != null) &&  !"undefined".equals(partstat)) {
      att.setPartstat(partstat);
    } else {
      att.setPartstat(IcalDefs.partstatValNeedsAction);
    }

    att.setRsvp(IcalDefs.partstatValNeedsAction.equals(att.getPartstat()));

    att.setCn(cn);
    att.setLanguage(lang);
    att.setCuType(cutype);
    att.setDir(dir);

    if (!update) {
      atts.addAttendee(att);
    }

    ev.setAttendees(atts.getAttendees());
    ev.setRecipients(atts.getRecipients());

    return forwardSuccess;
  }

  /** Make an event into a valid meeting object
   *
   * @param form
   * @param freebusy - true to do the freebusy thing.
   * @return int
   * @throws Throwable
   */
  public int initMeeting(final BwActionFormBase form,
                         final boolean freebusy) throws Throwable {
    CalSvcI svc = form.fetchSvci();
    BwEvent ev = form.getEvent();

    if (ev.getScheduleMethod() == ScheduleMethods.methodTypeNone) {
      ev.setScheduleMethod(ScheduleMethods.methodTypeRequest);
    }

    if (ev.getOrganizer() == null) {
      /* Set the organizer to us */
      BwOrganizer org = new BwOrganizer();

      org.setOrganizerUri(svc.getDirectories().principalToCaladdr(svc.getPrincipal()));

      ev.setOrganizer(org);
      ev.setOrganizerSchedulingObject(true);
      ev.setAttendeeSchedulingObject(false);

      // Might need this for ischedule
      ev.setOriginator(org.getOrganizerUri());
    }

    Attendees atts = form.getAttendees();
    if (atts == null) {
      atts = new Attendees();
      form.setAttendees(atts);
    }

    if (!form.getAddingEvent()) {
      atts.setAttendees(ev.getAttendees());
      atts.setRecipients(ev.getRecipients());
    }

    if (Util.isEmpty(form.getEvent().getAttendees())) {
      // Add ourselves as an attendee
      String uri = svc.getDirectories().principalToCaladdr(svc.getPrincipal());

      int res = doAttendee(form,
                           false, false, true, true,
                           true, // Initializing
                           IcalDefs.partstats[IcalDefs.partstatAccepted],
                           Role.CHAIR.getValue(),
                           uri,
                           null, // cn
                           null, // lang
                           null, // cutype
                           null  // dir
                           );

      if (res != forwardSuccess) {
        return res;
      }
    }

    if (!freebusy) {
      return forwardSuccess;
    }

    return doFreeBusy(form, form.getAttendees(),
                      null, null, null, 1);
  }

  protected boolean notifySubmitter(final EventInfo ei,
                                    final String submitterEmail,
                                    final BwActionFormBase form) throws Throwable {
    if (submitterEmail == null) {
      return false;
    }

    Message emsg = new Message();

    String[] to = new String[]{submitterEmail};
    emsg.setMailTo(to);

    emsg.setFrom(form.getSnfrom());
    emsg.setSubject(form.getSnsubject());
    emsg.setContent(form.getSntext());

    form.getMailer().post(emsg);

    return true;
  }

  protected boolean notifyEventReg(final EventInfo ei,
                                   final BwActionFormBase form) throws Throwable {
    CalSvcI svc = form.fetchSvci();
    BwEvent ev = form.getEvent();
    ChangeTable changes = ei.getChangeset(svc.getPrincipal().getPrincipalRef());

    String evregToken = svc.getSysparsHandler().get().getEventregAdminToken();
    String evregUrl = svc.getSysparsHandler().get().getEventregUrl();

    if ((evregToken == null) || (evregUrl == null)) {
      // Cannot notify
      return false;
    }

    /* Look for special changes - e.g. canceled */

    ChangeTableEntry cte = changes.getEntry(PropertyInfoIndex.STATUS);

    if (cte.getChanged()) {

    }

    return true;
  }
}
