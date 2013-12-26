package com.luxsoft.sw3.cfdi;

import java.text.MessageFormat;
import java.util.Date;
import java.util.List;

import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JPopupMenu;

import org.apache.commons.collections.ListUtils;
import org.apache.commons.lang.StringUtils;

import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.GlazedLists;

import com.luxsoft.cfdi.CFDIPrintUI;
import com.luxsoft.siipap.cxc.ui.selectores.SelectorDeClientes;
import com.luxsoft.siipap.model.Direccion;
import com.luxsoft.siipap.model.core.Cliente;
import com.luxsoft.siipap.service.ServiceLocator2;
//import com.luxsoft.siipap.pos.ui.forms.FacturaForm;
//import com.luxsoft.siipap.pos.ui.utils.ReportUtils2;
import com.luxsoft.siipap.swing.browser.FilteredBrowserPanel;
import com.luxsoft.siipap.swing.controls.Header;
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
				};
		return actions;
	}
	
	protected void beforeLoad(){
		super.beforeLoad();
		seleccionarCliente();
	}

	@Override
	protected List<CFDI> findData() {
		/*
		String hql="from CFDI c where date(c.log.creado) " +
				"between ? and ? " +
				" and c.origen not in(select cc.cargo.id from CancelacionDeCargo cc)";
		return ServiceLocator2.getHibernateTemplate().find(hql
				, new Object[]{periodo.getFechaInicial()
				,periodo.getFechaFinal()}
				);
		*/
		if(getCliente()==null)
			return ListUtils.EMPTY_LIST;
		String hql="from CFDI c where date(c.log.creado) " +
				"between ? and ? and c.rfc=? " +
				" and c.origen not in(select cc.cargo.id from CancelacionDeCargo cc)";
		return ServiceLocator2.getHibernateTemplate().find(hql
				, new Object[]{periodo.getFechaInicial()
				,periodo.getFechaFinal(),getCliente().getRfc()}
				);
	}
	
	
	
	
	@Override
	protected List<Action> createProccessActions(){
		List<Action> res=super.createProccessActions();
		res.add(addAction(null, "mandarPorCorreoElectronico", "Mandar por Correo"));
		return res;
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
	
	
	

}
