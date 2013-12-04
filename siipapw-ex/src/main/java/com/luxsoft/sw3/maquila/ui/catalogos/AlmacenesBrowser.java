package com.luxsoft.sw3.maquila.ui.catalogos;

import java.util.ArrayList;
import java.util.List;

import javax.swing.Action;

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.gui.TableFormat;



import com.luxsoft.siipap.service.KernellSecurity;
import com.luxsoft.siipap.swing.actions.SWXAction;
import com.luxsoft.siipap.swing.dialog.UniversalAbstractCatalogDialog;
import com.luxsoft.siipap.swing.utils.CommandUtils;
import com.luxsoft.sw3.maquila.MAQUILA_ROLES;
import com.luxsoft.sw3.maquila.model.Almacen;


/**
 *  Browser para el mantenimiento de entidades de tipo {@link Almacen}
 * 
 * @author Ruben Cancino
 *
 */
public class AlmacenesBrowser extends UniversalAbstractCatalogDialog<Almacen>{
	
	private static Action showAction;

	public AlmacenesBrowser() {
		super(Almacen.class,new BasicEventList<Almacen>(), "Catálogo de almacenes de maquilador ");
	}
	
	protected List<Action> getToolbarActions(){
		List<Action> actions=new ArrayList<Action>();
		if(KernellSecurity.instance().hasRole(MAQUILA_ROLES.MANTENIMIENTO_CATALOGOS_MAQ.name())){
			actions.add(CommandUtils.createInsertAction(this, "insert"));
			actions.add(CommandUtils.createDeleteAction(this,"delete"));
			actions.add(CommandUtils.createEditAction(this,"edit"));
		}		
		actions.add(CommandUtils.createViewAction(this,"view"));
		actions.add(CommandUtils.createRefreshAction(this,"refresh"));
		return actions;
	}

	
	/*
	 * (non-Javadoc)
	 * @see com.luxsoft.siipap.swing.dialog.UniversalAbstractCatalogDialog#getTableFormat()
	 */
	@Override
	protected TableFormat<Almacen> getTableFormat() {
		final String[] cols={"id","nombre","maquilador.nombre","telefono1"};
		final String[] names={"Id","Nombre","Maquilador","Tel1"};
		return GlazedLists.tableFormat(Almacen.class, cols,names);
	}
	
	/**** Personalizacion de comportamiento (A/B/C) ****/
	
	@Override
	protected Almacen doInsert() {
		Almacen res=AlmacenForm.showForm(new Almacen());
		if(res!=null)
			return save(res);
		
		return null;
		
	}
	
	@Override
	protected Almacen doEdit(Almacen bean) {
		Almacen res=AlmacenForm.showForm(bean);
		if(res!=null)
			return save(res);
		return null;
			
	}
	
	protected void doView(Almacen bean){
		AlmacenForm.showForm(bean,true);
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
			showAction.putValue(Action.NAME, "Almacenes");
			showAction.putValue(Action.SHORT_DESCRIPTION, "Mantenimiento al catálogo de almacenes");
		return showAction;
	}	
	
	public static void openDialog(){
		AlmacenesBrowser dialog=new AlmacenesBrowser();
		dialog.open();
	}
	

	public static void main(String[] args) {
		openDialog();
		System.exit(0);
		
	}

}
