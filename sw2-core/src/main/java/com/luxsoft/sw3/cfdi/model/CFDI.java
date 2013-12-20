package com.luxsoft.sw3.cfdi.model;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.text.MessageFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

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



import mx.gob.sat.cfd.x3.ComprobanteDocument;
import mx.gob.sat.cfd.x3.ComprobanteDocument.Comprobante;


import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.xmlbeans.XmlOptions;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
import org.hibernate.validator.Length;

import com.luxsoft.siipap.model.UserLog;
import com.luxsoft.sw3.cfd.model.CFDException;
import com.luxsoft.sw3.replica.Replicable;

/**
 * Entidad para el registro de comprobantes digitales
 * 
 * @author Ruben Cancino Ramos
 *
 */
@SuppressWarnings("serial")
@Entity
@Table(name="SX_CFDI")
@GenericGenerator(name="hibernate-uuid",strategy="uuid"
		,parameters={
				@Parameter(name="separator",value="-")
			}
		)
public class CFDI implements Replicable{
		
	

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
	
	@Column(name="XML_FILE")
	private String xmlFilePath;	
	
	@Column(name="XML",nullable=true,length=1048576)
	private byte[] xml;
	
	@Column(name="CADENA_ORIGINAL",length=1048576,nullable=true)
	private String cadenaOriginal;
	
	@Column(name="XML_SCHEMA_VERSION",nullable=false,length=5)
	private String xsdVersion="3.2";
	
	@Column(name="ORIGEN_ID",nullable=false,unique=true)
	private String origen;
	
	
	@Column(name="NO_CERTIFICADO",nullable=false,length=20)
	@Length(min=20,max=20,message="No de certificado invalido")
	private String numeroDeCertificado;
	
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
	
	@Column(name = "SUBTOTAL")
	private BigDecimal subtotal = BigDecimal.ZERO;
	
	@Column(name = "IMPUESTO")
	private BigDecimal impuesto = BigDecimal.ZERO;
	
	@Column(name = "TOTAL")
	private BigDecimal total = BigDecimal.ZERO;
	
	
	@Column(name="ESTADO",length=1)
	private String estado;
	
	@Column(name="TIPO_CFD",length=1)
	private String tipoCfd;
		
	@Column(name="TX_IMPORTADO",nullable=true)
	private Date importado;
	
	@Column(name="TX_REPLICADO",nullable=true)
	private Date replicado;
	
	@Column(name="TIMBRADO",nullable=true,length=50)
	private String timbrado;
	
	
	@Column(name="UUID",nullable=true,length=300)
	private String UUID;
	
	@Column(name="COMENTARIO",nullable=true,length=300)
	private String comentario;
	
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
	
	@Transient
	private TimbreFiscal timbre;
	
	 
	
	public CFDI(){}
	
	public CFDI(ComprobanteDocument cfdDocment){
		setEmisor(cfdDocment.getComprobante().getEmisor().getNombre());
		setReceptor(cfdDocment.getComprobante().getReceptor().getNombre());
		setXsdVersion(cfdDocment.getComprobante().getVersion());	
		setNumeroDeCertificado(cfdDocment.getComprobante().getNoCertificado());
		setFolio(cfdDocment.getComprobante().getFolio());
		setSerie(cfdDocment.getComprobante().getSerie());
		setSubtotal(cfdDocment.getComprobante().getSubTotal());
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

	
	

	public String getXmlFilePath() {
		return xmlFilePath;
	}

	public void setXmlFilePath(String xmlFilePath) {
		this.xmlFilePath = xmlFilePath;
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
		return getComprobanteDocument().getComprobante();
	}
	
	public ComprobanteDocument getComprobanteDocument(){
		if(this.document==null)
			loadComprobante();
		return this.document;
	}
	

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
	

	public BigDecimal getSubtotal() {
		return subtotal;
	}

	public void setSubtotal(BigDecimal subtotal) {
		this.subtotal = subtotal;
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
	
	
	public byte[] getXml() {
		return xml;
	}

	public void setXml(byte[] xml) {
		this.xml = xml;
	}
	

	public String getCadenaOriginal() {
		return cadenaOriginal;
	}

	public void setCadenaOriginal(String cadenaOriginal) {
		this.cadenaOriginal = cadenaOriginal;
	}

	public ComprobanteDocument getDocument() {
		return document;
	}

	public void setDocument(ComprobanteDocument document) {
		this.document = document;
	}

	public void setId(String id) {
		this.id = id;
	}
	

	
	 public String getTimbrado() {
		return timbrado;
	}

	public void setTimbrado(String timbrado) {
		this.timbrado = timbrado;
	}

	void loadComprobante(){
		
		try {
			ByteArrayInputStream is=new ByteArrayInputStream(getXml());
			this.document=ComprobanteDocument.Factory.parse(is);
		} catch (Exception e) {
			//e.printStackTrace();
			String msg=MessageFormat.format("Error al cargar XML de comprobante fiscal digital cfdi:{0}  Err:{1}",getId(), ExceptionUtils.getRootCauseMessage(e));
			throw new CFDException(msg,e);
		}
	} 
	
	 public void cargarTimbrado(){
		 setTimbrado(getTimbreFiscal().getFechaTimbrado());
	 }
	 
	 public TimbreFiscal getTimbreFiscal(){
		 if(timbre==null){
			 timbre=new TimbreFiscal(getComprobante());
		 }
		 return this.timbre;
	 }
	 public void setTimbre(TimbreFiscal timbre) {
		this.timbre = timbre;
	}
	 public void setUUID(String uUID) {
		UUID = uUID;
	}
	 public String getUUID() {
		return UUID;
	}
	 
	 
	
	public String getComentario() {
		return comentario;
	}

	public void setComentario(String comentario) {
		this.comentario = comentario;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		result = prime * result + ((origen == null) ? 0 : origen.hashCode());
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
		CFDI other = (CFDI) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		if (origen == null) {
			if (other.origen != null)
				return false;
		} else if (!origen.equals(other.origen))
			return false;
		return true;
	}

	public String toString(){
		return new ToStringBuilder(this,ToStringStyle.SHORT_PREFIX_STYLE)
		.append("Tipo: ",getTipo())
		.append("Serie:",getSerie())
		.append("Tipo CFD:",getTipoCfd())
		.append("Folio:",getFolio())
		.toString();
	}
	
	public void salvarArchivoTimbradoXml() throws IOException{
		String path=System.getProperty("cfd.dir.path")+"/cfdi/timbrados/"+getXmlFilePath();
		File xml=new File(path);
		FileOutputStream out=new FileOutputStream(xml);
		out.write(getXml());
		out.flush();
		out.close();
		
	}
	
	/*
	public void salvarArchivoSinTimbrar()throws IOException{
		if((getTimbreFiscal()!=null) && (getTimbreFiscal().getFechaTimbrado()!=null)){
			XmlOptions options = new XmlOptions();
			options.setCharacterEncoding("UTF-8");
	        options.put( XmlOptions.SAVE_INNER );
	        options.put( XmlOptions.SAVE_PRETTY_PRINT );
	        options.put( XmlOptions.SAVE_AGGRESSIVE_NAMESPACES );
	        options.put( XmlOptions.SAVE_USE_DEFAULT_NAMESPACE );
	        options.put(XmlOptions.SAVE_NAMESPACES_FIRST);
	        Map suggestedPrefix=new HashMap();
			suggestedPrefix.put("", "");
			String path=System.getProperty("cfd.dir.path")+"/"+getXmlFilePath();
			File xml=new File(path);
			//FileOutputStream out=new FileOutputStream(xml);
			getComprobanteDocument().save(xml, options);
		}
		
	}*/

}
