package com.luxsoft.siipap.pos.ui.venta.forms;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Date;

import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.SortedList;
import ca.odell.glazedlists.event.ListEvent;
import ca.odell.glazedlists.event.ListEventListener;
import ca.odell.glazedlists.swing.EventSelectionModel;

import com.jgoodies.validation.util.PropertyValidationSupport;
import com.luxsoft.siipap.model.Sucursal;
import com.luxsoft.siipap.model.core.Cliente;
import com.luxsoft.siipap.service.KernellSecurity;
import com.luxsoft.siipap.swing.form2.DefaultFormModel;
import com.luxsoft.siipap.swing.utils.MessageUtils;
import com.luxsoft.siipap.ventas.model.Devolucion;
import com.luxsoft.siipap.ventas.model.DevolucionDeVenta;
import com.luxsoft.siipap.ventas.model.VentaDet;
import com.luxsoft.sw3.services.Services;

/**
 * Estado y comportamiento para la generacion de Devoluciones
 * 
 * @author Ruben Cancino Ramos
 *
 */
public class DevolucionController extends DefaultFormModel implements ListEventListener{
	
	private EventList<DevolucionDeVenta> partidas;
	private EventSelectionModel<DevolucionDeVenta> selectionModel;
	private Cliente cliente;
	private Sucursal sucursal;
	 
	
	public DevolucionController() {
		super(Devolucion.class);
		getModel("venta").addValueChangeListener(new PropertyChangeListener(){
			public void propertyChange(PropertyChangeEvent evt) {
				generarPartidas();
			}
		});
		sucursal=Services.getInstance().getConfiguracion().getSucursal();
	}
	
	public Devolucion getDevolucion(){
		return (Devolucion)getBaseBean();
	}
	
	public Sucursal getSucursal() {
		return sucursal;
	}
	

	public void setSucursal(Sucursal sucursal) {
		this.sucursal = sucursal;
	}

	@Override
	protected void addValidation(PropertyValidationSupport support) {
		if(getDevolucion().getVenta()==null){
			support.getResult().addError("Seleccione la factura a regresar");
		}
		for(DevolucionDeVenta det:partidas){
			double vendido=Math.abs(det.getVentaDet().getCantidad());
			double devuelto=det.getCantidad();
			if(devuelto<0)
				support.getResult().addError("La cantidad devuelta no puede ser negativa: "+det.getClave());
			if(devuelto>vendido)
				support.getResult().addError("La cantidad devuelta no puede ser mayor a la vendida: "+det.getClave());
		}
		double devuelto=0;
		for(DevolucionDeVenta det:partidas){
			devuelto+=det.getCantidad();
			
		}
		if(devuelto==0){
			support.getResult().addError("Registre por lo menos una partida de devolución");
		}
	}

	protected void init(){
		partidas=GlazedLists.eventList(getDevolucion().getPartidas());
		partidas=new SortedList<DevolucionDeVenta>(partidas,null);
		partidas.addListEventListener(this);
		selectionModel=new EventSelectionModel<DevolucionDeVenta>(partidas);
		
	}
	
	public EventList<DevolucionDeVenta> getPartidas() {
		return partidas;
	}

	public EventSelectionModel<DevolucionDeVenta> getSelectionModel() {
		return selectionModel;
	}

	protected void doUpdate(){
		validate();
	}
	
	public void generarPartidas(){
		if(partidas.isEmpty()){
			for(VentaDet det:getDevolucion().getVenta().getPartidas()){
				DevolucionDeVenta dv=new DevolucionDeVenta();
				dv.setVentaDet(det);
				dv.setCantidad(0);
				//getDevolucion().agregarPartida(dv);
				partidas.add(dv);
				
			}
		}
		System.out.println("Partidas list: "+partidas.size());
		System.out.println("Partidas dev : "+getDevolucion().getPartidas().size());
		System.out.println("Partidas venta:"+getDevolucion().getVenta().getPartidas().size());
		
	}
	
	public Devolucion persist(){
		Date fecha=Services.getInstance().obtenerFechaDelSistema();		
		Devolucion dev= getDevolucion();
		registrarLog(dev);
		dev.setFecha(fecha);
		for(DevolucionDeVenta det:partidas){
			if(det.getCantidad()>0){
				dev.agregarPartida(det);
			}
		}
		//return(Devolucion) Services.getInstance().getUniversalDao().save(dev);
		Devolucion res= Services.getInstance().getInventariosManager().salvarDevolucion(dev);
		MessageUtils.showMessage("RMD Generado: "+res.getNumero(), "Devolución de ventas");
		return res;
	}
	
	private void registrarLog(final Devolucion v){
		try {
			Date creado=Services.getInstance().obtenerFechaDelSistema();
			String user=KernellSecurity.instance().getCurrentUserName();
			v.getLog().setCreado(creado);
			v.getLog().setModificado(creado);
			v.getLog().setCreateUser(user);
			v.getLog().setUpdateUser(user);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public Cliente getCliente() {
		return cliente;
	}

	public void setCliente(Cliente cliente) {
		this.cliente = cliente;
	}

	public void listChanged(ListEvent listChanges) {
		if(listChanges.next()){
			switch (listChanges.getType()) {
			case ListEvent.INSERT:
			case ListEvent.DELETE:
				
				break;
			case ListEvent.UPDATE:
				int index=listChanges.getIndex();
				doUpdate();
				break;
			default:
				break;
			}				
		}
	}
	
	

}
