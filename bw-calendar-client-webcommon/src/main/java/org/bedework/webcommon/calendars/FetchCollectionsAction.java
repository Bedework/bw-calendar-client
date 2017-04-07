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
package org.bedework.webcommon.calendars;

import org.bedework.appcommon.client.Client;
import org.bedework.calfacade.BwCalendar;
import org.bedework.calfacade.responses.CollectionsResponse;
import org.bedework.util.misc.Util;
import org.bedework.webcommon.BwAbstractAction;
import org.bedework.webcommon.BwActionFormBase;
import org.bedework.webcommon.BwRequest;
import org.bedework.webcommon.BwSession;

import java.io.PrintWriter;

import javax.servlet.http.HttpServletResponse;

/** This action fetches all categories and embeds them in the session.
 *
 * <p>Forwards to:<ul>
 *      <li>"success"      ok.</li>
 * </ul>
 *
 * @author Mike Douglass   douglm@rpi.edu
 */
public class FetchCollectionsAction extends BwAbstractAction {
  @Override
  public int doAction(final BwRequest request,
                      final BwActionFormBase form) throws Throwable {
    final BwSession sess = request.getSess();
    final CollectionsResponse cols = sess.getCollections(request);
    final Client cl = request.getClient();
    final HttpServletResponse resp = request.getResponse();

    final String format = request.getReqPar("format");
    
    if (format == null) {
      //resp.setHeader("Content-Disposition",
      //               "Attachment; Filename=\"categoryList.json\"");
      resp.setContentType("application/json; charset=UTF-8");

      cl.writeJson(resp, cols);
      resp.getOutputStream().close();

      return forwardNull;
    }

    if (format.equals("cmds")) {
      // Create a bunch of commands
      
      final BwCalendar col;
      
      if (cols.getCollections() != null) {
        col = cols.getCollections();
      } else if (cols.getPublicCollections() != null) {
        col = cols.getPublicCollections();
      } else if (cols.getUserCollections() != null) {
        col = cols.getUserCollections();
      } else {
        col = null;
      }

      resp.setContentType("application/text; charset=UTF-8");

      if (col == null) {
        return forwardNull;
      }

      final PrintWriter pw = resp.getWriter();
      
      writeCol(col, pw);
    }
    
    return forwardNull;
  }
  
  private void writeCol(final BwCalendar col, 
                        final PrintWriter pw) {
    pw.print("create collection ");
    pw.print("\"" + col.getPath() + "\" ");
    pw.print("\"" + col.getName() + "\"");
    
    if (col.getCalType() == BwCalendar.calTypeFolder) {
      pw.print(" folder ");
    } else if (col.getCalType() == BwCalendar.calTypeCalendarCollection) {
      pw.print(" calendar ");
    } else if (col.getCalType() == BwCalendar.calTypeAlias) {
      if (col.getIsTopicalArea()) {
        pw.print(" topic ");
      } else {
        pw.print(" alias ");
      }
    } else {
      pw.print(" *" + col.getCalType() + "* ");
    }

    pw.print(col.getOwnerHref());

    if (col.getCalType() == BwCalendar.calTypeAlias) {
      final String uri = col.getAliasUri();
      
      if (!uri.startsWith("bwcal://")) {
        pw.print(" \"" + col.getAliasUri() + "\"");
      } else {
        pw.print(" \"" + col.getAliasUri().substring(8) + "\"");
      }
    }
    
    if (col.getFilterExpr() != null) {
      pw.print(" filter=\"" + col.getFilterExpr() + "\"");
    }
    
    if (!Util.isEmpty(col.getCategoryUids())) {
      for (final String cuid: col.getCategoryUids()) {
        pw.print(" category=\"" + cuid + "\"");
      }
    }

    pw.println();
    
    if (Util.isEmpty(col.getChildren())) {
      return;
    }

    for (final BwCalendar child: col.getChildren()) {
      writeCol(child, pw);
    }
  }
}
