package com.luxsoft.siipap.inventario.ui.consultas;


import java.util.ArrayList;
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
import com.luxsoft.siipap.inventarios.model.SolicitudDeTraslado;
import com.luxsoft.siipap.inventarios.model.SolicitudDeTrasladoDet;
import com.luxsoft.siipap.reportes.SolicitudesPendientesReportForm;
import com.luxsoft.siipap.service.ServiceLocator2;
import com.luxsoft.siipap.swing.browser.AbstractMasterDatailFilteredBrowserPanel;
import com.luxsoft.siipap.swing.reports.ReportUtils;
import com.luxsoft.siipap.swing.utils.CommandUtils;


/**
 * Panel para el mantenimiento de traslados
 * 
 * @author Ruben Cancino
 *
 */
public class SolicitudDeTrasladosCentralizadosPanel extends AbstractMasterDatailFilteredBrowserPanel<SolicitudDeTraslado, SolicitudDeTrasladoDet>{

	public SolicitudDeTrasladosCentralizadosPanel() {
		super(SolicitudDeTraslado.class);
		setTitle("Solicitudes de traslados");
		
	}
	
	protected void init(){		
		super.init();
		addProperty("sucursal.nombre","documento","fecha","origen.nombre","atendido","comentario","referencia");
		addLabels("Sucursal","Docto","Fecha","Suc Origen","Atendido","Comentario","Referencia");
		
		installTextComponentMatcherEditor("Suc Solicita", new String[]{"sucursal.nombre"});
		installTextComponentMatcherEditor("Origen", new String[]{"origen.nombre"});
		installTextComponentMatcherEditor("Documento", new String[]{"documento"});
		installTextComponentMatcherEditor("Atendido", new String[]{"atendido"});
		installTextComponentMatcherEditor("Referencia", new String[]{"referencia"});
		setDefaultComparator(GlazedLists.beanPropertyComparator(SolicitudDeTraslado.class, "documento"));
		manejarPeriodo();
	}

	@Override
	protected TableFormat createDetailTableFormat() {
		String[] props={"solicitud.sucursal.nombre","solicitud.origen.nombre","solicitud.documento","producto.clave","producto.descripcion","solicitado","recibido","comentario"};
		String[] labels={"Sucursal","Suc Origen","Sol","Producto","Descripción","Solicitado","Recibido","Comentario"};
		return GlazedLists.tableFormat(SolicitudDeTrasladoDet.class, props,labels);
	}

	@Override
	protected Model<SolicitudDeTraslado, SolicitudDeTrasladoDet> createPartidasModel() {
		return new CollectionList.Model<SolicitudDeTraslado, SolicitudDeTrasladoDet>(){
			public List<SolicitudDeTrasladoDet> getChildren(SolicitudDeTraslado parent) {
				//String hql="select s.from SolicitudDeTrasladoDet det where det.solicitud.id=?";
				//Services.getInstance().getHibernateTemplate().initialize(parent.getPartidas());
				return parent.getPartidas();
			}
		};
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
		
		
		final TextFilterator prodFilterator=GlazedLists.textFilterator("producto.clave","producto.descripcion");
		final TextComponentMatcherEditor prodEditor=new TextComponentMatcherEditor(productoFilter,prodFilterator);
		editors.add(prodEditor);
		
		final TextFilterator sucFilterator=GlazedLists.textFilterator("solicitud.sucursal.nombre");
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
				//,getInsertAction()
				//,getDeleteAction()
				//,getEditAction()
				,getViewAction()
				,CommandUtils.createPrintAction(this, "print")
				};
		return actions;
	}	

	protected List<Action> createProccessActions(){
		List<Action> actions=new ArrayList<Action>();
		actions.add(addAction("", "reporteDeSolicitudesPendientes", "Solicitudes Pendientes"));
		return actions;
	}
	
	public void reporteDeSolicitudesPendientes(){
		SolicitudesPendientesReportForm.run();
	}
	
	@Override
	protected List<SolicitudDeTraslado> findData() {
		String hql="from SolicitudDeTraslado s" +
				" where s.fecha between ? and ? ";
		return ServiceLocator2.getHibernateTemplate().find(hql
				,new Object[]{periodo.getFechaInicial(),periodo.getFechaFinal()});
	}
	
	public void print(){
		if(getSelectedObject()!=null){
			SolicitudDeTraslado sol=(SolicitudDeTraslado)getSelectedObject();
			print(sol);
		}
	}
	
	public void print(SolicitudDeTraslado sol){
		Map params=new HashMap();
		params.put("SOL_ID", sol.getId());
		ReportUtils.viewReport(ReportUtils.toReportesPath("invent/SolicitudDeTraslado.jasper"), params);
	}
	

}
