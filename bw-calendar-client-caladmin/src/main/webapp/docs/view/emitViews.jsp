<%@ taglib uri='struts-bean' prefix='bean' %>
<%@ taglib uri='struts-logic' prefix='logic' %>

<views>
  <logic:present name="bw_views_list" scope="session">
    <logic:iterate id="view" name="bw_views_list"
                   scope="session">
      <view>
        <name><bean:write name="view" property="name" /></name>
        <logic:iterate name="view" property="collectionPaths" id="path">
          <path><bean:write name="path"/></path>
        </logic:iterate>
      </view>
    </logic:iterate>
  </logic:present>
</views>
