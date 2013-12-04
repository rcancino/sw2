package com.luxsoft.sw2.replica;

import java.util.Collection;

/**
 * Replicador por bloques
 * 
 * @author Ruben Cancino 
 *
 */
public interface ReplicadorBatch {
	
	
	public void replicar(Collection beans);

}
