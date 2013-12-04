package com.luxsoft.siipap.pos.ui.forms.caja;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.Date;

import org.hibernate.validator.NotNull;

import com.luxsoft.siipap.cxc.model.FormaDePago;
import com.luxsoft.siipap.cxc.model.OrigenDeOperacion;
import com.luxsoft.siipap.cxc.model.Pago;
import com.luxsoft.siipap.cxc.model.PagoConCheque;
import com.luxsoft.siipap.cxc.model.PagoConDeposito;
import com.luxsoft.siipap.cxc.model.PagoConEfectivo;
import com.luxsoft.siipap.cxc.model.PagoConTarjeta;
import com.luxsoft.siipap.cxc.model.Tarjeta;
import com.luxsoft.siipap.model.Sucursal;
import com.luxsoft.siipap.model.core.Cliente;
import com.luxsoft.siipap.model.tesoreria.Banco;
import com.luxsoft.siipap.model.tesoreria.Cuenta;
import com.luxsoft.siipap.util.MonedasUtils;

public class PagoModel {
	
	@NotNull(message="El cliente es mandatorio")
	private Cliente cliente;
	
	private Sucursal sucursal;
	private Date fecha=new Date();
	
	private Currency moneda=MonedasUtils.PESOS;
	private double tc=1;
	private boolean anticipo;
	private OrigenDeOperacion origen=OrigenDeOperacion.CAM;
	private Banco banco;
	
	
	@NotNull
	private FormaDePago formaDePago=FormaDePago.EFECTIVO;
	
	private String referencia;
	private String comentario;
	
	//Cheque
	private String cuenta;
	private long numero;
	private boolean postFechado;
	private Date vencimiento;
	private String nombre;
	
	//Transferencia interbancaria
	private Cuenta cuentaDestino;
	private BigDecimal efectivo=BigDecimal.ZERO;
	private BigDecimal cheque=BigDecimal.ZERO;
	private BigDecimal transferencia=BigDecimal.ZERO;
	private Date fechaDeposito;
	private Boolean salvoBuenCobro=null;
	
	//Tarjeta
	private Tarjeta tarjeta;
	private String autorizacion;
	private String numeroTarjeta;

	
	
	public Cliente getCliente() {
		return cliente;
	}
	public void setCliente(Cliente cliente) {
		this.cliente = cliente;
		if(cliente!=null){
			if(getFormaDePago().equals(FormaDePago.CHEQUE)||getFormaDePago().equals(FormaDePago.CHEQUE_POSTFECHADO)){
				setNombre(cliente.getNombre());
			}else
				setNombre(null);
		}
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
	public BigDecimal getImporte() {
		return getEfectivo().add(getCheque());
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
		this.tc = tc;
	}
	public boolean isAnticipo() {
		return anticipo;
	}
	public void setAnticipo(boolean anticipo) {
		this.anticipo = anticipo;
	}
	public OrigenDeOperacion getOrigen() {
		return origen;
	}
	public void setOrigen(OrigenDeOperacion origen) {
		this.origen = origen;
	}
	public FormaDePago getFormaDePago() {
		return formaDePago;
	}
	
	
	public void setFormaDePago(FormaDePago formaDePago) {
		this.formaDePago = formaDePago;		
		setCuenta(null);
		setNumero(0);
		setNombre(null);			
		setVencimiento(null);
		setCheque(BigDecimal.ZERO);
		setEfectivo(BigDecimal.ZERO);
		setReferencia(null);
		setBanco(null);
		setTarjeta(null);
		setNumeroTarjeta(null);
		setAutorizacion(null);		
		if(formaDePago!=null){
			if(getFormaDePago().equals(FormaDePago.CHEQUE)||getFormaDePago().equals(FormaDePago.CHEQUE_POSTFECHADO)){
				if(getCliente()!=null)
					setNombre(cliente.getNombre());
			}else{
				setNombre(null);
			}
		}
	}
	public String getReferencia() {
		return referencia;
	}
	public void setReferencia(String referencia) {
		this.referencia = referencia;
		
	}
	public String getComentario() {
		return comentario;
	}
	public void setComentario(String comentario) {
		this.comentario = comentario;
	}
	public String getCuenta() {
		return cuenta;
	}
	public void setCuenta(String cuenta) {
		this.cuenta = cuenta;
	}
	public long getNumero() {
		return numero;
	}
	public void setNumero(long numero) {
		this.numero = numero;
	}
	public boolean isPostFechado() {
		return postFechado;
	}
	public void setPostFechado(boolean postFechado) {
		this.postFechado = postFechado;
	}
	public Date getVencimiento() {
		return vencimiento;
	}
	public void setVencimiento(Date vencimiento) {
		this.vencimiento = vencimiento;
	}
	public String getNombre() {
		return nombre;
	}
	public void setNombre(String nombre) {
		this.nombre = nombre;
	}
	public BigDecimal getEfectivo() {
		return efectivo;
	}
	public void setEfectivo(BigDecimal efectivo) {
		this.efectivo = efectivo;
	}
	public BigDecimal getCheque() {
		return cheque;
	}
	public void setCheque(BigDecimal cheque) {
		this.cheque = cheque;
	}
	public BigDecimal getTransferencia() {
		return transferencia;
	}
	public void setTransferencia(BigDecimal transferencia) {
		this.transferencia = transferencia;
	}
	
	
	
	public Date getFechaDeposito() {
		return fechaDeposito;
	}
	public void setFechaDeposito(Date fechaDeposito) {
		this.fechaDeposito = fechaDeposito;
	}
	
	
	public Boolean getSalvoBuenCobro() {
		return salvoBuenCobro;
	}
	public void setSalvoBuenCobro(Boolean salvoBuenCobro) {
		this.salvoBuenCobro = salvoBuenCobro;
	}
	public Tarjeta getTarjeta() {
		return tarjeta;
	}
	public void setTarjeta(Tarjeta tarjeta) {
		this.tarjeta = tarjeta;
	}
	
	public String getNumeroTarjeta() {
		return numeroTarjeta;
	}
	public void setNumeroTarjeta(String numeroTarjeta) {
		this.numeroTarjeta = numeroTarjeta;
	}
	
	public String getAutorizacion() {
		return autorizacion;
	}
	public void setAutorizacion(String autorizacion) {
		this.autorizacion = autorizacion;
	}
	public Banco getBanco() {
		return banco;
	}
	public void setBanco(Banco banco) {
		this.banco = banco;
	}
	
	
	public Cuenta getCuentaDestino() {
		return cuentaDestino;
	}
	public void setCuentaDestino(Cuenta cuentaDestino) {
		this.cuentaDestino = cuentaDestino;
	}
	
	public Pago toPago(){		
		switch (formaDePago) {
		case EFECTIVO:
			return toEfectivo();
		case CHEQUE:			
		case CHEQUE_POSTFECHADO:
			return toCheque();
		case TARJETA_CREDITO:
		case TARJETA_DEBITO:
			return toTarjeta();
		case DEPOSITO:
		case TRANSFERENCIA:
			return toDeposito();
		default:
			throw new IllegalStateException("Forma de pago no implementada: "+formaDePago);
		}
	}
	
	private void prepare(Pago pago){
		pago.setCliente(getCliente());
		pago.setAnticipo(isAnticipo());
		if(banco!=null)
			pago.setBanco(getBanco().getNombre());
		pago.setComentario(getComentario());
		pago.setCuenta(getCuentaDestino());
		pago.setCuentaDelCliente(getCuenta());
		pago.setCuentaHabiente(getNombre());
		pago.setFecha(getFecha());
		
		BigDecimal total=getImporte();		
		pago.setTotal(total);		
		pago.setImporte(MonedasUtils.calcularImporteDelTotal(total));
		pago.setImpuesto(MonedasUtils.calcularImpuesto(pago.getImporte()));
		
		
		pago.setMoneda(getMoneda());
		pago.setOrigen(getOrigen());
		pago.setSucursal(getSucursal());
		pago.setTc(getTc());
		//pago.setTotal(MonedasUtils.calcularTotal(pago.getImporte()));
	}
	
	private PagoConEfectivo toEfectivo(){
		PagoConEfectivo efectivo=new PagoConEfectivo();
		prepare(efectivo);
		return efectivo;
	}
	
	private PagoConCheque toCheque(){
		PagoConCheque cheque=new PagoConCheque();
		prepare(cheque);
		cheque.setNumero(getNumero());
		cheque.setPostFechado(isPostFechado());
		cheque.setVencimiento(getVencimiento());
		return cheque;
	}
	
	private PagoConTarjeta toTarjeta(){
		PagoConTarjeta tarjeta=new PagoConTarjeta();
		prepare(tarjeta);
		tarjeta.setTarjeta(getTarjeta());
		tarjeta.setAutorizacionBancaria(getAutorizacion());		
		return tarjeta;
	}
	
	private PagoConDeposito toDeposito(){
		PagoConDeposito deposito=new PagoConDeposito();
		prepare(deposito);
		if(formaDePago.equals(FormaDePago.TRANSFERENCIA))
			deposito.setTransferencia(getEfectivo());
		else{
			deposito.setEfectivo(getEfectivo());
			deposito.setCheque(getCheque());
		}
		deposito.setCuenta(getCuentaDestino());
		deposito.setReferenciaBancaria(getReferencia());
		deposito.setFechaDeposito(getFechaDeposito());
		deposito.setSalvoBuenCobro(getSalvoBuenCobro());
		return deposito;
	}

	public void registrarImporte(BigDecimal importe){
		switch (formaDePago) {
		case EFECTIVO:
			setEfectivo(importe);
			setCheque(BigDecimal.ZERO);
			break;
		case CHEQUE:			
		case CHEQUE_POSTFECHADO:
			setEfectivo(BigDecimal.ZERO);
			setCheque(importe);
			break;
		case TARJETA_CREDITO:
		case TARJETA_DEBITO:
		case DEPOSITO:
		case TRANSFERENCIA:
			setEfectivo(importe);
			setCheque(BigDecimal.ZERO);
			break;
		default:
			break;
		}
	}
	
	

}
