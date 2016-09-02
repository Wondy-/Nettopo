package org.deri.nettopo.test;

import java.math.BigDecimal;

import org.deri.nettopo.util.RandomValue;
import org.junit.Test;

public class TestSSEH {

	@Test
	public void randomValueTest() {
		for(int i=0;i<100;i++){
			System.out.println(RandomValue.getInstance().getIntRandomValue(50, 20));
		}
		System.out.println("####");
		for(int i=0;i<100;i++){
			System.out.println(RandomValue.getInstance().getIntRandomValue(50, 20));
		}
	}

	@Test
	public void muti(){
		int lv_1=4;
		int lv_2=10;
		double all=lv_2+lv_1;
		double res=lv_2/all*10;
		System.out.println(res);
	}
	@Test
	public void doubleToInt(){
		BigDecimal bg = new BigDecimal(1.6964623039317601);  
        double f1 = bg.setScale(3, BigDecimal.ROUND_HALF_UP).doubleValue();  
        System.out.println(f1);  
	}
	
	@Test
	public void compare(){
		if(4.500154213061377>2.0){
			System.out.println("right");
		}
	}
}
