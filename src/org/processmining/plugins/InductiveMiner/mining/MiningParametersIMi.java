package org.processmining.plugins.InductiveMiner.mining;

import java.util.Arrays;
import java.util.LinkedList;

import org.processmining.plugins.InductiveMiner.mining.baseCases.BaseCaseFinder;
import org.processmining.plugins.InductiveMiner.mining.baseCases.BaseCaseFinderIMiEmptyLog;
import org.processmining.plugins.InductiveMiner.mining.baseCases.BaseCaseFinderIMiEmptyTrace;
import org.processmining.plugins.InductiveMiner.mining.baseCases.BaseCaseFinderIMiSingleActivity;
import org.processmining.plugins.InductiveMiner.mining.cuts.CutFinder;
import org.processmining.plugins.InductiveMiner.mining.cuts.IM.CutFinderIM;
import org.processmining.plugins.InductiveMiner.mining.cuts.IMi.CutFinderIMi;
import org.processmining.plugins.InductiveMiner.mining.fallthrough.FallThrough;
import org.processmining.plugins.InductiveMiner.mining.fallthrough.FallThroughFlower;
import org.processmining.plugins.InductiveMiner.mining.fallthrough.FallThroughTauLoop;
import org.processmining.plugins.InductiveMiner.mining.logSplitter.LogSplitterIMi;

public class MiningParametersIMi extends MiningParameters {

	/*
	 * No other parameter, except mentioned in this file, has influence on mined model
	 */
	
	public MiningParametersIMi() {
		//determine algorithm
		
		setBaseCaseFinders(new LinkedList<BaseCaseFinder>(Arrays.asList(
				new BaseCaseFinderIMiEmptyLog(),
				new BaseCaseFinderIMiEmptyTrace(),
				new BaseCaseFinderIMiSingleActivity()
				)));
		
		setCutFinder(new LinkedList<CutFinder>(Arrays.asList(
				new CutFinderIM(),
				new CutFinderIMi()
				)));
		
		setLogSplitter(new LogSplitterIMi());
		
		setFallThroughs(new LinkedList<FallThrough>(Arrays.asList(
				new FallThroughTauLoop(),
				new FallThroughFlower()
				)));
		
		//set parameters
		setNoiseThreshold((float) 0.2);
	}
}
