package com.luxsoft.sw3.maquila.ui.consultas;


import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.Action;

import org.apache.commons.lang.exception.ExceptionUtils;

import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.CollectionList.Model;
import ca.odell.glazedlists.gui.TableFormat;

import com.luxsoft.siipap.service.ServiceLocator2;
import com.luxsoft.siipap.swing.browser.AbstractMasterDatailFilteredBrowserPanel;
import com.luxsoft.siipap.swing.reports.ReportUtils;
import com.luxsoft.siipap.swing.utils.MessageUtils;
import com.luxsoft.sw3.maquila.model.OrdenDeCorte;
import com.luxsoft.sw3.maquila.model.OrdenDeCorteDet;
import com.luxsoft.sw3.maquila.model.RecepcionDeCorteDet;
import com.luxsoft.sw3.maquila.ui.forms.OrdenDeCorteForm;
import com.luxsoft.sw3.maquila.ui.forms.OrdenDeCorteFormModel;
import com.luxsoft.sw3.maquila.ui.reportes.ReporteDeInventarioEnProcesoDeCorte;
import com.luxsoft.sw3.maquila.ui.reportes.ReporteDeMermas;
import com.luxsoft.sw3.maquila.ui.reportes.ReporteInventarioHojeado;
import com.luxsoft.sw3.maquila.ui.reportes.ReporteOrdenDeCorte;
import com.luxsoft.sw3.maquila.ui.reportes.ReporteOrdenesDeCorteCosteadas;
import com.luxsoft.sw3.services.MaquilaManager;

public class OrdenesDeCortePanel extends AbstractMasterDatailFilteredBrowserPanel<OrdenDeCorte, OrdenDeCorteDet>{

	public OrdenesDeCortePanel() {
		super(OrdenDeCorte.class);		
	}

	@Override
	protected void agregarMasterProperties() {		
		addProperty("id","almacenNombre","fecha","maquiladorNombre","comentario");
		addLabels("Id","Almacén","Fecha","Maquilador","Comentario");
	}

	@Override
	protected TableFormat createDetailTableFormat() {
		String props[]={"orden.id","fecha","entradaDeMaquilador","origen.id","origen.clave","destino.clave","destino.descripcion","kilos","metros2","recibido"};
		String names[]={"Recepción","Fecha","Ent(Maq)","Entrada(Id)","Origen","Producto","Descripción","Kg","M2","Hojedo"};
		return GlazedLists.tableFormat(OrdenDeCorteDet.class, props,names);
	}

	@Override
	protected Model<OrdenDeCorte, OrdenDeCorteDet> createPartidasModel() {
		return new Model<OrdenDeCorte, OrdenDeCorteDet>(){
			@SuppressWarnings("unchecked")
			public List<OrdenDeCorteDet> getChildren(
					OrdenDeCorte parent) {
				String hql="from OrdenDeCorteDet e where e.orden.id=?";
				return ServiceLocator2.getHibernateTemplate().find(hql,parent.getId());
			}			
		};
	}
	
	@Override
	protected List<Action> createProccessActions() {
		List<Action> procesos=super.createProccessActions();
		procesos.add(addAction("", "analisisDeCorte", "Análisis de corte"));
		procesos.add(addAction("", "reporteDeMermas", "Reporte de Mermas"));
		procesos.add(addAction("", "reporteEnProceso", "Inventario en Transito"));
		
		procesos.add(addAction("", "ordenDeCorte", "Imprimir Orden "));
		procesos.add(addAction("", "reporteDeOrdenesCoteadas", "Reporte de Ordenes (Cos)"));
		//procesos.add(addAction("", "reporteDeInvHojeado", "Reporte Inventario Hojeado"));
		
		return procesos;
	}

	@Override
	protected OrdenDeCorte doInsert() {
		final OrdenDeCorteFormModel model=new OrdenDeCorteFormModel();
		final OrdenDeCorteForm form=new OrdenDeCorteForm(model);
		form.open();
		if(!form.hasBeenCanceled()){
			return getManager().salvarOrden(model.getOrden());
		}else
			return null;
	}	
	
	@Override
	protected void afterInsert(OrdenDeCorte bean) {
		super.afterInsert(bean);
	}
	
	@Override
	protected void doSelect(Object bean) {
		OrdenDeCorte entrada=(OrdenDeCorte)bean;
		entrada=getManager().getOrden(entrada.getId());
		final OrdenDeCorteFormModel model=new OrdenDeCorteFormModel(entrada);
		model.setReadOnly(true);
		final OrdenDeCorteForm form=new OrdenDeCorteForm(model);
		form.open();
	}
	
	@Override
	protected OrdenDeCorte doEdit(OrdenDeCorte bean) {
		OrdenDeCorte target=getManager().getOrden(bean.getId());
		final OrdenDeCorteFormModel model=new OrdenDeCorteFormModel(target);
		final OrdenDeCorteForm form=new OrdenDeCorteForm(model);
		form.open();
		if(!form.hasBeenCanceled()){
			return getManager().salvarOrden(model.getOrden());
		}else
			return bean;
	}

	@Override
	protected void afterEdit(OrdenDeCorte bean) {
		super.afterEdit(bean);
	}

	@Override
	public boolean doDelete(OrdenDeCorte bean) {
		try {
			getManager().eliminarOrden(bean);
			return true;
		} catch (Exception e) {
			MessageUtils.showMessage("Error eliminando orden:\n"+ExceptionUtils.getRootCauseMessage(e), "Ordenes de corte");
			logger.error(e);
			return false;
		}
	}
	
	public void analisisDeCorte(){	
		//Verificar si hay seleccion en el detalle
		if(!detailSelectionModel.isSelectionEmpty()){
			Object seleccion=detailSelectionModel.getSelected().get(0);			
			OrdenDeCorteDet ordendet=(OrdenDeCorteDet) seleccion;
			if(ordendet!=null){
				Map<String,Object> map=new HashMap<String, Object>();
				map.put("ORDEN", ordendet.getRecepcionId());
				ReportUtils.viewReport(ReportUtils
						.toReportesPath("maquila/Analisis_de_corte.jasper"), map);
			}
		}
		
	}
	
	
	public void ordenDeCorte(){		
		OrdenDeCorte orden=(OrdenDeCorte)getSelectedObject();
		if(orden!=null){
			Map<String,Object> map=new HashMap<String, Object>();
			map.put("ORDEN", orden.getId());
			ReportUtils.viewReport(ReportUtils
					.toReportesPath("maquila/Orden_de_Corte.jasper"), map);
		}
	}
	
	
	
	
	public void reporteDeMermas(){
		ReporteDeMermas.run();
	}
	

	
	public void reporteEnProceso(){
		ReporteDeInventarioEnProcesoDeCorte.run();
	}
	public void reporteDeOrdenes(){
		ReporteOrdenDeCorte.run();
	}
	public void reporteDeOrdenesCoteadas(){
		ReporteOrdenesDeCorteCosteadas.run();
	}

	private MaquilaManager getManager(){
		return ServiceLocator2.getMaquilaManager();
	}

}
