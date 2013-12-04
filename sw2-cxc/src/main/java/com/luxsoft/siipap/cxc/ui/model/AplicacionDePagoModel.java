package com.luxsoft.siipap.cxc.ui.model;


import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.UniqueList;
import ca.odell.glazedlists.event.ListEvent;
import ca.odell.glazedlists.event.ListEventListener;
import ca.odell.glazedlists.gui.TableFormat;

import com.jgoodies.validation.util.PropertyValidationSupport;
import com.luxsoft.siipap.cxc.model.Abono;
import com.luxsoft.siipap.cxc.model.Aplicacion;
import com.luxsoft.siipap.cxc.model.AplicacionDeNota;
import com.luxsoft.siipap.cxc.model.AplicacionDePago;
import com.luxsoft.siipap.cxc.model.AutorizacionDeAplicacionCxC;
import com.luxsoft.siipap.cxc.model.Pago;
//import com.luxsoft.siipap.cxc.model.AplicacionDePago;
import com.luxsoft.siipap.cxc.model.Cargo;
import com.luxsoft.siipap.cxc.model.OrigenDeOperacion;
import com.luxsoft.siipap.cxc.rules.AplicacionDeAbonoRules;
import com.luxsoft.siipap.model.CantidadMonetaria;
import com.luxsoft.siipap.model.core.Cliente;
import com.luxsoft.siipap.service.ServiceLocator2;
import com.luxsoft.siipap.swing.form2.DefaultFormModel;

/**
 * Presentation layer para la aplicacion de abonos a cuentas por cobrar 
 * 
 * @author Ruben Cancino
 *
 */
public class AplicacionDePagoModel extends DefaultFormModel {
	
	private EventList<Aplicacion> aplicaciones;
	private EventList<Abono> disponibles;
	private OrigenDeOperacion origen=OrigenDeOperacion.CRE;
	private AplicacionDeAbonoRules aplicacionDeAbonoRules=new AplicacionDeAbonoRules();
	private AutorizacionDeAplicacionCxC autorizacion;
	
	public AplicacionDePagoModel(){
		super(AplicacionModel.getModel());
	}
	
	protected void init(){
		super.init();
		aplicaciones=new UniqueList(new BasicEventList<Aplicacion>(),new Comparator<Aplicacion>(){
			public int compare(Aplicacion o1, Aplicacion o2) {
				if(o1.getCargo().getSucursal().equals(o2.getCargo().getSucursal())){
					if(o1.getCargo().getOrigen().equals(o2.getCargo().getOrigen())){					
						return o1.getCargo().getDocumento().compareTo(o2.getCargo().getDocumento());
					}else
						return o1.getCargo().getOrigen().compareTo(o2.getCargo().getOrigen());
				}else				
					return o1.getCargo().getSucursal().getNombre().compareTo(o2.getCargo().getSucursal().getNombre());
			}
			
		});
		aplicaciones.addListEventListener(new ListHandler());
		//Se trata de una lista pequeña y de pocas modificaciones
		disponibles=GlazedLists.threadSafeList(new BasicEventList<Abono>());
		getModel("abono").addValueChangeListener(new AbonoHandler());
		getModel("cliente").addValueChangeListener(new ClienteHandler());
		getModel("fecha").addValueChangeListener(new FechaHandler());
	}
	
	public AplicacionModel getAplicacionModel(){
		return (AplicacionModel)getBaseBean();
	}
	
	public Cliente getCurrentCliente(){
		return getAplicacionModel().getCliente();
	}
	
	public Abono getCurrentAbono(){
		return getAplicacionModel().getAbono();
	}

	public EventList<Aplicacion> getAplicaciones() {
		return aplicaciones;
	}

	
	
	public AutorizacionDeAplicacionCxC getAutorizacion() {
		return autorizacion;
	}

	public void setAutorizacion(AutorizacionDeAplicacionCxC autorizacion) {
		this.autorizacion = autorizacion;
	}

	public EventList<Abono> getDisponibles() {
		
		return disponibles;	
	}

	public TableFormat createTableFormat(){
		String[] props={"cargo.documento","cargo.fecha","cargo.vencimiento","cargo.tipoDocto","cargo.origen"						
						,"cargo.total","cargo.devoluciones","cargo.descuentos","importeDescuento","cargo.bonificaciones","cargo.pagos"
						,"cargo.saldoCalculado","importe","pendienteDePago"
						};
		String[] names={"Documento","Fecha","Vto","Tipo","Origen","Total(Docto)","Devs","Desc","D.F.","Bonific","Pagos","Saldo","Por Aplicar","Pendiente"};
		boolean[] edits={false,false,false,false,false,false,false,false,false,false,false,false,true,false};
		return GlazedLists.tableFormat(Aplicacion.class,props, names,edits);
	}
	
	public void actualizarPagosDisponibles(){
		if(getCurrentCliente()!=null){
			disponibles.clear();
			disponibles.addAll(ServiceLocator2.getCXCManager().buscarDisponibles(getCurrentCliente()));
		}		
	}
	
	/**
	 * A partir de una lista de cargos se genera un grupo 
	 * de aplicaciones. La generacion de estas aplicaciones
	 * se delega a el bean {@link AplicacionDeAbonoRules}
	 * 
	 */
	public void procesarCargos(final List<Cargo> cargos){
		//Con el abono  
		logger.info("Generando aplicaciones para cargos:"+cargos);
		BigDecimal disponible=getDisponible();
		for(Cargo cargo:cargos){
			if(disponible.doubleValue()>0){
				//AplicacionDePago aplicacion=new AplicacionDePago();
				Aplicacion aplicacion;
				if(getCurrentAbono() instanceof Pago)
					aplicacion=new AplicacionDePago();
				else
					aplicacion=new AplicacionDeNota();
				//Modificacion para no permitir aplicar Notas de Bonificacion a cargos de la Cartera de Cheques Devueltos
				/*if(aplicacion instanceof AplicacionDeNota && cargo.getOrigen().equals(OrigenDeOperacion.CHE)){
					continue;
				}*/
				aplicacion.setCargo(cargo);
				aplicacion.setFecha(getAplicacionModel().getFecha());
				BigDecimal importeAplicable=BigDecimal.ZERO;
				BigDecimal aplicado=cargo.getAplicado();
				
				BigDecimal notaDescuento=cargo.getDescuentos();
				if(notaDescuento.doubleValue()==0){
					double descuentoNota=cargo.getDescuentoNota()/100;
					if(descuentoNota==0){
						descuentoNota=cargo.getDescuentoFinanciero()/100;
						cargo.setDescuentoGeneral(descuentoNota);
						
					}
					CantidadMonetaria importeDescuento=cargo.getSaldoSinPagosCM();					
					importeDescuento=importeDescuento.multiply(descuentoNota);
					aplicacion.setImporteDescuento(importeDescuento.amount());
					importeAplicable=cargo.getTotalCM().subtract(importeDescuento).amount();
					importeAplicable=importeAplicable.subtract(aplicado);
				}else{
					importeAplicable=cargo.getTotal().subtract(aplicado);
				}
				if(importeAplicable.doubleValue()<=0)
					continue;
				if(disponible.doubleValue()>=importeAplicable.doubleValue()){
					aplicacion.setImporte(importeAplicable);
					disponible=disponible.subtract(importeAplicable);
				}else{
					aplicacion.setImporte(disponible);
					disponible=BigDecimal.ZERO;
				}
				aplicaciones.add(aplicacion);
			}
		}
		validate();
	}
	
	public void procesarCargo(final Cargo cargo){
		BigDecimal disponible=getDisponible();
		if(disponible.doubleValue()>0){
			Aplicacion aplicacion;
			if(getCurrentAbono() instanceof Pago)
				aplicacion=new AplicacionDePago();
			else
				aplicacion=new AplicacionDeNota();
			aplicacion.setCargo(cargo);
			aplicacion.setFecha(getAplicacionModel().getFecha());
			BigDecimal saldo=cargo.getSaldoCalculado();
			if(disponible.doubleValue()>=saldo.doubleValue()){
				aplicacion.setImporte(saldo);
				disponible=disponible.subtract(saldo);
			}else{
				aplicacion.setImporte(disponible);
				disponible=BigDecimal.ZERO;
			}
			aplicaciones.add(aplicacion);
		}
		validate();
	}
	
	public void eliminar(Aplicacion a){
		aplicaciones.remove(a);
	}

	public OrigenDeOperacion getOrigen() {
		return origen;
	}
	public void setOrigen(OrigenDeOperacion origen) {
		this.origen = origen;
	}

	protected void addValidation(PropertyValidationSupport support){
		if(aplicaciones.isEmpty())
			support.addError("Aplicaciones", "Sin aplicaciones");
	}
	
	/**
	 * Safe acces al disponible del abono selecciondo
	 * 
	 * @return
	 */
	public BigDecimal getCurrentDisponible(){
		if(getCurrentAbono()!=null)
			return getCurrentAbono().getDisponible();
		return BigDecimal.ZERO;
	}
	
	public void actualizarTotalYDisponible(){		
		BigDecimal val=BigDecimal.ZERO;
		for(Aplicacion a:aplicaciones){
			val=val.add(a.getImporte());
		}
		setValue("total", val);
		BigDecimal disp=getCurrentDisponible().subtract(val);
		setValue("disponible", disp);
	}
	
	public BigDecimal getDisponible(){
		BigDecimal disp=getCurrentDisponible();
		for(Aplicacion a:aplicaciones){
			disp=disp.subtract(a.getImporte());
		}
		return disp;
	}
	
	/**
	 * Asigna las aplicaciones al abono seleccionado y lo regresa
	 * para su posible persistencia 
	 * 
	 */
	public Abono procesar(){
		cargosAplicados.clear();
		//aplicaciones.clear();
		for(Aplicacion a:aplicaciones){
			a.setAutorizacion(getAutorizacion());
			aplicacionDeAbonoRules.generarAplicacion(getCurrentAbono(), a);			
			getCurrentAbono().agregarAplicacion(a);
			cargosAplicados.add(a.getCargo().getId());
		}
		
		return getCurrentAbono();
	}
	
	private List<String> cargosAplicados=new ArrayList<String>();
	
	public List<String> getCargosAfectados(){
		return cargosAplicados;
	}
	
	/**
	 * Restea el estado para volverlo a usar
	 * 
	 */
	public void reset(){
		//aplicaciones.clear();
		//disponibles.clear();
		setValue("abono", null);
		Cliente c=getCurrentCliente();
		setValue("cliente", null);
		setValue("cliente", c);
	}
	
	private class ListHandler implements ListEventListener{
		public void listChanged(ListEvent listChanges) {			
			while(listChanges.hasNext()){
				listChanges.next();				
			}
			actualizarTotalYDisponible();
		}		
	}
	
	private class AbonoHandler implements PropertyChangeListener{
		public void propertyChange(PropertyChangeEvent evt) {
			//actualizarTotalYDisponible();
			procesar();
		}		
	}
	private class ClienteHandler implements PropertyChangeListener{
		public void propertyChange(PropertyChangeEvent evt) {
			aplicaciones.clear();
		}
	}
	
	private class FechaHandler implements PropertyChangeListener{
		public void propertyChange(PropertyChangeEvent evt) {
			for(Aplicacion a:aplicaciones){
				a.setFecha(getAplicacionModel().getFecha());
			}
		}
	}

}
