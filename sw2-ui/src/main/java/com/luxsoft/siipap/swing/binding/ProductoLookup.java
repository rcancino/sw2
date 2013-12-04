package com.luxsoft.siipap.swing.binding;

import java.awt.BorderLayout;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.JPanel;

import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.gui.TableFormat;

import com.luxsoft.siipap.model.core.Producto;
import com.luxsoft.siipap.service.ServiceLocator2;
import com.luxsoft.siipap.swing.controls.SXAbstractDialog;

public class ProductoLookup extends GridLookupControl{
	
	@Override
	public Class getBeanClass() {
		return Producto.class;
	}

	@Override
	public List getData() {
		return ServiceLocator2.getProductoManager().buscarProductosActivos();
	}

	@Override
	public TableFormat getTableFormat() {
		String [] props=new String[]{"clave","descripcion","kilos"};
		return GlazedLists.tableFormat(Producto.class, props,props);
	}
	
	
	 /**
	  * Probamos la funcionalidad extendiendo para seleccionar 
	  * un Productos
	  * 
	  * @param args
	  */
	 public static void main(String[] args) {
		
		 final ProductoLookup control=new ProductoLookup();
		 
		SXAbstractDialog dialog=new SXAbstractDialog("TEST"){

			
			
			@Override
			protected JComponent buildContent() {
				JPanel panel=new JPanel(new BorderLayout());
				panel.add(control,BorderLayout.CENTER);
				panel.add(buildButtonBarWithOKCancel(),BorderLayout.SOUTH);
				return panel;
			}
			
		};
		dialog.open();
		if(!dialog.hasBeenCanceled()){
			System.out.println("Seleccion: "+control.getSelected());
		}
		
	}

}
