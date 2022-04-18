/* ********************************************************************
    Appropriate copyright notice
*/
package org.bedework.webcommon.tagcommon;

import org.bedework.webcommon.TimeDateComponents;

import java.io.IOException;

import javax.servlet.jsp.JspWriter;

/**
 * User: mike Date: 4/8/22 Time: 14:38
 */
public class BwFormTagUtils extends BwTagUtilCommon {
  /**
   *
   * @param out writer
   * @param indent starting indent level or null
   * @param tagName non-null for surrounding tag
   * @param value non-null for current value
   * @param name non-null
   * @param optionLabels labels for list
   * @param optionValues values for list
   * @throws IOException on write error
   */
  public static void outSelect(final JspWriter out,
                               final String indent,
                               final String tagName,
                               final String value,
                               final String name,
                               final String[] optionLabels,
                               final String[] optionValues)
          throws IOException {
    String curIndent;
    final String selectIndent;

    if (tagName == null) {
      selectIndent = null;
      curIndent = pushIndent(indent);
    } else {
      selectIndent = pushIndent(indent);
      curIndent = pushIndent(selectIndent);
      openTag(out, null, tagName, true);
    }
    // Assume indented for first
    openTag(out, selectIndent, "select", true,
            new Attribute("name", name));

    for (int i = 0; i<optionLabels.length; i++) {
      final var label = optionLabels[i];
      final String val;

      if (optionValues == null) {
        val = label;
      } else {
        val = optionValues[i];
      }

      if (val.equals(value)) {
        outTagged(out, curIndent, "option", label, false, false,
                  new Attribute("value", val),
                  new Attribute("selected","selected"));
      } else {
        outTagged(out, curIndent, "option", label, false, false,
                  new Attribute("value", val));
      }

    }

    curIndent = closeTag(out, curIndent, "select");
    if (tagName != null) {
      closeTag(out, curIndent, tagName);
    }
  }

  public static void outDateSelect(final JspWriter out,
                                   final String indent,
                                   final String name,
                                   final TimeDateComponents tdc,
                                   final String[] yearVals)
          throws IOException {
    outSelect(out, indent, "month",
              String.valueOf(tdc.getMonth()),
              name + ".month",
              tdc.getMonthLabels(),
              tdc.getMonthVals());

    outSelect(out, indent, "day",
              String.valueOf(tdc.getDay()),
              name + ".day",
              tdc.getDayLabels(),
              tdc.getDayVals());

    outSelect(out, indent, "year",
              String.valueOf(tdc.getYear()),
              name + ".year",
              yearVals,
              null);
  }

  public static void outTimeSelect(final JspWriter out,
                                   final String indent,
                                   final String name,
                                   final TimeDateComponents tdc,
                                   final boolean hour24)
          throws IOException {
    outSelect(out, indent, "hour",
              String.valueOf(tdc.getHour()),
              name + ".hour",
              tdc.getHourLabels(),
              tdc.getHourVals());

    outSelect(out, indent, "minute",
              String.valueOf(tdc.getMinute()),
              name + ".minute",
              tdc.getMinuteLabels(),
              tdc.getMinuteVals());

    if (!hour24) {
      outSelect(out, indent, "ampm",
                String.valueOf(tdc.getAmpm()),
                name + ".ampm",
                tdc.getAmpmLabels(),
                null);
    }
  }
}
