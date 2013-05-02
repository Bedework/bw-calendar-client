<%@ taglib uri='struts-bean' prefix='bean' %>
<%@ taglib uri='struts-logic' prefix='logic' %>
<%@ taglib uri='struts-html' prefix='html' %>
<%@ taglib uri='struts-genurl' prefix='genurl' %>
<html:xhtml/>

<%@ include file="/docs/header.jsp" %>

<%
try {
%>

<page>upload</page>

<formElements>
 <form>
   <!-- user's writable calendars -->
   <calendars>
     <html:select name="calForm" property="calendarId">
       <html:optionsCollection name="calForm" property="addContentCalendarCollections"
                                     label="path"
                                     value="path"/>
     </html:select>
   </calendars>
 </form>
</formElements>

<%
} catch (Throwable t) {
  t.printStackTrace();
}
%>

<%@ include file="/docs/footer.jsp" %>

