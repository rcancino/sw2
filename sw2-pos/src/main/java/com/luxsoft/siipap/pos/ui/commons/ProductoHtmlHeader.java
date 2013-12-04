package com.luxsoft.siipap.pos.ui.commons;

import javax.swing.JEditorPane;
import javax.swing.JPanel;

import com.luxsoft.siipap.model.core.Producto;

public class ProductoHtmlHeader extends JPanel{
	
	private Producto producto;
	
	protected JEditorPane editor;
	
	public ProductoHtmlHeader(){
		
	}

	public Producto getProducto() {
		return producto;
	}

	public void setProducto(Producto producto) {
		this.producto = producto;
	}
	
	

}
