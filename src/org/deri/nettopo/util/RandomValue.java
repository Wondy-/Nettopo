package org.deri.nettopo.util;

import java.util.Random;

public class RandomValue {
	private static RandomValue instance;
	public RandomValue() {
		// TODO Auto-generated constructor stub
	}
	public static RandomValue getInstance() {
		if(instance==null){
			synchronized (RandomValue.class) {
				if(instance==null){
					instance=new RandomValue();
				}
			}
		}
		return instance;
	}
	
	public int getIntRandomValue(int max,int min){
		Random rand = new Random();  
		return rand.nextInt(max)%(max-min+1) + min;
	}
}
