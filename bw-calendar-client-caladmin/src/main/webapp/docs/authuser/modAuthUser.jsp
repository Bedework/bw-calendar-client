<%@ taglib uri='struts-bean' prefix='bean' %>
<%@ taglib uri='struts-logic' prefix='logic' %>
<%@ taglib uri='struts-html' prefix='html' %>
<%@ taglib uri='struts-genurl' prefix='genurl' %>
<html:xhtml/>

<bedework>
<%@include file="/docs/header.jsp"%>

<page>modAuthUser</page>
<tab>users</tab>

<!-- Mod pages contain only formElements for now; we do this to
     take advantage of Struts' form processing features -->
<formElements>
  <genurl:form action="authuser/update" >
    <account><bean:write name="calForm" property="editAuthUser.user.account" /></account>
    <publicEvents><html:checkbox property="editAuthUserPublicEvents" /></publicEvents>
    <email></email><!-- should come from a directory, not internal -->
    <phone></phone><!-- should come from a directory, not internal -->
    <dept></dept><!-- should come from a directory, not internal -->
    <lastname></lastname><!-- should come from a directory, not internal -->
    <firstname></firstname><!-- should come from a directory, not internal -->

    <submitButtons>
      <button type="update">modAuthUser</button>
    </submitButtons>

  </genurl:form>
</formElements>

<%@include file="/docs/footer.jsp"%>

</bedework>
