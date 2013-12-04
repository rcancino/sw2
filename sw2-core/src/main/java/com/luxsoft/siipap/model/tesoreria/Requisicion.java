package com.luxsoft.siipap.model.tesoreria;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Currency;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.CascadeType;
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
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.Version;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.hibernate.annotations.AccessType;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.Columns;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.hibernate.annotations.Type;
import org.hibernate.validator.Email;
import org.hibernate.validator.NotNull;

import com.luxsoft.siipap.cxp.model.AnalisisDeFactura;
import com.luxsoft.siipap.cxp.model.CXPPago;
import com.luxsoft.siipap.model.Autorizacion;
import com.luxsoft.siipap.model.AutorizacionException;
import com.luxsoft.siipap.model.BaseBean;
import com.luxsoft.siipap.model.CantidadMonetaria;
import com.luxsoft.siipap.model.Sucursal;
import com.luxsoft.siipap.model.UserLog;
import com.luxsoft.siipap.model.core.Proveedor;
import com.luxsoft.siipap.util.MonedasUtils;


/**
 *  
 * La requisición es el eje para la generación de pagos a terceros, es decir
 * la generacion egresos mediante  instancias de {@link CargoAbono} 
 * 
 * @author Octavio Hernandez,Ruben Cancino
 * 
 *
 */
@Entity
@Table(name="SW_TREQUISICION")
public class Requisicion extends BaseBean{


	@Id @GeneratedValue(strategy=GenerationType.AUTO)
	@Column(name="REQUISICION_ID")
	private Long id;
	
	@Version
	private int version;
	
	
	@ManyToOne(optional=true)
	@JoinColumn(name="CONCEPTO_ID")
	private Concepto concepto;
	
	@Column(name="ORIGEN", length=10,nullable=false)	
	private String origen=TESORERIA;
	
	@Column(name="AFAVOR",length=255,nullable=false)
	private String afavor;
	
	@Column(name="RFC")
	//@Pattern (regex="^(([A-Z]|[a-z]|\\s){1})(([A-Z]|[a-z]){3})([0-9]{6})((([A-Z]|[a-z]|[0-9]){3}))",message="RFC incorrecto")
	private String rfc;
	
	@Column(name="PRESUPUESTO_ID",nullable=true)
	private Long presupuesto;
	
	@Enumerated(EnumType.STRING)
	@Column(name="ESTADO",nullable=false)
	private Estado estado=Estado.SOLICITADA;
	
	@Type (type="date")
	@Column(name="FECHA",nullable=false)
	private Date fecha=new Date();
	
	@Type (type="date")
	private Date fechaDePago;
		
	@Column(name="FORMADEPAGO", length=15)
	private FormaDePago formaDePago=FormaDePago.CHEQUE;
	
	@Transient
	private CantidadMonetaria impuesto=CantidadMonetaria.pesos(0);
	
	@Transient
	private CantidadMonetaria importe=CantidadMonetaria.pesos(0);
	
	@Type(type="com.luxsoft.siipap.support.hibernate.CantidadMonetariaCompositeUserType")
	@Columns(columns={
			 @Column(name="TOTAL",scale=2)
			,@Column(name="TOTAL_MON",length=3)
		
	})	
	private CantidadMonetaria total=CantidadMonetaria.pesos(0);
	
	@Column(name="MONEDA",nullable=false)
	@NotNull
	private Currency moneda=MonedasUtils.PESOS;
	
	@Column(name="TC", precision=19,scale=4 )
	private BigDecimal tipoDeCambio=BigDecimal.ONE;;
	
	
	@Column(name="COMENTARIO", length=100)
	private String comentario;
	
	@Column(name="NOTIFICAR", length=100)
	//@Email
	private String notificar;
	
	@OneToMany(cascade={
			 CascadeType.PERSIST
			,CascadeType.MERGE
			,CascadeType.REMOVE
			}
			,fetch=FetchType.LAZY,mappedBy="requisicion")
	@Cascade(value=org.hibernate.annotations.CascadeType.DELETE_ORPHAN)
	@AccessType (value="field")
	private Set<RequisicionDe> partidas=new HashSet<RequisicionDe>();
	
	
	@ManyToOne(optional=true
			,cascade={CascadeType.ALL}
			,fetch=FetchType.EAGER)
	@JoinColumn(name="AUT_ID",nullable=true)
	@Fetch(value=FetchMode.JOIN)
	@AccessType(value="field")
	private Autorizacion autorizacion;
	
	@OneToOne(optional=true
			,cascade={CascadeType.MERGE,CascadeType.PERSIST}
			,fetch=FetchType.EAGER)
	@JoinColumn(
    	name="CARGOABONO_ID", unique=true)
    @AccessType(value="field")
    @Fetch(value=FetchMode.JOIN)
	private CargoAbono pago;
	
	@ManyToOne(optional=true)
	@JoinColumn(name="PROVEEDOR_ID",nullable=true)
	private Proveedor proveedor;
	
	@OneToOne(mappedBy="requisicion")
	private CXPPago pagoCxp;
	
	@Column (name="DESCUENTOF",nullable=true)
	//@Transient
	private double descuentoFinanciero=0;
	
	@Embedded
	private UserLog userLog=new UserLog();
	
	public Requisicion(){}	
	
	/**
	 * Identificador de base de datos
	 * 
	 * @return {@link Long}
	 */	
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}	
	
	/**
	 * Control de version para el manejo de transacciones
	 * 
	 * @return
	 */
	public int getVersion() {
		return version;
	}
	public void setVersion(int version) {
		this.version = version;
	}

	
	
	public Proveedor getProveedor() {
		return proveedor;
	}

	public void setProveedor(Proveedor proveedor) {
		Object old=this.proveedor;
		this.proveedor = proveedor;
		firePropertyChange("proveedor", old, proveedor);
		if(proveedor!=null){
			setAfavor(proveedor.getNombreRazon());
			setRfc(proveedor.getRfc());
			setNotificar(proveedor.getEmail1());
		}else{
			setAfavor(null);
			setRfc(null);
			setNotificar(null);
		}
		
	}
	
	

	public CXPPago getPagoCxp() {
		return pagoCxp;
	}

	public void setPagoCxp(CXPPago pagoCxp) {
		this.pagoCxp = pagoCxp;
	}

	/**
	 * Concepto opcional para clasificar dentro del modulo de tesoria
	 * 
	 * @return {@link Concepto}
	 */	
	public Concepto getConcepto() {
		return concepto;
	}
	public void setConcepto(Concepto concepto) {
		Object old=this.concepto;
		this.concepto = concepto;
		firePropertyChange("concepto", old, concepto);
	}
	
	/**
	 * Modulo origen de la requisicion
	 * 
	 * @return
	 */
	public String getOrigen(){
		return origen;
	}	
	public void setOrigen(String origen) {
		this.origen = origen;
	}

	/**
	 * Fecha de la requisición
	 * 
	 * @return
	 */
	public Date getFecha() {
		return fecha;
	}
	public void setFecha(Date fecha) {
		this.fecha = fecha;
	}
	
	/**
	* Persona Moral/Fisica a la que se le debe pagar
	*  
	* @return El nombre del beneificario del pago
	*/	
	public String getAfavor() {
		return afavor;
	}
	public void setAfavor(String afavor) {
		String old=this.afavor;
		this.afavor = afavor;
		firePropertyChange("afavor", old, afavor);
	}
	
	/**
	 * RFC del beneficiario del pago
	 * 
	 * @return {@link String}
	 */
	public String getRfc() {
		return rfc;
	}
	public void setRfc(String rfc) {
		String old=this.rfc;
		this.rfc = rfc;
		firePropertyChange("rfc", old, rfc);
	}
	
	/**
	 * Comentario relacionado con la Requisición
	 * 
	 * @return {@link String}
	 */
	public String getComentario() {
		return comentario;
	}
	public void setComentario(String comentario) {
		this.comentario = comentario;
	}
	
	/**
	 * Estado actual de la requisición
	 * 
	 * @return {@link Estado}
	 */
	public Estado getEstado() {
		return estado;
	}
	public void setEstado(Estado estado) {
		this.estado = estado;
	}
	
	/**
	 * Fecha requerida para el pago. Con esta fecha se puede programar el pago
	 * 
	 * @return 
	 */
	public Date getFechaDePago() {
		return fechaDePago;
	}
	public void setFechaDePago(Date fechaDePago) {
		this.fechaDePago = fechaDePago;
	}
	
	/**
	 * Forma en que se generara el pago
	 * 
	 * @return {@link FormaDePago}
	 */
	public FormaDePago getFormaDePago() {
		return formaDePago;
	}
	public void setFormaDePago(FormaDePago formaDePago) {
		this.formaDePago = formaDePago;
	}

	

	/**
	 * 
	 * 
	 * @return {@link Currency}
	 */
	public Currency getMoneda() {
		return moneda;
	}
	public void setMoneda(Currency moneda) {
		Object old=this.moneda;
		this.moneda = moneda;
		firePropertyChange("moneda", old, moneda);
	}

	/**
	 * Define de que area de la programación presupuestal
	 * se toman los recursos
	 * 
	 * 
	 * @return
	 */
	public Long getPresupuesto() {
		return presupuesto;
	}
	public void setPresupuesto(Long presupuesto) {
		this.presupuesto = presupuesto;
	}

	/**
	 * Tipo de cambio al momento de la requisicion
	 * 
	 * @return {@link BigDecimal}
	 */
	public BigDecimal getTipoDeCambio() {
		return tipoDeCambio;
	}
	public void setTipoDeCambio(BigDecimal tipoDeCambio) {
		Object old=this.tipoDeCambio;
		this.tipoDeCambio = tipoDeCambio;
		firePropertyChange("tipoDeCambio", old, tipoDeCambio);
	}
	
	/**
	 * Correo electrónico de la persona a la que se le tenga que infromar
	 * del cargo
	 * 
	 * @return
	 */
	public String getNotificar() {
		return notificar;
	}
	public void setNotificar(String notificar) {
		String old=this.notificar;
		this.notificar = notificar;
		firePropertyChange("notificar", old, notificar);
	}
	
	public Autorizacion getAutorizacion() {
		return autorizacion;
	}
	
	
	
	

	/**
	 * Bitacora de modificaciones
	 * 
	 * @return
	 */
	public UserLog getUserLog() {
		return userLog;
	}
	public void setUserLog(UserLog userLog) {
		this.userLog = userLog;
	}
	
	public Set<RequisicionDe> getPartidas() {
		return Collections.unmodifiableSet(partidas);
	}
	/**
	 * Agrega una Detalle de requisicion a la colleccion de partidas
	 * 
	 * @param det
	 * @return
	 */
	public boolean agregarPartida(final RequisicionDe det){
		det.setRequisicion(this);
		return partidas.add(det);
	}
	
	@Transient
	private List<AnalisisDeFactura> analisisPorEliminar=new ArrayList<AnalisisDeFactura>();
	
	/**
	 * Elimina una partida de requisicion de la coleccion de partidas
	 * 
	 * @param det
	 * @return
	 */
	public boolean eleiminarPartida(final RequisicionDe det){
		if(det!=null){
			det.setRequisicion(null);
			det.setFacturaDeCompras(null);
			det.setFacturaDeGasto(null);
			if(det.getAnalisis()!=null){
				System.out.println("Agregando analisis por des vincular");
				analisisPorEliminar.add(det.getAnalisis());
			}
			det.setAnalisis(null);
			return partidas.remove(det);
		}
		return false;
		
	}
	
	public List<AnalisisDeFactura> getAnalisisPorActualizar(){
		return analisisPorEliminar;
	}
	
	public void eliminarPartidas(){
		partidas.clear();
	}
	
	/**
	 * Fija el total de la Requisición en funcion de sus partidas
	 * aun cuando el total es dinamico (la suma de los totales de las partidas)
	 * este metodo permite generar un propertychante event para informar 
	 * que el valor del total puede haber sido modificado. Util en la UI
	 * para actualizar la presentacion del total cuando se insertan/eliminan partidas
	 *  
	 */
	public void actualizarTotal(){
		/*
		CantidadMonetaria to=new CantidadMonetaria(0,getMoneda());
		for(RequisicionDe det:partidas){
			//det.actualizarDelTotal();
			to=to.add(det.getTotal());
		}
		//setImporte(getImporte());
		//setImpuesto(getImpuesto());
		 */ 
		 /*if((getConcepto()!=null) 
				 && getConcepto().getClave().equals("ANTICIPO")
				 && getOrigen().equals("COMPRAS"))
			 return;*/
		CantidadMonetaria tot=new CantidadMonetaria(0,getMoneda());
		for(RequisicionDe det:partidas){
			tot=tot.add(det.getTotal());
		}
		//System.out.println("Nuevo total calculado: "+tot);
		setTotal(tot);
	}
	
	public CantidadMonetaria getTotalCalculado(){
		CantidadMonetaria tot=new CantidadMonetaria(0,getMoneda());
		for(RequisicionDe det:partidas){
			tot=tot.add(det.getTotal());
		}
		return tot;
	}
	
	public CantidadMonetaria getImporte(){
		CantidadMonetaria imp=new CantidadMonetaria(0,getMoneda());
		for(RequisicionDe det:partidas){
			imp=imp.add(det.getImporte());
		}
		return imp;
	}
	
	public CantidadMonetaria getImpuesto(){
		CantidadMonetaria imp=new CantidadMonetaria(0,getMoneda());
		for(RequisicionDe det:partidas){
			imp=imp.add(det.getImpuesto());
		}
		return imp;
	}
	
	public CantidadMonetaria getTotal() {
		return total;
	}
	
	public void setTotal(final CantidadMonetaria total){
		Object old=this.total;
		this.total=total;
		//System.out.println("Old total: "+old+ "  New: "+total);
		firePropertyChange("total", old, total);
	}
	
	public void setImporte(CantidadMonetaria importe) {
		Object old=this.importe;
		this.importe = importe;
		firePropertyChange("importe", old, importe);
	}

	public void setImpuesto(CantidadMonetaria impuesto) {
		Object old=this.impuesto;
		this.impuesto = impuesto;
		firePropertyChange("impuesto", old, impuesto);
	}
	
	//@AssertTrue (message="Debe registrar por lo menos una partida con importe")
	public boolean validarTotal(){
		return getTotal().amount().doubleValue()>0;
	}

	public double getDescuentoFinanciero() {
		return descuentoFinanciero;
	}

	public void setDescuentoFinanciero(double descuentoFinanciero) {
		double old=this.descuentoFinanciero;
		this.descuentoFinanciero = descuentoFinanciero;
		firePropertyChange("descuentoFinanciero", old, descuentoFinanciero);
		System.out.println("DF: "+descuentoFinanciero+ "  Id:"+getId());
	}

	public boolean equals(Object o){
		if(o==null) return false;
		if(o==this) return true;
		Requisicion r=(Requisicion)o;
		return new EqualsBuilder()
		.append(getId(),r.getId())
		.isEquals();
	}
	
	public int hashCode(){
		return new HashCodeBuilder(17,35)
		.append(getId())
		.toHashCode();
	}
	
	@Override
	public String toString() {
		return getAfavor()+" Imp"+getTotal();
	}
	
	public boolean autorizar(final Autorizacion aut){
		if(estado.equals(Estado.REVISADA)){
			if(this.autorizacion==null){
				setEstado(Estado.AUTORIZADA);
				this.autorizacion=aut;
				return true;
			}
		}
		return false;
	}
	
	public Autorizacion cancelarAutorizacion(){
		if(estado.equals(Estado.AUTORIZADA)){
			setEstado(Estado.REVISADA);
			Autorizacion old=this.autorizacion;
			this.autorizacion=null;
			return old;
		}
		return null;
	}
	
	/**
	 * Cambia el estado de la requisicion a REVISADA
	 * @return
	 */
	public boolean revision(){
		if(getEstado().equals(Estado.SOLICITADA)){
			setEstado(Estado.REVISADA);
			return true;
		}
		return false;
	}
	
	/***
	 * Regresa el estado de  la requisicion a SOLICITADA
	 * @return
	 */
	public boolean cancelarRevision(){
		if(getEstado().equals(Estado.REVISADA)){
			setEstado(Estado.SOLICITADA);
			return true;
		}
		return false;
	}
	
	public CantidadMonetaria getPorPagar(){
		if(getTipoDeCambio().equals(BigDecimal.ONE)){
			return getTotal();
		}
		//CantidadMonetaria porPagar=CantidadMonetaria.pesos(getTotal().multiply(getTipoDeCambio()).amount());
		if(getMoneda().equals(MonedasUtils.PESOS)){
			return CantidadMonetaria.pesos(getTotal().divide(getTipoDeCambio()).amount());
		}else if(!getMoneda().equals(MonedasUtils.PESOS)){
			return getTotal();
		}
		return CantidadMonetaria.pesos(getTotal().multiply(getTipoDeCambio()).amount());
	}
	
	public void registrarPagoDeGastos(final Cuenta cta,final Date fecha,String coment,final String ref){
		Origen orig=Origen.valueOf(getOrigen());
		registrarCargo(cta, fecha, coment, ref, orig);
	}
	
	public void registrarCargo(final Cuenta cta,final Date fecha,String coment,final String ref,final Origen origen){		
		/*if(getAutorizacion()==null){
			throw new AutorizacionException("La requisicion no esta autorizada");
		}*/
		if(pago!=null)
			return;
		
		Currency monedaCuenta=cta.getMoneda();
		Currency monedaRequisicion=getMoneda();
		CantidadMonetaria importe=getPorPagar();
		
		if(monedaRequisicion.equals(MonedasUtils.DOLARES)  && monedaCuenta.equals(MonedasUtils.PESOS)){
			importe=importe.multiply(getTipoDeCambio());
		}
		CargoAbono cargo=CargoAbono.crearCargo(
				cta
				,importe.amount()
				, fecha
				,getAfavor()
				,getConcepto()
				,null
				);
		//cargo.setMoneda(getPorPagar().currency());
		cargo.setMoneda(cta.getMoneda());
		cargo.setTc(getTipoDeCambio());
		cargo.setAutorizacion(getAutorizacion());
		cargo.setComentario(coment);
		cargo.setRequisicion(this);
		cargo.setReferencia(ref);
		cargo.setConcepto(getConcepto());
		cargo.setOrigen(origen);
		this.pago=cargo;
	}
	
	
	
	public void cancelarPago(){
		if(getPago()!=null){			
			setEstado(Estado.REVISADA);
			this.pago=null;
		}		
	}
	
	/**
	 * Enumeracion para catalogar el estado de la requisicion
	 * El estado permite asociar reglas especificas para el comportamiento
	 * de la requisción y el flujo de procesos relacionados con la misma
	 * 
	 * Esta enumeración representa el ciclo de vida de una requisición
	 * 
	 * @author Ruben Cancino
	 *
	 */
	public static enum Estado{
		/**
		 * Tesoreria o algun otro modulo ha generado la requisición para
		 * su revisión y en su caso autorización. En esta etapa no se ha generado
		 * ningun cargo a la cuenta de bancos
		 * 
		 */
		SOLICITADA,
		
		/**
		 * Fase previa a la autorización, este estado implica que la requisición
		 * ha sido revisada por algun usuario con autorización y esta lista para su 
		 * autorización
		 * 
		 */
		REVISADA,
		
		/**
		 * La requisición no cumple con los requerimientos oficiales y por lo tanto
		 * es rechazada. El origen de la requisición puede hacer los cambios,  completar
		 * la documentación o en general cumplir con la regla de negicio y
		 * solicitar nuevamente la revisión
		 *  
		 */
		RECHAZADA,
		
		/**
		 * La requisición es autorizada por contraloria el pago puede proceder
		 * 
		 */
		AUTORIZADA,
		
		/**
		 * Este es el estado final de la requisición. Si la fomrma de pago es Cheque
		 * se entiende que el cheque esta listo para ser impreso y entregado.
		 * Si es transferencia se entiene que esta se ha realizado y se le puede informar 
		 * al benecificario. En este proceso se genera el Cargo a la cuenta de bancos  
		 * 
		 */
		PAGADA;
		
		public String getName(){
			return name();
		}
	}

	/**
	 * @return the pago
	 */
	public CargoAbono getPago() {
		return pago;
	}	
	

	public static final String TESORERIA="TESORERIA";
	public static final String GASTOS="GASTOS";
	public static final String COMPRAS="COMPRAS";
	
	
	public Sucursal getSucursalInicial(){
		if(partidas.isEmpty())
			return null;
		return partidas.iterator().next().getSucursal();
	}
	
	@Column(name="CLIENTE_CLAVE")
	private String claveCliente;
	

	public String getClaveCliente() {
		return claveCliente;
	}

	public void setClaveCliente(String claveCliente) {
		Object old=this.claveCliente;
		this.claveCliente = claveCliente;
		firePropertyChange("claveCliente", old, claveCliente);
	}

	public static String[] TiposDeDevoluciones={"SALDO_A_FAVOR","DEPOSITO_POR_IDENTIFICAR","DEPOSITO_DEVUELTO","NOTA_MOSTRADOR","NOTA_CAMIONETA","NOTA_CREDITO"};
	
	
}
