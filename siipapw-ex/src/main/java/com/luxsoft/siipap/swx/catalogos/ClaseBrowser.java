package com.luxsoft.siipap.swx.catalogos;

import javax.swing.Action;

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.gui.TableFormat;


import com.luxsoft.siipap.model.core.Clase;
import com.luxsoft.siipap.swing.actions.SWXAction;
import com.luxsoft.siipap.swing.dialog.UniversalAbstractCatalogDialog;


/**
 *  Browser para el mantenimiento de entidades de tipo {@link GProductoServicio}
 * 
 * @author Ruben Cancino
 *
 */
public class ClaseBrowser extends UniversalAbstractCatalogDialog<Clase>{
	
	private static Action showAction;

	public ClaseBrowser() {
		super(Clase.class,new BasicEventList<Clase>(), "Catálogo de Clases");
	}

	
	/*
	 * (non-Javadoc)
	 * @see com.luxsoft.siipap.swing.dialog.UniversalAbstractCatalogDialog#getTableFormat()
	 */
	@Override
	protected TableFormat<Clase> getTableFormat() {
		final String[] cols={"id","nombre"};
		final String[] names={"Id","Nombre"};
		return GlazedLists.tableFormat(Clase.class, cols,names);
	}
	
	/**** Personalizacion de comportamiento (A/B/C) ****/
	
	@Override
	protected Clase doInsert() {
		
		Clase res=ClaseForm.showForm(new Clase());
		if(res!=null)
			return save(res);
		return null;
	}
	
	@Override
	protected Clase doEdit(Clase bean) {		
		Clase res=ClaseForm.showForm(bean);
		if(res!=null)
			return save(res);
			
		return null;
	}
	
	protected void doView(Clase bean){
		ClaseForm.showForm(bean,true);
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
		ClaseBrowser dialog=new ClaseBrowser();
		dialog.open();
	}
	

	public static void main(String[] args) {
		openDialog();
		System.exit(0);
		
	}

}
