package org.deri.nettopo.algorithm.dvhop;

import java.util.Hashtable;

import org.deri.nettopo.algorithm.AlgorFunc;
import org.deri.nettopo.algorithm.Algorithm;
import org.deri.nettopo.algorithm.dvhop.function.DVHOP_Basic;
import org.deri.nettopo.algorithm.dvhop.function.DVHOP_ConnectNeighbors;
import org.deri.nettopo.algorithm.dvhop.function.DVHOP_Run_DVHop;
import org.deri.nettopo.algorithm.dvhop.function.DVHOP_SetAsymmetryPercentage;
import org.deri.nettopo.algorithm.dvhop.function.DVHOP_Test;
import org.deri.nettopo.node.localization.DVHopNode;


public class Algor_DVHOP implements Algorithm{

	AlgorFunc[] functions ;
	private int percentage=0;
	DVHopNode[] nodes;
	Hashtable<Integer, Integer> nodeIndex=new Hashtable<Integer,Integer>();
	public Algor_DVHOP(){
		functions = new AlgorFunc[5];
		functions[0] = new DVHOP_SetAsymmetryPercentage(this);
		functions[1] = new DVHOP_ConnectNeighbors(this);
		functions[2] = new DVHOP_Run_DVHop(this);
		functions[3] = new DVHOP_Basic(this);
		functions[4] = new DVHOP_Test(this);
		
	}
	
	//@Override
	@Override
	public AlgorFunc[] getFunctions() {
		return functions;
	}
	
	public void setPercentage(int per){
		this.percentage=per;
		/*for debug
		System.out.println("percentage has been set to "+this.percentage);*/
	}
	public int getPercentage(){
		return this.percentage;
	}

	public Hashtable<Integer, Integer> getNodeIndex() {
		return nodeIndex;
	}

	public void setNodeIndex(Hashtable<Integer, Integer> nodeIndex) {
		this.nodeIndex = nodeIndex;
	}

	public DVHopNode[] getNodes() {
		return nodes;
	}

	public void setNodes(DVHopNode[] nodes) {
		this.nodes = nodes;
	}
	
	

}
