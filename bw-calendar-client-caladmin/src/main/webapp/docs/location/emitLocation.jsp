<%@ taglib uri='struts-bean' prefix='bean' %>
<%@ taglib uri='struts-logic' prefix='logic' %>
<%@ taglib uri='struts-html' prefix='html' %>
<%@ taglib uri='bedework' prefix='bw' %>
<html:xhtml/>

<bean:define id="uid" name="location" property="uid"/>
<% rpitemp="/location/fetchForUpdate.do?uid=" + uid; %>
<location>
  <address>
    <genurl:link page="<%=rpitemp%>">
      <bean:write name="location" property="addressField" />
    </genurl:link>
  </address>

  <subaddress>
    <logic:present name="location" property="subaddress" >
      <bean:write name="location" property="subaddress.value" />
    </logic:present>
  </subaddress>

  <bw:emitText name="location" property="street" />
  <bw:emitText name="location" property="city" />
  <bw:emitText name="location" property="state" />
  <bw:emitText name="location" property="zip" />
  <bw:emitText name="location" property="link" />
</location>

