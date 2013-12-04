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
import com.luxsoft.siipap.maquila.model.MovimientoConFlete;
import com.luxsoft.siipap.model.Periodo;
import com.luxsoft.siipap.service.ServiceLocator2;
import com.luxsoft.siipap.swing.browser.AbstractMasterDatailFilteredBrowserPanel;
import com.luxsoft.siipap.swing.reports.ReportUtils;
import com.luxsoft.siipap.swing.utils.MessageUtils;
import com.luxsoft.sw3.maquila.model.AnalisisDeFlete;
import com.luxsoft.sw3.maquila.ui.forms.AnalisisDeFleteForm;
import com.luxsoft.sw3.maquila.ui.forms.AnalisisDeFleteFormModel;
import com.luxsoft.sw3.services.MaquilaManager;

public class AnalisisDeFletePanel extends AbstractMasterDatailFilteredBrowserPanel<AnalisisDeFlete, MovimientoConFlete>{

	public AnalisisDeFletePanel() {
		super(AnalisisDeFlete.class);
	}
	
	protected void agregarMasterProperties(){
		addProperty(
				"id",
				"cxpFactura.id",
				"nombre",
				"clave",
				"factura",
				"fecha",				
				"total",
				"comentario"
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
		String[] props={
				"sucursal.nombre"
				,"tipoDocto"
				,"documento"
				,"remision"
				,"producto.clave"
				,"producto.descripcion"
				,"cantidad"
				,"kilosCalculados"
				,"costoFlete"
				,"importeDelFlete"
				//,"comentario"
				};
		String[] names={
				"Sucursal"
				,"Tipo"
				,"Docto"
				,"Remisión"
				,"Producto"
				,"Descripción"
				,"Cantidad"
				,"Kilos"
				,"Costo Flete"
				,"Flete"
				,//"Comentario"
				};
		return GlazedLists.tableFormat(MovimientoConFlete.class, props,names);
	}

	@Override
	protected Model<AnalisisDeFlete, MovimientoConFlete> createPartidasModel() {
		return new Model<AnalisisDeFlete, MovimientoConFlete>(){
				public List<MovimientoConFlete> getChildren(AnalisisDeFlete parent) {
					String hql="from EntradaDeMaquila e left join fetch e.recepcion rr where e.analisisFlete.id=?";
					List data= ServiceLocator2.getHibernateTemplate().find(hql,parent.getId());
					hql="from EntradaPorCompra e left join fetch e.recepcion rr where e.analisisFlete.id=?";
					data.addAll(ServiceLocator2.getHibernateTemplate().find(hql,parent.getId()));
					hql="from TrasladoDet e  where e.analisisFlete.id=?";
					data.addAll(ServiceLocator2.getHibernateTemplate().find(hql,parent.getId()));
					hql="from TransformacionDet e  where e.analisisFlete.id=?";
					data.addAll(ServiceLocator2.getHibernateTemplate().find(hql,parent.getId()));
					return data;
				}			
		};
	}
	
	@Override
	protected List<AnalisisDeFlete> findData() {
		String hql="from AnalisisDeFlete a where a.fecha between ? and ?";
		return ServiceLocator2.getHibernateTemplate().find(hql,new Object[]{periodo.getFechaInicial(),periodo.getFechaFinal()});
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
				,addAction("","generarCuentaPorPagar","Generar cuenta x pagar")
				,addAction("","print","Imprimir")
				
				};
		return actions;
	}
	
	
	@Override
	protected AnalisisDeFlete doInsert() {
		
		final AnalisisDeFleteFormModel model=new AnalisisDeFleteFormModel();
		final AnalisisDeFleteForm form=new AnalisisDeFleteForm(model);
		form.open();
		if(!form.hasBeenCanceled()){
			AnalisisDeFlete analisis=model.getAnalisis();
			analisis=getManager().salvarAnalisisDeFlete(analisis);
			return analisis;
		}else
			return null;
		
	}
	
	@Override
	protected AnalisisDeFlete doEdit(AnalisisDeFlete bean) {
		if(bean.getCxpFactura()!=null){
			JOptionPane.showMessageDialog(getControl(), "Análisis con cuenta por pagar, no se puede modificar");
			return null;
		}
		AnalisisDeFlete target=getManager().getAnalisisDeFlete(bean.getId());
		final AnalisisDeFleteFormModel model=new AnalisisDeFleteFormModel(target);
		final AnalisisDeFleteForm form=new AnalisisDeFleteForm(model);
		form.open();
		if(!form.hasBeenCanceled()){
			AnalisisDeFlete analisis=model.getAnalisis();
			analisis=getManager().salvarAnalisisDeFlete(analisis);
			return analisis;
		}else
			return bean;
		
	}
	
	@Override
	protected void afterEdit(AnalisisDeFlete bean) {
		super.afterEdit(bean);
	}

	@Override
	protected void doSelect(Object bean) {		
		AnalisisDeFlete a=(AnalisisDeFlete)bean;
		a=getManager().getAnalisisDeFlete(a.getId());
		if(a!=null){
			final AnalisisDeFleteFormModel model=new AnalisisDeFleteFormModel(a);
			model.setReadOnly(true);
			final AnalisisDeFleteForm form=new AnalisisDeFleteForm(model);
			form.open();
		}
	}
	

	@Override
	public boolean doDelete(AnalisisDeFlete bean) {
		if(bean.getCxpFactura()!=null){
			//JOptionPane.showMessageDialog(getControl(), "Análisis con cuenta por pagar, no se puede modificar");
			if(MessageUtils.showConfirmationMessage("Análisis con cuenta por pagar generada, seguro que desea eliminar? ", "Eliminación ")){
				getManager().eliminarAnalisisDeFlete(bean);
				return true;
			}
			
		}
		return false;
	}

	public void print(){
		print((AnalisisDeFlete)getSelectedObject());
	}
	
	public void print(AnalisisDeFlete a){		
		if(a!=null){			
			final Map parameters=new HashMap();
			parameters.put("NUMERO", a.getId());
			ReportUtils.viewReport(ReportUtils.toReportesPath("maquila/AnalisisFleteMaq.jasper"),parameters);
		}
	}
	
	public void generarCuentaPorPagar(){
		if(getSelectedObject()!=null){
			AnalisisDeFlete a=(AnalisisDeFlete)getSelectedObject();
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
