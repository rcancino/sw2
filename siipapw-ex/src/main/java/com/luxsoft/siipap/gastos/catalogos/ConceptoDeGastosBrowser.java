package com.luxsoft.siipap.gastos.catalogos;

import javax.swing.Action;

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.gui.TableFormat;



import com.luxsoft.siipap.model.gastos.ConceptoDeGasto;
import com.luxsoft.siipap.swing.actions.SWXAction;
import com.luxsoft.siipap.swing.dialog.UniversalAbstractCatalogDialog;

/**
 *  
 * 
 * @author Ruben Cancino,Octavio Hernandez
 *
 */
public class ConceptoDeGastosBrowser extends UniversalAbstractCatalogDialog<ConceptoDeGasto>{
	
	private static Action showAction;

	public ConceptoDeGastosBrowser() {
		super(ConceptoDeGasto.class,new BasicEventList<ConceptoDeGasto>(), "Catálogo de ConceptoDeGastos ");
		addTextEditor("Buscar", "clave","descripcion");
	}

	
	@Override
	protected TableFormat<ConceptoDeGasto> getTableFormat() {
		final String[] cols={"id","clave","descripcion","parent.clave","cuentaContable","ietu"};
		final String[] names={"Id","Concepto","Descripcion","Padre","Cuenta","IETU"};
		return GlazedLists.tableFormat(ConceptoDeGasto.class, cols,names);
	}
	
	/**** Personalizacion de comportamiento (A/B/C) ****/
	
	@Override
	protected ConceptoDeGasto doInsert() {
		ConceptoDeGasto res=ConceptoDeGastoForm.showForm(new ConceptoDeGasto());
		if(res!=null)
			return save(res);
		return null;
	}
	
	@Override
	protected ConceptoDeGasto doEdit(ConceptoDeGasto bean) {
		ConceptoDeGasto res=ConceptoDeGastoForm.showForm(bean);
		if(res!=null)
			return save(res);
		return null;
	}
	
	protected void doView(ConceptoDeGasto bean){
		ConceptoDeGastoForm.showForm(bean,true);
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
		ConceptoDeGastosBrowser dialog=new ConceptoDeGastosBrowser();
		dialog.open();
	}
	

	public static void main(String[] args) {
		openDialog();
		System.exit(0);
		
	}

}
