package com.luxsoft.siipap.replica;

import java.util.Collection;



public interface ReplicaExporter {
	
	
	
	public Class getBeanClass();
	
	public String export(Object bean,Tipo tipo);
	
	public String export(Object bean,Tipo tipo,Object...objects );
	
	public String export(Collection beans,Tipo tipo);
	
	public enum Tipo{
		A,B,C;
	}

}
