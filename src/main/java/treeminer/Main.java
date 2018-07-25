package treeminer;

import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.tuple.ImmutablePair;

public class Main {

	public static void main(String[] args) {
		//List<String> trees = Arrays.asList("a b -1", "a");
//		List<String> trees = Arrays.asList("1 2 -1 3 4 -1 -1", "2 1 2 -1 4 -1 -1 2 -1 3 -1", "1 3 2 -1 -1 5 1 2 -1 3 4 -1 -1 -1 -1");
//		List<String> trees = Arrays.asList("A B A -1 C -1 B -1 -1 C -1 C B -1 A -1 -1");
		List<String> trees = Arrays.asList("A B -1 C D -1 -1");

		new TreeMiner().findFrequentSubtrees(trees, 1).forEach(tree -> {
			System.out.println(tree);
		});
	}
	

}
