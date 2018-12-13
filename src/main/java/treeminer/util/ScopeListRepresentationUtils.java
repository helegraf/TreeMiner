package treeminer.util;

import java.util.List;

import org.apache.commons.lang3.tuple.Pair;

import treeminer.EquivalenceClass;
import treeminer.scopelists.elements.SimpleScopeListElement;
import treeminer.scopelists.representation.AScopeListRepresentation;
import treeminer.scopelists.representation.ScopeListRepresentation;
import treeminer.scopelists.representation.ScopeVectorListRepresentation;

/**
 * Utils for scope lists. Does scope list representation-dependent scope list
 * joins and checks whether prefixes occur directly in a database of trees.
 * 
 * @author Helena Graf
 *
 */
public class ScopeListRepresentationUtils {

	private ScopeListRepresentationUtils() {
	}

	/**
	 * Do an out-scope join that is dependent on whether distinct occurrences of
	 * patterns are counted or not (join an x element that extends the pattern to
	 * build a new prefix with a y element that also extended the previous element).
	 * 
	 * @param yJElement
	 *            the element y that is attached to node j
	 * @param xScopeList
	 *            the scope list of the x element
	 * @param yScopeList
	 *            the scope list of the y element
	 * @param countMultipleOccurrences
	 *            whether multiple occurrences of a pattern are counted
	 * @return the new scope list resulting from the join
	 */
	public static AScopeListRepresentation<? extends SimpleScopeListElement> doOutScopeJoin(
			Pair<String, Integer> yJElement, AScopeListRepresentation<? extends SimpleScopeListElement> xScopeList,
			AScopeListRepresentation<? extends SimpleScopeListElement> yScopeList, boolean countMultipleOccurrences) {
		if (countMultipleOccurrences) {
			return ((ScopeListRepresentation) xScopeList).outScopeJoin((ScopeListRepresentation) yScopeList,
					yJElement.getRight());
		} else {
			return ((ScopeVectorListRepresentation) xScopeList).outScopeJoin((ScopeVectorListRepresentation) yScopeList,
					yJElement.getRight());
		}
	}

	/**
	 * Do an in-scope join that is dependent on whether distinct occurrences of
	 * patterns are counted or not (join an x element that extends the pattern to
	 * build a new prefix with a y element that also extended the previous element).
	 * 
	 * @param xScopeList
	 *            the scope list of the x element
	 * @param yScopeList
	 *            the scope list of the y element
	 * @param countMultipleOccurrences
	 *            whether multiple occurrences of a pattern are counted
	 * @return the new scope list resulting from the join
	 */
	public static AScopeListRepresentation<? extends SimpleScopeListElement> doInScopeJoin(
			AScopeListRepresentation<? extends SimpleScopeListElement> xScopeList,
			AScopeListRepresentation<? extends SimpleScopeListElement> yScopeList, boolean countMultipleOccurrences) {

		if (countMultipleOccurrences) {
			return ((ScopeListRepresentation) xScopeList).inScopeJoin((ScopeListRepresentation) yScopeList);
		} else {
			return ((ScopeVectorListRepresentation) xScopeList).inScopeJoin((ScopeVectorListRepresentation) yScopeList);
		}
	}

	/**
	 * Check whether this new given prefix occurs directly in the given database of
	 * trees, based on the occurrences given in the equivalence class.
	 * 
	 * @param equivalenceClass
	 *            the equivalence class which contains a scope list for the new
	 *            prefix
	 * @param trees
	 *            the database of trees in which we search for patterns
	 * @param newPrefix
	 *            the new prefix for which to check if it occurs directly anywhere
	 * @return whether the new prefix occurs directly
	 */
	public static boolean prefixOccursDirectly(EquivalenceClass equivalenceClass, List<String> trees,
			String newPrefix) {
		boolean occurrsDirectly = false;
		for (SimpleScopeListElement scopeListElement : equivalenceClass.getScopeListFor(newPrefix)) {
			if (TreeRepresentationUtils.containsSubtree(trees.get(scopeListElement.getTreeIndex()), newPrefix)) {
				occurrsDirectly = true;
			}
		}
		return occurrsDirectly;
	}

}
