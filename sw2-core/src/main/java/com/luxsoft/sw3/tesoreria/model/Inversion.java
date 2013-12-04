package com.luxsoft.sw3.tesoreria.model;

import java.math.BigDecimal;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Predicate;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.apache.commons.lang.time.DateUtils;
import org.hibernate.annotations.Type;
import org.hibernate.validator.AssertTrue;

import com.luxsoft.siipap.model.CantidadMonetaria;
import com.luxsoft.siipap.model.tesoreria.CargoAbono;
import com.luxsoft.siipap.util.DateUtil;
import com.luxsoft.siipap.util.MonedasUtils;


@Entity
@DiscriminatorValue("INVERSION")
public class Inversion extends TraspasoDeCuenta{
	
	@Column(name = "RENDIMIENTO_FECHA", nullable = true)
	@Type(type = "date")
	private Date rendimientoFecha;
	
	@Column(name="RENDIMIENTO_REAL",nullable=true)
	private BigDecimal rendimientoReal=BigDecimal.ZERO;
	
	@Column(name="RENDIMIENTO_CALCULADO",nullable=true)
	private BigDecimal rendimientoCalculado=BigDecimal.ZERO;
	
	@Column(name="RENDIMIENTO_IMPUESTO",nullable=false)
	private BigDecimal rendimientoImpuesto=BigDecimal.ZERO;
	
	@Column(name="IMPORTE_ISR",nullable=true)
	private BigDecimal importeRealISR=BigDecimal.ZERO;
	
	@Column(name="TASA")
	private double tasa=0;
	
	@Column(name="ISR")
	private double isr=0;
	
	@Column(name="PLAZO")
	private int plazo;
	
	@Column(name = "VENCIMIENTO", nullable = true)
	@Type(type = "date")
	private Date vencimiento;

	public BigDecimal getRendimientoReal() {
		return rendimientoReal;
	}

	public void setRendimientoReal(BigDecimal rendimientoReal) {
		Object old=this.rendimientoReal;
		this.rendimientoReal = rendimientoReal;
		firePropertyChange("rendimientoReal", old, rendimientoReal);
		if(rendimientoReal!=null){
			setRendimientoImpuesto(MonedasUtils.calcularImpuesto(rendimientoReal));
		}
	}

	public BigDecimal getRendimientoCalculado() {
		return rendimientoCalculado;
	}

	public void setRendimientoCalculado(BigDecimal rendimientoCalculado) {
		Object old=this.rendimientoCalculado;
		this.rendimientoCalculado = rendimientoCalculado;
		firePropertyChange("rendimientoCalculado", old, rendimientoCalculado);
	}

	public double getTasa() {
		return tasa;
	}

	public void setTasa(double tasa) {
		double old=this.tasa;
		this.tasa = tasa;
		firePropertyChange("tasa", old, tasa);
	}
	
	

	public double getIsr() {
		return isr;
	}

	public void setIsr(double isr) {
		double old=this.isr;
		this.isr = isr;
		firePropertyChange("isr", old, isr);
	}

	public int getPlazo() {
		return plazo;
	}

	public void setPlazo(int plazo) {
		int old=this.plazo;
		this.plazo = plazo;
		firePropertyChange("plazo", old, plazo);
		setVencimiento(DateUtils.addDays(getFecha(), plazo));
		
	}

	public Date getVencimiento() {
		return vencimiento;
	}
	
	public String getDescripcion(){
		return "INVERSION";
	}

	public void setVencimiento(Date vencimiento) {
		Object old=this.vencimiento;
		this.vencimiento = vencimiento;
		firePropertyChange("vencimiento", old, vencimiento);
	}
	
	
	public Date getRendimientoFecha() {
		return rendimientoFecha;
	}

	public void setRendimientoFecha(Date rendimientoFecha) {
		Object old=this.rendimientoFecha;
		this.rendimientoFecha = rendimientoFecha;
		firePropertyChange("rendimientoFecha", old, rendimientoFecha);
	}
	
	public BigDecimal getRendimientoImpuesto() {
		return rendimientoImpuesto;
	}

	public void setRendimientoImpuesto(BigDecimal rendimientoImpuesto) {
		Object old=this.rendimientoImpuesto;
		this.rendimientoImpuesto = rendimientoImpuesto;
		firePropertyChange("rendimientoImpuesto", old, rendimientoImpuesto);
	}
	

	public BigDecimal getImporteRealISR() {
		return importeRealISR;
	}

	public void setImporteRealISR(BigDecimal importeRealISR) {
		Object old=this.importeRealISR;
		this.importeRealISR = importeRealISR;
		firePropertyChange("importeRealISR", old, importeRealISR);
	}

	public String toString(){
		return new ToStringBuilder(this,ToStringStyle.SIMPLE_STYLE)
		.append("INVERSION ")
		.append(getId())
		.append(getImporte())
		.toString();
	}
	
	public CargoAbono buscarRegistroDeIntereses(){
		CargoAbono found=(CargoAbono) CollectionUtils.find(getMovimientos(), new Predicate() {
			public boolean evaluate(Object object) {
				CargoAbono ca=(CargoAbono)object;
				return StringUtils.equals(ca.getClasificacion(), Clasificacion.INTERESES.name());
			}
		});
		return found;
	}
	
	public CargoAbono buscarRegistroDeIvaIntereses(){
		CargoAbono found=(CargoAbono) CollectionUtils.find(getMovimientos(), new Predicate() {
			public boolean evaluate(Object object) {
				CargoAbono ca=(CargoAbono)object;
				return StringUtils.equals(ca.getClasificacion(), Clasificacion.IMPUESTO_POR_INTERESES.name());
			}
		});
		return found;
	}
	
	public CargoAbono buscarRegresoDeInversion(){
		CargoAbono found=(CargoAbono) CollectionUtils.find(getMovimientos(), new Predicate() {
			public boolean evaluate(Object object) {
				CargoAbono ca=(CargoAbono)object;
				return StringUtils.equals(ca.getClasificacion(), Clasificacion.RETIRO_POR_INVERSION.name());
			}
		});
		return found;
	}
	
	public CargoAbono buscarMovimiento(final Clasificacion tipo){
		CargoAbono found=(CargoAbono) CollectionUtils.find(getMovimientos(), new Predicate() {
			public boolean evaluate(Object object) {
				CargoAbono ca=(CargoAbono)object;
				return StringUtils.equals(ca.getClasificacion(), tipo.name());
			}
		});
		return found;
	}
	
	@AssertTrue(message="Vencimiento invalido")
	public boolean vencimientoValido(){
		if(getVencimiento()!=null && getFecha()!=null){
			return getVencimiento().compareTo(getFecha())>0;
		}
		return false;
	}
	
	public void actualizarRendimientoCalculado(){
		BigDecimal un=BigDecimal.valueOf( (getTasa())/100 );
		BigDecimal rendimcientoCalculado=getImporte().multiply(un);
		rendimcientoCalculado=BigDecimal.valueOf(rendimcientoCalculado.doubleValue()/360);
		rendimcientoCalculado=rendimcientoCalculado.multiply(BigDecimal.valueOf(getPlazo()));
		rendimcientoCalculado=rendimcientoCalculado.subtract(getImporteISR());
		setRendimientoCalculado(CantidadMonetaria.pesos(rendimcientoCalculado).amount());
	}
	
	/**
	 * Rendimiento calculado en linea
	 * 
	 * @return
	 */
	public BigDecimal  getRendimientoNeto(){
		BigDecimal un=BigDecimal.valueOf( (getTasa())/100 );
		BigDecimal rendimcientoCalculado=getImporte().multiply(un);
		rendimcientoCalculado=BigDecimal.valueOf(rendimcientoCalculado.doubleValue()/360);
		rendimcientoCalculado=rendimcientoCalculado.multiply(BigDecimal.valueOf(getPlazo()));
		rendimcientoCalculado=rendimcientoCalculado.subtract(getImporteISR());
		//setRendimientoCalculado(CantidadMonetaria.pesos(rendimcientoCalculado).amount());
		return CantidadMonetaria.pesos(rendimcientoCalculado).amount();
	}
	
	public BigDecimal getImporteISR(){
		BigDecimal un=BigDecimal.valueOf( (getIsr())/100 );
		BigDecimal importeISR=getImporte().multiply(un);
		//System.out.println("Dias inv :"+getCuentaDestino().getBanco().getDiasInversionIsr());
		importeISR=BigDecimal.valueOf(importeISR.doubleValue()/getCuentaDestino().getBanco().getDiasInversionIsr());
		importeISR=importeISR.multiply(BigDecimal.valueOf(getPlazo()));
		return CantidadMonetaria.pesos(importeISR).amount();
	}

}
