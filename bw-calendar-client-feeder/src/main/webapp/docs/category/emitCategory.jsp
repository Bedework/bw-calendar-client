<%@ taglib uri='struts-bean' prefix='bean' %>
<%@ taglib uri='struts-logic' prefix='logic' %>
<%@ taglib uri='bedework' prefix='bw' %>

<category>
  <%-- unique id  --%>
  <bw:emitText name="category" property="uid" />
  <%-- text value of the category --%>
  <logic:present name="category" property="word" >
    <bw:emitText name="category" property="word.value" tagName="value" />
  </logic:present>
  <logic:notPresent name="category" property="word" >
    <value></value>
  </logic:notPresent>
  <%-- description of the category  --%>
  <logic:present name="category" property="description" >
    <bw:emitText name="category" property="description.value" tagName="description" />
  </logic:present>
  <%-- creator of the category  --%>
  <bw:emitText name="category" property="creatorHref" tagName="creator" />
</category>
