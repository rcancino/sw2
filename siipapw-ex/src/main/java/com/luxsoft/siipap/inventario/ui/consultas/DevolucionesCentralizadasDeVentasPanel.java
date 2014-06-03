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
import com.luxsoft.siipap.model.Sucursal;
import com.luxsoft.siipap.service.ServiceLocator2;
import com.luxsoft.siipap.swing.browser.AbstractMasterDatailFilteredBrowserPanel;
import com.luxsoft.siipap.swing.reports.ReportUtils;
import com.luxsoft.siipap.swing.utils.CommandUtils;
import com.luxsoft.siipap.ventas.model.Devolucion;
import com.luxsoft.siipap.ventas.model.DevolucionDeVenta;


public class DevolucionesCentralizadasDeVentasPanel extends AbstractMasterDatailFilteredBrowserPanel<Devolucion, DevolucionDeVenta>{

	
	
	public DevolucionesCentralizadasDeVentasPanel() {
		super(Devolucion.class);
		setTitle("Devoluciones de ventas");
	}
	
	protected void agregarMasterProperties(){
		addProperty("venta.sucursal.nombre","numero","fecha","venta.documento","venta.origen","venta.fecha","comentario");
		addLabels("Sucursal","Documento","Fecha","Venta","Tipo","Fecha (F)","Comentario");
		installTextComponentMatcherEditor("Sucursal", "venta.sucursal.nombre");
		installTextComponentMatcherEditor("Docto", "documento");		
		installTextComponentMatcherEditor("Tipo", "concepto");
		installTextComponentMatcherEditor("Comentario", "comentario");
		manejarPeriodo();
	}

	@Override
	protected TableFormat createDetailTableFormat() {
		String[] props={"sucursal.nombre","documento","fecha","clave","descripcion","ventaDet.cantidad","cantidad","costoPromedio","costoPromedioMovimiento","kilosCalculados"};
		String[] labels={"Sucursal","Docto","Fecha","Prod","Desc","Vendido","Devuelto","Costop","Costo","Kilos"};
		return GlazedLists.tableFormat(DevolucionDeVenta.class, props,labels);
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
	protected Model<Devolucion, DevolucionDeVenta> createPartidasModel() {
		return new CollectionList.Model<Devolucion, DevolucionDeVenta>(){
			public List<DevolucionDeVenta> getChildren(Devolucion parent) {
				String hql="from DevolucionDeVenta d left join fetch d.ventaDet vd where d.devolucion.id=?";
				return ServiceLocator2.getHibernateTemplate().find(hql,parent.getId());
			}
		};
	}

	@Override
	protected List<Devolucion> findData() {		
		String hql="from Devolucion d left join fetch d.venta v " +
				" where  d.fecha between ? and ?" ;
		return ServiceLocator2.getHibernateTemplate()
		.find(hql, new Object[]{periodo.getFechaInicial(),periodo.getFechaFinal()});
	}	

	public void imprimir(){
		Devolucion d=(Devolucion)getSelectedObject();
		if(d!=null){
			Sucursal suc=d.getVenta().getSucursal();
			final Map parameters=new HashMap();
			parameters.put("DEVOLUCION", d.getId());
			parameters.put("SUCURSAL", String.valueOf(suc.getId()));
			ReportUtils.viewReport(ReportUtils.toReportesPath("invent/Devoluciones.jasper"), parameters);
		}
	}
	

}
