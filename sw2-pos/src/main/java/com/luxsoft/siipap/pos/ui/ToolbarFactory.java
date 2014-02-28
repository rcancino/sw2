package com.luxsoft.siipap.pos.ui;

import java.awt.event.ActionEvent;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JOptionPane;

import org.apache.commons.lang.math.NumberUtils;

import com.jgoodies.uif.builder.ToolBarBuilder;
import com.luxsoft.siipap.cxc.model.OrigenDeOperacion;
import com.luxsoft.siipap.model.Sucursal;
import com.luxsoft.siipap.model.User;
import com.luxsoft.siipap.pos.POSRoles;
import com.luxsoft.siipap.pos.ui.forms.FacturaForm;
import com.luxsoft.siipap.security.SeleccionDeUsuario;
import com.luxsoft.siipap.swing.AbstractView;
import com.luxsoft.siipap.swing.Application;
import com.luxsoft.siipap.swing.actions.ShowViewAction;
import com.luxsoft.siipap.swing.impl.ToolbarFactoryImpl;
import com.luxsoft.siipap.swing.utils.CommandUtils;
import com.luxsoft.siipap.swing.utils.MessageUtils;
import com.luxsoft.sw3.embarque.EmbarquesRoles;
import com.luxsoft.sw3.services.Services;
import com.luxsoft.sw3.ui.command.BuscadorDeCargos;
import com.luxsoft.sw3.ui.command.ExistenciaFinder;
import com.luxsoft.sw3.ui.services.KernellUtils;

public class ToolbarFactory extends ToolbarFactoryImpl{
	
	
	protected void addCustomButtons(ToolBarBuilder builder){
		
		
		ShowViewAction vent=new ShowViewAction("Ventas"){
			protected void execute() {
				Action delegate=getActionManager().getAction("showVentasView");
				User user=SeleccionDeUsuario.findUser(Services.getInstance().getHibernateTemplate());
				if((user!=null) && user.hasRole(POSRoles.VENDEDOR.name())){
					delegate.actionPerformed(null);
				}else{
					MessageUtils.showMessage("No tiene los derechos apropiados", "Ventas");
				}
			}
		};
		
		vent.putValue(Action.NAME, "Ventas");
		vent.putValue(Action.SMALL_ICON, CommandUtils.getIconFromResource("images/misc2/basket_go.png"));
		builder.add(vent);
		
		
		//builder.add(getActionManager().getAction("showVentasView"));
		
		
		ShowViewAction comp=new ShowViewAction("Compras"){
		protected void execute() {
			Action delegate=getActionManager().getAction("showComprasView");
			User user=SeleccionDeUsuario.findUser(Services.getInstance().getHibernateTemplate());
			if((user!=null) && user.hasRole(POSRoles.CONTROLADOR_DE_COMPRAS.name())){
				delegate.actionPerformed(null);
			}else{
				MessageUtils.showMessage("No tiene los derechos apropiados", "Compras");
			}
		}
	};
	
	comp.putValue(Action.NAME, "Compras");
	comp.putValue(Action.SMALL_ICON, CommandUtils.getIconFromResource("images2/basket_put.png"));
	builder.add(comp);
		
		
    	ShowViewAction sal=new ShowViewAction("Almacen"){
		protected void execute() {
			Action delegate=getActionManager().getAction("showAlmacenView");
			User user=SeleccionDeUsuario.findUser(Services.getInstance().getHibernateTemplate());
			if((user!=null) && user.hasRole(POSRoles.CONTROLADOR_DE_INVENTARIOS.name())){
				delegate.actionPerformed(null);
			}else{
				MessageUtils.showMessage("No tiene los derechos apropiados", "Almacen");
			}
		}
	};
	
	sal.putValue(Action.NAME, "Almacen");
	sal.putValue(Action.SMALL_ICON, CommandUtils.getIconFromResource("images/misc2/categories.png"));
	builder.add(sal);
	
	
/*	ShowViewAction cau=new ShowViewAction("CancelacionAutorizada"){
		protected void execute() {
			Action delegate=getActionManager().getAction("showCancelacionAutorizadaView");
			User user=SeleccionDeUsuario.findUser(Services.getInstance().getHibernateTemplate());
			if((user!=null) && user.hasRole(POSRoles.CONTROLADOR_DE_INVENTARIOS.name())){
				delegate.actionPerformed(null);
			}else{
				MessageUtils.showMessage("No tiene los derechos apropiados", "CancelacionAutorizada");
			}
		}
	};
	
	cau.putValue(Action.NAME, "CancelacionAutorizada");
	cau.putValue(Action.SMALL_ICON, CommandUtils.getIconFromResource("images/misc2/categories.png"));
	builder.add(cau);*/
	
	
        
		ShowViewAction se=new ShowViewAction("Embarques"){
			protected void execute() {
				Action delegate=getActionManager().getAction("showEmbarquesView");
				User user=SeleccionDeUsuario.findUser(Services.getInstance().getHibernateTemplate());
				if((user!=null) && user.hasRole(EmbarquesRoles.ContralorDeEmbarques.name())){
					delegate.actionPerformed(null);
				}else{
					MessageUtils.showMessage("No tiene los derechos apropiados", "Embarques");
				}
			}
		};
		
				se.putValue(Action.NAME, "Embarques");
		se.putValue(Action.SMALL_ICON, CommandUtils.getIconFromResource("images2/lorry_go.png"));
		builder.add(se);
		
		
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
		//builder.add(getActionManager().getAction("showCajaView"));
		builder.add(buildBuscarVentaAction());
		builder.add(buildBuscarExistenciaAction());
	}
	public Action buildBuscarExistenciaAction(){
		ExistenciaFinder action=new ExistenciaFinder();
		action.putValue(Action.SMALL_ICON, CommandUtils.getIconFromResource("images/misc2/application_form_magnify.png"));
		action.putValue(Action.SHORT_DESCRIPTION, "Consultar Existencia");
		
		return action;
	}
	
	
	
	public Action buildBuscarVentaAction(){ 
		/*Action action=new AbstractAction("Buscar Venta"){
			public void actionPerformed(ActionEvent e) {
				String res=JOptionPane.showInputDialog(Application.instance().getMainFrame(), "Número de factura");
				Object[] selectionValues={OrigenDeOperacion.MOS,OrigenDeOperacion.CAM,OrigenDeOperacion.CRE};
				OrigenDeOperacion tipo=(OrigenDeOperacion)JOptionPane.showInputDialog(Application.instance().getMainFrame(), "Tipo de Venta"
						,"Ventas", JOptionPane.QUESTION_MESSAGE, null, selectionValues, OrigenDeOperacion.MOS);
				boolean valid=NumberUtils.isNumber(res);
				if(!valid){
					JOptionPane.showMessageDialog(Application.instance().getMainFrame(), "Documento invalido","Facturas",JOptionPane.ERROR_MESSAGE);
					return;
				}
				Long docto=Long.valueOf(res);
				Sucursal sucursal=Services.getInstance().getConfiguracion().getSucursal();
				String hql="select v.id from Venta v where v.origen=\'@ORIGEN\' and v.sucursal.id=? and v.documento=?";
				hql=hql.replaceAll("@ORIGEN", tipo.name());
				List<String> ids=Services.getInstance().getHibernateTemplate().find(hql, new Object[]{sucursal.getId(),docto});
				if(ids.isEmpty()){
					JOptionPane.showMessageDialog(Application.instance().getMainFrame(), "Documento inexistente","Facturas",JOptionPane.WARNING_MESSAGE);
					return;
				}else{
					for(String id:ids){
						FacturaForm.show(id);
					}
				}
				
			}
			
		};*/
		BuscadorDeCargos action=new BuscadorDeCargos();
		action.putValue(Action.SMALL_ICON, CommandUtils.getIconFromResource("images2/folder_find.png"));
		action.putValue(Action.SHORT_DESCRIPTION, "Buscar factura");
		
		return action;
	}

}
