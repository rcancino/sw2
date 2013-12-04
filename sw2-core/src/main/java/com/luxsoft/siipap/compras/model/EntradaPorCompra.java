package com.luxsoft.siipap.compras.model;


import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.MessageFormat;
import java.text.NumberFormat;
import java.util.Date;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.hibernate.annotations.Formula;
import org.hibernate.annotations.Type;
import org.hibernate.validator.NotNull;

import com.luxsoft.siipap.inventarios.model.CostoHojeable;
import com.luxsoft.siipap.inventarios.model.Inventario;
import com.luxsoft.siipap.maquila.model.MovimientoConFlete;
import com.luxsoft.siipap.model.CantidadMonetaria;
import com.luxsoft.siipap.model.core.Proveedor;
import com.luxsoft.sw3.maquila.model.AnalisisDeFlete;
import com.luxsoft.sw3.maquila.model.AnalisisDeHojeo;


/**
 * Entrada unitaria de material al inventario por concepto de compra
 * 
 * La relacion entre {@link CompraDet} y {@link EntradaPorCompra} es One-To-Many
 * Solo hay q' conciderar que en la coleccion CompraDet.entradas las reglas de equals y hashCode
 * son las que se eredan de {@link Inventario}
 * 
 * @author Ruben Cancino
 *
 */
@Entity
@Table(name="SX_INVENTARIO_COM")
public class EntradaPorCompra extends Inventario implements MovimientoConFlete,CostoHojeable{
	
	
	@Formula("(select IFNULL(sum(a.CANTIDAD),0) FROM SX_ANALISISDET a where a.entrada_id=INVENTARIO_ID)")
	private Double analizado;
	
	
	
	@ManyToOne (optional=false
			,cascade={CascadeType.PERSIST,CascadeType.MERGE}
			,fetch=FetchType.EAGER)
	@JoinColumn (name="PROVEEDOR_ID", nullable=false, updatable=false)
	@NotNull
	private Proveedor proveedor;
	
	
	
	@ManyToOne(optional=true,fetch=FetchType.LAZY)
	@JoinColumn (name="RECEPCION_ID",nullable=true)		
	private RecepcionDeCompra recepcion;
	
	@ManyToOne(optional=true,cascade={CascadeType.MERGE,CascadeType.PERSIST})
	@JoinColumn (name="COMPRADET_ID",nullable=true)	
	private CompraUnitaria compraDet;
	
	@Column(name="COMS2_ID",unique=true)
	private Long coms2Id;
	
	@Transient
	private Date insertTime;
	
	
	
	public EntradaPorCompra(){
	}
	
	public Proveedor getProveedor() {
		return proveedor;
	}
	public void setProveedor(Proveedor proveedor) {
		this.proveedor = proveedor;
	}


	public Long getComs2Id() {
		return coms2Id;
	}
	
	public void setComs2Id(Long coms2Id) {
		this.coms2Id = coms2Id;
	}

	public CompraUnitaria getCompraDet() {
		return compraDet;
	}

	public void setCompraDet(CompraUnitaria compraDet) {
		this.compraDet = compraDet;
	}
	
	public double getSolicitado(){
		if(getCompraDet()==null)
			return 0d;
		else
			return getCompraDet().getSolicitado();
	}	
	
	public double getAnalizado(){
		if(analizado==null)
			analizado=0d;
    	return analizado;
    }
	
	public double getPendienteDeAnalisis(){
		return getCantidad()-getAnalizado();
	}
	
		

	@Override
	public String getTipoDocto() {
		return "COM";
	}
	
	/** Ajuste de impedancia entre SiipapWin y SW2 ***/
	
	@Column(name="REMISION_F")
	@Type(type="date")
	private Date fechaRemision;
	
	@Column(name="REMISION",length=20)
	public String  remision;
	
	@Column(name="COMPRA")
	private int compra;
	
	@Column(name="COMPRA_F")
	@Type(type="date")
	private Date fechaCompra;
	
	@Column(name="SUCCOM")
	private Integer sucursalCompra;

	public Date getFechaRemision() {
		return fechaRemision;
	}

	public void setFechaRemision(Date fechaRemision) {
		this.fechaRemision = fechaRemision;
	}

	public String getRemision() {
		return remision;
	}

	public void setRemision(String remision) {
		this.remision = remision;
	}

	public int getCompra() {
		return compra;
	}

	public void setCompra(int compra) {
		this.compra = compra;
	}

	public Date getFechaCompra() {
		return fechaCompra;
	}

	public void setFechaCompra(Date fechaCompra) {
		this.fechaCompra = fechaCompra;
	}

	public Integer getSucursalCompra() {
		return sucursalCompra;
	}

	public void setSucursalCompra(Integer sucursalCompra) {
		this.sucursalCompra = sucursalCompra;
	}

	public double getKilosEntrada(){
		return getProducto().getKilos()*(getCantidad()/getFactor());
	}
	
	public double getKilosAnalizados(){
		return getProducto().getKilos()*(getAnalizado()/getFactor());
	}

	public RecepcionDeCompra getRecepcion() {
		return recepcion;
	}

	public void setRecepcion(RecepcionDeCompra recepcion) {
		this.recepcion = recepcion;
	}
	
	public double getPendiente(){
		if(getCompraDet()!=null)
			return getSolicitado()-getCompraDet().getRecibido()-getCantidad();
		return 0;
	}
	
	public void setCantidad(double cantidad) {
		super.setCantidad(cantidad);
		updateInsertTime();
		
	}

	public Date getInsertTime() {
		return insertTime;
	}	

	private void updateInsertTime(){
		if(insertTime==null){
			try {
				Thread.sleep(65);
				insertTime=new Date(System.currentTimeMillis());
			} catch (Exception e) {}
		}
	}
	
	

	@Column(name="COSTO_FLETE",nullable=false,scale=6,precision=16)
	private BigDecimal costoFlete=BigDecimal.ZERO;
	
	
	@Column(name="COSTO_GASTO",nullable=false,scale=6,precision=16)
	private BigDecimal costoGasto=BigDecimal.ZERO;
	
	@ManyToOne(optional=true) 
    @JoinColumn(name="ANALISIS_FLETE_ID")
	private AnalisisDeFlete analisisFlete;
	
	public AnalisisDeFlete getAnalisisFlete() {
		return analisisFlete;
	}

	
	public void setAnalisisFlete(AnalisisDeFlete analisisFlete) {
		this.analisisFlete = analisisFlete;
	}
	
	
	@ManyToOne(optional=true) 
    @JoinColumn(name="ANALISIS_GASTO_ID")
	private AnalisisDeHojeo analisisGasto;
	
	public AnalisisDeHojeo getAnalisisGasto() {
		return analisisGasto;
	}
	
	public void setAnalisisGasto(AnalisisDeHojeo analisisGasto) {
		this.analisisGasto = analisisGasto;
	}

	public BigDecimal getCostoFlete() {
		return costoFlete;
	}

	public void setCostoFlete(BigDecimal costoFlete) {
		Object old=this.costoFlete;
		this.costoFlete = costoFlete;
		firePropertyChange("costoFlete", old, costoFlete);
	}
	
	public BigDecimal getCostoGasto() {
		return costoGasto;
	}

	public void setCostoGasto(BigDecimal costoGasto) {
		Object old=this.costoGasto;
		this.costoGasto = costoGasto;
		firePropertyChange("costoGasto", old, costoGasto);
	}
	
	public BigDecimal getImporteDelFlete(){
		BigDecimal imp=getCostoFlete().multiply(BigDecimal.valueOf(getCantidadEnUnidad()));
		return CantidadMonetaria.pesos(imp).amount();
	}
	
	public BigDecimal getCostoEntrada(){
		return getCosto()
				.multiply(BigDecimal.valueOf(getCantidadEnUnidad()))
				;
	}

	
	@Column(name="COSTO_MP",nullable=false,scale=6,precision=16)
	private BigDecimal costoMateriaPrima=BigDecimal.ZERO;

	
	
	public BigDecimal getCostoMateriaPrima() {
		return costoMateriaPrima;
	}

	public void setCostoMateriaPrima(BigDecimal costoMateriaPrima) {
		this.costoMateriaPrima = costoMateriaPrima;
	}

	public void actualizarCosto(){
		CantidadMonetaria costom=CantidadMonetaria.pesos(getCostoMateriaPrima());
		CantidadMonetaria flete=CantidadMonetaria.pesos(getCostoFlete());
		setCosto(costom.add(flete).amount());
	}

	public BigDecimal getCostoMateria() {
		return costoMateriaPrima;
	}

	public void setCostoCorte(BigDecimal costoGasto) {
		Object old=this.costoGasto;
		this.costoGasto = costoGasto;
		firePropertyChange("costoGasto", old, costoGasto);
	}

	public BigDecimal getCostoCorte() {
		// TODO Auto-generated method stub
		return costoGasto;
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
	
	public void actualizarDescripcion(){
		if(getProducto().isMedidaEspecial()){
			NumberFormat nf=new DecimalFormat("#.#");
			String desc=MessageFormat.format("{0} {1}X{2}"
					, getProducto().getDescripcion()
					,nf.format(getAncho())
					,nf.format(getLargo())
					);
			setDescripcion(desc);
		}
		
	}
	
}
