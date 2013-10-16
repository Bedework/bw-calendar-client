<%@ taglib uri='struts-bean' prefix='bean' %>
<%@ taglib uri='struts-logic' prefix='logic' %>
<%@ taglib uri='struts-html' prefix='html' %>
<%@ taglib uri='struts-genurl' prefix='genurl' %>
<html:xhtml/>

<%--  Generate events --%>
<bean:define id="detailView" value="true" toScope="request"/>
<events>
  <bean:define id="elpars" name="bw_event_list_pars" scope="request" />

  <bw:emitText name="elpars" property="paged" />
  <bw:emitText name="elpars" property="resultSize" />
  <bw:emitText name="elpars" property="curPage"/>
  <bw:emitText name="elpars" property="numPages"/>

  <logic:present name="bw_event_list" scope="request">
    <logic:iterate id="eventFormatter" name="bw_event_list" scope="request">
      <%@include file="/docs/event/emitEventForList.jsp"%>
    </logic:iterate>
  </logic:present>
</events>
