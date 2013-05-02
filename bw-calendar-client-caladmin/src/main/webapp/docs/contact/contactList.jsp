<%@ taglib uri='struts-bean' prefix='bean' %>
<%@ taglib uri='struts-logic' prefix='logic' %>
<%@ taglib uri='struts-html' prefix='html' %>
<%@ taglib uri='struts-genurl' prefix='genurl' %>
<html:xhtml/>

<%@include file="/docs/header.jsp"%>

<page>contactList</page>
<tab>main</tab>

<% /* used by included file */
   String rpitemp; %>
<contacts>
  <logic:iterate id="contact" name="calForm" property="editableContacts" >
    <%@include file="/docs/contact/emitContact.jsp"%>
  </logic:iterate>
</contacts>

<%@include file="/docs/footer.jsp"%>

