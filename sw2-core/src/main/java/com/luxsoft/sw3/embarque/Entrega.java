package com.luxsoft.sw3.embarque;

import java.math.BigDecimal;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.Version;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.apache.commons.lang.time.DateUtils;
import org.apache.poi.hssf.record.formula.functions.Floor;
import org.apache.poi.hssf.record.formula.functions.Round;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
import org.hibernate.annotations.Type;
import org.hibernate.validator.Length;
import org.hibernate.validator.Max;
import org.hibernate.validator.NotNull;


import com.luxsoft.siipap.cxc.model.OrigenDeOperacion;
import com.luxsoft.siipap.model.BaseBean;
import com.luxsoft.siipap.model.CantidadMonetaria;
import com.luxsoft.siipap.model.UserLog;
import com.luxsoft.siipap.model.core.Cliente;
import com.luxsoft.siipap.ventas.model.Venta;
import com.luxsoft.siipap.ventas.model.VentaDet;
import com.luxsoft.sw3.model.AdressLog;
import com.luxsoft.sw3.replica.Replicable;
import com.luxsoft.sw3.ventas.InstruccionDeEntrega;

/**
 * Seccion del embarque correspondiente a una factura
 * 
 * Normalmente es por factura
 * 
 * 
 * TODO Eliminar el tipo y usar un booleano para parcial
 *  Quitar Zona,Prioridad
 * 
 * @author Ruben Cancino Ramos
 * 
 */
@Entity
@Table(name = "SX_ENTREGAS")
@GenericGenerator(name="hibernate-uuid",strategy="uuid"
		,parameters={
				@Parameter(name="separator",value="-")
			}
		)

public class Entrega extends BaseBean implements Replicable{

	
	
	@Id @GeneratedValue(generator="hibernate-uuid")
	@Column(name="ENTREGA_ID")
	private String id;

	@SuppressWarnings("unused")
	@Version
	private int version;
	
	

	@ManyToOne(optional = false)
	@JoinColumn(name = "EMBARQUE_ID", nullable = false, updatable = false)
	private Embarque embarque;

	
	@ManyToOne(optional = false)
	@JoinColumn(name = "VENTA_ID", nullable = false)
	private Venta factura;
	
	@Column(name="ORIGEN",nullable=false,length=3)
	private String origen;
	
	@Column(name="PARCIAL",nullable=false)
	private boolean parcial=false;
	
	@Column(name="DOCUMENTO",nullable=false)
	private Long documento;
	
	@Column(name="FISCAL",nullable=false)
	private int numeroFiscal;
	
	@Column(name = "FECHA_DOCTO", nullable = false)
	@Type(type = "date")
	private Date fechaFactura ;
	
	@Column(name="TOTAL_DOCTO",nullable=false)
	private BigDecimal totalFactura;

	@ManyToOne(optional = false, fetch = FetchType.LAZY)
	@JoinColumn(name = "CLIENTE_ID", nullable = false, updatable = true)
	@NotNull(message = "El cliente es mandatorio")
	private Cliente cliente;

	@Column(name = "NOMBRE", nullable = false)
	private String nombre;

	@Column(name = "CLAVE", length = 7, nullable = false)
	private String clave;

	/**
	 * Persona del cliente que recibio el embarque
	 * 
	 */
	@Column(name = "RECIBIO")
	private String recibio;
	/**
	 * Numero de que constituyen el embarque Capturado por el controlista
	 */
	@Column(name = "PAQUETES", nullable = false)
	private int paquetes = 0;

	@Column(name = "KILOS", nullable = false)
	private double kilos = 0;
	
	@Column(name = "CANTIDAD", nullable = false)
	private double cantidad = 0;

	/**
	 * Fecha y hora en que llega al destino
	 * 
	 * Registrado por el chofer
	 * 
	 */
	@Column(name = "ARRIBO")
	private Date arribo;

	/**
	 * Fecha y hora en que el cliente recibe su pedido registrado por el
	 * representante del cliente, la persona que firma la entrega
	 */
	@Column(name = "RECEPCION", nullable = true)
	private Date recepcion;

	@Column(name = "COMENTARIO")
	@Length(max = 255, message = "El tamaño maximo del comentario es de 255 caracteres")
	private String comentario;

	@Column(name = "VALOR", nullable = false)
	private BigDecimal valor;

	@Column(name = "POR_COBRAR", nullable = false)
	private BigDecimal porCobrar=BigDecimal.ZERO;
	
	@Column(name = "COMISION", nullable = false)
	private double comision=1.1;
	
	@Column(name = "COMISION_IMP", nullable = false)
	private BigDecimal importeComision=BigDecimal.ZERO;
	
	@Column(name="COMISION_FECHA",nullable=true)
	private Date fechaComision;
	
	@Column(name="COMISION_COMENTARIO",nullable=true)
	private String comentarioComision;
	
	@Column(name="COMISION_POR_TON",nullable=false)
	private double comisionPorTonelada;
	
	
	@OneToOne(optional=true,fetch=FetchType.LAZY,cascade={
			CascadeType.MERGE
			,CascadeType.PERSIST			
			}
	)
	@JoinColumn(name = "INSTRUCCION_ID",nullable=true)	
	private InstruccionDeEntrega instruccionDeEntrega;

	
	@OneToMany(cascade={
			 CascadeType.PERSIST
			,CascadeType.MERGE
			,CascadeType.REMOVE
			}
			,fetch=FetchType.EAGER,mappedBy="entrega")
	@Cascade(value={org.hibernate.annotations.CascadeType.DELETE_ORPHAN
			,org.hibernate.annotations.CascadeType.REPLICATE
	})
	private Set<EntregaDet> partidas = new HashSet<EntregaDet>();
	
	@Column(name="TX_IMPORTADO",nullable=true)
	private Date importado;
	
	@Column(name="TX_REPLICADO",nullable=true)
	private Date replicado;
	
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
	
	/**
	 * Fecha y Hora en que esta listo el pedido para su envio
	 * Debe ser capturado en un futuro por persona no de camionetas
	 */
	@Column(name = "SURTIDO")
	private Date surtido=new Date();
	
	@Transient
	private int atrasoEnCobranza;

	public String getId() {
		return id;
	}

	public Embarque getEmbarque() {
		return embarque;
	}

	public void setEmbarque(Embarque embarque) {
		this.embarque = embarque;
	}

	public Venta getFactura() {
		return factura;
	}

	public void setFactura(Venta factura) {
		this.factura = factura;
		if (this.factura != null) {
			setCliente(factura.getCliente());
			this.documento=factura.getDocumento();
			this.numeroFiscal=factura.getNumeroFiscal();
			this.fechaFactura=factura.getFecha();
			this.totalFactura=factura.getTotal();
			this.origen=factura.getOrigen().name();
			setSurtidor(factura.getSurtidor());
		}
	}

	public Cliente getCliente() {
		return cliente;
	}

	public void setCliente(Cliente cliente) {
		Object old = this.cliente;
		this.cliente = cliente;
		firePropertyChange("cliente", old, cliente);
		if (cliente != null) {
			setClave(cliente.getClave());
			setNombre(cliente.getNombreRazon());
		} else {
			setClave(null);
			setNombre(null);
		}
	}

	public String getNombre() {
		return nombre;
	}

	public void setNombre(String nombre) {
		this.nombre = nombre;
	}

	public String getClave() {
		return clave;
	}

	public void setClave(String clave) {
		this.clave = clave;
	}
	
	

	public String getOrigen() {
		return origen;
	}

	public void setOrigen(String origen) {
		this.origen = origen;
	}

	public Long getDocumento() {
		return documento;
	}

	public void setDocumento(Long documento) {
		this.documento = documento;
	}

	public int getNumeroFiscal() {
		return numeroFiscal;
	}

	public void setNumeroFiscal(int numeroFiscal) {
		this.numeroFiscal = numeroFiscal;
	}

	public Date getFechaFactura() {
		return fechaFactura;
	}

	public void setFechaFactura(Date fechaFactura) {
		this.fechaFactura = fechaFactura;
	}

	public BigDecimal getTotalFactura() {
		return totalFactura;
	}

	public void setTotalFactura(BigDecimal totalFactura) {
		this.totalFactura = totalFactura;
	}

	public String getRecibio() {
		return recibio;
	}

	public void setRecibio(String recibio) {
		this.recibio = recibio;
	}

	/*public Zona getZona() {
		return zona;
	}

	public void setZona(Zona zona) {
		Object old=this.zona;
		this.zona = zona;
		firePropertyChange("zona", old, zona);
	}*/

	/*public Prioridad getPrioridad() {
		return prioridad;
	}

	public void setPrioridad(Prioridad prioridad) {
		Object old=this.prioridad;
		this.prioridad = prioridad;
		firePropertyChange("prioridad", old, prioridad);
	}*/

	public int getPaquetes() {
		return paquetes;
	}

	public void setPaquetes(int paquetes) {
		int old=this.paquetes;
		this.paquetes = paquetes;
		firePropertyChange("paquetes", old, paquetes);
	}	

	public Date getArribo() {
		return arribo;
	}

	public void setArribo(Date arribo) {
		this.arribo = arribo;
	}

	public Date getRecepcion() {
		return recepcion;
	}

	public void setRecepcion(Date recepcion) {
		this.recepcion = recepcion;
	}

	public String getComentario() {
		return comentario;
	}

	public void setComentario(String comentario) {
		this.comentario = comentario;
	}

	public BigDecimal getValor() {
		return valor;
	}

	public void setValor(BigDecimal valor) {
		this.valor = valor;
	}

	public BigDecimal getPorCobrar() {
		return porCobrar;
	}

	public void setPorCobrar(BigDecimal porCobrar) {
		this.porCobrar = porCobrar;
	}

	public double getKilos() {
		return kilos;
	}

	public void setKilos(double kilos) {
		this.kilos = kilos;
	}
/*
	public Tipo getTipo() {
		return tipo;
	}
	*/
	

	public double getCantidad() {
		return cantidad;
	}

	public void setCantidad(double cantidad) {
		this.cantidad = cantidad;
	}
/*
	public void setTipo(Tipo tipo) {
		Object old=this.tipo;
		this.tipo = tipo;
		firePropertyChange("tipo", old, tipo);
	}*/
	
	
	public double getComision() {		
		return comision;
	}

	public void setComision(double comision) {
		this.comision = comision;
	}
	
	

	public BigDecimal getImporteComision() {
		return importeComision;
	}

	public void setImporteComision(BigDecimal importeComision) {
		this.importeComision = importeComision;
	}

	public Date getFechaComision() {
		return fechaComision;
	}

	public void setFechaComision(Date fechaComision) {
		Object old=this.fechaComision;
		this.fechaComision = fechaComision;
		firePropertyChange("fechaComision", old, fechaComision);
	}

	public String getComentarioComision() {
		return comentarioComision;
	}

	public void setComentarioComision(String comentarioComision) {
		Object old=this.comentarioComision;
		this.comentarioComision = comentarioComision;
		firePropertyChange("comentarioComision", old, comentarioComision);
	}
	
	

	public double getComisionPorTonelada() {
		return comisionPorTonelada;
	}

	public void setComisionPorTonelada(double comisionPorTonelada) {
		this.comisionPorTonelada = comisionPorTonelada;
	}

	public InstruccionDeEntrega getInstruccionDeEntrega() {
		return instruccionDeEntrega;
	}

	public void setInstruccionDeEntrega(InstruccionDeEntrega instruccionDeEntrega) {
		Object old=this.instruccionDeEntrega;
		this.instruccionDeEntrega = instruccionDeEntrega;
		firePropertyChange("instruccionDeEntrega", old, instruccionDeEntrega);
	}

	public Date getSurtido() {
		return surtido;
	}

	public void setSurtido(Date surtido) {
		Object old=this.surtido;
		this.surtido = surtido;
		firePropertyChange("surtido", old, surtido);
	}
	
	

	public boolean isParcial() {
		return parcial;
	}

	public void setParcial(boolean parcial) {
		boolean old=this.parcial;
		this.parcial = parcial;
		firePropertyChange("parcial", old, parcial);
	}

	/**
	 * Actualiza los importes de la unidad
	 */
	public void actualziarValor() {
		if(getFactura()==null){
			setValor(BigDecimal.ZERO);
			return;
		}else{
			if(!isParcial()){
				BigDecimal imp=getFactura().getImporteBruto().subtract(getFactura().getImporteDescuento());
				//BigDecimal imp=getFactura().getSubTotal2();
				setValor(imp);
			}else{
				BigDecimal total=BigDecimal.ZERO;				
				for(EntregaDet det:getPartidas()){
					det.actualizar();
					total=total.add(det.getValor());
				}
				setValor(total);
			}
		}
	}
	
	public void actualizarKilosCantidad(){
		if(getFactura()!=null){
			if(!isParcial()){				
				double kilos = 0;
				double cantidad=0;
				for (VentaDet det : getFactura().getPartidas()) {
					kilos = kilos + det.getKilos();
					cantidad=cantidad+det.getCantidadEnUnidad();
				}
				setKilos(kilos);
				setCantidad(Math.abs(cantidad));
			}else{
				double kilos = 0;
				double cantidad=0;
				for (EntregaDet det : partidas) {
					double factor=det.getVentaDet().getFactor();
					double cantiUni=det.getCantidad()/factor;
					double kg=det.getProducto().getKilos();
					kilos = kilos+(cantiUni*kg);
					cantidad+=cantiUni;
				}
				setKilos(kilos);
				setCantidad(Math.abs(cantidad));
			}
		}
	}
	
	public void actualizarComision(){
		
		CantidadMonetaria imp=CantidadMonetaria.pesos(getValor());
		
		// Caso Union de Credito
		/*if(getCliente().getClave().equals("U050008")){
			imp=imp.multiply(.747);
			setValor(imp.amount());
			
			CantidadMonetaria comi=imp.multiply(getComision()/100);
			setImporteComision(comi.amount());
			return;
		}
		*/
		// Caso Clientes especiales
		if(getComisionPorTonelada()>0){
			double toneladas=getKilos()/1000;
			imp=CantidadMonetaria.pesos(getComisionPorTonelada());
			setImporteComision(imp.multiply(toneladas).amount());
			return;
		}
		BigDecimal importeBase=getFactura().getImporteBruto().subtract(getFactura().getImporteDescuento()); 
		//Caso  Sucursal Bolivar		
		if(getFactura().getSucursal().getId()==5L && !getFactura().getOrigen().equals(OrigenDeOperacion.CRE)){
			if(importeBase.doubleValue()<20000.00){
				if(!isParcial()){
					setComision(1.3);
				}				
			}
		}
		double com=getComision()/100;
		CantidadMonetaria impCom=imp.multiply(com);
		setImporteComision(impCom.amount());		
		
			
	}

	public Set<EntregaDet> getPartidas() {
		return partidas;
	}
	
	public boolean agregarEntregaUnitaria(EntregaDet det){
		det.setEntrega(this);
		return partidas.add(det);
	}
	
	public boolean eliminarEntregaUnitaria(EntregaDet det){
		det.setEntrega(null);
		return partidas.remove(det);
	}

	
	
	@Transient
	private Long oldId;

	public Long getOldId() {
		return oldId;
	}

	public void setOldId(Long oldId) {
		this.oldId = oldId;
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

	public UserLog getLog() {
		if(log==null)
			log=new UserLog();
		return log;
	}

	public void setLog(UserLog log) {
		this.log = log;
	}

	public AdressLog getAddresLog() {
		if(addresLog==null)
			addresLog=new AdressLog();
		return addresLog;
	}

	public void setAddresLog(AdressLog addresLog) {
		this.addresLog = addresLog;
	}
	
	
	
	

	public int getAtrasoEnCobranza() {
		return atrasoEnCobranza;
	}

	public void setAtrasoEnCobranza(int atrasoEnCobranza) {
		this.atrasoEnCobranza = atrasoEnCobranza;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((factura == null) ? 0 : factura.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Entrega other = (Entrega) obj;
		if (factura == null) {
			if (other.factura != null)
				return false;
		} else if (!factura.equals(other.factura))
			return false;
		return true;
	}

	public String toString() {
		return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
				.append(nombre).append(factura).toString();
	}

	@Column(name="SURTIDOR",length=255)
	@Length(max=255)
	private String surtidor;

	public String getSurtidor() {
		return surtidor;
	}

	public void setSurtidor(String surtidor) {
		Object old=this.surtidor;
		this.surtidor = surtidor;
		firePropertyChange("surtidor", old, surtidor);
	}

	public int getRetraso(){
		if(getEmbarque().getRegreso()==null){
			return 0;
		}
		Date pedido=getFactura().getPedidoCreado();
		Date regreso=getEmbarque().getRegreso();
		if(regreso==null){
			regreso=new Date(System.currentTimeMillis());
		}
		long mils=regreso.getTime()-pedido.getTime();
		int horas=(int)(mils/(60*60*1000));
		return horas;

	}
	
	
	public String getRetrasoCalculado(){
		Date pedido=getFactura().getPedidoCreado();
		if(pedido==null)
			pedido=getFactura().getLog().getCreado();
		Date regreso=getEmbarque().getRegreso();
		if(regreso==null){
			regreso=new Date(System.currentTimeMillis());
		}
		long mils=regreso.getTime()-pedido.getTime();
		double minutos=(int)(mils/(60*1000));
		int hrs=(int) Math.floor(minutos/60);
		double min=minutos-(hrs*60);
				
		String res=hrs+":"+min;
		return res;

	}
	
}
