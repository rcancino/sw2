package com.luxsoft.sw3.cxp.consultas;

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
import org.springframework.jdbc.core.BeanPropertyRowMapper;

import ca.odell.glazedlists.CollectionList.Model;
import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.gui.TableFormat;

import com.luxsoft.siipap.cxp.model.AnalisisDeFactura;
import com.luxsoft.siipap.cxp.model.AnalisisDeFacturaDet;
import com.luxsoft.siipap.cxp.model.AnticipoDeCompra;
import com.luxsoft.siipap.model.Periodo;
import com.luxsoft.siipap.reportes.ComsSinAnalizarReportForm;
import com.luxsoft.siipap.reports.DiarioDeEntradas;
import com.luxsoft.siipap.service.ServiceLocator2;
import com.luxsoft.siipap.swing.browser.AbstractMasterDatailFilteredBrowserPanel;
import com.luxsoft.siipap.swing.reports.ReportUtils;
import com.luxsoft.siipap.swing.utils.MessageUtils;
import com.luxsoft.siipap.swing.utils.TaskUtils;
import com.luxsoft.siipap.util.MonedasUtils;
import com.luxsoft.sw3.cxp.forms.FacturaDeComprasForm;
import com.luxsoft.sw3.cxp.forms.FacturaDeComprasFormModel;
import com.luxsoft.sw3.cxp.selectores.SelectorDeAnticipos;

/**
 * Panel para la administrcion de Analisis de Facturas de compras
 * @author Ruben Cancino
 *
 */
public class AnalisisDeFacturasDeComprasPanel extends AbstractMasterDatailFilteredBrowserPanel<AnalisisDeFacturaRow, AnalisisDeFacturaDet>{
	
	
	public AnalisisDeFacturasDeComprasPanel() {
		super(AnalisisDeFacturaRow.class);
	}

	@Override
	protected void agregarMasterProperties(){
		
		addProperty(
				"id"
				,"fecha"
				,"nombre"
				,"factura"
				,"fechaFactura"
				,"moneda"
				,"tc"
				,"totalFactura"
				,"totalAnalisis"
				,"bonificado"
				,"importeDescuentoFinanciero"
				,"pagos"
				,"saldo"
				,"porRequisitar"
				,"requisitado"
				,"comentario"
				);
		
		addLabels(
				"Análisis"
				,"Fecha"
				,"Proveedor"
				,"Factura"
				,"Fecha (Fac)"
				,"Moneda"
				,"TC"
				,"Total (Fac)"
				,"Analizado"
				,"Bonificado"
				,"D.F."
				,"Pagos"
				,"Saldo"
				,"Por Requisitar"
				,"Requisitado"
				,"Comentario"
				);
		installTextComponentMatcherEditor("Proveedor", "nombre");
		installTextComponentMatcherEditor("Factura", "factura");
		installTextComponentMatcherEditor("Analisis", "id");
		manejarPeriodo();		
	}
	
	protected void manejarPeriodo(){
		this.periodo=Periodo.getPeriodoDelMesActual();
	}

	@Override
	protected TableFormat createDetailTableFormat() {
		String[] props={
				"analisis.id"
				,"analisis.factura.clave"
				,"analisis.factura.documento"
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
				"Analisis"
				,"Proveedor"
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
		return GlazedLists.tableFormat(AnalisisDeFacturaDet.class, props,labels);
	}
	
	@Override
	public Action[] getActions() {
		if(actions==null)
			actions=new Action[]{
				getLoadAction()
				,getSecuredInsertAction("generarAnalisis")
				,getDeleteAction()
				,getEditAction()
				//,getViewAction()
				,addAction("reporteDeAnalisis", "printReport", "Imprimir")
				,addAction("diarioDeEntradas", "reporteDiarioDeEntradas", "Diario de entradas")
				,addAction("Entradas sin analizar", "reporteComSinAnalizar", "Entradas sin analizar")
				};
		return actions;
	}

	@Override
	protected Model<AnalisisDeFacturaRow, AnalisisDeFacturaDet> createPartidasModel() {
		Model<AnalisisDeFacturaRow, AnalisisDeFacturaDet> model=new Model<AnalisisDeFacturaRow, AnalisisDeFacturaDet>(){
			public List<AnalisisDeFacturaDet> getChildren(AnalisisDeFacturaRow parent) {
				try {
					String hql="from AnalisisDeFacturaDet a where a.analisis.id=?";
					return ServiceLocator2.getHibernateTemplate().find(hql,parent.getId());
				} catch (Exception e) {
					System.out.println(ExceptionUtils.getRootCauseMessage(e));
					return new ArrayList<AnalisisDeFacturaDet>();
				}
			}
		};
		return model;
	}

	

	@Override
	protected AnalisisDeFacturaRow doInsert() {
		AnalisisDeFactura a=FacturaDeComprasController.registrarFacura();
		if(a!=null)
			return new AnalisisDeFacturaRow(a);
		return null;
	}
	

	@Override
	protected void afterInsert(AnalisisDeFacturaRow bean) {
		super.afterInsert(bean);
		if(bean.getTotalFactura().doubleValue()<=0){
			if(MessageUtils.showConfirmationMessage("Analisis y factura de anticipo, desea asignar?", "Anticipos disponibles")){
				asignarAnticipo(bean);
			}
			
		}
		print(bean);
	}

	@Override
	protected AnalisisDeFacturaRow doEdit(AnalisisDeFacturaRow bean) {
		return FacturaDeComprasController.editarFactura(bean);
	}

	@Override
	public void select() {
		AnalisisDeFacturaRow selected=(AnalisisDeFacturaRow)getSelectedObject();
		if(selected!=null ){
			
			AnalisisDeFactura target=ServiceLocator2.getAnalisisDeCompraManager().get(selected.getId());
			int index=source.indexOf(selected);
			source.set(index, new AnalisisDeFacturaRow(target));
			final FacturaDeComprasFormModel model=new FacturaDeComprasFormModel(target);
			model.setReadOnly(true);
			final FacturaDeComprasForm form=new FacturaDeComprasForm(model);
			form.open();
		}
		
	}
	
	public void asignarAnticipo(AnalisisDeFacturaRow row){
		AnalisisDeFactura a=ServiceLocator2.getAnalisisDeCompraManager().get(row.getId());
		AnticipoDeCompra anticipo=SelectorDeAnticipos.buscarAnticipo(a.getFactura().getProveedor(), a.getFactura().getMoneda());
		if(anticipo!=null){
			ServiceLocator2.getAnticipoDeComprasManager().asignarFacturas(anticipo, a.getFactura());
		}
	}
	
	public void printReport(){
		if(getSelectedObject()!=null)
			print((AnalisisDeFacturaRow)getSelectedObject());
	}
	
	public void print(final AnalisisDeFacturaRow row){
		AnalisisDeFactura bean=ServiceLocator2.getAnalisisDeCompraManager().get(row.getId());
		if(confirmar("Desea imprimir Análisis?")){
			Map params=new HashMap();
			params.put("NUMERO", bean.getId());
			Currency moneda=bean.getFactura().getMoneda();
			if(!moneda.equals(MonedasUtils.PESOS)){
				if(confirmar("En moneda nacional"))
					moneda=MonedasUtils.PESOS;
			}
			params.put("MONEDA", moneda.getCurrencyCode());
			String path=ReportUtils.toReportesPath("cxp/AnalisisDeFacturaV2.jasper");
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
	
	
	@Override
	public boolean doDelete(AnalisisDeFacturaRow bean) {
		//AnalisisDeFacturaDeComprasController.eliminar(bean);
		FacturaDeComprasController.eliminarFactura(bean);
		return true;
	}	

	@Override
	protected void executeLoadWorker(SwingWorker worker) {
		TaskUtils.executeSwingWorker(worker);
	}
	
	@Override
	protected void adjustMainGrid(JXTable grid) {
		//grid.getColumn("TC").setCellRenderer(Renderers.buildDefaultNumberRenderer());
	}
	
	
	@Override
	protected List<AnalisisDeFacturaRow> findData() {
		String SQL="select b.ANALISIS_ID as id,b.fecha,a.nombre,a.documento as factura,a.fecha as fechaFactura" +
				",a.moneda,a.tc,a.total as totalFactura,b.importe*1.16 as totalAnalisis" +
				",(select IFNULL(sum(X.IMPORTE),0) FROM SX_CXP_APLICACIONES X where X.CARGO_ID=a.CXP_ID AND X.TIPO_ABONO='BONIFICACION') as bonificado " +
				"from sx_cxp a join sx_analisis b on (a.CXP_ID=b.CXP_ID)" +
				" where a.FECHA between ? and ? " +
				"order by b.ANALISIS_ID ";
		Object[] params={periodo.getFechaInicial(),periodo.getFechaFinal()};
		return ServiceLocator2.getJdbcTemplate().query(SQL, params, new BeanPropertyRowMapper(AnalisisDeFacturaRow.class));
	}
}
