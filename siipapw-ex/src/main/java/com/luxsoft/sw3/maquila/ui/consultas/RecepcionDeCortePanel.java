package com.luxsoft.sw3.maquila.ui.consultas;


import java.util.List;

import javax.swing.Action;

import org.apache.commons.lang.exception.ExceptionUtils;

import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.CollectionList.Model;
import ca.odell.glazedlists.gui.TableFormat;

import com.luxsoft.siipap.service.KernellSecurity;
import com.luxsoft.siipap.service.ServiceLocator2;
import com.luxsoft.siipap.swing.browser.AbstractMasterDatailFilteredBrowserPanel;
import com.luxsoft.siipap.swing.utils.MessageUtils;
import com.luxsoft.sw3.maquila.MAQUILA_ROLES;
import com.luxsoft.sw3.maquila.model.RecepcionDeCorte;
import com.luxsoft.sw3.maquila.model.RecepcionDeCorteDet;
import com.luxsoft.sw3.maquila.ui.forms.RecepcionDeCorteForm;
import com.luxsoft.sw3.maquila.ui.forms.RecepcionDeCorteFormModel;
import com.luxsoft.sw3.maquila.ui.reportes.ReporteEntradasHojeoMaq;
import com.luxsoft.sw3.maquila.ui.reportes.ReporteSalidaHojeoMaq;
import com.luxsoft.sw3.services.MaquilaManager;

public class RecepcionDeCortePanel extends AbstractMasterDatailFilteredBrowserPanel<RecepcionDeCorte, RecepcionDeCorteDet>{

	public RecepcionDeCortePanel() {
		super(RecepcionDeCorte.class);		
	}

	@Override
	protected void agregarMasterProperties() {		
		addProperty("id","almacen.nombre","fecha","comentario");
		addLabels("Id","Almacén","Fecha","Comentario");
	}

	@Override
	protected TableFormat createDetailTableFormat() {
		String props[]={
				"recepcion.id"
				,"fecha"
				,"origen.id"
				,"origen.entradaDeMaquilador"
				,"origen.clave"
				,"producto.clave"
				,"producto.descripcion"
				,"entrada"
				,"kilos"
				,"metros2"
				,"M2Teoricos"
				,"merma"
				,"mermaPor"
				,"costo"
				,"comentario"
				};
		String names[]={
				"Recepción"
				,"Fecha"
				,"Entrada"
				,"Ent Maquilador"
				,"Bobina"
				,"Producto"
				,"Descripción"
				,"Entrada"
				,"Kg"
				,"M2"
				,"M2(Teóricos)"
				,"Merma (M2)"
				,"Merma (%)"
				,"Costo"
				,"Comentario"
				};
		return GlazedLists.tableFormat(RecepcionDeCorteDet.class, props,names);
	}

	@Override
	protected Model<RecepcionDeCorte, RecepcionDeCorteDet> createPartidasModel() {
		return new Model<RecepcionDeCorte, RecepcionDeCorteDet>(){
			@SuppressWarnings("unchecked")
			public List<RecepcionDeCorteDet> getChildren(RecepcionDeCorte parent) {
				String hql="from RecepcionDeCorteDet e where e.recepcion.id=?";
				return ServiceLocator2.getHibernateTemplate().find(hql,parent.getId());
			}			
		};
	}

	@Override
	protected RecepcionDeCorte doInsert() {
		if(KernellSecurity.instance().hasRole(MAQUILA_ROLES.MOVIMIENTO_DE_BOBINAS.name())){
			final RecepcionDeCorteFormModel model=new RecepcionDeCorteFormModel();
			final RecepcionDeCorteForm form=new RecepcionDeCorteForm(model);
			form.open();
			if(!form.hasBeenCanceled()){
				return getManager().salvarRecepcionDeCorte(model.getRecepcion());
			}else
				return null;
		}
		return null;
	}	
	
	@Override
	protected void afterInsert(RecepcionDeCorte bean) {
		super.afterInsert(bean);
	}
	
	@Override
	protected void doSelect(Object bean) {
		RecepcionDeCorte recepcion=(RecepcionDeCorte)bean;		
		recepcion=getManager().getRecepcionDeCorte(recepcion.getId());
		final RecepcionDeCorteFormModel model=new RecepcionDeCorteFormModel(recepcion);
		model.setReadOnly(true);
		final RecepcionDeCorteForm form=new RecepcionDeCorteForm(model);
		form.open();
		
	}
	
	
	@Override
	public Action[] getActions() {
		if(actions==null)
			actions=new Action[]{
				getLoadAction()
				,getInsertAction()
				,getDeleteAction()
				,getEditAction()
				,getViewAction()
				};
		return actions;
	}

	@Override
	protected List<Action> createProccessActions() {
		List<Action> procesos=super.createProccessActions();
		procesos.add(addAction("", "reporteDeEntradaHojeo", "Rep. Entrada Hojeo"));
		procesos.add(addAction("", "reporteDeSalidaHojeo", "Rep. Salida Hojeo"));
		
		return procesos;
	}
	
	
	@Override
	protected RecepcionDeCorte doEdit(RecepcionDeCorte bean) {
		if(KernellSecurity.instance().hasRole(MAQUILA_ROLES.MOVIMIENTO_DE_BOBINAS.name())){
			RecepcionDeCorte target=getManager().getRecepcionDeCorte(bean.getId());
			final RecepcionDeCorteFormModel model=new RecepcionDeCorteFormModel(target);
			final RecepcionDeCorteForm form=new RecepcionDeCorteForm(model);
			form.open();
			if(!form.hasBeenCanceled()){
				return getManager().salvarRecepcionDeCorte(model.getRecepcion());
			}else
				return bean;
		}
		return bean;
		
	}

	@Override
	protected void afterEdit(RecepcionDeCorte bean) {
		super.afterEdit(bean);
	}

	@Override
	public boolean doDelete(RecepcionDeCorte bean) {
		if(KernellSecurity.instance().hasRole(MAQUILA_ROLES.MOVIMIENTO_DE_BOBINAS.name())){
			try {
				getManager().eliminarRecepcionDeCorte(bean);
				return true;
			} catch (Exception e) {
				MessageUtils.showMessage("Error eliminando recepción:\n"+ExceptionUtils.getRootCauseMessage(e), "Ordenes de corte");
				logger.error(e);
				return false;
			}
		}
		return false;
		
	}

	public void reporteDeEntradaHojeo(){
		ReporteEntradasHojeoMaq.run();
	}
	
	public void reporteDeSalidaHojeo(){
		ReporteSalidaHojeoMaq.run();
	}
	
	private MaquilaManager getManager(){
		return ServiceLocator2.getMaquilaManager();
	}

}
