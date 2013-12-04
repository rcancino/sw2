package com.luxsoft.siipap.pos.ui.venta.forms;

import java.util.Date;
import java.util.List;

import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.ListSelection;
import ca.odell.glazedlists.SortedList;
import ca.odell.glazedlists.UniqueList;
import ca.odell.glazedlists.event.ListEvent;
import ca.odell.glazedlists.event.ListEventListener;
import ca.odell.glazedlists.swing.EventSelectionModel;

import com.jgoodies.validation.util.PropertyValidationSupport;
import com.luxsoft.siipap.model.Periodo;
import com.luxsoft.siipap.model.Sucursal;
import com.luxsoft.siipap.model.core.Cliente;
import com.luxsoft.siipap.pos.ui.selectores.SelectorDeFacturas;
import com.luxsoft.siipap.service.KernellSecurity;
import com.luxsoft.siipap.swing.form2.DefaultFormModel;
import com.luxsoft.siipap.swing.utils.MessageUtils;
import com.luxsoft.siipap.ventas.model.DevolucionDeVenta;
import com.luxsoft.siipap.ventas.model.PreDevolucion;
import com.luxsoft.siipap.ventas.model.PreDevolucionDet;
import com.luxsoft.siipap.ventas.model.Venta;
import com.luxsoft.siipap.ventas.model.VentaDet;
import com.luxsoft.sw3.services.Services;

/**
 * Estado y comportamiento para la generacion de Devoluciones
 * 
 * @author Ruben Cancino Ramos
 *
 */
public class PreDevolucionController extends DefaultFormModel implements ListEventListener{
	
	private EventList<PreDevolucionDet> partidas;
	private EventSelectionModel<PreDevolucionDet> selectionModel;
	 
	
	public PreDevolucionController() {
		super(PreDevolucion.class);
		getPreDevolucion().setSucursal(Services.getInstance().getConfiguracion().getSucursal());
	}
	
	public PreDevolucion getPreDevolucion(){
		return (PreDevolucion)getBaseBean();
	}
	
	
	@Override
	protected void addValidation(PropertyValidationSupport support) {
		
		for(PreDevolucionDet det:partidas){
			double disponible=Math.abs(det.getDisponible());
			double devuelto=det.getCantidad();
			if(devuelto<0)
				support.getResult().addError("La cantidad devuelta no puede ser negativa: "+det.getClave());
			if(devuelto>disponible)
				support.getResult().addError("La cantidad devuelta no puede ser mayor a lo disponible para regresar: "+det.getClave());
			
		}
		double devuelto=0;
		for(PreDevolucionDet det:partidas){
			devuelto+=det.getCantidad();
			
		}
		if(devuelto==0){
			support.getResult().addError("Registre por lo menos una partida de devolución");
		}
	}
	
	public void registrarVenta(final Venta venta){
		for(VentaDet det:venta.getPartidas()){
			PreDevolucionDet dv=new PreDevolucionDet();
			dv.setVentaDet(det);			
			dv.setDevueltas(calcularDevueltasConAnterioridad(det));			
			dv.setCantidad(dv.getDisponible());
			partidas.add(dv);
			
		}
		validate();
		getPreDevolucion().setVenta(venta);
	}

	protected void init(){
		partidas=GlazedLists.eventList(getPreDevolucion().getPartidas());
		partidas=new UniqueList(partidas,GlazedLists.beanPropertyComparator(PreDevolucionDet.class, "ventaDet.id"));
		partidas=new SortedList<PreDevolucionDet>(partidas,null);
		partidas.addListEventListener(this);
		selectionModel=new EventSelectionModel<PreDevolucionDet>(partidas);
		
	}
	
	public EventList<PreDevolucionDet> getPartidas() {
		return partidas;
	}

	public EventSelectionModel<PreDevolucionDet> getSelectionModel() {
		return selectionModel;
	}

	protected void doUpdate(){
		validate();
	}
	
	public void seleccionarVenta(){
		if(getPreDevolucion().getCliente()==null){
			MessageUtils.showMessage("Seleccione primero al cliente", "Selector de ventas");
			return;
		}
		SelectorDeFacturas selector=new SelectorDeFacturas(){
			protected List<Venta> getData() {
				return buscarVentas(periodo);
			}
		};
		selector.setSelectionMode(ListSelection.SINGLE_SELECTION);
		selector.setCliente(getPreDevolucion().getCliente());
		selector.open();
		if(!selector.hasBeenCanceled()){
			Venta v= selector.getSelected();
			v= Services.getInstance().getFacturasManager().buscarVentaInicializada(v.getId());
			registrarVenta(v);
		}		
	}
	
	public List<Venta> buscarVentas(final Periodo periodo){		
		String hql="from Venta v " +
		"where v.sucursal.id=? " +		
		"  and v.fecha between ? and ? " +
		"  and v.cliente.clave=? " +
		"  and v.id not in(select d.venta.id from Devolucion " +
		"					d where d.venta.cliente.clave=?)";
		Object[] params={
				getPreDevolucion().getSucursal().getId()
				,periodo.getFechaInicial()
				,periodo.getFechaFinal()
				,getPreDevolucion().getCliente().getClave()
				,getPreDevolucion().getCliente().getClave()
				};
		List<Venta> res= Services.getInstance().getHibernateTemplate().find(hql,params);
		return res;
	}
	
	private double calcularDevueltasConAnterioridad(final VentaDet det){
		String hql="select sum(d.cantidad) from PreDevolucionDet d where d.ventaDet.id=?";
		Number res=(Number)Services.getInstance().getHibernateTemplate().find(hql,det.getId()).get(0);
		return res!=null?res.doubleValue():0.0;
	}
	
	public PreDevolucion persist(){
		Date fecha=Services.getInstance().obtenerFechaDelSistema();		
		PreDevolucion dev= getPreDevolucion();
		registrarLog(dev);
		dev.setFecha(fecha);
		for(PreDevolucionDet det:partidas){
			if(det.getCantidad()>0){
				dev.agregarPartida(det);
			}
		}		
		PreDevolucion res= Services.getInstance().getInventariosManager().salvarPreDevolucion(dev);
		MessageUtils.showMessage("Pre Devolucion Generada: "+res.getDocumento(), "Pre Devolución de ventas");
		return res;
	}
	
	private void registrarLog(final PreDevolucion v){
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
