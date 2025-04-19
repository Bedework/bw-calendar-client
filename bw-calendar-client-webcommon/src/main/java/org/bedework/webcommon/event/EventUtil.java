/* ********************************************************************
    Appropriate copyright notice
*/
package org.bedework.webcommon.event;

import org.bedework.appcommon.ClientError;
import org.bedework.appcommon.EventKey;
import org.bedework.appcommon.client.Client;
import org.bedework.calfacade.BwCollection;
import org.bedework.calfacade.RecurringRetrievalMode;
import org.bedework.calfacade.exc.ValidationError;
import org.bedework.calfacade.svc.EventInfo;
import org.bedework.util.logging.BwLogger;
import org.bedework.webcommon.BwRequest;

import java.util.Collection;

/**
 * User: mike Date: 8/5/24 Time: 23:45
 */
public class EventUtil {
  /** Method to retrieve an event. An event is identified by the calendar +
   * guid + recurrence id. We also take the subscription id as a parameter so
   * we can pass it along in the result for display purposes.
   *
   * <p>We cannot just take the calendar from the subscription, because the
   * calendar has to be the actual collection containing the event. A
   * subscription may be to higher up the tree (i.e. a folder).
   *
   * <p>It may be more appropriate to simply encode a url to the event.
   *
   * <p>Request parameters<ul>
   *      <li>"subid"    subscription id for event. < 0 if there is none
   *                     e.g. displayed directly from calendar.</li>
   *      <li>"calPath"  Path of calendar to search.</li>
   *      <li>"guid" | "eventName"    guid or name of event.</li>
   *      <li>"recurrenceId"   recurrence-id of event instance - possibly null.</li>
   * </ul>
   * <p>If the recurrenceId is null and the event is a recurring event we
   * should return the master event only,
   *
   * @param request   BwRequest for parameters
   * @param mode recurrence mode
   * @return EventInfo or null if not found
   */
  public static EventInfo findEvent(final BwRequest request,
                                    final RecurringRetrievalMode.Rmode mode,
                                    final BwLogger logger) {
    final Client cl = request.getClient();
    EventInfo ev = null;

    final String href = request.getReqPar("href");

    if (href != null) {
      final EventKey ekey = new EventKey(href, false);

      ev = cl.getEvent(ekey.getColPath(),
                       ekey.getName(),
                       ekey.getRecurrenceId());

      if (ev == null) {
        request.error(ClientError.unknownEvent,
                /*eid*/ekey.getName());
        return null;
      } else if (logger.debug()) {
        logger.debug("Get event found " + ev.getEvent());
      }

      return ev;
    }

    final BwCollection cal = request.getCalendar(true);

    if (cal == null) {
      return null;
    }

    final String guid = request.getReqPar("guid");
    String eventName = request.getReqPar("eventName");

    if (eventName == null) {
      eventName = request.getReqPar("contentName");
    }

    if (guid != null) {
      if (logger.debug()) {
        logger.debug("Get event by guid");
      }
      String rid = request.getReqPar("recurrenceId");
      // DORECUR is this right?
      final RecurringRetrievalMode rrm;
      if (mode == RecurringRetrievalMode.Rmode.overrides) {
        rrm = RecurringRetrievalMode.overrides;
        rid = null;
      } else if (mode == RecurringRetrievalMode.Rmode.expanded) {
        rrm = RecurringRetrievalMode.expanded;
      } else {
        rrm = new RecurringRetrievalMode(mode);
      }

      final var evs = cl.getEventByUid(cal.getPath(),
                                       guid, rid, rrm).getEntities();
      if (logger.debug()) {
        logger.debug("Get event by guid found " + evs.size());
      }
      if (evs.size() == 1) {
        ev = evs.iterator().next();
      } else if (evs.size() > 1) {
        // XXX this needs dealing with
        logger.warn("Multiple result from getEvent");
      }
    } else if (eventName != null) {
      if (logger.debug()) {
        logger.debug("Get event by name");
      }

      ev = cl.getEvent(cal.getPath(), eventName,
                       null);
    }

    if (ev == null) {
      request.error(ClientError.unknownEvent, /*eid*/
                    guid);
      return null;
    } else if (logger.debug()) {
      logger.debug("Get event found " + ev.getEvent());
    }

    return ev;
  }

  public static EventInfo findEvent(final BwRequest request,
                                    final EventKey ekey,
                                    final BwLogger logger) {
    final Client cl = request.getClient();
    EventInfo ev = null;

    if (ekey.getColPath() == null) {
      // bogus request
      request.error(ValidationError.missingCalendarPath);
      return null;
    }

    String key = null;

    if (ekey.getGuid() != null) {
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

      if (logger.debug()) {
        logger.debug("Get event by guid with rid " + rid + " and rrm " + rrm);
      }

      final Collection<EventInfo> evs =
              cl.getEventByUid(ekey.getColPath(),
                               ekey.getGuid(),
                               rid, rrm).getEntities();
      if (logger.debug()) {
        logger.debug("Get event by guid found " + evs.size());
      }

      if (evs.size() == 1) {
        ev = evs.iterator().next();
      } else {
        // XXX this needs dealing with
      }
    } else if (ekey.getName() != null) {
      if (logger.debug()) {
        logger.debug("Get event by name");
      }
      key = ekey.getName();

      ev = cl.getEvent(ekey.getColPath(),
                       ekey.getName(),
                       ekey.getRecurrenceId());
    }

    if (ev == null) {
      request.error(ClientError.unknownEvent, key);
      return null;
    } else if (logger.debug()) {
      logger.debug("Get event found " + ev.getEvent());
    }

    return ev;
  }
}
