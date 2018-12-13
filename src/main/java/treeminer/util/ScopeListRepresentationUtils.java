package treeminer.util;

import java.util.List;

import org.apache.commons.lang3.tuple.Pair;

import treeminer.EquivalenceClass;
import treeminer.scopelists.elements.SimpleScopeListElement;
import treeminer.scopelists.representation.AScopeListRepresentation;
import treeminer.scopelists.representation.ScopeListRepresentation;
import treeminer.scopelists.representation.ScopeVectorListRepresentation;

public class ScopeListRepresentationUtils {
	
	private ScopeListRepresentationUtils() {}

	public static AScopeListRepresentation<? extends SimpleScopeListElement> doOutScopeJoin(Pair<String, Integer> yJElement,
			AScopeListRepresentation<? extends SimpleScopeListElement> xScopeList,
			AScopeListRepresentation<? extends SimpleScopeListElement> yScopeList, boolean countMultipleOccurrences) {
		if (countMultipleOccurrences) {
			return ((ScopeListRepresentation) xScopeList).outScopeJoin((ScopeListRepresentation) yScopeList,
					yJElement.getRight());
		} else {
			return ((ScopeVectorListRepresentation) xScopeList).outScopeJoin((ScopeVectorListRepresentation) yScopeList,
					yJElement.getRight());
		}
	}

	public static AScopeListRepresentation<? extends SimpleScopeListElement> doInScopeJoin(
			AScopeListRepresentation<? extends SimpleScopeListElement> xScopeList,
			AScopeListRepresentation<? extends SimpleScopeListElement> yScopeList, boolean countMultipleOccurrences) {
	
		if (countMultipleOccurrences) {
			return ((ScopeListRepresentation) xScopeList).inScopeJoin((ScopeListRepresentation) yScopeList);
		} else {
			return ((ScopeVectorListRepresentation) xScopeList).inScopeJoin((ScopeVectorListRepresentation) yScopeList);
		}
	}

	public static boolean prefixOccursDirectly(EquivalenceClass equivalenceClass, List<String> trees, String newPrefix) {
		boolean occurrsDirectly = false;
		for (SimpleScopeListElement scopeListElement : equivalenceClass.getScopeListFor(newPrefix)) {
			if (TreeRepresentationUtils.containsSubtree(trees.get(scopeListElement.getTreeIndex()), newPrefix)) {
				occurrsDirectly = true;
			}
		}
		return occurrsDirectly;
	}

}
