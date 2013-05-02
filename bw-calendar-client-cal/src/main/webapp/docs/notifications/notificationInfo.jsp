<%@ taglib uri='struts-logic' prefix='logic' %>
<%@ taglib uri='bedework' prefix='bw' %>

  <bw:emitText name="notificationInfo" property="changed" />
  <logic:iterate id="notification" name="notificationInfo" property="notifications" >
    <notification>
      <bw:emitText name="notification" property="name" />
      <bw:emitText name="notification" property="type" />
      <bw:emitText name="notification" property="xmlFragment" 
                   tagName="message" filter="false" />
    </notification>
  </logic:iterate>

