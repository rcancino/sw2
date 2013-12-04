package com.luxsoft.siipap.compras.model;

import java.math.BigDecimal;
import java.text.MessageFormat;

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
import org.hibernate.annotations.Columns;
import org.hibernate.annotations.Type;
import org.hibernate.validator.AssertTrue;
import org.hibernate.validator.NotNull;

import com.luxsoft.siipap.model.BaseBean;
import com.luxsoft.siipap.model.CantidadMonetaria;
import com.luxsoft.siipap.model.core.Producto;
import com.luxsoft.siipap.util.MonedasUtils;

/**
 * Precios unitarios de productos 
 * 
 * @author Ruben Cancino Ramos
 *
 */
@Entity
@Table(name="SX_LP_PROVS_DET")
public class ListaDePreciosDet extends BaseBean{
	
	@TableGenerator(
            name="idsGen", 
            table="SX_TABLE_IDS", 
            pkColumnName="GEN_KEY", 
            valueColumnName="GEN_VALUE", 
            pkColumnValue="LP_PROVEEDORES_DET_ID", 
            initialValue=2000,
            allocationSize=1)
    @Id
    @GeneratedValue(strategy=GenerationType.TABLE, generator="idsGen")
	@Column(name="LISTADET_ID")
	private Long id;
	
	@ManyToOne(optional=false)
	@JoinColumn(name="LISTA_ID",nullable=false,updatable=false)
	private ListaDePrecios lista;
	
	@ManyToOne(optional=false,fetch=FetchType.EAGER)
	@JoinColumn(name="PROD_ID",nullable=false)
	@NotNull(message="Debe seleccionar el producto")
	private Producto producto;
	
	@Column(name="CLAVE",nullable=false)
	private String clave;
	
	@Column(name="DESCRIPCION",nullable=false)
	private String descripcion;
	
	@Column(name="UNIDAD",nullable=false,length=3)
	private String unidad;
	
	@Type(type="com.luxsoft.siipap.support.hibernate.CantidadMonetariaCompositeUserType")
	@Columns(columns={
			 @Column(name="PRECIO"	   ,scale=2,nullable=false)
			,@Column(name="PRECIO_MON" ,length=3,nullable=false)
		
	})
	@NotNull
	private CantidadMonetaria precio=CantidadMonetaria.pesos(0);
	
	private BigDecimal neto=BigDecimal.ZERO;
	
	@Column(name="DESC1",nullable=false,scale=6,precision=4)
	@NotNull 
	private double descuento1=0;
	
	@Column(name="DESC2",nullable=false,scale=6,precision=4)
	@NotNull
	private double descuento2=0;
	
	@Column(name="DESC3",nullable=false,scale=6,precision=4)
	@NotNull
	private double descuento3=0;
	
	@Column(name="DESC4",nullable=false,scale=6,precision=4)
	@NotNull
	private double descuento4=0;
	
	@Column(name="DESC5",nullable=false,scale=6,precision=4)
	@NotNull
	private double descuento5=0;
	
	@Column(name="DESC6",nullable=false,scale=6,precision=4)
	@NotNull
	private double descuento6=0;
	
	@Column(name="DESC_F",nullable=false,scale=6,precision=4)
	@NotNull
	private double descuentoFinanciero=0;
	
	@Column(name="CARGO1",nullable=false,scale=6,precision=4)
	@NotNull
	private Double cargo1=0d;
	
	@Column(name="CARGO2",nullable=false,scale=6,precision=4)
	@NotNull
	private Double cargo2=0d;
	
	public Long getId() {
		return id;
	}

	public ListaDePrecios getLista() {
		return lista;
	}

	public void setLista(ListaDePrecios lista) {
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
			this.unidad=producto.getUnidad().getUnidad();
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

	public CantidadMonetaria getPrecio() {
		return precio;
	}

	public void setPrecio(CantidadMonetaria precio) {
		Object old=this.precio;
		this.precio = precio;
		firePropertyChange("precio", old, precio);
	}

	public double getDescuento1() {
		return descuento1;
	}

	public void setDescuento1(double descuento1) {
		this.descuento1 = descuento1;
	}

	public double getDescuento2() {
		return descuento2;
	}

	public void setDescuento2(double descuento2) {
		this.descuento2 = descuento2;
	}

	public double getDescuento3() {
		return descuento3;
	}

	public void setDescuento3(double descuento3) {
		this.descuento3 = descuento3;
	}

	public double getDescuento4() {
		return descuento4;
	}

	public void setDescuento4(double descuento4) {
		this.descuento4 = descuento4;
	}
	
	public double getDescuento5() {
		return descuento5;
	}

	public void setDescuento5(double descuento5) {
		this.descuento5 = descuento5;
	}

	public double getDescuento6() {
		return descuento6;
	}

	public void setDescuento6(double descuento6) {
		this.descuento6 = descuento6;
	}

	public Double getCargo1() {
		return cargo1;
	}

	public void setCargo1(Double cargo1) {
		Object old=this.cargo1;
		this.cargo1 = cargo1;
		firePropertyChange("cargo1", old, cargo1);
	}

	public Double getCargo2() {
		return cargo2;
	}

	public void setCargo2(Double cargo2) {
		this.cargo2 = cargo2;
	}
	

	public double getDescuentoFinanciero() {
		return descuentoFinanciero;
	}

	public void setDescuentoFinanciero(double descuentoFinanciero) {
		this.descuentoFinanciero = descuentoFinanciero;
	}
	
	
	
	public BigDecimal getNeto() {
		return neto;
	}

	public void setNeto(BigDecimal neto) {
		this.neto = neto;
	}

	/**
	 * Precio - Descuentos  
	 * 
	 * @return
	 */
	public CantidadMonetaria getCosto() {		 
		return MonedasUtils
			.aplicarDescuentosEnCascadaBase100(precio
					,descuento1
					,descuento2
					,descuento3
					,descuento4
					,descuento5
					,descuento6
					); 
	}
	
	/**
	 * Costo + cargos 
	 * 
	 * @return
	 */
	public CantidadMonetaria getCostoConCargo() {
		CantidadMonetaria costo=getCosto();
		return MonedasUtils
			.aplicarCargosEnCascadaBase100(costo
					,cargo1
					,cargo2); 
	}
	
	
	
	/**
	 * Precio - Descuentos + Cargos - Descuento financiero
	 * 
	 * @return
	 */
	public CantidadMonetaria getCostoUltimo() {		 
		return MonedasUtils
			.aplicarDescuentosEnCascadaBase100(getCosto(),
					descuentoFinanciero
					); 
	}

	@AssertTrue (message="Descuento (s) incorrectos")
	public boolean validarDescuentos(){
		boolean res=true;
		if(descuento1>=99.99){
			res=false;
		}
		if(descuento2>=99.99){
			res=false;
		}
		if(descuento3>=99.99){
			res=false;
		}
		if(descuento4>=99.99){
			res=false;
		}
		if(descuento5>=99.99){
			res=false;
		}
		if(descuento6>=99.99){
			res=false;
		}
		return res;
	}
	
	/** toString equals hashCode***/
	
	public String toString(){
		String pattern="{0} {1} {2}";
		return MessageFormat.format(pattern, producto,precio,getCosto());
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder(17,35)
		.append(producto)
		.append(precio.currency())
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
		ListaDePreciosDet other = (ListaDePreciosDet) obj;
		return new EqualsBuilder()
		.append(producto, other.getProducto())
		.append(precio.currency(), other.getPrecio().currency())
		.isEquals();
	}

	

}
