<%@ taglib uri='struts-bean' prefix='bean' %>
<%@ taglib uri='struts-logic' prefix='logic' %>
<%@ taglib uri='struts-html' prefix='html' %>
<%@ taglib uri='struts-genurl' prefix='genurl' %>
<html:xhtml/>

<%@ include file="/docs/header.jsp" %>

<%
try {
%>

<page>emailOptions</page>

<emailoptionsform>
  <genurl:form action="mail/mailEvent">
    <email><html:text name="calForm" property="lastEmail"/></email>
    <subject><html:text name="calForm" property="lastSubject" /></subject>
  </genurl:form>
</emailoptionsform>

<%
} catch (Throwable t) {
  t.printStackTrace();
}
%>

<%@ include file="/docs/footer.jsp" %>

