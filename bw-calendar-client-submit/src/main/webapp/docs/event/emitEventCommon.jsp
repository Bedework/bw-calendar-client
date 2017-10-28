<%@ taglib uri='struts-bean' prefix='bean' %>
<%@ taglib uri='struts-logic' prefix='logic' %>
<%@ taglib uri='bedework' prefix='bw' %>

<%-- Output a single event. This page handles fields common to all views --%>
  <event>
    <entityType><bean:write name="event" property="entityType"/></entityType>
    <scheduleMethod><bean:write name="event" property="scheduleMethod"/></scheduleMethod>
    <start><%-- start date and time --%>
      <bean:define id="date" name="eventFmt"
                   property="start"
                   toScope="request" />
      <%@ include file="/docs/event/emitDate.jsp" %>
    </start>
    <end><%-- end date and time --%>
      <bean:define id="date" name="eventFmt"
                   property="end"
                   toScope="request" />
      <%@ include file="/docs/event/emitDate.jsp" %>
    </end>
    <bw:emitText name="event" property="creatorHref" tagName="creator"/>
    <bw:emitText name="event" property="ownerHref" tagName="owner"/>
    <bw:emitText name="event" property="calSuite"/>

    <id><bean:write name="event" property="id"/></id><%--
      Value: integer - event id --%>
    <bw:emitText name="event" property="uid" tagName="guid" />
    <bw:emitText name="event" property="encodedHref" />
    <bw:emitText name="event" property="recurrenceId" tagName="recurrenceId" />
    <bw:emitText name="event" property="summary" /><%--
      Value: string - short description, typically used for the event title  --%>
    <bw:emitText name="event" property="link"/><%--
      Value: URI - link associated with the event --%>
    <public><bean:write name="event" property="publick"/></public>
    <editable><bean:write name="eventInfo" property="editable"/></editable><%--
      Value: true,false - true if user can edit (and delete) event, false otherwise --%>
    <logic:present  name="event" property="target">
      <isAnnotation/>
    </logic:present>
    <kind><bean:write name="eventInfo" property="kind"/></kind><%--
      Value: 0 - actual event entry
             1 - 'added event' from a reference
             2 - from a subscription --%>
    <recurring><bean:write name="event" property="recurring"/></recurring><%--
      Value: true,false - true if the event is recurring --%>
    <bw:emitContainer name="event" indent="    " tagName="calendar" />
    <bw:emitText name="event" property="status" /><%-- Status
          Value: string, only one of CONFIRMED, TENTATIVE, or CANCELLED --%>

    <logic:present  name="event" property="percentComplete">
      <bw:emitText name="event" property="percentComplete"/>
    </logic:present>

    <logic:notPresent name="detailView" scope="request"><%-- look for short form --%>
      <logic:notPresent name="allView" scope="request">
        <jsp:include page="/docs/event/emitEventShort.jsp"/>
      </logic:notPresent>
    </logic:notPresent>

    <logic:present name="detailView" scope="request">
      <jsp:include page="/docs/event/emitEventDetail.jsp"/>
    </logic:present>

    <logic:present name="allView" scope="request">
      <jsp:include page="/docs/event/emitEventDetail.jsp"/>
      <jsp:include page="/docs/event/emitEventAll.jsp"/>
    </logic:present>


    <%-- ****************************************************************
          the following code should not be produced in the public client
         **************************************************************** --%>
    <logic:equal name="calForm" property="guest" value="false">
      <bw:emitCurrentPrivs name="eventInfo" property="currentAccess" />
    </logic:equal>
  </event>
