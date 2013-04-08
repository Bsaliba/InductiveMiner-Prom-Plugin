package bPrime.model;



public class Sequence extends Binoperator {

	public Sequence(int countChildren) {
		super(countChildren);
	}

	public String getOperatorString() {
		//return "seq";
		return "->";
	}

	/*
	public boolean canProduceEpsilon() {
		boolean result = true;
		for (Node child : children) {
			result = result && child.canProduceEpsilon();
		}
		return result;
	}
	
	public Set<Pair<XEventClass, XEventClass>> getDirectlyFollowsEdges() {
		Set<Pair<XEventClass, XEventClass>> result = new HashSet<Pair<XEventClass, XEventClass>>();
		for (Node child : children) {
			result.addAll(child.getDirectlyFollowsEdges());
			//Node child2 TODO 
		}
		
		
		
		return result;
	}
	*/
}
