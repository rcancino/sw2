package com.luxsoft.siipap.gastos.operaciones;

import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.SortedList;

import com.jgoodies.binding.value.ValueModel;
import com.luxsoft.siipap.gastos.catalogos.ConsignatarioForm;
import com.luxsoft.siipap.model.gastos.Consignatario;
import com.luxsoft.siipap.service.ServiceLocator2;
import com.luxsoft.siipap.swing.binding.LookupControl;

/**
 * 
 * Permite seleccionar un {@link Consignatario} de un JComboBox
 *  y si este no existe se puede agregar
 *  
 * @author Ruben Cancino
 *
 */
public class ConsignatarioControl extends LookupControl{
	
	
	
	@SuppressWarnings("unchecked")
	public ConsignatarioControl(final ValueModel vm){
		super(ServiceLocator2.getUniversalDao().getAll(Consignatario.class));
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
		return ConsignatarioForm.showForm(new Consignatario());
	}
	

}
