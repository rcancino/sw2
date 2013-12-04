package com.luxsoft.siipap.gastos;

import java.util.List;

import javax.swing.JMenu;
import javax.swing.JMenuBar;

import org.apache.log4j.Logger;

import com.jgoodies.uif.builder.MenuBuilder;
import com.luxsoft.siipap.kernell.KernellActions;
import com.luxsoft.siipap.swing.impl.MenuFactoryImpl;

public class GasMenuFactory extends MenuFactoryImpl{
	
	@SuppressWarnings("unused")
	private Logger logger=Logger.getLogger(getClass());
	
	protected void buildCustomMenus(List<JMenu> customMenus){		
		customMenus.add(buildConsultas());
		customMenus.add(buildCatalogos());
		customMenus.add(buildCompras());
		customMenus.add(buildActivoFijo());
		customMenus.add(buildMantenimiento());
		customMenus.add(buildProcesos());
	}
	
	protected JMenuBar buildMenuBar(){
		JMenuBar bar=super.buildMenuBar();		
		return bar;
	}
	
	public JMenu buildConsultas(){		
		MenuBuilder builder=new MenuBuilder("Consultas",'n');
		builder.add(getActionManager().getAction(GasActions.showEstadoDeCuenta.getId()));
		builder.add(getActionManager().getAction(GasActions.ShowAnalisisDeGastos.getId()));
		return builder.getMenu();
	}	
	
	
	public JMenu buildCatalogos(){
		MenuBuilder builder=new MenuBuilder("Catalogos",'C');
		builder.add(getActionManager().getAction(GasActions.TiposDeProveedorBrowser.getId()));
		builder.add(getActionManager().getAction(GasActions.ProveedoresBrowser.getId()));
		builder.add(getActionManager().getAction(GasActions.ConceptosBrowser.getId()));
		builder.add(getActionManager().getAction(GasActions.ProductosBrowser.getId()));
		//builder.addSeparator();
		
		
		return builder.getMenu();
	}
	
	public JMenu buildCompras(){
		MenuBuilder builder=new MenuBuilder("Compras",'S');
		builder.add(getActionManager().getAction(GasActions.OrdenDeCompraBrowser.getId()));		
		builder.add(getActionManager().getAction(GasActions.ShowRequisicionesView.getId()));
		builder.add(getActionManager().getAction(GasActions.ShowPagosView.getId()));
		builder.add(getActionManager().getAction(GasActions.ShowPolizasView.getId()));
		return builder.getMenu();
	}
	
	public JMenu buildActivoFijo(){
		MenuBuilder builder=new MenuBuilder("Activo Fijo",'P');
		builder.add(buildAFCatalogos());
		builder.add(getActionManager().getAction(GasActions.ActivoFijoBrowser.getId()));
		return builder.getMenu();
	}
	
	public JMenu buildAFCatalogos(){
		MenuBuilder builder=new MenuBuilder("Catalogos",'C');		
		builder.add(getActionManager().getAction(GasActions.INPCBrowser.getId()));
		builder.add(getActionManager().getAction(GasActions.ClasificacionDeActivosBrowser.getId()));
		builder.add(getActionManager().getAction(GasActions.ConsignatarioBrowser.getId()));
		return builder.getMenu();
	}
	
	
	public JMenu buildMantenimiento(){
		MenuBuilder builder=new MenuBuilder("Mantenimiento",'M');
		builder.add(getActionManager().getAction(KernellActions.MostrarKernell.getId()));
		return builder.getMenu();
	}
	
	public JMenu buildProcesos(){
		MenuBuilder builder=new MenuBuilder("Procesos",'T');
		builder.add(getActionManager().getAction(GasActions.showAnalisisIETU.getId()));
		builder.add(getActionManager().getAction(GasActions.GenerarPolizaEgresoCompras.getId()));
		
		return builder.getMenu();
	}
	
	
	
	
}
