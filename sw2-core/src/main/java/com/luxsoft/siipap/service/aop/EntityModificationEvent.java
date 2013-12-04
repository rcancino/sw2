/**
 * 
 */
package com.luxsoft.siipap.service.aop;

import java.io.Serializable;
import java.text.MessageFormat;
import java.util.Date;

import org.springframework.context.ApplicationEvent;
import org.springframework.util.ClassUtils;

public class EntityModificationEvent extends ApplicationEvent{
	
	private final String entityClassName;
	private final Serializable id;
	private Date time;
	private EntityEventType type;
	
	public EntityModificationEvent(Object source, Class entity,Serializable id) {
		super(source);
		this.entityClassName = ClassUtils.getQualifiedName(entity);
		this.id = id;
	}

	public String getEntityClassName() {
		return entityClassName;
	}

	public Serializable getId() {
		return id;
	}
	
	
	public Date getTime() {
		return time;
	}

	public void setTime(Date time) {
		this.time = time;
	}
	
	

	public EntityEventType getType() {
		return type;
	}

	public void setType(EntityEventType type) {
		this.type = type;
	}

	public String toString(){
		String pattern="{0} :{1} Id:{2} Time:{3,date,long}";
		return MessageFormat.format(pattern
				, getType()
				,ClassUtils.getShortName(getEntityClassName())
				,getId()
				,getTime());
	}
	
	public static enum EntityEventType{
		INSERT,UPDATE,DELETE
	}
	
}