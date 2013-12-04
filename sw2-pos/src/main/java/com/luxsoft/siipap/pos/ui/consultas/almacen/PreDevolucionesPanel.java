package com.luxsoft.siipap.pos.ui.consultas.almacen;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.Action;
import javax.swing.JOptionPane;

import ca.odell.glazedlists.CollectionList;
import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.CollectionList.Model;
import ca.odell.glazedlists.gui.TableFormat;

import com.luxsoft.siipap.model.Sucursal;
import com.luxsoft.siipap.pos.POSRoles;
import com.luxsoft.siipap.pos.ui.utils.ReportUtils2;
import com.luxsoft.siipap.pos.ui.venta.forms.DevolucionController;
import com.luxsoft.siipap.pos.ui.venta.forms.DevolucionForm;
import com.luxsoft.siipap.pos.ui.venta.forms.PreDevolucionController;
import com.luxsoft.siipap.pos.ui.venta.forms.PreDevolucionForm;
import com.luxsoft.siipap.swing.browser.AbstractMasterDatailFilteredBrowserPanel;
import com.luxsoft.siipap.swing.utils.CommandUtils;
import com.luxsoft.siipap.swing.utils.MessageUtils;
import com.luxsoft.siipap.ventas.model.Devolucion;
import com.luxsoft.siipap.ventas.model.PreDevolucion;
import com.luxsoft.siipap.ventas.model.PreDevolucionDet;
import com.luxsoft.sw3.services.Services;
import com.luxsoft.sw3.ui.services.KernellUtils;

public class PreDevolucionesPanel extends AbstractMasterDatailFilteredBrowserPanel<PreDevolucion, PreDevolucionDet>{

	private Sucursal sucursal;
	
	public PreDevolucionesPanel() {
		super(PreDevolucion.class);
	}
	
	protected void agregarMasterProperties(){
		//sucursal=Services.getInstance().getConfiguracion().getSucursal();
		addProperty("sucursal.nombre","fecha","documento"
				,"cliente.clave","cliente.nombre","chofer","total","devolucion.numero","comentario");
		addLabels("Sucursal","Fecha","Documento","Clave","Nombre","Chofer","Total","RMD","Comentario");
		installTextComponentMatcherEditor("Documento", "documento");
		installTextComponentMatcherEditor("Cliente", "cliente.clave","cliente.nombre");
		installTextComponentMatcherEditor("Chofer", "chofer");
		manejarPeriodo();
	}

	@Override
	protected TableFormat createDetailTableFormat() {
		String[] props={"preDevolucion.documento","renglon","clave","descripcion","cantidad"};
		String[] labels={"Docto","Producto","Descripción","Devuelto"};
		return GlazedLists.tableFormat(PreDevolucionDet.class, props,labels);
	}
	
	@Override
	public Action[] getActions() {
		if(actions==null)
			actions=new Action[]{
				getLoadAction()
				,getInsertAction()
				,getDeleteAction()
				,CommandUtils.createPrintAction(this, "imprimir")
				,addAction("", "generarDevolucion", "Generar RMD")
				};
		return actions;
	}

	@Override
	protected Model<PreDevolucion, PreDevolucionDet> createPartidasModel() {
		return new CollectionList.Model<PreDevolucion, PreDevolucionDet>(){
			public List<PreDevolucionDet> getChildren(PreDevolucion parent) {
				String hql="from PreDevolucionDet d left join fetch d.ventaDet vd where d.preDevolucion.id=?";
				return Services.getInstance().getHibernateTemplate().find(hql,parent.getId());
			}
		};
	}

	@Override
	protected List<PreDevolucion> findData() {
		if(sucursal==null)
			sucursal=Services.getInstance().getConfiguracion().getSucursal();
		String hql="from PreDevolucion d " +
				" where d.sucursal.id=? " +
				" and d.fecha between ? and ?" ;
		return Services.getInstance().getHibernateTemplate()
		.find(hql, new Object[]{sucursal.getId(),periodo.getFechaInicial(),periodo.getFechaFinal()});
	}

	@Override
	protected PreDevolucion doInsert() {
		PreDevolucionController controller=new PreDevolucionController();
		PreDevolucionForm form=new PreDevolucionForm(controller);
		form.open();
		if(!form.hasBeenCanceled()){
			PreDevolucion res= controller.persist();
			
			List<PreDevolucion> data=Services.getInstance()
				.getHibernateTemplate()
				.find("from PreDevolucion d  where d.id=?",res.getId());
			return data.get(0);
		}
		return null;
	}

	public void imprimir(){
		PreDevolucion d=(PreDevolucion)getSelectedObject();
		if(d!=null){
			Sucursal suc=Services.getInstance().getConfiguracion().getSucursal();
			final Map parameters=new HashMap();
			parameters.put("PREDEVOLUCION", d.getId());
			parameters.put("SUCURSAL", String.valueOf(suc.getId()));
			ReportUtils2.runReport("invent/PreDevoluciones.jasper", parameters);
		}
	}

	@Override
	public boolean doDelete(PreDevolucion bean) {
		if(bean.getDevolucion()!=null){
			MessageUtils.showMessage("Ya esta generado el RMD no se puede eliminar", "Pre Devoluciones");
			return false;
		}
		try {
			if(KernellUtils.validarAcceso(POSRoles.CONTROLADOR_DE_INVENTARIOS.name())){
				Services.getInstance().getUniversalDao().remove(PreDevolucion.class, bean.getId());
				return true;
			}else
				return false;
			
		} catch (Exception e) {
			logger.error(e);
			return false;
		}
	}

	public void generarDevolucion(){
		PreDevolucion devo=(PreDevolucion)getSelectedObject();
		if(devo.getDevolucion()!=null){
			MessageUtils.showMessage("Ya esta generado el RMD no se puede eliminar", "Pre Devoluciones");
			return ;
		}
		if(devo!=null){
			Date fecha=Services.getInstance().obtenerFechaDelSistema();
			Devolucion rmd=Services.getInstance().getInventariosManager().generarDevolucion(devo,fecha);
			MessageUtils.showMessage("RMD Generado: "+rmd.getNumero(), "Pre Devolucionees");
			
		}
	}
	
	public static void main(String[] args) {
		String id="8a8a81b5-27c0d329-0127-c1575c5c-0003";
		
		PreDevolucion pre=(PreDevolucion) Services.getInstance()
			.getHibernateTemplate()
			.find("from PreDevolucion p left join fetch p.partidas pa where p.id=?",id).get(0);
		//(PreDevolucion)Services.getInstance().getUniversalDao().get(PreDevolucion.class, id);
		Devolucion rmd=Services.getInstance().getInventariosManager().generarDevolucion(pre,new Date());
		System.out.println("RMD generado: "+rmd.getId());
	}

}
