package com.luxsoft.siipap.inventario.ui.consultas;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.Action;
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

import com.luxsoft.siipap.model.Periodo;
import com.luxsoft.siipap.service.ServiceLocator2;
import com.luxsoft.siipap.swing.browser.AbstractMasterDatailFilteredBrowserPanel;
import com.luxsoft.siipap.swing.reports.ReportUtils;
import com.luxsoft.siipap.swing.utils.CommandUtils;
import com.luxsoft.siipap.ventas.model.Venta;
import com.luxsoft.siipap.ventas.model.VentaDet;


public class VentasCentralizadasDeVentasPanel extends AbstractMasterDatailFilteredBrowserPanel<Venta, VentaDet>{

	
	
	public VentasCentralizadasDeVentasPanel() {
		super(Venta.class);
		setTitle("Ventas");
	}
	
	protected void agregarMasterProperties(){
		addProperty("sucursal.nombre","fecha","documento","origen","clave","nombre","comentario");
		addLabels("Sucursal","Fecha","Venta","Tipo","Cliente","Nombre","Comentario");
		installTextComponentMatcherEditor("Sucursal", "sucursal.nombre");
		installTextComponentMatcherEditor("Docto", "documento");		
		installTextComponentMatcherEditor("Tipo", "origen");
		installTextComponentMatcherEditor("Comentario", "comentario");
		manejarPeriodo();
	}
	
	protected void manejarPeriodo(){
		periodo=Periodo.getPeriodo(-2);
	}

	@Override
	protected TableFormat createDetailTableFormat() {
		String[] props={"sucursal.nombre","documento","fecha","clave","descripcion","cantidad"};
		String[] labels={"Sucursal","Docto","Fecha","Prod","Desc","Cantidad"};
		return GlazedLists.tableFormat(VentaDet.class, props,labels);
	}
	
	private JTextField productoFilter=new JTextField(5);
	private JTextField sucursalFilter=new JTextField(5);
	private JTextField comentarioFilter=new JTextField(5);

	@Override
	protected void installDetailFilterComponents(DefaultFormBuilder builder) {		
		super.installDetailFilterComponents(builder);
		builder.append("Producto",productoFilter);
		builder.append("Sucursal",sucursalFilter);
		builder.append("Comentario",comentarioFilter);
		
	}
	
	@Override
	protected EventList decorateDetailList( EventList data){
		EventList<MatcherEditor> editors=new BasicEventList<MatcherEditor>();
		
		
		final TextFilterator prodFilterator=GlazedLists.textFilterator("clave","descripcion");
		final TextComponentMatcherEditor prodEditor=new TextComponentMatcherEditor(productoFilter,prodFilterator);
		editors.add(prodEditor);
		
		final TextFilterator sucFilterator=GlazedLists.textFilterator("sucursal.nombre");
		final TextComponentMatcherEditor sucEditor=new TextComponentMatcherEditor(sucursalFilter,sucFilterator);
		editors.add(sucEditor);
		
		final TextFilterator comentarioFilterator=GlazedLists.textFilterator("comentario");
		final TextComponentMatcherEditor comentarioEditor=new TextComponentMatcherEditor(comentarioFilter,comentarioFilterator);
		editors.add(comentarioEditor);
		
		
		final CompositeMatcherEditor matcherEditor=new CompositeMatcherEditor(editors);
		final FilterList detailFilter=new FilterList(data,matcherEditor);
		return detailFilter;
	}
	
	@Override
	public Action[] getActions() {
		if(actions==null)
			actions=new Action[]{
				getLoadAction()
				,CommandUtils.createPrintAction(this, "imprimir")
				
				};
		return actions;
	}

	@Override
	protected Model<Venta, VentaDet> createPartidasModel() {
		return new CollectionList.Model<Venta, VentaDet>(){
			public List<VentaDet> getChildren(Venta parent) {
				String hql="from VentaDet det  where det.venta.id=?";
				return ServiceLocator2.getHibernateTemplate().find(hql,parent.getId());
			}
		};
	}

	@Override
	protected List<Venta> findData() {		
		String hql="from Venta v  where  v.fecha between ? and ?" ;
		ServiceLocator2.getHibernateTemplate().setMaxResults(5000);
		return ServiceLocator2.getHibernateTemplate()
		.find(hql, new Object[]{periodo.getFechaInicial(),periodo.getFechaFinal()});
	}	
	
	protected void sureAfterLoad(){
		ServiceLocator2.getHibernateTemplate().setMaxResults(0);
	}

	public void imprimir(){
		Venta v=(Venta)getSelectedObject();
		if(v!=null){
			final Map parameters=new HashMap();
			parameters.put("CARGO_ID", v.getId());
			ReportUtils.viewReport(ReportUtils.toReportesPath("ventas/FacturaCopia.jasper"), parameters);
		}
	}
	

}
