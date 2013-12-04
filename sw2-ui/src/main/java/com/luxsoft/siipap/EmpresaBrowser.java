package com.luxsoft.siipap;

import javax.swing.Action;

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.gui.TableFormat;




import com.luxsoft.siipap.model.Empresa;
import com.luxsoft.siipap.swing.actions.SWXAction;
import com.luxsoft.siipap.swing.dialog.UniversalAbstractCatalogDialog;

/**
 *  
 * 
 * @author Ruben Cancino,Octavio Hernandez
 *
 */
public class EmpresaBrowser extends UniversalAbstractCatalogDialog<Empresa>{
	
	private static Action showAction;

	public EmpresaBrowser() {
		super(Empresa.class,new BasicEventList<Empresa>(), "Catálogo de Empresas");
	}

	
	@Override
	protected TableFormat<Empresa> getTableFormat() {
		final String[] cols={"id","clave","nombre"};
		final String[] names={"Id","Clave","Nombre"};
		return GlazedLists.tableFormat(Empresa.class, cols,names);
	}
	
	/**** Personalizacion de comportamiento (A/B/C) ****/
	
	@Override
	protected Empresa doInsert() {
		Empresa res=EmpresaForm.showForm(new Empresa());
		if(res!=null)
			return save(res);
		return null;
	}
	
	@Override
	protected Empresa doEdit(Empresa bean) {
		Empresa res=EmpresaForm.showForm(bean);
		if(res!=null)
			return save(res);
		return null;
	}
	
	protected void doView(Empresa bean){
		EmpresaForm.showForm(bean,true);
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
		EmpresaBrowser dialog=new EmpresaBrowser();
		dialog.open();
	}
	

	public static void main(String[] args) {
		openDialog();
		System.exit(0);
		
	}

}
