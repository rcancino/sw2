package com.luxsoft.siipap.ventas.model;

import java.math.BigDecimal;
import java.math.RoundingMode;
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
import javax.persistence.TableGenerator;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.hibernate.validator.NotNull;

import com.luxsoft.siipap.model.BaseBean;
import com.luxsoft.siipap.model.CantidadMonetaria;
import com.luxsoft.siipap.model.core.Producto;
import com.luxsoft.siipap.util.MonedasUtils;

@Entity
@Table(name="SX_LP_CLIENTE_DET")
public class ListaDePreciosClienteDet extends BaseBean{
	
	@TableGenerator(
            name="idsGen", 
            table="SX_TABLE_IDS", 
            pkColumnName="GEN_KEY", 
            valueColumnName="GEN_VALUE", 
            pkColumnValue="LP_CLIENTES_DET_ID", 
            initialValue=2000,
            allocationSize=1)
    @Id
    @GeneratedValue(strategy=GenerationType.TABLE, generator="idsGen")
	@Column(name="LISTADET_ID")
	private Long id;
	
	
	@ManyToOne(optional=false,fetch=FetchType.EAGER)
	@JoinColumn(name="LISTA_ID",nullable=false,updatable=false)
	private ListaDePreciosCliente lista;
	
	@ManyToOne(optional=false,fetch=FetchType.EAGER)
	@JoinColumn(name="PRODUCTO_ID",nullable=false)
	@NotNull
	private Producto producto;
	
	@Column(name="CLAVE",nullable=false,length=10)
	private String clave;
	
	@Column(name="DESCRIPCION",nullable=false, length=250)
	private String descripcion;
	
	
	@Column(name="MONEDA",nullable=false)
	@NotNull
	private Currency moneda=MonedasUtils.PESOS;
	
	@Column(name="PRECIO_LISTA",nullable=false)
	@NotNull
	private double precioDeLista=0;
	
	@Column(name="DESCUENTO",nullable=false)
	@NotNull
	private double descuento=0;
	
	
	@Column(name="PRECIOU",nullable=false)
	@NotNull
	private double precio=0;
	
	@Column(name="COSTO",scale=6,precision=14,nullable=false)
    private BigDecimal costo=BigDecimal.ZERO;
    
    @Column(name="COSTOP",scale=6,precision=14,nullable=false)
    private BigDecimal costoPromedio=BigDecimal.ZERO;
    
    @Column(name="COSTOU",scale=6,precision=14,nullable=false)
    private BigDecimal costoUltimo=BigDecimal.ZERO;
	
	@Column(name="PRECIOK",nullable=false)
	@NotNull
	private double precioKilo=0;
	
	public Long getId() {
		return id;
	}	

	public ListaDePreciosCliente getLista() {
		return lista;
	}

	public void setLista(ListaDePreciosCliente lista) {
		this.lista = lista;
	}

	public Producto getProducto() {
		return producto;
	}

	public void setProducto(Producto producto) {
		Object old=this.producto;
		this.producto = producto;
		firePropertyChange("producto", old, producto);
		if(producto!=null){
			this.clave=producto.getClave();
			this.descripcion=producto.getDescripcion();
		}else{
			this.clave=null;
			this.descripcion=null;
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
	

	public double getIncrementoCalculado(){
		return precioDeLista==0
				?0:((precio/precioDeLista)*100)-100;
	}
	
	public void aplicarDescuentoSobrePrecioDeLista(){
		BigDecimal res=BigDecimal.valueOf(getPrecio());
		if(getDescuento()!=0){
			res=MonedasUtils
			.aplicarDescuentosEnCascada(BigDecimal.valueOf(getPrecioDeLista()), getDescuento());
			setPrecio(res.doubleValue());
		}else if(getPrecioKilo()>0){
			setPrecio(getPrecioKilo()*getProducto().getKilos());
		}
		firePropertyChange("margen", -1d, getMargen());
		firePropertyChange("diferencia", -1d, getDiferencia());
	}
	
	public void aplicarIncrementoSobrePrecioAnterior(double valor){
		double pa=precioDeLista;
		double rval=pa*valor;
		BigDecimal nuevo=BigDecimal.valueOf(Math.round(rval)).setScale(4, RoundingMode.HALF_EVEN);
		setPrecio(nuevo.doubleValue());
	}
	
	public void aplicarDecrementoSobrePrecioAnterior(double valor){
		if(valor==0) return;		
		double pa=precioDeLista;
		double rval=pa/valor;
		BigDecimal nuevo=BigDecimal.valueOf(Math.round(rval)).setScale(4, RoundingMode.HALF_EVEN);
		setPrecio(nuevo.doubleValue());
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
	
	public double getMargen(){
		if(getCosto()==null)
			return 0.0;
		if(getCosto().doubleValue()==0.0)
			return 0.0;
		return (getDiferencia()/getPrecioNeto().doubleValue())*100;
	}
	
	public double getDiferencia(){
		return getPrecioNeto().doubleValue()-getCosto().doubleValue();
	}
	
	public double getPrecioPorKiloNormal(){
		if(getProducto()!=null && (getProducto().getKilos()!=0))
			return getPrecioDeLista()/getProducto().getKilos();
		else
			return 0.0;
	}
	

	@Override
	public int hashCode() {
		return new HashCodeBuilder(17,35)
		.append(clave)		
		.toHashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if(obj==null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ListaDePreciosClienteDet other = (ListaDePreciosClienteDet) obj;
		return new EqualsBuilder()
		.append(clave, other.getClave())		
		.isEquals();
	}

	public String toString(){
		String pattern="{0} {1}({2} Costo:{3} Precio:{4}  )";
		return MessageFormat.format(pattern, id,descripcion,clave,precio);
	}


	public BigDecimal getPrecioNeto(){
		return MonedasUtils.aplicarDescuentosEnCascada(getPrecioFactura(), getLista().getCliente().getCredito().getDescuentoEstimado());
		/*
		if(getLista()!=null){
			if(getLista().getCliente()!=null){
				if(getLista().getCliente().getCredito()!=null){
					double descFijo=getLista().getCliente().getCredito().getDescuentoEstimado();
					double descuento=(getPrecio()*descFijo)/100;
					return getPrecio()-descuento;
				}
			}
		}
		return getPrecio();
		*/
	}
	
	public BigDecimal getPrecioFactura(){
		final CantidadMonetaria precioDeLista=CantidadMonetaria.pesos(getProducto().getPrecioCredito());
		CantidadMonetaria precio=MonedasUtils.aplicarDescuentosEnCascadaBase100(precioDeLista, getDescuento());
		return precio.amount();
	}
	
	
	
	
}
