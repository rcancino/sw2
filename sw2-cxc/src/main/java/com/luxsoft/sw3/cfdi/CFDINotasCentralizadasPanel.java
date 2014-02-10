package com.luxsoft.sw3.cfdi;

import java.util.Date;
import java.util.List;

import javax.swing.Action;
import javax.swing.JPopupMenu;

import com.luxsoft.cfdi.CFDIPrintUI;
import com.luxsoft.siipap.cxc.model.NotaDeCargo;
import com.luxsoft.siipap.cxc.model.NotaDeCredito;
import com.luxsoft.siipap.cxc.ui.CXCUIServiceFacade;
import com.luxsoft.siipap.service.ServiceLocator2;
import com.luxsoft.siipap.swing.browser.FilteredBrowserPanel;
import com.luxsoft.sw3.cfdi.model.CFDI;


public class CFDINotasCentralizadasPanel extends FilteredBrowserPanel<CFDI>{
	
	

	public CFDINotasCentralizadasPanel() {
		super(CFDI.class);
		setTitle("Comprobantes CFDI (Egresos)");		
	}
	
	public void init(){
		addProperty(
				"serie","tipo","folio","receptor","log.creado","total","estado","timbreFiscal.fechaTimbrado","timbreFiscal.UUID"
				,"rfc","comentario","cancelacion"
				);
		addLabels(
				"Serie","Tipo","Folio","Cliente","Fecha","Total","Estado","Timbrado","UUID","RFC","Comentario","Cancelacion"
				);
		installTextComponentMatcherEditor("Serie", "serie");
		installTextComponentMatcherEditor("Folio", "folio");
		installTextComponentMatcherEditor("Cliente", "receptor");
		installTextComponentMatcherEditor("Tipo", "tipo");
		installTextComponentMatcherEditor("Total", "total");
		manejarPeriodo();
	}
	
	@Override
	protected void afterGridCreated() {		
		super.afterGridCreated();
		JPopupMenu popup=new JPopupMenu("Operaciones");
		for(Action a:getActions()){
			popup.add(a);
		}
		getGrid().setComponentPopupMenu(popup);
	}
	
	@Override
	public Action[] getActions() {
		if(actions==null)
			actions=new Action[]{
				getLoadAction()
				,getViewAction()
				};
		return actions;
	}
	

	@Override
	protected List<CFDI> findData() {
		String hql="from CFDI c where date(c.log.creado) " +
				"between ? and ? and tipo like ?"; 
				
		return ServiceLocator2.getHibernateTemplate().find(hql
				, new Object[]{periodo.getFechaInicial()
				,periodo.getFechaFinal(),"NOTA%"}
				);
	}
	
	
	
	
	@Override
	protected List<Action> createProccessActions(){
		List<Action> res=super.createProccessActions();
		res.add(addAction(null, "timbrar", "Timbrar"));
		res.add(addAction(null, "mandarPorCorreoElectronico", "Mandar por Correo"));
		return res;
	}
	
	public void timbrar(){
		if(!getSelected().isEmpty()){
			CFDI selected=(CFDI)getSelectedObject();
			if(selected.getTipo().startsWith("NOTA_CREDITO")){
				NotaDeCredito nota=CXCUIServiceFacade.buscarNotaDeCreditoInicializada(selected.getOrigen());
				CXCUIServiceFacade.timbrar(nota);
			}else if(selected.getTipo().startsWith("NOTA_CARGO")){
				NotaDeCargo nc=CXCUIServiceFacade.buscarNotaDeCargoInicializada(selected.getOrigen());
				CXCUIServiceFacade.timbrar(nc);
			}
		}
	}

	@Override
	protected void doSelect(Object bean) {
		CFDI cfdi=(CFDI)bean;
		if(cfdi.getTipo().startsWith("NOTA_CREDITO")){
			//FacturaForm.show(cfdi.getOrigen());
			NotaDeCredito nota=CXCUIServiceFacade.buscarNotaDeCreditoInicializada(cfdi.getOrigen());
			Date time=ServiceLocator2.obtenerFechaDelSistema();
			CFDIPrintUI.impripirComprobante(nota, cfdi, " ", time,true);
		}
		if(cfdi.getTipo().equals("NOTA_CARGO")){
			NotaDeCargo nota=CXCUIServiceFacade.buscarNotaDeCargoInicializada(cfdi.getOrigen());
			Date time=ServiceLocator2.obtenerFechaDelSistema();
			CFDIPrintUI.impripirComprobante(nota, cfdi, "", time, true);
		}
		
	}

	
	public void mandarPorCorreoElectronico(){
		if(!getSelected().isEmpty()){
			//CorreoForm.mandarCorreo(getCliente(), getSelected());
			
		}
		
	}
	
	
	

}
