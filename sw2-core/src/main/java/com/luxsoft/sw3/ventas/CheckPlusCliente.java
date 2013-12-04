package com.luxsoft.sw3.ventas;

import java.math.BigDecimal;
import java.text.MessageFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Version;

import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
import org.hibernate.validator.Length;
import org.hibernate.validator.NotNull;

import com.luxsoft.siipap.model.Direccion;
import com.luxsoft.siipap.model.UserLog;
import com.luxsoft.siipap.model.core.Cliente;
import com.luxsoft.sw3.model.AdressLog;

/**
 * Cliente autorizado para la venta tipo check plus
 * 
 * @author Ruben Cancino Ramos
 *
 */
@Entity
@Table(name="SX_CHECKPLUS_CLIENTE")
@GenericGenerator(name="hibernate-uuid",strategy="uuid"
		,parameters={
				@Parameter(name="separator",value="-")
			}
		)
public class CheckPlusCliente {
	
	@Id @GeneratedValue(generator="hibernate-uuid")
	@Column(name="ID")
	protected String id;
	
	@Version
	private int version;
	
	@ManyToOne(optional = false,fetch=FetchType.EAGER)			
	@JoinColumn(name = "CLIENTE_ID", nullable = false, updatable = true)
	@NotNull(message="El cliente es mandatorio")
	private Cliente cliente;
	
	 @Column(name = "NOMBRE", nullable = true,length=100)
	 @Length (max=255)
	 @NotNull
	 private String nombre;
	 
	 @Column(name="RFC",length=13)
	 @NotNull
	 private String rfc;
	    
	 @Column(name = "FISICA")
	 @NotNull
	 private boolean personaFisica = false;
	    
	 @Column(name = "CURP", length = 18)
	 @NotNull
	 private String curp;
	
	 @Embedded
	 @NotNull
	 private Direccion direccion=new Direccion();
	
	@Column(name="AUTORIZACION")
	private Date autorizacion;
	
	@Column(name="COMENTARIO",length=255)
	@Length (max=255)
	private String comentario;
	
	@Column(name="TELEFONO1",length=30)
	@Length (max=30)
	private String telefono1;
	
	@Column(name="TELEFONO2",length=30)
	@Length (max=30)
	private String telefono2;
	
	@Column(name="FAX",length=30)
	@Length (max=30)
	private String fax;
	
	@Column(name="EMAIL",length=255)
	@Length (max=255)
	private String email;
	
	@Column(name="LINEA_SOLICITADA",nullable=false)
	@NotNull
	private BigDecimal creditoSolicitado;
	
	@Column(name="LINEA",nullable=false)
	@NotNull
	private BigDecimal lineaDeCredito=BigDecimal.ZERO;
	
	@Column(name="AUTORIZACION_REFERENCIA",length=255)
	private String autorizacionReferencia;
	
	 @OneToMany(cascade={
			 CascadeType.PERSIST
			,CascadeType.MERGE
			,CascadeType.REMOVE
			,CascadeType.REFRESH
			}
			,fetch=FetchType.LAZY,mappedBy="cliente")
	 @Cascade(value={org.hibernate.annotations.CascadeType.DELETE_ORPHAN,org.hibernate.annotations.CascadeType.SAVE_UPDATE})
	private Set<CheckPlusReferenciaBancaria> referenciasBancarias=new HashSet<CheckPlusReferenciaBancaria>();
	
	 @OneToMany(cascade={
			 CascadeType.PERSIST
			,CascadeType.MERGE
			,CascadeType.REMOVE
			,CascadeType.REFRESH
			}
			,fetch=FetchType.LAZY,mappedBy="cliente")
	 @Cascade(value={org.hibernate.annotations.CascadeType.DELETE_ORPHAN,org.hibernate.annotations.CascadeType.SAVE_UPDATE})
	private Set<CheckPlusDocumento> documentos=new HashSet<CheckPlusDocumento>();
	
	@Lob
	@Column(name="DIGITALIZACION")
	private byte[] digitalizacion;
	
	@Column(name="SUSPENDIDO")
	private boolean suspendido=false;
	
	@Column(name="SUSPENDIDO_COMENTARIO",length=255)
	@Length (max=255)
	private String suspendidoComentario;
	
	@Embedded
	@AttributeOverrides({
	       @AttributeOverride(name="createUser",	column=@Column(name="CREADO_USR"	,nullable=true,insertable=true,updatable=false)),
	       @AttributeOverride(name="updateUser",	column=@Column(name="MODIFICADO_USR",nullable=true,insertable=true,updatable=true)),
	       @AttributeOverride(name="creado", 		column=@Column(name="CREADO"		,nullable=true,insertable=true,updatable=false)),
	       @AttributeOverride(name="modificado", 	column=@Column(name="MODIFICADO"	,nullable=true,insertable=true,updatable=true))
	   })
	private UserLog log=new UserLog();
	
	@Embedded
	@AttributeOverrides({
	       @AttributeOverride(name="createdIp",	column=@Column(nullable=true,insertable=true,updatable=false)),
	       @AttributeOverride(name="updatedIp",	column=@Column(nullable=true,insertable=true,updatable=true)),
	       @AttributeOverride(name="createdMac",column=@Column(nullable=true,insertable=true,updatable=false)),
	       @AttributeOverride(name="updatedMac",column=@Column(nullable=true,insertable=true,updatable=true))
	   })
	private AdressLog addresLog=new AdressLog();

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public int getVersion() {
		return version;
	}

	public void setVersion(int version) {
		this.version = version;
	}

	public Cliente getCliente() {
		return cliente;
	}

	public void setCliente(Cliente cliente) {
		this.cliente = cliente;
		
	}

	public String getNombre() {
		return nombre;
	}

	public void setNombre(String nombre) {
		this.nombre = nombre;
	}

	public String getRfc() {
		return rfc;
	}

	public void setRfc(String rfc) {
		this.rfc = rfc;
	}

	public boolean isPersonaFisica() {
		return personaFisica;
	}

	public void setPersonaFisica(boolean personaFisica) {
		this.personaFisica = personaFisica;
	}

	public String getCurp() {
		return curp;
	}

	public void setCurp(String curp) {
		this.curp = curp;
	}

	public Direccion getDireccion() {
		return direccion;
	}

	public void setDireccion(Direccion direccion) {
		this.direccion = direccion;
	}

	public Date getAutorizacion() {
		return autorizacion;
	}

	public void setAutorizacion(Date autorizacion) {
		this.autorizacion = autorizacion;
	}

	public String getComentario() {
		return comentario;
	}

	public void setComentario(String comentario) {
		this.comentario = comentario;
	}

	public String getTelefono1() {
		return telefono1;
	}

	public void setTelefono1(String telefono1) {
		this.telefono1 = telefono1;
	}

	public String getTelefono2() {
		return telefono2;
	}

	public void setTelefono2(String telefono2) {
		this.telefono2 = telefono2;
	}

	public String getFax() {
		return fax;
	}

	public void setFax(String fax) {
		this.fax = fax;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public BigDecimal getCreditoSolicitado() {
		return creditoSolicitado;
	}

	public void setCreditoSolicitado(BigDecimal creditoSolicitado) {
		this.creditoSolicitado = creditoSolicitado;
	}

	public BigDecimal getLineaDeCredito() {
		return lineaDeCredito;
	}

	public void setLineaDeCredito(BigDecimal lineaDeCredito) {
		this.lineaDeCredito = lineaDeCredito;
	}

	public Set<CheckPlusReferenciaBancaria> getReferenciasBancarias() {
		return referenciasBancarias;
	}

	public void setReferenciasBancarias(
			Set<CheckPlusReferenciaBancaria> referenciasBancarias) {
		this.referenciasBancarias = referenciasBancarias;
	}
	
	public CheckPlusReferenciaBancaria agregarReferencia(CheckPlusReferenciaBancaria ref){
		ref.setCliente(this);
		getReferenciasBancarias().add(ref);
		return ref;
	}
	public void eliminarReferencia(CheckPlusReferenciaBancaria ref){
		getReferenciasBancarias().remove(ref);
		ref.setCliente(null);
	}
	
	
	public Set<CheckPlusDocumento> getDocumentos() {
		return documentos;
	}

	public void setDocumentos(Set<CheckPlusDocumento> documentos) {
		this.documentos = documentos;
	}

	public CheckPlusDocumento agregarDocumento(CheckPlusDocumento doc){
		doc.setCliente(this);
		getDocumentos().add(doc);
		return doc;
	}
	
	public void eliminarDocumento(CheckPlusDocumento doc){
		getDocumentos().remove(doc);
		doc.setCliente(this);
	}

	public byte[] getDigitalizacion() {
		return digitalizacion;
	}

	public void setDigitalizacion(byte[] digitalizacion) {
		this.digitalizacion = digitalizacion;
	}

	public UserLog getLog() {
		return log;
	}

	public void setLog(UserLog log) {
		this.log = log;
	}

	public AdressLog getAddresLog() {
		return addresLog;
	}

	public void setAddresLog(AdressLog addresLog) {
		this.addresLog = addresLog;
	}
	
	public boolean isSuspendido() {
		return suspendido;
	}

	public void setSuspendido(boolean suspendido) {
		this.suspendido = suspendido;
	}

	public String getSuspendidoComentario() {
		return suspendidoComentario;
	}

	public void setSuspendidoComentario(String suspendidoComentario) {
		this.suspendidoComentario = suspendidoComentario;
	}
	

	public String getAutorizacionReferencia() {
		return autorizacionReferencia;
	}

	public void setAutorizacionReferencia(String autorizacionReferencia) {
		this.autorizacionReferencia = autorizacionReferencia;
	}

	
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((rfc == null) ? 0 : rfc.hashCode());
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
		CheckPlusCliente other = (CheckPlusCliente) obj;
		if (rfc == null) {
			if (other.rfc != null)
				return false;
		} else if (!rfc.equals(other.rfc))
			return false;
		return true;
	}

	public String toString(){
		return MessageFormat.format("{0}", nombre);
	}
}
