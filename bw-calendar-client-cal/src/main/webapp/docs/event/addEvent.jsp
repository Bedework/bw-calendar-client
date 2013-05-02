<%@ taglib uri='struts-bean' prefix='bean' %>
<%@ taglib uri='struts-logic' prefix='logic' %>
<%@ taglib uri='struts-html' prefix='html' %>
<%@ taglib uri='struts-genurl' prefix='genurl' %>
<%@ taglib uri='bedework' prefix='bw' %>
<html:xhtml/>

<%@ include file="/docs/header.jsp" %>

<page>addEvent</page>

<bw:emitText name="calForm" property="addingEvent" tagName="creating"/>

<%@ include file="/docs/event/eventForm.jsp" %>

<%@ include file="/docs/footer.jsp" %>

