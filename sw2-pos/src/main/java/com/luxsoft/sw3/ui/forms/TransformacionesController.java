package com.luxsoft.sw3.ui.forms;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Date;

import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.event.ListEvent;
import ca.odell.glazedlists.event.ListEventListener;
import ca.odell.glazedlists.swing.EventSelectionModel;

import com.jgoodies.validation.util.PropertyValidationSupport;
import com.luxsoft.siipap.inventarios.model.Transformacion;
import com.luxsoft.siipap.inventarios.model.TransformacionDet;
import com.luxsoft.siipap.inventarios.model.TransformacionModel;
import com.luxsoft.siipap.swing.form2.DefaultFormModel;
import com.luxsoft.sw3.services.Services;


public class TransformacionesController extends DefaultFormModel implements ListEventListener{
	
	private EventList<TransformacionDet> partidas;
	private EventSelectionModel selectionModel;

	public TransformacionesController() {
		super(Transformacion.class);
	}
	
	public Transformacion getTransformacion(){
		return (Transformacion)getBaseBean();
	}
	
	
	@Override
	protected void addValidation(PropertyValidationSupport support) {
		if(partidas.isEmpty())
			support.getResult().addError("No hay partidas de transformación");
		
	}

	public EventList<TransformacionDet> getPartidas() {
		return partidas;
	}

	public EventSelectionModel getSelectionModel() {
		return selectionModel;
	}

	@Override
	protected void init() {
		partidas=GlazedLists.eventList(getTransformacion().getPartidas());
		partidas.addListEventListener(this);
		selectionModel=new EventSelectionModel(partidas);
		setValue("fecha", new Date());//Services.getInstance().obtenerFechaDelSistema());
		setValue("sucursal", Services.getInstance().getConfiguracion().getSucursal());
		if(getValue("id")==null){
			getModel("clase").addValueChangeListener(new CompraHandler());
		}
	}
	
	@Override
	public void dispose() {
		partidas.removeListEventListener(this);
	}
	
	public void insert(){
		TransformacionModel tmodel=TransformacionDetForm.showForm();
		if(tmodel!=null){
			TransformacionDet salida=tmodel.toTransformaciones(getTransformacion());
			getTransformacion().agregarTransformacion(salida);
			getTransformacion().agregarTransformacion(salida.getDestino());
			partidas.add(salida);
			partidas.add(salida.getDestino());
		}
		
	}
	
	
	
	public void registrarClase() {
		
	}

	public void listChanged(ListEvent listChanges) {
		if(listChanges.next()){
			switch (listChanges.getType()) {
			case ListEvent.INSERT:
			case ListEvent.DELETE:
			case ListEvent.UPDATE:
				validate();
				break;
			default:
				break;
			}				
		}
	}
	
	/**
	 * Controla el comportamiento en la seleccion de compra
	 * 
	 * @author Ruben Cancino Ramos
	 *
	 */
	private class CompraHandler implements PropertyChangeListener{
		public void propertyChange(PropertyChangeEvent evt) {
			registrarClase();
		}		
	}

	public Transformacion persis() {
		return Services.getInstance()
			.getTransfomracionesManager()
			.save(getTransformacion());
	}

	
}
