<%@ taglib uri='struts-bean' prefix='bean' %>
<%@ taglib uri='struts-logic' prefix='logic' %>
<%@ taglib uri='bedework' prefix='bw' %>

    <%-- Output any event fields with forms specific to short format displays --%>
    <logic:present  name="event" property="location">
      <bean:define id="location" name="event" property="location"/>
      <location>
        <id><bean:write name="location" property="id"/></id><%--
            Value: integer - location id --%>
        <bw:emitText name="location" property="address.value" tagName="address"/><%--
          Value: string - physical address of the location --%>
        <bw:emitText name="location" property="link"/><%--
            Value: URI - link to a web address for the location --%>
        <logic:present name="location" property="subaddress">
          <bw:emitText name="location" property="subaddress.value" tagName="subaddress"/><%--
            Value: string - more address information --%>
        </logic:present>
        <bw:emitText name="location" property="creatorHref" tagName="creator" /><%--
          Value: string - location creator id --%>
      </location>
    </logic:present>
    <logic:notPresent  name="event" property="location">
      <location>
        <address></address>
        <id></id><%--
          Value: integer - location id --%>
        <subaddress></subaddress><%--
          Value: string - more address information --%>
        <link></link><%--
          Value: URI - link to a web address for the location --%>
        <creator></creator><%--
          Value: string - location creator id --%>
      </location>
    </logic:notPresent>
    <categories>
      <logic:present name="event" property="categories">
        <logic:iterate id="category" name="event" property="categories">
          <category>
            <id><bean:write name="category" property="id"/></id><%--
              Value: integer - category id --%>
            <bw:emitText name="category" property="word.value" tagName="word" /><%--
              Value: string - the category value --%>
            <logic:present name="category" property="description" >
              <bw:emitText name="category" property="description.value"
                           tagName="description" /><%--
                  Value: string - long description of category --%>
            </logic:present>
            <bw:emitText name="category" property="creatorHref" tagName="creator" /><%--
              Value: string - category creator id --%>
          </category>
        </logic:iterate>
      </logic:present>
    </categories>
    <jsp:include page="/docs/event/emitRecur.jsp"/>
    <bw:emitText name="event" property="description" /><%--
        Value: string - long description of the event.  Limited to 500 characters. --%>
    <bw:emitText name="event" property="cost" /><%--
        Value: string - cost information --%>
    <sequence><bean:write name="event" property="sequence"/></sequence><%--
        RFC sequence number for the event --%>

    <logic:present name="event" property="contact">
      <bean:define id="contact" name="event" property="contact"/>
      <contact>
        <id><bean:write name="contact" property="id"/></id><%--
          Value: integer - contact id --%>
        <bw:emitText name="contact" property="cn.value" tagName="name"/><%--
          Value: string - contact's name --%>
        <bw:emitText name="contact" property="phone"/><%--
          Value (example): x7777 - contact's phone number --%>
        <bw:emitText name="contact" property="email"/><%--
          Value (example): nobody@nowhere.edu - contact's email address --%>
        <bw:emitText name="contact" property="link"/><%--
          Value: URI - link to contact web page --%>
      </contact>
    </logic:present>

    <logic:present name="event" property="xproperties">
      <xproperties>
        <logic:iterate id="xprop" name="event" property="xproperties">
          <logic:equal name="xprop" property="skipJsp" value="false">
            <bean:define id="xpropName" name="xprop" property="name"/>
            <% String xpropStart = "<" + (String)xpropName + ">";
               String xpropEnd = "</" + (String)xpropName + ">";%>
            <%=xpropStart%>
              <logic:present name="xprop" property="parameters">
                <parameters>
                <logic:iterate id="xpar" name="xprop" property="parameters">
                  <bean:define id="xparName" name="xpar" property="name"/>
                  <% String xparStart = "<" + (String)xparName + ">";
                     String xparEnd = "</" + (String)xparName + ">";%>
                  <%=xparStart%><![CDATA[<bean:write name="xpar" property="value" />]]><%=xparEnd%>
                </logic:iterate>
                </parameters>
              </logic:present>
              <values>
                <text><![CDATA[<bean:write name="xprop" property="value"/>]]></text>
              </values>
            <%=xpropEnd%>
          </logic:equal>
        </logic:iterate>
      </xproperties>
    </logic:present>

