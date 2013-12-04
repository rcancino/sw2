package com.luxsoft.sw3.ventas;

import java.math.BigDecimal;
import java.text.MessageFormat;
import java.util.Date;

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
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Version;

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
import org.hibernate.validator.NotEmpty;
import org.hibernate.validator.NotNull;

import com.luxsoft.siipap.cxc.model.Cargo;
import com.luxsoft.siipap.model.Direccion;
import com.luxsoft.siipap.model.UserLog;
import com.luxsoft.siipap.model.tesoreria.Banco;
import com.luxsoft.sw3.model.AdressLog;

/**
 * Venta tipo check plus
 * 
 * @author Ruben Cancino Ramos
 *
 */
@Entity
@Table(name="SX_CHECKPLUS_VENTA")
@GenericGenerator(name="hibernate-uuid",strategy="uuid"
		,parameters={
				@Parameter(name="separator",value="-")
			}
		)
public class CheckPlusVenta {
	
	@Id @GeneratedValue(generator="hibernate-uuid")
	@Column(name="ID")
	protected String id;
	
	@Version
	private int version;
	
	@Column(name="PLAZO",nullable=false)
	private int plazo;
	
	@Column(name="CARGO",nullable=false)
	private BigDecimal cargo;
	
	@ManyToOne(optional = false
			,cascade={CascadeType.PERSIST,CascadeType.MERGE},fetch=FetchType.EAGER)
	@JoinColumn(name = "CARGO_ID", nullable = false)
	private Cargo venta;
	
	@Column(name="FECHA_PROTECCION",nullable=false)
	@NotNull(message="Falta la fecha de la protección")
	private Date fechaDeProteccion=new Date();
	
	
	@Column(name="BANCO",nullable=false,length=100)
	@NotEmpty(message="Falta el banco origen")
	private String bancoNombre;
	
	private transient Banco banco;
	
	@Column(name="NUMERO_DE_CUENTA",nullable=false)
	@NotEmpty(message="Falta el número de la cuenta de cheques")
	private String numeroDeCuenta;
	
	@Column(name="NUMERO_DE_CHEQUE",nullable=false)
	@NotEmpty(message="Falta el número de cheque")
	private String numeroDeCheque;
	
	@Column(name="RAZON_SOCIAL",nullable=false,length=255)
	@NotEmpty(message="Falta la razón social")
	private String razonSocial;
	
	@Embedded
	@NotNull(message="Falta registrar la dirección")
	private Direccion direccion=new Direccion();
	
	@Column(name="TELEFONO",length=30,nullable=false)
	@NotNull(message="El teléfono es mandatorio ")
	private String telefono;
	
	
	@Column(name="IDENTIFICACION_TIPO",nullable=false,length=50)
	@NotEmpty(message="Falta el tipo de la identificación")
	private String tipoDeIdentificacion;
	
	@Column(name="IDENTIFICACION_FOLIO",nullable=false,length=100)
	@NotEmpty(message="Falta el folio de la identificación")
	private String folioDeIdentificacion;
	
	@Column(name="ATENDIO_CHECKPLUS",nullable=false,length=255)
	@NotEmpty(message="Debe registrar el nombre del ejecutivo de CheckPlus")
	private String atendioCheckPlus;
	
	@Column(name="CLAVE_AUTORIZACION",nullable=false,length=255)
	@NotEmpty(message="Debe registrar la clave de autorización")
	private String claveAutorizacion;
	
	@Embedded
	@AttributeOverrides({
	       @AttributeOverride(name="createUser",	column=@Column(name="CREADO_USR"	,nullable=false,insertable=true,updatable=false)),
	       @AttributeOverride(name="updateUser",	column=@Column(name="MODIFICADO_USR",nullable=false,insertable=true,updatable=true)),
	       @AttributeOverride(name="creado", 		column=@Column(name="CREADO"		,nullable=false,insertable=true,updatable=false)),
	       @AttributeOverride(name="modificado", 	column=@Column(name="MODIFICADO"	,nullable=false,insertable=true,updatable=true))
	   })
	private UserLog log;
	
	@Embedded
	@AttributeOverrides({
	       @AttributeOverride(name="createdIp",	column=@Column(nullable=false,insertable=true,updatable=false)),
	       @AttributeOverride(name="updatedIp",	column=@Column(nullable=false,insertable=true,updatable=true)),
	       @AttributeOverride(name="createdMac",column=@Column(nullable=false,insertable=true,updatable=false)),
	       @AttributeOverride(name="updatedMac",column=@Column(nullable=false,insertable=true,updatable=true))
	   })
	private AdressLog addresLog;

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

	public int getPlazo() {
		return plazo;
	}

	public void setPlazo(int plazo) {
		this.plazo = plazo;
	}

	public BigDecimal getCargo() {
		return cargo;
	}

	public void setCargo(BigDecimal cargo) {
		this.cargo = cargo;
	}
	

	public Cargo getVenta() {
		return venta;
	}

	public void setVenta(Cargo venta) {
		this.venta = venta;
	}
	

	public Date getFechaDeProteccion() {
		return fechaDeProteccion;
	}

	public void setFechaDeProteccion(Date fechaDeProteccion) {
		this.fechaDeProteccion = fechaDeProteccion;
	}

	public String getBancoNombre() {
		return bancoNombre;
	}

	public void setBancoNombre(String bancoNombre) {
		this.bancoNombre = bancoNombre;
	}

	public Banco getBanco() {
		return banco;
	}

	public void setBanco(Banco banco) {
		this.banco = banco;
		setBancoNombre(banco!=null?banco.getNombre():null);
	}

	public String getNumeroDeCuenta() {
		return numeroDeCuenta;
	}

	public void setNumeroDeCuenta(String numeroDeCuenta) {
		this.numeroDeCuenta = numeroDeCuenta;
	}

	public String getNumeroDeCheque() {
		return numeroDeCheque;
	}

	public void setNumeroDeCheque(String numeroDeCheque) {
		this.numeroDeCheque = numeroDeCheque;
	}

	public String getRazonSocial() {
		return razonSocial;
	}

	public void setRazonSocial(String razonSocial) {
		this.razonSocial = razonSocial;
	}

	public Direccion getDireccion() {
		return direccion;
	}

	public void setDireccion(Direccion direccion) {
		this.direccion = direccion;
	}

	public String getTelefono() {
		return telefono;
	}

	public void setTelefono(String telefono) {
		this.telefono = telefono;
	}

	public String getTipoDeIdentificacion() {
		return tipoDeIdentificacion;
	}

	public void setTipoDeIdentificacion(String tipoDeIdentificacion) {
		this.tipoDeIdentificacion = tipoDeIdentificacion;
	}

	public String getFolioDeIdentificacion() {
		return folioDeIdentificacion;
	}

	public void setFolioDeIdentificacion(String folioDeIdentificacion) {
		this.folioDeIdentificacion = folioDeIdentificacion;
	}

	public String getAtendioCheckPlus() {
		return atendioCheckPlus;
	}

	public void setAtendioCheckPlus(String atendioCheckPlus) {
		this.atendioCheckPlus = atendioCheckPlus;
	}

	public String getClaveAutorizacion() {
		return claveAutorizacion;
	}

	public void setClaveAutorizacion(String claveAutorizacion) {
		this.claveAutorizacion = claveAutorizacion;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + plazo;
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
		CheckPlusVenta other = (CheckPlusVenta) obj;
		if (plazo != other.plazo)
			return false;
		return true;
	}

	public String toString(){
		return MessageFormat.format("{0} ({1})  ", venta.getDocumento(),venta.getCliente());
	}
}
