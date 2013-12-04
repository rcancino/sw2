package com.luxsoft.siipap.cxc.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Currency;
import java.util.Date;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.DiscriminatorType;
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
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Version;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.Formula;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
import org.hibernate.annotations.Type;
import org.hibernate.validator.Length;
import org.hibernate.validator.NotNull;

import com.luxsoft.siipap.model.CantidadMonetaria;
import com.luxsoft.siipap.model.Sucursal;
import com.luxsoft.siipap.model.UserLog;
import com.luxsoft.siipap.model.core.Cliente;
import com.luxsoft.siipap.util.DateUtil;
import com.luxsoft.siipap.util.MonedasUtils;
import com.luxsoft.siipap.ventas.model.Cobrador;
import com.luxsoft.sw3.replica.Replicable;


/**
 * Cargo a la cuenta de un cliente
 * 
 * @author Ruben Cancino
 *
 */
@Entity
@Table(name="SX_VENTAS"
	//,uniqueConstraints=@UniqueConstraint(columnNames={"SUCURSAL_ID","TIPO","DOCTO","ORIGEN"})
	)
@Inheritance(strategy=InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name="TIPO",discriminatorType=DiscriminatorType.STRING)
@GenericGenerator(name="hibernate-uuid",strategy="uuid"
		,parameters={
				@Parameter(name="separator",value="-")
			}
		)
public abstract class Cargo implements Replicable,Serializable{
	
	static final long serialVersionUID = 82L;

	
	
	@Id @GeneratedValue(generator="hibernate-uuid")
	@Column(name="CARGO_ID")
	protected String id;
	
	@Version
	private int version;
	
	@ManyToOne(optional = false, cascade = { 
			//CascadeType.MERGE,
			//CascadeType.PERSIST 
			}
			,fetch=FetchType.EAGER)			
	@JoinColumn(name = "CLIENTE_ID", nullable = false, updatable = true)
	@NotNull(message="El cliente es mandatorio")
	//@Cascade(value={org.hibernate.annotations.CascadeType.REPLICATE})
	private Cliente cliente;
	
	@ManyToOne(optional = false,fetch=FetchType.EAGER)
	@JoinColumn(name = "SUCURSAL_ID", nullable = false, updatable = false)
	private Sucursal sucursal;
	
	@Column(name = "NOMBRE", nullable = false)
	private String nombre;
	
	@Column(name = "CLAVE", length = 7)
	private String clave;
	
	@Column(name = "FECHA", nullable = false)
	@Type(type = "date")
	private Date fecha = new Date();
	
	@Column(name="VTO",nullable=false)
    @Type(type="date")
    private Date vencimiento=new Date();
	
	@Column(name = "MONEDA", nullable = false)
	private Currency moneda = MonedasUtils.PESOS;
	
	@Column(name = "TC", nullable = false)
	private double tc = 1;
	
	@Column(name = "DOCTO")
	private Long documento;
	
	
	@Column(name = "IMPORTE", nullable = false)
	private BigDecimal importe = BigDecimal.ZERO;
	
	@Column(name = "IMPUESTO", nullable = false)
	private BigDecimal impuesto = BigDecimal.ZERO;
	
	@Column(name = "TOTAL", nullable = false)
	private BigDecimal total = BigDecimal.ZERO;
	
	
	@Column(name = "SALDO", nullable = false)
	private BigDecimal saldo = BigDecimal.ZERO;
	
	
	@Enumerated(EnumType.STRING)
	@Column(name = "ORIGEN", nullable = false, length = 3)
	private OrigenDeOperacion origen;
	
	@Column(name = "CARGOS", nullable = false)
	private BigDecimal cargos = BigDecimal.ZERO;
	
	@Column(name = "DESCRI_CARGO", length = 100)
	private String descCargo;
	
	@ManyToOne(optional = true,fetch=FetchType.LAZY)			
	@JoinColumn(name = "COBRADOR_ID",nullable=true)
	private Cobrador cobrador;
	
	/**
	 * Descuento asignado al cargo
	 * 
	 */
	@Column(name = "DESCUENTO", nullable=false,columnDefinition=" double precision  default 0")
	private double descuentoGeneral=0;
	
	@Formula("(" +
			"select ifnull(sum(x.importe),0) from sx_cxc_aplicaciones  x " +
			" where x.ABONO_ID in(select y.ABONO_ID from sx_cxc_abonos y where y.tipo_id like \'PAGO%\' ) " +
			"   and x.cargo_id=CARGO_ID"+
			")")
	private BigDecimal pagos=BigDecimal.ZERO;
	
	//@Column(name = "DESCUENTOS", nullable = false)
	@Formula("(" +
			"select ifnull(sum(x.importe),0) from sx_cxc_aplicaciones  x where x.ABONO_ID in(select y.ABONO_ID from sx_cxc_abonos y where y.tipo_id=\'NOTA_DES\' ) and x.cargo_id=CARGO_ID"+
			")")
	private BigDecimal descuentos = BigDecimal.ZERO;
	
	//@Column(name = "BONIFIC", nullable = false)
	@Formula("(" +
			"select ifnull(sum(x.importe),0) from sx_cxc_aplicaciones  x where x.ABONO_ID in(select y.ABONO_ID from sx_cxc_abonos y where y.tipo_id=\'NOTA_BON\' ) and x.cargo_id=CARGO_ID"+
			")")
	private BigDecimal bonificaciones= BigDecimal.ZERO;
	
	@Formula("(select ifnull(sum(X.IMPORTE*1.16),0) FROM SX_NOTADECARGO_DET X where X.VENTA_ID=CARGO_ID)")	
	private BigDecimal cargosAplicados=BigDecimal.ZERO;
	
	@Column(name="FECHA_REVISION",nullable=true)
    @Type(type="date")    
	private Date fechaRevision;
	
    @Column(name="FECHA_REVISION_CXC",nullable=true)
    @Type(type="date")
	private Date fechaRevisionCxc;
	
	
    @Column(name="COMENTARIO")
	@Length(max=255, message="El tamaño maximo del comentario es de 255 caracteres")	
	private String comentario;
    
    @Column(name="COMENTARIO2",length=70)
    private String comentario2;
	
	@Column(name="PLAZO",nullable=false)
	@NotNull
	private int plazo=0;
	
	@Column(name="REVISION",nullable=false)
	@NotNull
	private boolean revision=false;
	
	@Column(name="POST_FECHADO",nullable=false)
	@NotNull
	private boolean postFechado=false;
	
	@Column(name="PRECIO_BRUTO",nullable=false)
	@NotNull
	private boolean precioBruto=false;
	
	
	@Column(name="FECHA_RECEPCION_CXC",nullable=true)
	@Type(type="date")
	private Date fechaRecepcionCXC;
	
	@Column(name="REVISADA",nullable=false)
	@NotNull
	private boolean revisada=false;
	
	@Column(name="DIA_PAGO",nullable=true)
    @Type(type="date")    
	private Date diaPago;
	
	@Column(name="DIA_DEL_PAGO",nullable=false)
	private int diaDelPago=0;
	
	@Column(name="DIA_DE_REV",nullable=false)
	private int diaRevision=0;
	
	@Column(name="REPROGRAMAR_PAGO",nullable=true)
    @Type(type="date")    
	private Date reprogramarPago;
	
	/**
	 * Comentario de los cobradores como resultado de
	 *  la revision/cobro de la factura
	 * 
	 */
	@Column(name="COMENTARIO_REP_PAGO")
	@Length(max=255, message="El tamaño maximo del comentario es de 255 caracteres")
	private String comentarioRepPago;
	
	@Column(name="NFISCAL")	
	private Integer numeroFiscal=0;
	
	//@Column(name="CARGO",nullable=false)
	//@NotNull
	//private double cargo=0.0d;	
	

	@Column(name="IMPRESO",nullable=true)	
	private Date impreso;
	
	@OneToOne(mappedBy="cargo"
		//,cascade={CascadeType.MERGE,CascadeType.PERSIST},fetch=FetchType.LAZY
		)
		@Cascade(value={org.hibernate.annotations.CascadeType.REPLICATE})
	private CancelacionDeCargo cancelacion;
	
	@Formula("(select ifnull(sum(X.IMPORTE),0) FROM SX_CXC_APLICACIONES X where X.CARGO_ID=CARGO_ID)")
	//@Transient
	private BigDecimal aplicado=BigDecimal.ZERO;
	
	@Formula("(select ifnull(sum((X.DSCTO_NOTA/100)*x.importe),0) FROM SX_VENTASDET X where X.VENTA_ID=CARGO_ID)")
	//@Transient
	private BigDecimal provisionable=BigDecimal.ZERO;
	
	//@Column(name = "DEVOLUCIONES", nullable = false)
	@Formula("(" +
			"select ifnull(sum(x.importe),0) from sx_cxc_aplicaciones  x where x.ABONO_ID in(select y.ABONO_ID from sx_cxc_abonos y where y.tipo_id=\'NOTA_DEV\' ) and x.cargo_id=CARGO_ID"+
			")")
	private BigDecimal devoluciones = BigDecimal.ZERO;	
	
	
	@OneToMany(mappedBy="cargo",fetch=FetchType.LAZY)
	private List<Aplicacion> aplicaciones=new ArrayList<Aplicacion>();
	
	@OneToOne(optional=true, mappedBy="cargo")	
	private Juridico juridico;
	
	private UserLog log=new UserLog();
	
	@Column(name="TX_IMPORTADO",nullable=true)
	private Date importado;
	
	@Column(name="TX_REPLICADO",nullable=true)
	private Date replicado;

	public Cargo() {
		super();
	}
	
	public Cargo(Cliente cliente) {
        this.cliente = cliente;
        this.clave=cliente.getClave();
        this.nombre=cliente.getNombreRazon();
    }
	
	
    
    public void setId(String id) {
		this.id = id;
	}

	/**
     * Identificador para JPA/Hibernate
     * 
     * @return
     */    
    public String getId() {
        return id;
    }  
    
    public void registrarId(String id){
    	this.id=id;
    }
    
    /**
     * Control de versiones para JPA/Hibernate
     * @return
     */    
    public int getVersion() {
        return version;
    }

	/**
     * Cliente objeto de la venta. 
     * Inmutable
     * 
     * @return
     */    
    public Cliente getCliente() {
        return cliente;
    }
    public void setCliente(Cliente cliente) {
        this.cliente = cliente;
        this.clave=cliente.getClave();
        this.nombre=cliente.getNombre();
    }

    /**
     * Nombre del cliente al momento de la venta. Por razones fiscales
     * esta campo se congela a pesar de que se tiene la referencia del cliente
     * 
     * @return
     */    
    public String getNombre() {
        return nombre;
    }
    public void setNombre(String nombre) {
        this.nombre = nombre;
    }
    
   

	/**
     * Fecha de la realización de la venta. Tal y como se generara en la factura
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
	 * 
	 * @return
	 */
	public Date getVencimiento() {
		return vencimiento;
	}

	public void setVencimiento(Date vencimiento) {
		this.vencimiento = vencimiento;
	}

	public Currency getMoneda() {
        return moneda;
    }
    public void setMoneda(Currency moneda) {
        this.moneda = moneda;
    }

    /**
     * Tipo de cambio del dia de la operacion
     * 
     * @return
     */
    public double getTc() {
		return tc;
	}
	public void setTc(double tc) {
		this.tc = tc;
	}

	

    public BigDecimal getImporte() {
        return importe;
    }
    public void setImporte(BigDecimal importe) {
        this.importe = importe;
    }
    
    public BigDecimal getImpuesto() {
        return impuesto;
    }
    public void setImpuesto(BigDecimal impuesto) {
        this.impuesto = impuesto;
    }
    
    public BigDecimal getTotal() {
        return total;
    }
    public void setTotal(BigDecimal total) {
        this.total = total;
    }
    

    /**
     * Regresa el saldo de la venta
     * @return
     */
    public BigDecimal getSaldo() {    	
		return saldo;
	}
	public void setSaldo(BigDecimal saldo) {
		this.saldo = saldo;
	}
	
	
	

	public abstract String getTipoDocto();

	
	

	/**
	 * Numero de factura 
	 * 
	 * @return
	 */
	public Long getDocumento() {
        return documento;
    }
	
	public void setDocumento(Long factura) {
        this.documento = factura;
    }
    
    public OrigenDeOperacion getOrigen() {
        return origen;
    }
    public void setOrigen(OrigenDeOperacion origen) {
        this.origen = origen;
    }

    public Sucursal getSucursal() {
        return sucursal;
    }
    public void setSucursal(Sucursal sucursal) {
        this.sucursal = sucursal;
    }     
    
    public BigDecimal getCargos() {
    	if(cargos==null)
    		return BigDecimal.ZERO;
		return cargos;
	}

	public void setCargos(BigDecimal cargos) {
		this.cargos = cargos;
	}

	public String getDescCargo() {
		return descCargo;
	}

	public void setDescCargo(String descCargo) {
		this.descCargo = descCargo;
	}
	
	public BigDecimal getCargosAplicados(){
		if(cargosAplicados==null){
			cargosAplicados=BigDecimal.ZERO;
		}
		return cargosAplicados;
	}
	
	public double getCargosPorAplicar(){
		double pena=1.0;
		if(getFecha().getTime()<DateUtil.toDate("01/04/2009").getTime())
			pena=0.5;
		int atraso=getAtrasoReal();
		if(atraso<=0 || (getCargosAplicados().doubleValue()>0))
			return 0;
		int semanas=atraso/7;
		return ((double)semanas)*pena;
	}
	
	public CantidadMonetaria getCargosImpPorAplicar(){
		double car=getCargosPorAplicar()/100;
		CantidadMonetaria imp=new CantidadMonetaria(getTotal().subtract(getDevoluciones()),getMoneda());
		return imp.multiply(car);
	}
	
	public String getClave() {
		return clave;
	}

	public void setClave(String clave) {
		this.clave = clave;
	}
	

	

	public UserLog getLog() {
		if(log==null)
			log=new UserLog();
        return log;
    }
    public void setLog(UserLog log) {
        this.log = log;
    }
        

	
	@Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if(obj==this){
        	return true;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Cargo other = (Cargo) obj;
        return new EqualsBuilder()
        .append(sucursal,other.getSucursal())
        .append(origen, other.getOrigen())
        .append(documento, other.getDocumento())
        .append(getTipoDocto(), other.getTipoDocto())
        .isEquals();
        
    }

    @Override
    public int hashCode() {
       return new HashCodeBuilder(17,35)
       .append(sucursal)
       .append(origen)
       .append(documento)
       .append(getTipoDocto())
       .toHashCode();
    }    

    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.SIMPLE_STYLE)
        .append(clave)
        .append(nombre)        
        .append(origen)        
        .append(documento)
        .append(fecha)
        .append(importe)
        .append(impuesto)
        .append(total)
        .toString();        
    }

	public Cobrador getCobrador() {
		return cobrador;
	}

	public void setCobrador(Cobrador cobrador) {
		this.cobrador = cobrador;
	}
  
	public double getDescuento(){
		return 0;
	}
	
	public double getDescuentoNota(){
		return 0;
	}
	
	/**
	 * Regresa la provision historica para descuentos de la cuenta por pagar
	 * 
	 * @return
	 */
	public BigDecimal getProvision(){
		return BigDecimal.ZERO;
	}

	public BigDecimal getDevoluciones() {
		if(devoluciones==null)
			devoluciones=BigDecimal.ZERO;
		return devoluciones;
	}

	public void setDevoluciones(BigDecimal devo) {
		this.devoluciones = devo;
	}

	public BigDecimal getDescuentos() {
		return descuentos;
	}

	public void setDescuentos(BigDecimal desc) {
		this.descuentos = desc;
	}

	public BigDecimal getBonificaciones() {
		return bonificaciones;
	}

	public void setBonificaciones(BigDecimal bonific) {
		this.bonificaciones = bonific;
	}

	/**
	 * Descuento asignado al cargo. Normalmente es el descuento
	 * pactado segun las reglas de negocios
	 * 
	 * @return
	 */
	public double getDescuentoGeneral() {
		return descuentoGeneral;
	}

	public void setDescuentoGeneral(double descuentoGeneral) {
		this.descuentoGeneral = descuentoGeneral;
	}
    

	public int getAtraso(){
		Date today=new Date();		
		long res=today.getTime()-getVencimiento().getTime();
		if(res>0){				
			long dias=(res/(86400*1000));			
			return (int)dias;
		}else{
			return 0;
		}
	}
	
	public int getAtrasoReal(){
		Date today=new Date();		
		long res=today.getTime()-getFecha().getTime();
		if(res>0){			
			long dias=(res/(86400*1000));			
			return ((int)dias-getPlazo());
		}else{
			return 0;
		}
	}
	
	/*** Cuentas a credito ***/
	
	/**
	 * Numero real de la factura, es administrado por el departamento
	 * de Cargo
	 * (SOLO UTIL PARA VENTAS MIGRADAS DE SIIPAP)
	 * 
	 * @return
	 */
	public Integer getNumeroFiscal() {
		return numeroFiscal;
	}

	public void setNumeroFiscal(Integer numeroFiscal) {
		this.numeroFiscal = numeroFiscal;
	}

	
	
	/**
	 * Indica; si al momento de la venta; el cliente tiene autorizado
	 * una fecha de revisión programada y el plazo corre a partir de 
	 * la recepción del documento por el cliente. 
	 * 
	 * Nota. Este es un atributo delicado y no recomendable ya que puede
	 * prolongar de manera indefinida el pago del documento
	 * 
	 * @return
	 */
	public boolean isRevision() {
		return revision;
	}

	public void setRevision(boolean revision) {
		this.revision = revision;
	}

	/**
	 * Es la fecha CALCULADA POR EL SISTEMA 
	 * en que se debe mandar a revision con el cliente
	 * para su programación de pago
	 * 
	 * Solo es significativa para algunos clientes
	 * 
	 * @return
	 */
	public Date getFechaRevision() {
		return fechaRevision;
	}

	public void setFechaRevision(Date fechaRevision) {
		this.fechaRevision = fechaRevision;
	}

	/**
	 * Es la fecha en que se debe mandar a revision con el cliente
	 * para su programación de pago. ADMINSITRADA POR EL DEPARTAMENTO
	 * DE Cargo
	 * 
	 * Solo es significativa para algunos clientes
	 * 
	 * @return
	 */
	public Date getFechaRevisionCxc() {
		return fechaRevisionCxc;
	}

	public void setFechaRevisionCxc(Date fechaRevisionCxc) {
		this.fechaRevisionCxc = fechaRevisionCxc;
	}

	/**
	 * Comentario generico administrado por el departamento de Cargo
	 * 
	 * @return
	 */
	public String getComentario() {
		return comentario;
	}

	public void setComentario(String comentario) {
		this.comentario = comentario;
	}
	
	 public String getComentario2() {
			return comentario2;
	}

	public void setComentario2(String comentario2) {
			this.comentario2 = comentario2;
	}

	public int getPlazo() {
		return plazo;
	}

	public void setPlazo(int plazo) {
		this.plazo = plazo;
	}

	
	/**
	 * Fecha en que se recibio la factura por parte del depto de Cargo
	 * Esta fecha es administrada por el depto de Cargo
	 * 
	 * @return
	 */
	public Date getFechaRecepcionCXC() {
		return fechaRecepcionCXC;
	}

	public void setFechaRecepcionCXC(Date fechaRecepcionCXC) {
		this.fechaRecepcionCXC = fechaRecepcionCXC;
	}
	
	public boolean isRecibidaCXC() {
		return fechaRecepcionCXC!=null;
	}

	/**
	 * Control interno del depto de Cargo. Permite controlar
	 * el envio/aceptacion de facturas a revisión
	 * 
	 * @return
	 */
	public boolean isRevisada() {
		return revisada;
	}
	
	public void setRevisada(boolean revisada) {
		this.revisada = revisada;
	}

	/**
	 * Fecha original para mandar a cobro (Calculada por el sistema al momento de generar/importar) la cuenta
	 *  
	 * @return
	 */
	public Date getDiaPago() {
		return diaPago;
	}

	public void setDiaPago(Date diaPago) {
		this.diaPago = diaPago;
	}

	/**
	 * Fecha de pago tentativa calculada por el sistema como 
	 * consecuencia de la falta de pago en la fecha
	 * de programación de pago original
	 * 
	 * @return
	 */
	public Date getReprogramarPago() {
		return reprogramarPago;
	}

	public void setReprogramarPago(Date reprogramarPago) {
		this.reprogramarPago = reprogramarPago;
	}

	public String getComentarioRepPago() {
		return comentarioRepPago;
	}

	public void setComentarioRepPago(String comentarioRepPago) {
		this.comentarioRepPago = comentarioRepPago;
	}

	/**
	 * Indica si esta operación ha o sera pagada con un cheque posfechado
	 * 
	 * @return
	 */
	public boolean isPostFechado() {
		return postFechado;
	}

	public void setPostFechado(boolean postFechado) {
		this.postFechado = postFechado;
	}
	

	/**
	 * Cargo aplicable a la venta en funcion de las reglas de negocios
	 * vigentes al momento de la venta o el pago
	 * 
	 * @return
	 *//*
	public double getCargo() {
		return cargo;
	}

	public void setCargo(double cargo) {
		this.cargo = cargo;
	}*/

	public boolean isPrecioBruto() {
		return precioBruto;
	}

	public void setPrecioBruto(boolean precioBruto) {
		this.precioBruto = precioBruto;
	}

	public int getDiaDelPago() {
		return diaDelPago;
	}

	public void setDiaDelPago(int diaDelPago) {
		this.diaDelPago = diaDelPago;
	}

	public int getDiaRevision() {
		return diaRevision;
	}

	public void setDiaRevision(int diaRevision) {
		this.diaRevision = diaRevision;
	}
	
	/**
     * Id para ventas importadas del sistema anterior. Nulo para ventas
     * generadas en el nuevo sistema
     * 
     */
    @Column(name="SIIPAPWIN_ID",nullable=true)
    private Long siipapWinId;
    
    /**
     * Id de la venta cuando esta ha sido importada desde oracle
     * (Parte de la carga inicial)
     * 
     * @return
     */
    public Long getSiipapWinId() {
		return siipapWinId;
	}

	public void setSiipapWinId(Long siipapWinId) {
		this.siipapWinId = siipapWinId;
	}
	
	public boolean isPrecioNeto(){
		return !isPrecioBruto();
	}


	public Date getImpreso() {
		return impreso;
	}

	public void setImpreso(Date impreso) {
		this.impreso = impreso;
	}

	public CancelacionDeCargo getCancelacion() {
		return cancelacion;
	}

	public void setCancelacion(CancelacionDeCargo cancelacion) {
		this.cancelacion = cancelacion;
	}
	
	
	
	public BigDecimal getPagos(){
		if(pagos==null)
			pagos=BigDecimal.ZERO; 
		return pagos;
	}

	/**
	 * Monto de las alplicaciones asignadas a esta factura
	 * 
	 * @return
	 */
	public BigDecimal getAplicado() {
		if(aplicado==null)
			aplicado=BigDecimal.ZERO;
		return aplicado;
	}
	
	public BigDecimal getSaldoCalculado(){
		return getTotal().subtract(getAplicado());
	}
	
	public CantidadMonetaria getSaldoCalculadoCM(){
		return new CantidadMonetaria(getSaldoCalculado(),getMoneda());
	}
	
	/**
	 * Regresa el importe de la venta sin devoluciones,descuentos  y bonoficaciones 
	 * @return
	 * @deprecated Usar getVentaNeta() que es mas apropiado
	 */
	public BigDecimal getSaldoSinPagos(){
		return getTotal().subtract(getDevoluciones()).subtract(getDescuentos()).subtract(getBonificaciones());
	}
	
	/**
	 * Regresa el importe de la venta sin devoluciones,descuentos  y bonoficaciones
	 * 
	 * @return
	 */
	public BigDecimal getVentaNeta(){
		return getTotal().subtract(getDevoluciones()).subtract(getDescuentos()).subtract(getBonificaciones());
	}
	
	public CantidadMonetaria getSaldoSinPagosCM(){
		return new CantidadMonetaria(getSaldoSinPagos(),getMoneda());
	}

	public BigDecimal getProvisionable() {
		return provisionable;
	}
	
	public CantidadMonetaria getTotalCM(){
		return new CantidadMonetaria(getTotal(),getMoneda());
	}
	public CantidadMonetaria getImporteCM(){
		return new CantidadMonetaria(getImporte(),getMoneda());
	}
	public CantidadMonetaria getImpuestoCM(){
		return new CantidadMonetaria(getImpuesto(),getMoneda());
	}

	public List<Aplicacion> getAplicaciones() {
		return aplicaciones;
	}
	
	public String getTipoSiipap(){
		return "";
	}
	
	public double getDescuentoFinanciero(){
		return 0;
	}

	public Juridico getJuridico() {
		return juridico;
	}

	public void setJuridico(Juridico juridico) {
		this.juridico = juridico;
	}

	public boolean isJuridico() {
		return this.juridico!=null;
	}

		
	/**
	 * Util solo para cargos cheques devueltos
	 * @return
	 */
	public Date getFechaDeEntrega(){
		return null;
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
	
	
	
}