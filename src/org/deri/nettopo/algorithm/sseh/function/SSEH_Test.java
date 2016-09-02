package org.deri.nettopo.algorithm.sseh.function;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;

import org.deri.nettopo.algorithm.AlgorFunc;
import org.deri.nettopo.algorithm.Algorithm;
import org.deri.nettopo.app.NetTopoApp;
import org.deri.nettopo.network.WirelessSensorNetwork;
import org.deri.nettopo.node.sseh.SensorNode_Normal;
import org.deri.nettopo.node.sseh.SolarNode;

public class SSEH_Test implements AlgorFunc {
	private Algorithm algorithm;
	private SSEH_MAIN sseh;
	private WirelessSensorNetwork wsn;
	private NetTopoApp app;
	public SSEH_Test(Algorithm algorithm)
	{
		this.algorithm = algorithm;
		sseh = new SSEH_MAIN();
	}

	public SSEH_Test()
	{
		this(null);
	}
	@Override
	public void run() {
		LinkedList<Double> distanceList=sseh.getDistanceList();
		Iterator<Double> it=distanceList.iterator();
		while (it.hasNext()) {
			Double distance = (Double) it.next();
			System.out.println(distance);
		}
	}
	
	public void printSolarNodes(LinkedList<SolarNode> solarNodesList){
		Iterator<SolarNode> it=solarNodesList.iterator();
		while(it.hasNext()){
			SolarNode solarNode=it.next();
			System.out.println("Solar Node id is"+solarNode.getID()+"## node's TR is"+solarNode.getMaxTR()+"## node's eh"+solarNode.getEnergyHarvester()+"###battery is"+solarNode.getBattery());
		}
	}
	
	public void printNormalNodes(LinkedList<SensorNode_Normal> normalNodesList){
		Iterator<SensorNode_Normal> it=normalNodesList.iterator();
		while(it.hasNext()){
			SensorNode_Normal normalNode=it.next();
			System.out.println("Normal Node id is"+normalNode.getID()+"## node's TR is"+normalNode.getMaxTR()+"##node's battery is"+normalNode.getBattery());
		}
	}
	
	public Algorithm getAlgorithm() {
		return this.algorithm;
	}

}
