package com.luxsoft.siipap.cxc;

import java.util.List;

import javax.swing.Action;
import javax.swing.JMenu;
import javax.swing.JMenuBar;

import org.apache.log4j.Logger;

import com.jgoodies.uif.builder.MenuBuilder;
import com.luxsoft.siipap.cxc.ui.clientes.altas.SociosBrowser;
import com.luxsoft.siipap.service.KernellSecurity;
import com.luxsoft.siipap.swing.actions.ShowViewAction;
import com.luxsoft.siipap.swing.impl.MenuFactoryImpl;
import com.luxsoft.sw3.crm.CRM_Roles;
import com.luxsoft.sw3.crm.catalogos.CRM_ClienteBrowser;


public class MenuFactory extends MenuFactoryImpl{
	
	@SuppressWarnings("unused")
	private Logger logger=Logger.getLogger(getClass());
	
	protected void buildCustomMenus(List<JMenu> customMenus){
		customMenus.add(buildCatalogos());
		customMenus.add(buildCxCMenu());
		
		if(KernellSecurity.instance().hasRole(CRM_Roles.CRM_USER.name())){
			customMenus.add(buildCRM());
		}
		customMenus.add(buildSistemasMenu());
		
	}
	
	protected JMenuBar buildMenuBar(){
		JMenuBar bar=super.buildMenuBar();		
		return bar;
	}
	
	@Override
	public JMenu getModuleMenu() {
		return buildCxCMenu();
	}

	private JMenu buildCxCMenu(){		
		MenuBuilder builder=new MenuBuilder("CxC",'n');		
		builder.add(getActionManager().getAction(CXCActions.CuentasPorCobrar.getId()));
		
		
		builder.add(getActionManager().getAction(CXCActions.RegistrarPagos.getId()));
		builder.add(getActionManager().getAction(CXCActions.GenerarPolizasContablesCxC.getId()));
		builder.add(getActionManager().getAction(CXCRoles.CarteraDeContado.name()));
		
		
		builder.add(getViewAction("cxc.ListaDePreciosyDescuentosView",CXCRoles.LISTA_DE_PRECIOS_CLIENTES.name()));
		
		return builder.getMenu();
	}
	
	private JMenu buildCatalogos(){
		MenuBuilder builder=new MenuBuilder("Catalogos",'C');
		//Catalogos relacionados con clientes
		
		if(KernellSecurity.instance().hasRole(CRM_Roles.CRM_USER.name())){
			builder.add(getActionManager().getAction(CXCActions.ConsultaDeClientes.getId()));
		}
		
		//builder.add(getActionManager().getAction(CXCActions.ConsultaDeClientes.getId()));
		builder.add(SociosBrowser.getShowAction());
		builder.addSeparator();
		return builder.getMenu();
	}

	 
	private JMenu buildCRM(){
		MenuBuilder builder=new MenuBuilder("CRM",'R');
		
		JMenu subMenu=new JMenu("Catálogos");
		//Catalogos clientes
		if(KernellSecurity.instance().hasRole(CRM_Roles.MANTENIMIENTO_CLIENTES.name())){
			subMenu.add(CRM_ClienteBrowser.getShowAction());
		}
		builder.add(subMenu);
		builder.addSeparator();
		return builder.getMenu();
	}
	
	private JMenu buildSistemasMenu(){
		MenuBuilder builder=new MenuBuilder("Sistemas",'S');
		ShowViewAction sistemasViewAction=new ShowViewAction("sistemasView");
		sistemasViewAction.putValue(Action.NAME, "Solicitudes");
		builder.add(sistemasViewAction);
		builder.addSeparator();
		return builder.getMenu();
	}
	
}
