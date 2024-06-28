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
package org.bedework.webcommon;

import org.bedework.appcommon.BedeworkDefs;
import org.bedework.appcommon.CalendarInfo;
import org.bedework.appcommon.ClientError;
import org.bedework.appcommon.ClientMessage;
import org.bedework.appcommon.ConfigCommon;
import org.bedework.appcommon.EventKey;
import org.bedework.appcommon.MyCalendarVO;
import org.bedework.appcommon.TimeView;
import org.bedework.appcommon.client.Client;
import org.bedework.appcommon.client.SearchParams;
import org.bedework.caldav.util.filter.FilterBase;
import org.bedework.calfacade.BwCalendar;
import org.bedework.calfacade.BwDateTime;
import org.bedework.calfacade.BwPrincipal;
import org.bedework.calfacade.BwResource;
import org.bedework.calfacade.RecurringRetrievalMode;
import org.bedework.calfacade.RecurringRetrievalMode.Rmode;
import org.bedework.calfacade.base.BwTimeRange;
import org.bedework.calfacade.configs.AuthProperties;
import org.bedework.calfacade.exc.CalFacadeAccessException;
import org.bedework.calfacade.exc.CalFacadeClosed;
import org.bedework.calfacade.exc.CalFacadeException;
import org.bedework.calfacade.exc.ValidationError;
import org.bedework.calfacade.filter.BwCreatorFilter;
import org.bedework.calfacade.filter.SimpleFilterParser.ParseResult;
import org.bedework.calfacade.responses.GetFilterDefResponse;
import org.bedework.calfacade.svc.BwPreferences;
import org.bedework.calfacade.svc.EventInfo;
import org.bedework.calfacade.util.BwDateTimeUtil;
import org.bedework.util.calendar.XcalUtil;
import org.bedework.util.misc.Util;
import org.bedework.util.misc.response.Response;
import org.bedework.util.servlet.filters.PresentationState;
import org.bedework.util.struts.UtilAbstractAction;
import org.bedework.util.timezones.DateTimeUtil;
import org.bedework.util.timezones.Timezones;
import org.bedework.util.webaction.ErrorEmitSvlt;
import org.bedework.util.webaction.MessageEmitSvlt;
import org.bedework.util.webaction.Request;
import org.bedework.util.webaction.WebActionForm;
import org.bedework.webcommon.config.ClientConfigurations;

import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import static org.bedework.appcommon.BedeworkDefs.appTypeWebsubmit;

/** This abstract action performs common setup actions before the real
 * action method is called.
 *
 * @author  Mike Douglass  douglm@rpi.edu
 */
public abstract class BwAbstractAction extends UtilAbstractAction
                                       implements ForwardDefs {
  /** Name of the init parameter holding our name */
  private static final String appNameInitParameter = "bwappname";

  private String viewType;

  @Override
  public String getId() {
    return getClass().getName();
  }

  /** This is the routine which does the work.
   *
   * @param request   For request pars and BwSession
   * @param frm       Action form
   * @return int      forward index
   */
  public abstract int doAction(BwRequest request,
                               BwActionFormBase frm);

  @Override
  public String performAction(final Request request)  {
    final BwRequest bwreq = (BwRequest)request;
    final BwActionFormBase form = bwreq.getBwForm();
    String adminUserId = null;

    final BwCallback cb = BwCallback.getCb(request, form);

    final int status;

    try {
      status = cb.in(request);
    } catch (final Throwable t) {
      error(t);
      invalidateSession(request);
      return forwards[forwardError];
    }

    if (status != HttpServletResponse.SC_OK) {
      request.getResponse().setStatus(status);
      error("Callback.in status=" + status);
      // Try not invalidating. There are multiple requests and this may
      // cause errors in the one that got through
      //invalidateSession(request);
      return forwards[forwardError];
    }

    final ConfigCommon conf = setConfig(bwreq);

    if (conf.getGuestMode()) {
      form.assignCurrentUser(null);
    } else {
      adminUserId = form.fetchCurrentAdminUser();
      if (adminUserId == null) {
        adminUserId = form.getCurrentUser();
      }
    }

    if (debug()) {
      debug("About to get state");
    }

    final BwSession bsess = getState(bwreq, form,
                                     adminUserId, conf);

    if (debug()) {
      debug("Obtained state");
    }

    form.setSession(bsess);

    bwreq.setSess(bsess);

    if (bwreq.present("refresh")) {
      bwreq.refresh();
    }

    checkMvarReq(bwreq);

    final Client cl = bwreq.getClient();
    final BwModuleState mstate = bwreq.getModule().getState();

    form.assignAdminUserPrincipal(cl.getCurrentPrincipal());
    form.assignCurUserSuperUser(cl.isSuperUser());

    // We need to have set the current locale before we do this.
    mstate.setCalInfo(CalendarInfo.getInstance());

    form.setGuest(bsess.isGuest());

    final BwPreferences prefs = cl.getPreferences();

    if (BedeworkDefs.appTypeWebpublic.equals(cl.getAppType()) ||
            cl.getPublicAuth()) {
      // force public view on - off by default
      form.setPublicView(true);
    } else {
      form.assignImageUploadDirectory(prefs.getDefaultImageDirectory());
    }

    if (cl.getWebSubmit() && (request.getReqPar("cs") != null)) {
      form.setCalSuiteName(request.getReqPar("cs"));
    }

    if (form.getNewSession()) {
      if (adminUserId != null) {
        final var loginMsg = "Logged in to " + conf.getAppType() +
                " as " + adminUserId;
        if (isAuditLoggerEnabled()) {
          audit(loginMsg);
        }
        info(loginMsg);
      }

      form.setHour24(form.getConfig().getHour24());
      if (!cl.getPublicAdmin() &&
              !form.getSubmitApp() &&
              !form.getGuest()) {
        form.setHour24(prefs.getHour24());
      }

      form.setEndDateType(BwPreferences.preferredEndTypeDuration);
      if (!cl.getPublicAdmin() && !form.getGuest()) {
        form.setEndDateType(prefs.getPreferredEndType());
      }

      bsess.embedFilters(bwreq);

      if (debug()) {
        debug("Filters embedded");
      }

      if (!cl.getPublicAdmin()) {
        if (getViewType() != null) {
          mstate.setViewType(getViewType());
        } else {
          mstate.setViewType(prefs.getPreferredViewPeriod());
        }
      }
    }

    /*if (debug()) {
      BwFilter filter = bwreq.getFilter(debug);
      if (filter != null) {
        debug(filter.toString());
      }
    }*/

    if (debug()) {
      debug("About to prepare render");
    }

    bsess.prepareRender(bwreq);

    if (form.getNewSession()) {
      gotoDateView(bwreq,
                   mstate.getDate(),
                   mstate.getViewType());
    }

    if (debug()) {
      debug("current change token: " +
                       bwreq.getSessionAttr(BwSession.changeTokenAttr));
    }

    try{
      final String tzid = prefs.getDefaultTzid();

      if (tzid != null) {
        Timezones.setThreadDefaultTzid(tzid);
      }
    } catch (final Throwable t) {
      error("Unable to set default tzid");
      error(t);
    }

    if (form.getDirInfo() == null) {
      form.setDirInfo(cl.getDirectoryInfo());
    }

    final PresentationState ps = request.getPresentationState();

    if (ps.getAppRoot() == null) {
      ps.setAppRoot(suffixRoot(bwreq,
                               form.getConfig().getAppRoot()));
      ps.setBrowserResourceRoot(suffixRoot(bwreq,
                                           form.getConfig().getBrowserResourceRoot()));

      // Set the default skin

      if (ps.getSkinName() == null) {
        // No skin name supplied - use the default
        final String skinName = prefs.getSkinName();

        ps.setSkinName(skinName);
        ps.setSkinNameSticky(true);
      }
    }

    /* see if we got cancelled */

    final String reqpar = request.getReqPar("cancelled");

    if (reqpar != null) {
      /* Set the objects to null so we get new ones.
       */
      request.message(ClientMessage.cancelled);
      return forwards[forwardCancelled];
    }

    /* Set up ready for the action - may reset svci */
    final BwModule mdl = bwreq.getModule();

    final int temp = mdl.actionSetup(bwreq);
    if (temp != forwardNoAction) {
      return forwards[temp];
    }

    mdl.checkMessaging(bwreq);

    String forward;

    try {
      /*
      if (bwreq.present("viewType")) {
        mstate.setViewType(bwreq.getReqPar("viewType"));

        gotoDateView(bwreq,
                     mstate.getDate(),
                     mstate.getCurViewPeriod());
      }
      */

      forward = forwards[doAction(bwreq, form)];

//      bsess.prepareRender(bwreq);
    } catch (final CalFacadeAccessException cfae) {
      request.error(ClientError.noAccess);
      if (debug()) {
        error(cfae);
      }
      forward = forwards[forwardNoAccess];
      cl.rollback();
    } catch (final CalFacadeClosed cfc) {
      request.warn(ClientError.closed);
      warn("Interface closed");
      forward = forwards[forwardGotomain];
    } catch (final CalFacadeException cfe) {
      request.error(cfe.getMessage(), cfe.getExtra());
      request.error(cfe);
      if (debug()) {
        error(cfe);
      }

      forward = forwards[forwardError];
      cl.rollback();
    } catch (final Throwable t) {
      request.error(t);
      if (debug()) {
        error(t);
      }
      forward = forwards[forwardError];
      cl.rollback();
    }

    return forward;
  }

  protected Request getRequest(final HttpServletRequest request,
                               final HttpServletResponse response,
                               final Map<String, String> params,
                               final String actionPath,
                               final ErrorEmitSvlt err,
                               final MessageEmitSvlt msg,
                               final WebActionForm form) {
    return new BwRequest(request, response, params, actionPath, err, msg, form);
  }

  /** Set the config object.
   *
   * @param request wrapper
   * @return config object
   */
  public ConfigCommon setConfig(final BwRequest request) {
    final BwActionFormBase form = request.getBwForm();

    if (form.configSet()) {
      return form.getConfig();
    }

    final HttpSession session = request.getRequest().getSession();
    final ServletContext sc = session.getServletContext();

    String appname = sc.getInitParameter("bwappname");

    if ((appname == null) || (appname.isEmpty())) {
      appname = "unknown-app-name";
    }

    final ConfigCommon conf = ClientConfigurations
            .getConfigs().getClientConfig(appname);
    if (conf == null) {
      throw new CalFacadeException("No config available for app " + appname);
    }

//    conf = (ConfigCommon)conf.clone();
    form.setConfig(conf); // So we can get an svci object and set defaults
    return conf;
  }

  protected int setSearchParams(final BwRequest request,
                                final SearchParams params,
                                final boolean gridMode) {
    final BwActionFormBase form = request.getBwForm();
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

  private boolean filterAndQuery(final BwRequest request,
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

    final ParseResult pr = cl.parseSort(sort);
    if (!pr.ok) {
      request.error(pr.message);
      params.setStatus(Response.Status.failed);
      params.setMessage(pr.message);
      return false;
    }
    
    params.setSort(pr.sortTerms);
    
    return true;
  }

  protected BwDateTime todaysDateTime() {
    return BwDateTimeUtil.getDateTime(DateTimeUtil.isoDate(),
                                      true,
                                      false,   // floating
                                      null);   // tzid
  }
  protected EventInfo findEvent(final BwRequest request,
                                final EventKey ekey) {
    final Client cl = request.getClient();
    EventInfo ev = null;

    if (ekey.getColPath() == null) {
      // bogus request
      request.error(ValidationError.missingCalendarPath);
      return null;
    }

    String key = null;

    if (ekey.getGuid() != null) {
      key = ekey.getGuid();
      String rid = ekey.getRecurrenceId();
      // DORECUR is this right?
      final RecurringRetrievalMode rrm;
      if (ekey.getForExport()) {
        rrm = RecurringRetrievalMode.overrides;
        rid = null;
      } else {
        rrm = RecurringRetrievalMode.expanded;
      }

      if (debug()) {
        debug("Get event by guid with rid " + rid + " and rrm " + rrm);
      }

      final Collection<EventInfo> evs =
              cl.getEventByUid(ekey.getColPath(),
                               ekey.getGuid(),
                               rid, rrm).getEntities();
      if (debug()) {
        debug("Get event by guid found " + evs.size());
      }

      if (evs.size() == 1) {
        ev = evs.iterator().next();
      } else {
        // XXX this needs dealing with
      }
    } else if (ekey.getName() != null) {
      if (debug()) {
        debug("Get event by name");
      }
      key = ekey.getName();

      ev = cl.getEvent(ekey.getColPath(),
                       ekey.getName(),
                       ekey.getRecurrenceId());
    }

    if (ev == null) {
      request.error(ClientError.unknownEvent, key);
      return null;
    } else if (debug()) {
      debug("Get event found " + ev.getEvent());
    }

    return ev;
  }

  /** Find a principal object given a "user" request parameter.
   *
   * @param request     BwRequest for parameters
   * @return BwPrincipal     null if not found. Messages emitted
   */
  protected BwPrincipal<?> findPrincipal(final BwRequest request) {
    final String str = request.getReqPar("user");
    if (str == null) {
      request.error(ClientError.unknownUser, "null");
      return null;
    }

    final BwPrincipal<?> p = request.getClient().getUser(str);
    if (p == null) {
      request.error(ClientError.unknownUser, str);
      return null;
    }

    return p;
  }

  /** Method to retrieve an event. An event is identified by the calendar +
   * guid + recurrence id. We also take the subscription id as a parameter so
   * we can pass it along in the result for display purposes.
   *
   * <p>We cannot just take the calendar from the subscription, because the
   * calendar has to be the actual collection containing the event. A
   * subscription may be to higher up the tree (i.e. a folder).
   *
   * <p>It may be more appropriate to simply encode a url to the event.
   *
   * <p>Request parameters<ul>
   *      <li>"subid"    subscription id for event. < 0 if there is none
   *                     e.g. displayed directly from calendar.</li>
   *      <li>"calPath"  Path of calendar to search.</li>
   *      <li>"guid" | "eventName"    guid or name of event.</li>
   *      <li>"recurrenceId"   recurrence-id of event instance - possibly null.</li>
   * </ul>
   * <p>If the recurrenceId is null and the event is a recurring event we
   * should return the master event only,
   *
   * @param request   BwRequest for parameters
   * @param mode recurrence mode
   * @return EventInfo or null if not found
   */
  protected EventInfo findEvent(final BwRequest request,
                                final Rmode mode) {
    final Client cl = request.getClient();
    EventInfo ev = null;

    final String href = request.getReqPar("href");

    if (href != null) {
      final EventKey ekey = new EventKey(href, false);

      ev = cl.getEvent(ekey.getColPath(),
                       ekey.getName(),
                       ekey.getRecurrenceId());

      if (ev == null) {
        request.error(ClientError.unknownEvent,
                /*eid*/ekey.getName());
        return null;
      } else if (debug()) {
        debug("Get event found " + ev.getEvent());
      }

      return ev;
    }

    final BwCalendar cal = request.getCalendar(true);

    if (cal == null) {
      return null;
    }

    final String guid = request.getReqPar("guid");
    String eventName = request.getReqPar("eventName");
    
    if (eventName == null) {
      eventName = request.getReqPar("contentName");
    }
    
    if (guid != null) {
      if (debug()) {
        debug("Get event by guid");
      }
      String rid = request.getReqPar("recurrenceId");
      // DORECUR is this right?
      final RecurringRetrievalMode rrm;
      if (mode == Rmode.overrides) {
        rrm = RecurringRetrievalMode.overrides;
        rid = null;
      } else if (mode == Rmode.expanded) {
        rrm = RecurringRetrievalMode.expanded;
      } else {
        rrm = new RecurringRetrievalMode(mode);
      }

      final var evs = cl.getEventByUid(cal.getPath(),
                                        guid, rid, rrm).getEntities();
      if (debug()) {
        debug("Get event by guid found " + evs.size());
      }
      if (evs.size() == 1) {
        ev = evs.iterator().next();
      } else if (evs.size() > 1) {
        // XXX this needs dealing with
        warn("Multiple result from getEvent");
      }
    } else if (eventName != null) {
      if (debug()) {
        debug("Get event by name");
      }

      ev = cl.getEvent(cal.getPath(), eventName,
                       null);
    }

    if (ev == null) {
      request.error(ClientError.unknownEvent, /*eid*/
                            guid);
      return null;
    } else if (debug()) {
      debug("Get event found " + ev.getEvent());
    }

    return ev;
  }

  protected BwCalendar findCalendar(final BwRequest request,
                                    final String url) {
    if (url == null) {
      return null;
    }

    return request.getClient().getCollection(url);
  }

  /** An image processed to produce a thumbnail and storeable resources
   *
   * @author douglm
   */
  public static class ProcessedImage {
    /** true for OK -otherwise an error has been emitted */
    public boolean OK;

    /** true for a possibly recoverable error - otherwise we rolled back and
     * should restart */
    public boolean retry;

    /** The file as uploaded */
    public BwResource image;

    /** Reduced to a thumbnail */
    public BwResource thumbnail;
  }

  /** Invalidate session.
   *
   * @param request    HttpServletRequest
   */
  protected void invalidateSession(final Request request) {
    final HttpSession sess = request.getRequest().getSession(false);

    if (sess != null) {
      sess.invalidate();
    }
  }

  /* ********************************************************************
                             view methods
     ******************************************************************** */

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
  protected void gotoDateView(final BwRequest request,
                              final String date,
                              String newViewType) {
    final BwModuleState mstate = request.getModule().getState();

    //BwActionFormBase form = request.getBwForm();
    /* We get a new view if either the date changed or the view changed.
     */
    boolean newView = false;

    if (debug()) {
      debug("ViewType=" + newViewType);
    }

    MyCalendarVO dt;

    if (BedeworkDefs.vtToday.equals(newViewType)) {
      final Date jdt = new Date(System.currentTimeMillis());
      dt = new MyCalendarVO(jdt);
      newView = true;
      newViewType = BedeworkDefs.vtDay;
    } else if (date == null) {
      if (BedeworkDefs.vtDay.equals(newViewType)) {
        // selected specific day to display from personal event entry screen.

        final Date jdt = BwDateTimeUtil.getDate(mstate.getViewStartDate().getDateTime());
        dt = new MyCalendarVO(jdt);
        newView = true;
      } else {
        if (debug()) {
          debug("No date supplied: go with current date");
        }

        // Just stay here
        dt = mstate.getViewMcDate();
        if (dt == null) {
          // Just in case
          dt = new MyCalendarVO(new Date(System.currentTimeMillis()));
        }
      }
    } else {
      if (debug()) {
        debug("Date=" + date + ": go with that");
      }

      Date jdt = DateTimeUtil.fromISODate(date);
      dt = new MyCalendarVO(jdt);
      if (!checkDateInRange(request, dt.getYear())) {
        // Set it to today
        jdt = new Date(System.currentTimeMillis());
        dt = new MyCalendarVO(jdt);
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
        if (debug()) {
          debug("vsdate=" + vsdate);
        }

        if (!(vsdate.equals(request.getSess().getCurTimeView(request).getFirstDayFmt().getDateDigits()))) {
          newView = true;
          newViewType = mstate.getViewType();
          final Date jdt = DateTimeUtil.fromISODate(vsdate);
          dt = new MyCalendarVO(jdt);
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
  protected void setViewDate(final BwRequest request,
                             final String date) {
    final BwModuleState mstate = request.getModule().getState();
    Date jdt = DateTimeUtil.fromISODate(date);
    MyCalendarVO dt = new MyCalendarVO(jdt);

    if (debug()) {
      debug("calvo dt = " + dt);
    }

    if (!checkDateInRange(request, dt.getYear())) {
      // Set it to today
      jdt = new Date(System.currentTimeMillis());
      dt = new MyCalendarVO(jdt);
    }
    mstate.setViewMcDate(dt);
    mstate.setRefresh(true);
  }

  /* ********************************************************************
                             private methods
     ******************************************************************** */

  private boolean checkDateInRange(final BwRequest req,
                                   final int year) {
    final BwActionFormBase form = req.getBwForm();

    // XXX make system parameters for allowable start/end year
    final int thisYear = form.getToday().getFormatted().getYear();

    if ((year < (thisYear - 50)) || (year > (thisYear + 50))) {
      req.error(ValidationError.invalidDate, year);
      return false;
    }

    return true;
  }

  /** Get the session state object for a web session. If we've already been
   * here it's embedded in the current session. Otherwise create a new one.
   *
   * <p>We also carry out a number of web related operations.
   *
   * @param request       HttpServletRequest Needed to locate session
   * @param form          Action form
   * @param adminUserId   id we want to administer
   * @return BwSession never null
   */
  private BwSession getState(final BwRequest request,
                             final BwActionFormBase form,
                             final String adminUserId,
                             final ConfigCommon conf) {
    BwSession s = BwWebUtil.getState(request.getRequest());
    final HttpSession sess = request.getRequest().getSession(false);
    final String appName = getAppName(sess);

    if (s != null) {
      if (debug()) {
        debug("getState-- obtainedfrom session");
        debug("getState-- timeout interval = " +
                      sess.getMaxInactiveInterval());
      }

      form.assignNewSession(false);
    } else {
      if (debug()) {
        debug("getState-- get new object");
      }

      form.assignNewSession(true);

      s = new BwSessionImpl(conf,
                            form.getCurrentUser(),
                            appName);

      BwWebUtil.setState(request.getRequest(), s);

      final String raddr = request.getRemoteAddr();
      final String rhost = request.getRemoteHost();
      info("===============" + appName + ": New session (" +
                   s.getSessionNum() + ") from " +
                   rhost + "(" + raddr + ")");
    }

    /* Ensure we have a client
       */

    request.getModule().
            checkClient(request, s, adminUserId, false, conf);

    return s;
  }

  private String getAppName(final HttpSession sess) {
    final ServletContext sc = sess.getServletContext();

    String appname = sc.getInitParameter(appNameInitParameter);
    if (appname == null) {
      appname = "?";
    }

    return appname;
  }

  /** Check for request parameter setting a module variable
   * We expect the request parameter to be of the form<br/>
   * setmvar=name(value) or <br/>
   * setmvar=name{value}<p>.
   *  Currently we're not escaping characters so if you want both right
   *  terminators in the value you're out of luck - actually we cheat a bit
   *  We just look at the last char and then look for that from the start.
   *
   * @param request  Needed to locate session
   * @return true if no error found.
   */
  private boolean checkMvarReq(final BwRequest request) {
    final Collection<String> mvs = request.getReqPars("setmvar");
    if (mvs == null) {
      return true;
    }

    final BwModuleState state = request.getModule().getState();

    for (final String reqpar: mvs) {
      final int start;

      if (reqpar.endsWith("}")) {
        start = reqpar.indexOf('{');
      } else if (reqpar.endsWith(")")) {
        start = reqpar.indexOf('(');
      } else {
        return false;
      }

      if (start < 0) {
        return false;
      }

      final String varName = reqpar.substring(0, start);
      String varVal = reqpar.substring(start + 1, reqpar.length() - 1);

      if (varVal.isEmpty()) {
        varVal = null;
      }

      if (!state.setVar(varName, varVal)) {
        return false;
      }
    }

    return true;
  }

  private String suffixRoot(final BwRequest req,
                            final String val) {
    final BwActionFormBase form = req.getBwForm();

    final StringBuilder sb = new StringBuilder(val);

    /* If we're running as a portlet change the app root to point to a
     * portlet specific directory.
     *
     * DISABLED - probably not working
    final String portalPlatform = form.getConfig().getPortalPlatform();

    if (isPortlet && (portalPlatform != null)) {
      sb.append(".");
      sb.append(portalPlatform);
    }
     */

    if (!appTypeWebsubmit.equals(form.getConfig().getAppType())) { 
      /* If calendar suite is non-null append that. */
      final String calSuite = form.getConfig().getCalSuite();
      if (calSuite != null) {
        sb.append(".");
        sb.append(calSuite);
      }
    }

    return sb.toString();
  }

  /** Return the value or a default if it's invalid
   *
   * @param val to check
   * @return String valid view period
   */
  public String validViewPeriod(String val) {
    int vt = BedeworkDefs.defaultView;

    val = Util.checkNull(val);
    if (val != null) {
      final Integer i = BwRequest.viewTypeMap.get(val);

      if (i != null) {
        vt = i;
      }
    }

    return BedeworkDefs.viewPeriodNames[vt];
  }

  public BwActionFormBase getBwForm() {
    return (BwActionFormBase)getForm();
  }

  public void setCalPath(final String val) {
    getBwForm().setCalPath(val);
  }

  /**
   * @return cal Path
   */
  public String getCalPath() {
    return getBwForm().getCalPath();
  }

  /* ============================================================
   *                 Request parameters
   * ============================================================ */

  public void setCount(final String val) {
    // Handled elsewhere.
  }

  public void setFexpr(final String val) {
    // Handled elsewhere.
  }

  public void setSort(final String val) {
    // Handled elsewhere.
  }

  public void setStart(final String val) {
    // Handled elsewhere.
  }

  public void setViewType(final String val) {
    viewType = val;
  }

  public String getViewType() {
    return viewType;
  }
}
