package org.deri.nettopo.algorithm.dvhop.function;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import org.deri.nettopo.algorithm.AlgorFunc;
import org.deri.nettopo.algorithm.Algorithm;
import org.deri.nettopo.app.NetTopoApp;
import org.deri.nettopo.network.WirelessSensorNetwork;
import org.deri.nettopo.node.VNode;
import org.deri.nettopo.node.localization.DVHopNode;

public class DVHOP_Test implements AlgorFunc {
	private Algorithm algorithm;
	private NetTopoApp app;
	WirelessSensorNetwork wsn;
	DVHOP_ConnectNeighbors connectNeighbors;

	public DVHOP_Test(Algorithm algorithm) {
		this.algorithm = algorithm;
		
	}
	public DVHOP_Test(){
		this.algorithm = null;
	}
	/********************************/
	
	@Override
	public void run() {
		connectNeighbors = new DVHOP_ConnectNeighbors(algorithm);
		app = NetTopoApp.getApp();
		wsn = app.getNetwork();
		connectNeighbors.run();
		Collection<VNode> sensorNodes = wsn.getNodes("org.deri.nettopo.node.localization.DVHopNode", true);
		Iterator<VNode>itAll=sensorNodes.iterator();
		while (itAll.hasNext()) {
			DVHopNode node = (DVHopNode) itAll.next();
			System.out.println("##For node id:"+node.getID());
			ArrayList<Integer> neighbors=node.getNeighbors();
			Iterator<Integer> it=neighbors.iterator();
			while (it.hasNext()) {
				Integer id = (Integer) it.next();
				System.out.println("neighbor id is:"+id);
			}
		}
			
	}

	public Algorithm getAlgorithm() {
		return algorithm;
	}

	public void setAlgorithm(Algorithm algorithm) {
		this.algorithm = algorithm;
	}
}
