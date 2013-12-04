/**
 * 
 */
package com.luxsoft.sw3.ui.catalogos;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;

import com.jgoodies.validation.util.PropertyValidationSupport;
import com.luxsoft.siipap.model.Direccion;
import com.luxsoft.siipap.model.core.Cliente;
import com.luxsoft.siipap.swing.form2.DefaultFormModel;

public  class ClienteModel extends DefaultFormModel implements PropertyChangeListener{
	
	public ClienteModel(Cliente cliente) {
		super(cliente);
	}
	protected void init(){
		super.init();
		addBeanPropertyChangeListener(this);
		getCliente().getDireccionFiscal().addPropertyChangeListener(new PropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent evt) {
				validate();
			}
		});
	}

	private Cliente getCliente(){
		return (Cliente)getBaseBean();
	}
	
	

	@Override
	protected void addValidation(PropertyValidationSupport support) {
		if(StringUtils.isBlank(getCliente().getRfc())){
			support.getResult().addWarning("Es recomendable capturar el RFC");
		}if(getCliente().isPersonaFisica()){
			if(StringUtils.isBlank(getCliente().getApellidoP()))
				support.getResult().addError("En personas físicas el apellido paterno es mandatorio");
			if(StringUtils.isBlank(getCliente().getApellidoM()))
				support.getResult().addError("En personas físicas el apellido materno es mandatorio");
			if(StringUtils.isBlank(getCliente().getNombres()))
				support.getResult().addError("En personas físicas el(los) nombres  es(son) mandatorio(s)");
		}if(!getCliente().isPersonaFisica()){
			if(StringUtils.isBlank(getCliente().getNombre())){
				support.getResult().addError("El nombre o razón es mandatorio");
			}
		}
		validarDireccion(getCliente().getDireccionFiscal(), support);
		super.addValidation(support);
	}
	
	private void validarDireccion(Direccion direccion,PropertyValidationSupport support){
		if(StringUtils.isBlank(direccion.getCalle())){
			support.getResult().addError("Digite la calle" );
			return;
		}if(StringUtils.isBlank(direccion.getColonia())){
			support.getResult().addError("Digite la colonia" );
			return;
		}if(StringUtils.isBlank(direccion.getMunicipio())){
			support.getResult().addError("Digite la delegación/municipio" );
			return;
		}if(StringUtils.isBlank(direccion.getEstado())){
			support.getResult().addError("Digite el estado" );
		}if(StringUtils.isBlank(direccion.getCp())){
			support.getResult().addError("Digite el codigo postal" );
			return;
		}
	}
	
	public void propertyChange(PropertyChangeEvent evt) {
		if("rfc".equals(evt.getPropertyName())){
			if(getCliente().getId()==null){
				String val=(String)evt.getNewValue();
				val=StringUtils.substring(val, 0, 4);
				boolean pf=Pattern.compile("[A-Z]{3,4}").matcher(val).matches();
				setValue("personaFisica", pf);
			}
		}else if(evt.getPropertyName().equals("apellidoP")
				||evt.getPropertyName().equals("apellidoM")
				||evt.getPropertyName().equals("nombres")
				){
			if(getCliente().isPersonaFisica())
				getCliente().setNombre(getCliente().getNombreRazon());
		}
		
	}
	
	
}