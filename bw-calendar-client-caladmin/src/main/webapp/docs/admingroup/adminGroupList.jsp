<%@ taglib uri='struts-bean' prefix='bean' %>
<%@ taglib uri='struts-logic' prefix='logic' %>

<bedework>
<%@include file="/docs/header.jsp"%>

<page>adminGroupList</page>
<tab>users</tab>

<groups>
  <showMembers><bean:write name="calForm" property="showAgMembers"/></showMembers>
  <logic:iterate id="adminGroup" name="bw_admin_groups" scope="session" >
    <group>
      <name><bean:write name="adminGroup" property="account" /></name>
      <desc><bean:write name="adminGroup" property="description" /></desc>
      <members>
        <logic:equal name="calForm" property="showAgMembers" value="true">
          <logic:present name="adminGroup" property="groupMembers" >
            <logic:iterate name="adminGroup" property="groupMembers"
                           id="member" >
              <member>
                <account><bean:write name="member" property="account" /></account>
                <kind><bean:write name="member" property="kind" /></kind>
              </member>
            </logic:iterate>
          </logic:present>
        </logic:equal>
      </members>
    </group>
  </logic:iterate>
</groups>

<calSuites>
  <logic:iterate id="calSuite" name="calForm" property="calSuites" >
    <%@include file="/docs/calsuite/emitCalSuite.jsp"%>
  </logic:iterate>
</calSuites>

<%@include file="/docs/footer.jsp"%>
</bedework>
