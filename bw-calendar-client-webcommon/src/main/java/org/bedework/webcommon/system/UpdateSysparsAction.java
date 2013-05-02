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
package org.bedework.webcommon.system;

import org.bedework.appcommon.ClientError;
import org.bedework.appcommon.ClientMessage;
import org.bedework.calfacade.BwSystem;
import org.bedework.calfacade.SubContext;
import org.bedework.calfacade.svc.BwCalSuite;
import org.bedework.calsvci.CalSvcI;
import org.bedework.calsvci.SysparsI;
import org.bedework.webcommon.BwAbstractAction;
import org.bedework.webcommon.BwActionFormBase;
import org.bedework.webcommon.BwRequest;

import java.lang.reflect.Method;
import java.util.Set;

/** This action updates the system parameters
 *
 * <p>Parameters are:<ul>
 *      <li>updateCancelled</li>
 *      <li>admingroupsClass</li>
 *      <li>usergroupsClass</li>
 *      <li>defaultUserViewName</li>
 *      <li>directoryBrowsingDisallowed</li>
 *      <li>httpConnectionsPerUser</li>
 *      <li>httpConnectionsPerHost</li>
 *      <li>httpConnections</li>
 *      <li>defaultUserQuota</li>
 * </ul>
 *
 * <p>Forwards to:<ul>
 *      <li>"noAccess"     user not authorised.</li>
 *      <li>"notFound"     no such user.</li>
 *      <li>"continue"     continue on to update page.</li>
 * </ul>
 *
 * @author Mike Douglass   douglm@rpi.edu
 */
public class UpdateSysparsAction extends BwAbstractAction {
  /* (non-Javadoc)
   * @see org.bedework.webcommon.BwAbstractAction#doAction(org.bedework.webcommon.BwRequest, org.bedework.webcommon.BwActionFormBase)
   */
  @Override
  public int doAction(final BwRequest request,
                      final BwActionFormBase form) throws Throwable {
    /** Check access
     */
    if (!form.getCurUserSuperUser()) {
      return forwardNoAccess;
    }

    CalSvcI svci = form.fetchSvci();
    SysparsI sysi = svci.getSysparsHandler();

    // Force refetch of the data
    BwSystem syspars = sysi.get(sysi.getSystemName());
    Boolean changed = false;

    String str = request.getReqPar("updateCancelled");
    if (str != null) {
      // refetch
      form.setSyspars(sysi.get());
      return forwardCancelled;
    }

    changed = changedString(request, form, syspars, "tzid", changed);
    if (changed == null) {
      return forwardBadRequest;
    }

    changed = changedBoolean(request, form, syspars, "defaultUserHour24", changed);
    if (changed == null) {
      return forwardBadRequest;
    }

    changed = changedString(request, form, syspars, "systemid", changed);
    if (changed == null) {
      return forwardBadRequest;
    }

    changed = changedBoolean(request, form, syspars, "indexing", changed);
    if (changed == null) {
      return forwardBadRequest;
    }

    changed = changedInteger(request, form, syspars, "defaultFBPeriod", changed);
    if (changed == null) {
      return forwardBadRequest;
    }

    changed = changedInteger(request, form, syspars, "maxFBPeriod", changed);
    if (changed == null) {
      return forwardBadRequest;
    }

    changed = changedInteger(request, form, syspars, "defaultWebCalPeriod", changed);
    if (changed == null) {
      return forwardBadRequest;
    }

    changed = changedInteger(request, form, syspars, "maxWebCalPeriod", changed);
    if (changed == null) {
      return forwardBadRequest;
    }

    changed = changedString(request, form, syspars, "principalRoot", changed);
    if (changed == null) {
      return forwardBadRequest;
    }

    changed = changedString(request, form, syspars, "userPrincipalRoot", changed);
    if (changed == null) {
      return forwardBadRequest;
    }

    changed = changedString(request, form, syspars, "groupPrincipalRoot", changed);
    if (changed == null) {
      return forwardBadRequest;
    }

    changed = changedString(request, form, syspars, "userDefaultCalendar", changed);
    if (changed == null) {
      return forwardBadRequest;
    }

    changed = changedString(request, form, syspars, "defaultTrashCalendar", changed);
    if (changed == null) {
      return forwardBadRequest;
    }

    changed = changedString(request, form, syspars, "userInbox", changed);
    if (changed == null) {
      return forwardBadRequest;
    }

    changed = changedString(request, form, syspars, "userOutbox", changed);
    if (changed == null) {
      return forwardBadRequest;
    }

    changed = changedString(request, form, syspars, "deletedCalendar", changed);
    if (changed == null) {
      return forwardBadRequest;
    }

    changed = changedString(request, form, syspars, "busyCalendar", changed);
    if (changed == null) {
      return forwardBadRequest;
    }

    changed = changedString(request, form, syspars, "defaultUserViewName", changed);
    if (changed == null) {
      return forwardBadRequest;
    }

    // publicUser;

    /* Not used
    changed = changedInteger(request, form, syspars, "httpConnectionsPerUser", changed);
    if (changed == null) {
      return forwardBadRequest;
    }

    changed = changedInteger(request, form, syspars, "httpConnectionsPerHost", changed);
    if (changed == null) {
      return forwardBadRequest;
    }

    changed = changedInteger(request, form, syspars, "httpConnections", changed);
    if (changed == null) {
      return forwardBadRequest;
    }
    */

    changed = changedInteger(request, form, syspars, "maxPublicDescriptionLength", changed);
    if (changed == null) {
      return forwardBadRequest;
    }

    changed = changedInteger(request, form, syspars, "maxUserDescriptionLength", changed);
    if (changed == null) {
      return forwardBadRequest;
    }

    changed = changedInteger(request, form, syspars, "maxUserEntitySize", changed);
    if (changed == null) {
      return forwardBadRequest;
    }

    changed = changedLong(request, form, syspars, "defaultUserQuota", changed);
    if (changed == null) {
      return forwardBadRequest;
    }

    changed = changedInteger(request, form, syspars, "maxInstances", changed);
    if (changed == null) {
      return forwardBadRequest;
    }

    changed = changedInteger(request, form, syspars, "maxYears", changed);
    if (changed == null) {
      return forwardBadRequest;
    }

    changed = changedInteger(request, form, syspars, "maxAttendees", changed);
    if (changed == null) {
      return forwardBadRequest;
    }

    changed = changedString(request, form, syspars, "userauthClass", changed);
    if (changed == null) {
      return forwardBadRequest;
    }

    changed = changedString(request, form, syspars, "mailerClass", changed);
    if (changed == null) {
      return forwardBadRequest;
    }

    changed = changedString(request, form, syspars, "admingroupsClass", changed);
    if (changed == null) {
      return forwardBadRequest;
    }

    changed = changedString(request, form, syspars, "usergroupsClass", changed);
    if (changed == null) {
      return forwardBadRequest;
    }

    changed = changedBoolean(request, form, syspars, "directoryBrowsingDisallowed", changed);
    if (changed == null) {
      return forwardBadRequest;
    }

    changed = changedString(request, form, syspars, "eventregAdminToken", changed);
    if (changed == null) {
      return forwardBadRequest;
    }

    changed = changedString(request, form, syspars, "globalResourcesPath", changed);
    if (changed == null) {
      return forwardBadRequest;
    }

    changed = changedString(request, form, syspars, "indexRoot", changed);
    if (changed == null) {
      return forwardBadRequest;
    }

    changed = changedBoolean(request, form, syspars, "useSolr", changed);
    if (changed == null) {
      return forwardBadRequest;
    }

    changed = changedString(request, form, syspars, "solrURL", changed);
    if (changed == null) {
      return forwardBadRequest;
    }

    changed = changedString(request, form, syspars, "solrCoreAdmin", changed);
    if (changed == null) {
      return forwardBadRequest;
    }

    changed = changedString(request, form, syspars, "solrDefaultCore", changed);
    if (changed == null) {
      return forwardBadRequest;
    }

    changed = changedString(request, form, syspars, "localeList", changed);
    if (changed == null) {
      return forwardBadRequest;
    }

    changed = changedString(request, form, syspars, "rootUsers", changed);
    if (changed == null) {
      return forwardBadRequest;
    }

    changed = changedBoolean(request, form, syspars, "defaultChangesNotifications", changed);
    if (changed == null) {
      return forwardBadRequest;
    }

    changed = updateContext(request, form, syspars, changed);

    if (!changed) {
      return forwardNochange;
    }

    sysi.update(syspars);

    // Refetch and cache
    form.setSyspars(sysi.get(sysi.getSystemName()));

    form.getMsg().emit(ClientMessage.updatedSyspars);
    return forwardSuccess;
  }

  private Boolean updateContext(final BwRequest request,
                                final BwActionFormBase form,
                                final BwSystem syspars,
                                final Boolean chg) throws Throwable {
    String cName = request.getReqPar("subcontext");
    if (cName == null) {
      return false || chg;
    }

    String action = request.getReqPar("action");
    if (action == null) {
      form.getErr().emit(ClientError.badRequest, "subcontext (no action)");
      return false;
    }

    boolean add = false;
    boolean remove = false;
    boolean update = false;

    if (action.equals("add")) {
      add = true;
    } else if (action.equals("remove")) {
      remove = true;
    } else if (action.equals("update")) {
      update = true;
    } else {
      form.getErr().emit(ClientError.badRequest, "subcontext (bad action) " + action);
      return false;
    }

    BwCalSuite cs = null;
    String csName = null;
    boolean defaultContext = request.getBooleanReqPar("default", false);

    if (add | update) {
      // require calsuite
      csName = request.getReqPar("calSuiteName");
      if (csName == null) {
        form.getErr().emit(ClientError.badRequest, "subcontext (no calSuite)");
        return false;
      }

      cs = form.fetchSvci().getCalSuitesHandler().get(csName);
      if (cs == null) {
        form.getErr().emit(ClientError.badRequest, "subcontext:unknown calSuite " +
            csName);
        return false;
      }
    }

    Set<SubContext> cnames = syspars.getContexts();

    SubContext propCname = null;
    SubContext defaultCname = null;

    for (SubContext sc: cnames) {
      if ((defaultCname == null) && sc.getDefaultContext()) {
        defaultCname = sc;
      }

      if (propCname != null) {
        continue;
      }

      if (sc.getContextName().equals(cName)) {
        propCname = sc;
      }
    }

    if (add) {
      if (propCname != null) {
        // Already there
        form.getErr().emit(ClientError.badRequest, "subcontext:add " + cName +
                           " already present");
        return false;
      }

      if (defaultContext && (defaultCname != null)) {
        // Remove current default - making this one the default
        unDefaultContext(syspars, defaultCname);
      }

      syspars.addContext(new SubContext(cName, csName,defaultContext));
      return true;
    }

    if (remove) {
      if (propCname == null) {
        // Not there
        form.getErr().emit(ClientError.badRequest, "subcontext:remove " + cName +
                           " not present");
        return false;
      }

      syspars.removeContext(propCname);
      return true;
    }

    /* Update */

    if (defaultContext && (defaultCname != null)) {
      // Remove current default - making this one the default
      unDefaultContext(syspars, defaultCname);
    }

    syspars.removeContext(propCname);
    syspars.addContext(new SubContext(cName, csName,defaultContext));
    return true;
  }

  private void unDefaultContext(final BwSystem syspars,
                                final SubContext defaultCname) {
    // Remove as default
    syspars.removeContext(defaultCname);

    syspars.addContext(new SubContext(defaultCname.getContextName(),
                                      defaultCname.getCalSuite(),
                                      false));
  }

  private Boolean changedString(final BwRequest request,
                                final BwActionFormBase form,
                                final BwSystem syspars,
                                final String name,
                                final Boolean chg) throws Throwable {
    String val = request.getReqPar(name);
    if ((val == null) && !request.present(name)) {
      return false || chg;
    }

    return changed(form, syspars, name, val, chg);
  }

  private Boolean changedInteger(final BwRequest request,
                                 final BwActionFormBase form, final BwSystem syspars,
                                 final String name, final Boolean chg) throws Throwable {
    Integer val = request.getIntReqPar(name);
    if (val == null) {
      return false || chg;
    }

    return changed(form, syspars, name, val, chg);
  }

  private Boolean changedLong(final BwRequest request,
                              final BwActionFormBase form, final BwSystem syspars,
                              final String name, final Boolean chg) throws Throwable {
    Long val = request.getLongReqPar(name);
    if (val == null) {
      return false || chg;
    }

    return changed(form, syspars, name, val, chg);
  }

  private Boolean changedBoolean(final BwRequest request,
                                 final BwActionFormBase form, final BwSystem syspars,
                                 final String name, final Boolean chg) throws Throwable {
    Boolean val = request.getBooleanReqPar(name);
    if (val == null) {
      return false || chg;
    }

    return changed(form, syspars, name, val, chg);
  }

  private Boolean changed(final BwActionFormBase form, final BwSystem syspars,
                          final String name, final Object val,
                          final Boolean changed) {
    try {
      Method setter = findSetter(syspars, name, val);
      if (setter == null) {
        if (debug) {
          debugMsg("Bad request: " + name);
        }
        form.getErr().emit(ClientError.badRequest, name);
        return null;
      }

      Method getter = findGetter(syspars, name, val);
      if (getter == null) {
        if (debug) {
          debugMsg("Bad request: " + name);
        }
        form.getErr().emit(ClientError.badRequest, name);
        return null;
      }

      Object o = getter.invoke(syspars);

      if ((o != null) && o.equals(val)) {
        return false || changed;
      }

      if ((o == null) && (val == null)) {
        return false || changed;
      }

      Object[] pars = new Object[]{val};

      setter.invoke(syspars, pars);
      return true;
    } catch (Throwable t) {
      if (debug) {
        t.printStackTrace();
      }
      form.getErr().emit(t);
      return null;
    }
  }

  private Method findSetter(final Object val, final String name, final Object parVal) throws Throwable {
    Method meth = findMethod(val, name, true);
    if (meth == null) {
      return null;
    }

    Class<?>[] parClasses = meth.getParameterTypes();
    if (parClasses.length != 1) {
      if (debug) {
        debugMsg("Invalid setter method " + name);
      }
      return null;
      //throw new CalEnvException("org.bedework.calenv.invalid.setter");
    }

    Class<?> parClass = parClasses[0];

    if (!checkClassType(parClass, parVal)) {
      if (debug) {
        debugMsg("Invalid setter method(type) " + name);
      }
      return null;
    }

    return meth;
  }

  private Method findGetter(final Object val, final String name,
                            final Object parVal) throws Throwable {
    Method meth = findMethod(val, name, false);
    if (meth == null) {
      return null;
    }

    Class<?>[] parClasses = meth.getParameterTypes();
    if ((parClasses != null) && (parClasses.length != 0)) {
      if (debug) {
        debugMsg("Invalid getter method " + name);
      }
      return null;
      //throw new CalEnvException("org.bedework.calenv.invalid.getter");
    }

    Class<?> rclass = meth.getReturnType();
    if ((rclass == null) ||
        !checkClassType(rclass, parVal)) {
      if (debug) {
        debugMsg("Invalid getter method(type) " + name);
      }
      return null;
      //throw new CalEnvException("org.bedework.calenv.invalid.getter");
    }

    return meth;
  }

  private boolean checkClassType(final Class<?> cl, final Object parVal) {
    if (parVal == null) {
      return !cl.isPrimitive();
    }

    String valClassName = parVal.getClass().getName();

    String clName = cl.getName();

    if (clName.equals(valClassName)) {
      return true;
    }

    if (clName.equals("int") &&
        valClassName.equals(Integer.class.getName())) {
      return true;
    }

    if (clName.equals("long") &&
        valClassName.equals(Long.class.getName())) {
      return true;
    }

    if (clName.equals("boolean") &&
        valClassName.equals(Boolean.class.getName())) {
      return true;
    }

    return false;
  }

  private Method findMethod(final Object val, final String name,
                            final boolean setter) throws Throwable {
    String setget = "set";
    if (!setter) {
      setget = "get";
    }
    String methodName = setget + name.substring(0, 1).toUpperCase() +
                        name.substring(1);
    Method[] meths = val.getClass().getMethods();
    Method meth = null;

    for (int i = 0; i < meths.length; i++) {
      Method m = meths[i];

      if (m.getName().equals(methodName)) {
        if (meth != null) {
          return null;  // claim not found
        }
        meth = m;
      }
    }

    return meth;
  }
}
