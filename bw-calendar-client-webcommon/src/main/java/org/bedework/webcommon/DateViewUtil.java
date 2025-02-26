/* ********************************************************************
    Appropriate copyright notice
*/
package org.bedework.webcommon;

import org.bedework.appcommon.BedeworkDefs;
import org.bedework.appcommon.CalendarFormatter;
import org.bedework.appcommon.TimeView;
import org.bedework.calfacade.exc.ValidationError;
import org.bedework.calfacade.util.BwDateTimeUtil;
import org.bedework.util.timezones.DateTimeUtil;

import java.util.Calendar;
import java.util.Date;

/**
 * User: mike Date: 8/5/24 Time: 23:17
 */
public class DateViewUtil {
  /** Set the current date and/or view. The date may be null indicating we
   * should switch to a new view based on the current date.
   *
   * <p>newViewTypeI may be less than 0 indicating we stay with the current
   * view but switch to a new date.
   *
   * @param request       action form
   * @param date         String yyyymmdd date or null
   * @param newViewType  requested new view or null
   */
  public static void gotoDateView(final BwRequest request,
                                  final String date,
                                  String newViewType) {
    final BwModuleState mstate = request.getModule().getState();

    /* We get a new view if either the date changed or the view changed.
     */
    boolean newView = false;

    CalendarFormatter dt;

    if (BedeworkDefs.vtToday.equals(newViewType)) {
      final Date jdt = new Date(System.currentTimeMillis());
      dt = new CalendarFormatter(jdt);
      newView = true;
      newViewType = BedeworkDefs.vtDay;
    } else if (date == null) {
      if (BedeworkDefs.vtDay.equals(newViewType)) {
        // selected specific day to display from personal event entry screen.

        final Date jdt = BwDateTimeUtil.getDate(mstate.getViewStartDate().getDateTime());
        dt = new CalendarFormatter(jdt);
        newView = true;
      } else {
        // Just stay here
        dt = mstate.getViewMcDate();
        if (dt == null) {
          // Just in case
          dt = new CalendarFormatter(new Date(System.currentTimeMillis()));
        }
      }
    } else {
      Date jdt = DateTimeUtil.fromISODate(date);
      dt = new CalendarFormatter(jdt);
      if (!checkDateInRange(request, dt.getYear())) {
        // Set it to today
        jdt = new Date(System.currentTimeMillis());
        dt = new CalendarFormatter(jdt);
      }
      newView = true;
    }

    if (!newView) {
      if (mstate.getCurTimeView() == null) {
        newView = true;
      } else if ((newViewType != null) &&
              !newViewType.equals(mstate.getCurTimeView()
                                        .getViewType())) {
        // Change of view
        newView = true;
      }
    }

    // Need to set default view?
    if (newView && (newViewType == null)) {
      newViewType = mstate.getViewType();
      if (newViewType == null) {
        newViewType = BedeworkDefs.viewPeriodNames[BedeworkDefs.defaultView];
      }
    }

    final TimeDateComponents viewStart = mstate.getViewStartDate();

    if (!newView) {
      /* See if we were given an explicit date as view start date components.
         If so we'll set a new view of the same period as the current.
       */
      final int year = viewStart.getYear();

      if (checkDateInRange(request, year)) {
        final String vsdate = viewStart.getDateTime().getDtval().substring(0, 8);

        if (!(vsdate.equals(request.getSess().getCurTimeView(request).getFirstDayFmt().getDateDigits()))) {
          newView = true;
          newViewType = mstate.getViewType();
          final Date jdt = DateTimeUtil.fromISODate(vsdate);
          dt = new CalendarFormatter(jdt);
        }
      }
    }

    if (newView) {
      mstate.setViewType(newViewType);
      mstate.setViewMcDate(dt);
      mstate.setRefresh(true);
      request.getClient().clearSearchEntries();
      request.getClient().clearSearch();
    }

    final TimeView tv = request.getSess().getCurTimeView(request);

    /* Set first day, month and year
     */

    final Calendar firstDay = tv.getFirstDay();

    viewStart.setDay(firstDay.get(Calendar.DATE));
    viewStart.setMonth(firstDay.get(Calendar.MONTH) + 1);
    viewStart.setYear(firstDay.get(Calendar.YEAR));

    //form.getEventStartDate().setDateTime(tv.getCurDayFmt().getDateTimeString());
    //form.getEventEndDate().setDateTime(tv.getCurDayFmt().getDateTimeString());
  }

  /** Set the current date for view.
   *
   * @param request BwRequest
   * @param date         String yyyymmdd date
   */
  public static void setViewDate(final BwRequest request,
                             final String date) {
    final BwModuleState mstate = request.getModule().getState();
    Date jdt = DateTimeUtil.fromISODate(date);
    CalendarFormatter dt = new CalendarFormatter(jdt);

    if (!checkDateInRange(request, dt.getYear())) {
      // Set it to today
      jdt = new Date(System.currentTimeMillis());
      dt = new CalendarFormatter(jdt);
    }
    mstate.setViewMcDate(dt);
    mstate.setRefresh(true);
  }

  private static boolean checkDateInRange(final BwRequest req,
                                          final int year) {
    final BwWebGlobals globals = req.getBwGlobals();

    // XXX make system parameters for allowable start/end year
    final int thisYear = globals.getToday().getFormatted().getYear();

    if ((year < (thisYear - 50)) || (year > (thisYear + 50))) {
      req.error(ValidationError.invalidDate, year);
      return false;
    }

    return true;
  }
}
