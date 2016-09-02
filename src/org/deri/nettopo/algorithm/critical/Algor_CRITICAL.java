package org.deri.nettopo.algorithm.critical;

import org.deri.nettopo.algorithm.AlgorFunc;
import org.deri.nettopo.algorithm.Algorithm;
import org.deri.nettopo.algorithm.critical.function.CRITICAL_Judge;

public class Algor_CRITICAL implements Algorithm {

	AlgorFunc[] functions ;
	public Algor_CRITICAL() {
		functions = new AlgorFunc[1];
		functions[0]=new CRITICAL_Judge(this);

	}
	@Override
	public AlgorFunc[] getFunctions() {
		// TODO Auto-generated method stub
		return functions;
	}
	

}
