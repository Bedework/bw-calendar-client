<bean:define id="uid" name="contact" property="uid"/>
<% rpitemp="/contact/fetchForUpdate.do?uid=" + uid; %>
<contact>
  <name>
    <genurl:link page="<%=rpitemp%>">
      <bean:write name="contact" property="name.value" />
    </genurl:link>
  </name>
  <phone><bean:write name="contact" property="phone" /></phone>
  <logic:present name="contact" property="email">
    <email><bean:write name="contact" property="email"/></email>
  </logic:present>
  <logic:present name="contact" property="link">
    <link><bean:write name="contact" property="link" /></link>
  </logic:present>
</contact>
