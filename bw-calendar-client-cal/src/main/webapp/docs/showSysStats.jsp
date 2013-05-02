<%@ taglib uri='struts-bean' prefix='bean' %>
<%@ taglib uri='struts-logic' prefix='logic' %>
<%@ taglib uri='struts-genurl' prefix='genurl' %>

<%@include file="/docs/header.jsp"%>

<page>showSysStats</page>

<sysStats>
  <logic:iterate id="sysStat" name="calForm" property="sysStats">
    <logic:equal name="sysStat" property="statKind" value="0">
      <header><bean:write name="sysStat" property="statLabel" /></header>
    </logic:equal>
    <logic:notEqual name="sysStat" property="statKind" value="0">
      <stat>
        <label><bean:write name="sysStat" property="statLabel" /></label>
        <type><bean:write name="sysStat" property="statType" /></type>
        <value><bean:write name="sysStat" property="statVal" /></value>
      </stat>
    </logic:notEqual>
  </logic:iterate>
</sysStats>

<%@include file="/docs/footer.jsp"%>
