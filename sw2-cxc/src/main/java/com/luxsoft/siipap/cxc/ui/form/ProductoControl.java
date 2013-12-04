package com.luxsoft.siipap.cxc.ui.form;

import javax.swing.JComponent;

import com.jgoodies.binding.value.ValueHolder;
import com.jgoodies.binding.value.ValueModel;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.uif.AbstractDialog;
import com.luxsoft.siipap.model.core.Producto;
import com.luxsoft.siipap.service.ServiceLocator2;
import com.luxsoft.siipap.swing.binding.LookupControl;
import com.luxsoft.siipap.swing.controls.SXAbstractDialog;
import com.luxsoft.siipap.swing.utils.SWExtUIManager;

/**
 * 
 * Permite seleccionar un {@link Producto} de un JComboBox
 *  y si este no existe se puede agregar
 *  
 * @author Ruben Cancino
 *
 */
public class ProductoControl extends LookupControl{
	
	
	
	@SuppressWarnings("unchecked")
	public ProductoControl(final ValueModel vm){
		super(ServiceLocator2.getProductoManager().buscarProductosActivos());
		setValueModel(vm);		
	}
	
	protected Object newObject(){
		return null;//ProductoForm.showForm(new Producto());
	}
	
	protected FormLayout getFormLayout(){
		final FormLayout layout=new FormLayout("f:max(p;200dlu):g,p","f:p");
		return layout;
	}


	public static void main(String[] args) {
		SWExtUIManager.setup();
		final ValueHolder holder=new ValueHolder();
		
		final AbstractDialog dialog=new SXAbstractDialog("TEST"){			
			protected JComponent buildContent() {				
				return new ProductoControl(holder);
			}
			protected void onWindowOpened(){
				System.out.println(getContentPane().getPreferredSize());
			}
		};
		dialog.open();
		System.exit(0);
	}

}
