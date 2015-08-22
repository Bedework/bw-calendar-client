<%@ taglib uri='struts-html' prefix='html' %>

<html:xhtml/>

<bedework>
<%@include file="/docs/header.jsp"%>
<%
try {
%>
<page>modEventApprovalQueue</page>
<tab>approvalQueue</tab>

<%@include file="/docs/event/eventForm.jsp"%>
<%
} catch (Throwable t) {
  t.printStackTrace();
}
%>
<%@include file="/docs/footer.jsp"%>

</bedework>
