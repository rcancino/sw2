package com.luxsoft.sw2.replica;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.hibernate.Hibernate;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.stereotype.Service;

import com.luxsoft.siipap.cxc.model.Abono;
import com.luxsoft.utils.LoggerHelper;

@Service("replicaManager")
public class ReplicaManagerImpl implements ReplicaManager{
	
	
	private Map<Class, Replicador> replicadores=new HashMap<Class, Replicador>();
	
	Logger logger=LoggerHelper.getLogger();
	
	private Replicador defaultReplicador;
	
	public void replicar(Collection beans){
		for(Object bean:beans){
			replicar(bean);
		}
	}
	
	public void replicar(Object bean){
		
		Replicador replicador=replicadores.get(bean.getClass());
		if(replicador!=null){
			replicador.replicar(bean);
		}else{
			defaultReplicador.replicar(bean);
		}
		
	}
	
	public void replicaBatch(Collection beans) {
		
	}
	

	
	public Map<Class, Replicador> getReplicadores() {
		return replicadores;
	}

	public void setReplicadores(Map<Class, Replicador> replicadores) {
		this.replicadores = replicadores;
	}

	@Required
	public Replicador getDefaultReplicador() {
		return defaultReplicador;
	}

	public void setDefaultReplicador(Replicador defaultReplicador) {
		this.defaultReplicador = defaultReplicador;
	}
	
	

}
