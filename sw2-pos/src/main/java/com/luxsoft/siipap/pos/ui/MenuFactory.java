package com.luxsoft.siipap.pos.ui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.EventHandler;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JMenu;
import javax.swing.JMenuBar;

import org.apache.log4j.Logger;

import com.jgoodies.uif.builder.MenuBuilder;
import com.jgoodies.uif.component.UIFMenuItem;
import com.luxsoft.siipap.model.User;
import com.luxsoft.siipap.pos.POSRoles;
import com.luxsoft.siipap.pos.ui.consultas.TransportistasBrowser;
import com.luxsoft.siipap.pos.ui.venta.forms.DevolucionForm;
import com.luxsoft.siipap.security.SeleccionDeUsuario;
import com.luxsoft.siipap.service.KernellSecurity;
import com.luxsoft.siipap.swing.AbstractView;
import com.luxsoft.siipap.swing.Application;
import com.luxsoft.siipap.swing.actions.ShowViewAction;
import com.luxsoft.siipap.swing.impl.MenuFactoryImpl;
import com.luxsoft.siipap.swing.utils.CommandUtils;
import com.luxsoft.siipap.swing.utils.MessageUtils;
import com.luxsoft.siipap.ventas.model.Devolucion;
import com.luxsoft.sw3.services.Services;
import com.luxsoft.sw3.tasks.CancelarFacturas;
import com.luxsoft.sw3.ui.services.KernellUtils;


public class MenuFactory extends MenuFactoryImpl{
	 
	@SuppressWarnings("unused")
	private Logger logger=Logger.getLogger(getClass());
	
	protected void buildCustomMenus(List<JMenu> customMenus){		
		customMenus.add(buildConsultas());
		customMenus.add(buildCatalogos());		
		customMenus.add(buildProcesos());
		customMenus.add(buildSistemasMenu());
	}
	
	protected JMenuBar buildMenuBar(){
		JMenuBar bar=super.buildMenuBar();		
		return bar;
	}
	
	private JMenu buildConsultas(){		
		MenuBuilder builder=new MenuBuilder("Operaciones",'n');
		//builder.add(getActionManager().getAction("showVentasView"));
		//builder.add(getActionManager().getAction("showComprasView"));
		//builder.add(getActionManager().getAction("showAlmacenView"));
		//builder.add(getActionManager().getAction("showEmbarquesView"));
		
		if(KernellSecurity.instance().hasRole(POSRoles.CAJERO.name())){
			ShowViewAction sa=new ShowViewAction("Caja"){

				@Override
				protected void execute() {
					Action delegate=getActionManager().getAction("showCajaView");
					User user=SeleccionDeUsuario.findUser(Services.getInstance().getHibernateTemplate());
					if((user!=null) && user.hasRole(POSRoles.CAJERO.name())){
						delegate.actionPerformed(null);
					}else{
						MessageUtils.showMessage("No tiene los derechos apropiados", "Caja");
					}
					
				}
				
			};
			sa.putValue(Action.NAME, "Caja");
			sa.putValue(Action.SMALL_ICON, CommandUtils.getIconFromResource("images2/money_dollar.png"));
			builder.add(sa);
		}
		//builder.add(getActionManager().getAction("showAutorizacionView"));
		builder.add(getActionManager().getAction("showNotasView"));
		builder.add(buildDevoluciones());
		return builder.getMenu();
	}
	
	private JMenu buildCatalogos(){
		MenuBuilder builder=new MenuBuilder("Catalogos",'C');
		UIFMenuItem item=builder.add("Transportisats");
		item.addActionListener(EventHandler.create(ActionListener.class, this, "catalogoTransporistas"));
		item.setMnemonic('T');
		boolean ok=KernellSecurity.instance().hasRole(POSRoles.VENDEDOR.name());
		item.setEnabled(ok);
		builder.add(getActionManager().getAction("consultaDeClientes"));
		//builder.add(getActionManager().getAction("clienteController"));
		
		return builder.getMenu();
	}
	
	private JMenu buildProcesos(){		
		MenuBuilder builder=new MenuBuilder("Procesos",'p');
		Action action=new CancelarFacturas();
		action.putValue(Action.NAME, "Cancelar Facturas");
		builder.add(action);
		return builder.getMenu();
	}
	
	private JMenu buildDevoluciones(){		
		MenuBuilder builder=new MenuBuilder("Devoluciones",'p');
		builder.add(new RegistrarDevolucionAction());
		return builder.getMenu();
	}
	
	public void catalogoTransporistas(){
		TransportistasBrowser browser=new TransportistasBrowser();
		browser.open();
	}
	
	private class RegistrarDevolucionAction extends AbstractAction{
		
		public RegistrarDevolucionAction(){
			super("Registrar");
		}

		public void actionPerformed(ActionEvent e) {
			Devolucion d=DevolucionForm.showForm();
			if(d!=null){
				MessageUtils.showMessage("Devolucion registrada :"+d.getNumero(), "Devoluciones");
			}
			
		}
		
	}
	
	private JMenu buildSistemasMenu(){
		MenuBuilder builder=new MenuBuilder("Sistemas",'S');
		
		
		builder.add(getActionManager().getAction("sistemasViewAction"));
		
		
		builder.addSeparator();
		return builder.getMenu();
	}
	
}
