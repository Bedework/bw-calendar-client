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

import org.bedework.base.exc.BedeworkException;
import org.bedework.calfacade.locale.BwLocale;
import org.bedework.util.timezones.DateTimeUtil;
import org.bedework.util.timezones.Timezones;
import org.bedework.util.timezones.TimezonesException;

import java.io.Serializable;
import java.text.DateFormat;
import java.text.FieldPosition;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/** Representation of the MyCalendar uwcal class.
 *<br/>
 * This object is intended to allow applications to interact with the
 * calendar back end. It does not represent the internal stored structure of a
 * MyCalendar object.
 *
 *   @author Mike Douglass douglm rpi.edu
 *  @version 1.0
 */
public class CalendarFormatter implements Serializable {
  /** LOCALE - we should be looking for locale changing?
   * Time and date as a Calendar object.
   */
  private final Calendar calendar;

  private static final DateFormat isoformat = new SimpleDateFormat("yyyyMMdd");

  /** Create a CalendarFormatter object representing a particular date and time
   * in the current locale.
   *
   * @param date   Non-null Date object.
   */
  public CalendarFormatter(final Date date) {
    //calendar = Calendar.getInstance(BwLocale.getLocale());

    try {
      calendar = Calendar.getInstance(Timezones.getDefaultTz(),
                                      BwLocale.getLocale());
    } catch (final TimezonesException tze) {
      throw new BedeworkException(tze);
    }

    calendar.setTime(date);
  }

  /** Create a CalendarFormatter object representing a particular date and time.
   * The calendar object must have the same locale.
   *
   * @param calendar     Calendar object representing the date and time.
   */
  private CalendarFormatter(final Calendar calendar) {
    this.calendar = calendar;
  }

  /** Return Calendar object representing this object
   *
   * @return Calendar    object representing this object
   */
  public Calendar getCalendar() {
    return calendar;
  }

  /** Return Date object representing this object
   *
   * @return Date    object representing this object
   */
  public Date getTime() {
    return calendar.getTime();
  }

  /** get time in millisecs
   *
   * @return long
   */
  public long getTimeInMillis() {
    return calendar.getTimeInMillis();
  }

  /* =======================================================
   *                Components of the date
   * ======================================================= */

  /** Get the year for this object.
   *
   * @return int    year for this object
   */
  public int getYear() {
    return calendar.get(Calendar.YEAR);
  }

  /** Get the number of the month for this object.
   * Note:  The first month is number 1.
   *
   * @return int    month number for this object
   */
  public int getMonth() {
    return calendar.get(Calendar.MONTH) + 1;
  }

  /** Get the day of the month for this object.
   *
   * @return int    day of the month for this object
   */
  public int getDay() {
    return calendar.get(Calendar.DATE);
  }

  /** Get the hour of the (24 hour) day for this object.
   *
   * @return int    hour of the day for this object
   */
  public int getHour24() {
    return calendar.get(Calendar.HOUR_OF_DAY);
  }

  /** Get the hour of the day for this object.
   *
   * @return int    hour of the day for this object
   */
  public int getHour() {
    return calendar.get(Calendar.HOUR);
  }

  /** Get the minute of the hour for this object.
   *
   * @return int    minute of the hour for this object
   */
  public int getMinute() {
    return calendar.get(Calendar.MINUTE);
  }

  /** Get the am/pm value
   *
   * @return int   am/pm for this object.
   */
  public int getAmPm() {
    return calendar.get(Calendar.AM_PM);
  }

  /** Get the day of the week for this object.
   *
   * @return int    day of the week
   */
  public int getDayOfWeek() {
    return getDayOfWeek(calendar);
  }

  /** Get the day name for this object.
   *
   * @return String    day name for this object
   */
  public String getDayName() {
    return CalendarInfo.getInstance().getDayName(getDayOfWeek());
  }

  /** Get the day of the week for a Calendar object
   *
   * @param c The date to evaluate
   * @return The day of the week for a Calendar
   */
  public int getDayOfWeek(final Calendar c) {
//    return c.get(Calendar.DAY_OF_WEEK) % numberDaysInWeek;
    return c.get(Calendar.DAY_OF_WEEK);
  }

  /** Get the long month name for this object.
   *
   * @return String    month name for this object
   */
  public String getMonthName() {
    return getComponent(DateFormat.MONTH_FIELD, DateFormat.LONG);
  }

  /** Get the short month name for this object.
   *
   * @return String    short month name for this object
   */
  public String getShortMonthName() {
    return getComponent(DateFormat.MONTH_FIELD, DateFormat.MEDIUM);
  }

  /** Get a four-digit representation of the year
   *
   * @return String   four-digit representation of the year for this object.
   */
  public String getFourDigitYear() {
    return String.valueOf(getYear());
  }

  /** Get a two-digit representation of the month of year
   *
   * @return String   two-digit representation of the month of year for
   *                  this object.
   */
  public String getTwoDigitMonth() {
    return getTwoDigit(getMonth());
  }

  /** Get a two-digit representation of the day of the month
   *
   * @return String   two-digit representation of the day of the month for
   *                  this object.
   */
  public String getTwoDigitDay() {
    return getTwoDigit(getDay());
  }

  /** Get a two-digit representation of the 24 hour day hour
   *
   * @return String   two-digit representation of the hour for this object.
   */
  public String getTwoDigitHour24() {
    return getTwoDigit(getHour24());
  }

  /** Get a two-digit representation of the hour
   *
   * @return String   two-digit representation of the hour for this object.
   */
  public String getTwoDigitHour() {
    return getTwoDigit(getHour());
  }

  /* * Get a two-digit representation of the minutes
   *
   *   @return String   two-digit representation of the minutes for this object.
   * /
  public String getTwoDigitMinute() {
    return getTwoDigit(getMinute());
  }

  /** ===================================================================
   *                Get various representations of date/time
   *  =================================================================== */

  /**  Get a short String representation of the time of day
   *
   * @return String        Short representation of the time of day
   *            represented by this object.
   */
  public String getTimeString() {
    return getTimeString(DateFormat.getTimeInstance(DateFormat.SHORT));
  }

  /** Get a <code>String</code> representation of the time of day
   *
   * @param df      DateFormat format for the result
   * @return String  time of day, formatted per df
   */
  public String getTimeString(final DateFormat df) {
    synchronized (df) {
      try {
        df.setTimeZone(Timezones.getDefaultTz());
        return df.format(getTime());
      } catch (final TimezonesException tze) {
        throw new BedeworkException(tze);
      }
    }
  }

  /**  Get an ISO String representation of the date
   *
   * @return String        ISO representation of the date
   *            represented by this object.
   */
  public String getISODateString() {
    return DateTimeUtil.isoDate(getTime());
  }

  /**  Get an ISO String representation of the date/time
   *
   * @return String        ISO representation of the date/time
   *            represented by this object.
   */
  public String getISODateTimeString() {
    return DateTimeUtil.isoDateTime(getTime());
  }

  /**  Get a short String representation of the date
   *
   * @return String        Short representation of the date
   *            represented by this object.
   */
  public String getDateString() {
    return getFormattedDateString(DateFormat.SHORT);
  }

  /**  Get a long String representation of the date
   *
   * @return String        long representation of the date
   *            represented by this object.
   */
  public String getLongDateString() {
    return getFormattedDateString(DateFormat.LONG);
  }

  /**  Get a full String representation of the date
   *
   * @return String        full representation of the date
   *            represented by this object.
   */
  public String getFullDateString() {
    return getFormattedDateString(DateFormat.FULL);
  }

  /** Get a <code>String</code> representation of the date
   *
   * @param style       int DateFormat style for the result
   * @return String  date formatted per df
   */
  public String getFormattedDateString(final int style) {
    return getFormattedDateString(DateFormat.getDateInstance(style));
  }

  /** Get a <code>String</code> representation of the date
   *
   * @param df      DateFormat format for the result
   * @return String  date formatted per df
   */
  public String getFormattedDateString(final DateFormat df) {
    synchronized (df) {
      try {
        df.setTimeZone(Timezones.getDefaultTz());
        return df.format(getTime());
      } catch (final TimezonesException tze) {
        throw new BedeworkException(tze);
      }
    }
  }

  /** Get a <code>String</code> representation of the given date
   *
   * @param df      DateFormat format for the result
   * @param date    Date value
   * @return String  date formatted per df
   */
  public String getFormattedDateString(final DateFormat df, final Date date) {
    synchronized (df) {
      try {
        df.setTimeZone(Timezones.getDefaultTz());
        return df.format(date.getTime());
      } catch (final TimezonesException tze) {
        throw new BedeworkException(tze);
      }
    }
  }

  /** Get an eight-digit String representation of the date
   *
   * @return String  date in the form <code>YYYYMMDD</code>
   */
  public String getDateDigits() {
    synchronized (isoformat) {
      return getFormattedDateString(isoformat);
    }
  }

  /* =======================================================
   *                Adding and subtracting time
   * ======================================================= */

  /** Get a calendar some time later or earlier than this one
   *
   * @param unit Units to add/subtract, <i>e.g.</i>,
   *          <code>Calendar.DATE</code> to add days
   * @param amount Number of units to add or subtract.  Use negative
   *            numbers to subtract
   * @return CalendarFormatter    new object corresponding to this
   *                  object +/- the appropriate number of units
   */
  private CalendarFormatter addTime(final int unit, final int amount) {
    return new CalendarFormatter(add(calendar, unit, amount));
  }

  /**  Get a CalendarFormatter object one day earlier.
   *
   * @return CalendarFormatter    equivalent to this object one day earlier.
   */
  public CalendarFormatter getYesterday() {
    return addTime(Calendar.DATE, -1);
  }

  /**  Get a CalendarFormatter object one day later.
   *
   * @return CalendarFormatter    equivalent to this object one day later.
   */
  public CalendarFormatter getTomorrow() {
    return addTime(Calendar.DATE, 1);
  }

  /**  Get a CalendarFormatter object one week earlier.
   *
   * @return CalendarFormatter    equivalent to this object one week earlier.
   */
  public CalendarFormatter getPrevWeek() {
    return addTime(Calendar.WEEK_OF_YEAR, -1);
  }

  /**  Get a CalendarFormatter object one week later.
   *
   * @return CalendarFormatter    equivalent to this object one week later.
   */
  public CalendarFormatter getNextWeek() {
    return addTime(Calendar.WEEK_OF_YEAR, 1);
  }

  /**  Get a CalendarFormatter object one month earlier.
   *
   * @return CalendarFormatter    equivalent to this object one month earlier.
   */
  public CalendarFormatter getPrevMonth() {
    return addTime(Calendar.MONTH, -1);
  }

  /**  Get a CalendarFormatter object one month later.
   *
   * @return CalendarFormatter    equivalent to this object one month later.
   */
  public CalendarFormatter getNextMonth() {
    return addTime(Calendar.MONTH, 1);
  }

  /**  Get a CalendarFormatter object one year earlier.
   *
   * @return CalendarFormatter    equivalent to this object one year earlier.
   */
  public CalendarFormatter getPrevYear() {
    return addTime(Calendar.YEAR, -1);
  }

  /**  Get a CalendarFormatter object one year later.
   *
   * @return CalendarFormatter    equivalent to this object one year later.
   */
  public CalendarFormatter getNextYear() {
    return addTime(Calendar.YEAR, 1);
  }

  /* ====================================================================
   *                Comparisons
   * ==================================================================== */

  /**
   * @return boolean true if this alendar represents today.
   */
  public boolean isToday() {
    synchronized (isoformat) {
      return getFormattedDateString(isoformat).equals(
            getFormattedDateString(isoformat, new Date()));
    }
  }

  /* ====================================================================
   *                Object methods
   * ==================================================================== */

  @Override
  public boolean equals(final Object val) {
    if (!(val instanceof final CalendarFormatter that)) {
      return false;
    }

    return calendar.equals(that.calendar);
  }

  @Override
  public String toString() {
    return getDateString() + " " + getTimeString();
  }

//  public Object clone() {
//    return new CalendarFormatter(this.getCalendar(), getLocale());
//  }

  /* ====================================================================
   *                Private methods
   * ==================================================================== */

  /** Get a two-digit representation of a one to two-digit number
   *
   * @param  i         int one or two digit number
   * @return String    two-digit representation of the number
   */
  private static String getTwoDigit(final int i) {
    if (i < 10) {
      return "0" + i;
    }

    return String.valueOf(i);
  }

  /** Get a calendar some time later or earlier than this one
   *
   * @param c    The calendar to start with
   * @param unit The unit of time to add or subtract, <i>e.g.</i>,
   *           <code>MONTH</code>.  For possible values, see the constants in
   *           <code>java.util.Calendar</code>
   * @param amount The number of units to add or subtract.  A positive
   *              value means to add, a negative value to subtract
   * @return Calendar    equivalent to c some time later or earlier
   */
  private static Calendar add(final Calendar c,
                              final int unit,
                              final int amount) {
    final Calendar newc = (Calendar)c.clone();
    newc.add(unit, amount);
    return newc;
  }

  /** Get a String representation of a particular time
   *  field of the object.
   *
   * @param field The field to be returned,
   *        <i>e.g.</i>, <code>MONTH_FIELD</code>.  For possible values, see
   *       the constants in <code>java.text.DateFormat</code>
   * @param dateFormat The style of <code>DateFormat</code> to use,
   *            <i>e.g.</i>, <code>SHORT</code>.  For possible values, see
   *           the constants in <code>java.text.DateFormat</code>.
   * @return A <code>String</code> representation of a particular time
   *             field of the object.
   */
  private String getComponent(final int field, final int dateFormat) {
    try {
      final FieldPosition f = new FieldPosition(field);
      final DateFormat df = DateFormat.
          getDateTimeInstance(dateFormat, dateFormat, BwLocale.getLocale());

      df.setTimeZone(Timezones.getDefaultTz());
      final StringBuffer s = df.format(getTime(),
                                       new StringBuffer(), f);
      return s.substring(f.getBeginIndex(), f.getEndIndex());
    } catch (final TimezonesException tze) {
      throw new BedeworkException(tze);
    }
  }
}
