package com.luxsoft.sw3.cfd;

import com.luxsoft.siipap.ventas.model.VentaDet;

public class FacturaDetRow {
	
	
	private double CANTIDAD;
	private String DESCRIPCION;
	private Number KXM;
	private Number GRAMOS;
	private Number PRECIO;
	private Number IMPORTE;
	private String CORTES_INSTRUCCION;
	
	public FacturaDetRow(VentaDet det){
		setCANTIDAD(det.getCantidad());
		setDESCRIPCION(det.getDescripcion());
	}
	
	public double getCANTIDAD() {
		return CANTIDAD;
	}
	public void setCANTIDAD(double cantidad) {
		CANTIDAD = cantidad;
	}
	public String getDESCRIPCION() {
		return DESCRIPCION;
	}
	public void setDESCRIPCION(String descripcion) {
		DESCRIPCION = descripcion;
	}
	public Number getKXM() {
		return KXM;
	}
	public void setKXM(Number kxm) {
		KXM = kxm;
	}
	public Number getGRAMOS() {
		return GRAMOS;
	}
	public void setGRAMOS(Number gramos) {
		GRAMOS = gramos;
	}
	public Number getPRECIO() {
		return PRECIO;
	}
	public void setPRECIO(Number precio) {
		PRECIO = precio;
	}
	public Number getIMPORTE() {
		return IMPORTE;
	}
	public void setIMPORTE(Number importe) {
		IMPORTE = importe;
	}
	public String getCORTES_INSTRUCCION() {
		return CORTES_INSTRUCCION;
	}
	public void setCORTES_INSTRUCCION(String cortes_instruccion) {
		CORTES_INSTRUCCION = cortes_instruccion;
	}
	
	

}
