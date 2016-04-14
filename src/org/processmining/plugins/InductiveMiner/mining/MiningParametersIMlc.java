package org.processmining.plugins.InductiveMiner.mining;

import java.util.ArrayList;
import java.util.Arrays;

import org.processmining.plugins.InductiveMiner.dfgOnly.log2logInfo.IMLog2IMLogInfoLifeCycle;
import org.processmining.plugins.InductiveMiner.efficienttree.EfficientTreeReduceParameters;
import org.processmining.plugins.InductiveMiner.mining.baseCases.BaseCaseFinder;
import org.processmining.plugins.InductiveMiner.mining.baseCases.BaseCaseFinderIM;
import org.processmining.plugins.InductiveMiner.mining.cuts.CutFinder;
import org.processmining.plugins.InductiveMiner.mining.cuts.IMlc.CutFinderIMlc;
import org.processmining.plugins.InductiveMiner.mining.fallthrough.FallThrough;
import org.processmining.plugins.InductiveMiner.mining.fallthrough.FallThroughFlowerWithEpsilon;
import org.processmining.plugins.InductiveMiner.mining.fallthrough.FallThroughActivityOncePerTraceConcurrent;
import org.processmining.plugins.InductiveMiner.mining.fallthrough.FallThroughTauLoop;
import org.processmining.plugins.InductiveMiner.mining.logSplitter.LogSplitterIMi;
import org.processmining.plugins.InductiveMiner.mining.postprocessor.PostProcessor;
import org.processmining.plugins.InductiveMiner.mining.postprocessor.PostProcessorInterleaved;

public class MiningParametersIMlc extends MiningParameters {
	
	/*
	 * No other parameter, except mentioned in this file, has influence on the mined model
	 */
	
	public MiningParametersIMlc() {
		setRepairLifeCycle(true);
		
		setLog2LogInfo(new IMLog2IMLogInfoLifeCycle());
		
		setBaseCaseFinders(new ArrayList<BaseCaseFinder>(Arrays.asList(
				new BaseCaseFinderIM()
				)));
		
		setCutFinder(new ArrayList<CutFinder>(Arrays.asList(
				new CutFinderIMlc()
				)));
		
		setLogSplitter(new LogSplitterIMi());
		
		setFallThroughs(new ArrayList<FallThrough>(Arrays.asList(
				new FallThroughActivityOncePerTraceConcurrent(true),
				new FallThroughTauLoop(true),
				new FallThroughFlowerWithEpsilon()
				)));
		
		setPostProcessors(new ArrayList<PostProcessor>(Arrays.asList(
				new PostProcessorInterleaved()
				)));
		
		setReduceParameters(new EfficientTreeReduceParameters(true));
	}
}
