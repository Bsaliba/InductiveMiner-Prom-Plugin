package org.processmining.plugins.InductiveMiner.mining.kSuccessorRelations;

import org.deckfour.xes.classification.XEventClass;
import org.processmining.plugins.InductiveMiner.Sets;
import org.processmining.plugins.InductiveMiner.mining.kSuccessorRelations.UpToKSuccessorRelation.KSuccessorMatrix;


public class CombineParallel implements CombineUpToKSuccessors {

	public UpToKSuccessorRelation combine(KSuccessorMatrix A, KSuccessorMatrix B) {
		
		UpToKSuccessorRelation result = new UpToKSuccessorRelation(Sets.union(A.getActivities(), B.getActivities()));
		KSuccessorMatrix C = result.getkSuccessors();
		
		C.feedKSuccessor(null, null, A.getKSuccessor(null, null) + B.getKSuccessor(null, null) - 1);
		
		//inter-cells are all 1
		for (XEventClass a : A.getActivities()) {
			for (XEventClass b : B.getActivities()) {
				C.feedKSuccessor(a, b, 1);
				C.feedKSuccessor(b, a, 1);
			}
		}
		
		// S to .. and .. to E are copied
		for (XEventClass a : A.getActivities()) {
			C.feedKSuccessor(null, a, A.getKSuccessor(null, a));
			C.feedKSuccessor(a, null, A.getKSuccessor(a, null));
		}
		for (XEventClass b : B.getActivities()) {
			C.feedKSuccessor(null, b, B.getKSuccessor(null, b));
			C.feedKSuccessor(b, null, B.getKSuccessor(b, null));
		}
		
		//intra-cells are copied
		for (XEventClass a1 : A.getActivities()) {
			for (XEventClass a2 : A.getActivities()) {
				C.feedKSuccessor(a1, a2, A.getKSuccessor(a1, a2));
			}
		}
		for (XEventClass b1 : B.getActivities()) {
			for (XEventClass b2 : B.getActivities()) {
				C.feedKSuccessor(b1, b2, B.getKSuccessor(b1, b2));
			}
		}
		
		return result;
		
	}

}
