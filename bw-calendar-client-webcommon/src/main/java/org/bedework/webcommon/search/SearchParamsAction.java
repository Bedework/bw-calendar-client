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
import org.bedework.calfacade.exc.CalFacadeException;
import org.bedework.calfacade.indexing.BwIndexer.Position;
import org.bedework.calfacade.indexing.SearchResultEntry;
import org.bedework.calfacade.svc.EventInfo;
import org.bedework.convert.IcalTranslator;
import org.bedework.convert.jcal.JcalTranslator;
import org.bedework.convert.jscal.JSCalTranslator;
import org.bedework.convert.xcal.XmlTranslator;
import org.bedework.jsforj.model.JSGroup;
import org.bedework.util.calendar.IcalendarUtil;
import org.bedework.util.calendar.ScheduleMethods;
import org.bedework.util.misc.response.Response;
import org.bedework.util.xml.XmlEmit;
import org.bedework.util.xml.XmlEmit.NameSpace;
import org.bedework.util.xml.tagdefs.XcalTags;
import org.bedework.webcommon.BwAbstractAction;
import org.bedework.webcommon.BwActionFormBase;
import org.bedework.webcommon.BwModuleState;
import org.bedework.webcommon.BwRequest;
import org.bedework.webcommon.BwSession;

import net.fortuna.ical4j.model.Calendar;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collection;

import javax.servlet.http.HttpServletResponse;

import static org.bedework.webcommon.search.SearchUtil.setSearchParams;

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
public class SearchParamsAction extends BwAbstractAction {
  private boolean listMode;

  @Override
  public int doAction(final BwRequest request) {
    final BwModuleState mstate = request.getModule().getState();
    final SearchParams params = new SearchParams();
    final Client cl = request.getClient();
    final boolean forFeederOneShot =
            "y".equals(request.getReqPar("f")) ||
                    "y".equals(request.getStringActionPar("f"));

    if (listMode) {
      cl.setViewMode(Client.listViewMode);
    }

    final boolean listMode = Client.listViewMode.equals(cl.getViewMode()) ||
            forFeederOneShot;

    final boolean gridMode = !listMode &&
            Client.gridViewMode.equals(cl.getViewMode());

    if (debug()) {
      debug("Client mode is " + cl.getViewMode());
    }

    final int forward = setSearchParams(request, params,
                                        gridMode);
    if (forward != forwardSuccess) {
      return forward;
    }

    final String outFormat = params.getFormat();
    boolean generateCalendarContent = false;
    boolean generateIcal = false;
    boolean generateJscal = false;
    boolean generateXcal = false;

    if (outFormat != null) {
      switch (outFormat) {
        case "text/calendar":
          generateCalendarContent = true;
          generateIcal = true;
          break;
        case "application/calendar+xml":
          generateCalendarContent = true;
          generateXcal = true;
          break;
        case "application/jscalendar+json":
          generateCalendarContent = true;
          generateJscal = true;
          break;
        case "application/calendar+json":
          generateCalendarContent = true;
          break;
      }
    }

    final HttpServletResponse response = request.getResponse();

    if (forFeederOneShot || generateCalendarContent) {
      request.setNocache(false);

      if (!request.contentChanged()) {
        return forwardNull;
      }
    }

    params.setForExport(request.getBooleanReqPar("forExport", false));

    request.setRequestAttr(BwRequest.bwSearchParamsName, params);

    if (params.getStatus() == Response.Status.ok) {
//    if (!gridMode) {
      /* Do the search */
      mstate.setSearchResult(cl.search(params));
      //  }
    }

    request.refresh();

    if (generateCalendarContent) {
      final Collection<SearchResultEntry> sres = cl.getSearchResult(
              Position.current);
      if ((sres == null) || (sres.isEmpty())) {
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

      String contentName = request.getContentName();
      if (contentName == null) {
        if (generateIcal) {
          contentName = "calendar.ics";
        } else if (generateXcal) {
          contentName = "calendar.xcs";
        } else if (generateJscal) {
          contentName = "calendar.json";
        } else {
          contentName = "calendar.jcs";
        }
      }

      response.setHeader("Content-Disposition",
                         "Attachment; Filename=\"" +
                                 contentName + "\"");
      response.setContentType(outFormat + "; charset=UTF-8");

      /* Add an etag */
      response.addHeader("etag", cl.getCurrentChangeToken());
      response.setContentLength(-1);

      try (final Writer wtr = response.getWriter()) {
        if (generateIcal) {
          final Calendar ical = trans.toIcal(eis, ScheduleMethods.methodTypePublish);

          IcalendarUtil.writeCalendar(ical, wtr);
        } else if (generateXcal) {
          final XmlTranslator xmlTrans =
                  new XmlTranslator(new IcalCallbackcb(request.getClient()));

          final XmlEmit xml = new XmlEmit();

          xml.addNs(new NameSpace(XcalTags.namespace, "X"), true);

          xml.startEmit(wtr);

          xmlTrans.writeXmlCalendar(eis, ScheduleMethods.methodTypePublish,
                                 xml);
        } else if (generateJscal) {
          final JSCalTranslator jscalTrans =
                  new JSCalTranslator(new IcalCallbackcb(request.getClient()));

          final JSGroup grp = jscalTrans.toJScal(eis,
                                                 ScheduleMethods.methodTypePublish);

          JSCalTranslator.writeJSCalendar(grp, wtr);
        } else {
          final JcalTranslator jcalTrans =
                  new JcalTranslator(new IcalCallbackcb(request.getClient()));

          jcalTrans.writeJcal(eis, ScheduleMethods.methodTypePublish,
                              wtr);
        }
      } catch (final IOException e) {
        throw new CalFacadeException(e);
      }

      return forwardNull;
    }

    if (params.getStatus() == Response.Status.ok) {
      request.setRequestAttr(BwRequest.bwSearchResultName,
                             mstate.getSearchResult());
    }
    
    final BwSession sess = request.getSess();

    /* Embed the writable collections in session for admin client */
    if (cl.getPublicAdmin()) {
      sess.embedAddContentCalendarCollections(request);
    }

    if (!forFeederOneShot) {
      if (!gridMode) {
        return forwardListEvents;
      }
      return forwardSuccess;
    }

    /* Embed the result */

    if (params.getStatus() == Response.Status.ok) {
      request.setRequestAttr(BwRequest.bwSearchListName,
                             cl.getSearchResult(Position.current));
    }
    
    /* Ensure we have categories embedded in session */
    sess.embedCategories(request, false,
                                      BwSession.ownersEntity);

    /* Add an etag */
    response.addHeader("etag",
                       cl.getCurrentChangeToken());

    return forwardSuccess;
  }

  /* ============================================================
   *                 Request parameters
   * ============================================================ */

  public void setListMode(final String val) {
    listMode = Boolean.parseBoolean(val);
  }
}
