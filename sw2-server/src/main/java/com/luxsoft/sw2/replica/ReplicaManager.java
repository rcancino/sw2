package com.luxsoft.sw2.replica;

import java.util.Collection;

/**
 * Interface central para proporcionar y configurar los servicios de replica
 * 
 * 
 * @author Ruben Cancino 
 *
 */
public interface ReplicaManager {
	
	public void replicar(Collection beans);
	
	public void replicar(Object bean);
	
	public void replicaBatch(Collection beans);
	
	

}
