package org.processmining.plugins.InductiveMiner.dfgOnly;

import java.util.concurrent.ExecutorService;

import org.processmining.plugins.InductiveMiner.dfgOnly.dfgBaseCaseFinder.DfgBaseCaseFinder;
import org.processmining.plugins.InductiveMiner.dfgOnly.dfgCutFinder.DfgCutFinder;
import org.processmining.plugins.InductiveMiner.dfgOnly.dfgFallThrough.DfgFallThrough;
import org.processmining.plugins.InductiveMiner.dfgOnly.dfgSplitter.DfgSplitter;
import org.processmining.plugins.InductiveMiner.jobList.ThreadPoolSingleton1;
import org.processmining.plugins.InductiveMiner.mining.cuts.IMin.probabilities.Probabilities;

import com.google.common.util.concurrent.MoreExecutors;

public abstract class DfgMiningParameters {

	private Iterable<DfgBaseCaseFinder> dfgBaseCaseFinders;
	private DfgCutFinder dfgCutFinder;
	private DfgSplitter dfgSplitter;
	private Iterable<DfgFallThrough> dfgFallThroughs;

	private boolean reduce;
	private boolean debug;

	private float noiseThreshold = 0.2f;
	private Probabilities satProbabilities = null;
	private float incompleteThreshold = 0;
	private ExecutorService satPool = null;

	public DfgMiningParameters() {
		setUseMultithreading(true);
	}

	public void setUseMultithreading(boolean useMultithreading) {
		if (!useMultithreading) {
			satPool = MoreExecutors.sameThreadExecutor();
		} else {
			satPool = ThreadPoolSingleton1.getInstance();
		}
	}

	public Iterable<DfgBaseCaseFinder> getDfgBaseCaseFinders() {
		return dfgBaseCaseFinders;
	}

	public void setDfgBaseCaseFinders(Iterable<DfgBaseCaseFinder> baseCaseFinders) {
		this.dfgBaseCaseFinders = baseCaseFinders;
	}

	public DfgCutFinder getDfgCutFinder() {
		return dfgCutFinder;
	}

	public void setDfgCutFinder(DfgCutFinder dfgCutFinder) {
		this.dfgCutFinder = dfgCutFinder;
	}

	public DfgSplitter getDfgSplitter() {
		return dfgSplitter;
	}

	public void setDfgSplitter(DfgSplitter dfgSplitter) {
		this.dfgSplitter = dfgSplitter;
	}

	public Iterable<DfgFallThrough> getDfgFallThroughs() {
		return dfgFallThroughs;
	}

	public void setDfgFallThroughs(Iterable<DfgFallThrough> dfgFallThroughs) {
		this.dfgFallThroughs = dfgFallThroughs;
	}

	public boolean isReduce() {
		return reduce;
	}

	public void setReduce(boolean reduce) {
		this.reduce = reduce;
	}

	public boolean isDebug() {
		return debug;
	}

	public void setDebug(boolean debug) {
		this.debug = debug;
	}

	public float getNoiseThreshold() {
		return noiseThreshold;
	}

	public void setNoiseThreshold(float noiseThreshold) {
		this.noiseThreshold = noiseThreshold;
	}

	public Probabilities getSatProbabilities() {
		return this.satProbabilities;
	}

	public void setSatProbabilities(Probabilities satProbabilities) {
		this.satProbabilities = satProbabilities;
	}

	public float getIncompleteThreshold() {
		return this.incompleteThreshold;
	}
	
	public void setIncompleteThreshold(float incompleteThreshold) {
		this.incompleteThreshold = incompleteThreshold;
	}

	public ExecutorService getSatPool() {
		return this.satPool;
	}

}
