/* ********************************************************************
    Appropriate copyright notice
*/
package org.bedework.webcommon;

import org.bedework.appcommon.client.Client;
import org.bedework.calfacade.BwCalendar;
import org.bedework.calfacade.BwCategory;
import org.bedework.calfacade.BwProperty;
import org.bedework.calfacade.responses.Response;
import org.bedework.util.misc.Logged;
import org.bedework.util.misc.Util;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import static org.bedework.calfacade.responses.Response.Status.ok;

/**
 * User: mike Date: 3/17/16 Time: 22:49
 */
public class ColCloner extends Logged{
  private final Map<String, BwCalendar> clonedCols = new HashMap<>();

  private final Map<String, BwCategory> clonedCats = new HashMap<>();

  private final Client cl;

  private final Set<String> openStates;
  
  private int numCloned;
  private int numCached;
  private int numCopied;
  private int numNodes;
  private int numSearches;
  private int numAliasResolve;

  public static class CloneResult extends Response {
    /* true if we found it in the map */
    boolean alreadyCloned;

    private BwCalendar col;

    CloneResult() {
    }
    
    CloneResult(final BwCalendar col,
                final boolean alreadyCloned) {
      setStatus(ok);
      this.col = col;
      this.alreadyCloned = alreadyCloned;
    }
    
    public BwCalendar getCol() {
      return col;
    }
  }

  ColCloner(final Client cl,
         final Set<String> openStates) {
    this.cl = cl;
    this.openStates = openStates;
  }

  private static class CloneStatus {
    Deque<String> virtualPath = new ArrayDeque<>();
    
    void pushVp(final BwCalendar col) {
      if (virtualPath.isEmpty()) {
        virtualPath.push(Util.buildPath(true, "/", col.getName()));
        return;
      }

      virtualPath.push(Util.buildPath(true, "/", virtualPath.peek(),
                                      "/", col.getName()));
    }
    
    void popVp() {
      virtualPath.pop();
    }

    String getVp() {
      return virtualPath.peek();
    }
  }
  
  /** Given the root, fetch all the collections and clone them. 
   * 
   * @param val root of tree
   * @param fromCopy true if copying previously cloned tree
   * @return response containing root of cloned tree
   */
  CloneResult deepClone(final BwCalendar val,
                        final boolean fromCopy) {
    final long start = System.currentTimeMillis();
    if (debug) {
      debug("start clone. fromCopy: " + fromCopy);
    }
    
    final CloneResult cr = deepClone(new CloneStatus(), val, fromCopy);

    if (debug) {
      debug("================================");
      debug("          cloned: " + numCloned);
      debug("          cached: " + numCached);
      debug("          copied: " + numCopied);
      debug("           nodes: " + numNodes);
      debug("        searches: " + numSearches);
      debug("aliases resolved: " + numAliasResolve);
      debug("            time: " + (System.currentTimeMillis() - start));
      debug("================================");
    }
    
    return cr;
  }

  private CloneResult deepClone(final CloneStatus status, 
                                final BwCalendar val,
                                final boolean fromCopy) {
    try {
      status.pushVp(val);
      
      final CloneResult cr = cloneOne(status, val, fromCopy);

      if (cr.getStatus() != ok) {
        return cr;
      }
      
      cr.getCol().setVirtualPath(status.getVp());
      
      if (val.getCalType() != BwCalendar.calTypeExtSub) {
        final CloneResult gccr = getChildren(status, cr.col, fromCopy);
        if (!gccr.isOk()) {
          return gccr;
        }
      }

      return cr;
    } finally {
      status.popVp();      
    }
  }

  /* Clone the single entity - which might already be cloned.
   *
   * If this is an alias then the alias target will also be cloned, 
   * which might in itself require a deep clone.
   * 
   * If any entity is already cloned a rewrapped copy will be returned.
   * 
   */
  private CloneResult cloneOne(final CloneStatus status,
                               final BwCalendar val,
                               final boolean fromCopy) {
    numNodes++;
    BwCalendar clCol;
    
    if (fromCopy) {
      numCopied++;
      clCol = val.cloneWrapper();
      clonedCols.put(val.getPath(), clCol);

      return new CloneResult(clCol, true);
    }
    
    clCol = clonedCols.get(val.getPath());

    if (clCol != null) {
      numCached++;
      if (openStates != null) {
        clCol.setOpen(openStates.contains(clCol.getPath()));
      }
      
      clCol = clCol.cloneWrapper();

      return new CloneResult(clCol, true);
    }

    numCloned++;
    clCol = val.shallowClone();

    clCol.setCategories(cloneCategories(val));
    clCol.setProperties(cloneProperties(val));

    if (openStates != null) {
      clCol.setOpen(openStates.contains(clCol.getPath()));
    }

    clonedCols.put(val.getPath(), clCol);

    if ((val.getCalType() == BwCalendar.calTypeAlias) &&
            (val.getAliasUri() != null)) {
      final BwCalendar aliased;
      try {
        numAliasResolve++;
        aliased = cl.resolveAlias(val, false, false);
      } catch (final Throwable t) {
        if (debug) {
          error(t);
        }
        return Response.error(new CloneResult(), t.getMessage());
      }
      
      if (aliased != null) {
        // Do a deep clone to rewrap entities if we already fetched them
        final CloneResult resp = deepClone(status, 
                                           aliased, 
                                           false); // fromCopy
        if (!resp.isOk()) {
          return resp;
        }
          
        final BwCalendar clonedAlias = resp.getCol();
        
        clCol.setAliasCalType(clonedAlias.getCalType());
        clCol.setAliasTarget(clonedAlias);
        clonedAlias.setAliasOrigin(clCol);
      }
    }

    return new CloneResult(clCol, false);
  }

  /* Get the children for the already cloned collection. 
   *
   */
  private CloneResult getChildren(final CloneStatus status,
                                  final BwCalendar col,
                                  final boolean fromCopy) {
    Collection<BwCalendar> children = col.getChildren();

    if (Util.isEmpty(children) && (col.getCalType() == BwCalendar.calTypeAlias)) {
      // The alias target will already have been resolved and cloned
      if (col.getAliasTarget() != null) {
        children = col.getAliasTarget().getChildren();
      }
    }
    
    final int size;
    
    if ((children != null) ||
        !col.getCollectionInfo().childrenAllowed) {
      // A null collection signifies we haven't tried fetching
      size = 0;
    } else {
      try {
        numSearches++;
        children = cl.getChildren(col);
      } catch (final Throwable t) {
        if (debug) {
          error(t);
        }
        return Response.error(new CloneResult(), t.getMessage());
      }
      
      size = children.size();
    }
    
    final Collection<BwCalendar> cloned = new ArrayList<>(size);
    col.setChildren(cloned);

    if (Util.isEmpty(children)) {
      return okReturn();
    }
    
    for (final BwCalendar c:children) {
      final CloneResult cr = deepClone(status, c, fromCopy);
      if (!cr.isOk()) {
        return cr;
      }
      
      cloned.add(cr.col);
    }

    return okReturn();
  }

  private CloneResult okReturn() {
    final CloneResult cr = new CloneResult();
    cr.setStatus(ok);
    
    return cr;
  }
  
  private Set<BwCategory> cloneCategories(final BwCalendar val) {
    if (val.getNumCategories() == 0) {
      return null;
    }

    final TreeSet<BwCategory> ts = new TreeSet<>();

    for (final BwCategory cat: val.getCategories()) {
      final BwCategory clCat = 
              clonedCats.computeIfAbsent(cat.getUid(),
                                         k -> (BwCategory)cat.clone());

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
