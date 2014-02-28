package com.luxsoft.sw3.ventas;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.MessageFormat;
import java.text.NumberFormat;
import java.util.Calendar;
import java.util.Date;

import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.Version;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.hibernate.annotations.AccessType;
import org.hibernate.validator.AssertTrue;
import org.hibernate.validator.Length;
import org.hibernate.validator.NotNull;

import com.luxsoft.siipap.cxc.model.FormaDePago;
import com.luxsoft.siipap.model.BaseBean;
import com.luxsoft.siipap.model.CantidadMonetaria;
import com.luxsoft.siipap.model.Sucursal;
import com.luxsoft.siipap.model.UserLog;
import com.luxsoft.siipap.model.core.Producto;

import com.luxsoft.siipap.util.MonedasUtils;
import com.luxsoft.siipap.ventas.model.VentaDet;

/** 
 * 
 * @author Ruben Cancino Ramos
 * 
 */
@Entity
@Table(name = "SX_PEDIDOSDET")
public class PedidoDet extends BaseBean{

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name = "PEDIDODET_ID")
	private Long id;
	
	@Version
	private int version;
	
	@ManyToOne(optional = false)
	@JoinColumn(name = "PEDIDO_ID",nullable=false,updatable=false)
	private Pedido pedido;

	@ManyToOne(optional = false,fetch=FetchType.EAGER)
	@JoinColumn(name = "PRODUCTO_ID", nullable = false)
	@NotNull
	private Producto producto;

	@Column(name = "CLAVE", nullable = false)
	@Length(max = 10)
	private String clave;

	@Column(name = "DESCRIPCION", nullable = false)
	@Length(max = 250)
	private String descripcion;
	
	@Column(name = "KILOS", scale = 3, nullable = true)
	private double kilos = 0;

	@Column(name="UNIDAD",length=3,nullable=false)
	private String unidad;
		
	

	@Column(name = "CANTIDAD", nullable = false)
	@AccessType(value = "field")
	private double cantidad = 0;
	
	@Column(name = "PAQUETE", nullable = false,columnDefinition="INTEGER default 1")
	private int paquete= 1;
	
	//@Column(name = "CANTIDAD", nullable = false)
	@Transient
	private double backOrder=0;

	@Column(name = "FACTORU", nullable = false)
	private double factor;
	
	
	@Column(name = "PRECIO_ORIG", nullable = false)
	private BigDecimal precioOriginal=BigDecimal.ZERO;
	
	@Column(name = "PRECIO_L", nullable = false)
	private BigDecimal precioLista=BigDecimal.ZERO;

	@Column(name = "PRECIO",  nullable = false)
	private BigDecimal precio=BigDecimal.ZERO;
	
	/**
	 * (cantidad/factor)*precio 
	 * 
	 */
	@Column(name = "IMP_BRUTO", nullable = false)
	private BigDecimal importeBruto = BigDecimal.ZERO;
	
	
	@Column(name = "DESCUENTO", scale = 6, precision = 8, nullable = false)
	private double descuento = 0;
	
	/**
	 *  importeBruto*(descuento/100)
	 *  
	 */
	@Column(name = "IMP_DESCUENTO", nullable = false)
	private BigDecimal importeDescuento = BigDecimal.ZERO;
	
	/**
	 * importeBruto-importeDescuento
	 * 
	 */
	@Column(name = "IMP_NETO", nullable = false)
	private BigDecimal importeNeto = BigDecimal.ZERO;

	@Column(name = "CORTES", nullable = false)
	private int cortes = 0;

	@Column(name = "CORTES_PRECIO", nullable = false)
	private BigDecimal precioCorte = BigDecimal.ZERO;

	/**
	 * cortes*precioCorte
	 * 
	 */
	@Column(name = "IMP_CORTES", nullable = false)
	private BigDecimal importeCorte = BigDecimal.ZERO;

	/**
	 * importeNeto+importeCortes
	 * 
	 */
	@Column(name = "SUBTOTAL", scale = 4, precision = 19, nullable = false)
	private BigDecimal subTotal = BigDecimal.ZERO;
		
	@Column(name = "COMENTARIO")
	private String comentario;
	
	@Column(name = "CORTE_LARGO")
	private double corteLargo;
	
	@Column(name = "CORTE_ANCHO")
	private double corteAncho;
	
	/**
	 * TODO Contemplar todo lo relacionado con cortes a una refactorizacion
	 *      en una o mas entidades
	 */
	@Column(name="CORTES_INSTRUCCION")
	@Length(max=17)
	private String instruccionesDecorte;
	
	@Transient
	private boolean cotizable=false;
	
	public boolean isCotizable() {
		return cotizable;
	}

	public void setCotizable(boolean cotizable) {
		boolean old=this.cotizable;
        this.cotizable = cotizable;
        firePropertyChange("cotizable", old, cotizable);
	}
	
	
	@Embedded
	@AttributeOverrides({
	       @AttributeOverride(name="createUser",	column=@Column(name="CREADO_USR"	,nullable=true,insertable=true,updatable=false)),
	       @AttributeOverride(name="updateUser",	column=@Column(name="MODIFICADO_USR",nullable=true,insertable=true,updatable=true)),
	       @AttributeOverride(name="creado", 		column=@Column(name="CREADO"		,nullable=true,insertable=true,updatable=false)),
	       @AttributeOverride(name="modificado", 	column=@Column(name="MODIFICADO"	,nullable=true,insertable=true,updatable=true))
	   })
	private UserLog log=new UserLog();
	
	public PedidoDet(){}
	
	public static PedidoDet getPedidoDet(){
		PedidoDet det=new PedidoDet();
		/*try {
			Thread.sleep(120);
		} catch (InterruptedException e) {}*/
		Date creado=new Date(System.currentTimeMillis()+2000);
		det.getLog().setCreado(creado);
		return det;
	}

	public static PedidoDet getPedidoDet(int hora){
		PedidoDet det=new PedidoDet();
		Calendar cal=Calendar.getInstance();
		cal.set(Calendar.HOUR_OF_DAY,hora);
		det.getLog().setCreado(cal.getTime());
		det.getLog().setModificado(det.getLog().getCreado());
		return det;
	}
	
	public Long getId() {
		return id;
	}

	public int getVersion() {
		return version;
	}

	public Producto getProducto() {
		return producto;
	}

	public void setProducto(Producto producto) {
		if(producto==null)
			throw new IllegalArgumentException("No se permite producto nulo");
		Object old=this.producto;
		this.producto = producto;	
		firePropertyChange("producto", old, producto);
		this.clave=producto.getClave();
		this.descripcion=producto.getDescripcion();
		this.unidad=producto.getUnidad().getUnidad();
		this.factor=producto.getUnidad().getFactor();
		
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

	public void setString(String unidad) {
		this.unidad = unidad;
	}

	public double getKilos() {
		return kilos;
	}

	public void setKilos(double kilos) {
		this.kilos = kilos;
	}

	public String getComentario() {
		return comentario;
	}

	public void setComentario(String comentario) {
		this.comentario = comentario;
	}	

	public double getCantidad() {
		return cantidad;
	}

	public void setCantidad(double cantidad) {
		double old=this.cantidad; 
		this.cantidad = cantidad;
		firePropertyChange("cantidad", old, cantidad);
		
	}
	
	public double getBackOrder() {
		return backOrder;
	}
	public void setBackOrder(double backOrder) {
		double old=this.backOrder;
		this.backOrder = backOrder;
		firePropertyChange("backOrder", old, backOrder);
	}

	public double getFactor() {
		return factor;
	}

	public void setFactor(double factor) {
		this.factor = factor;
	}

	public Pedido getPedido() {
		return pedido;
	}

	public void setPedido(Pedido pedido) {
		this.pedido = pedido;
	}
	
	

	public BigDecimal getPrecioOriginal() {
		return precioOriginal;
	}

	public void setPrecioOriginal(BigDecimal precioOriginal) {
		Object old=this.precioOriginal;
		this.precioOriginal = precioOriginal;
		firePropertyChange("precioOriginal", old, precioOriginal);
	}

	public BigDecimal getPrecioLista() {
		return precioLista;
	}

	public void setPrecioLista(BigDecimal precioLista) {
		Object old=this.precioLista;
		this.precioLista = precioLista;
		firePropertyChange("precioLista", old, precioLista);
	}

	public BigDecimal getPrecio() {
		return precio;
	}

	public void setPrecio(BigDecimal precio) {
		Object old=this.precio;
		this.precio = precio;
		firePropertyChange("precio", old, precio);
	}

	public double getDescuento() {
		return descuento;
	}

	public void setDescuento(double descuento) {
		this.descuento = descuento;
	}

	

	public BigDecimal getImporteBruto() {
		return importeBruto;
	}

	public void setImporteBruto(BigDecimal importeBruto) {
		Object old=this.importeBruto;
		this.importeBruto = importeBruto;
		firePropertyChange("importeBruto", old, importeBruto);
	}

	public BigDecimal getImporteDescuento() {
		return importeDescuento;
	}

	public void setImporteDescuento(BigDecimal importeDescuento) {
		Object old=this.importeDescuento;
		this.importeDescuento = importeDescuento;
		firePropertyChange("importeDescuento", old, importeDescuento);
	}

	public BigDecimal getImporteNeto() {
		return importeNeto;
	}

	public void setImporteNeto(BigDecimal neto) {
		Object old=this.importeNeto;
		this.importeNeto = neto;
		firePropertyChange("importeNeto", old, neto);
	}

	public BigDecimal getSubTotal() {
		return subTotal;
	}

	public void setSubTotal(BigDecimal subTotal) {
		Object old=this.subTotal;
		this.subTotal = subTotal;
		firePropertyChange("subTotal", old, subTotal);
	}

	public int getCortes() {
		return cortes;
	}

	public void setCortes(int cortes) {
		int old=this.cortes;
		this.cortes = cortes;
		firePropertyChange("cortes", old, cortes);
	}

	public BigDecimal getPrecioCorte() {
		return precioCorte;
	}

	public void setPrecioCorte(BigDecimal precioCorte) {
		Object old=this.precioCorte;
		this.precioCorte = precioCorte;
		firePropertyChange("precioCorte", old, precioCorte);
	}

	public BigDecimal getImporteCorte() {
		return importeCorte;
	}

	public void setImporteCorte(BigDecimal importeCorte) {
		Object old=this.importeCorte;
		this.importeCorte = importeCorte;
		firePropertyChange("importeCorte", old, importeCorte);
	}
	

	public String getInstruccionesDecorte() {
		return instruccionesDecorte;
		//return getDescripcionCorte();
	}

	public void setInstruccionesDecorte(String instruccionesDecorte) {
		Object old=this.instruccionesDecorte;
		this.instruccionesDecorte = instruccionesDecorte;
		firePropertyChange("instruccionesDecorte", old, instruccionesDecorte);
	}

	public double getCorteLargo() {
		return corteLargo;
	}

	public void setCorteLargo(double corteLargo) {
		double old=this.corteLargo;
		this.corteLargo = corteLargo;
		firePropertyChange("corteLargo", old, corteLargo);
	}

	public double getCorteAncho() {
		return corteAncho;
	}

	public void setCorteAncho(double corteAncho) {
		double old=this.corteAncho;
		this.corteAncho = corteAncho;
		firePropertyChange("corteAncho", old, corteAncho);
	}
	
	public String getDescripcionCorte(){
		if(getCorteAncho()>0 || getCorteLargo()>0 ){
			String c= getCorteAncho()+ "X"+getCorteLargo();
			c=c+" "+getInstruccionesDecorte();
			return c;
		}else if(StringUtils.isNotBlank(getInstruccionesDecorte())){
			return getInstruccionesDecorte();
		}
		return "";
	}
	
	public UserLog getLog() {
		return log;
	}

	public void setLog(UserLog log) {
		this.log = log;
	}
	
	
	/** Propiedades calculadas y transient **/
	
	public double getKilosCalculados(){
		if(this.factor>0 && !isEspecial()){
			double canti=this.cantidad/this.factor;
			double res=canti*this.getProducto().getKilos();
			return res;
		}if(this.factor>0 && isEspecial()){
			double canti=this.cantidad/this.factor;
			double kilosMillar=(getLargo()*getAncho()/10000d)*getProducto().getGramos();
			double res=canti*kilosMillar;
			res=Math.round(res);
			//System.out.println("Kilos calculados para medida especial: "+res);
			return res;
		}
		return 0;
	}
	
	public Sucursal getSucursal(){
		return getPedido()!=null?getPedido().getSucursal():null;
	}

	@Transient
	private BigDecimal precioEspecial=BigDecimal.ZERO;



	public BigDecimal getPrecioEspecial() {
		return precioEspecial;
	}

	public void setPrecioEspecial(BigDecimal precioEspecial) {
		Object old=this.precioEspecial;
		this.precioEspecial = precioEspecial;
		firePropertyChange("precioEspecial", old, precioEspecial);
	}
	
	
	
	/** Comportamiento **/
	
	public int getPaquete() {
		return paquete;
	}

	public void setPaquete(int paquete) {
		int old=this.paquete;
		this.paquete = paquete;
		firePropertyChange("paquete", old, paquete);
	}

	/**
	 * Actualiza el precio de lista tomandolo del producto
	 * Util en la aplicacion de algunas reglas de negocios
	 * relacionadas con el almacenamiento del precio de lista
	 * al momento del pedido
	 * 
	 */
	public void actualizarPrecioDeLista(){
		double pl=getPedido().isDeCredito()?getProducto().getPrecioCredito():getProducto().getPrecioContado();
		setPrecioLista(BigDecimal.valueOf(pl));
	}
	
	public void actualizarImporteBruto(){
		//Actualizar el importe bruto
		CantidadMonetaria precio=CantidadMonetaria.pesos(getPrecio().doubleValue());
		double cantidad=getCantidad()/getFactor();
		CantidadMonetaria importeBruto=precio.multiply(cantidad);
		setImporteBruto(importeBruto.amount());
		
	}
	
	
	/**
	 * Actualiza los importes de la partida en funcion del precio,cantida,descuento,cortes y precioPorCorte
	 * 
	 */
	public void actualizar(){
		
		if(getProducto()==null)
			return;
		
		//Actualizar el importe bruto
		if(isEspecial())
			actualizarPrecioEspecial();
		CantidadMonetaria precio=CantidadMonetaria.pesos(getPrecio().doubleValue());
		double cantidad=getCantidad()/getProducto().getUnidad().getFactor();
		CantidadMonetaria importeBruto=precio.multiply(cantidad);
		setImporteBruto(importeBruto.amount());
		
		//Actualizar el importe del descuento
		CantidadMonetaria importeDescuento=importeBruto.multiply(getDescuento()/100);
		setImporteDescuento(importeDescuento.amount());
		
		//Actualizar el importe neto
		CantidadMonetaria importeNeto=importeBruto.subtract(importeDescuento);
		setImporteNeto(importeNeto.amount());
		
		//Actualziar el importe de los cortes
		CantidadMonetaria importeCorte=CantidadMonetaria.pesos(getPrecioCorte());
		importeCorte=importeCorte.multiply((double)getCortes());
		setImporteCorte(importeCorte.amount());
		
		//Actualizar subTotal
		CantidadMonetaria subTotal=importeNeto.add(importeCorte);
		setSubTotal(subTotal.amount());
		actualizarKilos();
		
	}
	
	public void actualizarPrecioEspecial(){
		
		CantidadMonetaria precio=CantidadMonetaria.pesos(getProducto().getPrecioPorKiloContado());
		if(getPedido().isDeCredito())
			precio=CantidadMonetaria.pesos(getProducto().getPrecioPorKiloCredito());
		if(getPedido().getFormaDePago().equals(FormaDePago.CHEQUE_POSTFECHADO))			
			precio=CantidadMonetaria.pesos(getProducto().getPrecioPorKiloContado());
		double gramos=(double)getProducto().getGramos();
		
		double ancho=getAncho();
		double largo=getLargo();
		
		precio=precio.multiply(ancho).multiply(largo).multiply(gramos);
		precio=precio.divide(10000d);
		setPrecio(precio.amount());
		
		//setValue("precio", precio.amount());
		//getPedidoDet().actualizar();
	}
	
	
	
	protected void actualizarKilos(){
		setKilos(getKilosCalculados());
	}
	
	public VentaDet toVentaDet(){
		VentaDet vd=new VentaDet();
		vd.setProducto(getProducto());
		vd.setCantidad(getCantidad()*-1);
		vd.setComentario(getComentario());
		vd.setCortes(getCortes());
		vd.setDescuento(getDescuento());
		//vd.setDescuentoOriginal(descuentoOriginal);
		vd.setFecha(getPedido().getFecha());
		vd.setImporte(getImporteBruto());
		vd.setImporteNeto(getImporteNeto());
		vd.setKilos(getKilos());
		vd.setNacional(getProducto().isNacional());
		vd.setPrecio(getPrecio());
		vd.setPrecioCorte(getPrecioCorte());
		vd.setPrecioLista(getPrecioLista());
		vd.setSubTotal(getSubTotal());
		vd.setSucursal(getSucursal());
		vd.setCorteAncho(corteAncho);
		vd.setCorteLargo(corteLargo);
		vd.setInstruccionesDecorte(getInstruccionesDecorte());
		vd.setOrdenp(getLog().getCreado());
		vd.setAncho(getAncho());
		vd.setLargo(getLargo());
		vd.setEspecial(isEspecial());
		vd.setPrecioPorKilo(getPrecioPorKilo());
		if(isEspecial()){
			vd.setDescripcion(getDescripcion());
		}
		
		
		return vd;
	}
	
	/*** Validaciones ***/
	
	//@AssertTrue(message="La cantidad es requerida")
	public boolean validarCantidad(){
		return getCantidad()>0;
	}
	
	@AssertTrue(message="Este producto no permite cortes")
	public boolean precioPorCortes(){
		if(getCorteAncho()>0 && getCorteLargo()>0){
			return getCortes()>0;
		}
		return true;
	}
	
	@AssertTrue(message="Medidas incorrectas")
	public boolean validarLongitudes(){
		if(getCorteAncho()>0 && getCorteLargo()>0){
			return (getCorteAncho()<=getProducto().getAncho()
					&& getCorteLargo()<=getProducto().getLargo()
					);
		}
		return true;
	}
	
	@Override
	public int hashCode() {
		return new HashCodeBuilder(17,35)
		/**.append(getClave())
		.append(getCantidad())
		.append(getPaquete())
		.append(getCorteAncho())
		.append(getCorteLargo())**/
		//.append(getLog().getCreado())
		//.append(value)
		.append(getId())
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
		PedidoDet other = (PedidoDet) obj;
		return new EqualsBuilder()
		/**.append(getClave(), other.getClave())
		.append(getCantidad(), other.getCantidad())
		.append(getPaquete(), other.getPaquete())
		.append(getCorteAncho(), other.getCorteAncho())
		.append(getCorteLargo(), other.getCorteLargo())**/
		.append(getLog().getCreado(), other.getLog().getCreado())
		.isEquals();
	}

	@Override
	public String toString() {
		String pattern="{0} {1} {2}";
		return MessageFormat.format(pattern, getClave(),getDescripcion(),getCantidad());
	}
	
	@Column(name="ESPECIAL")	
	private boolean especial=false;
	
	@Column(name = "LARGO", scale = 3)
	private double largo = 0;
	
	
	@Column(name = "ANCHO", scale = 3)
	private double ancho = 0;
	
	@Column(name="PRECIO_KILO")
	private BigDecimal precioPorKilo;

	
	public boolean isEspecial() {
		return especial;
	}

	public void setEspecial(boolean especial) {
		this.especial = especial;
	}

	public double getLargo() {
		return largo;
	}

	public void setLargo(double largo) {
		double old=this.largo;
		this.largo = largo;
		firePropertyChange("largo", old, largo);
	}

	public double getAncho() {
		return ancho;
	}

	public void setAncho(double ancho) {
		double old=this.ancho;
		this.ancho = ancho;
		firePropertyChange("ancho", old, ancho);
	}

	public BigDecimal getPrecioPorKilo() {
		return precioPorKilo;
	}

	public void setPrecioPorKilo(BigDecimal precioPorKilo) {
		Object old=this.precioPorKilo;
		this.precioPorKilo = precioPorKilo;
		firePropertyChange("precioPorKilo", old, precioPorKilo);
	}

	@Column(name="ENTRADA",nullable=true)
	private String entrada;
	
	@Column(name="TIPO_E",nullable=true,length=3)
	private String tipoEntrada;

	public String getEntrada() {
		return entrada;
	}

	public void setEntrada(String entrada) {
		this.entrada = entrada;
	}

	public String getTipoEntrada() {
		return tipoEntrada;
	}

	public void setTipoEntrada(String tipoEntrada) {
		this.tipoEntrada = tipoEntrada;
	}
	

	public void actualizarDescripcion(){
		NumberFormat nf=new DecimalFormat("#.#");
		String desc=MessageFormat.format("{0} {1}X{2}"
				, getDescripcion()
				,nf.format(getAncho())
				,nf.format(getLargo())
				);
		setDescripcion(desc);
	}
	
	

	
}
