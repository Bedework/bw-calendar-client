<%@ taglib uri='struts-bean' prefix='bean' %>
<%@ taglib uri='struts-logic' prefix='logic' %>
<%@ taglib uri='struts-html' prefix='html' %>
<%@ taglib uri='struts-genurl' prefix='genurl' %>
<html:xhtml/>

<bedework>
<%@include file="/docs/header.jsp"%>

<page>modLocation</page>
<tab>main</tab>

<creating><bean:write name="calForm" property="addingLocation"/></creating>

<!-- Mod pages contain only formElements for now; we do this to
     take advantage of Struts' form processing features -->
<formElements>
  <genurl:form action="location/update" >
      <!--
    <address><html:text property="locationAddress.value" size="30" /></address> -->
    <addressField><html:text property="location.addressField" size="30" /></addressField>
    <roomField><html:text property="location.roomField" size="30" /></roomField>
    <subField1><html:text property="location.subField1" size="30" /></subField1>
    <subField2><html:text property="location.subField2" size="30" /></subField2>
    <geouri><html:text property="location.geouri" size="30" /></geouri>
    <subaddress><html:text property="location.subaddressField" size="30" /></subaddress>
    <street><html:text property="location.street" size="30" /></street>
    <city><html:text property="location.city" size="30" /></city>
    <state><html:text property="location.state" size="30" /></state>
    <zip><html:text property="location.zip" size="30" /></zip>
    <link><html:text property="location.link" size="30" /></link>
    <accessible><html:checkbox property="location.accessible" /></accessible>
    <code><html:text property="location.code" size="30" /></code>
    <alternateAddress><html:text property="location.alternateAddress" size="30" /></alternateAddress>
    <status><html:text property="locationStatus" size="30" /></status>

    <!-- these are the values that may be submitted to the update action -->
    <submitButtons>
      <button type="add">addLocation</button>
      <button type="update">updateLocation</button>
      <button type="cancel">forwardto</button>
      <button type="delete">delete</button>
    </submitButtons>

  </genurl:form>
</formElements>

<%@include file="/docs/footer.jsp"%>

</bedework>
