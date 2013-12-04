package com.luxsoft.sw3.cfd.model;

import java.io.File;
import java.io.IOException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Date;

import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.Version;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.commons.lang.time.DateUtils;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.hibernate.annotations.Type;
import org.hibernate.validator.Length;
import org.hibernate.validator.NotEmpty;
import org.hibernate.validator.NotNull;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;

import com.luxsoft.siipap.model.BaseBean;
import com.luxsoft.siipap.model.UserLog;
import com.luxsoft.sw3.replica.Replicable;

/**
 * Entidad de certificado digital requerido para la generacion
 * de comprobantes fiscales digitales  
 * 
 * @author Ruben Cancino Ramos
 *
 */
@Entity
@Table(name="SX_CFD_CERTIFICADOS")
public class CertificadoDeSelloDigital extends BaseBean  {
	
	
	@Id
    @Column(name = "CERTIFICADO_DIGITAL_ID")   
	protected Long id;
	
	@Version
	private int version;
	
	@Column(name="NO_CERTIFICADO",nullable=false,length=20,unique=true)
	@Length(min=20,max=20,message="No de certificado invalido")
	@NotEmpty
	private String numeroDeCertificado;
	
	@Column(name="EXPEDICION",nullable=false)
	@Type(type="date")
	@NotNull
	private Date expedicion=new Date();
	
	@Column(name="VENCIMIENTO",nullable=false)
	@Type(type="date")
	@NotNull
	private Date vencimiento=new Date();
	
	@Column(name="CERTIFICADO",nullable=false,unique=true)
	@NotEmpty
	private String certificadoPath;
	
	
	@Column(name="PRIVATE_KEY",nullable=false,unique=true)
	@NotEmpty
	private String privateKeyPath;
	
	@Column(name="ALGORITMO",length=40,nullable=false,unique=true)
	@NotNull
	private String algoritmo;
			
	@Column(name="TX_IMPORTADO",nullable=true)
	private Date importado;
	
	@Column(name="TX_REPLICADO",nullable=true)
	private Date replicado;
	
	
	
	@Embedded
	@AttributeOverrides({
	       @AttributeOverride(name="createUser",	column=@Column(name="CREADO_USR"	,nullable=true,insertable=true,updatable=false)),
	       @AttributeOverride(name="updateUser",	column=@Column(name="MODIFICADO_USR",nullable=true,insertable=true,updatable=true)),
	       @AttributeOverride(name="creado", 		column=@Column(name="CREADO"		,nullable=true,insertable=true,updatable=false)),
	       @AttributeOverride(name="modificado", 	column=@Column(name="MODIFICADO"	,nullable=true,insertable=true,updatable=true))
	   })
	private UserLog log=new UserLog();
	
	public CertificadoDeSelloDigital(){}
	
	
	public String getNumeroDeCertificado() {
		return numeroDeCertificado;
	}

	public void resolverNumero(){
		if(StringUtils.isNotBlank(getCertificadoPath())){
			Resource r=new FileSystemResource(getCertificadoPath());
			try {
				File file=r.getFile();
				String fname=file.getName();
				String num=StringUtils.substring(fname, 0, 20);
				setNumeroDeCertificado(num);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	public void resolverVencimiento(){
		setVencimiento(getCertificado().getNotAfter());
		setExpedicion(getCertificado().getNotBefore());
	}

	public void setNumeroDeCertificado(String numeroDeCertificado) {
		Object old=this.numeroDeCertificado;
		this.numeroDeCertificado = numeroDeCertificado;
		firePropertyChange("numeroDeCertificado", old, numeroDeCertificado);
	}


	public Date getExpedicion() {
		return expedicion;
	}
	public void setExpedicion(Date expedicion) {
		Object old=this.expedicion;
		this.expedicion = expedicion;
		firePropertyChange("expedicion", old, expedicion);
		if(this.expedicion!=null){
			setVencimiento(DateUtils.addYears(expedicion, 2));
		}else
			setVencimiento(null);
	}

	public Date getVencimiento() {
		return vencimiento;
	}
	public void setVencimiento(Date vencimiento) {
		Object old=this.vencimiento;
		this.vencimiento = vencimiento;
		firePropertyChange("vencimiento", old, vencimiento);
	}

	

	public String getCertificadoPath() {
		return certificadoPath;
	}


	public void setCertificadoPath(String certificadoPath) {
		Object old=this.certificadoPath;
		this.certificadoPath = certificadoPath;
		firePropertyChange("certificadoPath", old, certificadoPath);
	}


	public String getPrivateKeyPath() {
		return privateKeyPath;
	}


	public void setPrivateKeyPath(String privateKeyPath) {
		Object old=this.privateKeyPath;
		this.privateKeyPath = privateKeyPath;
		firePropertyChange("privateKeyPath", old, privateKeyPath);
	}


	/**
	 * Bitacora de generacion
	 * 
	 * @return
	 */
	public UserLog getLog() {
		return log;
	}
	

	public void setId(Long id) {
		this.id = id;
	}


	public void setLog(UserLog log) {
		this.log = log;
	}

	public Long getId() {
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

	

	public String getAlgoritmo() {
		return algoritmo;
	}


	public void setAlgoritmo(String algoritmo) {
		Object old=this.algoritmo;
		this.algoritmo = algoritmo;
		firePropertyChange("algoritmo", old, algoritmo);
	}


	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime
				* result
				+ ((numeroDeCertificado == null) ? 0 : numeroDeCertificado
						.hashCode());
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
		CertificadoDeSelloDigital other = (CertificadoDeSelloDigital) obj;
		if (numeroDeCertificado == null) {
			if (other.numeroDeCertificado != null)
				return false;
		} else if (!numeroDeCertificado.equals(other.numeroDeCertificado))
			return false;
		return true;
	}

	public String toString(){
		return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
		.append(getNumeroDeCertificado())
		.append(getCertificadoPath())
		.append(getVencimiento())
		.toString();
	}
	
	@Transient
	private X509Certificate certificado;
	
	public X509Certificate getCertificado() {
		if(certificado==null){
			try{
				System.out.println("Abriendo certificado de: "+getCertificadoPath());
				java.security.Security.addProvider(new BouncyCastleProvider());
				String cerPath=System.getProperty("cfd.cer.path")+"/"+getCertificadoPath();
				FileSystemResource publicKeyResource = new FileSystemResource(cerPath);
				CertificateFactory fact= CertificateFactory.getInstance("X.509","BC");
				certificado = (X509Certificate)fact.generateCertificate(publicKeyResource.getInputStream());
				certificado.checkValidity();
				return certificado;
			}catch (Exception e) {
				String msg=ExceptionUtils.getRootCauseMessage(e);
				throw new RuntimeException("Error tratando de leer Certificado: "+msg,e);
			}
		}
		return certificado;
		
	}
	
	public static String[] ALGORITMOS={"MD5withRSA","SHA-1"};		
	
}
