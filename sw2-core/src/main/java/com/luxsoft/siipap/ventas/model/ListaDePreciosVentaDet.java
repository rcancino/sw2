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
public class ListaDePreciosVentaDet extends BaseBean{
	
	@Id @GeneratedValue(strategy=GenerationType.AUTO)
	@Column(name="LISTADET_ID")
	private Long id;
	
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
	
	@Column(name="PRECIO_ANTERIOR_CRE",nullable=false)
	@NotNull
	private BigDecimal precioAnteriorCredito=BigDecimal.ZERO;
	
	@Column(name="COSTO",nullable=false)
	@NotNull
	private BigDecimal costo=BigDecimal.ZERO;
	
	@Column(name="COSTOU", nullable=false)
	@NotNull
	private BigDecimal costoUltimo=BigDecimal.ZERO;
	
	@Column(name="PRECIO",nullable=false)
	@NotNull
	private BigDecimal precio=BigDecimal.ZERO;
	
	@Column(name="PRECIO_CREDITO",nullable=false)
	@NotNull
	private BigDecimal precioCredito=BigDecimal.ZERO;
	
	@Column(name="INCREMENTO",nullable=false)
	@NotNull
	private double incremento=0;
	
	@Column(name="FACTOR",nullable=false)
	@NotNull
	private double factor=0;
	
	
	@Column(name="FACTOR_CREDITO",nullable=false)
	@NotNull
	private double factorCredito=0;
	
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
		String pattern="{0} {3} {1} {2} {4}";
		setDescripcion(MessageFormat.format(pattern
				,producto.getAncho()!=0?producto.getAncho():""
				,producto.getLargo()!=0?producto.getLargo():""
				,producto.getCalibre()!=0?producto.getCalibre():""
				,producto.getAncho()!=0?"X":""	
				,producto.getCalibre()!=0?"Pts":""
				));
		
		setKilos(producto.getKilos());
		setGramos(producto.getGramos());		
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
	
	public BigDecimal getPrecioAnteriorCredito() {
		return precioAnteriorCredito;
	}

	public void setPrecioAnteriorCredito(BigDecimal precioAnteriorCredito) {
		this.precioAnteriorCredito = precioAnteriorCredito;
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
	
	public BigDecimal getPrecioCredito() {
		return precioCredito;
	}

	public void setPrecioCredito(BigDecimal precioCredito) {
		Object old=this.precioCredito;
		this.precioCredito = precioCredito;
		firePropertyChange("precioCredito", old, precioCredito);
		if(precioCredito!=null){
			if(precioCredito.doubleValue()>0){
				double costo=getCosto().doubleValue();
				if(costo>0){
					double fac=precioCredito.doubleValue()/costo;
					setFactorCredito(fac);
				}
			}
		}
		
	}
	
	public double getIncrementoCalculado(){
		return precioAnterior.doubleValue()==0
				?0:((precio.doubleValue()/precioAnterior.doubleValue())*100)-100;
	}
	
	public double getIncrementoCalculadoCredito(){
		return precioAnteriorCredito.doubleValue()==0
				?0:((precioCredito.doubleValue()/precioAnteriorCredito.doubleValue())*100)-100;
	}
	
	public void aplicarIncrementoSobrePrecioAnterior(double valor,boolean contado){
		if(contado){
			double pa=precioAnterior.doubleValue();
			double rval=pa*valor;
			BigDecimal nuevo=BigDecimal.valueOf(Math.round(rval));
			setPrecio(nuevo);
		}else{
			double pa=precioAnteriorCredito.doubleValue();
			double rval=pa*valor;
			BigDecimal nuevo=BigDecimal.valueOf(Math.round(rval));
			setPrecioCredito(nuevo);
		}
		
	}
	
	public void aplicarDecrementoSobrePrecioAnterior(double valor,boolean contado){
		if(valor==0) return;
		if(contado){
			double pa=precioAnterior.doubleValue();
			double rval=pa/valor;
			BigDecimal nuevo=BigDecimal.valueOf(Math.round(rval));
			setPrecio(nuevo);
		}else{
			double pa=precioAnteriorCredito.doubleValue();
			double rval=pa/valor;
			BigDecimal nuevo=BigDecimal.valueOf(Math.round(rval));
			setPrecioCredito(nuevo);
		}
		
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
	
	public double getFactorCredito() {
		return factorCredito;
	}

	public void setFactorCredito(double factorCredito) {
		double old=this.factorCredito;
		this.factorCredito = factorCredito;
		firePropertyChange("factorCredito", old, factorCredito);
	}

	
	public void aplicarFactor(double factor,boolean contado){
		BigDecimal costoNuevo=BigDecimal.valueOf(this.costo.doubleValue()*factor);
		if(contado){
			setPrecio(costoNuevo);
			setFactor(factor);
		}else{
			setPrecioCredito(costoNuevo);
			setFactorCredito(factor);
		}
	}
	
	public void aplicarFactorSobreCostoUltimo(double factor,boolean contado){
		BigDecimal costoNuevo=BigDecimal.valueOf(getCostoUltimo().doubleValue()*factor);
		if(contado){
			setPrecio(costoNuevo);
			setFactor(factor);
		}else{
			setPrecioCredito(costoNuevo);
			setFactorCredito(factor);
		}
	}
	
	public void actualizarFactor(boolean contado){
		if(costo.doubleValue()>0){
			if(contado){
				double val=precio.doubleValue()/costo.doubleValue();
				setFactor(val);
			}else{
				double val=precioCredito.doubleValue()/costo.doubleValue();
				setFactorCredito(val);
			}
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

	
	public BigDecimal getCostoUltimo() {
		if(costoUltimo==null)
			costoUltimo=BigDecimal.ZERO;
		return costoUltimo;
	}

	public void setCostoUltimo(BigDecimal costoUltimo) {
		this.costoUltimo = costoUltimo;
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
		ListaDePreciosVentaDet other = (ListaDePreciosVentaDet) obj;
		return new EqualsBuilder()
		.append(clave, other.getClave())		
		.isEquals();
	}

	public String toString(){
		String pattern="{0} {1}({2} Costo:{3} Precio:{4}  )";
		return MessageFormat.format(pattern, id,descripcion,clave,costo,precio);
	}
	
	@Column(name="PAGINA",nullable=false)
	//@NotNull
	private int pagina;
	
	/*
	@Column(name="COMENTARIO_PAGINA_H")
	@Length(max=255)
	private String comentarioPaginaH;
	
	@Column(name="COMENTARIO_PAGINA_F")
	@Length(max=255)
	private String comentarioPaginaF;
	*/
	
	@Column(name="COLUMNA",nullable=false)
	private int columna;
	/*
	@Column(name="COMENTARIO_COLUMNA_H")
	@Length(max=255)
	private String comentarioColumnaH;
	
	@Column(name="COMENTARIO_COLUMNA_F")
	@Length(max=255)
	private String comentarioColumnaF;
	*/
	@Column(name="GRUPO",nullable=false)
	private int grupo;
	/*
	@Column(name="COMENTARIO_GRUPO_H")
	@Length(max=255)
	private String comentarioGrupoH;
	
	@Column(name="COMENTARIO_GRUPO_F")
	@Length(max=255)
	private String comentarioGrupoF;
	*/
	@Column(name="GRAMOS",nullable=false)	
	private double gramos;
	
	@Column(name="KILOS",nullable=false)	
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

	/*public String getComentarioPaginaH() {
		return comentarioPaginaH;
	}

	public void setComentarioPaginaH(String comentarioPaginaH) {
		this.comentarioPaginaH = comentarioPaginaH;
	}

	public String getComentarioPaginaF() {
		return comentarioPaginaF;
	}

	public void setComentarioPaginaF(String comentarioPaginaF) {
		this.comentarioPaginaF = comentarioPaginaF;
	}

	public String getComentarioColumnaH() {
		return comentarioColumnaH;
	}

	public void setComentarioColumnaH(String comentarioColumnaH) {
		this.comentarioColumnaH = comentarioColumnaH;
	}

	public String getComentarioColumnaF() {
		return comentarioColumnaF;
	}

	public void setComentarioColumnaF(String comentarioColumnaF) {
		this.comentarioColumnaF = comentarioColumnaF;
	}

	public String getComentarioGrupoH() {
		return comentarioGrupoH;
	}

	public void setComentarioGrupoH(String comentarioGrupoH) {
		this.comentarioGrupoH = comentarioGrupoH;
	}

	public String getComentarioGrupoF() {
		return comentarioGrupoF;
	}

	public void setComentarioGrupoF(String comentarioGrupoF) {
		this.comentarioGrupoF = comentarioGrupoF;
	}	*/
	
	
	
}
