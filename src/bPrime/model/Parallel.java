package bPrime.model;


public class Parallel extends Binoperator{

	public Parallel(int countChildren) {
		super(countChildren);
	}

	public String getOperatorString() {
		return "/\\";
		//return "&Lambda;";
	}
	
	/*
	public boolean canProduceEpsilon() {
		boolean result = true;
		for (Node child : children) {
			result = result && child.canProduceEpsilon();
		}
		return result;
	}
	*/
}
