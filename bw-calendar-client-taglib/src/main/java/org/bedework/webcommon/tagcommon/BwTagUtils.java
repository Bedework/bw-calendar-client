/* ********************************************************************
    Appropriate copyright notice
*/
package org.bedework.webcommon.tagcommon;

import org.bedework.appcommon.DateTimeFormatter;
import org.bedework.calfacade.BwCategory;
import org.bedework.calfacade.BwXproperty;
import org.bedework.convert.RecurRuleComponents;
import org.bedework.util.misc.Util;
import org.bedework.util.webaction.ErrorEmitSvlt;
import org.bedework.webcommon.BwModuleState;

import jakarta.servlet.jsp.JspWriter;

import java.io.IOException;
import java.util.Collection;

/**
 * User: mike Date: 4/2/22 Time: 11:52
 */
public class BwTagUtils extends BwTagUtilCommon {
  /**
   *
   * @param out writer
   * @param indent starting indent level or null
   * @param date the formatted date
   * @throws IOException on write error
   */
  public static void outFormattedDate(final JspWriter out,
                                      final String indent,
                                      final String tagName,
                                      final DateTimeFormatter date)
          throws IOException {
    var curIndent = indent;
    // All date/time information below is in "local" time
    outTagged(out, curIndent, "allday", date.getDateType());
    outTagged(out, curIndent, "floating", date.getFloating());
    outTagged(out, curIndent, "utc", date.getUtc());
    outTagged(out, curIndent, "utcdate", date.getUtcDate());
    outTagged(out, curIndent, "unformatted", date.getUnformatted());
    final var fmtdate = date.getFormatted();

    outTagged(out, curIndent, "year", fmtdate.getYear());
    outTagged(out, curIndent, "year4", fmtdate.getFourDigitYear());
    outTagged(out, curIndent, "month", fmtdate.getMonth());
    outTagged(out, curIndent, "twodigitmonth", fmtdate.getTwoDigitMonth());
    outTagged(out, curIndent, "monthname", fmtdate.getMonthName());
    outTagged(out, curIndent, "day", fmtdate.getDay());
    outTagged(out, curIndent, "dayname", fmtdate.getDayName());
    outTagged(out, curIndent, "twodigitday", fmtdate.getTwoDigitDay());
    outTagged(out, curIndent, "hour24", fmtdate.getHour24());
    outTagged(out, curIndent, "twodigithour24", fmtdate.getTwoDigitHour24());
    outTagged(out, curIndent, "hour", fmtdate.getHour());
    outTagged(out, curIndent, "twodigithour", fmtdate.getTwoDigitHour());
    outTagged(out, curIndent, "minute", fmtdate.getMinute());
    outTagged(out, curIndent, "twodigitminute", fmtdate.getTwoDigitMinute());
    outTagged(out, curIndent, "ampm", fmtdate.getAmPm());
    outTagged(out, curIndent, "longdate", fmtdate.getLongDateString());
    outTagged(out, curIndent, "shortdate", fmtdate.getDateString());
    outTagged(out, curIndent, "time", fmtdate.getTimeString());
    curIndent = openTag(out, curIndent, "timezone", true);

    outTagged(out, curIndent, "id", date.getTzid());
    outTagged(out, curIndent, "islocal", date.getTzIsLocal());
    if (!date.getTzIsLocal()) {
      final var tzdate = date.getTzFormatted();

      outTagged(out, curIndent, "date", tzdate.getDate());
      outTagged(out, curIndent, "year", tzdate.getYear());
      outTagged(out, curIndent, "fourdigityear", tzdate.getFourDigitYear());
      outTagged(out, curIndent, "month", tzdate.getMonth());
      outTagged(out, curIndent, "twodigitmonth", tzdate.getTwoDigitMonth());
      outTagged(out, curIndent, "monthname", tzdate.getMonthName());
      outTagged(out, curIndent, "day", tzdate.getDay());
      outTagged(out, curIndent, "dayname", tzdate.getDayName());
      outTagged(out, curIndent, "twodigitday", tzdate.getTwoDigitDay());
      outTagged(out, curIndent, "hour24", tzdate.getHour24());
      outTagged(out, curIndent, "twodigithour24", tzdate.getTwoDigitHour24());
      outTagged(out, curIndent, "hour", tzdate.getHour());
      outTagged(out, curIndent, "twodigithour", tzdate.getTwoDigitHour());
      outTagged(out, curIndent, "minute", tzdate.getMinute());
      outTagged(out, curIndent, "twodigitminute", tzdate.getTwoDigitMinute());
      outTagged(out, curIndent, "ampm", tzdate.getAmPm());
      outTagged(out, curIndent, "longdate", tzdate.getLongDateString());
      outTagged(out, curIndent, "shortdate", tzdate.getDateString());
      outTagged(out, curIndent, "time", tzdate.getTimeString());

    }

    closeTag(out, curIndent, "timezone");
  }

  /**
   *
   * @param out writer
   * @param indent starting indent level or null
   * @param cats the categories
   * @throws IOException on write error
   */
  public static void outCategories(final JspWriter out,
                                   final String indent,
                                   final String tagName,
                                   final boolean full,
                                   final Collection<BwCategory> cats)
          throws IOException {
    final var curIndent = pushIndent(indent);

    // Assume indented for first
    openTag(out, null, tagName, true);

    if (!Util.isEmpty(cats)) {
      for (final var category: cats) {
        outCategory(out, curIndent, null, full, category);
      }
    }

    closeTag(out, curIndent, tagName);
  }

  /** Output with surrounding tag
   *
   * @param out writer
   * @param indent starting indent level or null
   * @param category the category
   * @throws IOException on write error
   */
  public static void outCategory(final JspWriter out,
                                 final String indent,
                                 final String tagName,
                                 final boolean full,
                                 final BwCategory category)
          throws IOException {
    var curIndent = pushIndent(indent);

    if (tagName != null) {
      // Assume indented for first
      openTag(out, null, tagName, true);
    }

    curIndent = openTag(out, curIndent, "category", true);

    outTagged(out, curIndent, "uid", category.getUid());
    outTagged(out, curIndent, "value",
              category.getWordVal());
    outTagged(out, curIndent, "description", category.getDescriptionVal());

    if (full) {
      outTagged(out, curIndent, "href", category.getHref());
      outTagged(out, curIndent, "colPath", category.getColPath());
      outTagged(out, curIndent, "name", category.getName());
      outTagged(out, curIndent, "status", category.getStatus());
      outTagged(out, curIndent, "creator", category.getCreatorHref());
    }

    curIndent = closeTag(out, curIndent, "category");

    if (tagName != null) {
      closeTag(out, curIndent, tagName);
    }
  }

  /**
   *
   * @param out writer
   * @param indent starting indent level or null
   * @param msgs the message object
   * @throws IOException on write error
   */
  public static void outMsgErr(final JspWriter out,
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

      curIndent = closeTag(out, curIndent, tagName);
    }
  }

  /**
   *
   * @param out writer
   * @param indent starting indent level or null
   * @param rrules the rules
   * @throws IOException on write error
   */
  public static void outRrules(final JspWriter out,
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
        curIndent = openTag(out, curIndent, "byday", true);

        for (final var posdays: rrc.getByDay()) {
          curIndent = openTag(out, curIndent, "pos", true,
                              new Attribute("val",
                                            posdays.getPos().toString()));

          for (final var day: posdays.getDays()) {
            outTagged(out, curIndent, "day", day);
          }

          curIndent = closeTag(out, curIndent, "pos");
        }

        curIndent = closeTag(out, curIndent, "byday");
      }

      if (!Util.isEmpty(rrc.getByMonthDay())) {
        curIndent = openTag(out, curIndent, "bymonthday", true);

        for (final var val: rrc.getByMonthDay()) {
          outTagged(out, curIndent, "val", val);
        }

        curIndent = closeTag(out, curIndent, "bymonthday");
      }

      if (!Util.isEmpty(rrc.getByYearDay())) {
        curIndent = openTag(out, curIndent, "byyearday", true);

        for (final var val: rrc.getByYearDay()) {
          outTagged(out, curIndent, "val", val);
        }

        curIndent = closeTag(out, curIndent, "byyearday");
      }

      if (!Util.isEmpty(rrc.getByWeekNo())) {
        curIndent = openTag(out, curIndent, "byweekno", true);

        for (final var val: rrc.getByWeekNo()) {
          outTagged(out, curIndent, "val", val);
        }

        curIndent = closeTag(out, curIndent, "byweekno");
      }

      if (!Util.isEmpty(rrc.getByMonth())) {
        curIndent = openTag(out, curIndent, "bymonth", true);

        for (final var val: rrc.getByMonth()) {
          outTagged(out, curIndent, "val", val);
        }

        curIndent = closeTag(out, curIndent, "bymonth");
      }

      if (!Util.isEmpty(rrc.getBySetPos())) {
        curIndent = openTag(out, curIndent, "bysetpos", true);

        for (final var val: rrc.getBySetPos()) {
          outTagged(out, curIndent, "val", val);
        }

        curIndent = closeTag(out, curIndent, "bysetpos");
      }

      if (rrc.getWkst() != null) {
        outTagged(out, curIndent, "wkst", rrc.getWkst());
      }

      curIndent = closeTag(out, curIndent, "recurrence");
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
      curIndent = openTag(out, curIndent, xprop.getName(), true);

      if (!Util.isEmpty(xprop.getParameters())) {
        curIndent = openTag(out, curIndent, "parameters", true);

        for (final var param: xprop.getParameters()) {
          outTagged(out, curIndent, param.getName(),
                    param.getValue(), false, true);
        }

        curIndent = closeTag(out, curIndent, "parameters");
      }

      curIndent = openTag(out, curIndent, "values", true);

      outTagged(out, curIndent, "text",
                xprop.getValue(), false, true);

      curIndent = closeTag(out, curIndent, "values");

      curIndent = closeTag(out, curIndent, xprop.getName());
    }

    closeTag(out, indent, "xproperties");
  }

  public static void outLabels(final JspWriter out,
                               final String indent,
                               final BwModuleState mstate)
          throws IOException {
    var curIndent = pushIndent(indent);
    final var calInfo = mstate.getCalInfo();
    final var forLabels = mstate.getEventDates().getForLabels();

    // Assume indented for first
    openTag(out, null, "daylabels", true);

    for (final var dayLabel: calInfo.getDayLabels()) {
      outTagged(out, indent, "val", dayLabel);
    }

    curIndent = closeTag(out, curIndent, "daylabels");

    curIndent = openTag(out, curIndent, "dayvalues", true);
    for (final var dayVal: calInfo.getDayVals()) {
      outTagged(out, indent, "val", dayVal);
    }
    outTagged(out, indent, "start",
              mstate.getViewStartDate().getDay());
    curIndent = closeTag(out, curIndent, "dayvalues");

    curIndent = openTag(out, curIndent, "daynames", true);
    for (final var dayName: calInfo.getDayNamesAdjusted()) {
      outTagged(out, indent, "val", dayName);
    }
    curIndent = closeTag(out, curIndent, "daynames");

    curIndent = openTag(out, curIndent, "shortdaynames", true);
    for (final var shortDayName: calInfo.getShortDayNamesAdjusted()) {
      outTagged(out, indent, "val", shortDayName);
    }
    curIndent = closeTag(out, curIndent, "shortdaynames");

    curIndent = openTag(out, curIndent, "recurdayvals", true);
    for (final var recurDayName: calInfo.getRecurDayNamesAdjusted()) {
      outTagged(out, indent, "val", recurDayName);
    }
    curIndent = closeTag(out, curIndent, "recurdayvals");
    curIndent = openTag(out, curIndent, "monthlabels", true);
    for (final var monthLabel: calInfo.getMonthLabels()) {
      outTagged(out, indent, "val", monthLabel);
    }
    curIndent = closeTag(out, curIndent, "monthlabels");

    curIndent = openTag(out, curIndent, "monthvalues", true);
    for (final var monthVal: calInfo.getMonthVals()) {
      outTagged(out, indent, "val", monthVal);
    }
    outTagged(out, indent, "start", mstate.getViewStartDate().getMonth());
    curIndent = closeTag(out, curIndent, "monthvalues");

    curIndent = openTag(out, curIndent, "yearvalues", true);
    for (final var yearVals: calInfo.getYearVals()) {
      outTagged(out, indent, "val", yearVals);
    }
    outTagged(out, indent, "start", mstate.getViewStartDate().getYear());
    curIndent = closeTag(out, curIndent, "yearvalues");

    curIndent = openTag(out, curIndent, "hourlabels", true);
    for (final var hourLabel: forLabels.getHourLabels()) {
      outTagged(out, indent, "val", hourLabel);
    }
    curIndent = closeTag(out, curIndent, "hourlabels");

    curIndent = openTag(out, curIndent, "hourvalues", true);
    for (final var hourVal: forLabels.getHourVals()) {
      outTagged(out, indent, "val", hourVal);
    }
    outTagged(out, indent, "start", mstate.getViewStartDate().getHour());
    curIndent = closeTag(out, curIndent, "hourvalues");

    curIndent = openTag(out, curIndent, "minvalues", true);
    for (final var minuteVals: forLabels.getMinuteLabels()) {
      outTagged(out, indent, "val", minuteVals);
    }
    outTagged(out, indent, "start", mstate.getViewStartDate().getMinute());
//   for (final var minuteVals: forLabels.getMinuteLabels()) {
    curIndent = closeTag(out, curIndent, "minvalues");

    curIndent = openTag(out, curIndent, "ampmvalues", true);
    for (final var ampmVals: forLabels.getAmpmLabels()) {
      outTagged(out, indent, "val", ampmVals);
    }
    outTagged(out, indent, "start", mstate.getViewStartDate().getAmpm());
    curIndent = closeTag(out, curIndent, "ampmvalues");
  }
}
