<%@ taglib uri='struts-bean' prefix='bean' %>
<%@ taglib uri='struts-logic' prefix='logic' %>
<%@ taglib uri='struts-html' prefix='html' %>
<%@ taglib uri='struts-genurl' prefix='genurl' %>
<html:xhtml/>

<bedework>
<%@include file="/docs/header.jsp"%>

<page>authUserList</page>
<tab>users</tab>
<%
  String rpitemp;
%>

<authUsers>
  <logic:iterate id="authUser" name="bw_auth_users" scope="session" >
    <authUser>
      <userHref><bean:write name="authUser" property="userHref" /></userHref>
      <publicEventUser><bean:write name="authUser" property="publicEventUser"/></publicEventUser>
      <contentAdminUser><bean:write name="authUser" property="contentAdminUser"/></contentAdminUser>
      <approverUser><bean:write name="authUser" property="approverUser"/></approverUser>
    </authUser>
  </logic:iterate>
</authUsers>

<%@include file="/docs/footer.jsp"%>

</bedework>
