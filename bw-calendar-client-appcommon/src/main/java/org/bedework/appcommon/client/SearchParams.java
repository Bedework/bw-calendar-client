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
package org.bedework.appcommon.client;

import org.bedework.appcommon.DateTimeFormatter;
import org.bedework.caldav.util.filter.FilterBase;
import org.bedework.calfacade.BwDateTime;
import org.bedework.calfacade.RecurringRetrievalMode;
import org.bedework.calfacade.filter.SortTerm;
import org.bedework.util.misc.ToString;

import java.io.Serializable;
import java.util.List;

/** Parameters for fetching a bunch of events.
 *
 * @author Mike Douglass douglm - rpi.edu
 */
public class SearchParams implements Serializable {
  private boolean publicIndexRequested;

  /* null if times not limited. */
  private BwDateTime fromDate;

  /* null if times not limited. */
  private BwDateTime toDate;

  private String query;

  private boolean relevance;

  private FilterBase filter;

  private List<SortTerm> sort;

  /* null for default */
  private String format;

  private boolean forExport;

  private int curOffset;

  private int pageSize;

  private DateTimeFormatter formattedStart;

  private DateTimeFormatter formattedEnd;

  private RecurringRetrievalMode recurMode;

  /** True for public
   *
   * @param val
   */
  public void setPublicIndexRequested(final boolean val) {
    publicIndexRequested = val;
  }

  /**
   * @return boolean
   */
  public boolean getPublicIndexRequested() {
    return publicIndexRequested;
  }

  /** if null no date time limiting
   * @param val
   */
  public void setFromDate(final BwDateTime val) {
    fromDate = val;
  }

  /** if null no date time limiting
   * @return from date
   */
  public BwDateTime getFromDate() {
    return fromDate;
  }

  /** if null no date time limiting
   * @param val
   */
  public void setToDate(final BwDateTime val) {
    toDate = val;
  }

  /** if null no date time limiting
   * @return from date
   */
  public BwDateTime getToDate() {
    return toDate;
  }

  /** if null no query
   * @param val
   */
  public void setQuery(final String val) {
    query = val;
  }


  /** if null no query
   * @return query string or null
   */
  public String getQuery() {
    return query;
  }

  /**
   * @param val true for a relevance style query
   */
  public void setRelevance(final boolean val) {
    relevance = val;
  }

  /**
   * @return true for a relevance style query
   */
  public boolean getRelevance() {
    return relevance;
  }

  /** if null no filtering
   * @param val
   */
  public void setFilter(final FilterBase val) {
    filter = val;
  }

  /** if null no filtering
   * @return filter
   */
  public FilterBase getFilter() {
    return filter;
  }

  /** Set the sort expression
   *
   * @param  val  String sort expression
   * @throws
   */
  public void setSort(List<SortTerm> val) {
    sort = val;
  }

  /**
   * @return sort expression
   */
  public List<SortTerm> getSort() {
    return sort;
  }

  /** True if we should retrieve the event to export
   *
   * @param val
   */
  public void setForExport(final boolean val) {
    forExport = val;
  }

  /**
   * @return boolean
   */
  public boolean getForExport() {
    return forExport;
  }

  /** Set the format
   *
   * @param val
   */
  public void setFormat(final String val) {
    format = val;
  }

  /**
   * @return String format, e.g. text/calendar or null
   */
  public String getFormat() {
    return format;
  }

  /** Set current page in search result
   * @param val
   */
  public void setCurOffset(final int val) {
    curOffset = val;
  }

  /**
   * @return current page in search result
   */
  public int getCurOffset() {
    return curOffset;
  }

  /** Set current page size
   * @param val
   */
  public void setPageSize(final int val) {
    pageSize = val;
  }

  /**
   * @return current page size
   */
  public int getPageSize() {
    return pageSize;
  }

  public DateTimeFormatter getFormattedStart() {
    if (formattedStart == null) {
      formattedStart = new DateTimeFormatter(fromDate);
    }
    return formattedStart;
  }

  public DateTimeFormatter getFormattedEnd() {
    if (formattedEnd == null) {
      formattedEnd  = new DateTimeFormatter(toDate);
    }
    return formattedEnd;
  }

  public void setRecurMode(final RecurringRetrievalMode val) {
    recurMode = val;
  }

  public RecurringRetrievalMode getRecurMode() {
    if (recurMode == null) {
      return RecurringRetrievalMode.expanded;
    }

    return recurMode;
  }

  @Override
  public String toString() {
    final ToString ts = new ToString(this);

    ts.append("publicIndexRequested", getPublicIndexRequested())
            .append("fromDate", getFromDate())
            .append("toDate", getToDate())
            .append("query", getQuery())
            .append("relevance", getRelevance())
            .append("filter", getFilter())
            .append("format", getFormat())
            .append("forExport", getForExport())
            .append("curOffset", getCurOffset())
            .append("pageSize", getPageSize());

    return ts.toString();
  }
}
