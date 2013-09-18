package org.processmining.plugins.InductiveMiner.mining.kSuccessorRelations;

import org.deckfour.xes.classification.XEventClass;
import org.processmining.plugins.InductiveMiner.mining.kSuccessorRelations.UpToKSuccessorRelation.KSuccessorMatrix;

public class DistanceEuclidian implements Distance {

	public int computeDistance(KSuccessorMatrix A, KSuccessorMatrix B) {

		if (!A.getActivities().equals(B.getActivities())) {
			return -1;
		}

		int sum = 0;

		//normal cells
		for (XEventClass a1 : A.getActivities()) {
			for (XEventClass a2 : A.getActivities()) {
				if (A.getKSuccessor(a1, a2) != null && B.getKSuccessor(a1, a2) != null) {
					sum += Math.pow(A.getKSuccessor(a1, a2) - B.getKSuccessor(a1, a2), 2);
				} else if (A.getKSuccessor(a1, a2) != B.getKSuccessor(a1, a2)) {
					sum += 10;
				}
			}
		}
		
		//begin, end cells
		for (XEventClass a : A.getActivities()) {
			sum += Math.pow(A.getKSuccessor(null, a) - B.getKSuccessor(null, a), 2);
			sum += Math.pow(A.getKSuccessor(a, null) - B.getKSuccessor(a, null), 2);
		}
		
		return sum;

	}

}
