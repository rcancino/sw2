package com.luxsoft.siipap.ventas.model;

import java.math.BigDecimal;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.Version;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.hibernate.annotations.AccessType;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
import org.hibernate.validator.Length;
import org.hibernate.validator.NotNull;

import com.luxsoft.siipap.model.BaseBean;
import com.luxsoft.siipap.model.CantidadMonetaria;
import com.luxsoft.siipap.model.core.Producto;

/**
 * Salida de inventario originado por una venta
 * 
 * @author Ruben Cancino
 *
 */
@Entity
@Table(name="SX_PREDEVOLUCIONES_DET")
@GenericGenerator(name="hibernate-uuid",strategy="uuid"
		,parameters={
				@Parameter(name="separator",value="-")
			}
		)
public class PreDevolucionDet extends BaseBean{
	
	@Id @GeneratedValue(generator="hibernate-uuid")
	@Column(name="PREDEVODET_ID")
	private String id;
	
	@Version
    private int version;
	
	@ManyToOne(optional = false)
    @JoinColumn(name = "PREDEVO_ID"
    	, nullable = false
    	, updatable = false,insertable=false
    	)
	private PreDevolucion preDevolucion;
	
	@ManyToOne(optional=false)
	@JoinColumn (name="VENTADET_ID",nullable=false,updatable=false)
	private VentaDet ventaDet;
    
    @ManyToOne (optional=false)
    @JoinColumn (name="PRODUCTO_ID", nullable=false,updatable=false) 
    @NotNull
    private Producto producto;   
    
    @Column(name="CLAVE",nullable=false,length=10)    
    private String clave;
    
    @Column(name="DESCRIPCION",nullable=false,length=250)    
    private String descripcion;
           
    @Column(name="CANTIDAD",nullable=false)
    @AccessType(value="field")
    private double cantidad=0;
    
    @Transient
    private double devueltas=0d;
    
    @Column(name="COMENTARIO")
    @Length(max=250)
    private String comentario;
    
	@Column(name="CORTES",nullable=false)
	private int cortes=0;
	
	@Column(name="RENGLON",nullable=false,insertable=false,updatable=false)
	private int renglon;
	
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public VentaDet getVentaDet() {
		return ventaDet;
	}
	
	public void setVentaDet(VentaDet ventaDet) {
		this.ventaDet = ventaDet;
		if(ventaDet!=null){
			setProducto(ventaDet.getProducto());
		}else
			setProducto(null);
	}
	
	public PreDevolucion getPreDevolucion() {
		return preDevolucion;
	}

	public void setPreDevolucion(PreDevolucion preDevolucion) {
		this.preDevolucion = preDevolucion;
	}	
	
	

	public int getRenglon() {
		return renglon;
	}

	public void setRenglon(int renglon) {
		this.renglon = renglon;
	}

	public Producto getProducto() {
		return producto;
	}

	public void setProducto(Producto producto) {
		this.producto = producto;
		if(producto!=null){
			setClave(producto.getClave());
			setDescripcion(producto.getDescripcion());
		}else{
			setClave(null);
			setDescripcion(null);
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

	public double getCantidad() {
		return cantidad;
	}

	public void setCantidad(double cantidad) {
		double old=this.cantidad;
		this.cantidad = cantidad;
		firePropertyChange("cantidad", old, cantidad);
		
	}
	
	public double getCantidadEnUnidad(){
    	return getCantidad()/getFactor();
    }

	public String getComentario() {
		return comentario;
	}

	public void setComentario(String comentario) {
		this.comentario = comentario;
	}

	public int getVersion() {
		return version;
	}

	public BigDecimal getPrecio(){
		if(getVentaDet()==null)
			return BigDecimal.ZERO;
		return getVentaDet().getPrecio();
	}
	
	public BigDecimal getImporteBruto(){
		CantidadMonetaria imp=CantidadMonetaria.pesos(getPrecio().doubleValue());
		double cant=getCantidad()/getFactor();
		return imp.multiply(cant).amount();
	}
	
	public BigDecimal getImporteNeto(){
		CantidadMonetaria importeBruto=CantidadMonetaria.pesos(getImporteBruto().doubleValue());
		double descuento=getVentaDet().getDescuento()/100;
		CantidadMonetaria descImp=importeBruto.multiply(descuento);
		return importeBruto.subtract(descImp).amount();
		
	}
	
	public BigDecimal getImporteCortesCalculado(){
		if(getVentaDet()!=null){
			double cant=getVentaDet().getCortes()/getFactor();
			CantidadMonetaria impCortes=CantidadMonetaria.pesos(cant);
			impCortes=impCortes.multiply(getVentaDet().getPrecioCorte().doubleValue());
			double descuento=0;
			if(getVentaDet().getVenta().getDescuentos().doubleValue()>0)
				descuento=1-getVentaDet().getVenta().getDescuentoGeneral();			
			impCortes=impCortes.multiply(descuento);
			return impCortes.amount();
		}
		return BigDecimal.ZERO;
		
	}

	public double getFactor(){
		return getProducto().getUnidad().getFactor();
	}
	
	public int getCortes() {
		return cortes;
	}

	public void setCortes(int cortes) {
		this.cortes = cortes;
	}
	
	public double getDevueltas() {
		return devueltas;
	}

	public void setDevueltas(double devueltas) {
		this.devueltas = devueltas;
	}
	
	public double getDisponible(){
		return Math.abs(getVentaDet().getCantidad())
		-Math.abs(getVentaDet().getDevueltas())
		-Math.abs(getDevueltas());
	}

	public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (obj == this) {
            return true;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final PreDevolucionDet other = (PreDevolucionDet) obj;
        return new EqualsBuilder()
        .append(getRenglon(),other.getRenglon())
        .isEquals();
    }
	
	public DevolucionDeVenta toDevoDet(){
		DevolucionDeVenta dev=new DevolucionDeVenta();
		dev.setCantidad(getCantidad());
		dev.setVentaDet(getVentaDet());
		dev.setComentario(getComentario());
		dev.setCortes(getCortes());
		dev.setRenglon(getRenglon());
		return dev;
	}

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17,35)
        .append(getRenglon())        
        .toHashCode();
    }

    public String toString(){
    	return new ToStringBuilder(this,ToStringStyle.SIMPLE_STYLE)
    	.append(getClave())
    	.append(getCantidad())
    	.toString();
    }
}
