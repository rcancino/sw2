package com.luxsoft.siipap.inventario.ui.consultas;


import java.util.Comparator;
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
import ca.odell.glazedlists.matchers.Matcher;
import ca.odell.glazedlists.matchers.MatcherEditor;
import ca.odell.glazedlists.swing.TextComponentMatcherEditor;

import com.jgoodies.forms.builder.DefaultFormBuilder;

import com.luxsoft.siipap.inventarios.model.Transformacion;
import com.luxsoft.siipap.inventarios.model.TransformacionDet;


import com.luxsoft.siipap.service.ServiceLocator2;
import com.luxsoft.siipap.swing.browser.AbstractMasterDatailFilteredBrowserPanel;
import com.luxsoft.siipap.swing.matchers.CheckBoxMatcher;
import com.luxsoft.siipap.swing.reports.ReportUtils;
import com.luxsoft.siipap.swing.utils.CommandUtils;



/**
 * Panel para el mantenimiento de traslados
 * 
 * @author Ruben Cancino
 *
 */
public class TransformacionesCentralizadasPanel extends AbstractMasterDatailFilteredBrowserPanel<Transformacion, TransformacionDet>{

	public TransformacionesCentralizadasPanel() {
		super(Transformacion.class);
		setTitle("Transformaciones");
		
	}
	
	protected void init(){		
		addProperty("sucursal.nombre","clase","fecha","documento","porInventario","comentario");
		addLabels("Sucursal","Clase","Fecha","Docto","Por Inv","Comentario");
		
		installTextComponentMatcherEditor("Sucursal", "sucursal.nombre");
		installTextComponentMatcherEditor("Docto", "documento");		
		installTextComponentMatcherEditor("Tipo", "concepto");
		installTextComponentMatcherEditor("Comentario", "comentario");
		CheckBoxMatcher<Transformacion> porInvMatcher=new CheckBoxMatcher<Transformacion>(){
			@Override
			protected Matcher<Transformacion> getSelectMatcher(Object... obj) {
				return new Matcher<Transformacion>(){
					public boolean matches(Transformacion item) {
						if(item!=null)
							return item.getPorInventario();
						return false;
					}
				};
			}
		};
		installCustomMatcherEditor("Por Inventario", porInvMatcher.getBox(), porInvMatcher);
		manejarPeriodo();
		setActions(
				new Action[]{getLoadAction()
						,getInsertAction()
						,getDeleteAction()
						,CommandUtils.createPrintAction(this, "imprimir")
						}
				);
	}
	
	protected Comparator getDefaultDetailComparator(){
		return GlazedLists.beanPropertyComparator(TransformacionDet.class, "renglon");
	}

	@Override
	protected TableFormat createDetailTableFormat() {
		String[] props={"sucursal.nombre","documento","fecha","transformacion.clase","renglon","clave","descripcion","producto.linea.nombre","unidad.nombre","kilos","cantidad","comentario"};
		String[] names={"sucursal.nombre","documento","Fecha","Tipo","Rngl","Clave","Desc","Línea","U","Kg","Cant","Comentario"};		
		return GlazedLists.tableFormat(TransformacionDet.class, props,names);
	}

	@Override
	protected Model<Transformacion, TransformacionDet> createPartidasModel() {
		return new CollectionList.Model<Transformacion, TransformacionDet>(){
			public List<TransformacionDet> getChildren(Transformacion parent) {
				String hql="from TransformacionDet d where d.transformacion.id=?";
				return ServiceLocator2.getHibernateTemplate().find(hql,parent.getId());
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

	@Override
	protected List<Transformacion> findData() {
		String hql="from Transformacion t where t.fecha between ? and ?";
		return ServiceLocator2.getHibernateTemplate().find(hql,new Object[]{periodo.getFechaInicial(),periodo.getFechaFinal()});
	}

	
	public void imprimir(){
		Transformacion d=(Transformacion)getSelectedObject();
		if(d!=null){
			final Map parameters=new HashMap();
			parameters.put("TRANSFORMACION", d.getId());
			parameters.put("SUCURSAL", String.valueOf(d.getSucursal().getId()));
			ReportUtils.viewReport(ReportUtils.toReportesPath("invent/Transformacion.jasper"), parameters);
			
			
		}
	}

	
	
	
}
