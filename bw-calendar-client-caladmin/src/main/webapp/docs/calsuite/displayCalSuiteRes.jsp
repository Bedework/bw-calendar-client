<%@ taglib uri='struts-bean' prefix='bean' %>
<%@ taglib uri='struts-logic' prefix='logic' %>
<%@ taglib uri='struts-html' prefix='html' %>
<%@ taglib uri='struts-genurl' prefix='genurl' %>
<html:xhtml/>

<bedework>
<%@include file="/docs/header.jsp"%>

<page>calSuiteRes</page>
<tab>system</tab>

<bean:define id="csres" name="calForm" property="calSuiteResource" />

  <calSuiteResource>
    <bw:emitText name="csres" property="name" />
    <bw:emitText name="csres" property="rclass" />
    <bw:emitText name="csres" property="type" />
    <bw:emitText name="csres" property="path" />
    <bw:emitText name="csres" property="contentType" />
    <bw:emitText name="csres" property="content" />
  </calSuiteResource>

<%@include file="/docs/footer.jsp"%>



</bedework>
