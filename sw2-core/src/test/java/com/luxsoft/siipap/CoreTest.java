package com.luxsoft.siipap;

import java.math.BigDecimal;
import java.math.RoundingMode;

import junit.framework.TestCase;

public class CoreTest extends TestCase {
    public void testGetHello() throws Exception {
        assertEquals("Hello", Core.getHello());
    }
    
    public void testBigDecimal(){
    	BigDecimal val=BigDecimal.valueOf(124352/4.25);
    	BigDecimal res=val.setScale(2,RoundingMode.HALF_EVEN);
    	System.out.println(res.precision());
    	System.out.println(val);
    	System.out.println(res);
    }
}