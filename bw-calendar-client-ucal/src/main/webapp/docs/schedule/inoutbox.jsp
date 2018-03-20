<%@ taglib uri='struts-logic' prefix='logic' %>
<%@ taglib uri='bedework' prefix='bw' %>

<events>
  <logic:iterate id="formattedEvent" name="boxInfo" property="events" >

    <bean:define id="event" name="formattedEvent" property="event"/>
    <% String rpitemprecurid = ""; %>
    <logic:present name="event" property="recurrenceId" >
      <bean:define id="recurid" name="event" property="recurrenceId" />
      <% rpitemprecurid = String.valueOf(recurid); %>
    </logic:present>

    <event>
      <bw:emitText name="event" property="name" />
      <bw:emitText name="event" property="entityType" />

      <bw:emitText name="event" property="scheduleState" />
      <bw:emitText name="event" property="scheduleMethod" />

      <%@ include file="/docs/schedule/emitEventProperties.jsp" %>

      <logic:present  name="event" property="requestStatuses">
        <logic:iterate id="requestStatus" name="event" property="requestStatuses">
          <bw:emitText name="requestStatus" />
        </logic:iterate>
      </logic:present>

      <title><bean:write name="event" property="summary" /></title>
      <bw:emitText name="event" property="uid" tagName="guid" />
      <bw:emitText name="event" property="recurrenceId" tagName="recurrenceId" />

      <logic:present name="formattedEvent" property="start">
        <start><%-- start date and time --%>
          <bean:define id="date" name="formattedEvent"
                     property="start"
                     toScope="request" />
          <%@ include file="/docs/event/emitDate.jsp" %>
        </start>
      </logic:present>
      <logic:present name="formattedEvent" property="end">
        <end><%-- end date and time --%>
          <bean:define id="date" name="formattedEvent"
                       property="end"
                       toScope="request" />
          <%@ include file="/docs/event/emitDate.jsp" %>
        </end>
      </logic:present>
      <%-- last mod date string --%>
      <bw:emitText name="event" property="lastmod" />
      <dtstamp><%-- date stamp and time --%>
        <bean:define id="date" name="formattedEvent"
                     property="dtstamp"
                     toScope="request" />
        <%@ include file="/docs/event/emitDate.jsp" %>
      </dtstamp>

      <bw:emitContainer name="event" indent="      " tagName="calendar" />
      <bw:emitText name="event" property="description" tagName="desc" />
      <status><bean:write name="formattedEvent" property="event.status" /></status>
      <bw:emitText name="event" property="link" />
      <bw:emitText name="event" property="cost" />

      <logic:present name="event" property="location">
        <location><bean:write name="formattedEvent" property="event.location.address.value" /></location>
      </logic:present>
      <logic:notPresent name="event" property="location">
        <location></location>
      </logic:notPresent>

      <logic:present name="event" property="contact">
        <contact><bean:write name="formattedEvent" property="event.contact.cn.value" /></contact>
      </logic:present>
      <logic:notPresent name="event" property="contact">
        <contact></contact>
      </logic:notPresent>

      <creator><bean:write name="formattedEvent" property="event.creatorHref" /></creator>
    </event>
  </logic:iterate>
</events>

