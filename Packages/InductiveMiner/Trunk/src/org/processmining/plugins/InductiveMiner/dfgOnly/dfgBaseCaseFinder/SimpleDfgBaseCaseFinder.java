package org.processmining.plugins.InductiveMiner.dfgOnly.dfgBaseCaseFinder;

import org.deckfour.xes.classification.XEventClass;
import org.processmining.plugins.InductiveMiner.dfgOnly.Dfg;
import org.processmining.plugins.InductiveMiner.dfgOnly.DfgMiner;
import org.processmining.plugins.InductiveMiner.dfgOnly.DfgMinerState;
import org.processmining.plugins.InductiveMiner.dfgOnly.dfgFallThrough.DfgFallThroughFlower;
import org.processmining.processtree.Node;
import org.processmining.processtree.ProcessTree;
import org.processmining.processtree.impl.AbstractTask;

public class SimpleDfgBaseCaseFinder implements DfgBaseCaseFinder {

	private static DfgFallThroughFlower flower = new DfgFallThroughFlower();
	
	public Node findBaseCases(Dfg dfg, ProcessTree tree, DfgMinerState minerState) {
		if (dfg.getDirectlyFollowsGraph().getNumberOfVertices() == 0) {
			//no activities (should not happen)
			Node node = new AbstractTask.Automatic("tau empty log");
			DfgMiner.addNode(tree, node);
			return node;
			
		} else if (dfg.getDirectlyFollowsGraph().getNumberOfVertices() == 1) {
			//single activity
			
			if (dfg.getDirectlyFollowsGraph().getWeightOfHeaviestEdge() < 1) {
				//no self-edges present: single activity
				XEventClass activity = dfg.getDirectlyFollowsGraph().getVertices()[0];
				Node node = new AbstractTask.Manual(activity.toString());
				DfgMiner.addNode(tree, node);
				return node;
			} else {
				//edges present, must be a self-edge
				
				//let fail to flower loop
				return flower.fallThrough(dfg, tree, minerState);
			}
			
		}

		return null;
	}

}
