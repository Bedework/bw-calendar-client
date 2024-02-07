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
import org.bedework.calfacade.BwCategory;
import org.bedework.calfacade.exc.CalFacadeException;
import org.bedework.calfacade.responses.CollectionsResponse;
import org.bedework.util.misc.Util;
import org.bedework.webcommon.BwAbstractAction;
import org.bedework.webcommon.BwActionFormBase;
import org.bedework.webcommon.BwRequest;
import org.bedework.webcommon.BwSession;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.http.HttpServletResponse;

import static org.bedework.util.misc.Util.escapeJava;

/** This action fetches all collections and embeds them in the session.
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
                      final BwActionFormBase form) {
    final BwSession sess = request.getSess();
    final CollectionsResponse cols = sess.getCollections(request);
    final Client cl = request.getClient();
    final HttpServletResponse resp = request.getResponse();

    final String format = request.getReqPar("format");
    
    if (format == null) {
      //resp.setHeader("Content-Disposition",
      //               "Attachment; Filename=\"categoryList.json\"");
      cl.outputJson(resp, null, null, cols);

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

      try (final PrintWriter pw = resp.getWriter()) {

        writeCols(cl, col, pw);
      } catch (final IOException ioe) {
        throw new CalFacadeException(ioe);
      }
    }
    
    return forwardNull;
  }
  
  private void writeCols(final Client cl,
                         final BwCalendar col, 
                         final PrintWriter pw) {
    writeCol(cl, col, pw);
    
    if (Util.isEmpty(col.getChildren())) {
      return;
    }

    for (final BwCalendar child: col.getChildren()) {
      writeCols(cl, child, pw);
    }
  }

  private void writeCol(final Client cl,
                        final BwCalendar col,
                        final PrintWriter pw) {
    if (col.getColPath() == null) {
      // Skip root collections
      return;
    }

    pw.print("create collection ");

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

    pw.print("\"" + col.getColPath() + "\" ");
    pw.print("\"" + col.getName() + "\" ");
    pw.print("\"" + escapeJava(col.getSummary()) + "\" ");

    if (col.getCalType() == BwCalendar.calTypeAlias) {
      final String uri = col.getAliasUri();

      if (!uri.startsWith("bwcal://")) {
        pw.print(" \"" + col.getAliasUri() + "\" ");
      } else {
        pw.print(" \"" + col.getAliasUri().substring(8) + "\" ");
      }
    }

    pw.print("\"" + col.getOwnerHref() + "\" ");
    pw.print("\"" + col.getCreatorHref() + "\" ");

    if (col.getDescription() != null) {
      pw.print(" desc=\"" +
                       escapeJava(col.getDescription())
                       + "\"");
    }

    //if (col.getFilterExpr() != null) {
    //  pw.print(" filter=\"" + 
    //                   escapeJava(col.getFilterExpr())
    //                   + "\"");
    //}

    final StringBuilder filterExpr = new StringBuilder();

    if (!Util.isEmpty(col.getCategoryHrefs())) {
      try {
        for (final String href: col.getCategoryHrefs()) {
          final BwCategory cat = cl.getCategory(href);

          pw.print(" category=\"" + cat.getHref() + "\"");

          if (filterExpr.length() == 0) {
            filterExpr.append("(");
          } else {
            filterExpr.append(" | ");
          }
          filterExpr.append("categories.href=\"");
          filterExpr.append(cat.getHref());
          filterExpr.append("\"");
        }
      } catch (final CalFacadeException cfe) {
        pw.print(" category-error=\"" + cfe.getDetailMessage() + "\"");
      }
    }

    if (filterExpr.length() != 0) {
      filterExpr.append(")");
      pw.print(" filter=\"");
      pw.print(escapeJava(filterExpr.toString()));
      pw.print("\"");
    }

    pw.println();
  }
}
