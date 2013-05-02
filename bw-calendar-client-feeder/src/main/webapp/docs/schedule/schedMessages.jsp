<%@ taglib uri='struts-logic' prefix='logic' %>
<%@ taglib uri='bedework' prefix='bw' %>

<events>
  <logic:iterate id="formattedEventForSchedMessages" name="boxInfoForMessages" property="events" >

    <bean:define id="schedEvent" name="formattedEventForSchedMessages" property="event"/>
    <% String rpitemprecurid = ""; %>
    <logic:present name="schedEvent" property="recurrenceId" >
      <bean:define id="recurid" name="schedEvent" property="recurrenceId" />
      <% rpitemprecurid = String.valueOf(recurid); %>
    </logic:present>

    <event>
      <bw:emitText name="schedEvent" property="name" />

      <bw:emitText name="schedEvent" property="scheduleState" />
      <bw:emitText name="schedEvent" property="scheduleMethod" />

      <%@ include file="/docs/schedule/schedEmitEventProperties.jsp" %>

      <logic:present  name="schedEvent" property="requestStatuses">
        <logic:iterate id="requestStatus" name="schedEvent" property="requestStatuses">
          <bw:emitText name="requestStatus" />
        </logic:iterate>
      </logic:present>

      <bw:emitText name="schedEvent" property="summary" />
      <bw:emitText name="schedEvent" property="uid" tagName="guid" />
      <bw:emitText name="schedEvent" property="recurrenceId" tagName="recurrenceId" />

      <logic:present name="formattedEventForSchedMessages" property="start">
        <start><%-- start date and time --%>
          <bean:define id="date" name="formattedEventForSchedMessages"
                     property="start"
                     toScope="request" />
          <%@ include file="/docs/event/emitDate.jsp" %>
        </start>
      </logic:present>
      <logic:present name="formattedEventForSchedMessages" property="end">
        <end><%-- end date and time --%>
          <bean:define id="date" name="formattedEventForSchedMessages"
                       property="end"
                       toScope="request" />
          <%@ include file="/docs/event/emitDate.jsp" %>
        </end>
      </logic:present>
      <%-- last mod date string --%>
      <bw:emitText name="schedEvent" property="lastmod" />
      <dtstamp><%-- date stamp and time --%>
        <bean:define id="date" name="formattedEventForSchedMessages"
                     property="dtstamp"
                     toScope="request" />
        <%@ include file="/docs/event/emitDate.jsp" %>
      </dtstamp>

      <bw:emitContainer name="schedEvent" indent="      " tagName="calendar" />
      <bw:emitText name="schedEvent" property="description" tagName="desc" />
      <status><bean:write name="formattedEventForSchedMessages" property="event.status" /></status>
      <bw:emitText name="schedEvent" property="link" />
      <bw:emitText name="schedEvent" property="cost" />

      <logic:present name="schedEvent" property="location">
        <location><bean:write name="formattedEventForSchedMessages" property="event.location.address.value" /></location>
      </logic:present>
      <logic:notPresent name="schedEvent" property="location">
        <location></location>
      </logic:notPresent>

      <logic:present name="schedEvent" property="contact">
        <contact><bean:write name="formattedEventForSchedMessages" property="event.contact.name.value" /></contact>
      </logic:present>
      <logic:notPresent name="schedEvent" property="contact">
        <contact></contact>
      </logic:notPresent>

      <creator><bean:write name="formattedEventForSchedMessages" property="event.creatorHref" /></creator>
    </event>
  </logic:iterate>
</events>

