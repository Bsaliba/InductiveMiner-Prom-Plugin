package org.processmining.plugins.InductiveMiner.mining.interleaved;

import org.processmining.plugins.InductiveMiner.mining.Miner;
import org.processmining.plugins.InductiveMiner.mining.interleaved.FootPrint.DfgUnfoldedNode;
import org.processmining.processtree.Block;
import org.processmining.processtree.Block.Seq;
import org.processmining.processtree.Node;
import org.processmining.processtree.conversion.ProcessTree2Petrinet.UnfoldedNode;

public class DetectInterleaved {

	public static Block remove(MaybeInterleaved node) {
		return removeTwo(node);
	}

	public static Block removeTwo(MaybeInterleaved node) {
		//this method only works for n=2
		if (node.getChildren().size() != 2) {
			return node;
		}
		
		//check whether all children are sequences
		for (Node child : node.getChildren()) {
			if (!(child instanceof Seq)) {
				return node;
			}
		}

		//check whether footprints are equal
		UnfoldedNode grandChildA1 = new UnfoldedNode(((Block) node.getChildren().get(0)).getChildren().get(0));
		UnfoldedNode grandChildA2 = new UnfoldedNode(((Block) node.getChildren().get(0)).getChildren().get(1));
		UnfoldedNode grandChildB1 = new UnfoldedNode(((Block) node.getChildren().get(1)).getChildren().get(0));
		UnfoldedNode grandChildB2 = new UnfoldedNode(((Block) node.getChildren().get(1)).getChildren().get(1));

		DfgUnfoldedNode A1 = FootPrint.makeDfg(grandChildA1);
		DfgUnfoldedNode A2 = FootPrint.makeDfg(grandChildA2);
		DfgUnfoldedNode B1 = FootPrint.makeDfg(grandChildB1);
		DfgUnfoldedNode B2 = FootPrint.makeDfg(grandChildB2);

		if (!(A1.equals(B2) || A2.equals(B1))) {
			return node;
		}

		//just pick the first children
		Block newNode = new Interleaved("");
		Miner.addNode(node.getProcessTree(), newNode);

		for (Node grandChild : ((Block) node.getChildren().get(0)).getChildren()) {
			newNode.addChild(grandChild);
		}

		return newNode;
	}
}