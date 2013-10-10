<%@ taglib uri='struts-bean' prefix='bean' %>
<%@ taglib uri='struts-logic' prefix='logic' %>

<%@include file="/docs/header.jsp"%>

<page>chooseGroup</page>
<tab>none</tab>

<%
  String rpitemp;
%>

<groups>
  <logic:iterate id="adminGroup" name="bw_admin_groups" scope="session" >
    <group>
      <name><bean:write name="adminGroup" property="account" /></name>
      <desc><bean:write name="adminGroup" property="description" /></desc>
    </group>
  </logic:iterate>
</groups>

<calSuites>
  <logic:iterate id="calSuite" name="calForm" property="calSuites" >
    <%@include file="/docs/calsuite/emitCalSuite.jsp"%>
  </logic:iterate>
</calSuites>

<%@include file="/docs/footer.jsp"%>
