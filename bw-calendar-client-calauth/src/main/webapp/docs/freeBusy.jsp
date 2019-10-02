<%@ taglib uri='struts-bean' prefix='bean' %>
<%@ taglib uri='struts-logic' prefix='logic' %>
<%@ taglib uri='struts-genurl' prefix='genurl' %>
<%@ taglib uri='bedework' prefix='bw' %>

<freebusy>
  <logic:present name="calForm" property="formattedFreeBusy" >
    <bean:define id="freeBusyObj" name="calForm" property="formattedFreeBusy" />
    <bw:emitText name="freeBusyObj" property="account" tagName="who" />
    <bw:emitText name="freeBusyObj" property="start.dtval" tagName="start" />
    <bw:emitText name="freeBusyObj" property="end.dtval" tagName="end" />
    <logic:iterate id="day" name="freeBusyObj" property="days" >
      <day>
        <bw:emitText name="day" property="dateString" />
        <logic:iterate id="fbperiod"  name="day" property="periods" >
          <period>
            <bw:emitText name="fbperiod" property="type" tagName="fbtype" />
            <bw:emitText name="fbperiod" property="startTime" tagName="start" />
            <bw:emitText name="fbperiod" property="minutesLength" tagName="length" />
            <bw:emitText name="fbperiod" property="numBusy" />
            <bw:emitText name="fbperiod" property="numTentative" />
          </period>
        </logic:iterate>
      </day>
    </logic:iterate>
  </logic:present>
  <logic:present name="calForm" property="fbResponses" >
    <bean:define id="fbresps" name="calForm" property="fbResponses" />
      <logic:present name="fbresps" property="responses" >
        <logic:iterate id="resp" name="fbresps" property="responses" >
          <fbattendee>
            <bw:emitText name="resp" property="respCode" />
            <bw:emitText name="resp" property="recipient" />
          </fbattendee>
        </logic:iterate>
      </logic:present>
  </logic:present>
</freebusy>

