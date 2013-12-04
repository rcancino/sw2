package com.luxsoft.siipap.gastos.catalogos;

import javax.swing.Action;

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.gui.TableFormat;





import com.luxsoft.siipap.model.gastos.ClasificacionDeActivo;
import com.luxsoft.siipap.swing.actions.SWXAction;
import com.luxsoft.siipap.swing.dialog.UniversalAbstractCatalogDialog;

/**
 *  
 * 
 * @author Ruben Cancino,Octavio Hernandez
 *
 */
public class ClasificacionDeActivoBrowser extends UniversalAbstractCatalogDialog<ClasificacionDeActivo>{
	
	private static Action showAction;

	public ClasificacionDeActivoBrowser() {
		super(ClasificacionDeActivo.class,new BasicEventList<ClasificacionDeActivo>(), "Clasificación de Activo Fijo");
	}

	
	@Override
	protected TableFormat<ClasificacionDeActivo> getTableFormat() {
		final String[] cols={"id","nombre","tasa","cuentaContable"};
		final String[] names={"Id","Nombre","Tasa","Cuenta"};
		return GlazedLists.tableFormat(ClasificacionDeActivo.class, cols,names);
	}
	
	/**** Personalizacion de comportamiento (A/B/C) ****/
	
	@Override
	protected ClasificacionDeActivo doInsert() {
		ClasificacionDeActivo res=ClasificacionDeActivoForm.showForm(new ClasificacionDeActivo());
		if(res!=null)
			return save(res);
		return null;
	}
	
	@Override
	protected ClasificacionDeActivo doEdit(ClasificacionDeActivo bean) {
		ClasificacionDeActivo res=ClasificacionDeActivoForm.showForm(bean);
		if(res!=null)
			return save(res);
		return null;
	}
	
	protected void doView(ClasificacionDeActivo bean){
		ClasificacionDeActivoForm.showForm(bean,true);
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
		ClasificacionDeActivoBrowser dialog=new ClasificacionDeActivoBrowser();
		dialog.open();
	}
	

	public static void main(String[] args) {
		openDialog();
		System.exit(0);
		
	}

}
