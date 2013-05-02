<%@ taglib uri='struts-bean' prefix='bean' %>
<%@ taglib uri='struts-logic' prefix='logic' %>
<%@ taglib uri='bedework' prefix='bw' %>

<bean:define id="curcal" name="calForm" property="calendar"/>
<currentCalendar>
  <bw:emitText name="curcal" property="name" />
  <bw:emitText name="curcal" property="path" />
  <bw:emitText name="curcal" property="encodedPath" />
  <bw:emitText name="curcal" property="calType" />
  <bw:emitText name="curcal" property="summary" />
  <bw:emitText name="curcal" property="description" tagName="desc" />
  <calendarCollection><bean:write name="curcal" property="calendarCollection" /></calendarCollection>
  <logic:notEqual name="calForm" property="addingCalendar" value="true">
    <bw:emitCurrentPrivs name="curcal" property="currentAccess" />
    <bw:emitAcl name="curcal" property="currentAccess" />
  </logic:notEqual>
</currentCalendar>
