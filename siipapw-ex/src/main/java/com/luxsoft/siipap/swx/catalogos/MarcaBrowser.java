package com.luxsoft.siipap.swx.catalogos;

import javax.swing.Action;

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.gui.TableFormat;


import com.luxsoft.siipap.model.core.Marca;
import com.luxsoft.siipap.swing.actions.SWXAction;
import com.luxsoft.siipap.swing.dialog.UniversalAbstractCatalogDialog;


/**
 *  Browser para el mantenimiento de entidades de tipo {@link GProductoServicio}
 * 
 * @author Ruben Cancino
 *
 */
public class MarcaBrowser extends UniversalAbstractCatalogDialog<Marca>{
	
	private static Action showAction;

	public MarcaBrowser() {
		super(Marca.class,new BasicEventList<Marca>(), "Catálogo de Marcas");
	}

	
	/*
	 * (non-Javadoc)
	 * @see com.luxsoft.siipap.swing.dialog.UniversalAbstractCatalogDialog#getTableFormat()
	 */
	@Override
	protected TableFormat<Marca> getTableFormat() {
		final String[] cols={"id","nombre"};
		final String[] names={"Id","Nombre"};
		return GlazedLists.tableFormat(Marca.class, cols,names);
	}
	
	/**** Personalizacion de comportamiento (A/B/C) ****/
	
	@Override
	protected Marca doInsert() {		
		Marca res=MarcaForm.showForm(new Marca());
		if(res!=null)
			return save(res);
		return null;
	}
	
	@Override
	protected Marca doEdit(Marca bean) {		
		Marca res=MarcaForm.showForm(bean);
		if(res!=null)
			return save(res);
		return null;
	}
	
	protected void doView(Marca bean){
		MarcaForm.showForm(bean,true);
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
		MarcaBrowser dialog=new MarcaBrowser();
		dialog.open();
	}
	

	public static void main(String[] args) {
		openDialog();
		System.exit(0);
		
	}

}
