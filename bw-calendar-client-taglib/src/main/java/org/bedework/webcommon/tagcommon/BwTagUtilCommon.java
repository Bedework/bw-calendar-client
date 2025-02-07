/* ********************************************************************
    Appropriate copyright notice
*/
package org.bedework.webcommon.tagcommon;

import java.io.IOException;

import jakarta.servlet.jsp.JspWriter;

/**
 * User: mike Date: 4/8/22 Time: 14:39
 */
public class BwTagUtilCommon {
  public static class Attribute {
    final String name;
    final String val;

    public Attribute(final String name, final String val) {
      this.name = name;
      this.val = val;
    }

    public String getName() {
      return name;
    }

    public String getVal() {
      return val;
    }
  }

  public static void outTagged(final JspWriter out,
                               final String indent,
                               final String tagName,
                               final boolean value)
          throws IOException {
    outTagged(out, indent, tagName, String.valueOf(value),
              false, false, (Attribute[])null);
  }

  public static void outTagged(final JspWriter out,
                               final String indent,
                               final String tagName,
                               final int value)
          throws IOException {
    outTagged(out, indent, tagName, String.valueOf(value),
              false, false, (Attribute[])null);
  }

  public static void outTagged(final JspWriter out,
                               final String indent,
                               final String tagName,
                               final String value)
          throws IOException {
    outTagged(out, indent, tagName, value, false, false,
              (Attribute[])null);
  }

  public static void outTaggedIfPresent(final JspWriter out,
                                        final String indent,
                                        final String tagName,
                                        final String value)
          throws IOException {
    outTagged(out, indent, tagName, value, false, false,
              true, (Attribute[])null);
  }

  public static void outTagged(final JspWriter out,
                               final String indent,
                               final String tagName,
                               final String value,
                               final boolean filtered,
                               final boolean alwaysCdata)
          throws IOException {
    outTagged(out, indent, tagName, value, filtered, alwaysCdata,
              (Attribute[])null);
  }

  public static void outTagged(final JspWriter out,
                               final String indent,
                               final String tagName,
                               final String value,
                               final boolean filtered,
                               final boolean alwaysCdata,
                               final Attribute... attr)
          throws IOException {
    outTagged(out, indent, tagName, value, filtered, alwaysCdata,
              false, attr);
  }

  public static void outTagged(final JspWriter out,
                               final String indent,
                               final String tagName,
                               final String value,
                               final boolean filtered,
                               final boolean alwaysCdata,
                               final boolean skipIfNull,
                               final Attribute... attr)
          throws IOException {
    if (skipIfNull && (value == null)) {
      return;
    }

    openTag(out, indent, tagName, false, attr);

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

  public static String openTag(final JspWriter out,
                               final String indent,
                               final String tagName,
                               final boolean newline)
          throws IOException {
    return openTag(out, indent, tagName, newline, (Attribute[])null);
  }

  public static String openTag(final JspWriter out,
                               final String indent,
                               final String tagName,
                               final boolean newline,
                               final Attribute... attrs)
          throws IOException {
    if (indent != null) {
      out.print(indent);
    }
    out.print('<');
    out.print(tagName);

    if (attrs != null) {
      for (final var attr: attrs) {
        out.print(' ');
        out.print(attr.getName());
        out.print("=\"");
        out.print(attr.getVal());
        out.print('"');
      }
    }

    out.print('>');
    if (newline) {
      out.newLine();
    }

    return pushIndent(indent);
  }

  public static String startTag(final JspWriter out,
                                final String indent,
                                final String tagName,
                                final boolean newline)
          throws IOException {
    if (indent != null) {
      out.print(indent);
    }
    out.print('<');
    out.print(tagName);
    out.print(' ');

    if (newline) {
      out.newLine();
    }

    return pushIndent(indent);
  }

  public static void outAttribute(final JspWriter out,
                                  final String indent,
                                  final boolean newline,
                                  final Attribute attr)
          throws IOException {
    if (indent != null) {
      out.print(indent);
    }

    out.print(attr.getName());
    out.print("=\"");
    if (attr.getVal() != null) {
      out.print(attr.getVal());
    }
    out.print('"');
    out.print(' ');

    if (newline) {
      out.newLine();
    }
  }

  public static String endTag(final JspWriter out,
                              final String indent,
                              final boolean newline)
          throws IOException {
    if (indent != null) {
      out.print(indent);
    }
    out.print("/>");

    if (newline) {
      out.newLine();
    }

    return popIndent(indent);
  }

  public static String closeTag(final JspWriter out,
                                final String indent,
                                final String tagName)
          throws IOException {
    return closeTag(out, indent, tagName, true);
  }

  public static String closeTag(final JspWriter out,
                                final String indent,
                                final String tagName,
                                final boolean newline)
          throws IOException {
    final var curIndent = popIndent(indent);
    if (curIndent != null) {
      out.print(curIndent);
    }
    out.print("</");
    out.print(tagName);
    out.print('>');

    if (newline) {
      out.newLine();
    }

    return curIndent;
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

  protected static String pushIndent(final String val) {
    if (val == null) {
      return null;
    }

    return val + "  ";
  }

  protected static String popIndent(final String val) {
    if (val == null) {
      return null;
    }

    if (val.length() < 2) {
      return val;
    }

    return val.substring(2);
  }
}
