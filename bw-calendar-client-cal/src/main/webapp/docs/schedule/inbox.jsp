<%@ taglib uri='struts-bean' prefix='bean' %>
<%@ taglib uri='struts-logic' prefix='logic' %>
<%@ taglib uri='struts-html' prefix='html' %>
<%@ taglib uri='struts-genurl' prefix='genurl' %>
<html:xhtml/>

<%@include file="/docs/header.jsp"%>

<page>inbox</page>

<%--  Display inbox --%>
<bean:define id="boxInfo" name="calForm" property="inBoxInfoRefreshed" />
<inbox>
  <%@include file="/docs/schedule/inoutbox.jsp"%>
</inbox>

<%@include file="/docs/footer.jsp"%>

