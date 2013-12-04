package com.luxsoft.siipap.tesoreria.catalogos;

import javax.swing.Action;

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.gui.TableFormat;

import com.luxsoft.siipap.model.tesoreria.Banco;
import com.luxsoft.siipap.swing.actions.SWXAction;
import com.luxsoft.siipap.swing.dialog.UniversalAbstractCatalogDialog;

/**
 *  
 * 
 * @author Ruben Cancino,Octavio Hernandez
 *
 */
public class ControlDeBancos extends UniversalAbstractCatalogDialog<Banco>{
	
	private static Action showAction;

	public ControlDeBancos() {
		super(Banco.class,new BasicEventList<Banco>(), "Catálogo de Bancos");
	}

	
	@Override
	protected TableFormat<Banco> getTableFormat() {
		final String[] cols={"id","empresa.id","clave","nombre"};
		final String[] names={"Id","EmpresId","Clave","Nombre"};
		return GlazedLists.tableFormat(Banco.class, cols,names);
	}
	
	/**** Personalizacion de comportamiento (A/B/C) ****/
	
	@Override
	protected Banco doInsert() {
		Banco res=BancoForm.showForm(new Banco());
		if(res!=null)
			return save(res);
		return null;
	}
	
	@Override
	protected Banco doEdit(Banco bean) {
		Banco res=BancoForm.showForm(bean);
		if(res!=null)
			return save(res);
		return null;
	}
	
	protected void doView(Banco bean){
		BancoForm.showForm(bean,true);
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
		ControlDeBancos dialog=new ControlDeBancos();
		dialog.open();
	}
	/**
	protected Banco save(Banco o){
		return (Banco)ServiceLocator2.getUniversalDao().save(o);
	}
	**/

	public static void main(String[] args) {
		openDialog();
		
	}

}
