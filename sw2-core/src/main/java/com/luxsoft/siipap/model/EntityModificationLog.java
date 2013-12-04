package com.luxsoft.siipap.model;

import java.util.Date;

/**
 * Interface que deben implementar las entidades interesadas
 * en registrar bitacoras
 *  
 * 
 * @author Ruben Cancino Ramos
 *
 */
public interface EntityModificationLog {
	
	

	public String getCreateUser();
	
	public void setCreateUser(String createUser);

	public String getUpdateUser();

	public void setUpdateUser(String updateUser);
	
	public Date getCreado() ;
	
	public void setCreado(Date creado);

	public Date getModificado() ;

	public void setModificado(Date modificado);
	
	
	

}
