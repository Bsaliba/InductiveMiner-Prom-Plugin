package bPrime.model;

import org.processmining.processtree.Block;
import org.processmining.processtree.Edge;
import org.processmining.processtree.ProcessTree;
import org.processmining.processtree.Task.Automatic;
import org.processmining.processtree.impl.AbstractBlock;
import org.processmining.processtree.impl.AbstractTask;
import org.processmining.processtree.impl.ProcessTreeImpl;

public class ConversionToProcessTrees {
	
	public static ProcessTree convert(Node root) {
		ProcessTree tree = new ProcessTreeImpl();

		System.out.println("before conversion: " + root.toString());
		
		tree.setRoot(convertNode(tree, root));
		
		System.out.println(tree.toString());

		return tree;
	}
	
	private static org.processmining.processtree.Node convertNode(ProcessTree tree, Node node) {
		org.processmining.processtree.Node newNode = null;
		
		if (node instanceof Binoperator) {
			//operator node
			if (node instanceof ExclusiveChoice) {
				newNode = new AbstractBlock.Xor("");
			} else if (node instanceof Sequence) {
				newNode = new AbstractBlock.Seq("");
			} else if (node instanceof Parallel) {
				newNode = new AbstractBlock.And("");
			} else if (node instanceof Loop) {
				newNode = new AbstractBlock.XorLoop("");
			}
		} else if (node instanceof EventClass) {
			//activity leaf
			newNode = new AbstractTask.Manual(node.toString());
		} else if (node instanceof Tau){
			//tau leaf
			newNode = new AbstractTask.Automatic("");
		}
		
		newNode.setProcessTree(tree);
		tree.addNode(newNode);
		
		//Non block are leafs which end here
		if (!(newNode instanceof Block)) {
			return newNode;
		}
		
		Block newBlock = (Block) newNode;
		
		//And give it its children
		for (Node child : node.children) {
			org.processmining.processtree.Node newChild = convertNode(tree, child);
			//Connect new child to parent and connect the edges
			Edge edge = newBlock.addChild(newChild);
			newChild.addIncomingEdge(edge);
			tree.addEdge(edge);
		}
		
		if (node instanceof Loop && node.children.size() < 3) {
			//For loops a third child is required
			Automatic tau = new AbstractTask.Automatic("");
			Edge edge = newBlock.addChild(tau);
			tau.addIncomingEdge(edge);
			tree.addEdge(edge);
		}
		
		return newNode;
	}
	
}
