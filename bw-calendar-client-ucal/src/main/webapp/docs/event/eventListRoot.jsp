<%@ taglib uri='struts-bean' prefix='bean' %>
<%@ taglib uri='struts-logic' prefix='logic' %>
<%@ taglib uri='struts-html' prefix='html' %>
<%@ taglib uri='struts-genurl' prefix='genurl' %>
<html:xhtml/>

<%--  Generate events --%>
<bean:define id="detailView" value="true" toScope="request"/>
<events>
  <logic:present name="bw_search_result" scope="request">
    <bean:define id="sres" name="bw_search_result" scope="request" />
    <logic:present name="bw_search_params" scope="request" >
      <bean:define id="params" name="bw_search_params" scope="request" />
      <bw:emitText name="params" property="curOffset" />
      <bw:emitText name="params" property="pageSize" />
      <logic:present  name="params" property="toDate">
        <bw:emitText name="params" property="toDate.dtval" />
      </logic:present>
    </logic:present>
    <logic:notPresent name="bw_search_params" scope="request" >
      <curOffset>0</curOffset>
      <pageSize>0</pageSize>
      <toDate.dtval>19530315</toDate.dtval>
    </logic:notPresent>

    <paged>true</paged>
    <bw:emitText name="sres" property="found" tagName="resultSize" />

    <logic:present name="bw_search_list" scope="request">
      <logic:iterate id="sre" name="bw_search_list" scope="request">
        <logic:equal name="sre" property="docType" value="event">
          <bean:define id="eventFormatter"
                     name="sre" property="entity" toScope="request"  />
          <%@include file="/docs/event/emitEvent.jsp"%>
        </logic:equal>
      </logic:iterate>
    </logic:present>
  </logic:present>
</events>
