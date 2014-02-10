package com.luxsoft.siipap.pos.ui.cfdi;

import java.util.Date;
import java.util.List;

import javax.swing.Action;
import javax.swing.JPopupMenu;

import org.apache.commons.lang.exception.ExceptionUtils;

import com.luxsoft.cfdi.CFDIPrintUI;
import com.luxsoft.siipap.inventarios.model.Traslado;
import com.luxsoft.siipap.pos.ui.forms.FacturaForm;
import com.luxsoft.siipap.pos.ui.utils.ReportUtils2;
import com.luxsoft.siipap.service.ServiceLocator2;
import com.luxsoft.siipap.swing.browser.FilteredBrowserPanel;
import com.luxsoft.siipap.swing.utils.MessageUtils;
import com.luxsoft.siipap.ventas.model.Venta;
import com.luxsoft.sw3.cfdi.model.CFDI;
import com.luxsoft.sw3.services.Services;



public class CFDIPanel extends FilteredBrowserPanel<CFDI>{
	
	

	public CFDIPanel() {
		super(CFDI.class);
		setTitle("Comprobantes fiscales digitales por Internet(CFDI)");		
	}
	
	public void init(){
		addProperty(
				"serie","tipo","folio","receptor","log.creado","total","estado","timbreFiscal.fechaTimbrado","timbreFiscal.UUID","rfc"
				);
		addLabels(
				"Serie","Tipo","Folio","Cliente","Fecha","Total","Estado","Timbrado","UUID","RFC"
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
		String hql="from CFDI c where date(c.log.creado)"+
				//= ? " +
				"between ? and ? " +
				" and c.origen not in(select cc.cargo.id from CancelacionDeCargo cc)";
		return Services.getInstance().getHibernateTemplate().find(hql
				//, new Object[]{new Date()}
				, new Object[]{periodo.getFechaInicial(),periodo.getFechaFinal()}
				);
	}
	
	
	
	
	@Override
	protected List<Action> createProccessActions(){
		List<Action> res=super.createProccessActions();
		//res.add(addAction(null, "mandarPorCorreoElectronico", "Mandar por Correo"));
		//res.add(addAction(null, "timbrar", "Timbrar"));
		return res;
	}

	@Override
	protected void doSelect(Object bean) {
		CFDI cfdi=(CFDI)bean;
		if(cfdi.getTipo().equals("FACTURA")){
			//FacturaForm.show(cfdi.getOrigen());
			Venta venta=Services.getInstance().getFacturasManager().buscarVentaInicializada(cfdi.getOrigen());
			Date time=Services.getInstance().obtenerFechaDelSistema();
			CFDIPrintUI.impripirComprobante(venta, cfdi, " ", time,Services.getInstance().getHibernateTemplate(),true);
		}else if(cfdi.getTipo().equals("TPS")){
			System.out.println("CFDI de TPS...");
			List<Traslado> found=Services.getInstance().getHibernateTemplate().find("from Traslado t left join fetch t.partidas where t.id=?"
					,cfdi.getOrigen());
			for(Traslado tps :found){
				CFDIPrintUI.impripirComprobante(tps,cfdi, true);
			}
		}
		
	}

	
	public void mandarPorCorreoElectronico(){
		if(!getSelected().isEmpty()){
			//CorreoForm.mandarCorreo(getCliente(), getSelected());
		}
		
	}
	
	public void timbrar(){
		CFDI cfdi=(CFDI)getSelectedObject();
			if(cfdi!=null){
				if(cfdi.getTimbreFiscal().getUUID()!=null){
					MessageUtils.showMessage("CFDI ya generado para la venta UUID: "+cfdi.getTimbreFiscal().getUUID(), "CFDI");
					return;
				}
				int index=source.indexOf(cfdi);
				try {
					logger.info("Timbrando CFDI: "+cfdi);
					CFDI res=ServiceLocator2.getCFDIManager().timbrar(cfdi);
					if(index!=-1){
						source.set(index, res);
					}
					doSelect(res);
				} catch (Exception e) {
					e.printStackTrace();
					MessageUtils.showMessage(ExceptionUtils.getRootCauseMessage(e), "Timbrado de CFDI");
					return;
				}
			}
	}
	
	

}
