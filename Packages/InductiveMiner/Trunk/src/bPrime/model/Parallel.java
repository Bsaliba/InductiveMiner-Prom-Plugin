package bPrime.model;


public class Parallel extends Binoperator{

	public Parallel(int countChildren) {
		super(countChildren);
	}

	public String getOperatorString() {
		return "&Lambda;";
	}
}
