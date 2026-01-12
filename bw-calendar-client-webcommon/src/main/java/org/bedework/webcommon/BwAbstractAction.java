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
import org.bedework.appcommon.client.Client;
import org.bedework.base.exc.BedeworkAccessException;
import org.bedework.base.exc.BedeworkClosed;
import org.bedework.base.exc.BedeworkException;
import org.bedework.calfacade.svc.BwPreferences;
import org.bedework.util.misc.Util;
import org.bedework.util.struts.UtilAbstractAction;
import org.bedework.util.timezones.Timezones;
import org.bedework.util.webaction.ErrorEmitSvlt;
import org.bedework.util.webaction.MessageEmitSvlt;
import org.bedework.util.webaction.Request;
import org.bedework.util.webaction.WebActionForm;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.struts2.interceptor.parameter.StrutsParameter;

import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Map;

import static org.bedework.webcommon.DateViewUtil.gotoDateView;

/** This abstract action performs common setup actions before the real
 * action method is called.
 *
 * @author  Mike Douglass  douglm@rpi.edu
 */
public abstract class BwAbstractAction
        extends UtilAbstractAction
        implements ForwardDefs {
  /** Name of the init parameter holding our name */
  private static final String appNameInitParameter = "bwappname";

  private String viewType;

  private static final ObjectMapper mapper = new ObjectMapper(); // create once, reuse

  static {
    final DateFormat df = new SimpleDateFormat("yyyy'-'MM'-'dd'T'HH':'mm':'ss'Z'");

    mapper.setDateFormat(df);

    mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
  }

  @Override
  public String getId() {
    return getClass().getName();
  }

  /** This is the routine which does the work.
   *
   * @param request   For request pars and BwSession
   * @return int      forward index
   */
  public abstract String doAction(BwRequest request);

  @Override
  public String performAction(final Request request)  {
    final int status;

    try {
      status = BwCallback.getCb(request).in(request);
    } catch (final Throwable t) {
      error(t);
      invalidateSession(request);
      return forwardError;
    }

    if (status != HttpServletResponse.SC_OK) {
      request.getResponse().setStatus(status);
      error("Callback.in status=" + status);
      // Try not invalidating. There are multiple requests and this may
      // cause errors in the one that got through
      //invalidateSession(request);
      return forwardError;
    }

    final BwRequest bwreq = (BwRequest)request;
    final BwActionFormBase form = bwreq.getBwForm();
    final ConfigCommon conf = bwreq.getConfig();
    final var globals = bwreq.getBwGlobals();
    String adminUserId = null;

    if (conf.getGuestMode()) {
      request.clearCurrentUser();
    } else {
      adminUserId = globals.getAdminUserId();
      if (adminUserId == null) {
        adminUserId = request.getCurrentUser();
      }
    }

    if (debug()) {
      debug("About to get state");
    }

    final BwSession bsess = getState(bwreq,
                                     adminUserId, conf);

    final Client cl = bwreq.getClient();
    globals.changeAdminUser(cl.getCurrentPrincipal());

    if (debug()) {
      debug("Obtained state");
    }

    bwreq.setSess(bsess);

    if (bwreq.present("refresh")) {
      bwreq.refresh();
    }

    checkMvarReq(bwreq);

    final BwModuleState mstate = bwreq.getModule().getState();

    // We need to have set the current locale before we do this.
    mstate.setCalInfo(CalendarInfo.getInstance());

    final BwPreferences prefs = cl.getPreferences();

    if (cl.getWebSubmit() && (request.getReqPar("cs") != null)) {
      globals.setCalSuiteName(request.getReqPar("cs"));
    }

    if (bsess.isNewSession()) {
      if (adminUserId != null) {
        final var loginMsg = "Logged in to " + conf.getAppType() +
                " as " + adminUserId;
        if (isAuditLoggerEnabled()) {
          audit(loginMsg);
        }
        info(loginMsg);
      }

      globals.setHour24(conf.getHour24());
      if (!cl.getPublicAdmin() &&
              !cl.getWebSubmit() &&
              !cl.isGuest()) {
        globals.setHour24(prefs.getHour24());
      }

      final String endDateType;
      if (!cl.getPublicAdmin() && !cl.isGuest()) {
        endDateType = prefs.getPreferredEndType();
      } else {
        endDateType = BwPreferences.preferredEndTypeDuration;
      }
      form.assignEventDates(
              new EventDates(cl.getCurrentPrincipalHref(),
                             mstate.getCalInfo(),
                             globals.getHour24(),
                             endDateType,
                             conf.getMinIncrement(),
                             request.getErr()));


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

    if (debug()) {
      debug("About to prepare render");
    }

    bsess.prepareRender(bwreq);

    if (bsess.isNewSession()) {
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

    final var ps = request.getPresentationState();

    if (ps.getAppRoot() == null) {
      ps.setAppRoot(suffixRoot(bwreq, conf.getAppRoot()));
      ps.setBrowserResourceRoot(suffixRoot(bwreq,
                                           conf.getBrowserResourceRoot()));

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
      return forwardCancelled;
    }

    /* Set up ready for the action - may reset svci */
    final BwModule mdl = bwreq.getModule();

    final var temp = mdl.actionSetup(bwreq);
    if (!forwardNoAction.equals(temp)) {
      return temp;
    }

    // May have changed (again)
    globals.changeAdminUser(cl.getCurrentPrincipal());
    mdl.checkMessaging(bwreq);

    String forward;

    try {
      forward = doAction(bwreq);

//      bsess.prepareRender(bwreq);
    } catch (final BedeworkAccessException bae) {
      request.error(ClientError.noAccess);
      if (debug()) {
        error(bae);
      }
      forward = forwardNoAccess;
      cl.rollback();
    } catch (final BedeworkClosed ignored) {
      request.warn(ClientError.closed);
      warn("Interface closed");
      forward = forwardGotomain;
    } catch (final BedeworkException be) {
      request.error(be.getMessage(), be.getExtra());
      request.error(be);
      if (debug()) {
        error(be);
      }

      forward = forwardError;
      cl.rollback();
    } catch (final Throwable t) {
      request.error(t);
      if (debug()) {
        error(t);
      }
      forward = forwardError;
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

  /* **************************************************************
                             private methods
     ************************************************************** */

  /** Invalidate session.
   *
   * @param request    HttpServletRequest
   */
  private void invalidateSession(final Request request) {
    final HttpSession sess = request.getRequest().getSession(false);

    if (sess != null) {
      sess.invalidate();
    }
  }

  /** Get the session state object for a web session. If we've already been
   * here it's embedded in the current session. Otherwise create a new one.
   *
   * <p>We also carry out a number of web related operations.
   *
   * @param request       HttpServletRequest Needed to locate session
   * @param adminUserId   id we want to administer
   * @return BwSession never null
   */
  private BwSession getState(final BwRequest request,
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

      s.resetNewSession();
    } else {
      if (debug()) {
        debug("getState-- get new object");
      }

      s = new BwSessionImpl(conf,
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

    final Client cl = request.getClient();
    request.getBwGlobals().reset(cl);
    s.finishInit(cl.getAuthProperties().cloneIt());

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
    final StringBuilder sb = new StringBuilder(val);

    if (Client.ClientType.submission != req.getClient()
                                           .getClientType()) {
      /* If calendar suite is non-null append that. */
      final String calSuite = req.getConfig().getCalSuite();
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

  @StrutsParameter
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

  @StrutsParameter
  public void setCount(final String val) {
    // Handled elsewhere.
  }

  @StrutsParameter
  public void setFexpr(final String val) {
    // Handled elsewhere.
  }

  @StrutsParameter
  public void setSort(final String val) {
    // Handled elsewhere.
  }

  @StrutsParameter
  public void setStart(final String val) {
    // Handled elsewhere.
  }

  @StrutsParameter
  public void setViewType(final String val) {
    viewType = val;
  }

  public String getViewType() {
    return viewType;
  }

  /** Write the value as json to the response stream.
   *
   * @param resp to write to
   * @param val to output
   */
  public boolean writeJson(final HttpServletResponse resp,
                           final Object val) {
    try {
      mapper.writeValue(resp.getOutputStream(), val);
    } catch (final Throwable t) {
      final var eof = BedeworkException.excOrFail(t);
      if (eof.message() != null) {
        return false; // socket closed by other end?
      }
      throw eof.exc();
    }

    return true;
  }

  /** Write the value as json to the response stream.
   * Sets status ok, dds header, writes data and closes stream.
   *
   * <p>Adds content type header and possible additional.
   *
   * @param resp to write to
   * @param etag added if non-null
   * @param header if not null adds header with name and value.
   * @param val to output
   * @return false for "Broken pipe"
   */
  public boolean outputJson(final HttpServletResponse resp,
                            final String etag,
                            final String[] header,
                            final Object val) {
    resp.setStatus(HttpServletResponse.SC_OK);

    if (etag != null) {
      resp.setHeader("etag", etag);
    }

    if (header != null) {
      resp.setHeader(header[0], header[1]);
    }

    resp.setContentType("application/json; charset=UTF-8");

    try (final var os = resp.getOutputStream()){
      return writeJson(resp, val);
    } catch (final IOException ioe) {
      if (ioe.getMessage().contains("Broken pipe")) {
          return false; // socket closed by other end?
      } else {
        throw new BedeworkException(ioe);
      }
    }
  }
}
