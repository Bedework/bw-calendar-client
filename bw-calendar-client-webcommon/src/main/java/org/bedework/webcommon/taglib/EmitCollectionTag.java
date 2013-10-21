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
package org.bedework.webcommon.taglib;

import org.bedework.calfacade.BwCalendar;
import org.bedework.calfacade.wrappers.CalendarWrapper;

import org.bedework.util.xml.tagdefs.AppleServerTags;
import org.bedework.util.xml.tagdefs.NamespaceAbbrevs;

import java.net.URLEncoder;

import javax.servlet.jsp.JspTagException;
import javax.servlet.jsp.JspWriter;

/** Emit information about the given collection object. Parameters, <ul>
 * <li><b>name<b> (Required) defines the name of object embedded somewhere
 *               in the page context containing the path object.</li>
 * <li><b>property<b> Optional property</li>
 * <li><b>tagname<b> Optional property</li>
 * </ul>
 *
 * We retrieve the value and emit xml.
 *
 * @author Mike Douglass
 */
public class EmitCollectionTag extends EmitTextTag {
  private boolean full;

  private boolean noTag;

  /**
   * Constructor
   */
  public EmitCollectionTag() {
  }

  /** Called at end of Tag
   *
   * @return int      either EVAL_PAGE or SKIP_PAGE
   */
  @Override
  public int doEndTag() throws JspTagException {
    try {
      /* Try to retrieve the object */
      BwCalendar col = (BwCalendar)getObject(false);

      String outerTag = null;

      if (!getNoTag()) {
        outerTag = getTagName();
        if (outerTag == null) {
          outerTag = property;
        }

        if (outerTag == null) {
          outerTag = "collection";
        }
      }

      JspWriter out = pageContext.getOut();

      String indent = getIndent();
      if (indent == null) {
        indent = "";
      }

      outerTagStart(out, outerTag);

      emitCollection(col, out, indent);

      outerTagEnd(out, outerTag, indent);
    } catch(Throwable t) {
      t.printStackTrace();
      throw new JspTagException("Error: " + t.getMessage());
    } finally {
    }

    return EVAL_PAGE;
  }

  protected void outerTagStart(final JspWriter out,
                               final String tagName) throws Throwable {
    if (tagName == null) {
      return;
    }

    out.print('<');
    out.print(tagName);
    out.println('>');
  }

  protected void outerTagEnd(final JspWriter out,
                             final String tagName,
                             final String indent) throws Throwable {
    if (tagName == null) {
      return;
    }

    out.print(indent);
    out.print("</");
    out.print(tagName);
    out.println('>');
  }

  protected void emitCollection(final BwCalendar col,
                                final JspWriter out,
                                final String indent) throws Throwable {
    String val = null;

    if (col != null) {
      val = col.getName();
    }

    emitElement(out, indent, "name", val);

    if (col != null) {
      val = col.getSummary();
    }

    emitElement(out, indent, "summary", val);

    if (col != null) {
      val = col.getPath();
    }

    emitElement(out, indent, "path", val);

    if (val != null) {
      val = URLEncoder.encode(val, "UTF-8");
    }

    emitElement(out, indent, "encodedPath", val);

    if (col != null) {
      val = col.getOwnerHref();
    }

    emitElement(out, indent, "ownerHref", val);

    if (col != null) {
      val = String.valueOf(col.getCalType());
    }

    emitElement(out, indent, "actualCalType", val);

    if ((col != null) && (col.getCalType() == BwCalendar.calTypeAlias)) {
      if (col != null) {
        CalendarWrapper cw = (CalendarWrapper)col;
        BwCalendar target = cw.getAliasedEntity();
        if (target == null) {
          val = String.valueOf(BwCalendar.calTypeUnknown);
        } else {
          val = String.valueOf(target.getCalType());
        }
      }
    }

    emitElement(out, indent, "calType", val);

    if (col != null) {
      val = col.getProperty(NamespaceAbbrevs.prefixed(AppleServerTags.readWrite));
    } else {
      val = null;
    }

    if (val != null) {
      emitElement(out, indent, "read-write", "true");
    }

    if (col != null) {
      if (col.getShared()) {
        emitElement(out, indent, "shared", "true");
      }
    }

    emitElement(out, indent, "calendarCollection", val);

    if (col != null) {
      val = String.valueOf(col.getCalendarCollection());
    }

    emitElement(out, indent, "calendarCollection", val);

    if (col != null) {
      val = String.valueOf(col.getAffectsFreeBusy());
    }

    emitElement(out, indent, "affectsFreeBusy", val);

    if (col != null) {
      val = String.valueOf(col.getColor());
    }

    emitElement(out, indent, "color", val);

    if (col != null) {
      val = String.valueOf(col.getIsTopicalArea());
    }

    emitElement(out, indent, "isTopicalArea", val);

    if (col != null) {
      val = String.valueOf(col.getDisplay());
    }

    emitElement(out, indent, "display", val);

    if (col != null) {
      val = String.valueOf(col.getDisabled());
    }

    emitElement(out, indent, "disabled", val);

    if (col != null) {
      val = col.getLastRefreshStatus();
    }

    emitElement(out, indent, "lastRefreshStatus", val);

    if (col != null) {
      val = col.getLastRefresh();
    }

    emitElement(out, indent, "lastRefresh", val);

    if (col != null) {
      val = String.valueOf(col.getOpen());
    }

    emitElement(out, indent, "open", val);

    if ((col == null) || !full) {
      return;
    }

    String prefPath = getPreferences().getDefaultCalendarPath();

    if ((prefPath != null) && prefPath.equals(col.getPath())) {
      emitElement(out, indent, "default-scheduling-collection", null);
    } else if (col.getCollectionInfo().shareable) {
      emitElement(out, indent, "can-be-shared", null);
      emitElement(out, indent, "can-be-published", null);
    }

    String inviteStr = col.getProperty(NamespaceAbbrevs.prefixed(AppleServerTags.invite));

    if (inviteStr != null) {
      /* Lop off any XML header */

      if (inviteStr.startsWith("<?xml")) {
        inviteStr = inviteStr.substring(inviteStr.indexOf("<", 3));
      }
      out.println(inviteStr);
    }

    emitElement(out, indent, "desc",
                col.getDescription());

    emitElement(out, indent, "canAlias",
                String.valueOf(col.getCanAlias()));

    emitElement(out, indent, "isSubscription",
                String.valueOf(col.getAlias()));

    emitCdataElement(out, indent, "aliasUri",
                     col.getAliasUri());

    emitElement(out, indent, "internalAlias",
                String.valueOf(col.getInternalAlias()));

    emitElement(out, indent, "externalSub",
                String.valueOf(col.getExternalSub()));

    emitElement(out, indent, "unremoveable",
                String.valueOf(col.getUnremoveable()));

    emitElement(out, indent, "refreshRate",
                String.valueOf(col.getRefreshRate()));

    emitElement(out, indent, "filterExpr",
                col.getFilterExpr());

//      emitElement(out, indent, "timezone",
//                  col.getTimezone());
  }

  private void emitElement(final JspWriter out,
                           final String indent,
                           final String name, final String val) throws Throwable {
    emitElement(out, indent, name, val, false);
  }

  private void emitCdataElement(final JspWriter out,
                                final String indent,
                                final String name, final String val) throws Throwable {
    emitElement(out, indent, name, val, true);
  }

  private void emitElement(final JspWriter out,
                           final String indent,
                           final String name, final String val,
                           final boolean alwaysCdata) throws Throwable {
    out.print(indent);
    out.print("  <");
    out.print(name);
    out.print('>');

    if (val != null) {
      boolean cdata = alwaysCdata ||
          (val.indexOf('&') >= 0) ||
          (val.indexOf('<') >= 0) ||
          (val.indexOf("<![CDATA[") >= 0);

      if (!cdata) {
        out.print(val);
      } else {
        // We have to watch for text that includes "]]"

        int start = 0;

        while (start < val.length()) {
          int end = val.indexOf("]]", start);
          boolean lastSeg = end < 0;
          String seg;

          if (lastSeg) {
            seg = val.substring(start);
          } else {
            seg = val.substring(start, end);
          }

          cdata = alwaysCdata ||
              (seg.indexOf('&') >= 0) ||
              (seg.indexOf('<') >= 0) ||
              (seg.indexOf("<![CDATA[") >= 0) ||
              ((start > 0) && seg.startsWith(">")); // Don't rebuild "]]>"

          if (!cdata) {
            out.print(seg);
          } else {
            out.print("<![CDATA[");
            out.print(seg);
            out.print("]]>");
          }

          if (lastSeg) {
            break;
          }

          out.print("]]");
          start = end + 2;
        }
      }
    }

    out.print("</");
    out.print(name);
    out.println('>');
  }

  /** Sets the full value.
   *
   * @param val
   */
  public void setFull(final boolean val) {
    full = val;
  }

  /** Returns the full value.
   *
   * @return boolean
   */
  public boolean getFull() {
      return full;
  }

  /** Sets the noTag value.
   *
   * @param val
   */
  public void setNoTag(final boolean val) {
    noTag = val;
  }

  /** Returns the full value.
   *
   * @return boolean
   */
  public boolean getNoTag() {
      return noTag;
  }
}
