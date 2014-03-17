package com.luxsoft.sw3.cfd.ui.consultas;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.util.Date;

import com.luxsoft.siipap.util.MonedasUtils;

public class CFDIRow {
	
	
		
	
	private String cfd_id;
	private String serie;
	private String tipo;
	private String folio;
	private String cliente;
	private Date fecha ;
	private BigDecimal impuesto;
	private BigDecimal total;
	private String estado;
	private String timbrado;
	private String uuid;
	private String rfc;
	private String comentarioCfdi;
	private String cargo_id;
	private Date  cancelacionSat;
	private Date  fechaCan;
	private String comentarioCan;
	private boolean cancelado=false;
	private boolean canceladoSat=false;
	
	
	
	
	public String getCfd_id() {
		return cfd_id;
	}
	public void setCfd_id(String cfd_id) {
		this.cfd_id = cfd_id;
	}
	public String getSerie() {
		return serie;
	}
	public void setSerie(String serie) {
		this.serie = serie;
	}
	public String getTipo() {
		return tipo;
	}
	public void setTipo(String tipo) {
		this.tipo = tipo;
	}
	public String getFolio() {
		return folio;
	}
	public void setFolio(String folio) {
		this.folio = folio;
	}
	public String getCliente() {
		return cliente;
	}
	public void setCliente(String cliente) {
		this.cliente = cliente;
	}
	public Date getFecha() {
		return fecha;
	}
	public void setFecha(Date fecha) {
		this.fecha = fecha;
	}
	public BigDecimal getTotal() {
		return total;
	}
	public void setTotal(BigDecimal total) {
		this.total = total;
	}
	public String getEstado() {
		return estado;
	}
	public void setEstado(String estado) {
		this.estado = estado;
	}
	public String getTimbrado() {
		return timbrado;
	}
	public void setTimbrado(String timbrado) {
		this.timbrado = timbrado;
	}
	public String getUuid() {
		return uuid;
	}
	public void setUuid(String uuid) {
		this.uuid = uuid;
	}
	public String getRfc() {
		return rfc;
	}
	public void setRfc(String rfc) {
		this.rfc = rfc;
	}
	public String getComentarioCfdi() {
		return comentarioCfdi;
	}
	public void setComentario_cfdi(String comentarioCfdi) {
		this.comentarioCfdi = comentarioCfdi;
	}
	public String getCargo_id() {
		return cargo_id;
	}
	public void setCargo_id(String cargo_id) {
		this.cargo_id = cargo_id;
	}
	public Date getCancelacionSat() {
		return cancelacionSat;
	}
	public void setCancelacionSat(Date cancelacionSat) {
		this.cancelacionSat = cancelacionSat;
	}
	public Date getFechaCan() {
		return fechaCan;
	}
	public void setFechaCan(Date fechaCan) {
		this.fechaCan = fechaCan;
	}
	public String getComentarioCan() {
		return comentarioCan;
	}
	public void setComentarioCan(String comentarioCan) {
		this.comentarioCan = comentarioCan;
	}
	public boolean isCancelado() {
		if(fechaCan!=null)
			cancelado=true;
		return cancelado;
	}
	public boolean isCanceladoSat() {
		if(cancelacionSat!=null)
			canceladoSat=true;
		return canceladoSat;
	}
	public BigDecimal getImpuesto() {
		if(impuesto==null)
			impuesto=MonedasUtils.calcularImpuestoDelTotal(total);
		return impuesto;
	}
	public void setImpuesto(BigDecimal impuesto) {
		this.impuesto = impuesto;
	}
	
	

	

}
