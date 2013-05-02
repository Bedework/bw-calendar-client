<%@ taglib uri='struts-bean' prefix='bean' %>
<%@ taglib uri='struts-logic' prefix='logic' %>
<%@ taglib uri='struts-html' prefix='html' %>
<%@ taglib uri='struts-genurl' prefix='genurl' %>
<html:xhtml/>

<%@include file="/docs/header.jsp"%>

<page>modAdminGroup</page>
<tab>users</tab>
<creating><bean:write name="calForm" property="addingAdmingroup"/></creating>

<formElements>
  <genurl:form action="admingroup/update" >
    <name>
      <logic:equal name="calForm" property="addingAdmingroup" value="true" >
        <html:text name="calForm" property="updAdminGroup.account" />
      </logic:equal>
      <logic:notEqual name="calForm" property="addingAdmingroup" value="true" >
        <bean:write name="calForm" property="updAdminGroup.account" />
      </logic:notEqual>
    </name>
    <desc>
      <html:textarea property="updAdminGroup.description" cols="50"  rows="3"></html:textarea>
    </desc>
    <groupOwner>
      <html:text name="calForm" property="adminGroupGroupOwner" />
    </groupOwner>
    <eventsOwner>
      <html:text name="calForm" property="adminGroupEventOwner" />
    </eventsOwner>

    <!-- these are the values that may be submitted to the update action -->
    <submitButtons>
      <button type="add">addAdminGroup</button>
      <button type="update">updateAdminGroup</button>
      <button type="cancel">cancelled</button>
      <button type="delete">delete</button>
    </submitButtons>

  </genurl:form>
</formElements>

<%@include file="/docs/footer.jsp"%>

