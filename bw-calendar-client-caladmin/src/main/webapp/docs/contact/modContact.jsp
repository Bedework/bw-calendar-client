<%@ taglib uri='struts-bean' prefix='bean' %>
<%@ taglib uri='struts-logic' prefix='logic' %>
<%@ taglib uri='struts-html' prefix='html' %>
<%@ taglib uri='struts-genurl' prefix='genurl' %>
<html:xhtml/>

<%@include file="/docs/header.jsp"%>

<page>modContact</page>
<tab>main</tab>
<creating><bean:write name="calForm" property="addingContact"/></creating>

<!-- Mod pages contain only formElements for now; we do this to
     take advantage of Struts' form processing features -->
<formElements>
  <genurl:form action="contact/update" >

    <%@include file="/docs/contact/modContactCommon.jsp"%>

    <!-- these are the values that may be submitted to the update action -->
    <submitButtons>
      <button type="add">addContact</button>
      <button type="update">updateContact</button>
      <button type="cancel">forwardto</button>
      <button type="delete">delete</button>
    </submitButtons>

  </genurl:form>
</formElements>

<%@include file="/docs/footer.jsp"%>

