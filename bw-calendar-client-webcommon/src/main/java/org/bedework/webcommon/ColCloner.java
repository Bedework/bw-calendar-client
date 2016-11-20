/* ********************************************************************
    Appropriate copyright notice
*/
package org.bedework.webcommon;

import org.bedework.appcommon.client.Client;
import org.bedework.calfacade.BwCalendar;
import org.bedework.calfacade.BwCategory;
import org.bedework.calfacade.BwProperty;
import org.bedework.util.misc.Util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

/**
 * User: mike Date: 3/17/16 Time: 22:49
 */
public class ColCloner {
  private final Map<String, BwCalendar> clonedCols = new HashMap<>();

  private final Map<String, BwCategory> clonedCats = new HashMap<>();

  private final Client cl;

  private final Set<String> openStates;

  private static class CloneResult {
    /* true if we found it in the map */
    boolean alreadyCloned;

    BwCalendar col;

    CloneResult(final BwCalendar col,
                final boolean alreadyCloned) {
      this.col = col;
      this.alreadyCloned = alreadyCloned;
    }
  }

  ColCloner(final Client cl,
         final Set<String> openStates) {
    this.cl = cl;
    this.openStates = openStates;
  }

  /** Given the root, fetch all the collections and clone them. 
   * 
   * @param val root of tree
   * @param fromCopy true if copying previously cloned tree
   * @return cloned tree
   * @throws Throwable
   */
  BwCalendar deepClone(final BwCalendar val,
                       final boolean fromCopy) throws Throwable {
    final CloneResult cr = cloneOne(val, fromCopy);

    if (cr.alreadyCloned) {
      return cr.col;
    }

    if ((val.getCalType() != BwCalendar.calTypeAlias) &&
            (val.getCalType() != BwCalendar.calTypeExtSub)) {
      cr.col.setChildren(getChildren(val, fromCopy));
    }

    return cr.col;
  }

  private CloneResult cloneOne(final BwCalendar val,
                               final boolean fromCopy) throws Throwable {
    BwCalendar clCol = clonedCols.get(val.getPath());

    if (clCol != null) {
      if (openStates != null) {
        clCol.setOpen(openStates.contains(clCol.getPath()));
      }

      return new CloneResult(clCol, true);
    }

    clCol = val.shallowClone();

    clCol.setCategories(cloneCategories(val));
    clCol.setProperties(cloneProperties(val));

    if (openStates != null) {
      clCol.setOpen(openStates.contains(clCol.getPath()));
    }

    clonedCols.put(val.getPath(), clCol);

    if ((val.getCalType() == BwCalendar.calTypeAlias) &&
            (val.getAliasUri() != null)) {
      final BwCalendar aliased = cl.resolveAlias(val, false, false);

      if (aliased != null) {
        BwCalendar clonedAlias = clonedCols.get(aliased.getPath());
        
        if (clonedAlias == null) {
          // Need to clone this one
          clonedAlias = deepClone(aliased, fromCopy);
          clonedCols.put(clonedAlias.getPath(), clonedAlias);
        }
        clCol.setAliasCalType(clonedAlias.getCalType());
        clCol.setAliasTarget(clonedAlias);
        clonedAlias.setAliasOrigin(clCol);
      }
    }

    return new CloneResult(clCol, false);
  }

  private Collection<BwCalendar> getChildren(final BwCalendar col,
                                             final boolean fromCopy) throws Throwable {
    final Collection<BwCalendar> children;
    
    if (fromCopy) {
      children = col.getChildren();
    } else {
      children = cl.getChildren(col);
    }
    
    final Collection<BwCalendar> cloned = new ArrayList<>(children.size());

    if (!Util.isEmpty(children)) {
      for (final BwCalendar c:children) {
        final CloneResult cr = cloneOne(c, fromCopy);
        cloned.add(cr.col);

        if (cr.alreadyCloned) {
          continue;
        }

        // Clone the subtree
        if (c.getCalType() == BwCalendar.calTypeAlias) {
          // Subtree is referenced by the target
          if (cr.col.getAliasTarget() != null) {
            cr.col.setChildren(cr.col.getAliasTarget().getChildren());
          }
          continue;
        }
        
        if (c.getCalType() != BwCalendar.calTypeExtSub) {
          cr.col.setChildren(getChildren(c, fromCopy));
        }
      }
    }

    return cloned;
  }

  private Set<BwCategory> cloneCategories(final BwCalendar val) {
    if (val.getNumCategories() == 0) {
      return null;
    }

    final TreeSet<BwCategory> ts = new TreeSet<>();

    for (final BwCategory cat: val.getCategories()) {
      BwCategory clCat = clonedCats.get(cat.getUid());

      if (clCat == null) {
        clCat = (BwCategory)cat.clone();
        clonedCats.put(cat.getUid(), clCat);
      }

      ts.add(clCat);
    }

    return ts;
  }

  private Set<BwProperty> cloneProperties(final BwCalendar val) {
    if (val.getNumProperties() == 0) {
      return null;
    }

    final TreeSet<BwProperty> ts = new TreeSet<>();

    for (final BwProperty p: val.getProperties()) {
      ts.add((BwProperty)p.clone());
    }

    return ts;
  }
}
