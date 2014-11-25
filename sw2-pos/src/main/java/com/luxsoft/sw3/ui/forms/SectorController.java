package com.luxsoft.sw3.ui.forms;

import org.apache.log4j.Logger;

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.event.ListEvent;
import ca.odell.glazedlists.event.ListEventListener;

import com.jgoodies.validation.util.PropertyValidationSupport;

import com.luxsoft.siipap.inventarios.model.Sector;
import com.luxsoft.siipap.inventarios.model.SectorDet;
import com.luxsoft.siipap.model.core.Producto;
import com.luxsoft.siipap.swing.form2.DefaultFormModel;
import com.luxsoft.siipap.swing.utils.MessageUtils;
import com.luxsoft.sw3.maquila.model.EntradaDeMaterialDet;
import com.luxsoft.sw3.services.Services;

/**
 * Controlador y PresentationModel para la fomra de devolucion de compras
 * 
 * @author Ruben Cancino Ramos
 *
 */
public class SectorController extends DefaultFormModel {
	
	private EventList<SectorDet> partidasSource;
	
	
	protected Logger logger=Logger.getLogger(getClass());
	
	public SectorController() {
		super(new Sector());
	}
	
	public SectorController(Object bean, boolean readOnly) {
		super(bean, readOnly);
	}

	
	
	protected void init(){
		
		
		if(getSector().getId()==null){
			getSector().setSucursal(Services.getInstance().getConfiguracion().getSucursal());
			getSector().setFecha(Services.getInstance().obtenerFechaDelSistema());
			
		}
		partidasSource=GlazedLists.eventList(getSector().getPartidas());
		GlazedLists.syncEventListToList(partidasSource, getSector().getPartidas());
		partidasSource.addListEventListener(new ListHandler());
	}
	
	@Override
	protected void addValidation(PropertyValidationSupport support) {
		if(getSector().getSector()<=0){
			support.getResult().addError("Registre el sector");
		}
		
	}
	
	public EventList<SectorDet> getPartidasSource() {
		return partidasSource;
	}	

	public Sector getSector(){
		return (Sector)getBaseBean();
	}
	
	
	public Sector persistir(){	
		//getSector().setReplicado(null);
		//getSector().setImportado(null);
		Sector d=Services.getInstance().getInventariosManager().registrarSector(getSector());
		/*MessageUtils.showMessage("Captura de inventario actualizada:\n Sector: "+d.getSector()+"  Folio: "
				, "Conteo de inventario");*/
		return d;
		
	}
	
	private class ListHandler implements ListEventListener{
		public void listChanged(ListEvent listChanges) {
			while(listChanges.next()){}
			validate();
		}
	}
	
	public void insertarPartida(){
		
		SectorDet det=new SectorDet();
		det.setSector(getSector());
		final DefaultFormModel model=new DefaultFormModel(det);
		SectorDetForm form=new SectorDetForm(model);
		form.setProductos(getProductos());
		form.open();
		if(!form.hasBeenCanceled()){
			
			boolean ok=true;
			for(SectorDet sd : getSector().getPartidas()){
				
				 if(sd.getClave().equals(det.getClave())){
					 ok=false;
				 }
			 }
		   if(ok){
			   partidasSource.add(det);   
		   }
		   else{
			   MessageUtils.showMessage("Ya esta registrado el producto: "+det.getClave(), "");   
		   }
			
		   
			
		}
	}


	
/*EntradaDeMaterialDet det=(EntradaDeMaterialDet)model.getBaseBean();
			boolean ok=getRecepcion().agregarEntrada(det);
			if(ok){
				afterInserPartida(det);
				partidasSource.add(det);
			}else{
				MessageUtils.showMessage("Ya esta registrado el producto: "+det.getProducto(), "Recepción de bobinas");
			}*/
	
	public void elminarPartida(int index) {
		SectorDet det=partidasSource.get(index);
		partidasSource.remove(index);
		/*
		boolean res=getConteo().getPartidas().remove(det);
		if(res){
			
		}else{
			throw new RuntimeException("No localizo la partida: "+index+ " en las partidas de la Dec");
		}*/
	}	

	private EventList<Producto> productos;
	
	public EventList<Producto> getProductos(){
		if(productos==null || productos.isEmpty()){
			productos=GlazedLists.eventList(Services.getInstance().getProductosManager().getAll());
		}
		return productos;
	}
	
	public void dispose(){
		if(productos!=null)
			productos.clear();
	}
	
}
