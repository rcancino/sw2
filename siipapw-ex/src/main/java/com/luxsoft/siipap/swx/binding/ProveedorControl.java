package com.luxsoft.siipap.swx.binding;

import java.util.List;

import com.jgoodies.binding.value.ValueModel;

import com.luxsoft.siipap.model.core.Proveedor;
import com.luxsoft.siipap.service.ServiceLocator2;
import com.luxsoft.siipap.swing.binding.LookupControl;
import com.luxsoft.siipap.swx.catalogos.ProveedorForm;

/**
 * 
 * Permite seleccionar un Proveedor de un JComboBox
 *  y si este no existe se puede agregar
 *  
 * @author Ruben Cancino
 *
 */
public class ProveedorControl extends LookupControl{
	
	
	public ProveedorControl(final List<Proveedor> data,final ValueModel model){
		super(data);
		setValueModel(model);
	}
	
	public ProveedorControl(final ValueModel model){
		super(ServiceLocator2.getProveedorManager().getAll());
		setValueModel(model);
	}	
	
	protected Object newObject(){
		return ProveedorForm.showForm(new Proveedor());
	}
	

}
