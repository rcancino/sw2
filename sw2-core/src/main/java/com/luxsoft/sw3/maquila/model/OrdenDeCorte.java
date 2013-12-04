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

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.Type;
import org.hibernate.validator.Length;
import org.hibernate.validator.NotNull;

import com.luxsoft.siipap.model.BaseBean;
import com.luxsoft.siipap.model.UserLog;
import com.luxsoft.sw3.model.AdressLog;


@Entity
@Table(name="SX_MAQ_ORDENES")
public class OrdenDeCorte  extends BaseBean{
	
	@Id @GeneratedValue(strategy=GenerationType.AUTO)
    @Column(name = "ORDEN_ID")
	private Long id;
	
	@Column(name="FECHA")
	@Type(type="date")
	@NotNull
	private Date fecha=new Date();
	
	@ManyToOne(optional = false,fetch=FetchType.EAGER)			
	@JoinColumn(name = "ALMACEN_ID", nullable = false)
	@NotNull(message="El almacen es mandatorio")
	private Almacen almacen;
	
	@Column(name="OBSERVACIONES")
	@Length(max=255)
	private String comentario;
	
	@OneToMany(cascade={
			 CascadeType.PERSIST
			,CascadeType.MERGE
			,CascadeType.REMOVE
			}
			,fetch=FetchType.LAZY,mappedBy="orden")
	@Cascade(value=org.hibernate.annotations.CascadeType.DELETE_ORPHAN)
	private Set<OrdenDeCorteDet> cortes=new HashSet<OrdenDeCorteDet>();
	
	@Column(name="ALMACEN_NOMBRE",nullable=false)
	private String almacenNombre; 
	
	@Column(name="MAQUILADOR_NOMBRE",nullable=false,length=70)
	private String maquiladorNombre;
	
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
	
	public Long getId() {
		return id;
	}
	
	public Almacen getAlmacen() {
		return almacen;
	}

	public void setAlmacen(Almacen almacen) {
		Object old=this.almacen;
		this.almacen = almacen;
		firePropertyChange("almacen", old, almacen);
		if(almacen!=null){
			setAlmacenNombre(almacen.getNombre());
			setMaquiladorNombre(almacen.getMaquilador().getNombre());
		}else{
			setAlmacen(null);
			setMaquiladorNombre(null);
		}
	}

	public String getAlmacenNombre() {
		return almacenNombre;
	}

	public void setAlmacenNombre(String almacenNombre) {
		this.almacenNombre = almacenNombre;
	}

	public String getMaquiladorNombre() {
		return maquiladorNombre;
	}

	public void setMaquiladorNombre(String maquiladorNombre) {
		this.maquiladorNombre = maquiladorNombre;
	}

	public Date getFecha() {
		return fecha;
	}

	public void setFecha(Date fecha) {
		Object old=this.fecha;
		this.fecha = fecha;
		firePropertyChange("fecha", old, fecha);
	}

	public String getComentario() {
		return comentario;
	}

	public void setComentario(String comentario) {
		Object old=this.comentario;
		this.comentario = comentario;
		firePropertyChange("comentario", old, comentario);
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

	public Set<OrdenDeCorteDet> getCortes() {
		return cortes;
	}
	
	public boolean addCorte(final OrdenDeCorteDet sc){
		sc.setOrden(this);
		return getCortes().add(sc);
	}

	public String toString(){
		return new ToStringBuilder(this,ToStringStyle.SHORT_PREFIX_STYLE)
		.append(getId())
		.append(fecha)
		.append(almacen)
		.toString();
	}
	
	@Override
	public boolean equals(Object obj) {
		if(obj==null) return false;
		if(obj ==this)return true;
		OrdenDeCorte oc=(OrdenDeCorte)obj;
		return new EqualsBuilder()
			.append(getId(),oc.getId())
			.isEquals();
	}
	
	@Override
	public int hashCode() {
		return new HashCodeBuilder(17,35)
		.append(getId())
		.toHashCode();
	}

}
