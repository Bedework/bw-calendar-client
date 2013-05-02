<%@ taglib uri='struts-bean' prefix='bean' %>
<%@ taglib uri='struts-logic' prefix='logic' %>
<%@ taglib uri='struts-html' prefix='html' %>
<%@ taglib uri='struts-genurl' prefix='genurl' %>
<html:xhtml/>

<%@include file="/docs/header.jsp"%>

<page>authUserList</page>
<tab>users</tab>
<%
  String rpitemp;
%>

<authUsers>
  <logic:iterate id="authUser" name="calForm" property="authUsers" >
    <authUser>
      <account><bean:write name="authUser" property="user.account" /></account>
      <publicEventUser><bean:write name="authUser" property="publicEventUser"/></publicEventUser>
    </authUser>
  </logic:iterate>
</authUsers>

<%@include file="/docs/footer.jsp"%>

