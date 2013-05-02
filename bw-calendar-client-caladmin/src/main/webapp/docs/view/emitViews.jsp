<%@ taglib uri='struts-bean' prefix='bean' %>
<%@ taglib uri='struts-logic' prefix='logic' %>

<views>
  <logic:iterate name="calForm" property="views" id="view">
    <view>
      <name><bean:write name="view" property="name" /></name>
      <logic:iterate name="view" property="collectionPaths" id="path">
        <path><bean:write name="path"/></path>
      </logic:iterate>
    </view>
  </logic:iterate>
</views>
