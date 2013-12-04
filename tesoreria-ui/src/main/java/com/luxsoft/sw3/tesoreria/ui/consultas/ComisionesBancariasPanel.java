package com.luxsoft.sw3.tesoreria.ui.consultas;


import java.util.List;

import javax.swing.Action;

import ca.odell.glazedlists.CollectionList.Model;
import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.gui.TableFormat;

import com.luxsoft.siipap.model.tesoreria.CargoAbono;
import com.luxsoft.siipap.service.ServiceLocator2;
import com.luxsoft.siipap.swing.browser.AbstractMasterDatailFilteredBrowserPanel;
import com.luxsoft.sw3.tesoreria.model.ComisionBancaria;
import com.luxsoft.sw3.tesoreria.ui.forms.ComisionBancariaForm;
import com.luxsoft.sw3.tesoreria.ui.forms.ComisionBancariaFormModel;


public class ComisionesBancariasPanel extends AbstractMasterDatailFilteredBrowserPanel<ComisionBancaria,CargoAbono>{

	public ComisionesBancariasPanel() {
		super(ComisionBancaria.class);		
	}
	protected void agregarMasterProperties(){
		addProperty(
				"id"
				,"fecha"
				,"cuenta.cuentaDesc"
				,"comision"
				,"impuesto"
				,"referenciaOrigen"
				,"comentario"
				);
		addLabels(
				"Folio"
				,"Fecha"
				,"Cuenta"		
				,"Comisión"
				,"Impuesto"
				,"Referencia"
				,"Comentario"
				);
		setDefaultComparator(GlazedLists.beanPropertyComparator(ComisionBancaria.class, "id"));
		manejarPeriodo();		
	}
	
	@Override
	protected TableFormat createDetailTableFormat() {
		String props[]={"id","cuenta.cuentaDesc","fecha","clasificacion","importe","referencia","comentario"};
		String names[]={"Folio","Cuenta","Fecha","Clase","Importe","Ref","Comentario"};
		return GlazedLists.tableFormat(CargoAbono.class, props, names);
	}
	@Override
	protected Model<ComisionBancaria, CargoAbono> createPartidasModel() {
		return new Model<ComisionBancaria, CargoAbono>(){
			public List<CargoAbono> getChildren(ComisionBancaria parent) {
				return parent.getMovimientos();
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
				};
		}
		return actions;
	}

	@Override
	protected List<ComisionBancaria> findData() {
		String hql="from ComisionBancaria c where c.fecha between ? and ?";
		return ServiceLocator2.getHibernateTemplate().find(hql
				,new Object[]{
					periodo.getFechaInicial()
					,periodo.getFechaFinal()
					}
		);
	}
	
	public void open(){
		load();
	}
	@Override
	protected ComisionBancaria doInsert() {
		
		ComisionBancariaFormModel model=new ComisionBancariaFormModel();
		ComisionBancariaForm form=new ComisionBancariaForm(model);
		form.open();
		if(!form.hasBeenCanceled()){
		
		
			ComisionBancaria t=model.commit();
			
			t=ServiceLocator2.getTesoreriaManager().salvar(t);
			return t;
		}
		return super.doInsert();
	}
	
	@Override
	public boolean doDelete(ComisionBancaria bean) {
		ServiceLocator2.getUniversalDao().remove(ComisionBancaria.class, bean.getId());
		return true;
	}
	
	
}
