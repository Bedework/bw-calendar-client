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
import org.bedework.appcommon.client.IcalCallbackcb;
import org.bedework.caldav.util.filter.FilterBase;
import org.bedework.calfacade.BwDateTime;
import org.bedework.calfacade.BwEvent;
import org.bedework.calfacade.exc.CalFacadeException;
import org.bedework.calfacade.indexing.IndexKeys;
import org.bedework.calfacade.indexing.SearchResultEntry;
import org.bedework.calfacade.locale.BwLocale;
import org.bedework.calfacade.svc.EventInfo;
import org.bedework.calfacade.util.BwDateTimeUtil;
import org.bedework.icalendar.IcalTranslator;
import org.bedework.util.calendar.IcalDefs;
import org.bedework.util.servlet.MessageEmit;
import org.bedework.util.timezones.DateTimeUtil;
import org.bedework.util.timezones.Timezones;
import org.bedework.util.timezones.TimezonesException;

import org.apache.log4j.Logger;

import java.io.Serializable;
import java.text.DateFormat;
import java.text.FieldPosition;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/** This class represents a view of the calendar from a startDate to an
 * endDate. The getTimePeriodInfo method always returns a tree structure
 * with a single year as the root containing months, weeks, then days.
 *
 * <p>Each day element can return data for that day.
 *
 * <p>This structure facilitates the production of calendar like views as
 * each month and week is padded out at the start and end with filler
 * elements.
 *
 * <p>The calendar is represented as a sequence of, possibly overlapping,
 * events which must be rendered in some manner by the display.
 *
 * @author  Mike Douglass douglm  rpi.edu
 */
public class TimeView implements Serializable {
  protected boolean debug;

  private MessageEmit err;

  protected IcalTranslator trans;
  protected String periodName;
  protected int viewPeriod;
  protected Calendar firstDay;
  protected Calendar lastDay;
  protected String prevDate;
  protected String nextDate;
  protected boolean showData;

  private static DateFormat fullDf = DateFormat.getDateInstance(DateFormat.FULL);

  private static DateFormat shortDf = DateFormat.getDateInstance(DateFormat.SHORT);

  private IndexKeys keys = new IndexKeys();

  protected BwDateTime viewStart;
  protected BwDateTime viewEnd;

  /* Fetched when required
   */
  protected Map<String, EventFormatter> events;

  /** set on the first call to getTimePeriodInfo
   */
  private TimeViewDailyInfo[] tvdis;

  private CalFmt curDayFmt;
  private CalFmt firstDayFmt;
  private CalFmt lastDayFmt;

  private FilterBase filter;

  /**
   *
   */
  public static class CalFmt implements Serializable {
    Calendar cal;

    CalFmt(final Calendar cal) {
      this.cal = cal;
    }

    /**  Get an ISO String representation of the date/time
     *
     * @return String        ISO representation of the date/time
     *            represented by this object.
     */
    public String getDateTimeString() {
      return DateTimeUtil.isoDateTime(getTime());
    }

    /**  Get date digits
     *
     * @return String
     */
    public String getDateDigits() {
      return DateTimeUtil.isoDate(getTime());
    }

    /**  Get full date
     *
     * @return String
     */
    public String getFullDateString() {
      synchronized (fullDf) {
        try {
          fullDf.setTimeZone(Timezones.getDefaultTz());
        } catch (TimezonesException tze) {
          throw new RuntimeException(tze);
        }
        return fullDf.format(getTime());
      }
    }

    /**  Get date
     *
     * @return String
     */
    public String getShortDateString() {
      synchronized (shortDf) {
        try {
          shortDf.setTimeZone(Timezones.getDefaultTz());
        } catch (TimezonesException tze) {
          throw new RuntimeException(tze);
        }
        return shortDf.format(getTime());
      }
    }

    /**  Get month name
     *
     * @return String
     */
    public String getMonthName() {
      FieldPosition f = new FieldPosition(DateFormat.MONTH_FIELD);

      DateFormat df = DateFormat.
          getDateTimeInstance(DateFormat.LONG,
                              DateFormat.LONG,
                              BwLocale.getLocale());
      try {
        df.setTimeZone(Timezones.getDefaultTz());
      } catch (Throwable t) {
        throw new RuntimeException(t);
      }

      StringBuffer s = df.format(getTime(), new StringBuffer(), f);

      return s.substring(f.getBeginIndex(), f.getEndIndex());
    }

    /**
     * @return Date
     */
    public Date getTime() {
      return cal.getTime();
    }

    /* Unused?
    private String getComponent(final int field, final int dateFormat) {
      FieldPosition f = new FieldPosition(field);
      StringBuffer s = DateFormat.
          getDateTimeInstance(dateFormat, dateFormat, BwLocale.getLocale()).
          format(getTime(), new StringBuffer(), f);
      return s.substring(f.getBeginIndex(), f.getEndIndex());
    }*/
  }

  //private static final TimezoneCache tzcache = new TimezoneCache(false);

  /** Constructor:
   *
   * @param  err - for error messages
   * @param  curDay    MyCalendarVO representing current day.
   * @param  periodName Name of period, capitalized, e.g. Week
   * @param  firstDay  Calendar representing first day of period.
   * @param  lastDay   Calendar representing last day of period.
   * @param  prevDate  previous date for this time period in YYYYMMDD form
   * @param  nextDate  next date for this time period in YYYYMMDD form
   * @param  showData  boolean true if this TimeView can be used to
   *                   display events or if it is used for structure only.
   *                   For example we may use the year for navigation only
   *                   to reduce the amount of data retrieved.
   * @param  filter    non-null to filter the results.
   * @throws CalFacadeException
   */
  public TimeView(final MessageEmit err,
                  final Calendar curDay,
                  final String periodName,
                  final Calendar firstDay,
                  final Calendar lastDay,
                  final String prevDate,
                  final String nextDate,
                  final boolean showData,
                  final FilterBase filter) throws CalFacadeException {
    this.err = err;

    curDayFmt = new CalFmt(curDay);
    firstDayFmt = new CalFmt(firstDay);
    lastDayFmt = new CalFmt(lastDay);

    this.periodName = periodName;
    this.firstDay = firstDay;
    this.lastDay = lastDay;
    this.prevDate = prevDate;
    this.nextDate = nextDate;
    this.showData = showData;
    this.filter = filter;
    debug = Logger.getLogger(this.getClass()).isDebugEnabled();

    viewStart = getBwDate(firstDay);
    viewEnd = getBwDate(lastDay).getNextDay();
  }

  /**
   * @return CalendarInfo
   */
  public CalendarInfo getCalInfo() {
    return CalendarInfo.getInstance();
  }

  /**
   * @param cl
   * @return translator for ical
   */
  public IcalTranslator getTrans(Client cl) {
    if (trans == null) {
      trans = new IcalTranslator(new IcalCallbackcb(cl));
    }

    return trans;
  }

  public BwDateTime getViewStart() {
    return viewStart;
  }

  public BwDateTime getViewEnd() {
    return viewEnd;
  }

  /** Override this for a single day view
   *
   * @return true for multi-day
   */
  public boolean isMultiDay() {
    return true;
  }

  /** This method returns the period name (week, month etc)
   *
   * @return  String  period name
   */
  public String getPeriodName() {
    return periodName;
  }

  /** This method returns the view period as defined in BedeworkDefs
   *
   * @return  int  view period
   */
  public int getViewPeriod() {
    return viewPeriod;
  }

  /** This method returns the view period as defined in BedeworkDefs
   *
   * @return  String  view type "monthView" etc
   */
  public String getViewType() {
    if (viewPeriod < 0) {
      return null;
    }

    try {
      return BedeworkDefs.viewPeriodNames[viewPeriod];
    } catch (final Throwable ignored) {
      return null;
    }
  }

  /**
   * @return Calendar first day
   */
  public Calendar getFirstDay() {
    return firstDay;
  }

  /**
   * @return CalFmt first day formatter
   */
  public CalFmt getFirstDayFmt() {
    return firstDayFmt;
  }

  /**
   * @return Calendar last day
   */
  public Calendar getLastDay() {
    return lastDay;
  }

  /**
   * @return CalFmt last day formatter
   */
  public CalFmt getLastDayFmt() {
    return lastDayFmt;
  }

  /**
   * @return CalFmt current day formatter
   */
  public CalFmt getCurDayFmt() {
    return curDayFmt;
  }

  /** This method returns the previous date in YYYYMMDD format.
   *
   * @return  String  previous date
   */
  public String getPrevDate() {
    return prevDate;
  }

  /** This method returns the next date in YYYYMMDD format.
   *
   * @return  String  next date
   */
  public String getNextDate() {
    return nextDate;
  }

  /** Return showData flag.
   *
   * @return  boolean  true if we should show data
   */
  public boolean getShowData() {
    return showData;
  }

  /** Called to force a refresh of the events. Call ONCE only per request.
   */
  public void refreshEvents() {
    events = null;
    tvdis = null;
  }

  /** Return the events for the given day as an array of value objects
   *
   * @param   date    MyCalendar object defining day
   * @return  Collection of EventFormatter being one days events or empty for no events.
   * @throws Throwable
   */
  public Collection<EventFormatter> getDaysEvents(final MyCalendarVO date) throws Throwable {
    ArrayList<EventFormatter> al = new ArrayList<>();

    BwDateTime startDt = getBwDate(date.getDateDigits());
    BwDateTime endDt = startDt.getNextDay();

    // UTC times
    String start = startDt.getDate();
    String end = endDt.getDate();

    // local times - for floating check
    //String startLocal = startDt.getDtval();
    //String endLocal = endDt.getDtval();

    //if (debug) {
    //  debug("Did UTC stuff in " + (System.currentTimeMillis() - millis));
    //}

    /* The following looks for events in the given day. There is a special case
     * of todos with no start date. These should only appear in the current day,
     * i.e today
     */
    boolean today = date.isToday();

    for (EventFormatter ef: events.values()) {
      EventInfo ei = ef.getEventInfo();
      BwEvent ev = ei.getEvent();

      if ((ev.getEntityType() == IcalDefs.entityTypeTodo)  &&
           ev.getNoStart()) {
        if (today) {
          al.add(ef);
        }

        continue;
      }

      if (ev.inDateTimeRange(start, end)) {
        al.add(ef);
      }
    }

    /*
      //boolean floating = ev.getDtstart().getFloating();

      String evStart;
      String evEnd;

//      if (floating) {
  //      evStart = ev.getDtstart().getDtval();
    //    evEnd = ev.getDtend().getDtval();
      //} else {
        evStart = ev.getDtstart().getDate();
        evEnd = ev.getDtend().getDate();
//      }

      /* Event is within range if:
         1.   (((evStart <= :start) and (evEnd > :start)) or
         2.    ((evStart >= :start) and (evStart < :end)) or
         3.    ((evEnd > :start) and (evEnd <= :end)))

         XXX followed caldav which might be wrong. Try
         3.    ((evEnd > :start) and (evEnd < :end)))

             ((evstart < end) and ((evend > start) or
                 ((evstart = evend) and (evend >= start))))
      * /

      int evstSt;

  //    if (floating) {
    //    evstSt = evStart.compareTo(endLocal);
      //} else {
        evstSt = evStart.compareTo(end);
//      }

      if (evstSt >= 0) {
        // Start is past the (exclusive) end
        continue;
      }

      int evendSt;

//      if (floating) {
  //      evendSt = evEnd.compareTo(startLocal);
    //  } else {
        evendSt = evEnd.compareTo(start);
      //}

      if ((evendSt > 0) ||
          (evStart.equals(evEnd) && (evendSt >= 0))) {
        // Passed the tests.

        /*
        if (debug) {
          debug("Event (floating=" + floating + ") passed range " +
              start + "(" + startLocal + ")-" +
              end + "(" + endLocal +
              ") with dates " + evStart + "-" + evEnd +
              ": " + ev.getSummary());
        }
        * /
        al.add(ef);
      }
    }*/

    return al;
  }

  /** Return an array of the days of the week indexed from 0
   * Elements have been adjusted so that the first day of the week is at 0
   *
   * @return String[]  days of week
   */
  public String[] getDayNamesAdjusted() {
    return getCalInfo().getDayNamesAdjusted();
  }

  /** Return an array of the short days of the week indexed from 0
   * Elements have been adjusted so that the first day of the week is at 0
   *
   * @return String[]  days of week
   */
  public String[] getShortDayNamesAdjusted() {
    return getCalInfo().getShortDayNamesAdjusted();
  }

  /** Return the dayOfWeek regarded as the first day
   *
   * @return int   firstDayOfWeek
   */
  public int getFirstDayOfWeek() {
    return getCalInfo().getFirstDayOfWeek();
  }

  /** Class to hold data needed while we build this thing
   */
  static class GtpiData {
    /** True if we've done the last entry
     */
    boolean isLast;

    /** True if we're doing the first entry
     */
    boolean isFirst = true;

    /** True if we're starting a new month
     */
    boolean newMonth = true;

    MyCalendarVO currentDay;

    //MyCalendarVO first;
    Calendar last;
    boolean multi;

    String monthName = null;

    String shortMonthName = null;

    /** Cuurent month we are processing
     */
    int curMonth;

    /** todays month
     */
    String todaysMonth;

    /** True if we are in the current month */
    boolean inThisMonth = false;

    String year;

    int weekOfYear = 1;

    /** Need this so we can flag end of month
     */
    TimeViewDailyInfo prevTvdi;
  }

  /** Return an array of TimeViewDailyInfo describing the period this
   * view covers. This will not include events
   *
   * @return TimeViewDailyInfo[]  array of info - one entry per day
   */
  public TimeViewDailyInfo[] getTimePeriodInfo() {
    /* In the following code we assume that we never cross year boundaries
      so that the year is always constant.
     */
    if (tvdis != null) {
      return tvdis;
    }

    try {
      GtpiData gtpi = new GtpiData();

      ArrayList<TimeViewDailyInfo> months = new ArrayList<TimeViewDailyInfo>();
      ArrayList<TimeViewDailyInfo> weeks = new ArrayList<TimeViewDailyInfo>();

      //gtpi.first = getFirstDay();
      gtpi.last = getLastDay();
      gtpi.multi = !gtpi.last.equals(getFirstDay());
      gtpi.currentDay = new MyCalendarVO(getFirstDay().getTime());
      gtpi.year = String.valueOf(gtpi.currentDay.getYear());

      gtpi.todaysMonth = new MyCalendarVO(   // XXX Expensive??
                   new Date(System.currentTimeMillis())).getTwoDigitMonth();

      if (debug) {
        debugMsg("getFirstDayOfWeek() = " + getFirstDayOfWeek());
        debugMsg("gtpi.first.getFirstDayOfWeek() = " +
                 getCalInfo().getFirstDayOfWeek());
      }

      initGtpiForMonth(gtpi);

      /* Our month entry */
      TimeViewDailyInfo monthTvdi = new TimeViewDailyInfo(err);
      initTvdi(monthTvdi, gtpi);

      /* Create a year entry */
      TimeViewDailyInfo yearTvdi = new TimeViewDailyInfo(err);
      yearTvdi.setCal(gtpi.currentDay);
      yearTvdi.setYear(gtpi.year);
      yearTvdi.setDate(gtpi.currentDay.getDateDigits());
      yearTvdi.setDateShort(gtpi.currentDay.getDateString());
      yearTvdi.setDateLong(gtpi.currentDay.getLongDateString());

      for (;;) {
        TimeViewDailyInfo weekTvdi = new TimeViewDailyInfo(err);

        initTvdi(weekTvdi, gtpi);

        weekTvdi.setEntries(getOneWeekTvdi(gtpi));
        weeks.add(weekTvdi);

        if (getFirstDayOfWeek() == gtpi.currentDay.getDayOfWeek()) {
          gtpi.weekOfYear++;
        }

        if (gtpi.isLast || gtpi.newMonth) {
          /** First add all the weeks to this month
           */

          if (gtpi.prevTvdi != null) {
            gtpi.prevTvdi.setLastDayOfMonth(true);
          }

          monthTvdi.setEntries(
             weeks.toArray(new TimeViewDailyInfo[
                  weeks.size()]));
          months.add(monthTvdi);

          if (gtpi.isLast) {
            break;
          }

          /** Set up for a new month.
           */

          initGtpiForMonth(gtpi);

          monthTvdi = new TimeViewDailyInfo(err);
          initTvdi(monthTvdi, gtpi);
          weeks = new ArrayList<TimeViewDailyInfo>();
        }
      }

      yearTvdi.setEntries(
              months.toArray(new TimeViewDailyInfo[months.size()]));

      tvdis = new TimeViewDailyInfo[1];
      tvdis[0] = yearTvdi;

      return tvdis;
    } catch (Throwable t) {
      Logger.getLogger(this.getClass()).error("getTimePeriodInfo", t);
      //XXX We need an error object

      return null;
    }
  }

  /** Ensure we have the current set of events.
   *
   * @param cl - the client
   * @param refresh if true will get a new set.
   * @throws CalFacadeException
   */
  public void getEvents(final Client cl,
                        final boolean refresh) throws CalFacadeException {
    if (!refresh && (events != null)) {
      return;
    }

    /*
    long curTime = System.currentTimeMillis();

    Calendar lastP1 = (Calendar)lastDay.clone();
    lastP1.add(Calendar.DATE, 1);

    BwDateTime start = getBwDate(firstDayFmt.getDateDigits());
    BwDateTime end = getBwDate(DateTimeUtil.isoDate(lastP1.getTime()));
    */

    final Collection<SearchResultEntry> sres = cl.getSearchResult(0, -1);
    events = new HashMap<>(sres.size());

    for (SearchResultEntry sre: sres) {
      if (sre.getEntity() instanceof EventFormatter) {
        EventFormatter ef = (EventFormatter)sre.getEntity();

        events.put(makeKey(ef.getEvent()), ef);
      }
    }
  }

  /** Update an event in our cache so it appears in the users display
   *
   * @param cl
   * @param ei
   * @throws CalFacadeException
   */
  public void putEvent(final Client cl,
                       final EventInfo ei) throws CalFacadeException {
    EventFormatter ef = new EventFormatter(cl,
                                           new IcalTranslator(new IcalCallbackcb(cl)),
                                           ei);

    events.put(makeKey(ef.getEvent()), ef);
  }

  private String makeKey(final BwEvent ev) {
    return keys.makeKeyVal("event",
                           ev.getHref(),
                           ev.getRecurrenceId());
  }

  private BwDateTime getBwDate(final Calendar date) throws CalFacadeException {
    final String dateStr = new MyCalendarVO(date.getTime()).getDateDigits();

    return getBwDate(dateStr);
  }

  private BwDateTime getBwDate(final String date) throws CalFacadeException {
    return BwDateTimeUtil.getDateTime(date,
                                      true,
                                      false,
                                      null);   // tzid
  }

  private void initGtpiForMonth(final GtpiData gtpi) {
    gtpi.curMonth = gtpi.currentDay.getMonth();
    gtpi.monthName = gtpi.currentDay.getMonthName();
    gtpi.shortMonthName = gtpi.currentDay.getShortMonthName();
    gtpi.inThisMonth = gtpi.todaysMonth.equals(gtpi.currentDay.getTwoDigitMonth());
  }

  private void initTvdi(final TimeViewDailyInfo tvdi, final GtpiData gtpi) {
    tvdi.setView(this);
    tvdi.setCal(gtpi.currentDay);
    tvdi.setMultiDay(gtpi.multi);
    tvdi.setMonth(gtpi.currentDay.getTwoDigitMonth());
    tvdi.setShortMonthName(gtpi.shortMonthName);
    tvdi.setMonthName(gtpi.monthName);
    tvdi.setYear(gtpi.year);
    tvdi.setDate(gtpi.currentDay.getDateDigits());
    tvdi.setDateShort(gtpi.currentDay.getDateString());
    tvdi.setDateLong(gtpi.currentDay.getLongDateString());
    tvdi.setCurrentMonth(gtpi.inThisMonth);
    tvdi.setWeekOfYear("" + gtpi.weekOfYear);
  }

  /** Build up to one weeks worth of days info. We assume that at least one
   * day will go into the current week. We exit at the end of the week, the
   * end of the month or the end of the time period.
   *
   * @param gtpi     GtpiData object supplying many parameters
   * @return TimeViewDailyInfo[] array of entries for this week.
   * @throws Throwable
   */
  private TimeViewDailyInfo[] getOneWeekTvdi(final GtpiData gtpi) throws Throwable {
    ArrayList<TimeViewDailyInfo> days = new ArrayList<TimeViewDailyInfo>();
    TimeViewDailyInfo tvdi;

    /** First see if we need to insert leading fillers */
    int dayOfWeek = gtpi.currentDay.getDayOfWeek();
    int dayNum = getFirstDayOfWeek();

    if (debug) {
      debugMsg("dayOfWeek=" + dayOfWeek + " dayNum = " + dayNum);
    }

    while (dayNum != dayOfWeek) {
      tvdi = new TimeViewDailyInfo(err);
      tvdi.setFiller(true);

      days.add(tvdi);
      dayNum++;

      if (debug) {
        debugMsg("dayNum = " + dayNum);
      }

      if (dayNum > 7) {
        dayNum = 1;
      }

      // Check we got this right
      if (days.size() > 7) {
        throw new Exception("Programming error in getOneWeekTvdi");
      }
    }

    for (;;) {
      dayOfWeek = gtpi.currentDay.getDayOfWeek();

      if (gtpi.currentDay.getMonth() != gtpi.curMonth) {
        gtpi.newMonth = true;
        break;
      }

//    gtpi.isLast = gtpi.last.equals(gtpi.currentDay.getCalendar());
      gtpi.isLast = gtpi.last.getTimeInMillis() <= gtpi.currentDay.getCalendar().getTimeInMillis();

      /* Create a day entry */
      tvdi = new TimeViewDailyInfo(err);

      initTvdi(tvdi, gtpi);

      tvdi.setDayEntry(true);

      tvdi.setFirstDay(gtpi.isFirst);
      tvdi.setLastDay(gtpi.isLast);
      tvdi.setDayOfMonth(gtpi.currentDay.getDay());
      tvdi.setDayOfWeek(dayOfWeek);

      /** Is this correct? The days of the week are rotated to adjust for
       *   first day differences. */
      tvdi.setDayName(getCalInfo().getDayName(dayOfWeek));
      tvdi.setFirstDayOfMonth(gtpi.newMonth);
      gtpi.newMonth = false;

      tvdi.setFirstDayOfWeek(getFirstDayOfWeek() == dayOfWeek);
      tvdi.setLastDayOfWeek(getCalInfo().getLastDayOfWeek() == dayOfWeek);

      days.add(tvdi);
      gtpi.isFirst = false;

      gtpi.prevTvdi = tvdi;

      gtpi.currentDay = gtpi.currentDay.getTomorrow();

      if (gtpi.isLast || tvdi.isLastDayOfWeek()) {
        // Watch for it also being the last day of the month
        if (gtpi.currentDay.getMonth() != gtpi.curMonth) {
          gtpi.newMonth = true;
        }

        break;
      }
    }

    /** Pad it out to seven days
     */
    while (days.size() < 7) {
      tvdi = new TimeViewDailyInfo(err);
      tvdi.setFiller(true);

      days.add(tvdi);
    }

    return days.toArray(new TimeViewDailyInfo[
                    days.size()]);
  }

  /**
   * @param msg
   */
  public void debugMsg(final String msg) {
    Logger.getLogger(this.getClass()).debug(msg);
  }

  @Override
  public String toString() {
    StringBuffer sb = new StringBuffer();

    sb.append("TimeView{");
    sb.append(periodName);
    sb.append(", firstDay=");
    sb.append(String.valueOf(firstDay));
    sb.append(", lastDay=");
    sb.append(String.valueOf(lastDay));
    sb.append("}");

    return sb.toString();
  }
}

