package com.luxsoft.sw3.ui.forms;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Date;

import javax.swing.JOptionPane;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.event.ListEvent;
import ca.odell.glazedlists.event.ListEventListener;

import com.jgoodies.binding.value.ValueHolder;
import com.jgoodies.binding.value.ValueModel;
import com.jgoodies.validation.util.PropertyValidationSupport;
import com.luxsoft.siipap.inventarios.model.Existencia;
import com.luxsoft.siipap.inventarios.model.SolicitudDeTraslado;
import com.luxsoft.siipap.inventarios.model.SolicitudDeTrasladoDet;
import com.luxsoft.siipap.inventarios.model.Traslado;
import com.luxsoft.siipap.model.Periodo;
import com.luxsoft.siipap.model.Sucursal;
import com.luxsoft.siipap.model.User;
import com.luxsoft.siipap.model.core.Producto;
import com.luxsoft.siipap.swing.form2.DefaultFormModel;
import com.luxsoft.siipap.swing.utils.MessageUtils;
import com.luxsoft.sw3.cfdi.model.CFDI;
import com.luxsoft.sw3.embarque.Chofer;
import com.luxsoft.sw3.services.Services;
import com.luxsoft.sw3.ui.services.KernellUtils;

/**
 * Controlador y PresentationModel para la fomra atención de solicitudes de traslados
 * 
 * @author Ruben Cancino Ramos
 *
 */
public class TrasladoController extends DefaultFormModel {
	
	private EventList<SolicitudDeTrasladoDet> partidasSource;
	
	private ValueModel choferHolder;
	
	private ValueModel cortadorHolder;
	private ValueModel surtidorHolder;
	private ValueModel supervisorHolder;
	
	private ValueModel userModel;
	
	private Date fecha;
	
	protected Logger logger=Logger.getLogger(getClass());
	
	
	public TrasladoController(SolicitudDeTraslado solicitud) {
		super(solicitud);
	}
	
	protected void init(){
		fecha=Services.getInstance().obtenerFechaDelSistema();
		partidasSource=GlazedLists.eventList(getSolicitud().getPartidas());
		partidasSource.addListEventListener(new ListHandler());
		GlazedLists.syncEventListToList(partidasSource, getSolicitud().getPartidas());
		actualizarExistencia();
		choferHolder=new ValueHolder(null);
		choferHolder.addValueChangeListener(new PropertyChangeListener(){
			public void propertyChange(PropertyChangeEvent evt) {
				validate();
			}
		});
		getUserModel().addValueChangeListener(new PropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent evt) {
				validate();
			}
		});
		
		final PropertyChangeListener logList=new PropertyChangeListener() {
			
			@Override
			public void propertyChange(PropertyChangeEvent evt) {
				validate();
			}
		};
		surtidorHolder=new ValueHolder(null);
		cortadorHolder=new ValueHolder(null);
		supervisorHolder=new ValueHolder(null);
		
		surtidorHolder.addValueChangeListener(logList);
		cortadorHolder.addValueChangeListener(logList);
		supervisorHolder.addValueChangeListener(logList);
	}
	
	private void actualizarExistencia(){
		for(SolicitudDeTrasladoDet det:partidasSource){
			int year=Periodo.obtenerYear(fecha);
			int mes=Periodo.obtenerMes(fecha)+1;
			Sucursal suc=getSolicitud().getOrigen();
			Producto prod=det.getProducto();
			Existencia exis=Services.getInstance()
				.getInventariosManager()
				.buscarExistencia(suc, prod, year, mes);
			if(exis!=null){
				det.setExistencia(exis.getCantidad());
			}
		}
	}
	
	@Override
	protected void addValidation(PropertyValidationSupport support) {
		
		double recibido=0;		
		for(SolicitudDeTrasladoDet det:partidasSource){
			
			//Validamos la existencia
			/*
			if(det.getExistencia()<=0){
				support.addError("", "No hay existencia disponible para el producto: "+det.getProducto().getClave());
				continue;
			}
			*/
			recibido=recibido+det.getRecibido();
			if(det.getRecibido()<0 ){
				support.addError("","Cantidad para envio es incorrecta para el producto: "+det.getProducto());
				continue;
			}
			
			/*
			// No mas de lo solicitado
			if(det.getRecibido()>det.getSolicitado()){
				support.addError("","Asignación mayor a lo solicitado para: "+det.getProducto());
				continue;
			}
			*/
		}
		
		if(recibido==0 ){
			support.addError("","Debe atender por lo menos una partida: ");
			
		}
		
		Chofer chofer=(Chofer)getChoferHolder().getValue();
		if(chofer==null){
			support.addError("","El chofer es mandatorio");
		}
		if(getUser()==null){
			support.getResult().addError("Registre usuario que atiende");
			return;
		}
		
		if(surtidorHolder.getValue()==null){
			support.getResult().addError("Debe registrar el surtidor");
			return;
		}
		
		if(supervisorHolder.getValue()==null){
			support.getResult().addError("Debe registrar el supervisor");
			return;
		}
		
		
	}	
	
	public EventList<SolicitudDeTrasladoDet> getPartidasSource() {
		return partidasSource;
	}	
	
	public ValueModel getChoferHolder() {
		return choferHolder;
	}

	public void setChoferHolder(ValueModel choferHolder) {
		this.choferHolder = choferHolder;
	}
	

	public ValueModel getCortadorHolder() {
		return cortadorHolder;
	}

	
	public ValueModel getSurtidorHolder() {
		return surtidorHolder;
	}

	public ValueModel getSupervisorHolder() {
		return supervisorHolder;
	}

	public SolicitudDeTraslado getSolicitud(){
		return (SolicitudDeTraslado)getBaseBean();
	}
	
	public boolean isReadOnly(){
		return (
			(getSolicitud().getAtendido()!=null)
			);
	}		
	
	public Traslado[] persistir(){
		if(getSolicitud().getAtendido()==null){
			Date fecha=Services.getInstance().obtenerFechaDelSistema();
			Chofer chofer=(Chofer)choferHolder.getValue();
			String surtidor=surtidorHolder.getValue().toString();
			String supervisor=supervisorHolder.getValue().toString();
			
			String cortador=null;
			if(cortadorHolder.getValue()!=null)
				cortador=cortadorHolder.getValue().toString();
			Traslado[] t=Services.getInstance().getInventariosManager()
					.generarSalidaPorTraslado(getSolicitud(),fecha,chofer.getNombre(),getUser(),surtidor,supervisor,cortador);
			MessageUtils.showMessage("Traslado generado:\n "+t[0].getDocumento()+"\n Generando CFDI para TPS"
					, "Atención  de traslados");
			Traslado tps=t[0];
			CFDI cfdi=Services.getCFDITraslado().generar(tps);
			MessageUtils.showMessage("CFDI generado: "+cfdi, "Sistema CFDI");
			return t;
		}
		return null;
		
	}
	
	public  String getUser(){
		if(getUserModel().getValue()!=null){
			String password=(String)getUserModel().getValue();
			User user=KernellUtils.buscarUsuarioPorPassword(password);	
			if(user==null)
				return null;
			return StringUtils.abbreviate(user.getFullName(), 255);
		}
		return null;
	}
	
	public ValueModel getUserModel() {
		if(userModel==null)
			userModel=new ValueHolder(null);
		return userModel;
	}

	public void setUserModel(ValueModel userModel) {
		this.userModel = userModel;
	}
	
	private void warningDeDif(){
		for(SolicitudDeTrasladoDet det:partidasSource){
			if(det.getRecibido()==0){
				continue;
			}else{
				if(det.getRecibido() < det.getSolicitado() || det.getRecibido() > det.getSolicitado()) {
					JOptionPane.showMessageDialog(null,"Cantidad diferente a lo solicitado para: "+det.getProducto()+
							                      "\n Solicitado: "+ det.getSolicitado() + "  Recibido: "+ det.getRecibido()   );
				}
			}
			
		}
	}
	
	
	private class ListHandler implements ListEventListener<SolicitudDeTrasladoDet>{

		
		public void listChanged(ListEvent<SolicitudDeTrasladoDet> listChanges) {
			while(listChanges.next()){
				warningDeDif();
			}
			validate();
		}
		
	}
}
