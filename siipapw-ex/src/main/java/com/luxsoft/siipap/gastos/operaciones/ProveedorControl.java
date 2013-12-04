package com.luxsoft.siipap.gastos.operaciones;

import com.jgoodies.binding.value.ValueModel;
import com.luxsoft.siipap.gastos.catalogos.ProveedorDeGastosForm;
import com.luxsoft.siipap.model.gastos.GProveedor;
import com.luxsoft.siipap.service.ServiceLocator2;
import com.luxsoft.siipap.swing.binding.LookupControl;

/**
 * 
 * Permite seleccionar un Proveedor de un JComboBox
 *  y si este no existe se puede agregar
 *  
 * @author Ruben Cancino
 *
 */
public class ProveedorControl extends LookupControl{
	
	public ProveedorControl(final ValueModel model){
		super(ServiceLocator2.getLookupManager().getProveedores());
		setValueModel(model);
		setEnableInsertObject(true);
	}	
	
	protected Object newObject(){
		return ProveedorDeGastosForm.showForm(new GProveedor());
	}
	

}
