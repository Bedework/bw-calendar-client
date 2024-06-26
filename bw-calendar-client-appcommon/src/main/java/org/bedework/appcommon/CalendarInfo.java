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

import org.bedework.calfacade.locale.BwLocale;

import java.io.Serializable;
import java.text.DateFormat;
import java.text.FieldPosition;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.TimeZone;

/** Class to hold localized calendar info.
 *
 * @author Mike Douglass   douglm     rpi.edu
 *  @version 1.0
 */
public class CalendarInfo implements Serializable {
  private static final HashMap<Locale, CalendarInfo> infoMap = new HashMap<>();

  /** Current locale
   */
  private Locale locale;

  private transient Formatters formatters;

  /** Days of the week indexed by day of the week number - 1
   */
  private final String[] dayNames = new String[7]; // indexed from 0
  private final String[] shortDayNames = new String[7]; // indexed from 0
  private final String[] recurDayNames; // indexed from 0

  /** Days of week adjusted with the first day of the week at index 0
   * This is used for presentation.
   */
  private final String[] dayNamesAdjusted; // indexed from 0
  private String[] shortDayNamesAdjusted; // indexed from 0
  private final String[] recurDayNamesAdjusted; // indexed from 0

  private final int firstDayOfWeek;
  private int lastDayOfWeek;
  private final int numberDaysInWeek;

  /** labels for the dates in a month */
  private final String[] dayLabels;
  /** internal values for the dates in a month */
  private final String[] dayVals;

  /** labels for the months of the year */
  private final String[] monthLabels;
  /** internal values for the months of the year */
  private final String[] monthVals;

  /** labels for the hours of the day */
  private final String[] hourLabels;
  /** internal values for the hours of the day */
  private final String[] hourVals;
  /** labels for the hours of the day (24-hour clock) */
  private final String[] hour24Labels;
  /** internal values for the hours of the day (24-hour clock) */
  private final String[] hour24Vals;

  /** labels for the minutes of the hour */
  private final String[] minuteLabels;
  /** internal values for the minutes of the hour */
  private final String[] minuteVals;

  private String[] yearVals;
  private static final int numYearVals = 10;

  /* What we store in the formtters table */
  private static class Formatters implements Serializable {
    DateFormat dayFormatter;
    DateFormat shortDayFormatter;

    Formatters(final Locale loc) {
      dayFormatter = new SimpleDateFormat("EEEE", loc);
      shortDayFormatter = new SimpleDateFormat("E", loc);
    }
  }

  /** Return an instance of CalendarInfo for the current locale set for this
   * thread.
   *
   * @return CalendarInfo
   */
  public static CalendarInfo getInstance() {
    return getInstance(BwLocale.getLocale());
  }

  /** Return an instance of CalendarInfo for the given locale.
   *
   * @param loc
   * @return CalendarInfo
   */
  public static CalendarInfo getInstance(final Locale loc) {
    CalendarInfo ci = infoMap.get(loc);

    if (ci != null) {
      return ci;
    }

    ci = new CalendarInfo(loc);
    infoMap.put(loc, ci);

    return ci;
  }

  /** Constructor
   *
   * @param locale
   */
  private CalendarInfo(final Locale locale) {
    this.locale = locale;

    /* Set the localized names
     */

//    SimpleDateFormat dayFormat = new SimpleDateFormat("EEEE", getLocale());
//    SimpleDateFormat shortDayFormat = new SimpleDateFormat("E", getLocale());

    final Calendar c = Calendar.getInstance(getLocale());
    final ArrayList<String> dow = new ArrayList<>();
    final ArrayList<String> sdow = new ArrayList<>();
    final ArrayList<String> rdow = new ArrayList<>();

    /* ********************* Day of Week ************************* */

    firstDayOfWeek = c.getFirstDayOfWeek();

    /* Get the number of days in a week. Is this ever anything other than 7?
     */
    numberDaysInWeek = getRangeSize(c, Calendar.DAY_OF_WEEK);

    lastDayOfWeek = firstDayOfWeek - 1;
    if (lastDayOfWeek < 1) {
      lastDayOfWeek += numberDaysInWeek;
    }

    recurDayNames = new String[]{"SU", "MO", "TU", "WE", "TH", "FR", "SA"};

    for (int i = 0; i < 7; i++) {
      c.set(Calendar.DAY_OF_WEEK, i + 1);

      final Date dt = c.getTime();
      dayNames[i] = getDayName(dt);
      shortDayNames[i] = getShortDayName(dt);
      dow.add(dayNames[i]);
      sdow.add(shortDayNames[i]);
      rdow.add(recurDayNames[i]);
    }

    if (firstDayOfWeek != 1) {
      /* Rotate the days of the week so that the first day is at the beginning
       */

      int fdow = firstDayOfWeek;
      while (fdow > 1) {
        String s = dow.remove(0);
        dow.add(s);

        s = sdow.remove(0);
        sdow.add(s);

        s = rdow.remove(0);
        rdow.add(s);

        fdow--;
      }

      dayNamesAdjusted = dow.toArray(new String[0]);
      shortDayNamesAdjusted = sdow.toArray(new String[0]);
      recurDayNamesAdjusted = rdow.toArray(new String[0]);
    } else {
      dayNamesAdjusted = dayNames;
      shortDayNamesAdjusted = shortDayNames;
      recurDayNamesAdjusted = recurDayNames;
    }

    /* ****************** Day of Month ***************************** */

    dayLabels = new String[getRangeSize(c, Calendar.DAY_OF_MONTH)];
    dayVals = new String[getRangeSize(c, Calendar.DAY_OF_MONTH)];

    int dom = c.getMinimum(Calendar.DAY_OF_MONTH);
    for (int i = 0; i < dayLabels.length; i++) {
      dayLabels[i] = String.valueOf(dom);
      dayVals[i] = String.valueOf(dom);//twoDigit(dom);
      dom++;
    }

    /* *********************** Months ***************************** */

    monthLabels = new String[getRangeSize(c, Calendar.MONTH)];
    monthVals = new String[getRangeSize(c, Calendar.MONTH)];

    c.set(Calendar.DAY_OF_MONTH, 1);
    c.set(Calendar.MONTH, c.getMinimum(Calendar.MONTH));
    c.getTime(); // force recompute

    for (int i = 0; i < monthLabels.length; i++) {
      // this gives abbreviated form of month name
      monthLabels[i] = getComponent(c, DateFormat.MONTH_FIELD,
                                    DateFormat.MEDIUM);
      /* Calendar class month numbers start at 0 */
      monthVals[i] = String.valueOf(c.get(Calendar.MONTH) + 1);//twoDigit(c.get(Calendar.MONTH) + 1);
      c.add(Calendar.MONTH, 1);
    }

    /* *********************** Hours ***************************** */

    /* Calendar.HOUR is 0 for 12 o'clock.  Skip 0, then add it to the end
    labeled as 12, but with value 0 */

    /*
    hourLabels = new String[getRangeSize(c, Calendar.HOUR)];
    hourVals = new String[getRangeSize(c, Calendar.HOUR)];

    int hour = c.getMinimum(Calendar.HOUR) + 1;

    for (int i = 0; i < hourLabels.length - 1; i++) {
      hourLabels[i] = String.valueOf(hour);
      hourVals[i] = String.valueOf(hour);//twoDigit(hour);
      hour++;
    }
    hourLabels[hourLabels.length - 1] = String.valueOf(hourLabels.length - 1);
    hourVals[hourLabels.length - 1] = twoDigit(0);
    */
    hourLabels = new String[]{"1", "2", "3", "4", "5", "6",
                              "7", "8", "9", "10", "11", "12"};
    hourVals = new String[]{"1", "2", "3", "4", "5", "6",
                            "7", "8", "9", "10", "11", "0"};

    hour24Labels = new String[getRangeSize(c, Calendar.HOUR_OF_DAY)];
    hour24Vals = new String[getRangeSize(c, Calendar.HOUR_OF_DAY)];

    int hourOfDay = c.getMinimum(Calendar.HOUR_OF_DAY);

    for (int i = 0; i < hour24Labels.length; i++) {
      hour24Labels[i] = twoDigit(hourOfDay);
      hour24Vals[i] = String.valueOf(hourOfDay);//twoDigit(hourOfDay);
      hourOfDay++;
    }

    /* *********************** Minutes ***************************** */

    minuteLabels = new String[getRangeSize(c, Calendar.MINUTE)];
    minuteVals = new String[getRangeSize(c, Calendar.MINUTE)];

    int minute = c.getMinimum(Calendar.MINUTE);

    for (int i=0; i < minuteLabels.length; i++) {
      minuteLabels[i] = twoDigit(minute);
      minuteVals[i] = String.valueOf(minute); //twoDigit(minute);
      minute++;
    }
  }

  /**
   * @param val Locale
   */
  public void setLocale(final Locale val) {
    locale = val;
  }

  /**
   * @return Locale
   */
  public Locale getLocale() {
    return locale;
  }

  /** Get the day name for the given day number (1-7).
   *
   * @param dow        int day of week value 1-7
   * @return String    day name
   */
  public String getDayName(final int dow) {
    return dayNames[dow - 1];
  }

  /** Get the names of the day of the week
   *
   * @return String[] day names
   */
  public String[] getDayNames() {
    return dayNames;
  }

  /** Get the short names of the day of the week
   *
   * @return String day names
   */
  public String[] getShortDayNames() {
    return shortDayNames;
  }

  /** Get the recur names of the day of the week
   *
   * @return String day names
   */
  public String[] getRecurDayNames() {
    return recurDayNames;
  }

  /** Get the names of the day of the week
   * Elements have been adjusted so that the first day of the week is at 0
   *
   * @return String[] day names
   */
  public String[] getDayNamesAdjusted() {
    return dayNamesAdjusted;
  }

  /** Get the short names of the day of the week
   * Elements have been adjusted so that the first day of the week is at 0
   *
   * @return String day names
   */
  public String[] getShortDayNamesAdjusted() {
    return shortDayNamesAdjusted;
  }

  /** Get the recur names of the day of the week
   *
   * @return String day names
   */
  public String[] getRecurDayNamesAdjusted() {
    return recurDayNamesAdjusted;
  }

  /** Return the dayOfWeek regarded as the first day
   *
   * @return int   firstDayOfWeek from 1 - 7
   */
  public int getFirstDayOfWeek() {
    return firstDayOfWeek;
  }

  /** Return the dayOfWeek regarded as the last day
   *
   * @return int   lastDayOfWeek from 1 - 7
   */
  public int getLastDayOfWeek() {
    return lastDayOfWeek;
  }

  /**
   * @return labels
   */
  public String[] getDayLabels() {
    return dayLabels;
  }

  /**
   * @return vals
   */
  public String[] getDayVals() {
    return dayVals;
  }

  /**
   * @return labels
   */
  public String[] getMonthLabels() {
    return monthLabels;
  }

  /**
   * @return vals
   */
  public String[] getMonthVals() {
    return monthVals;
  }

  /**
   * @return labels
   */
  public String[] getHourLabels() {
    return hourLabels;
  }

  /**
   * @return vals
   */
  public String[] getHourVals() {
    return hourVals;
  }

  /**
   * @return labels
   */
  public String[] getHour24Labels() {
    return hour24Labels;
  }

  /**
   * @return vals
   */
  public String[] getHour24Vals() {
    return hour24Vals;
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

  /**  Get the first day of the week for this date
   *
   * @param tz
   * @param jdt   Date
   * @return Calendar    representing the first day of the week for this date
   */
  public Calendar getFirstDayOfThisWeek(final TimeZone tz,
                                        final Date jdt) {
    final Calendar c = getCalendar(tz);

    c.setTime(jdt);
    c.set(Calendar.DAY_OF_WEEK, getFirstDayOfWeek());
    c.set(Calendar.HOUR_OF_DAY, 0);
    c.set(Calendar.MINUTE, 0);
    c.set(Calendar.SECOND, 0);
    c.set(Calendar.MILLISECOND, 0);

    return c;
  }

  /** Get the last day of the week for this object
   *
   * @param tz
   * @param jdt   Date
   * @return Calendar    representing the last day of the week for this
   *             object.
   */
  public Calendar getLastDayOfThisWeek(final TimeZone tz,
                                       final Date jdt) {
    final Calendar c = getCalendar(tz);

    c.setTime(jdt);
    c.set(Calendar.DAY_OF_WEEK, getFirstDayOfWeek());
    c.add(Calendar.WEEK_OF_YEAR, 1);
    c.add(Calendar.DATE, -1);
    c.set(Calendar.HOUR_OF_DAY, 0);
    c.set(Calendar.MINUTE, 0);
    c.set(Calendar.SECOND, 0);
    c.set(Calendar.MILLISECOND, 0);

    return c;
  }

  /**  Get the first day of the month for this object
   *
   * @param tz
   * @param jdt   Date
   * @return Calendar    representing the first day of the month for this
   *             date, e.g. if this object represents February 5, 2000,
   *             returns a MyCalendarVO object representing February 1, 2000.
   */
  public Calendar getFirstDayOfThisMonth(final TimeZone tz,
                                         final Date jdt) {
    final Calendar c = getCalendar(tz);

    c.setTime(jdt);
    c.set(Calendar.DAY_OF_MONTH,
                 c.getMinimum(Calendar.DAY_OF_MONTH));
    c.set(Calendar.HOUR_OF_DAY, 0);
    c.set(Calendar.MINUTE, 0);
    c.set(Calendar.SECOND, 0);
    c.set(Calendar.MILLISECOND, 0);

    return c;
  }

  /** Get the last day of the month for this object
   *
   * @param tz
   * @param jdt   Date
   * @return Calendar    representing the last day of the month for this
   *             object, e.g. if this object represents February 5, 2000,
   *             returns a MyCalendarVO object representing February 29, 2000.
   */
  public Calendar getLastDayOfThisMonth(final TimeZone tz,
                                        final Date jdt) {
    final Calendar c = getCalendar(tz);

    c.setTime(jdt);
    c.set(Calendar.DAY_OF_MONTH, c.getMinimum(Calendar.DAY_OF_MONTH));
    c.add(Calendar.MONTH, 1);
    c.add(Calendar.DATE, -1);
    c.set(Calendar.HOUR_OF_DAY, 0);
    c.set(Calendar.MINUTE, 0);
    c.set(Calendar.SECOND, 0);
    c.set(Calendar.MILLISECOND, 0);

    return c;
  }

  /** Get the first day of the year for this object
   *
   * @param tz
   * @param jdt   Date
   * @return Calendar    representing the first day of the year for this
   *             object, e.g. if this object represents February 5, 2000,
   *             returns a MyCalendarVO object representing January 1, 2000.
   */
  public Calendar getFirstDayOfThisYear(final TimeZone tz,
                                        final Date jdt) {
    final Calendar c = getCalendar(tz);

    c.setTime(jdt);
    c.set(Calendar.DAY_OF_YEAR, c.getMinimum(Calendar.DAY_OF_YEAR));
    c.set(Calendar.HOUR_OF_DAY, 0);
    c.set(Calendar.MINUTE, 0);
    c.set(Calendar.SECOND, 0);
    c.set(Calendar.MILLISECOND, 0);

    return c;
  }

  /** Get the last day of the year for this object
   *
   * @param tz
   * @param jdt   Date
   * @return Calendar    epresenting the last day of the year for this
   *             object, e.g. if this object represents February 5, 2000,
   *             returns a MyCalendarVO object representing December 31, 2000.
   */
  public Calendar getLastDayOfThisYear(final TimeZone tz,
                                       final Date jdt) {
    final Calendar c = getCalendar(tz);

    c.setTime(jdt);
    c.set(Calendar.DAY_OF_YEAR, c.getMinimum(Calendar.DAY_OF_YEAR));
    c.add(Calendar.YEAR, 1);
    c.add(Calendar.DATE, -1);
    c.set(Calendar.HOUR_OF_DAY, 0);
    c.set(Calendar.MINUTE, 0);
    c.set(Calendar.SECOND, 0);
    c.set(Calendar.MILLISECOND, 0);

    return c;
  }

  /**
   * @return year values
   */
  public String[] getYearVals() {
    if (yearVals == null) {
      yearVals = new String[numYearVals];
      final int year = java.util.Calendar.getInstance()
                                         .get(java.util.Calendar.YEAR);
      //curYear = String.valueOf(year);

      for (int i = 0; i < numYearVals; i++) {
        yearVals[i] = String.valueOf(year + i);
      }
    }

    return yearVals;
  }

  /* ====================================================================
   *                Private methods
   * ==================================================================== */

  private Calendar getCalendar(final TimeZone tz) {
    return Calendar.getInstance(tz, getLocale());
  }

  /* Get a String representation of a particular time field of the object.
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
  private String getComponent(final Calendar cal,
                              final int field,
                              final int dateFormat) {
    final FieldPosition f = new FieldPosition(field);
    final StringBuffer s = DateFormat.
        getDateTimeInstance(dateFormat, dateFormat, getLocale()).
        format(cal.getTime(), new StringBuffer(), f);

    return s.substring(f.getBeginIndex(), f.getEndIndex());
  }

  /* Return the size of the range for a given unit of time
   *
   * @param unit      value defined in java.util.Calendar
   * @return int      size of range
   */
  private int getRangeSize(final Calendar cal, final int unit) {
    return (cal.getMaximum(unit) - cal.getMinimum(unit)) + 1;
  }

  private static String twoDigit(final int val) {
    if (val > 9) {
      return String.valueOf(val);
    }

    return "0" + String.valueOf(val);
  }

  /**  Get a short String representation of the day represented by the parameter
   *
   * @param  val           Date
   * @return String        Short representation of the day
   *                       represented by this object.
   */
  private String getShortDayName(final Date val) {
    final Formatters fmt = getFormatter();

    synchronized (fmt) {
      return fmt.shortDayFormatter.format(val);
    }
  }

  /**  Get a String representation of the day represented by the parameter
   *
   * @param  val           Date
   * @return String        Representation of the day represented by this object.
   */
  private String getDayName(final Date val) {
    final Formatters fmt = getFormatter();

    synchronized (fmt) {
      return fmt.dayFormatter.format(val);
    }
  }

  private Formatters getFormatter() {
    if (formatters == null) {
      formatters = new Formatters(getLocale());
    }

    return formatters;
  }
}
