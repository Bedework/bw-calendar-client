<%@ taglib uri='struts-bean' prefix='bean' %>
<%@ taglib uri='struts-logic' prefix='logic' %>
<%@ taglib uri='struts-html' prefix='html' %>
<%@ taglib uri='struts-genurl' prefix='genurl' %>
<html:xhtml/>

<bedework>
<%@include file="/docs/header.jsp"%>

<page>deleteLocationConfirm</page>
<tab>main</tab>

<location>
  <address>
    <bean:write name="calForm" property="location.address.value" />
  </address>
  <subaddress>
    <logic:present name="calForm" property="location.subaddress">
      <bean:write name="calForm" property="location.subaddress.value" />
    </logic:present>
  </subaddress>
  <logic:present name="calForm" property="location.link">
    <link><bean:write name="calForm" property="location.link" /></link>
  </logic:present>
</location>

<formElements>
  <genurl:form action="location/delete.do" >
    <html:submit property="cancelled" value="Cancel"/>
    <html:submit property="deleteLocationOK" value="Delete"/>
  </genurl:form>
</formElements>

<%@include file="/docs/footer.jsp"%>

</bedework>
