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
import org.bedework.caldav.util.filter.FilterBase;
import org.bedework.calfacade.exc.CalFacadeException;
import org.bedework.util.servlet.MessageEmit;

/** This class represents a month of events. The firstDay and lastDay are set
 * to be the latest and earliest including the curDay.
 *
 * @author  Mike Douglass douglm@bedework.edu
 */
public class MonthView extends TimeView {
  /** Constructor:
   *
   * @param  cl        Client interface
   * @param err - for error messages
   * @param  curDay    MyCalendarVO representing current day.
   * @param  filter    non-null to filter the results.
   * @throws CalFacadeException
   */
  public MonthView(final Client cl,
                   final MessageEmit err,
                   final MyCalendarVO curDay,
                   final FilterBase filter) throws CalFacadeException {
    super(cl, err,
          curDay.getCalendar(), "Month",
          CalendarInfo.getInstance().getFirstDayOfThisMonth(curDay.getCalendar().getTimeZone(),
                                                            curDay.getCalendar().getTime()),
          CalendarInfo.getInstance().getLastDayOfThisMonth(curDay.getCalendar().getTimeZone(),
                                                           curDay.getCalendar().getTime()),
          curDay.getPrevMonth().getDateDigits(),
          curDay.getNextMonth().getDateDigits(),
          true,  // showdata
          filter);
  }
}

