package com.luxsoft.siipap.cxp.model;

import java.math.BigDecimal;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.hibernate.validator.AssertTrue;
import org.hibernate.validator.NotNull;

import com.luxsoft.siipap.compras.model.EntradaPorCompra;
import com.luxsoft.siipap.model.BaseBean;
import com.luxsoft.siipap.model.CantidadMonetaria;
import com.luxsoft.siipap.util.MonedasUtils;

/**
 * Representa el analisis a detalle de una factura de proveedor
 * para justificar su pago con entradas al inventario
 * 
 * @author Ruben Cancino
 *
 */
@Entity
@Table(name="SX_CXP_ANALISISDET")
public class CXPAnalisisDet extends BaseBean{
	
	@Id @GeneratedValue(strategy=GenerationType.AUTO)	
	private Long id;
	
	@ManyToOne(optional=false)
	@JoinColumn (name="CXP_ID",nullable=false,updatable=false)
	private CXPFactura factura;
	
	@ManyToOne(optional=false,cascade={CascadeType.PERSIST})
	@JoinColumn (name="ENTRADA_ID")
	@NotNull
	private EntradaPorCompra entrada;
	
	@Column(name="CANTIDAD",nullable=false)
	private double cantidad=0;
	
	@Column (name="PRECIO",nullable=false,scale=2,precision=10)
	private BigDecimal precio=BigDecimal.ZERO;
	
	@Column (name="DESC1",scale=4)
	private double desc1=0;
	
	@Column (name="DESC2",scale=4)
	private double desc2=0;
	
	@Column (name="DESC3",scale=4)
	private double desc3=0;
	
	@Column (name="DESC4",scale=4)
	private double desc4=0;
	
	@Column (name="DESC5",scale=4)
	private double desc5=0;
	
	@Column (name="DESC6",scale=4)
	private double desc6=0;
	
	private double descuentof=0;
	
	@Column (name="COSTO",nullable=false,scale=6,precision=16)
	private BigDecimal costo=BigDecimal.ZERO;

	@Column (name="NETO",nullable=false,scale=6,precision=16)
	private BigDecimal neto=BigDecimal.ZERO;
	
	@Column (name="IMPORTE",nullable=false,scale=2,precision=16)
	private BigDecimal importe=BigDecimal.ZERO;
	
	//@Column (name="APAGAR",nullable=false,scale=2,precision=16)
	//private BigDecimal apagar=BigDecimal.ZERO;
	
	@Column(name="SIIPAPWIN_ID")
	private Long siipapId;
	
	public Long getId() {
		return id;
	}

	public CXPFactura getFactura() {
		return factura;
	}

	public void setFactura(CXPFactura cXPFactura) {
		this.factura = cXPFactura;
	}

	public EntradaPorCompra getEntrada() {
		return entrada;
	}

	public void setEntrada(EntradaPorCompra entrada) {
		this.entrada = entrada;
	}

	public double getCantidad() {
		return cantidad;
	}

	public void setCantidad(double cantidad) {
		double old=this.cantidad;
		this.cantidad = cantidad;
		firePropertyChange("cantidad", old, cantidad);
	}
	
	
	public BigDecimal getPrecio() {
		return precio;
	}

	public void setPrecio(BigDecimal precio) {
		Object old=this.precio;
		this.precio = precio;
		firePropertyChange("precio", old, precio);
	}

	public double getDesc1() {
		return desc1;
	}

	public void setDesc1(double desc1) {
		double old=this.desc1;
		this.desc1 = desc1;
		firePropertyChange("desc1", old, desc1);
	}

	public double getDesc2() {
		return desc2;
	}

	public void setDesc2(double desc2) {
		double old=this.desc2;
		this.desc2 = desc2;
		firePropertyChange("desc2", old, desc2);
	}

	public double getDesc3() {
		return desc3;
	}

	public void setDesc3(double desc3) {
		double old=this.desc3;
		this.desc3 = desc3;
		firePropertyChange("desc3", old, desc3);
	}

	public double getDesc4() {
		return desc4;
	}

	public void setDesc4(double desc4) {
		double old=this.desc4;
		this.desc4 = desc4;
		firePropertyChange("desc4", old, desc4);
	}
	
	

	public double getDesc5() {
		return desc5;
	}

	public void setDesc5(double desc5) {
		double old=this.desc5;
		this.desc5 = desc5;
		firePropertyChange("desc5", old, desc5);
	}

	public double getDesc6() {
		return desc6;
	}

	public void setDesc6(double desc6) {
		Object old=this.desc6;
		this.desc6 = desc6;
		firePropertyChange("desc6", old, desc6);
	}

	public double getDescuentof() {
		return descuentof;
	}

	public void setDescuentof(double descuentof) {
		this.descuentof = descuentof;
	}

	public BigDecimal getCosto() {
		return costo;
	}

	public void setCosto(BigDecimal costo) {
		Object old=this.costo;
		this.costo = costo;
		firePropertyChange("costo", old, costo);
	}

	public BigDecimal getNeto() {
		return neto;
	}

	public void setNeto(BigDecimal neto) {
		this.neto = neto;
	}
	
	public BigDecimal getImporte() {
		return importe;
	}

	public void setImporte(BigDecimal importe) {
		Object old=this.importe;
		this.importe = importe;
		firePropertyChange("importe", old, importe);
	}
	
	/**
	 * El importe a pagar, normalmente es igual al importe calculado
	 *  a partir de los descuentos y el precio. A este importe anteriormente
	 *  se le conocia como analizado
	 *   
	 * @return
	 *//*
	public BigDecimal getApagar() {
		return apagar;
	}

	public void setApagar(BigDecimal apagar) {
		this.apagar = apagar;
	}
	*/
	/**
	 * Utils solo para la importacion desde siipapwin
	 * 
	 * @return
	 */
	public Long getSiipapId() {
		return siipapId;
	}

	public void setSiipapId(Long siipapId) {
		this.siipapId = siipapId;
	}

	/**
	 * Calcula el importe multiplicando primero el precio y posteriormente  aplicando
	 * los descuentos
	 *  
	 */
	public void calcularImporte(){
		if(this.precio!=null){
			BigDecimal importe=this.precio.multiply(BigDecimal.valueOf(cantidad/entrada.getFactor()));
			BigDecimal res=MonedasUtils.aplicarDescuentosEnCascada(importe, desc1,desc2,desc3,desc4,desc5,desc6);
			//res=res.multiply(BigDecimal.valueOf(getFactura().getTc()));
			setImporte(res);
			calcularCosto();
			calcularNeto();
		}
	}
	
	public CantidadMonetaria getImporteBrutoCalculadoMN(){
		BigDecimal importe=this.precio.multiply(BigDecimal.valueOf(cantidad/entrada.getFactor()));
		//BigDecimal res=MonedasUtils.aplicarDescuentosEnCascada(importe, desc1,desc2,desc3,desc4,desc5,desc6);
		CantidadMonetaria resmn=CantidadMonetaria.pesos(importe);
		resmn=resmn.multiply(getFactura().getTc());
		return resmn;
	}
	
	public CantidadMonetaria getImporteNetoCalculadoMN(){
		BigDecimal importe=this.precio.multiply(BigDecimal.valueOf(cantidad/entrada.getFactor()));
		importe=MonedasUtils.aplicarDescuentosEnCascada(importe, desc1,desc2,desc3,desc4,desc5,desc6);
		CantidadMonetaria resmn=CantidadMonetaria.pesos(importe);
		resmn=resmn.multiply(getFactura().getTc());
		return resmn;
	}

	private void calcularCosto(){
		if(this.importe!=null ){
			/*CantidadMonetaria imp=CantidadMonetaria.pesos(this.importe.doubleValue());
			double factor=entrada.getFactor();
			imp=imp.divide(this.cantidad/factor);
			setCosto(imp.amount());*/
			
			BigDecimal pr=this.precio;
			BigDecimal res=MonedasUtils.aplicarDescuentosEnCascadaSinRedondeo(pr, desc1,desc2,desc3,desc4,desc5,desc6);
			setCosto(res);
			//System.out.println("Costo :"+res);
		}
	}
	
	private void calcularNeto(){
		if(this.precio!=null){
			CantidadMonetaria res=CantidadMonetaria.pesos(importe.doubleValue());
			res=MonedasUtils.aplicarDescuentosEnCascada(res, descuentof);
			setNeto(res.amount());
		}
	}
	
	public void actualizarInventario(){
		if(getEntrada()!=null){
			getEntrada().setCosto(getCostoUnitarioMN());
		}
	}
	
	public BigDecimal getCostoUnitarioMN(){
		CantidadMonetaria costo=CantidadMonetaria.pesos(this.costo.doubleValue());
		return costo.multiply(getFactura().getTc()).amount();
	}
	
	@Transient
	private BigDecimal precioOriginal=BigDecimal.ZERO;
	
	

	public BigDecimal getPrecioOriginal() {
		return precioOriginal;
	}
	
	public void setPrecioOriginal(BigDecimal precioOrigen) {
		this.precioOriginal = precioOrigen;
	}

	public double getCantidadEnFactor() {
		return cantidad/getFactor();
	}

	public void setCantidadEnFactor(double cantidad) {
		setCantidad(cantidad*getFactor());
	}
	
	public double getFactor(){
		return getEntrada().getFactor();
	}

	public String toString(){
		return new ToStringBuilder(this,ToStringStyle.SIMPLE_STYLE)
		.append(getEntrada())
		.append(getCantidad())
		.toString();
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder(45,73)
		.append(this.entrada.getId())
		//.append(this.entrada.getProducto())
		//.append(this.entrada.getRemision())
		.toHashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		CXPAnalisisDet other = (CXPAnalisisDet) obj;
		return new EqualsBuilder()
		.append(this.entrada.getId(), other.getEntrada().getId())
		.isEquals();
	}
	
	@AssertTrue(message="La cantidad debe ser >=0")
	public boolean validarCantidad(){
		return cantidad>0;
	}
	
	

}
