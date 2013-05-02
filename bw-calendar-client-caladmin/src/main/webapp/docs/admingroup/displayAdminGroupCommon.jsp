<%@ taglib uri='struts-bean' prefix='bean' %>
<%@ taglib uri='struts-logic' prefix='logic' %>

<group>
  <name><bean:write name="calForm" property="updAdminGroup.account" /></name>
  <desc><bean:write name="calForm" property="updAdminGroup.description" /></desc>
  <groupOwner><bean:write name="calForm" property="adminGroupGroupOwner" /></groupOwner>
  <eventsOwner><bean:write name="calForm" property="adminGroupEventOwner" /></eventsOwner>
  <members>
    <logic:present name="calForm" property="updAdminGroup.groupMembers" >
      <logic:iterate name="calForm" property="updAdminGroup.groupMembers"
                     id="member" >
        <member>
          <account><bean:write name="member" property="account" /></account>
          <kind><bean:write name="member" property="kind" /></kind>
        </member>
      </logic:iterate>
    </logic:present>
  </members>
</group>
