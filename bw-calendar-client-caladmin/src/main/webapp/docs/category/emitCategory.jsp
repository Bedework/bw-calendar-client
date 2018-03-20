<%@ taglib uri='struts-bean' prefix='bean' %>
<%@ taglib uri='struts-logic' prefix='logic' %>

<category>
  <%-- unique id  --%>
  <bw:emitText name="category" property="uid" />
  <bw:emitText name="category" property="href" />
  <%-- text value of the category --%>
  <logic:present name="category" property="word" >
    <bw:emitText name="category" property="word.value" tagName="value" />
  </logic:present>
  <logic:notPresent name="category" property="word" >
    <value></value>
  </logic:notPresent>
    <bw:emitText name="category" property="colPath" />
    <bw:emitText name="category" property="name" />
  <%-- description of the category  --%>
    <bw:emitText name="category" property="descriptionVal" tagName="description" />
    <%-- status of the category  --%>
    <bw:emitText name="category" property="status" tagName="status" />
  <%-- creator of the category  --%>
  <bw:emitText name="category" property="creatorHref" tagName="creator" />
</category>

