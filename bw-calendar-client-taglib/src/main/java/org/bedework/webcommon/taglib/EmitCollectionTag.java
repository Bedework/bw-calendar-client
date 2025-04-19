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

import org.bedework.calfacade.BwCollection;
import org.bedework.util.xml.tagdefs.AppleServerTags;
import org.bedework.util.xml.tagdefs.NamespaceAbbrevs;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import jakarta.servlet.jsp.JspTagException;
import jakarta.servlet.jsp.JspWriter;

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
      final BwCollection col = (BwCollection)getObject(false);

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

      final JspWriter out = pageContext.getOut();

      String indent = getIndent();
      if (indent == null) {
        indent = "";
      }

      outerTagStart(out, outerTag);

      emitCollection(col, out, indent);

      outerTagEnd(out, outerTag, indent);
    } catch(final Throwable t) {
      t.printStackTrace();
      throw new JspTagException("Error: " + t.getMessage());
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

  protected void emitCollection(final BwCollection colPar,
                                final JspWriter out,
                                final String indent) throws Throwable {
    final BwCollection col;
    if (colPar != null) {
      col = colPar;
    } else {
      col = new BwCollection();
    }

    emitElement(out, indent, "name", col.getName());
    emitElement(out, indent, "summary", col.getSummary());
    emitElement(out, indent, "path", col.getPath());

    if (col.getPath() != null) {
      emitElement(out, indent, "encodedPath",
                  URLEncoder.encode(col.getPath(),
                                    StandardCharsets.UTF_8));
    } else {
      emitElement(out, indent, "encodedPath", "null");
    }

    emitElement(out, indent, "ownerHref", col.getOwnerHref());
    emitElement(out, indent, "actualCalType",
                String.valueOf(col.getCalType()));

    int calType = col.getCalType();
    if (calType == BwCollection.calTypeAlias) {
      final BwCollection target = col.getAliasedEntity();

      if (target == null) {
        calType = BwCollection.calTypeUnknown;
      } else {
        calType = target.getCalType();
      }
    }

    emitElement(out, indent, "calType", String.valueOf(calType));

    emitElement(out, indent, "shared",
                col.getShared());

    emitElement(out, indent, "read-write",
                col.getSharedWritable());

    emitElement(out, indent, "calendarCollection",
                col.getCalendarCollection());

    emitElement(out, indent, "affectsFreeBusy",
                col.getAffectsFreeBusy());

    emitElement(out, indent, "color", col.getColor());

    emitElement(out, indent, "isTopicalArea",
                col.getIsTopicalArea());

    emitElement(out, indent, "primaryCollection",
                col.getPrimaryCollection());

    emitElement(out, indent, "display", col.getDisplay());

    emitElement(out, indent, "disabled", col.getDisabled());

    emitElement(out, indent, "lastRefreshStatus",
                col.getLastRefreshStatus());

    emitElement(out, indent, "lastRefresh", col.getLastRefresh());

    emitElement(out, indent, "open", col.getOpen());

    if (!full) {
      return;
    }

    final String prefPath = getPreferences().getDefaultCalendarPath();

    if ((prefPath != null) && prefPath.equals(col.getPath())) {
      emitElement(out, indent, "default-scheduling-collection", null);
      emitElement(out, indent, "can-be-shared", null);
    } else if (col.getCalType() == BwCollection.calTypeCalendarCollection) {
      emitElement(out, indent, "can-be-shared", null);
      emitElement(out, indent, "can-be-published", null);
    } else if (col.getCalType() == BwCollection.calTypeAlias ||
               col.getCalType() == BwCollection.calTypeExtSub) {
      emitElement(out, indent, "can-be-shared", null);
    }

    String inviteStr = col.getProperty(NamespaceAbbrevs.prefixed(AppleServerTags.invite));

    if (inviteStr != null) {
      /* Lop off any XML header */

      if (inviteStr.startsWith("<?xml")) {
        inviteStr = inviteStr.substring(inviteStr.indexOf("<", 3));
      }
      out.println(inviteStr);
    }

    emitElement(out, indent, "desc", col.getDescription());

    emitElement(out, indent, "canAlias",
                col.getCanAlias());

    emitElement(out, indent, "isSubscription",
                col.getAlias());

    emitCdataElement(out, indent, "aliasUri",
                     col.getAliasUri());

    emitElement(out, indent, "internalAlias",
                col.getInternalAlias());

    emitElement(out, indent, "externalSub",
                col.getExternalSub());

    emitElement(out, indent, "unremoveable",
                col.getUnremoveable());

    emitElement(out, indent, "refreshRate",
                String.valueOf(col.getRefreshRate()));

    emitElement(out, indent, "filterExpr",
                col.getFilterExpr());

//      emitElement(out, indent, "timezone",
//                  col.getTimezone());
  }

  private void emitElement(final JspWriter out,
                           final String indent,
                           final String name,
                           final String val) throws Throwable {
    emitElement(out, indent, name, val, false);
  }

  private void emitElement(final JspWriter out,
                           final String indent,
                           final String name,
                           final boolean val) throws Throwable {
    emitElement(out, indent, name, String.valueOf(val), false);
  }

  private void emitCdataElement(final JspWriter out,
                                final String indent,
                                final String name,
                                final String val) throws Throwable {
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
          (val.contains("<![CDATA["));

      if (!cdata) {
        out.print(val);
      } else {
        // We have to watch for text that includes "]]"

        int start = 0;

        while (start < val.length()) {
          final int end = val.indexOf("]]", start);
          final boolean lastSeg = end < 0;
          final String seg;

          if (lastSeg) {
            seg = val.substring(start);
          } else {
            seg = val.substring(start, end);
          }

          cdata = alwaysCdata ||
              (seg.indexOf('&') >= 0) ||
              (seg.indexOf('<') >= 0) ||
              (seg.contains("<![CDATA[")) ||
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
   * @param val tru for full
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
   * @param val the noTag value.
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
