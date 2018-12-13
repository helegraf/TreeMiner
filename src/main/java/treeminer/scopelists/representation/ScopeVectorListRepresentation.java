package treeminer.scopelists.representation;

import java.util.ArrayList;
import java.util.List;

import treeminer.Scope;
import treeminer.scopelists.elements.ScopeVectorListElement;

/**
 * A scope list representation that contains scope vector list elements as
 * elements.
 * 
 * @author Helena Graf
 *
 */
public class ScopeVectorListRepresentation extends AScopeListRepresentation<ScopeVectorListElement> {

	@Override
	public AScopeListRepresentation<ScopeVectorListElement> outScopeJoin(
			AScopeListRepresentation<ScopeVectorListElement> other, int attachedTo) {

		ScopeVectorListRepresentation newScopeVectorList = new ScopeVectorListRepresentation();

		this.forEach(scopeListElementX -> other.forEach(scopeListElementY -> {
			if (scopeListElementX.getTreeIndex() == scopeListElementY.getTreeIndex()
					&& scopeListElementX.getLastElement().isStrictlyLessThan(scopeListElementY.getLastElement())) {

				Scope sYN = scopeListElementY.getLastElement();

				int jOffset = scopeListElementX.getScopes().get(0).getLowerBound() + attachedTo;
				int jIndex = 0;
				for (int i = 0; i < scopeListElementX.getScopes().size(); scopeListElementX.getScopes()) {
					if (scopeListElementX.getScopes().get(i).getLowerBound() == jOffset) {
						jIndex = i;
						break;
					}
				}

				if (scopeListElementX.getScopes().get(jIndex).contains(sYN)
						&& scopeListElementX.getScopes().get(jIndex + 1).isStrictlyLessThan(sYN)) {
					System.err.println("Join done case 1");
					List<Scope> scopes = new ArrayList<>(scopeListElementX.getScopes());
					scopes = scopes.subList(0, jIndex + 1);
					scopes.add(sYN);
					newScopeVectorList.add(new ScopeVectorListElement(scopeListElementX.getTreeIndex(), scopes));
				} else if (scopeListElementX.getScopes().get(jIndex).isStrictlyLessThan(sYN) && scopeListElementY
						.getScopes().get(jIndex).contains(scopeListElementX.getScopes().get(jIndex))) {
					System.err.println("Join done case 2");
					List<Scope> scopes = new ArrayList<>(scopeListElementY.getScopes());
					scopes = scopes.subList(0, jIndex + 1);
					scopes.add(sYN);
					newScopeVectorList.add(new ScopeVectorListElement(scopeListElementX.getTreeIndex(), scopes));
				} else {
					System.out.println("Join not done for other reasons");
					System.out.println(scopeListElementX);
					System.out.println(scopeListElementY);
					System.out.println(jIndex);
				}

			}
		}));

		return newScopeVectorList;
	}

	@Override
	public AScopeListRepresentation<ScopeVectorListElement> inScopeJoin(
			AScopeListRepresentation<ScopeVectorListElement> other) {
		ScopeVectorListRepresentation newScopeVectorList = new ScopeVectorListRepresentation();

		this.forEach(scopeListElementX -> other.forEach(scopeListElementY -> {
			if (scopeListElementX.getTreeIndex() == scopeListElementY.getTreeIndex()) {
				// Compare last element of lists
				Scope sXM = scopeListElementX.getLastElement();
				Scope sYN = scopeListElementY.getLastElement();
				if (sXM.contains(sYN)) {
					boolean isMinimal = true;
					for (ScopeVectorListElement otherX : this) {
						Scope sXL = otherX.getLastElement();

						if (sXM.contains(sXL) && sXL.contains(sYN)) {
							isMinimal = false;
							break;
						}
					}

					if (isMinimal) {
						// these scopes should be the rightmost nodes on the path. Since s_y gets added
						// under s_x, we just add s_y to the x scope list
						List<Scope> scopes = new ArrayList<>(scopeListElementX.getScopes());
						scopes.add(sYN);
						newScopeVectorList.add(new ScopeVectorListElement(scopeListElementX.getTreeIndex(), scopes));
					}
				}
			}
		}));

		return newScopeVectorList;
	}

}
