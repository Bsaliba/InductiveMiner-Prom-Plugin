package org.processmining.plugins.InductiveMiner.dfgOnly.dfgSplitter;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.deckfour.xes.classification.XEventClass;
import org.processmining.plugins.InductiveMiner.dfgOnly.Dfg;
import org.processmining.plugins.InductiveMiner.dfgOnly.DfgMinerState;
import org.processmining.plugins.InductiveMiner.mining.cuts.Cut;
import org.processmining.plugins.InductiveMiner.mining.cuts.Cut.Operator;

public class SimpleDfgSplitter implements DfgSplitter {

	public DfgSplitResult split(Dfg dfg, Cut cut, DfgMinerState minerState) {
		List<Dfg> subDfgs = new ArrayList<>();
		for (Set<XEventClass> sigma : cut.getPartition()) {
			Dfg subDfg = new Dfg();
			subDfgs.add(subDfg);

			//walk through the nodes
			for (XEventClass activity : sigma) {
				subDfg.getDirectlyFollowsGraph().addVertex(activity);

				if (dfg.getStartActivities().contains(activity)) {
					subDfg.getStartActivities().add(activity, dfg.getStartActivities().getCardinalityOf(activity));
				}
				if (dfg.getEndActivities().contains(activity)) {
					subDfg.getEndActivities().add(activity, dfg.getEndActivities().getCardinalityOf(activity));
				}
			}

			//walk through the edges
			{
				//directly-follows graph
				for (long edge : dfg.getDirectlyFollowsGraph().getEdges()) {
					int cardinality = (int) dfg.getDirectlyFollowsGraph().getEdgeWeight(edge);
					XEventClass source = dfg.getDirectlyFollowsGraph().getEdgeSource(edge);
					XEventClass target = dfg.getDirectlyFollowsGraph().getEdgeTarget(edge);

					if (sigma.contains(source) && sigma.contains(target)) {
						//internal edge in sigma
						subDfg.getDirectlyFollowsGraph().addEdge(source, target, cardinality);
					} else if (sigma.contains(source) && !sigma.contains(target)) {
						//edge going out of sigma
						if (cut.getOperator() == Operator.sequence || cut.getOperator() == Operator.loop) {
							//source is an end activity
							subDfg.getEndActivities().add(source, cardinality);
						}
					} else if (!sigma.contains(source) && sigma.contains(target)) {
						//edge going into sigma
						if (cut.getOperator() == Operator.sequence || cut.getOperator() == Operator.loop) {
							//target is a start activity
							subDfg.getStartActivities().add(target, cardinality);
						}
					} else {
						//edge unrelated to sigma
					}
				}
			}
		}

		return new DfgSplitResult(subDfgs);
	}
}
