/* ********************************************************************
    Appropriate copyright notice
*/
package org.bedework.webcommon.taglib;

import org.bedework.convert.RecurRuleComponents;
import org.bedework.webcommon.tagcommon.BwTagUtils;

import java.util.Collection;

import javax.servlet.jsp.JspTagException;
import javax.servlet.jsp.JspWriter;

/**
 * User: mike Date: 4/2/22 Time: 14:32
 */
public class EmitRrulesTag extends NameScopePropertyTag {
  /** Optional attribute: for those who like tidy xml
   * If specified we add the value after a new line. */
  private String indent;

  /** Called at end of Tag
   *
   * @return int      either EVAL_PAGE or SKIP_PAGE
   */
  public int doEndTag() throws JspTagException {
    try {
      /* Try to retrieve the value */
      final Collection<RecurRuleComponents> rrules =
              (Collection<RecurRuleComponents>)getObject(false);

      final JspWriter out = pageContext.getOut();

      BwTagUtils.OutRrules(out, indent, rrules);
    } catch(final Throwable t) {
      t.printStackTrace();
      throw new JspTagException("Error: " + t.getMessage());
    }

    return EVAL_PAGE;
  }

  /**
   * @param val String indent
   */
  public void setIndent(final String val) {
    indent = val;
  }

  /**
   * @return  String indent
   */
  public String getIndent() {
    return indent;
  }
}
