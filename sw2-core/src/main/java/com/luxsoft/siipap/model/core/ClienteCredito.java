package com.luxsoft.siipap.model.core;

import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Date;

import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.TableGenerator;
import javax.persistence.Transient;

import org.hibernate.annotations.AccessType;
import org.hibernate.annotations.Columns;
import org.hibernate.annotations.Type;

import com.luxsoft.siipap.model.BaseBean;
import com.luxsoft.siipap.model.CantidadMonetaria;
import com.luxsoft.siipap.model.UserLog;
import com.luxsoft.sw3.model.AdressLog;

/**
 * Entidad que almacena los datos relacionados con la linea de credito de un
 * cliente de credito
 * 
 * @author Ruben Cancino
 * 
 */
@Entity
@Table(name="SX_CLIENTES_CREDITO")
public class ClienteCredito extends BaseBean {
	
	
	//@Id @GeneratedValue(strategy=GenerationType.AUTO)
	@TableGenerator(
            name="idsGen", 
            table="SX_TABLE_IDS", 
            pkColumnName="GEN_KEY", 
            valueColumnName="GEN_VALUE", 
            pkColumnValue="CREDITO_ID", 
            allocationSize=1)
    @Id
    @GeneratedValue(strategy=GenerationType.TABLE, generator="idsGen")
	@Column(name="CREDITO_ID")
	private Long id;
	
	@OneToOne(optional=false,mappedBy="credito")		
	private Cliente cliente;

	 @Column(name = "CLAVE",length=7,nullable=false)
	 private String clave;
	    
	 @Column(name = "NOMBRE", nullable = false)
	 private String nombre;
	
	@Type(type = "com.luxsoft.siipap.support.hibernate.CantidadMonetariaCompositeUserType")
	@Columns(columns = { @Column(name = "LINEA", scale = 2, nullable = true),
			@Column(name = "LINEA_MON", length = 3, nullable = true) })
	private CantidadMonetaria linea = CantidadMonetaria.pesos(0);

	@Column(name = "PLAZO", nullable = false)
	private int plazo = 0;

	@Column(name = "CRED_SUSPENDIDO", nullable = false)
	private boolean suspendido = false;

	@Column(name = "REVISION", nullable = false)
	@AccessType(value="field")
	private boolean revision = false;

	@Column(name = "DIA_REVISION", nullable = false)
	@AccessType(value="field")
	private int diarevision = 7;

	@Column(name = "DIA_COBRO", nullable = false)
	@AccessType(value="field")
	private int diacobro = 7;
	
	@Column(name = "VENCE_FACTURA", nullable = false)
	@AccessType(value="field")
	private boolean vencimientoFactura=true;

	@Column(name="POSTFECHADO",nullable=false)
	private boolean chequePostfechado=false;
	
	@Column(name="CHECKPLUS",nullable=false)
	private boolean checkplus=false;
	
	@Column(name="CNOTA",nullable=false)
	private boolean conNotaAnticipada=false;
	
	@Column(name="SUSP_DESCTO",nullable=false)
	private boolean suspenderDescuento=false;
	
	@Column(name="DESC_ESTIMADO")
	private double descuentoEstimado=0;
	
	@Column(name="OPERADOR_CXC")
	private int operador;
	
	@Column(name="SALDO")
	private BigDecimal saldo=BigDecimal.ZERO;
	
	//@Column(name="SALDO_GENERAL")
	@Transient
	private BigDecimal saldoGeneral;
	
	@Column(name="ATRASO_MAX",nullable=false)
	private int atrasoMaximo;
	
	@Column(name="NO_ATRASO",nullable=false)
	private boolean noAtraso=false;
	
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
	
	public ClienteCredito() {
	}

	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}


	public Cliente getCliente() {
		return cliente;
	}

	public void setCliente(Cliente cliente) {
		this.cliente = cliente;
		if(cliente!=null){
			this.clave=cliente.getClave();
			this.nombre=cliente.getNombreRazon();
		}else{
			this.clave=null;
			this.nombre=null;
		}
	}

	public String getClave() {
		return clave;
	}

	public void setClave(String clave) {
		this.clave = clave;
	}

	public String getNombre() {
		return nombre;
	}

	public void setNombre(String nombre) {
		this.nombre = nombre;
	}

	/**
	 * @return the arevision
	 */
	public boolean isRevision() {
		return revision;
	}

	/**
	 * @param arevision the arevision to set
	 */
	public void setRevision(boolean arevision) {
		this.revision = arevision;
	}

	public int getDiacobro() {
		return diacobro;
	}

	public void setDiacobro(int diaCobro) {
		this.diacobro = diaCobro;
	}

	public int getDiarevision() {
		return diarevision;
	}

	public void setDiarevision(int diaRvision) {
		this.diarevision = diaRvision;
	}

	public CantidadMonetaria getLinea() {
		return linea;
	}

	public void setLinea(CantidadMonetaria linea) {
		Object old=this.linea;
		this.linea = linea;
		firePropertyChange("linea", old, linea);
	}

	public int getPlazo() {
		return plazo;
	}

	public void setPlazo(int plazo) {
		int old=this.plazo;
		this.plazo = plazo;
		firePropertyChange("plazo", old, plazo);
	}

	public boolean isSuspendido() {
		return suspendido;
	}

	public void setSuspendido(boolean suspendido) {
		boolean old=this.suspendido;
		this.suspendido = suspendido;
		firePropertyChange("suspendido", old, suspendido);
	}
	
	public boolean isNoAtraso() {
		return noAtraso;
	}

	
	public void setNoAtraso(boolean noAtraso) {
		this.noAtraso = noAtraso;
	}
	
	
	/**
     * Calcula el vencimiento para una opracion de venta
     * con este cliente
     * 
     * @param ventaFecha
     * @return
     */
    public Date calcularVencimiento(Date ventaFecha){
    	if(isSuspendido()){
    		return ventaFecha;
    	}else{
        	Calendar c=Calendar.getInstance();
        	c.setTime(ventaFecha);
        	c.add(Calendar.DATE, getPlazo());
        	return c.getTime();
    	}
    }

	/**
	 * @return the vencimientoFactura
	 */
	public boolean isVencimientoFactura() {
		return vencimientoFactura;
	}

	/**
	 * @param vencimientoFactura the vencimientoFactura to set
	 */
	public void setVencimientoFactura(boolean vencimientoFactura) {
		Object old=this.vencimientoFactura;
		this.vencimientoFactura = vencimientoFactura;
		firePropertyChange("vencimientoFactura", old, vencimientoFactura);
	}

	@Override
	public boolean equals(Object o) {
		if(o==null) return false;
		if(o==this) return true;
		if(getClass()!=o.getClass())
			return false;
		ClienteCredito otro=(ClienteCredito)o;
		return getLinea().equals(otro.getLinea());
	}

	@Override
	public int hashCode() {
		return getLinea().hashCode();
	}

	@Override
	public String toString() {
		return getLinea().toString();
	}

	public boolean isChequePostfechado() {
		return chequePostfechado;
	}

	public void setChequePostfechado(boolean chequePostfechado) {
		boolean old=this.chequePostfechado;
		this.chequePostfechado = chequePostfechado;
		firePropertyChange("chequePostfechado", old, chequePostfechado);
	}

	public boolean isConNotaAnticipada() {
		return conNotaAnticipada;
	}

	public void setConNotaAnticipada(boolean conNotaAnticipada) {
		this.conNotaAnticipada = conNotaAnticipada;
	}

	public boolean isSuspenderDescuento() {
		return suspenderDescuento;
	}

	public void setSuspenderDescuento(boolean suspenderDescuento) {
		boolean old=this.suspenderDescuento;
		this.suspenderDescuento = suspenderDescuento;
		firePropertyChange("suspenderDescuento", old, suspenderDescuento);
	}

	public double getDescuentoEstimado() {
		return descuentoEstimado;
	}

	public void setDescuentoEstimado(double descuentoEstimado) {
		this.descuentoEstimado = descuentoEstimado;
	}

	public int getOperador() {
		return operador;
	}

	public void setOperador(int operador) {
		int old=this.operador;
		this.operador = operador;
		firePropertyChange("operador", old, operador);
	}

	public BigDecimal getSaldo() {
		return saldo;
	}

	public void setSaldo(BigDecimal saldo) {
		this.saldo = saldo;
	}

	public BigDecimal getSaldoGeneral() {
		return saldoGeneral;
	}

	public void setSaldoGeneral(BigDecimal saldoGeneral) {
		this.saldoGeneral = saldoGeneral;
	}
	
	public BigDecimal getCreditoDisponible(){
		return getLinea().getAmount().subtract(getSaldo());
	}

	public int getAtrasoMaximo() {
		return atrasoMaximo;
	}

	public void setAtrasoMaximo(int atrasoMaximo) {
		this.atrasoMaximo = atrasoMaximo;
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

	public boolean isCheckplus() {
		return checkplus;
	}

	public void setCheckplus(boolean checkplus) {
		this.checkplus = checkplus;
	}
	
	
	
	
}
