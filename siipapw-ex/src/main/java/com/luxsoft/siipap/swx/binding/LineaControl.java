package com.luxsoft.siipap.swx.binding;

import com.jgoodies.binding.value.ValueModel;
import com.luxsoft.siipap.model.core.Linea;
import com.luxsoft.siipap.service.ServiceLocator2;
import com.luxsoft.siipap.swing.binding.LookupControl;
import com.luxsoft.siipap.swx.catalogos.LineaForm;

/**
 * 
 * Permite seleccionar una {@link Linea} de un JComboBox
 *  y si este no existe se puede agregar
 *  
 * @author Ruben Cancino
 *
 */
public class LineaControl extends LookupControl{
	
	
	
	@SuppressWarnings("unchecked")
	public LineaControl(final ValueModel vm){
		super(ServiceLocator2.getUniversalDao().getAll(Linea.class));
		setValueModel(vm);		
	}
	
	protected Object newObject(){
		return LineaForm.showForm(new Linea());
	}


	

}
