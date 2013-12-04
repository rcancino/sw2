package com.luxsoft.sw3.replica;

import java.io.Serializable;

import org.hibernate.event.PostDeleteEvent;
import org.hibernate.event.PostInsertEvent;
import org.hibernate.event.PostUpdateEvent;
import org.springframework.util.ClassUtils;

public class EntityLog implements Serializable{
	
	Object bean;
	Tipo tipo;
	final Serializable id;
	final String sucursalOrigen;
	
	public EntityLog(Object bean,  Serializable id,String sucursalOrigen,Tipo tipo) {		
		this.bean = bean;
		this.tipo = tipo;
		this.id = id;
		this.sucursalOrigen=sucursalOrigen;
	}
	
	public EntityLog(PostInsertEvent event,String sucursalOrigen){
		this.bean = event.getEntity();
		this.tipo = Tipo.ALTA;
		this.id = event.getId();
		this.sucursalOrigen=sucursalOrigen;
	}
	
	public EntityLog(PostUpdateEvent event,String sucursalOrigen){
		this.bean = event.getEntity();
		this.tipo = Tipo.CAMBIO;
		this.id = event.getId();
		this.sucursalOrigen=sucursalOrigen;
	}
	
	public EntityLog(PostDeleteEvent event,String sucursalOrigen){
		this.bean = event.getEntity();
		this.tipo = Tipo.BAJA;
		this.id = event.getId();
		this.sucursalOrigen=sucursalOrigen;
	}
	
	public void setBean(Object bean) {
		this.bean = bean;
	}

	public Object getBean() {
		return bean;
	}

	public Tipo getTipo() {
		return tipo;
	}
	public void setTipo(Tipo tipo) {
		this.tipo = tipo;
	}

	public Serializable getId() {
		return id;
	}
	public String getSucursalOrigen() {
		return sucursalOrigen;
	}

	public String toString(){
		return ClassUtils.getShortName(getBean().getClass())+ " Id: "+getId()+ " Tipo: "+getTipo()+ " Suc origen: "+getSucursalOrigen();
	}
	
	public String getDestinationOut(){
		//return ClassUtils.getShortName(getBean().getClass())+"QUEUE";
		return "REPLICA.QUEUE";
	}
	

	public static enum Tipo{
		ALTA,BAJA,CAMBIO
	}
}
