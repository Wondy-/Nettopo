package org.deri.nettopo.algorithm.sseh.function;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.deri.nettopo.algorithm.AlgorFunc;
import org.deri.nettopo.algorithm.Algorithm;
import org.deri.nettopo.app.NetTopoApp;
import org.deri.nettopo.display.Painter;
import org.deri.nettopo.network.WirelessSensorNetwork;
import org.deri.nettopo.node.SensorNode;
import org.deri.nettopo.node.SinkNode;
import org.deri.nettopo.node.VNode;
import org.deri.nettopo.node.sseh.SensorNode_Normal;
import org.deri.nettopo.node.sseh.SolarNode;
import org.deri.nettopo.util.Coordinate;

/**Connect all of nodes include normal nodes and solar nodes
 * @author panhao
 *
 */
public class SSEH_ConnectNeighbors implements AlgorFunc{
private Algorithm algorithm;
	
	public SSEH_ConnectNeighbors(Algorithm algorithm){
		this.algorithm = algorithm;
	}
	
	public SSEH_ConnectNeighbors(){
		this.algorithm = null;
	}
	
	public Algorithm getAlgorithm(){
		return this.algorithm;
	}
	
	public void run(){
		connectNeighbors(true);
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
	
	public void connectNeighbors(boolean needPainting){
		final NetTopoApp app = NetTopoApp.getApp();
		WirelessSensorNetwork wsn = app.getNetwork();
		Painter painter = app.getPainter();
		
//		Collection<VNode> sensorNodes = wsn.getNodes("org.deri.nettopo.node.sseh.SensorNode_SSEH",true);
		Collection<VNode> sensorNodes = wsn.getAllNodes();
		sensorNodes = getActiveSensorNode(sensorNodes);
		
		
		
		SensorNode_Normal[] nodes = new SensorNode_Normal[sensorNodes.size()];
		
		if(nodes.length>0){
			nodes = (SensorNode_Normal[])sensorNodes.toArray(nodes);
			
			/* clear all nodes' neighbor list */
			for(int i=0;i<nodes.length;i++){
				nodes[i].getNeighbors().clear();
			}
			
			/* traverse all the TPGF sensor nodes, if the distance between any two nodes is no more than 
			 * than transmission radius of both nodes, they are neighbors.  */
			for(int i=0;i<nodes.length;i++){
				
				int id_i = nodes[i].getID();
				Coordinate c_i = wsn.getCoordianteByID(id_i);
				
				int tr_i = nodes[i].getMaxTR();
				List<Integer> neighborList_i = nodes[i].getNeighbors();
				
				for(int j=i+1;j<nodes.length;j++){
					
					int id_j = nodes[j].getID();
					Coordinate c_j = wsn.getCoordianteByID(id_j);
					
					int tr_j = nodes[j].getMaxTR();
					List<Integer> neighborList_j = nodes[j].getNeighbors();
					//neighborList_j.clear();
//					System.out.println("id " + id_j);
					double distance = Double.MAX_VALUE;
					if(c_j != null){
						distance = c_i.distance(c_j);
					}
					
					if(distance<=tr_i && distance<=tr_j){ // check the distance
						/* update both nodes' neighbor list */
						neighborList_i.add(Integer.valueOf(id_j));
						neighborList_j.add(Integer.valueOf(id_i));
						
						/* paint the connection */
						if(needPainting)
							painter.paintConnection(id_i, id_j);
					}
				}
			}
		}
		
		/* find the sink node's neighbor */
		Collection<VNode> sinkNodes = wsn.getNodes("org.deri.nettopo.node.SinkNode");
		
		if(sinkNodes.size()>0){
			SinkNode sink = (SinkNode)sinkNodes.iterator().next();
			for(int i=0;i<nodes.length;i++){
				int id_i = nodes[i].getID();
				Coordinate c_i = wsn.getCoordianteByID(id_i);
				int tr_i = nodes[i].getMaxTR();
				List<Integer> neighborList_i = nodes[i].getNeighbors();
				
				int id_sink = sink.getID();
				Coordinate c_sink = wsn.getCoordianteByID(id_sink);
				int tr_sink = sink.getMaxTR();
				
				double distance = c_i.distance(c_sink);
				if(distance<=tr_i && distance<=tr_sink){ // check the distance
					/* update both nodes' neighbor list */
					neighborList_i.add(Integer.valueOf(id_sink));
					
					/* paint the connection */
					if(needPainting)
						painter.paintConnection(id_i, id_sink);
				}
			}
		}
		
		if(needPainting){
			app.getDisplay().asyncExec(new Runnable(){
				public void run() {
					app.refresh();
				}
			});
		}
	}


}
