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
package org.bedework.client.web.rw;

import org.bedework.calfacade.BwDateTime;
import org.bedework.calfacade.util.CalFacadeUtil;
import org.bedework.calfacade.util.EventPeriod;
import org.bedework.calsvci.SchedulingI.FbGranulatedResponse;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Locale;

/** Reformatted free busy information for display.
 * We build a collection of these to represent the free busy. Each one represents
 * a day only with each period starting at the given number of minutes into the
 * day.
 *
 * <p>With a suitably formatted free busy query we can produce this object to
 * make presentation a relatively simple prospect.
 *
 * @author Mike Douglass douglm @ rpi.edu
 *
 */
public class FormattedFreeBusy implements Serializable {
  /** Who's free busy? Normally a user - could be a room.
   */
  private String account;

  private BwDateTime start;
  private BwDateTime end;

  /** Collection of FbDayPeriod
   */
  private Collection<FbDayPeriod> days = new ArrayList<>();

  /** Class to represent a free busy day
   *
   * @author Mike Douglass
   */
  public static class FbDayPeriod implements Serializable {
    private final String dateString;

    /** Collection of FbPeriod
     */
    private Collection<FbPeriod> periods = new ArrayList<>();

    FbDayPeriod(final String dateString) {
      this.dateString = dateString;
    }

    /**
     * @return String
     */
    public String getDateString() {
      return dateString;
    }

    /** Get the free busy periods
     *
     * @return Collection    of FbPeriod
     */
    public Collection<FbPeriod> getPeriods() {
      if (periods == null) {
        periods = new ArrayList<>();
      }
      return periods;
    }
  }

  /** Class to represent a free busy period
   *
   * @author Mike Douglass
   */
  public static class FbPeriod implements Serializable {
    int minutesStart;
    int minutesLength;
    int type; // From BwFreeBusyComponent

    /* Number of busy entries this period - for the free/busy aggregator */
    private int numBusy;

    /* Number of tentative entries this period - for the free/busy aggregator */
    private int numTentative;

    /** Constructor
     *
     * @param minutesStart Start time on minutes
     * @param minutesLength length in minutes
     * @param type From BwFreeBusyComponent
     * @param numBusy Number of busy entries this period
     * @param numTentative Number of tentative entries this period
     */
    public FbPeriod(final int minutesStart, final int minutesLength,
                    final int type,
                    final int numBusy, final int numTentative) {
      this.minutesStart = minutesStart;
      this.minutesLength = minutesLength;
      this.type = type;
      this.numBusy = numBusy;
      this.numTentative = numTentative;
    }

    /** Start time on minutes
     *
     * @return int
     */
    public int getMinutesStart() {
      return minutesStart;
    }

    /** Length of free busy
     *
     * @return int
     */
    public int getMinutesLength() {
      return minutesLength;
    }

    /** type of free busy
     *
     * @return int
     */
    public int getType() {
      return type;
    }

    /**
     * @param val
     */
    public void setNumBusy(final int val) {
      numBusy = val;
    }

    /**
     * @return int
     */
    public int getNumBusy() {
      return numBusy;
    }

    /**
     * @param val
     */
    public void setNumTentative(final int val) {
      numTentative = val;
    }

    /**
     * @return int
     */
    public int getNumTentative() {
      return numTentative;
    }

    /** Get the start as a 4 digit String hours and minutes value
     *
     * @return String start time
     */
    public String getStartTime() {
      return CalFacadeUtil.getTimeFromMinutes(getMinutesStart());
    }
  }

  /** Constructor
   *
   * <p>Generates a formatted free busy object suitable for gui interfaces.
   * @param fbresp response
   * @param loc locale
   */
  public FormattedFreeBusy(final FbGranulatedResponse fbresp,
                           final Locale loc) {
    final SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");

    if (fbresp.getAttendee() != null) {
      setAccount(fbresp.getAttendee().getAttendeeUri());
    } else {
      // Aggregated response
      setAccount(null);
    }

    setStart(fbresp.getStart());
    setEnd(fbresp.getEnd());

    /* We use Calendar objects to determine the start of the next day.
     * This should allow for daylight savings problems to some extent
     * though there is going to be some confusion at the boundaries I suspect.
     *
     * Maybe we should flag when the current locale goes over a daylight savings
     * boundary?
     */
    final Calendar startCal = Calendar.getInstance(loc);
    final Calendar endDayCal = Calendar.getInstance(loc);

    startCal.setTime(getStart().makeDate());

    endDayCal.setTime(getStart().makeDate());
    endDayCal.add(Calendar.DATE, 1);

    /* We expect a number of BwFreeBusyComponent each containing a single
     * free busy period.
     */
    final long startMillis = getStart().makeDate().getTime();

    // setup first day.
    FbDayPeriod day = new FbDayPeriod(sdf.format(startCal.getTime()));
    days.add(day);
    int dayStartMinutes = -1;

    for (final EventPeriod ep: fbresp.eps) {
      if (!startCal.before(endDayCal)) {
        // new day

        day = new FbDayPeriod(sdf.format(startCal.getTime()));
        days.add(day);
        endDayCal.add(Calendar.DATE, 1);

        /* Needs to be set to start value of next period. */
        dayStartMinutes = -1;
      }

      final long pstartMillis = ep.getStart().getTime();
      final int plen = Math.round((ep.getEnd().getTime() - pstartMillis) / 60000);
      int pstart = Math.round((pstartMillis - startMillis) / 60000);

      /* Decrement period start by start of day value.
       */
      if (dayStartMinutes < 0) {
        dayStartMinutes = pstart;
      }

      pstart -= dayStartMinutes;

      day.getPeriods().add(new FbPeriod(pstart, plen, ep.getType(),
                                        ep.getNumBusy(), ep.getNumTentative()));

      startCal.add(Calendar.MINUTE, plen);
    }
  }

  /** who owns or asked for
   *
   * @param val String
   */
  public void setAccount(final String val) {
    account = val;
  }

  /**
   * @return String
   */
  public String getAccount() {
    return account;
  }

  /**
   * @param val
   */
  public void setStart(final BwDateTime val) {
    start = val;
  }

  /**
   * @return BwDateTime start
   */
  public BwDateTime getStart() {
    return start;
  }

  /**
   * @param val
   */
  public void setEnd(final BwDateTime val) {
    end = val;
  }

  /**
   * @return BwDateTime end
   */
  public BwDateTime getEnd() {
    return end;
  }

  /** Get the free busy periods
   *
   * @return Collection    of FbPeriod
   */
  public Collection<FbDayPeriod> getDays() {
    if (days == null) {
      days = new ArrayList<>();
    }
    return days;
  }
}
