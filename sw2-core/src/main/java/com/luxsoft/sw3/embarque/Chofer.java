package com.luxsoft.sw3.embarque;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.TableGenerator;

import org.hibernate.annotations.CollectionOfElements;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.hibernate.validator.Email;
import org.hibernate.validator.Length;
import org.hibernate.validator.NotEmpty;

import com.luxsoft.siipap.model.BaseBean;
import com.luxsoft.siipap.model.UserLog;

@Entity
@Table(name="SX_CHOFERES")
public class Chofer extends BaseBean{
	
	
	@TableGenerator(
            name="idsGen", 
            table="SX_TABLE_IDS", 
            pkColumnName="GEN_KEY", 
            valueColumnName="GEN_VALUE", 
            pkColumnValue="CHOFER_ID", 
            initialValue=100,
            allocationSize=1)
    @Id
    @GeneratedValue(strategy=GenerationType.TABLE, generator="idsGen")
	@Column(name="CHOFER_ID")
	private Long id;
	
	@Column(name="NOMBRE", nullable=false,unique=true)
	@NotEmpty(message="Nombre invalido")
	private String nombre;
	
	@Column(name="RFC")
	@Length (max=15)
	private String rfc;
	
	@Column(name="RADIO")
	@Length (max=50)
	private String radio;
	
	@ManyToOne(optional=true)
	@JoinColumn (name="FACTURISTA_ID",nullable=true)	
	private ChoferFacturista facturista;
	
	@Column(name="SUSPENDIDO",nullable=false)
	private boolean suspendido=false;
	
	@Column(name="SUSPENDIDO_FECHA",nullable=true)
	private Date suspendidoFecha;
	
	@Email
	@Length (max=100)
	private String email1;
	
	@Column(name="COMENTARIO")
	private String comentario;
	
	@CollectionOfElements (fetch=FetchType.EAGER)
	@JoinTable(
			name="SX_CHOFER_OBSERVACIONES",
			joinColumns=@JoinColumn(name="CHOFER_ID")	
	)
	@Fetch(value=FetchMode.SUBSELECT)	
	private Set<ChoferObservacion> observaciones=new HashSet<ChoferObservacion>();
	
	@Embedded
	@AttributeOverrides({
	       @AttributeOverride(name="createUser",	column=@Column(name="CREADO_USR"	,nullable=true,insertable=true,updatable=false)),
	       @AttributeOverride(name="updateUser",	column=@Column(name="MODIFICADO_USR",nullable=true,insertable=true,updatable=true)),
	       @AttributeOverride(name="creado", 		column=@Column(name="CREADO"		,nullable=true,insertable=true,updatable=false)),
	       @AttributeOverride(name="modificado", 	column=@Column(name="MODIFICADO"	,nullable=true,insertable=true,updatable=true))
	   })
	private UserLog log=new UserLog();
	
	@Column(name="TX_IMPORTADO",nullable=true)
	private Date importado;
	
	@Column(name="TX_REPLICADO",nullable=true)
	private Date replicado;

	public String getNombre() {
		return nombre;
	}

	public void setNombre(String nombre) {
		Object old=this.nombre;
		this.nombre = nombre;
		firePropertyChange("nombre", old, nombre);
	}

	public String getRfc() {
		return rfc;
	}

	public void setRfc(String rfc) {
		Object old=this.rfc;
		this.rfc = rfc;
		firePropertyChange("rfc", old, rfc);
	}

	public String getRadio() {
		return radio;
	}

	public void setRadio(String radio) {
		Object old=this.radio;
		this.radio = radio;
		firePropertyChange("radio", old, radio);
	}

	public Long getId() {
		return id;
	}

	public ChoferFacturista getFacturista() {
		return facturista;
	}

	public void setFacturista(ChoferFacturista facturista) {
		this.facturista = facturista;
	}

	public UserLog getLog() {
		return log;
	}

	public void setLog(UserLog log) {
		this.log = log;
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

	public boolean isSuspendido() {
		return suspendido;
	}

	public void setSuspendido(boolean suspendido) {
		boolean old=this.suspendido;
		this.suspendido = suspendido;
		firePropertyChange("suspendido", old, suspendido);
		if(suspendido)
			setSuspendidoFecha(new Date());
	}

	public Date getSuspendidoFecha() {
		return suspendidoFecha;
	}

	public void setSuspendidoFecha(Date suspendidoFecha) {
		Object old=this.suspendidoFecha;
		this.suspendidoFecha = suspendidoFecha;
		firePropertyChange("suspendidoFecha", old, suspendidoFecha);
	}

	public String getEmail1() {
		return email1;
	}

	public void setEmail1(String email1) {
		Object old=this.email1;
		this.email1 = email1;
		firePropertyChange("email1", old, email1);
	}	

	public String getComentario() {
		return comentario;
	}

	public void setComentario(String comentario) {
		Object old=this.comentario;
		this.comentario = comentario;
		firePropertyChange("comentario", old, comentario);
	}	

	public Set<ChoferObservacion> getObservaciones() {
		return observaciones;
	}

	public void setObservaciones(Set<ChoferObservacion> observaciones) {
		this.observaciones = observaciones;
	}
	
	public boolean agregarObservacion(ChoferObservacion o){
		return this.observaciones.add(o);
	}
	public boolean eliminarObservacion(ChoferObservacion o){
		return this.observaciones.remove(o);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((nombre == null) ? 0 : nombre.hashCode());
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
		Chofer other = (Chofer) obj;
		if (nombre == null) {
			if (other.nombre != null)
				return false;
		} else if (!nombre.equals(other.nombre))
			return false;
		return true;
	}
	
	public String toString(){
		return nombre;
	}

}
