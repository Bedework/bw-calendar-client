<%@ taglib uri='struts-bean' prefix='bean' %>
<%@ taglib uri='struts-logic' prefix='logic' %>

<%@include file="/docs/header.jsp"%>

<page>deleteViewConfirm</page>

<views>
  <bean:define name="calForm" property="view" id="view"/>
  <view>
    <name><bean:write name="view" property="name" /></name>
    <collections>
      <logic:iterate name="view" property="collections" id="collection">
        <collection>
          <name><bean:write name="collection" property="name" /></name>
          <path><bean:write name="collection" property="path" /></path>
        </collection>
      </logic:iterate>
    </collections>
  </view>
</views>

<%@include file="/docs/footer.jsp"%>
