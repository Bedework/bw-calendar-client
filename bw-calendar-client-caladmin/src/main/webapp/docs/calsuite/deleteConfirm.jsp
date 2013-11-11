<%@ taglib uri='struts-bean' prefix='bean' %>
<%@ taglib uri='struts-logic' prefix='logic' %>
<%@ taglib uri='struts-html' prefix='html' %>
<%@ taglib uri='struts-genurl' prefix='genurl' %>
<%@ taglib uri='bedework' prefix='bw' %>
<html:xhtml/>

<bedework>
<%@include file="/docs/header.jsp"%>

<page>deleteCalSuiteConfirm</page>
<tab>system</tab>

<bean:define id="calSuite" name="calForm" property="calSuite" >
<calSuite>
  <bw:emitText name="calSuite" property="name" />
  <bw:emitText name="calSuite" property="group.name" tagName"group" />
  <bw:emitText name="calSuite" property="calendar.path" tagName"calPath" />
</calSuite>

<formElements>
  <genurl:form action="calSuite/delete.do" >
    <html:submit property="cancelled" value="Cancel"/>
    <html:submit property="deleteCalSuiteOK" value="Delete"/>
  </genurl:form>
</formElements>

<%@include file="/docs/footer.jsp"%>

</bedework>
