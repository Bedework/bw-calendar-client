<%@ taglib uri='struts-bean' prefix='bean' %>
<%@ taglib uri='struts-logic' prefix='logic' %>
<%@ taglib uri='bedework' prefix='bw' %>

    <%@ include file="/docs/schedule/emitEventProperties.jsp" %>
    <bw:emitText name="eventFormatter" property="xmlAccess" tagName="access"
                 filter="no"/>

