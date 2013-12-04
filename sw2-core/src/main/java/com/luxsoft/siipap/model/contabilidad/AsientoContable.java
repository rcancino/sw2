package com.luxsoft.siipap.model.contabilidad;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

import com.luxsoft.siipap.model.CantidadMonetaria;

/**
 * Asiento contable para una poliza de gastos
 * 
 * @author RUBEN
 *
 */
public class AsientoContable {
	
	private Poliza poliza;
	private String cuenta;
	private String concepto;
	private String descripcion="";
	private String descripcion2;
	private String descripcion3;
	private String agrupador;
	private String sucursal;
	private CantidadMonetaria debe=CantidadMonetaria.pesos(0);
	private CantidadMonetaria haber=CantidadMonetaria.pesos(0);
	private String tipo;
	private int orden;
	
	public AsientoContable(){
		
	}
	
	public Poliza getPoliza() {
		return poliza;
	}

	public void setPoliza(Poliza poliza) {
		this.poliza = poliza;
	}



	/**
	 * @return the concepto
	 */
	public String getConcepto() {
		return concepto;
	}
	/**
	 * @param concepto the concepto to set
	 */
	public void setConcepto(String concepto) {		
		this.concepto = StringUtils.substring(concepto, 0,28);;
	}
	/**
	 * @return the cuenta
	 */
	public String getCuenta() {
		return cuenta;
	}
	/**
	 * @param cuenta the cuenta to set
	 */
	public void setCuenta(String cuenta) {
		this.cuenta = cuenta;
	}
	/**
	 * @return the debe
	 */
	public CantidadMonetaria getDebe() {
		return debe;
	}
	/**
	 * @param debe the debe to set
	 */
	public void setDebe(CantidadMonetaria debe) {
		this.debe = debe;
		if(debe!=null)
			if(debe.amount().doubleValue()!=0)
				this.tipo="D";
		
	}
	/**
	 * @return the descripcion
	 */
	public String getDescripcion() {
		return descripcion;
	}
	/**
	 * @param descripcion the descripcion to set
	 */
	public void setDescripcion(String descripcion) {		
		this.descripcion = StringUtils.substring(descripcion, 0,28);
	}
	/**
	 * @return the haber
	 */
	public CantidadMonetaria getHaber() {
		return haber;
	}
	/**
	 * @param haber the haber to set
	 */
	public void setHaber(CantidadMonetaria haber) {
		this.haber = haber;
		if(haber!=null)
			if(haber.amount().doubleValue()!=0)
				this.tipo="H";
	}
	
	public String getSucursal() {
		return sucursal;
	}
	public void setSucursal(String sucursal) {
		this.sucursal = sucursal;
	}
	
	
	
	public double getDebeAsDouble(){
		return debe.amount().doubleValue();
	}
	public double getHaberAsDouble(){
		return haber.amount().doubleValue();
	}
	
	
	public String getImporteAsString(){
		if(getDebe().amount().doubleValue()!=0){ //Cargo
			double valor=getDebe().amount().doubleValue();
			return String.valueOf(valor)+",1";
		}else{
			double valor=getHaber().amount().doubleValue();
			return " Abono:"+String.valueOf(valor)+",1";
		}
		
	}
	
	public boolean isCargo(){
		return getDebe().amount().doubleValue()!=0;
	}

	public String getDescripcion2() {
		return descripcion2;
	}

	public void setDescripcion2(String descripcion2) {
		this.descripcion2 = descripcion2;
	}

	public String getDescripcion3() {
		return descripcion3;
	}

	public void setDescripcion3(String descripcion3) {
		this.descripcion3 = descripcion3;
	}
	
	
	
	public String getAgrupador() {
		return agrupador;
	}

	public void setAgrupador(String agrupador) {
		this.agrupador = agrupador;
	}
	
	

	public String getTipo() {
		return tipo;
	}

	public void setTipo(String tipo) {
		this.tipo = tipo;
	}

	public String toString(){
		return ToStringBuilder.reflectionToString(this,ToStringStyle.SHORT_PREFIX_STYLE);
	}

	public int getOrden() {
		return orden;
	}

	public void setOrden(int orden) {
		this.orden = orden;
	}
	
	

}
