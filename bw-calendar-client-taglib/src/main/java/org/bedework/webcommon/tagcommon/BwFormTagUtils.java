/* ********************************************************************
    Appropriate copyright notice
*/
package org.bedework.webcommon.tagcommon;

import org.bedework.calfacade.BwCalendar;
import org.bedework.calfacade.BwContact;
import org.bedework.calfacade.BwLocation;
import org.bedework.util.misc.Util;
import org.bedework.webcommon.TimeDateComponents;

import java.io.IOException;
import java.util.Collection;

import jakarta.servlet.jsp.JspWriter;

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
   * @throws IOException on write error
   */
  public static void outCheckbox(final JspWriter out,
                                  final String indent,
                                  final String tagName,
                                  final boolean value,
                                  final String name)
          throws IOException {
    String curIndent;
    final String firstIndent;

    if (tagName == null) {
      firstIndent = null;
      curIndent = pushIndent(indent);
    } else {
      firstIndent = pushIndent(indent);
      curIndent = pushIndent(firstIndent);
      openTag(out, null, tagName, indent != null);
    }

    // Assume indented for first
    startTag(out, firstIndent, "input", false);
    outAttribute(out, null, false, new Attribute("type", "checkbox"));
    outAttribute(out, null, false, new Attribute("name", name));
    outAttribute(out, null, false, new Attribute("value", "on"));

    if (value) {
      outAttribute(out, null, false,
                   new Attribute("checked", "checked"));
    }

    curIndent = endTag(out, null, indent != null);

    if (tagName != null) {
      closeTag(out, curIndent, tagName, false);
    }
  }
  /**
   *
   * @param out writer
   * @param indent starting indent level or null
   * @param tagName non-null for surrounding tag
   * @param value non-null for current value
   * @param name non-null
   * @throws IOException on write error
   */
  public static void outTextField(final JspWriter out,
                                  final String indent,
                                  final String tagName,
                                  final String value,
                                  final String name,
                                  final Collection<Attribute> attrs)
          throws IOException {
    String curIndent;
    final String firstIndent;

    if (tagName == null) {
      firstIndent = null;
      curIndent = pushIndent(indent);
    } else {
      firstIndent = pushIndent(indent);
      curIndent = pushIndent(firstIndent);
      openTag(out, null, tagName, indent != null);
    }

    // Assume indented for first
    startTag(out, firstIndent, "input", false);
    outAttribute(out, null, false, new Attribute("type", "text"));
    outAttribute(out, null, false, new Attribute("name", name));
    outAttribute(out, null, false, new Attribute("value", value));

    if (!Util.isEmpty(attrs)) {
      for (final var attr: attrs) {
        outAttribute(out, null, false, attr);
      }
    }

    curIndent = endTag(out, null, indent != null);

    if (tagName != null) {
      closeTag(out, curIndent, tagName, false);
    }
  }

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
                                   final String[] yearVals,
                                   final boolean notag,
                                   final boolean noyear)
          throws IOException {
    outSelect(out, indent, tag("month", notag),
              String.valueOf(tdc.getMonth()),
              name + ".month",
              tdc.getMonthLabels(),
              tdc.getMonthVals());

    outSelect(out, indent, tag("day", notag),
              String.valueOf(tdc.getDay()),
              name + ".day",
              tdc.getDayLabels(),
              tdc.getDayVals());

    if (!noyear) {
      outSelect(out, indent, tag("year", notag),
                String.valueOf(tdc.getYear()),
                name + ".year",
                yearVals,
                null);
    }
  }

  public static void outTimeSelect(final JspWriter out,
                                   final String indent,
                                   final String name,
                                   final TimeDateComponents tdc,
                                   final boolean notag)
          throws IOException {
    outSelect(out, indent, tag("hour", notag),
              String.valueOf(tdc.getHour()),
              name + ".hour",
              tdc.getHourLabels(),
              tdc.getHourVals());

    outSelect(out, indent, tag("minute", notag),
              String.valueOf(tdc.getMinute()),
              name + ".minute",
              tdc.getMinuteLabels(),
              tdc.getMinuteVals());

    if (!tdc.getHour24()) {
      outSelect(out, indent, tag("ampm", notag),
                String.valueOf(tdc.getAmpm()),
                name + ".ampm",
                tdc.getAmpmLabels(),
                null);
    }
  }

  public static void outCollectionSelect(
          final JspWriter out,
          final String indent,
          final String name,
          final String curval,
          final Collection<BwCalendar> cols)
          throws IOException {
    final String[] labels = new String[cols.size()];
    int i = 0;
    for (final var col: cols) {
      labels[i] = col.getPath();
      i++;
    }

    outSelect(out, indent,
              null,
              curval,
              name,
              labels, null);
  }

  public static void outLocationSelect(
          final JspWriter out,
          final String indent,
          final String name,
          final String curval,
          final Collection<BwLocation> locs)
          throws IOException {
    final String[] labels = new String[locs.size()];
    final String[] vals = new String[locs.size()];
    int i = 0;
    for (final var loc: locs) {
      labels[i] = loc.getAddress().getValue();
      vals[i] = loc.getUid();
      i++;
    }

    outSelect(out, indent,
              null,
              curval,
              name,
              labels, vals);
  }

  public static void outContactSelect(
          final JspWriter out,
          final String indent,
          final String name,
          final String curval,
          final Collection<BwContact> contacts)
          throws IOException {
    final String[] labels = new String[contacts.size()];
    final String[] vals = new String[contacts.size()];
    int i = 0;
    for (final var contact: contacts) {
      labels[i] = contact.getCn().getValue();
      vals[i] = contact.getUid();
      i++;
    }

    outSelect(out, indent,
              null,
              curval,
              name,
              labels, vals);
  }

  private static String tag(final String val,
                            final boolean notag) {
    if (notag) {
      return null;
    }
    return val;
  }
}
