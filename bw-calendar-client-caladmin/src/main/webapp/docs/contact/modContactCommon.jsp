<%@ taglib uri='struts-bean' prefix='bean' %>
<%@ taglib uri='struts-logic' prefix='logic' %>
<%@ taglib uri='struts-html' prefix='html' %>
<%@ taglib uri='struts-genurl' prefix='genurl' %>
<html:xhtml/>

<name><html:text property="contactName.value" size="30"/></name>
<status><html:text property="contactStatus" size="30"/></status>
<phone><html:text property="contact.phone" size="30"/></phone>
<link><html:text property="contact.link" size="30"/></link>
<email><html:text property="contact.email" size="30"/></email>
