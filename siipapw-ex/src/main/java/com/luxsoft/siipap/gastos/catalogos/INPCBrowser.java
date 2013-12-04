package com.luxsoft.siipap.gastos.catalogos;

import javax.swing.Action;

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.gui.TableFormat;




import com.luxsoft.siipap.model.gastos.INPC;
import com.luxsoft.siipap.swing.actions.SWXAction;
import com.luxsoft.siipap.swing.dialog.UniversalAbstractCatalogDialog;

/**
 *  
 * 
 * @author Ruben Cancino,Octavio Hernandez
 *
 */
public class INPCBrowser extends UniversalAbstractCatalogDialog<INPC>{
	
	private static Action showAction;

	public INPCBrowser() {
		super(INPC.class,new BasicEventList<INPC>(), "INPC");
	}

	
	@Override
	protected TableFormat<INPC> getTableFormat() {
		final String[] cols={"id","year","mes","indice"};
		final String[] names={"Id","Año","Mes","Indice"};
		return GlazedLists.tableFormat(INPC.class, cols,names);
	}
	
	/**** Personalizacion de comportamiento (A/B/C) ****/
	
	@Override
	protected INPC doInsert() {
		INPC res=INPCForm.showForm(new INPC());
		if(res!=null)
			return save(res);
		return null;
	}
	
	@Override
	protected INPC doEdit(INPC bean) {
		INPC res=INPCForm.showForm(bean);
		if(res!=null)
			return save(res);
		return null;
	}
	
	protected void doView(INPC bean){
		INPCForm.showForm(bean,true);
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
		INPCBrowser dialog=new INPCBrowser();
		dialog.open();
	}
	

	public static void main(String[] args) {
		openDialog();
		System.exit(0);
		
	}

}
