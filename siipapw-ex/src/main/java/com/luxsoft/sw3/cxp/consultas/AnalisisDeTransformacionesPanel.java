package com.luxsoft.sw3.cxp.consultas;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.Action;
import javax.swing.JOptionPane;

import ca.odell.glazedlists.CollectionList.Model;
import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.gui.TableFormat;

import com.luxsoft.siipap.cxp.model.AnalisisDeTransformacion;
import com.luxsoft.siipap.inventarios.model.TransformacionDet;
import com.luxsoft.siipap.model.Periodo;
import com.luxsoft.siipap.service.ServiceLocator2;
import com.luxsoft.siipap.swing.browser.AbstractMasterDatailFilteredBrowserPanel;
import com.luxsoft.siipap.swing.reports.ReportUtils;
import com.luxsoft.siipap.swing.utils.MessageUtils;
import com.luxsoft.sw3.cxp.forms.AnalisisDeTransformacionForm;
import com.luxsoft.sw3.cxp.forms.AnalisisDeTransformacionFormModel;
import com.luxsoft.sw3.services.AnalisisDeTransfomracionesManager;

public class AnalisisDeTransformacionesPanel extends AbstractMasterDatailFilteredBrowserPanel<AnalisisDeTransformacion, TransformacionDet>{

	public AnalisisDeTransformacionesPanel() {
		super(AnalisisDeTransformacion.class);
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
		periodo=Periodo.getPeriodoDelMesActual(new Date());
	}

	@Override
	protected TableFormat createDetailTableFormat() {
		String[] props={
				"sucursal.nombre"
				,"documento"
				,"producto.clave"
				,"producto.descripcion"
				,"cantidad"
				,"kilosCalculados"
				,"costoOrigen"
				,"costo"
				,"gastos"
				};
		String[] names={
				"Sucursal"
				,"Docto"
				,"Producto"
				,"Descripción"
				,"Cantidad"
				,"Kilos"
				,"Costo Origen"
				,"Costo"
				,"Gasto"
				};
		return GlazedLists.tableFormat(TransformacionDet.class, props,names);
	}

	@Override
	protected Model<AnalisisDeTransformacion, TransformacionDet> createPartidasModel() {
		return new Model<AnalisisDeTransformacion, TransformacionDet>(){
				public List<TransformacionDet> getChildren(AnalisisDeTransformacion parent) {
					String hql="from TransformacionDet t left join fetch t.transformacion rr where t.analisis.id=?";
					return ServiceLocator2.getHibernateTemplate().find(hql,parent.getId());
				}			
		};
	}
	
	@Override
	protected List<AnalisisDeTransformacion> findData() {
		String hql="from AnalisisDeTransformacion a where a.fecha between ? and ?";
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
	protected AnalisisDeTransformacion doInsert() {
		
		final  AnalisisDeTransformacionFormModel model=new  AnalisisDeTransformacionFormModel();
		final  AnalisisDeTransformacionForm form=new  AnalisisDeTransformacionForm(model);
		form.open();
		if(!form.hasBeenCanceled()){
			AnalisisDeTransformacion analisis=model.getAnalisis();
			analisis=getManager().salvarAnalisis(analisis);
			return analisis;
		}else
			return null;
		
	}
	
	@Override
	protected AnalisisDeTransformacion doEdit(AnalisisDeTransformacion bean) {
		if(bean.getCxpFactura()!=null){
			JOptionPane.showMessageDialog(getControl(), "Análisis con cuenta por pagar, no se puede modificar");
			return null;
		}
		AnalisisDeTransformacion target=getManager().getAnalisis(bean.getId());
		final AnalisisDeTransformacionFormModel model=new AnalisisDeTransformacionFormModel(target);
		final AnalisisDeTransformacionForm form=new AnalisisDeTransformacionForm(model);
		form.open();
		if(!form.hasBeenCanceled()){
			AnalisisDeTransformacion analisis=model.getAnalisis();
			analisis=getManager().salvarAnalisis(analisis);
			return analisis;
		}else
			return bean;
		
	}

	@Override
	protected void doSelect(Object bean) {		
		AnalisisDeTransformacion a=(AnalisisDeTransformacion)bean;
		a=getManager().getAnalisis(a.getId());
		if(a!=null){
			final AnalisisDeTransformacionFormModel model=new  AnalisisDeTransformacionFormModel(a);
			model.setReadOnly(true);
			final  AnalisisDeTransformacionForm form=new  AnalisisDeTransformacionForm(model);
			form.open();
		}
	}
	

	@Override
	public boolean doDelete(AnalisisDeTransformacion bean) {		
		if(bean.getCxpFactura()!=null){
			//JOptionPane.showMessageDialog(getControl(), "Análisis con cuenta por pagar, no se puede modificar");
			if(MessageUtils.showConfirmationMessage("Análisis con cuenta por pagar generada, seguro que desea eliminar? ", "Eliminación ")){
				getManager().eliminarAnalisis(bean);
				return true;
			}
			
		}
		return false;
	}

	public void print(){
		print((AnalisisDeTransformacion)getSelectedObject());
	}
	
	public void print(AnalisisDeTransformacion a){		
		if(a!=null){			
			final Map parameters=new HashMap();
			parameters.put("NUMERO", a.getId());
			ReportUtils.viewReport(ReportUtils.toReportesPath("cxp/AnalisisDeTransformacion.jasper"),parameters);
		}
	}
	
	public void generarCuentaPorPagar(){
		if(getSelectedObject()!=null){
			AnalisisDeTransformacion a=(AnalisisDeTransformacion)getSelectedObject();
			int index=source.indexOf(a);
			if(index!=-1){
				a=getManager().generarCuentaPorPagar(a);				
				source.set(index, a);
				JOptionPane.showMessageDialog(getControl(), "Cuenta por pagar generada: "+a.getCxpFactura().getId());
			}
		}
		
	}
	
	private AnalisisDeTransfomracionesManager getManager(){
		return ServiceLocator2.getAnalisisDeTransformacionManager();
	}
	

}
