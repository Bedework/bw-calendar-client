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
import org.bedework.appcommon.EventKey;
import org.bedework.appcommon.SelectId;
import org.bedework.appcommon.client.Client;
import org.bedework.appcommon.client.IcalCallbackcb;
import org.bedework.calfacade.BwAttendee;
import org.bedework.calfacade.BwCalendar;
import org.bedework.calfacade.BwContact;
import org.bedework.calfacade.BwDateTime;
import org.bedework.calfacade.BwEvent;
import org.bedework.calfacade.BwGroup;
import org.bedework.calfacade.BwLocation;
import org.bedework.calfacade.BwOrganizer;
import org.bedework.calfacade.BwPrincipal;
import org.bedework.calfacade.BwXproperty;
import org.bedework.calfacade.CalFacadeDefs;
import org.bedework.calfacade.RecurringRetrievalMode;
import org.bedework.calfacade.base.BwStringBase;
import org.bedework.calfacade.exc.ValidationError;
import org.bedework.calfacade.mail.Message;
import org.bedework.calfacade.svc.BwCalSuite;
import org.bedework.calfacade.svc.EventInfo;
import org.bedework.calfacade.util.ChangeTable;
import org.bedework.convert.IcalTranslator;
import org.bedework.convert.RecurRuleComponents;
import org.bedework.util.calendar.IcalDefs;
import org.bedework.util.calendar.PropertyIndex.PropertyInfoIndex;
import org.bedework.util.calendar.ScheduleMethods;
import org.bedework.util.http.PooledHttpClient;
import org.bedework.util.http.PooledHttpClient.ResponseHolder;
import org.bedework.util.http.RequestBuilder;
import org.bedework.util.misc.Util;
import org.bedework.webcommon.Attendees;
import org.bedework.webcommon.BwAbstractAction;
import org.bedework.webcommon.BwActionFormBase;
import org.bedework.webcommon.BwRequest;
import org.bedework.webcommon.BwWebUtil;
import org.bedework.webcommon.BwWebUtil.ValidateResult;

import net.fortuna.ical4j.model.parameter.Role;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import javax.servlet.http.HttpServletResponse;

/** Common base class fro some or all event actions.
 *
 * @author douglm
 *
 */
public abstract class EventActionBase extends BwAbstractAction {
  /** Given the EventInfo object refresh the information in the form.
   *
   * @param request
   * @param ei
   * @return int forward.
   * @throws Throwable
   */
  protected int refreshEvent(final BwRequest request,
                             final EventInfo ei) throws Throwable {
    BwActionFormBase form = request.getBwForm();

    if (ei == null) {
      request.getErr().emit(ClientError.unknownEvent);
      return forwardNotFound;
    }

    Client cl = request.getClient();
    BwEvent ev = ei.getEvent();

    form.setEventInfo(ei, false);
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
                                  ei.getChangeset(cl.getCurrentPrincipalHref()))) {
      return forwardNoAction;
    }

    BwLocation loc = ev.getLocation();

    if (debug()) {
      if (loc == null) {
        debug("Set event with null location");
      } else {
        debug("Set event with location " + loc);
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

    EventFormatter ef = new EventFormatter(cl,
                                           new IcalTranslator(new IcalCallbackcb(cl)),
                                           ei);

    form.setCurEventFmt(ef);

    if (ev.getScheduleMethod() != ScheduleMethods.methodTypeNone) {
      // Assume we need a list of event calendars
      form.setMeetingCal(cl.getCollection(ev.getColPath()));
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

  protected void embedPreferredAdminGroups(final BwRequest request) throws Throwable {
    final Client cl = request.getClient();

    final Set<String> prefGroupHrefs = cl.getPreferences().getPreferredGroups();

    final List<BwGroup> prefGroups = new ArrayList<>(prefGroupHrefs.size());

    for (final String href: prefGroupHrefs) {
      final BwGroup group = cl.getAdminGroup(href);

      if (group == null) {
        continue;
      }

      prefGroups.add(group);
    }

    request.setSessionAttr(BwRequest.bwPreferredAdminGroupsInfoName,
                           prefGroups);
  }

  /* Create a copy of the event.
   *
   * @return false for not found
   */
  protected void copyEvent(final BwRequest request,
                           final BwEvent ev) throws Throwable {
    /* Refetch the event and switch it for a cloned copy.
     * guid and name must be set to null to avoid dup guid.
     */
    final BwActionFormBase form = request.getBwForm();
    final Client cl = request.getClient();
    EventInfo ei = fetchEvent(request, ev);
    final BwEvent evcopy = (BwEvent)ei.getEvent().clone();

    /* Ensure new attendee set is in form */
    form.setAttendees(new Attendees());
    form.getAttendees().setAttendees(evcopy.getAttendees());

    evcopy.setId(CalFacadeDefs.unsavedItemKey);
    evcopy.setUid(null);
    evcopy.setName(null);

    if (cl.getPublicAdmin()) {
      /* For a public event remove all the suggested information and
       * any topical areas not owned by this suite
       */

      evcopy.removeXproperties(BwXproperty.bedeworkSuggestedTo);
      final BwPrincipal p = cl.getPrincipal(
              form.getCurrentCalSuite().getGroup().getOwnerHref());
      final BwCalSuite cs = cl.getCalSuite();

      final BwCalendar col = cl.getHome(p, false);

      if (col == null) {
        request.getErr().emit("No calendar home");
      } else {
        final Collection<BwXproperty> aliases = evcopy.getXproperties(BwXproperty.bedeworkAlias);

        final String homePath = Util.buildPath(true, col.getPath());

        for (final BwXproperty xp: aliases) {
          if (xp.getValue().startsWith(homePath)) {
            continue;
          }

          evcopy.removeXproperty(xp);
        }
      }

      evcopy.getCategories().clear();
    }

    ei = new EventInfo(evcopy);

    form.setEventInfo(ei, false);
    resetEvent(request, false);
    form.assignAddingEvent(true);
  }

  /* Mostly for admin use at the moment. */
  protected void resetEvent(final BwRequest request,
                            final boolean clearForm) throws Throwable {
    BwActionFormBase form = request.getBwForm();
    Client cl = request.getClient();
    //form.setEventInfo(null);

    BwEvent event = form.getEvent();

    /* A is the All box, B is the user preferred values. * /
    form.retrieveCategoryKey().reset(null, IntSelectId.AHasPrecedence);
    */

    if (!clearForm) {
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
    }

    BwCalendar c = cl.getCollection(event.getColPath());
    String path = null;
    if (c != null) {
      path = c.getPath();
      form.setCalendar(c);
      //request.getSess().getChildren(cl, c);
    }

    form.retrieveCalendarId().reset(path, SelectId.AHasPrecedence);
  }

  protected EventInfo findEvent(final BwRequest request,
                                final EventKey ekey) throws Throwable {
    final Client cl = request.getClient();
    EventInfo ev = null;

    if (ekey.getColPath() == null) {
      // bogus request
      request.getErr().emit(ValidationError.missingCalendarPath);
      return null;
    }

    String key = null;

    if (ekey.getGuid() != null) {
      if (debug()) {
        debug("Get event by guid");
      }
      key = ekey.getGuid();
      String rid = ekey.getRecurrenceId();
      // DORECUR is this right?
      final RecurringRetrievalMode rrm;
      if (ekey.getForExport()) {
        rrm = RecurringRetrievalMode.overrides;
        rid = null;
      } else {
        rrm = RecurringRetrievalMode.expanded;
      }

      final Collection<EventInfo> evs =
              cl.getEventByUid(ekey.getColPath(),
                               ekey.getGuid(),
                               rid, rrm).getEntities();
      if (debug()) {
        debug("Get event by guid found " + evs.size());
      }

      if (evs.size() == 1) {
        ev = evs.iterator().next();
      } else {
        // XXX this needs dealing with
      }
    } else if (ekey.getName() != null) {
      if (debug()) {
        debug("Get event by name");
      }
      key = ekey.getName();

      ev = cl.getEvent(ekey.getColPath(),
                       ekey.getName(),
                       ekey.getRecurrenceId());
    }

    if (ev == null) {
      request.getErr().emit(ClientError.unknownEvent, key);
      return null;
    } else if (debug()) {
      debug("Get event found " + ev.getEvent());
    }

    return ev;
  }

  /** Refetch an event given a copy of that event. Collection path, uid and possibly
   * recurrence id must be set.
   *
   * @param request
   * @param event   BwEvent to refetch
   * @return EventInfo or null if not found
   * @throws Throwable
   */
  protected EventInfo fetchEvent(final BwRequest request,
                                 final BwEvent event) throws Throwable {
    Client cl = request.getClient();
    EventInfo ei = null;

    BwCalendar cal = cl.getCollection(event.getColPath());

    if (cal == null) {
      // Assume no access
      request.getErr().emit(ClientError.noAccess);
      return null;
    }

    String key;

    if (!cal.getCollectionInfo().uniqueKey) {
      /* Use name */
      key = event.getName();
      ei = cl.getEvent(cal.getPath(), key,
                       null);
    } else {
      /* Use uid */
      String uid = event.getUid();
      key = uid;

      if (uid == null) {
        // Assume no access
        request.getErr().emit(ClientError.noAccess);
        return null;
      }

      String rid = event.getRecurrenceId();

      final Collection<EventInfo> evs =
              cl.getEventByUid(cal.getPath(),
                               uid, rid,
                               RecurringRetrievalMode.overrides).getEntities();
      if (debug()) {
        debug("Get event by guid found " + evs.size());
      }

      if (evs.size() == 1) {
        ei = evs.iterator().next();
      } else {
        // XXX this needs dealing with
      }
    }

    if (ei == null) {
      request.getErr().emit(ClientError.unknownEvent, key);
      return null;
    } else if (debug()) {
      debug("Fetch event found " + ei.getEvent());
    }

    return ei;
  }

  protected EventInfo refetchEvent(final BwRequest request) throws Throwable {
    EventInfo ei = request.getBwForm().getEventInfo();

    if (ei == null) {
      return null;
    }

    // Refetch
    return fetchEvent(request, ei.getEvent());
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

    ev.updateSummaries(lang, text);

    if (lang == null) {
      changes.changed(PropertyInfoIndex.SUMMARY, evText, ev.findSummary(lang));
    } else {
      warn("No multi-language support in change table.");
    }
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

    ev.updateDescriptions(lang, text);

    if (lang == null) {
      changes.changed(PropertyInfoIndex.DESCRIPTION, evText, ev.findDescription(lang));
    } else {
      warn("No multi-language support in change table.");
    }
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
   * @param req the request
   * @param ei event object
   * @return boolean  true OK, false not OK and message(s) emitted.
   */
  protected boolean adminEventLocation(final BwRequest req,
                                       final EventInfo ei) {
    BwActionFormBase form = req.getBwForm();
    Client cl = req.getClient();
    BwEvent event = ei.getEvent();
    ChangeTable changes = ei.getChangeset(cl.getCurrentPrincipalHref());

    if (!form.retrieveLocId().getChanged()) {
      /* The location id from the form didn't change so they didn't select from
       * the list. If we allow auto create - did they provide a new location
       */
      if (form.getConfig().getAutoCreateLocations()) {
        BwLocation l = form.getLocation();

        ValidateResult vr = BwWebUtil.validateLocation(form);
        if (!vr.ok) {
          return false;
        }

        var lres = cl.ensureLocationExists(l,
                                           cl.getCurrentPrincipalHref());
        if (!lres.isOk()) {
          return false;
        }

        l = lres.getEntity();

        if (changes.changed(PropertyInfoIndex.LOCATION,
                            event.getLocation(), l)) {
          event.setLocation(l);
        }
      }

      return true;
    }

    // The user selected one from the list

    try {
      final String uid = form.retrieveLocId().getVal();
      final BwLocation loc = cl.getPersistentLocation(uid);
      final BwLocation eloc = event.getLocation();

      if ((loc == null) || !loc.getPublick()) {
        // Somebody's faking
        form.getErr().emit(ValidationError.locationNotPublic);
        return false;
      }

      if ((eloc == null) || !loc.equals(eloc)) {
        event.setLocation(loc);
        form.setLocation(loc);
        changes.changed(PropertyInfoIndex.LOCATION,
                        eloc, loc);
      }

      return true;
    } catch (Throwable t) {
      form.getErr().emit(t);
      return false;
    }
  }

  protected boolean setEventLocation(final BwRequest request,
                                     final EventInfo ei,
                                     final BwActionFormBase form,
                                     final boolean webSubmit) throws Throwable {
    Client cl = request.getClient();
    BwEvent ev = ei.getEvent();
    ChangeTable changes = ei.getChangeset(cl.getCurrentPrincipalHref());

    BwLocation loc = getLocation(cl,
                                 form, ev.getOwnerHref(), webSubmit);
    BwLocation eloc = ev.getLocation();

    if ((eloc == null) && (loc == null)) {
      return false;
    }

    if ((eloc == null) || (loc == null) || !loc.equals(eloc)) {
      changes.changed(PropertyInfoIndex.LOCATION,
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
   * @param request
   * @param svci
   * @param event
   * @return boolean  true OK, false not OK and message(s) emitted.
   */
  protected boolean setEventContact(final BwRequest request,
                                    final boolean webSubmit) {
    Client cl = request.getClient();
    BwActionFormBase form = request.getBwForm();
    EventInfo ei = form.getEventInfo();
    BwEvent event = ei.getEvent();
    ChangeTable changes = ei.getChangeset(cl.getCurrentPrincipalHref());

    BwContact c = null;
    String owner = event.getOwnerHref();

    if (owner == null) {
      owner = cl.getCurrentPrincipalHref();
    }

    if (!form.retrieveCtctId().getChanged()) {
      /* Didn't select from list. Do we allow auto-create */
      if (form.getConfig().getAutoCreateContacts()) {
        c = form.getContact();

        ValidateResult vr = BwWebUtil.validateContact(form);
        if (!vr.ok) {
          return false;
        }

        var cres = cl.ensureContactExists(c, owner);
        if (!cres.isOk()) {
          return false;
        }

        c = cres.getEntity();

        if (changes.changed(PropertyInfoIndex.CONTACT,
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
        c = cl.getPersistentContact(uid);
      } catch (Throwable t) {
        form.getErr().emit(t);
        return false;
      }
    }

    BwContact evc = event.getContact();

    if (Util.cmpObjval(evc, c) != 0) {
      changes.changed(PropertyInfoIndex.CONTACT,
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
   * @param request
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
  public int doAttendee(final BwRequest request,
                        final BwActionFormBase form,
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
    Client cl = request.getClient();

    if (uri == null) {
      request.getErr().emit(ValidationError.invalidUser, "null");
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
      initMeeting(request, form, false);
    }

    Attendees atts = form.getAttendees();

    if (!form.getAddingEvent()) {
      atts.setAttendees(ev.getAttendees());
      atts.setRecipients(ev.getRecipients());
    }

    String calAddr = cl.uriToCaladdr(uri);
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

    Integer maxAttendees;

    if (cl.getAuthProperties().getMaxAttendeesPerInstance() == null) {
      maxAttendees = 250;
    } else {
      maxAttendees = cl.getAuthProperties().getMaxAttendeesPerInstance();
    }

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

    /* TODO - do we need this
        if (ev.getRecipients() == null) {
          ev.setRecipients(val);
        } else {
          Util.adjustCollection(val, ev.getRecipients());
        }

     */

    return forwardSuccess;
  }

  /** Make an event into a valid meeting object
   *
   * @param request
   * @param form
   * @param freebusy - true to do the freebusy thing.
   * @return int
   * @throws Throwable
   */
  public int initMeeting(final BwRequest request,
                         final BwActionFormBase form,
                         final boolean freebusy) throws Throwable {
    Client cl = request.getClient();
    BwEvent ev = form.getEvent();

    if (ev.getScheduleMethod() == ScheduleMethods.methodTypeNone) {
      ev.setScheduleMethod(ScheduleMethods.methodTypeRequest);
    }

    if (ev.getOrganizer() == null) {
      /* Set the organizer to us */
      BwOrganizer org = new BwOrganizer();

      org.setOrganizerUri(cl.getCurrentCalendarAddress());

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
      String uri = cl.getCurrentCalendarAddress();

      int res = doAttendee(request,
                           form,
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

    return doFreeBusy(request, form, form.getAttendees(),
                      null, null, null, 1);
  }

  protected boolean notifySubmitter(final BwRequest request,
                                    final EventInfo ei,
                                    final String submitterEmail) throws Throwable {
    if (submitterEmail == null) {
      return false;
    }

    final Message emsg = new Message();

    final String[] to = new String[]{submitterEmail};
    emsg.setMailTo(to);

    final BwActionFormBase form = request.getBwForm();

    emsg.setFrom(form.getSnfrom());
    emsg.setSubject(form.getSnsubject());
    emsg.setContent(form.getSntext());

    request.getClient().postMessage(emsg);

    return true;
  }

  // TODO - this needs to be somewhere it gets shut down properly
  private static PooledHttpClient http;

  protected boolean notifyEventReg(final BwRequest request,
                                   final EventInfo ei) throws Throwable {
    final Client cl = request.getClient();

    final String evregToken = cl.getSystemProperties().getEventregAdminToken();
    final String evregUrl = cl.getSystemProperties().getEventregUrl();

    if ((evregToken == null) || (evregUrl == null)) {
      // Cannot notify
      return false;
    }

    /* Send a notification to the event registration system that a
     * registerable event has changed. It's up to that system to
     * do something with it.
     */

    if (http == null) {
      http = new PooledHttpClient(new URI(evregUrl));
    }

    final RequestBuilder rb = new RequestBuilder("info/eventChg.do");
    rb.par("atkn", evregToken);
    rb.par("href", ei.getEvent().getHref());

    final ResponseHolder resp = http.get(rb.toString(),
                                         "application/xml",
                                         null); // No content expected

    return resp.status == HttpServletResponse.SC_OK;
  }
}
