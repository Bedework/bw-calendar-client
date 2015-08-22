<%@ taglib uri='struts-logic' prefix='logic' %>
<%@ taglib uri='struts-bean' prefix='bean' %>
<%@ taglib uri='bedework' prefix='bw' %>

<notifications>
    <logic:present name="calForm" property="notificationInfo" >
        <bean:define id="notificationInfo" name="calForm" property="notificationInfo" />
        <%@include file="/docs/notifications/notificationInfo.jsp"%>
    </logic:present>
</notifications>

