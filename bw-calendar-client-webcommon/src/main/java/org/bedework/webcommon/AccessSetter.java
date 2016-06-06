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

import org.bedework.access.Ace;
import org.bedework.access.AceWho;
import org.bedework.access.Privilege;
import org.bedework.access.PrivilegeDefs;
import org.bedework.access.Privileges;
import org.bedework.appcommon.ClientError;
import org.bedework.calfacade.BwCalendar;
import org.bedework.calfacade.exc.ValidationError;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;

/**
 * @author Mike Douglass
 *
 */
public class AccessSetter implements ForwardDefs, Serializable {
  /** */
  public static class Pars {
    /** Set to forwardSuccesss if we had access setting requests otherwise
     * forwardNoAction for no parameters or an error code.
     */
    public int status;

    /** true if we should completely replace the acl
     */
    public boolean replaceAll;

    /** */
    public AceWho who;
    /** */
    public boolean defaultHow;
    /** */
    public ArrayList<Ace> aces = new ArrayList<Ace>();

    /** Some of the following is set */
    private boolean isScheduling;

    /** */
    public int calType = BwCalendar.calTypeUnknown;

    /** */
    public boolean schedule;
    /** */
    public boolean scheduleRequest;
    /** */
    public boolean scheduleReply;
    /** */
    public boolean scheduleFreeBusy;
  }

  /**
   * <p>Request parameters:<ul>
   *      <li>  how:                   concatenated String of schedule access rights
   *                                   @see PrivilegeDefs
   *                                   Each flag can be refixed with "-" for denial
   *                                   e.g. R-W allow read, deny write</li>.
   *      <li>  whoType:               "user" (default), "group",
   *                                   "auth", "unauth", "other", "all" </li>.
   *      <li>  who:                   name of principal - default to owner</li>.
   *      <li>  notWho:                "yes" to invert sense of "who"</li>.
   *      <li>  what:                  in/out</li>.
   * </ul> or <ul>
   *      <li>  calPath alone:         path (or url) of calendar or...</li>.
   *      <li>  calPath+guid+recurid:  event or ...</li>.
   *      <li>  calSuiteName:          name of calendar suite</li>.
   *      <li>  how:                   "default" or concatenated String of
   *                                   desired access rights
   *                                   @see PrivilegeDefs </li>.
   *      <li>  whoType:               user (default), group</li>.
   *      <li>  who:                   name of principal - default to owner</li>.
   * @param request
   * @return Pars
   * @throws Throwable
   */
  public static Pars parseRequest(BwRequest request) throws Throwable {
    Pars pars = new Pars();

    pars.status = getWho(request, pars);

    if (pars.status != forwardSuccess) {
      return pars;
    }

    pars.status = getHow(request, pars);

    if (pars.status != forwardSuccess) {
      return pars;
    }

    pars.status = getWhat(request, pars);

    return pars;
  }

  /* ********************************************************************
                             private methods
     ******************************************************************** */

  private static int getWhat(BwRequest request,
                             Pars pars) throws Throwable {
    String what = request.getReqPar("what");
    if (what == null) {
      if (pars.isScheduling) {
        request.getErr().emit(ClientError.missingScheduleWhat);
        return forwardError;
      }

      return forwardSuccess;
    } else if (!pars.isScheduling && !pars.defaultHow) {
      // Cannot specify a what for non-scheduling only access
      request.getErr().emit(ClientError.badScheduleWhat, what);
      return forwardError;
    }

    if (what.equals("in")) {
      pars.calType = BwCalendar.calTypeInbox;
      pars.isScheduling = true;
    } else if (what.equals("out")) {
      pars.calType = BwCalendar.calTypeOutbox;
      pars.isScheduling = true;
    } else {
      request.getErr().emit(ClientError.badScheduleWhat, what);
      return forwardError;
    }

    return forwardSuccess;
  }

  private static int getWho(BwRequest request,
                            Pars pars) throws Throwable {
    String whoTypeStr = request.getReqPar("whoType");
    String who = request.getReqPar("who");
    int whoType = -1;

    boolean needWho = false;

    if ((whoTypeStr == null) || (whoTypeStr.equals("user")))  {
      if (who == null) {
        // Assume no access setting
        return forwardNoAction;
      }

      whoType = Ace.whoTypeUser;
      needWho = true;
    } else if (whoTypeStr.equals("owner")) {
      whoType = Ace.whoTypeOwner;
    } else if (whoTypeStr.equals("user")) {
      whoType = Ace.whoTypeUser;
      needWho = true;
    } else if (whoTypeStr.equals("group")) {
      whoType = Ace.whoTypeGroup;
      needWho = true;
    } else if (whoTypeStr.equals("resource")) {
      whoType = Ace.whoTypeResource;
      needWho = true;
    } else if (whoTypeStr.equals("venue")) {
      whoType = Ace.whoTypeVenue;
      needWho = true;
    } else if (whoTypeStr.equals("ticket")) {
      whoType = Ace.whoTypeTicket;
      needWho = true;
    } else if (whoTypeStr.equals("host")) {
      whoType = Ace.whoTypeHost;
      needWho = true;
    } else if (whoTypeStr.equals("auth")) {
      whoType = Ace.whoTypeAuthenticated;
    } else if (whoTypeStr.equals("unauth")) {
      whoType = Ace.whoTypeUnauthenticated;
    } else if (whoTypeStr.equals("all")) {
      whoType = Ace.whoTypeAll;
    } else if (whoTypeStr.equals("other")) {
      whoType = Ace.whoTypeOther;
    } else {
      request.getErr().emit(ValidationError.invalidAccessWhoType, whoTypeStr);
      return forwardError;
    }

    if (needWho && (who == null)) {
      request.getErr().emit(ValidationError.missingAccessWho);
      return forwardError;
    }

    pars.who = AceWho.getAceWho(who, whoType,
                                "yes".equals(request.getReqPar("notWho")));

    return forwardSuccess;
  }

  private static int getHow(BwRequest request,
                            Pars pars) throws Throwable {
    String how = request.getReqPar("how");

    if (how == null) {
      request.getErr().emit(ValidationError.missingAccessHow);
      return forwardError;
    }

    if (how.equals("default")) {
      pars.defaultHow = true;
      return forwardSuccess;
    }

    char[] howchs = how.toCharArray();

    boolean scheduling = true;

    Collection<Privilege> privs = new ArrayList<Privilege>();

    for (int hi = 0; hi < howchs.length; hi++) {
      char howch = howchs[hi];
      boolean found = false;
      boolean denial = false;

      if (howch == '-') {
        denial = true;
        hi++;
        if (hi == howchs.length) {
          request.getErr().emit(ValidationError.invalidAccessHow);
          return forwardError;
        }
        howch = howchs[hi];
      }

      for (int pi = 0; pi <= PrivilegeDefs.privMaxType; pi++) {
        if (howch == PrivilegeDefs.privEncoding[pi]) {
          if (!schedulingPriv(pi, pars)) {
            scheduling = false;
          }

          privs.add(Privileges.makePriv(pi, denial));
          found = true;
          break;
        }
      }

      if (!found) {
        request.getErr().emit(ValidationError.invalidAccessHow);
        return forwardError;
      }

      pars.isScheduling = scheduling;
    }

    pars.aces.add(Ace.makeAce(pars.who, privs, null));

    return forwardSuccess;
  }

  private static boolean schedulingPriv(int pi, Pars pars) {
    if (pi == PrivilegeDefs.privSchedule) {
      pars.schedule = true;
      return true;
    }

    if (pi == PrivilegeDefs.privScheduleRequest) {
      pars.scheduleRequest = true;
      return true;
    }

    if (pi == PrivilegeDefs.privScheduleReply) {
      pars.scheduleReply = true;
      return true;
    }

    if (pi == PrivilegeDefs.privScheduleFreeBusy) {
      pars.scheduleFreeBusy = true;
      return true;
    }

    return false;
  }
}
