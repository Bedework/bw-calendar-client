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

import org.bedework.util.servlet.filters.PresentationState;

import org.apache.struts.util.MessageResources;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;

/** This ought to be made pluggable. We need a session factory which uses
 * CalEnv to figure out which implementation to use.
 *
 * <p>This class represents a session for the Bedework web interface.
 * Some user state will be retained here.
 * We also provide a number of methods which act as the interface between
 * the web world and the calendar world.
 *
 * @author Mike Douglass   douglm@bedework.edu
 */
public class BwSessionImpl implements BwSession {
  /** Not completely valid in the j2ee world but it's only used to count sessions.
   */
  private static class Counts {
    long totalSessions = 0;
  }

  private static volatile HashMap<String, Counts> countsMap =
    new HashMap<String, Counts>();
  private long sessionNum = 0;

  /** The current user - null for guest
   */
  private String user;

  /** The application root
   */
  //private String appRoot;

  /** The application name
   */
  private String appName;

  /** The current presentation state of the application
   */
  private PresentationState ps;

  /** Constructor for a Session
   *
   * @param user       String user id
   * @param browserResourceRoot
   * @param appRoot
   * @param appName    String identifying particular application
   * @param ps
   * @param messages
     @param schemeHostPort The prefix for generated urls referring to this server
   * @throws Throwable
   */
  public BwSessionImpl(final String user,
                       final String browserResourceRoot,
                       final String appRoot,
                       final String appName,
                       final PresentationState ps,
                       final MessageResources messages,
                       final String schemeHostPort) throws Throwable {
    this.user = user;

    this.appName = appName;
    this.ps = ps;

    if (ps != null) {
      /*
      if (ps.getAppRoot() == null) {
        ps.setAppRoot(prefixUri(schemeHostPort, appRoot));
      }

      if (ps.getBrowserResourceRoot() == null) {
        ps.setBrowserResourceRoot(prefixUri(schemeHostPort, browserResourceRoot));
      }
      */
      ps.setAppRoot(appRoot);
      ps.setBrowserResourceRoot(browserResourceRoot);
    }

    setSessionNum(appName);
  }

  /* NOTE: This is NOT intended to turn a relative URL into an
  absolute URL. It is a convenience for development which turns a
  not fully specified url into a url referring to the server.

  This will not work if they are treated as relative to the servlet.

  In production mode, the appRoot will normally be fully specified to a
  different web server.
* /
  private String prefixUri(final String schemeHostPort,
                           final String val) {
    if (val.toLowerCase().startsWith("http")) {
      return val;
    }

    StringBuilder sb = new StringBuilder(schemeHostPort);

    if (!val.startsWith("/")) {
      sb.append("/");
    }
    sb.append(val);

    return sb.toString();
  }
  */

  /* ======================================================================
   *                     Property methods
   * ====================================================================== */

  /* (non-Javadoc)
   * @see org.bedework.webcommon.BwSession#getSessionNum()
   */
  @Override
  public long getSessionNum() {
    return sessionNum;
  }

  /**
   * @param val
   */
  public void setAppName(final String val) {
    appName = val;
  }

  /**
   * @return app name
   */
  public String getAppName() {
    return appName;
  }

  /**
   * @param val
   */
  @Override
  public void setUser(final String val) {
    user = val;
  }

  /**
   * @return user
   */
  @Override
  public String getUser() {
    return user;
  }

  /**
   * @param val
   */
  @Override
  public void setPresentationState(final PresentationState val) {
    ps = val;
  }

  /**
   * @return PresentationState
   */
  @Override
  public PresentationState getPresentationState() {
    return ps;
  }

  /** Is this a guest user?
   *
   * @return boolean true for a guest
   */
  @Override
  public boolean isGuest() {
    return user == null;
  }

  /**
    Does the given string represent a rootless URI?  A URI is rootless
    if it is not absolute (that is, does not contain a scheme like 'http')
    and does not start with a '/'
    @param uri String to test
    @return Is the string a rootless URI?  If the string is not a valid
      URI at all (for example, it is null), returns false
   */
  private boolean rootlessUri(final String uri) {
    try {
      return !((uri == null) || uri.startsWith("/") || new URI(uri).isAbsolute());
    } catch (URISyntaxException e) {  // not a URI at all
      return false;
    }
  }

  private void setSessionNum(final String name) {
    try {
      synchronized (countsMap) {
        Counts c = countsMap.get(name);

        if (c == null) {
          c = new Counts();
          countsMap.put(name, c);
        }

        sessionNum = c.totalSessions;
        c.totalSessions++;
      }
    } catch (Throwable t) {
    }
  }
}
