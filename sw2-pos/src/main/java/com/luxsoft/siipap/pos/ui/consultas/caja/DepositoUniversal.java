/**
 * 
 */
package com.luxsoft.siipap.pos.ui.consultas.caja;

import java.math.BigDecimal;
import java.util.Date;

public class DepositoUniversal {
	private String TIPO_VTA;
	private String SUCURSAL;
	private int FOLIO;
	private String SOLICITO;
	private Date FECHA;
	private String CLAVE;
	private String NOMBRE;
	private String ORIGEN;
	private Date FECHA_DEPOSITO;
	private String REFERENCIA;
	private String FPAGO;
	private String BANCO;
	private BigDecimal TOTAL;
	private String BANCO_DEST;
	private String STATUS;       
	private String COMENTARIO;
	
	public String getTIPO_VTA(){
		return TIPO_VTA;
	}
	public void setTIPO_VTA(String vta){
		this.TIPO_VTA=vta;
	}
	
	public String getSUCURSAL() {
		return SUCURSAL;
	}
	public void setSUCURSAL(String sucursal) {
		SUCURSAL = sucursal;
	}
	public int getFOLIO() {
		return FOLIO;
	}
	public void setFOLIO(int folio) {
		FOLIO = folio;
	}
	public Date getFECHA() {
		return FECHA;
	}
	public void setFECHA(Date fecha) {
		FECHA = fecha;
	}
	public String getCLAVE() {
		return CLAVE;
	}
	public void setCLAVE(String clave) {
		CLAVE = clave;
	}
	public String getNOMBRE() {
		return NOMBRE;
	}
	public void setNOMBRE(String nombre) {
		NOMBRE = nombre;
	}
	public String getORIGEN() {
		return ORIGEN;
	}
	public void setORIGEN(String origen) {
		ORIGEN = origen;
	}
	
	public Date getFECHA_DEPOSITO() {
		return FECHA_DEPOSITO;
	}
	public void setFECHA_DEPOSITO(Date fecha_deposito) {
		FECHA_DEPOSITO = fecha_deposito;
	}
	public String getREFERENCIA() {
		return REFERENCIA;
	}
	public void setREFERENCIA(String referencia) {
		REFERENCIA = referencia;
	}
	public String getFPAGO() {
		return FPAGO;
	}
	public void setFPAGO(String fpago) {
		FPAGO = fpago;
	}
	public String getBANCO() {
		return BANCO;
	}
	public void setBANCO(String banco) {
		BANCO = banco;
	}
	public BigDecimal getTOTAL() {
		return TOTAL;
	}
	public void setTOTAL(BigDecimal total) {
		TOTAL = total;
	}
	public String getBANCO_DEST() {
		return BANCO_DEST;
	}
	public void setBANCO_DEST(String banco_dest) {
		BANCO_DEST = banco_dest;
	}
	public String getSTATUS() {
		return STATUS;
	}
	public void setSTATUS(String status) {
		STATUS = status;
	}
	public String getCOMENTARIO() {
		return COMENTARIO;
	}
	public void setCOMENTARIO(String comentario) {
		COMENTARIO = comentario;
	}
	public String getSOLICITO() {
		return SOLICITO;
	}
	public void setSOLICITO(String solicito) {
		SOLICITO = solicito;
	}
	
	
}