<%@ taglib uri='struts-html' prefix='html' %>

<html:xhtml/>

<%@include file="/docs/header.jsp"%>
<%
try {
%>
<page>modEventPending</page>
<tab>pending</tab>

<%@include file="/docs/event/eventForm.jsp"%>
<%
} catch (Throwable t) {
  t.printStackTrace();
}
%>
<%@include file="/docs/footer.jsp"%>

