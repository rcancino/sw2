package com.luxsoft.sw3.model;

import java.util.List;

import org.apache.commons.lang.StringUtils;

/**
 * Implementacion de {@link Evaluador} delegando a una lista de {@link Evaluador}
 * para ser ejecutados en cadena
 * 
 * @author Ruben Cancino
 *
 */
public  class EvaluadorGenerico  implements Evaluador{

	private List<Evaluador> autirizables;

	
	public String requiereParaActualizar(Object entidad) {		
		StringBuffer sb=new StringBuffer();
		for(Evaluador a:getAutirizables()){
			String r=a.requiereParaActualizar(entidad);
			if(r!=null){
				sb.append(r);
				sb.append("  ");
			}
		}
		String res=sb.toString();
		return StringUtils.isBlank(res)?null:res;
	}

	
	public String requiereParaEliminar(Object entidad) {
		StringBuffer sb=new StringBuffer();
		for(Evaluador a:getAutirizables()){
			String r=a.requiereParaEliminar(entidad);
			if(r!=null){
				sb.append(r);
				sb.append("  ");
			}
		}
		String res=sb.toString();
		return StringUtils.isBlank(res)?null:res;
	}

	
	public String requiereParaSalvar(Object entidad) {
		StringBuffer sb=new StringBuffer();
		for(Evaluador a:getAutirizables()){
			String r=a.requiereParaSalvar(entidad);
			if(r!=null){
				sb.append(r);
				sb.append("  ");
			}
		}
		String res=sb.toString();
		return StringUtils.isBlank(res)?null:res;
	}

	public List<Evaluador> getAutirizables() {
		return autirizables;
	}

	public void setAutirizables(List<Evaluador> autirizables) {
		this.autirizables = autirizables;
	}
	
	
	
}
