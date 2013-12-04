package com.luxsoft.siipap.inventarios.model;

/**
 * Enumeracion de movimientos generados por documentos de otros
 * modulos 
 * 
 * @author Ruben Cancino
 *
 */
public enum MovimientosExternos {
	
	COM,DEC,FAC,RMD;
	
	public static boolean existe(String val){
		for(MovimientosExternos m:values()){
			if(m.name().equals(val))
				return true;
		}
		return false;
	}

}
