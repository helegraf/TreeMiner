package treeminer.util;

import org.apache.commons.lang3.tuple.Pair;

import treeminer.AScopeListRepresentation;
import treeminer.ScopeListRepresentation;
import treeminer.ScopeVectorListRepresentation;
import treeminer.SimpleScopeListElement;

public class ScopeListRepresentationUtils {

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

}
