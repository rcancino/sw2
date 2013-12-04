package com.luxsoft.siipap.tesoreria.catalogos;

import javax.swing.Action;

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.gui.TableFormat;


import com.luxsoft.siipap.cxc.model.Tarjeta;
import com.luxsoft.siipap.swing.actions.SWXAction;
import com.luxsoft.siipap.swing.dialog.UniversalAbstractCatalogDialog;

/**
 *  
 * 
 * @author Ruben Cancino,Octavio Hernandez
 *
 */
public class TarjetasBrowser extends UniversalAbstractCatalogDialog<Tarjeta>{
	
	private static Action showAction;

	public TarjetasBrowser() {
		super(Tarjeta.class,new BasicEventList<Tarjeta>(), "Catálogo de Tarjetas ");
	}

	
	@Override
	protected TableFormat<Tarjeta> getTableFormat() {
		final String[] cols={"id","nombre","banco.clave","debito","comisionBancaria","comisionVenta"};
		final String[] names={"Id","Tarjeta","Banco","Débito","Comisión","Comisión (Vent)"};
		return GlazedLists.tableFormat(Tarjeta.class, cols,names);
	}
	
	/**** Personalizacion de comportamiento (A/B/C) ****/
	
	@Override
	protected Tarjeta doInsert() {
		Tarjeta res=TarjetaForm.showForm();
		if(res!=null)
			return save(res);
		return null;
	}
	
	@Override
	protected Tarjeta doEdit(final Tarjeta bean) {		
		Tarjeta res=TarjetaForm.showForm(bean,false);
		if(res!=null)
			return save(res);
		return null;
	}
	
	protected void doView(Tarjeta bean){
		TarjetaForm.showForm(bean,true);
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
		final TarjetasBrowser dialog=new TarjetasBrowser();
		dialog.open();
	}
	

	public static void main(String[] args) {
		openDialog();
		
	}

}
