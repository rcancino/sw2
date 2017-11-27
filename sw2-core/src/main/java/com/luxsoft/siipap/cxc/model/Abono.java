package com.luxsoft.siipap.cxc.model;



import java.math.BigDecimal;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Currency;
import java.util.Date;
import java.util.List;
import java.util.ListIterator;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.DiscriminatorType;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Version;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.time.DateUtils;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.Formula;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.IndexColumn;
import org.hibernate.annotations.Parameter;
import org.hibernate.annotations.Type;
import org.hibernate.validator.AssertTrue;
import org.hibernate.validator.Length;
import org.hibernate.validator.NotNull;
import org.springframework.util.Assert;

import com.luxsoft.siipap.model.BaseBean;
import com.luxsoft.siipap.model.CantidadMonetaria;
import com.luxsoft.siipap.model.EntityUserLog;
import com.luxsoft.siipap.model.Sucursal;
import com.luxsoft.siipap.model.UserLog;
import com.luxsoft.siipap.model.core.Cliente;
import com.luxsoft.siipap.util.DateUtil;
import com.luxsoft.siipap.util.MonedasUtils;
import com.luxsoft.sw3.replica.ReplicaDestination;
import com.luxsoft.sw3.replica.Replicable;

/**
 * Abono a la cuenta de un cliente
 * 
 * @author Ruben Cancino
 *
 */
@Entity
@Table(name="SX_CXC_ABONOS")
@Inheritance(strategy=InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name="TIPO_ID",discriminatorType=DiscriminatorType.STRING,length=10)
@GenericGenerator(name="hibernate-uuid",strategy="uuid"
		,parameters={
				@Parameter(name="separator",value="-")
			}
		)
public abstract class Abono extends BaseBean implements EntityUserLog,Replicable,ReplicaDestination{
	
	static final long serialVersionUID = 76959458772L;
	
	@Id @GeneratedValue(generator="hibernate-uuid")
	@Column(name="ABONO_ID")
	protected String id;
	
	@Column(name="FOLIO",nullable=true)	
	private int folio=0;	
	
	@Version
	private int version;
	
	@Column(name="TIPO_ID",updatable=false,insertable=false)
	private String tipo;
	
				
	@ManyToOne(optional = false)
	@JoinColumn(name = "CLIENTE_ID", nullable = false, updatable = true)
	//@Cascade(value={org.hibernate.annotations.CascadeType.REPLICATE})
	@NotNull(message="El Cliente es requerido")
	private Cliente cliente;
	
	@ManyToOne(optional = false)
	@JoinColumn(name = "SUCURSAL_ID", nullable = false, updatable = false)
	private Sucursal sucursal;
	
	@Enumerated(EnumType.STRING)
	@Column(name = "ORIGEN", nullable = false, length = 3)
	private OrigenDeOperacion origen=OrigenDeOperacion.CRE;
	
	@Column(name = "NOMBRE", nullable = false)
	private String nombre;
	
	@Column(name = "CLAVE", length = 7)
	private String clave;
	
	@Column(name = "FECHA", nullable = false)
	@Type(type = "date")
	private Date fecha = new Date();
	
	@Column(name = "MONEDA", nullable = false)
	private Currency moneda = MonedasUtils.PESOS;
	
	@Column(name = "TC", nullable = false)
	private double tc = 1;
	
	
	@Column(name = "COMENTARIO")
	@Length(max = 255)
	private String comentario;
	
	@Column(name="IMPORTE",nullable=false)
	private BigDecimal importe=BigDecimal.ZERO;
	
	@Column(name="IMPUESTO",nullable=false)
	private BigDecimal impuesto=BigDecimal.ZERO;
	
	@Column(name = "TOTAL", nullable = false)
	private BigDecimal total = BigDecimal.ZERO;
	
		
	@Formula("(select ifnull(sum(X.IMPORTE),0) FROM SX_CXC_APLICACIONES X where X.ABONO_ID=ABONO_ID)")
	private BigDecimal aplicado=BigDecimal.ZERO;
	
	@Formula("(select MAX(X.CAR_ORIGEN) FROM SX_CXC_APLICACIONES X  where X.ABONO_ID=ABONO_ID AND X.FECHA=SAF )")  // se cambio select MAX(X.CAR_ORIGEN) FROM SX_CXC_APLICACIONES X where X.ABONO_ID=ABONO_ID
	private String origenAplicacion;
	
	@Formula("(select MAX(X.CAR_TIPO) FROM SX_CXC_APLICACIONES X  where X.ABONO_ID=ABONO_ID AND X.FECHA=SAF )")  // se cambio select MAX(X.CAR_ORIGEN) FROM SX_CXC_APLICACIONES X where X.ABONO_ID=ABONO_ID
	private String carTipoAplicacion;
	

	
	
	@OneToMany(cascade={
			 CascadeType.PERSIST
			,CascadeType.MERGE
			,CascadeType.REMOVE
			}
			,fetch= FetchType.LAZY
				)
	@JoinColumn(name="ABONO_ID",nullable=false)
	@Cascade(value={org.hibernate.annotations.CascadeType.DELETE_ORPHAN,org.hibernate.annotations.CascadeType.REPLICATE})
	@IndexColumn(name="RENGLON",base=0)
	private List<Aplicacion> aplicaciones=new ArrayList<Aplicacion>();
	
	@Column(name = "LIBERADO", nullable = true)
	@Type(type = "date")
	private Date liberado;
	
	
	@Embedded
	private UserLog log=new UserLog();
	
	
	
			
	@ManyToOne(optional = true,cascade={CascadeType.PERSIST,CascadeType.MERGE},fetch=FetchType.EAGER)
	@JoinColumn(name = "AUTORIZACION_ID", nullable = true)
	@Cascade(value={org.hibernate.annotations.CascadeType.REPLICATE})
	private AutorizacionDeAbono autorizacion;
	
	@Column(name="SAF")
	@Type(type = "date")
	private Date primeraAplicacion;
	
	@Column(name="DIFERENCIA")
	private BigDecimal diferencia=BigDecimal.ZERO;
	
	@Column(name="DIFERENCIA_FECHA")
	@Type(type = "date")
	private Date direfenciaFecha;
	
	@Column(name="TX_IMPORTADO",nullable=true)
	protected Date importado;
	
	@Column(name="TX_REPLICADO",nullable=true)
	protected Date replicado;

	public String getId() {
		return id;
	}
	
	public int getVersion() {
		return version;
	}
	
	public int getFolio() {
		return folio;
	}

	public void setFolio(int folio) {
		int old=this.folio;
		this.folio = folio;
		firePropertyChange("folio", old, folio);
	}	

	public Cliente getCliente() {
		return cliente;
	}

	public void setCliente(Cliente cliente) {
		Object old=this.cliente;
		this.cliente = cliente;
		if(this.cliente!=null){
			this.clave=cliente.getClave();
			this.nombre=cliente.getNombre();
		}else{
			this.clave=null;
			this.nombre=null;
		}
		firePropertyChange("cliente", old, cliente);
	}

	public String getClave() {
		return clave;
	}	

	public String getNombre() {
		return nombre;
	}	

	public Date getFecha() {
		return fecha;
	}
	
	

	public Date getPrimeraAplicacion() {
		return primeraAplicacion;
	}

	public void setPrimeraAplicacion(Date primeraAplicacion) {
		this.primeraAplicacion = primeraAplicacion;
	}

	public void setFecha(Date fecha) {
		Object old=this.fecha;
		this.fecha = fecha;
		firePropertyChange("fecha", old, fecha);
	}
	
	public void setSafeFecha(Date fecha){
		this.fecha=fecha;
	}
	
	public OrigenDeOperacion getOrigen() {
        return origen;
    }
    public void setOrigen(OrigenDeOperacion origen) {
    	Object old=this.origen;
        this.origen = origen;
        firePropertyChange("origen", old, origen);
    }

	public Sucursal getSucursal() {
		return sucursal;
	}

	public void setSucursal(Sucursal sucursal) {
		Object old=this.sucursal;
		this.sucursal = sucursal;
		firePropertyChange("sucursal", old, sucursal);
	}

	public Currency getMoneda() {
		return moneda;
	}

	public void setMoneda(Currency moneda) {
		Object old=this.moneda;
		this.moneda = moneda;
		firePropertyChange("moneda", old, moneda);
		
	}

	public double getTc() {
		return tc;
	}

	public void setTc(double tc) {
		double old=this.tc;
		this.tc = tc;
		firePropertyChange("tc", old, tc);
	}

	

	public String getComentario() {
		return comentario;
	}

	public void setComentario(String comentario) {
		Object old=this.comentario;
		this.comentario = comentario;
		firePropertyChange("comentario", old, comentario);
	}
	
	public BigDecimal getImporte() {
		if(importe==null)
			importe=BigDecimal.ZERO;
		return importe;
	}
	
	public CantidadMonetaria getImporteCM(){
		//return new CantidadMonetaria(getImporte(),getMoneda());
		return MonedasUtils.calcularImporteDelTotal(getTotalCM());
	}

	public void setImporte(BigDecimal importe) {
		Object old=this.importe;
		this.importe = importe;
		firePropertyChange("importe", old, importe);
		//actualizarImpuesto();
	}

	public BigDecimal getImpuesto() {
		if(impuesto==null)
			impuesto=BigDecimal.ZERO;
		return impuesto;
	}

	public void setImpuesto(BigDecimal impuesto) {
		Object old=this.impuesto;
		this.impuesto = impuesto;
		firePropertyChange("impuesto", old, impuesto);
		//actualizarTotal();
	}
	
	/**
	 * Actualiza el total en funcion del importe e impuesto
	 * 
	 */
	public void actualizarTotal(){
		setTotal(getImporte().add(getImpuesto()));
	}
	
	/**
	 * Actualiza el impuesto en funcion del importe aplicando
	 * el IVA estandar 
	 */
	public void actualizarImpuesto(){
		setImpuesto(MonedasUtils.calcularImpuesto(getImporte()));
	}


	public BigDecimal getTotal() {
		return total;
	}

	public void setTotal(BigDecimal total) {
		Object old=this.total;
		this.total = total;
		firePropertyChange("total", old, total);
	}

	public UserLog getLog() {
		return log;
	}

	public void setLog(UserLog log) {
		this.log = log;
	}

	public void setNombre(String nombre) {
		Object old=this.nombre;
		this.nombre = nombre;
		firePropertyChange("nombre", old, nombre);
	}

	public void setClave(String clave) {
		this.clave = clave;
	}
	
	
	public BigDecimal getDisponible() {
		return getTotal().subtract(getAplicado()).subtract(getDiferencia());
		//return getTotal().subtract(getAplicado());
		//return disponible;
	}

	public CantidadMonetaria getDisponibleCM(){
		return new CantidadMonetaria(getDisponible(),getMoneda());
	}
	
	public List<Aplicacion> getAplicaciones() {
		return aplicaciones;
		
		
	}
	
	public void agregarAplicacion(Aplicacion a){
		agregarAplicacion(a,0);
	}
	public void agregarAplicacion(Aplicacion a, int index){
		//Assert.notNull(a,"No se permite aplicaciones nulas");
		//Assert.notNull(a.getCargo(),"No se ha asignado una cuenta por pagar por lo tanto no se puede aplicar");
		a.setAbono(this);
		if(aplicaciones.isEmpty())
			setPrimeraAplicacion(a.getFecha());
		//aplicaciones.add(index, a);
		aplicaciones.add(a);
	}
	
	public boolean eliminarAplicacion(Aplicacion a){
		a.setAbono(null);
		boolean val= aplicaciones.remove(a);
		return val;
	}

	/**
	 * El monto de lo aplicado, en forma calculado. Un sub-select 
	 * a la tabla de aplicaciones
	 * 
	 * @return
	 */
	public BigDecimal getAplicado() {
		if(aplicado==null)
			aplicado=BigDecimal.ZERO; //Evitar NPE
		return aplicado;
	}
	
	public BigDecimal getAplicado(Date fecha){
	
		BigDecimal aplicado=BigDecimal.ZERO;
		for(Aplicacion a:getAplicaciones()){
			
			if(DateUtils.isSameDay(a.getFecha(),fecha)){
				aplicado=aplicado.add(a.getImporte());
			}
		}
		return aplicado;
	}
	
	public BigDecimal getAplicado(Date fecha,String sucursal){
		BigDecimal aplicado=BigDecimal.ZERO;
		for(Aplicacion a:getAplicaciones()){
			if(DateUtils.isSameDay(a.getFecha(),fecha) && a.getDetalle().getSucursal().equals(sucursal)){
				aplicado=aplicado.add(a.getImporte());
			}
		}
		return aplicado;
	}
	
	public CantidadMonetaria getDisponibleAlCorte(final Date fecha){
		CantidadMonetaria disp=getTotalCM();
		
		for(Aplicacion a:getAplicaciones()){
			if(a==null){
				System.out.println("Aplicacion nula en abono: "+getId());
				continue;
			}
			Date diaAplicacion=DateUtil.truncate(a.getFecha(), Calendar.DATE);
			Date corte=DateUtil.truncate(fecha, Calendar.DATE);
			if(diaAplicacion.compareTo(corte)<=0){
				disp=disp.subtract(a.getImporteCM());
			}
		}
		return disp;
	}
	
	/**
	 * El monto diponible para aplicar
	 * Se obtiene en forma calculada
	 * 
	 * @return
	 */
	public BigDecimal getDisponibleCalculado(){		
		//return total.subtract(getAplicado());
		return getTotal().subtract(getAplicado()).subtract(getDiferencia());
	}
	
	/**
	 * Obtiene el disponible del abono en funcion del total y las aplicaciones
	 * Util para ser ejecutado dentro de una session de Hibernate
	 * @return
	 */
	public CantidadMonetaria getDisponibleEnLinea(){
		CantidadMonetaria disponible=getTotalCM();
		ListIterator<Aplicacion> iter=aplicaciones.listIterator();
		while(iter.hasNext()){
			Aplicacion a=iter.next();
			disponible=disponible.subtract(a.getImporteCM());
		}
		return disponible;
	}
	
	/**
	 * Actualiza la propiedad de disponible a partir de las aplicaciones
	 * @deprecated El disponible es un sub-select
	 */	
	public void acutalizarDisponible(){
		BigDecimal tot=getTotal();
		ListIterator<Aplicacion> iter=aplicaciones.listIterator();
		while(iter.hasNext()){
			Aplicacion a=iter.next();
			if(a==null){
				iter.remove();
				continue;
			}
			tot=tot.subtract(a.getImporte());
		}
	}
	
	/**
	 * Origen del abono en siipapw
	 * 
	 */
	@Column(name="SIIPAP_ID", nullable=true)
	private Long siipapId=null;
	
	public Long getSiipapId() {
		return siipapId;
	}

	public void setSiipapId(Long siipapId) {
		this.siipapId = siipapId;
	}
	
	public abstract String getInfo();
	
	
	/**
	 * Autorizacion para aplicar el abono
	 * 
	 * @return
	 */
	public AutorizacionDeAbono getAutorizacion() {
		return autorizacion;
	}
	
	

	public void setAutorizacion(AutorizacionDeAbono autorizacion) {
		this.autorizacion = autorizacion;
		if(autorizacion!=null)
			setLiberado(autorizacion.getFechaAutorizacion());
		else{
			setLiberado(null);
			
		}
	}

	/**
	 * Fecha util para determinar si el abono se puede aplicar
	 * Su uso depende de las reglas asociadas en las sub clases
	 *  
	 * @return
	 */
	public Date getLiberado() {
		return liberado;
	}

	public void setLiberado(Date liberado) {
		this.liberado = liberado;
	}

	/**
	 * 
	 * @param aplicacionAut
	 * @deprecated usar setAutorizacion
	 */
	public void setAplicacionAut(AutorizacionDeAbono aplicacionAut) {
		this.autorizacion = aplicacionAut;
	}
	
	/**
	 * Define si la generacion de un abono requiere autorizacion.
	 * Por default los abonos no requieren autorizacion. En casos especiales
	 * las subclases pueden sobre escribir este metodo. 
	 *  
	 * @return
	 */
	public boolean requiereAutorizacion(){
		return true;
	}
	
	/**
	 * Verifica si este abono esta disponible para aplicar
	 * 
	 * @param sysdate La en la que se quiere aplicar. 
	 * @return
	 */
	public boolean disponibleParaAplicacion(final Date sysdate){
		if(requiereAutorizacion()){
			return getAutorizacion().isVigente(sysdate);
		}else
			return true;
	}
	
	public String getAutorizacionInfo(){
		if(requiereAutorizacion()){
			if(getAutorizacion()!=null)
				return getAutorizacion().getInfo();
			else
				return "PENDIENTE";
		}
		return "";
	}
	

	public String getTipo() {
		return tipo;
	}
	
	@AssertTrue(message="Todas las aplicaciones deben ser del mimo cliente")
	public boolean validarConsistencia(){
		for(Aplicacion a:aplicaciones){
			if(a==null) continue;
			if(!getCliente().equals(a.getCargo().getCliente())){
				System.out.println("Cliente del abono: "+getCliente());
				System.out.println("Cliente del cargo: "+a.getCargo().getCliente());
				return false;
			}
			
		}
		return true;
	}
	
	public String toString(){
		String pattern=" {0}  -  {1}    Fecha: {2,date,short}   Total:{3}  Disponible: {4} Origen:{5}";
		return MessageFormat.format(pattern
				,getTipo()
				,getInfo()
				,getFecha()
				,getTotalCM()
				,getDisponibleCalculado()
				,getOrigen()
				);
	}

	public boolean equals(Object other){
		if(other==null) return false;
		if(other==this) return true;
		if(other.getClass()!=this.getClass()) return false;
		Abono next=(Abono)other;
		return new  EqualsBuilder()		
		.append(this.sucursal, next.getSucursal())
		.append(this.clave, next.getClave())
		.append(this.getNombre(), next.getNombre())
		.append(this.fecha, next.getFecha())
		.append(this.total, next.getTotal())
		.append(this.comentario, next.getComentario())
		//.append(this.log.getCreado(), next.getLog().getCreado())
		.isEquals();		
	}
	
	public int hashCode(){
		return new HashCodeBuilder(19,37)
		.append(sucursal)
		.append(clave)
		.append(nombre)
		.append(fecha)
		.append(total)
		.append(comentario)
		//.append(log.getCreado())
		.toHashCode();
	}

	public CantidadMonetaria getTotalCM(){
		return new CantidadMonetaria(getTotal(),getMoneda());
	}
	
	
	
	public boolean isCancelado(){
		//boolean res=StringUtils.containsIgnoreCase("CANCELADO", getComentario());
		return (getTotal().doubleValue()==0 
				//|| res
				);
	}

	public BigDecimal getDiferencia() {
		if(diferencia==null)
			diferencia=BigDecimal.ZERO;
		return diferencia;
	}

	public void setDiferencia(BigDecimal diferencia) {
		this.diferencia = diferencia;
	}

	public Date getDirefenciaFecha() {
		return direfenciaFecha;
	}

	public void setDirefenciaFecha(Date direfenciaFecha) {
		this.direfenciaFecha = direfenciaFecha;
	}
	

	

	public Date getImportado() {
		return importado;
	}

	public void setImportado(Date importado) {
		this.importado = importado;
	}

	public Date getReplicado() {
		return replicado;
	}

	public void setReplicado(Date replicado) {
		this.replicado = replicado;
	}

	public String getOrigenAplicacion() {
		if(origenAplicacion==null)
			return "";
		return origenAplicacion;
	}
	
	public OrigenDeOperacion toOrienDeAplicacion(){
		if(StringUtils.isNotBlank(origenAplicacion)){
			return OrigenDeOperacion.valueOf(origenAplicacion);
		}
		return getOrigen();
	}
	
	
	
	public BigDecimal getImporteAplicado(Date fecha){
		return MonedasUtils.calcularImporteDelTotal(getAplicado(fecha));
	}
	public BigDecimal getImpuestoAplicado(Date fecha){
		return MonedasUtils.calcularImpuestoDelTotal(getAplicado(fecha));
	}
	
	public String getDestino() {
		return "REPLICA.Abono";
	}

	public String getCarTipoAplicacion() {
		return carTipoAplicacion;
	}


}
