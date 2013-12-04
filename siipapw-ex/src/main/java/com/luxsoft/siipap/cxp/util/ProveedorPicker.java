/**
 * 
 */
package com.luxsoft.siipap.cxp.util;

import java.util.Date;

import com.luxsoft.luxor.utils.Bean;
import com.luxsoft.siipap.model.Periodo;
import com.luxsoft.siipap.model.core.Proveedor;

public class ProveedorPicker{
	
	
	private Proveedor proveedor;
	
	private Date fechaInicial;
	
	private Date fechaFinal;
	
	public ProveedorPicker(){
		Periodo per=Periodo.getPeriodoDelMesActual();
		fechaInicial=per.getFechaInicial();
		fechaFinal=per.getFechaFinal();
	}

	public Proveedor getProveedor() {
		return proveedor;
	}
	public void setProveedor(Proveedor proveedor) {
		this.proveedor = proveedor;
	}

	public Date getFechaInicial() {
		return fechaInicial;
	}
	public void setFechaInicial(Date fechaInicial) {
		this.fechaInicial = fechaInicial;
	}

	
	
	public Date getFechaFinal() {
		return fechaFinal;
	}

	public void setFechaFinal(Date fechaFinal) {
		this.fechaFinal = fechaFinal;
	}

	/**
	 * FactoryMethod para la creacion de instancias
	 * 
	 * @return
	 */
	public static ProveedorPicker getNewInstance(){
		return (ProveedorPicker)Bean.proxy(ProveedorPicker.class);
	}
}