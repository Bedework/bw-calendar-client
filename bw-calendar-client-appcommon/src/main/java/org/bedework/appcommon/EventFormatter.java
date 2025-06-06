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

package org.bedework.appcommon;

import org.bedework.access.CurrentAccess;
import org.bedework.appcommon.client.Client;
import org.bedework.calfacade.BwDateTime;
import org.bedework.calfacade.BwEvent;
import org.bedework.calfacade.svc.EventInfo;
import org.bedework.calfacade.util.BwDateTimeUtil;
import org.bedework.convert.EventTimeZonesRegistry;
import org.bedework.convert.IcalTranslator;
import org.bedework.util.logging.BwLogger;
import org.bedework.util.logging.Logged;

import net.fortuna.ical4j.model.TimeZoneRegistry;

import java.io.Serializable;

/** Object to provide formatting services for a BwEvent.
 *
 * @author Mike Douglass   douglm  rpi.edu
 */
public class EventFormatter extends EventTimeZonesRegistry
  implements Logged, TimeZoneRegistry, Serializable {
  /** The event
   */
  private EventInfo eventInfo;

  /* Set dynamically on request to represent start date/time */
  private DateTimeFormatter start;

  /* Set dynamically on request to represent end date/time */
  private DateTimeFormatter end;

  /* Set dynamically on request to represent dtstamp */
  private DateTimeFormatter dtstamp;

  private String xmlAccess;

  /** Constructor
   *
   * @param cl for callbacks
   * @param trans - will be synchronized so may be shared
   * @param eventInfo the event
   */
  public EventFormatter(final Client cl,
                        final IcalTranslator trans,
                        final EventInfo eventInfo) {
    super(trans,
          eventInfo.getEvent());
    this.eventInfo = eventInfo;

    try {
      final CurrentAccess ca = eventInfo.getCurrentAccess();

      if (ca == null) {
        warn("No current access for " + eventInfo.getEvent().getUid());
      } else {
        xmlAccess = AccessXmlUtil.getXmlAclString(ca.getAcl(),
                                                  cl);
      }
    } catch (final Throwable t) {
      error(t);
    }
  }

  /* =========================================================
   *                     Property methods
   * ========================================================= */

  /**
   * @return EventInfo
   */
  public EventInfo getEventInfo() {
    return eventInfo;
  }

  /**
   * @return BwEvent
   */
  public BwEvent getEvent() {
    return eventInfo.getEvent();
  }

  /* =======================================================
                      Convenience methods
     ======================================================= */

  /** Get the event's starting day and time
   *
   * @return DateTimeFormatter  object corresponding to the event's
   *                     starting day and time
   */
  public DateTimeFormatter getStart() {
    if (getEvent().getDtstart() != null) {
      try {
        if (start == null) {
          start = new DateTimeFormatter(getEvent().getDtstart(), this);
        }
      } catch (final Throwable t) {
        error(t);
      }
    }

    return start;
  }

  /** Get the event's ending date and time. If the value is a date only object
   * we decrement the date by 1 day to comply with accepted practice - that is,
   * the displayed date for a 1 day event has the end date equal to the start.
   *
   * <p>Internally we store a 1 day event with the end date 1 day after the
   * start date
   *
   * @return DateTimeFormatter  object corresponding to the event's
   *                     ending day and time
   */
  public DateTimeFormatter getEnd() {
    if (getEvent().getDtend() != null) {
      try {
        if (end == null) {
          BwDateTime dt = getEvent().getDtend();
          if (dt.getDateType()) {
            dt = dt.getPreviousDay();
          }
          end = new DateTimeFormatter(dt, this);
        }
      } catch (final Throwable t) {
        error(t);
      }
    }

    return end;
  }

  /** Get the event's date stamp. This is always stored as a UTC value
   *
   * @return DateTimeFormatter  object corresponding to the event's dtstamp
   */
  @SuppressWarnings("unused")
  public DateTimeFormatter getDtstamp() {
    if (dtstamp == null) {
      final BwDateTime dt =
              BwDateTimeUtil.getDateTimeUTC(
                      getEvent().getDtstamp());
      dtstamp = new DateTimeFormatter(dt);
    }

    return dtstamp;
  }

  /** Emit current event access as an xml String
   *
   * @return String
   */
  @SuppressWarnings("unused")
  public String getXmlAccess() {
    return xmlAccess;
  }

  /* ========================================================
   *                   Logged methods
   * ======================================================== */

  private final BwLogger logger = new BwLogger();

  @Override
  public BwLogger getLogger() {
    if ((logger.getLoggedClass() == null) && (logger.getLoggedName() == null)) {
      logger.setLoggedClass(getClass());
    }

    return logger;
  }
}

