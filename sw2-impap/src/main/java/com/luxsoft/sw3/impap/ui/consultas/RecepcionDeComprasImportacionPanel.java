package com.luxsoft.sw3.impap.ui.consultas;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.Action;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JTextField;

import org.hibernate.validator.NotNull;

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

import com.jgoodies.binding.adapter.BasicComponentFactory;
import com.jgoodies.binding.list.SelectionInList;
import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;
import com.luxsoft.siipap.compras.model.EntradaPorCompra;
import com.luxsoft.siipap.compras.model.RecepcionDeCompra;
import com.luxsoft.siipap.model.Periodo;
import com.luxsoft.siipap.model.Sucursal;
import com.luxsoft.siipap.service.ServiceLocator2;
import com.luxsoft.siipap.swing.browser.AbstractMasterDatailFilteredBrowserPanel;
import com.luxsoft.siipap.swing.form2.AbstractForm;
import com.luxsoft.siipap.swing.form2.DefaultFormModel;
import com.luxsoft.siipap.swing.reports.ReportUtils;
import com.luxsoft.siipap.swing.utils.CommandUtils;
import com.luxsoft.sw3.impap.ui.form.RecepcionDeCompraController;
import com.luxsoft.sw3.impap.ui.form.RecepcionDeCompraForm;

/**
 * Consulta para administrar las recepciones de compras de importacion
 * 
 * @author Ruben Cancino Ramos
 *
 */
public class RecepcionDeComprasImportacionPanel extends AbstractMasterDatailFilteredBrowserPanel<RecepcionDeCompra, EntradaPorCompra>{

	public RecepcionDeComprasImportacionPanel() {
		super(RecepcionDeCompra.class);
		setTitle("Recepciones de Compras");
	}
	
	
	@Override
	protected void agregarMasterProperties(){
		addProperty("documento","fecha","compra.proveedor.nombre","remision","Compra.folio","compra.fecha");
		addLabels("Docto","Fecha","Proveedor","Remisión","Compra","Fecha C");
		installTextComponentMatcherEditor("Com", "documento");
		installTextComponentMatcherEditor("Remisión", "remision");
		installTextComponentMatcherEditor("Proveedor", "compra.proveedor.nombre");
		manejarPeriodo();
	}

	protected void manejarPeriodo(){
		periodo=Periodo.getPeriodoConAnteriroridad(6);
	}
	

	@Override
	protected TableFormat createDetailTableFormat() {
		return GlazedLists.tableFormat(
				EntradaPorCompra.class
				,new String[]{"sucursal.nombre","documento","fecha","clave","descripcion","compraDet.solicitado","cantidad","facturada","costo"}
				,new String[]{"Sucursal","Docto","Fecha","Prod","Desc","Solicitado","Recibido","Facturada","costo"}
				);
		
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
	protected Model<RecepcionDeCompra, EntradaPorCompra> createPartidasModel() {
		return new CollectionList.Model<RecepcionDeCompra, EntradaPorCompra>(){
			public List<EntradaPorCompra> getChildren(RecepcionDeCompra parent) {
				String hql="from EntradaPorCompra e where e.recepcion.id=?";
				return ServiceLocator2.getHibernateTemplate().find(hql, parent.getId());
			}
			
		};
	}


	@Override
	protected List<RecepcionDeCompra> findData() {
		String hql="from RecepcionDeCompra r " +
				" left join fetch r.compra c" +
				" left join fetch c.proveedor p" +
				" where r.fecha between ? and ?";
		Object[] params={periodo.getFechaInicial(),periodo.getFechaFinal()};
		return ServiceLocator2.getHibernateTemplate().find(hql,params);
	}


	@Override
	public Action[] getActions() {
		if(actions==null)
			actions=new Action[]{
				getLoadAction()
				,getViewAction()
				,getInsertAction()
				,CommandUtils.createPrintAction(this, "imprimir")
				,addAction("", "entradasPorDia", "Entradas por día")
				};
		return actions;
	}

	
	public void imprimir(){
		RecepcionDeCompra rec=(RecepcionDeCompra)getSelectedObject();
		if(rec!=null){
			Sucursal suc=rec.getSucursal();
			final Map parameters=new HashMap();
			parameters.put("ENTRADA", rec.getId());
			parameters.put("SUCURSAL", String.valueOf(suc.getId()));
			ReportUtils.viewReport(ReportUtils.toReportesPath("compras/EntradaPorCompra.jasper"), parameters);
		}
	}
	
	public void entradasPorDia(){
		final ReportForm1 form=new ReportForm1();
		form.open();
		if(!form.hasBeenCanceled()){
			final Map parameters=new HashMap();
			parameters.put("FECHA_ENT", form.getFecha());
			parameters.put("SUCURSAL", form.getSucursal().getId().toString());		
			ReportUtils.viewReport(ReportUtils.toReportesPath("compras/RecepDeMercancia.jasper"), parameters);
		}		
	}
	
	
	@Override
	protected RecepcionDeCompra doInsert() {
		RecepcionDeCompraController controller=new RecepcionDeCompraController();
		RecepcionDeCompraForm form=new RecepcionDeCompraForm(controller);
		form.open();
		if(!form.hasBeenCanceled()){
			return controller.persist();
		}
		return null;
	}


	

	private static class ReportForm1 extends AbstractForm{
		
		

		public ReportForm1() {
			super(new DefaultFormModel(new ReportModel()));
			
		}
		
		@Override
		protected JComponent buildFormPanel() {
			final FormLayout layout=new FormLayout("p,3dlu,70dlu:g","");
			final DefaultFormBuilder builder=new DefaultFormBuilder(layout);
			builder.append("Sucursal",getControl("sucursal"));
			builder.append("Fecha",getControl("fecha"));
			return builder.getPanel();
		}

		
		
		
		protected JComponent createCustomComponent(String property) {
			if("sucursal".equals(property)){
				List data=ServiceLocator2.getLookupManager().getSucursalesOperativas();
				SelectionInList sl=new SelectionInList(data,model.getModel(property));
				JComboBox box=BasicComponentFactory.createComboBox(sl);
				return box;			
			}
			return null;
		}

		public Sucursal getSucursal() {
			return (Sucursal)model.getValue("sucursal");
		}
		

		public Date getFecha() {
			return (Date)model.getValue("fecha");
		}

		

		
		public static class ReportModel extends com.jgoodies.binding.beans.Model{
			@NotNull
			private Sucursal sucursal;
			@NotNull
			private Date fecha=new Date();
			
			public Sucursal getSucursal() {
				return sucursal;
			}

			public void setSucursal(Sucursal sucursal) {
				Object old=this.sucursal;
				this.sucursal = sucursal;
				firePropertyChange("sucursal", old, sucursal);
				
			}

			public Date getFecha() {
				return fecha;
			}

			public void setFecha(Date fecha) {
				Object old=this.fecha;
				this.fecha = fecha;
				firePropertyChange("fecha", old, fecha);
			}

		}
		
		
	}

}
