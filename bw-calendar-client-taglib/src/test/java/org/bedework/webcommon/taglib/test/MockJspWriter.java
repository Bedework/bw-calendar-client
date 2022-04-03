/* ********************************************************************
    Appropriate copyright notice
*/
package org.bedework.webcommon.taglib.test;

import javax.servlet.jsp.JspWriter;

/**
 * User: mike Date: 4/2/22 Time: 13:28
 */
public class MockJspWriter extends JspWriter {
  private StringBuilder content = new StringBuilder();

  MockJspWriter() {
    super(100, false);
  }

  String getContent() {
    return content.toString();
  }

  @Override
  public void newLine() {
    content.append('\n');
  }

  @Override
  public void print(final boolean b) {
    content.append(b);
  }

  @Override
  public void print(final char c) {
    content.append(c);
  }

  @Override
  public void print(final int i) {
    content.append(i);
  }

  @Override
  public void print(final long l) {
    content.append(l);
  }

  @Override
  public void print(final float v) {
    content.append(v);
  }

  @Override
  public void print(final double v) {
    content.append(v);
  }

  @Override
  public void print(final char[] chars) {
    content.append(chars);
  }

  @Override
  public void print(final String s) {
    content.append(s);
  }

  @Override
  public void print(final Object o) {
    content.append(o);
  }

  @Override
  public void println() {
    newLine();
  }

  @Override
  public void println(final boolean b) {
    print(b);
    newLine();
  }

  @Override
  public void println(final char c) {
    print(c);
    newLine();
  }

  @Override
  public void println(final int i) {
    print(i);
    newLine();
  }

  @Override
  public void println(final long l) {
    print(l);
    newLine();
  }

  @Override
  public void println(final float v) {
    print(v);
    newLine();
  }

  @Override
  public void println(final double v) {
    print(v);
    newLine();
  }

  @Override
  public void println(final char[] chars) {
    print(chars);
    newLine();
  }

  @Override
  public void println(final String s) {
    print(s);
    newLine();
  }

  @Override
  public void println(final Object o) {
    print(o);
    newLine();
  }

  @Override
  public void clear() {
    content = new StringBuilder();
  }

  @Override
  public void clearBuffer() {
    content = new StringBuilder();
  }

  @Override
  public void write(final char[] cbuf, final int off, final int len)
          {
    content.append(cbuf, off, len);
  }

  @Override
  public void flush() {

  }

  @Override
  public void close() {

  }

  @Override
  public int getRemaining() {
    return 0;
  }
}
