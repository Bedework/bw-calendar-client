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

import org.bedework.appcommon.BedeworkDefs;
import org.bedework.appcommon.ClientError;
import org.bedework.appcommon.client.Client;
import org.bedework.appcommon.client.IcalCallbackcb;
import org.bedework.appcommon.client.SearchParams;
import org.bedework.calfacade.svc.BwView;
import org.bedework.calfacade.svc.EventInfo;
import org.bedework.calsvci.indexing.BwIndexer.Position;
import org.bedework.calsvci.indexing.SearchResultEntry;
import org.bedework.icalendar.IcalTranslator;
import org.bedework.util.calendar.ScheduleMethods;
import org.bedework.webcommon.BwActionFormBase;
import org.bedework.webcommon.BwModuleState;
import org.bedework.webcommon.BwRequest;
import org.bedework.webcommon.event.EventActionBase;

import net.fortuna.ical4j.model.Calendar;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Action to set up parameters for search.
 * <p>Request parameters<ul>
 *      <li>"ignoreCreator" show all events regardless of creator - value: "yes"
 *      <li>"calPath"  calendar for event.</li>
 *      <li>"start"             start date</li>
 *      <li>"end"               end date</li>
 *      <li>"listAllEvents"     (if no start/end) true/false.</li>
 *      <li>"days"              (if no start/end) integer number of days</li>
 *      <li>"searchLimits"      "beforeToday", "fromToday", "none"</li>
 *      <li>"filterName"        name of predefined filter</li>
 *      <li>"fexpr"             filter expression</li>
 *      <li>"cat"               (if no filterName) multi-valued category names</li>
 *      <li>"forExport"         true if the intent is to export the list.</li>
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
    BwModuleState mstate = request.getModule().getState();
    SearchParams params = new SearchParams();
    Client cl = request.getClient();

    int forward = setSearchParams(request, params);
    if (forward != forwardSuccess) {
      return forward;
    }

    params.setForExport(request.getBooleanReqPar("forExport", false));

    forward = tryCal(request, form);

    if (forward == forwardNoAction) {
      forward = doView(request, form);
    }

    if (params.getFromDate() != null) {
      gotoDateView(request,
                   params.getFromDate().getDtval(),
                   request.getModule().getState().getViewTypeI());
    }

    request.setRequestAttr(BwRequest.bwSearchParamsName, params);

    if ((params.getFormat() != null) &&
        (params.getFormat().equals("text/calendar"))) {
      Collection<SearchResultEntry> sres = cl.getSearchResult(
              Position.current);
      if (sres.size() == 0) {
        return forwardNull;
      }

      IcalTranslator trans = new IcalTranslator(new IcalCallbackcb(request.getClient()));

      Collection<EventInfo> eis = new ArrayList<>(sres.size());
      for (SearchResultEntry sre: sres) {
        if (sre.getEntity() instanceof EventInfo) {
          eis.add((EventInfo)sre.getEntity());
        }
      }

      Calendar ical = trans.toIcal(eis, ScheduleMethods.methodTypePublish);

      request.getResponse().setHeader("Content-Disposition",
                                      "Attachment; Filename=\"" +
                                      form.getContentName() + "\"");
      request.getResponse().setContentType("text/calendar; charset=UTF-8");

      IcalTranslator.writeCalendar(ical, request.getResponse().getWriter());
      request.getResponse().getWriter().close();

      return forwardNull;
    }

    /* Do the search */
    mstate.setSearchResult(cl.search(params));
    request.setRequestAttr(BwRequest.bwSearchResultName,
                           mstate.getSearchResult());

    return forwardSuccess;
  }

  /* Try for a calendar url. Return with forward or null for not found.
   */
  private int tryCal(final BwRequest request,
                     final BwActionFormBase form) throws Throwable {
    BwModuleState mstate = request.getModule().getState();
    String vpath = request.getReqPar("virtualPath");

    if (vpath == null) {
      return forwardNoAction;
    }

    if (!request.getClient().setVirtualPath(vpath)) {
      form.getErr().emit(ClientError.badVpath, vpath);
      return forwardNoAction;
    }

    mstate.setSelectionType(BedeworkDefs.selectionTypeCollections);

    request.refresh();
    return forwardSuccess;
  }

  /* Do the view thing. This is the default action
   */
  private int doView(final BwRequest request,
                     final BwActionFormBase form) throws Throwable {
    BwModuleState mstate = request.getModule().getState();
    List<String> vpaths = request.getReqPars("vpath");
    if (vpaths != null) {
      BwView view = new BwView();

      view.setName("--temp--");
      view.setCollectionPaths(vpaths);
      view.setConjunction(request.getBooleanReqPar("conjunction", false));
      request.getClient().setCurrentView(view);

      mstate.setSelectionType(BedeworkDefs.selectionTypeView);
      request.refresh();

      return forwardSuccess;
    }

    if (!setView(request, request.getReqPar("viewName"))) {
      return forwardNoViewDefined;
    }

    return forwardSuccess;
  }
}
