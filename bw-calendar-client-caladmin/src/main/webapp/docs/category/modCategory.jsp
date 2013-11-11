<%@ taglib uri='struts-bean' prefix='bean' %>
<%@ taglib uri='struts-logic' prefix='logic' %>
<%@ taglib uri='struts-html' prefix='html' %>
<%@ taglib uri='struts-genurl' prefix='genurl' %>
<html:xhtml/>

<bedework>
<%@include file="/docs/header.jsp"%>

<page>modCategory</page>
<tab>system</tab>

<creating><bean:write name="calForm" property="addingCategory"/></creating>

<currentCategory>
  <logic:present name="calForm" property="category">
    <bean:define id="category" name="calForm" property="category"/>
    <%@include file="/docs/category/emitCategory.jsp"%>
  </logic:present>
</currentCategory>

<%@include file="/docs/footer.jsp"%>

</bedework>
