package com.luxsoft.siipap.cxp.ui.consultas;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.Action;
import javax.swing.JOptionPane;
import javax.swing.JTextField;

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.CollectionList;
import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.FilterList;
import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.TextFilterator;
import ca.odell.glazedlists.CollectionList.Model;
import ca.odell.glazedlists.gui.TableFormat;
import ca.odell.glazedlists.matchers.CompositeMatcherEditor;
import ca.odell.glazedlists.matchers.MatcherEditor;
import ca.odell.glazedlists.swing.TextComponentMatcherEditor;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.luxsoft.siipap.cxp.CXPActions;
import com.luxsoft.siipap.cxp.model.ContraRecibo;
import com.luxsoft.siipap.cxp.model.ContraReciboDet;
import com.luxsoft.siipap.cxp.service.CXPServiceLocator;
import com.luxsoft.siipap.cxp.service.ContraReciboManager;
import com.luxsoft.siipap.cxp.ui.form.ContraRecibosForm;
import com.luxsoft.siipap.swing.browser.AbstractMasterDatailFilteredBrowserPanel;
import com.luxsoft.siipap.swing.reports.ReportUtils;

public class ContraRecibosPanel extends AbstractMasterDatailFilteredBrowserPanel<ContraRecibo, ContraReciboDet>{

	public ContraRecibosPanel() {
		super(ContraRecibo.class);
	}
	
	protected void agregarMasterProperties(){
		manejarPeriodo();
		addProperty("id","fecha","proveedor.nombre","comentario","total");
		addLabels("Folio","Fecha","Proveedor","Comentario","Total");
		installTextComponentMatcherEditor("Proveedor", "proveedor.nombre","proveedor.clave");
	}

	@Override
	protected TableFormat createDetailTableFormat() {
		String[] props={"recibo.id","recibo.proveedor.nombre","fecha","vencimiento","documento","tipo","moneda","tc","total","estado","analisis"};
		String[] names={"Recibo","Proveedor","Fecha","Vto","Docto","Tipo","Moneda","T.C","Total","Estado","Analisis"};
		return GlazedLists.tableFormat(ContraReciboDet.class,props, names);
	}
	
	@Override
	public Action[] getActions() {
		if(actions==null)
			actions=new Action[]{
				getLoadAction()
				,getSecuredInsertAction(CXPActions.MantenimientoDeContrarecibos.getId())
				,getSecuredDeleteAction(CXPActions.MantenimientoDeContrarecibos.getId())
				,getSecuredEditAction(CXPActions.MantenimientoDeContrarecibos.getId())
				,addAction("imprimir", "imprimir", "Imprimir")
				//,getViewAction()
				};
		return actions;
	}

	@Override
	protected Model<ContraRecibo, ContraReciboDet> createPartidasModel() {		
		return new CollectionList.Model<ContraRecibo, ContraReciboDet>(){
			public List<ContraReciboDet> getChildren(ContraRecibo parent) {
				return getManager().buscarPartidas(parent);
			}
		};
	}
	
	private JTextField documentField=new JTextField(5);
	
	protected void installDetailFilterComponents(DefaultFormBuilder builder){
		builder.appendSeparator("Detalle");
		builder.append("Documento",documentField);
	}
	
	@Override
	protected EventList decorateDetailList( EventList data){
		EventList<MatcherEditor> editors=new BasicEventList<MatcherEditor>();
		
		
		TextFilterator docFilterator=GlazedLists.textFilterator("documento");
		TextComponentMatcherEditor docEditor=new TextComponentMatcherEditor(documentField,docFilterator);
		editors.add(docEditor);
		
		CompositeMatcherEditor matcherEditor=new CompositeMatcherEditor(editors);
		FilterList detailFilter=new FilterList(data,matcherEditor);
		return detailFilter;
	}
	
	
	@Override
	public boolean doDelete(ContraRecibo bean) {
		getManager().remove(bean.getId());
		return true;
	}

	@Override
	protected ContraRecibo doInsert() {
		ContraRecibo res=ContraRecibosForm.showForm();
		if(res!=null)
			return res=getManager().save(res);
		return null;
	}

	@Override
	protected void afterInsert(ContraRecibo bean) {
		super.afterInsert(bean);
		if(confirmar("Desea imprimir el recibo?")){
			imprimir(bean);
		}
	}
	
	public void imprimir(){
		if(getSelectedObject()!=null)
			imprimir((ContraRecibo)getSelectedObject());
	}
	
	public void imprimir(ContraRecibo bean){
		Map params=new HashMap();
		params.put("ID", bean.getId());
		String path=ReportUtils.toReportesPath("cxp/ContraRecibo.jasper");
		if(ReportUtils.existe(path))
			ReportUtils.viewReport(path, params);
		else
			JOptionPane.showMessageDialog(this.getControl()
					,MessageFormat.format("El reporte:\n {0} no existe",path),"Reportes",JOptionPane.ERROR_MESSAGE);
	}

	@Override
	protected ContraRecibo doEdit(ContraRecibo bean) {
		ContraRecibo proxy=getManager().buscarInicializado(bean.getId());
		proxy=ContraRecibosForm.showForm(proxy);
		if(proxy!=null){
			return getManager().save(proxy);
		}else
			return null;
	}

	public List<ContraRecibo> findData(){
		return getManager().buscarRecibos(periodo);
	}
	
	private ContraReciboManager getManager(){
		return CXPServiceLocator.getInstance().getRecibosManager();
	}
	
	

}
