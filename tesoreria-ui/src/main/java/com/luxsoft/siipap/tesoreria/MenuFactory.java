package com.luxsoft.siipap.tesoreria;

import java.util.List;

import javax.swing.JMenu;
import javax.swing.JMenuBar;

import org.apache.log4j.Logger;

import com.jgoodies.uif.builder.MenuBuilder;
import com.luxsoft.siipap.swing.impl.MenuFactoryImpl;

public class MenuFactory extends MenuFactoryImpl{
	
	@SuppressWarnings("unused")
	private Logger logger=Logger.getLogger(getClass());
	
	protected void buildCustomMenus(List<JMenu> customMenus){		
		customMenus.add(buildConsultas());
		customMenus.add(buildCatalogos());		
		customMenus.add(buildProcesos());
	}
	
	protected JMenuBar buildMenuBar(){
		JMenuBar bar=super.buildMenuBar();		
		return bar;
	}
	
	private JMenu buildConsultas(){		
		MenuBuilder builder=new MenuBuilder("Operaciones",'n');
		builder.add(getActionManager().getAction(TesoreriaActions.ShowMovimientosView.getId()));
		builder.add(getActionManager().getAction(TesoreriaActions.ShowRequisicionesView.getId()));
		
		
		return builder.getMenu();
	}
	
	private JMenu buildCatalogos(){
		MenuBuilder builder=new MenuBuilder("Catalogos",'C');
		builder.add(getActionManager().getAction(TesoreriaActions.ShowInstitucionBancaria.getId()));
		builder.add(getActionManager().getAction(TesoreriaActions.ShowCuentaBancaria.getId()));
		builder.add(getActionManager().getAction(TesoreriaActions.MantenimientoDeconceptos.getId()));
		
		return builder.getMenu();
	}
	
	private JMenu buildProcesos(){		
		MenuBuilder builder=new MenuBuilder("Procesos",'p');
		
		return builder.getMenu();
	}
	
}
