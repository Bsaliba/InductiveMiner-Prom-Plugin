package org.processmining.plugins.InductiveMiner.mining.kSuccessorRelations;

import org.processmining.plugins.InductiveMiner.mining.kSuccessorRelations.UpToKSuccessorRelation.KSuccessorMatrix;

public interface Distance {
	public int computeDistance(KSuccessorMatrix A, KSuccessorMatrix B);
}
