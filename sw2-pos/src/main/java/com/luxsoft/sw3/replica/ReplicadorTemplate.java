package com.luxsoft.sw3.replica;


import org.apache.log4j.Logger;
import org.springframework.orm.hibernate3.HibernateTemplate;

/**
 * Plantilla para facilitar la generacion de replicadores
 * 
 * @author Ruben Cancino Ramos
 *
 */
public class ReplicadorTemplate {
	
	private HibernateTemplate sourceTemplate;
	private HibernateTemplate targetTemplate;
	
	protected Logger logger=Logger.getLogger(getClass());
	
	public HibernateTemplate getSourceTemplate() {
		return sourceTemplate;
	}
	public void setSourceTemplate(HibernateTemplate sourceTemplate) {
		this.sourceTemplate = sourceTemplate;
	}
	public HibernateTemplate getTargetTemplate() {
		return targetTemplate;
	}
	public void setTargetTemplate(HibernateTemplate targetTemplate) {
		this.targetTemplate = targetTemplate;
	}
	
	

		
	

}
