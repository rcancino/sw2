package com.luxsoft.siipap.gastos.catalogos;

import javax.swing.Action;

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.gui.TableFormat;



import com.luxsoft.siipap.model.gastos.GTipoProveedor;
import com.luxsoft.siipap.swing.actions.SWXAction;
import com.luxsoft.siipap.swing.dialog.UniversalAbstractCatalogDialog;

/**
 *  
 * 
 * @author Ruben Cancino,Octavio Hernandez
 *
 */
public class TipoDeProveedorBrowser extends UniversalAbstractCatalogDialog<GTipoProveedor>{
	
	private static Action showAction;

	public TipoDeProveedorBrowser() {
		super(GTipoProveedor.class,new BasicEventList<GTipoProveedor>(), "Tipos de proveedores ");
	}

	
	@Override
	protected TableFormat<GTipoProveedor> getTableFormat() {
		final String[] cols={"id","clave","descripcion","parent.clave"};
		final String[] names={"Id","Concepto","Descripcion","Padre"};
		return GlazedLists.tableFormat(GTipoProveedor.class, cols,names);
	}
	
	/**** Personalizacion de comportamiento (A/B/C) ****/
	
	@Override
	protected GTipoProveedor doInsert() {
		GTipoProveedor res=TipoDeProveedorForm.showForm(new GTipoProveedor());
		if(res!=null)
			return save(res);
		return null;
	}
	
	@Override
	protected GTipoProveedor doEdit(GTipoProveedor bean) {
		GTipoProveedor res=TipoDeProveedorForm.showForm(bean);
		if(res!=null)
			return save(res);
		return null;
	}
	
	protected void doView(GTipoProveedor bean){
		TipoDeProveedorForm.showForm(bean,true);
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
		TipoDeProveedorBrowser dialog=new TipoDeProveedorBrowser();
		dialog.open();
	}
	

	public static void main(String[] args) {
		openDialog();
		System.exit(0);
		
	}

}
