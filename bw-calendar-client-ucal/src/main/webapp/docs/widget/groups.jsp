<%@ page contentType="text/xml;charset=UTF-8" buffer="none" language="java" %><?xml version="1.0" encoding="UTF-8"?>
<%@ taglib uri='struts-bean' prefix='bean' %>
<%@ taglib uri='struts-logic' prefix='logic' %>
<%@ taglib uri='struts-html' prefix='html' %>
<%@ taglib uri='struts-genurl' prefix='genurl' %>
<%@ taglib uri='bedework' prefix='bw' %>
<html:xhtml/>

<bedework>
  <page>groupWidget</page>

  <logic:iterate id="appvar" name="calForm" property="appVars">
    <appvar>
      <key><bean:write name="appvar" property="key" /></key>
      <value><bean:write name="appvar" property="value" /></value>
    </appvar>
  </logic:iterate>

  <logic:iterate id="msg" name="calForm" property="msg.msgList">
    <message>
      <id><bean:write name="msg" property="msgId" /></id>
      <logic:iterate id="param" name="msg" property="params" >
        <param><bean:write name="param" /></param>
      </logic:iterate>
    </message>
  </logic:iterate>

  <logic:iterate id="errBean" name="calForm" property="err.msgList">
    <error>
      <id><bean:write name="errBean" property="msgId" /></id>
      <logic:iterate id="param" name="errBean" property="params" >
        <param><bean:write name="param" /></param>
      </logic:iterate>
    </error>
  </logic:iterate>

  <bean:define id="presentationState"
               name="bw_presentationstate" scope="request" />
  <bw:emitText name="presentationState" property="appRoot" tagName="appRoot" />

  <%-- List of groups  --%>
  <groups>
    <logic:present name="bw_admin_groups" scope="session" >
      <logic:iterate id="adminGroup" name="bw_admin_groups" scope="session" >
        <group>
          <eventOwner><bean:write name="adminGroup" property="ownerHref" /></eventOwner>
          <name><bean:write name="adminGroup" property="account" /></name>
          <description><bean:write name="adminGroup" property="description" /></description>
          <logic:iterate id="ancestorGroup" name="adminGroup" property="groups" >
            <memberof>
              <name><bean:write name="ancestorGroup" property="account" /></name>
            </memberof>
          </logic:iterate>
        </group>
      </logic:iterate>
    </logic:present>
  </groups>
</bedework>

