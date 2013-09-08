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
package org.bedework.appcommon.client;

import org.bedework.caldav.util.filter.FilterBase;
import org.bedework.calfacade.BwCalendar;
import org.bedework.calfacade.BwCategory;
import org.bedework.calfacade.BwString;
import org.bedework.calfacade.exc.CalFacadeException;
import org.bedework.calfacade.filter.ColorMap;
import org.bedework.calfacade.filter.FilterBuilder;
import org.bedework.calfacade.svc.BwView;
import org.bedework.calfacade.svc.EventInfo;
import org.bedework.util.misc.Util;

import org.apache.log4j.Logger;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/** This class represents the current state of a bedework client - most probably
 * the web clients.
 *
 * @author Mike Douglass
 *
 */
public class ClientState implements Serializable {
  private transient Logger log;

  private boolean debug;

  private BwView currentView;

  /* The current virtual path */
  private String vpath;

  /* The current virtual path target */
  private String vpathTarget;

  /* Filter resulting from the view or vpath */
  private FilterBase filter;

  /* Built as we build filters. */
  private ColorMap colorMap;

  private String vfilter;
  private Client cl;

  /**
   * @param cl-the client object
   */
  public ClientState(final Client cl) {
    this.cl = cl;
    debug = getLogger().isDebugEnabled();
  }

  /** Called if anything is changed which affects the state of the client, e.g
   * switching display flags, deleting collections.
   *
   * @throws CalFacadeException
   */
  public void flush() throws CalFacadeException {
    filter = null;
  }

  /* ====================================================================
   *                   Current selection
   * This defines how we select events to display.
   * ==================================================================== */

  /** Set the view to the given named view. Null means reset to default.
   * Unset current calendar.
   *
   * @param  val     String view name - null for default
   * @return boolean false - view not found.
   * @throws CalFacadeException
   */
  public boolean setCurrentView(final String val) throws CalFacadeException {
    if (val == null) {
      currentView = null;
      colorMap = new ColorMap();

      return true;
    }

    filter = null;
    vfilter = null;

    Collection<BwView> v = cl.getPreferences().getViews();
    if ((v == null) || (v.size() == 0)) {
      return false;
    }

    for (BwView view: v) {
      if (val.equals(view.getName())) {
        currentView = view;
        vpath = null;
        vpathTarget = null;

        if (debug) {
          trace("set view to " + view);
        }

        return true;
      }
    }

    return false;
  }

  /** Set the view to the given view object. Null means reset to default.
   * Unset current calendar.
   *
   * @param  val     view name - null for default
   * @throws CalFacadeException
   */
  public void setCurrentView(final BwView val) throws CalFacadeException {
    if (val == null) {
      currentView = null;
      colorMap = new ColorMap();

      return;
    }

    filter = null;
    vfilter = null;

    currentView = val;
    vpath = null;
    vpathTarget = null;
  }

  /** Get the current view we have set
   *
   * @return BwView    named Collection of Collections or null for default
   * @throws CalFacadeException
   */
  public BwView getCurrentView() throws CalFacadeException {
    return currentView;
  }

  /** Set the virtual path and unset any current view.
   *
   * <p>A virtual path is the apparent path for a user looking at an explorer
   * view of collections.
   *
   * <p>We might have,
   * <pre>
   *    home-->Arts-->Theatre
   * </pre>
   *
   * <p>In reality the Arts collection might be an alias to another alias which
   * is an alias to a collection containing aliases including "Theatre".
   *
   * <p>So the real picture might be something like...
   * <pre>
   *    home-->Arts             (categories="ChemEng")
   *            |
   *            V
   *           Arts             (categories="Approved")
   *            |
   *            V
   *           Arts-->Theatre   (categories="Arts" AND categories="Theatre")
   *                     |
   *                     V
   *                    MainCal
   * </pre>
   * where the vertical links are aliasing. The importance of thsi is that
   * each alias might introduce another filtering term, the intent of which is
   * to restrict the retrieval to a specific subset. The parenthesized terms
   * represent example filters.
   *
   * <p>The desired filter is the ANDing of all the above.
   *
   * @param  vpath  a String virtual path
   * @return false for bad path
   * @throws CalFacadeException
   */
  public boolean setVirtualPath(final String vpath) throws CalFacadeException {
    /* We decompose the virtual path into it's elements and then try to
     * build a sequence of collections that include the aliases and their
     * targets until we reach the last element in the path.
     *
     * We'll assume the path is already normalized and that no "/" are allowed
     * as parts of names.
     *
     * What we're doing here is resolving aliases to aliases and accumulating
     * any filtering that might be in place as a sequence of ANDed terms. For
     * example:
     *
     * /user/eng/Lectures has the filter cat=eng and is aliased to
     * /public/aliases/Lectures which has the filter cat=lectures and is aliased to
     * /public/cals/MainCal
     *
     * We want the filter (cat=eng) & (cat=Lectures) on MainCal.
     *
     * Below, we decompose the virtual path and we save the path to an actual
     * folder or calendar collection.
     */

    Collection<BwCalendar> cols = cl.decomposeVirtualPath(vpath);

    if (cols == null) {
      // Bad vpath
      return false;
    }

    vfilter = null;
    this.vpath = vpath;
    vpathTarget = vpath;

    for (BwCalendar col: cols) {
      if (debug) {
        trace("      vpath collection:" + col.getPath());
      }

      if (col.getFilterExpr() != null) {
        if (vfilter == null) {
          vfilter = "(" ;
        } else {
          vfilter += " & (";
        }
        vfilter += col.getFilterExpr() + ")";
      }

      if (col.getCollectionInfo().onlyCalEntities ||
          (col.getCalType() == BwCalendar.calTypeFolder)) {
        // reached an end point
        vpathTarget = col.getPath();
      }
    }

    if (debug) {
      trace("      vpath filter: " + vfilter);
    }

    colorMap = new ColorMap();
    currentView = null;
    filter = null;
//    filter = new FilterBuilder(getSvc(),
  //                             colorMap).buildFilter(null, vfilter, true);

    return true;
  }

  /**
   * @return non-null if setVirtualPath was called succesfully
   * @throws CalFacadeException
   */
  public String getVirtualPath() throws CalFacadeException {
    return vpath;
  }

  /**
   * @return BwCalendar
   * @throws CalFacadeException
   */
  public BwCalendar getCurrentCollection() throws CalFacadeException {
    return null; //cc currentCollection;
  }

  /** Given a possible collection object return whatever is appropriate for the
   * current view.
   *
   * <p>If the collection is non-null go with that, otherwise go with the
   * current selected collection or the current selected view.
   *
   * @param cal
   * @return BwFilter or null
   * @throws CalFacadeException
   */
  public FilterBase getViewFilter(final BwCalendar cal) throws CalFacadeException {
    List<String> paths;
    boolean conjunction = false;

    if (cal != null) {
      /* One shot collection supplied */
      paths = new ArrayList<String>();

      paths.add(cal.getPath());
      colorMap = new ColorMap();

      return new ClsFilterBuilder(colorMap).buildFilter(paths,
                                                     conjunction,
                                                     null, true);
    }

    if (filter != null) {
      return filter;
    }

    if (vpathTarget != null) {
      paths = new ArrayList<String>();

      paths.add(vpathTarget);
    } else if (currentView != null) {
      paths = currentView.getCollectionPaths();
      conjunction = currentView.getConjunction();
    } else {
      return null;
    }

    colorMap = new ColorMap();
    filter = new ClsFilterBuilder(colorMap).buildFilter(paths,
                                                     conjunction,
                                                     vfilter, true);
    return filter;
  }

  /** Attempt to set the color for the given events. If there is no appropriate
   * mapping the event color will be set to null.
   *
   * @param eis
   */
  public void setColor(final Collection<EventInfo> eis) {
    if ((colorMap == null) || Util.isEmpty(eis)) {
      return;
    }

    try {
      colorMap.setColor(eis, cl.getCurrentPrincipalHref());
    } catch (Throwable t) {
      throw new RuntimeException(t);
    }
  }

  /* ====================================================================
   *                   Private methods
   * ==================================================================== */

  private class ClsFilterBuilder extends FilterBuilder {
    public ClsFilterBuilder(ColorMap colorMap) {
      super(colorMap);
    }

    public BwCalendar getCollection(String path) throws CalFacadeException {
      return cl.getCollection(path);
    }

    public BwCalendar resolveAlias(BwCalendar val,
                                   boolean resolveSubAlias) throws CalFacadeException {
      return cl.resolveAlias(val, resolveSubAlias, false);
    }
    public Collection<BwCalendar> getChildren(BwCalendar col)
            throws CalFacadeException {
      return cl.getChildren(col);
    }

    @Override
    public BwCategory getCategoryByName(final String name) throws CalFacadeException {
      return cl.getCategoryByName(new BwString(null, name));
    }

    @Override
    public BwCategory getCategory(final String uid) throws CalFacadeException {
      return cl.getCategory(uid);
    }
  }

  /* Get a logger for messages
   */
  private Logger getLogger() {
    if (log == null) {
      log = Logger.getLogger(this.getClass());
    }

    return log;
  }

  protected void trace(final String msg) {
    getLogger().debug("trace: " + msg);
  }
}
