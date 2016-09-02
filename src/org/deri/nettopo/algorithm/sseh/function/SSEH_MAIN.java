package org.deri.nettopo.algorithm.sseh.function;

import java.math.BigDecimal;
import java.util.*;

import org.deri.nettopo.algorithm.AlgorFunc;
import org.deri.nettopo.algorithm.Algorithm;
import org.deri.nettopo.app.NetTopoApp;
import org.deri.nettopo.network.WirelessSensorNetwork;
import org.deri.nettopo.node.NodeConfiguration;
import org.deri.nettopo.node.SensorNode;
import org.deri.nettopo.node.VNode;
import org.deri.nettopo.node.sseh.SensorNode_Normal;
import org.deri.nettopo.node.sseh.SolarNode;
import org.deri.nettopo.util.Coordinate;
import org.deri.nettopo.util.Property;
import org.deri.nettopo.util.RandomValue;
import org.deri.nettopo.util.Util;

/**
 * the description of the energy-based sleep scheduling algorithm:
 * 
 * (*Run the following at each node u*) 1. Get the energy harvesting data from
 * solar panel(Es[]); 2. Obtain the residual energy of the battery (Bresidual)
 * of each solar-based node that belongs to theSet Ssolar ={s1; s2; : : : ;
 * sNSolar}. 3. If Bresidual < Ecritical recharge its battery with the harvested
 * solar power 4. Return 5. Else awake solar-nodes 6. Run EC-CKN algorithm 7.
 * Check the battery levels 8. If levels=1 Increase the k-value to wake-up more
 * number of nodes If levels=2 The transmission range of the solarnode is
 * increased to maintain the connectivity and the coverage degree.
 * 
 * @author code by Hao Pan based on Mithun's paper
 */
/**
 * @author paine
 *
 */
public class SSEH_MAIN implements AlgorFunc {

	private Algorithm algorithm;
	private WirelessSensorNetwork wsn;
	private NetTopoApp app;
	// private HashMap<Integer,Double> ranks;
	private HashMap<Integer, Integer[]> neighbors;
	private HashMap<Integer, Integer[]> neighborsOf2Hops;
	private HashMap<Integer, Boolean> awake;
	private HashMap<Integer, Boolean> solarSleepList;
	private static int k = 4;// the least awake neighbors
	boolean needInitialization;
	private int lifeTime = 0;
	private static double TargetDegree = 3;
	boolean isinit = true;
	private static double Es_value = 2;
	private double Ec_value = 2.5;
	//private static double res = 0;
	private static int TR = 60;

	public SSEH_MAIN(Algorithm algorithm) {
		this.algorithm = algorithm;
		neighbors = new HashMap<Integer, Integer[]>();
		neighborsOf2Hops = new HashMap<Integer, Integer[]>();
		awake = new HashMap<Integer, Boolean>();
		solarSleepList = new HashMap<Integer, Boolean>();
		k = 4;
		needInitialization = true;
	}

	public SSEH_MAIN() {
		this(null);
	}

	@Override
	public void run() {
		if (isNeedInitialization()) {
			initializeWork();
		}
		SSEH_Function();

		System.out.print("1 " + "10 " + wsn.getSensorActiveNodes().length + " " + solarSleepList.size() + " ");
		checkTheSolarNodesBatteryLevel();
		System.out.println(k + " " + getCoverageDegree());
		//System.out.println("-------------------------------------");
		resetColorAfterSSEH();
		app.getPainter().rePaintAllNodes();
		double getDegree =  getCoverageDegree();
		if(getDegree - TargetDegree > 0 ){
			app.getDisplay().asyncExec(new Runnable() {
				@Override
				public void run() {
					app.refresh();
				}
			});
		}else{
			LinkedList<SolarNode> solarNodesList = getSolarNodes();
			Iterator<SolarNode> itSolarNodes = solarNodesList.iterator();
			while (itSolarNodes.hasNext()) {
				SolarNode solarNode = (SolarNode) itSolarNodes.next();
				if (solarNode.getBattery() < 10.0 ) {
					
				} else {			
					incresTr(solarNode.getID());
					re_ec_ckn_function();
				}
			}
			Iterator<Integer> it=awake.keySet().iterator();
			while (it.hasNext()) {
				Integer id = (Integer) it.next();
				if(awake.get(id)){
					updateAwakeNormalNodeBattery(id);
				}
				else{
					updateSleepNormalNodeBattery(id);
				}
			}
			//k = k + 1;
			app.getDisplay().asyncExec(new Runnable() {
				@Override
				public void run() {
					app.refresh();
				}
			});
		}
	
	}
	/**
	 * 
	 * @return the ranks between 0 and 1, and with id as the key
	 */

	public HashMap<Integer, Double> initializeHashMapEranks() {
		HashMap<Integer, Double> temp_Eranks = new HashMap<Integer, Double>();
		int[] ids = wsn.getAllSensorNodesID();
		for (int i = 0; i < ids.length; i++) {
			int id = ids[i];
			SensorNode_Normal node = (SensorNode_Normal) wsn.getNodeByID(id);
			double Erank = node.getBattery();
			temp_Eranks.put(new Integer(id), new Double(Erank));
		}
		return temp_Eranks;
	}

	private void initializeAwake() {
		int ids[] = wsn.getAllSensorNodesID();
		for (int i = 0; i < ids.length; i++) {
			setAwake(ids[i], true);
		}
	}

	private void setAwake(int id, boolean isAwake) {
		Integer ID = new Integer(id);
		awake.put(ID, isAwake);
		if(wsn.nodeSimpleTypeNameOfID(id).contains("Node")){
			((SensorNode) wsn.getNodeByID(id)).setActive(isAwake);
			((SensorNode) wsn.getNodeByID(id)).setAvailable(isAwake);
		}
	}

	public void alterNeighbour(double nodeRate, double neighRate) {
		for (Integer key : neighbors.keySet()) {
			VNode vnode = wsn.getNodeByID(key);
			if (vnode instanceof SensorNode && Util.roulette(nodeRate)) {
				// vnode.setColor(NodeConfiguration.InteredNodeColor);
				Integer[] allNer = neighbors.get(key);
				int removeNum = (int) Math.floor(allNer.length * neighRate);
				Integer[] temp = new Integer[allNer.length - removeNum];
				System.arraycopy(allNer, removeNum, temp, 0, temp.length);
				neighbors.put(key, temp);
				// for(;removeNum>0;removeNum--)
				// {
				//
				//// connectionBroken.put(vnode.getID(), allNer.remove(0));
				// }
			}
		}

	}

	private void initializeNeighbors() { //
		int[] ids = getJoinECKNodeIds();
		for (int i = 0; i < ids.length; i++) {
			Integer ID = new Integer(ids[i]);
			Integer[] neighbor = getNeighbor(ids[i]);
			neighbors.put(ID, neighbor);
		}
		alterNeighbour(Property.nodeRate, Property.neighborRate);
	}

	private Integer[] getNeighbor(int id) {
		int[] ids = getJoinECKNodeIds();
		ArrayList<Integer> neighbor = new ArrayList<Integer>();
		int maxTR = Integer.parseInt(wsn.getNodeByID(id).getAttrValue("Max TR"));
		Coordinate coordinate = wsn.getCoordianteByID(id);
		for (int i = 0; i < ids.length; i++) {
			Coordinate tempCoordinate = wsn.getCoordianteByID(ids[i]);
			if (ids[i] != id && Coordinate.isInCircle(tempCoordinate, coordinate, maxTR)) {
				neighbor.add(new Integer(ids[i]));
			}
		}
		return neighbor.toArray(new Integer[neighbor.size()]);
	}

	private void initializeNeighborsOf2Hops() {
		int[] ids = getJoinECKNodeIds();
		for (int i = 0; i < ids.length; i++) {
			Integer[] neighbor1 = neighbors.get(new Integer(ids[i]));
			HashSet<Integer> neighborOf2Hops = new HashSet<Integer>(Arrays.asList(neighbor1));
			for (int j = 0; j < neighbor1.length; j++) {
				Integer[] neighbor2 = neighbors.get(new Integer(neighbor1[j]));
				for (int t = 1; t < neighbor2.length; t++) {
					neighborOf2Hops.add(neighbor2[t]);
				}
			}
			if (neighborOf2Hops.contains(new Integer(ids[i])))
				neighborOf2Hops.remove(new Integer(ids[i]));

			neighborsOf2Hops.put(new Integer(ids[i]), neighborOf2Hops.toArray(new Integer[neighborOf2Hops.size()]));
		}
	}

	/**
	 * ranks, neighbours, neighborsOf2Hops, won't change with time and if you
	 * don't delete nodes, the nodes,coordinates won't change either.
	 */
	private void initializeWork() {
		app = NetTopoApp.getApp();
		wsn = app.getNetwork();
		initializeAwake();// at first all are true
		setNeedInitialization(false);
	}

	/************
	 * the above methods are to initialise the CKN fields
	 ***************/

	/************
	 * the following methods are to be used in CKN_Function
	 *************/

	private Integer[] getAwakeNeighborsOf2HopsMoreThanEranku(int id) {
		Integer[] neighborsOf2HopsOfID = neighborsOf2Hops.get(new Integer(id));
		Vector<Integer> result = new Vector<Integer>();
		double Eranku = ((SensorNode_Normal) wsn.getNodeByID(id)).getBattery();
		for (int i = 0; i < neighborsOf2HopsOfID.length; i++) {
			if (awake.get(neighborsOf2HopsOfID[i]).booleanValue()
					&& ((SensorNode_Normal) wsn.getNodeByID(neighborsOf2HopsOfID[i])).getBattery() > Eranku) {
				result.add(neighborsOf2HopsOfID[i]);
			}
		}
		return result.toArray(new Integer[result.size()]);
	}

	private Integer[] getAwakeNeighbors(int id) {
		HashSet<Integer> nowAwakeNeighbor = new HashSet<Integer>();
		Integer[] neighbor = neighbors.get(new Integer(id));
		for (int i = 0; i < neighbor.length; i++) {
			if (awake.get(neighbor[i]).booleanValue()) {
				nowAwakeNeighbor.add(neighbor[i]);
			}
		}
		return nowAwakeNeighbor.toArray(new Integer[nowAwakeNeighbor.size()]);
	}

	private boolean isOneOfAwakeNeighborsNumLessThanK(int id) { // �����Ҳ���Ҫ�����Ķ�����Ϊ�ǵ�һ���������õ�
		boolean result = false;
		Integer[] nowAwakeNeighbors = getAwakeNeighbors(id);
		for (int i = 0; i < nowAwakeNeighbors.length; i++) {
			if (getAwakeNeighbors(nowAwakeNeighbors[i].intValue()).length < k) {
				result = true;
				break;
			}
		}
		return result;
	}

	// C_u={v|v��N_u and Erank_v > Erank_u};
	private Integer[] getCu(int id) {
		Integer ID = new Integer(id);
		LinkedList<Integer> result = new LinkedList<Integer>();
		List<Integer> availableAwakeNeighbors = Arrays.asList(getAwakeNeighbors(id));
		Iterator<Integer> iter = availableAwakeNeighbors.iterator();
		while (iter.hasNext()) {
			Integer neighbor = iter.next();
			if (((SensorNode_Normal) wsn.getNodeByID(neighbor)).getBattery() > ((SensorNode_Normal) wsn.getNodeByID(ID))
					.getBattery()) {
				result.add(neighbor);
			}
		}
		return result.toArray(new Integer[result.size()]);
	}

	/**
	 * any node in Nu has at least k neighbours from Cu
	 * 
	 * @param Nu
	 * @param Cu
	 * @return
	 */
	private boolean atLeast_k_Neighbors(Integer[] Nu, Integer[] Cu) {
		if (Cu.length < k) {
			return false;
		}
		int[] intCu = Util.IntegerArray2IntArray(Cu);
		for (int i = 0; i < Nu.length; i++) {
			int[] neighbor = Util.IntegerArray2IntArray(neighbors.get(Nu[i]));
			int[] neighborInCu = Util.IntegerArrayInIntegerArray(neighbor, intCu);
			if (neighborInCu.length < k + 1) {
				return false;
			}
		}
		return true;
	}

	/**
	 * node in Cu is directly connect or indirectly connect within u's 2-hop
	 * wake neighbours that rank < ranku
	 * 
	 * @param Cu
	 * @param u
	 * @return
	 */
	private boolean qualifiedConnectedInCu(Integer[] Cu, int[] awakeNeighborsOf2HopsMoreThanEranku) {
		if (Cu.length == 0) {
			return false;
		}

		if (Cu.length == 1) {
			return true;
		}

		Integer[] connectionOfCuElementFromCu1 = getConnection(Cu[1], awakeNeighborsOf2HopsMoreThanEranku);
		if (!Util.isIntegerArrayInIntegerArray(Util.IntegerArray2IntArray(Cu),
				Util.IntegerArray2IntArray(connectionOfCuElementFromCu1))) {
			return false;
		}
		return true;
	}

	/**
	 * to get connection of id with id's neighbour in array
	 * 
	 * @param beginning
	 * @param array
	 * @return
	 */
	private Integer[] getConnection(int beginning, int[] array) {
		ArrayList<Integer> connectedCu = new ArrayList<Integer>();
		Queue<Integer> queue = new LinkedList<Integer>();
		queue.offer(new Integer(beginning));
		while (!queue.isEmpty()) {
			Integer head = queue.poll();
			connectedCu.add(head);
			int[] CuNeighbor = Util.IntegerArrayInIntegerArray(Util.IntegerArray2IntArray(neighbors.get(head)), array);
			for (int i = 0; i < CuNeighbor.length; i++) {
				if (!connectedCu.contains(new Integer(CuNeighbor[i])) && !queue.contains(new Integer(CuNeighbor[i]))) {
					queue.offer(new Integer(CuNeighbor[i]));
				}
			}
		}
		return connectedCu.toArray(new Integer[connectedCu.size()]);
	}

	/****************************************************************************/

	private void resetColorAfterSSEH() {
		Iterator<Integer> iter = awake.keySet().iterator();
		while (iter.hasNext()) {
			Integer id = iter.next();
			Boolean isAwake = awake.get(id);
			if (isAwake.booleanValue()) {
				String name = wsn.getNodeByID(id).getClass().getSimpleName();
				if (name.contains("Solar")) {
					wsn.resetNodeColorByID(id.intValue(), NodeConfiguration.AwakeSolarNodeColorRGB);	
					// System.out.println(id+"is solar");
					//wsn.resetNodeColorByID(id.intValue(), NodeConfiguration.AwakeSolarNodeColorRGB);
				} else {
					// System.out.println(id+"is sensor");
					wsn.resetNodeColorByID(id.intValue(), NodeConfiguration.AwakeNodeColorRGB);
				}

			} else {
					String name = wsn.getNodeByID(id).getClass().getSimpleName();
					if (name.contains("Solar")){
						wsn.resetNodeColorByID(id.intValue(), NodeConfiguration.SleepSolarNodeColorRGB);
					}else{
						wsn.resetNodeColorByID(id.intValue(), NodeConfiguration.SleepNormalNodeColorRGB);
					}
				}
		}
	}

	/****************************************************************************/

	/**
	 * Get Coverage Degree
	 * 
	 * @return Coverage Degree
	 * @author HuliKun
	 */
	public double getCoverageDegree() // 计算覆盖度
	{
		int[] coors = wsn.getSensorActiveNodes();
		int x, y, sum = 0;
		Coordinate coor;
		int maxtr = 0;
		for (int coorId : coors) {
			maxtr = Integer.parseInt(wsn.getNodeByID(coorId).getAttrValue("Max TR"));
			coor = wsn.getCoordianteByID(coorId);
			for (int i = 0; i < 30; ++i) {
				x = i * 20 + 10;
				if (Math.abs(x - coor.x) > maxtr)
					continue;
				for (int j = 0; j < 30; ++j) {
					y = j * 20 + 10;
					if (Math.abs(y - coor.y) > maxtr)
						continue;
					sum++;
				}
			}
		}
		return sum / 30.0 / 30.0;
	}

	public double get2NumDouble(double d){
		BigDecimal bg = new BigDecimal(d);  
        return bg.setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();  
	}
	
	
	private int[] getJoinECKNodeIds() {
		LinkedList<Integer> cknList=new LinkedList<Integer>();
		Collection<VNode> nodesCollection = wsn.getAllNodes();
		Iterator<VNode> it = nodesCollection.iterator();
		while (it.hasNext()) {
			VNode temp = it.next();
			if (wsn.getNodeByID(temp.getID()).getClass().getSimpleName().contains("Solar")) {
				if(awake.get(temp.getID())){
					cknList.add(temp.getID());
				}
			}
			else{
				cknList.add(temp.getID());
			}
		}
		Integer[] ids = new Integer[cknList.size()];
		ids = (Integer[]) cknList.toArray(ids);
		return Util.IntegerArray2IntArray(ids);
	}

	private void incresTr(int id) {
		SolarNode solarNode = (SolarNode) wsn.getNodeByID(id);
		double result = TR + solarNode.getBattery() / 100 * TR;
		String resStr = "" + result;
		String intes = resStr.substring(0, resStr.indexOf("."));
		int intRes = Integer.parseInt(intes);
		solarNode.setMaxTR(intRes);
	}

	/**
	 * Set the energy harvesting data for solar panel
	 * 
	 * @author panhao
	 */
	// public void setSolarNodesEnergyHarvesting() {
	// LinkedList<SolarNode> solarNodes=getSolarNodes();
	// Iterator<SolarNode> it=solarNodes.iterator();
	// while (it.hasNext()) {
	// SolarNode temp = (SolarNode) it.next();
	// temp.setEnergyHarvester(Es_value);
	// }
	// }

	/**
	 * Initialize all nodes battery before run this algorithm
	 * 
	 * @author panhao
	 */
	public void initAllNodesBattery() {
		LinkedList<SolarNode> solarNodes = getSolarNodes();
		Iterator<SolarNode> itSolar = solarNodes.iterator();
		while (itSolar.hasNext()) {
			SolarNode temp = (SolarNode) itSolar.next();
			double b = RandomValue.getInstance().getIntRandomValue(75, 30);
			temp.setBattery(b);
		}

		LinkedList<SensorNode_Normal> normalNodes = getNormalNodes();
		Iterator<SensorNode_Normal> itNormal = normalNodes.iterator();
		while (itNormal.hasNext()) {
			SensorNode_Normal temp = (SensorNode_Normal) itNormal.next();
			temp.setBattery(RandomValue.getInstance().getIntRandomValue(50, 20));

		}
	}

	private void checkTheSolarNodes() {
		solarSleepList=new HashMap<Integer,Boolean>();
		LinkedList<SolarNode> solarNodesList = getSolarNodes();
		Iterator<SolarNode> itSolarNodes = solarNodesList.iterator();
		while (itSolarNodes.hasNext()) {
			SolarNode solarNode = (SolarNode) itSolarNodes.next();
			//double solarNodeEcritical = getEcknById(solarNode.getID());
			int nodeId = solarNode.getID();

			double br = solarNode.getBattery();
			
			if(br>100){
				solarNode.setBattery(100.0);
			}
//			System.out.println("ID:" + nodeId + "Battery:" + br);
//			System.out.println("ID:" + solarNode.getID() + "Need Eckn:" + solarNodeEcritical);
			if (br - Ec_value<0) {
				if (Ec_value- Es_value > 0){
					setAwake(nodeId, false);
					solarSleepList.put(nodeId, true);
					updateSleepSolarNodeBattery(nodeId);
				} else {
					setAwake(nodeId, true);
					updateAwakeSolarNodeBattery(nodeId);
				}
			} else {
				setAwake(nodeId, true);
				updateAwakeSolarNodeBattery(nodeId);
			}
		}
	}

	private void checkTheSolarNodesBatteryLevel() {
		int lv_1 = 0;
		int lv_2 = 0;
		LinkedList<SolarNode> solarNodesList = getSolarNodes();
		Iterator<SolarNode> itSolarNodes = solarNodesList.iterator();
		while (itSolarNodes.hasNext()) {
			SolarNode solarNode = (SolarNode) itSolarNodes.next();
			if (solarNode.getBattery() < 5.0 ) {
				lv_1++;
				
			} else {
				lv_2++;
				incresTr(solarNode.getID());
			}
		}
		System.out.print(lv_1 + " " + lv_2 + " ");
		//System.out.println("Number of Type1 solarNode:" + lv_1);
		//System.out.println("Number of Type2 solarNode:" + lv_2);
		//double all = lv_2 + lv_1;
		//res = lv_2 / all;
		//if (res > 0.7) {
		//	k++;
		//} else {	
		//}

	}

	/**
	 * Update Sleep node battery remaining
	 * 
	 * @param Node
	 *            id
	 */
	public void updateSleepSolarNodeBattery(int id) {
		SolarNode node = (SolarNode) wsn.getNodeByID(id);
		double br = node.getBattery();
		br = br + Es_value - 0.1;
		node.setBattery(br);
	}

	public void updateSleepNormalNodeBattery(int id){
		SensorNode_Normal node = (SensorNode_Normal) wsn.getNodeByID(id);
		double br = node.getBattery();
		br = br - 0.1;
		node.setBattery(br);
	}
	
	/**
	 * Update Awake node battery remaining
	 * 
	 * @param Node
	 *            id
	 */
	public void updateAwakeSolarNodeBattery(int id) {
		SolarNode node = (SolarNode) wsn.getNodeByID(id);
		double br = node.getBattery();
		br = br + Es_value - getEcknById(id);
		node.setBattery(br);
	}

	
	public void updateAwakeNormalNodeBattery(int id) {
		SensorNode_Normal node = (SensorNode_Normal) wsn.getNodeByID(id);
		double br = node.getBattery();
		br = br - getEcknById(id);
		node.setBattery(br);
	}
	
	
	/**
	 * Get Average Distance
	 * 
	 * @return Distance List
	 * @author panhao
	 */
	public LinkedList<Double> getDistanceList() {
		if (isNeedInitialization()) {
			initializeWork();
		}
		LinkedList<Double> distanceList = new LinkedList<Double>();
		Collection<VNode> sensorNodes = wsn.getAllNodes();
		SensorNode_Normal[] nodes = new SensorNode_Normal[sensorNodes.size()];

		if (nodes.length > 0) {
			nodes = (SensorNode_Normal[]) sensorNodes.toArray(nodes);

			/* clear all nodes' neighbor list */
			for (int i = 0; i < nodes.length; i++) {
				nodes[i].getNeighbors().clear();
			}

			/*
			 * traverse all the TPGF sensor nodes, if the distance between any
			 * two nodes is no more than than transmission radius of both nodes,
			 * they are neighbors.
			 */

			for (int i = 0; i < nodes.length; i++) {

				int id_i = nodes[i].getID();
				Coordinate c_i = wsn.getCoordianteByID(id_i);

				int tr_i = nodes[i].getMaxTR();
				ArrayList<Integer> neighborList_i = nodes[i].getNeighbors();

				for (int j = i + 1; j < nodes.length; j++) {

					int id_j = nodes[j].getID();
					Coordinate c_j = wsn.getCoordianteByID(id_j);

					int tr_j = nodes[j].getMaxTR();
					ArrayList<Integer> neighborList_j = nodes[j].getNeighbors();
					// neighborList_j.clear();
					// System.out.println("id " + id_j);
					double distance = Double.MAX_VALUE;
					if (c_j != null) {
						distance = c_i.distance(c_j);
					}

					if (distance <= tr_i && distance <= tr_j) { // check the
																// distance
						/* update both nodes' neighbor list */
						neighborList_i.add(Integer.valueOf(id_j));
						neighborList_j.add(Integer.valueOf(id_i));
						distanceList.add(distance);

					}
				} // end for
				nodes[i].setNeighbors(neighborList_i);
			} // end for
		} // end if
		return distanceList;
	}

	/**
	 * Get the number of a node's neighbors
	 * 
	 * @param node
	 *            id
	 * @return number of a node's neighbors
	 * @author panhao
	 */
	public int getNumberOfOneHopNode(int id) {
		SensorNode_Normal node = (SensorNode_Normal) wsn.getNodeByID(id);
		ArrayList<Integer> neighbors = node.getNeighbors();
		return neighbors.size();
	}

	/**
	 * Get the average distaces
	 * 
	 * @return Average Distance
	 * @author panhao
	 */
	public double getAverageDistance() {
		double d = 0.000;
		double av_d = 0.000;
		LinkedList<Double> distance = getDistanceList();
		Iterator<Double> it = distance.iterator();
		while (it.hasNext()) {
			Double dn = (Double) it.next();
			d = d + dn;
		}
		av_d = d / distance.size();
		return av_d;
	}

	/**
	 * Get node's awake run ec-ckn energy by node id
	 * 
	 * @param node
	 *            id
	 * @return Eckn
	 * @author panhao
	 */
	public double getEcknById(int id) {
		SensorNode node = (SensorNode) wsn.getNodeByID(id);
		int nu = getNumberOfOneHopNode(id);
		double e_ckn_awake = 0.000;
		double e_elec = 0.000001;
		double e_rx = 0.1;
		int r = node.getMaxTR();
		double e_amp = 0.000000000001 * 2 * 3.1415926 * r * r;
		double d = getAverageDistance();
		double e_tx = e_elec + e_amp * d * d;
		e_ckn_awake = 0.1 + 2 * e_tx + 2 * nu * e_rx;
		return e_ckn_awake;
	}

	public void printAllSensorNodesNeighbors() {
		Collection<VNode> sensorNodes = wsn.getAllNodes();
		Iterator<VNode> it = sensorNodes.iterator();
		while (it.hasNext()) {
			SensorNode_Normal node = (SensorNode_Normal) it.next();
			ArrayList<Integer> neighbors = node.getNeighbors();
			Iterator<Integer> itNeigh = neighbors.iterator();
			System.out.println(
					"Node ID:" + node.getID() + "#Number of neighbors:" + neighbors.size() + "#neighbors ID:");
			while (itNeigh.hasNext()) {
				Integer neighborID = (Integer) itNeigh.next();
				System.out.println(neighborID);
			}

		}
	}

	public void printAllNodesId() {
		int[] nodesid = wsn.getAllNodesID();
		for (int i = 0; i < nodesid.length; i++) {
			System.out.println(nodesid[i]);
		}
	}


	public void printECKNodesIds() {
		int[] ids = getJoinECKNodeIds();
		for (int i = 0; i < ids.length; i++) {
			System.out.println("id:" + ids[i]);
		}
	}

	private void printSleepSolarIds(){
		System.out.println("sleep nodes of solar nodes:");
		Iterator<Integer>it=solarSleepList.keySet().iterator();
		while (it.hasNext()) {
			Integer id = (Integer) it.next();
			System.out.println("ID: "+id);
		}
	}
	private void printSleepSolarNumbers(){
		System.out.println("sleep nodes of solar nodes number:" + solarSleepList.size());
	}
	
	private void printAwakeNodes(){
		System.out.println("awake nodes of normal nodes:");
		Iterator<Integer> it=awake.keySet().iterator();
		while (it.hasNext()) {
			Integer id = (Integer) it.next();
			if(awake.get(id)){
				System.out.println("ID: "+id);
			}
			else{
				
			}
			
		}
	}

	private void printSolarNodesBatteryRemain() {
		 LinkedList<SolarNode> solarNodesList=getSolarNodes();
		 Iterator<SolarNode> itSolarNodes=solarNodesList.iterator();
		 while (itSolarNodes.hasNext()) {
		 SolarNode solarNode = (SolarNode) itSolarNodes.next();
		 	if(solarNode.getBattery() < 10 ){
		 		System.out.println("Type1 solar node id:" + solarNode.getID()+" battery:"+solarNode.getBattery()+ " Tr:"+solarNode.getMaxTR());
		 	}else{
		 		System.out.println("Type2 solar node id:" + solarNode.getID()+" battery:"+solarNode.getBattery()+ " Tr:"+solarNode.getMaxTR());
		 	}
		 }
	}

	/**
	 * Get all solar nodes array
	 * 
	 * @return solar nodes array
	 * @author panhao
	 */
	public LinkedList<SolarNode> getSolarNodes() {
		LinkedList<SolarNode> solarNodesList = new LinkedList<SolarNode>();
		if (isNeedInitialization()) {
			initializeWork();
		}
		Collection<VNode> nodesCollection = wsn.getAllNodes();
		Iterator<VNode> it = nodesCollection.iterator();
		while (it.hasNext()) {
			VNode temp = it.next();
			if (wsn.getNodeByID(temp.getID()).getClass().getSimpleName().contains("Solar")) {
				solarNodesList.add((SolarNode) temp);
			}
		}
		return solarNodesList;
	}

	/**
	 * Get all normal nodes
	 * 
	 * @return normal sensor nodes
	 * @author panhao
	 */
	public LinkedList<SensorNode_Normal> getNormalNodes() {
		LinkedList<SensorNode_Normal> normalNodesList = new LinkedList<SensorNode_Normal>();
		if (isNeedInitialization()) {
			initializeWork();
		}
		Collection<VNode> nodesCollection = wsn.getAllNodes();
		Iterator<VNode> it = nodesCollection.iterator();
		while (it.hasNext()) {
			VNode temp = it.next();
			if (wsn.getNodeByID(temp.getID()).getClass().getSimpleName().contains("Sensor")) {
				normalNodesList.add((SensorNode_Normal) temp);
			}
		}
		return normalNodesList;
	}

	/**
	 * EC-CKN Algorithm
	 * 
	 * @author HuliKun
	 */
	private void ec_ckn_function() {
		initializeNeighbors();
		initializeNeighborsOf2Hops();
		int[] disordered = Util.generateDisorderedIntArrayWithExistingArray(getJoinECKNodeIds());
		for (int i = 0; i < disordered.length; i++) {
			int currentID = disordered[i];
			Integer[] Nu = getAwakeNeighbors(currentID);// Nu is the currentAwakeNeighbor
			if (Nu.length < k || isOneOfAwakeNeighborsNumLessThanK(currentID)) {
				this.setAwake(currentID, true);
				if (wsn.getNodeByID(currentID).getClass().getSimpleName().contains("Solar")) {
					updateAwakeSolarNodeBattery(currentID);
				}
				else{
					updateAwakeNormalNodeBattery(currentID);
				}

			} else {
				Integer[] Cu = getCu(currentID);// Cu is related to the Erank
				int[] awakeNeighborsOf2HopsMoreThanEranku = Util.IntegerArray2IntArray(getAwakeNeighborsOf2HopsMoreThanEranku(currentID));
				if (atLeast_k_Neighbors(Nu, Cu) && qualifiedConnectedInCu(Cu, awakeNeighborsOf2HopsMoreThanEranku)) {
					if (wsn.getNodeByID(currentID).getClass().getSimpleName().contains("Solar")) {
						setAwake(currentID, true);
					} else {
						updateSleepNormalNodeBattery(currentID);
						setAwake(currentID, false); // sleep
					}

				} else {
					if (wsn.getNodeByID(currentID).getClass().getSimpleName().contains("Solar")) {
						setAwake(currentID, true);
						updateAwakeSolarNodeBattery(currentID);
					} else {
						setAwake(currentID, false);//wondy修改
						updateAwakeNormalNodeBattery(currentID);
					}

				}
			}
		}
	}

	private void re_ec_ckn_function() {
		//initializeNeighbors();
		//initializeNeighborsOf2Hops();
		int[] disordered = Util.generateDisorderedIntArrayWithExistingArray(getJoinECKNodeIds());
		for (int i = 0; i < disordered.length; i++) {
			int currentID = disordered[i];
			Integer[] Nu = getAwakeNeighbors(currentID);// Nu is the currentAwakeNeighbor
			if (Nu.length < k || isOneOfAwakeNeighborsNumLessThanK(currentID)) {
				this.setAwake(currentID, true);
				if (wsn.getNodeByID(currentID).getClass().getSimpleName().contains("Solar")) {
					//updateAwakeSolarNodeBattery(currentID);
				}
				else{
					//updateAwakeNormalNodeBattery(currentID);
				}

			} else {
				Integer[] Cu = getCu(currentID);// Cu is related to the Erank
				int[] awakeNeighborsOf2HopsMoreThanEranku = Util.IntegerArray2IntArray(getAwakeNeighborsOf2HopsMoreThanEranku(currentID));
				if (atLeast_k_Neighbors(Nu, Cu) && qualifiedConnectedInCu(Cu, awakeNeighborsOf2HopsMoreThanEranku)) {
					if (wsn.getNodeByID(currentID).getClass().getSimpleName().contains("Solar")) {
						setAwake(currentID, true);
					} else {
						//updateSleepNormalNodeBattery(currentID);
						setAwake(currentID, false); // sleep
					}

				} else {
					if (wsn.getNodeByID(currentID).getClass().getSimpleName().contains("Solar")) {
						setAwake(currentID, true);
						//updateAwakeSolarNodeBattery(currentID);
					} else {
						setAwake(currentID, false);//wondy修改
						//updateAwakeNormalNodeBattery(currentID);
					}

				}
			}
		}
	}
	/**
	 * SSEH algorithm
	 * 
	 * @author panhao
	 */
	private void SSEH_Function() {
		// initialize
		if (!isinit) {
			initAllNodesBattery();
			isinit = true;
		}
		// check the solar node
		checkTheSolarNodes();
		// printECKNodesIds();
		// run ec-ckn function
		 ec_ckn_function();
		 //printSleepSolarNumbers();
		// printSolarNodesBatteryRemain();
		// check the level of solar nodes
		//getCoverageDegree();
		 //printSleepSolarIds();
		 //printAwakeNodes();
		 //checkTheSolarNodesBatteryLevel();
		 //printSolarNodesBatteryRemain();
	}

	public Algorithm getAlgorithm() {
		return algorithm;
	}

	public boolean isNeedInitialization() {
		return needInitialization;
	}

	public void setNeedInitialization(boolean needInitialization) {
		this.needInitialization = needInitialization;
	}

	public int getK() {
		return k;
	}

	public void setK(int s) {
		this.k = s;
	}

	public int getLifeTime() {
		return lifeTime;
	}

	public void setLifeTime(int lifeTime) {
		this.lifeTime = lifeTime;
	}

	/**********************************************************************/
	public double getEs_value() {
		return Es_value;
	}

	public void setEs_value(double es_value) {
		Es_value = es_value;
	}
	
	public double getDegreeTarget() {
		return TargetDegree;
	}

	public void setDegreeTarget(double targetDegree) {
		TargetDegree = targetDegree;
	}

	public double getEc_value() {
		return Ec_value;
	}

	public void setEc_value(double ec_value) {
		Ec_value = ec_value;
	}

	public static int getTR() {
		return TR;
	}

	public static void setTR(int tR) {
		TR = tR;
	}
}
