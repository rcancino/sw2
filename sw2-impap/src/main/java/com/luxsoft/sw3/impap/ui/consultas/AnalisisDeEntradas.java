package com.luxsoft.sw3.impap.ui.consultas;

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
import com.luxsoft.siipap.model.Periodo;
import com.luxsoft.siipap.reports.DiarioDeEntradas;
import com.luxsoft.siipap.swing.browser.AbstractMasterDatailFilteredBrowserPanel;
import com.luxsoft.siipap.swing.reports.ReportUtils;
import com.luxsoft.siipap.swing.utils.MessageUtils;
import com.luxsoft.siipap.swing.utils.TaskUtils;
import com.luxsoft.siipap.util.MonedasUtils;
import com.luxsoft.sw3.impap.ui.form.AnalisisDeEntradaForm;
import com.luxsoft.sw3.impap.ui.form.AnalisisDeEntradaModel;

public class AnalisisDeEntradas extends AbstractMasterDatailFilteredBrowserPanel<CXPFactura, CXPAnalisisDet>{
	
	
	public AnalisisDeEntradas() {
		super(CXPFactura.class);
	}

	@Override
	protected void agregarMasterProperties(){
		addProperty("id","fecha","clave","nombre","documento","fecha","vencimiento","moneda","tc","total","totalAnalisis","bonificado","importeDescuentoFinanciero","pagos","saldoCalculado","porRequisitar","requisitado","comentario");
		addLabels("CXP_ID","Fecha","Prov","Nombre","Docto","F Docto","Vto","Mon","TC","Facturado","Analizado","Bonificado","D.F.","Pagos","Saldo","Por Requisitar","Requisitado","Comentario");
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
				};
		return actions;
	}

	@Override
	protected Model<CXPFactura, CXPAnalisisDet> createPartidasModel() {
		Model<CXPFactura, CXPAnalisisDet> model=new Model<CXPFactura,CXPAnalisisDet>(){
			public List<CXPAnalisisDet> getChildren(CXPFactura parent) {
				try {
					return AnalisisDeEntradas.this.getManager().buscarAnalisis(parent);
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
		CXPFactura factura=new CXPFactura();
		AnalisisDeEntradaModel model=new AnalisisDeEntradaModel(factura);		
		AnalisisDeEntradaForm form=new AnalisisDeEntradaForm(model);
		form.open();
		if(!form.hasBeenCanceled()){
			CXPFactura target=model.getAnalisis();
			target=CXPServiceLocator.getInstance().getFacturasManager().save(target);
			return CXPServiceLocator.getInstance().getFacturasManager().get(target.getId());
		}
		return null;
	}
	
	@Override
	protected void afterInsert(CXPFactura bean) {
		super.afterInsert(bean);
		print(bean);
	}

	@Override
	protected CXPFactura doEdit(CXPFactura bean) {
		CXPFactura analisis=CXPServiceLocator.getInstance().getFacturasManager().get(bean.getId());
		if(analisis.getPagos().doubleValue()!=0){
			MessageUtils.showMessage("El analisis ya tiene pagos aplicados no se puede modificar", "Modificacion de análisis");
			return null;
		}
		if(analisis.getRequisitado().doubleValue()!=0){
			MessageUtils.showMessage("El analisis ya esta requisitado no se puede modificar", "Modificacion de análisis");
			return null;
		}
		AnalisisDeEntradaModel model=new AnalisisDeEntradaModel(analisis);
		AnalisisDeEntradaForm form=new AnalisisDeEntradaForm(model);
		form.open();
		if(!form.hasBeenCanceled()){
			CXPFactura target=model.getAnalisis();
			target=CXPServiceLocator.getInstance().getFacturasManager().save(target);
			return target;
		}
		return null;
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
	
	
	
	 
	
	@Override
	protected void executeLoadWorker(SwingWorker worker) {
		TaskUtils.executeSwingWorker(worker);
	}

	@Override
	public boolean doDelete(CXPFactura bean) {
		
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
