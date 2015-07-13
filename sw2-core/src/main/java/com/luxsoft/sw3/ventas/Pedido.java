package com.luxsoft.sw3.ventas;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.Version;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.Formula;
import org.hibernate.annotations.Generated;
import org.hibernate.annotations.GenerationTime;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
import org.hibernate.annotations.Type;
import org.hibernate.validator.Length;
import org.hibernate.validator.NotNull;

import com.luxsoft.siipap.cxc.model.Esquema;
import com.luxsoft.siipap.cxc.model.EsquemaPorTarjeta;
import com.luxsoft.siipap.cxc.model.FormaDePago;
import com.luxsoft.siipap.cxc.model.Tarjeta;
import com.luxsoft.siipap.model.BaseBean;
import com.luxsoft.siipap.model.CantidadMonetaria;
import com.luxsoft.siipap.model.Sucursal;
import com.luxsoft.siipap.model.UserLog;
import com.luxsoft.siipap.model.core.Cliente;
import com.luxsoft.siipap.util.MonedasUtils;
import com.luxsoft.siipap.ventas.model.Asociado;
import com.luxsoft.siipap.ventas.model.AutorizacionParaFacturarSinExistencia;
import com.luxsoft.sw3.model.AddressLoggable;
import com.luxsoft.sw3.model.AdressLog;

/**
 * JavaBean / Entidad que es la abstraccion basica de lo que es un pedido de 
 * venta
 * 
 * @author Ruben Cancino Ramos
 *
 */
@Entity
@Table(name="SX_PEDIDOS")
//@SecondaryTable(name="SX_PEDIDOS_EXT")
@GenericGenerator(name="hibernate-uuid",strategy="uuid"
		,parameters={
				@Parameter(name="separator",value="-")
			}
		)
public class Pedido extends BaseBean implements AddressLoggable{
	
	/*@Id @GeneratedValue(strategy=GenerationType.AUTO)
	@Column(name="PEDIDO_ID")
	private Long id;
	*/
	
	@Id @GeneratedValue(generator="hibernate-uuid")
	@Column(name="PEDIDO_ID")
	protected String id;
	
	@Version
	private int version;
	
	@Column(name="FOLIO",updatable=false,insertable=true)
	//@Generated(GenerationTime.INSERT)
	private long folio;
	
	@ManyToOne(optional = false, 
			//cascade = { CascadeType.MERGE,CascadeType.PERSIST },
			fetch=FetchType.EAGER)			
	@JoinColumn(name = "CLIENTE_ID", nullable = false, updatable = true)
	@NotNull(message="El cliente es mandatorio")
	private Cliente cliente;
	
	@Column(name = "NOMBRE", nullable = false)
	private String nombre;
	
	@Column(name = "CLAVE", length = 7)
	private String clave;
	
	@ManyToOne(optional = false)
	@JoinColumn(name = "SUCURSAL_ID", nullable = false, updatable = false)
	private Sucursal sucursal;
	
	@Column(name = "FECHA", nullable = false)
	@Type(type = "date")
	private Date fecha=new Date();
	
	@Column(name = "MONEDA", nullable = false)
	private Currency moneda = MonedasUtils.PESOS;
	
	@Column(name = "TC", nullable = false)
	private double tc = 1;
	
	/**
	 * La suma del importeBruto en las partidas
	 * 
	 */
	@Column(name = "IMP_BRUTO", nullable = false)
	private BigDecimal importeBruto = BigDecimal.ZERO;
	
	
	@Column(name = "DESCUENTO", scale = 6, precision = 8, nullable = false)
	private double descuento = 0;
	
	@Column(name = "DESCUENTO_ORIG", scale = 6, precision = 8, nullable = false)
	private double descuentoOrigen = 0;
	
	/**
	 *  importeBruto*(descuento/100)
	 *  
	 */
	@Column(name = "IMP_DESCUENTO", nullable = false)
	private BigDecimal importeDescuento = BigDecimal.ZERO;
	
	/**
	 * La suma del importe de los cortes en las partidas
	 * 
	 */
	@Column(name = "IMP_CORTES", nullable = false)
	private BigDecimal importeCorte = BigDecimal.ZERO;
	
	@Column(name = "FLETE", nullable = false)
	private BigDecimal flete = BigDecimal.ZERO;
	
	@Column(name = "TARJ_COM", scale = 6, precision = 8, nullable = false)
	private double comisionTarjeta = 0;
	
	@Column(name = "TAR_COM_IMP", nullable = false)
	private BigDecimal comisionTarjetaImporte = BigDecimal.ZERO;
	
	/**
	 * importeBruto - importeDescuento + importeCortes + importeFlete + comisionTarjetaImporte
	 * 
	 */
	@Column(name = "SUBTOTAL", nullable = false)
	private BigDecimal subTotal = BigDecimal.ZERO;
	
	
	@Column(name = "IMPUESTO", nullable = false)
	private BigDecimal impuesto = BigDecimal.ZERO;	
	
	@Column(name = "TOTAL", nullable = false)
	private BigDecimal total = BigDecimal.ZERO;
	
	@Enumerated(EnumType.STRING)
	@Column(name = "TIPO", nullable = false, length = 15)
	private Tipo tipo=Tipo.CONTADO;
	
	@Enumerated(EnumType.STRING)
	@Column(name = "FENTREGA", nullable = false, length = 20)	
	private FormaDeEntrega entrega=FormaDeEntrega.LOCAL;
	
	@Enumerated(EnumType.STRING)
	@Column(name = "FPAGO", nullable = false, length = 25)
	private FormaDePago formaDePago=FormaDePago.EFECTIVO;
	
    @Column(name="COMENTARIO")
	@Length(max=255, message="El tamao maximo del comentario es de 255 caracteres")	
	private String comentario;
    
    @Column(name="COMENTARIO2",length=70)
    @Length(max=255, message="El tamao maximo del comentario es de 255 caracteres")
    private String comentario2;
    
    @Column(name="COMISION_DESC")
    private String comentarioComision;
	
	@Column(name="IMPRESO",nullable=true)	
	private Date impreso;
	
	
	@OneToMany(cascade={
			 CascadeType.PERSIST
			,CascadeType.MERGE
			,CascadeType.REMOVE
			}
			,fetch=FetchType.EAGER,mappedBy="pedido")
	@Cascade(value=org.hibernate.annotations.CascadeType.DELETE_ORPHAN)
	private Set<PedidoDet> partidas=new HashSet<PedidoDet>();
	
	
	@ManyToOne(optional = true
			,cascade={CascadeType.MERGE,CascadeType.PERSIST,CascadeType.REMOVE}
			,fetch=FetchType.EAGER)
	@JoinColumn(name = "AUTORIZACION_ID", nullable = true)
	private AutorizacionDePedido autorizacion;
	
	@OneToOne(cascade={CascadeType.MERGE,CascadeType.PERSIST,CascadeType.REMOVE})
	@JoinTable(name="SX_PEDIDOS_PAGOCE"
		,joinColumns=@JoinColumn(name="PEDIDO_ID")
		,inverseJoinColumns=@JoinColumn(name="AUT_ID")
		)
		@Cascade(value=org.hibernate.annotations.CascadeType.DELETE_ORPHAN)
	private AutorizacionDePedido pagoContraEntrega;
	
	@Column(name="FACTURAR",nullable=false)
	private boolean facturable=false;
	
	@OneToOne(mappedBy="pedido"
		,cascade={CascadeType.MERGE,CascadeType.PERSIST,CascadeType.REMOVE}
		,fetch=FetchType.EAGER
		)
	private PedidoPendiente pendiente;
	
	@Formula("(select ifnull(sum(x.total),0)+ifnull(sum(x.ANTICIPO_APLICADO),0) from sx_ventas  x where x.PEDIDO_ID=PEDIDO_ID)")	
	private BigDecimal totalFacturado=BigDecimal.ZERO;
	
	
	
	@ManyToOne(optional = true,fetch=FetchType.LAZY)
	@JoinColumn(name = "TARJETA_ID", nullable = true)
	private Tarjeta tarjeta;
	
	@ManyToOne(optional = true,fetch=FetchType.LAZY)
	@JoinColumn(name = "ESQUEMA_ID", nullable = true)
	private Esquema esquema;
	
	@OneToOne(optional=true,fetch=FetchType.LAZY,cascade={CascadeType.MERGE,CascadeType.PERSIST,CascadeType.REMOVE})
	@JoinColumn(name = "INSTRUCCION_ID",nullable=true)	
	private InstruccionDeEntrega instruccionDeEntrega;
	
	@Column(name="MISMA",nullable=false)
	private boolean mismaDireccion=false;
	
	@Column(name="PUESTO",nullable=false)
	private boolean puesto=false;
	
	@ManyToOne(optional = true,fetch=FetchType.LAZY
			,cascade={CascadeType.MERGE,CascadeType.PERSIST,CascadeType.REMOVE})			
	@JoinColumn(name = "AUT_SIN_EXIS_ID")
    private AutorizacionParaFacturarSinExistencia autorizacionSinExistencia;
	
	@Column(name="SURTIDOR",length=100)
    private String surtidor;
	
	@Column(name="PARCIAL",nullable=false)
	private boolean entregaParcial=false;
	
	@Enumerated(EnumType.STRING)
	@Column(name = "MODO", nullable = false, length = 15)
	private Modo modo=Modo.MOSTRADOR;
	
	@Enumerated(EnumType.STRING)
	@Column(name = "CLASIFICACION_VALE", nullable = false, length = 30)
	private ClasificacionVale clasificacionVale=ClasificacionVale.SIN_VALE;
	
	
	@ManyToOne 
    @JoinColumn (name="SUCURSAL_VALE")
    private Sucursal sucursalVale;
	
	@Column(name="VALE",nullable=false)
	private boolean vale=false;
	
	@Column(name="TPUESTO",nullable=true)
	private Date tpuesto;
	

	
	public boolean isVale() {
		return vale;
	}

	public void setVale(boolean vale) {
		boolean old=this.vale;
		this.vale = vale;
		firePropertyChange("vale", old, vale);
	}
	
	
	
	
	@Embedded
	@AttributeOverrides({
	       @AttributeOverride(name="createUser",	column=@Column(name="CREADO_USR"	,nullable=true,insertable=true,updatable=false)),
	       @AttributeOverride(name="updateUser",	column=@Column(name="MODIFICADO_USR",nullable=true,insertable=true,updatable=true)),
	       @AttributeOverride(name="creado", 		column=@Column(name="CREADO"		,nullable=true,insertable=true,updatable=false)),
	       @AttributeOverride(name="modificado", 	column=@Column(name="MODIFICADO"	,nullable=true,insertable=true,updatable=true))
	   })
	private UserLog log=new UserLog();
	
	@Embedded
	@AttributeOverrides({
	       @AttributeOverride(name="createdIp",	column=@Column(nullable=true,insertable=true,updatable=false)),
	       @AttributeOverride(name="updatedIp",	column=@Column(nullable=true,insertable=true,updatable=true)),
	       @AttributeOverride(name="createdMac",column=@Column(nullable=true,insertable=true,updatable=false)),
	       @AttributeOverride(name="updatedMac",column=@Column(nullable=true,insertable=true,updatable=true))
	   })
	private AdressLog addresLog=new AdressLog();
	
	@ManyToOne (optional=true,
			cascade= {CascadeType.MERGE , CascadeType.PERSIST},fetch=FetchType.LAZY)
    @JoinColumn (name="SOCIO_ID")
	//@Transient
    private Asociado socio;
	
	
	@Column(name="COMPRADOR")
	@Length(max=255)
	private String comprador;
	
	@Transient
	private boolean mismoComprador=false;
	
	@Column(name="ESPECIAL")	
	private boolean especial=false;
	
	@ManyToOne(optional = true,fetch=FetchType.EAGER)			
	@JoinColumn(name = "CHECKPLUS_ID", nullable = true, updatable = true)
	private CheckPlusOpcion checkplusOpcion;
	
	public Pedido(){}
	
	
	public Sucursal getSucursalVale() {
		return sucursalVale;
	}

	public void setSucursalVale(Sucursal sucursalVale) {
		Object old=this.sucursalVale;
		this.sucursalVale =sucursalVale;
		firePropertyChange("sucursalVale", old, sucursalVale);
	}
	
	
	
	public Pedido(boolean especial) {
		super();
		this.especial = especial;
	}



	public String getId() {
		return id;
	}

	public long getFolio() {
		return folio;
	}
	

	public void setFolio(long folio) {
		this.folio = folio;
	}



	public int getVersion() {
		return version;
	}
	

	public Cliente getCliente() {
		return cliente;
	}

	public void setCliente(Cliente cliente) {
		/*
		if(cliente==null)
			throw new IllegalArgumentException("El cliente de un pedido no puede ser nulo");
		if(isFacturado())
			throw new IllegalArgumentException("Pedido facturado no puede modificar el cliente");
			*/
		Object old=this.cliente;
		this.cliente = cliente;
		firePropertyChange("cliente", old,cliente);
		if(cliente!=null){
			setClave(cliente.getClave());
			setNombre(cliente.getNombreRazon());
		}else{
			setClave(null);
			setNombre(null);
		}
		
	}

	public String getNombre() {
		return nombre;
	}

	public void setNombre(String nombre) {
		Object old=this.nombre;
		this.nombre = nombre;
		firePropertyChange("nombre", old, nombre);
	}

	public String getClave() {
		return clave;
	}

	public void setClave(String clave) {
		Object old=this.clave;
		this.clave = clave;
		firePropertyChange("clave", old, clave);
	}

	public Sucursal getSucursal() {
		return sucursal;
	}

	public void setSucursal(Sucursal sucursal) {
		this.sucursal = sucursal;
	}

	public Date getFecha() {
		return fecha;
	}

	public void setFecha(Date fecha) {
		this.fecha = fecha;
	}
	
	

	public FormaDePago getFormaDePago() {
		return formaDePago;
	}

	public void setFormaDePago(FormaDePago formaDePago) {
		Object old=this.formaDePago;
		this.formaDePago = formaDePago;
		firePropertyChange("formaDePago", old, formaDePago);
		//setDescripcionFormaDePago(formaDePago!=null?formaDePago.name():null);
	}
	

	/*public String getDescripcionFormaDePago() {
		return descripcionFormaDePago;
	}
	public void setDescripcionFormaDePago(String descripcionFormaDePago) {
		Object old=this.descripcionFormaDePago;
		this.descripcionFormaDePago = descripcionFormaDePago;
		firePropertyChange("descripcionFormaDePago", old, descripcionFormaDePago);
	}*/

	public Tarjeta getTarjeta() {
		return tarjeta;
	}
	public void setTarjeta(Tarjeta tarjeta) {
		if(this.formaDePago==null)
			throw new IllegalStateException("La forma de pago se debe definir con antarioridad a la tarjeta");
		Object old=this.tarjeta;
		this.tarjeta = tarjeta;
		firePropertyChange("tarjeta", old, tarjeta);
		if(tarjeta!=null){
			setComisionTarjeta(this.tarjeta.getComisionVenta());			
			//setDescripcionFormaDePago(tarjeta.toString());
		}else{
			setComisionTarjeta(0);			
		}
	}

	public Esquema getEsquema() {
		return esquema;
	}

	public void setEsquema(Esquema esquema) {
		if(this.tarjeta==null)
			throw new IllegalStateException("La tarjeta se debe definir con anterioridad a la promocion");
		this.esquema = esquema;
		/*if(this.esquema!=null){
			setDescripcionFormaDePago(getDescripcionFormaDePago()+ " Prom:"+esquema.toString());
		}*/
	}
	
	public void setTarjetaConPromicion(EsquemaPorTarjeta promocion){
		if(promocion==null)
			throw new NullPointerException("No se puede definir una promocion nula");
		setTarjeta(promocion.getTarjeta());
		setEsquema(promocion.getEsquema());
		setComisionTarjeta(promocion.getComisionVenta());
	}

	public Currency getMoneda() {
		return moneda;
	}

	public void setMoneda(Currency moneda) {
		this.moneda = moneda;
	}

	public double getTc() {
		return tc;
	}

	public void setTc(double tc) {
		double old=this.tc;		
		this.tc = tc;
		firePropertyChange("tc", old, tc);
	}
	
	public BigDecimal getImporteBrutoParaDescuento(){
		BigDecimal bruto=BigDecimal.ZERO;
		for(PedidoDet det:getPartidas()){
			if(det.getProducto().getModoDeVenta().equals("B"))
				bruto=bruto.add(det.getImporteBruto());
		}
		return bruto;
	}

	public BigDecimal getImporteBruto() {
		return importeBruto;
	}
	public void setImporteBruto(BigDecimal importe) {
		Object old=this.importeBruto;
		this.importeBruto = importe;
		firePropertyChange("importeBruto", old, importeBruto);
	}
	
	
	public double getDescuento() {
		return descuento;
	}

	public void setDescuento(double descuento) {
		double old=this.descuento;
		this.descuento = descuento;
		firePropertyChange("descuento", old, descuento);
	}
	

	public double getDescuentoOrigen() {		
		return descuentoOrigen;
		
	}

	public void setDescuentoOrigen(double descuentoOrigen) {
		double old=this.descuentoOrigen;
		this.descuentoOrigen = descuentoOrigen;
		firePropertyChange("descuentoOrigen", old, descuentoOrigen);
	}

	public BigDecimal getImporteDescuento() {
		return importeDescuento;
	}

	public void setImporteDescuento(BigDecimal importeDescuento) {
		Object old=this.importeDescuento;
		this.importeDescuento = importeDescuento;
		firePropertyChange("importeDescuento", old, importeDescuento);
	}

	public BigDecimal getImporteCorte() {
		return importeCorte;
	}

	public void setImporteCorte(BigDecimal importeCorte) {
		Object old=this.importeCorte;
		this.importeCorte = importeCorte;
		firePropertyChange("importeCorte", old, importeCorte);
	}

	public double getComisionTarjeta() {
		return comisionTarjeta;
	}

	public void setComisionTarjeta(double comisionTarjeta) {
		double old=this.comisionTarjeta;
		this.comisionTarjeta = comisionTarjeta;
		firePropertyChange("comisionTarjeta", old, comisionTarjeta);
		
	}

	public BigDecimal getComisionTarjetaImporte() {
		return comisionTarjetaImporte;
	}

	public void setComisionTarjetaImporte(BigDecimal comisionTarjetaImporte) {
		Object old=this.comisionTarjetaImporte;
		this.comisionTarjetaImporte = comisionTarjetaImporte;
		firePropertyChange("comisionTarjetaImporte", old, comisionTarjetaImporte);
	}

	public BigDecimal getSubTotal() {
		return subTotal;
	}

	public void setSubTotal(BigDecimal subTotal) {
		Object old=this.subTotal;
		this.subTotal = subTotal;
		firePropertyChange("subTotal", old, subTotal);
	}

	public BigDecimal getFlete() {
		return flete;
	}

	public void setFlete(BigDecimal flete) {
		Object old=this.flete;
		this.flete = flete;
		firePropertyChange("flete", old, flete);
	}

	
	public BigDecimal getImpuesto() {
		return impuesto;
	}	

	public void setImpuesto(BigDecimal impuesto) {
		Object old=this.impuesto;
		this.impuesto = impuesto;
		firePropertyChange("impuesto", old, impuesto);
	}
	

	public BigDecimal getTotal() {
		return total;
	}
	
	public CantidadMonetaria getTotalMN(){
		return CantidadMonetaria.pesos(getTotal()).multiply(getTc());
	}

	public void setTotal(BigDecimal total) {
		Object old=this.total;
		this.total = total;
		firePropertyChange("total", old, total);
	}	

	public Tipo getTipo() {
		return tipo;
	}

	public void setTipo(Tipo tipo) {
		Object old=this.tipo;
		this.tipo = tipo;
		firePropertyChange("tipo", old, tipo);
	}
	 

	public FormaDeEntrega getEntrega() {
		return entrega;
	}

	public void setEntrega(FormaDeEntrega entrega) {
		Object old=this.entrega;
		this.entrega = entrega;
		firePropertyChange("entrega", old, entrega);
	}

	public String getComentario() {
		return comentario;
	}

	public void setComentario(String comentario) {
		Object old=this.comentario;
		this.comentario = comentario;
		firePropertyChange("comentario", old, comentario);
	}

	public String getComentario2() {
		return comentario2;
	}

	public void setComentario2(String comentario2) {
		Object old=this.comentario2;
		this.comentario2 = comentario2;
		firePropertyChange("comentario2", old, comentario2);
	}
	
	

	public String getComentarioComision() {
		return comentarioComision;
	}

	public void setComentarioComision(String comentarioComision) {
		this.comentarioComision = comentarioComision;
	}

	public boolean isFacturable() {
		return facturable;
	}

	public void setFacturable(boolean facturable) {
		this.facturable = facturable;
	}	

	public Date getImpreso() {
		return impreso;
	}

	public void setImpreso(Date impreso) {
		this.impreso = impreso;
	}

	public UserLog getLog() {
		if(log==null){
			log=new UserLog();
		}
		return log;
	}

	public void setLog(UserLog log) {
		this.log = log;
	}

	public AdressLog getAddresLog() {
		return addresLog;
	}

	public void setAddresLog(AdressLog addresLog) {
		this.addresLog = addresLog;
	}

	public Set<PedidoDet> getPartidas() {
		return partidas;
	}
	

	public boolean agregarPartida(PedidoDet det){
		det.setPedido(this);
		return partidas.add(det);
	}
	
	public boolean eliminarPartida(PedidoDet det){
		det.setPedido(null);
		return partidas.remove(det);
	}

	public InstruccionDeEntrega getInstruccionDeEntrega() {
		return instruccionDeEntrega;
	}

	public void setInstruccionDeEntrega(InstruccionDeEntrega instruccionDeEntrega) {
		if(instruccionDeEntrega!=null){
			instruccionDeEntrega.setPedido(this);
		}
		Object old=this.instruccionDeEntrega;
		this.instruccionDeEntrega = instruccionDeEntrega;
		firePropertyChange("instruccionDeEntrega", old, instruccionDeEntrega);
	}

	public boolean isMismaDireccion() {
		return mismaDireccion;
	}

	public void setMismaDireccion(boolean mismaDireccion) {
		boolean old=this.mismaDireccion;
		this.mismaDireccion = mismaDireccion;
		firePropertyChange("mismaDireccion", old, mismaDireccion);
	}
	
	
	public boolean isEntregaParcial() {
		return entregaParcial;
	}

	public void setEntregaParcial(boolean entregaParcial) {
		boolean old=this.entregaParcial;
		this.entregaParcial = entregaParcial;
		firePropertyChange("entregaParcial", old, entregaParcial);
	}
	
	
	public Modo getModo() {
		return modo;
	}	

	public void setModo(Modo modo) {
		Object old=this.modo;
		this.modo = modo;
		firePropertyChange("modo", old, modo);
	}
	

	public ClasificacionVale getClasificacionVale() {
		return clasificacionVale;
	}	

	public void setClasificacionVale(ClasificacionVale clasificacionVale) {
		Object old=this.clasificacionVale;
		this.clasificacionVale = clasificacionVale;
		firePropertyChange("clasificacionVale", old, clasificacionVale);
	}


	

	public AutorizacionDePedido getAutorizacion() {
		return autorizacion;
	}

	public void setAutorizacion(AutorizacionDePedido autorizacion) {
		this.autorizacion = autorizacion;
	}
	
	public AutorizacionDePedido getPagoContraEntrega() {
		return pagoContraEntrega;
	}

	public void setPagoContraEntrega(AutorizacionDePedido pagoContraEntrega) {
		this.pagoContraEntrega = pagoContraEntrega;
	}
	
	public boolean isContraEntrega(){
		return pagoContraEntrega!=null;
	}

	public String getComentarioAutorizacion(){
		if(autorizacion!=null)
			return autorizacion.getComentario();
		return "";
	}
	
	

	public double getUtilidad(){
		return 0;
	}
	
	public String getOperador(){
		if(getLog()!=null){
			return getLog().getCreateUser();
		}else
			return "ND";
	}
	
	

	public String getEstado(){
		if(getTotalFacturado().doubleValue()>0)
			return "FACTURADO";		
		if(isFacturable())
			return "FACTURABLE";
		if(getPendiente()!=null)
			return "POR AUTORIZAR";
		if(StringUtils.containsIgnoreCase(comentario2, "CANCELADO"))
			return "CANCELADO";
		else
			return "PENDIENTE";
		
	}
	
	
	
	public PedidoPendiente getPendiente() {
		return pendiente;
	}

	public void setPendiente(PedidoPendiente pendiente) {
		this.pendiente = pendiente;
		if(pendiente!=null)
			pendiente.setPedido(this);
	}
	
	public boolean isPorAutorizar(){
		return getPendiente()!=null;
	}
	
	public String getPendienteDesc(){
		if(pendiente!=null)
			return pendiente.getComentario()+ " "+StringUtils.trimToEmpty(pendiente.getComentario2());
		return "";
	}

	/**
	 * Actualiza los importes en funcion de los productos
	 * 
	 */
	public void actualizarImportes(){
		if(isFacturado())
			throw new IllegalStateException("El pedido ya esta facturado por lo que no se pueden actualizar los importes");
		BigDecimal bruto=BigDecimal.valueOf(0);
		BigDecimal impDesc=BigDecimal.valueOf(0);
		BigDecimal impCortes=BigDecimal.valueOf(0);
		
		//Actualizamos la comision de tarjeta
		//actualizarComisiones();
		
		for(PedidoDet det:getPartidas()){
			det.actualizar();
			bruto=bruto.add(det.getImporteBruto());
			impDesc=impDesc.add(det.getImporteDescuento());
			impCortes=impCortes.add(det.getImporteCorte());
		}
		
		setImporteBruto(bruto);
		setImporteDescuento(impDesc);
		setImporteCorte(impCortes);
		
		setSubTotal(getSubTotal1()
				//.add(getComisionTarjetaImporte()
				//.add(getFlete()))
				);
		
		// Disparamos cambio en las propiedades dinamicas
		setImpuesto(MonedasUtils.calcularImpuesto(getSubTotal()));
		setTotal(getSubTotal().add(getImpuesto()));
		
		
		
		//Notificamos de cambios en propiedades dinamicas (Listeners en UI)
		firePropertyChange("subTotal1", BigDecimal.ZERO, getSubTotal1());
		firePropertyChange("kilos", 0, getKilos());
	}
	
	/**
	 * Calcula y actualiza el importe de las comisiones
	 * 
	 *  Por Tarjeta de credito
	 * 
	
	public void actualizarComisiones(){
		switch (getFormaDePago()) {
		case TARJETA:
			registrarComision(.02,"TARJETA");
			break;
		case CHEQUE_POSTFECHADO:
			registrarComision(.04,"CHEQUE POSTFECHADO");
			break;
		default:
			break;
		}
	} */
	

	/**
	 * Registra una comision aplicable al pedido, por el % indicado
	 * 
	 * @param comision El % desado de la comision
	 * Nota Por el momento la comision a nivel pedido se registra en la columna
	 * de comision importe tarjeta
	 
	private void registrarComision(double comision,String desc){
		CantidadMonetaria importeComisionable=CantidadMonetaria.pesos(0);
		//setComisionTarjeta(comision);
		for(PedidoDet det:getPartidas()){
			if(det.getProducto().getModoDeVenta().equals("N")){
				if(det.getProducto().getClave().equals("CORTE") || det.getProducto().getClave().equals("MANIOBRA")){
					continue;
				}
				CantidadMonetaria importe=CantidadMonetaria.pesos(det.getImporteBruto());
				importe=importe.multiply(comision);
				importeComisionable=importeComisionable.add(importe);
			}
		}
		setComisionTarjetaImporte(importeComisionable.amount());
		setComentarioComision(desc);
	}*/
	
	public BigDecimal getSubTotal1(){
		return getImporteBruto()
			.subtract(getImporteDescuento()
				//.subtract(getImporteCorte())
				);
	}
	
	public boolean isFacturado(){
		return getTotalFacturado().doubleValue()>0;
	}
	
	public BigDecimal getTotalFacturado() {
		return totalFacturado;
	} 

	/**
	 * Comodity para checar si el pedido es de credito
	 */
	public boolean isDeCredito(){
		return getTipo().equals(Tipo.CREDITO);
		
	}
	
	@Transient
	private double descuentoEspecial;
	
	

	public double getDescuentoEspecial() {
		return descuentoEspecial;
	}

	public void setDescuentoEspecial(double descuentoEspecial) {
		double old=this.descuentoEspecial;
		this.descuentoEspecial = descuentoEspecial;
		firePropertyChange("descuentoEspecial", old, descuentoEspecial);
	}

	public double getKilos(){
		double kilos=0;
		for(PedidoDet det:partidas){
			if(det!=null)
				kilos+=det.getKilosCalculados();
		}
		return kilos;
	}
	
	public String getOrigen(){
		if(isDeCredito())
			return "CREDITO";
		return "CONTADO";
	}
	
	
	
	public Asociado getSocio() {
		return socio;
	}

	public void setSocio(Asociado socio) {
		Object old=this.socio;
		this.socio = socio;
		firePropertyChange("socio", old, socio);
	}

	public AutorizacionParaFacturarSinExistencia getAutorizacionSinExistencia() {
		return autorizacionSinExistencia;
	}

	public void setAutorizacionSinExistencia(
			AutorizacionParaFacturarSinExistencia autorizacionSinExistencia) {
		this.autorizacionSinExistencia = autorizacionSinExistencia;
	}

	public String getSurtidor() {
		return surtidor;
	}

	public void setSurtidor(String surtidor) {
		Object old=this.surtidor;
		this.surtidor = surtidor;
		firePropertyChange("surtidor", old, surtidor);
	}
	
	

	public String getComprador() {
		return comprador;
	}

	public void setComprador(String comprador) {
		Object old=this.comprador;
		this.comprador = comprador;
		firePropertyChange("comprador", old, comprador);
	}
	
	

	public boolean isMismoComprador() {
		return mismoComprador;
	}

	public void setMismoComprador(boolean mismoComprador) {
		boolean old=this.mismoComprador;
		this.mismoComprador = mismoComprador;
		firePropertyChange("mismoComprador", old, mismoComprador);
	}

	@Override
	public boolean equals(Object obj) {
		if(obj==null) return false;
		if(this==obj) return true;
		if(obj.getClass()!=getClass()) return false;
		Pedido other=(Pedido)obj;
		return new EqualsBuilder()
		.append(this.sucursal, other.getSucursal())
		.append(this.id,other.getId())
		.isEquals();
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder(17,35)
		.append(this.sucursal)
		.append(this.id)
		.toHashCode();
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this)
		.append("", clave)
		.append("  ", nombre)
		.append("  ", tipo)
		.append("  ",fecha)
		.append(" " ,total)
		.append(" Id: ",id)
		.toString();
	}
	
	public static enum Tipo{
		CONTADO,CREDITO
	}
	
	public static enum FormaDeEntrega{
		LOCAL,ENVIO,ENVIO_FORANEO,ENVIO_CARGO
	}

	
	public static enum Modo{
		MOSTRADOR,TELEFONICA
	}
	
	
	public static enum ClasificacionVale{
		SIN_VALE,ENVIA_SUCURSAL,RECOGE_CLIENTE,EXISTENCIA_VENTA
	}
	
	
	
	public void setTotalFacturado(BigDecimal totalFacturado) {
		this.totalFacturado = totalFacturado;
	}

	@Column(name="ANTICIPO")
	private boolean anticipo=false;
	
	



	public boolean isAnticipo() {
		return anticipo;
	}

	public void setAnticipo(boolean anticipo) {
		Object old=this.anticipo;
		this.anticipo = anticipo;
		firePropertyChange("anticipo", old, anticipo);
	}
	/*
	private String anticipoAplicado;
	
	private BigDecimal anticipoAplicadoImporte;

	public String getAnticipoAplicado() {
		return anticipoAplicado;
	}

	public void setAnticipoAplicado(String anticipoAplicado) {
		Object old=this.anticipoAplicado;
		this.anticipoAplicado = anticipoAplicado;
		firePropertyChange("anticipoAplicado", old, anticipoAplicado);
	}

	public BigDecimal getAnticipoAplicadoImporte() {
		return anticipoAplicadoImporte;
	}

	public void setAnticipoAplicadoImporte(BigDecimal anticipoAplicadoImporte) {
		Object old=this.anticipoAplicadoImporte;
		this.anticipoAplicadoImporte = anticipoAplicadoImporte;
		firePropertyChange("anticipoAplicadoImporte", old, anticipoAplicadoImporte);
	}
	
	
	*/

	public boolean isEspecial() {
		return especial;
	}

	public void setEspecial(boolean especial) {
		this.especial = especial;
	}



	public CheckPlusOpcion getCheckplusOpcion() {
		return checkplusOpcion;
	}
	public void setCheckplusOpcion(CheckPlusOpcion checkplusOpcion) {
		Object old=this.checkplusOpcion;
		this.checkplusOpcion = checkplusOpcion;
		firePropertyChange("checkplusOpcion", old,checkplusOpcion);
	}
	
	
	public Date getTpuesto() {
		return tpuesto;
	}
	public void setTpuesto(Date tpuesto) {
		Object old=this.tpuesto;
		this.tpuesto = tpuesto;
		firePropertyChange("tpuesto", old,tpuesto);
	}
	
	
	public boolean isPuesto() {
		return puesto;
	}

	public void setPuesto(boolean puesto) {
		boolean old=this.puesto;
		this.puesto = puesto;
		if(puesto){
			setTpuesto(new Date());
		}else{
			setTpuesto(null);
		}
		firePropertyChange("puesto", old, puesto);
	}

	
	
	
	
}
