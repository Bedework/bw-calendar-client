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
package org.bedework.webcommon.calsuite;

import org.bedework.calfacade.BwSystem;
import org.bedework.calfacade.SubContext;
import org.bedework.calfacade.svc.BwCalSuite;

import java.util.Set;

/**
 * A simple class to encapsulate the management of a calendar suite's
 * context.
 *
 * @author eric.wittmann@redhat.com
 */
public class CalSuiteContextHelper {
  private BwSystem syspars;

  /**
   * Constructor.
   * @param syspars
   */
  public CalSuiteContextHelper(final BwSystem syspars) {
    this.syspars = syspars;
  }

  /**
   * Called to manage the new calendar suite context.
   * @param suite
   * @param newContextName
   * @param newDefContext
   * @return true if anything was changed
   */
  public boolean updateSuiteContext(final BwCalSuite suite, final String newContextName, final boolean newDefContext) {
    boolean somethingChanged = false;
    boolean hasNewContext = (newContextName != null) && (newContextName.trim().length() > 0);

    // Figure out the currently configured sub-context (if any)
    SubContext oldContext = null;
    SubContext oldDefaultContext = null;
    Set<SubContext> contexts = syspars.getContexts();
    for (SubContext subContext : contexts) {
      if (subContext.getCalSuite().equals(suite.getName())) {
        oldContext = subContext;
      }
      if (subContext.getDefaultContext()) {
        oldDefaultContext = subContext;
      }
    }
    boolean hasOldContext = oldContext != null;

    // No old context, no new context - do nothing
    if (!hasOldContext && !hasNewContext) {
      // Nothing to be done!
    }
    // Add a new context for the suite (no need to delete the old one, none existed)
    if (!hasOldContext && hasNewContext) {
      if (newDefContext && (oldDefaultContext != null)) {
        syspars.removeContext(oldDefaultContext);
        syspars.addContext(new SubContext(oldDefaultContext.getContextName(),
                                          oldDefaultContext.getCalSuite(),
                                          false));
      }
      syspars.addContext(new SubContext(newContextName, suite.getName(), newDefContext));
      somethingChanged = true;
    }
    // Remove the old context
    if (hasOldContext && !hasNewContext) {
      syspars.removeContext(oldContext);
      somethingChanged = true;
    }
    // Finally, the hardest case - the context changed in some way
    if (hasOldContext && hasNewContext) {
      boolean oldDefContext = oldContext.getDefaultContext();
      // default changed - need to un-default the old default context
      if (newDefContext && (newDefContext != oldDefContext) && (oldDefaultContext != null)) {
        syspars.removeContext(oldDefaultContext);
        syspars.addContext(new SubContext(oldDefaultContext.getContextName(),
                                          oldDefaultContext.getCalSuite(),
                                          false));
      }
      // do the update (remove/add actually)
      syspars.removeContext(oldContext);
      syspars.addContext(new SubContext(newContextName, suite.getName(), newDefContext));
      somethingChanged = true;
    }

    return somethingChanged;
  }

  /**
   * Removes the context from the list of contexts.
   * @param ctxName the context name
   * @return true if the context was successfully removed
   */
  public boolean removeSuiteContext(final String ctxName) {
    SubContext oldContext = null;
    Set<SubContext> contexts = syspars.getContexts();
    for (SubContext subContext : contexts) {
      if (subContext.getContextName().equals(ctxName)) {
        oldContext = subContext;
      }
    }
    if (oldContext != null) {
      syspars.removeContext(oldContext);
      return true;
    } else {
      return false;
    }
  }

}
