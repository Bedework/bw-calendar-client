<%@ taglib uri='struts-bean' prefix='bean' %>
<%@ taglib uri='struts-logic' prefix='logic' %>
<%@ taglib uri='bedework' prefix='bw' %>

<logic:present  name="calForm" property="rruleComponents">
  <logic:iterate  id="rrc" name="calForm" property="rruleComponents">
    <recurrence>
      <bw:emitText name="rrc" property="rule"/>

      <bw:emitText name="rrc" property="freq"/>
      <logic:present  name="rrc" property="until">
        <bw:emitText name="rrc" property="until"/>
      </logic:present>
      <bw:emitText name="rrc" property="count"/>
      <bw:emitText name="rrc" property="interval"/>
      <%-- bySecond --%>
      <%-- byMinue --%>
      <%-- byHour --%>
      <logic:present  name="rrc" property="byDay">
        <byday>
          <logic:iterate  id="posdays" name="rrc" property="byDay">
            <bean:define id="pos" name="posdays" property="pos" />
            <pos val="<%=String.valueOf(pos)%>">
            <logic:iterate  id="day" name="posdays" property="days">
              <bw:emitText name="day" />
            </logic:iterate>
            </pos>
          </logic:iterate>
        </byday>
      </logic:present>

      <logic:present  name="rrc" property="byMonthDay">
        <bymonthday>
          <logic:iterate  id="val" name="rrc" property="byMonthDay">
            <bw:emitText name="val" />
          </logic:iterate>
        </bymonthday>
      </logic:present>

      <logic:present  name="rrc" property="byYearDay">
        <byyearday>
          <logic:iterate  id="val" name="rrc" property="byYearDay">
            <bw:emitText name="val" />
          </logic:iterate>
        </byyearday>
      </logic:present>

      <logic:present  name="rrc" property="byWeekNo">
        <byweekno>
          <logic:iterate  id="val" name="rrc" property="byWeekNo">
            <bw:emitText name="val" />
          </logic:iterate>
        </byweekno>
      </logic:present>

      <logic:present  name="rrc" property="byMonth">
        <bymonth>
          <logic:iterate  id="val" name="rrc" property="byMonth">
            <bw:emitText name="val" />
          </logic:iterate>
        </bymonth>
      </logic:present>

      <logic:present  name="rrc" property="bySetPos">
        <bysetpos>
          <logic:iterate  id="val" name="rrc" property="bySetPos">
            <bw:emitText name="val" />
          </logic:iterate>
        </bysetpos>
      </logic:present>

      <logic:present  name="rrc" property="wkst">
        <bw:emitText name="rrc" property="wkst"/>
      </logic:present>
    </recurrence>
  </logic:iterate>

  <logic:present name="event" property="rdates">
    <bw:emitRexdates name="event" property="rdates" indent="    " />
  </logic:present>

  <logic:present name="event" property="exdates">
    <bw:emitRexdates name="event" property="exdates" indent="    " />
  </logic:present>
</logic:present>

