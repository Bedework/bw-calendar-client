<%@ taglib uri='struts-bean' prefix='bean' %>
<%@ taglib uri='struts-logic' prefix='logic' %>
<%@ taglib uri='struts-html' prefix='html' %>
<%@ taglib uri='struts-genurl' prefix='genurl' %>
<html:xhtml/>

<bedework>
  <page>groupWidget</page>

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

  <approot><bean:write name="calForm" property="presentationState.appRoot"/></approot>

  <%-- List of groups  --%>
  <groups>
    <logic:iterate id="adminGroup" name="calForm" property="adminGroupsInfo" >
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
  </groups>
</bedework>

