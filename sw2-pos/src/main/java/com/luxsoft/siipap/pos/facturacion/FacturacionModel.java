package com.luxsoft.siipap.pos.facturacion;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Predicate;
import org.hibernate.validator.AssertTrue;
import org.springframework.util.Assert;

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.event.ListEvent;
import ca.odell.glazedlists.event.ListEventListener;

import com.luxsoft.luxor.utils.Bean;
import com.luxsoft.siipap.cxc.model.Abono;
import com.luxsoft.siipap.cxc.model.FormaDePago;
import com.luxsoft.siipap.cxc.model.PagoConTarjeta;
import com.luxsoft.siipap.model.CantidadMonetaria;
import com.luxsoft.siipap.pos.ui.venta.forms.PedidoFormSupport;
import com.luxsoft.siipap.ventas.model.Venta;
import com.luxsoft.sw3.services.PedidosManager;
import com.luxsoft.sw3.services.Services;
import com.luxsoft.sw3.ventas.Pedido;

/**
 * Modelo para el proceso de facturacion
 * 
 * Proporciona el estado y comportamiento adecuado para el proceso de facturar y 
 * cobrar un pedido. 
 * 
 * @author Ruben Cancino
 *
 */
public class FacturacionModel implements ListEventListener{
	
	private Pedido pedido;
	private Date fecha=new Date();
	private EventList<Venta> facturas;
	private EventList<Abono>  pagos;
	private BigDecimal totalFacturas=BigDecimal.ZERO;
	private BigDecimal totalPagos=BigDecimal.ZERO;
	private BigDecimal porPagar=BigDecimal.ZERO;
	private int numeroDeFacturas;
	private int numeroDePagos;
	
	
	public FacturacionModel(){
		facturas=GlazedLists.eventList(new BasicEventList<Venta>());
		pagos=GlazedLists.eventList(new BasicEventList<Abono>());
		facturas.addListEventListener(this);
		pagos.addListEventListener(this);
	}

	
	public Pedido getPedido() {
		return pedido;
	}
	
	private void setPedido(Pedido pedido) {
		this.pedido = pedido;
	}
	
	
	public Date getFecha() {
		return fecha;
	}


	public void setFecha(Date fecha) {
		this.fecha = fecha;
	}


	public EventList<Venta> getFacturas() {
		return facturas;
	}
	
	public void agregarFacturas(List<Venta> facs){
		facturas.addAll(facs);
	}
	
	public EventList<Abono> getPagos() {
		return pagos;
	}
	
	public void agregarPago(Abono pago){
		Assert.isTrue(pago.getCliente().equals(getPedido().getCliente()),"El pago seleccionado no corresponde  al cliente del pedido");
		pagos.add(pago);
	}
	
	public BigDecimal getTotalFacturas() {
		return totalFacturas;
	}

	public void setTotalFacturas(BigDecimal totalFacturas) {
		this.totalFacturas = totalFacturas;
	}

	public BigDecimal getTotalPagos() {
		return totalPagos;
	}

	public void setTotalPagos(BigDecimal totalPagos) {
		this.totalPagos = totalPagos;
	}

	private BigDecimal calcularPorPagar(){
		BigDecimal res= getTotalFacturas().subtract(getTotalPagos());
		if(res.doubleValue()<=0)
			return BigDecimal.ZERO;
		return res;
	}
	

	public BigDecimal getPorPagar() {
		return porPagar;
	}


	public void setPorPagar(BigDecimal porPagar) {
		this.porPagar = porPagar;
	}


	public int getNumeroDePagos() {
		return numeroDePagos;
	}


	public void setNumeroDePagos(int numeroDePagos) {
		this.numeroDePagos = numeroDePagos;
	}


	public int getNumeroDeFacturas() {
		return numeroDeFacturas;
	}

	public void setNumeroDeFacturas(int numeroDeFacturas) {
		this.numeroDeFacturas = numeroDeFacturas;
	}

	
	public void listChanged(ListEvent listChanges) {
		while(listChanges.next()){
			
		}
		recalcular();
		
	}
	
	/**
	 * 
	 */
	public void recalcular(){
		//BigDecimal total=BigDecimal.ZERO;
		//CantidadMonetaria total=CantidadMonetaria.pesos(BigDecimal.ZERO);
		CantidadMonetaria total=new CantidadMonetaria(BigDecimal.ZERO,pedido.getMoneda());
		for(Abono p:pagos){
			//total=total.add(p.getTotalCM());
			total=total.add(p.getDisponibleEnLinea());
		}
		setTotalPagos(total.amount());
		setNumeroDePagos(pagos.size());
		
		//total=CantidadMonetaria.pesos(BigDecimal.ZERO);
		total=new CantidadMonetaria(BigDecimal.ZERO,pedido.getMoneda());
		for(Venta fac:facturas){
			total=total.add(fac.getTotalCM());
		}
		setTotalFacturas(total.amount());
		setNumeroDeFacturas(facturas.size());
		setPorPagar(calcularPorPagar());
	}
	
	/*@AssertTrue(message="Registre las facturas necesarias")
	public boolean validarFacturas(){
		return getTotalFacturas().doubleValue()>0;
	}*/
	
	//Permite imprimir una factura con totales en cero
	
	@AssertTrue(message="Registre las facturas necesarias")
	public boolean validarFacturas(){
		return getTotalFacturas().doubleValue()>=0;
	}
	
	@AssertTrue(message="Pago insuficiente para facturar el pedido")
	public boolean validarTotalPorPagar(){
		/*if(getPedido()!=null){
			if(!getPedido().isContraEntrega()){
				return getTotalPagos().doubleValue()>=getPedido().getTotal().doubleValue();
			}
		}
		return false;
		*/
		//System.out.println("Total por pagar: "+getPorPagar());
		if(getPedido().isContraEntrega())
			return true;
		else
			return getPorPagar().doubleValue()<=1.0d;
	}
	
	public boolean existePagoConTarjeta(){
		return CollectionUtils.find(pagos, new Predicate(){

			public boolean evaluate(Object object) {
				return object instanceof PagoConTarjeta;
			}
			
		})!=null;
	}
	
	/**
	 * Centralizamos toda la regla de negocios para el manejo de la forma de pago
	 * al momento de la facturacion
	 * 
	 * @param target
	 */
	public void modificarFormaDePago(final FormaDePago target){
		System.out.println("Modificando la forma de pago a: "+target);
		getPedido().setFormaDePago(target);
		getPedidosManager().actualizarFormaDePago(getPedido());
		
		//Nuevo por cambios en CFD
		getPedido().actualizarImportes();
		PedidoFormSupport.actualizarCortes(getPedido(),null);
		PedidoFormSupport.actualizarManiobras(getPedido(),null);		
		getPedido().actualizarImportes();
		
		Pedido newPedido=getPedidosManager().save(getPedido());
		setPedido(newPedido);
		calcularFacturas();
		
	}
	
	private void calcularFacturas(){
		facturas.clear();
		Date fecha=Services.getInstance().obtenerFechaDelSistema();
		facturas.addAll(Services.getInstance().getFacturasManager()
				.prepararParaFacturar(getPedido(),fecha));
	}
	
	private PedidosManager getPedidosManager(){
		return Services.getInstance().getPedidosManager();
	}
	
	/**
	 * Dentro del proceso de facturacion es valido eliminar los pagos registrados siempre
	 * y cuando estos no sean con tarjeta
	 * 
	 */
	public void eliminarPagos(){
		
	}

	/**
	 * FactoryMethod para crear instancias de FacturacionModel adecuadamente inicializadas
	 * 
	 * @param pedido
	 * @return
	 */
	public static FacturacionModel getModel(final Pedido pedido){
		FacturacionModel model=(FacturacionModel)Bean.proxy(FacturacionModel.class);
		model.setPedido(pedido);		
		return model;
	}

}
