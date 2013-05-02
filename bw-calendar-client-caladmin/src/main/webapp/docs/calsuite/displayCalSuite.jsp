<%@ taglib uri='struts-bean' prefix='bean' %>
<%@ taglib uri='struts-logic' prefix='logic' %>
<%@ taglib uri='struts-html' prefix='html' %>
<%@ taglib uri='struts-genurl' prefix='genurl' %>
<html:xhtml/>

<%@include file="/docs/header.jsp"%>

<page>displayCalSuite</page>
<tab>system</tab>

<calSuites>
  <logic:iterate id="calSuite" name="calForm" property="calSuite" >
    <%@include file="/docs/calsuite/emitcalSuite.jsp"%>
  </logic:iterate>
</calSuites>

<%@include file="/docs/footer.jsp"%>



