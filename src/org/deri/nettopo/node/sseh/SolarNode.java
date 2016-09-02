package org.deri.nettopo.node.sseh;

import java.util.ArrayList;

import org.deri.nettopo.node.NodeConfiguration;
import org.deri.nettopo.node.SensorNode;
import org.deri.nettopo.util.FormatVerifier;
import org.deri.nettopo.util.Util;

public class SolarNode extends SensorNode_Normal{
	private int energyHarvester;
	private int batteryLevel;
	private ArrayList<Integer> neighbors;  //存储所有邻居节点的ID
	public SolarNode() {
		super();
		super.setColor(NodeConfiguration.SolarNodeColorRGB);
		energyHarvester=1;
		neighbors=new ArrayList<Integer>();
	}
	
	public int getBatteryLevel() {
		return batteryLevel;
	}

	public void setBatteryLevel(int batteryLevel) {
		this.batteryLevel = batteryLevel;
	}
	public int getEnergyHarvester() {
		return energyHarvester;
	}

	public void setEnergyHarvester(int energyHarvester) {
		this.energyHarvester = energyHarvester;
	}

	public ArrayList<Integer> getNeighbors() {
		return neighbors;
	}

	public void setNeighbors(ArrayList<Integer> neighbors) {
		this.neighbors = neighbors;
	}
	
}
