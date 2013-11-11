<%@ taglib uri='struts-bean' prefix='bean' %>
<%@ taglib uri='struts-logic' prefix='logic' %>
<%@ taglib uri='struts-html' prefix='html' %>
<%@ taglib uri='struts-genurl' prefix='genurl' %>
<%@ taglib uri='bedework' prefix='bw' %>
<html:xhtml/>

<bedework>
<%@ include file="/docs/header.jsp" %>

<page>editEvent</page>

<creating>false</creating>

<%@ include file="/docs/event/eventForm.jsp" %>

<%@ include file="/docs/footer.jsp" %>

</bedework>
