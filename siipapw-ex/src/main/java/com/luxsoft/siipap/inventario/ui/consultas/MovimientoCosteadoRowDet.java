package com.luxsoft.siipap.inventario.ui.consultas;

import java.math.BigDecimal;
import java.util.Date;

public class MovimientoCosteadoRowDet {
	
	private String GRUPO;
	private String TIPO;
	private String CLAVE;
	private Date FECHA;
	private Long DOCTO;
	private Long RENGL;
	private String NOMBRE;
	private String UNIDAD;
	private Double CANTIDAD;
	private Double KILOS;
	private Double COSTO;
	private BigDecimal COSTOP;
	private Double IMPORTECOSTO;
	private Long YEAR;
	private Long MES;
	private String COMENTARIO;
	public String getGRUPO() {
		return GRUPO;
	}
	public void setGRUPO(String grupo) {
		GRUPO = grupo;
	}
	public String getTIPO() {
		return TIPO;
	}
	public void setTIPO(String tipo) {
		TIPO = tipo;
	}
	public String getCLAVE() {
		return CLAVE;
	}
	public void setCLAVE(String clave) {
		CLAVE = clave;
	}
	public Date getFECHA() {
		return FECHA;
	}
	public void setFECHA(Date fecha) {
		FECHA = fecha;
	}
	public Long getDOCTO() {
		return DOCTO;
	}
	public void setDOCTO(Long docto) {
		DOCTO = docto;
	}
	public Long getRENGL() {
		return RENGL;
	}
	public void setRENGL(Long rengl) {
		RENGL = rengl;
	}
	public String getNOMBRE() {
		return NOMBRE;
	}
	public void setNOMBRE(String nombre) {
		NOMBRE = nombre;
	}
	public String getUNIDAD() {
		return UNIDAD;
	}
	public void setUNIDAD(String unidad) {
		UNIDAD = unidad;
	}
	public Double getCANTIDAD() {
		return CANTIDAD;
	}
	public void setCANTIDAD(Double cantidad) {
		CANTIDAD = cantidad;
	}
	public Double getKILOS() {
		return KILOS;
	}
	public void setKILOS(Double kilos) {
		KILOS = kilos;
	}
	public Double getCOSTO() {
		return COSTO;
	}
	public void setCOSTO(Double costo) {
		COSTO = costo;
	}
	public BigDecimal getCOSTOP() {
		return COSTOP;
	}
	public void setCOSTOP(BigDecimal costop) {
		COSTOP = costop;
	}
	public Double getIMPORTECOSTO() {
		return IMPORTECOSTO;
	}
	public void setIMPORTECOSTO(Double importecosto) {
		IMPORTECOSTO = importecosto;
	}
	public Long getYEAR() {
		return YEAR;
	}
	public void setYEAR(Long year) {
		YEAR = year;
	}
	public Long getMES() {
		return MES;
	}
	public void setMES(Long mes) {
		MES = mes;
	}
	public String getCOMENTARIO() {
		return COMENTARIO;
	}
	public void setCOMENTARIO(String comentario) {
		COMENTARIO = comentario;
	}
	
	

}
