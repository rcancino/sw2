package com.luxsoft.sw3.ui.forms;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Iterator;

import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.ObservableElementList;
import ca.odell.glazedlists.event.ListEvent;
import ca.odell.glazedlists.event.ListEventListener;

import com.jgoodies.validation.util.PropertyValidationSupport;
import com.luxsoft.siipap.compras.model.Compra2;
import com.luxsoft.siipap.compras.model.CompraUnitaria;
import com.luxsoft.siipap.compras.model.EntradaPorCompra;
import com.luxsoft.siipap.compras.model.RecepcionDeCompra;
import com.luxsoft.siipap.model.Sucursal;
import com.luxsoft.siipap.swing.form2.DefaultFormModel;
import com.luxsoft.siipap.swing.utils.MessageUtils;
import com.luxsoft.sw3.services.Services;


public class RecepcionDeCompraController extends DefaultFormModel implements ListEventListener{
	
	private EventList<EntradaPorCompra> partidas;
	
	private final Sucursal sucursal;

	public RecepcionDeCompraController() {
		super(RecepcionDeCompra.class);
		this.sucursal=Services.getInstance().getConfiguracion().getSucursal();
	}
	
	public RecepcionDeCompra getRecepcion(){
		return (RecepcionDeCompra)getBaseBean();
	}
	public Compra2 getCompra(){
		return getRecepcion().getCompra();
	}
	
	@Override
	protected void addValidation(PropertyValidationSupport support) {
		double total=0;
		for(EntradaPorCompra e:partidas){
			total+=e.getCantidad();
			if(e.getCantidad()<0){
				//support.getResult().addWarning("Existen entradas en cero : "+e.getClave());
				support.getResult().addError("La cantidad por recibir no puede ser <0");
			}
			
		}
		if(total<=0){
			support.getResult().addError("No se permite salvar la recepción sin partidas");
		}
		
	}

	public EventList<EntradaPorCompra> getPartidas() {
		return partidas;
	}

	@Override
	protected void init() {
		final EventList source=GlazedLists.eventList(getRecepcion().getPartidas());
		partidas=new ObservableElementList<EntradaPorCompra>(source
				,GlazedLists.beanConnector(EntradaPorCompra.class));
		partidas.addListEventListener(this);
		setValue("fecha", Services.getInstance().obtenerFechaDelSistema());
		setValue("sucursal", Services.getInstance().getConfiguracion().getSucursal());
		if(getRecepcion().getId()==null){
			getModel("compra").addValueChangeListener(new CompraHandler());
		}
	}
	
	private void registrarEntradas(){
		if(getCompra()!=null){
			if(!getPartidas().isEmpty())
				getPartidas().clear();
			for(CompraUnitaria cdet:getCompra().getPartidas()){
				if(!cdet.getSucursal().equals(sucursal))
					if(!cdet.getSucursal().getId().equals(new Long(1)))
						continue;
				EntradaPorCompra entrada=new EntradaPorCompra();
				entrada.setCompraDet(cdet);
				entrada.setSucursal(getRecepcion().getSucursal());
				entrada.setProducto(cdet.getProducto());
				entrada.setRenglon(0);
				entrada.setLargo(cdet.getLargo());
				entrada.setAncho(cdet.getAncho());
				entrada.setEspecial(cdet.isEspecial());
				entrada.actualizarDescripcion();
				getRecepcion().registrarEntrada(entrada);
				partidas.add(entrada);
			}
		}
	}
	
	@Override
	public void dispose() {
		partidas.removeListEventListener(this);
	}
	
	protected void doUpdate(){
		validate();
	}
	
	public RecepcionDeCompra persist(){
		RecepcionDeCompra r=getRecepcion();
		//Long docto=Services.getInstance().getComprasManager().buscarFolio(r);
		//r.setDocumento(docto);
		Iterator<EntradaPorCompra> iterator=r.getPartidas().iterator();
		while(iterator.hasNext()){
			EntradaPorCompra e=(EntradaPorCompra)iterator.next();
			//e.setDocumento(docto);
			if(e.getCantidad()<=0)
				iterator.remove();
		}
		RecepcionDeCompra res= Services.getInstance()
			.getComprasManager()
			.registrarRecepcion(r);
		MessageUtils.showMessage("COM Generada: "+res.getDocumento(), "Recepcuón de compras");
		return res;
	}

	public void listChanged(ListEvent listChanges) {
		if(listChanges.next()){
			switch (listChanges.getType()) {
			case ListEvent.INSERT:
			case ListEvent.DELETE:
				
				break;
			case ListEvent.UPDATE:
				doUpdate();
				int index=listChanges.getIndex();				
				EntradaPorCompra e=this.partidas.get(index);
				if(e.getRenglon()==0)
					e.setRenglon(getMax());
				//System.out.println("Partida actualizda: "+e+ " Rngl:"+e.getRenglon()+ " Cantidad:"+e.getCantidadEnUnidad());
				break;
			default:
				break;
			}				
		}
	}
	
	private int getMax(){
		int max=0;
		for(EntradaPorCompra e:partidas){
			if(e.getRenglon()>max)
				max=e.getRenglon();
		}
		return ++max;
	}
	
	/**
	 * Controla el comportamiento en la seleccion de compra
	 * 
	 * @author Ruben Cancino Ramos
	 *
	 */
	private class CompraHandler implements PropertyChangeListener{
		public void propertyChange(PropertyChangeEvent evt) {
			registrarEntradas();
		}		
	}
}
