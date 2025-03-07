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

import org.bedework.calfacade.BwDateTime;
import org.bedework.calfacade.locale.BwLocale;
import org.bedework.calfacade.util.BwDateTimeUtil;
import org.bedework.util.timezones.DateTimeUtil;
import org.bedework.util.timezones.Timezones;

import com.ibm.icu.text.DateFormat;
import com.ibm.icu.util.GregorianCalendar;
import com.ibm.icu.util.TimeZone;
import net.fortuna.ical4j.model.TimeZoneRegistry;

import java.io.Serializable;
import java.text.FieldPosition;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

/** Class to format and provide segments of dates and times.
 *
 * <p>There are 3 different times available to us (though some or all may be
 * equal depending upon your location).
 * <ul>
 * <li>The local time represented by the object</li>
 * <li>The time in the timezone specified by the object</li>
 * <li>UTC time for the object</li>
 * </ul>
 * <p>For example, we might have </br>
 *  EDT date/time of 2006-10-10 at 14:00 </br>
 *  UTC time of 2006-10-10 at 19:00 and a </br>
 *  local time if on the US West coast of 2006-10-10 at 11:00
 *
 * <p>Generally for display to the user we want to display the local time
 * whatever the timezone of the original date time.
 *
 * <p>This class makes two formatted date objects available, one the current
 * timezone representation and the other being the timezone for the date/time
 * object handed to the constructor.
 *
 * <p>If the timezone of the date/time object is the same as the current
 * timezone they are the same object.
 *
 * <ul>
 * <li>getFormattedDate - returns the local form</li>
 * <li>getTzFormattedDate - returns the form in it's specified timezone</li>
 * </ul>
 *
 * <p>The getXXX value and the getTZXXX will only differ if the timezone is
 * not the local timezone.
 *
 * @author Mike Douglass   douglm - rpi.edu
 *  @version 1.0
 */
public class DateTimeFormatter
        implements Comparable<DateTimeFormatter>,
        Comparator<DateTimeFormatter>, Serializable {
  /** The date/time we are handling.
   */
  private BwDateTime date;

  /* True if the date value has the local timezone */
  private boolean tzIsLocal;

  /* True if the date value is UTC */
  private boolean isUtc;

  private boolean error;

  /* What we store in the table */
  private static class Formatters implements Serializable {
//    DateFormat dayFormatter;
 ///   DateFormat shortDayFormatter;

    DateFormat longDateFormatter;
    DateFormat shortDateFormatter;
    DateFormat shortTimeFormatter;

    Formatters(final Locale loc) {
    //  dayFormatter = new SimpleDateFormat("EEEE", loc);
      //shortDayFormatter = new SimpleDateFormat("E", loc);

      longDateFormatter = DateFormat.getDateInstance(DateFormat.LONG, loc);
      shortDateFormatter = DateFormat.getDateInstance(DateFormat.SHORT, loc);
      shortTimeFormatter = DateFormat.getTimeInstance(DateFormat.SHORT, loc);
    }
  }

  private FormattedDate formatted;

  private FormattedDate tzFormatted;

  /** The date and time fields will be either for the current timezone, usually
   *  the local one, or the timezone in the supplied BwDateTime object..
   *
   * @author douglm
   */
  public static class FormattedDate implements Serializable {
    private static final HashMap<Locale, Formatters> formattersTbl =
            new HashMap<>();

    //private CalendarInfo calInfo;

    private GregorianCalendar cal;

    /* yyyyMMddThhmmss */
    private String isoDateTime;

    private boolean dateOnly;

    private TimeZone tz;

    private boolean error;

    /** Constructor
     *
     * @param error true indicates an error object
     */
    public FormattedDate(final boolean error) {
      this.error = error;
    }

    /** Constructor
     *
     * @param dt date object
     * @param isoDateTime yyyyMMdd
     * @param dateOnly   true if date was a date-only value.
     * @param tz timezone
     */
    public FormattedDate(final Date dt,
                         final String isoDateTime,
                         final boolean dateOnly,
                         final TimeZone tz) {
      this.isoDateTime = isoDateTime;
      this.dateOnly = dateOnly;
      this.tz = tz;

      cal = new GregorianCalendar();
      if (tz != null) {
        cal.setTimeZone(tz);
      }
      cal.setTime(dt);
    }

    /** Return the date part in the rfc format yyyymmdd
     *
     * @return String date part in the rfc format yyyymmdd
     */
    public String getDate() {
      if (error) {
        return "error";
      }

      return isoDateTime.substring(0, 8);
    }


    /** Get the year for this object.
     *
     * @return int    year for this object
     */
    public int getYear() {
      if (error) {
        return -1;
      }

      try {
        return Integer.parseInt(isoDateTime.substring(0, 4));
      } catch (final Throwable t) {
        return -1;
      }
    }

    /** Get a four-digit representation of the year
     *
     * @return String   four-digit representation of the year for this object.
     */
    public String getFourDigitYear() {
      if (error) {
        return "XXXX";
      }

      try {
        return isoDateTime.substring(0, 4);
      } catch (final Throwable t) {
        return "XXXX";
      }
    }

    /** Get the number of the month for this object. A value between 1-12
     *
     * @return int    month number for this object
     */
    public int getMonth() {
      if (error) {
        return -1;
      }

      try {
        return Integer.parseInt(isoDateTime.substring(4, 6));
      } catch (final Throwable t) {
        return -1;
      }
    }

    /** Get a two-digit representation of the month of year
     *
     * @return String   two-digit representation of the month of year for
     *                  this object.
     */
    public String getTwoDigitMonth() {
      if (error) {
        return "XX";
      }

      try {
        return isoDateTime.substring(4, 6);
      } catch (final Throwable t) {
        return "XX";
      }
    }

    /** Get the long month name for this object.
     *
     * @return String    month name for this object
     */
    public String getMonthName() {
      if (error) {
        return "error";
      }

      final Formatters fmt = getFormatters();
      final FieldPosition f =
              new FieldPosition(DateFormat.MONTH_FIELD);
      synchronized (fmt) {
        final StringBuffer s = fmt.longDateFormatter
                .format(cal.getTime(),
                        new StringBuffer(), f);
        return s.substring(f.getBeginIndex(), f.getEndIndex());
      }
    }

    /** Get the day of the month for this object. A value between 1-31
     *
     * @return int    day of the month for this object
     */
    public int getDay() {
      if (error) {
        return -1;
      }

      try {
        return Integer.parseInt(isoDateTime.substring(6, 8));
      } catch (final Throwable t) {
        return -1;
      }
    }

    /** Get the day name for this object.
     *
     * @return String    day name for this object
     */
    public String getDayName() {
      if (error) {
        return "error";
      }

      return CalendarInfo.getInstance().getDayName(getDayOfWeek());
    }

    /** Get the day of the week
     *
     * @return The day of the week for a Calendar
     */
    public int getDayOfWeek() {
      if (error) {
        return -1;
      }

      return cal.get(Calendar.DAY_OF_WEEK);
    }

    /** Get a two-digit representation of the day of the month
     *
     * @return String   two-digit representation of the day of the month for
     *                  this object.
     */
    public String getTwoDigitDay() {
      if (error) {
        return "XX";
      }

      try {
        return isoDateTime.substring(6, 8);
      } catch (final Throwable t) {
        return "XX";
      }
    }

    /** Get the hour of the (24 hour) day for this object. A value between 0-23
     *
     * @return int    hour of the day for this object
     */
    public int getHour24() {
      if (error) {
        return-1;
      }

      if (dateOnly) {
        return 0;
      }

      try {
        return Integer.parseInt(isoDateTime.substring(9, 11));
      } catch (final Throwable t) {
        return -1;
      }
    }

    /** Get a two-digit representation of the 24 hour day hour
     *
     * @return String   two-digit representation of the hour for this object.
     */
    public String getTwoDigitHour24() {
      if (error) {
        return "XX";
      }

      if (dateOnly) {
        return "00";
      }

      try {
        return isoDateTime.substring(9, 11);
      } catch (final Throwable t) {
        return "XX";
      }
    }

    /** Get the hour of the day for this object. A value between 1-12
     *
     * @return int    hour of the day for this object
     */
    public int getHour() {
      if (error) {
        return -1;
      }

      final int hr = getHour24();

      if (hr == 0) {
        return 12; // midnight+
      }

      if (hr < 13) {
        return hr;
      }

      return hr - 12;
    }

    /** Get a two-digit representation of the hour
     *
     * @return String   two-digit representation of the hour for this object.
     */
    public String getTwoDigitHour() {
      if (error) {
        return "XX";
      }

      final String hr = String.valueOf(getHour());

      if (hr.length() == 2) {
        return hr;
      }

      return "0" + hr;
    }

    /** Get the minute of the hour for this object. A value between 0-59
     *
     * @return int    minute of the hour for this object
     */
    public int getMinute() {
      if (error) {
        return -1;
      }

      if (dateOnly) {
        return 0;
      }

      try {
        return Integer.parseInt(isoDateTime.substring(11, 13));
      } catch (final Throwable t) {
        return -1;
      }
    }

    /** Get a two-digit representation of the minutes
     *
     * @return String   two-digit representation of the minutes for this object.
     */
    public String getTwoDigitMinute() {
      if (error) {
        return "XX";
      }

      if (dateOnly) {
        return "00";
      }

      try {
        return isoDateTime.substring(11, 13);
      } catch (final Throwable t) {
        return "XX";
      }
    }

    /** Get the am/pm value
     *
     * @return int   am/pm for this object.
     */
    public int getAmPm() {
      if (error) {
        return 0;
      }

      return cal.get(Calendar.AM_PM);
    }

    /**  Get a short String representation of the date
     *
     * @return String        Short representation of the date
     *            represented by this object.
     */
    public String getDateString() {
      if (error) {
        return "error";
      }

      final Formatters fmt = getFormatters();

      synchronized (fmt) {
        final DateFormat df = fmt.shortDateFormatter;
        df.setTimeZone(tz);
        return df.format(cal.getTime());
      }
    }

    /**  Get a long String representation of the date
     *
     * @return String        long representation of the date
     *            represented by this object.
     */
    public String getLongDateString() {
      if (error) {
        return "error";
      }

      final Formatters fmt = getFormatters();

      synchronized (fmt) {
        final DateFormat df = fmt.longDateFormatter;
        df.setTimeZone(tz);
        return df.format(cal.getTime());
      }
    }

    /**  Get a short String representation of the time of day
     *
     * @return String        Short representation of the time of day
     *            represented by this object.
     *            If there is no time, returns a zero length string.
     */
    public String getTimeString() {
      if (error) {
        return "error";
      }

      final Formatters fmt = getFormatters();

      synchronized (fmt) {
        final DateFormat df = fmt.shortTimeFormatter;
        df.setTimeZone(tz);
        return df.format(cal.getTime());
      }
    }

    private Formatters getFormatters() {
      return getFormatters(BwLocale.getLocale());
    }

    private static Formatters getFormatters(final Locale loc) {
      synchronized (formattersTbl) {
        Formatters fmt = formattersTbl.get(loc);

        if (fmt == null) {
          fmt = new Formatters(loc);
          formattersTbl.put(loc, fmt);
        }

        return fmt;
      }
    }
  }

  /** Constructor
   *
   * @param date BwDateTime object to be formatted
   */
  public DateTimeFormatter(final BwDateTime date) {
    this(date, Timezones.getTzRegistry());
  }

  /** Constructor
   *
   * @param date BwDateTime object to be formatted
   * @param tzreg registry
   */
  public DateTimeFormatter(final BwDateTime date,
                           final TimeZoneRegistry tzreg) {
    try {
      this.date = date;

      isUtc = date.isUTC();
      if (isUtc) {
        tzIsLocal = false;
      } else {
        tzIsLocal = Timezones.getThreadDefaultTzid().equals(date.getTzid());
      }

      if (date.getDateType()) {
        tzIsLocal = true;
      }

      final java.util.TimeZone jtz;
      if (tzIsLocal) {
        jtz = Timezones.getDefaultTz();
      } else if (isUtc) {
        jtz = java.util.TimeZone.getTimeZone("GMT");
      } else if (date.getTzid() != null) {
        jtz = tzreg.getTimeZone(date.getTzid());
      } else {
        // date only or floating.
        jtz = Timezones.getDefaultTz();
      }

      final TimeZone tz = TimeZone.getTimeZone(jtz.getID());

      final Date dt = BwDateTimeUtil.getDate(date, tzreg);

      tzFormatted = new FormattedDate(dt, date.getDtval(),
                                      date.getDateType(), tz);

      if (tzIsLocal) {
        formatted = tzFormatted;
      } else {
        final String localIso =
                DateTimeUtil.isoDateTime(dt, Timezones.getDefaultTz());
        formatted = new FormattedDate(dt, localIso, date.getDateType(),
                                      tz);
      }
    } catch (final Throwable t) {
      error = true;
      tzFormatted = new FormattedDate(error);
      formatted = new FormattedDate(error);
    }
  }

  /**
   * @return date formatted for current timezone
   */
  public FormattedDate getFormatted() {
    return formatted;
  }

  /**
   * @return date formatted for supplied timezone
   */
  public FormattedDate getTzFormatted() {
    return tzFormatted;
  }

  /** test for local timezone.
   *
   * @return true if local time zone.
   */
  public boolean getTzIsLocal() {
    return tzIsLocal;
  }

  /** get timezone id.
   *
   * @return String tzid
   */
  public String getTzid() {
    if (isUtc) {
      return "UTC";
    }
    return date.getTzid();
  }

  /** True if this is a date only field - i.e all day
   *
   * @return boolean true for all day
   */
  public boolean getDateType() {
    return date.getDateType();
  }

  /** True if this is a utc date
   *
   * @return boolean true for utc
   */
  public boolean getUtc() {
    return isUtc;
  }

  /** True if this is a floating date
   *
   * @return boolean true for all day
   */
  public boolean getFloating() {
    return date.getFloating();
  }

  /** Return the unformatted date
   *
   * @return String
   */
  public String getUnformatted() {
    return date.getDtval();
  }

  /** Return the utc formatted date
   *
   * @return String (possibly adjusted) date in the rfc format yyyymmddThhmmssZ
   */
  public String getUtcDate() {
    return date.getDate();
  }

  /* ====================================================================
   *                        Object methods
   * ==================================================================== */

  public int compare(final DateTimeFormatter dt1,
                     final DateTimeFormatter dt2) {
    if (dt1 == dt2) {
      return 0;
    }

    return dt1.date.compareTo(dt2.date);
  }

  public int compareTo(final DateTimeFormatter o2) {
    return compare(this, o2);
  }

  @Override
  public int hashCode() {
    return date.hashCode();
  }

  @Override
  public boolean equals(final Object obj) {
    if (!(obj instanceof final DateTimeFormatter dtf)) {
      return false;
    }
    return compareTo(dtf) == 0;
  }
}
