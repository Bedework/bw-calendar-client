<%@ taglib uri='struts-bean' prefix='bean' %>
<%@ taglib uri='struts-logic' prefix='logic' %>

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
  <logic:present name="category" property="colPath" >
      <bw:emitText name="category" property="colPath" />
  </logic:present>
  <logic:present name="category" property="name" >
      <bw:emitText name="category" property="name" />
  </logic:present>
  <%-- description of the category  --%>
  <logic:present name="category" property="description" >
    <bw:emitText name="category" property="description.value" tagName="description" />
  </logic:present>
  <%-- creator of the category  --%>
  <bw:emitText name="category" property="creatorHref" tagName="creator" />
</category>

