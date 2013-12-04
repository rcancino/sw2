package com.luxsoft.siipap.cxp.ui.consultas;

import java.math.BigDecimal;
import java.text.MessageFormat;
import java.text.NumberFormat;
import java.util.Currency;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.swing.Action;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.CollectionList;
import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.FilterList;
import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.TextFilterator;
import ca.odell.glazedlists.CollectionList.Model;
import ca.odell.glazedlists.event.ListEvent;
import ca.odell.glazedlists.event.ListEventListener;
import ca.odell.glazedlists.gui.TableFormat;
import ca.odell.glazedlists.matchers.CompositeMatcherEditor;
import ca.odell.glazedlists.matchers.MatcherEditor;
import ca.odell.glazedlists.swing.TextComponentMatcherEditor;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;
import com.luxsoft.siipap.cxp.model.CXPAnticipo;
import com.luxsoft.siipap.cxp.model.CXPPago;
import com.luxsoft.siipap.cxp.ui.CXPServices;
import com.luxsoft.siipap.cxp.ui.form.RequisicionForm;
import com.luxsoft.siipap.model.tesoreria.Requisicion;
import com.luxsoft.siipap.model.tesoreria.RequisicionDe;
import com.luxsoft.siipap.service.ServiceLocator2;
import com.luxsoft.siipap.swing.browser.AbstractMasterDatailFilteredBrowserPanel;
import com.luxsoft.siipap.swing.reports.ReportUtils;
import com.luxsoft.siipap.swing.utils.MessageUtils;
import com.luxsoft.siipap.util.MonedasUtils;

public class RequisicionesPanel extends  AbstractMasterDatailFilteredBrowserPanel<Requisicion,RequisicionDe>{

	
	public RequisicionesPanel() {
		super(Requisicion.class);
	} 

	@Override
	protected void agregarMasterProperties() {
		super.agregarMasterProperties();
		addProperty("id","afavor","fecha","concepto.clave","moneda","tipoDeCambio","total.amount","estado.name","pagoCxp.id","origen","comentario");
		addLabels("Id","Proveedor (Nom)","Fecha","Concepto","Moneda","TC","Total","estado","Pago (Apl)","Origen","Comentario");
		installTextComponentMatcherEditor("Id", "id");
		installTextComponentMatcherEditor("Proveedor", "afavor");		
		installTextComponentMatcherEditor("Moneda", "moneda");
		installTextComponentMatcherEditor("Estado", "estado");
		manejarPeriodo();
	}

	@Override
	protected TableFormat createDetailTableFormat() {
		String[] props={"requisicion.id","documento","fechaDocumento","total"
				,"facturaDeCompras.documento","facturaDeCompras.id"
				,"comentario"};
		String[] labels={"Requisicion","Factura","Fecha","Total","Factura","Analisis","Comentario"};
		return GlazedLists.tableFormat(RequisicionDe.class, props,labels);
	}

	@Override
	protected Model<Requisicion, RequisicionDe> createPartidasModel() {
		return new CollectionList.Model<Requisicion, RequisicionDe>(){
			public List<RequisicionDe> getChildren(Requisicion parent) {
				return buscarDetalle(parent);
			}
		};
	}
	
	private List<RequisicionDe> buscarDetalle(final Requisicion r){
		String hql="from RequisicionDe r " +
				" left join fetch r.facturaDeCompras fac" +
				" where r.requisicion.id=? ";
		return ServiceLocator2.getHibernateTemplate().find(hql, r.getId());
	}
	
	
	@Override
	protected List<Requisicion> findData() {
		return ServiceLocator2
		.getRequisiciionesManager()
		.buscarRequisicionesDeCompras(periodo);
	}

	@Override
	public Action[] getActions() {
		if(actions==null)
			actions=new Action[]{
				getLoadAction()
				,getInsertAction()
				,getDeleteAction()
				,getEditAction()
				,getViewAction()
				,addContextAction(new RequisicionPagable(), "aplicarRequisicionEnCXP", "aplicarPago", "Aplicar Pago")
				,addContextAction(new AnticipoPagable(), "aplicarAnticipoEnCXP", "aplicarAnticipo", "Aplicar Anticipo")
				,addAction("requisicionDeCompras", "print", "Imprimir")
				};
		return actions;
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
	
	public void print(){
		if(getSelectedObject()!=null)
			print((Requisicion)getSelectedObject());
	}
	
	public void print(Requisicion bean){
		Map params=new HashMap();
		params.put("ORIGEN", "COMPRAS");
		params.put("ID", bean.getId());
		Currency moneda=bean.getMoneda();
		if(!moneda.equals(MonedasUtils.PESOS)){
			if(confirmar("En moneda nacional"))
				moneda=MonedasUtils.PESOS;
		}
		params.put("MONEDA", moneda.getCurrencyCode());
		String path=ReportUtils.toReportesPath("tesoreria/Requisicion.jasper");
		if(ReportUtils.existe(path))
			ReportUtils.viewReport(path, params);
		else
			JOptionPane.showMessageDialog(this.getControl()
					,MessageFormat.format("El reporte:\n {0} no existe",path),"Reportes",JOptionPane.ERROR_MESSAGE);
	}

	@Override
	protected Requisicion doInsert() {
		return CXPServices.generarRequisicion(periodo);
	}
	
	@Override
	public boolean doDelete(Requisicion bean) {
		CXPServices.eliminarRequisicion(bean);
		return true;
		
	}

	@Override
	protected void afterInsert(Requisicion bean) {
		super.afterInsert(bean);
		if(confirmar("Desea imprimir la requisición: "+bean.getId()))
			print(bean);
		
	}
	
	@Override
	protected Requisicion doEdit(Requisicion bean) {
		return CXPServices.modificarRequisicion(bean.getId());
	}

	@Override
	protected void doSelect(Object bean) {
		Requisicion proxy=(Requisicion)bean;
		proxy=ServiceLocator2.getRequisiciionesManager().buscarRequisicionDeCompras(proxy.getId());
		RequisicionForm.showForm(proxy, true);
	}

	public void aplicarPago(){
		if(getSelectedObject()!=null){
			Requisicion selected=(Requisicion)getSelectedObject();
			
			CXPPago pago=CXPServices.aplicarPago(selected);
			if(pago!=null){
				String pattern="Pago generado:\n "+pago.toString();
				MessageUtils.showMessage(MessageFormat.format(pattern,pago.getId()), "Pago automático");
				Requisicion fresh=ServiceLocator2.getRequisiciionesManager().get(selected.getId());
				int index=source.indexOf(selected);
				source.set(index, fresh);
				selectionModel.clearSelection();
				
			}
		}
	}
	
	public void aplicarAnticipo(){
		if(getSelectedObject()!=null){
			Requisicion selected=(Requisicion)getSelectedObject();
			
			CXPAnticipo anticipo=CXPServices.aplicarAnticipo(selected);
			if(anticipo!=null){
				String pattern="Anticipo generado:\n "+anticipo.toString();
				MessageUtils.showMessage(MessageFormat.format(pattern,anticipo.getId()), "Anticipo automático");
				Requisicion fresh=ServiceLocator2.getRequisiciionesManager().get(selected.getId());
				int index=source.indexOf(selected);
				source.set(index, fresh);
				selectionModel.clearSelection();
				
			}
		}
	}

	private JPanel totalPanel;
	private JLabel granTotal=new JLabel();
	private JLabel totalPendiente=new JLabel();
	private JLabel totalPagado=new JLabel();
	
	private NumberFormat nf=NumberFormat.getCurrencyInstance(Locale.US);
	
	@SuppressWarnings("unchecked")
	public JPanel getTotalesPanel(){
		if(totalPanel==null){
			final FormLayout layout=new FormLayout("p,4dlu,f:max(100dlu;p):g","");
			DefaultFormBuilder builder=new DefaultFormBuilder(layout);
			granTotal.setHorizontalAlignment(SwingConstants.LEFT);
			totalPendiente.setHorizontalAlignment(SwingConstants.LEFT);
			totalPagado.setHorizontalAlignment(SwingConstants.LEFT);
			builder.append("Pendiente:",totalPendiente);
			builder.append("Pagado:",totalPagado);
			builder.append("Total:",granTotal);
			totalPanel=builder.getPanel();
			totalPanel.setOpaque(false);
			getFilteredSource().addListEventListener(new TotalesHandler());
			
		}
		return totalPanel;
	}
	
	private class TotalesHandler implements ListEventListener{
		public void listChanged(ListEvent listChanges) {
			if(listChanges.next()){
				updateTotales();
			}
		}
		
		private void updateTotales(){
			BigDecimal tot=BigDecimal.ZERO;
			BigDecimal pendiente=BigDecimal.ZERO;
			BigDecimal pagado=BigDecimal.ZERO;
			for(Object  r:getFilteredSource()){
				Requisicion c=(Requisicion)r;
				tot=tot.add(c.getTotal().multiply(c.getTipoDeCambio()).amount());
				if(c.getPagoCxp()!=null)
					pagado=pagado.add(c.getTotal().getAmount());
				else
					pendiente=pendiente.add(c.getTotal().getAmount());
			}
			granTotal.setText(nf.format(tot.doubleValue()));
			totalPagado.setText(nf.format(pagado.doubleValue()));
			totalPendiente.setText(nf.format(pendiente.doubleValue()));
			
		}
		
	}
	
	private class RequisicionPagable implements Predicate{

		public boolean evaluate(Object bean) {
			Requisicion selected=(Requisicion)bean;
			if(selected==null) return false;
			//return selected.getPagoCxp()==null;
			return ((selected.getPagoCxp()==null)
					&&(selected.getConcepto()!=null)
					&&!(selected.getConcepto().getClave().equals("ANTICIPO"))
					);
		}
		
	}
	
	private class AnticipoPagable implements Predicate{

		public boolean evaluate(Object bean) {
			Requisicion selected=(Requisicion)bean;
			if(selected==null) return false;
			return ((selected.getPagoCxp()==null)
					&&(selected.getConcepto()!=null)
					&&(selected.getConcepto().getClave().equals("ANTICIPO"))
					);
		}
		
	}

}
