package com.luxsoft.sw3.solicitudes;

import java.util.Date;
import java.util.List;

import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Version;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
import org.hibernate.annotations.Type;
import org.hibernate.validator.Length;
import org.hibernate.validator.NotNull;

import com.luxsoft.siipap.model.Sucursal;
import com.luxsoft.siipap.model.User;
import com.luxsoft.siipap.model.UserLog;
import com.luxsoft.sw3.model.AdressLog;

import edu.emory.mathcs.backport.java.util.Arrays;


@Entity
@Table(name="SX_SOLICITUD_MODIFICACIONES")
@GenericGenerator(name="hibernate-uuid",strategy="uuid"
,parameters={
		@Parameter(name="separator",value="-")
	}
)
public class SolicitudDeModificacion {
	
	@Id @GeneratedValue(generator="hibernate-uuid")
	@Column(name="ID")
	private String id;
	
	@ManyToOne(optional = false)
	@JoinColumn(name = "SUCURSAL_ID", nullable = false, updatable = false)
	private Sucursal sucursal;
	
	@Column(name="FOLIO",updatable=false,insertable=true)
	private long folio;
	
	@Enumerated(EnumType.STRING)
	@Column(name="MODULO",nullable=false)
	@NotNull
	private Modulo modulo;
	
	@Column(name="TIPO",length=100)
	@Length (max=100)
	@NotNull
	private String tipo;
	
	
	@Enumerated(EnumType.STRING)
	@Column(name="ESTADO",nullable=false)
	@NotNull
	private ESTADO estado=ESTADO.PENDIENTE;
	
	@Column(name="DESCRIPCION",length=400)
	@Length (max=400)
	@NotNull(message="Escriba una descripción corta de la razón para la modificación")
	private String descripcion;
	
	@Column(name="COMENTARIO",length=255)
	@Length (max=255)
	private String comentario;
	
	@ManyToOne(optional = false,fetch=FetchType.EAGER)			
	@JoinColumn(name = "USER_ID", nullable = false, updatable = true)
	@NotNull(message="El usuario es mandatorio")
	private User usuario;
	
	@Type (type="date")
	@Column(name="FECHA",nullable=false)
	@NotNull
	private Date fecha=new Date();
	
	@Column(name="AUTORIZACION",nullable=true)
	private Date autorizacion;
	
	
	@ManyToOne(optional = true,fetch=FetchType.EAGER)
	@JoinColumn(name = "AUTORIZO_USER_ID")
	private User autorizo;
	
	@Column(name="COMENTARIO_AUTORIZACION",length=255)
	@Length (max=255)
	private String comentarioAutorizacion;
	
	@ManyToOne(optional = true,fetch=FetchType.EAGER)			
	@JoinColumn(name = "ATENDIO_USER_ID")
	private User atendio;
	
	@Column(name="COMENTARIO_ATENCION",length=255)
	@Length (max=255)
	private String comentarioDeAtencion;
	
	@Column(name="DOCUMENTO",length=255)
	@Length (max=255)
	private String documento;
	
	@Column(name="DOCUMENTO_DESCRIPCION",length=600)
	@Length (max=600)
	private String documentoDescripcion;
	
	@Version
	private int version;
	
	@Embedded
	@AttributeOverrides({
	       @AttributeOverride(name="createUser",	column=@Column(name="CREADO_USR"	,nullable=true,insertable=true,updatable=false)),
	       @AttributeOverride(name="updateUser",	column=@Column(name="MODIFICADO_USR",nullable=true,insertable=true,updatable=true)),
	       @AttributeOverride(name="creado", 		column=@Column(name="CREADO"		,nullable=true,insertable=true,updatable=false)),
	       @AttributeOverride(name="modificado", 	column=@Column(name="MODIFICADO"	,nullable=true,insertable=true,updatable=true))
	   })
	private UserLog log;
	
	@Embedded
	@AttributeOverrides({
	       @AttributeOverride(name="createdIp",	column=@Column(nullable=true,insertable=true,updatable=false)),
	       @AttributeOverride(name="updatedIp",	column=@Column(nullable=true,insertable=true,updatable=true)),
	       @AttributeOverride(name="createdMac",column=@Column(nullable=true,insertable=true,updatable=false)),
	       @AttributeOverride(name="updatedMac",column=@Column(nullable=true,insertable=true,updatable=true))
	   })
	private AdressLog addresLog;
	
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}
	
	public Sucursal getSucursal() {
		return sucursal;
	}

	public void setSucursal(Sucursal sucursal) {
		this.sucursal = sucursal;
	}

	public long getFolio() {
		return folio;
	}

	public void setFolio(long folio) {
		this.folio = folio;
	}

	public Modulo getModulo() {
		return modulo;
	}

	public void setModulo(Modulo modulo) {
		this.modulo = modulo;
	}

	public String getTipo() {
		return tipo;
	}

	public void setTipo(String tipo) {
		this.tipo = tipo;
	}

	public String getDescripcion() {
		return descripcion;
	}

	public void setDescripcion(String descripcion) {
		this.descripcion = descripcion;
	}

	public String getComentario() {
		return comentario;
	}

	public void setComentario(String comentario) {
		this.comentario = comentario;
	}

	public User getUsuario() {
		return usuario;
	}

	public void setUsuario(User usuario) {
		this.usuario = usuario;
	}

	public Date getFecha() {
		return fecha;
	}

	public void setFecha(Date fecha) {
		this.fecha = fecha;
	}

	public Date getAutorizacion() {
		return autorizacion;
	}

	public void setAutorizacion(Date autorizacion) {
		this.autorizacion = autorizacion;
	}

	public User getAutorizo() {
		return autorizo;
	}

	public void setAutorizo(User autorizo) {
		this.autorizo = autorizo;
	}

	public String getComentarioAutorizacion() {
		return comentarioAutorizacion;
	}

	public void setComentarioAutorizacion(String comentarioAutorizacion) {
		this.comentarioAutorizacion = comentarioAutorizacion;
	}

	public User getAtendio() {
		return atendio;
	}

	public void setAtendio(User atendio) {
		this.atendio = atendio;
	}

	public String getComentarioDeAtencion() {
		return comentarioDeAtencion;
	}

	public void setComentarioDeAtencion(String comentarioDeAtencion) {
		this.comentarioDeAtencion = comentarioDeAtencion;
	}

	public int getVersion() {
		return version;
	}

	public void setVersion(int version) {
		this.version = version;
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
	
	public ESTADO getEstado() {
		return estado;
	}

	public void setEstado(ESTADO estado) {
		this.estado = estado;
	}
	  

	public String getDocumento() {
		return documento;
	}

	public void setDocumento(String documento) {
		this.documento = documento;
	}

	public String getDocumentoDescripcion() {
		return documentoDescripcion;
	}

	public void setDocumentoDescripcion(String documentoDescripcion) {
		this.documentoDescripcion = documentoDescripcion;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (int) (folio ^ (folio >>> 32));
		result = prime * result
				+ ((sucursal == null) ? 0 : sucursal.hashCode());
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
		SolicitudDeModificacion other = (SolicitudDeModificacion) obj;
		if (folio != other.folio)
			return false;
		if (sucursal == null) {
			if (other.sucursal != null)
				return false;
		} else if (!sucursal.equals(other.sucursal))
			return false;
		return true;
	}
	
	public String toString(){
		return new ToStringBuilder(this,ToStringStyle.DEFAULT_STYLE)
		.append(this.sucursal)
		.append("Folio: ",this.folio)
		.append(" ",this.modulo)
		.append("", this.tipo)
		.toString();
	}

	public static enum Modulo{
		
		DEPOSITOS("IMPORTE","CLIENTE","BANCO","FOMRA_DE_PAGO","CANCELACION"),
		EMBARQUES("LIBERACION","CANCELACION","CAMBIO_CHOFER","QUITAR_RETORNO","CORRECCION"),
		TRASLADOS("CANCELACION_SOL","CANCELACION","CAMBIO_CHOFER"),
		VENTAS("CANCELACION","AUTORIZAR_ASIGNACION","AUTORIZAR_PAGO","REIMPRESION","CAMBIO_A_ENVIO"),
		CLIENTES("CAMBIO_RFC","CAMBIO_RAZON_SOCIAL","MODIFICACION_DATOS"),
		COBRANZA("IMPORTE","FORMA_DE_PAGO","CANCELACION_DE_PAGO","MODIFICACION_DE_PAGO","CAMBIO_TARJETA"),
		CAJA("CANCELACION_CORTE","CORRECCION_CORTE","BANCO_CHEQUE","CORRECION_GASTOS","CANCELACION_GASTOS","CORRECCION_REEMBOLSO","CANCELAR_CAMBIO_CHEQUE","REGISTRO_FALTANTE","REGISTRO_SOBRANTE"),
		USUARIOS("MODIFICACION_PERMISOS","ALTA","CAMBIO_PASSWORD"),
		COMPRAS("CANCELACION","CAMBIO_REMISION","CANCELACION_COM","MODFICACION_COM"),
		PEDIDOS("QUITAR_PUESTO","MODFICAR_FECHA_ENTREGA"),
		INVENTARIOS("ELIMINACION","CORRECCION"),
		CXC("CAMBIO_FECHA_APLICACION","CANCELACION_APLICACION","CORREGIR_COMISIONES","CANCELACION_NOTA","REFACTURACION","MODIFICAR_ENVIADO","MODIFICAR_REVISION","CAMBIO_VENCIMIENTO")
		;
		
		
		String[] tipos;
		
		private Modulo(String... tipos){
			this.tipos=tipos;
		}
		
		public String[] tipos(){
			return tipos;
		}
		public List<String> getTipos(){
			return Arrays.asList(tipos);
		}
		public String getName(){
			return name();
		}
	}
	
	public static enum ESTADO{
		PENDIENTE,AUTORIZADA,ATENDIDA,RECHAZADA;
		
		public static ESTADO[] getSistemas(){
			return new ESTADO[]{PENDIENTE,RECHAZADA,ATENDIDA};
		}
	}

}
