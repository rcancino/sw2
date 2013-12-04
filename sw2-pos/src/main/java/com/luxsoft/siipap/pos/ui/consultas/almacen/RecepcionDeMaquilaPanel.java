package com.luxsoft.siipap.pos.ui.consultas.almacen;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.Action;

import org.springframework.orm.hibernate3.HibernateTemplate;

import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.CollectionList.Model;
import ca.odell.glazedlists.gui.TableFormat;

import com.luxsoft.siipap.maquila.model.EntradaDeMaquila;
import com.luxsoft.siipap.maquila.model.RecepcionDeMaquila;
import com.luxsoft.siipap.model.Sucursal;
import com.luxsoft.siipap.pos.ui.forms.RecepcionDeMaquilaForm;
import com.luxsoft.siipap.pos.ui.forms.RecepcionDeMaquilaFormModel;
import com.luxsoft.siipap.pos.ui.utils.ReportUtils2;
import com.luxsoft.siipap.swing.browser.AbstractMasterDatailFilteredBrowserPanel;
import com.luxsoft.sw3.services.InventariosManager;
import com.luxsoft.sw3.services.Services;

/**
 * Panel para el mantenimiento de entradas de maquila
 * 
 * @author Ruben Cancino
 *
 */
public class RecepcionDeMaquilaPanel extends AbstractMasterDatailFilteredBrowserPanel<RecepcionDeMaquila, EntradaDeMaquila>{

	public RecepcionDeMaquilaPanel() {
		super(RecepcionDeMaquila.class);
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

	@Override
	protected Model<RecepcionDeMaquila, EntradaDeMaquila> createPartidasModel() {
		return new Model<RecepcionDeMaquila, EntradaDeMaquila>(){
			public List<EntradaDeMaquila> getChildren(RecepcionDeMaquila parent) {
				return getHibernateTemplate().find("from EntradaDeMaquila e where e.recepcion.id=?",parent.getId());
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
			actions=new Action[]{getLoadAction(),getInsertAction(),getDeleteAction(),getEditAction(),getViewAction()};
		return actions;
	}
	
	@Override
	protected List<RecepcionDeMaquila> findData() {
		String hql="from RecepcionDeMaquila r where r.fecha between ? and ?";
		Object params[]={periodo.getFechaInicial(),periodo.getFechaFinal()};
		return getHibernateTemplate().find(hql,params);
	}
	
	@Override
	protected void doSelect(Object bean) {
		RecepcionDeMaquila rec=(RecepcionDeMaquila)bean;
		rec=getManager().getRecepcion(rec.getId());
		final RecepcionDeMaquilaFormModel model=new RecepcionDeMaquilaFormModel(rec);
		model.setReadOnly(true);
		final RecepcionDeMaquilaForm form=new RecepcionDeMaquilaForm(model);
		form.open();
	}

	@Override
	protected RecepcionDeMaquila doInsert() {
		final RecepcionDeMaquilaFormModel model=new RecepcionDeMaquilaFormModel();
		final RecepcionDeMaquilaForm form=new RecepcionDeMaquilaForm(model);
		form.open();
		if(!form.hasBeenCanceled()){
			RecepcionDeMaquila target=model.getRecepcion();
			return getManager().salvarRecepcion(target);
		}
		return null;
	}

	@Override
	protected void afterInsert(RecepcionDeMaquila bean) {		
		super.afterInsert(bean);
		print(bean);
	}	

	@Override
	protected RecepcionDeMaquila doEdit(RecepcionDeMaquila bean) {
		bean=getManager().getRecepcion(bean.getId());
		final RecepcionDeMaquilaFormModel model=new RecepcionDeMaquilaFormModel(bean);
		final RecepcionDeMaquilaForm form=new RecepcionDeMaquilaForm(model);
		form.open();
		if(!form.hasBeenCanceled()){
			RecepcionDeMaquila target=model.getRecepcion();
			return getManager().salvarRecepcion(target);
		}
		return null;
	}

	@Override
	protected void afterEdit(RecepcionDeMaquila bean) {		
		super.afterEdit(bean);
	}

	@Override
	public boolean doDelete(RecepcionDeMaquila bean) {
		getManager().eliminarRecepcion(bean);
		return true;
	}
	
	public void imprimirDocumento(){
		RecepcionDeMaquila rec=(RecepcionDeMaquila)getSelectedObject();
		print(rec);
	}
	
	public void print(RecepcionDeMaquila rec){
		//RecepcionDeMaquila rec=(RecepcionDeMaquila)getSelectedObject();
		if(rec!=null){
			Sucursal suc=rec.getSucursal();
			final Map parameters=new HashMap();
			parameters.put("ENTRADA", rec.getId());
			parameters.put("SUCURSAL", String.valueOf(suc.getId()));
			ReportUtils2.runReport("maquila/EntradaPorMaquila.jasper", parameters);
		}
	}
	
	

	private InventariosManager getManager(){
		return Services.getInstance().getInventariosManager();
	}
	
	protected HibernateTemplate getHibernateTemplate(){
		return Services.getInstance().getHibernateTemplate();
	}

}
