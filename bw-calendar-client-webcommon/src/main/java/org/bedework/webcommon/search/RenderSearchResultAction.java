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
import org.bedework.calsvci.BwIndexSearchResultEntry;
import org.bedework.calsvci.CalSvcI;
import org.bedework.icalendar.IcalTranslator;
import org.bedework.webcommon.BwActionFormBase;
import org.bedework.webcommon.BwRequest;
import org.bedework.webcommon.RenderAction;

import edu.rpi.cct.misc.indexing.SearchLimits;

import java.util.ArrayList;
import java.util.Collection;

/** Implant the search result in the form.
 *
 * <p>Forwards to:<ul>
 *      <li>"success"      subscribed ok.</li>
 * </ul>
 *
 * @author Mike Douglass   douglm@rpi.edu
 */
public class RenderSearchResultAction extends RenderAction {
  /* (non-Javadoc)
   * @see org.bedework.webcommon.BwAbstractAction#doAction(org.bedework.webcommon.BwRequest, org.bedework.webcommon.BwActionFormBase)
   */
  @Override
  public int doAction(final BwRequest request,
                      final BwActionFormBase form) throws Throwable {
    CalSvcI svci = form.fetchSvci();
    Client cl = form.fetchClient();

    int start = form.getResultStart();
    int count = form.getResultCt();

    SearchLimits limits = null;

    String lim = form.getSearchLimits();
    if (lim != null) {
      if ("none".equals(lim)) {
        // no limits
      } else if ("beforeToday".equals(lim)) {
        limits = svci.getIndexingHandler().beforeToday();
      } else if ("fromToday".equals(lim)) {
        limits = svci.getIndexingHandler().fromToday();
      }
    }

    Collection<BwIndexSearchResultEntry> sr =
      svci.getIndexingHandler().getSearchResult(start, count, limits);
    Collection<SearchResultEntry> sres = new ArrayList<SearchResultEntry>();

    IcalTranslator trans = new IcalTranslator(new IcalCallbackcb(cl));

    for (BwIndexSearchResultEntry sre: sr) {
      if (sre.getEvent() != null) {
        EventFormatter ev = new EventFormatter(cl, trans, sre.getEvent());
        sres.add(new SearchResultEntry(ev, sre.getScore()));

      } else {
        sres.add(new SearchResultEntry(sre.getCal(), sre.getScore()));
      }
    }

    form.setSearchResult(sres);

    return forwardSuccess;
  }
}
