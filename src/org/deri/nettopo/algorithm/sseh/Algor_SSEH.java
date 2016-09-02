package org.deri.nettopo.algorithm.sseh;

import org.deri.nettopo.algorithm.AlgorFunc;
import org.deri.nettopo.algorithm.Algorithm;
import org.deri.nettopo.algorithm.sseh.function.SSEH_ConnectNeighbors;
import org.deri.nettopo.algorithm.sseh.function.SSEH_RunAlgorithm;
import org.deri.nettopo.algorithm.sseh.function.SSEH_SetEsValue;
import org.deri.nettopo.algorithm.sseh.function.SSEH_SetDegreeTarget;
import org.deri.nettopo.algorithm.sseh.function.SSEH_Test;

public class Algor_SSEH implements Algorithm {
	AlgorFunc[] functions ;
	public Algor_SSEH() {
		functions = new AlgorFunc[5];
		functions[0]=new SSEH_ConnectNeighbors(this);
		functions[1]=new SSEH_RunAlgorithm(this);
		functions[2]=new SSEH_SetEsValue(this);
		functions[3]=new SSEH_SetDegreeTarget(this);
		functions[4]=new SSEH_Test(this);
		
	}
	@Override
	public AlgorFunc[] getFunctions() {
		
		return functions;
	}

	
}
