package treeminer;

import java.util.List;

public interface FrequentSubtreeFinder {
	public boolean containsSubtree(String tree, String subtree);
	public List<String> findFrequentSubtrees(List<String> trees, int minSupport);
}
