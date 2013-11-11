<%@ taglib uri='struts-bean' prefix='bean' %>
<%@ taglib uri='struts-logic' prefix='logic' %>
<%@ taglib uri='struts-html' prefix='html' %>
<%@ taglib uri='struts-genurl' prefix='genurl' %>
<html:xhtml/>

<bedework>
<%@include file="/docs/header.jsp"%>

<page>locationList</page>
<tab>main</tab>

<% /* used by included file */
   String rpitemp; %>
<locations>
  <logic:iterate id="location" name="calForm" property="editableLocations" >
    <%@include file="/docs/location/emitLocation.jsp"%>
  </logic:iterate>
</locations>

<%@include file="/docs/footer.jsp"%>

</bedework>
