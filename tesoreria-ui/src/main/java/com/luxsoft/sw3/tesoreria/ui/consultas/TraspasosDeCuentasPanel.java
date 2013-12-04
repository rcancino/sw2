package com.luxsoft.sw3.tesoreria.ui.consultas;


import java.util.List;

import javax.swing.Action;

import ca.odell.glazedlists.CollectionList.Model;
import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.FilterList;
import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.gui.TableFormat;
import ca.odell.glazedlists.matchers.Matchers;

import com.luxsoft.siipap.model.tesoreria.CargoAbono;
import com.luxsoft.siipap.service.ServiceLocator2;
import com.luxsoft.siipap.swing.browser.AbstractMasterDatailFilteredBrowserPanel;
import com.luxsoft.sw3.tesoreria.model.TraspasoDeCuenta;
import com.luxsoft.sw3.tesoreria.ui.forms.TraspasoDeCuentaForm;
import com.luxsoft.sw3.tesoreria.ui.forms.TraspasoDeCuentaFormModel;

public class TraspasosDeCuentasPanel extends AbstractMasterDatailFilteredBrowserPanel<TraspasoDeCuenta,CargoAbono>{

	public TraspasosDeCuentasPanel() {
		super(TraspasoDeCuenta.class);		
	}
	
	protected void agregarMasterProperties(){
		addProperty(
				"id"
				,"fecha"
				,"cuentaOrigen.cuentaDesc"
				,"cuentaDestino.cuentaDesc"
				,"importe"
				,"comision"
				,"impuesto"
				,"comentario"
				,"descripcion"
				
				
				);
		addLabels(
				"Folio"
				,"Fecha"
				,"Origen"
				,"Destino"
				,"Importe"				
				,"Comisión"
				,"Impuesto"
				,"Comentario"
				,"Tipo"
				);
		setDefaultComparator(GlazedLists.beanPropertyComparator(TraspasoDeCuenta.class, "id"));
		manejarPeriodo();		
	}
	
	@Override
	protected TableFormat createDetailTableFormat() {
		String props[]={"id","traspaso.id","cuenta.cuentaDesc","fecha","clasificacion","importe","referencia","comentario"};
		String names[]={"Folio","Traspaso","Cuenta","Fecha","Clase","Importe","Ref","Comentario"};
		return GlazedLists.tableFormat(CargoAbono.class, props, names);
	}
	@Override
	protected Model<TraspasoDeCuenta, CargoAbono> createPartidasModel() {
		return new Model<TraspasoDeCuenta, CargoAbono>(){
			public List<CargoAbono> getChildren(TraspasoDeCuenta parent) {
				return ServiceLocator2.getHibernateTemplate().find("from CargoAbono c where c.traspaso.id=?",parent.getId());
			}
		};
	}
	
	@Override
	public Action[] getActions() {
		if(actions==null){
			actions=new Action[]{
				getLoadAction()
				,getInsertAction()
				,getDeleteAction()				
				,getViewAction()				
				,addAction(null, "imprimir", "Imprimir")
				};
		}
		return actions;
	}

	@Override
	protected List<TraspasoDeCuenta> findData() {
		String hql="from TraspasoDeCuenta c where c.class=TraspasoDeCuenta and fecha between ? and ?";
		return ServiceLocator2.getHibernateTemplate().find(hql
				,new Object[]{
					periodo.getFechaInicial()
					,periodo.getFechaFinal()
					}
		);
	}
	/*
	@Override
	protected EventList getSourceEventList() {		
		EventList eventList=super.getSourceEventList();
		eventList=new FilterList(eventList,Matchers.beanPropertyMatcher(TraspasoDeCuenta.class, "descripcion", "TRASPASO ENTRE CUENTAS"));
		return eventList;
	}
	*/
	public void open(){
		load();
	}
	@Override
	protected TraspasoDeCuenta doInsert() {
		TraspasoDeCuentaFormModel model=new TraspasoDeCuentaFormModel();
		TraspasoDeCuentaForm form=new TraspasoDeCuentaForm(model);
		form.open();
		if(!form.hasBeenCanceled()){
			TraspasoDeCuenta t=model.commit();
			t=ServiceLocator2.getTesoreriaManager().salvar(t);
			return t;
		}
		return super.doInsert();
	}
	
	@Override
	public boolean doDelete(TraspasoDeCuenta bean) {
		ServiceLocator2.getUniversalDao().remove(TraspasoDeCuenta.class, bean.getId());
		return true;
	}

}
