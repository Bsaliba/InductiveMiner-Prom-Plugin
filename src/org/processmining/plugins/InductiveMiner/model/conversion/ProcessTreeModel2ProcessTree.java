package org.processmining.plugins.InductiveMiner.model.conversion;

import java.util.Iterator;

import org.processmining.plugins.InductiveMiner.mining.metrics.fitness;
import org.processmining.plugins.InductiveMiner.model.Binoperator;
import org.processmining.plugins.InductiveMiner.model.EventClass;
import org.processmining.plugins.InductiveMiner.model.ExclusiveChoice;
import org.processmining.plugins.InductiveMiner.model.Loop;
import org.processmining.plugins.InductiveMiner.model.Node;
import org.processmining.plugins.InductiveMiner.model.Parallel;
import org.processmining.plugins.InductiveMiner.model.Sequence;
import org.processmining.plugins.InductiveMiner.model.Tau;
import org.processmining.plugins.properties.processmodel.impl.Fitness;
import org.processmining.processtree.Block;
import org.processmining.processtree.Edge;
import org.processmining.processtree.ProcessTree;
import org.processmining.processtree.Task.Automatic;
import org.processmining.processtree.impl.AbstractBlock;
import org.processmining.processtree.impl.AbstractTask;
import org.processmining.processtree.impl.ProcessTreeImpl;


public class ProcessTreeModel2ProcessTree {
	
	public static ProcessTree convert(Node root) {
		ProcessTree tree = new ProcessTreeImpl();
		
		fitness.computeFitness(root);
		//debug("empty traces " + root.metadata.get("subtreeFilteredEmptyTraces").toString());
		//debug("filtered events " + root.metadata.get("subtreeFilteredEvents").toString());
		//debug("fitness " + root.metadata.get("subtreeFitness").toString());
		//debug(root.toString());
		
		tree.setRoot(convertNode(tree, root));

		return tree;
	}
	
	private static org.processmining.processtree.Node convertNode(ProcessTree tree, Node node) {
		if (node == null) {
			return null;
		}
		
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
		attachFitness(node, newNode);
		
		//Non block are leafs which end here
		if (!(newNode instanceof Block)) {
			return newNode;
		}
		
		Block newBlock = (Block) newNode;
		
		if (node instanceof Loop) {
			//For loops, the process tree package requires three children: body, redo and exit.
			
			Iterator<Node> it = node.getChildren().iterator();
			//body
			org.processmining.processtree.Node bodyChild = convertNode(tree, it.next());
			bodyChild.setProcessTree(tree);
			tree.addNode(bodyChild);
			Edge edge = newBlock.addChild(bodyChild);
			bodyChild.addIncomingEdge(edge);
			tree.addEdge(edge);
			
			//redo
			if (node.getChildren().size() > 2) {
				org.processmining.processtree.Node redoChild = new AbstractBlock.Xor("");
				redoChild.setProcessTree(tree);
				tree.addNode(redoChild);
				Edge edge2 = newBlock.addChild(redoChild);
				redoChild.addIncomingEdge(edge2);
				tree.addEdge(edge2);
				Block newXorBlock = (Block) redoChild;
				while (it.hasNext()) {
					org.processmining.processtree.Node redoXorChild = convertNode(tree, it.next());
					redoXorChild.setProcessTree(tree);
					tree.addNode(redoXorChild);
					Edge edgeXor = newXorBlock.addChild(redoXorChild);
					redoXorChild.addIncomingEdge(edgeXor);
					tree.addEdge(edgeXor);
				}
			} else {
				//don't add xor node if not needed
				org.processmining.processtree.Node redoChild = convertNode(tree, it.next());
				redoChild.setProcessTree(tree);
				tree.addNode(redoChild);
				Edge edge2 = newBlock.addChild(redoChild);
				redoChild.addIncomingEdge(edge2);
				tree.addEdge(edge2);
			}
			
			//exit
			Automatic tau = new AbstractTask.Automatic("");
			tau.setProcessTree(tree);
			tree.addNode(tau);
			Edge edgeExit = newBlock.addChild(tau);
			tau.addIncomingEdge(edgeExit);
			tree.addEdge(edgeExit);
		} else {
			//And give it its children
			for (Node child : node.getChildren()) {
				org.processmining.processtree.Node newChild = convertNode(tree, child);
				//Connect new child to parent and connect the edges
				Edge edge = newBlock.addChild(newChild);
				newChild.addIncomingEdge(edge);
				tree.addEdge(edge);
			}
		}

		return newNode;
	}
	
	private static void attachFitness(Node node, org.processmining.processtree.Node newNode) {
		Double f = (Double) node.metadata.get("subtreeFitness");
		try {
			newNode.setIndependentProperty(new Fitness(), f);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	//private static void debug(String x) {System.out.println(x);}
	
}
