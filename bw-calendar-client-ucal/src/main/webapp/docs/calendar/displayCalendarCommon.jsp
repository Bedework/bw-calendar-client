<%@ taglib uri='struts-bean' prefix='bean' %>
<%@ taglib uri='struts-logic' prefix='logic' %>
<%@ taglib uri='bedework' prefix='bw' %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<bean:define id="curcal" name="calForm" property="calendar"/>
<currentCalendar>
  <bw:emitCollection name="curcal" indent="  " full="true" noTag="true" />

  <logic:notEqual name="calForm" property="addingCalendar" value="true">
    <bw:emitCurrentPrivs name="curcal" property="currentAccess" />
    <bw:emitAcl name="curcal" property="currentAccess" />
  </logic:notEqual>
</currentCalendar>
