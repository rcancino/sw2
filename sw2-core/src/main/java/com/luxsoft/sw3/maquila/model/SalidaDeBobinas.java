package com.luxsoft.sw3.maquila.model;

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
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Version;

import org.hibernate.validator.Length;
import org.hibernate.validator.NotNull;
import org.springframework.util.Assert;

import com.luxsoft.siipap.maquila.model.EntradaDeMaquila;
import com.luxsoft.siipap.model.BaseBean;
import com.luxsoft.siipap.model.UserLog;
import com.luxsoft.siipap.model.core.Producto;
import com.luxsoft.sw3.model.AdressLog;



/**
 * Salida de bobinas que se venden directamente, es decir sin proceso de corte
 * el destino de las mismas debe ser un MAQ con unidad de medida en kilos
 * 
 * @author Ruben Cancino
 *
 */
@Entity
@Table(name="SX_MAQ_SALIDA_BOBINAS")
public class SalidaDeBobinas extends BaseBean{
	
	
	@Id @GeneratedValue(strategy=GenerationType.AUTO)
    @Column(name = "SALIDA_ID")
	private Long id;
	
	@Version
	private int version;
	
	@Column(name="FECHA",nullable=false)
	private Date fecha=new Date();	
		
	@ManyToOne(optional = false)
	@JoinColumn(name = "ENTRADADET_ID",nullable=false,updatable=false)
	private EntradaDeMaterialDet origen;
	
	@ManyToOne(optional = false)
	@JoinColumn(name = "INVENTARIO_ID",nullable=false,updatable=false)
	private EntradaDeMaquila destino;
	
	@ManyToOne(optional = false,fetch=FetchType.EAGER
			,cascade={CascadeType.MERGE,CascadeType.PERSIST})
	@JoinColumn(name = "PRODUCTO_ID", nullable = false)
	@NotNull(message="El producto destino es mandatorio")
	private Producto producto;
	
	@Column(name="CANTIDAD")
	private double cantidad;
	
	@Column(name = "COSTO", nullable = true)	
	private double costo;	
	
	@Column(name="COMENTARIO")
	@Length(max=255)
	private String comentario;
	
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

	public Date getFecha() {
		return fecha;
	}

	public void setFecha(Date fecha) {
		Object old=this.fecha;
		this.fecha = fecha;
		firePropertyChange("fecha", old, fecha);
	}

	public EntradaDeMaterialDet getOrigen() {
		return origen;
	}

	public void setOrigen(EntradaDeMaterialDet origen) {
		this.origen = origen;
		if(origen!=null){
			setProducto(origen.getProducto());
		}
		
	}

	public EntradaDeMaquila getDestino() {
		return destino;
	}

	public void setDestino(EntradaDeMaquila destino) {
		Assert.notNull(destino);
		Assert.isTrue(destino.getProducto().equals(getOrigen().getProducto()),"Origen y Destino no son de la misma bobina");
		Assert.isTrue(destino.getUnidad().getFactor()==1,"El destino no esta en kilos");
		this.destino = destino;
	}

	public Producto getProducto() {
		return producto;
	}

	public void setProducto(Producto producto) {
		this.producto = producto;
	}

	public double getCantidad() {
		return cantidad;
	}

	public void setCantidad(double cantidad) {
		double old=this.cantidad;
		this.cantidad = cantidad;
		firePropertyChange("cantidad", old, cantidad);
	}

	public double getCosto() {
		return costo;
	}

	public void setCosto(double costo) {
		this.costo = costo;
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

	public Long getId() {
		return id;
	}

	public int getVersion() {
		return version;
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
		SalidaDeBobinas other = (SalidaDeBobinas) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}
	
	public String toString(){
		return MessageFormat.format("Bobina: {0} Cant: {1} Origen: {3} Maq:{4}"
				, getProducto().getClave(),getCantidad(),getOrigen().getEntradaDeMaquilador(),getDestino().getDocumento());
	}
	
}
