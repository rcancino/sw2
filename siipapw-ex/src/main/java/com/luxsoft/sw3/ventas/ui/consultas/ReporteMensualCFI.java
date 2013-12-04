package com.luxsoft.sw3.ventas.ui.consultas;

import java.math.BigDecimal;
import java.util.Date;

public class ReporteMensualCFI {
	
	
	private String ORIGEN_ID;
	private String ESTADO;
	private String TIPO;
	private String TIPO_CFD;
	private String SERIE;
	private String FOLIO;
	private Date FECHA;
	private String RECEPTOR;
	private String RFC;
	private String IMPUESTO;
	private String TOTAL;
	private String ANO_APROBACION;
	private String NO_APROBACION;
	private String EMISOR;
	public String getORIGEN_ID() {
		return ORIGEN_ID;
	}
	public void setORIGEN_ID(String origen_id) {
		ORIGEN_ID = origen_id;
	}
	public String getESTADO() {
		return ESTADO;
	}
	public void setESTADO(String estado) {
		ESTADO = estado;
	}
	public String getTIPO() {
		return TIPO;
	}
	public void setTIPO(String tipo) {
		TIPO = tipo;
	}
	public String getTIPO_CFD() {
		return TIPO_CFD;
	}
	public void setTIPO_CFD(String tipo_cfd) {
		TIPO_CFD = tipo_cfd;
	}
	public String getSERIE() {
		return SERIE;
	}
	public void setSERIE(String serie) {
		SERIE = serie;
	}
	
	public Date getFECHA() {
		return FECHA;
	}
	public void setFECHA(Date fecha) {
		FECHA = fecha;
	}
	public String getRECEPTOR() {
		return RECEPTOR;
	}
	public void setRECEPTOR(String receptor) {
		RECEPTOR = receptor;
	}
	public String getRFC() {
		return RFC;
	}
	public void setRFC(String rfc) {
		RFC = rfc;
	}
	
	public String getTOTAL() {
		return TOTAL;
	}
	public void setTOTAL(String total) {
		TOTAL = total;
	}
	
	public String getNO_APROBACION() {
		return NO_APROBACION;
	}
	public void setNO_APROBACION(String no_aprobacion) {
		NO_APROBACION = no_aprobacion;
	}
	public String getEMISOR() {
		return EMISOR;
	}
	public void setEMISOR(String emisor) {
		EMISOR = emisor;
	}
	public String getFOLIO() {
		return FOLIO;
	}
	public void setFOLIO(String folio) {
		FOLIO = folio;
	}
	public String getIMPUESTO() {
		return IMPUESTO;
	}
	public void setIMPUESTO(String impuesto) {
		IMPUESTO = impuesto;
	}
	public String getANO_APROBACION() {
		return ANO_APROBACION;
	}
	public void setANO_APROBACION(String ano_aprobacion) {
		ANO_APROBACION = ano_aprobacion;
	}
	
	
	
}
