package com.luxsoft.sw3.ui.forms;

import org.apache.log4j.Logger;

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.event.ListEvent;
import ca.odell.glazedlists.event.ListEventListener;

import com.jgoodies.validation.util.PropertyValidationSupport;
import com.luxsoft.siipap.inventarios.model.Conteo;
import com.luxsoft.siipap.inventarios.model.ConteoDet;
import com.luxsoft.siipap.model.core.Producto;
import com.luxsoft.siipap.swing.form2.DefaultFormModel;
import com.luxsoft.siipap.swing.utils.MessageUtils;
import com.luxsoft.sw3.services.Services;

/**
 * Controlador y PresentationModel para la fomra de devolucion de compras
 * 
 * @author Ruben Cancino Ramos
 *
 */
public class ConteoController extends DefaultFormModel {
	
	private EventList<ConteoDet> partidasSource;
	
	
	protected Logger logger=Logger.getLogger(getClass());
	
	public ConteoController() {
		super(new Conteo());
	}
	
	public ConteoController(Object bean, boolean readOnly) {
		super(bean, readOnly);
	}

	
	
	protected void init(){
		
		
		if(getConteo().getId()==null){
			getConteo().setSucursal(Services.getInstance().getConfiguracion().getSucursal());
			getConteo().setFecha(Services.getInstance().obtenerFechaDelSistema());
			
		}
		partidasSource=GlazedLists.eventList(getConteo().getPartidas());
		GlazedLists.syncEventListToList(partidasSource, getConteo().getPartidas());
		partidasSource.addListEventListener(new ListHandler());
	}
	
	@Override
	protected void addValidation(PropertyValidationSupport support) {
		if(getConteo().getSector()<=0){
			support.getResult().addError("Registre el sector");
		}
		
	}
	
	public EventList<ConteoDet> getPartidasSource() {
		return partidasSource;
	}	

	public Conteo getConteo(){
		return (Conteo)getBaseBean();
	}
	
	
	public Conteo persistir(){	
		getConteo().setReplicado(null);
		getConteo().setImportado(null);
		Conteo d=Services.getInstance().getInventariosManager().registrarConteo(getConteo());
		MessageUtils.showMessage("Captura de inventario actualizada:\n Sector: "+d.getSector()+"  Folio: "+d.getDocumento()
				, "Conteo de inventario");
		return d;
		
	}
	
	private class ListHandler implements ListEventListener{
		public void listChanged(ListEvent listChanges) {
			while(listChanges.next()){}
			validate();
		}
	}
	
	public void insertarPartida(){
		
		ConteoDet det=new ConteoDet();
		det.setConteo(getConteo());
		final DefaultFormModel model=new DefaultFormModel(det);
		ConteoDetForm form=new ConteoDetForm(model);
		form.setProductos(getProductos());
		form.open();
		if(!form.hasBeenCanceled()){
			partidasSource.add(det);
			
		}
	}

	public void elminarPartida(int index) {
		ConteoDet det=partidasSource.get(index);
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
