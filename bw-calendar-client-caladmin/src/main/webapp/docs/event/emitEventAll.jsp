<%@ taglib uri='struts-bean' prefix='bean' %>
<%@ taglib uri='struts-logic' prefix='logic' %>
<%@ taglib uri='bedework' prefix='bw' %>

    <%-- Output any additional event fields for full format displays --%>
    <logic:present  name="event" property="organizer">
      <bean:define id="organizer" name="event" property="organizer"/>
      <organizer>
        <bw:emitText name="organizer" property="cn"/><%--
          Value: string - cn of the organizer --%>
        <bw:emitText name="organizer" property="dir"/><%--
              Value: URI - link to a directory for lookup --%>
        <bw:emitText name="organizer" property="language"/><%--
            Value: string - language code --%>
        <bw:emitText name="organizer" property="sentBy"/><%--
          Value: string - usually mailto url --%>
        <bw:emitText name="organizer" property="organizerUri"/><%--
          Value: string - u --%>
      </organizer>
    </logic:present>
    <%--  hide attendees altogether in admin:
    <logic:present name="event" property="attendees">
      <logic:iterate id="attendee" name="event" property="attendees">
        <attendee>
          <id><bean:write name="attendee" property="id"/></id>< %--
              Value: integer - attendee id --% >
          <bw:emitText name="attendee" property="cn"/>< %--
            Value: string - cn of the attendee --% >
          <bw:emitText name="attendee" property="cuType"/>< %--
            Value: string - type of calendar user --% >
          <bw:emitText name="attendee" property="delegatedFrom"/>< %--
                 mailto url --% >
          <bw:emitText name="attendee" property="delegatedTo"/>< %--
                 mailto url --% >
          <bw:emitText name="attendee" property="dir"/>< %--
                Value: URI - link to a directory for lookup --% >
          <bw:emitText name="attendee" property="member"/>
          <bw:emitText name="attendee" property="language"/>< %--
              Value: string - language code --% >
          <bw:emitText name="attendee" property="sentBy"/>< %--
            Value: string - usually mailto url --% >
          <rsvp><bean:write name="attendee" property="rsvp"/></rsvp>
          <bw:emitText name="attendee" property="role"/>
          <bw:emitText name="attendee" property="partstat"/>
          <bw:emitText name="attendee" property="attendeeUri"/>
        </attendee>
      </logic:iterate>
    </logic:present> --%>
    <logic:present name="event" property="comments">
      <comments>
        <logic:iterate id="comment" name="event" property="comments">
            <bw:emitText name="comment"/>
        </logic:iterate>
      </comments>
    </logic:present>
    <bw:emitText name="eventFormatter" property="xmlAccess" tagName="access"
                 filter="no"/>

