<%@ taglib uri='struts-bean' prefix='bean' %>
<%@ taglib uri='struts-logic' prefix='logic' %>
<%@ taglib uri='struts-html' prefix='html' %>
<%@ taglib uri='struts-genurl' prefix='genurl' %>
<%@ taglib uri='bedework' prefix='bw' %>
<html:xhtml/>

<%@ include file="/docs/header.jsp" %>

<%
try {
%>

<page>searchResult</page>

<searchResults>
  <bw:emitText name="calForm" property="query"/>
  <bw:emitText name="calForm" property="resultSize" />
  <bw:emitText name="calForm" property="curPage"/>
  <bw:emitText name="calForm" property="numPages"/>
  <bw:emitText name="calForm" property="searchLimits"/>

  <logic:present name="calForm" property="searchResult" >
    <logic:iterate id="sre" name="calForm" property="searchResult" >
      <searchResult>
        <bw:emitText name="sre" property="score" />
        <logic:present name="sre" property="event" >
          <bean:define id="eventFmt" name="sre" property="event" toScope="request"  />
          <bean:define id="eventInfo" name="eventFmt" property="eventInfo" toScope="request"  />
          <bean:define id="event" name="eventFmt" property="event" toScope="request"  />
          <%@ include file="/docs/event/emitEventCommon.jsp" %>
        </logic:present>
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

