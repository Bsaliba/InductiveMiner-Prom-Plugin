package org.processmining.plugins.InductiveMiner.dfgOnly.plugins;

import org.processmining.plugins.InductiveMiner.dfgOnly.Dfg;
import org.processmining.plugins.InductiveMiner.dfgOnly.DfgMiner;
import org.processmining.plugins.InductiveMiner.dfgOnly.DfgMiningParameters;
import org.processmining.processtree.ProcessTree;

public class IMdProcessTree {

	public static ProcessTree mineProcessTree(Dfg dfg, DfgMiningParameters parameters) {
		return DfgMiner.mine(dfg, parameters);
	}
}
