<%@ taglib uri='struts-bean' prefix='bean' %>
<%@ taglib uri='struts-logic' prefix='logic' %>
<%@ taglib uri='struts-html' prefix='html' %>
<html:xhtml/>

<bean:define id="uid" name="location" property="uid"/>
<% rpitemp="/location/fetchForUpdate.do?uid=" + uid; %>
<location>
  <address>
    <genurl:link page="<%=rpitemp%>">
      <bean:write name="location" property="address.value" />
    </genurl:link>
  </address>
  <subaddress>
    <logic:present name="location" property="subaddress" >
      <bean:write name="location" property="subaddress.value" />
    </logic:present>
  </subaddress>
  <logic:present name="location" property="link">
    <link><bean:write name="location" property="link" /></link>
  </logic:present>
</location>

