package com.luxsoft.siipap.swx.catalogos;

import javax.swing.Action;

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.gui.TableFormat;


import com.luxsoft.siipap.model.core.Producto;
import com.luxsoft.siipap.swing.actions.SWXAction;
import com.luxsoft.siipap.swing.dialog.UniversalAbstractCatalogDialog;


/**
 *  Browser para el mantenimiento de entidades de tipo {@link GProductoServicio}
 * 
 * @author Ruben Cancino
 *
 */
public class ProductoBrowser extends UniversalAbstractCatalogDialog<Producto>{
	
	private static Action showAction;

	public ProductoBrowser() {
		super(Producto.class,new BasicEventList<Producto>(), "Catálogo de Productos (Bienes/Servicioes)");
		addTextEditor("Filtrar", "descripcion","clave");
	}

	
	/*
	 * (non-Javadoc)
	 * @see com.luxsoft.siipap.swing.dialog.UniversalAbstractCatalogDialog#getTableFormat()
	 */
	@Override
	protected TableFormat<Producto> getTableFormat() {
		final String[] cols={"id","clave","descripcion","activo","inventariable","kilos","gramos"};
		final String[] names={"Id","Clave","Descripción","Activo","Inventariable","Kilos","Gramos"};
		return GlazedLists.tableFormat(Producto.class, cols,names);
	}
	
	/**** Personalizacion de comportamiento (A/B/C) ****/
	
	@Override
	protected Producto doInsert() {
		Producto res=ProductoForm.showForm(new Producto());
		if(res!=null)
			return save(res);
		return null;
	}
	
	@Override
	protected Producto doEdit(Producto bean) {
		Producto res=ProductoForm.showForm(bean);
		if(res!=null)
			return save(res);
		return null;
	}
	
	protected void doView(Producto bean){
		ProductoForm.showForm(bean,true);
	}
	
	
	
	/**** Fin Personalizacion de comportamiento****/

	/**
	 * Acceso a una Action que permite mostrar este browser.	 * 
	 * Patron FactoryMethod para se usado desde  Spring
	 * Existe solo para facilitar el uso en Spring
	 * 
	 * @return
	 */
	public static Action getShowAction(){		
		showAction=new SWXAction(){
				@Override
				protected void execute() {
					openDialog();
				}				
			};		
		return showAction;
	}	
	
	public static void openDialog(){
		ProductoBrowser dialog=new ProductoBrowser();
		dialog.open();
	}
	

	public static void main(String[] args) {
		openDialog();
		System.exit(0);
		
	}

}
