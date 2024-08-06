/* ********************************************************************
    Appropriate copyright notice
*/
package org.bedework.webcommon.search;

import org.bedework.appcommon.ClientError;
import org.bedework.appcommon.TimeView;
import org.bedework.appcommon.client.Client;
import org.bedework.appcommon.client.SearchParams;
import org.bedework.caldav.util.filter.FilterBase;
import org.bedework.calfacade.BwDateTime;
import org.bedework.calfacade.RecurringRetrievalMode;
import org.bedework.calfacade.base.BwTimeRange;
import org.bedework.calfacade.configs.AuthProperties;
import org.bedework.calfacade.filter.BwCreatorFilter;
import org.bedework.calfacade.filter.SimpleFilterParser;
import org.bedework.calfacade.responses.GetFilterDefResponse;
import org.bedework.calfacade.util.BwDateTimeUtil;
import org.bedework.util.calendar.XcalUtil;
import org.bedework.util.misc.response.Response;
import org.bedework.util.timezones.DateTimeUtil;
import org.bedework.webcommon.BwModuleState;
import org.bedework.webcommon.BwRequest;

import static org.bedework.webcommon.DateViewUtil.gotoDateView;
import static org.bedework.webcommon.ForwardDefs.forwardNoAction;
import static org.bedework.webcommon.ForwardDefs.forwardSuccess;

/**
 * User: mike Date: 8/5/24 Time: 23:12
 */
public class SearchUtil {
  public static int setSearchParams(final BwRequest request,
                             final SearchParams params,
                             final boolean gridMode) {
    final BwModuleState mstate = request.getModule().getState();
    final Client cl = request.getClient();

    params.setPublicIndexRequested(request.present("public"));

    String icalStart = XcalUtil.getIcalFormatDateTime(request.getReqPar("start"));
    String endStr = request.getReqPar("end");

    filterAndQuery(request, params);

    if (gridMode) {
      TimeView tv = mstate.getCurTimeView();
      if (tv == null) {
        // Pretty much broken here
        params.setFromDate(todaysDateTime());
        return forwardSuccess;
      }

      // Ignore any end date
      if (icalStart == null) {
        params.setFromDate(tv.getViewStart());
        params.setToDate(tv.getViewEnd());
        return forwardSuccess;
      }

      // Set current timeview to given date - rounded approopriately

      final BwDateTime bdt = BwDateTimeUtil.getDateTime(
              icalStart,
              true,
              false, null);
      gotoDateView(request,
                   bdt.getDtval(),
                   mstate.getViewType());
      tv = mstate.getCurTimeView();

      params.setFromDate(tv.getViewStart());
      params.setToDate(tv.getViewEnd());

      return forwardSuccess;
    }

    if (icalStart == null) {
      final String lim = mstate.getSearchLimits();
      if ((lim != null) && (!"none".equals(lim))) {  // there are limits
        if ("beforeToday".equals(lim)) {
          endStr = DateTimeUtil.isoDate(DateTimeUtil.yesterday());
        } else if ("fromToday".equals(lim)) {
          icalStart = DateTimeUtil.isoDate(new java.util.Date());
        }
      }
    }

    final AuthProperties authp = cl.getAuthProperties();

    if (params.getFromDate() == null) {
      int days = request.getIntReqPar("days", -32767);
//    if (days < 0) {
      //    days = authp.getDefaultWebCalPeriod();
      //}

      if ((icalStart == null) && (endStr == null)) {
        if (!cl.getWebSubmit() && !cl.getPublicAdmin()) {
          if (!request.getBooleanReqPar("listAllEvents", false)) {
            params.setFromDate(todaysDateTime());

            final int max = authp.getMaxWebCalPeriod();
            if (days < 0) {
              days = max;
            } else if ((days > max) && !cl.isSuperUser()) {
              days = max;
            }

            params.setToDate(
                    params.getFromDate().addDur("P" + days + "D"));
          }
        }
      } else if ((endStr != null) || (days > 0)) {
        int max = 0;

        if (!cl.isSuperUser()) {
          max = authp.getMaxWebCalPeriod();
        }

        final BwTimeRange tr =
                BwDateTimeUtil.getPeriod(icalStart,
                                         endStr,
                                         java.util.Calendar.DATE,
                                         days,
                                         java.util.Calendar.DATE,
                                         max);

        if (tr == null) {
          request.error(ClientError.badRequest, "dates");
          return forwardNoAction;
        }

        params.setFromDate(tr.getStart());
        params.setToDate(tr.getEnd());
      } else {
        params.setFromDate(BwDateTimeUtil.getDateTime(
                icalStart,
                true,
                false, null));
        params.setToDate(params.getFromDate()
                               .addDur("P" + authp.getMaxWebCalPeriod()
                                               + "D"));
      }
    }

    if (params.getFromDate() != null) {
      gotoDateView(request,
                   params.getFromDate().getDtval(),
                   mstate.getViewType());
    }

    final int offset = request.getIntReqPar("offset", -1);

    if (offset > 0) {
      params.setCurOffset(offset);
    }

    int count = request.getIntReqPar("count", -1);

    if (count < 0) {
      count = cl.getPreferences().getPageSize();
    }

    if (count < 0) {
      count = 20;
    }

    if (count > 250) {
      count = 250;
    }

    params.setPageSize(count);

    params.setFormat(request.getReqPar("format"));

    if (request.getBooleanReqPar("master", false)) {
      params.setRecurMode(RecurringRetrievalMode.entityOnly);
    }

    return forwardSuccess;
  }

  private static boolean filterAndQuery(final BwRequest request,
                                 final SearchParams params) {
    final Client cl = request.getClient();

    params.setQuery(request.getReqPar("query"));
    params.setRelevance(request.getBooleanReqPar("relevance", false));

    final GetFilterDefResponse gfdr = request.getFilterDef();

    if (gfdr.getStatus() != Response.Status.ok) {
      params.setStatus(gfdr.getStatus());
      params.setMessage(gfdr.getMessage());
      return false;
    }

    FilterBase filter = null;
    if (gfdr.getFilterDef() != null) {
      filter = gfdr.getFilterDef().getFilters();
    }

    if (cl.getWebSubmit() || cl.getPublicAdmin()) {
      boolean ignoreCreator = false; //cl.getWebSubmit();

      if (request.getBooleanReqPar("sg", false) ||
              request.getBooleanReqPar("ignoreCreator", false)) {
        ignoreCreator = true;
      }

      if (!ignoreCreator) {
        final BwCreatorFilter crefilter = new BwCreatorFilter(null);
        crefilter.setEntity(cl.getCurrentPrincipalHref());

        filter = FilterBase.addAndChild(filter, crefilter);
      }
    }

    if (filter == null) {
      filter = request.getModule().defaultSearchFilter(request);
    }

    params.setFilter(filter);

    String sort = request.getReqPar("sort");
    if (sort == null) {
      // TODO - this shouldn't be a fixed string
      sort = "dtstart.utc:asc";
    }

    final SimpleFilterParser.ParseResult pr = cl.parseSort(sort);
    if (!pr.ok) {
      request.error(pr.message);
      params.setStatus(Response.Status.failed);
      params.setMessage(pr.message);
      return false;
    }

    params.setSort(pr.sortTerms);

    return true;
  }

  private static BwDateTime todaysDateTime() {
    return BwDateTimeUtil.getDateTime(DateTimeUtil.isoDate(),
                                      true,
                                      false,   // floating
                                      null);   // tzid
  }
}
