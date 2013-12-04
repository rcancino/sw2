package com.luxsoft.sw3.bi;

import java.math.BigDecimal;
import java.text.MessageFormat;
import java.util.Currency;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.hibernate.validator.NotNull;

import com.luxsoft.siipap.inventarios.model.CostoPromedio;
import com.luxsoft.siipap.model.BaseBean;
import com.luxsoft.siipap.model.CantidadMonetaria;
import com.luxsoft.siipap.model.core.Producto;
import com.luxsoft.siipap.util.MonedasUtils;

@Entity
@Table(name="SX_SIMULADOR_PRECIOS_DET")
public class SimuladorDePreciosPorClienteDet extends BaseBean{
	
	@Id @GeneratedValue(strategy=GenerationType.AUTO)
	private Long id;
	
	
	@ManyToOne(optional=true,fetch=FetchType.EAGER)
	@JoinColumn(name="LISTA_ID",nullable=true,updatable=false)
	private SimuladorDePreciosPorCliente lista;
	
	@ManyToOne(optional=false,fetch=FetchType.EAGER)
	@JoinColumn(name="PRODUCTO_ID",nullable=false)
	@NotNull
	private Producto producto;
	
	
	@Column(name="MONEDA",nullable=false)
	@NotNull
	private Currency moneda=MonedasUtils.PESOS;
	
	@Column(name="PRECIO_LISTA",nullable=false)
	@NotNull
	private double precioDeLista=0;
	
	@Column(name="DESCUENTO",nullable=false)
	private double descuento=0;
	
	
	
	@Column(name="PRECIO",nullable=false)
	@NotNull
	private double precio=0;
	
	@Column(name="DIFERENCIA")
    private BigDecimal diferencia=BigDecimal.ZERO;
	
	@Column(name="MARGEN")
	private double margen=0;
    
    @Column(name="COSTOP")
    private BigDecimal costoPromedio=BigDecimal.ZERO;
    
    @Column(name="COSTOU")
    private BigDecimal costoUltimo=BigDecimal.ZERO;
    
    @Column(name="COSTOR")
    private BigDecimal costoRepo=BigDecimal.ZERO;
    
    @Column(name="COSTO")
    private BigDecimal costo=BigDecimal.ZERO;
	
	@Column(name="PRECIOK",nullable=false)
	@NotNull
	private double precioKilo=0;
	
	@Column(name="CANTIDAD_ACUMULADA")
	private BigDecimal cantidadAcumulada=BigDecimal.ZERO;
	
	@Column(name="VENTA_ACUMULADA")
	private BigDecimal ventaAcumulada=BigDecimal.ZERO;
	
	@Column(name="VENTA_ANTERIOR")
	private BigDecimal ventaPeriodoAnterior=BigDecimal.ZERO;
	
	@Column(name="VENTA_PERIODO_ANT")
	private BigDecimal ventaPeriodoInmediatoAnterior=BigDecimal.ZERO;
	
	
	@Column(name="PRECIO_MINIMO")
	private BigDecimal precioMinimo=BigDecimal.ZERO;
	
	@Transient
	private CostoPromedio costoPromedioRef;
	
	public Long getId() {
		return id;
	}	

	public SimuladorDePreciosPorCliente getLista() {
		return lista;
	}

	public void setLista(SimuladorDePreciosPorCliente lista) {
		this.lista = lista;
	}

	public Producto getProducto() {
		return producto;
	}

	public void setProducto(Producto producto) {
		Object old=this.producto;
		this.producto = producto;
		firePropertyChange("producto", old, producto);
		
		
	}

	

	public Currency getMoneda() {
		return moneda;
	}

	public void setMoneda(Currency moneda) {
		this.moneda = moneda;
	}	

	public double getPrecioDeLista() {
		return precioDeLista;
	}

	public void setPrecioDeLista(double precioDeLista) {
		Object old=this.precioDeLista;
		this.precioDeLista = precioDeLista;
		firePropertyChange("precioDeLista", old, precioDeLista);
	}
	

	public double getPrecio() {
		return precio;
	}

	public void setPrecio(double precio) {
		Object old=this.precio;
		this.precio = precio;
		firePropertyChange("precio", old, precio);
	}

	public double getPrecioKilo() {
		return precioKilo;
	}

	public void setPrecioKilo(double precioKilo) {
		Object old=this.precioKilo;
		this.precioKilo = precioKilo;
		firePropertyChange("precioKilo", old, precioKilo);
	}
	
	public double getDescuento() {
		return descuento;
	}

	public void setDescuento(double descuento) {
		Object old=this.descuento;
		this.descuento = descuento;
		firePropertyChange("descuento", old, descuento);
	}

	public BigDecimal getCostoPromedio() {
		return costoPromedio;
	}

	public void setCostoPromedio(BigDecimal costoPromedio) {
		BigDecimal old=this.costoPromedio;
		this.costoPromedio = costoPromedio;
		firePropertyChange("costoPromedio", old, costoPromedio);
	}

	public BigDecimal getCostoUltimo() {
		return costoUltimo;
	}

	public void setCostoUltimo(BigDecimal costoUltimo) {
		BigDecimal old=this.costoUltimo;
		this.costoUltimo = costoUltimo;
		firePropertyChange("costoUltimo", old, costoUltimo);
	}

	public BigDecimal getCostoRepo() {
		return costoRepo;
	}

	public void setCostoRepo(BigDecimal costoRepo) {
		BigDecimal old=this.costoRepo;
		this.costoRepo = costoRepo;
		firePropertyChange("costoRepo", old, costoRepo);
	}

	public BigDecimal getVentaAcumulada() {
		return ventaAcumulada;
	}

	public void setVentaAcumulada(BigDecimal ventaAcumulada) {
		Object old=this.ventaAcumulada;
		firePropertyChange("ventaAcumulada", old, ventaAcumulada);
		this.ventaAcumulada = ventaAcumulada;
	}

	public BigDecimal getVentaPeriodoAnterior() {
		return ventaPeriodoAnterior;
	}

	public void setVentaPeriodoAnterior(BigDecimal ventaPeriodoAnterior) {
		this.ventaPeriodoAnterior = ventaPeriodoAnterior;
	}

	public BigDecimal getVentaPeriodoInmediatoAnterior() {
		return ventaPeriodoInmediatoAnterior;
	}

	public void setVentaPeriodoInmediatoAnterior(
			BigDecimal ventaPeriodoInmediatoAnterior) {
		this.ventaPeriodoInmediatoAnterior = ventaPeriodoInmediatoAnterior;
	}

	public BigDecimal getDiferencia() {
		return diferencia;
	}

	public void setDiferencia(BigDecimal diferencia) {
		this.diferencia = diferencia;
	}

	public double getMargen() {
		return margen;
	}

	public void setMargen(double margen) {
		this.margen = margen;
	}
	
	public double getMargenCalculado(){
		if(getCosto()==null)
			return 0.0;
		if(getCosto().doubleValue()==0.0)
			return 0.0;
		BigDecimal dif=getPrecioNeto().subtract(getCosto());
		return (dif.doubleValue()/getPrecioNeto().doubleValue())*100;
	}

	public BigDecimal getCantidadAcumulada() {
		return cantidadAcumulada;
	}

	public void setCantidadAcumulada(BigDecimal cantidadAcumulada) {
		this.cantidadAcumulada = cantidadAcumulada;
	}

	public BigDecimal getCosto() {
		return costo;
	}

	public void setCosto(BigDecimal costo) {
		Object old=this.costo;
		this.costo = costo;
		firePropertyChange("costo", old, costo);
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder(17,35)
		.append(producto)		
		.toHashCode();
	}
	
	

	public BigDecimal getPrecioMinimo() {
		return precioMinimo;
	}

	public void setPrecioMinimo(BigDecimal precioMinimo) {
		Object old=this.precioMinimo;
		this.precioMinimo = precioMinimo;
		firePropertyChange("precioMinimo", old, precioMinimo);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if(obj==null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		SimuladorDePreciosPorClienteDet other = (SimuladorDePreciosPorClienteDet) obj;
		return new EqualsBuilder()
		.append(producto, other.getProducto())		
		.isEquals();
	}

	public String toString(){
		String pattern="{0} {1} Costo:{3} Precio:{4}  ";
		return MessageFormat.format(pattern, id,producto,precio);
	}


	public BigDecimal getPrecioNeto(){
		return MonedasUtils.aplicarDescuentosEnCascada(BigDecimal.valueOf(getPrecioDeLista())
				, getDescuento());
		
	}
	
	public BigDecimal getPrecioFactura(){
		final CantidadMonetaria precioDeLista=CantidadMonetaria.pesos(getProducto().getPrecioCredito());
		CantidadMonetaria precio=MonedasUtils.aplicarDescuentosEnCascadaBase100(precioDeLista, getDescuento());
		return precio.amount();
	}

	public CostoPromedio getCostoPromedioRef() {
		return costoPromedioRef;
	}

	public void setCostoPromedioRef(CostoPromedio costoPromedioRef) {
		this.costoPromedioRef = costoPromedioRef;
	}
	

	
	
	
}
