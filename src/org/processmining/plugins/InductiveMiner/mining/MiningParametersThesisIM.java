package org.processmining.plugins.InductiveMiner.mining;

import java.util.ArrayList;
import java.util.Arrays;

import org.processmining.plugins.InductiveMiner.dfgOnly.log2logInfo.IMLog2IMLogInfoDefault;
import org.processmining.plugins.InductiveMiner.mining.baseCases.BaseCaseFinder;
import org.processmining.plugins.InductiveMiner.mining.baseCases.BaseCaseFinderIM;
import org.processmining.plugins.InductiveMiner.mining.cuts.CutFinder;
import org.processmining.plugins.InductiveMiner.mining.cuts.IM.CutFinderIMExclusiveChoice;
import org.processmining.plugins.InductiveMiner.mining.cuts.IM.CutFinderIMInterleaved;
import org.processmining.plugins.InductiveMiner.mining.cuts.IM.CutFinderIMLoop;
import org.processmining.plugins.InductiveMiner.mining.cuts.IM.CutFinderIMParallelWithMinimumSelfDistance;
import org.processmining.plugins.InductiveMiner.mining.cuts.IM.CutFinderIMSequence;
import org.processmining.plugins.InductiveMiner.mining.fallthrough.FallThrough;
import org.processmining.plugins.InductiveMiner.mining.fallthrough.FallThroughFlower;
import org.processmining.plugins.InductiveMiner.mining.fallthrough.FallThroughSingleParallel;
import org.processmining.plugins.InductiveMiner.mining.fallthrough.FallThroughTauLoop;
import org.processmining.plugins.InductiveMiner.mining.logSplitter.LogSplitterIMi;
import org.processmining.plugins.InductiveMiner.mining.postprocessor.PostProcessor;
import org.processmining.plugins.InductiveMiner.mining.postprocessor.PostProcessorInterleaved;

public class MiningParametersThesisIM extends MiningParameters {
	
	public MiningParametersThesisIM() {
	setLog2LogInfo(new IMLog2IMLogInfoDefault());
	
		setBaseCaseFinders(new ArrayList<BaseCaseFinder>(Arrays.asList(
				new BaseCaseFinderIM()
				)));
		
		setCutFinder(new ArrayList<CutFinder>(Arrays.asList(
				new CutFinderIMExclusiveChoice(),
				new CutFinderIMSequence(),
				new CutFinderIMInterleaved(),
				new CutFinderIMParallelWithMinimumSelfDistance(),
				new CutFinderIMLoop()
				)));
		
		setLogSplitter(new LogSplitterIMi());
		
		setFallThroughs(new ArrayList<FallThrough>(Arrays.asList(
				new FallThroughSingleParallel(true),
				new FallThroughTauLoop(false),
				new FallThroughFlower()
				)));
		
		setPostProcessors(new ArrayList<PostProcessor>(Arrays.asList(
				new PostProcessorInterleaved()
				)));
	}
}
