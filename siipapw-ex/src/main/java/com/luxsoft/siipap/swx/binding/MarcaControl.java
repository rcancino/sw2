package com.luxsoft.siipap.swx.binding;

import com.jgoodies.binding.value.ValueModel;
import com.luxsoft.siipap.model.core.Marca;
import com.luxsoft.siipap.service.ServiceLocator2;
import com.luxsoft.siipap.swing.binding.LookupControl;
import com.luxsoft.siipap.swx.catalogos.MarcaForm;

/**
 * 
 * Permite seleccionar una {@link Marca} de un JComboBox
 *  y si este no existe se puede agregar
 *  
 * @author Ruben Cancino
 *
 */
public class MarcaControl extends LookupControl{
	
	
	
	@SuppressWarnings("unchecked")
	public MarcaControl(final ValueModel vm){
		super(ServiceLocator2.getUniversalDao().getAll(Marca.class));
		setValueModel(vm);		
	}
	/*
	public ProductoControl(){
		super(ServiceLocator2.getLookupManager().getProductos());
	}
	*/
	protected Object newObject(){
		return MarcaForm.showForm(new Marca());
	}


	

}
