/* ********************************************************************
    Appropriate copyright notice
*/
package org.bedework.util.struts;

import org.apache.struts.Globals;
import org.apache.struts.taglib.html.FormTag;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;

/**
 * User: mike Date: 4/6/22 Time: 15:29
 */
public class BwFormTag extends FormTag {
  public int doStartTag() throws JspException {
    pageContext.setAttribute(Globals.XHTML_KEY, "true",
                             PageContext.PAGE_SCOPE);
    return super.doStartTag();
  }
}
