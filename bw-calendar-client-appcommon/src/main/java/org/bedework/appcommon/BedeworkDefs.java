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

import java.io.Serializable;

/** This interface defines some values for the Bedework web interface.
 *
 * @author Mike Douglass   douglm@rpi.edu
 */
public interface BedeworkDefs extends Serializable {
  /** These names are internal
   */
  public static final String vtToday = "todayView";
  public static final String vtDay = "dayView";
  public static final String vtWeek = "weekView";
  public static final String vtMonth = "monthView";
  public static final String vtYear = "yearView";

  public static final String[] viewPeriodNames =
    {vtToday, vtDay, vtWeek, vtMonth, vtYear};

  /** today */
  public static final int todayView = 0;

  /** day */
  public static final int dayView = 1;

  /** week */
  public static final int weekView = 2;

  /** month */
  public static final int monthView = 3;

  /** year*/
  public static final int yearView = 4;

  /** XXX - take out of syspars. The default view - this may be redundant
   */
  public static final int defaultView = weekView;

  /* Selection type constants */

  /** Display using a view */
  public static final String selectionTypeView = "view";

  /** Display using one or more collections */
  public static final String selectionTypeCollections = "collections";

  /** Display using a search */
  //public static final String selectionTypeSearch = "search";

  /** Display using a 'filter' */
  //public static final String selectionTypeFiltered = "filtered";

  /** */
  public static final String appTypeWebadmin = "webadmin";
  /** */
  public static final String appTypeWebsubmit = "websubmit";
  /** */
  public static final String appTypeWebpublic = "webpublic";
  /** */
  public static final String appTypeFeeder = "feeder";
  /** */
  public static final String appTypeWebuser = "webuser";
}

