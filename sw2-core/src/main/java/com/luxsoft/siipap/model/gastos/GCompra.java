package com.luxsoft.siipap.model.gastos;

import java.math.BigDecimal;
import java.text.MessageFormat;
import java.util.Calendar;
import java.util.Collections;
import java.util.Currency;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.Version;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.hibernate.annotations.AccessType;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.hibernate.annotations.Type;
import org.hibernate.validator.NotNull;
import org.springframework.util.Assert;

import com.luxsoft.siipap.model.Autorizacion;
import com.luxsoft.siipap.model.BaseBean;
import com.luxsoft.siipap.model.CantidadMonetaria;
import com.luxsoft.siipap.model.Departamento;
import com.luxsoft.siipap.model.Sucursal;
import com.luxsoft.siipap.util.MonedasUtils;



@Entity
@Table (name="SW_GCOMPRA")
public class GCompra extends BaseBean implements GCargoAbono{
	
	@Id @GeneratedValue (strategy=GenerationType.AUTO)
	@Column (name="COMPRA_ID")
	private Long id;
	
	@Version
	private int version;
	
	@ManyToOne (optional=false)
	@JoinColumn (name="SUCURSAL_ID", nullable=false)
	private Sucursal sucursal;
	
	@ManyToOne (optional=false,cascade={CascadeType.PERSIST,CascadeType.MERGE})
	@JoinColumn (name="PROVEEDOR_ID", nullable=false, updatable=true)
	@NotNull
	private GProveedor proveedor;
	
		
	@Column (name="FECHA",nullable=false)
	@Type (type="date")
	private Date fecha=new Date();
	
	@ManyToOne (optional=false)
	@JoinColumn (name="DEPARTAMENTO_ID", nullable=false)
	private Departamento departamento;
	
	@Enumerated(EnumType.STRING)
	@Column (name="TIPO",nullable=false, length=30)
	private TipoDeCompra tipo=TipoDeCompra.NORMAL;
	
	@Enumerated(EnumType.STRING)
	@Column (name="ESTADO",nullable=false,length=20)
	private EstadoDeCompra estado=EstadoDeCompra.GENERADA;
	
	@Column (name="ENTREGA")
	@Type (type="date")
	private Date fechaEntrega=new Date();
	
	@Column (name="VENCE")
	@Type (type="date")
	private Date vencimiento=new Date();
	
		
	@Column(name="FACTURA",length=20)
	private String factura;
	
	@Column (name="FACTURA_FECHA",nullable=true)
	@Type (type="date")
	private Date facturaFecha;
	
	@Column (name="MONEDA",length=3)
	private Currency moneda=MonedasUtils.PESOS;
	
	@Column (name="TC",scale=4,precision=12)
	private BigDecimal tc=BigDecimal.ONE;
	
	@Column (name="IMPORTE",scale=2)
	private BigDecimal importe=BigDecimal.ZERO;
	
	@Column (name="IMPUESTO",scale=2)
	private BigDecimal impuesto=BigDecimal.ZERO;
	
	@Column (name="TOTAL",scale=2)
	private BigDecimal total=BigDecimal.ZERO;
	
	@Column (name="RET1_IMPP",scale=4)
	private BigDecimal retencion1Imp=BigDecimal.ZERO;
	
	@Column (name="RET2_IMPP",scale=4)
	private BigDecimal retencion2Imp=BigDecimal.ZERO;
	
	@Column (name="COMENTARIO")
	private String comentario;
	
	@Column (name="PRESUPUESTO")
	private Long presupuesto;
	
	//@Embedded
	@Transient
	private Autorizacion autorizacion;
	
	@Column (name="INVERSION")
	private boolean inversion;
	
	@Column (name="YEAR",nullable=false)
	private int year;
	
	@Column (name="MES",nullable=false)
	private int mes;
	
	
	@OneToMany(cascade={
			 CascadeType.PERSIST
			,CascadeType.MERGE
			,CascadeType.REMOVE
			}
			,fetch=FetchType.LAZY,mappedBy="compra")
	@Cascade(value=org.hibernate.annotations.CascadeType.DELETE_ORPHAN)
	@AccessType (value="field")
	private Set<GCompraDet> partidas=new HashSet<GCompraDet>();
	
	@OneToMany(cascade={
			 CascadeType.PERSIST
			,CascadeType.MERGE
			,CascadeType.REMOVE
			}
			,fetch=FetchType.EAGER,mappedBy="compra")
	@Cascade(value=org.hibernate.annotations.CascadeType.DELETE_ORPHAN)
	@Fetch(value=FetchMode.SELECT)
	@AccessType (value="field")
	private Set<GFacturaPorCompra> facturas=new HashSet<GFacturaPorCompra>();
		
	
	public GCompra(){}
	
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}	

	public String getComentario() {
		return comentario;
	}
	public void setComentario(String comentario) {
		Object old=this.comentario;
		this.comentario = comentario;
		firePropertyChange("comentario", old, comentario);
	}

	public Departamento getDepartamento() {
		return departamento;
	}
	public void setDepartamento(Departamento departamento) {
		Object old=this.departamento;
		this.departamento = departamento;
		firePropertyChange("departamento", old, departamento);
	}	

	public EstadoDeCompra getEstado() {
		return estado;
	}
	public void setEstado(EstadoDeCompra estado) {
		this.estado = estado;
	}
	
	public Date getFecha() {
		return fecha;
	}
	public void setFecha(Date fecha) {
		Object old=this.fecha;
		this.fecha = fecha;
		firePropertyChange("fecha", old, fecha);
	}

	public Date getFechaEntrega() {
		return fechaEntrega;
	}
	public void setFechaEntrega(Date fechaEntrega) {
		Object old=this.fechaEntrega;
		this.fechaEntrega = fechaEntrega;
		firePropertyChange("fechaEntrega", old, fechaEntrega);
	}

	public boolean isInversion() {
		return inversion;
	}
	public void setInversion(boolean inversion) {
		boolean old=this.inversion;
		this.inversion = inversion;
		firePropertyChange("inversion", old, inversion);
	}
	
	public Long getPresupuesto() {
		return presupuesto;
	}
	public void setPresupuesto(Long presupuesto) {
		this.presupuesto = presupuesto;
	}

	public GProveedor getProveedor() {
		return proveedor;
	}
	public void setProveedor(GProveedor proveedor) {
		Object old=this.proveedor;
		this.proveedor = proveedor;
		firePropertyChange("proveedor", old, proveedor);
	}	

	public Sucursal getSucursal() {
		return sucursal;
	}
	public void setSucursal(Sucursal sucursal) {
		Object old=this.sucursal;
		this.sucursal = sucursal;
		firePropertyChange("sucursal", old, sucursal);
	}

	public TipoDeCompra getTipo() {
		return tipo;
	}
	public void setTipo(TipoDeCompra tipo) {
		Object old=this.tipo;
		this.tipo = tipo;
		firePropertyChange("tipo", old, tipo);
	}
	
	public Currency getMoneda() {
		return moneda;
	}
	public void setMoneda(Currency moneda) {
		Object old=this.moneda;
		this.moneda = moneda;
		firePropertyChange("moneda", old, moneda);
	}
	
	public BigDecimal getTc() {
		return tc;
	}
	public void setTc(BigDecimal tc) {
		Object old=this.tc;
		this.tc = tc;
		firePropertyChange("tc", old, tc);
	}	
	
	public CantidadMonetaria getImporteMN() {
		return toMN(getImporte());
	}
	
	public BigDecimal getImporte() {
		return importe;
	}
	public void setImporte(BigDecimal importe) {
		Object old=this.importe;
		this.importe = importe;
		firePropertyChange("importe", old, importe);
	}
	
	public CantidadMonetaria getImporteEnCantidadMonetaria(){
		return new CantidadMonetaria(getImporte().doubleValue(),getMoneda());
	}

	public CantidadMonetaria getImpuestoMN() {
		return toMN(getImpuesto());
	}
	
	public BigDecimal getImpuesto() {
		return impuesto;
	}
	public void setImpuesto(BigDecimal impuesto) {
		Object old=this.impuesto;
		this.impuesto = impuesto;
		firePropertyChange("impuesto", old, impuesto);
	}
	public CantidadMonetaria getImpuestoEnCantidadMonetaria(){
		return new CantidadMonetaria(getImpuesto().doubleValue(),getMoneda());
	}

	public BigDecimal getTotal() {
		return total;
	}
	public void setTotal(BigDecimal total) {
		Object old=this.total;
		this.total = total;
		firePropertyChange("total", old, total);
	}
	
	public CantidadMonetaria getTotalAsCantidadMonetaria(){
		return new CantidadMonetaria(getTotal().doubleValue(),getMoneda());
	}
	public CantidadMonetaria getTotalMN(){
		return toMN(getTotal());
	}

	public Date getVencimiento() {
		return vencimiento;
	}
	public void setVencimiento(Date vencimiento) {
		Object old=this.vencimiento;
		this.vencimiento = vencimiento;
		firePropertyChange("vencimiento", old, vencimiento);
	}
	
	public String getFactura() {
		return factura;
	}
	public void setFactura(String factura) {
		Object old=this.factura;
		this.factura = factura;
		firePropertyChange("factura", old, factura);
	}
	
	public Date getFacturaFecha() {
		return facturaFecha;
	}
	public void setFacturaFecha(Date facturaFecha) {
		Object old=this.facturaFecha;
		this.facturaFecha = facturaFecha;
		firePropertyChange("facturaFecha", old, facturaFecha);
	}

	public BigDecimal getRetencion1Imp() {
		BigDecimal ret=BigDecimal.ZERO;
		for(GCompraDet det:partidas){
			ret=ret.add(det.getRetencion1Imp());
			retencion1Imp=ret;
		}
		return retencion1Imp;
	}
	public void setRetencion1Imp(BigDecimal retencion1Imp) {
		this.retencion1Imp = retencion1Imp;
	}

	public BigDecimal getRetencion2Imp() {
		return retencion2Imp;
	}
	public void setRetencion2Imp(BigDecimal retencion2Imp) {
		this.retencion2Imp = retencion2Imp;
	}
	
	public CantidadMonetaria getRet1MN(){
		return toMN(getRetencion1Imp());
	}
	public CantidadMonetaria getRet2MN(){
		return toMN(getRetencion2Imp());
	}
	public CantidadMonetaria getRetencionesMN(){
		return getRet1MN().add(getRet2MN());
	}
	public CantidadMonetaria toMN(final BigDecimal val){
		return new CantidadMonetaria(val.doubleValue()*getTc().doubleValue(),MonedasUtils.PESOS);
	}
	
	public int getMes() {
		return mes;
	}
	public void setMes(int mes) {
		this.mes = mes;
	}

	public int getYear() {
		return year;
	}
	public void setYear(int year) {
		this.year = year;
	}
	
	public int getVersion() {
		return version;
	}
	public void setVersion(int version) {
		this.version = version;
	}
	
	public Autorizacion getAutorizacion() {
		return autorizacion;
	}
	public void setAutorizacion(Autorizacion autorizacion) {
		this.autorizacion = autorizacion;
	}
	
	
	/** Manejo de collecciones **/
	
	public Set<GCompraDet> getPartidas() {
		return Collections.unmodifiableSet(partidas);
	}
	
	public boolean agregarPartida(final GCompraDet det){
		Assert.notNull(det,"La partida a agregar no debe ser nula");
		det.setCompra(this);
		return partidas.add(det);
	}
	
	public boolean removerPartida(final GCompraDet det){
		Assert.notNull(det,"La partida no debe ser nula");		
		boolean res= partidas.remove(det);
		if(res)
			det.setCompra(null);
		return res;
	}
	
	
	/**Factory methos */
	
	/**
	 * Genera una partida para el producto indicado
	 */
	public GCompraDet generarPartida(final GProductoServicio prod){
		final GCompraDet det=new GCompraDet(prod);
		if(agregarPartida(det))
			return det;
		return null;
	}
	
	public Autorizacion autorizar(final String usuario){
		/*Autorizacion a=new Autorizacion(new Date(),usuario);
		setAutorizacion(a);
		return a;
		*/return null;
	}
	
	/** Utility methos **/
	
	
	
	/**
	 * Regresa la fecha probable de entrega en funcion de los dias
	 * indicados
	 *  
	 */
	public Date estimarFechaDeEntrega(final int dias){
		final Calendar c=Calendar.getInstance();
		c.setTime(getFecha());
		c.add(Calendar.DATE, dias);
		setFechaEntrega(c.getTime());
		return getFechaEntrega();
	}
	
	public void calcularVencimiento(){
		if(getProveedor()!=null){
			final Calendar c=Calendar.getInstance();
			c.add(Calendar.DATE, getProveedor().getPlazo());
			setVencimiento(c.getTime());
		}
		
	}
	

	
	/**
	 * Actualiza los datos relevantes en el contexto actual.
	 * Es decir segun el estado que guarden sus propiedades
	 * 
	 */
	public void actualizar(){
		actualizarTotal();		
		if(getProveedor()!=null){
			estimarFechaDeEntrega(getProveedor().getTiempoDeEntrega());
			calcularVencimiento();
		}
		
	}
	
	public void actualizarTotal(){
		BigDecimal importe=BigDecimal.ZERO;
		BigDecimal impuesto=BigDecimal.ZERO;
		BigDecimal total=BigDecimal.ZERO;
		BigDecimal ret1=BigDecimal.ZERO;
		BigDecimal ret2=BigDecimal.ZERO;
		
		for(GCompraDet det: getPartidas()){
			det.actualizar();
			importe=importe.add(det.getImporte());
			impuesto=impuesto.add(det.getImpuestoImp());
			total=total.add(det.getTotal());
			ret1=ret1.add(det.getRetencion1Imp());
			ret2=ret2.add(det.getRetencion2Imp());
			
		}
		/*
		importe=importe.multiply(getTc());
		impuesto=impuesto.multiply(getTc());
		total=total.multiply(getTc());
		ret1=ret1.multiply(getTc());
		ret2=ret2.multiply(getTc());
		*/
		
		
		setImporte(importe);
		setImpuesto(impuesto);
		setTotal(total);
		setRetencion1Imp(ret1);
		setRetencion2Imp(ret2);
		
	}
	
	/**** BASEBEAN Implementation *****/
	
	public boolean equals(Object o){
		if(o==null) return false;
		if(o==this) return true;
		GCompra c=(GCompra)o;
		return new EqualsBuilder()
		.append(getId(),c.getId())
		.isEquals();
	}
	
	public int hashCode(){
		return new HashCodeBuilder(17,35)
		.append(getId())
		.toHashCode();
	}
	
	@Override
	public String toString() {
		String pattern="Id: {0} {1} {2} {3} {4} {5} {6} {7} {8} {9}";
		return MessageFormat.format(pattern
				,getId()
				,getProveedor()
				,getImporte()
				,getMoneda()
				,getEstado()
				,getTipo()
				,getFecha()
				,getTotal()
				,getMes()
				,getYear()
				);
	}
	

	/**
	 * @return the facturas
	 */
	public Set<GFacturaPorCompra> getFacturas() {
		return Collections.unmodifiableSet(facturas);
	}
	
	/**
	 * Agrega facturas a la coleccion de facturas para esta compra, la factura
	 * puede estar asociada o no a la compra
	 * 
	 * 
	 * @param fac
	 * @return
	 */
	public boolean agregarFactura(GFacturaPorCompra fac){
		validarFacturaRegsitrada(fac.getDocumento());
		validarImporteDeFacturas(fac.getImporte());
		return facturas.add(fac);
	}
	
	public boolean eliminarFactura(GFacturaPorCompra fac){
		return facturas.remove(fac);
	}

	/**
	 * Factory-Method para la generacion de la cuenta por pagar
	 * 
	 * @return
	 */
	public GFacturaPorCompra crearCuentaPorPagar(){
		if(!facturas.isEmpty())
			return null;
		GFacturaPorCompra cxp=new GFacturaPorCompra();
		cxp.setCompra(this);
		cxp.setProveedor(getProveedor().getNombreRazon());
		cxp.setFecha(new Date());
		CantidadMonetaria tot=getSaldoPorRevisar();
		cxp.setImporte(MonedasUtils.calcularImporteDelTotal(tot));
		cxp.setImpuesto(MonedasUtils.calcularImpuesto(cxp.getImporte()));
		cxp.setTotal(tot);
		cxp.setApagar(tot);
		cxp.setMoneda(getMoneda());
		cxp.setTc(getTc());
		cxp.setRfc(getProveedor().getRfc());
		cxp.setVencimiento(getVencimiento());
		cxp.actualizarSaldo();
		return cxp;
	}
	
	/**
	 * Valida que la factura no este ya registrada para esta Orden de Compra
	 * 
	 * @param factura
	 */
	public void validarFacturaRegsitrada(final String factura){
		for(GFacturaPorCompra fac:facturas){
			if(fac.getDocumento().equals(factura))
				throw new RuntimeException("Esta factura ya esta registrada para esta compra");
		}
	}
	
	/**
	 * Valida que el importe de la factura que se desa registrar no haga que el importe de todas las facturas supere
	 * el importe de la compra
	 * 
	 * @param importe
	 */
	public void validarImporteDeFacturas(CantidadMonetaria valor){
		/*
		if(getTotalAsCantidadMonetaria().compareTo(getImporteDeFacturasRegistradas())==0)
			throw new RuntimeException("El importe de la orden de compra ya esta amparado por facturas existentes," +
					"\n no se pueden agregar mas facturas sin modificar la orden");
		CantidadMonetaria saldo=getSaldoPorRevisar();
		if(saldo.compareTo(valor)<=0){
			final String pattern="Importe de la factura incorrecto. " +
					"\nSaldo por asignar={0}" +
					"\nImporte registrado={1}";;
			throw new RuntimeException(MessageFormat.format(pattern, saldo,valor));
		}
		*/
	}
	/**
	 * Obtiene el importe de las facturas asociadas con esta orden de
	 * compra
	 * 
	 * 
	 * @return
	 */
	public CantidadMonetaria getImporteDeFacturasRegistradas(){
		CantidadMonetaria monto=new CantidadMonetaria(0,getMoneda());
		for(GFacturaPorCompra fac:facturas){
			monto=monto.add(fac.getTotal());
		}
		return monto;
		
	}
	
	/**
	 * Regresa el importe ya requisitado 
	 * 
	 * @return
	 */
	public CantidadMonetaria getImporteDeFacturasRequisitadas(){
		CantidadMonetaria monto=new CantidadMonetaria(0,getMoneda());
		for(GFacturaPorCompra fac:facturas){
			monto=monto.add(fac.getRequisitado());
		}
		return monto;
		
	}
	
	/**
	 * Saldo por ser requisitado
	 * 
	 * @return
	 */
	public CantidadMonetaria getSaldoPorRevisar(){
		CantidadMonetaria saldo=getTotalAsCantidadMonetaria().subtract(getImporteDeFacturasRequisitadas());
		return saldo;
	}
	
	
	
	
	public void actualizarSaldoDeFacturas(){
		for(GFacturaPorCompra fac:facturas){
			fac.actualizarSaldo();
		}
	}
	
	
	public CantidadMonetaria getIetu(){
		CantidadMonetaria valor=CantidadMonetaria.pesos(0);
		for(GCompraDet det:partidas){
			valor=valor.add(det.getIetu());
		}
		return valor;
	}
	
}                                                                                                                                                                                                                                                                                                                                                                                                                                 
