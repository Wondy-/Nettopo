package org.deri.nettopo.node.localization;

import java.util.ArrayList;
import java.util.Hashtable;
import org.deri.nettopo.node.*;
import org.deri.nettopo.node.localization.dvhop.HopItem;
import org.deri.nettopo.node.localization.dvhop.NodeType;
import org.deri.nettopo.util.*;

public class DVHopNode extends SensorNode{
	
	
	protected Coordinate co = new Coordinate();
	protected ArrayList<Integer> neighbors;
	protected Hashtable<Integer,HopItem> hopTable;//������
	protected int type;
	
	//private String[] extraAttrNames ;
	
	public DVHopNode(){
		super();
		type=NodeType.Undefined;
		neighbors = new ArrayList<Integer>();
		hopTable = new Hashtable<Integer,HopItem>();
		this.setColor(NodeConfiguration.UndefinedNodeColorRGB);
	}
	public DVHopNode(int x,int y){
		super();
		type=NodeType.Undefined;
		neighbors = new ArrayList<Integer>();
		hopTable = new Hashtable<Integer,HopItem>();
		this.setColor(NodeConfiguration.UndefinedNodeColorRGB);
		this.setCoordinate(x, y);
	}
	public Coordinate getCoordinate(){
		return this.co;
	}
	public void setCoordinate(int x,int y,int z){
		if(co!=null){
			co.setCoord(new Coordinate(x,y,z));
		}
	}
	public void setCoordinate(int x,int y){
		if(co!=null){
			co.setCoord(new Coordinate(x,y));
		}
	}
	public ArrayList<Integer> getNeighbors(){
		return neighbors;
	}
	public int getType(){
		return this.type;
	}
	public void setType(int type){
		this.type=type;
	}
	public Hashtable<Integer,HopItem> getHopTable() {
		return hopTable;
	}
	public void setHopTable(Hashtable<Integer,HopItem> hopTable) {
		this.hopTable = hopTable;
	}
	
	
	/*
	 * ������ܵ���������
	 * */
	public long HandleHopItem(HopItem item){
		long inum=0;//��Ҫ�㲥�ı�����Ŀ
		int hop=item.getHops();
		if(this.hopTable.containsKey(Integer.valueOf(item.getaID()))){
			/*������������Ѿ� ������item*/
			HopItem e=this.hopTable.get(item.getaID());
			if(hop<e.getHops()){
				e.setHops(hop);
				if(e.isCasted()){
					inum++;
					e.setCasted(false);
				}
				System.out.println(this.toString()
						+" modified anchor "+e.getaID()+" with hops "+e.getHops());
			}
			else{
				//inum--;//����������û���޸�
				System.out.println(this.toString()+" compare hops:"+e.getHops()+"<="+hop);
			}
		}else{
			/*�����������û�а���item*/
			item.setCasted(false);
			this.hopTable.put(item.getaID(), item);
			inum++;
			System.out.println(this.toString()
					+" append anchor "+item.getaID()+" with hops "+item.getHops());
		}
		return inum;
	}
	
	@Override
	public String toString(){
		String description = "node "+this.getID();
		
		return description;
	}
	
}
