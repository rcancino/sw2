package com.luxsoft.sw3.maquila.ui.consultas;


import java.util.List;

import javax.swing.Action;

import org.apache.commons.lang.exception.ExceptionUtils;

import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.CollectionList.Model;
import ca.odell.glazedlists.gui.TableFormat;

import com.luxsoft.siipap.service.ServiceLocator2;
import com.luxsoft.siipap.swing.browser.AbstractMasterDatailFilteredBrowserPanel;
import com.luxsoft.siipap.swing.utils.MessageUtils;
import com.luxsoft.sw3.maquila.model.EntradaDeMaterial;
import com.luxsoft.sw3.maquila.model.EntradaDeMaterialDet;
import com.luxsoft.sw3.maquila.ui.forms.RecepcionDeMaterialForm;
import com.luxsoft.sw3.maquila.ui.forms.RecepcionDeMaterialFormModel;
import com.luxsoft.sw3.maquila.ui.reportes.ReporteDeDiferenciasEnGramaje;
import com.luxsoft.sw3.maquila.ui.reportes.ReporteDeEntradasCosteadas;
import com.luxsoft.sw3.maquila.ui.reportes.ReporteDeSalidaBobinasMaq;
import com.luxsoft.sw3.maquila.ui.reportes.ReporteInventarioBobinaXEntrada;
import com.luxsoft.sw3.maquila.ui.reportes.ReporteInventarioXClaveDeBobina;
import com.luxsoft.sw3.maquila.ui.selectores.SelectorDeBobinasDisponibles;
import com.luxsoft.sw3.services.MaquilaManager;

public class EntradaDeMaterialPanel extends AbstractMasterDatailFilteredBrowserPanel<EntradaDeMaterial, EntradaDeMaterialDet>{

	public EntradaDeMaterialPanel() {
		super(EntradaDeMaterial.class);		
	}

	@Override
	protected void agregarMasterProperties() {		
		addProperty("id","almacenNombre","entradaDeMaquilador","fecha","maquiladorNombre","observaciones");
		addLabels("Id","Almacén","Entrada(Maq)","Fecha","Maquilador","Observaciones");
		installTextComponentMatcherEditor("Almacén", "almacenNombre");
		installTextComponentMatcherEditor("Id", "id");
		installTextComponentMatcherEditor("Entrada", "entradaDeMaquilador");
	}

	@Override
	protected TableFormat createDetailTableFormat() {
		String props[]={"recepcion.id","entradaDeMaquilador","fecha","clave","descripcion","kilos","metros2","disponibleKilos","disponibleEnM2"};
		String names[]={"Recepción","Ent(Maq)","Fecha","Producto","Descripción","Kg","M2","Disp Kg","Disp M2"};
		return GlazedLists.tableFormat(EntradaDeMaterialDet.class, props,names);
	}

	@Override
	protected Model<EntradaDeMaterial, EntradaDeMaterialDet> createPartidasModel() {
		return new Model<EntradaDeMaterial, EntradaDeMaterialDet>(){
			@SuppressWarnings("unchecked")
			public List<EntradaDeMaterialDet> getChildren(
					EntradaDeMaterial parent) {
				String hql="from EntradaDeMaterialDet e where e.recepcion.id=?";
				return ServiceLocator2.getHibernateTemplate().find(hql,parent.getId());
			}			
		};
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
				,addAction("Disponibles", "mostrarBobinasDisponibles", "Bobinas Disponibles")
				};
		return actions;
	}

	@Override
	protected List<Action> createProccessActions() {
		List<Action> procesos=super.createProccessActions();
		procesos.add(addAction("", "reporteDiferenciaEnGramaje", "Rep. Dif en Gramaje"));
		procesos.add(addAction("", "reporteEntradasCosteadas", "Entradas costeadas"));
		procesos.add(addAction("", "reporteDeSalidaBobinasMaq", "Rep. Salidas Maq"));
		procesos.add(addAction("", "reporteDeInventario", "Rep Inventario"));
		procesos.add(addAction("", "reporteDeInventarioDetalle", "Rep Inventario Costeado"));
		
		
		
		return procesos;
	}

	@Override
	protected EntradaDeMaterial doInsert() {
		final RecepcionDeMaterialFormModel model=new RecepcionDeMaterialFormModel();
		final RecepcionDeMaterialForm form=new RecepcionDeMaterialForm(model);
		form.open();
		if(!form.hasBeenCanceled()){
			return getManager().salvarEntrada(model.getRecepcion());
		}else
			return null;
	}	
	
	@Override
	protected void afterInsert(EntradaDeMaterial bean) {
		super.afterInsert(bean);
	}
	
	@Override
	protected void doSelect(Object bean) {
		EntradaDeMaterial entrada=(EntradaDeMaterial)bean;
		entrada=getManager().getEntrada(entrada.getId());
		final RecepcionDeMaterialFormModel model=new RecepcionDeMaterialFormModel(entrada);
		model.setReadOnly(true);
		final RecepcionDeMaterialForm form=new RecepcionDeMaterialForm(model);
		form.open();
	}
	
	@Override
	protected EntradaDeMaterial doEdit(EntradaDeMaterial bean) {
		EntradaDeMaterial target=getManager().getEntrada(bean.getId());
		final RecepcionDeMaterialFormModel model=new RecepcionDeMaterialFormModel(target);
		final RecepcionDeMaterialForm form=new RecepcionDeMaterialForm(model);
		form.open();
		if(!form.hasBeenCanceled()){
			return getManager().salvarEntrada(model.getRecepcion());
		}else
			return bean;
	}

	@Override
	protected void afterEdit(EntradaDeMaterial bean) {
		super.afterEdit(bean);
	}

	@Override
	public boolean doDelete(EntradaDeMaterial bean) {
		try {
			getManager().eliminarEntrada(bean);
			return true;
		} catch (Exception e) {
			MessageUtils.showMessage("Error eliminando entrada:\n"+ExceptionUtils.getRootCauseMessage(e), "Recepción de bobinas");
			logger.error(e);
			return false;
		}
	}

	private MaquilaManager getManager(){
		return ServiceLocator2.getMaquilaManager();
	}
	
	public void reporteDiferenciaEnGramaje(){
		ReporteDeDiferenciasEnGramaje.run();
	}
	public void reporteEntradasCosteadas(){
		ReporteDeEntradasCosteadas.run();
	}
	
	public void reporteDeInventarioDetalle(){
		ReporteInventarioBobinaXEntrada.run();
	}
	public void reporteDeInventario(){
		ReporteInventarioXClaveDeBobina.run();
	}
	
	public void reporteDeSalidaBobinasMaq(){
		ReporteDeSalidaBobinasMaq.run();
	}
	
	public void consultarSalidasDeBobinas(){
		
	}
	
	
	public void mostrarBobinasDisponibles(){
		SelectorDeBobinasDisponibles.seleccionar(null);
	}
	
	

}
