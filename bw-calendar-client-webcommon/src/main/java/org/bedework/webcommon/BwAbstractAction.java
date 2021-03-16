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
import org.bedework.appcommon.ImageProcessing;
import org.bedework.appcommon.InOutBoxInfo;
import org.bedework.appcommon.MyCalendarVO;
import org.bedework.appcommon.TimeView;
import org.bedework.appcommon.client.Client;
import org.bedework.appcommon.client.SearchParams;
import org.bedework.caldav.util.filter.FilterBase;
import org.bedework.calfacade.BwCalendar;
import org.bedework.calfacade.BwCategory;
import org.bedework.calfacade.BwDateTime;
import org.bedework.calfacade.BwEvent;
import org.bedework.calfacade.BwPrincipal;
import org.bedework.calfacade.BwResource;
import org.bedework.calfacade.RecurringRetrievalMode;
import org.bedework.calfacade.RecurringRetrievalMode.Rmode;
import org.bedework.calfacade.base.BwTimeRange;
import org.bedework.calfacade.base.CategorisedEntity;
import org.bedework.calfacade.configs.AuthProperties;
import org.bedework.calfacade.exc.CalFacadeAccessException;
import org.bedework.calfacade.exc.CalFacadeException;
import org.bedework.calfacade.exc.ValidationError;
import org.bedework.calfacade.filter.BwCollectionFilter;
import org.bedework.calfacade.filter.BwCreatorFilter;
import org.bedework.calfacade.filter.SimpleFilterParser.ParseResult;
import org.bedework.calfacade.locale.BwLocale;
import org.bedework.calfacade.responses.GetFilterDefResponse;
import org.bedework.calfacade.svc.BwPreferences;
import org.bedework.calfacade.svc.EventInfo;
import org.bedework.calfacade.util.BwDateTimeUtil;
import org.bedework.calfacade.util.ChangeTable;
import org.bedework.calfacade.util.ChangeTableEntry;
import org.bedework.client.admin.AdminClient;
import org.bedework.client.rw.NotificationInfo;
import org.bedework.client.rw.RWClient;
import org.bedework.util.calendar.PropertyIndex.PropertyInfoIndex;
import org.bedework.util.calendar.XcalUtil;
import org.bedework.util.misc.Util;
import org.bedework.util.misc.response.Response;
import org.bedework.util.servlet.filters.ConfiguredXSLTFilter.XSLTConfig;
import org.bedework.util.servlet.filters.PresentationState;
import org.bedework.util.struts.Request;
import org.bedework.util.struts.UtilAbstractAction;
import org.bedework.util.struts.UtilActionForm;
import org.bedework.util.timezones.DateTimeUtil;
import org.bedework.util.timezones.Timezones;
import org.bedework.webcommon.config.ClientConfigurations;

import org.apache.struts.upload.FormFile;
import org.apache.struts.util.MessageResources;

import java.io.ByteArrayInputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.Locale;
import java.util.Set;
import java.util.TreeSet;

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
  
  private static boolean configTraced;

  @Override
  public String getId() {
    return getClass().getName();
  }

  /** This is the routine which does the work.
   *
   * @param request   For request pars and BwSession
   * @param frm       Action form
   * @return int      forward index
   * @throws Throwable on fatal error
   */
  public abstract int doAction(BwRequest request,
                               BwActionFormBase frm) throws Throwable;

  @Override
  public String performAction(final Request request,
                              final MessageResources messages) throws Throwable {
    final BwActionFormBase form = (BwActionFormBase)request.getForm();
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
      invalidateSession(request);
      return forwards[forwardError];
    }

    final ConfigCommon conf = setConfig(request);

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

    final BwSession bsess = getState(request, form,
                                     adminUserId, conf);

    if (debug()) {
      debug("Obtained state");
    }

    form.setSession(bsess);

    final BwRequest bwreq = new BwRequest(request, bsess, this);

    if (bwreq.present("refresh")) {
      bwreq.refresh();
    }

    checkMvarReq(bwreq);

    final Client cl = bwreq.getClient();
    final BwModuleState mstate = bwreq.getModule().getState();

    setLocale(bwreq, mstate, form);

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
      if (debug() && !configTraced) {
        traceConfig(request);
        configTraced = true;
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
        final String viewType = request.getReqPar("viewType");
        if (viewType != null) {
          mstate.setViewType(viewType);
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

    final PresentationState ps = getPresentationState(request);

    if (ps.getAppRoot() == null) {
      ps.setAppRoot(suffixRoot(request,
                               form.getConfig().getAppRoot()));
      ps.setBrowserResourceRoot(suffixRoot(request,
                                           form.getConfig().getBrowserResourceRoot()));

      // Set the default skin

      if (ps.getSkinName() == null) {
        // No skin name supplied - use the default
        final String skinName = prefs.getSkinName();

        ps.setSkinName(skinName);
        ps.setSkinNameSticky(true);
      }
    }

    /* Set up ready for the action - may reset svci */

    final int temp = bwreq.getModule().actionSetup(bwreq, form);
    if (temp != forwardNoAction) {
      return forwards[temp];
    }

    /* see if we got cancelled */

    final String reqpar = request.getReqPar("cancelled");

    if (reqpar != null) {
      /* Set the objects to null so we get new ones.
       */
      form.getMsg().emit(ClientMessage.cancelled);
      return forwards[forwardCancelled];
    }

    if (!form.getGuest()) {
      final RWClient rwcl = (RWClient)cl;

      if (!cl.getPublicAdmin()) {
        InOutBoxInfo ib = form.getInBoxInfo();
        if (ib == null) {
          ib = new InOutBoxInfo(cl, true);
          form.setInBoxInfo(ib);
        } else {
          ib.refresh(cl, false);
        }
      }

      if ((!cl.getPublicAdmin() ||
                   ((AdminClient)cl).getGroupSet())) {
        NotificationInfo ni = form.getNotificationInfo();
        if (ni == null) {
          ni = new NotificationInfo();
          form.setNotificationInfo(ni);
        }

        ni.refresh(rwcl, false);
      }
    }

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
      form.getErr().emit(ClientError.noAccess);
      if (debug()) {
        error(cfae);
      }
      forward = forwards[forwardNoAccess];
      cl.rollback();
    } catch (final CalFacadeException cfe) {
      form.getErr().emit(cfe.getMessage(), cfe.getExtra());
      form.getErr().emit(cfe);
      if (debug()) {
        error(cfe);
      }

      forward = forwards[forwardError];
      cl.rollback();
    } catch (final Throwable t) {
      form.getErr().emit(t);
      if (debug()) {
        error(t);
      }
      forward = forwards[forwardError];
      cl.rollback();
    }

    return forward;
  }

  private void setLocale(final BwRequest request,
                         final BwModuleState mstate,
                         final BwActionFormBase form) {
    final Collection<Locale> reqLocales = request.getLocales();
    final String reqLoc = request.getReqPar("locale");

    if (reqLoc != null) {
      if ("default".equals(reqLoc)) {
        form.setRequestedLocale(null);
      } else {
        try {
          final Locale loc = Util.makeLocale(reqLoc);
          form.setRequestedLocale(loc); // Make it stick
        } catch (final Throwable t) {
          // Ignore bad parameter?
        }
      }
    }

    final Locale loc =
            request.getClient().getUserLocale(reqLocales,
                                        form.getRequestedLocale());

    if (loc != null) {
      BwLocale.setLocale(loc);
      final Locale cloc = form.getCurrentLocale();
      if ((cloc == null) || (!cloc.equals(loc))) {
        mstate.setRefresh(true);
      }
      form.setCurrentLocale(loc);
    }
  }

  /** Set the config object.
   *
   * @param request wrapper
   * @return config object
   * @throws Throwable on fatal error
   */
  public ConfigCommon setConfig(final Request request) throws Throwable {
    final BwActionFormBase form = (BwActionFormBase)request.getForm();

    if (form.configSet()) {
      return form.getConfig();
    }

    final HttpSession session = request.getRequest().getSession();
    final ServletContext sc = session.getServletContext();

    String appname = sc.getInitParameter("bwappname");

    if ((appname == null) || (appname.length() == 0)) {
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

  @Override
  public PresentationState getPresentationState(final Request req) {
    /* First try to get it from the module. */

    final BwActionFormBase form = (BwActionFormBase)req.getForm();

    final BwModule module = form.fetchModule(req.getModuleName());
    final BwModuleState mstate = module.getState();

    PresentationState ps = mstate.getPresentationState();

    if (ps == null) {
      ps = new PresentationState();
      initPresentationState(req, ps);

      mstate.setPresentationState(ps);
    }

    XSLTConfig xc = mstate.getXsltConfig();

    if (xc == null) {
      final Object o = req.getRequestAttr(ModuleXsltFilter.globalsName);

      if (o instanceof XSLTConfig) {
        xc = (XSLTConfig)o;
      } else {
        xc = new XSLTConfig();
      }

      mstate.setXsltConfig(xc);
    }

    req.setRequestAttr(ModuleXsltFilter.globalsName, xc);

    return ps;
  }

  protected int setSearchParams(final BwRequest request,
                                final SearchParams params,
                                final boolean gridMode) throws Throwable {
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
          form.getErr().emit(ClientError.badRequest, "dates");
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
                                 final SearchParams params) throws Throwable {
    final BwActionFormBase form = request.getBwForm();
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

    /*
       Not sure this is the best place...
       If the filter is null for a user calendar - add a filter to
       exclude the inbox.
     */

    if (cl.getWebUser() && (filter == null)) {
      final var inBoxInfo = form.getInBoxInfo();
      if ((inBoxInfo != null) && (inBoxInfo.getColPath() != null)) {
        filter = new BwCollectionFilter(null,
                                        inBoxInfo.getColPath());
        filter.setNot(true);
      }
    }

    params.setFilter(filter);

    String sort = request.getReqPar("sort");
    if (sort == null) {
      // TODO - this shouldn't be a fixed string
      sort = "dtstart.utc:asc";
    }

    final ParseResult pr = cl.parseSort(sort);
    if (!pr.ok) {
      form.getErr().emit(pr.message);
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
                                final EventKey ekey) throws Throwable {
    final Client cl = request.getClient();
    EventInfo ev = null;

    if (ekey.getColPath() == null) {
      // bogus request
      request.getErr().emit(ValidationError.missingCalendarPath);
      return null;
    }

    String key = null;

    if (ekey.getGuid() != null) {
      if (debug()) {
        debug("Get event by guid");
      }
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
      request.getErr().emit(ClientError.unknownEvent, key);
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
  protected BwPrincipal findPrincipal(final BwRequest request) {
    final String str = request.getReqPar("user");
    if (str == null) {
      request.getErr().emit(ClientError.unknownUser, "null");
      return null;
    }

    final BwPrincipal p = request.getClient().getUser(str);
    if (p == null) {
      request.getErr().emit(ClientError.unknownUser, str);
      return null;
    }

    return p;
  }

  protected static class SetEntityCategoriesResult {
    /** rc */
    public int rcode = forwardNoAction;

    /** Number of BwCategory created */
    public int numCreated;

    /** Number of BwCategory added */
    public int numAdded;

    /** Number of BwCategory removed */
    public int numRemoved;
  }

  /** Set the entity categories based on multivalued request parameter "categoryKey".
   *
   * <p>We build a list of categories then update the membership of the entity
   * category collection to correspond.
   *
   * @param request     BwRequest for parameters
   * @param extraCats Categories to add as a result of other operations
   * @param ent the entity
   * @param changes change table
   * @return setEventCategoriesResult  with rcode = error forward or
   *                    forwardNoAction for validated OK or
   *                    forwardSuccess for calendar changed
   * @throws Throwable on fatal error
   */
  protected SetEntityCategoriesResult setEntityCategories(
          final BwRequest request,
          final Set<BwCategory> extraCats,
          final CategorisedEntity ent,
          final ChangeTable changes) throws Throwable {
    final RWClient cl = (RWClient)request.getClient();

    // XXX We should use the change table code for this.
    final SetEntityCategoriesResult secr = new SetEntityCategoriesResult();

    /* categories already set in event */
    final Set<BwCategory> evcats = ent.getCategories();

    final Set<String> defCatuids = cl.getPreferences().getDefaultCategoryUids();

    /* allDefCatUids is used to preserve the default categories for
       calendar suites. For example we migth have an event created by
       Admissions with a default category of .admissions.

       If it was suggested to and accepted by Payroll it might have
       .payroll applied.

       The assumption (and I think experience) was that .payroll would
       not appear in the update request so would get lost on update.

       This code was being applied to all categorised entities and resulted in
       being unable to turn off a default category on an alias.

       I believe it should only be applied to events.
     */
    /* Get the uids of all public default categories */
    final Set<String> allDefCatUids;
    if (ent instanceof BwEvent) {
      allDefCatUids = cl.getDefaultPublicCategoryUids();
    } else {
      allDefCatUids = null;
    }

    /* Get the uids */
    final Collection<String> strCatUids = request.getReqPars("catUid");

    /* Remove all categories if we don't supply any
     */

    if (Util.isEmpty(strCatUids) &&
        Util.isEmpty(extraCats) &&
        Util.isEmpty(defCatuids) &&
        Util.isEmpty(allDefCatUids)) {
      if (!Util.isEmpty(evcats)) {
        if (changes != null) {
          final ChangeTableEntry cte = changes.getEntry(PropertyInfoIndex.CATEGORIES);
          cte.setRemovedValues(new ArrayList<>(evcats));
        }

        secr.numRemoved = evcats.size();
        evcats.clear();
      }
      secr.rcode = forwardSuccess;
      return secr;
    }

    final Set<BwCategory> cats = new TreeSet<>();

    if (extraCats != null) {
      cats.addAll(extraCats);
    }

    if (!Util.isEmpty(defCatuids)) {
      for (final String uid: defCatuids) {
        cats.add(cl.getPersistentCategory(uid));
      }
    }

    /* Preserve any default public category that we have set by adding it
       to the cats list.
     */
    if (!Util.isEmpty(allDefCatUids) &&
        (evcats != null)) {
      buildList:
      for (final String catUid: allDefCatUids) {
        /* If it's in the event add it to the list we're building then move on
         * to the next requested category.
         */
        for (final BwCategory evcat: evcats) {
          if (evcat.getUid().equals(catUid)) {
            cats.add(evcat);
            continue buildList;
          }
        }
      }
    }

    if (!Util.isEmpty(strCatUids)) {
      buildList:
      for (final String catUid: strCatUids) {
        /* If it's in the event add it to the list we're building then move on
         * to the next requested category.
         */
        if (evcats != null) {
          for (final BwCategory evcat: evcats) {
            if (evcat.getUid().equals(catUid)) {
              cats.add(evcat);
              continue buildList;
            }
          }
        }

        final BwCategory cat = cl.getPersistentCategory(catUid);

        if (cat != null) {
          cats.add(cat);
        }
      }
    }

    /* See if the user is adding new categories 

    final Collection<String> reqCatKeys = request.getReqPars("categoryKey");

    if (!Util.isEmpty(reqCatKeys)) {
      final Collection<String> catKeys = new ArrayList<>();

      / * request parameter can be comma delimited list * /
      for (final String catkey: reqCatKeys) {
        final String[] parts = catkey.split(",");

        for (String part: parts) {
          if (part == null) {
            continue;
          }

          part = part.trim();

          if (part.length() == 0) {
            continue;
          }

          catKeys.add(part);
        }
      }

      for (final String catkey: catKeys) {
        // LANG - use current language code?
        final BwString key = new BwString(null, catkey);

        BwCategory cat = cl.getCategoryByName(key);
        if (cat == null) {
          cat = BwCategory.makeCategory();

          cat.setOwnerHref(cl.getCurrentPrincipalHref());
          cat.setWord(key);

          cl.addCategory(cat);
          secr.numCreated++;
        }

        cats.add(cat);
      }
    }
    */

    /* cats now contains category objects corresponding to the request parameters
     *
     * Now we need to add or remove any in the event but not in our list.
     */

    /* First make a list to remove - to avoid concurrent update
     * problems with the iterator
     */

    final ArrayList<BwCategory> toRemove = new ArrayList<>();

    if (evcats != null) {
      for (final BwCategory evcat: evcats) {
        if (cats.contains(evcat)) {
          cats.remove(evcat);
          continue;
        }

        toRemove.add(evcat);
      }
    }

    for (final BwCategory cat: cats) {
      ent.addCategory(cat);
      secr.numAdded++;
    }

    for (final BwCategory cat: toRemove) {
      if (evcats.remove(cat)) {
        secr.numRemoved++;
      }
    }

    if ((changes != null)  &&
        (secr.numAdded > 0) && (secr.numRemoved > 0)) {
      final ChangeTableEntry cte =
              changes.getEntry(PropertyInfoIndex.CATEGORIES);
      cte.setRemovedValues(toRemove);
      cte.setAddedValues(cats);
    }

    secr.rcode = forwardSuccess;

    if (secr.numCreated > 0) {
      request.getMsg().emit(ClientMessage.addedCategories, secr.numCreated);
    }

    return secr;
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
   * @throws Throwable on fatal error
   */
  protected EventInfo findEvent(final BwRequest request,
                                final Rmode mode) throws Throwable {
    final Client cl = request.getClient();
    EventInfo ev = null;

    final String href = request.getReqPar("href");

    if (href != null) {
      final EventKey ekey = new EventKey(href, false);

      ev = cl.getEvent(ekey.getColPath(),
                       ekey.getName(),
                       ekey.getRecurrenceId());

      if (ev == null) {
        request.getForm().getErr().emit(ClientError.unknownEvent,
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
      request.getForm().getErr().emit(ClientError.unknownEvent, /*eid*/
                                      guid);
      return null;
    } else if (debug()) {
      debug("Get event found " + ev.getEvent());
    }

    return ev;
  }

  protected BwCalendar findCalendar(final BwRequest request,
                                    final String url) throws CalFacadeException {
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

  /** Create resource entities based on the uploaded file.
   *
   * @param request BwRequest object
   * @param file - uploaded
   * @return never null.
   */
  protected ProcessedImage processImage(final BwRequest request,
                                        final FormFile file) {
    final ProcessedImage pi = new ProcessedImage();
    final RWClient cl = (RWClient)request.getClient();

    try {
      final long maxSize = cl.getUserMaxEntitySize();

      if (file.getFileSize() > maxSize) {
        request.getErr().emit(ValidationError.tooLarge, file.getFileSize(), maxSize);
        pi.retry = true;
        return pi;
      }

      /* If the user has set a default images directory preference it must exist.
       * Otherwise we use a system default. For the moment we
       * try to create a folder called "Images"
       */

      BwCalendar imageCol;

      String imagecolPath = cl.getPreferences().getDefaultImageDirectory();
      if (imagecolPath == null) {
        final BwCalendar home = cl.getHome();

        final String imageColName = "Images";

        imagecolPath = Util.buildPath(false, home.getPath(), "/",
                                      imageColName);

//        for (BwCalendar col: cl.getChildren(home)) {
//          if (col.getName().equals(imageColName)) {
//            imageCol = col;
//            break;
//          }
//        }

        imageCol = cl.getCollection(imagecolPath);

        if (imageCol == null) {
          imageCol = new BwCalendar();

          imageCol.setSummary(imageColName);
          imageCol.setName(imageColName);
          imageCol = cl.addCollection(imageCol, home.getPath());
        }
      } else {
        imageCol = cl.getCollection(imagecolPath);
        if (imageCol == null) {
          request.getErr().emit(ClientError.missingImageDirectory);
          return pi;
        }
      }

      final String thumbType = "png";
      final Filenames fns = makeFilenames(file.getFileName(),
                                          thumbType);

      /* See if the resource exists already */

      boolean replace = false;
      boolean replaceThumb = false;

      pi.image = cl.getResource(
              Util.buildPath(false, imageCol.getPath(), "/", fns.fn));

      if (pi.image != null) {
        if (!request.getBooleanReqPar("replaceImage", false)) {
          request.getErr().emit(ClientError.duplicateImage);
          pi.retry = true;
          return pi;
        }

        replace = true;
        
        if (!cl.getResourceContent(pi.image)) {
          request.getErr().emit("Missing content for " +
                                        imageCol.getPath() + "/" + fns.fn);
          pi.retry = true;
          return pi;
        }
      } else {
        pi.image = new BwResource();
        pi.image.setColPath(imagecolPath);
        pi.image.setName(fns.fn);
      }

      final byte[] fileData = file.getFileData();
      final byte[] thumbContent;

      try {
        thumbContent = ImageProcessing.createThumbnail(
               new ByteArrayInputStream(fileData),
               thumbType, 160);
      } catch (final Throwable t) {
        /* Probably an image type we can't process or maybe not an image at all
         */
        if (debug()) {
          error(t);
        }

        request.getErr().emit(ClientError.imageError);
        pi.retry = true;
        return pi;
      }

      cl.setResourceValue(pi.image, fileData);
      pi.image.setContentType(file.getContentType());

      /* Make a thumbnail */

      pi.thumbnail = cl.getResource(
              Util.buildPath(false, imageCol.getPath(), "/",
                             fns.thumbFn));

      if (pi.thumbnail != null) {
        replaceThumb = true;
        if (!cl.getResourceContent(pi.image)) {
          request.getErr().emit("Missing content for " +
                                        imageCol.getPath() + "/" + 
                                        fns.thumbFn);
          pi.retry = true;
          return pi;
        }
      } else {
        pi.thumbnail = new BwResource();
        pi.thumbnail.setName(fns.thumbFn);
        pi.thumbnail.setColPath(imagecolPath);
      }

      pi.thumbnail.setContentType("image/" + thumbType);
      
      cl.setResourceValue(pi.thumbnail, thumbContent);

      if (!replace) {
        cl.saveResource(pi.image);
      } else {
        cl.updateResource(pi.image, true);
      }

      if (!replaceThumb) {
        cl.saveResource(pi.thumbnail);
      } else {
        cl.updateResource(pi.thumbnail, true);
      }

      pi.OK = true;
    } catch (final Throwable t) {
      if (debug()) {
        error(t);
        request.getErr().emit(t);
      }
    }

    return pi;
  }

  private static class Filenames {
    String fn;
    String thumbFn;
  }

  /**
   *
   * @param imageName from upload
   * @param thumbType "png" etc
   * @return datestamped names
   */
  private Filenames makeFilenames(final String imageName,
                                  final String thumbType) {
    final int dotPos = imageName.lastIndexOf('.');
    final Filenames fns = new Filenames();

    final String dt = new SimpleDateFormat("yyyyMMddhhmm").format(new Date());

    if (dotPos < 0) {
      fns.fn = imageName + "-" + dt;
      fns.thumbFn = fns.fn + "-thumb" + "." + thumbType;
    } else {
      final String namePart = imageName.substring(0, dotPos) + "-" + dt;
      fns.fn = namePart + imageName.substring(dotPos);
      fns.thumbFn =  namePart + "-thumb" + "." + thumbType;
    }

    return fns;
  }

  @Override
  protected boolean logOutCleanup(final HttpServletRequest request,
                                  final UtilActionForm form) {
    final HttpSession hsess = request.getSession();
    final BwCallback cb =
            (BwCallback)hsess.getAttribute(BwCallback.cbAttrName);

    if (cb == null) {
      if (form.getDebug()) {
        debug("No cb object for logout");
      }
    } else {
      if (form.getDebug()) {
        debug("cb object found for logout");
      }
      try {
        cb.out(request);
      } catch (final Throwable ignored) {}

      try {
        cb.close(request, true);
      } catch (final Throwable ignored) {}
    }

    return true;
  }

  /** Check for logout request. Overridden so we can close anything we
   * need to before the session is invalidated.
   *
   * @param request    HttpServletRequest
   * @return null for continue, forwardLoggedOut to end session.
   */
  protected String checkLogOut(final Request request) {
    final String temp = request.getReqPar(requestLogout);
    if (temp != null) {
      final HttpSession sess = request.getRequest().getSession(false);

      if (sess != null) {
        /* I don't think I need this - we didn't come through the svci filter on the
           way in?
        UWCalCallback cb = (UWCalCallback)sess.getAttribute(UWCalCallback.cbAttrName);

        try {
          if (cb != null) {
            cb.out();
          }
        } catch (Throwable t) {
          error("Callback exception: ", t);
          /* Probably no point in throwing it. We're leaving anyway. * /
        } finally {
          if (cb != null) {
            try {
              cb.close();
            } catch (Throwable t) {
              error("Callback exception: ", t);
              /* Probably no point in throwing it. We're leaving anyway. * /
            }
          }
        }
        */

        sess.invalidate();
      }
      return forwardLoggedOut;
    }

    return null;
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
   * @throws Throwable on fatal error
   */
  protected void gotoDateView(final BwRequest request,
                              final String date,
                              String newViewType) throws Throwable {
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
      form.getErr().emit(ValidationError.invalidDate, year);
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
   * @throws Throwable on fatal error
   */
  private BwSession getState(final Request request,
                             final BwActionFormBase form,
                             final String adminUserId,
                             final ConfigCommon conf) throws Throwable {
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

    form.fetchModule(request.getModuleName()).
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
   * @throws Throwable on fatal error
   */
  private boolean checkMvarReq(final BwRequest request) throws Throwable {
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

      if (varVal.length() == 0) {
        varVal = null;
      }

      if (!state.setVar(varName, varVal)) {
        return false;
      }
    }

    return true;
  }

  private String suffixRoot(final Request req,
                            final String val) {
    final BwActionFormBase form = (BwActionFormBase)req.getForm();

    final StringBuilder sb = new StringBuilder(val);

    /* If we're running as a portlet change the app root to point to a
     * portlet specific directory.
     */
    final String portalPlatform = form.getConfig().getPortalPlatform();

    if (isPortlet && (portalPlatform != null)) {
      sb.append(".");
      sb.append(portalPlatform);
    }

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
}
