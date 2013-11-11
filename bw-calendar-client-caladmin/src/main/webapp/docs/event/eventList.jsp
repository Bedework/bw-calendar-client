<%@ taglib uri='struts-bean' prefix='bean' %>
<%@ taglib uri='struts-logic' prefix='logic' %>
<%@ taglib uri='struts-html' prefix='html' %>
<%@ taglib uri='struts-genurl' prefix='genurl' %>
<html:xhtml/>

<bedework>
<%@include file="/docs/header.jsp"%>

<page>eventList</page>
<tab>main</tab>

<%@include file="/docs/event/eventListRoot.jsp"%>

<%--  Generate form elements to be exposed --%>
<formElements>
  <genurl:form action="event/fetchForDisplay.do">
    <listAllSwitchFalse>
      <html:radio name="calForm" property="listAllEvents"
                    value="false"
                    onclick="document.calForm.submit();" />
    </listAllSwitchFalse>
    <listAllSwitchTrue>
      <html:radio name="calForm" property="listAllEvents"
                    value="true"
                    onclick="document.calForm.submit();" />
    </listAllSwitchTrue>
  </genurl:form>
</formElements>

<%@include file="/docs/footer.jsp"%>

</bedework>
