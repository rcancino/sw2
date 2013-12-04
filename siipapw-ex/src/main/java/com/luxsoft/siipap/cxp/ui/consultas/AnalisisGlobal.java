package com.luxsoft.siipap.cxp.ui.consultas;

import java.util.List;

import javax.swing.JComponent;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.SwingWorker;

import ca.odell.glazedlists.CollectionList;
import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.swing.EventSelectionModel;

import com.luxsoft.siipap.compras.model.EntradaPorCompra;
import com.luxsoft.siipap.cxp.model.CXPAnalisisDet;
import com.luxsoft.siipap.cxp.model.CXPAplicacion;
import com.luxsoft.siipap.cxp.service.CXPServiceLocator;
import com.luxsoft.siipap.cxp.ui.reportes.EntradasPorAbono;
import com.luxsoft.siipap.service.ServiceLocator2;
import com.luxsoft.siipap.swing.browser.FilteredBrowserPanel;
import com.luxsoft.siipap.swing.utils.CommandUtils;
import com.luxsoft.siipap.swing.utils.TaskUtils;

public class AnalisisGlobal extends FilteredBrowserPanel<EntradaPorCompra>{

	public AnalisisGlobal() {
		super(EntradaPorCompra.class);
	}
	
	protected void init(){
		addProperty("sucursal.nombre","proveedor.nombreRazon","compra","fechaCompra","remision","documento","fecha","clave","descripcion","unidad.unidad","producto.kilos","cantidad","kilosEntrada","analizado","kilosAnalizados");
		addLabels("Suc","Proveedor","Compra","Fecha (C)","Rem","Entrada","Fecha","Producto","Descripcion","Uni","Kgr","Recibido","Kg Rec","Analizado","Analizado (Kg)");
		
		installTextComponentMatcherEditor("Proveedor", "proveedor.nombreRazon");
		installTextComponentMatcherEditor("Producto", "clave","descripcion");
		installTextComponentMatcherEditor("Compra", "compra");
		installTextComponentMatcherEditor("Entrada", "documento");
		installTextComponentMatcherEditor("Sucursal", "sucursal.nombre");
		manejarPeriodo();
		addActions(
				getLoadAction()
				,CommandUtils.createPrintAction(this, "imprimir")
				);
	}

	@Override
	protected JComponent buildContent() {
		JComponent parent=super.buildContent();
		JSplitPane sp=new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		sp.setOneTouchExpandable(true);
		sp.setResizeWeight(1);
		sp.setTopComponent(parent);
		sp.setBottomComponent(buildDetailPanel());
		return sp;
	}
	
	
	
	@Override
	protected List<EntradaPorCompra> findData() {
		logger.info("Buscando compras para el preiodo: "+periodo);
		
		List<EntradaPorCompra> data= CXPServiceLocator.getInstance().getEntradaPorCompraDao().buscarEntradas(periodo);
		logger.info("Entradas encontradas: "+data.size());
		return data;
	}

	@Override
	protected void executeLoadWorker(SwingWorker worker) {		
		TaskUtils.executeSwingWorker(worker);
	}

	private JComponent buildDetailPanel(){
		JTabbedPane tp=new JTabbedPane();
		tp.addTab("Análisis", buildAnalisisPanel());
		tp.addTab("Abonos", buildAbonosPanel());
		return tp;
	}
	
	private AnalisisDeEntradas analisisPanel;

	private JComponent buildAnalisisPanel(){
		analisisPanel=new AnalisisDeEntradas();
		return analisisPanel.getControl();
	}
	
	private AbonosPanel abonosPanel;
	
	private JComponent buildAbonosPanel(){
		abonosPanel=new AbonosPanel();
		return abonosPanel.getControl();
	}
	
	public void imprimir(){
		final EntradasPorAbono action=new EntradasPorAbono();
		action.execute();
	}
	
	private class AnalisisDeEntradas extends FilteredBrowserPanel<CXPAnalisisDet>{ 

		public AnalisisDeEntradas() {
			super(CXPAnalisisDet.class);
			
		}
		
		public EventSelectionModel getSelectionModel(){
			return selectionModel;
		}
		
		protected void init(){
			String[] props={
					"factura.id"
					,"factura.documento"
					,"entrada.remision"
					,"entrada.fechaRemision"
					,"entrada.documento"
					,"cantidad"
					,"precio"
					,"costo"
					,"Importe"};
			String[] labels={
					"CXP_ID"
					,"Factura"
					,"Remisión"
					,"F.Remisión"
					,"Entrada"
					,"Analizado"
					,"Precio"
					,"CostoU"
					,"Importe"};
			addProperty(props);
			addLabels(labels);
		}

		@Override
		protected EventList getSourceEventList() {			
			CollectionList.Model<EntradaPorCompra,CXPAnalisisDet> model=new CollectionList.Model<EntradaPorCompra, CXPAnalisisDet>(){
				public List<CXPAnalisisDet> getChildren(EntradaPorCompra parent) {
					 return buscarAnalisis(parent.getId());
				}				
			};
			CollectionList<EntradaPorCompra, CXPAnalisisDet> colList=new CollectionList<EntradaPorCompra, CXPAnalisisDet>(AnalisisGlobal.this.selectionModel.getSelected(),model);
			return colList;
		}
		
		public List<CXPAnalisisDet> buscarAnalisis(final String entradaId){
			String hql="from CXPAnalisisDet a where a.entrada.id=?";
			return ServiceLocator2.getHibernateTemplate().find(hql,entradaId);
		}
		
	}
	
	private class AbonosPanel extends FilteredBrowserPanel<CXPAplicacion>{

		public AbonosPanel() {
			super(CXPAplicacion.class);
		}
		
		protected void init(){
			addProperty("cargo.id","cargo.documento","fecha","importe","tipoAbono");
			addLabels("CXP_ID","Factura","Fecha","Aplicado","Tipo");
		}
		
		@Override
		protected EventList getSourceEventList() {
			CollectionList.Model<CXPAnalisisDet,CXPAplicacion> model=new CollectionList.Model<CXPAnalisisDet, CXPAplicacion>(){

				public List<CXPAplicacion> getChildren(CXPAnalisisDet parent) {
					return buscarAplicaciones(parent.getFactura().getId());
				}
				
			};
			CollectionList<CXPAnalisisDet,CXPAplicacion> colList=new CollectionList<CXPAnalisisDet,CXPAplicacion>(AnalisisGlobal.this.analisisPanel.getSelectionModel().getSelected(),model);
			return colList;		
		}
		
		public List<CXPAplicacion> buscarAplicaciones(final Long cargoId){
			String hql="from CXPAplicacion a where a.cargo.id=?";
			return ServiceLocator2.getHibernateTemplate().find(hql,cargoId);
		}
		
	}

}
