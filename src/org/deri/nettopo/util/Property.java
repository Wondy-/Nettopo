package org.deri.nettopo.util;

import java.util.HashMap;

import org.deri.nettopo.network.WirelessSensorNetwork;

public class Property {
	/**
	 * flag=false;//true Ϊ������falseΪ�ݼ�
	 * <p>
	 * 
	 */
	public static int TPGF_step = 100;
	public static int TPGF_min = 400; // �߽��С
	public static int TPGF_max = 600; // ��ǰ�������ڵ�����
	public static boolean flag = false;// true Ϊ������falseΪ�ݼ�
	public static boolean isSlave = true;
	public static boolean isTPGF = false; // �Ƿ��ǲ���
	public static HashMap<String, Integer> nodeAttr;
	public static int[] randomAttr;
	public static boolean isSensor = false;
	public static java.util.List<WirelessSensorNetwork> tasks;
	public static int times = 5;// cknִ�д���
	public static boolean needpaint = true;
	public static boolean isCreateTasks = false;
	public static boolean isCreateOneTask = false;
	public static final double R=0.7;	// delete neighbour rate
	
	public static double nodeRate=0.0;
	public static double neighborRate=0.0;
	
	/*denger level table*/
	public static int getDengerLevel(String gasName)
	{
		if(gasName.equals("CO"))		//һ����̼
			return 1;
		else if(gasName.equals("CL2"))	//����
			return 14;//200
		else if(gasName.equals("C2H4O"))	//��������
			return 6;//40
		else if(gasName.equals("HCHO"))//��ȩ
			return 10;//100
		else if(gasName.equals("HCl"))//��ȩ
			return 7;//52
		else if(gasName.equals("H2S"))//��ȩ
			return 3;//10
		else if(gasName.equals("SO2"))//��ȩ
			return 7;//50
		return -1;
	}
}
