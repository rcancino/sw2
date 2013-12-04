package com.luxsoft.siipap.tesoreria.catalogos;

import javax.swing.Action;

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.gui.TableFormat;


import com.luxsoft.siipap.model.tesoreria.Concepto;
import com.luxsoft.siipap.swing.actions.SWXAction;
import com.luxsoft.siipap.swing.dialog.UniversalAbstractCatalogDialog;

/**
 *  
 * 
 * @author Ruben Cancino,Octavio Hernandez
 *
 */
public class ConceptosBrowser extends UniversalAbstractCatalogDialog<Concepto>{
	
	private static Action showAction;

	public ConceptosBrowser() {
		super(Concepto.class,new BasicEventList<Concepto>(), "Catálogo de Conceptos ");
		addTextEditor("Filtrar", "clave","descripcion","tipo","clase");
	}

	
	@Override
	protected TableFormat<Concepto> getTableFormat() {
		final String[] cols={"id","clave","descripcion","tipo","clase"};
		final String[] names={"Id","Clave","Descripción","Tipo","CLase"};
		return GlazedLists.tableFormat(Concepto.class, cols,names);
	}
	
	/**** Personalizacion de comportamiento (A/B/C) ****/
	
	@Override
	protected Concepto doInsert() {
		Concepto res=ConceptoForm.showForm(new Concepto());
		if(res!=null)
			return save(res);
		return null;
	}
	
	@Override
	protected Concepto doEdit(Concepto bean) {
		Concepto res=ConceptoForm.showForm(bean);
		if(res!=null)
			return save(res);
		return null;
	}
	
	protected void doView(Concepto bean){
		ConceptoForm.showForm(bean,true);
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
		ConceptosBrowser dialog=new ConceptosBrowser();
		dialog.open();
	}
	

	public static void main(String[] args) {
		openDialog();
		
	}

}
