package com.luxsoft.siipap.swx.binding;

import com.jgoodies.binding.value.ValueModel;
import com.luxsoft.siipap.model.core.Clase;
import com.luxsoft.siipap.service.ServiceLocator2;
import com.luxsoft.siipap.swing.binding.LookupControl;
import com.luxsoft.siipap.swx.catalogos.ClaseForm;

/**
 * 
 * Permite seleccionar una {@link Clase} de un JComboBox
 *  y si este no existe se puede agregar
 *  
 * @author Ruben Cancino
 *
 */
public class ClaseControl extends LookupControl{
	
	
	
	@SuppressWarnings("unchecked")
	public ClaseControl(final ValueModel vm){
		super(ServiceLocator2.getUniversalDao().getAll(Clase.class));
		setValueModel(vm);		
	}
	
	protected Object newObject(){
		return ClaseForm.showForm(new Clase());
	}


	

}
