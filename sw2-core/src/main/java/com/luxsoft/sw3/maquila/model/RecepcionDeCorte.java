package com.luxsoft.sw3.maquila.model;

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
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Version;

import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.Type;
import org.hibernate.validator.Length;
import org.hibernate.validator.NotNull;

import com.luxsoft.siipap.model.BaseBean;
import com.luxsoft.siipap.model.UserLog;
import com.luxsoft.sw3.model.AdressLog;

/**
 * Recepcion de material cortado
 * 
 * @author Ruben Cancino
 *
 */
@Entity
@Table(name="SX_MAQ_RECEPCION_CORTE")
public class RecepcionDeCorte extends BaseBean{
	
	@Id @GeneratedValue(strategy=GenerationType.AUTO)
    @Column(name = "RECEPCION_ID")
	private Long id;
    
    @SuppressWarnings("unused")
	@Version
	private int version;
	
	@ManyToOne(optional = false,
			fetch=FetchType.EAGER)			
	@JoinColumn(name = "ALMACEN_ID", nullable = false,updatable=false)
	@NotNull(message="El almacen es mandatorio")
	private Almacen almacen;
	/*
	@ManyToOne(optional = false,
			fetch=FetchType.LAZY)			
	@JoinColumn(name = "ORDEN_ID", nullable = false,updatable=false)
	@NotNull(message="La orden  es mandatoria")
	private OrdenDeCorte orden;*/
	
	@Column(name="FECHA",nullable=false)
	@Type(type="date")
	private Date fecha=new Date();
	
	@Column(name="COMENTARIO")
	@Length(max=255)
	private String comentario;

	@OneToMany(cascade={
			 CascadeType.PERSIST
			,CascadeType.MERGE
			,CascadeType.REMOVE
			}
			,fetch=FetchType.LAZY,mappedBy="recepcion")
	@Cascade(value=org.hibernate.annotations.CascadeType.DELETE_ORPHAN)
	private Set<RecepcionDeCorteDet> partidas=new HashSet<RecepcionDeCorteDet>();
	
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

	public Almacen getAlmacen() {
		return almacen;
	}

	public void setAlmacen(Almacen almacen) {
		Object old=this.almacen;
		this.almacen = almacen;
		firePropertyChange("almacen", old, almacen);
	}

	/*public OrdenDeCorte getOrden() {
		return orden;
	}

	public void setOrden(OrdenDeCorte orden) {
		this.orden = orden;
	}
*/
	public Date getFecha() {
		return fecha;
	}

	public void setFecha(Date fecha) {
		this.fecha = fecha;
	}

	public String getComentario() {
		return comentario;
	}

	public void setComentario(String comentario) {
		this.comentario = comentario;
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

	public Long getId() {
		return id;
	}
	
	public boolean agregarRecepcion(final RecepcionDeCorteDet det){
		det.setRecepcion(this);
		return partidas.add(det);
	}
	public boolean eliminarRecepcion(final RecepcionDeCorteDet det){
		det.setRecepcion(null);
		return partidas.remove(det);
	}
	

	public Set<RecepcionDeCorteDet> getPartidas() {
		return partidas;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
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
		RecepcionDeCorte other = (RecepcionDeCorte) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "Recepcion : "+getId();
	}
	
	

}
