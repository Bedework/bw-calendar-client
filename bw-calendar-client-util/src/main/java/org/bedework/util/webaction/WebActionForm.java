package org.bedework.util.webaction;

import org.bedework.util.servlet.MessageEmit;

import java.util.HashMap;
import java.util.Locale;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

/**
 * User: mike Date: 3/31/22 Time: 14:18
 */
public interface WebActionForm {
  /** Inc waiting for resource
   *
   */
  void incWaiters();

  /** Dec waiting for resource
   *
   */
  void decWaiters();

  /** Get waiting for resource
   *
   * @return num waiting for resource
   */
  public int getWaiters();

  /** Set inuse flag
   *
   * @param val boolean value of inuse flag
   */
  void assignInuse(boolean val);

  /**
   * @return boolean value of inuse flag
   */
  public boolean getInuse();

  /* ================ Properties methods ============== */

  /** Set initialised state
   *
   * @param val initialised state
   */
  void setInitialised(boolean val);

  /**
   * @return initialised state
   */
  public boolean getInitialised();

  /**
   * @param val current locale
   */
  void setCurrentLocale(Locale val);

  /**
   * @return current locale
   */
  public Locale getCurrentLocale();

  /**
   * @param val true for no cache
   */
  void setNocache(boolean val);

  /**
   * @return boolean true for nocache
   */
  public boolean getNocache();

  /**
   * @param val MessageEmit
   */
  void setErr(final MessageEmit val);

  /**
   * @return MessageEmit
   */
  MessageEmit getErr();

  /**
   * @return boolean
   */
  boolean getErrorsEmitted();

  /**
   * @param val message object
   */
  void setMsg(MessageEmit val);

  /**
   * @return boolean
   */
  MessageEmit getMsg();

  /**
   * @return boolean
   */
  @SuppressWarnings("unused")
  boolean getMessagesEmitted();

  /** Can be called by a page to signal an exceptiuon
   *
   * @param t exception
   */
  void setException(Throwable t);

  /**
   * @param val application vars
   */
  void setAppVarsTbl(HashMap<String, String> val);

  /** Used by jsp.
   *
   * @return Set of  application vars
   */
  @SuppressWarnings("unused")
  public Set getAppVars();

  /**
   * @param val name for content
   */
  void setContentName(String val);

  /**
   * @return name for content
   */
  String getContentName();
  
  /**
   * @param val url for app
   */
  void setUrl(String val);

  /**
   * @return String url for app
   */
  String getUrl();

  /**
   * @param val schema + host + port part of uri
   */
  void setSchemeHostPort(String val);

  /**
   * @return schema + host + port part of uri
   */
  String getSchemeHostPort();

  /** Set the part of the URL that identifies the application.
   *
   * @param val       context path in form "/" + name-of-app, e.g. /kiosk
   */
  void setContext(String val);

  /**
   * @return String
   */
  String getContext();

  /** Sets the scheme + host + port part of the url together with the
   *  path up to the servlet path. This allows us to append a new action to
   *  the end.
   *  <p>For example, we want val="http://myhost.com:8080/myapp"
   *
   *  @param  val   the URL prefix
   */
  void setUrlPrefix(String val);

  /** Returns the scheme + host + port part of the url together with the
   *  path up to the servlet path. This allows us to append a new action to
   *  the end.
   *
   *  @return  String   the URL prefix
   */
  String getUrlPrefix();

  /** This should not be setCurrentUser as that exposes it to the incoming
   * request.
   *
   * @param val      String user id
   */
  void assignCurrentUser(String val);

  /**
   * @return String
   */
  String getCurrentUser();

  /** This should not be setSessionId as that exposes it to the incoming
   * request.
   *
   * @param val      String session id
   */
  void assignSessionId(String val);

  /**
   * @return String
   */
  String getSessionId();

  void assignErrorForward(String val);

  String getErrorForward();

  /** This should not be setConfirmationId as that exposes it to the incoming
   * request.
   *
   * @param val      String confirmation id
   */
  void assignConfirmationId(String val);

  /**
   * @return String
   */
  String getConfirmationId();

  /**
   * @param val browser type
   */
  void setBrowserType(String val);

  /**
   * @return browser type
   */
  String getBrowserType();

  void reset(HttpServletRequest request);
}
