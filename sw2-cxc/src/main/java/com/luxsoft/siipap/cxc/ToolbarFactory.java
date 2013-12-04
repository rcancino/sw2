package com.luxsoft.siipap.cxc;

import java.awt.Component;

import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JToolBar;

import com.jgoodies.uif.builder.ToolBarBuilder;
import com.jgoodies.uif.component.PopupButton;
import com.luxsoft.siipap.model.Modulos;
import com.luxsoft.siipap.service.KernellSecurity;
import com.luxsoft.siipap.swing.actions.ShowViewAction;
import com.luxsoft.siipap.swing.impl.ToolbarFactoryImpl;
import com.luxsoft.siipap.swing.utils.CommandUtils;
import com.luxsoft.sw3.crm.CRM_Roles;

public class ToolbarFactory extends ToolbarFactoryImpl{
	
	
	protected void addCustomButtons(ToolBarBuilder builder){
	//	builder.add(getActionManager().getAction(TesActions.ShowReportsView.getId()));
	//	builder.add(getActionManager().getAction(TesActions.ShowInventarioDeMaquilaView.getId()));
		
		buildReportButton(builder.getToolBar());
		Action a=getActionManager().getAction("buscadorDeCargos");
		a.setEnabled(true);		
		builder.add(a);
		
		Action a2=getActionManager().getAction("buscadorDeNotasDeCredito");
		a2.setEnabled(true);
		builder.add(a2);
		//builder.add(getActionManager().getAction(CXCActions.ConsultasDeAnalisis.getId()));
		if(KernellSecurity.instance().hasRole(CRM_Roles.CRM_USER.name())){
			ShowViewAction sa=new ShowViewAction("cxc.AnalisisView");
			getActionManager().configure(sa, "cxc.AnalisisView");
			CommandUtils.configAction(sa, "consultasDeAnalisis", "images/misc2/report_picture.png");
			//builder.add(getActionManager().getAction(CXCActions.ConsultasDeAnalisis.getId()));
			builder.add(sa);
		}
		
	}
	
	
	private void buildReportButton(final JToolBar bar){		
		final JButton btn=new JButton("Reportes (CRE)",CommandUtils.getIconFromResource("images/misc2/report_picture.png"));
		final JPopupMenu pm=new JPopupMenu();
		pm.add(getActionManager().getAction(CXCActions.RevisionyCobro.getId()));
		pm.add(getActionManager().getAction(CXCActions.EstadoDeCuentaReport.getId()));
		
		Action a1=getActionManager().getAction(Reportes.CobranzaCredito.getId());
		a1.setEnabled(true);
		pm.add(a1);
		
		pm.add(getActionManager().getAction(Reportes.NotasDeCreditoGeneradas.getId()));
		pm.add(getActionManager().getAction(Reportes.NotasDeCargoGeneradas.getId()));
		pm.add(getActionManager().getAction(Reportes.PagosConNotaCre.getId()));
		pm.add(getActionManager().getAction(Reportes.AuxiliarNCCre.getId()));
		pm.add(getActionManager().getAction(Reportes.Provision.getId()));
		pm.add(getActionManager().getAction(Reportes.ClientesVencidos.getId()));
		pm.add(getActionManager().getAction(Reportes.Depositos.getId()));
		pm.add(getActionManager().getAction(Reportes.ChequeDevueltoContaForm.getId()));
		pm.add(getActionManager().getAction(Reportes.VentasPorVendedorReport.getId()));
		pm.add(getActionManager().getAction(Reportes.VentasCreditoContadoReport.getId()));
		pm.add(getActionManager().getAction(Reportes.ClientesCreditoReport.getId()));
		pm.add(getActionManager().getAction(Reportes.ClienteCreditoDetalleReport.getId()));
		pm.add(getActionManager().getAction("facturasPendientesPorRecibir"));
		pm.add(getActionManager().getAction("resumenDeCobranza"));
	 
		for(int i=0;i<pm.getComponentCount();i++){
			JMenuItem item=(JMenuItem)pm.getComponent(i);
			item.getAction().setEnabled(KernellSecurity.instance().isResourceGranted(CXCActions.ReportesGenerales.getId(), Modulos.CXC));
		}
		PopupButton pop=new PopupButton(btn,pm);
		pop.addTo(bar);
	}

}
