<%@ taglib uri='struts-bean' prefix='bean' %>
<%@ taglib uri='struts-logic' prefix='logic' %>
<%@ taglib uri='struts-html' prefix='html' %>
<%@ taglib uri='struts-genurl' prefix='genurl' %>
<html:xhtml/>

<%@include file="/docs/header.jsp"%>

<page>modCalSuite</page>
<tab>system</tab>

<bean:define id="calSuite" name="calForm" property="calSuite"/>
<%@include file="/docs/calsuite/emitCalSuite.jsp"%>

<%@include file="/docs/footer.jsp"%>

