package org.deri.nettopo.node.sseh;

import java.util.ArrayList;

import org.deri.nettopo.node.SensorNode;
import org.deri.nettopo.util.FormatVerifier;
import org.deri.nettopo.util.Util;

public class SensorNode_Normal extends SensorNode {
	private double battery;
	private ArrayList<Integer> neighbors;  //存储所有邻居节点的ID
	private String[] extraAttrNames;
	public SensorNode_Normal() {
		neighbors=new ArrayList<Integer>();
		extraAttrNames=new String[]{"Battery"};
	}
	
	public double getBattery() {
		return battery;
	}
	public void setBattery(double battery) {
		this.battery = battery;
	}
	public ArrayList<Integer> getNeighbors() {
		return neighbors;
	}
	public void setNeighbors(ArrayList<Integer> neighbors) {
		this.neighbors = neighbors;
	}
	
	public String[] getAttrNames(){
		String[] superAttrNames = super.getAttrNames();
		String[] attrNames = Util.stringArrayConcat(superAttrNames, extraAttrNames);
		return attrNames;
	}
	
	public boolean setAttrValue(String attrName, String value){
		boolean isAttrValid = true;
		int index = Util.indexOf(extraAttrNames, attrName);
		switch(index){
		case 0:
			this.setBattery(Double.parseDouble(value));
			break;
		default:
			isAttrValid = super.setAttrValue(attrName, value);
			break;
		}
		return isAttrValid;
	}
	
	public String getAttrValue(String attrName){
		int index = Util.indexOf(extraAttrNames, attrName);
		switch(index){
		case 0:
			return String.valueOf(getBattery());
		default:
			return super.getAttrValue(attrName);
		}
	}
}
