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

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Predicate;
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



/**
 * Esta es la entidad que representa el documento de Recepcion de Material 
 * La RecepcionDeMaterial agrupa una seria de entradas unitarias todas
 * dirigidas al mismo almacen del maquilador
 * Mantiene una relacion BIDIRECCIONAL 1-many con EntradaDeMaterial en lo que se conoce
 * com relacion Padre-Hijo
 * 
 * @author Ruben Cancino
 *
 */
@Entity
@Table(name="SX_MAQ_ENTRADAS")
public class EntradaDeMaterial extends BaseBean{
	
    @Id @GeneratedValue(strategy=GenerationType.AUTO)
    @Column(name = "ENTRADA_ID")
	private Long id;
    
    @SuppressWarnings("unused")
	@Version
	private int version;
	
	@ManyToOne(optional = false,
			fetch=FetchType.EAGER)			
	@JoinColumn(name = "ALMACEN_ID", nullable = false,updatable=false)
	@NotNull(message="El almacen es mandatorio")
	private Almacen almacen;
	
	@Column(name="ALMACEN_NOMBRE",nullable=false,updatable=false)
	private String almacenNombre; 
	
	@Column(name="MAQUILADOR_NOMBRE",nullable=false,length=70)
	private String maquiladorNombre;
	
	@Column(name="ENTRADA_DE_MAQUILADOR",nullable=false,length=15)
	@Length(max=15)
	private String entradaDeMaquilador;
	
	@Column(name="FECHA",nullable=false)
	@Type(type="date")
	private Date fecha=new Date();
	
	@Column(name="FACTURA",length=40)
	@Length(max=40)
	private String factura;
	
	@Column(name="FABRICANTE",length=70)
	@Length(max=70)
	private String fabricante;
	
	@Column(name="OBSERVACIONES")
	@Length(max=255)
	private String observaciones;
	
	@OneToMany(cascade={
			 CascadeType.PERSIST
			,CascadeType.MERGE
			,CascadeType.REMOVE
			}
			,fetch=FetchType.LAZY,mappedBy="recepcion")
	@Cascade(value=org.hibernate.annotations.CascadeType.DELETE_ORPHAN)
	private Set<EntradaDeMaterialDet> partidas=new HashSet<EntradaDeMaterialDet>();
		
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
	public void setId(Long id) {
		this.id = id;
	}
	
	public String getEntradaDeMaquilador() {
		return entradaDeMaquilador;
	}
	public void setEntradaDeMaquilador(String entradaDeMaquilador) {
		Object old=this.entradaDeMaquilador;
		this.entradaDeMaquilador = entradaDeMaquilador;
		firePropertyChange("entradaDeMaquilador", old, entradaDeMaquilador);
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
	
	public Date getFecha() {
		return fecha;
	}
	public void setFecha(Date fecha) {
		Object old=this.fecha;
		this.fecha = fecha;
		firePropertyChange("fecha", old, fecha);
	}
	
	public String getObservaciones() {
		return observaciones;
	}
	public void setObservaciones(String observaciones) {
		Object old=this.observaciones;
		this.observaciones = observaciones;
		firePropertyChange("observaciones", old, observaciones);
	}
	
	
	
	public String getFactura() {
		return factura;
	}
	public void setFactura(String factura) {
		this.factura = factura;
	}
	public String getFabricante() {
		return fabricante;
	}
	public void setFabricante(String fabricante) {
		Object old=this.fabricante;
		this.fabricante = fabricante;
		firePropertyChange("fabricante", old, fabricante);
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
	public Set<EntradaDeMaterialDet> getPartidas() {
		return partidas;
	}
	
	public boolean agregarEntrada(final EntradaDeMaterialDet e){
		e.setRecepcion(this);
		return partidas.add(e);
		
	}
	
	public boolean eliminarEntrada(final EntradaDeMaterialDet det){
		det.setRecepcion(null);
		return partidas.remove(det);
	}
	
	public boolean tieneCortes(){
		return CollectionUtils.exists(getPartidas(),new Predicate(){
			public boolean evaluate(Object object) {
				EntradaDeMaterialDet em=(EntradaDeMaterialDet)object;
				return em.isCortado();
			}
		});
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
	
	
	
	
	@Override
	public String toString(){
		return new ToStringBuilder(this,ToStringStyle.SHORT_PREFIX_STYLE)
			.append(getId())
			.toString();
	}
	
	@Override
	public boolean equals(Object obj) {
		if(obj==null)return false;
		if(obj==this)return true;
		EntradaDeMaterial r=(EntradaDeMaterial)obj;		
		return new EqualsBuilder()
		.append(getId(),r.getId())
		.isEquals();
	}
	
	@Override
	public int hashCode() {
		return new HashCodeBuilder(17,35)
			.append(getId())
			.toHashCode();
	}
	

}
