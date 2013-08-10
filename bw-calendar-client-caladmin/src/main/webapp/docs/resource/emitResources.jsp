<%@ taglib uri='struts-bean' prefix='bean' %>
<%@ taglib uri='struts-logic' prefix='logic' %>

<resources>
  <logic:iterate name="calForm" property="calSuiteResources" id="resource">
    <resource>
      <name><bean:write name="resource" property="name" /></name>
      <content-type><bean:write name="resource" property="contentType" /></content-type>
      <class><bean:write name="resource" property="rclass" /></class>
      <type><bean:write name="resource" property="type" /></type>
      <path><bean:write name="resource" property="path" /></path>
    </resource>
  </logic:iterate>
</resources>
