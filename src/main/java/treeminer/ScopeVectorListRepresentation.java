package treeminer;

import java.util.List;

public class ScopeVectorListRepresentation extends AScopeListRepresentation<ScopeVectorListElement> {

	@Override
	public AScopeListRepresentation<ScopeVectorListElement> outScopeJoin(
			AScopeListRepresentation<ScopeVectorListElement> other, int attachedTo) {

		ScopeVectorListRepresentation newScopeVectorList = new ScopeVectorListRepresentation();

		this.forEach(scopeListElementX -> other.forEach(scopeListElementY -> {
			if (scopeListElementX.getTreeIndex() == scopeListElementY.getTreeIndex()
					&& scopeListElementX.getScopes().get(scopeListElementX.getScopes().size() - 1).isStrictlyLessThan(
							scopeListElementY.getScopes().get(scopeListElementY.getScopes().size() - 1))) {

				Scope sYN = scopeListElementY.getScopes().get(scopeListElementY.getScopes().size() - 1);

				if (scopeListElementX.getScopes().get(attachedTo).contains(sYN)
						&& scopeListElementX.getScopes().get(attachedTo + 1).isStrictlyLessThan(sYN)) {
					List<Scope> scopes = scopeListElementX.getScopes().subList(0, attachedTo);
					scopes.add(sYN);
					newScopeVectorList.add(new ScopeVectorListElement(scopeListElementX.getTreeIndex(), scopes));
				} else if (scopeListElementX.getScopes().get(attachedTo).isStrictlyLessThan(sYN) && scopeListElementY
						.getScopes().get(attachedTo).contains(scopeListElementX.getScopes().get(attachedTo))) {
					List<Scope> scopes = scopeListElementY.getScopes().subList(0, attachedTo);
					scopes.add(sYN);
					newScopeVectorList.add(new ScopeVectorListElement(scopeListElementX.getTreeIndex(), scopes));
				}

			}
		}));

		return newScopeVectorList;
	}

	@Override
	public AScopeListRepresentation<ScopeVectorListElement> inScopeJoin (
			AScopeListRepresentation<ScopeVectorListElement> other) {
		ScopeVectorListRepresentation newScopeVectorList = new ScopeVectorListRepresentation();

		this.forEach(scopeListElementX -> other.forEach(scopeListElementY -> {
			if (scopeListElementX.getTreeIndex() == scopeListElementY.getTreeIndex()) {
				// Compare last element of lists
				Scope sXM = scopeListElementX.getScopes().get(scopeListElementX.getScopes().size() - 1);
				Scope sYN = scopeListElementY.getScopes().get(scopeListElementY.getScopes().size() - 1);
				if (sXM.contains(sYN)) {
					boolean isMinimal = true;
					for (ScopeVectorListElement otherX : this) {
						Scope sXL = otherX.getScopes().get(otherX.getScopes().size() - 1);

						if (sXM.contains(sXL) && sXL.contains(sYN)) {
							isMinimal = false;
							break;
						}
					}

					if (isMinimal) {
						List<Scope> scopes = scopeListElementX.getScopes();
						scopes.add(sYN);
						newScopeVectorList.add(new ScopeVectorListElement(scopeListElementX.getTreeIndex(), scopes));
					}
				}
			}
		}));

		return newScopeVectorList;
	}

}
