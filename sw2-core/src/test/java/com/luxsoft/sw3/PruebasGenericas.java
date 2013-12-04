package com.luxsoft.sw3;

import java.util.Map;

import com.luxsoft.siipap.service.ServiceLocator2;

public class PruebasGenericas {
	
	public static void main(String[] args) {
		Map map=ServiceLocator2.getSessionFactory().getAllClassMetadata();
		for(Object  entry:map.entrySet()){
			System.out.println(entry);
		}
	}

}
