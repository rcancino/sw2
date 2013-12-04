package com.luxsoft.sw3.ventas.ui.consultas;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.util.Date;

import com.luxsoft.siipap.util.MonedasUtils;

public class ReporteMensualCFD {
	
	
	private String rfc;
	private String receptor;
	private String serie;
	private String folio;
	private String no_aprobacion;
	private String ano_aprobacion;
	private Date fecha;
	private String total;
	private String impuesto;
	private String estado;
	private String tipo_cfd;
	private String pedimento;
	private Date fecha_ped;
	private String aduana;

	public String getRfc() {
		return rfc;
	}

	public void setRfc(String rfc) {
		this.rfc = rfc;
	}
	
	
	
	
	
	
	
	public String getSerie() {
		return serie;
	}

	public void setSerie(String serie) {
		this.serie = serie;
	}

	public String getFolio() {
		return folio;
	}

	public void setFolio(String folio) {
		this.folio = folio;
	}

	public String getNo_aprobacion() {
		return no_aprobacion;
	}

	public void setNo_aprobacion(String no_aprobacion) {
		this.no_aprobacion = no_aprobacion;
	}

	

	public String getTotal() {
		return total;
	}

	public void setTotal(String total) {
		this.total = total;
	}
	
	//private NumberFormat nf=new DecimalF

	public String getImpuesto() {
		
		return impuesto;
	}

	public void setImpuesto(String impuesto) {
		this.impuesto = impuesto;
	}

	public String getEstado() {
		return estado;
	}

	public void setEstado(String estado) {
		this.estado = estado;
	}

	public String getTipo_cfd() {
		return tipo_cfd;
	}

	public void setTipo_cfd(String tipo_cfd) {
		this.tipo_cfd = tipo_cfd;
	}

	public String getPedimento() {
		return pedimento;
	}

	public void setPedimento(String pedimento) {
		this.pedimento = pedimento;
	}

	

	public String getAduana() {
		return aduana;
	}

	public void setAduana(String aduana) {
		this.aduana = aduana;
	}

	public void setReceptor(String receptor) {
		this.receptor = receptor;
	}

	public String getReceptor() {
		return receptor;
	}

	public String getAno_aprobacion() {
		return ano_aprobacion;
	}

	public void setAno_aprobacion(String ano_aprobacion) {
		this.ano_aprobacion = ano_aprobacion;
	}

	public Date getFecha() {
		return fecha;
	}

	public void setFecha(Date fecha) {
		this.fecha = fecha;
	}

	public Date getFecha_ped() {
		return fecha_ped;
	}

	public void setFecha_ped(Date fecha_ped) {
		this.fecha_ped = fecha_ped;
	}


	public Long getFolioAsNumber(){
		return new Long(getFolio());
	}
	

}
