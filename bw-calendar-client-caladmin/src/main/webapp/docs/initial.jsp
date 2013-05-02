<%@ taglib uri="struts-logic" prefix="logic" %>
<%--
  Redirect default requests to the initial action.
  Currently, as defined in ../WEB-INF/struts-config.xml, this will result in
  loading main.jsp
--%>
<logic:forward name="initial" />
