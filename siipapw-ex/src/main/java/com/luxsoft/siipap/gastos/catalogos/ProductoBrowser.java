package com.luxsoft.siipap.gastos.catalogos;

import javax.swing.Action;

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.gui.TableFormat;

import com.luxsoft.siipap.model.gastos.GProductoServicio;
import com.luxsoft.siipap.swing.actions.SWXAction;
import com.luxsoft.siipap.swing.dialog.UniversalAbstractCatalogDialog;

/**
 *  Browser para el mantenimiento de entidades de tipo {@link GProductoServicio}
 * 
 * @author Ruben Cancino
 *
 */
public class ProductoBrowser extends UniversalAbstractCatalogDialog<GProductoServicio>{
	
	private static Action showAction;

	public ProductoBrowser() {
		super(GProductoServicio.class,new BasicEventList<GProductoServicio>(), "Catálogo de Productos (Bienes/Servicioes)");
		addTextEditor("Filtrar", "descripcion");
	}

	
	/*
	 * (non-Javadoc)
	 * @see com.luxsoft.siipap.swing.dialog.UniversalAbstractCatalogDialog#getTableFormat()
	 */
	@Override
	protected TableFormat<GProductoServicio> getTableFormat() {
		final String[] cols={"id","descripcion","unidad","rubro","servicio","inventariable","ietu","nacional","inversion","codigo","nota"};
		final String[] names={"Id","Descripción","Uni","Rubro","Servicio","Inventariable","IETU","Nacional","Inversión","Codigo","Comentario"};
		return GlazedLists.tableFormat(GProductoServicio.class, cols,names);
	}
	
	/**** Personalizacion de comportamiento (A/B/C) ****/
	
	@Override
	protected GProductoServicio doInsert() {
		GProductoServicio res=ProductoForm.showForm(new GProductoServicio());
		if(res!=null)
			return save(res);
		return null;
	}
	
	@Override
	protected GProductoServicio doEdit(GProductoServicio bean) {
		GProductoServicio res=ProductoForm.showForm(bean);
		if(res!=null)
			return save(res);
		return null;
	}
	
	protected void doView(GProductoServicio bean){
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
