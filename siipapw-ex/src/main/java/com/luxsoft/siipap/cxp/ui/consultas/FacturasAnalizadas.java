package com.luxsoft.siipap.cxp.ui.consultas;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Currency;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.Action;
import javax.swing.JOptionPane;
import javax.swing.SwingWorker;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.jdesktop.swingx.JXTable;

import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.CollectionList.Model;
import ca.odell.glazedlists.gui.TableFormat;

import com.luxsoft.siipap.cxp.model.CXPAnalisisDet;
import com.luxsoft.siipap.cxp.model.CXPFactura;
import com.luxsoft.siipap.cxp.service.CXPServiceLocator;
import com.luxsoft.siipap.cxp.service.FacturaManager;
import com.luxsoft.siipap.cxp.ui.CXPServices;
import com.luxsoft.siipap.model.Periodo;
import com.luxsoft.siipap.reportes.ComsSinAnalizarReportForm;
import com.luxsoft.siipap.reports.DiarioDeEntradas;
import com.luxsoft.siipap.swing.browser.AbstractMasterDatailFilteredBrowserPanel;
import com.luxsoft.siipap.swing.reports.ReportUtils;
import com.luxsoft.siipap.swing.utils.TaskUtils;
import com.luxsoft.siipap.util.MonedasUtils;

public class FacturasAnalizadas extends AbstractMasterDatailFilteredBrowserPanel<CXPFactura, CXPAnalisisDet>{
	
	
	public FacturasAnalizadas() {
		super(CXPFactura.class);
	}

	@Override
	protected void agregarMasterProperties(){
		addProperty("id","tipoDeFactura","fecha","clave","nombre","documento","fecha","vencimiento","moneda","tc","total","totalAnalisis","bonificado","importeDescuentoFinanciero","pagos","saldoCalculado","porRequisitar","requisitado","comentario");
		addLabels("CXP_ID","Tipo","Fecha","Prov","Nombre","Docto","F Docto","Vto","Mon","TC","Facturado","Analizado","Bonificado","D.F.","Pagos","Saldo","Por Requisitar","Requisitado","Comentario");
		installTextComponentMatcherEditor("Proveedor", "nombre");
		installTextComponentMatcherEditor("Factura", "documento");
		installTextComponentMatcherEditor("Analisis", "id");
		manejarPeriodo();		
	}
	
	protected void manejarPeriodo(){
		this.periodo=Periodo.getPeriodoDelMesActual();
	}

	@Override
	protected TableFormat createDetailTableFormat() {
		String[] props={
				"factura.id"
				,"factura.documento"
				,"entrada.remision"
				,"entrada.fechaRemision"
				,"entrada.fecha"
				,"entrada.documento"
				,"entrada.clave"
				,"entrada.descripcion"
				,"entrada.cantidad"
				,"cantidad"
				,"precio"
				,"costo"
				,"Importe"
				};
		String[] labels={
				"CXP_ID"
				,"Factura"
				,"Remisión"
				,"F.Remisión"
				,"Entrada"
				,"COM"
				,"Producto"
				,"Descripción"
				,"Recibido"
				,"Analizado"
				,"Precio"
				,"CostoU"
				,"Importe"
				};
		return GlazedLists.tableFormat(CXPAnalisisDet.class, props,labels);
	}
	
	@Override
	public Action[] getActions() {
		if(actions==null)
			actions=new Action[]{
				getLoadAction()
				,getSecuredInsertAction("generarAnalisis")
				,addAction("generarAnalisis", "generarAnalisisConRecibo", "Nuevo (Contra recibo)")
				,getDeleteAction()
				,getEditAction()
				//,getViewAction()
				,addAction("actualizarSeleccion", "actualizarSeleccion", "Actualizar (sel)")
				,addAction("reporteDeAnalisis", "printReport", "Imprimir")
				,addAction("diarioDeEntradas", "reporteDiarioDeEntradas", "Diario de entradas")
				,addAction("Entradas sin analizar", "reporteComSinAnalizar", "Entradas sin analizar")
				
				};
		return actions;
	}

	@Override
	protected Model<CXPFactura, CXPAnalisisDet> createPartidasModel() {
		Model<CXPFactura, CXPAnalisisDet> model=new Model<CXPFactura,CXPAnalisisDet>(){
			public List<CXPAnalisisDet> getChildren(CXPFactura parent) {
				try {
					return FacturasAnalizadas.this.getManager().buscarAnalisis(parent);
				} catch (Exception e) {
					System.out.println(ExceptionUtils.getRootCauseMessage(e));
					return new ArrayList<CXPAnalisisDet>();
				}
				
			}
		};
		return model;
	}

	@Override
	protected List<CXPFactura> findData() {
		return getManager().buscarFacturas(this.periodo);
	}
	
	public void actualizarSeleccion(){
		if(!getSelected().isEmpty()){
			for (Object row:getSelected()){
				CXPFactura selected=(CXPFactura)row;
				int index=source.indexOf(selected);
				if(index>=0){
					CXPFactura fac=getManager().get(selected.getId());
					source.set(index, fac);
				}
			}
			
		}
	}

	@Override
	protected CXPFactura doInsert() {
		CXPFactura target=CXPServices.generarAnalisis();
		return target;
	}
	
	@Override
	protected void afterInsert(CXPFactura bean) {
		super.afterInsert(bean);
		print(bean);
	}

	@Override
	protected CXPFactura doEdit(CXPFactura bean) {
		if(bean.getAnalizadoComoFlete().doubleValue()>0)
			return null;
		return CXPServices.editarAnalisis(bean.getId());
	}

	public void printReport(){
		if(getSelectedObject()!=null)
			print((CXPFactura)getSelectedObject());
	}
	
	public void print(final CXPFactura bean){
		if(confirmar("Desea imprimir Análisis?")){
			Map params=new HashMap();
			params.put("NUMERO", bean.getId());
			Currency moneda=bean.getMoneda();
			if(!moneda.equals(MonedasUtils.PESOS)){
				if(confirmar("En moneda nacional"))
					moneda=MonedasUtils.PESOS;
			}
			params.put("MONEDA", moneda.getCurrencyCode());
			String path=ReportUtils.toReportesPath("cxp/AnalisisDeFactura.jasper");
			if(ReportUtils.existe(path))
				ReportUtils.viewReport(path, params);
			else
				JOptionPane.showMessageDialog(this.getControl()
						,MessageFormat.format("El reporte:\n {0} no existe",path),"Reportes",JOptionPane.ERROR_MESSAGE);
		}
	}

	
	public void reporteDiarioDeEntradas(){
		DiarioDeEntradas d=new DiarioDeEntradas();
		d.actionPerformed(null);
	}
	
	public void reporteComSinAnalizar(){
		ComsSinAnalizarReportForm.run();
	}
	
	public void generarAnalisisConRecibo(){
		CXPFactura target=CXPServices.generarAnalisisDesdeRecibo();
		if(target!=null){
			source.add(target);
			afterInsert(target);			
		}
	}
	
	 
	
	@Override
	protected void executeLoadWorker(SwingWorker worker) {
		TaskUtils.executeSwingWorker(worker);
	}

	@Override
	public boolean doDelete(CXPFactura bean) {
		if(bean.getAnalizadoComoFlete().doubleValue()>0){
			JOptionPane.showMessageDialog(getControl(), "Factura de flete, no se puede eliminar en esta consulta");
			return false;
		}
		
		getManager().remove(bean.getId());
		
		return true;
	}

	private FacturaManager getManager(){
		return CXPServiceLocator.getInstance().getFacturasManager();
	}

	@Override
	protected void adjustMainGrid(JXTable grid) {
		//grid.getColumn("TC").setCellRenderer(Renderers.buildDefaultNumberRenderer());
	}
	
	
	

}
