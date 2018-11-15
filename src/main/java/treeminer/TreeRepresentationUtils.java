package treeminer;

import java.util.List;

import org.apache.commons.lang3.tuple.Pair;

/**
 * Class that provides methods that help with working with String
 * representations of trees. Here, the
 * 
 * @author Helena Graf
 *
 */
public class TreeRepresentationUtils {

	private TreeRepresentationUtils() {
	}

	/**
	 * The token that is used to separate tree nodes in the String representation of
	 * a tree. It cannot appear in the labels of nodes, as it is used to separate
	 * nodes.
	 */
	public static final String TREE_NODE_SEPARATOR = " ";

	/**
	 * The token that is used to represent moving up to a parent node in the String
	 * representation of a tree. There cannot be a label in a tree representation
	 * whose name is equal to this token.
	 */
	public static final String MOVE_UP_TOKEN = "-1";

	/**
	 * Creates the String representation of the tree that is created when appending
	 * all the given children in order to the node that is given as the root.
	 * Children can also be whole subtrees.
	 * 
	 * @param node
	 *            The root of the new tree
	 * @param children
	 *            The children of the root of the new tree
	 * @return The String representation of the new tree.
	 */
	public static String addChildrenToNode(String node, List<String> children) {
		StringBuilder builder = new StringBuilder();
		builder.append(node);

		children.forEach(child -> {
			builder.append(TREE_NODE_SEPARATOR);
			builder.append(child);
			builder.append(TREE_NODE_SEPARATOR);
			builder.append(MOVE_UP_TOKEN);
		});

		return builder.toString();
	}

	/**
	 * Adds the given node to the given tree and returns the new tree.
	 * 
	 * @param tree
	 *            The tree to which the node is added
	 * @param node
	 *            The node that is added to the tree
	 * @return The new tree after the node has been added
	 */
	public static String addNodeToTree(String tree, Pair<String, Integer> node) {
		// Check if empty prefix
		if (tree == null || tree.equals("")) {
			return node.getLeft();
		}

		// Check if the tree consists of only a root node
		if (tree.length() == 1) {
			StringBuilder builder = new StringBuilder();
			builder.append(tree);
			builder.append(TreeRepresentationUtils.TREE_NODE_SEPARATOR);
			builder.append(node.getLeft());
			builder.append(TreeRepresentationUtils.TREE_NODE_SEPARATOR);
			builder.append(TreeRepresentationUtils.MOVE_UP_TOKEN);
			return builder.toString();
		}

		StringBuilder builder = new StringBuilder();
		String[] treeElements = tree.split(TreeRepresentationUtils.TREE_NODE_SEPARATOR);
		boolean foundNode = false;
		boolean attachedNode = false;
		int atNode = -1;
		for (int i = 0; i < treeElements.length - 1; i++) {
			String treeElement = treeElements[i];

			// Find the node where the node shall be attached
			if (!foundNode) {
				// If the element is a node
				if (!treeElement.equals(TreeRepresentationUtils.MOVE_UP_TOKEN)) {
					// Increase the count
					atNode++;
					if (atNode == node.getRight()) {
						// We have found the node where we want to attach
						foundNode = true;
						atNode = 0;
					}
				}
				// Append the current node to the tree
				builder.append(treeElement);
				builder.append(TreeRepresentationUtils.TREE_NODE_SEPARATOR);
			} else if (!attachedNode) {
				// Find out if we can directly attach the child now
				if (atNode == 0) {
					// Check if there are no more children (would move up again here!)
					if (treeElements[i].equals(TreeRepresentationUtils.MOVE_UP_TOKEN)) {
						builder.append(node.getLeft());
						builder.append(TreeRepresentationUtils.TREE_NODE_SEPARATOR);
						builder.append(TreeRepresentationUtils.MOVE_UP_TOKEN);
						builder.append(TreeRepresentationUtils.TREE_NODE_SEPARATOR);
						attachedNode = true;
					} else {
						atNode--;
					}
				} else {
					atNode = (treeElement.equals(TreeRepresentationUtils.MOVE_UP_TOKEN)) ? atNode + 1 : atNode - 1;
				}
				builder.append(treeElement);
				builder.append(TreeRepresentationUtils.TREE_NODE_SEPARATOR);
			} else {
				// In this case we have found and attached the node and just need to add all the
				// -1
				builder.append(treeElement);
				builder.append(TreeRepresentationUtils.TREE_NODE_SEPARATOR);
			}
		}

		// Last node
		if (!attachedNode) {
			if (atNode == 0) {
				builder.append(node.getLeft());
				builder.append(TreeRepresentationUtils.TREE_NODE_SEPARATOR);
				builder.append(TreeRepresentationUtils.MOVE_UP_TOKEN);
				builder.append(TreeRepresentationUtils.TREE_NODE_SEPARATOR);
				builder.append(treeElements[treeElements.length - 1]);
			} else {
				builder.append(treeElements[treeElements.length - 1]);
				builder.append(TreeRepresentationUtils.TREE_NODE_SEPARATOR);
				builder.append(node.getLeft());
				builder.append(TreeRepresentationUtils.TREE_NODE_SEPARATOR);
				builder.append(TreeRepresentationUtils.MOVE_UP_TOKEN);
			}
		} else {
			builder.append(treeElements[treeElements.length - 1]);
		}

		return builder.toString();
	}

	/**
	 * Creates a String representation of the tree branch represented by the given
	 * nodes. The first node will be the root of the created branch, the second node
	 * a child of the first node, the third node a child of the second node, and so
	 * forth.
	 * 
	 * @param nodes
	 *            The nodes to be converted to a branch
	 * @return The String representation of the converted branch
	 */
	public static String makeRepresentationForBranch(List<String> nodes) {
		StringBuilder builder = new StringBuilder();
		nodes.forEach(node -> {
			builder.append(node);
			builder.append(TREE_NODE_SEPARATOR);
		});

		for (int i = 0; i < nodes.size() - 2; i++) {
			builder.append(MOVE_UP_TOKEN);
			builder.append(TREE_NODE_SEPARATOR);
		}

		builder.append(MOVE_UP_TOKEN);

		return builder.toString();
	}

	/**
	 * Gives the number of children of a given node in a given tree.
	 * 
	 * @param tree
	 *            The tree in which the node is contained
	 * @param node
	 *            The node in the tree identified by its position in the depth-first
	 *            pre-order traversal of the tree
	 * @return The number of children the node has in the tree
	 */
	public static int findNumberOfChildrenOfNode(String tree, int node) {
		String[] treeElements = tree.split(" ");

		// Represents the latest node we have found in the given tree
		int atNode = -1;
		boolean foundNode = false;
		int numChildren = 0;
		for (String treeElement : treeElements) {
			if (!foundNode) {
				// If we haven't found the node, keep searching
				if (!treeElement.equals(TreeRepresentationUtils.MOVE_UP_TOKEN)) {
					atNode++;
					if (atNode == node) {
						foundNode = true;
						// atNode now represents depth we are under
						atNode = 0;
					}
				}
			} else {
				if (!treeElement.equals(TreeRepresentationUtils.MOVE_UP_TOKEN)) {
					// We have found a new child
					atNode++;
					numChildren++;
				} else {
					if (atNode == 0) {
						// We have found all children
						break;
					} else {
						// We are moving closer to the node again
						atNode--;
					}
				}
			}
		}

		return numChildren;
	}

	/**
	 * Checks whether the given subtree occurs in the given tree.
	 * 
	 * @param tree
	 *            The tree to check for the occurrence of a subtree
	 * @param subtree
	 *            The subtree which is searched for in the given tree
	 * @return Whether the given subtree occurs in the given tree at least once
	 */
	public static boolean containsSubtree(String tree, String subtree) {
		// Split the given trees in their elements
		String[] treeElements = tree.split(TreeRepresentationUtils.TREE_NODE_SEPARATOR);
		String[] subTreeElements = subtree.split(TreeRepresentationUtils.TREE_NODE_SEPARATOR);

		// Represents the next node from the subtree we are trying to match in the tree
		int nextNode = 0;
		// Represents the number of children a current parent node has that are not
		// found in the subtree
		int childNum = 0;

		// Iterate over the tree representation trying ot match the subtree
		// representation
		for (int i = 0; i < treeElements.length; i++) {
			if (treeElements[i].equals(subTreeElements[nextNode])) {
				if (childNum == 0) {
					// If we are not currently in an unrelated branch in the tree, and have found a
					// matching node, we can start searching for the next one
					nextNode++;
					if (nextNode == subTreeElements.length) {
						return true;
					}
				} else {
					// We have found a matching node, but are in an unrelated branch of the tree
					if (treeElements[i].equals(TreeRepresentationUtils.MOVE_UP_TOKEN)) {
						// We are moving back to the parent
						childNum--;
					} else {
						// We have found another child
						childNum++;
					}
				}
			} else {
				// We have found an unrelated node. If we haven't found the start of the pattern
				// yet, proceed iteration.
				if (nextNode != 0) {
					if (treeElements[i].equals(TreeRepresentationUtils.MOVE_UP_TOKEN)) {
						if (childNum > 0) {
							// If we have found a moveUpToken, and the current matched node has children
							// that aren't in the pattern, we have moved closer to the parent again
							childNum--;
						} else {
							// If there are no children of the current matched node anymore, but the pattern
							// is not completely matched, it means that we have followed a branch that does
							// not contain the whole pattern and need to retreat
							nextNode--;
						}
					} else {
						// We have found another child not in the pattern that we want to match
						childNum++;
					}
				}
			}
		}

		// We iterated over the whole tree without finding the pattern
		return false;
	}
}
