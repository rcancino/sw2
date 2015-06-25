package com.luxsoft.sw3.ui.forms;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;

import javax.swing.JOptionPane;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.hibernate.Hibernate;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.stereotype.Service;

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
import com.luxsoft.siipap.inventarios.model.TrasladoDet;
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
public class TrasladoAutomaticoController extends DefaultFormModel {
	
	private EventList<TrasladoDet> partidasSource;
	
	private ValueModel choferHolder;
	
	private ValueModel cortadorHolder;
	private ValueModel surtidorHolder;
	private ValueModel supervisorHolder;
	
	private ValueModel userModel;
	
	private Date fecha;
	
	protected Logger logger=Logger.getLogger(getClass());
	
	
	
	public TrasladoAutomaticoController(Traslado traslado) {
		super(traslado);
	}
	
	protected void init(){
		
		
		fecha=Services.getInstance().obtenerFechaDelSistema();
		partidasSource=GlazedLists.eventList(getPartidas());
		partidasSource.addListEventListener(new ListHandler());
		GlazedLists.syncEventListToList(partidasSource, getPartidas());
		//actualizarExistencia();
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
		for(TrasladoDet det:partidasSource){
			int year=Periodo.obtenerYear(fecha);
			int mes=Periodo.obtenerMes(fecha)+1;
			Sucursal suc=det.getSucursal();
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
	
	public List<TrasladoDet> getPartidas(){
		 List<TrasladoDet> partidas;
		return partidas=Services.getInstance().getHibernateTemplate().find("from TrasladoDet t where  t.traslado.id=?",new Object[]{getTraslado().getId()});
	}
	
	
	public EventList<TrasladoDet> getPartidasSource() {
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

	public Traslado getTraslado(){
		return (Traslado)getBaseBean();
	}
	
	public boolean isReadOnly(){
		return (
			(getTraslado().getCfdi()!=null)
			);
	
	}		
	
	public Traslado persistir(){
		
			Date fecha=Services.getInstance().obtenerFechaDelSistema();
			Chofer chofer=(Chofer)choferHolder.getValue();
			String surtidor=surtidorHolder.getValue().toString();
			String supervisor=supervisorHolder.getValue().toString();
			
			String cortador=null;
			if(cortadorHolder.getValue()!=null)
				cortador=cortadorHolder.getValue().toString();
			Traslado t=getTraslado();
			
			t.setChofer(chofer.getNombre());
			t.setSurtidor(surtidor);
			t.setSuperviso(supervisor);
			t.setCortador(cortador);
			t=(Traslado)Services.getInstance().getHibernateTemplate().merge(t);
			
			MessageUtils.showMessage(" Generando CFDI para TPS"+t.getDocumento()
					, "Atención  de traslados");
			Traslado tps=t;
			CFDI cfdi=Services.getCFDITraslado().generar(tps);
			MessageUtils.showMessage("CFDI generado: "+cfdi, "Sistema CFDI");
			return t;
		
		
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
	

	
	
	private class ListHandler implements ListEventListener<TrasladoDet>{

		
		public void listChanged(ListEvent<TrasladoDet> listChanges) {
			while(listChanges.next()){

			}
			validate();
		}
		
	}
}
