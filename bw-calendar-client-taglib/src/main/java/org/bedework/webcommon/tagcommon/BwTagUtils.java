/* ********************************************************************
    Appropriate copyright notice
*/
package org.bedework.webcommon.tagcommon;

import org.bedework.calfacade.BwXproperty;
import org.bedework.convert.RecurRuleComponents;
import org.bedework.util.misc.Util;
import org.bedework.util.webaction.ErrorEmitSvlt;

import java.io.IOException;
import java.util.Collection;

import javax.servlet.jsp.JspWriter;

/**
 * User: mike Date: 4/2/22 Time: 11:52
 */
public class BwTagUtils {
  /**
   *
   * @param out writer
   * @param indent starting indent level or null
   * @param msgs the message object
   * @throws IOException on write error
   */
  public static void OutMsgErr(final JspWriter out,
                               final String indent,
                               final String tagName,
                               final ErrorEmitSvlt msgs)
          throws IOException {
    var curIndent = indent;
    boolean first = true;

    for (final var msg: msgs.getMsgList()) {
      if (first) {
        // Assume indented for first
        openTag(out, null, tagName, true);
        first = false;
      } else {
        openTag(out, curIndent, tagName, true);
      }
      curIndent = pushIndent(curIndent);

      outTagged(out, curIndent, "id", msg.getMsgId());

      for (final var param: msg.getParams()) {
        outTagged(out, curIndent, "param", param.toString());
      }

      curIndent = popIndent(curIndent);
      closeTag(out, curIndent, tagName);
    }
  }

  /**
   *
   * @param out writer
   * @param indent starting indent level or null
   * @param rrules the rules
   * @throws IOException on write error
   */
  public static void OutRrules(final JspWriter out,
                               final String indent,
                               final Collection<RecurRuleComponents> rrules)
          throws IOException {
    if (Util.isEmpty(rrules)) {
      return;
    }

    var curIndent = indent;
    boolean first = true;

    for (final var rrc: rrules) {
      if (first) {
        // Assume indented for first
        openTag(out, null, "recurrence", true);
        first = false;
      } else {
        openTag(out, curIndent, "recurrence", true);
      }
      curIndent = pushIndent(curIndent);

      outTagged(out, curIndent, "rule", rrc.getRule());
      outTagged(out, curIndent, "freq", rrc.getFreq().name());

      if (rrc.getUntil() != null) {
        outTagged(out, curIndent, "until", rrc.getUntil().toString());
      }

      outTagged(out, curIndent, "count",
                String.valueOf(rrc.getCount()));
      outTagged(out, curIndent, "interval",
                String.valueOf(rrc.getInterval()));

      // bySecond --%>
      // byMinue --%>
      // byHour --%>

      if (!Util.isEmpty(rrc.getByDay())) {
        openTag(out, curIndent, "byday", true);
        curIndent = pushIndent(curIndent);

        for (final var posdays: rrc.getByDay()) {
          openTag(out, curIndent, "pos", true,
                  new Attribute("val", posdays.getPos().toString()));
          curIndent = pushIndent(curIndent);

          for (final var day: posdays.getDays()) {
            outTagged(out, curIndent, "day", day);
          }

          curIndent = popIndent(curIndent);
          closeTag(out, curIndent, "pos");
        }

        curIndent = popIndent(curIndent);
        closeTag(out, curIndent, "byday");
      }

      if (!Util.isEmpty(rrc.getByMonthDay())) {
        openTag(out, curIndent, "bymonthday", true);
        curIndent = pushIndent(curIndent);

        for (final var val: rrc.getByMonthDay()) {
          outTagged(out, curIndent, "val", val.toString());
        }

        curIndent = popIndent(curIndent);
        closeTag(out, curIndent, "bymonthday");
      }

      if (!Util.isEmpty(rrc.getByYearDay())) {
        openTag(out, curIndent, "byyearday", true);
        curIndent = pushIndent(curIndent);

        for (final var val: rrc.getByYearDay()) {
          outTagged(out, curIndent, "val", val.toString());
        }

        curIndent = popIndent(curIndent);
        closeTag(out, curIndent, "byyearday");
      }

      if (!Util.isEmpty(rrc.getByWeekNo())) {
        openTag(out, curIndent, "byweekno", true);
        curIndent = pushIndent(curIndent);

        for (final var val: rrc.getByWeekNo()) {
          outTagged(out, curIndent, "val", val.toString());
        }

        curIndent = popIndent(curIndent);
        closeTag(out, curIndent, "byweekno");
      }

      if (!Util.isEmpty(rrc.getByMonth())) {
        openTag(out, curIndent, "bymonth", true);
        curIndent = pushIndent(curIndent);

        for (final var val: rrc.getByMonth()) {
          outTagged(out, curIndent, "val", val.toString());
        }

        curIndent = popIndent(curIndent);
        closeTag(out, curIndent, "bymonth");
      }

      if (!Util.isEmpty(rrc.getBySetPos())) {
        openTag(out, curIndent, "bysetpos", true);
        curIndent = pushIndent(curIndent);

        for (final var val: rrc.getBySetPos()) {
          outTagged(out, curIndent, "val", val.toString());
        }

        curIndent = popIndent(curIndent);
        closeTag(out, curIndent, "bysetpos");
      }

      if (rrc.getWkst() != null) {
        outTagged(out, curIndent, "wkst", rrc.getWkst());
      }

      curIndent = popIndent(curIndent);
      closeTag(out, curIndent, "recurrence");
    }
  }

  /**
   *
   * @param out writer
   * @param indent starting indent level or null
   * @param skipNonJsp false to include those marked with skip
   * @param xprops the xprops
   * @throws IOException on write error
   */
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
                               final String value)
          throws IOException {
    outTagged(out, indent, tagName, value, false, false, null);
  }

  public static void outTagged(final JspWriter out,
                               final String indent,
                               final String tagName,
                               final String value,
                               final boolean filtered,
                               final boolean alwaysCdata)
          throws IOException {
    outTagged(out, indent, tagName, value, filtered, alwaysCdata, null);
  }

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
                               final String value,
                               final boolean filtered,
                               final boolean alwaysCdata,
                               final Attribute attr)
          throws IOException {
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

  public static void openTag(final JspWriter out,
                             final String indent,
                             final String tagName,
                             final boolean newline)
          throws IOException {
    openTag(out, indent, tagName, newline, null);
  }

  public static void openTag(final JspWriter out,
                             final String indent,
                             final String tagName,
                             final boolean newline,
                             final Attribute attr)
          throws IOException {
    if (indent != null) {
      out.print(indent);
    }
    out.print('<');
    out.print(tagName);

    if (attr != null) {
      out.print(' ');
      out.print(attr.getName());
      out.print("=\"");
      out.print(attr.getVal());
      out.print('"');
    }

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
