package org.deri.nettopo.node.localization.dvhop;

import java.util.Enumeration;
import java.util.Hashtable;
import java.lang.Math;

import org.deri.nettopo.node.NodeConfiguration;
import org.deri.nettopo.node.localization.DVHopNode;
import org.deri.nettopo.util.*;

import Jama.Matrix;

public class UnknownNode extends DVHopNode {
	
	private Coordinate colculatedCo = new Coordinate();/*�������������*/
	private boolean hasLocated;/*�Ƿ��Ѿ����������*/
	private double disPerHop=0.0;//ƽ��ÿ������
	private boolean isCastDPH=false;
	
	public UnknownNode(){
		super();
		super.setType(NodeType.Unknown);
		super.setColor(NodeConfiguration.UnknownNodeColorRGB);
		this.hasLocated=false;
		this.colculatedCo.x=-1;
		this.colculatedCo.y=-1;
	}
	
	public Coordinate getColculatedCoordinate(){
		return this.colculatedCo;
	}
	public void setColculatedCoordinate(int x,int y){
		this.colculatedCo.setX(x);
		this.colculatedCo.setY(y);
	}
	public boolean HasLocated(){
		return hasLocated;
	}
	public void setHasLocated(boolean tmp){
		this.hasLocated=tmp;
	}

	public double getDisPerHop() {
		return disPerHop;
	}

	public void setDisPerHop(double disPerHop) {
		this.disPerHop = disPerHop;
	}
	
	/**
	 * �����յ���DPH��������ֵ1��ʾ1���ڵ���DPH�����ʾ�ýڵ�����DPH
	 * */
	public int HandleDPH(double dph){
		if(Double.compare(this.disPerHop, 0.0)==0){
			this.disPerHop=dph;
			//for debug
			System.out.println("unknown node "+this.getID()+" get DPH :"+this.disPerHop);
			return 1;
		}
		return 0;
	}
	
	public boolean isRecievedDPH(){
		if(Double.compare(this.disPerHop, 0.0)==0)
			return false;
		else 
			return true;
	}

	public boolean isCastDPH() {
		return isCastDPH;
	}

	public void setCastDPH(boolean isCastDPH) {
		this.isCastDPH = isCastDPH;
	}
	
	/**
	  * ת����ʽAx=b�õ�x=(AT*A)-1*AT*b
	  *AT��A��ת��
	  *I=(AT*A)-1��ʾ(AT*A)�������
	*/
	public Coordinate DeduceCoordinate(){
		
		HopItem[] ht=new HopItem[hopTable.size()];
		
		int i=0;
		Enumeration<HopItem> items=hopTable.elements();
		while(items.hasMoreElements()){
			HopItem it=items.nextElement();
			ht[i++]=it.Copy();
		}
		
		/*¼�����ݣ�����A*/		
		double[][] tmpA=new double[ht.length-1][2];
		//System.out.println("ht's length is "+ht.length);
		i=0;
		for(double[] row : tmpA){
			//row=new double[2];
			if(i<ht.length-1){
				row[0]=(-2)*(ht[i].getCoor().x-ht[ht.length-1].getCoor().x);
				row[1]=(-2)*(ht[i].getCoor().y-ht[ht.length-1].getCoor().y);
				i++;
			}
		}
		
		/*¼�����ݣ�����b*/
		double[][] tmpb=new double [ht.length-1][1];
		i=0;
		for(double[] row : tmpb){
			if(i<ht.length-1){
				row[0]=Math.pow(DistanceTo(ht[i].getaID()),2)
					-Math.pow(DistanceTo(ht[ht.length-1].getaID()),2)
					-Math.pow(ht[i].getCoor().x, 2)
					+Math.pow(ht[ht.length-1].getCoor().x, 2)
					-Math.pow(ht[i].getCoor().y, 2)
					+Math.pow(ht[ht.length-1].getCoor().y, 2);
				i++;
			}
		}
		
		/*��ʼ��������x=(AT*A)-1*AT*b*/
		Matrix A=new Matrix(tmpA);
		Matrix b=new Matrix(tmpb);
		Matrix AT=(A.copy()).transpose();
		Matrix ATxA=AT.times(A);//(AT*A)
		Matrix I=ATxA.inverse();//I=(AT*A)-1,I=(AT*A)-1��ʾ(AT*A)�������
		Matrix res=I.times(AT);
		res=res.times(b);
		double[][] xy=res.getArray();
		/*������������*/
		this.colculatedCo.x=(int)xy[0][0];
		this.colculatedCo.y=(int)xy[1][0];
		this.hasLocated=true;
		super.setColor(NodeConfiguration.AnchorNodeColorRGB);
		return this.colculatedCo;
	}
	
	private double DistanceTo(int anchorId){
		
		Hashtable<Integer, HopItem> htable=super.hopTable;
		HopItem hi=htable.get(anchorId);
		return hi.getHops()*this.disPerHop;
	}
	
	
}
