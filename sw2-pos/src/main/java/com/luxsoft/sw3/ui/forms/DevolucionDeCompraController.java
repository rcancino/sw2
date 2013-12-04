package com.luxsoft.sw3.ui.forms;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.ObservableElementList;
import ca.odell.glazedlists.event.ListEvent;
import ca.odell.glazedlists.event.ListEventListener;

import com.jgoodies.validation.util.PropertyValidationSupport;
import com.luxsoft.siipap.compras.model.DevolucionDeCompra;
import com.luxsoft.siipap.compras.model.DevolucionDeCompraDet;
import com.luxsoft.siipap.model.core.Producto;
import com.luxsoft.siipap.pos.ui.selectores.SelectorDeProductos2;
import com.luxsoft.siipap.swing.form2.DefaultFormModel;
import com.luxsoft.siipap.swing.utils.MessageUtils;
import com.luxsoft.sw3.services.Services;

/**
 * Controlador y PresentationModel para la fomra de devolucion de compras
 * 
 * @author Ruben Cancino Ramos
 *
 */
public class DevolucionDeCompraController extends DefaultFormModel {
	
	private EventList<DevolucionDeCompraDet> partidasSource;
	
	
	protected Logger logger=Logger.getLogger(getClass());
	
	
	public DevolucionDeCompraController() {
		super(new DevolucionDeCompra());
		getDevolucion().setSucursal(Services.getInstance().getConfiguracion().getSucursal());
		getDevolucion().setFecha(Services.getInstance().obtenerFechaDelSistema());
		getModel("proveedor").addValueChangeListener(new PropertyChangeListener(){
			public void propertyChange(PropertyChangeEvent evt) {
				partidasSource.clear();
			}			
		});
		partidasSource=new BasicEventList<DevolucionDeCompraDet>();
		partidasSource=new ObservableElementList<DevolucionDeCompraDet>(partidasSource, GlazedLists.beanConnector(DevolucionDeCompraDet.class));
		partidasSource.addListEventListener(new ListHandler());
	}
	
	
	@Override
	protected void addValidation(PropertyValidationSupport support) {
		if(StringUtils.isBlank(getDevolucion().getUsuario())){
			support.getResult().addError("Digite su usuario");
		}
		if(StringUtils.isBlank(getDevolucion().getComentario())){
			support.getResult().addError("Digite un comentario");
		}
		if(getDevolucion().getPartidas().isEmpty()){
			support.getResult().addError("Seleccione partidas a devolver");
		}
		for(DevolucionDeCompraDet det:getDevolucion().getPartidas()){
			if(det.getCantidad()<=0 ){
				support.getResult().addError("Cantidad incorrecta para la partida del renglon: "+det.getRenglon());
				return;
			}
		}
		
	}
	
	public EventList<DevolucionDeCompraDet> getPartidasSource() {
		return partidasSource;
	}	

	public DevolucionDeCompra getDevolucion(){
		return (DevolucionDeCompra)getBaseBean();
	}
	
	public boolean isReadOnly(){
		return (
			(getValue("id")!=null)
			);
	}
	
	
	
	public DevolucionDeCompra persistir(){		
		DevolucionDeCompra d=Services.getInstance().getInventariosManager().registrarDevolucionDeCompra(getDevolucion());
		MessageUtils.showMessage("Devolución de compra generada:\n "+d.getDocumento()
				, "Devolución de Compras");
		return d;
		
	}
	
	private class ListHandler implements ListEventListener{
		public void listChanged(ListEvent listChanges) {
			while(listChanges.next()){}
			validate();
		}
		
	}
	
	public void insertarPartida(){
		DevolucionDeCompraDet det=new DevolucionDeCompraDet();
		det.setSucursal(getDevolucion().getSucursal());
		final DefaultFormModel model=new DefaultFormModel(det);
		DevolucionDeCompraDetForm form=new DevolucionDeCompraDetForm(model);
		form.setProductos(getProductos());
		form.open();
		if(!form.hasBeenCanceled()){
			if(getDevolucion().agregarPartida(det)){
				partidasSource.add(det);
			}
		}
	}

	public void elminarPartida(int index) {
		DevolucionDeCompraDet det=partidasSource.get(index);
		boolean res=getDevolucion().getPartidas().remove(det);
		if(res){
			partidasSource.remove(index);
		}else{
			throw new RuntimeException("No localizo la partida: "+index+ " en las partidas de la Dec");
		}
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
