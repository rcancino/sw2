package com.luxsoft.siipap.ventas.model;

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

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.hibernate.validator.Length;
import org.hibernate.validator.NotNull;

import com.luxsoft.siipap.model.BaseBean;
import com.luxsoft.siipap.model.core.Producto;
import com.luxsoft.siipap.util.MonedasUtils;

@Entity
@Table(name="SX_LP_VENT_DET")
public class ListaDePreciosVentaDet_bak extends BaseBean{
	
	@Id @GeneratedValue(strategy=GenerationType.AUTO)
	@Column(name="LISTADET_ID")
	private Long id;
	
	@Column(name="TIPO",nullable=false,length=10)
	private String tipo="CRE";
	
	@ManyToOne(optional=false)
	@JoinColumn (name="LISTA_ID",nullable=false,updatable=false)	
	private ListaDePreciosVenta lista;
	
	@ManyToOne(optional=false,fetch=FetchType.EAGER)
	@JoinColumn(name="PRODUCTO_ID",nullable=false)
	@NotNull
	private Producto producto;
	
	@Column(name="CLAVE",nullable=false)
	private String clave;
	
	@Column(name="DESCRIPCION",nullable=false)
	private String descripcion;
	
	@Column(name="MONEDA",nullable=false)
	@NotNull
	private Currency moneda=MonedasUtils.PESOS;
	
	@Column(name="PRECIO_ANTERIOR",nullable=false)
	@NotNull
	private BigDecimal precioAnterior=BigDecimal.ZERO;
	
	@Column(name="COSTO",nullable=false)
	@NotNull
	private BigDecimal costo=BigDecimal.ZERO;
	
	@Column(name="COSTOU", nullable=false)
	@NotNull
	private BigDecimal costoUltimo=BigDecimal.ZERO;
	
	@Column(name="PRECIO",nullable=false)
	@NotNull
	private BigDecimal precio=BigDecimal.ZERO;
	
	@Column(name="INCREMENTO",nullable=false)
	@NotNull
	private double incremento=0;
	
	@Column(name="FACTOR",nullable=false)
	@NotNull
	private double factor=0;
	
	@Column(name="comentario")
	@Length(max=255)
	private String comentario;
	
	@Column(name="PROV_CLAVE",nullable=true,length=4)
	private String proveedorClave="";
	
	@Column (name="PROV_NOMBRE",nullable=true)
	@Length (max=250)
	private String proveedorNombre="";
	
	public Long getId() {
		return id;
	}	

	public ListaDePreciosVenta getLista() {
		return lista;
	}

	public void setLista(ListaDePreciosVenta lista) {
		this.lista = lista;
	}

	public Producto getProducto() {
		return producto;
	}

	public void setProducto(Producto producto) {
		this.producto = producto;
		this.clave=producto.getClave();
		this.descripcion=producto.getDescripcion();
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

	public BigDecimal getPrecioAnterior() {
		return precioAnterior;
	}

	public void setPrecioAnterior(BigDecimal precioAnterior) {
		this.precioAnterior = precioAnterior;
	}
	
	public BigDecimal getCosto() {
		return costo;
	}

	public void setCosto(BigDecimal costo) {
		this.costo = costo;
	}

	public BigDecimal getPrecio() {
		return precio;
	}

	public void setPrecio(BigDecimal precio) {
		Object old=this.precio;
		this.precio = precio;
		firePropertyChange("precio", old, precio);
		if(precio!=null){
			if(precio.doubleValue()>0){
				double costo=getCosto().doubleValue();
				if(costo>0){
					double fac=precio.doubleValue()/costo;
					setFactor(fac);
				}
				
			}
		}
		
	}
	
	public double getIncrementoCalculado(){
		return precioAnterior.doubleValue()==0
				?0:((precio.doubleValue()/precioAnterior.doubleValue())*100)-100;
	}
	
	public void aplicarIncrementoSobrePrecioAnterior(double valor){
		double pa=precioAnterior.doubleValue();
		double rval=pa*valor;
		BigDecimal nuevo=BigDecimal.valueOf(Math.round(rval));
		setPrecio(nuevo);
	}
	
	public void aplicarDecrementoSobrePrecioAnterior(double valor){
		if(valor==0) return;		
		double pa=precioAnterior.doubleValue();
		double rval=pa/valor;
		BigDecimal nuevo=BigDecimal.valueOf(Math.round(rval));
		setPrecio(nuevo);
	}

	public double getIncremento() {
		return incremento;
	}

	public void setIncremento(double incremento) {
		this.incremento = incremento;
	}

	public double getFactor() {
		return factor;
	}

	public void setFactor(double factorDeUtilidad) {
		double old=this.factor;
		this.factor = factorDeUtilidad;
		firePropertyChange("factor", old, factor);
		
	}
	
	public void aplicarFactor(double factor,boolean aCosto){
		if(aCosto){
			double pre=this.costo.doubleValue()*factor;
			setPrecio(BigDecimal.valueOf(Math.round(pre)));
			setFactor(factor);
		}else {
			double pre=this.costoUltimo.doubleValue()*factor;
			setPrecio(BigDecimal.valueOf(Math.round(pre)));
			setFactor(factor);
		}
		
	}
	
	public void actualizarFactor(){
		if(costo.doubleValue()>0){
			double val=precio.doubleValue()/costo.doubleValue();
			setFactor(val);
		}
		
	}

	public String getComentario() {
		return comentario;
	}

	public void setComentario(String comentario) {
		this.comentario = comentario;
	}

	public String getProveedorClave() {
		return proveedorClave;
	}

	public void setProveedorClave(String proveedorClave) {
		this.proveedorClave = proveedorClave;
	}

	public String getProveedorNombre() {
		return proveedorNombre;
	}

	public void setProveedorNombre(String proveedorNombre) {
		this.proveedorNombre = proveedorNombre;
	}

	public String getTipo() {
		return tipo;
	}

	public void setTipo(String tipo) {
		this.tipo = tipo;
	}
	
	public BigDecimal getCostoUltimo() {
		return costoUltimo;
	}

	public void setCostoUltimo(BigDecimal costoUltimo) {
		this.costoUltimo = costoUltimo;
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder(17,35)
		.append(clave)
		.append(tipo)
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
		ListaDePreciosVentaDet_bak other = (ListaDePreciosVentaDet_bak) obj;
		return new EqualsBuilder()
		.append(clave, other.getClave())
		.append(tipo, other.getTipo())
		.isEquals();
	}

	public String toString(){
		String pattern="{0} {1}({2} Costo:{3} Precio:{4}  )";
		return MessageFormat.format(pattern, id,descripcion,clave,costo,precio);
	}
	
	@Column(name="PAGINA",nullable=false)
	//@NotNull
	private int pagina;
	
	@Column(name="COLUMNA",nullable=false)
	//@NotNull
	private int columna;
	
	@Column(name="GRUPO",nullable=false)
	//@NotNull
	private int grupo;
	
	@Column(name="GRAMOS",nullable=false)
	//@NotNull
	private double gramos;
	
	@Column(name="KILOS",nullable=false)
	//@NotNull
	private double kilos;
	
	@Column(name="PRESENTACION",nullable=false)
	@NotNull
	private String presentacion;
	
	public int getPagina() {
		return pagina;
	}

	public void setPagina(int pagina) {
		this.pagina = pagina;
	}

	public int getColumna() {
		return columna;
	}

	public void setColumna(int columna) {
		this.columna = columna;
	}

	public int getGrupo() {
		return grupo;
	}

	public void setGrupo(int grupo) {
		this.grupo = grupo;
	}

	public double getGramos() {
		return gramos;
	}

	public void setGramos(double gramos) {
		this.gramos = gramos;
	}

	public double getKilos() {
		return kilos;
	}

	public void setKilos(double kilos) {
		this.kilos = kilos;
	}

	public String getPresentacion() {
		return presentacion;
	}

	public void setPresentacion(String presentacion) {
		this.presentacion = presentacion;
	}	
	
	
}
