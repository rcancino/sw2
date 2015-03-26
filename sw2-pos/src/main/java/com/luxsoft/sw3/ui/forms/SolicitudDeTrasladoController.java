package com.luxsoft.sw3.ui.forms;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.text.MessageFormat;
import java.util.Date;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.JOptionPane;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.log4j.Logger;

import ca.odell.glazedlists.BasicEventList;
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
import com.luxsoft.siipap.model.User;
import com.luxsoft.siipap.pos.ui.selectores.SelectorDeExistencias;
import com.luxsoft.siipap.service.KernellSecurity;
import com.luxsoft.siipap.swing.form2.DefaultFormModel;
import com.luxsoft.siipap.swing.utils.MessageUtils;
import com.luxsoft.sw3.services.Services;
import com.luxsoft.sw3.ui.services.KernellUtils;

/**
 * Controlador y PresentationModel para la fomra de mantenimiento de Solicitudes
 * de traslado
 * 
 * @author Ruben Cancino Ramos
 *
 */
public class SolicitudDeTrasladoController extends DefaultFormModel {
	
	private EventList<SolicitudDeTrasladoDet> partidasSource;
	
	
	protected Logger logger=Logger.getLogger(getClass());
	
	private ValueModel userModel;

	public SolicitudDeTrasladoController() {
		this(new SolicitudDeTraslado());
	}
	
	public SolicitudDeTrasladoController(SolicitudDeTraslado solicitud) {
		super(solicitud);
	}
	
	protected void init(){
		if(getValue("id")==null){
			//partidasSource=GlazedLists.eventList(new ArrayList<SolicitudDeTrasladoDet>());
			setValue("sucursal", Services.getInstance().getConfiguracion().getSucursal());
			setValue("fecha", Services.getInstance().obtenerFechaDelSistema());
			getModel("origen").addValueChangeListener(new PropertyChangeListener(){
				public void propertyChange(PropertyChangeEvent evt) {
					partidasSource.clear();
				}
			});
		}else{			
			//partidasSource=GlazedLists.eventList(getSolicitud().getPartidas());
		}	
		partidasSource=new BasicEventList<SolicitudDeTrasladoDet>();
		partidasSource.addListEventListener(new ListHandler());
		GlazedLists.syncEventListToList(partidasSource, getSolicitud().getPartidas());
		
		getUserModel().addValueChangeListener(new PropertyChangeListener() {			
			
			public void propertyChange(PropertyChangeEvent evt) {
				validate();
			}
		});
	}
	
	@Override
	protected void addValidation(PropertyValidationSupport support) {
		
		if(StringUtils.isBlank(getSolicitud().getComentario())){
			support.getResult().addError("El comentario es mandatorio");
		}
		
		if(getUser()==null){
			support.getResult().addError("Registre usuario solicitando");
			return;
		}if(StringUtils.isBlank(getSolicitud().getReferencia())){
			support.getResult().addError("La referencia es mandatoria");
			return;
		}
		if( (partidasSource!=null) && partidasSource.isEmpty()){
			support.getResult().addError("Registre las partidas a trasladar");
		}
	}	
	
	public EventList<SolicitudDeTrasladoDet> getPartidasSource() {
		return partidasSource;
	}	
	
	public SolicitudDeTraslado getSolicitud(){
		return (SolicitudDeTraslado)getBaseBean();
	}
	
	
	public void insertar(){
		
	}
	
	public void insertarBulk(JComponent parent){
		List<Existencia> selected=SelectorDeExistencias.find(getSolicitud().getOrigen(), getSolicitud().getFecha());
		if(!selected.isEmpty()){
			/*for(Existencia e:selected){
				double cantidad=0d;
				String pattern="Producto ({0})  {1} \n Disponible en {2} : {3}";
				String msg=MessageFormat.format(pattern
						,e.getProducto().getClave()
						,e.getDescripcion()
						,e.getSucursal().getNombre()
						,e.getCantidad()
						);
				String res=JOptionPane.showInputDialog(parent, msg,"Cantidad");
				cantidad=NumberUtils.toDouble(res);
				if(cantidad>0){
					SolicitudDeTrasladoDet det=new SolicitudDeTrasladoDet(e.getProducto(),cantidad);
					det.setSucursal(getSolicitud().getSucursal().getId());
					det.setOrigen(getSolicitud().getOrigen().getId());
					det.setRenglon(partidasSource.size()+1);
					partidasSource.add(det);
					//getSolicitud().agregarPartida(p, cantidad);
				}
			}*/
			
			for(Existencia e:selected){
				double cantidad=0d;
				SolicitudDeTrasladoDet det=new SolicitudDeTrasladoDet(e.getProducto(),cantidad);
				final DefaultFormModel model=new DefaultFormModel(det);
				SolicitudDeTrasladoDetForm form=new SolicitudDeTrasladoDetForm(model);
				form.open();
				if(!form.hasBeenCanceled()){
					
					 System.out.println("det:"+ det.getSolicitado()+"   "+det.getCortes()+"   "+det.getInstruccionesDecorte());
					    det.setSucursal(getSolicitud().getSucursal().getId());
						det.setOrigen(getSolicitud().getOrigen().getId());
						det.setRenglon(partidasSource.size()+1);
					partidasSource.add(det);
					
				}
				
			}
		}
	}
	
	public void elminarPartida(int index){
		if(!isReadOnly())
			partidasSource.remove(index);
		else{
			MessageUtils.showMessage("La solicitud no es modificable", "Solicitud de traslado");
		}
	}
	
	public boolean isReadOnly(){
		return (
				(getSolicitud().getReplicado()!=null)
			|| (getSolicitud().getAtendido()!=null)
			);
	}
	
	public void editar(int index){
		
	}
	
	public SolicitudDeTraslado persist(){
		SolicitudDeTraslado sol=getSolicitud();
		registrarBitacoras(sol);
		sol=Services.getInstance()
		.getSolicitudDeTrasladosManager()
		.save(getSolicitud());
		MessageUtils.showMessage("Solicitud generada: "+sol.getDocumento(), "Solicitud de traslado");
		return sol;
	}
	
	private void registrarBitacoras(SolicitudDeTraslado sol){
		Date time=Services.getInstance().obtenerFechaDelSistema();
		String user=getUser();
		if(sol.getId()==null){
			sol.getLog().setCreado(time);
			sol.getLog().setCreateUser(user);
			sol.getAddresLog().setCreatedIp(sol.getAddresLog().getUpdatedIp());
			sol.getAddresLog().setCreatedMac(KernellSecurity.getMacAdress());
		}
		sol.getLog().setModificado(time);	
		sol.getLog().setUpdateUser(user);
		sol.getAddresLog().setUpdatedIp(KernellSecurity.getIPAdress());
		sol.getAddresLog().setUpdatedMac(KernellSecurity.getMacAdress());
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
	
	



	private class ListHandler implements ListEventListener<SolicitudDeTrasladoDet>{

		
		public void listChanged(ListEvent<SolicitudDeTrasladoDet> listChanges) {
			while(listChanges.next()){}
			validate();
		}
		
	}
}
