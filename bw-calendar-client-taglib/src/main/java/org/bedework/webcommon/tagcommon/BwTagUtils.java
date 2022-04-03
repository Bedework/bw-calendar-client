/* ********************************************************************
    Appropriate copyright notice
*/
package org.bedework.webcommon.tagcommon;

import org.bedework.calfacade.BwXproperty;
import org.bedework.util.misc.Util;

import java.io.IOException;
import java.util.Collection;

import javax.servlet.jsp.JspWriter;

/**
 * User: mike Date: 4/2/22 Time: 11:52
 */
public class BwTagUtils {
  public static void OutXprops(final JspWriter out,
                               final String indent,
                               final boolean skipNonJsp,
                               final Collection<BwXproperty> xprops)
          throws IOException {
    if (Util.isEmpty(xprops)) {
      return;
    }

    var curIndent = pushIndent(indent);

    // Assume indented for first
    openTag(out, null, "xproperties", true);

    for (final var xprop: xprops) {
      if (skipNonJsp && xprop.getSkipJsp()) {
        continue;
      }
      openTag(out, curIndent, xprop.getName(), true);
      curIndent = pushIndent(curIndent);

      if (!Util.isEmpty(xprop.getParameters())) {
        openTag(out, curIndent, "parameters", true);
        curIndent = pushIndent(curIndent);

        for (final var param: xprop.getParameters()) {
          outTagged(out, curIndent, param.getName(),
                    param.getValue(), false, true);
        }

        curIndent = popIndent(curIndent);
        closeTag(out, curIndent, "parameters");
      }

      openTag(out, curIndent, "values", true);
      curIndent = pushIndent(curIndent);

      outTagged(out, curIndent, "text",
                xprop.getValue(), false, true);

      curIndent = popIndent(curIndent);
      closeTag(out, curIndent, "values");

      curIndent = popIndent(curIndent);
      closeTag(out, curIndent, xprop.getName());
    }

    closeTag(out, indent, "xproperties");
  }

  public static void outTagged(final JspWriter out,
                               final String indent,
                               final String tagName,
                               final String value,
                               final boolean filtered,
                               final boolean alwaysCdata)
          throws IOException {
    openTag(out, indent, tagName, false);

    if (value != null) {
      boolean cdata = alwaysCdata;
      if (!cdata && !filtered) {
        cdata = value.contains("&") ||
              value.contains("<") ||
              value.contains("<![CDATA[");
      }

      if (!cdata) {
        out.print(formatted(value, filtered));
      } else {
        // We have to watch for text that includes "]]"

        int start = 0;

        while (start < value.length()) {
          final int end = value.indexOf("]]", start);
          final boolean lastSeg = end < 0;
          final String seg;

          if (lastSeg) {
            seg = value.substring(start);
          } else {
            seg = value.substring(start, end);
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

    closeTag(out, null, tagName);
  }

  public static void openTag(final JspWriter out,
                             final String indent,
                             final String tagName,
                             final boolean newline)
          throws IOException {
    if (indent != null) {
      out.print(indent);
    }
    out.print('<');
    out.print(tagName);
    out.print('>');
    if (newline) {
      out.newLine();
    }
  }

  public static void closeTag(final JspWriter out,
                              final String indent,
                              final String tagName)
          throws IOException {
    if (indent != null) {
      out.print(indent);
    }
    out.print("</");
    out.print(tagName);
    out.println('>');
  }

  public static String filter(final String value) {
    if (value != null && value.length() != 0) {
      StringBuilder result = null;
      String filtered;

      for (int i = 0; i < value.length(); ++i) {
        filtered = null;
        switch(value.charAt(i)) {
          case '"':
            filtered = "&quot;";
            break;
          case '&':
            filtered = "&amp;";
            break;
          case '\'':
            filtered = "&#39;";
            break;
          case '<':
            filtered = "&lt;";
            break;
          case '>':
            filtered = "&gt;";
        }

        if (result == null) {
          if (filtered != null) {
            result = new StringBuilder(value.length() + 50);
            if (i > 0) {
              result.append(value, 0, i);
            }

            result.append(filtered);
          }
        } else if (filtered == null) {
          result.append(value.charAt(i));
        } else {
          result.append(filtered);
        }
      }

      return result == null ? value : result.toString();
    } else {
      return value;
    }
  }

  private static String formatted(final String value,
                                  final boolean filtered) {
    if (filtered) {
      return filter(value);
    }

    return value;
  }

  private static String pushIndent(final String val) {
    if (val == null) {
      return null;
    }

    return val + "  ";
  }

  private static String popIndent(final String val) {
    if (val == null) {
      return null;
    }

    if (val.length() < 2) {
      return val;
    }

    return val.substring(2);
  }
}
