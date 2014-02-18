package org.processmining.plugins.InductiveMiner.mining.fallthrough;

import org.processmining.contexts.cli.CLIContext;
import org.processmining.contexts.cli.CLIPluginContext;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.plugins.InductiveMiner.mining.IMLog;
import org.processmining.plugins.InductiveMiner.mining.IMLogInfo;
import org.processmining.plugins.InductiveMiner.mining.MiningParameters;
import org.processmining.processtree.Node;
import org.processmining.processtree.ProcessTree;

public class FallThroughETM implements FallThrough {

	private static PluginContext context = new CLIPluginContext(new CLIContext(), "");
	
	public Node fallThrough(IMLog log, IMLogInfo logInfo, ProcessTree tree, MiningParameters parameters) {
		/*
		ETMParam etmParameters = ETMParamFactory.buildStandardParam(log.toXLog(), context);
		
		//set maximum duration
		ETMParamFactory.removeTerminationConditionIfExists(etmParameters, ElapsedTime.class);
		etmParameters.addTerminationCondition(new ElapsedTime(2000));
		
		ETM miner = new org.processmining.plugins.etm.ETM(etmParameters);
		miner.run();
		
		NAryTree internalTree = miner.getResult();
		
		ProcessTree tree1 = NAryTreeToProcessTree.convert(internalTree);
		*/
		
		return null;
	}

}
