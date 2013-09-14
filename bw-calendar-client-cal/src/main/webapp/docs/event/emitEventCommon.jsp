<%@ taglib uri='struts-bean' prefix='bean' %>
<%@ taglib uri='struts-logic' prefix='logic' %>
<%@ taglib uri='bedework' prefix='bw' %>

<%-- Output a single event. This page handles fields common to all views --%>
  <event>
    <entityType><bean:write name="event" property="entityType"/></entityType>
    <scheduleMethod><bean:write name="event" property="scheduleMethod"/></scheduleMethod>
    <name><bean:write name="event" property="name"/></name>
    <created><bean:write name="event" property="created"/></created>
    <start><%-- start date and time --%>
      <noStart><bean:write name="event" property="noStart"/></noStart>
      <bean:define id="date" name="eventFmt"
                   property="start"
                   toScope="request" />
      <%@ include file="/docs/event/emitDate.jsp" %>
    </start>
    <end><%-- end date and time --%>
      <type><bean:write name="event" property="endType"/></type>
      <bean:define id="date" name="eventFmt"
                   property="end"
                   toScope="request" />
      <%@ include file="/docs/event/emitDate.jsp" %>
    </end>
    <bw:emitText name="event" property="creatorHref" tagName="creator"/>
    <bw:emitText name="event" property="ownerHref" tagName="owner"/>

    <id><bean:write name="event" property="id"/></id><%--
      Value: integer - event id --%>
    <bw:emitText name="event" property="uid" tagName="guid" />
    <bw:emitText name="event" property="recurrenceId" tagName="recurrenceId" />
    <bw:emitText name="event" property="summary" /><%--
      Value: string - short description, typically used for the event title  --%>
    <bw:emitText name="event" property="color"/>
    <bw:emitText name="event" property="link"/><%--
      Value: URI - link associated with the event --%>
    <deleted><bean:write name="event" property="deleted"/></deleted>
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

    <!-- always produce the xproperties, if they exist -->
    <logic:present name="event" property="xproperties">
      <xproperties>
        <logic:iterate id="xprop" name="event" property="xproperties">
          <logic:equal name="xprop" property="skipJsp" value="false">
            <bean:define id="xpropName" name="xprop" property="name"/>
            <% String xpropStart = "<" + (String)xpropName + ">";
               String xpropEnd = "</" + (String)xpropName + ">";%>
            <%=xpropStart%>
              <logic:present name="xprop" property="parameters">
                <parameters>
                <logic:iterate id="xpar" name="xprop" property="parameters">
                  <bean:define id="xparName" name="xpar" property="name"/>
                  <% String xparStart = "<" + (String)xparName + ">";
                     String xparEnd = "</" + (String)xparName + ">";%>
                  <%=xparStart%><![CDATA[<bean:write name="xpar" property="value" filter="false"/>]]><%=xparEnd%>
                </logic:iterate>
                </parameters>
              </logic:present>
              <values>
                <text><![CDATA[<bean:write name="xprop" property="value" filter="false"/>]]></text>
              </values>
            <%=xpropEnd%>
          </logic:equal>
        </logic:iterate>
      </xproperties>
    </logic:present>

    <logic:present  name="event" property="percentComplete">
      <bw:emitText name="event" property="percentComplete"/>
    </logic:present>

    <logic:present  name="event" property="geo">
      <bw:emitText name="event" property="geo.latitude" tagName="latitude"/>
      <bw:emitText name="event" property="geo.longitude" tagName="longitude"/>
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
