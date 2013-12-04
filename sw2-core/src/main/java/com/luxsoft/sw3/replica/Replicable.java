package com.luxsoft.sw3.replica;

import java.io.Serializable;
import java.util.Date;

/**
 * TAG interface para marcar las instancias q son replicables
 * 
 * @author Ruben Cancino
 *
 */
public interface Replicable extends Serializable{
	
	public Date getReplicado();
	
	public void setReplicado(final Date time);
	
	public Date getImportado();
	
	public void setImportado(final Date time);
	

}
