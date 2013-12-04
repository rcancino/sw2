package com.luxsoft.sw3.maquila.ui.consultas;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.Action;
import javax.swing.JOptionPane;

import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.CollectionList.Model;
import ca.odell.glazedlists.gui.TableFormat;

import com.luxsoft.siipap.maquila.model.EntradaDeMaquila;
import com.luxsoft.siipap.model.Periodo;
import com.luxsoft.siipap.service.ServiceLocator2;
import com.luxsoft.siipap.swing.browser.AbstractMasterDatailFilteredBrowserPanel;
import com.luxsoft.siipap.swing.reports.ReportUtils;
import com.luxsoft.siipap.swing.utils.MessageUtils;


import com.luxsoft.sw3.maquila.model.AnalisisDeHojeo;
import com.luxsoft.sw3.maquila.ui.forms.AnalisisDeHojeoForm;
import com.luxsoft.sw3.maquila.ui.forms.AnalisisDeHojeoFormModel;
import com.luxsoft.sw3.services.MaquilaManager;

public class AnalisisDeHojeoPanel extends AbstractMasterDatailFilteredBrowserPanel<AnalisisDeHojeo, EntradaDeMaquila>{

	public AnalisisDeHojeoPanel() {
		super(AnalisisDeHojeo.class);
	}
	
	protected void agregarMasterProperties(){
		addProperty(
				"id",
				"cxpFactura.id",
				"nombre",
				"clave",
				"factura"
				,"fecha"				
				,"total"
				,"comentario"
				);
		addLabels(
				"Id",
				"CxP",
				"Nombre",
				"Prov",
				"Factura"
				,"Fecha"				
				,"Total"
				,"Comentario"
				);
		installTextComponentMatcherEditor("Proveedor ", "clave","nombre");
		installTextComponentMatcherEditor("Factura ", "factura");
		manejarPeriodo();
		
		
	}
	
	protected void manejarPeriodo(){
		periodo=Periodo.getPeriodoConAnteriroridad(3);
	}

	@Override
	protected TableFormat createDetailTableFormat() {
		String[] props={"sucursal.nombre","documento","recepcion.remision","clave","descripcion","cantidad","kilosCalculados","costoFlete","costoCorte","costoMateria","costo","comentario"};
		String[] names={"Sucursal","Docto","Remisión","Producto","Descripción","Cantidad","Kilos","Flete","Hojeo","Costo M.P.","Costo","Comentario"};
		return GlazedLists.tableFormat(EntradaDeMaquila.class, props,names);
	}

	@Override
	protected Model<AnalisisDeHojeo, EntradaDeMaquila> createPartidasModel() {
		return new Model<AnalisisDeHojeo, EntradaDeMaquila>(){
				public List<EntradaDeMaquila> getChildren(AnalisisDeHojeo parent) {
					String hql="from EntradaDeMaquila e left join fetch e.recepcion rr where e.analisisHojeo.id=?";
					return ServiceLocator2.getHibernateTemplate().find(hql,parent.getId());
				}			
		};
	}
	
	@Override
	protected List<AnalisisDeHojeo> findData() {
		String hql="from AnalisisDeHojeo a where a.fecha between ? and ?";
		return ServiceLocator2.getHibernateTemplate().find(hql,new Object[]{periodo.getFechaInicial(),periodo.getFechaFinal()});
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
	protected AnalisisDeHojeo doInsert() {
		
		final AnalisisDeHojeoFormModel model=new AnalisisDeHojeoFormModel();
		final AnalisisDeHojeoForm form=new AnalisisDeHojeoForm(model);
		form.open();
		if(!form.hasBeenCanceled()){
			AnalisisDeHojeo analisis=model.getAnalisis();
			analisis=getManager().salvarAnalisisDeHojeo(analisis);
			return analisis;
		}else
			return null;
		
	}
	
	@Override
	protected AnalisisDeHojeo doEdit(AnalisisDeHojeo bean) {
		if(bean.getCxpFactura()!=null){
			JOptionPane.showMessageDialog(getControl(), "Análisis con cuenta por pagar, no se puede modificar");
			return null;
		}
		AnalisisDeHojeo target=getManager().getAnalisisDeHojeo(bean.getId());
		final AnalisisDeHojeoFormModel model=new AnalisisDeHojeoFormModel(target);
		final AnalisisDeHojeoForm form=new AnalisisDeHojeoForm(model);
		form.open();
		if(!form.hasBeenCanceled()){
			AnalisisDeHojeo analisis=model.getAnalisis();
			analisis=getManager().salvarAnalisisDeHojeo(analisis);
			return analisis;
		}else
			return bean;
		
	}
	
	@Override
	protected void afterEdit(AnalisisDeHojeo bean) {
		super.afterEdit(bean);
	}

	@Override
	protected void doSelect(Object bean) {		
		AnalisisDeHojeo a=(AnalisisDeHojeo)bean;
		if(a!=null){
			a=getManager().getAnalisisDeHojeo(a.getId());
			final AnalisisDeHojeoFormModel model=new AnalisisDeHojeoFormModel(a);
			model.setReadOnly(true);
			final AnalisisDeHojeoForm form=new AnalisisDeHojeoForm(model);
			form.open();
		}
	}
	
	public void print(){
		print((AnalisisDeHojeo)getSelectedObject());
	}
	
	public void print(AnalisisDeHojeo a){		
		if(a!=null){			
			final Map parameters=new HashMap();
			parameters.put("NUMERO", a.getId());
			ReportUtils.viewReport(ReportUtils.toReportesPath("maquila/AnalisisHojeoMaq.jasper"),parameters);
		}
	}

	@Override
	public boolean doDelete(AnalisisDeHojeo bean) {
		if(bean.getCxpFactura()!=null){
			if(MessageUtils.showConfirmationMessage("Análisis con cuenta por pagar generada, seguro que desea eliminar? ", "Eliminación ")){
				getManager().eliminarAnalisisDeHojeo(bean);
				return true;
			}
		}
		return false;
	}
	
	public void generarCuentaPorPagar(){
		if(getSelectedObject()!=null){
			AnalisisDeHojeo a=(AnalisisDeHojeo)getSelectedObject();
			a=getManager().getAnalisisDeHojeo(a.getId());
			int index=source.indexOf(a);
			if(index!=-1){
				a=getManager().generarCuentaPorPagar(a);				
				source.set(index, a);
				JOptionPane.showMessageDialog(getControl(), "Cuenta por pagar generada: "+a.getCxpFactura().getId());
			}
		}
		
	}

	private MaquilaManager getManager(){
		return ServiceLocator2.getMaquilaManager();
	}
	

}
