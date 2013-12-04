package com.luxsoft.siipap;

import javax.swing.Action;

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.gui.TableFormat;




import com.luxsoft.siipap.model.Departamento;
import com.luxsoft.siipap.swing.actions.SWXAction;
import com.luxsoft.siipap.swing.dialog.UniversalAbstractCatalogDialog;

/**
 *  
 * 
 * @author Ruben Cancino,Octavio Hernandez
 *
 */
public class DepartamentoBrowser extends UniversalAbstractCatalogDialog<Departamento>{
	
	private static Action showAction;

	public DepartamentoBrowser() {
		super(Departamento.class,new BasicEventList<Departamento>(), "Catálogo de Departamentos");
	}

	
	@Override
	protected TableFormat<Departamento> getTableFormat() {
		final String[] cols={"id","clave","descripcion"};
		final String[] names={"Id","Clave","Descripcion"};
		return GlazedLists.tableFormat(Departamento.class, cols,names);
	}
	
	/**** Personalizacion de comportamiento (A/B/C) ****/
	
	@Override
	protected Departamento doInsert() {
		Departamento res=DepartamentoForm.showForm(new Departamento());
		if(res!=null)
			return save(res);
		return null;
	}
	
	@Override
	protected Departamento doEdit(Departamento bean) {
		Departamento res=DepartamentoForm.showForm(bean);
		if(res!=null)
			return save(res);
		return null;
	}
	
	protected void doView(Departamento bean){
		DepartamentoForm.showForm(bean,true);
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
		DepartamentoBrowser dialog=new DepartamentoBrowser();
		dialog.open();
	}
	

	public static void main(String[] args) {
		openDialog();
		System.exit(0);
		
	}

}
