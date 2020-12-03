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

import org.bedework.caldav.util.filter.FilterBase;
import org.bedework.util.servlet.MessageEmit;

/** This class represents a year of events. The firstDay and lastDay are set
 * to be the latest and earliest including the curDay.
 *
 * @author  Mike Douglass douglm   rpi.edu
 */
public class YearView extends TimeView {
  /** Constructor:
   *
   * @param  err - for error messages
   * @param  curDay    MyCalendarVO representing current day.
   * @param  showData  boolean true if this TimeView can be used to
   *                   display events or if it is used for structure only.
   *                   For example we may use the year for navigation only
   *                   to reduce the amount of data retrieved.
   * @param  filter    non-null to filter the results.
   */
  public YearView(final MessageEmit err,
                  final MyCalendarVO curDay,
                  final boolean showData,
                  final FilterBase filter) {
    super(err,
          curDay.getCalendar(), "Year",
          CalendarInfo.getInstance().getFirstDayOfThisYear(curDay.getCalendar().getTimeZone(),
                                                           curDay.getCalendar().getTime()),
          CalendarInfo.getInstance().getLastDayOfThisYear(curDay.getCalendar().getTimeZone(),
                                                          curDay.getCalendar().getTime()),
          curDay.getPrevYear().getDateDigits(),
          curDay.getNextYear().getDateDigits(),
          showData,
          filter);

    viewPeriod = BedeworkDefs.yearView;
  }
}

