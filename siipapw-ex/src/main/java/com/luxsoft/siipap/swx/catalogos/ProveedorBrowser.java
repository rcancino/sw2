package com.luxsoft.siipap.swx.catalogos;

import javax.swing.Action;
import javax.swing.JComponent;

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.gui.TableFormat;


import com.luxsoft.siipap.model.core.Proveedor;
import com.luxsoft.siipap.service.ServiceLocator2;
import com.luxsoft.siipap.swing.actions.SWXAction;
import com.luxsoft.siipap.swing.dialog.UniversalAbstractCatalogDialog;

/**
 *  
 * 
 * @author Ruben Cancino,Octavio Hernandez
 *
 */
public class ProveedorBrowser extends UniversalAbstractCatalogDialog<Proveedor>{
	
	private static Action showAction;

	public ProveedorBrowser() {
		super(Proveedor.class,new BasicEventList<Proveedor>(), "Catálogo de Proveedores ");
	}

	
	/*
	 * (non-Javadoc)
	 * @see com.luxsoft.siipap.swing.dialog.UniversalAbstractCatalogDialog#getTableFormat()
	 */
	@Override
	protected TableFormat<Proveedor> getTableFormat() {
		final String[] cols={"id","clave","nombreRazon","rfc","telefono1","contacto1","credito"};
		final String[] names={"Id","Clave","Nombre","RFC","Tel","Contacto","L.Crédito"};
		return GlazedLists.tableFormat(Proveedor.class, cols,names);
	}
	
	protected JComponent buildContent(){		
		addTextEditor(" Filtrar: ", "clave","nombre");
		JComponent c=super.buildContent();
		return c;
	}
	
	/**** Personalizacion de comportamiento (A/B/C) ****/
	
	@Override
	protected Proveedor doInsert() {
		Proveedor res=ProveedorForm.showForm(new Proveedor());
		if(res!=null)
			return save(res);
		return null;
	}
	
	@Override
	protected Proveedor doEdit(Proveedor bean) {
		Proveedor target=ServiceLocator2.getProveedorManager().get(bean.getId());
		Proveedor res=ProveedorForm.showForm(target);
		
		if(res!=null)
			return save(res);
		return null;
	}
	
	protected void doView(Proveedor bean){
		Proveedor target=ServiceLocator2.getProveedorManager().get(bean.getId());
		ProveedorForm.showForm(target,true);
	}
	
	
	
	
	
	/**** Fin Personalizacion de comportamiento****/

	@Override
	protected Proveedor save(Proveedor bean) {
		bean.setReplicado(null);
		return super.save(bean);
	}


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
		ProveedorBrowser dialog=new ProveedorBrowser();
		dialog.open();
	}
	

	public static void main(String[] args) {
		openDialog();
		System.exit(0);
		
	}

}
