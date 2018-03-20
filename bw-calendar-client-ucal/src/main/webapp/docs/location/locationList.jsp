<%@ page contentType="text/xml;charset=UTF-8" buffer="none" language="java" %><?xml version="1.0" encoding="UTF-8"?>
<%@ taglib uri='struts-bean' prefix='bean' %>
<%@ taglib uri='struts-logic' prefix='logic' %>
<%@ taglib uri='struts-html' prefix='html' %>
<%@ taglib uri='struts-genurl' prefix='genurl' %>
<%@ taglib uri='bedework' prefix='bw' %>
<html:xhtml/>

<bedework>
<%@ include file="/docs/header.jsp" %>

<%
try {
%>

<page>locationList</page>

<locations>
  <logic:iterate id="location" name="bw_editable_locations_list"
                 scope="session" >
    <location>
      <bw:emitText name="location" property="addressField" tagName="address" />
      <bw:emitText name="location" property="street" tagName="subaddress"/>
      <bw:emitText name="location" property="link" />
      <bw:emitText name="location" property="uid" />
    </location>
  </logic:iterate>
</locations>

<%
} catch (Throwable t) {
  t.printStackTrace();
}
%>

<%@ include file="/docs/footer.jsp" %>

</bedework>
