package bPrime.model;


public class Sequence extends Binoperator {

	public Sequence(int countChildren) {
		super(countChildren);
	}

	public String getOperatorString() {
		//return "seq";
		return "->";
	}
	
}
