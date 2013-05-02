<%@ taglib uri='struts-bean' prefix='bean' %>
<%@ taglib uri='struts-logic' prefix='logic' %>
<%@ taglib uri='struts-html' prefix='html' %>
<%@ taglib uri='struts-genurl' prefix='genurl' %>
<html:xhtml/>

<html>
  <head>
    <title>Events Calendar: Logged Out</title>
  </head>
  <body>
    <logic:notEmpty name="calForm" property="err.msgList">
      <p>The following errors occurred:</p>
      <ul>
      <logic:iterate id="errBean" name="calForm" property="err.msgList">
        <li><bean:write name="errBean" property="msg" filter="no" /></li>
      </logic:iterate>
      </ul>
    </logic:notEmpty>

    <logic:notEmpty name="calForm" property="msg.msgList">
      <bean:message key="messages.header" />
      <logic:iterate id="msgBean" name="calForm" property="msg.msgList">
        <li><bean:write name="msgBean" property="msg" filter="no" /></li>
      </logic:iterate>
      <bean:message key="messages.footer" />
    </logic:notEmpty>
    <div id="header">
      <h1>Events Calendar Administration</h1>
    </div>

    <h2>Application error</h2>

    <div id="content">
      <genurl:form action="setup.do" >
        <p>An application error occurred. If the problem persists please inform us
        using the link below.
        </p>

        <html:submit property="confirm" value="OK"/>
      </genurl:form>
    </div>
    <div id="footer">
      Based on the <a href="http://www.bedework.org/">Bedework Calendar</a>
    </div>
  </body>
</html>



