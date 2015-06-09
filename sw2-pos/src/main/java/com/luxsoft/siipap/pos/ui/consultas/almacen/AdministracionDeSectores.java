package com.luxsoft.siipap.pos.ui.consultas.almacen;


import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.Action;
import javax.swing.JTextField;

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.CollectionList;
import ca.odell.glazedlists.CollectionList.Model;
import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.FilterList;
import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.TextFilterator;
import ca.odell.glazedlists.gui.TableFormat;
import ca.odell.glazedlists.matchers.CompositeMatcherEditor;
import ca.odell.glazedlists.matchers.MatcherEditor;
import ca.odell.glazedlists.swing.TextComponentMatcherEditor;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.luxsoft.siipap.inventarios.model.Sector;
import com.luxsoft.siipap.inventarios.model.SectorDet;
import com.luxsoft.siipap.model.Configuracion;
import com.luxsoft.siipap.model.Periodo;
import com.luxsoft.siipap.pos.ui.reports.ConteoSelectivoDeInventarioForm;
import com.luxsoft.siipap.pos.ui.reports.ProductosSinSectorForm;
import com.luxsoft.siipap.pos.ui.reports.RecorridosPorLineaForm;
import com.luxsoft.siipap.pos.ui.selectores.SelectorDeExistenciasParaConteo;
import com.luxsoft.siipap.pos.ui.utils.ReportUtils2;
import com.luxsoft.siipap.swing.browser.AbstractMasterDatailFilteredBrowserPanel;
import com.luxsoft.siipap.swing.reports.ReportUtils;
import com.luxsoft.siipap.swing.utils.CommandUtils;
import com.luxsoft.sw3.services.Services;
import com.luxsoft.sw3.ui.forms.SectorController;
import com.luxsoft.sw3.ui.forms.SectorForm;

/**
 * Panel para el proceso de conteo de inventario
 * 
 * @author Ruben Cancino
 *
 */
public class AdministracionDeSectores extends AbstractMasterDatailFilteredBrowserPanel<Sector, SectorDet>{
	
	
	public AdministracionDeSectores() {
		super(Sector.class);
	}
	
	
	
	protected void init(){		
		super.init();
		addProperty("sector","comentario","responsable1","responsable2");
		addLabels("Sector","Comentario","Responsable 1","Responsable 2");
		
		installTextComponentMatcherEditor("Sector", "sector");
	
		
		manejarPeriodo();
		periodo=Periodo.getPeriodoDelYear(Periodo.obtenerYear(new Date()));
	}

	@Override
	protected TableFormat createDetailTableFormat() {
		String[] props={"ind","clave","descripcion","producto.unidad","producto.kilos","comentario","producto.linea.nombre","producto.clase.nombre","producto.marca.nombre"};
		String[] labels={"Rngl","Producto","Descripción","Unidad","Kilos","Comentario","Linea","Clase","Marca"};
		return GlazedLists.tableFormat(SectorDet.class, props,labels);
		//final TableFormat tf=GlazedLists.tableFormat(SectorDet.class,propertyNames, columnLabels,edits);
	}

	@Override
	protected Model<Sector, SectorDet> createPartidasModel() {
		return new CollectionList.Model<Sector, SectorDet>(){
			public List<SectorDet> getChildren(Sector parent) {
				return parent.getPartidas();
			}
		};
	}

	@Override
	public Action[] getActions() {
		if(actions==null)
			actions=new Action[]{
				getLoadAction()
				,getInsertAction()
				,getEditAction()
				,getViewAction()
				,CommandUtils.createDeleteAction(this, "cancelar")
				,CommandUtils.createPrintAction(this, "print")
				,addAction("", "consultarExistencias", "Consultar Existencias")
				};
		return actions;
	}	
	
	

	@Override
	protected List<Action> createProccessActions() {
		List<Action> procesos=new ArrayList<Action>();
		procesos.add(addAction("", "recorridosPorLinea", "Reporte de Recorridos"));
	//	procesos.add(addAction("", "reporteDeConteoSelectivo", "Rep Conteo selectivo"));	
		procesos.add(addAction("", "reporteProdcutosSinSector", "Productos Sin Sector"));
		procesos.add(addAction("", "reporteDeMovsDelDia", "Movimientos Del Dia"));
		
		
		return procesos;
	}

	
	private JTextField claveField=new JTextField(5);
	
	
	protected void installDetailFilterComponents(DefaultFormBuilder builder){
		builder.appendSeparator("Detalle");
		builder.append("Clave",claveField );
	
		
	}
	
	@Override
	protected EventList decorateDetailList( EventList data){
		EventList<MatcherEditor> editors=new BasicEventList<MatcherEditor>();
		
		
		TextFilterator docFilterator=GlazedLists.textFilterator("clave");
		TextComponentMatcherEditor docEditor=new TextComponentMatcherEditor(claveField,docFilterator);
		editors.add(docEditor);
		
		
		
		CompositeMatcherEditor matcherEditor=new CompositeMatcherEditor(editors);
		FilterList detailFilter=new FilterList(data,matcherEditor);
		return detailFilter;
	}
	
	
	@Override
	protected List<Sector> findData() {
		
		String hql="from Sector c ";
		return Services.getInstance().getHibernateTemplate().find(hql);
	}
	
	@Override
	protected Sector doInsert() {
		final SectorController controller=new SectorController();
		final SectorForm form=new SectorForm(controller);
		form.open();
		if(!form.hasBeenCanceled()){
			Sector res=controller.persistir();
			controller.dispose();
			return res;
		}
		return null;
	}
	
	@Override
	public boolean doDelete(Sector bean) {
		Services.getInstance().getUniversalDao().remove(Sector.class, bean.getId());
		return true;
	}

	@Override
	protected Sector doEdit(Sector bean) {
	
			Sector target=(Sector)Services.getInstance().getHibernateTemplate().get(Sector.class, bean.getId());
			final SectorController controller=new SectorController(target,false);
			final SectorForm form=new SectorForm(controller);
			form.open();
			if(!form.hasBeenCanceled()){
				return controller.persistir();
			}
			return bean;
	
	}
	
	@Override
	protected void doSelect(Object bean) {
		if(bean!=null){
			Sector c=(Sector)bean;
			final SectorController controller=new SectorController(c,true);
			final SectorForm form=new SectorForm(controller);
			form.open();
		}
	}
	@Override
	public void open() {
		load();
	}
	
	@Override
	protected void afterInsert(Sector bean) {
		super.afterInsert(bean);
		load();
	
	}
	
	
	@Override
	protected void afterEdit(Sector bean) {
		super.afterEdit(bean);
		load();
	
	}

	public void cancelar() {
		
	}
	

	public void consultarExistencias(){
		SelectorDeExistenciasParaConteo.seleccionar(Configuracion.getSucursalLocalId());
	}

	public void print(){
		for(Object o:getSelected()){
			Sector dec=(Sector)o;
			print(dec);	
		}
	}
	
	public void print(Sector dec){
		Map params=new HashMap();
		params.put("SECTOR", dec.getId());
		ReportUtils2.runReport("invent/SectorAlmacen.jasper", params);
		//ReportUtils.printReport("file:z:/Reportes_MySQL/invent/SectorAlmacen.jasper", params, false);
		//ReportUtils.printReport("file:/mnt/siipapwin/Reportes_MySQL/invent/SectorAlmacen.jasper", params, false);
		
	}
	
	public void reporteDeConteoSelectovo(){
		ConteoSelectivoDeInventarioForm.run();
	}

	public void reporteProdcutosSinSector(){
			ProductosSinSectorForm.run();
	}
	
	public void recorridosPorLinea(){
		RecorridosPorLineaForm.run();
	}

	
	public void reporteDeMovsDelDia(){
		Map parametros=new HashMap();
		DateFormat df = new SimpleDateFormat("yyyy/MM/dd");
		String fechaIni=df.format(new Date()).toString().concat(" 00:00:00");
		
		
		String fechaFin=df.format(new Date()).toString().concat(" 23:00:00");
		
		parametros.put("FECHA_INI", fechaIni);
		parametros.put("FECHA_FIN", fechaFin);
		System.out.println("Parametros de reporte:"+parametros);
		ReportUtils.viewReport(ReportUtils.toReportesPath("invent/MovtosDelDiaPorProducto.jasper"), parametros);
	}

	

	
	

	


	
	
	
	
	
}
