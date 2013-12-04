package com.luxsoft.siipap.cxc.ui.form;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.hibernate.validator.Length;

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.FilterList;
import ca.odell.glazedlists.matchers.Matcher;

import com.jgoodies.validation.util.PropertyValidationSupport;
import com.luxsoft.luxor.utils.Bean;
import com.luxsoft.siipap.cxc.model.AplicacionDeNota;
import com.luxsoft.siipap.cxc.model.Cargo;
import com.luxsoft.siipap.cxc.model.NotaDeCreditoDevolucion;
import com.luxsoft.siipap.model.CantidadMonetaria;
import com.luxsoft.siipap.service.ServiceLocator2;
import com.luxsoft.siipap.swing.form2.DefaultFormModel;
import com.luxsoft.siipap.util.MonedasUtils;
import com.luxsoft.siipap.ventas.model.Devolucion;
import com.luxsoft.siipap.ventas.model.DevolucionDeVenta;

/**
 * Modelo para la generacion de Notas de Credito por devolucion
 * 
 * @author Ruben Cancino Ramos
 *
 */
public class NotaDevolucionFormModel extends DefaultFormModel{
	
	private EventList<DevolucionDeVenta> partidas;
	
	public NotaDevolucionFormModel() {
		super(Bean.proxy(DevolucionModel.class));
		partidas=new BasicEventList<DevolucionDeVenta>();
		partidas=new FilterList<DevolucionDeVenta>(partidas,new Matcher<DevolucionDeVenta>(){
			public boolean matches(DevolucionDeVenta item) {
				return item.getNota()==null;
			}
		});
		getModel("devolucion").addValueChangeListener(new DevoHandler());
		getModel("impCortes").addValueChangeListener(new CortesHandler());
	}
	
	public NotaDevolucionFormModel(Devolucion d){
		this();
		getDevoModel().setDevolucion(d);
	}
	
	public DevolucionModel getDevoModel(){
		return (DevolucionModel)getBaseBean();
	}
	
	public void asignarFolio(){
		//getDevoModel().setFolio(ServiceLocator2.getCXCManager().buscarProximaNota());
	}

	public EventList<DevolucionDeVenta> getPartidas() {
		return partidas;
	}
	
	/**
	 * Procesa la nota de Devolución
	 * 
	 * @return
	 */
	public NotaDeCreditoDevolucion procesarNota(){
		
		BigDecimal saldo=getDevoModel().getDevolucion().getVenta().getSaldoCalculado();
		
		int folio=getDevoModel().getFolio();		
		NotaDeCreditoDevolucion nota=generarNota();
		nota.setFolio(folio++);  //Folio independiente al CFD ?
		
		nota.setTotal(getDevoModel().getTotal());
		nota.setImpuesto(getDevoModel().getImpuesto());
		nota.setImporte(getDevoModel().getImporte());
		if(saldo.doubleValue()>0)
			generarAplicacion(nota);
		return nota;
	}

	/**
	 * Genera las notas de credito requeridas para hacer
	 * efectiva la devolución.
	 * Contiene la logica y reglas de negocios para genera una o mas
	 * nota de credito y si deben o no generar la aplicacion a la venta
	 *  de forma automatica.
	 * 
	 * Si la devolucion esta autorizada se 
	 * @deprecated NO USAR MAS EN VIRTUD DE LOS CFD
	 * @return
	 */
	public List<NotaDeCreditoDevolucion> procesar_BAK(){
		List<NotaDeCreditoDevolucion> notas=new ArrayList<NotaDeCreditoDevolucion>();
		
		BigDecimal saldo=getDevoModel().getDevolucion().getVenta().getSaldoCalculado();
		int folio=getDevoModel().getFolio();
		
		if(partidas.size()<=5000){
			
			NotaDeCreditoDevolucion nota=generarNota();
			nota.setTotal(getDevoModel().getTotal());
			nota.setImpuesto(getDevoModel().getImpuesto());
			nota.setImporte(getDevoModel().getImporte());			
			for(DevolucionDeVenta rmd:partidas){
				rmd.setNota(nota);
			}
			nota.setFolio(folio++);			
			if(saldo.doubleValue()>0)
				generarAplicacion(nota);
			notas.add(nota);
		}else{
			
			
			final BigDecimal dis=getDevoModel().getImporte();
			
			//Generamos la nota 1
			NotaDeCreditoDevolucion n1=generarNota();
			BigDecimal impNota1=BigDecimal.ZERO;
			for(int index=0;index<3;index++){
				DevolucionDeVenta dd=partidas.get(index);
				impNota1=impNota1.add(dd.getVentaDet().getImporte());
				dd.setNota(n1);
			}
			n1.setImporte(impNota1);
			n1.actualizarImpuesto();
			n1.actualizarTotal();
			n1.setFolio(folio++);
			//Generamos la nota 2
			
			NotaDeCreditoDevolucion n2=generarNota();
			BigDecimal impNota2=dis.subtract(n1.getTotal());
			
			n2.setTotal(impNota2);
			n2.setImporte(MonedasUtils.calcularImporteDelTotal(impNota2));
			n2.setImpuesto(MonedasUtils.calcularImpuesto(n2.getImporte()));
			n2.setFolio(folio++);
			for(int index=3;index<partidas.size();index++){
				DevolucionDeVenta dd=partidas.get(index);
				dd.setNota(n2);
			}
			
			if(saldo.doubleValue()>=dis.doubleValue()){
				generarAplicacion(n1);
				generarAplicacion(n2);
			}
			notas.add(n1);
			notas.add(n2);
			
		}
		return notas;
	}
	
	/**
	 * Genera las notas de credito requeridas para hacer
	 * efectiva la devolución.
	 * Contiene la logica y reglas de negocios para genera una o mas
	 * nota de credito y si deben o no generar la aplicacion a la venta
	 *  de forma automatica.
	 * 
	 * Si la devolucion esta autorizada se 
	 * 
	 * @return
	 */
	public List<NotaDeCreditoDevolucion> procesar(){
		List<NotaDeCreditoDevolucion> notas=new ArrayList<NotaDeCreditoDevolucion>();
		
		
		int folio=getDevoModel().getFolio();
		
		NotaDeCreditoDevolucion nota=generarNota();
		nota.setTotal(getDevoModel().getTotal());
		nota.setImpuesto(getDevoModel().getImpuesto());
		nota.setImporte(getDevoModel().getImporte());			
		for(DevolucionDeVenta rmd:partidas){
			rmd.setNota(nota);
		}
		nota.setFolio(folio++);
		BigDecimal saldo=getDevoModel().getDevolucion().getVenta().getSaldoCalculado();
		if(saldo.doubleValue()>0)
			generarAplicacion(nota);
		notas.add(nota);
		return notas;
	}
	
	private NotaDeCreditoDevolucion generarNota(){
		NotaDeCreditoDevolucion nota=new NotaDeCreditoDevolucion();
		nota.setAplicacionAut(getDevoModel().getDevolucion().getAutorizacion());
		nota.setDevolucion(getDevoModel().getDevolucion());
		nota.setComentario(getDevoModel().getComentario());
		nota.setFecha(getDevoModel().getFecha());
		nota.setOrigen(getDevoModel().getDevolucion().getVenta().getOrigen());
		nota.setSucursal(getDevoModel().getDevolucion().getVenta().getSucursal());
		nota.setDescuento(getDevoModel().getDescuento1());
		return nota;
	}
	
	private AplicacionDeNota generarAplicacion(final NotaDeCreditoDevolucion nota){
		AplicacionDeNota a=new AplicacionDeNota();
		BigDecimal saldo=getDevoModel().getDevolucion().getVenta().getSaldoCalculado();
		BigDecimal total=nota.getTotal();
		BigDecimal porAplicar;
		if(saldo.doubleValue()<=total.doubleValue()){
			porAplicar=saldo;
		}else{
			porAplicar=total;
		}
		a.setCargo(getDevoModel().getDevolucion().getVenta());
		a.setComentario("APLICACION AUTOMATICA POR DEVOLUCION");
		a.setFecha(nota.getFecha());	
		a.setImporte(porAplicar);
		nota.agregarAplicacion(a);		
		return a;
	}
	
	
	@Override
	protected void addValidation(PropertyValidationSupport support) {
		if(getDevoModel().getDevolucion()==null){
			support.addError("", "No se ha seleccionado la devolucion");
		}else{
			if(getDevoModel().getDevolucion().getAutorizacion()==null){
				//support.addError("", "La devolucion no ha sido autorizada");
			}
			
		}
	}
	
	

	private class DevoHandler implements PropertyChangeListener{

		public void propertyChange(PropertyChangeEvent evt) {
			Devolucion d=(Devolucion)evt.getNewValue();
			partidas.clear();
			partidas.addAll(d.getPartidas());
			
		}
		
	}
	
	private class CortesHandler implements PropertyChangeListener{
		public void propertyChange(PropertyChangeEvent evt) {
			getDevoModel().actualizarImportes();
			
		}
		
	}

	public static class DevolucionModel {
		
		private Devolucion devolucion;
		
		private Date fecha=new Date();
		
		private int cortes=0;
		
		private int folio=0;
		
		private double descuento1;
		
		private double descuento2;
		
		private BigDecimal importe;		
		
		private BigDecimal impCortes=BigDecimal.ZERO;
		
		private BigDecimal impuesto=BigDecimal.ZERO;
		
		private BigDecimal total=BigDecimal.ZERO;
		
		private String cliente;
		
		private boolean precioNeto;
		 
		public DevolucionModel() {}
		
		

		@Length(max=200)
		private String comentario;

		public Devolucion getDevolucion() {
			return devolucion;
		}
		
		

		public void setDevolucion(Devolucion devolucion) {
			this.devolucion = devolucion;
			setCliente(devolucion.getVenta().getNombre());
			setFacFecha(devolucion.getVenta().getFecha());			
			setFacBonificaciones(devolucion.getVenta().getBonificaciones());
			setFacDescuentos(devolucion.getVenta().getDescuentos());
			setFacDevoluciones(devolucion.getVenta().getDevoluciones());
			setFacNumero(devolucion.getVenta().getDocumento());
			setPrecioNeto(devolucion.getVenta().isPrecioNeto());
			//setFacPrecioCortes(facPrecioCortes)
			//setFacCortes(Big)
			setFacSaldo(devolucion.getVenta().getSaldoCalculado());
			setFacTotal(devolucion.getVenta().getTotal());
			setFacVto(devolucion.getVenta().getVencimiento());
			if(devolucion.getVenta().getDescuentos().doubleValue()>0)
				setDescuento1(devolucion.getVenta().getDescuentoNota());
			// RMD
			setCliente(devolucion.getVenta().getNombre());
			setRmdComentario(devolucion.getComentario());
			setRmdFecha(devolucion.getFecha());
			setRmdNumero(devolucion.getNumero());
			setRmdSucursal(devolucion.getVenta().getSucursal().getNombre());
			
			
			BigDecimal cortes=BigDecimal.ZERO;
			for(DevolucionDeVenta det:devolucion.getPartidas()){
				cortes=cortes.add(det.getImporteCortesCalculado());
			}
			setImpCortes(cortes);
			actualizarImportes();
			
		}
		
		public void actualizarImportes(){
			BigDecimal  subTotal=BigDecimal.ZERO;;
			BigDecimal cortes=getImpCortes();
			double descuentoNota=0;
			
			if(devolucion.getVenta().getDescuentos().doubleValue()>0)
				descuentoNota=devolucion.getVenta().getDescuentoGeneral();
			
			for(DevolucionDeVenta det:devolucion.getPartidas()){
				subTotal=subTotal.add(det.getImporteNeto());
				//cortes=cortes.add(det.getImporteCortesCalculado());
			}
			
			BigDecimal descuento=subTotal.multiply(BigDecimal.valueOf(descuentoNota));
			
			CantidadMonetaria importeDevolucion=CantidadMonetaria.pesos(subTotal.add(cortes).subtract(descuento).doubleValue());
			
			setImporte(importeDevolucion.amount());
			
		}

		public Date getFecha() {
			return fecha;
		}

		public void setFecha(Date fecha) {
			this.fecha = fecha;
		}

		public String getComentario() {
			return comentario;
		}

		public void setComentario(String comentario) {
			this.comentario = comentario;
		}
		
		public int getCortes() {
			return cortes;
		}

		public void setCortes(int cortes) {
			this.cortes = cortes;
		}

		public double getDescuento1() {
			return descuento1;
		}

		public void setDescuento1(double descuento1) {
			this.descuento1 = descuento1;
		}

		public double getDescuento2() {
			return descuento2;
		}

		public void setDescuento2(double descuento2) {
			this.descuento2 = descuento2;
		}

		public BigDecimal getImporte() {
			return importe;
		}

		public void setImporte(BigDecimal subTotal) {
			this.importe = subTotal;
			setImpuesto(MonedasUtils.calcularImpuesto(getImporte()));
			setTotal(MonedasUtils.calcularTotal(getImporte()));
		}

		public BigDecimal getImpCortes() {
			return impCortes;
		}

		public void setImpCortes(BigDecimal impCortes) {
			this.impCortes = impCortes;
			getDevolucion().setImporteCortes(impCortes);
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
		
		

		public boolean isPrecioNeto() {
			return precioNeto;
		}



		public void setPrecioNeto(boolean precioNeto) {
			this.precioNeto = precioNeto;
		}



		public int getFolio() {
			return folio;
		}



		public void setFolio(int folio) {
			this.folio = folio;
		}



		/*** Propiedades de solo lectura ***/

		// Datos de la factura
		private Long facNumero;
		private Date facFecha;
		private Date facVto;
		private BigDecimal facTotal=BigDecimal.ZERO;
		private BigDecimal facSaldo=BigDecimal.ZERO;
		
		private BigDecimal facDescuentos=BigDecimal.ZERO;
		private BigDecimal facBonificaciones=BigDecimal.ZERO;
		private BigDecimal facDevoluciones=BigDecimal.ZERO;
		private int facCortes;
		private BigDecimal facPrecioCortes=BigDecimal.ZERO;
		private BigDecimal facImporteCortes=BigDecimal.ZERO;
		
		// RMD
		private Long rmdNumero;
		private Date rmdFecha;
		private String rmdSucursal;
		private String rmdComentario;

		public String getCliente() {
			return cliente;
		}

		public void setCliente(String cliente) {
			this.cliente = cliente;
		}

		public Long getFacNumero() {
			return facNumero;
		}

		public void setFacNumero(Long facNumero) {
			this.facNumero = facNumero;
		}

		public Date getFacFecha() {
			return facFecha;
		}

		public void setFacFecha(Date facFecha) {
			this.facFecha = facFecha;
		}

		public Date getFacVto() {
			return facVto;
		}

		public void setFacVto(Date facVto) {
			this.facVto = facVto;
		}

		public BigDecimal getFacTotal() {
			return facTotal;
		}

		public void setFacTotal(BigDecimal facTotal) {
			this.facTotal = facTotal;
		}

		public BigDecimal getFacSaldo() {
			return facSaldo;
		}

		public void setFacSaldo(BigDecimal facSaldo) {
			this.facSaldo = facSaldo;
		}

		public BigDecimal getFacDescuentos() {
			return facDescuentos;
		}

		public void setFacDescuentos(BigDecimal facDescuentos) {
			this.facDescuentos = facDescuentos;
		}

		public BigDecimal getFacBonificaciones() {
			return facBonificaciones;
		}

		public void setFacBonificaciones(BigDecimal facBonificaciones) {
			this.facBonificaciones = facBonificaciones;
		}

		public BigDecimal getFacDevoluciones() {
			return facDevoluciones;
		}

		public void setFacDevoluciones(BigDecimal facDevoluciones) {
			this.facDevoluciones = facDevoluciones;
		}

		public int getFacCortes() {
			return facCortes;
		}

		public void setFacCortes(int facCortes) {
			this.facCortes = facCortes;
		}

		public BigDecimal getFacPrecioCortes() {
			return facPrecioCortes;
		}

		public void setFacPrecioCortes(BigDecimal facPrecioCortes) {
			this.facPrecioCortes = facPrecioCortes;
		}

		public Long getRmdNumero() {
			return rmdNumero;
		}

		public void setRmdNumero(Long rmdNumero) {
			this.rmdNumero = rmdNumero;
		}

		public Date getRmdFecha() {
			return rmdFecha;
		}

		public void setRmdFecha(Date rmdFecha) {
			this.rmdFecha = rmdFecha;
		}

		public String getRmdSucursal() {
			return rmdSucursal;
		}

		public void setRmdSucursal(String rmdSucursal) {
			this.rmdSucursal = rmdSucursal;
		}

		public String getRmdComentario() {
			return rmdComentario;
		}

		public void setRmdComentario(String rmdComentario) {
			this.rmdComentario = rmdComentario;
		}

		public BigDecimal getFacImporteCortes() {
			return facImporteCortes;
		}

		public void setFacImporteCortes(BigDecimal facImporteCortes) {
			this.facImporteCortes = facImporteCortes;
		}
		
		
		
	}

}
