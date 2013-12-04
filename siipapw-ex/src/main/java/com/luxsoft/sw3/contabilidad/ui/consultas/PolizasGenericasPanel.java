package com.luxsoft.sw3.contabilidad.ui.consultas;

import java.util.List;

import javax.swing.Action;

import com.luxsoft.siipap.service.ServiceLocator2;
import com.luxsoft.siipap.swing.utils.CommandUtils;
import com.luxsoft.sw3.contabilidad.model.Poliza;
import com.luxsoft.sw3.contabilidad.services.PolizasManager;
import com.luxsoft.sw3.contabilidad.ui.form.PolizaForm;
import com.luxsoft.sw3.contabilidad.ui.form.PolizaFormModel;

public class PolizasGenericasPanel extends PanelGenericoDePoliza{
	
	

	public PolizasGenericasPanel() {
		super();
		setClase("GENERICA");
	}

	@Override
	public boolean doDelete(Poliza bean) {
		ServiceLocator2.getPolizasManager().getPolizaDao().remove(bean.getId());
		return true;
	}

	@Override
	protected Poliza doEdit(Poliza bean) {
		
		final Poliza source=getPolizasManager().getPolizaDao().get(bean.getId());		
		final PolizaFormModel model=new PolizaFormModel(source);		
		final PolizaForm form=new PolizaForm(model);			
		form.open();
		if(!form.hasBeenCanceled()){
			Poliza res=model.getPoliza();
			return getPolizasManager().salvarPoliza(res);
		}
		return source;
	}
	
	public void insert(){
		Poliza bean=doInsert();
		if(bean!=null){
			source.add(bean);
			afterInsert(bean);
		}
	}
	
	@Override
	public Action[] getActions() {
		if(actions==null)
			actions=new Action[]{
				getLoadAction()				
				,getInsertAction()
				,getEditAction()
				,getDeleteAction()
				,CommandUtils.createPrintAction(this, "imprimirPoliza")
				//,addAction(null, "generarPoliza", "Salvar póliza")
												};
		return actions;
	}

	@Override
	protected Poliza doInsert() {
		final PolizaFormModel model=new PolizaFormModel();
		model.getPoliza().setClase("GENERICA");
		final PolizaForm form=new PolizaForm(model);
		
		form.open();
		if(!form.hasBeenCanceled()){
			Poliza res=model.getPoliza();			
			return getPolizasManager().salvarPoliza(res);
		}
		return super.doInsert();
	}
	@Override
	public void generarPoliza() {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	protected List<Poliza> findData() {
		System.out.println("Buscando polizas periodo: "+periodo);
		String hql="from Poliza p " +
				" where p.clase=? " +
				"   and date(p.fecha) between ? and ?";
		Object[] params={getClase(),periodo.getFechaInicial(),periodo.getFechaFinal()};
		return ServiceLocator2
			.getHibernateTemplate()
			.find(hql,params);
	}
	private PolizasManager getPolizasManager(){
		return ServiceLocator2.getPolizasManager();
	}

}
