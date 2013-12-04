package com.luxsoft.sw3.cfd.ui.validaciones;

/**
 * Bean para representar el resultado de una validacion
 * 
 * @author Ruben Cancino Ramos
 *
 */
public class Resultado {
	
	public String tipo;
	public String resultado;
	public String descripcion;
	
	public static String CORRECTO="CORRECTO";
	
	public Resultado(String tipo) {
		super();
		this.tipo = tipo;
	}
	public String getTipo() {
		return tipo;
	}
	public void setTipo(String tipo) {
		this.tipo = tipo;
	}
	public String getResultado() {
		return resultado;
	}
	public void setResultado(String resultado) {
		this.resultado = resultado;
	}
	public String getDescripcion() {
		return descripcion;
	}
	public void setDescripcion(String descripcion) {
		this.descripcion = descripcion;
	}
	

}
