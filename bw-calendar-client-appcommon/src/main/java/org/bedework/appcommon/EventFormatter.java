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

import org.bedework.appcommon.client.Client;
import org.bedework.calfacade.BwDateTime;
import org.bedework.calfacade.BwEvent;
import org.bedework.calfacade.svc.EventInfo;
import org.bedework.calfacade.util.BwDateTimeUtil;
import org.bedework.icalendar.EventTimeZonesRegistry;
import org.bedework.icalendar.IcalTranslator;

import net.fortuna.ical4j.model.TimeZoneRegistry;
import org.apache.log4j.Logger;

import java.io.Serializable;
import java.util.Date;

/** Object to provide formatting services for a BwEvent.
 *
 * @author Mike Douglass   douglm  rpi.edu
 */
public class EventFormatter extends EventTimeZonesRegistry
  implements TimeZoneRegistry, Serializable {
  /** The event
   */
  private EventInfo eventInfo;

  private Client cl;

  /** The view currently in place.
   */
  //private TimeView view;

  /* Set so that questions can be asked about the time */
  private MyCalendarVO today;

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
   * @param eventInfo
   */
  public EventFormatter(final Client cl,
                        final IcalTranslator trans,
                        final EventInfo eventInfo) {
    super(trans,
          eventInfo.getEvent());
    this.eventInfo = eventInfo;
    this.cl = cl;
  }

  /** =====================================================================
   *                     Property methods
   *  ===================================================================== */

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

  /** ===================================================================
                      Convenience methods
      =================================================================== */

  /** Get today as a calendar object
   *
   * @return MyCalendarVO
   */
  public MyCalendarVO getToday() {
    if (today == null) {
      today = new MyCalendarVO(new Date(System.currentTimeMillis()));
    }

    return today;
  }

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
      } catch (Throwable t) {
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
      } catch (Throwable t) {
        error(t);
      }
    }

    return end;
  }

  /** Get the event's date stamp. This is always stored as a UTC value
   *
   * @return DateTimeFormatter  object corresponding to the event's dtstamp
   */
  public DateTimeFormatter getDtstamp() {
    try {
      if (dtstamp == null) {
        BwDateTime dt = BwDateTimeUtil.getDateTimeUTC(getEvent().getDtstamp());
        dtstamp = new DateTimeFormatter(dt);
      }
    } catch (Throwable t) {
      error(t);
    }

    return dtstamp;
  }

  /** Emit current event access as an xml String
   *
   * @return String
   */
  public String getXmlAccess() {
    try {
      if (xmlAccess == null) {
        xmlAccess = AccessXmlUtil.getXmlAclString(eventInfo.getCurrentAccess().getAcl(),
                                                  cl);
      }
    } catch (Throwable t) {
      error(t);
    }

    return xmlAccess;
  }

  /* ===================================================================
                      Private methods
     ==================================================================== */

  private void error(final Throwable t) {
    Logger.getLogger(this.getClass()).error(this, t);
  }

}

