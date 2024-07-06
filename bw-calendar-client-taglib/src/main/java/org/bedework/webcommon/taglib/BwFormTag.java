/* ********************************************************************
    Appropriate copyright notice
*/
package org.bedework.webcommon.taglib;

import org.bedework.util.misc.Util;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspTagException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.tagext.TagSupport;

/**
 * Bedework form actions always end in ".do" and are
 * never absolute.
 *
 * User: mike Date: 4/6/22 Time: 15:29
 */
public class BwFormTag extends TagSupport {
  private String action;

  public int doStartTag() throws JspException {
    try {
      final JspWriter out = pageContext.getOut();

      out.print("<form method=\"post\" action=\"");
      out.print(resolveUrl(action));
      out.println("\">");
    } catch(final Throwable t) {
      throw new JspTagException("Error: " + t.getMessage());
    }

    return EVAL_PAGE;
  }

  public int doEndTag() throws JspException {
    try {
      final JspWriter out = pageContext.getOut();

      out.print("</form>");
    } catch(final Throwable t) {
      throw new JspTagException("Error: " + t.getMessage());
    }

    return EVAL_PAGE;
  }

  public void setAction(final String val) {
    action = val;
  }

  public String getAction() {
    return action;
  }

  public String resolveUrl(final String url) {
    final var request = (HttpServletRequest)pageContext.getRequest();

    final String suffix;
    if (url.endsWith(".do")) {
      suffix = "";
    } else {
      suffix = ".do";
    }

    final String context = request.getContextPath();

    return Util.buildPath(false, context, "/", url) + suffix;
  }
}
