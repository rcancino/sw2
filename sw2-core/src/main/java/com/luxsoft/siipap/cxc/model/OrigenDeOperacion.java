package com.luxsoft.siipap.cxc.model;

/**
 * Clasifica si la operiacion originada es de tipo Credito,Camioneta
 * o Mostrador
 * @author RUBEN
 *
 */
public enum OrigenDeOperacion {
	
	 CRE,MOS,CAM,CHE,JUR;
	 
	 public String toString(){
		 return toString(this);
	 }
	 
	 public static String toString(OrigenDeOperacion o){
		 switch (o) {
		case CRE:
			return "CREDITO";
		case CAM:
			return "CAMIONETA";
		case MOS:
			return "MOSTRADOR";
		default:
			return o.name();
		}
	 }
	 
	 public String getShortName(){
		 return name();
	 }

}
