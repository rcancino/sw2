package com.luxsoft.sw3.crm.catalogos;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;

import com.jgoodies.binding.PresentationModel;
import com.jgoodies.binding.value.ValueHolder;
import com.jgoodies.validation.util.PropertyValidationSupport;
import com.luxsoft.siipap.model.Direccion;
import com.luxsoft.siipap.model.core.Cliente;
import com.luxsoft.siipap.model.core.ClienteCredito;
import com.luxsoft.siipap.swing.form2.DefaultFormModel;

/**
 * 
 * @author Ruben Cancino
 * TODO Validar los sub modelos
 *
 */
public class CRM_ClienteFormModel extends DefaultFormModel implements PropertyChangeListener{
	
	private PresentationModel creditoModel;
	private ValueHolder contactoChannel;
	

	public CRM_ClienteFormModel() {
		super(Cliente.class);
	}

	public CRM_ClienteFormModel(Cliente bean, boolean readOnly) {
		super(bean, readOnly);
	}

	public CRM_ClienteFormModel(Cliente bean) {
		super(bean);
	}
	
	protected Cliente getCliente(){
		return (Cliente)getBaseBean();
	}
	
	public PresentationModel getCreditoModel(){
		return creditoModel;
	}
	
	protected void init(){
		
		addBeanPropertyChangeListener(this);
		getCliente().getDireccionFiscal().addPropertyChangeListener(new PropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent evt) {
				validate();
			}
		});
		
		pmodel.addPropertyChangeListener(PresentationModel.PROPERTYNAME_BEAN, new BeanChannelHandler());
		
		getModel("credito").addValueChangeListener(new CreditoHandler());
		initSubModels();
	}	
	
	private void initSubModels(){
		if(getCliente().getCredito()!=null)
			creditoModel=new PresentationModel(getCliente().getCredito());
		else
			creditoModel=new PresentationModel(new ClienteCredito());
		contactoChannel=new ValueHolder(null,true);
	}
	
	public ValueHolder getContactosChannel(){
		return contactoChannel;
	}
	
	@Override
	protected void addValidation(PropertyValidationSupport support) {
		String rfc=getCliente().getRfc();
		if(StringUtils.isBlank(rfc)
				|| rfc.length()<12
				|| rfc.length()>13){
			support.getResult().addWarning("CORREGIR EL RFC");
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
			support.getResult().addWarning("Falta calle en dirección" );
			return;
		}if(StringUtils.isBlank(direccion.getColonia())){
			support.getResult().addWarning("Falta colonia en dirección" );
			return;
		}if(StringUtils.isBlank(direccion.getMunicipio())){
			support.getResult().addWarning("Falta la delegación/municipio en dirección" );
			return;
		}if(StringUtils.isBlank(direccion.getEstado())){
			support.getResult().addWarning("Falta el estado en la dirección" );
		}if(StringUtils.isBlank(direccion.getCp())){
			support.getResult().addWarning("Falta el codigo postal en la dirección" );
			return;
		}if(StringUtils.isNotBlank(direccion.getNumero())){
			if(direccion.getNumero().length()>10){
				support.getResult().addError("Tamaño de número extorior debe ser<=10" );
			}
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
	
	
	/**
	 * Detecta cambios en el BeanChannel para actualizar los sub modelos
	 * 
	 * @author Ruben Cancino
	 *
	 */
	private class BeanChannelHandler implements PropertyChangeListener{

		public void propertyChange(PropertyChangeEvent evt) {
			initSubModels();
		}
		
	}
	
	private class CreditoHandler implements PropertyChangeListener{
		public void propertyChange(PropertyChangeEvent evt){			
			if(evt.getOldValue()==null){
				getCliente().habilitarCredito();
				creditoModel.setBean(getCliente().getCredito());
			}
		}
	}

}
