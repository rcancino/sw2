package com.luxsoft.siipap.swx;

import java.awt.event.ActionEvent;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;

import org.apache.log4j.Logger;

import com.jgoodies.uif.builder.MenuBuilder;
import com.luxsoft.siipap.compras.ComprasActions;
import com.luxsoft.siipap.cxp.CXPActions;
import com.luxsoft.siipap.gastos.GasActions;
import com.luxsoft.siipap.gastos.GasMenuFactory;
import com.luxsoft.siipap.inventario.InventariosActions;
import com.luxsoft.siipap.inventario.InventariosRoles;
import com.luxsoft.siipap.kernell.KernellActions;
import com.luxsoft.siipap.model.User;
import com.luxsoft.siipap.security.SeleccionDeUsuario;
import com.luxsoft.siipap.service.KernellSecurity;
import com.luxsoft.siipap.service.LoginManager;
import com.luxsoft.siipap.service.ServiceLocator2;
import com.luxsoft.siipap.swing.Application;
import com.luxsoft.siipap.swing.actions.ActionManager;
import com.luxsoft.siipap.swing.actions.ShowViewAction;
import com.luxsoft.siipap.swing.impl.MenuFactoryImpl;
import com.luxsoft.siipap.swing.login.LoginWindow;
import com.luxsoft.siipap.swing.utils.CommandUtils;
import com.luxsoft.siipap.swing.utils.MessageUtils;
import com.luxsoft.siipap.tesoreria.TesoreriaActions;
import com.luxsoft.siipap.ventas.VentasActions;
import com.luxsoft.sw3.contabilidad.CONTABILIDAD_ROLES;
import com.luxsoft.sw3.contabilidad.ui.form.TipoDeCambioBrowser;
import com.luxsoft.sw3.embarque.EmbarquesRoles;
import com.luxsoft.sw3.maquila.MAQUILA_ROLES;
import com.luxsoft.sw3.maquila.ui.catalogos.AlmacenesBrowser;
import com.luxsoft.sw3.tesoreria.TESORERIA_ROLES;

import com.luxsoft.sw3.cfd.ui.form.CertificadoForm;


public class MainMenuFactory extends MenuFactoryImpl{
	
	@SuppressWarnings("unused")
	private Logger logger=Logger.getLogger(getClass());
	
	 private GasMenuFactory gastosFactory=new GasMenuFactory();
	
	protected void buildCustomMenus(List<JMenu> customMenus){		
		
		customMenus.add(buildCatalogos());
		customMenus.add(buildCompras());
		customMenus.add(buildCXP());
		customMenus.add(buildVentas());
		//customMenus.add(buildCxC());
		customMenus.add(buildInventarios());
		customMenus.add(buildMaquila());
		
		if(KernellSecurity.instance().hasRole(TESORERIA_ROLES.TESORERIA_USER.name())){
			customMenus.add(buildTesoreria());
		}
		customMenus.add(buildGastos());
		customMenus.add(buildContabilidad());
		customMenus.add(buildBI());
		customMenus.add(buildMantenimiento());
		
	}
	
	protected JMenuBar buildMenuBar(){
		JMenuBar bar=super.buildMenuBar();		
		return bar;
	}
	
	private JMenu buildCatalogos(){
		MenuBuilder builder=new MenuBuilder("Catalogos",'C');		
		
		//Catalogos relacionados con productos
		builder.add(getActionManager().getAction(GlobalActions.LineasBrowser.getId()));
		builder.add(getActionManager().getAction(GlobalActions.ClasesBrowser.getId()));
		builder.add(getActionManager().getAction(GlobalActions.MarcasBrowser.getId()));
		builder.add(getActionManager().getAction(GlobalActions.ProductosBrowser.getId()));
		builder.add(getActionManager().getAction(GlobalActions.ProveedoresBrowser.getId()));
		builder.addSeparator();
		
		//Catalogos relacionados con clientes
		
		builder.add(getActionManager().getAction(GlobalActions.ClientesBrowser.getId()));
		builder.addSeparator();
		
		// Catalgos generales
		builder.add(getActionManager().getAction(GlobalActions.EmpresaBrowser.getId()));		
		builder.add(getActionManager().getAction(GlobalActions.SucursalBrowser.getId()));
		builder.add(getActionManager().getAction(GlobalActions.DepartamentoBrowser.getId()));
		builder.addSeparator();
		
		//Gastos
		JMenu catalogosGastos=gastosFactory.buildCatalogos();
		catalogosGastos.setText("Gastos");
		builder.add(catalogosGastos);
		
		//Activo Fijo
		JMenu afCatalogo=gastosFactory.buildAFCatalogos();
		afCatalogo.setText("Activo Fijo");
		builder.add(afCatalogo);
		
		
		return builder.getMenu();		
	}
	
	private JMenu buildCompras(){
		MenuBuilder builder=new MenuBuilder("Compras",'S');
		builder.add(getActionManager().getAction(ComprasActions.ShowComprasView.getId()));
		builder.add(getActionManager().getAction(ComprasActions.ShowListasDePrecios.getId()));
		builder.add(getActionManager().getAction(ComprasActions.ShowProductosView.getId())).setText("Productos"); // some bug no permite
		builder.add(getActionManager().getAction(ComprasActions.AnalisisDeComprasView.getId())); 
		return builder.getMenu();
	}
	
	private JMenu buildCXP(){
		MenuBuilder builder=new MenuBuilder("CxP",'P');
		builder.add(getActionManager().getAction(CXPActions.ShowCXPView.getId()));
		builder.add(getActionManager().getAction(CXPActions.ConsultasBI.getId()));
		return builder.getMenu();
	}
	
	
	private JMenu buildVentas(){
		MenuBuilder builder=new MenuBuilder("Ventas",'V');
		//Pedidos  			Levantar ventas nuevas
		//Cotizaciones		Levantar cotizaciones
		//Facturacion		Facturar pedidos (Mediane una lista)
		//Devoluciones
		//Operaciones		Lista de ventas generadas (ShowVentasTask)
		//Punto de Venta
		builder.add(getActionManager().getAction(VentasActions.PreciosDeVenta.getId()));
		builder.add(getActionManager().getAction(GlobalActions.ShowVentasTaskView.getId()));
		builder.add(getActionManager().getAction(GlobalActions.ShowAnalisisDeVentasView.getId()));
		if(KernellSecurity.instance().hasRole(EmbarquesRoles.ContralorDeEmbarques.name())){
			builder.add(getActionManager().getAction("ContralorDeEmbarques"));
		}
		return builder.getMenu();
	}
	
	private JMenu buildInventarios(){
		MenuBuilder builder=new MenuBuilder("Inventarios",'I');
		builder.add(getActionManager().getAction(InventariosActions.ConsultaDeCostosDeInventario.getId()));
		if(KernellSecurity.instance().hasRole(InventariosRoles.CONSULTA_MOVIMIENTOS_CENTRALIZADOS.name())){
			builder.add(getActionManager().getAction("consultaDeMovimientosDeInventario"));
		}
		ShowViewAction sa=new ShowViewAction(){
			protected void execute() {
				
				LoginWindow window=new LoginWindow(){
					public void loggedIn() {
						User user=LoginManager.getCurrentUser();
						System.out.println("Current user: "+user);
						Action delegate=getActionManager().getAction("showProductosParaInventariosPanel");
						if(user.hasRole(InventariosRoles.GERENTE_INVENTARIOS.name())){
							delegate.actionPerformed(null);
						}else{
							MessageUtils.showMessage("No tiene los derechos apropiados", "Inventarios");
						}
					};
				};
				window.login();
				
			}
		};
		sa.putValue(Action.NAME, "Mto Productos");
		builder.add(sa);
		return builder.getMenu();
	}
	
	private JMenu buildMaquila(){
		MenuBuilder builder=new MenuBuilder("Maquila",'Q');
		JMenu catalogosMnu=new JMenu("Catálogos");
		if(KernellSecurity.instance().hasRole(MAQUILA_ROLES.MAQUILA_USER.name())){			
			catalogosMnu.add(AlmacenesBrowser.getShowAction());
		}
		builder.add(catalogosMnu);
		builder.addSeparator();
		builder.add(getActionManager().getAction("showMaquilaView"));
		return builder.getMenu();
	}
	
	
	
	private JMenu buildGastos(){
		JMenu mnu=gastosFactory.buildCompras();
		mnu.setText("Gastos");
		mnu.setMnemonic('g');
		mnu.add(gastosFactory.buildProcesos());
		mnu.add(gastosFactory.buildConsultas());
		mnu.addSeparator();
		mnu.add(buildActivoFijo());
		return mnu;
	}
	
	private JMenu buildTesoreria(){
		MenuBuilder builder=new MenuBuilder("Tesorería",'T');
		builder.add(getActionManager().getAction(TesoreriaActions.MantenimientoDeTarjetas.getId()));
		builder.add(getActionManager().getAction(TESORERIA_ROLES.SolicitudesDeDepositosView.getId()));
		ShowViewAction control1=new ShowViewAction("tesoreria.controlDeIngresosView");
		//control1.setApplicationContext(Application.instance().getApplicationContext());
		control1.putValue(Action.NAME, "Registro de ingresos");
		builder.add(control1);
		return builder.getMenu();
	}
	
	private JMenu buildBI(){
		MenuBuilder builder=new MenuBuilder("BI",'x');
		//builder.add(getActionManager().getAction(GlobalActions.ShowReportView.getId()));
		builder.add(getActionManager().getAction(GlobalActions.ShowBIConsultasView.getId()));
		builder.add(getActionManager().getAction("biShowReportsView"));
		return builder.getMenu();
	}
	
	private JMenu buildContabilidad(){
		MenuBuilder builder=new MenuBuilder("Contabilidad",'b');
		
		if(KernellSecurity.instance().hasRole(CONTABILIDAD_ROLES.CFD_USER.getId())){
			builder.add(buildCFDMenu());
			if(KernellSecurity.instance().hasRole(CONTABILIDAD_ROLES.CFD_ADMINISTRACION.getId())){
				Action a=new AbstractAction("Tipos de Cambio"){
					public void actionPerformed(ActionEvent e) {
						TipoDeCambioBrowser browser=new TipoDeCambioBrowser();
						browser.open();
					}
				};
				builder.add(a);
			}
			builder.add(getActionManager().getAction("showCuentasContablesView"));
			//builder.add(getActionManager().getAction("showPolizasView"));
			builder.add(getActionManager().getAction("showPolizasView2"));
			builder.add(getActionManager().getAction("showContabilidadReportView"));
		}
		return builder.getMenu();
	}
	
	private JMenu buildCFDMenu(){
		MenuBuilder builder=new MenuBuilder("Comprobantes CFD",'F');
		
		
		builder.add(getActionManager().getAction("cfdFolios"));
		if(KernellSecurity.instance().hasRole(CONTABILIDAD_ROLES.CFD_ADMINISTRACION.getId())){
			Action a=new AbstractAction("Certificados"){
				public void actionPerformed(ActionEvent e) {
					CertificadoForm.showForm();
				}
			};
			builder.add(a);
		}
		
		if(KernellSecurity.instance().hasRole(CONTABILIDAD_ROLES.CFD_USER.getId())){
			ShowViewAction sh1=new ShowViewAction("cfdView");
			CommandUtils.configAction(sh1, "showComprobantesFiscalesView", null);
			builder.add(sh1);
		}
		
		return builder.getMenu();
	}
	
	
	private JMenu buildMantenimiento(){
		MenuBuilder builder=new MenuBuilder("Sistemas",'S');
		builder.add(getActionManager().getAction(KernellActions.MostrarKernell.getId()));
		builder.add(getActionManager().getAction("sistemasAdminViewAction"));
		return builder.getMenu();
	}
	
	public JMenu buildActivoFijo(){
		MenuBuilder builder=new MenuBuilder("Activo Fijo",'P');		
		builder.add(getActionManager().getAction(GasActions.ActivoFijoBrowser.getId()));
		return builder.getMenu();
	}

	@Override
	public void setActionManager(ActionManager actionManager) {		
		super.setActionManager(actionManager);
		this.gastosFactory.setActionManager(actionManager);
	}
	
	
	
}
