package com.luxsoft.siipap.inventario.ui.consultas;


import java.util.ArrayList;
import java.util.Date;
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
import com.luxsoft.siipap.compras.model.DevolucionDeCompra;
import com.luxsoft.siipap.compras.model.DevolucionDeCompraDet;
import com.luxsoft.siipap.model.Periodo;
import com.luxsoft.siipap.service.ServiceLocator2;
import com.luxsoft.siipap.swing.browser.AbstractMasterDatailFilteredBrowserPanel;
import com.luxsoft.siipap.swing.reports.ReportUtils;
import com.luxsoft.siipap.swing.utils.CommandUtils;

/**
 * Panel para la atención y mantenimiento de devoluciones de compras
 * 
 * @author Ruben Cancino
 *
 */
public class DevolucionesCentralizadasDeComprasPanel extends AbstractMasterDatailFilteredBrowserPanel<DevolucionDeCompra, DevolucionDeCompraDet>{
	
	

	public DevolucionesCentralizadasDeComprasPanel() {
		super(DevolucionDeCompra.class);
		setTitle("Devoluciones de Compras");
		
	}
	
	protected void init(){		
		super.init();
		addProperty("sucursal.nombre","documento","fecha","nombre","referencia","comentario");
		addLabels("Sucursal","Docto","Fecha","Proveedor","Referencia","Comentario");
		installTextComponentMatcherEditor("Documento", "documento");
		installTextComponentMatcherEditor("Proveedor", "nombre");
		manejarPeriodo();
		periodo=Periodo.getPeriodoDelYear(Periodo.obtenerYear(new Date()));
	}

	@Override
	protected TableFormat createDetailTableFormat() {
		String[] props={"devolucion.documento","clave","descripcion","cantidad","costoPromedio","costoPromedioMovimiento","kilosCalculados"};
		String[] labels={"Documento","Producto","Descripción","cantidad","Costop","Costo","Kilos"};
		return GlazedLists.tableFormat(DevolucionDeCompraDet.class, props,labels);
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
	protected Model<DevolucionDeCompra, DevolucionDeCompraDet> createPartidasModel() {
		return new CollectionList.Model<DevolucionDeCompra, DevolucionDeCompraDet>(){
			public List<DevolucionDeCompraDet> getChildren(DevolucionDeCompra parent) {
				return new ArrayList<DevolucionDeCompraDet>(parent.getPartidas());
			}
		};
	}
	
	

	@Override
	public Action[] getActions() {
		if(actions==null)
			actions=new Action[]{
				getLoadAction()
				,CommandUtils.createPrintAction(this, "print")
				};
		return actions;
	}	

	@Override
	protected List<DevolucionDeCompra> findData() {
		String hql="from DevolucionDeCompra s where s.fecha between ? and ? ";
		return ServiceLocator2.getHibernateTemplate().find(hql
				,new Object[]{
				periodo.getFechaInicial()
				,periodo.getFechaFinal()
				});
	}
	
	
	public void print(){
		for(Object o:getSelected()){
			DevolucionDeCompra dec=(DevolucionDeCompra)o;
			print(dec);	
		}
	}
	
	public void print(DevolucionDeCompra dec){
		Map params=new HashMap();
		params.put("DEVOLUCION_ID", dec.getId());
		ReportUtils.viewReport(ReportUtils.toReportesPath("invent/DevolucionDeCompra.jasper"), params);
	}

}
