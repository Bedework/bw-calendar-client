<%@ taglib uri='struts-bean' prefix='bean' %>
<%@ taglib uri='struts-logic' prefix='logic' %>
<%@ taglib uri='bedework' prefix='bw' %>
    <%-- Output any event fields with forms specific to short format displays --%>
    <logic:present  name="event" property="location">
      <bean:define id="location" name="event" property="location"/>
      <location>
        <id><bean:write name="location" property="id"/></id><%--
            Value: integer - location id --%>
        <bw:emitText name="location" property="addressField" tagName="address"/><%--
          Value: string - physical address of the location --%>
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
        <logic:present name="location" property="subaddress">
          <bw:emitText name="location" property="subaddress.value" tagName="subaddress"/><%--
            Value: string - more address information --%>
        </logic:present>
        <bw:emitText name="location" property="street" />
        <bw:emitText name="location" property="city" />
        <bw:emitText name="location" property="state" />
        <bw:emitText name="location" property="zip" />
        <bw:emitText name="location" property="link"/><%--
            Value: URI - link to a web address for the location --%>
        <bw:emitText name="location" property="code" />
        <bw:emitText name="location" property="alternateAddress" />
      </location>
    </logic:present>
    <logic:notPresent  name="event" property="location">
      <location>
        <address></address>
      </location>
    </logic:notPresent>
    <categories>
      <logic:present name="event" property="categories">
        <logic:iterate id="category" name="event" property="categories">
          <%@include file="/docs/category/emitCategory.jsp"%>
        </logic:iterate>
      </logic:present>
    </categories>

