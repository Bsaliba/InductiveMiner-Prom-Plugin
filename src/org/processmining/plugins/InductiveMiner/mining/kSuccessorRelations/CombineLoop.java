package org.processmining.plugins.InductiveMiner.mining.kSuccessorRelations;

import org.deckfour.xes.classification.XEventClass;
import org.processmining.plugins.InductiveMiner.Sets;
import org.processmining.plugins.InductiveMiner.mining.kSuccessorRelations.UpToKSuccessorRelation.KSuccessorMatrix;

public class CombineLoop implements CombineUpToKSuccessors {

	public UpToKSuccessorRelation combine(KSuccessorMatrix A, KSuccessorMatrix B) {

		UpToKSuccessorRelation result = new UpToKSuccessorRelation(Sets.union(A.getActivities(), B.getActivities()));
		KSuccessorMatrix C = result.getkSuccessors();

		C.feedKSuccessor(null, null, A.getKSuccessor(null, null));

		//S-a and a-E are copied
		for (XEventClass a : A.getActivities()) {
			C.feedKSuccessor(null, a, A.getKSuccessor(null, a));
			C.feedKSuccessor(a, null, A.getKSuccessor(a, null));
		}

		//S-b and b-E need addition
		for (XEventClass b : B.getActivities()) {
			C.feedKSuccessor(null, b, A.getKSuccessor(null, null) + B.getKSuccessor(null, b) - 1);
			C.feedKSuccessor(b, null, B.getKSuccessor(b, null) + A.getKSuccessor(null, null) - 1);
		}

		//a -> b, b-> a
		for (XEventClass a : A.getActivities()) {
			for (XEventClass b : B.getActivities()) {
				C.feedKSuccessor(a, b, A.getKSuccessor(a, null) + B.getKSuccessor(null, b) - 1);
				C.feedKSuccessor(b, a, B.getKSuccessor(b, null) + A.getKSuccessor(null, a) - 1);
			}
		}

		//b -> b
		for (XEventClass b1 : B.getActivities()) {
			for (XEventClass b2 : B.getActivities()) {
				C.feedKSuccessor(
						b1,
						b2,
						min(B.getKSuccessor(b1, b2),
								B.getKSuccessor(b1, null) + A.getKSuccessor(null, null) + B.getKSuccessor(null, b2) - 2));
				C.feedKSuccessor(
						b2,
						b1,
						min(B.getKSuccessor(b2, b1),
								B.getKSuccessor(b2, null) + A.getKSuccessor(null, null) + B.getKSuccessor(null, b1) - 2));
			}
		}

		//a -> a
		for (XEventClass a1 : A.getActivities()) {
			for (XEventClass a2 : A.getActivities()) {
				C.feedKSuccessor(
						a1,
						a2,
						min(A.getKSuccessor(a1, a2),
								A.getKSuccessor(a1, null) + B.getKSuccessor(null, null) + A.getKSuccessor(null, a2) - 2));
				C.feedKSuccessor(
						a2,
						a1,
						min(A.getKSuccessor(a2, a1),
								A.getKSuccessor(a2, null) + B.getKSuccessor(null, null) + A.getKSuccessor(null, a1) - 2));
			}
		}

		return result;

	}

	private Integer min(Integer a, Integer b) {
		if (a == null) {
			return b;
		} else if (b == null) {
			return a;
		} else if (a < b) {
			return a;
		} else {
			return b;
		}
	}

}
