package org.deri.nettopo.algorithm.critical.function;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;

import org.deri.nettopo.algorithm.AlgorFunc;
import org.deri.nettopo.algorithm.Algorithm;
import org.deri.nettopo.app.NetTopoApp;
import org.deri.nettopo.network.WirelessSensorNetwork;
import org.deri.nettopo.node.SensorNode;
import org.deri.nettopo.node.VNode;
import org.deri.nettopo.util.Coordinate;


public class CRITICAL_Judge implements AlgorFunc {
	
	private Algorithm algorithm;
	private WirelessSensorNetwork wsn;
	private NetTopoApp app;
	public CRITICAL_Judge(Algorithm algorithm){
		this.algorithm = algorithm;
	}

	public CRITICAL_Judge(){
		this.algorithm = null;
	}
	
	public Algorithm getAlgorithm(){
		return this.algorithm;
	}
	
	@Override
	public void run() {
		// TODO Auto-generated method stub
		critical_judge(true);
	}
	
	private Collection<VNode> getActiveSensorNode(Collection<VNode> sensorNodes){
		Collection<VNode> result =  new LinkedList<VNode>();
		Iterator<VNode> iter = sensorNodes.iterator();
		while(iter.hasNext()){
			SensorNode node = (SensorNode)iter.next();
			if(node.isActive()){
				result.add(node);
			}
		}
		return result;
	}
	
	private void critical_judge(boolean needPainting) {
		// TODO Auto-generated method stub
		app = NetTopoApp.getApp();
		wsn = app.getNetwork();
		int[] ids=wsn.getAllNodesID();
		HashSet<VNode> nodes = new HashSet<VNode>();
		Collection<VNode> sensorNodes = wsn.getAllNodes();
		nodes.addAll(sensorNodes);
		Collection<VNode> sinkNodes = wsn.getNodes("org.deri.nettopo.node.SinkNode");
		nodes.addAll(sinkNodes);
		Iterator<VNode> it = nodes.iterator();
		while(it.hasNext()){ 
			int id=it.next().getID();
			Integer[] neighbor = getNeighbor(id);
			System.out.println("id"+id+" "+Arrays.toString(neighbor));
		}
	}		
	private Integer[] getNeighbor(int id){
		int[] ids = wsn.getAllNodesID();
		ArrayList<Integer> neighbor = new ArrayList<Integer>();
		int maxTR = Integer.parseInt(wsn.getNodeByID(id).getAttrValue("Max TR"));
		Coordinate coordinate = wsn.getCoordianteByID(id);
		for(int i=0;i<ids.length;i++){
			Coordinate tempCoordinate = wsn.getCoordianteByID(ids[i]);
			if(ids[i] != id && Coordinate.isInCircle(tempCoordinate, coordinate, maxTR)){
				neighbor.add(new Integer(ids[i]));
			}
		}
		return neighbor.toArray(new Integer[neighbor.size()]);
	} 
}






