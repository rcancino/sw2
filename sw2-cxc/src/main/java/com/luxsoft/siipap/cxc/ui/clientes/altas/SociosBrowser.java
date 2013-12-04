package com.luxsoft.siipap.cxc.ui.clientes.altas;

import java.util.List;

import javax.swing.Action;

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.gui.TableFormat;

import com.luxsoft.siipap.cxc.CXCActions;
import com.luxsoft.siipap.cxc.CXCRoles;
import com.luxsoft.siipap.model.User;
import com.luxsoft.siipap.model.core.Socio;
import com.luxsoft.siipap.service.KernellSecurity;
import com.luxsoft.siipap.service.ServiceLocator2;
import com.luxsoft.siipap.swing.actions.SWXAction;
import com.luxsoft.siipap.swing.dialog.UniversalAbstractCatalogDialog;
import com.luxsoft.siipap.swing.form2.DefaultFormModel;
import com.luxsoft.siipap.swing.utils.MessageUtils;


/**
 *  Browser para el mantenimiento de entidades de tipo {@link Socio}
 * 
 * @author Ruben Cancino
 *
 */
public class SociosBrowser extends UniversalAbstractCatalogDialog<Socio>{
	
	private static Action showAction;

	public SociosBrowser() {
		super(Socio.class,new BasicEventList<Socio>(), "Catálogo de socios de la unión");
		addTextEditor("Filtrar", "nombre");
	}

	
	/*
	 * (non-Javadoc)
	 * @see com.luxsoft.siipap.swing.dialog.UniversalAbstractCatalogDialog#getTableFormat()
	 */
	@Override
	protected TableFormat<Socio> getTableFormat() {
		final String[] cols={"id","clave","nombre","vendedor.nombreCompleto","direccion","comisionVendedor"};
		final String[] names={"id","Clave","Nombre","Vendedor","Dirección","Comisión Vend"};
		return GlazedLists.tableFormat(Socio.class, cols,names);
	}
	
	
	@Override
	protected List<Socio> getData() {
		return ServiceLocator2.getHibernateTemplate().find("from Socio ");
	}


	/**** Personalizacion de comportamiento (A/B/C) ****/
	
	@Override
	protected Socio doInsert() {	
		if(validarPerisos()){
			Socio socio=new Socio();
			DefaultFormModel model=new DefaultFormModel(socio);
			SociosForm form=new SociosForm(model);
			form.open();
			if(!form.hasBeenCanceled()){
				return (Socio)ServiceLocator2.getHibernateTemplate().merge(model.getBaseBean());
			}
		}
		return null;
	}
	
	@Override
	protected Socio doEdit(Socio bean) {
		if(validarPerisos()){
			Socio socio=(Socio)ServiceLocator2.getHibernateTemplate()
			.find("from Socio s  where s.id=?",bean.getId()).get(0);
			DefaultFormModel model=new DefaultFormModel(socio);
			SociosForm form=new SociosForm(model);
			form.open();
			if(!form.hasBeenCanceled()){
				return (Socio)ServiceLocator2.getHibernateTemplate().merge(model.getBaseBean());
			}
		}
		return bean;
	}
	
	private boolean validarPerisos(){
		boolean res=KernellSecurity.instance().hasRole(CXCRoles.ADMINISTRADOR_COBRANZA_CREDITO.name());
		if(!res){
			MessageUtils.showMessage("No cuenta con el rol requerido\n Rol: "+CXCRoles.ADMINISTRADOR_COBRANZA_CREDITO.name()
					, "Acceso no permitido");
			
		}
		return res;
	}
	
	protected void doView(Socio bean){
		Socio socio=(Socio)ServiceLocator2.getHibernateTemplate()
			.find("from Socio s where s.id=?",bean.getId()).get(0);
		DefaultFormModel model=new DefaultFormModel(socio,true);
		SociosForm form=new SociosForm(model);
		form.open();
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
		showAction.putValue(Action.NAME, "Socios");
		return showAction;
	}	
	
	public static void openDialog(){
		SociosBrowser dialog=new SociosBrowser();
		dialog.open();
	}
	

	/**
	 * Prueba local en el EDT
	 * 
	 * @param args
	 * 
	 */
	public static void main(String[] args) {
		javax.swing.SwingUtilities.invokeLater(new Runnable() {

			public void run() {
				com.luxsoft.siipap.swing.utils.SWExtUIManager.setup();
				openDialog();
				System.exit(0);				
			}

		});
	}

}
