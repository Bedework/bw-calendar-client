<%@ taglib uri='struts-bean' prefix='bean' %>
<%@ taglib uri='struts-logic' prefix='logic' %>
<%@ taglib uri='struts-html' prefix='html' %>
<%@ taglib uri='struts-genurl' prefix='genurl' %>
<%@ taglib uri='bedework' prefix='bw' %>
<html:xhtml/>

<bedework>
<%@ include file="/docs/header.jsp" %>

<%
try {
%>

<page>searchResult</page>
<tab>main</tab>

<searchResults>
  <bw:emitText name="moduleState" property="query"/>
  <bw:emitText name="moduleState" property="searchLimits"/>
  <logic:notPresent name="bw_search_result" scope="request">
    <resultSize>0</resultSize>
  </logic:notPresent>

  <logic:present name="bw_search_result" scope="request">
    <bean:define id="sres" name="bw_search_result" scope="request"/>
    <bw:emitText name="sres" property="found" tagName="resultSize" />
    <logic:iterate id="sre" name="bw_search_list" scope="request">
      <searchResult>
        <bw:emitText name="sre" property="score" />
        <logic:equal name="sre" property="docType" value="event">
          <bean:define id="eventFmt" name="sre" property="entity" toScope="request"  />
          <bean:define id="eventInfo" name="eventFmt" property="eventInfo" toScope="request"  />
          <bean:define id="event" name="eventFmt" property="event" toScope="request"  />
          <%@ include file="/docs/event/emitEventCommon.jsp" %>
        </logic:equal>
      </searchResult>
    </logic:iterate>
  </logic:present>

</searchResults>

<%
} catch (Throwable t) {
  t.printStackTrace();
}
%>

<%@ include file="/docs/footer.jsp" %>

</bedework>
