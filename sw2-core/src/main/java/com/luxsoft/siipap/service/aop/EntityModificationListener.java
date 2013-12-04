package com.luxsoft.siipap.service.aop;

/**
 * Interfaz para todo aquel que quiera ser notificado
 * de cambios en entidades persistidas 
 * 
 * @author Ruben Cancino Ramos
 *
 */
public interface EntityModificationListener {
	
	public void onEntityModification(final EntityModificationEvent event);

}
