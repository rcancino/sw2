package com.luxsoft.siipap.inventarios.model;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Transient;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.hibernate.annotations.Parent;
import org.hibernate.validator.Length;
import org.hibernate.validator.NotNull;

import com.luxsoft.siipap.model.core.Producto;

/**
 * Detalle de un traslado
 * 
 * @author Ruben Cancino
 *
 */
@Embeddable
public class SolicitudDeTrasladoDet implements Serializable{
	 
	
    @Column (name="SUCURSAL_ID", nullable=false)    
	private Long sucursal;
    
    @Column (name="ORIGEN_ID", nullable=false)
	private Long origen;
	
	@ManyToOne (optional=false)
    @JoinColumn (name="PRODUCTO_ID", nullable=false) 
    @NotNull
    private Producto producto;
	
	@Column(name="SOLICITADO",nullable=false)
    private double solicitado=0;
	
	
	@Column(name="RECIBIDO",nullable=false)
	private double recibido=0;
	
	@Transient
	private double existencia=0;
	
	@Column(name="COMENTARIO")
	@Length(max=255)
	private String comentario;
	
	@Transient
	@Length(max=255)
	private String comentarioTps;
	
	@Parent
	private SolicitudDeTraslado solicitud;
	
	@Column(name="RENGLON")
	private int renglon=1;
	
	public SolicitudDeTrasladoDet() {}

	public SolicitudDeTrasladoDet(Producto producto, double solicitado) {
		this.producto = producto;
		this.solicitado = solicitado;
	}
	
	

	public int getRenglon() {
		return renglon;
	}

	public void setRenglon(int renglon) {
		this.renglon = renglon;
	}

	public Long getSucursal() {
		return sucursal;
	}

	public void setSucursal(Long sucursal) {
		this.sucursal = sucursal;
	}

	public Long getOrigen() {
		return origen;
	}

	public void setOrigen(Long origen) {
		this.origen = origen;
	}

	public Producto getProducto() {
		return producto;
	}

	public void setProducto(Producto producto) {
		this.producto = producto;
	}

	public double getSolicitado() {
		return solicitado;
	}

	public void setSolicitado(double solicitado) {
		this.solicitado = solicitado;
	}
	
	

	public double getRecibido() {
		return recibido;
	}

	public void setRecibido(double recibido) {
		double old=this.recibido;
		this.recibido = recibido;
		firePropertyChange("recibido", old, recibido);
	}
	

	public String getComentario() {
		return comentario;
	}

	public void setComentario(String comentario) {
		Object old=this.comentario;
		this.comentario = comentario;
		firePropertyChange("comentario", old, comentario);
		
	}

	public SolicitudDeTraslado getSolicitud() {
		return solicitud;
	}

	public void setSolicitud(SolicitudDeTraslado solicitud) {
		this.solicitud = solicitud;
	}
	

	public double getExistencia() {
		return existencia;
	}

	public void setExistencia(double existencia) {
		this.existencia = existencia;
	}
	
	public String getComentarioTps() {
		return comentarioTps;
	}

	public void setComentarioTps(String comentarioTps) {
		Object old=this.comentarioTps;
		this.comentarioTps = comentarioTps;
		firePropertyChange("comentarioTps", old, comentarioTps);
	}
	
	public double getNuevaExistencia(){
		return getExistencia()-getRecibido();
	}

	public String toString(){
		return new ToStringBuilder(this,ToStringStyle.SHORT_PREFIX_STYLE)
		.append(getProducto())
		.append(getSolicitado())
		.toString();
	}
	
	
	protected transient PropertyChangeSupport support=new PropertyChangeSupport(this);
	
	public final synchronized void addPropertyChangeListener(
             PropertyChangeListener listener) {
		support.addPropertyChangeListener(listener);
		 
	}
	 
	public final synchronized void removePropertyChangeListener(
             PropertyChangeListener listener) {
		support.removePropertyChangeListener(listener);		
	}
	
	public void firePropertyChange(String propertyName, Object old, Object value){
		support.firePropertyChange(propertyName, old, value);
	}

	
}
