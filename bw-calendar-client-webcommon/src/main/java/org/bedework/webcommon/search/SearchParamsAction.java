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
package org.bedework.webcommon.search;

import org.bedework.appcommon.EventFormatter;
import org.bedework.appcommon.client.Client;
import org.bedework.appcommon.client.IcalCallbackcb;
import org.bedework.appcommon.client.SearchParams;
import org.bedework.calfacade.indexing.BwIndexer.Position;
import org.bedework.calfacade.indexing.SearchResultEntry;
import org.bedework.calfacade.svc.EventInfo;
import org.bedework.icalendar.IcalTranslator;
import org.bedework.util.calendar.ScheduleMethods;
import org.bedework.webcommon.BwActionFormBase;
import org.bedework.webcommon.BwModuleState;
import org.bedework.webcommon.BwRequest;
import org.bedework.webcommon.BwSession;
import org.bedework.webcommon.event.EventActionBase;

import net.fortuna.ical4j.model.Calendar;

import java.util.ArrayList;
import java.util.Collection;

import javax.servlet.http.HttpServletResponse;

/**
 * Action to set up parameters for search.
 * <p>Request parameters<ul>
 *      <li>"ignoreCreator" show all events regardless of creator - value: "yes"
 *      <li>"calPath"  calendar for event.</li>
 *      <li>"start"             start date</li>
 *      <li>"end"               end date</li>
 *      <li>"days"              (if no start/end) integer number of days</li>
 *      <li>"searchLimits"      "beforeToday", "fromToday", "none"</li>
 *      <li>"filterName"        name of predefined filter</li>
 *      <li>"fexpr"             filter expression</li>
 *      <li>"cat"               (if no filterName) multi-valued category names</li>
 *      <li>"forExport"         true if the intent is to export the list.</li>
 *      <li>"f=y"               If this is from the URL builder and we
 *                              do everything in one action.</li>
 * </ul>
 * <p>Forwards to:<ul>
 *      <li>"noAction"     when request seems wrong.</li>
 *      <li>"showEvent"    event is setup for viewing.</li>
 * </ul>
 */
public class SearchParamsAction extends EventActionBase {
  @Override
  public int doAction(final BwRequest request,
                      final BwActionFormBase form) throws Throwable {
    final BwModuleState mstate = request.getModule().getState();
    final SearchParams params = new SearchParams();
    final Client cl = request.getClient();
    final boolean forFeederOneShot = "y".equals(request.getReqPar("f"));
    final String outFormat = params.getFormat();
    boolean generateCalendarContent = false;

    if ((outFormat != null) &&
            (outFormat.equals("text/calendar"))) {
      generateCalendarContent = true;
    }

    String changeToken = null;

    if (forFeederOneShot || generateCalendarContent) {
      form.setNocache(false);
      changeToken = cl.getCurrentChangeToken();

      final String ifNoneMatch = request.getRequest().getHeader("if-none-match");

      if ((changeToken != null) && changeToken.equals(ifNoneMatch)) {
        request.getResponse().setStatus(HttpServletResponse.SC_NOT_MODIFIED);
        return forwardNull;
      }
    }

    final boolean listMode = Client.listViewMode.equals(cl.getViewMode()) ||
            forFeederOneShot;

    final int forward = setSearchParams(request, params, listMode);
    if (forward != forwardSuccess) {
      return forward;
    }

    params.setForExport(request.getBooleanReqPar("forExport", false));

    if (params.getFromDate() != null) {
      gotoDateView(request,
                   params.getFromDate().getDtval(),
                   mstate.getCurViewPeriod());
    }

    request.setRequestAttr(BwRequest.bwSearchParamsName, params);

    /* Do the search */
    mstate.setSearchResult(cl.search(params));

    if (generateCalendarContent) {
      final Collection<SearchResultEntry> sres = cl.getSearchResult(
              Position.current);
      if (sres.size() == 0) {
        return forwardNull;
      }

      final IcalTranslator trans =
              new IcalTranslator(new IcalCallbackcb(request.getClient()));

      final Collection<EventInfo> eis = new ArrayList<>(sres.size());
      for (final SearchResultEntry sre: sres) {
        if (sre.getEntity() instanceof EventFormatter) {
          eis.add(((EventFormatter)sre.getEntity()).getEventInfo());
        }
      }

      final Calendar ical = trans.toIcal(eis, ScheduleMethods.methodTypePublish);

      String contentName = form.getContentName();
      if (contentName == null) {
        contentName = "calendar.ics";
      }

      request.getResponse().setHeader("Content-Disposition",
                                      "Attachment; Filename=\"" +
                                              contentName + "\"");
      request.getResponse().setContentType("text/calendar; charset=UTF-8");

      IcalTranslator.writeCalendar(ical, request.getResponse().getWriter());
      request.getResponse().getWriter().close();

      /* Add an etag */
      request.getResponse().addHeader("etag", changeToken);

      return forwardNull;
    }

    request.setRequestAttr(BwRequest.bwSearchResultName,
                           mstate.getSearchResult());

    if (!forFeederOneShot) {
      return forwardSuccess;
    }

    /* Embed the result */

    request.setRequestAttr(BwRequest.bwSearchListName,
                           cl.getSearchResult(Position.current));

    /* Ensure we have categories embedded in session */
    request.getSess().embedCategories(request, false,
                                      BwSession.ownersEntity);

    /* Add an etag */
    request.getResponse().addHeader("etag", changeToken);

    return forwardSuccess;
  }
}
