package org.processmining.plugins.InductiveMiner.conversion;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.processmining.contexts.uitopia.annotations.UITopiaVariant;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginVariant;
import org.processmining.plugins.InductiveMiner.Pair;
import org.processmining.plugins.InductiveMiner.mining.metrics.MinerMetrics;
import org.processmining.plugins.InductiveMiner.mining.metrics.PropertyNumberOfTracesRepresented;
import org.processmining.processtree.Block;
import org.processmining.processtree.Edge;
import org.processmining.processtree.Node;
import org.processmining.processtree.Originator;
import org.processmining.processtree.ProcessTree;
import org.processmining.processtree.Task;
import org.processmining.processtree.impl.AbstractBlock;
import org.processmining.processtree.impl.AbstractBlock.And;
import org.processmining.processtree.impl.AbstractBlock.Seq;
import org.processmining.processtree.impl.AbstractBlock.Xor;
import org.processmining.processtree.impl.AbstractBlock.XorLoop;
import org.processmining.processtree.impl.AbstractTask.Automatic;
import org.processmining.processtree.impl.ProcessTreeImpl;



@Plugin(name = "Reduce process tree language-equivalently", returnLabels = { "Process Tree" }, returnTypes = { ProcessTree.class }, parameterLabels = { "Process Tree" }, userAccessible = true)
public class ReduceTree {

	@UITopiaVariant(affiliation = UITopiaVariant.EHV, author = "S.J.J. Leemans", email = "s.j.j.leemans@tue.nl")
	@PluginVariant(variantLabel = "Reduce Process Tree Language-equivalently, default", requiredParameterLabels = { 0 })
	public ProcessTree reduceTree(PluginContext context, ProcessTree tree) {
		return reduceTree(tree);
	}
	
	public static ProcessTree reduceTree(ProcessTree tree) {
		ProcessTree newTree = new ProcessTreeImpl();
		Node root = tree.getRoot();
		newTree.setRoot(reduceNode(root, newTree));

		return newTree;
	}

	private static Node reduceNode(Node node, ProcessTree newTree) {
		
		Node newNode = null;
		List<Node> children = null;
		if (node instanceof Seq) {
			newNode = new Seq("");
			children = flattenSequence((Seq) node);
		} else if (node instanceof Xor || node instanceof DummyXor) {
			newNode = new Xor("");
			children = flattenXor((AbstractBlock) node);
		} else if (node instanceof XorLoop) {
			newNode = new XorLoop("");
			Pair<Node, Set<Node>> p = flattenLoop((XorLoop) node);
			
			//construct a children array
			children = new LinkedList<Node>();
			children.add(p.getLeft());
			if (p.getRight().size() == 1) {
				//if only one right-side child remains, add it
				children.add(p.getRight().iterator().next());
			} else {
				//else, add all children as an xor
				DummyXor middleChild = new DummyXor("dummy");
				for (Node grandChild : p.getRight()) {
					middleChild.addChild(grandChild);
				}
				children.add(middleChild);
			}
			children.add(((XorLoop) node).getChildren().get(2));
		} else if (node instanceof And) {
			newNode = new And("");
			children = flattenParallel((And) node);
		} else {

			//copy the node
			Class<? extends Node> c = node.getClass();

			try {
				Constructor<? extends Node> ctor = c.getConstructor(String.class);
				newNode = ctor.newInstance(new Object[] { node.getName() });
			} catch (NoSuchMethodException e) {
				try {
					Constructor<? extends Node> ctor = c.getConstructor(String.class, Originator[].class);
					newNode = ctor.newInstance(new Object[] { node.getName(), new Originator[0] });
				} catch (NoSuchMethodException e1) {
					e1.printStackTrace();
				} catch (SecurityException e1) {
					e1.printStackTrace();
				} catch (InstantiationException e1) {
					e1.printStackTrace();
				} catch (IllegalAccessException e1) {
					e1.printStackTrace();
				} catch (IllegalArgumentException e1) {
					e1.printStackTrace();
				} catch (InvocationTargetException e1) {
					e1.printStackTrace();
				}
			} catch (SecurityException e) {
				e.printStackTrace();
			} catch (InstantiationException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			} catch (InvocationTargetException e) {
				e.printStackTrace();
			}

			if (newNode instanceof Block) {
				children = ((Block) node).getChildren();
			}
		}
		
		newNode.setProcessTree(newTree);
		newTree.addNode(newNode);
		
		//copy statistics
		if (getStatistics(node) != null) {
			MinerMetrics.attachStatistics(newNode, getStatistics(node));	
		}

		if (newNode instanceof Block) {
			if (children.size() >= 2) {
				for (Node child : children) {
					Node newChild = reduceNode(child, newTree);
					connectChildToParent(newChild, newNode, newTree);
				}
			} else {
				newNode = reduceNode(children.get(0), newTree);
			}
		}

		return newNode;
	}

	private static List<Node> flattenSequence(Seq node) {
		List<Node> result = new LinkedList<Node>();
		for (Node child : node.getChildren()) {
			if (child instanceof Seq) {
				//this child is a sequence-child of a sequence
				result.addAll(flattenSequence((Seq) child));
			} else {
				result.add(child);
			}
		}
		return result;
	}

	private static List<Node> flattenXor(AbstractBlock node) {
		List<Node> result = new LinkedList<Node>();
		for (Node child : node.getChildren()) {
			if (child instanceof Xor) {
				//this child is an xor-child of an xor
				result.addAll(flattenXor((Xor) child));
			} else {
				result.add(child);
			}
		}
		
		//remove superfluous taus
		//if one of the children can produce tau, then no explicit tau is necessary
		boolean oneOfChildrenCanProduceTau = false;
		Node tauRemoved = null;
		Iterator<Node> it = result.iterator();
		while(it.hasNext()) {
			Node child = it.next();
			if (child instanceof Task.Automatic) {
				tauRemoved = child;
				it.remove();
			} else {
				oneOfChildrenCanProduceTau = oneOfChildrenCanProduceTau || canProduceTau(child);
			}
		}
		if (tauRemoved != null && !oneOfChildrenCanProduceTau) {
			result.add(tauRemoved);
		}
		
		return result;
	}
	
	private static Pair<Node, Set<Node>> flattenLoop(XorLoop node) {
		Node leftChild = node.getChildren().get(0);
		Node middleChild = node.getChildren().get(1);
		Set<Node> result = new HashSet<Node>();
		
		//this reduction only works on loops with an empty third branch
		if (!(node.getChildren().get(2) instanceof Automatic)) {
			result.add(middleChild);
			return new Pair<Node, Set<Node>>(leftChild, result);
		}
		
		if (leftChild instanceof XorLoop) {
			
			Pair<Node, Set<Node>> p = flattenLoop((XorLoop) leftChild);
			
			result.addAll(p.getRight());
			if (middleChild instanceof Xor) {
				result.addAll(flattenXor((Xor) middleChild));
			} else {
				result.add(middleChild);
			}
			return new Pair<Node, Set<Node>>(p.getLeft(), result);
		} else {
			if (middleChild instanceof Xor) {
				result.addAll(flattenXor((Xor) middleChild));
			} else {
				result.add(middleChild);
			}
			return new Pair<Node, Set<Node>>(leftChild, result);
		}
	}

	private static List<Node> flattenParallel(And node) {
		List<Node> result = new LinkedList<Node>();
		for (Node child : node.getChildren()) {
			if (child instanceof And) {
				//this child is an xor-child of an xor
				result.addAll(flattenParallel((And) child));
			} else {
				result.add(child);
			}
		}
		return result;
	}

	private static void connectChildToParent(Node child, Node parent, ProcessTree newTree) {
		Edge edge = ((Block) parent).addChild(child);
		child.addIncomingEdge(edge);
		newTree.addEdge(edge);
	}	
	
	private static class DummyXor extends AbstractBlock {
		
		private List<Node> children2;

		public DummyXor(String name) {
			super(name);
			children2 = new LinkedList<Node>();
		}
		
		public List<Node> getChildren() {
			return children2;
		}
		
		public Edge addChild(Node child) {
			children2.add(child);
			return null;
		}

		public boolean orderingOfChildernMatters() {
			return false;
		}

		public boolean expressionsOfOutgoingEdgesMatter() {
			return false;
		}

		public String toStringShort() {
			return null;
		}
	}
	
	public static boolean canProduceTau(Node node) {
		if (node instanceof Task.Automatic) {
			return true;
		} else if (node instanceof Task.Manual) {
			return false;
		} else if (node instanceof Seq || node instanceof And) {
			for (Node child : ((Block) node).getChildren()) {
				if (!canProduceTau(child)) {
					return false;
				}
			}
			return true;
		} else if (node instanceof Xor) {
			for (Node child : ((Block) node).getChildren()) {
				if (canProduceTau(child)) {
					return true;
				}
			}
			return false;
		} else if (node instanceof XorLoop) {
			return canProduceTau(((XorLoop) node).getChildren().get(0));
		}
		return false;
	}
	
	public static Integer getStatistics(Node node) {
		PropertyNumberOfTracesRepresented property1 = new PropertyNumberOfTracesRepresented();
		try {
			return (Integer) node.getIndependentProperty(property1);
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
		return null;
	}
}
