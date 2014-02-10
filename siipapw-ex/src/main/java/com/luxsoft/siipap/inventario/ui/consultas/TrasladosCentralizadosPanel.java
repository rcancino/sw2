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
import com.luxsoft.siipap.inventarios.model.Traslado;
import com.luxsoft.siipap.inventarios.model.TrasladoDet;

import com.luxsoft.siipap.service.ServiceLocator2;
import com.luxsoft.siipap.swing.browser.AbstractMasterDatailFilteredBrowserPanel;
import com.luxsoft.siipap.swing.reports.ReportUtils;
import com.luxsoft.siipap.swing.utils.CommandUtils;


/**
 * Panel centralizado para consultas de traslados
 * 
 * @author Ruben Cancino
 *
 */
public class TrasladosCentralizadosPanel extends AbstractMasterDatailFilteredBrowserPanel<Traslado, TrasladoDet>{

	public TrasladosCentralizadosPanel() {
		super(Traslado.class);
		setTitle("Traslados");
		
	}
	
	protected void init(){		
		super.init();
		addProperty("sucursal.nombre","tipo","documento","fecha","solicitud.documento","solicitud.sucursal","chofer","porInventario","comentario","solicitud.referencia");
		addLabels("Sucursal","Tipo","Docto","Fecha","Sol","Sucursal (SOL)","Chofer","Por Inv","Comentario","Ref (Sol)");
		installTextComponentMatcherEditor("Solicitante", new String[]{"sucursal.nombre"});
		installTextComponentMatcherEditor("Tipo", new String[]{"tipo"});
		installTextComponentMatcherEditor("Documento", new String[]{"documento"});
		installTextComponentMatcherEditor("Solicitud", new String[]{"solicitud.documento"});
		installTextComponentMatcherEditor("Chofer", new String[]{"chofer"});
		installTextComponentMatcherEditor("Comentario", new String[]{"comentario"});
		manejarPeriodo();
	}

	@Override
	protected TableFormat createDetailTableFormat() {
		String[] props={"traslado.documento","producto.clave","producto.descripcion","tipo","solicitado","cantidad","comentario"};
		String[] labels={"Traslado","Producto","Descripción","Tipo","Solicitado","Cantidad","Comentario"};
		return GlazedLists.tableFormat(TrasladoDet.class, props,labels);
	}

	@Override
	protected Model<Traslado, TrasladoDet> createPartidasModel() {
		return new CollectionList.Model<Traslado, TrasladoDet>(){
			public List<TrasladoDet> getChildren(Traslado parent) {
				String hql="from TrasladoDet det where det.traslado.id=?";
				return ServiceLocator2
					.getHibernateTemplate()
					.find(hql,parent.getId());
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
	
	/**
	 * SOLO UTIL PARA EL POS
	 */
	public void atender(){
	}

	@Override
	public Action[] getActions() {
		if(actions==null)
			actions=new Action[]{
				getLoadAction()
				//,addRoleBasedContextAction(null,POSRoles.CONTROLADOR_DE_INVENTARIOS.name(), this, "atender", "Atender Sol.")
				//,getViewAction()
				,CommandUtils.createPrintAction(this, "print")
				};
		return actions;
	}	

	@Override
	protected List<Traslado> findData() {
		String hql="from Traslado s where " +
				" s.fecha between ? and ? ";		
		return ServiceLocator2.getHibernateTemplate().find(hql
				,new Object[]{
				periodo.getFechaInicial()
				,periodo.getFechaFinal()
				});
	}
	
	public void print(){
		if(getSelectedObject()!=null){
			Object seleccion=getSelectedObject();
			Traslado m=(Traslado)getSelectedObject();
			Map params=new HashMap();
			params.put("TRALADO_ID", m.getId());
			//ReportUtils2.runReport("invent/SalidaDeTraslado.jasper", params);
			
			ReportUtils.viewReport(ReportUtils.toReportesPath("invent/SalidaDeTraslado.jasper"), params);
			
		}
	}

	
	
	

}
