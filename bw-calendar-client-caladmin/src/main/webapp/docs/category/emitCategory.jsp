<%@ taglib uri='struts-bean' prefix='bean' %>
<%@ taglib uri='struts-logic' prefix='logic' %>

<category>
  <%-- unique id  --%>
  <bw:emitText name="category" property="uid" />
  <%-- text value of the category --%>
  <logic:present name="category" property="word" >
    <logic:present name="category" property="word.value" >
      <bw:emitText name="category" property="word.value" tagName="value" />
    </logic:present>
    <logic:notPresent name="category" property="word.value" >
      <value></value>
    </logic:notPresent>
  </logic:present>
  <logic:notPresent name="category" property="word" >
    <value></value>
  </logic:notPresent>
  <%-- description of the category  --%>
  <logic:present name="category" property="description" >
    <logic:present name="category" property="description.value" >
      <bw:emitText name="category" property="description.value" tagName="description" />
    </logic:present>
  </logic:present>
  <%-- creator of the category  --%>
  <bw:emitText name="category" property="creatorHref" tagName="creator" />
</category>

