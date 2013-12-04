package com.luxsoft.siipap.gastos.operaciones;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.JComponent;

import com.jgoodies.binding.value.ValueHolder;
import com.jgoodies.binding.value.ValueModel;
import com.jgoodies.uif.AbstractDialog;
import com.luxsoft.siipap.gastos.catalogos.ProductoForm;
import com.luxsoft.siipap.model.gastos.GProductoServicio;
import com.luxsoft.siipap.service.ServiceLocator2;
import com.luxsoft.siipap.swing.binding.LookupControl;
import com.luxsoft.siipap.swing.controls.SXAbstractDialog;
import com.luxsoft.siipap.swing.utils.SWExtUIManager;

/**
 * 
 * Permite seleccionar un {@link GProductoServicio} de un JComboBox
 *  y si este no existe se puede agregar
 *  
 * @author Ruben Cancino
 *
 */
public class ProductoControl extends LookupControl{
	
	
	
	@SuppressWarnings("unchecked")
	public ProductoControl(final ValueModel vm){
		super(ServiceLocator2.getLookupManager().getProductos());
		setValueModel(vm);		
	}
	/*
	public ProductoControl(){
		super(ServiceLocator2.getLookupManager().getProductos());
	}
	*/
	protected Object newObject(){
		return ProductoForm.showForm(new GProductoServicio());
	}


	public static void main(String[] args) {
		SWExtUIManager.setup();
		final ValueHolder holder=new ValueHolder();
		holder.addPropertyChangeListener(new PropertyChangeListener(){
			public void propertyChange(PropertyChangeEvent evt) {
				System.out.println("Rubro: "+ ((GProductoServicio)evt.getNewValue()).getRubro());
			}			
		});
		final AbstractDialog dialog=new SXAbstractDialog("TEST"){			
			protected JComponent buildContent() {				
				return new ProductoControl(holder);
			}
		};
		dialog.open();
		System.exit(0);
	}

}
