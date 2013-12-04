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

import com.luxsoft.sw3.embarque.ClientePorTonelada;
import com.luxsoft.sw3.embarque.EmbarquesRoles;
import com.luxsoft.sw3.embarques.ui.forms.ClientePorToneladaForm;




/**
 *  Browser para el mantenimiento de entidades de tipo {@link Chofer}
 * 
 * @author Ruben Cancino
 *
 */
public class ClientesPorToneladaBrowser extends UniversalAbstractCatalogDialog<ClientePorTonelada>{
	
	private static Action showAction;

	public ClientesPorToneladaBrowser() {
		super(ClientePorTonelada.class,new BasicEventList<ClientePorTonelada>()
				, "Clientes con precio por tonelada");
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
	protected TableFormat<ClientePorTonelada> getTableFormat() {
		final String[] cols={"id","cliente.clave","cliente.nombre","precio","comentario"};
		final String[] names={"Id","Cliente","Nombre","Precio (TON)","Comentario"};
		return GlazedLists.tableFormat(ClientePorTonelada.class, cols,names);
	}
	
	/**** Personalizacion de comportamiento (A/B/C) ****/
	
	@Override
	protected ClientePorTonelada doInsert() {
		ClientePorTonelada res= ClientePorToneladaForm.showForm();
		if(res!=null)
			return save(res);
		else
			return null;
		
	}
	
	@Override
	protected ClientePorTonelada doEdit(ClientePorTonelada bean) {
		ClientePorTonelada target= ClientePorToneladaForm.showForm(bean, false);
		if(target!=null)
			return save(target);
		else
			return (ClientePorTonelada) ServiceLocator2
			.getHibernateTemplate().get(ClientePorTonelada.class, bean.getId());
			
	}
	
	protected void doView(ClientePorTonelada bean){
		ClientePorToneladaForm.showForm(bean, true);
		
	}
	
	/**** Fin Personalizacion de comportamiento****/

	@Override
	protected ClientePorTonelada save(ClientePorTonelada bean) {
		Date time=ServiceLocator2.obtenerFechaDelSistema();
		
		//bean.setReplicado(null);
		
		String user=KernellSecurity.instance().getCurrentUserName();
		
		
		bean.getLog().setUpdateUser(user);
		bean.getLog().setModificado(time);
		if(bean.getId()==null){
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
		ClientesPorToneladaBrowser dialog=new ClientesPorToneladaBrowser();
		dialog.open();
	}
	

	public static void main(String[] args) {
		openDialog();
		System.exit(0);
		
	}

}
