package com.luxsoft.siipap.gastos.catalogos;

import javax.swing.Action;

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.gui.TableFormat;

import com.luxsoft.siipap.model.gastos.GProveedor;
import com.luxsoft.siipap.swing.actions.SWXAction;
import com.luxsoft.siipap.swing.dialog.UniversalAbstractCatalogDialog;

/**
 *  
 * 
 * @author Ruben Cancino,Octavio Hernandez
 *
 */
public class ProveedorDeGastosBrowser extends UniversalAbstractCatalogDialog<GProveedor>{
	
	private static Action showAction;

	public ProveedorDeGastosBrowser() {
		super(GProveedor.class,new BasicEventList<GProveedor>(), "Catálogo de Proveedores de bienes y servicios");
		addTextEditor("Filtrar","nombreRazon");
	}

	
	/*
	 * (non-Javadoc)
	 * @see com.luxsoft.siipap.swing.dialog.UniversalAbstractCatalogDialog#getTableFormat()
	 */
	@Override
	protected TableFormat<GProveedor> getTableFormat() {
		final String[] cols={"id","nombreRazon","tipo","rfc","telefono1","contacto1","credito"};
		final String[] names={"Id","Nombre","Tipo","RFC","Tel","Contacto","L.Crédito"};
		return GlazedLists.tableFormat(GProveedor.class, cols,names);
	}
	
	/**** Personalizacion de comportamiento (A/B/C) ****/
	
	@Override
	protected GProveedor doInsert() {
		GProveedor res=ProveedorDeGastosForm.showForm(new GProveedor());
		if(res!=null)
			return save(res);
		return null;
	}
	
	@Override
	protected GProveedor doEdit(GProveedor bean) {
		GProveedor res=ProveedorDeGastosForm.showForm(bean);
		if(res!=null)
			return save(res);
		return null;
	}
	
	protected void doView(GProveedor bean){
		ProveedorDeGastosForm.showForm(bean,true);
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
		ProveedorDeGastosBrowser dialog=new ProveedorDeGastosBrowser();
		dialog.open();
	}
	

	public static void main(String[] args) {
		openDialog();
		System.exit(0);
		
	}

}
