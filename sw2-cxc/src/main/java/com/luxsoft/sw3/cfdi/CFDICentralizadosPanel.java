package com.luxsoft.sw3.cfdi;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JPopupMenu;

import org.apache.commons.collections.ListUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;

import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.GlazedLists;

import com.luxsoft.cfdi.CFDIPrintUI;
import com.luxsoft.siipap.cxc.CXCRoles;
import com.luxsoft.siipap.cxc.model.NotaDeCargo;
import com.luxsoft.siipap.cxc.model.NotaDeCredito;
import com.luxsoft.siipap.cxc.ui.CXCUIServiceFacade;
import com.luxsoft.siipap.cxc.ui.selectores.SelectorDeClientes;
import com.luxsoft.siipap.model.Direccion;
import com.luxsoft.siipap.model.core.Cliente;
import com.luxsoft.siipap.reports.CfdisNoEnviadosReport;
import com.luxsoft.siipap.service.ServiceLocator2;
//import com.luxsoft.siipap.pos.ui.forms.FacturaForm;
//import com.luxsoft.siipap.pos.ui.utils.ReportUtils2;
import com.luxsoft.siipap.swing.browser.FilteredBrowserPanel;
import com.luxsoft.siipap.swing.controls.Header;
import com.luxsoft.siipap.swing.utils.MessageUtils;
import com.luxsoft.siipap.swing.utils.SWExtUIManager;
import com.luxsoft.siipap.ventas.model.Venta;
import com.luxsoft.sw3.cfdi.model.CFDI;
//import com.luxsoft.sw3.services.Services;





public class CFDICentralizadosPanel extends FilteredBrowserPanel<CFDI>{
	
	private Cliente cliente;

	public CFDICentralizadosPanel() {
		super(CFDI.class);
		setTitle("Comprobantes fiscales digitales por Internet(CFDI)");		
	}
	
	public void init(){
		addProperty(
				"serie","tipo","folio","receptor"
				,"log.creado","total","estado","timbreFiscal.fechaTimbrado","timbreFiscal.UUID"
				,"rfc","comentario"
				);
		addLabels(
				"Serie","Tipo","Folio","Cliente","Fecha","Total","Estado","Timbrado","UUID","RFC","Comentario"
				);
		installTextComponentMatcherEditor("Serie", "serie");
		installTextComponentMatcherEditor("Folio", "folio");
		installTextComponentMatcherEditor("Cliente", "receptor");
		installTextComponentMatcherEditor("Tipo", "tipo");
		installTextComponentMatcherEditor("Total", "total");
		manejarPeriodo();
	}
	
	private Header header;
	
	@Override
	protected JComponent buildHeader() {
		header = new Header("Seleccione un cliente", "");
		return header.getHeader();
	}
	
	public void updateHeader(Cliente c) {
		
		if (c != null) {
			
			header.setTitulo(c.getNombreRazon()+ " ( "+c.getClave()+" )");
			if (c.getDireccionFiscal() != null) {
				String pattern = "" +
						"Calle  : {0}  #       {1} Int  : {7} 		Crédito{2} \n"+
						"Col    : {3} CP:      {4} \n" +
						"Del/Mpo: {5} Entidad: {6} 	Tel(s):{8}  {9}	";
				Direccion df = c.getDireccionFiscal();
				String msg = MessageFormat.format(pattern, 
						df.getCalle(), 
						df.getNumero(),
						c.isDeCredito() ? (c.getCredito().isSuspendido() ? "NO": "SI") : "NO",
						df.getColonia(),
						df.getCp(), 
						df.getMunicipio(),
						df.getEstado()
						,StringUtils.trimToEmpty(df.getNumeroInterior())
						,StringUtils.trimToEmpty(c.getTelefono1())
						,c.getTelefono2()
							);
				header.setDescripcion(msg);
			}

		} else {
			header.setTitulo("Seleccione un cliente");
			header.setDescripcion("");
		}
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
				,addAction("", "timbrar", "Timbrar")
				};
		return actions;
	}
	
	protected void beforeLoad(){
		super.beforeLoad();
		seleccionarCliente();
	}

	@Override
	protected List<CFDI> findData() {
	
		List<CFDI> comp = new ArrayList<CFDI>();
		
		if(getCliente()==null)
			return ListUtils.EMPTY_LIST;
		
		
		String sql="SELECT CARGO_ID FROM sx_ventas where fecha between ? and ? and CLIENTE_ID=? " +
				" and COMENTARIO2 is null"+
				" union "+
				" select abono_id from sx_cxc_abonos where fecha between ? and ? and CLIENTE_ID=? "+
				" AND tipo_id like 'NOTA%' AND TOTAL<>0"
				;
		List<Map> ids=ServiceLocator2.getJdbcTemplate().queryForList(sql, new Object[]{periodo.getFechaInicial()
				,periodo.getFechaFinal(),getCliente().getId(),periodo.getFechaInicial()
				,periodo.getFechaFinal(),getCliente().getId()});
		
		for(Map id:ids){
			String origen_id=(String) id.get("CARGO_ID");
			
			CFDI cfdi=ServiceLocator2.getCFDIManager().buscarPorOrigen(origen_id);
			
			if(cfdi !=null){
				comp.add(cfdi);
			}
		}
		return comp;
		
	}
	
	
	
	
	@Override
	protected List<Action> createProccessActions(){
		List<Action> res=super.createProccessActions();
		res.add(addAction(null, "mandarPorCorreoElectronico", "Mandar por Correo"));
		res.add(addAction("", "mantenimientoCorreo", "Mantenimiento de Correo"));
		res.add(addAction("", "xmlNoEnviados", "Xml no Enviados"));
		
		return res;
	}
	
	public void mantenimientoCorreo(){
		javax.swing.SwingUtilities.invokeLater(new Runnable() {

			public void run() {
				com.luxsoft.siipap.swing.utils.SWExtUIManager.setup();
			com.luxsoft.sw3.crm.catalogos.CRM_ClienteBrowser browser = new com.luxsoft.sw3.crm.catalogos.CRM_ClienteBrowser();
			browser.open();
				
			}

		});
	}
	
	
	public Cliente getCliente() {
		return cliente;
	}
	public void seleccionarCliente(){
		cliente=SelectorDeClientes.seleccionar();
		updateHeader(cliente);
	}

	@Override
	protected void doSelect(Object bean) {
		CFDI cfdi=(CFDI)bean;
		if(cfdi.getTipo().equals("FACTURA")){
			//FacturaForm.show(cfdi.getOrigen());
			Venta venta=ServiceLocator2.getFacturasManager().buscarVentaInicializada(cfdi.getOrigen());
			Date time=ServiceLocator2.obtenerFechaDelSistema();
			CFDIPrintUI.impripirComprobante(venta, cfdi, " ", time,ServiceLocator2.getHibernateTemplate(),true);
		}else if(cfdi.getTipo().equals("NOTA_CREDITO")){
			NotaDeCredito nota=CXCUIServiceFacade.buscarNotaDeCreditoInicializada(cfdi.getOrigen());
			Date time=ServiceLocator2.obtenerFechaDelSistema();
			CFDIPrintUI.impripirComprobante(nota, cfdi, "", time, true);
		}else if(cfdi.getTipo().equals("NOTA_CARGO")){
			NotaDeCargo nota=CXCUIServiceFacade.buscarNotaDeCargoInicializada(cfdi.getOrigen());
			Date time=ServiceLocator2.obtenerFechaDelSistema();
			CFDIPrintUI.impripirComprobante(nota, cfdi, "", time, true);
		}
		
	}

	
	public void mandarPorCorreoElectronico(){
		if(!getSelected().isEmpty()){
			if(!getSelected().isEmpty()){			
				EventList<CFDI> selected=GlazedLists.eventList(getSelected());
				CFDICorreoForm.mandarCorreo(cliente, selected);
			}
		}
		
	}
	
	
	public void xmlNoEnviados(){
		CfdisNoEnviadosReport.run();
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
