package com.luxsoft.sw3.maquila.ui.consultas;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.Action;
import javax.swing.JOptionPane;

import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.CollectionList.Model;
import ca.odell.glazedlists.gui.TableFormat;


import com.luxsoft.siipap.service.ServiceLocator2;
import com.luxsoft.siipap.swing.browser.AbstractMasterDatailFilteredBrowserPanel;
import com.luxsoft.siipap.swing.reports.ReportUtils;
import com.luxsoft.sw3.maquila.model.AnalisisDeMaterial;
import com.luxsoft.sw3.maquila.model.EntradaDeMaterialDet;
import com.luxsoft.sw3.maquila.ui.forms.AnalisisDeMaterialForm;
import com.luxsoft.sw3.maquila.ui.forms.AnalisisDeMaterialFormModel;
import com.luxsoft.sw3.services.MaquilaManager;

public class AnalisisDeMaterialPanel extends AbstractMasterDatailFilteredBrowserPanel<AnalisisDeMaterial, EntradaDeMaterialDet>{

	public AnalisisDeMaterialPanel() {
		super(AnalisisDeMaterial.class);
	}
	
	public void init(){
		addProperty(
				"id",
				"nombre",
				"clave",
				"factura"
				,"fecha"
				,"cxpFactura.id"
				,"moneda"
				,"tc"
				,"total"
				,"comentario"
				);
		addLabels(
				"Id",
				"Nombre",
				"Prov",
				"Nombre"
				,"Fecha"
				,"Factura_Id"
				,"Mon"
				,"TC"
				,"Total"
				,"Comentario"
				);
	}

	@Override
	protected TableFormat createDetailTableFormat() {
		String props[]={"recepcion.id","entradaDeMaquilador","fecha","clave","descripcion","kilos","metros2","precioPorKilo","precioPorM2","disponibleKilos","disponibleEnM2"};
		String names[]={"Recepción","Ent(Maq)","Fecha","Producto","Descripción","Kg","M2","Precio Kg","Precio M2","Disp Kg","Disp M2"};
		return GlazedLists.tableFormat(EntradaDeMaterialDet.class, props,names);
	}

	@Override
	protected Model<AnalisisDeMaterial, EntradaDeMaterialDet> createPartidasModel() {
		return new Model<AnalisisDeMaterial, EntradaDeMaterialDet>(){
				public List<EntradaDeMaterialDet> getChildren(AnalisisDeMaterial parent) {
					String hql="from EntradaDeMaterialDet e where e.analisis.id=?";
					return ServiceLocator2.getHibernateTemplate().find(hql,parent.getId());
				}			
		};
	}
	
	
	
	@Override
	public Action[] getActions() {
		if(actions==null)
			actions=new Action[]{getLoadAction(),getInsertAction(),getDeleteAction(),getEditAction()
				,getViewAction()
				,addAction("","generarCuentaPorPagar","Generar cuenta x pagar")
				,addAction("","print","Imprimir")
				};
		return actions;
	}

	@Override
	protected AnalisisDeMaterial doInsert() {
		final AnalisisDeMaterialFormModel model=new AnalisisDeMaterialFormModel();
		final AnalisisDeMaterialForm form=new AnalisisDeMaterialForm(model);
		form.open();
		if(!form.hasBeenCanceled()){
			AnalisisDeMaterial analisis=model.getAnalisis();
			analisis=getManager().salvarAnalisis(analisis);
			print(analisis);
			return analisis;
		}else
			return null;
	}
	
	@Override
	protected AnalisisDeMaterial doEdit(AnalisisDeMaterial bean) {
		AnalisisDeMaterial target=getManager().getAnalisis(bean.getId());
		final AnalisisDeMaterialFormModel model=new AnalisisDeMaterialFormModel(target);
		final AnalisisDeMaterialForm form=new AnalisisDeMaterialForm(model);
		form.open();
		if(!form.hasBeenCanceled()){
			AnalisisDeMaterial analisis=model.getAnalisis();
			analisis=getManager().salvarAnalisis(analisis);
			return analisis;
		}else
			return bean;
	}
	
	@Override
	protected void afterEdit(AnalisisDeMaterial bean) {
		super.afterEdit(bean);
	}

	@Override
	protected void doSelect(Object bean) {
		AnalisisDeMaterial a=(AnalisisDeMaterial)bean;
		final AnalisisDeMaterialFormModel model=new AnalisisDeMaterialFormModel(a);
		model.setReadOnly(true);
		final AnalisisDeMaterialForm form=new AnalisisDeMaterialForm(model);
		form.open();
	}
	

	@Override
	public boolean doDelete(AnalisisDeMaterial bean) {
		getManager().eliminarAnalisis(bean);
		return true;
	}
	
	public void print(){
		print((AnalisisDeMaterial)getSelectedObject());
	}
	
	public void print(AnalisisDeMaterial a){		
		if(a!=null){			
			final Map parameters=new HashMap();
			parameters.put("NUMERO", a.getId());
			ReportUtils.viewReport(ReportUtils.toReportesPath("maquila/AnalisisDeFacturaMaq.jasper"),parameters);
		}
	}

	private MaquilaManager getManager(){
		return ServiceLocator2.getMaquilaManager();
	}
	
	public void generarCuentaPorPagar(){
		if(getSelectedObject()!=null){
			AnalisisDeMaterial a=(AnalisisDeMaterial)getSelectedObject();
			int index=source.indexOf(a);
			if(index!=-1){
				a=getManager().generarCuentaPorPagar(a);				
				source.set(index, a);
				JOptionPane.showMessageDialog(getControl(), "Cuenta por pagar generada: "+a.getCxpFactura().getId());
			}
		}
		
	}
	

}
