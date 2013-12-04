package com.luxsoft.siipap.gastos.operaciones;

import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.SortedList;

import com.jgoodies.binding.value.ValueModel;
import com.luxsoft.siipap.gastos.catalogos.INPCForm;
import com.luxsoft.siipap.model.gastos.INPC;
import com.luxsoft.siipap.service.ServiceLocator2;
import com.luxsoft.siipap.swing.binding.LookupControl;

/**
 * 
 * Permite seleccionar un {@link INPC} de un JComboBox
 *  y si este no existe se puede agregar
 *  
 * @author Ruben Cancino
 *
 */
public class INPCControl extends LookupControl{
	
	
	
	@SuppressWarnings("unchecked")
	public INPCControl(final ValueModel vm){
		super(ServiceLocator2.getLookupManager().getINPCs());
		setValueModel(vm);
	}
	
	/**
	 * Implementamos para ordenar
	 */
	@SuppressWarnings("unchecked")
	protected EventList createEventList(){
		return new SortedList(super.createEventList(),null);
	}
	
	
	protected Object newObject(){
		return INPCForm.showForm(new INPC());
	}
	

}
