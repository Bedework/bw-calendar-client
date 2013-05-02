/* ********************************************************************
    Licensed to Jasig under one or more contributor license
    agreements. See the NOTICE file distributed with this work
    for additional information regarding copyright ownership.
    Jasig licenses this file to you under the Apache License,
    Version 2.0 (the "License"); you may not use this file
    except in compliance with the License. You may obtain a
    copy of the License at:
        
    http://www.apache.org/licenses/LICENSE-2.0
        
    Unless required by applicable law or agreed to in writing,
    software distributed under the License is distributed on
    an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
    KIND, either express or implied. See the License for the
    specific language governing permissions and limitations
    under the License.
*/

package org.bedework.appcommon;

import java.io.Serializable;

/** This class is used by the clients to determine which of two incoming
 * request parameters will set the final value. It is typically used when we
 * have preferred and all select boxes and we get an id from each.
 *
 * <p>We have two values, A and B
 *
 * @author  Mike Douglass douglm @ rpi.edu
 *
 * @param <T>
 */
public class SelectId<T> implements Serializable {
  /** ENUM Neither A nor B has precedence
   */
  public static final int NoneHasPrecedence = 0;

  /** A has precedence
   */
  public static final int AHasPrecedence = 1;

  /** B has precedence
   */
  public static final int BHasPrecedence = 2;

  private int precedence;

  private T originalValue;
  private T newValue;

  private boolean changed;

  /** Create an object with the given original value in which neither A nor B
   * have preference.
   *
   * @param originalValue int
   */
  public SelectId(T originalValue) {
    this.originalValue = originalValue;
    this.newValue = originalValue;
  }

  /** Create an object with the given original value indicating which of A
   * or B have preference.
   *
   * @param originalValue
   * @param preferred
   */
  public SelectId(T originalValue, int preferred) {
    reset(originalValue, preferred);
  }

  /** Reset an object with the given original value indicating which of A
   * or B have preference.
   *
   * @param originalValue
   * @param preferred
   */
  public void reset(T originalValue, int preferred) {
    this.originalValue = originalValue;
    this.newValue = originalValue;
    precedence = preferred;
    changed = false;
  }

  /** Set the A value
   *
   * @param val
   */
  public void setA(T val) {
    if (val.equals(originalValue)) {
      return;
    }

    if (!changed ||
        (precedence == AHasPrecedence) ||
        (precedence == NoneHasPrecedence)) {
      newValue = val;
      changed = true;
    }
  }

  /** Set the B value
   *
   * @param val
   */
  public void setB(T val) {
    if (val.equals(originalValue)) {
      return;
    }

    if (!changed ||
        (precedence == BHasPrecedence) ||
        (precedence == NoneHasPrecedence)) {
      newValue = val;
      changed = true;
    }
  }

  /** Return true if value changed
   *
   * @return boolean
   */
  public boolean getChanged() {
    return changed;
  }

  /**
   * @return T original value
   */
  public T getOriginalVal() {
    return originalValue;
  }

  /**
   * @return T new value
   */
  public T getVal() {
    return newValue;
  }
}
