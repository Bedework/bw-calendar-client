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
import org.bedework.appcommon.ClientError;
import org.bedework.appcommon.EventKey;
import org.bedework.appcommon.client.Client;
import org.bedework.calfacade.BwCalendar;
import org.bedework.calfacade.BwDateTime;
import org.bedework.calfacade.BwEvent;
import org.bedework.calfacade.BwFilterDef;
import org.bedework.calfacade.exc.CalFacadeException;
import org.bedework.calfacade.exc.ValidationError;
import org.bedework.calfacade.filter.SimpleFilterParser.ParseResult;
import org.bedework.calfacade.responses.GetFilterDefResponse;
import org.bedework.calfacade.svc.EventInfo;
import org.bedework.calfacade.util.ChangeTable;
import org.bedework.util.calendar.IcalDefs;
import org.bedework.util.calendar.PropertyIndex.PropertyInfoIndex;
import org.bedework.util.calendar.ScheduleMethods;
import org.bedework.util.misc.Util;
import org.bedework.util.misc.response.Response;
import org.bedework.util.servlet.filters.ConfiguredXSLTFilter;
import org.bedework.util.servlet.filters.PresentationState;
import org.bedework.util.timezones.Timezones;
import org.bedework.util.webaction.ErrorEmitSvlt;
import org.bedework.util.webaction.MessageEmitSvlt;
import org.bedework.util.webaction.Request;
import org.bedework.util.webaction.WebActionForm;
import org.bedework.util.webaction.WebGlobals;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * @author douglm
 *
 */
public class BwRequest extends Request {
  private BwSession sess;

  private final String modulesClass;
  private BwModules modules;
  private BwModule module;

  private Client cl;

  static HashMap<String, Integer> viewTypeMap =
          new HashMap<>();

  static {
    for (int i = 0; i < BedeworkDefs.viewPeriodNames.length; i++) {
      viewTypeMap.put(BedeworkDefs.viewPeriodNames[i], i);
    }
  }

  /** client stored in request */
  public final static String embeddedClientName = "bw_embedded_client";

  /** event state stored in request */
  public final static String eventStateName = "bw_event_state";

  /** module state stored in request */
  public final static String moduleStateName = "bw_module_state";

  /** feature flags stored in session */
  public final static String bwFeatureFlagsName = "bw_feature_flags";

  /** cache prefix stored in session */
  public final static String bwCachePrefixName = "bw_cache_prefix";

  /** auth user list stored in session */
  public final static String bwAuthUsersListName = "bw_auth_users";

  /** admin group info list stored in session */
  public final static String bwAdminGroupsInfoName = "bw_admin_groups";

  /** calsuite admin group info list stored in session */
  public final static String bwCsAdminGroupsInfoName = "bw_suite_admin_groups";

  /** preferred admin group info list stored in session */
  public final static String bwPreferredAdminGroupsInfoName = "bw_preferred_admin_groups";

  /** admin groups for this user info list stored in session */
  public final static String bwUserAdminGroupsInfoName = "bw_user_admin_groups";

  /** admin groups this user can search stored in session */
  public final static String bwUserSearchableAdminGroups = "bw_user_admin_search_groups";

  /** Search pars stored in session */
  public final static String bwSearchParamsName = "bw_search_params";

  /** Search result stored in session */
  public final static String bwSearchResultName = "bw_search_result";

  /** Formatted entity list stored in session */
  public final static String bwSearchListName = "bw_search_list";

  /** filter list stored in session */
  public final static String bwFiltersListName = "bw_filters_list";

  /** collection list stored in session */
  public final static String bwCollectionListName = "bw_collection_list";

  /** public collection list stored in session */
  public final static String bwPublicCollectionListName = "bw_public_collection_list";

  /** user collection list stored in session */
  public final static String bwUserCollectionListName = "bw_user_collection_list";

  /** add content collection list stored in session */
  public final static String bwAddContentCollectionListName = "bw_addcontent_collection_list";

  /** preferences stored in session */
  public final static String bwPreferencesName = "bw_preferences";

  /** view list stored in session */
  public final static String bwViewsListName = "bw_views_list";

  /** category list stored in session */
  public final static String bwCategoriesListName = "bw_categories_list";

  /** default category list stored in session */
  public final static String bwDefaultCategoriesListName = "bw_default_categories_list";

  /** editable category list stored in session */
  public final static String bwEditableCategoriesListName = "bw_editable_categories_list";

  /** preferred category list stored in session */
  public final static String bwPreferredCategoriesListName = "bw_preferred_categories_list";

  /** contact list stored in session */
  public final static String bwContactsListName = "bw_contacts_list";

  /** editable contact list stored in session */
  public final static String bwEditableContactsListName = "bw_editable_contacts_list";

  /** preferred contact list stored in session */
  public final static String bwPreferredContactsListName = "bw_preferred_contacts_list";

  /** location list stored in session */
  public final static String bwLocationsListName = "bw_locations_list";

  /** preferred location list stored in session */
  public final static String bwPreferredLocationsListName = "bw_preferred_locations_list";

  /** editable location list stored in session */
  public final static String bwEditableLocationsListName = "bw_editable_locations_list";

  /** requested uid stored in session */
  public final static String bwReqUidName = "bw_req_uid";

  /** vpoll tab requests stored in session */
  public final static String bwReqVpollTabName = "bw_req_vpoll_tab";

  /** default event calendar stored in session */
  public final static String bwDefaultEventCalendar = "bw_default_event_calendar";

  /** subscription status stored in session */
  public final static String bwSubscriptionStatus = "bw_subscription_status";

  /**
   * @param request the http request
   * @param response the response
   * @param params action parameters
   * @param actionPath from mapping
   * @param form form object
   */
  public BwRequest(final HttpServletRequest request,
                   final HttpServletResponse response,
                   final Map<String, String> params,
                   final String actionPath,
                   final ErrorEmitSvlt err,
                   final MessageEmitSvlt msg,
                   final WebActionForm form) {
    super(request, response, params, actionPath, err, msg, form);

    final HttpSession session = request.getSession();
    final ServletContext sc = session.getServletContext();

    modulesClass = sc.getInitParameter("modulesClass");
    if (modulesClass == null) {
      throw new RuntimeException("modulesClass not set");
    }

    getPresentationState().reinit(request);
  }

  public BwWebGlobals getBwGlobals() {
    return (BwWebGlobals)getGlobals();
  }

  public WebGlobals getGlobals() {
    var globals = (WebGlobals)getSessionAttr(WebGlobals.webGlobalsAttrName);

    if (globals == null) {

      final HttpSession session = request.getSession();
      final ServletContext sc = session.getServletContext();

      final var globalsClass = sc.getInitParameter("globalsClass");
      if (globalsClass == null) {
        throw new RuntimeException("globalsClass not set");
      }
      globals = (BwWebGlobals)Util.getObject(globalsClass,
                                             BwWebGlobals.class);
      setSessionAttr(WebGlobals.webGlobalsAttrName, globals);
    }

    return globals;
  }

  void setSess(final BwSession val) {
    sess = val;
  }

  /**
   * @return BwSession
   */
  public BwSession getSess() {
    return sess;
  }

  public boolean isNewSession() {
    return (sess == null) || (sess.isNewSession());
  }

  public BwModules getModules() {
    modules = (BwModules)getSessionAttr(BwModules.modulesAttrName);
    if (modules == null) {
      if (debug()) {
        debug("No modules in session - creating " +
                      modulesClass);
      }
      modules = (BwModules)Util.getObject(modulesClass,
                                          BwModules.class);
      setSessionAttr(BwModules.modulesAttrName, modules);
    } else if (debug()) {
      debug("Found modules in session with class " +
                    modules.getClass());
    }

    return modules;
  }

  public BwModule getModule() {
    if (module == null) {
      module = getModules().fetchModule(getModuleName());
      request.setAttribute(moduleStateName, module.getState());
    }

    return module;
  }

  /**
   *
   * @return false if we have an if-none-match which
   * matches current change token. Status is set to not-modified.
   */
  public boolean contentChanged() {
    final String changeToken = cl.getCurrentChangeToken();

    final String ifNoneMatch = request.getHeader("if-none-match");

    if ((changeToken != null) && changeToken.equals(ifNoneMatch)) {
      getResponse().setStatus(HttpServletResponse.SC_NOT_MODIFIED);
      return false;
    }

    return true;
  }

  public void sendError(final int status,
                        final String text) {
    try {
      getResponse().sendError(status, text);
    } catch (final IOException e) {
      throw new CalFacadeException(e);
    }
  }

  public Writer getWriter() {
    try {
      return getResponse().getWriter();
    } catch (final IOException e) {
      throw new CalFacadeException(e);
    }
  }


  @Override
  protected boolean logOutCleanup() {
    final HttpSession hsess = request.getSession();
    final BwCallback cb =
            (BwCallback)hsess.getAttribute(BwCallback.cbAttrName);

    if (cb != null) {
      try {
        cb.out(request);
      } catch (final Throwable ignored) {}

      try {
        cb.close(request, true);
      } catch (final Throwable ignored) {}
    }

    return true;
  }

  public Client getClient() {
    if (cl == null) {
      cl = getModule().getClient();
      request.setAttribute(embeddedClientName, cl);
    }

    return cl;
  }

  public boolean isGuest() {
    return getClient().isGuest();
  }

  /**
   * @return BwActionFormBase
   */
  public BwActionFormBase getBwForm() {
    return (BwActionFormBase)getForm();
  }

  /**
   * Signal we need to refresh the module state
   */
  public void refresh() {
    getModule().getState().setRefresh(true);
  }

  /**
   * @return true for "copy" present
   */
  public boolean hasCopy() {
    return present("copy");
  }

  /**
   * @return true for "delete" present
   */
  public boolean hasDelete() {
    return present("delete");
  }

  /**
   * @return true for "marKeleted" present
   */
  public boolean hasMarkDeleted() {
    return present("markDeleted");
  }

  /**
   * @return true for "export" present
   */
  public boolean hasExport() {
    return present("export");
  }

  /**
   * <p>Request parameters are:<ul>
   *      <li>"entityType"              Absent for an event, otherwise
   *                                    event | task | journal</li>
   * </ul>
   *
   * @return int
   */
  public int getEntityType() {
    final String s = getReqPar("entityType");

    if ((s == null) || "event".equals(s)) {
      return IcalDefs.entityTypeEvent;
    }

    switch (s) {
      case "task" -> {
        return IcalDefs.entityTypeTodo;
      }
      case "journal" -> {
        return IcalDefs.entityTypeJournal;
      }
      case "freebusy" -> {
        return IcalDefs.entityTypeFreeAndBusy;
      }
    }

    getErr().emit(ValidationError.invalidEntityType, s);
    return 0;
  }

  /** If filterName or fexpr is specified will return a parsed filter definition
   *
   * @return GetFilterDefResponse
   */
  public GetFilterDefResponse getFilterDef() {
    final Client cl = getClient();
    final String name = getReqPar("filterName");
    String fexpr = getReqPar("fexpr");

    if ((name == null) && ((fexpr == null) || fexpr.equals("no--filter"))) {
      return new GetFilterDefResponse();
    }

    final GetFilterDefResponse gfdr;

    if (name != null) {
      gfdr = cl.getFilter(name);
      if (gfdr.getStatus() == Response.Status.notFound) {
        error(ClientError.unknownFilter, name);
        return gfdr;
      }
      
      if (gfdr.getStatus() != Response.Status.ok) {
        error(ClientError.exc, gfdr.getMessage());
        return gfdr;
      }
    } else {
      fexpr = fexpr.replace("-_", "|"); //  For the old webcache
      gfdr = new GetFilterDefResponse();
      gfdr.setFilterDef(new BwFilterDef());
      gfdr.getFilterDef().setDefinition(fexpr);
    }

    final BwFilterDef fd = gfdr.getFilterDef();
    
    if (fd.getFilters() == null) {
      final ParseResult pr = cl.parseFilter(fd);
      if (!pr.ok) {
        gfdr.setStatus(Response.Status.failed);
        gfdr.setMessage(pr.message +
                                " expression: " + fd.getDefinition());
        error(gfdr.getMessage());
        return gfdr;
      }
    }

    return gfdr;
  }

  /**
   * @param evDateOnly  true if event says date only values
   * @return Collection of BwDateTime or null
   */
  public Collection<BwDateTime> getRdates(final boolean evDateOnly) {
    return getRExdates(true, evDateOnly);
  }

  /**
   * @param evDateOnly  true if event says date only values
   * @return (possibly empty) Collection of BwDateTime, never null
   */
  public Collection<BwDateTime> getExdates(final boolean evDateOnly) {
    return getRExdates(false, evDateOnly);
  }

  private Collection<BwDateTime> getRExdates(final boolean rdates,
                                             final boolean evDateOnly) {
    final String reqPar;
    final String token = "DATE\t";
    final Collection<BwDateTime> bwdts = new ArrayList<>();

    if (rdates) {
      reqPar = "rdates";
    } else {
      reqPar = "exdates";
    }

    final String dtsPar = getReqPar(reqPar);
    if (dtsPar == null) {
      return bwdts;
    }

    final String[] dts = dtsPar.split(token);

    for (final String dtVal: dts) {
      if ((dtVal == null) || (dtVal.isEmpty())) {
        continue;
      }

      final String[] dtParts = dtVal.split("\t");

      /* date, time, tzid */

      final StringBuilder dtm = new StringBuilder(dtParts[0]);
      boolean dateOnly = true;

      if (!evDateOnly && (Util.checkNull(dtParts[1]) != null)) {
        dtm.append("T");
        dtm.append(dtParts[1]);
        dtm.append("00");  // Seconds

        /*
         * if (UTC) {
             dtm.append("Z");
           }
         *
         */

        dateOnly = false;
      }

      String tzid = null;

      if (!evDateOnly && (dtParts.length > 2)) {
        tzid = Util.checkNull(dtParts[2]);
      }

      bwdts.add(BwDateTime.makeBwDateTime(dateOnly, dtm.toString(), tzid));
    }

    return bwdts;
  }

  /**
   * @return view type - "monthView" etc
   */
  public String getViewType() {
    final String vt = getReqPar("viewType");

    if (vt == null) {
      return null;
    }

    if (viewTypeMap.get(vt) == null) {
      return null;
    }

    return vt;
  }

  /** Get date/time object
   *
   * @param dtPar name of request parameter for date
   * @param tzidPar name of request parameter for tzid
   * @return date/time
   */
  public BwDateTime getDateTime(final String dtPar,
                                final String tzidPar) {
    final String dt = getReqPar(dtPar);
    if (dt == null) {
      return null;
    }

    String tzid  = getReqPar(tzidPar);
    if (tzid == null) {
      tzid = Timezones.getThreadDefaultTzid();
    }

    return BwDateTime.makeBwDateTime(!dt.contains("T"),
                                     dt, tzid);
  }

  /** Get calendar identified by newCalPath. Emits error message for invalid path
   * Returns null for bad or missing calendar.
   *
   * @param required boolean true if we require a calendar.
   * @return calendar or null for invalid path.
   */
  public BwCalendar getNewCal(final boolean required) {
    final String newCalPath = getReqPar("newCalPath");

    if (newCalPath == null) {
      if (required) {
        getErr().emit(ValidationError.missingCalendar);
      }
      return null;
    }

    final BwCalendar newCal = getClient().getCollection(newCalPath);
    if (newCal == null) {
      getErr().emit(ClientError.unknownCalendar, newCalPath);
      return null;
    }

    return newCal;
  }

  /** Set the event calendar based on request parameters. If newCalPath
   * is specified we use the named calendar as the event calendar.
   *
   * <p>If newCalPath is not specified and no calendar was already set in the
   * event, we fail with an error.
   *
   * <p>If a calendar was already set in the event, this action will only
   * change that calendar if newCalPath is specified. It will not
   * reset the calendar to the default.
   *
   * @param ei the event
   * @param changes to flag change
   * @return boolean false if none or errors
   */
  public boolean setEventCalendar(final EventInfo ei,
                                  final ChangeTable changes) {
    final BwEvent ev = ei.getEvent();
    final BwCalendar cal = getNewCal(false);

    if (getErrorsEmitted()) {
      return false;
    }

    if ((cal == null) && (ev.getColPath() == null)) {
      getErr().emit(ValidationError.missingCalendar);
      return false;
    }

    if (cal != null) {
      changes.changed(PropertyInfoIndex.COLLECTION,
                      ev.getColPath(),
                      cal.getPath());
      ev.setColPath(cal.getPath());
    }

    return true;
  }

  /** Get calendars identified by multivalued parameter calPath.
   *
   * @return Collection<BwCalendar> (possibly empty).
   */
  public Collection<BwCalendar> getCalendars() {
    final Collection<String> calPaths = getReqPars("calPath");
    final Collection<BwCalendar> cals = new ArrayList<>();

    if (calPaths == null) {
      return cals;
    }

    for (final String calPath: calPaths) {
      final BwCalendar cal = getClient().getCollection(calPath);

      if (cal != null) {
        cals.add(cal);
      }
    }

    return cals;
  }

  /** Get collection identified by single-valued parameter colHref.
   *
   * @param required true => emit error if not present
   * @return BwCalendar or null.
   */
  public BwCalendar getCollection(final boolean required) {
    return getCalendar("colHref", required);
  }

  /** Get calendar user address identified by single-valued parameter cua.
   *
   * @return String or null.
   */
  public String getCua() {
    final String cua = getReqPar("cua");

    if (cua == null) {
      getErr().emit(ClientError.badRequest, "Missing cua");
      return null;
    }

    return cua;
  }

  /** Get calendar identified by single-valued parameter calPath.
   *
   * @param required true => emit error if not present
   * @return BwCalendar or null.
   */
  public BwCalendar getCalendar(final boolean required) {
    if (present("calPath")) {               // TODO - drop this
      return getCalendar("calPath", required);
    }
    return getCalendar("colPath", required);
  }

  /** Get calendar identified by single-valued request parameter specified.
   *
   * @param reqParName name of request parameter
   * @param required true => emit error if not present
   * @return BwCalendar or null.
   */
  public BwCalendar getCalendar(final String reqParName,
                                final boolean required) {
    final String calPath = getReqPar(reqParName);

    if (calPath == null) {
      if (required) {
        // bogus request
        getErr().emit(ValidationError.missingCalendarPath);
      }
      return null;
    }

    final BwCalendar cal = getClient().getCollection(calPath);

    if (cal == null) {
      getErr().emit(ClientError.unknownCalendar, calPath);
      return null;
    }

    return cal;
  }

  /**
   * <p>Request parameters are:<ul>
   *      <li>"schedule=none"           Not a scheduling request</li>
   *      <li>"schedule=request"        Send a scheduling request</li>
   *      <li>"schedule=reconfirm"      Ask for reconfirmation</li>
   *      <li>"schedule=publish"        Publish an event</li>
   * </ul>
   *
   * @return int schedule method
   */
  public int getSchedule() {
    final String schedStr = getReqPar("schedule");

    if (schedStr == null) {
      return ScheduleMethods.methodTypeNone;
    }

    switch (schedStr) {
      case "none" -> {
        return ScheduleMethods.methodTypeNone;
      }
      case "request" -> {
        return ScheduleMethods.methodTypeRequest;
      }
      case "reconfirm" -> {
        // Use refresh to indicate we want to reconfirm
        return ScheduleMethods.methodTypeRefresh;
      }
      case "publish" -> {
        return ScheduleMethods.methodTypePublish;
      }
    }

    getErr().emit(ValidationError.invalidSchedMethod, schedStr);
    return ScheduleMethods.methodTypeNone;
  }

  /**
   * @param forExport true if we are to export the event
   * @return EventKey
   */
  public EventKey makeEventKey(boolean forExport) {
    final String href = getReqPar("href");

    if (href != null) {
      return new EventKey(href, forExport);
    }

    final String calPath = getReqPar("calPath");

    if (calPath == null) {
      // bogus request
      getErr().emit(ValidationError.missingCalendarPath);
      return null;
    }

    final BwCalendar cal = getClient().getCollection(calPath);

    if (cal == null) {
      // Assume no access
      getErr().emit(ClientError.noAccess);
      return null;
    }

    final String guid = getReqPar("guid");
    String rid = null;
    final String eventName = getReqPar("eventName");
    final EventKey ekey;

    if (guid != null) {
      if (getReqPar("master") != null) {
        forExport = true;
      }

      if (!forExport) {
        rid = getReqPar("recurrenceId");
      }

      ekey = new EventKey(calPath, guid, rid, forExport);
    } else if (eventName != null) {
      ekey = new EventKey(calPath, eventName, forExport);
    } else {
      getErr().emit(ClientError.missingEventKeyFields);
      return null;
    }

    return ekey;
  }

  public void embedAdminGroups() {
    setSessionAttr(bwAdminGroupsInfoName,
                   getClient().getAdminGroups());
  }

  /* ====================================================================
   *               Presentation state methods
   * ==================================================================== */

  @Override
  public PresentationState getPresentationState() {
    /* First try to get it from the module. */

    final BwModule module = getModule();
    final BwModuleState mstate = module.getState();

    PresentationState ps = mstate.getPresentationState();

    if (ps == null) {
      ps = new PresentationState().reinit(getRequest());

      mstate.setPresentationState(ps);
    }

    setRequestAttr(PresentationState.presentationAttrName, ps);

    ConfiguredXSLTFilter.XSLTConfig xc = mstate.getXsltConfig();

    if (xc == null) {
      final Object o = getRequestAttr(ModuleXsltFilter.globalsName);

      if (o instanceof ConfiguredXSLTFilter.XSLTConfig) {
        xc = (ConfiguredXSLTFilter.XSLTConfig)o;
      } else {
        xc = new ConfiguredXSLTFilter.XSLTConfig();
      }

      mstate.setXsltConfig(xc);
    }

    setRequestAttr(ModuleXsltFilter.globalsName, xc);

    return ps;
  }
}
