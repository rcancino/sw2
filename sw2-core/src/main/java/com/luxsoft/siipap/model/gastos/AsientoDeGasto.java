package com.luxsoft.siipap.model.gastos;

import java.text.MessageFormat;

import org.apache.commons.lang.StringUtils;

import com.luxsoft.siipap.model.CantidadMonetaria;

/**
 * Asiento contable para una poliza de gastos
 * 
 * @author RUBEN
 *
 */
public class AsientoDeGasto {
	
	private String cuenta;
	private String concepto;
	private String descripcion;
	private String sucursal;
	private CantidadMonetaria debe=CantidadMonetaria.pesos(0);
	private CantidadMonetaria haber=CantidadMonetaria.pesos(0);
	
	public AsientoDeGasto(){
		
	}
	/*
	public AsientoDeGasto(final List<E>GCompraDet det){
		registrarCuentaContable(det);
		this.concepto=det.getRubro().getRubroCuentaOrigen().getDescripcion();
		this.concepto=StringUtils.substring(this.concepto, 0,28);
		registrarDescripcion(det);
		debe=det.getImporteMN().abs();
		sucursal=det.getSucursal().getNombre();
	}
	*/
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
		this.concepto = concepto;
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
		this.descripcion = descripcion;
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
	}
	
	public String getSucursal() {
		return sucursal;
	}
	public void setSucursal(String sucursal) {
		this.sucursal = sucursal;
	}
	
	public void registrarCuentaContable(GCompraDet det){
		
		String cc=det.getRubro().getCuentaOrigen();
		String prefix=StringUtils.substring(cc,0,1);
		String suffix=StringUtils.substring(cc,3,cc.length());
		
		String suc=String.valueOf(det.getSucursal().getClaveContable());		
		suc=StringUtils.leftPad(suc, 2,'0');
		
		String cta=prefix+suc+suffix;
		this.cuenta=cta;
	}
	
	public  void registrarDescripcion(GCompraDet det){
		String pattern="F-{0} {1}";
		this.descripcion=MessageFormat.format(pattern, det.getFactura(),det.getProducto().getDescripcion());
		this.descripcion=StringUtils.substring(this.descripcion, 0,28);
		
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
	

}
