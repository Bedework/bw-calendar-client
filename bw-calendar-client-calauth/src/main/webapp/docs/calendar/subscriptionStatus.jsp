<%@ taglib uri='struts-bean' prefix='bean' %>
<%@ taglib uri='struts-logic' prefix='logic' %>
<%@ taglib uri='struts-html' prefix='html' %>
<%@ taglib uri='struts-genurl' prefix='genurl' %>
<%@ taglib uri='bedework' prefix='bw' %>
<html:xhtml/>

<subscriptionStatus>
  <logic:present name="bw_subscription_status" scope="session">
    <bean:define id="sstatus" name="bw_subscription_status"
                 scope="session"
                 toScope="request" />
    <bw:emitText name="sstatus" property="requestStatus" />
    <logic:present name="sstatus" property="subscriptionStatus" >
      <bean:define id="ss" name="sstatus"
                   property="subscriptionStatus"
                   toScope="request" />
      <bw:emitText name="ss" property="subscriptionId" />
      <bw:emitText name="ss" property="principalHref" />
      <bw:emitText name="ss" property="direction" />
      <bw:emitText name="ss" property="master" />
      <bw:emitText name="ss" property="lastRefresh" />
      <bw:emitText name="ss" property="errorCt" />
      <bw:emitText name="ss" property="missingTarget" />
      <endA>
        <logic:present name="ss" property="endAConnector" >
          <bean:define id="connector" name="ss"
                       property="endAConnector"
                       toScope="request" />
          <bw:emitText name="connector" property="connectorId" />
          <bean:define id="properties" name="connector"
                       property="properties"
                       toScope="request" />
          <%@ include file="/docs/synchProperties.jsp" %>
        </logic:present>
      </endA>
      <endB>
        <logic:present name="ss" property="endBConnector" >
          <bean:define id="connector" name="ss"
                       property="endBConnector"
                       toScope="request" />
          <bw:emitText name="connector" property="connectorId" />
          <bean:define id="properties" name="connector"
                       property="properties"
                       toScope="request" />
          <%@ include file="/docs/synchProperties.jsp" %>
        </logic:present>
      </endB>
      <properties>

      </properties>
    </logic:present>
  </logic:present>
</subscriptionStatus>
