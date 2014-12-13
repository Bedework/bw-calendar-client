<%@ taglib uri='struts-bean' prefix='bean' %>
<%@ taglib uri='struts-logic' prefix='logic' %>
<%@ taglib uri='struts-html' prefix='html' %>
<%@ taglib uri='struts-genurl' prefix='genurl' %>
<html:xhtml/>

<bedework>
<%@include file="/docs/header.jsp"%>

<page>modAuthUser</page>
<tab>users</tab>

<formElements>
  <genurl:form action="authuser/update" >
    <userHref><bean:write name="calForm" property="editAuthUser.userHref" /></userHref>
    <publicEvents><html:checkbox property="editAuthUserPublicEvents" /></publicEvents>
    <contentAdmin><html:checkbox property="editAuthUserContentAdmin" /></contentAdmin>
    <approver><html:checkbox property="editAuthUserApprover" /></approver>

    <submitButtons>
      <button type="update">modAuthUser</button>
    </submitButtons>

  </genurl:form>
</formElements>

<%@include file="/docs/footer.jsp"%>

</bedework>
