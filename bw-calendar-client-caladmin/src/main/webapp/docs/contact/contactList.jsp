<%@ taglib uri='struts-bean' prefix='bean' %>
<%@ taglib uri='struts-logic' prefix='logic' %>
<%@ taglib uri='struts-html' prefix='html' %>
<%@ taglib uri='struts-genurl' prefix='genurl' %>
<html:xhtml/>

<bedework>
<%@include file="/docs/header.jsp"%>

<page>contactList</page>
<tab>main</tab>

<% /* used by included file */
   String rpitemp; %>
<contacts>
  <logic:present name="bw_editable_contacts_list" scope="session">
    <logic:iterate id="contact" name="bw_editable_contacts_list"
                   scope="session">
      <%@include file="/docs/contact/emitContact.jsp"%>
    </logic:iterate>
  </logic:present>
</contacts>

<%@include file="/docs/footer.jsp"%>

</bedework>
