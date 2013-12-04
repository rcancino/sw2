package com.luxsoft.sw3.tesoreria.model;

import java.math.BigDecimal;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

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
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.Version;

import org.apache.commons.lang.StringUtils;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
import org.hibernate.annotations.Type;
import org.hibernate.validator.AssertTrue;
import org.hibernate.validator.Length;
import org.hibernate.validator.NotEmpty;
import org.hibernate.validator.NotNull;

import com.luxsoft.siipap.cxc.model.AutorizacionDeAbono;
import com.luxsoft.siipap.cxc.model.OrigenDeOperacion;
import com.luxsoft.siipap.cxc.model.PagoConDeposito;
import com.luxsoft.siipap.model.BaseBean;
import com.luxsoft.siipap.model.Sucursal;
import com.luxsoft.siipap.model.UserLog;
import com.luxsoft.siipap.model.core.Cliente;
import com.luxsoft.siipap.model.tesoreria.Banco;
import com.luxsoft.siipap.model.tesoreria.Cuenta;
import com.luxsoft.sw3.model.AdressLog;
import com.luxsoft.sw3.replica.Replicable;

/**
 * Entidad q representa la solicitud de autorizacion de un pago con deposito
 * 
 * @author Ruben Cancino Ramos
 *
 */
@Entity
@Table(name="SX_SOLICITUDES_DEPOSITO")
@GenericGenerator(name="hibernate-uuid",strategy="uuid"
		,parameters={
				@Parameter(name="separator",value="-")
			}
		)
public class SolicitudDeDeposito extends BaseBean implements Replicable{
	
	@Id @GeneratedValue(generator="hibernate-uuid")
	@Column(name="SOL_ID")
	protected String id;
	
	@Version
	private int version;
	
	@ManyToOne(optional = false)
	@JoinColumn(name = "SUCURSAL_ID", nullable = false, updatable = false)
	private Sucursal sucursal;
				
	@ManyToOne(optional = false,fetch=FetchType.LAZY)
	@JoinColumn(name = "CLIENTE_ID", nullable = false)
	@NotNull(message="El Cliente es requerido")
	private Cliente cliente;
	
	@Column(name="DOCUMENTO",nullable=false)	
	private Long documento;
	
	@Enumerated(EnumType.STRING)
	@Column(name = "ORIGEN", nullable = false, length = 3)
	private OrigenDeOperacion origen=OrigenDeOperacion.CRE;
	
	@Column(name = "NOMBRE", nullable = false)
	private String nombre;
	
	@Column(name = "CLAVE", length = 7,nullable=false)
	private String clave;
	
	@Column(name = "FECHA", nullable = false)
	private Date fecha = new Date();
	
	@Column(name="FECHA_DEPOSITO",nullable=false)
	@Type(type="date")
	@NotNull(message="  Se requiere la fecha del deposito")
	private Date fechaDeposito;
	
	@Column(name = "COMENTARIO")
	@Length(max = 255)
	private String comentario;
	
	@Column(name="REFERENCIA")
	private String referenciaBancaria;
	
	@Column(name="TRANSFERENCIA",nullable=false)
	private BigDecimal transferencia=BigDecimal.ZERO;
	
	@Column(name="CHEQUE",nullable=false)
	private BigDecimal cheque=BigDecimal.ZERO;
	
	@Column(name="EFECTIVO",nullable=false)
	private BigDecimal efectivo=BigDecimal.ZERO;
	
	@Column(name="TOTAL",nullable=false)
	private BigDecimal total=BigDecimal.ZERO;
	
	@ManyToOne(optional = true
			,cascade={CascadeType.PERSIST,CascadeType.MERGE},fetch=FetchType.EAGER)
	@JoinColumn(name = "ABONO_ID", nullable = true)
	@Cascade(value={org.hibernate.annotations.CascadeType.REPLICATE})
	private PagoConDeposito pago;
	
	@ManyToOne(optional = false,fetch=FetchType.LAZY)			
	@JoinColumn(name = "CUENTA_ID", nullable = false)
	@NotNull(message="Se requiere la cuenta destino")
	private Cuenta cuentaDestino;
	
	@ManyToOne(optional = false,fetch=FetchType.LAZY)			
	@JoinColumn(name = "BANCO_ID", nullable = false)
	@NotNull(message="Se requiere el banco origen")
	private Banco bancoOrigen;
	
	@Column(name="ANTICIPO",nullable=false)
	private Boolean anticipo=false;
	
	@Column(name="SOLICITA",nullable=false,length=25)
	@Length(max=25)
	@NotEmpty(message=" Registre quien solicita")
	private String solicita;
	
	@Column(name="CANCELACION",nullable=true)
	private Date cancelacion;
	
	@Column(name="COMENTARIO_CANCELACION",length=250)
	@Length(max=250)
	private String comentarioCancelacion;
	
	@Column(name="SALVO_COBRO")
	private Boolean salvoBuenCobro=false;
	
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
	
	@Column(name="TX_IMPORTADO",nullable=true)
	private Date importado;
	
	@Column(name="TX_REPLICADO",nullable=true)
	private Date replicado;
	
	
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	
	public Sucursal getSucursal() {
		return sucursal;
	}
	public void setSucursal(Sucursal sucursal) {
		this.sucursal = sucursal;
	}
	
	public Cliente getCliente() {
		return cliente;
	}
	public void setCliente(Cliente cliente) {
		Object old=this.cliente;
		this.cliente = cliente;
		firePropertyChange("cliente", old, cliente);
		setClave(cliente!=null?cliente.getClave():null);
		setNombre(cliente!=null?cliente.getNombre():null);
	}
	
	public Long getDocumento() {
		return documento;
	}
	public void setDocumento(Long documento) {
		this.documento = documento;
	}
	
	public OrigenDeOperacion getOrigen() {
		return origen;
	}
	public void setOrigen(OrigenDeOperacion origen) {
		this.origen = origen;
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
	
	public Date getFecha() {
		return fecha;
	}
	public void setFecha(Date fecha) {
		Object old=this.fecha;
		this.fecha = fecha;
		firePropertyChange("fecha", old, fecha);
	}
	
	public Date getFechaDeposito() {
		return fechaDeposito;
	}
	public void setFechaDeposito(Date fechaDeposito) {
		Object old=this.fechaDeposito;
		this.fechaDeposito = fechaDeposito;
		firePropertyChange("fechaDeposito", old, fechaDeposito);
	}
	
	public String getComentario() {
		return comentario;
	}
	public void setComentario(String comentario) {
		Object old=this.comentario;
		this.comentario = comentario;
		firePropertyChange("comentario", old, comentario);
	}
	
	public String getReferenciaBancaria() {
		return referenciaBancaria;
	}
	public void setReferenciaBancaria(String referenciaBancaria) {
		Object old=this.referenciaBancaria;
		this.referenciaBancaria = referenciaBancaria;
		firePropertyChange("referenciaBancaria", old, referenciaBancaria);
	}
	
	public BigDecimal getTransferencia() {
		return transferencia;
	}
	public void setTransferencia(BigDecimal transferencia) {
		Object old=this.transferencia;
		this.transferencia = transferencia;
		firePropertyChange("transferencia", old, transferencia);
		if(transferencia.doubleValue()>0){
			setCheque(BigDecimal.ZERO);
			setEfectivo(BigDecimal.ZERO);
		}
		actualizarTotal();
	}
	
	public BigDecimal getCheque() {
		return cheque;
	}
	public void setCheque(BigDecimal cheque) {
		Object old=this.cheque;
		this.cheque = cheque;
		firePropertyChange("cheque", old, cheque);
		if(cheque.doubleValue()>0)
			setTransferencia(BigDecimal.ZERO);
		actualizarTotal();
	}
	
	public BigDecimal getEfectivo() {
		return efectivo;
	}
	public void setEfectivo(BigDecimal efectivo) {
		Object old=this.efectivo;
		this.efectivo = efectivo;
		firePropertyChange("efectivo", old, efectivo);
		if(efectivo.doubleValue()>0)
			setTransferencia(BigDecimal.ZERO);
		actualizarTotal();
	}
	
	public void actualizarTotal(){
		if(transferencia.doubleValue()>0){
			setTotal(transferencia);
		}else
			setTotal(efectivo.add(cheque));
	}	
	
	public BigDecimal getTotal() {
		return total;
	}
	public void setTotal(BigDecimal total) {
		this.total = total;
	}	
	
	public PagoConDeposito getPago() {
		return pago;
	}
	public void setPago(PagoConDeposito pago) {
		this.pago = pago;
	}
	
	public Cuenta getCuentaDestino() {
		return cuentaDestino;
	}
	public void setCuentaDestino(Cuenta cuentaDestino) {
		Object old=this.cuentaDestino;
		this.cuentaDestino = cuentaDestino;
		firePropertyChange("cuentaDestino", old, cuentaDestino);
	}
	
	public Banco getBancoOrigen() {
		return bancoOrigen;
	}
	public void setBancoOrigen(Banco bancoOrigen) {
		Object old=this.bancoOrigen;
		this.bancoOrigen = bancoOrigen;
		firePropertyChange("bancoOrigen", old, bancoOrigen);
	}
	
	public Boolean getAnticipo() {
		return anticipo;
	}
	public void setAnticipo(Boolean anticipo) {
		Object old=this.anticipo;
		this.anticipo = anticipo;
		firePropertyChange("anticipo", old, anticipo);
	}
	
	public String getSolicita() {
		return solicita;
	}
	public void setSolicita(String solicita) {
		Object old=this.solicita;
		this.solicita = solicita;
		firePropertyChange("solicita", old, solicita);
	}
	
	public UserLog getLog() {
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
	public int getVersion() {
		return version;
	}
	
	public Date getCancelacion() {
		return cancelacion;
	}
	public void setCancelacion(Date cancelacion) {
		Object old=this.cancelacion;
		this.cancelacion = cancelacion;
		firePropertyChange("cancelacion", old, cancelacion);
	}
	
	public String getComentarioCancelacion() {
		return comentarioCancelacion;
	}
	public void setComentarioCancelacion(String comentarioCancelacion) {
		Object old=this.comentarioCancelacion;
		this.comentarioCancelacion = comentarioCancelacion;
		firePropertyChange("comentarioCancelacion", old, comentarioCancelacion);
	}
	
	public Boolean getSalvoBuenCobro() {
		return salvoBuenCobro;
	}

	public void setSalvoBuenCobro(Boolean salvoBuenCobro) {
		Object old=this.salvoBuenCobro;
		this.salvoBuenCobro = salvoBuenCobro;
		firePropertyChange("salvoBuenCobro", old, salvoBuenCobro);
	}
	
	public String getTipo(){
		if(transferencia.doubleValue()>0)
			return "TRANSFERENCIA";
		else if(efectivo.doubleValue()>0 && cheque.doubleValue()>0){
			return "MIXTO";
		}
		else if(efectivo.doubleValue()>0 && cheque.doubleValue()==0)
			return "EFECTIVO";
		else
			return "CHEQUE"; 
	}
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((documento == null) ? 0 : documento.hashCode());
		result = prime * result
				+ ((sucursal == null) ? 0 : sucursal.hashCode());
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
		SolicitudDeDeposito other = (SolicitudDeDeposito) obj;
		if (documento == null) {
			if (other.documento != null)
				return false;
		} else if (!documento.equals(other.documento))
			return false;
		if (sucursal == null) {
			if (other.sucursal != null)
				return false;
		} else if (!sucursal.equals(other.sucursal))
			return false;
		return true;
	}
	
	public String toString(){
		return sucursal.getNombre()+ " "+documento+" Tot"+getTotal();
	}
	
	@AssertTrue(message="Debe registrar el importe del deposito")
	public boolean importeValido(){
		if(getId()==null){
			return getTotal().doubleValue()>0;
		}else
			return true;				
	}
	
	public String getPagoInfo(){
		if(getCancelacion()!=null){
			return getCancelacionInfo();
		}else{
			if(getPago()!=null){
				String pattern="Autorizado:  {0,time, dd/MM/yyyy :hh:mm }";
				if(getPago().getAutorizacion()==null)
					return "AUTO -ERR";
				return MessageFormat.format(pattern, getPago().getAutorizacion().getFechaAutorizacion());
			}else
				return "PENDIENTE";
		}
	}
	
	public String getCancelacionInfo(){
		String pattern="CANCELADO: {0,date,short}  {1}";
		return MessageFormat.format(pattern, getCancelacion(),getComentarioCancelacion());
	}
	
	public boolean isAtendido(){
		if(getPago()!=null)
			return true;
		else if(StringUtils.isNotBlank(getComentario()))
			return true;
		else if(getCancelacion()!=null)
			return true;
		else 
			return false;
	}
	
	@Transient
	private boolean autorizar=false;


	public boolean isAutorizar() {
		return autorizar;
	}
	public void setAutorizar(boolean autorizar) {
		boolean old=this.autorizar;
		this.autorizar = autorizar;
		firePropertyChange("autorizar", old, autorizar);
		if(autorizar)
			setComentario(null);
	}
	
	@Transient
	private boolean duplicado=false;


	public boolean isDuplicado() {
		return duplicado;
	}
	public void setDuplicado(boolean duplicado) {
		this.duplicado = duplicado;
	}
	
	
	
}
