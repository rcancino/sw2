package com.luxsoft.siipap.inventarios.model;

import java.math.BigDecimal;
import java.util.Date;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

/**
 * 
 * @author Ruben Cancino Ramos
 *
 */
public class MovimientoRow {	
	
	private String id;
	
    private String tipo;    
    
    private String sucursal;
    
    private Long documento;
    
    private Date fecha=new Date();  
        
    private String clave;
        
    private String descripcion;
   
    private boolean nacional=true;
        
    private double cantidad=0;
        
    private String unidad;
    
	private double factor;
    
    private BigDecimal costo=BigDecimal.ZERO;
    
    private BigDecimal costoPromedio=BigDecimal.ZERO;
    
    private BigDecimal costoUltimo=BigDecimal.ZERO;
    
    private String comentario;
    
    public MovimientoRow(){}
    
    public MovimientoRow(Inventario inv){
    	
    	this.id=inv.getId();    	
        this.tipo=inv.getTipoDocto();
        this.sucursal=inv.getSucursal().getNombre();
        this.documento=inv.getDocumento();
        this.fecha=inv.getFecha();
        this.clave=inv.getClave();
        this.descripcion=inv.getDescripcion();
        this.nacional=inv.isNacional();
        this.cantidad=inv.getCantidad();
        this.unidad=inv.getUnidad().getUnidad();
        this.factor=inv.getFactor();
        this.costo=inv.getCosto();
        this.costoPromedio=inv.getCostoPromedio();
        this.costoUltimo=inv.getCostoUltimo();
        this.comentario=inv.getComentario();
    }

	public String getId() {
		return id;
	}	

	public String getTipo() {
		return tipo;
	}

	public void setTipo(String tipo) {
		this.tipo = tipo;
	}

	public String getSucursal() {
		return sucursal;
	}

	public void setSucursal(String sucursal) {
		this.sucursal = sucursal;
	}

	public Long getDocumento() {
		return documento;
	}

	public void setDocumento(Long documento) {
		this.documento = documento;
	}

	public Date getFecha() {
		return fecha;
	}

	public void setFecha(Date fecha) {
		this.fecha = fecha;
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

	public boolean isNacional() {
		return nacional;
	}

	public void setNacional(boolean nacional) {
		this.nacional = nacional;
	}

	public double getCantidad() {
		return cantidad;
	}

	public void setCantidad(double cantidad) {
		this.cantidad = cantidad;
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

	public BigDecimal getCosto() {
		return costo;
	}

	public void setCosto(BigDecimal costo) {
		this.costo = costo;
	}

	public BigDecimal getCostoPromedio() {
		return costoPromedio;
	}

	public void setCostoPromedio(BigDecimal costoPromedio) {
		this.costoPromedio = costoPromedio;
	}

	public BigDecimal getCostoUltimo() {
		return costoUltimo;
	}

	public void setCostoUltimo(BigDecimal costoUltimo) {
		this.costoUltimo = costoUltimo;
	}

	public String getComentario() {
		return comentario;
	}

	public void setComentario(String comentario) {
		this.comentario = comentario;
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
		MovimientoRow other = (MovimientoRow) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}
    
	public String toString(){
		return ToStringBuilder.reflectionToString(this,ToStringStyle.SIMPLE_STYLE); 
	}
    
}
