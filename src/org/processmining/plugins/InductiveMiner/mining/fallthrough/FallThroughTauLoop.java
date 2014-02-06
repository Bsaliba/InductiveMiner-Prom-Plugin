package org.processmining.plugins.InductiveMiner.mining.fallthrough;



public class FallThroughTauLoop {
	/*Filteredlog tauloopSublog = tauLoop(log, parameters, target, index, directlyFollowsRelation,
			directlyFollowsRelationNoiseFiltered);
	if (tauloopSublog != null) {
		final Binoperator node = new Loop(2);
		target.setChild(index, node);
		node.metadata.put("numberOfEvents", new Integer(log.getNumberOfEvents()));
		node.metadata.put("numberOfTraces", new Integer(log.getNumberOfTraces()));

		Tau tau = new Tau();
		node.setChild(1, tau);

		debug("Chosen tau loop", parameters);

		recurse(parameters, pool, tauloopSublog, node, 0);
		return;
	}*/
	
	/*private Filteredlog tauLoop(Filteredlog log, final MiningParameters parameters, final Binoperator target,
			final int index, DirectlyFollowsRelation directlyFollowsRelation,
			DirectlyFollowsRelation directlyFollowsRelationNoiseFiltered) {

		DirectlyFollowsRelation dfr;
		if (parameters.getNoiseThreshold() != 0) {
			dfr = directlyFollowsRelationNoiseFiltered;
		} else {
			dfr = directlyFollowsRelation;
		}

		List<Set<XEventClass>> tauLoopCut = new LinkedList<Set<XEventClass>>();
		tauLoopCut.add(new HashSet<XEventClass>());
		tauLoopCut.get(0).addAll(directlyFollowsRelation.getDirectlyFollowsGraph().vertexSet());
		FilterResults filterResults = log.applyFilterTauLoop(tauLoopCut, dfr.getStartActivities().toSet(), dfr
				.getEndActivities().toSet());
		final Filteredlog sublog = ((List<Filteredlog>) filterResults.sublogs).get(0);

		if (sublog.getNumberOfTraces() > log.getNumberOfTraces()) {
			return sublog;
		}

		return null;
	}*/
}
