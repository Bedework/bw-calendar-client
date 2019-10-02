<%@ taglib uri='struts-bean' prefix='bean' %>
      <%-- All date/time information below is in "local" time --%>
      <allday><bean:write name="date" property="dateType"/></allday><%--
        Value: true/false --%>
      <floating><bean:write name="date" property="floating"/></floating>
      <utc><bean:write name="date" property="utc"/></utc><%--
        Value: true or false --%>
      <utcdate><bean:write name="date" property="utcDate"/></utcdate><%--
        Value: yyyymmdd - date value --%>
      <unformatted><bean:write name="date" property="unformatted"/></unformatted><%--
        Value: yyyymmdd - date value --%>

      <bean:define id="fmtdate" name="date" property="formatted" />

      <year><bean:write name="fmtdate" property="year"/></year><%--
        Value: yyyy - year value --%>
      <fourdigityear><bean:write name="fmtdate" property="fourDigitYear"/></fourdigityear><%--
        Value: yyyy - four digit year value.  --%>
      <month><bean:write name="fmtdate" property="month"/></month><%--
        Value: m - single or two digit month value --%>
      <twodigitmonth><bean:write name="fmtdate" property="twoDigitMonth"/></twodigitmonth><%--
        Value: mm - two digit month value --%>
      <monthname><bean:write name="fmtdate" property="monthName"/></monthname><%--
        Value (example): January - full month name --%>
      <day><bean:write name="fmtdate" property="day"/></day><%--
        Value: d - single or two digit day value --%>
      <dayname><bean:write name="fmtdate" property="dayName"/></dayname><%--
        Value (example): Monday - full day name --%>
      <twodigitday><bean:write name="fmtdate" property="twoDigitDay"/></twodigitday><%--
        Value: dd - two digit day value --%>
      <hour24><bean:write name="fmtdate" property="hour24"/></hour24><%--
        Value: h - single to two digit 24 hour value (0-23) --%>
      <twodigithour24><bean:write name="fmtdate" property="twoDigitHour24"/></twodigithour24><%--
        Value: hh - two digit 24 hour value (00-23) --%>
      <hour><bean:write name="fmtdate" property="hour"/></hour><%--
        Value: h - single to two digit hour value (0-12) --%>
      <twodigithour><bean:write name="fmtdate" property="twoDigitHour"/></twodigithour><%--
        Value: hh - two digit hour value (00-12) --%>
      <minute><bean:write name="fmtdate" property="minute"/></minute><%--
        Value: m - single to two digit minute value (0-59) --%>
      <twodigitminute><bean:write name="fmtdate" property="twoDigitMinute"/></twodigitminute><%--
        Value: mm - two digit minute value (00-59) --%>
      <ampm><bean:write name="fmtdate" property="amPm"/></ampm><%--
        Value: am,pm --%>
      <longdate><bean:write name="fmtdate" property="longDateString"/></longdate><%--
        Value (example): February 8, 2004 - long representation of the date --%>
      <shortdate><bean:write name="fmtdate" property="dateString"/></shortdate><%--
        Value (example): 2/8/04 - short representation of the date --%>
      <time><bean:write name="fmtdate" property="timeString"/></time><%--
        Value (example): 10:15 PM - representation of the time --%>
      <timezone>
        <id><bean:write name="date" property="tzid"/></id>
        <islocal><bean:write name="date" property="tzIsLocal"/></islocal>
        <logic:equal name="date" property="tzIsLocal" value="false">
          <bean:define id="tzdate" name="date" property="tzFormatted" />

          <date><bean:write name="tzdate" property="date"/></date>
          <year><bean:write name="tzdate" property="year"/></year>
          <fourdigityear><bean:write name="tzdate" property="fourDigitYear"/></fourdigityear>
          <month><bean:write name="tzdate" property="month"/></month>
          <twodigitmonth><bean:write name="tzdate" property="twoDigitMonth"/></twodigitmonth>
          <monthname><bean:write name="tzdate" property="monthName"/></monthname>
          <day><bean:write name="tzdate" property="day"/></day>
          <dayname><bean:write name="tzdate" property="dayName"/></dayname>
          <twodigitday><bean:write name="tzdate" property="twoDigitDay"/></twodigitday>
          <hour24><bean:write name="tzdate" property="hour24"/></hour24>
          <twodigithour24><bean:write name="tzdate" property="twoDigitHour24"/></twodigithour24>
          <hour><bean:write name="tzdate" property="hour"/></hour>
          <twodigithour><bean:write name="tzdate" property="twoDigitHour"/></twodigithour>
          <minute><bean:write name="tzdate" property="minute"/></minute>
          <twodigitminute><bean:write name="tzdate" property="twoDigitMinute"/></twodigitminute>
          <ampm><bean:write name="tzdate" property="amPm"/></ampm>
          <longdate><bean:write name="tzdate" property="longDateString"/></longdate>
          <shortdate><bean:write name="tzdate" property="dateString"/></shortdate>
          <time><bean:write name="tzdate" property="timeString"/></time>
        </logic:equal>
      </timezone>

