package com.luxsoft.siipap.jms.rpc.test;

import java.io.Serializable;
import java.text.MessageFormat;
import java.util.Date;

import org.springframework.util.ClassUtils;

/**
 * Informacion de modificacion sobre una entidad
 * 
 * @author Ruben Cancino Ramos
 *
 */
public class EntityModificationInfo implements Serializable{
	
	private Class entityClass;
	private Serializable id;
	private Date time;
	private String tipo;
	
	public EntityModificationInfo(){}

	public EntityModificationInfo(Class entity, Serializable id) {
		this.entityClass=entity;
		this.id = id;
	}
	
	public Class getEntityClass() {
		return entityClass;
	}

	public void setEntityClass(Class entityClass) {
		this.entityClass = entityClass;
	}

	public Serializable getId() {
		return id;
	}

	public void setId(Serializable id) {
		this.id = id;
	}

	public Date getTime() {
		return time;
	}

	public void setTime(Date time) {
		this.time = time;
	}
	
	
	public String getTipo() {
		return tipo;
	}

	public void setTipo(Tipo tipo) {
		this.tipo = tipo.name();
	}
	
	public void setTipo(String tipo){
		this.tipo=tipo;
	}


	public String toString(){
		String pattern="{0} :{1} Id:{2} Time:{3,date,long}";
		return MessageFormat.format(pattern
				, getTipo()
				,ClassUtils.getShortName(getEntityClass())
				,getId()
				,getTime());
	}
	
	
	public static enum Tipo{
		INSERT,UPDATE,DELETE
	}
	

}
