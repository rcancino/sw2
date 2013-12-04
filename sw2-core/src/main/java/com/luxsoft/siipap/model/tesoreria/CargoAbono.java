package com.luxsoft.siipap.model.tesoreria;

import java.math.BigDecimal;
import java.text.MessageFormat;
import java.util.Currency;
import java.util.Date;

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
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.Version;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.hibernate.annotations.Formula;
import org.hibernate.annotations.Type;
import org.hibernate.validator.NotNull;
import org.jasypt.util.numeric.BasicDecimalNumberEncryptor;
import org.springframework.beans.BeanUtils;

import com.luxsoft.siipap.cxc.model.Pago;
import com.luxsoft.siipap.model.Autorizacion;
import com.luxsoft.siipap.model.BaseBean;
import com.luxsoft.siipap.model.CantidadMonetaria;
import com.luxsoft.siipap.model.Sucursal;
import com.luxsoft.siipap.model.UserLog;
import com.luxsoft.siipap.model.tesoreria.Cuenta.Clasificacion;
import com.luxsoft.siipap.util.MonedasUtils;
import com.luxsoft.sw3.tesoreria.model.TraspasoDeCuenta;

import freemarker.template.utility.StringUtil;


/**
 * Movimiento de las cuentas bancarias
 * 
 * Desde el punto de vista del sistema un Cargo es una disminición del saldo de la cuenta
 * y se expresa mediante el signo negativo en el importe.
 * Por otro lado el abono es con signo positivo
 * 
 * @author Ruben Cancino
 *
 */
@Entity
@Table (name="SW_BCARGOABONO")
public class CargoAbono extends BaseBean{
	
	@Id @GeneratedValue (strategy=GenerationType.AUTO)
	@Column(name="CARGOABONO_ID")
	private Long id;
	
	@Version
	private int version;
	
	@ManyToOne(optional=false,fetch=FetchType.EAGER)
    @JoinColumn(name="CUENTA_ID", nullable=false,updatable=false)
    @NotNull
	private Cuenta cuenta;
	
	@Column(name="fecha" , nullable=false)
	@Type(type="date")
	private Date fecha;
	
	@Column(name="FECHA_COBRO" , nullable=false)
	@Type(type="date")
	private Date fechaCobro=new Date();
	
	@Column(name="FECHA_DEPOSITO" )
	@Type(type="date")
	private Date fechaDeposito=new Date();
	
	@Column(name="AFAVOR",length=255,nullable=false)
	private String aFavor;
	
	@Column(name="RFC",length=20)
	private String rfc;
	
	@Column (name="MONEDA",nullable=false,length=3)
	private Currency moneda=MonedasUtils.PESOS;
	
	@Column (name="TC",nullable=false,scale=4)
	private BigDecimal tc=BigDecimal.ONE;
	
	@Column (name="IMPORTE",nullable=false,scale=2)
	private BigDecimal importe=BigDecimal.ZERO;
			
	@Column (name="COMENTARIO",length=150)
	private String comentario;
	
	@Column (name="COMENTARIO2",length=150)
	private String comentario2;
	
	@Column (name="REFERENCIA",length=100)
	private String referencia;	
	
	@ManyToOne(optional=true)
    @JoinColumn(name="CONCEPTO_ID")	
	private Concepto concepto;
		
	@Enumerated(EnumType.STRING)
	@Column(name="FORMAPAGO",nullable=true)
	private FormaDePago formaDePago=FormaDePago.CHEQUE;
	
	@Enumerated(EnumType.STRING)
	@Column(name="ORIGEN",nullable=false,length=30)
	private Origen origen=Origen.MOVIMIENTO_MANUAL;
	
	@ManyToOne(optional=true)
    @JoinColumn(name="SUCURSAL_ID")
    private Sucursal sucursal;
	
	@ManyToOne(optional=true
			,cascade={CascadeType.PERSIST,CascadeType.MERGE}
	,fetch=FetchType.EAGER)
	@JoinColumn(name="AUT_ID")
	private Autorizacion autorizacion;
	
	@Embedded
	private UserLog userLog=new UserLog();
	
	@Column(name="CONCILIADO",nullable=false)
	private Boolean conciliado=Boolean.FALSE;
	
	@Column(name="ENCRIPTAR",nullable=false)
	private boolean encriptado=false;
	
	@OneToOne(optional=true,mappedBy="pago")
	private Requisicion requisicion;
		
	private Date impreso;
	
	@Column(name="LIBERADO",nullable=false)
	private boolean liberado;
	
	@Column(name="ENTREGADO",nullable=false)
	private boolean entregado=false;
	
	@Type(type="date")
	private Date entregadoFecha;
	
	@Column(name="revisado",nullable=false)
	private boolean revisado=false;
	
	//@Column(name="PAGO_ID")
	//public String pago;
	
	@ManyToOne(optional=true,fetch=FetchType.EAGER)
    @JoinColumn(name="PAGO_ID" )
	private Pago pago;
	
	@Column(name="CHE_RECIBIDO")
	@Type(type="date")
	private Date chequeDevueltoRecibido;
	
	@Formula("(select a.CARGOABONO_ID FROM SX_CORTE_TARJETAS_APLICACIONES a where a.CARGOABONO_ID=CARGOABONO_ID)")
	private Long aplicacionDeTarjeta;
	
	@ManyToOne(optional=true)
	@JoinColumn (name="TRASPASO_ID",nullable=true)
	private TraspasoDeCuenta traspaso;

	@Column(name="CLASIFICACION",length=50)
	private String clasificacion;
	
	public CargoAbono(){}
	
	/**
	 * Destinatario del pago (Solo aplica para los egresos)
	 * 
	 * @return
	 */
	public String getAFavor() {
		return aFavor;
	}
	public void setAFavor(String favor) {
		Object old=this.aFavor;
		aFavor = favor;
		firePropertyChange("aFavor", old, favor);
	}
	
	
	public Autorizacion getAutorizacion() {
		return autorizacion;
	}
	public void setAutorizacion(Autorizacion autorizacion) {
		this.autorizacion = autorizacion;
	}
	
	public UserLog getUserLog() {
		if(userLog==null){
			userLog=new UserLog();
		}
		return userLog;
	}
	public void setUserLog(UserLog userLog) {
		this.userLog = userLog;
	}

	public String getComentario() {
		return comentario;
	}
	public void setComentario(String comentario) {
		Object old=this.comentario;
		this.comentario = comentario;
		firePropertyChange("comentario", old, comentario);
	}

	public Cuenta getCuenta() {
		return cuenta;
	}
	public void setCuenta(Cuenta cuenta) {
		Object old=this.cuenta;
		this.cuenta = cuenta;
		firePropertyChange("cuenta", old, cuenta);
	}	

	public Date getFecha() {
		return fecha;
	}
	public void setFecha(Date fecha) {
		Object old=this.fecha;
		this.fecha = fecha;
		firePropertyChange("fecha", old, fecha);
	}

	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}

	public BigDecimal getImporte() {
		return importe;
	}
	public void setImporte(BigDecimal importe) {
		Object old=this.importe;
		this.importe = importe;
		firePropertyChange("importe", old, importe);
	}	

	public Currency getMoneda() {
		return moneda;
	}
	public void setMoneda(Currency moneda) {
		Object old=this.moneda;
		this.moneda = moneda;
		firePropertyChange("moneda", old, moneda);
	}	

	public String getRfc() {
		return rfc;
	}
	public void setRfc(String rfc) {
		Object old=this.rfc;
		this.rfc = rfc;
		firePropertyChange("rfc", old, rfc);
	}	

	public BigDecimal getTc() {
		return tc;
	}
	public void setTc(BigDecimal tc) {
		Object old=this.tc;
		this.tc = tc;
		firePropertyChange("tc", old, tc);
	}
	
	public Concepto getConcepto() {
		return concepto;
	}
	public void setConcepto(Concepto tipo) {
		Object old=this.concepto;
		this.concepto = tipo;
		firePropertyChange("concepto", old, concepto);
	}
	
	public int getVersion() {
		return version;
	}
	public void setVersion(int version) {
		this.version = version;
	}
	
	
	/**
	 * Define si este movimiento ha sido conciliado con el banco
	 *  
	 * @return
	 */
	public Boolean getConciliado() {
		return conciliado;
	}
	public void setConciliado(Boolean conciliado) {
		Object old=this.conciliado;
		this.conciliado = conciliado;
		firePropertyChange("conciliado", old, conciliado);
	}

	/**
	 * Normalmente es el numero de cheque o el numero de autorizacion/transferencia 
	 * En la implementación actual y en el caso de los cargos este campo se considera
	 * al liberar el pago
	 * 
	 * @return
	 */
	public String getReferencia() {
		return referencia;
	}
	public void setReferencia(String referencia) {
		Object old=this.referencia;
		this.referencia = referencia;
		firePropertyChange("referencia", old, referencia);
	}
	
	public FormaDePago getFormaDePago() {
		return formaDePago;
	}
	public void setFormaDePago(FormaDePago formaDePago) {
		Object old=this.formaDePago;
		this.formaDePago = formaDePago;
		firePropertyChange("formaDePago", old, formaDePago);
	}
	
	public Sucursal getSucursal() {
		return sucursal;
	}
	public void setSucursal(Sucursal sucursal) {
		Object old=this.sucursal;
		this.sucursal = sucursal;
		firePropertyChange("sucursal", old, sucursal);
	}
	
	public boolean isEncriptado() {
		return encriptado;
	}
	public void setEncriptado(boolean encriptado) {
		this.encriptado = encriptado;
	}
	
	public Origen getOrigen() {
		return origen;
	}
	public void setOrigen(Origen origen) {
		this.origen = origen;
	}

	/**
	 * @return the requisicion
	 */
	public Requisicion getRequisicion() {
		return requisicion;
	}

	/**
	 * @param requisicion the requisicion to set
	 */
	public void setRequisicion(Requisicion requisicion) {
		this.requisicion = requisicion;
	}

	/****		equals,hashCode y toString implementations **/
	
	public boolean equals(Object target){
		if(target==null)return false;
		if(target==this) return true;
		CargoAbono next=(CargoAbono)target;
		return new EqualsBuilder()
		.append(getId(), next.getId())
		/*.append(getAFavor(), next.getAFavor())
		.append(getTipo(), next.getTipo())
		.append(getFecha(), next.getFecha())
		.append(getCreado(), next.getCreado())
		.append(getMoneda(),next.getMoneda())
		*/.append(getImporte(),next.getImporte())
		.isEquals();
	}
	
	public int hashCode(){
		return new HashCodeBuilder()
		.append(getId())
		/*.append(getAFavor())
		.append(getTipo())
		.append(getFecha())
		.append(getCreado())
		.append(getMoneda())
		*/.append(getImporte())
		.toHashCode();		
	}
	
	public String toString(){
		String pattern="{0} {1} {2} {3} {4}";
		return MessageFormat.format(pattern, getId(),getFecha(),getAFavor(),getImporte(),getMoneda());
	}
	
	public void cancelar(){
		setImporte(BigDecimal.ZERO);
	}
	
	/*** FactoryMethod implementations ****/
	
	
	/**
	 * Genera un cargo a la cuenta de banco indicada con datos de importe,fecha y destinatario
	 * 
	 */
	public static CargoAbono crearCargo(Cuenta cta, BigDecimal importe,Date fecha,String aFavor,final Concepto tipo,final Sucursal suc){
		CargoAbono cargo=new CargoAbono();
		cargo.setAFavor(aFavor);		
		cargo.setImporte(importe.multiply(BigDecimal.valueOf(-1)));
		cargo.setCuenta(cta);
		cargo.setFecha(fecha);
		cargo.setConcepto(tipo);
		cargo.setMoneda(cta.getMoneda());		
		cargo.setSucursal(suc);
		cargo.setEncriptado(cta.isEncriptar());
		return cargo;
	}
	
	public static CargoAbono crearAbono(Cuenta cta, BigDecimal importe,Date fecha,final Concepto tipo,final Sucursal suc){
		CargoAbono cargo=new CargoAbono();
		cargo.setAFavor(cta.getBanco().getEmpresa().getNombre());
		cargo.setImporte(importe);
		cargo.setCuenta(cta);
		cargo.setFecha(fecha);
		cargo.setConcepto(tipo);
		cargo.setMoneda(cta.getMoneda());
		cargo.setSucursal(suc);
		cargo.setEncriptado(cta.isEncriptar());
		return cargo;
	}
	
	public static CargoAbono crearCargoAbono(BigDecimal importe,Date fecha,String comentario){
		CargoAbono cargo=new CargoAbono();		
		cargo.setImporte(importe);		
		cargo.setFecha(fecha);
		cargo.setComentario(comentario);
		return cargo;
	}	
	
	/**
	private static BasicDecimalNumberEncryptor decimalEncryptor=new BasicDecimalNumberEncryptor();
	static{
		decimalEncryptor.setPassword("sys");
	}**/
	
	
	/**
	 * Encripta el cargo  
	 * 
	 * @param cargo
	 
	public static void encrypt(final CargoAbono cargo){
		//BasicDecimalNumberEncryptor decimalEncryptor=new BasicDecimalNumberEncryptor();
		//decimalEncryptor.setPassword("sys");
		BigDecimal val=decimalEncryptor.encrypt(cargo.getImporte());
		cargo.setImporte(val);
	}*/
	
	/**
	 * Decripta el cargo
	 * 
	 * @param cargo
	 
	public static void decypt(final CargoAbono cargo){
		//BasicDecimalNumberEncryptor decimalEncryptor=new BasicDecimalNumberEncryptor();
		//decimalEncryptor.setPassword("sys");
		BigDecimal val=decimalEncryptor.decrypt(cargo.getImporte());
		cargo.setImporte(val);
	}
	*/
	
	/**
	 * Decripta el importe del cargo
	 * 
	 * @param cargo
	
	public static BigDecimal decyptImporte(final BigDecimal imp){		
		BigDecimal val=decimalEncryptor.decrypt(imp);
		return val;
	}*/
	
	public static void main(String[] args) {
		String val="25249276750655042048813831091111651553367293953.6";
		BasicDecimalNumberEncryptor decimalEncryptor=new BasicDecimalNumberEncryptor();
		decimalEncryptor.setPassword("sys");
		System.out.println(decimalEncryptor.decrypt(new BigDecimal(val)));
		/*
		CargoAbono abono=new CargoAbono();
		abono.setImporte(val);
		//encrypt(abono);
		//System.out.println("Abono: "+abono.getImporte());
		abono.decypt(abono);
		System.out.println("Abono: "+abono.getImporte());
		System.out.println(val);
		*/
	}

	/**
	 * @return the impreso
	 */
	public Date getImpreso() {
		return impreso;
	}

	/**
	 * @param impreso the impreso to set
	 */
	public void setImpreso(Date impreso) {
		this.impreso = impreso;
	}

	
	public CantidadMonetaria getImporteMN(){
		BigDecimal val=getImporte().multiply(getTc());
		return CantidadMonetaria.pesos(val.doubleValue());
	}
	
	public CantidadMonetaria getImporteMNSinIva(){
		return MonedasUtils.calcularImporteDelTotal(getImporteMN()); 
	}
	
	public long getChequeNumero(){
		if(getCuenta()!=null)
			if(getCuenta().getTipo()!=null)
				if(getCuenta().getTipo().equals(Clasificacion.CHEQUES))
					return Long.valueOf(getReferencia());
		return 0;
			
			
	}
	
	public CargoAbono clone(){
		CargoAbono clone=new CargoAbono();
		BeanUtils.copyProperties(this, clone);
		clone.setId(null);
		clone.setVersion(0);
		return clone;
	}

	public boolean isEntregado() {
		return entregado;
	}
	public void setEntregado(boolean entregado) {
		this.entregado = entregado;
	}

	public Date getEntregadoFecha() {
		return entregadoFecha;
	}
	public void setEntregadoFecha(Date entregadoFecha) {
		this.entregadoFecha = entregadoFecha;
	}

	public boolean isLiberado() {
		return liberado;
	}
	public void setLiberado(boolean liberado) {
		this.liberado = liberado;
	}

	/**
	 * Comentario para el control de cheques en Gastos
	 * @return
	 */
	public String getComentario2() {
		return comentario2;
	}

	public void setComentario2(String comentario2) {
		this.comentario2 = comentario2;
	}

	/**
	 * Indica si un deposito importado ha sido revisado por tesoreria 
	 * 
	 * @return
	 */
	public boolean isRevisado() {
		return revisado;
	}

	public void setRevisado(boolean revisado) {
		this.revisado = revisado;
	}

	public Pago getPago() {
		return pago;
	}

	public void setPago(Pago pago) {
		this.pago = pago;
	}

		
	public BigDecimal getDeposito(){
		return getImporte().doubleValue()>0?getImporte():BigDecimal.ZERO;
	}
	
	public BigDecimal getRetiro(){
		return getImporte().doubleValue()<0?getImporte():BigDecimal.ZERO;
	}
	
	public String getDescripcion(){
		if(StringUtils.isNotBlank(getClasificacion())){
			return getComentario();
		}
		if(getOrigen().equals(Origen.MOVIMIENTO_MANUAL) || getOrigen().equals(Origen.CHE))
			return getComentario();
		else if(getImporte().doubleValue()>0){
			String pattern="Deposito suc: {0}";
			return MessageFormat.format(pattern, getSucursal()!=null?getSucursal().getNombre():"ND");
		}else if(getAplicacionDeTarjeta()!=null){
			return getComentario();
		}else
			return getAFavor();
	}
	
	
	public String getConcep(){
		String concep;
			if (getConcepto()==null)
				concep=" ";
			else
				concep=getConcepto().getClave();
			return concep;
		}

	public Date getChequeDevueltoRecibido() {
		return chequeDevueltoRecibido;
	}

	public void setChequeDevueltoRecibido(Date chequeDevueltoRecibido) {
		Object old=this.chequeDevueltoRecibido;
		this.chequeDevueltoRecibido = chequeDevueltoRecibido;
		firePropertyChange("chequeDevueltoRecibido", old, chequeDevueltoRecibido);
	}
	
	@Transient
	private boolean recibidoChe;

	public boolean isRecibidoChe() {
		if(getOrigen().equals(Origen.CHE) && (getImporte().doubleValue()<0))
				return getChequeDevueltoRecibido()==null;
		return false;
	}

	public void setRecibidoChe(boolean recibidoChe) {
		Object old=this.recibidoChe;
		this.recibidoChe = recibidoChe;
		if(!recibidoChe && getOrigen().equals(Origen.CHE))
			if(getImporte().doubleValue()<0){
				setChequeDevueltoRecibido(new Date());
				firePropertyChange("recibidoChe", old, recibidoChe);
			}
		
	}

	public Long getAplicacionDeTarjeta() {
		return aplicacionDeTarjeta;
	}

	public Date getFechaCobro() {
		return fechaCobro;
	}

	public void setFechaCobro(Date fechaCobro) {
		this.fechaCobro = fechaCobro;
	}

	public TraspasoDeCuenta getTraspaso() {
		return traspaso;
	}

	public void setTraspaso(TraspasoDeCuenta traspaso) {
		this.traspaso = traspaso;
	}

	public String getClasificacion() {
		return clasificacion;
	}

	public void setClasificacion(String clasificacion) {
		this.clasificacion = clasificacion;
	}

	public Date getFechaDeposito() {
		return fechaDeposito;
	}

	public void setFechaDeposito(Date fechaDeposito) {
		this.fechaDeposito = fechaDeposito;
	}

	

}
