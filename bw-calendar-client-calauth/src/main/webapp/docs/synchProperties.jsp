<%@ taglib uri='struts-bean' prefix='bean' %>
<%@ taglib uri='struts-logic' prefix='logic' %>
<%@ taglib uri='struts-html' prefix='html' %>
<%@ taglib uri='struts-genurl' prefix='genurl' %>
<%@ taglib uri='bedework' prefix='bw' %>

<properties>
  <logic:iterate id="property" name="properties" property="property">
    <property>
      <bw:emitText name="property" property="name" />
      <bw:emitText name="property" property="value" />
    </property>
  </logic:iterate>
</properties>
