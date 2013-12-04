package com.luxsoft.siipap.inventario.ui.consultas;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.Action;
import javax.swing.JTextField;

import ca.odell.glazedlists.BasicEventList;
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
import com.luxsoft.siipap.maquila.model.EntradaDeMaquila;
import com.luxsoft.siipap.maquila.model.RecepcionDeMaquila;
import com.luxsoft.siipap.service.ServiceLocator2;
import com.luxsoft.siipap.swing.browser.AbstractMasterDatailFilteredBrowserPanel;
import com.luxsoft.siipap.swing.reports.ReportUtils;

/**
 * Panel para el mantenimiento de entradas de maquila
 * 
 * @author Ruben Cancino
 *
 */
public class RecepcionDeMaquilaCentralizadaPanel extends AbstractMasterDatailFilteredBrowserPanel<RecepcionDeMaquila, EntradaDeMaquila>{

	public RecepcionDeMaquilaCentralizadaPanel() {
		super(RecepcionDeMaquila.class);
		setTitle("Recepciones de maquila");
	}
	
	@Override
	protected void agregarMasterProperties() {
		addProperty("sucursal.nombre","fecha","documento","proveedor.nombre","comentario");
		addLabels("sucursal","fecha","Docto","Proveedor","Comentario");
		installTextComponentMatcherEditor("Sucursal", new String[]{"sucursal.nombre"});
		manejarPeriodo();
	}

	@Override
	protected TableFormat createDetailTableFormat() {
		String[] props={"sucursal.nombre","documento","clave","descripcion","cantidad","comentario"};
		String[] names={"Sucursal","Docto","Producto","Descripción","Cantidad","Comentario"};
		return GlazedLists.tableFormat(EntradaDeMaquila.class, props,names);
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
	protected Model<RecepcionDeMaquila, EntradaDeMaquila> createPartidasModel() {
		return new Model<RecepcionDeMaquila, EntradaDeMaquila>(){
			public List<EntradaDeMaquila> getChildren(RecepcionDeMaquila parent) {
				return ServiceLocator2.getHibernateTemplate().find("from EntradaDeMaquila e where e.recepcion.id=?",parent.getId());
			}
		};
	}
	
	@Override
	protected List<Action> createProccessActions() {
		List<Action> actions=new ArrayList<Action>();
		actions.add(addContextAction(new SelectionPredicate(), "", "imprimirDocumento", "Imprimir Dcto"));
		return actions;
	}

	@Override
	public Action[] getActions() {
		if(actions==null)
			actions=new Action[]{
				getLoadAction()};
		return actions;
	}
	
	@Override
	protected List<RecepcionDeMaquila> findData() {
		String hql="from RecepcionDeMaquila r where r.fecha between ? and ?";
		Object params[]={periodo.getFechaInicial(),periodo.getFechaFinal()};
		return ServiceLocator2.getHibernateTemplate().find(hql,params);
	}
	

	
	public void imprimirDocumento(){
		RecepcionDeMaquila rec=(RecepcionDeMaquila)getSelectedObject();
		print(rec);
	}
	
	public void print(RecepcionDeMaquila rec){		
		if(rec!=null){
			final Map parameters=new HashMap();
			parameters.put("ENTRADA", rec.getId());
			parameters.put("SUCURSAL", String.valueOf(rec.getSucursal().getId()));
			ReportUtils.viewReport(ReportUtils.toReportesPath("maquila/EntradaPorMaquila.jasper"), parameters);
		}
	}
	
	


}
