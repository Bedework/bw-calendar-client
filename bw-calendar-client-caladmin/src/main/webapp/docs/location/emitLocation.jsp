<%@ taglib uri='struts-bean' prefix='bean' %>
<%@ taglib uri='struts-logic' prefix='logic' %>
<%@ taglib uri='struts-html' prefix='html' %>
<%@ taglib uri='bedework' prefix='bw' %>
<html:xhtml/>

<bean:define id="uid" name="location" property="uid"/>
<% rpitemp="/location/fetchForUpdate.do?uid=" + uid; %>
<location>
  <address>
    <genurl:link page="<%=rpitemp%>">
      <bean:write name="location" property="addressField" />
    </genurl:link>
  </address>

  <bw:emitText name="location" property="roomField" />
  <bw:emitText name="location" property="subField1" />
  <bw:emitText name="location" property="subField2" />

  <logic:equal name="location" property="accessible" value="true" >
    <accessible>true</accessible>
  </logic:equal>
  <logic:notEqual name="location" property="accessible" value="true" >
    <accessible>false</accessible>
  </logic:notEqual>
  <bw:emitText name="location" property="geouri" />
  <bw:emitText name="location" property="status" />

  <subaddress>
    <logic:present name="location" property="subaddress" >
      <bean:write name="location" property="subaddress.value" />
    </logic:present>
  </subaddress>

  <bw:emitText name="location" property="street" />
  <bw:emitText name="location" property="city" />
  <bw:emitText name="location" property="state" />
  <bw:emitText name="location" property="zip" />
  <bw:emitText name="location" property="link" />

  <bw:emitText name="location" property="code" />
  <bw:emitText name="location" property="alternateAddress" />
  <keys>
    <logic:present name="location" property="keys">
      <logic:iterate id="keyFld" name="location" property="keys">
        <bw:emitText name="keyFld" property="keyName" />
        <bw:emitText name="keyFld" property="keyVal" />
      </logic:iterate>
    </logic:present>
  </keys>
</location>

