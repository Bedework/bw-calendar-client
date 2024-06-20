/* ********************************************************************
    Appropriate copyright notice
*/
package org.bedework.client.web.rw;

import org.bedework.access.AccessPrincipal;
import org.bedework.appcommon.ClientError;
import org.bedework.appcommon.ClientMessage;
import org.bedework.appcommon.SelectId;
import org.bedework.appcommon.TimeView;
import org.bedework.appcommon.client.Client;
import org.bedework.calfacade.BwAttendee;
import org.bedework.calfacade.BwCalendar;
import org.bedework.calfacade.BwCategory;
import org.bedework.calfacade.BwContact;
import org.bedework.calfacade.BwDateTime;
import org.bedework.calfacade.BwDuration;
import org.bedework.calfacade.BwEvent;
import org.bedework.calfacade.BwEventObj;
import org.bedework.calfacade.BwLocation;
import org.bedework.calfacade.BwLongString;
import org.bedework.calfacade.BwOrganizer;
import org.bedework.calfacade.BwPrincipal;
import org.bedework.calfacade.BwString;
import org.bedework.calfacade.BwXproperty;
import org.bedework.calfacade.CalFacadeDefs;
import org.bedework.calfacade.RecurringRetrievalMode;
import org.bedework.calfacade.ScheduleResult;
import org.bedework.calfacade.ScheduleResult.ScheduleRecipientResult;
import org.bedework.calfacade.base.BwStringBase;
import org.bedework.calfacade.base.CategorisedEntity;
import org.bedework.calfacade.base.StartEndComponent;
import org.bedework.calfacade.exc.CalFacadeException;
import org.bedework.calfacade.exc.ValidationError;
import org.bedework.calfacade.mail.Message;
import org.bedework.calfacade.svc.EventInfo;
import org.bedework.calfacade.util.BwDateTimeUtil;
import org.bedework.calfacade.util.ChangeTable;
import org.bedework.calfacade.util.ChangeTableEntry;
import org.bedework.calsvci.EventsI.SetEntityCategoriesResult;
import org.bedework.calsvci.SchedulingI.FbResponses;
import org.bedework.client.rw.RWClient;
import org.bedework.client.web.rw.EventProps.ValidateResult;
import org.bedework.convert.ical.IcalUtil;
import org.bedework.util.calendar.IcalDefs;
import org.bedework.util.calendar.PropertyIndex.PropertyInfoIndex;
import org.bedework.util.calendar.ScheduleMethods;
import org.bedework.util.calendar.ScheduleStates;
import org.bedework.util.http.PooledHttpClient;
import org.bedework.util.http.PooledHttpClient.ResponseHolder;
import org.bedework.util.http.RequestBuilder;
import org.bedework.util.logging.BwLogger;
import org.bedework.util.misc.Util;
import org.bedework.util.servlet.HttpServletUtils;
import org.bedework.util.timezones.DateTimeUtil;
import org.bedework.util.timezones.Timezones;
import org.bedework.util.timezones.TimezonesException;
import org.bedework.webcommon.BwModuleState;
import org.bedework.webcommon.BwRequest;

import net.fortuna.ical4j.model.Dur;
import net.fortuna.ical4j.model.parameter.Role;
import net.fortuna.ical4j.model.property.DtEnd;
import net.fortuna.ical4j.model.property.DtStart;
import net.fortuna.ical4j.model.property.Duration;

import java.net.URI;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import javax.servlet.http.HttpServletResponse;

import static org.bedework.client.web.rw.EventProps.validateContact;
import static org.bedework.client.web.rw.EventProps.validateLocation;
import static org.bedework.util.misc.Util.checkNull;
import static org.bedework.webcommon.ForwardDefs.forwardBadRequest;
import static org.bedework.webcommon.ForwardDefs.forwardError;
import static org.bedework.webcommon.ForwardDefs.forwardNoAction;
import static org.bedework.webcommon.ForwardDefs.forwardSuccess;

/**
 * User: mike Date: 3/12/21 Time: 21:23
 */
public class EventCommon {
  private static final BwLogger logger =
          new BwLogger().setLoggedClass(HttpServletUtils.class);

  public static EventInfo refetchEvent(final BwRequest request) {
    final BwRWActionForm form = (BwRWActionForm)request.getBwForm();
    final EventInfo ei = form.getEventInfo();

    if (ei == null) {
      return null;
    }

    // Refetch
    return fetchEvent(request, ei.getEvent());
  }

  /** Refetch an event given a copy of that event. Collection path, uid and possibly
   * recurrence id must be set.
   *
   * @param request wRequest object
   * @param event   BwEvent to refetch
   * @return EventInfo or null if not found
   */
  public static EventInfo fetchEvent(final BwRequest request,
                                     final BwEvent event) {
    final RWClient cl = (RWClient)request.getClient();
    EventInfo ei = null;

    final BwCalendar cal = cl.getCollection(event.getColPath());

    if (cal == null) {
      // Assume no access
      request.error(ClientError.noAccess);
      return null;
    }

    final String key;

    if (!cal.getCollectionInfo().uniqueKey) {
      /* Use name */
      key = event.getName();
      ei = cl.getEvent(cal.getPath(), key,
                       null);
    } else {
      /* Use uid */
      final String uid = event.getUid();
      key = uid;

      if (uid == null) {
        // Assume no access
        request.error(ClientError.noAccess);
        return null;
      }

      final String rid = event.getRecurrenceId();

      final Collection<EventInfo> evs =
              cl.getEventByUid(cal.getPath(),
                               uid, rid,
                               RecurringRetrievalMode.overrides).getEntities();
      if (logger.debug()) {
        logger.debug("Get event by guid found " + evs.size());
      }

      if (evs.size() == 1) {
        ei = evs.iterator().next();
      } else {
        // XXX this needs dealing with
      }
    }

    if (ei == null) {
      request.error(ClientError.unknownEvent, key);
      return null;
    } else if (logger.debug()) {
      logger.debug("Fetch event found " + ei.getEvent());
    }

    return ei;
  }

  /* Create a copy of the event.
   *
   * @return false for not found
   */
  public static void copyEvent(final BwRequest request,
                               final BwEvent ev) {
    /* Refetch the event and switch it for a cloned copy.
     * guid and name must be set to null to avoid dup guid.
     */
    final BwRWActionForm form = (BwRWActionForm)request.getBwForm();
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
      final BwPrincipal<?> p = cl.getPrincipal(
              form.getCurrentCalSuite().getGroup().getOwnerHref());
      final BwCalendar col = cl.getHome(p, false);

      if (col == null) {
        request.error("No calendar home");
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
  public static void resetEvent(final BwRequest request,
                            final boolean clearForm) {
    final BwRWActionForm form = (BwRWActionForm)request.getBwForm();
    final RWClient cl = (RWClient)request.getClient();
    //form.setEventInfo(null);

    final BwEvent event = form.getEvent();

    /* A is the All box, B is the user preferred values. * /
    form.retrieveCategoryKey().reset(null, IntSelectId.AHasPrecedence);
    */

    if (!clearForm) {
      final BwContact s = event.getContact();
      String uid = null;
      if (s != null) {
        uid = s.getUid();
        form.setContact(s);
      }

      form.retrieveCtctId().reset(uid, SelectId.AHasPrecedence);

      final BwLocation l = event.getLocation();
      uid = null;
      if (l != null) {
        uid = l.getUid();
        form.setLocation(l);
      }

      form.retrieveLocId().reset(uid, SelectId.AHasPrecedence);
    }

    final BwCalendar c = cl.getCollection(event.getColPath());
    String path = null;
    if (c != null) {
      path = c.getPath();
      form.setCalendar(c);
      //request.getSess().getChildren(cl, c);
    }

    form.retrieveCalendarId().reset(path, SelectId.AHasPrecedence);
  }

  /** Make an event into a valid meeting object
   *
   * @param request bw request
   * @param form action form
   * @param freebusy - true to do the freebusy thing.
   * @return int
   */
  public static int initMeeting(final BwRequest request,
                                final BwRWActionForm form,
                                final boolean freebusy) {
    final RWClient cl = (RWClient)request.getClient();
    final BwEvent ev = form.getEvent();

    if (ev.getScheduleMethod() == ScheduleMethods.methodTypeNone) {
      ev.setScheduleMethod(ScheduleMethods.methodTypeRequest);
    }

    if (ev.getOrganizer() == null) {
      /* Set the organizer to us */
      final BwOrganizer org = new BwOrganizer();

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
      final String uri = cl.getCurrentCalendarAddress();

      final int res = doAttendee(request,
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

  /**
   * @param request     BwRequest for parameters
   * @param form        action form
   * @param atts Attendees
   * @param st start time
   * @param et end time
   * @param intunitStr interval unit as string
   * @param interval value
   * @return int
   */
  public static int doFreeBusy(final BwRequest request,
                               final BwRWActionForm form,
                               final Attendees atts,
                               final String st,
                               final String et,
                               final String intunitStr,
                               final int interval) {
    final RWClient cl = (RWClient)request.getClient();
    final BwModuleState mstate = request.getModule().getState();

    /*  Start of getting date/time - make a common method? */

    final Calendar start;
    final Calendar end;

    if (st == null) {
      /* Set period and start from the current timeview */
      final TimeView tv = request.getSess().getCurTimeView(request);

      /* Clone calendar so we don't mess up time */
      start = (Calendar)tv.getFirstDay().clone();
      end = tv.getLastDay();
      end.add(Calendar.DATE, 1);
    } else {
      try {
        start = mstate.getCalInfo().getFirstDayOfThisWeek(
                Timezones.getDefaultTz(),
                DateTimeUtil.fromISODate(st));
      } catch (final TimezonesException e) {
        throw new CalFacadeException(e);
      }

      // Set end to 1 week on.
      end = (Calendar)start.clone();
      end.add(Calendar.WEEK_OF_YEAR, 1);
    }

    // Don't allow more than a month
    final Calendar check = Calendar.getInstance(
            mstate.getCalInfo().getLocale());
    check.setTime(start.getTime());
    check.add(Calendar.DATE, 32);

    if (check.before(end)) {
      return forwardBadRequest;
    }

    final BwDateTime sdt = BwDateTimeUtil.getDateTime(start.getTime());
    final BwDateTime edt = BwDateTimeUtil.getDateTime(end.getTime());

    /*  End of getting date/time - make a common method? */

    final String originator = cl.getCurrentCalendarAddress();
    final BwEvent fbreq = BwEventObj.makeFreeBusyRequest(sdt, edt,
                                                         null,// organizer
                                                         originator,
                                                         atts.getAttendees(),
                                                         atts.getRecipients());
    if (fbreq == null) {
      return forwardBadRequest;
    }

    final ScheduleResult sr = cl.schedule(new EventInfo(fbreq),
                                          null, null, false);
    if (logger.debug()) {
      logger.debug(sr.toString());
    }

    if (sr.recipientResults != null) {
      for (final ScheduleRecipientResult srr: sr.recipientResults.values()) {
        if (srr.getStatus() != ScheduleStates.scheduleOk) {
          request.message(ClientMessage.freebusyUnavailable, srr.recipient);
        }
      }
    }

    final BwDuration dur = new BwDuration();

    if (interval <= 0) {
      request.error(ClientError.badInterval, interval);
      return forwardError;
    }

    if (intunitStr != null) {
      switch (intunitStr) {
        case "minutes":
          dur.setMinutes(interval);
          break;
        case "hours":
          dur.setHours(interval);
          break;
        case "days":
          dur.setDays(interval);
          break;
        case "weeks":
          dur.setWeeks(interval);
          break;
        default:
          request.error(ClientError.badIntervalUnit, interval);
          return forwardError;
      }
    } else {
      dur.setHours(interval);
    }

    final FbResponses resps = cl.aggregateFreeBusy(sr, sdt, edt, dur);
    form.setFbResponses(resps);

    final FormattedFreeBusy ffb = new FormattedFreeBusy(
            resps.getAggregatedResponse(),
            mstate.getCalInfo().getLocale());

    form.setFormattedFreeBusy(ffb);

    emitScheduleStatus(request, sr, true);

    return forwardSuccess;
  }

  public static void emitScheduleStatus(final BwRequest request,
                                         final ScheduleResult sr,
                                         final boolean errorsOnly) {
    if (sr.errorCode != null) {
      request.error(sr.errorCode, sr.extraInfo);
    }

    if (sr.ignored) {
      request.message(ClientMessage.scheduleIgnored);
    }

    if (sr.reschedule) {
      request.message(ClientMessage.scheduleRescheduled);
    }

    if (sr.update) {
      request.message(ClientMessage.scheduleUpdated);
    }

    for (final ScheduleRecipientResult srr: sr.recipientResults.values()) {
      if (srr.getStatus() == ScheduleStates.scheduleDeferred) {
        request.message(ClientMessage.scheduleDeferred, srr.recipient);
      } else if (srr.getStatus() == ScheduleStates.scheduleNoAccess) {
        request.error(ClientError.noSchedulingAccess, srr.recipient);
      } else if (!errorsOnly) {
        request.message(ClientMessage.scheduleSent, srr.recipient);
      }
    }
  }

  /** Validate the date properties of the event.
   *
   * @param request bw request
   * @param ei event info
   * @return boolean true for ok
   */
  public static boolean validateEventDates(final BwRequest request,
                                           final EventInfo ei) {
    final BwEvent ev = ei.getEvent();
    boolean ok = true;

    /* ------------- Start, end and duration  ------------------ */

    final BwDateTime evstart = ev.getDtstart();
    final DtStart start = evstart.makeDtStart();
    DtEnd end = null;
    Duration dur = null;

    final char endType = ev.getEndType();

    if (endType == StartEndComponent.endTypeDate) {
      final BwDateTime evend = ev.getDtend();

      if (evstart.after(evend)) {
        request.error(ValidationError.startAfterEnd);
        ok = false;
      } else {
        end = IcalUtil.makeDtEnd(evend);
      }
    } else if (endType == StartEndComponent.endTypeDuration) {
      dur = new Duration(new Dur(ev.getDuration()));
    } else if (endType != StartEndComponent.endTypeNone) {
      request.error(ValidationError.invalidEndtype);
      ok = false;
    }

    if (ok) {
      /* This calculates the duration etc. We need to merge this in with the
       * EventDates stuff as we are setting things twice
       */
      IcalUtil.setDates(request.getClient().getCurrentPrincipalHref(),
                        ei, start, end, dur);
    }

    return ok;
  }

  /** Validate the properties of the event.
   *
   * @param cl - for system properties
   * @param ev event
   * @return  null for OK, validation errors otherwise.
   */
  public static List<ValidationError> validateEvent(final Client cl,
                                                    final int maxDescLen,
                                                    final boolean nullOk,
                                                    final BwEvent ev) {
    List<ValidationError> ves = null;

    /* ------------------------- colPath -------------------------- */
    if(ev.getColPath() == null) {
      ves = addError(ves, ValidationError.missingCalendar);
    }

    /* ------------- Set zero length fields to null --------------- */

    ev.setLink(checkNull(ev.getLink()));

    /* ------------------------- summary -------------------------- */

    final Collection<BwString> sums = ev.getSummaries();
    if ((sums == null) || (sums.isEmpty())) {
      if (!nullOk) {
        ves = addError(ves, ValidationError.missingTitle);
      }
    } else {
      for (final BwString s: sums) {
        if (s.getValue().length() > maxDescLen) {
          ves = addError(ves, ValidationError.tooLongSummary,
                         String.valueOf(maxDescLen));
          break;
        }
      }
    }

    /* ------------------------- description ------------------------------- */

    final Collection<BwLongString> descs = ev.getDescriptions();
    if ((descs == null) || (descs.isEmpty())) {
      if (!nullOk) {
        ves = addError(ves, ValidationError.missingDescription);
      }
    } else {
      for (final BwLongString s: descs) {
        if (s.getValue().length() > maxDescLen) {
          ves = addError(ves, ValidationError.tooLongDescription,
                         String.valueOf(maxDescLen));
          break;
        }
      }
    }

    /* ------------- Transparency and status ------------------ */

    if (!checkTransparency(ev.getTransparency())) {
      ves = addError(ves, ValidationError.invalidTransparency,
                     ev.getTransparency());
    }

    if (!checkStatus(ev.getStatus())) {
      ves = addError(ves, ValidationError.invalidStatus,
                     ev.getStatus());
    }

    /* ------------- All referenced users valid? ------------------ */

    if (ev.getNumRecipients() > 0) {
      for (final String recip: ev.getRecipients()) {
        if (!validateUserHref(cl, recip)) {
          ves = addError(ves, ValidationError.invalidRecipient, recip);
        }
      }
    }

    if (ev.getNumAttendees() > 0) {
      for (final BwAttendee att: ev.getAttendees()) {
        if (!validateUserHref(cl, att.getAttendeeUri())) {
          ves = addError(ves, ValidationError.invalidAttendee,
                         att.getAttendeeUri());
        }
      }
    }

    final BwOrganizer org = ev.getOrganizer();
    if (org != null) {
      if (!validateUserHref(cl, org.getOrganizerUri())) {
        ves = addError(ves, ValidationError.invalidOrganizer,
                       org.getOrganizerUri());
      }
    }

    return ves;
  }

  public static List<ValidationError> addError(final List<ValidationError> ves,
                                               final String errorCode) {
    return addError(ves, errorCode, null);
  }

  public static List<ValidationError> addError(List<ValidationError> ves,
                                               final String errorCode,
                                               final String extra) {
    if (ves == null) {
      ves = new ArrayList<>();
    }

    ves.add(new ValidationError(errorCode, extra));

    return ves;
  }

  /** Given a calendar user href check for validity.
   *
   * <p>If it's external to the system we just accept it. If it's internal we
   * require it be a valid user account.
   *
   * @param cl - client
   * @param href of possible principal
   * @return boolean true for ok
   */
  public static boolean validateUserHref(final Client cl,
                                         final String href) {
    final AccessPrincipal p = cl.calAddrToPrincipal(href);

    if (p == null) {
      return true; // External user.
    }

    return cl.validPrincipal(p.getPrincipalRef());
  }

  /** Check for valid transparency setting
   *
   * @param val possible transparency value
   * @return boolean true for ok
   */
  public static boolean checkTransparency(final String val) {
    return (val == null) ||  // Defaulted
            IcalDefs.transparencyOpaque.equals(val) ||
            IcalDefs.transparencyTransparent.equals(val);
  }

  /** Check for valid status setting
   *
   * @param val possible status value
   * @return boolean true for ok
   */
  public static boolean checkStatus(final String val) {
    return (val == null) ||  // Defaulted
            BwEvent.statusConfirmed.equals(val) ||
            BwEvent.statusTentative.equals(val) ||
            BwEvent.statusCancelled.equals(val);
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
   * @param attendee true for attendee
   * @param initializing true for new meeting
   * @param partstat parameter
   * @param role parameter
   * @param uri of attendee
   * @param cn parameter
   * @param lang parameter
   * @param cutype cu type parameter
   * @param dir directory
   * @return int
   */
  public static int doAttendee(final BwRequest request,
                               final BwRWActionForm form,
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
                               final String dir) {
    final RWClient cl = (RWClient)request.getClient();

    if (uri == null) {
      request.error(ValidationError.invalidUser, "null");
      return forwardNoAction;
    }

    // XXX This is to reattach a collection. May not be needed with conversations
    final EventInfo ei = form.getEventInfo();
    final BwEvent ev = ei.getEvent();
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

    final Attendees atts = form.getAttendees();

    if (!form.getAddingEvent()) {
      atts.setAttendees(ev.getAttendees());
      atts.setRecipients(ev.getRecipients());
    }

    final String calAddr = cl.uriToCaladdr(uri);
    if (calAddr == null) {
      request.error(ValidationError.invalidUser, uri);
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
        for (final BwAttendee att1: atts.getAttendees()) {
          if (att1.equals(att)) {
            att = att1;
            found = true;
            break;
          }
        }
      }

      if (!found) {
        request.error(ClientError.unknownAttendee, calAddr);
        return forwardNoAction;
      }
    }

    final Integer maxAttendees;

    if (cl.getAuthProperties().getMaxAttendeesPerInstance() == null) {
      maxAttendees = 250;
    } else {
      maxAttendees = cl.getAuthProperties().getMaxAttendeesPerInstance();
    }

    if ((atts.getAttendees() != null) &&
            (atts.getAttendees().size() == maxAttendees)) {
      request.error(ValidationError.tooManyAttendees, uri);
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

  /** Validate the location provided for an event and embed it in the event and
   * the form.
   *
   * @param req the request
   * @param ei event object
   * @return boolean  true OK, false not OK and message(s) emitted.
   */
  public static boolean adminEventLocation(final BwRequest req,
                                           final EventInfo ei) {
    final BwRWActionForm form = (BwRWActionForm)req.getBwForm();
    final RWClient cl = (RWClient)req.getClient();
    final BwEvent event = ei.getEvent();
    final ChangeTable changes = ei.getChangeset(cl.getCurrentPrincipalHref());

    if (!form.retrieveLocId().getChanged()) {
      /* The location id from the form didn't change so they didn't select from
       * the list. If we allow auto create - did they provide a new location
       */
      if (form.getConfig().getAutoCreateLocations()) {
        BwLocation l = form.getLocation();

        final ValidateResult vr = validateLocation(req, form);
        if (!vr.ok) {
          return false;
        }

        final var lres =
                cl.ensureLocationExists(l,
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
        req.error(ValidationError.locationNotPublic);
        return false;
      }

      if (!loc.equals(eloc)) {
        event.setLocation(loc);
        form.setLocation(loc);
        changes.changed(PropertyInfoIndex.LOCATION,
                        eloc, loc);
      }

      return true;
    } catch (final Throwable t) {
      req.error(t);
      return false;
    }
  }

  public static boolean setEventLocation(final BwRequest request,
                                         final EventInfo ei,
                                         final BwRWActionForm form,
                                         final boolean webSubmit) {
    final BwEvent ev = ei.getEvent();

    final BwLocation loc = getLocation(request,
                                       ev.getOwnerHref(), webSubmit);
    final BwLocation eloc = ev.getLocation();

    if ((eloc == null) && (loc == null)) {
      return false;
    }

    if ((loc == null) || !loc.equals(eloc)) {
      final ChangeTable changes = 
              ei.getChangeset(request.getClient().getCurrentPrincipalHref());
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
  public static boolean setEventContact(
          final BwRequest request,
          final boolean webSubmit) {
    final RWClient cl = (RWClient)request.getClient();
    final BwRWActionForm form = (BwRWActionForm)request.getBwForm();
    final EventInfo ei = form.getEventInfo();
    final BwEvent event = ei.getEvent();
    final ChangeTable changes =
            ei.getChangeset(cl.getCurrentPrincipalHref());

    BwContact c = null;
    String owner = event.getOwnerHref();

    if (owner == null) {
      owner = cl.getCurrentPrincipalHref();
    }

    if (!form.retrieveCtctId().getChanged()) {
      /* Didn't select from list. Do we allow auto-create */
      if (form.getConfig().getAutoCreateContacts()) {
        c = form.getContact();

        final ValidateResult vr = validateContact(request, form);
        if (!vr.ok) {
          return false;
        }

        final var cres = cl.ensureContactExists(c, owner);
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
      uid = checkNull(form.getContactUid());
    }

    if (uid != null) {
      try {
        c = cl.getPersistentContact(uid);
      } catch (final Throwable t) {
        request.error(t);
        return false;
      }
    }

    final BwContact evc = event.getContact();

    if (Util.cmpObjval(evc, c) != 0) {
      changes.changed(PropertyInfoIndex.CONTACT,
                      event.getContact(), c);
      event.setContact(c);
      form.setContact(c);
    }

    return true;
  }

  /** Set the event text fields
   *
   * @param request bw request object
   * @param ev event
   * @param skipNull - don't update for null values.
   * @param changes changetable
   */
  public static void setEventText(final BwRequest request,
                                  final BwEvent ev,
                                  final boolean skipNull,
                                  final ChangeTable changes) {
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
      for (final String s: vals) {
        final int pos = s.indexOf(":");

        final String text;
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
      for (final String s: vals) {
        final int pos = s.indexOf(":");

        final String text;
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

  private static void setEventSummary(final String lang,
                                      final String text,
                                      final BwEvent ev,
                                      final boolean skipNull,
                                      final ChangeTable changes) {
    final BwStringBase<?> evText = ev.findSummary(lang);

    if (!eventTextChanged(lang, text, evText, skipNull)) {
      return;
    }

    ev.updateSummaries(lang, text);

    if (lang == null) {
      changes.changed(PropertyInfoIndex.SUMMARY, evText, ev.findSummary(lang));
    } else {
      logger.warn("No multi-language support in change table.");
    }
  }

  private static void setEventDescription(final String lang,
                                   final String text,
                                   final BwEvent ev,
                                   final boolean skipNull,
                                   final ChangeTable changes) {
    final BwStringBase<?> evText = ev.findDescription(lang);

    if (!eventTextChanged(lang, text, evText, skipNull)) {
      return;
    }

    ev.updateDescriptions(lang, text);

    if (lang == null) {
      changes.changed(PropertyInfoIndex.DESCRIPTION, evText, ev.findDescription(lang));
    } else {
      logger.warn("No multi-language support in change table.");
    }
  }

  /** Set the entity categories based on multivalued request parameter "categoryKey".
   *
   * <p>We build a list of categories then update the membership of the entity
   * category collection to correspond.
   *
   * @param request     BwRequest for parameters
   * @param extraCats Categories to add as a result of other operations
   * @param ent the entity
   * @param changes change table
   * @return setEventCategoriesResult  with rcode = error forward or
   *                    forwardNoAction for validated OK or
   *                    forwardSuccess for calendar changed
   */
  public static SetEntityCategoriesResult setEntityCategories(
          final BwRequest request,
          final Set<BwCategory> extraCats,
          final CategorisedEntity ent,
          final ChangeTable changes) {
    final RWClient cl = (RWClient)request.getClient();

    // XXX We should use the change table code for this.
    final SetEntityCategoriesResult secr = new SetEntityCategoriesResult();

    /* categories already set in event */
    final Set<BwCategory> evcats = ent.getCategories();

    final Set<String> defCatuids = cl.getPreferences().getDefaultCategoryUids();

    /* allDefCatUids is used to preserve the default categories for
       calendar suites. For example we migth have an event created by
       Admissions with a default category of .admissions.

       If it was suggested to and accepted by Payroll it might have
       .payroll applied.

       The assumption (and I think experience) was that .payroll would
       not appear in the update request so would get lost on update.

       This code was being applied to all categorised entities and resulted in
       being unable to turn off a default category on an alias.

       I believe it should only be applied to events.
     */
    /* Get the uids of all public default categories */
    final Set<String> allDefCatUids;
    if (ent instanceof BwEvent) {
      allDefCatUids = cl.getDefaultPublicCategoryUids();
    } else {
      allDefCatUids = null;
    }

    /* Get the uids */
    final Collection<String> strCatUids = request.getReqPars("catUid");

    /* Remove all categories if we don't supply any
     */

    if (Util.isEmpty(strCatUids) &&
            Util.isEmpty(extraCats) &&
            Util.isEmpty(defCatuids) &&
            Util.isEmpty(allDefCatUids)) {
      if (!Util.isEmpty(evcats)) {
        if (changes != null) {
          final ChangeTableEntry cte = changes.getEntry(PropertyInfoIndex.CATEGORIES);
          cte.setRemovedValues(new ArrayList<>(evcats));
        }

        secr.numRemoved = evcats.size();
        evcats.clear();
      }
      secr.rcode = forwardSuccess;
      return secr;
    }

    final Set<BwCategory> cats = new TreeSet<>();

    if (extraCats != null) {
      cats.addAll(extraCats);
    }

    if (!Util.isEmpty(defCatuids)) {
      for (final String uid: defCatuids) {
        cats.add(cl.getPersistentCategory(uid));
      }
    }

    /* Preserve any default public category that we have set by adding it
       to the cats list.
     */
    if (!Util.isEmpty(allDefCatUids) &&
            (evcats != null)) {
      buildList:
      for (final String catUid: allDefCatUids) {
        /* If it's in the event add it to the list we're building then move on
         * to the next requested category.
         */
        for (final BwCategory evcat: evcats) {
          if (evcat.getUid().equals(catUid)) {
            cats.add(evcat);
            continue buildList;
          }
        }
      }
    }

    if (!Util.isEmpty(strCatUids)) {
      buildList:
      for (final String catUid: strCatUids) {
        /* If it's in the event add it to the list we're building then move on
         * to the next requested category.
         */
        if (evcats != null) {
          for (final BwCategory evcat: evcats) {
            if (evcat.getUid().equals(catUid)) {
              cats.add(evcat);
              continue buildList;
            }
          }
        }

        final BwCategory cat = cl.getPersistentCategory(catUid);

        if (cat != null) {
          cats.add(cat);
        }
      }
    }

    /* See if the user is adding new categories

    final Collection<String> reqCatKeys = request.getReqPars("categoryKey");

    if (!Util.isEmpty(reqCatKeys)) {
      final Collection<String> catKeys = new ArrayList<>();

      / * request parameter can be comma delimited list * /
      for (final String catkey: reqCatKeys) {
        final String[] parts = catkey.split(",");

        for (String part: parts) {
          if (part == null) {
            continue;
          }

          part = part.trim();

          if (part.length() == 0) {
            continue;
          }

          catKeys.add(part);
        }
      }

      for (final String catkey: catKeys) {
        // LANG - use current language code?
        final BwString key = new BwString(null, catkey);

        BwCategory cat = cl.getCategoryByName(key);
        if (cat == null) {
          cat = BwCategory.makeCategory();

          cat.setOwnerHref(cl.getCurrentPrincipalHref());
          cat.setWord(key);

          cl.addCategory(cat);
          secr.numCreated++;
        }

        cats.add(cat);
      }
    }
    */

    /* cats now contains category objects corresponding to the request parameters
     *
     * Now we need to add or remove any in the event but not in our list.
     */

    /* First make a list to remove - to avoid concurrent update
     * problems with the iterator
     */

    final ArrayList<BwCategory> toRemove = new ArrayList<>();

    if (evcats != null) {
      for (final BwCategory evcat: evcats) {
        if (cats.contains(evcat)) {
          cats.remove(evcat);
          continue;
        }

        toRemove.add(evcat);
      }
    }

    for (final BwCategory cat: cats) {
      ent.addCategory(cat);
      secr.numAdded++;
    }

    for (final BwCategory cat: toRemove) {
      if (evcats.remove(cat)) {
        secr.numRemoved++;
      }
    }

    if ((changes != null)  &&
            (secr.numAdded > 0) && (secr.numRemoved > 0)) {
      final ChangeTableEntry cte =
              changes.getEntry(PropertyInfoIndex.CATEGORIES);
      cte.setRemovedValues(toRemove);
      cte.setAddedValues(cats);
    }

    secr.rcode = forwardSuccess;

    if (secr.numCreated > 0) {
      request.message(ClientMessage.addedCategories, secr.numCreated);
    }

    return secr;
  }

  private static boolean eventTextChanged(final String lang,
                                          final String text,
                                          final BwStringBase<?> evText,
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

  private static BwLocation getLocation(final BwRequest req,
                                        final String owner,
                                        final boolean webSubmit) {
    final RWClient cl = (RWClient)req.getClient();
    final BwRWActionForm form = (BwRWActionForm)req.getBwForm();
    BwLocation loc = null;

    if (!webSubmit) {
      /* Check for user typing a new location into a text area.
       */
      final String a =
              checkNull(form.getLocationAddress().getValue());
      if (a != null) {
        // explicitly provided location overrides all others
        loc = BwLocation.makeLocation();
        loc.setAddress(new BwString(null, a));
      }
    }

    /* No new location supplied - try to retrieve by uid
     */
    if (loc == null) {
      if (form.getLocationUid() != null) {
        loc = cl.getPersistentLocation(form.getLocationUid());
      }
    }

    if (loc != null) {
      loc.setLink(checkNull(loc.getLink()));
      String ownerHref = owner;

      if (ownerHref == null) {
        ownerHref = cl.getCurrentPrincipalHref();
      }

      final var cer = cl.ensureLocationExists(loc, ownerHref);

      loc = cer.getEntity();

      if (cer.isAdded()) {
        req.message(ClientMessage.addedLocations, 1);
      }
    }

    return loc;
  }

  public static boolean notifySubmitter(final BwRequest request,
                                        final EventInfo ei,
                                        final String submitterEmail) {
    if (submitterEmail == null) {
      return false;
    }

    final Message emsg = new Message();

    final String[] to = new String[]{submitterEmail};
    emsg.setMailTo(to);

    final BwRWActionForm form = (BwRWActionForm)request.getBwForm();

    emsg.setFrom(form.getSnfrom());
    emsg.setSubject(form.getSnsubject());
    emsg.setContent(form.getSntext());

    final RWClient cl = (RWClient)request.getClient();
    cl.postMessage(emsg);

    return true;
  }

  // TODO - this needs to be somewhere it gets shut down properly
  private static PooledHttpClient http;

  public static boolean notifyEventReg(final BwRequest request,
                                       final EventInfo ei) {
    final RWClient cl = (RWClient)request.getClient();

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

    try {
      if (http == null) {
        http = new PooledHttpClient(new URI(evregUrl));
      }

      final RequestBuilder rb = new RequestBuilder(
              "info/eventChg.do");
      rb.par("atkn", evregToken);
      rb.par("href", ei.getEvent().getHref());

      final ResponseHolder<?> resp =
              http.get(rb.toString(),
                       "application/xml",
                       null); // No content expected

      return resp.status == HttpServletResponse.SC_OK;
    } catch (final Throwable t) {
      throw new CalFacadeException(t);
    }
  }
}
