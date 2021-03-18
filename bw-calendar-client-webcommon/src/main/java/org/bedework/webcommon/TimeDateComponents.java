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
package org.bedework.webcommon;

import org.bedework.appcommon.CalendarInfo;
import org.bedework.calfacade.BwDateTime;
import org.bedework.calfacade.exc.CalFacadeException;
import org.bedework.calfacade.util.BwDateTimeUtil;
import org.bedework.util.timezones.DateTimeUtil;
import org.bedework.util.timezones.TimeZoneName;
import org.bedework.util.timezones.Timezones;

import java.io.Serializable;
import java.util.Collection;
import java.util.Date;
import java.util.TreeSet;

/** A wrapper around date/time objects that is used to generate form elements

   @author   Mike Douglass douglm   rpi.edu
   @author   Greg Barnes
 */
/* Note:  because this is commonly (exclusively) used as a Struts bean,
   throwing exceptions from the setter methods leads to errors that
   are hard to catch (they are called by BeanUtils.populate, which
   is executed even before Struts lets you validate).

   Presumably, bad values (e.g., setYear("blxk")) are mostly due to
   malicious or erroneous form input, so we will turn them into default
   values instead.

   If you want a bean that throws exceptions when you pass in bad values,
   you should create a superclass, subclass, or sibling to this one.
 */
public class TimeDateComponents implements Serializable {
  // arrays of values and labels for dropdown menus for various units of time

  /** default labels for am and pm */
  // XXX: Should localize
  private static final String[] DEFAULT_AMPM_LABELS = {"am", "pm"};

  //private boolean debug;

  //private transient Logger log;

  private final CalendarInfo calInfo;

  /* We populate minuteLabels and minuteVals with the appropriate increments. */
  private String[] minuteLabels;
  private String[] minuteVals;

  /** Only accept minute values that are multiples of this */
  private int minuteIncrement;

  private final String[] ampmLabels;

  /** Are we on the 24-hour clock? */
  private final boolean hour24;

  /** current value of am/pm */
  private boolean isAm = true;

  private boolean dateOnly;

  private boolean floating;

  private boolean storeUTC;

  private String tzid;

  private int year;
  private int month;
  private int day;
  private int hour; // 0-23 for hour24 == true else 0-11
  private int minute;

  private String fieldInError;

  /**  An exception used by this class
   */
  public static class TimeDateException extends CalFacadeException {
    /**
     * @param msg
     */
    public TimeDateException(final String msg) {
      super(msg);
    }
  }

  /** Set up instance of this class using default values.
   *
   * @param calInfo
   * @param minuteIncrement  increment for minutes: &le; 1 is all,
   *                        5 is every 5 minutes, etc
   * @param hour24        true if we ignore am/pm and use 24hr clock
   * @exception TimeDateException If there is something wrong with the minutes
   *                        arrays
   */
  public TimeDateComponents(final CalendarInfo calInfo,
                            final int minuteIncrement,
                            final boolean hour24) throws TimeDateException {
    this.calInfo = calInfo;
    this.hour24 = hour24;

    setMinutes(minuteIncrement);

    ampmLabels = DEFAULT_AMPM_LABELS;
  }

  /**
   * @return CalendarInfo
   */
  public CalendarInfo getCalInfo() {
    return calInfo;
  }

  /**
   *
   */
  public void resetError() {
    fieldInError = null;
  }

  /**
   * @return field name
   */
  public String getError() {
    return fieldInError;
  }

  /** Get a list of system timezones
   *
   * @return Collection of timezone names
   */
  public Collection<TimeZoneName> getTimeZoneNames() {
    final Collection<TimeZoneName> nms = new TreeSet<>();

    try {
      nms.addAll(Timezones.getTzNames());
    } catch (final Throwable t) {
      fieldInError = "TimeZoneNames";
    }

    return nms;
  }

  /**
   * @return labels
   */
  public String[] getDayLabels() {
    return getCalInfo().getDayLabels();
  }

  /**
   * @return vals
   */
  public String[] getDayVals() {
    return getCalInfo().getDayVals();
  }

  /**
   * @return labels
   */
  public String[] getMonthLabels() {
    return getCalInfo().getMonthLabels();
  }

  /**
   * @return vals
   */
  public String[] getMonthVals() {
    return getCalInfo().getMonthVals();
  }

  /**
   * @return labels
   */
  public String[] getHourLabels() {
    if (hour24) {
      return getCalInfo().getHour24Labels();
    }
    return getCalInfo().getHourLabels();
  }

  /**
   * @return vals
   */
  public String[] getHourVals() {
    if (hour24) {
      return getCalInfo().getHour24Vals();
    }
    return getCalInfo().getHourVals();
  }

  /**
   * @return labels
   */
  public String[] getMinuteLabels() {
    return minuteLabels;
  }

  /**
   * @return vals
   */
  public String[] getMinuteVals() {
    return minuteVals;
  }

  /**
   * @return labels
   */
  public String[] getAmpmLabels() {
    return ampmLabels;
  }

  /**
   * @param val
   */
  public void setDateOnly(final boolean val) {
    dateOnly = val;
  }

  /**
   * @return bool
   */
  public boolean getDateOnly() {
    return dateOnly;
  }

  /**
   * @param val  tzid
   */
  public void setTzid(final String val) {
    tzid = val;
  }

  /**
   * @return current tzid
   */
  public String getTzid() {
    if (tzid == null) {
      tzid = Timezones.getThreadDefaultTzid();
    }

    return tzid;
  }

  /**
   * @param val true for floating
   */
  public void setFloating(final boolean val) {
    floating = val;
  }

  /**
   * @return bool
   */
  public boolean getFloating() {
    return floating;
  }

  /**
   *
   * @param val If true we should store a UTC value only
   */
  public void setStoreUTC(final boolean val) {
    storeUTC = val;
  }

  /**
   * @return bool
   */
  public boolean getStoreUTC() {
    return storeUTC;
  }

  /** Sets this object's current date to now.
   *
   */
  public void setNow() {
    setDateTime(DateTimeUtil.isoDateTime(new Date(System.currentTimeMillis())));
  }

  /** Set this object's date.
   *
   * @param val   String date to use as yyyyMMdd or yyyyMMddThhmmss
   */
  public void setDateTime(final String val) {
      if (DateTimeUtil.isISODateTime(val)) {
        setDateOnly(false);
      } else if (DateTimeUtil.isISODate(val)) {
        setDateOnly(true);
      } else {
        throw new RuntimeException(CalFacadeException.badDate +
                                           ": " + val);
      }

      setTzid(null);
      storeUTC = false;
      floating = false;

      initTimeParts(val);
  }

  /** Sets this object's current time from the given BwDateTime value.
   *
   * @param val    BwDateTime value.
   */
  public void setDateTime(final BwDateTime val) {
    setTzid(val.getTzid());

    storeUTC = val.isUTC();
    floating = val.getFloating();
    setDateOnly(val.getDateType());

    initTimeParts(val.getDtval());
  }

  /** Get a date object representing the date of the event
   *
   * @return Date object representing the date
   */
  public BwDateTime getDateTime() {
    /*
    Calendar c = Calendar.getInstance(getCalInfo().getLocale());

    TimeZone tz = svci.getTimeZone(getTzid());

    c.setTimeZone(tz);
    c.set(Calendar.YEAR, getYear());
    c.set(Calendar.MONTH, getMonth() - 1);
    c.set(Calendar.DAY_OF_MONTH, getDay());
    if (getDateOnly()) {
      c.set(Calendar.HOUR_OF_DAY, 0);
      c.set(Calendar.MINUTE, 0);
    } else {
      int hr = hour;
      if (!hour24) {
        if (isAm) {
          if (hr == 12) {
            hr = 0;
          }
        } else {
          if (hr < 12) {
            hr += 12;
          }
        }
      }
      c.set(Calendar.HOUR_OF_DAY, hr);
      c.set(Calendar.MINUTE, getMinute());
    }
    */
    final StringBuilder sb = new StringBuilder();
    getFourDigit(sb, getYear());
    getTwoDigit(sb, getMonth());
    getTwoDigit(sb, getDay());

    if (!getDateOnly()) {
      sb.append("T");
      getTwoDigit(sb, get24Hourval());
      getTwoDigit(sb, getMinute());
      //getTwoDigit(sb, getSecond());
      sb.append("00");
    }

    if (storeUTC) {
      sb.append("Z");
      return BwDateTimeUtil.getDateTimeUTC(sb.toString());
    }

    return BwDateTimeUtil.getDateTime(sb.toString(),
                                      getDateOnly(),
                                      floating,
                                      tzid);
  }

  /**
   * @return hours in 24-hour mode
   */
  public int get24Hourval() {
    int hr = getHour();
    if (!hour24 && !isAm) {
      hr += 12;
    }

    return hr;
  }
  /**
   * @return String date time in rfc3339 format
   */
  public String getRfc3339DateTime() {
    final StringBuilder sb = new StringBuilder();
    getFourDigit(sb, getYear());
    sb.append("-");
    getTwoDigit(sb, getMonth());
    sb.append("-");
    getTwoDigit(sb, getDay());
    sb.append("T");

    if (getDateOnly()) {
      sb.append("00:00:00");
    } else {
      getTwoDigit(sb, get24Hourval());
      sb.append(":");
      getTwoDigit(sb, getMinute());
      sb.append(":");
      //getTwoDigit(sb, getSecond());
      sb.append("00");
    }

    return sb.toString();
  }

  /* These methods are probably the ones that matter - the set and get the
   * components that make up the date. Store them then use them to set the
   * calendar during the update action.
   */

  /** Set the year
   *
   * @param val   String 4 digit year yyyy
   */
  public void setYear(final int val) {
    year = val;
  }

  /**
   * @return int year
   */
  public int getYear() {
    return year;
  }

  /* XXX
     In the get methods below, we try to translate real values to the
     'values' in the arrays defined above (e.g., dayVals).
     We should also translate array values into real vals in the set methods.
     We don't.  The only reason this works is because currently we only
     use the default arrays, which map things in the obvious way (1 <-> 1).
     If one allows non-default arrays to be used (e.g., with Val arrays
     that contain non-numbers, the set methods would need to be changed.

     Note that the years are exempt here, as they are not selected by
     dropdown menus
   */
  /** Set the month number
   *
   * @param val   int month number 01-xx
   */
  public void setMonth(final int val) {
    // -1 below because MONTH starts at 0.
    month = val;
  }

  /**
   * @return int month 1-12
   */
  public int getMonth() {
    return month;
  }

  /** Set the day number
   *
   * @param val   int day number 01-xx
   */
  public void setDay(final int val) {
    day = val;
  }

  /**
   * @return int day
   */
  public int getDay() {
    // Calendar.DAY_OF_MONTH returns 1-31
    return day;
  }

  /**
   * Set the hour in the underlying calendar
   * @param val Hour of the day (0-23)
   */
  public void setHour(final int val) {
    hour = val;
  }

  /**
   * @return int hour
   */
  public int getHour() {
    return hour;
  }

  /** Set the minute. Will be rounded to minuteIncrement
   *
   * @param val   int minute
   */
  public void setMinute(final int val) {
    minute = validMinute(val);
  }

  /**
   * @return int minute
   */
  public int getMinute() {
    return minute;
  }

  /** Set the am/pm
   *
   * @param val   String am/pm
   */
  public void setAmpm(final String val) {
    // am/pm is set with the hour
    isAm = val.equals(ampmLabels[0]);
  }

  /**
   * @return String am/pm
   */
  public String getAmpm() {
    // Calendar.AM_PM returns 0-1
    if (isAm) {
      return ampmLabels[0];
    }
    return ampmLabels[1];
  }

  /* ====================================================================
   *                Private methods
   * ==================================================================== */

  private void initTimeParts(final String iso) {
    year = getInt(iso.substring(0, 4));
    month = getInt(iso.substring(4, 6));
    day = getInt(iso.substring(6, 8));
    if (getDateOnly()) {
      hour = 0;
      minute = 0;
    } else {
      hour = getInt(iso.substring(9, 11));
      minute = getInt(iso.substring(11, 13));
    }
    isAm = hour < 12;

    if (!hour24 && (hour > 11)) {
      hour -= 12;
    }
  }

  private int getInt(final String val) {
    return Integer.parseInt(val);
  }

  private int validMinute(final int val) {
    if (minuteIncrement > 1) {
      return ((val + 1) / minuteIncrement) * minuteIncrement;
    }

    return val;
  }

  /** Get a two-digit representation of a one to two-digit number
   *
   * @param sb         for result.
   * @param  i         int one or two digit number
   */
  private static void getTwoDigit(final StringBuilder sb, final int i) {
    if (i < 10) {
      sb.append("0");
    }

    sb.append(i);
  }

  /** Get a four-digit representation of a one to four-digit number
   *
   * @param sb         for result.
   * @param  i         int number
   */
  private static void getFourDigit(final StringBuilder sb, final int i) {
    if (i < 10) {
      sb.append("000");
    } else if (i < 100) {
      sb.append("00");
    } else if (i < 1000) {
      sb.append("0");
    }

    sb.append(i);
  }

  /*
    Set the minutes arrays to a subset of the values in two given arrays
    @param increment Choose the 0th entry in each array, and every
       minuteIncrement'th one after that
    @exception TimeDateException If either of the arrays given is not
       the proper length
   */
  private void setMinutes(final int increment) {
    minuteIncrement = Math.max(increment, 1);

    final String[] labels = getCalInfo().getMinuteLabels();
    final String[] vals = getCalInfo().getMinuteVals();

    if (minuteIncrement == 1) {
      minuteLabels = labels;
      minuteVals = vals;
      return;
    }

    final int sz = labels.length / minuteIncrement;

    minuteLabels = new String[sz];
    minuteVals = new String[sz];

    int i = 0;
    for (int j=0; j < labels.length; j += minuteIncrement) {
      minuteLabels[i] = labels[j];
      minuteVals[i] = vals[j];
      i++;
    }
  }

  /* ====================================================================
   *                        Object methods
   * ==================================================================== */

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder();

    sb.append("TimeDateComponents{");
    try {
      sb.append(getDateTime());
    } catch (final Throwable t) {
      sb.append("Exception ").append(t);
    }
    sb.append("}");

    return sb.toString();
  }
}

