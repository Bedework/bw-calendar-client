<%@ taglib uri='struts-bean' prefix='bean' %>
<%@ taglib uri='struts-logic' prefix='logic' %>
<%@ taglib uri='struts-html' prefix='html' %>
<%@ taglib uri='struts-genurl' prefix='genurl' %>
<html:xhtml/>

<%--  Generate events --%>
<bean:define id="detailView" value="true" toScope="request"/>
<events>
  <bean:define id="sres" name="bw_search_result" scope="request" />
  <bean:define id="params" name="bw_search_params" scope="request" />

  <logic:present name="bw_search_list" scope="request">
    <logic:iterate id="sre" name="bw_search_list" scope="request">
      <logic:equal name="sre" property="docType" value="event">
        <bean:define id="eventFormatter"
                     name="sre" property="entity" toScope="request"  />
        <%@include file="/docs/event/emitEvent.jsp"%>
      </logic:equal>
    </logic:iterate>
  </logic:present>
</events>


