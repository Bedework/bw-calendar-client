<%@ taglib uri='struts-bean' prefix='bean' %>
<%@ taglib uri='struts-logic' prefix='logic' %>
<%@ taglib uri='struts-html' prefix='html' %>
<%@ taglib uri='struts-genurl' prefix='genurl' %>
<html:xhtml/>

<bedework>
<%@include file="/docs/header.jsp"%>

<page>deleteContactConfirm</page>
<tab>main</tab>

<contact>
  <name>
    <bean:write name="calForm" property="contact.cn.value" />
  </name>
  <phone><bean:write name="calForm" property="contact.phone" /></phone>
  <logic:present name="calForm" property="contact.email">
    <email><bean:write name="calForm" property="contact.email"/></email>
  </logic:present>
  <logic:present name="calForm" property="contact.link">
    <link><bean:write name="calForm" property="contact.link" /></link>
  </logic:present>
</contact>

<formElements>
  <genurl:form action="contact/delete.do" >
    <html:submit property="cancelled" value="Cancel"/>
    <html:submit property="deleteContactOK" value="Delete"/>
  </genurl:form>
</formElements>

<%@include file="/docs/footer.jsp"%>

</bedework>
