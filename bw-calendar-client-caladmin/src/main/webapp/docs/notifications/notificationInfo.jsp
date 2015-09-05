<%@ taglib uri='struts-logic' prefix='logic' %>
<%@ taglib uri='struts-bean' prefix='bean' %>
<%@ taglib uri='bedework' prefix='bw' %>

  <bw:emitText name="notificationInfo" property="changed" />
  <logic:iterate id="notification" name="notificationInfo" property="notifications" >
    <notification>
      <bw:emitText name="notification" property="name" />
      <bw:emitText name="notification" property="type" />
      <bw:emitText name="notification" property="xmlFragment"
                   tagName="message" filter="false" />
      <logic:present name="notification" property="resourcesInfo" >
        <logic:iterate id="resourceInfo"
                       name="notification" property="resourcesInfo" >
          <resource>
            <bw:emitText name="resourceInfo" property="href" />
            <bw:emitText name="resourceInfo" property="created" />
            <bw:emitText name="resourceInfo" property="deleted" />
            <logic:present name="resourceInfo" property="summary" >
              <bw:emitText name="resourceInfo" property="summary" />
            </logic:present>
            <logic:present name="resourceInfo" property="formattedStart" >
              <bean:define id="fdt"
                           name="resourceInfo" property="formattedStart.formatted" />
              <bw:emitText name="fdt" property="dayName" />
              <bw:emitText name="resourceInfo" property="formattedStart.dateType"
                           tagName="allday" /><%--
        Value: true/false --%>
              <%-- Whatever else is needed --%>
            </logic:present>
          </resource>
        </logic:iterate>
      </logic:present>
    </notification>
  </logic:iterate>

