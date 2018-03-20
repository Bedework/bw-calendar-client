<%@ taglib uri='struts-bean' prefix='bean' %>
<%@ taglib uri='struts-logic' prefix='logic' %>
<%@ taglib uri='struts-html' prefix='html' %>
<%@ taglib uri='struts-genurl' prefix='genurl' %>
<%@ taglib uri='bedework' prefix='bw' %>
<html:xhtml/>

<synchInfo>
  <logic:present name="calForm" property="synchInfo">
    <bean:define id="synchInfo" name="calForm"
                 property="synchInfo"
                 toScope="request" />
    <logic:iterate id="conn" name="synchInfo" property="conns">
      <conn>
        <bw:emitText name="conn" property="name" />
        <bw:emitText name="conn" property="manager" />
        <bw:emitText name="conn" property="readOnly" />
        <props>
          <logic:iterate id="prop" name="conn" property="props">
            <prop>
              <bw:emitText name="prop" property="name" />
              <bw:emitText name="prop" property="type" />
              <bw:emitText name="prop" property="secure" />
              <bw:emitText name="prop" property="description" />
              <bw:emitText name="prop" property="required" />
            </prop>
          </logic:iterate>
        </props>
      </conn>
    </logic:iterate>
  </logic:present>
</synchInfo>
