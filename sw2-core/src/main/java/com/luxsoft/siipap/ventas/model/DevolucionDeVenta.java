package com.luxsoft.siipap.ventas.model;

import java.math.BigDecimal;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.hibernate.annotations.Cascade;

import com.luxsoft.siipap.cxc.model.NotaDeCredito;
import com.luxsoft.siipap.inventarios.model.Inventario;
import com.luxsoft.siipap.model.CantidadMonetaria;

/**
 * Salida de inventario originado por una venta
 * 
 * @author Ruben Cancino
 *
 */
@Entity
@Table(name="SX_INVENTARIO_DEV")
public class DevolucionDeVenta extends Inventario{
	
	
	
	@ManyToOne(optional=false)
	@JoinColumn (name="DEVO_ID",nullable=false,updatable=false)	
	private Devolucion devolucion;
	
	@ManyToOne(optional=false)
	@JoinColumn (name="VENTADET_ID",nullable=false,updatable=false)
	private VentaDet ventaDet;
	
	
	@ManyToOne(optional=true)
	@JoinColumn (name="ABONO_ID",nullable=true)
	@Cascade(value={org.hibernate.annotations.CascadeType.REPLICATE
			})
	private NotaDeCredito nota;
	
	@Column(name="CORTES",nullable=false)
	private int cortes=0;

	public VentaDet getVentaDet() {
		return ventaDet;
	}

	public void setVentaDet(VentaDet ventaDet) {
		this.ventaDet = ventaDet;
		setProducto(ventaDet.getProducto());
		actualizarKilosDelMovimiento();
		
	}

	public Devolucion getDevolucion() {
		return devolucion;
	}

	public void setDevolucion(Devolucion devolucion) {
		this.devolucion = devolucion;
	}


	public NotaDeCredito getNota() {
		return nota;
	}

	public void setNota(NotaDeCredito nota) {
		this.nota = nota;
	}

	@Override
	public String getTipoDocto() {
		return "FAC";
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
	
	public BigDecimal getImporteDescuento(){
		CantidadMonetaria importeBruto=CantidadMonetaria.pesos(getImporteBruto().doubleValue());
		double descuento=getVentaDet().getDescuento()/100;
		CantidadMonetaria descImp=importeBruto.multiply(descuento);
		return descImp.amount();
		
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

	public int getCortes() {
		return cortes;
	}

	public void setCortes(int cortes) {
		this.cortes = cortes;
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
        final DevolucionDeVenta other = (DevolucionDeVenta) obj;
        return new EqualsBuilder()
        .append(getClave(), other.getClave())
        .append(getRenglon(),other.getRenglon())
        .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17,35)
        .append(getClave())
        .append(getRenglon())        
        .toHashCode();
    }

}
