package com.luxsoft.siipap.pos.ui.forms;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.math.BigDecimal;
import java.util.Date;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.GlazedLists;

import com.jgoodies.binding.value.ValueHolder;
import com.jgoodies.binding.value.ValueModel;
import com.jgoodies.validation.util.PropertyValidationSupport;
import com.luxsoft.siipap.model.User;
import com.luxsoft.siipap.swing.form2.DefaultFormModel;
import com.luxsoft.sw3.embarque.Embarque;
import com.luxsoft.sw3.embarque.Entrega;
import com.luxsoft.sw3.ui.services.KernellUtils;

/**
 * Controlador y PresentationModel para el mantenimiento del regreso de embarques
 * 
 * @author Ruben Cancino Ramos
 *
 */
public class RegresoDeEmbarqueFormModel extends DefaultFormModel {
	
	private EventList<Entrega> partidasSource;
	
	protected Logger logger=Logger.getLogger(getClass());
	
	public RegresoDeEmbarqueFormModel(Embarque embarque) {
		super(embarque);
	}
	
	protected void init(){
		getEmbarque().setValor(getEmbarque().getValorCalculado());
		getEmbarque().setRegreso(new Date());
		partidasSource=GlazedLists.eventList(getEmbarque().getPartidas());
		passwordModel=new ValueHolder();
		passwordModel.addValueChangeListener(new PropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent evt) {
				resolverUsuario((String)evt.getNewValue());
			}
		});
	}
	
	@Override
	protected void addValidation(PropertyValidationSupport support) {
		
		if(getUserModel().getValue()==null){
			support.getResult().addError("Registre usuario que atiende");
			return;
		}
		
		for(Entrega ent:getPartidasSource()){
			if(ent.getRecepcion()==null){
				support.getResult().addError("No se ha registrado la Recepcion del cliente para una entrega");
				return;
			}
		}
	}
	
	public EventList<Entrega> getPartidasSource() {
		return partidasSource;
	}	
	public Embarque getEmbarque(){
		return (Embarque)getBaseBean();
	}
	
	public void insertar(){
		
	}
		
	public void elminarPartida(int index){
		Entrega det=partidasSource.get(index);
		if(det!=null){
			boolean ok=getEmbarque().getPartidas().remove(det);
			if(ok){
				partidasSource.remove(index);
				validate();
				return;
			}
		}
		System.out.println("Existe un error en la seleccion de partidas");
	}
	
	public void elminarPartida(Entrega det){
		int index=partidasSource.indexOf(det);
		if(index!=-1){
			elminarPartida(index);
			actualizarValor();
		}
		System.out.println("Existe un error en la seleccion de partidas");
	}
	
	private void actualizarValor(){
		BigDecimal valor=BigDecimal.ZERO;
		for(Entrega e:getEmbarque().getPartidas()){
			valor=valor.add(e.getValor());
		}
		getEmbarque().setValor(valor);
	}
	
	public void editar(int index){
	}
	
	public void view(int index){
	}

	private ValueModel userModel;
	private ValueModel passwordModel;
	
	public ValueModel getUserModel() {
		if(userModel==null)
			userModel=new ValueHolder(null);
		return userModel;
	}

	public void setUserModel(ValueModel userModel) {
		this.userModel = userModel;
	}
	
	public void resolverUsuario(String password){
		//System.out.println("Buscando usuario con password: "+password);
		User user=KernellUtils.buscarUsuarioPorPassword(password);
		//System.out.println("Usuario localizado: "+user);
		getUserModel().setValue(user);
		validate();
	}
	
	public void comiit(){
		User user=(User)getUserModel().getValue();
		getEmbarque().getLog().setUpdateUser(user.getUsername());
	}

	public ValueModel getPasswordModel() {
		return passwordModel;
	}

	public void setPasswordModel(ValueModel passwordModel) {
		this.passwordModel = passwordModel;
	}
	
	
}
