package org.deri.nettopo.algorithm.dvhop.function;

import java.util.Collection;

import org.deri.nettopo.algorithm.AlgorFunc;
import org.deri.nettopo.algorithm.Algorithm;
import org.deri.nettopo.app.NetTopoApp;
import org.deri.nettopo.network.WirelessSensorNetwork;
import org.deri.nettopo.node.VNode;

public class DVHOP_Basic implements AlgorFunc {
	private Algorithm algorithm;
	private NetTopoApp app;
	private WirelessSensorNetwork wsn;
	private DVHOP_ConnectNeighbors connectNeighbors;
	private Collection<VNode> allNodesCollection;
	public DVHOP_Basic(Algorithm algorithm) {
		this.algorithm=algorithm;
	}
	
	/****************function in this algorithm****************/
	
	@Override
	public void run() {
		/*********************** Initialize*************************/
		connectNeighbors = new DVHOP_ConnectNeighbors(algorithm);
		app = NetTopoApp.getApp();
		wsn = app.getNetwork();
		allNodesCollection = wsn.getNodes("org.deri.nettopo.node.localization.DVHopNode", true);
		/*****************first step:connect neighbors*********************/
		connectNeighbors.run();
		
		
	}

	public Algorithm getAlgorithm() {
		return algorithm;
	}

	public void setAlgorithm(Algorithm algorithm) {
		this.algorithm = algorithm;
	}
}
