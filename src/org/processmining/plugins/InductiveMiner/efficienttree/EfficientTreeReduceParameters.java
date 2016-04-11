package org.processmining.plugins.InductiveMiner.efficienttree;

import org.processmining.plugins.InductiveMiner.efficienttree.reductionrules.IntShortLanguage;
import org.processmining.plugins.InductiveMiner.efficienttree.reductionrules.LoopATauTau2flower;
import org.processmining.plugins.InductiveMiner.efficienttree.reductionrules.LoopTauATau2flower;
import org.processmining.plugins.InductiveMiner.efficienttree.reductionrules.SameOperator;
import org.processmining.plugins.InductiveMiner.efficienttree.reductionrules.SingleChild;
import org.processmining.plugins.InductiveMiner.efficienttree.reductionrules.TauChildOfSeqAnd;
import org.processmining.plugins.InductiveMiner.efficienttree.reductionrules.XorTauTau;
import org.processmining.plugins.InductiveMiner.efficienttree.reductionrules.XorTauTauLoop2flower;

public class EfficientTreeReduceParameters {

	private boolean collapsed;

	/**
	 * 
	 * @param collapsed
	 *            Denotes what a leaf A means: true = seq(xor(tau, A)), false =
	 *            A.
	 */
	public EfficientTreeReduceParameters(boolean collapsed) {
		setCollapsed(collapsed);
	}

	public boolean isCollapsed() {
		return collapsed;
	}

	public void setCollapsed(boolean collapsed) {
		this.collapsed = collapsed;
	}

	public EfficientTreeReductionRule[] rulesXor = new EfficientTreeReductionRule[] { new SingleChild(),
			new XorTauTau(), new SameOperator(), new XorTauTauLoop2flower() };
	public EfficientTreeReductionRule[] rulesSeq = new EfficientTreeReductionRule[] { new SingleChild(),
			new TauChildOfSeqAnd(), new SameOperator() };
	public EfficientTreeReductionRule[] rulesAnd = new EfficientTreeReductionRule[] { new SingleChild(),
			new TauChildOfSeqAnd(), new SameOperator() };
	public EfficientTreeReductionRule[] rulesLoop = new EfficientTreeReductionRule[] { new LoopATauTau2flower(),
			new LoopTauATau2flower() };
	public EfficientTreeReductionRule[] rulesIntCollapsed = new EfficientTreeReductionRule[] { new SingleChild(),
			new TauChildOfSeqAnd(), new SameOperator() };
	public EfficientTreeReductionRule[] rulesIntExpanded = new EfficientTreeReductionRule[] { new SingleChild(),
			new TauChildOfSeqAnd(), new SameOperator(), new IntShortLanguage() };

	public EfficientTreeReductionRule[] getRulesXor() {
		return rulesXor;
	}

	public EfficientTreeReductionRule[] getRulesSequence() {
		return rulesSeq;
	}

	public EfficientTreeReductionRule[] getRulesLoop() {
		return rulesLoop;
	}

	public EfficientTreeReductionRule[] getRulesConcurrent() {
		return rulesAnd;
	}

	public EfficientTreeReductionRule[] getRulesInterleaved() {
		if (collapsed) {
			return rulesIntCollapsed;
		} else {
			return rulesIntExpanded;
		}
	}
}
