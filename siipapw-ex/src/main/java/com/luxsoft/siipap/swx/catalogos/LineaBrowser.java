package com.luxsoft.siipap.swx.catalogos;

import javax.swing.Action;

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.gui.TableFormat;


import com.luxsoft.siipap.model.core.Linea;
import com.luxsoft.siipap.swing.actions.SWXAction;
import com.luxsoft.siipap.swing.dialog.UniversalAbstractCatalogDialog;


/**
 *  Browser para el mantenimiento de entidades de tipo {@link GProductoServicio}
 * 
 * @author Ruben Cancino
 *
 */
public class LineaBrowser extends UniversalAbstractCatalogDialog<Linea>{
	
	private static Action showAction;

	public LineaBrowser() {
		super(Linea.class,new BasicEventList<Linea>(), "Catálogo de Líneas ");
	}

	
	/*
	 * (non-Javadoc)
	 * @see com.luxsoft.siipap.swing.dialog.UniversalAbstractCatalogDialog#getTableFormat()
	 */
	@Override
	protected TableFormat<Linea> getTableFormat() {
		final String[] cols={"id","nombre","descripcion"};
		final String[] names={"Id","Nombre","Descripción"};
		return GlazedLists.tableFormat(Linea.class, cols,names);
	}
	
	/**** Personalizacion de comportamiento (A/B/C) ****/
	
	@Override
	protected Linea doInsert() {		
		Linea res=LineaForm.showForm(new Linea());
		if(res!=null)
			return save(res);
		
		return null;
	}
	
	@Override
	protected Linea doEdit(Linea bean) {		
		Linea res=LineaForm.showForm(bean);
		if(res!=null)
			return save(res);
		return null;
	}
	
	protected void doView(Linea bean){
		LineaForm.showForm(bean,true);
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
		LineaBrowser dialog=new LineaBrowser();
		dialog.open();
	}
	

	public static void main(String[] args) {
		openDialog();
		System.exit(0);
		
	}

}
