package com.luxsoft.siipap.tesoreria.catalogos;

import javax.swing.Action;

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.gui.TableFormat;


import com.luxsoft.siipap.model.tesoreria.Cuenta;
import com.luxsoft.siipap.swing.actions.SWXAction;
import com.luxsoft.siipap.swing.dialog.UniversalAbstractCatalogDialog;

/**
 *  
 * 
 * @author Ruben Cancino,Octavio Hernandez
 *
 */
public class CuentasBrowser extends UniversalAbstractCatalogDialog<Cuenta>{
	
	private static Action showAction;

	public CuentasBrowser() {
		super(Cuenta.class,new BasicEventList<Cuenta>(), "Catálogo de Cuentas ");
	}

	
	@Override
	protected TableFormat<Cuenta> getTableFormat() {
		final String[] cols={"id","clave","banco.clave","numero","descripcion","cuentaContable","moneda","tipo","encriptar"};
		final String[] names={"Id","Clave","Banco","Número","Descripcion","Cuenta Contable","Moneda","Tipo","Encriptada"};
		return GlazedLists.tableFormat(Cuenta.class, cols,names);
	}
	
	/**** Personalizacion de comportamiento (A/B/C) ****/
	
	@Override
	protected Cuenta doInsert() {
		Cuenta res=CuentaForm.showForm(new Cuenta());
		if(res!=null)
			return save(res);
		return null;
	}
	
	@Override
	protected Cuenta doEdit(Cuenta bean) {		
		Cuenta res=CuentaForm.showForm(bean);
		if(res!=null)
			return save(res);
		return null;
	}
	
	protected void doView(Cuenta bean){
		CuentaForm.showForm(bean,true);
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
		CuentasBrowser dialog=new CuentasBrowser();
		dialog.open();
	}
	

	public static void main(String[] args) {
		openDialog();
		
	}

}
