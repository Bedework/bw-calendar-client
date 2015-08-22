<%@ taglib uri='struts-bean' prefix='bean' %>
<%@ taglib uri='struts-logic' prefix='logic' %>
<%@ taglib uri='struts-html' prefix='html' %>
<%@ taglib uri='struts-genurl' prefix='genurl' %>

<bedework>
<%@include file="header.jsp"%>
<%
try {
%>
<page>tabSuggestionQueueEvents</page>
<tab>suggestionQueue</tab>

<%@include file="/docs/event/eventListRoot.jsp"%>
<%
} catch (final Throwable t) {
  t.printStackTrace();
}
%>
<%@include file="footer.jsp"%>
</bedework>
