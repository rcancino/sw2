package com.luxsoft.sw3.embarques.ui.catalogos;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.swing.Action;

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.gui.TableFormat;



import com.luxsoft.siipap.service.KernellSecurity;
import com.luxsoft.siipap.service.ServiceLocator2;
import com.luxsoft.siipap.swing.actions.SWXAction;
import com.luxsoft.siipap.swing.dialog.UniversalAbstractCatalogDialog;
import com.luxsoft.siipap.swing.utils.CommandUtils;
import com.luxsoft.sw3.embarque.Chofer;
import com.luxsoft.sw3.embarque.EmbarquesRoles;




/**
 *  Browser para el mantenimiento de entidades de tipo {@link Chofer}
 * 
 * @author Ruben Cancino
 *
 */
public class ChoferesBrowser extends UniversalAbstractCatalogDialog<Chofer>{
	
	private static Action showAction;

	public ChoferesBrowser() {
		super(Chofer.class,new BasicEventList<Chofer>()
				, "Catálogo de choferes");
	}
	
	protected List<Action> getToolbarActions(){
		List<Action> actions=new ArrayList<Action>();
		if(KernellSecurity.instance().hasRole(EmbarquesRoles.ContralorDeEmbarques.name())){
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
	protected TableFormat<Chofer> getTableFormat() {
		final String[] cols={"id","nombre","radio","facturista.nombre","suspendido","suspendidoFecha","comentario"};
		final String[] names={"Id","Nombre","Radio","Facturista","Suspendido","Susp(Fecha)","Comentario"};
		return GlazedLists.tableFormat(Chofer.class, cols,names);
	}
	
	/**** Personalizacion de comportamiento (A/B/C) ****/
	
	@Override
	protected Chofer doInsert() {
		
		Chofer res=ChoferForm.showForm(new Chofer());
		if(res!=null)
			return save(res);
		return null;
		
	}
	
	@Override
	protected Chofer doEdit(Chofer bean) {
		Chofer res=ChoferForm.showForm(bean);
		if(res!=null)
			return save(res);
		return null;
			
	}
	
	protected void doView(Chofer bean){
		ChoferForm.showForm(bean,true);
	}
	
	/**** Fin Personalizacion de comportamiento****/

	@Override
	protected Chofer save(Chofer bean) {
		Date time=ServiceLocator2.obtenerFechaDelSistema();
		
		bean.setReplicado(null);
		
		String user=KernellSecurity.instance().getCurrentUserName();
		String ip=KernellSecurity.getIPAdress();
		String mac=KernellSecurity.getMacAdress();
		
		bean.getLog().setUpdateUser(user);
		bean.getLog().setModificado(time);
		if(bean.getId()!=null){
			bean.getLog().setCreado(time);
			bean.getLog().setCreateUser(user);
		}
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
			showAction.putValue(Action.NAME, "Choferes");
			showAction.putValue(Action.SHORT_DESCRIPTION, "Mantenimiento al catálogo de choferes");
		return showAction;
	}	
	
	public static void openDialog(){
		ChoferesBrowser dialog=new ChoferesBrowser();
		dialog.open();
	}
	

	public static void main(String[] args) {
		openDialog();
		System.exit(0);
		
	}

}
