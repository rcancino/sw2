package com.luxsoft.sw3.model;

/**
 * Determina si una entidad requiere autorizacion para su pesistencia
 * 
 * @author Ruben Cancino
 *
 */
public interface Evaluador {
	
	/**
	 * Determina si la entidad requiere de autorizacion para ser salvada
	 * Si lo requiere regresa un String con la descripcion corta que explica el por que
	 * requiere autorizacion
	 * 
	 * @param entidad
	 * @return Una descripcion de la razon por la que requiere autorización o nulo si no se requiere
	 */
	public String requiereParaSalvar(Object entidad);
	
	/**
	 * Determina si la entidad requiere de autorizacion para ser actualizada
	 * Si lo requiere regresa un String con la descripcion corta que explica el por que
	 * requiere autorizacion
	 * 
	 * @param entidad
	 * @return Una descripcion de la razon por la que requiere autorización o nulo si no se requiere
	 */
	public String requiereParaActualizar(Object entidad);
	
	/**
	 * Determina si la entidad requiere de autorizacion para ser eliminada
	 * Si lo requiere regresa un String con la descripcion corta que explica el por que
	 * requiere autorizacion
	 * @param entidad
	 * @return Una descripcion de la razon por la que requiere autorización o nulo si no se requiere
	 */
	public String requiereParaEliminar(Object entidad);

}
