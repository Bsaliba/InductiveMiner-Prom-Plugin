package org.processmining.plugins.InductiveMiner.dfgOnly;

import org.processmining.plugins.InductiveMiner.dfgOnly.dfgBaseCaseFinder.DfgBaseCaseFinder;
import org.processmining.plugins.InductiveMiner.dfgOnly.dfgCutFinder.DfgCutFinder;
import org.processmining.plugins.InductiveMiner.dfgOnly.dfgFallThrough.DfgFallThrough;
import org.processmining.plugins.InductiveMiner.dfgOnly.dfgSplitter.DfgSplitter;

public abstract class DfgMiningParameters {

	private Iterable<DfgBaseCaseFinder> dfgBaseCaseFinders;
	private Iterable<DfgCutFinder> dfgCutFinders;
	private DfgSplitter dfgSplitter;
	private Iterable<DfgFallThrough> dfgFallThroughs;

	private boolean reduce;
	private boolean debug;

	public Iterable<DfgBaseCaseFinder> getDfgBaseCaseFinders() {
		return dfgBaseCaseFinders;
	}

	public void setDfgBaseCaseFinders(Iterable<DfgBaseCaseFinder> baseCaseFinders) {
		this.dfgBaseCaseFinders = baseCaseFinders;
	}

	public Iterable<DfgCutFinder> getDfgCutFinders() {
		return dfgCutFinders;
	}

	public void setDfgCutFinders(Iterable<DfgCutFinder> dfgCutFinders) {
		this.dfgCutFinders = dfgCutFinders;
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

}
