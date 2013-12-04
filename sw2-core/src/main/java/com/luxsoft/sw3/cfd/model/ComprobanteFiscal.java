package com.luxsoft.sw3.cfd.model;

import java.io.File;
import java.math.BigDecimal;
import java.net.URL;
import java.text.MessageFormat;
import java.util.Date;

import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.Version;

import mx.gob.sat.cfd.x2.ComprobanteDocument;
import mx.gob.sat.cfd.x2.ComprobanteDocument.Comprobante;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
import org.hibernate.validator.Length;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;

import com.luxsoft.siipap.model.UserLog;
import com.luxsoft.sw3.replica.Replicable;

/**
 * Entidad para el registro de comprobantes digitales
 * 
 * @author Ruben Cancino Ramos
 *
 */
@Entity
@Table(name="SX_CFD")
@GenericGenerator(name="hibernate-uuid",strategy="uuid"
		,parameters={
				@Parameter(name="separator",value="-")
			}
		)
public class ComprobanteFiscal implements Replicable{
		
	
	@Id @GeneratedValue(generator="hibernate-uuid")
	@Column(name="CFD_ID")
	protected String id;
	
	@Version
	private int version;
	
	@Column(name="SERIE",length=15,nullable=false)
	private String serie;
	
	@Column(name="TIPO",nullable=false, length=15)
	private String tipo;
	
	@Column(name="FOLIO",nullable=false,length=20)
	private String folio;
	
	@Column(name="EMISOR",nullable=false)
	private String emisor;
	
	@Column(name="RECEPTOR",nullable=false)
	private String receptor;
	
	@Column(name="XML_PATH",nullable=false,unique=true)
	private String xmlPath;	
	
	@Column(name="XML_SCHEMA_VERSION",nullable=false,length=5)
	private String xsdVersion="2.0";
	
	@Column(name="ORIGEN_ID",nullable=false,unique=true)
	private String origen;
	
	@Column(name="NO_APROBACION",nullable=false)
	private Integer noAprobacion;
	
	@Column(name="ANO_APROBACION",nullable=false)
	private Integer anoAprobacion;
	
	@Column(name="NO_CERTIFICADO",nullable=false,length=20)
	@Length(min=20,max=20,message="No de certificado invalido")
	private String numeroDeCertificado;
		
	@Column(name="TX_IMPORTADO",nullable=true)
	private Date importado;
	
	@Column(name="TX_REPLICADO",nullable=true)
	private Date replicado;
	
	@Embedded
	@AttributeOverrides({
	       @AttributeOverride(name="createUser",	column=@Column(name="CREADO_USR"	,nullable=true,insertable=true,updatable=false)),
	       @AttributeOverride(name="updateUser",	column=@Column(name="MODIFICADO_USR",nullable=true,insertable=true,updatable=true)),
	       @AttributeOverride(name="creado", 		column=@Column(name="CREADO"		,nullable=true,insertable=true,updatable=true)),
	       @AttributeOverride(name="modificado", 	column=@Column(name="MODIFICADO"	,nullable=true,insertable=true,updatable=true))
	   })
	private UserLog log=new UserLog();
	
	@Transient
	private ComprobanteDocument document;
	
	public ComprobanteFiscal(){}
	
	public ComprobanteFiscal(ComprobanteDocument cfdDocment){
		setEmisor(cfdDocment.getComprobante().getEmisor().getNombre());
		setReceptor(cfdDocment.getComprobante().getReceptor().getNombre());
		setXsdVersion(cfdDocment.getComprobante().getVersion());	
		setAnoAprobacion(cfdDocment.getComprobante().getAnoAprobacion());
		setNumeroDeCertificado(cfdDocment.getComprobante().getNoCertificado());
		setNoAprobacion(cfdDocment.getComprobante().getNoAprobacion().intValue());
		setFolio(cfdDocment.getComprobante().getFolio());
		setSerie(cfdDocment.getComprobante().getSerie());
		this.document=cfdDocment;
	}
	
	/**
	 * Tipo del comprobante fiscal, puede ser cualquiera de los valores definidos 
	 * 	
	 * @return El tipo del CFD
	 */
	public String getTipo() {
		return tipo;
	}

	public void setTipo(String tipo) {
		this.tipo = tipo;
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

	/**
	 * Nombre del emisor del CFD
	 *  
	 * @return El nombre del emisor
	 */
	public String getEmisor() {
		return emisor;
	}
	public void setEmisor(String emisor) {
		this.emisor = emisor;
	}

	/**
	 * Nombre del receptor
	 * 
	 * @return
	 */
	public String getReceptor() {
		return receptor;
	}

	public void setReceptor(String receptor) {
		this.receptor = receptor;
	}
	
	/**
	 * Origen del CFD puede ser un Cargo o un Abono
	 * 
	 * @return
	 */
	public String getOrigen() {
		return origen;
	}

	public void setOrigen(String origen) {
		this.origen = origen;
	}	

	/**
	 * Ruta completa del archivo XML en donde se almacena el CFD
	 * Este valor es unico par cada comprobante
	 * El CFD como tal es el documento XML definido en esta ruta de acceso
	 * Eje: F:\pruebas\TACCRE2330
	 * 
	 * @return
	 */
	public String getXmlPath() {
		return xmlPath;
	}

	public void setXmlPath(String xmlPath) {
		this.xmlPath = xmlPath;
	}
	
	/**
	 * Version del archivo de Schema del SAT
	 * 
	 * Sirve de referencia para saber con que version del schema se genero el CFD
	 * 
	 * @return
	 */
	public String getXsdVersion() {
		return xsdVersion;
	}

	public void setXsdVersion(String xsdVersion) {
		this.xsdVersion = xsdVersion;
	}
	

	public Integer getNoAprobacion() {
		return noAprobacion;
	}

	public void setNoAprobacion(Integer noAprobacion) {
		this.noAprobacion = noAprobacion;
	}

	public Integer getAnoAprobacion() {
		return anoAprobacion;
	}

	public void setAnoAprobacion(Integer anoAprobacion) {
		this.anoAprobacion = anoAprobacion;
	}

	public String getNumeroDeCertificado() {
		return numeroDeCertificado;
	}

	public void setNumeroDeCertificado(String numeroDeCertificado) {
		this.numeroDeCertificado = numeroDeCertificado;
	}

	/**
	 * Bitacora de generacion
	 * 
	 * @return
	 */
	public UserLog getLog() {
		if(log==null)
			log=new UserLog();
		return log;
	}

	public void setLog(UserLog log) {
		this.log = log;
	}

	public String getId() {
		return id;
	}

	public int getVersion() {
		return version;
	}

	public Date getImportado() {
		return importado;
	}

	public void setImportado(Date importado) {
		this.importado = importado;
	}

	public Date getReplicado() {
		return replicado;
	}

	public void setReplicado(Date replicado) {
		this.replicado = replicado;
	}
	
	public Comprobante getComprobante(){
		return this.document.getComprobante();
	}
	public ComprobanteDocument getComprobanteDocument(){
		return this.document;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		result = prime * result + ((xmlPath == null) ? 0 : xmlPath.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ComprobanteFiscal other = (ComprobanteFiscal) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		if (xmlPath == null) {
			if (other.xmlPath != null)
				return false;
		} else if (!xmlPath.equals(other.xmlPath))
			return false;
		return true;
	}
	
	public String toString(){
		return new ToStringBuilder(this,ToStringStyle.SHORT_PREFIX_STYLE)
		.append("Tipo: ",getTipo())
		.append("Serie:",getSerie())
		.append("Folio:",getFolio())
		.append("XML:",getXmlPath())
		.append("Tipo CFD:",getTipoCfd())
		.toString();
	}
	
	
	
	public void loadComprobante(){
		//File file=new File(getXmlPath());
		
		//FileSystemResource resource=new FileSystemResource(getXmlPath());
		if(getXmlPath().startsWith("file")){
			oldLoadComprobante();
			return;
		}
		try {
			//URL url=new URL(getXmlPath());
			String path=System.getProperty("cfd.dir.path")+"/"+getXmlPath();
			Resource resource=new FileSystemResource(path);
			if(!resource.exists()){
				throw new RuntimeException("No existe el CFD: "+path);
			}
			this.document=ComprobanteDocument.Factory.parse(resource.getInputStream());
		} catch (Exception e) {
			//e.printStackTrace();
			String msg=MessageFormat.format("Error al cargar XML de comprobante fiscal digital {0}", ExceptionUtils.getRootCauseMessage(e));
			throw new CFDException(msg,e);
		}
	}
	
	public void oldLoadComprobante(){
	//File file=new File(getXmlPath());
		
		//FileSystemResource resource=new FileSystemResource(getXmlPath());
		try {
			URL url=new URL(getXmlPath());
			this.document=ComprobanteDocument.Factory.parse(url);
		} catch (Exception e) {
			//e.printStackTrace();
			String msg=MessageFormat.format("Error al cargar XML de comprobante fiscal digital {0}", ExceptionUtils.getRootCauseMessage(e));
			throw new CFDException(msg,e);
		}
	}
	
	@Column(name="PEDIMENTO")
	private String pedimento;
	
	@Column(name="PEDIMENTO_FECHA")
	private Date pedimentoFecha;
	
	@Column(name="ADUANA")
	private String aduana;
	
	@Column(name="CUENTA_PREDIAL")
	private String cuentaPredial;
	
	@Column(name="RFC",length=13)
	private String rfc;
	
	@Column(name = "TOTAL")
	private BigDecimal total = BigDecimal.ZERO;
	
	@Column(name = "IMPUESTO")
	private BigDecimal impuesto = BigDecimal.ZERO;
	
	@Column(name="ESTADO",length=1)
	private String estado;
	
	@Column(name="TIPO_CFD",length=1)
	private String tipoCfd;

	public String getPedimento() {
		return pedimento;
	}

	public void setPedimento(String pedimento) {
		this.pedimento = pedimento;
	}

	public Date getPedimentoFecha() {
		return pedimentoFecha;
	}

	public void setPedimentoFecha(Date pedimentoFecha) {
		this.pedimentoFecha = pedimentoFecha;
	}

	public String getAduana() {
		return aduana;
	}

	public void setAduana(String aduana) {
		this.aduana = aduana;
	}

	public String getCuentaPredial() {
		return cuentaPredial;
	}

	public void setCuentaPredial(String cuentaPredial) {
		this.cuentaPredial = cuentaPredial;
	}

	public String getRfc() {
		return rfc;
	}

	public void setRfc(String rfc) {
		this.rfc = rfc;
	}

	public BigDecimal getTotal() {
		return total;
	}

	public void setTotal(BigDecimal total) {
		this.total = total;
	}

	public BigDecimal getImpuesto() {
		return impuesto;
	}

	public void setImpuesto(BigDecimal impuesto) {
		this.impuesto = impuesto;
	}

	public String getEstado() {
		return estado;
	}

	public void setEstado(String estado) {
		this.estado = estado;
	}

	public String getTipoCfd() {
		return tipoCfd;
	}

	public void setTipoCfd(String tipoCfd) {
		this.tipoCfd = tipoCfd;
	}
	
	public String getTotalAsString(){
		return getTotal().toString();
	}
	
	public String getFileName(){
	
		return StringUtils.substringAfterLast(getXmlPath(), "/");
	}

}
