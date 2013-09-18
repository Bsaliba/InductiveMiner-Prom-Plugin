package org.processmining.plugins.InductiveMiner.mining.kSuccessorRelations;

import org.processmining.plugins.InductiveMiner.mining.kSuccessorRelations.UpToKSuccessorRelation.KSuccessorMatrix;


public interface CombineUpToKSuccessors {
	public UpToKSuccessorRelation combine(KSuccessorMatrix A, KSuccessorMatrix B);
}
