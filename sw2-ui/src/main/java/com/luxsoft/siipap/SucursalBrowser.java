package com.luxsoft.siipap;

import javax.swing.Action;

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.gui.TableFormat;




import com.luxsoft.siipap.model.Sucursal;
import com.luxsoft.siipap.swing.actions.SWXAction;
import com.luxsoft.siipap.swing.dialog.UniversalAbstractCatalogDialog;

/**
 *  
 * 
 * @author Ruben Cancino,Octavio Hernandez
 *
 */
public class SucursalBrowser extends UniversalAbstractCatalogDialog<Sucursal>{
	
	private static Action showAction;

	public SucursalBrowser() {
		super(Sucursal.class,new BasicEventList<Sucursal>(), "Catálogo de Sucursales");
	}

	
	@Override
	protected TableFormat<Sucursal> getTableFormat() {
		final String[] cols={"id","clave","nombre","empresa.nombre"};
		final String[] names={"Id","Clave","Nombre","Empresa"};
		return GlazedLists.tableFormat(Sucursal.class, cols,names);
	}
	
	/**** Personalizacion de comportamiento (A/B/C) ****/
	
	@Override
	protected Sucursal doInsert() {
		Sucursal res=SucursalForm.showForm(new Sucursal());
		if(res!=null)
			return save(res);
		return null;
	}
	
	@Override
	protected Sucursal doEdit(Sucursal bean) {
		Sucursal res=SucursalForm.showForm(bean);
		if(res!=null)
			return save(res);
		return null;
	}
	
	protected void doView(Sucursal bean){
		SucursalForm.showForm(bean,true);
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
		SucursalBrowser dialog=new SucursalBrowser();
		dialog.open();
	}
	

	public static void main(String[] args) {
		openDialog();
		System.exit(0);
		
	}

}
