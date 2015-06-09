package com.luxsoft.siipap.inventarios.model;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.Serializable;
import java.text.MessageFormat;

import javax.persistence.Column;
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
import org.hibernate.validator.AssertTrue;
import org.hibernate.validator.Length;
import org.hibernate.validator.NotNull;

import com.luxsoft.siipap.model.core.Producto;

/**
 * Entidad para el detalle del conteo de inventario
 * 
 * 
 * @author Ruben Cancino Ramos
 *
 */
@Entity
@Table(name="SX_SECTORDET"	
	)
@GenericGenerator(name="hibernate-uuid",strategy="uuid"
		,parameters={
				@Parameter(name="separator",value="-")
			}
		)
public class SectorDet implements Serializable
{
	
	@Id @GeneratedValue(generator="hibernate-uuid")
	@Column(name="SECTORDET_ID")
	protected String id;
	
	
	
	
	@Version
	private int version;
	
	@ManyToOne(optional = false)
    @JoinColumn(name = "SECTOR_ID"
    	, nullable = false
    	, updatable = false
    	,insertable=false
    	)
    private Sector sector;
	
	@ManyToOne(optional=false)
	@JoinColumn (name="PRODUCTO_ID",nullable=false)
	@NotNull
	private Producto producto;
	
	 
	
	@Column(name="CLAVE",nullable=false)
    private String clave;
    
    @Column(name="DESCRIPCION",nullable=false,length=250)
    private String descripcion;
	
    @Column(name="UNIDAD",length=3,nullable=false)
	private String unidad;
    
    @Column(name="FACTOR",nullable=false)
    private double factor;
    
    @Column(name="KILOS",scale=3,nullable=false)
    @NotNull
    private double kilos=0;
	
    @Column(name="CANTIDAD")
	private double cantidad;
    
    @Column(name="COMENTARIO",length=255)
	@Length(max=255)
	private String comentario;
	
    
    @Column(name="RNGL" , updatable = false
        	,insertable=false)
	private int renglon;
    
    
    @Column(name="IND")
   	private int ind;
       
       
       public int getInd() {
   		return ind;
   	}

   	public void setInd(int ind) {
   		int old=this.ind;
   		this.ind = ind;
   		firePropertyChange("ind", old, ind);
   	}
    
    public int getRenglon() {
		return renglon;
	}
	

	public int getVersion() {
		return version;
	}

	public void setVersion(int version) {
		this.version = version;
	}

	public Sector getSector() {
		return sector;
	}

	public void setSector(Sector sector) {
		this.sector = sector;
	}
	
	public Producto getProducto() {
		return producto;
	}
	
	public String getComentario() {
		return comentario;
	}

	public void setComentario(String comentario) {
		Object old=this.comentario;
		this.comentario = comentario;
		firePropertyChange("comentario", old, comentario);
	}

	public void setProducto(Producto producto) {
		Object oldvalue=this.producto;
        this.producto = producto;
        firePropertyChange("producto", oldvalue, producto);
        if(producto!=null){
        	//Copiamos algunas propiedades del producto 
           this.unidad=producto.getUnidad().getUnidad();
           this.clave=producto.getClave();
           this.descripcion=producto.getDescripcion();
           this.factor=producto.getUnidad().getFactor();
           this.kilos=producto.getKilos();
        }else{
        	this.unidad=null;
            this.clave=null;
            this.descripcion=null;
            this.factor=0;
            this.kilos=0;
        }
        
	}


	public String getClave() {
		return clave;
	}

	public void setClave(String clave) {
		this.clave = clave;
	}

	public String getDescripcion() {
		return descripcion;
	}

	public void setDescripcion(String descripcion) {
		this.descripcion = descripcion;
	}

	public String getUnidad() {
		return unidad;
	}

	public void setUnidad(String unidad) {
		this.unidad = unidad;
	}

	public double getFactor() {
		return factor;
	}

	public void setFactor(double factor) {
		this.factor = factor;
	}

	public double getKilos() {
		return kilos;
	}

	public void setKilos(double kilos) {
		this.kilos = kilos;
	}

	public double getCantidad() {
		return cantidad;
	}

	public void setCantidad(double cantidad) {
		double old=this.cantidad;
		this.cantidad = cantidad;
		firePropertyChange("cantidad", old, cantidad);
	}

	public String getId() {
		return id;
	}	
	
	public String toString(){
		String pattern="({0})" +
				" {1}" +
				" {2}";
		return MessageFormat.format(pattern, getClave(),getDescripcion(),getCantidad());
		
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
	
	/*@AssertTrue(message="Capture la cantidad")
	public boolean validarCantidad(){
		return getCantidad()>=0;
	}*/

}
