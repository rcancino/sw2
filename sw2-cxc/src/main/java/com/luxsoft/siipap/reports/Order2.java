package com.luxsoft.siipap.reports;

import java.util.ArrayList;
import java.util.List;

public enum Order2{
		credito("CREDITO","CRE"),
		camioneta("CAMIONETA","CAM"),
		mostrador("MOSTRADOR","MOS"),
		ambos("TODOS","%"),;
		
		private final String item;
		private final String valor;
		
		private Order2(final String item, final String valor) {
			this.item = item;
			this.valor = valor;
		}
		
		public String toString(){
			return item;
		}
		
		public String getValor(){
			return valor;
		}
		
		public Integer[] todos(){
			return new Integer[]{1,4,6,7,8};
		}
		
		public static List<Order2> getOrder2(){
			ArrayList<Order2> l=new ArrayList<Order2>();
			for(Order2 c:values()){			
				l.add(c);
			}
			return l;
		}
		
		public static Order2 getOrder2(String id){
			for(Order2 c:values()){
				if(c.getValor()==id)
					return c;
			}
			return null;
		}
		
	}