package org.processmining.plugins.InductiveMiner.mining.operators;

import java.util.List;
import java.util.UUID;

import org.processmining.processtree.Block;
import org.processmining.processtree.Edge;
import org.processmining.processtree.impl.AbstractBlock;

public class InterleavedXor extends AbstractBlock.Xor {
	public InterleavedXor(String name) {
		super(name);
	}

	public InterleavedXor(UUID id, String name) {
		super(id, name);
	}

	public InterleavedXor(String name, List<Edge> incoming, List<Edge> outgoing) {
		super(name, incoming, outgoing);
	}

	public InterleavedXor(UUID id, String name, List<Edge> incoming, List<Edge> outgoing) {
		super(id, name, incoming, outgoing);
	}

	public InterleavedXor(Block.Xor b) {
		super(b);
	}
	
	@Override
	public String toStringShort() {
		return "InX";
	}
}
