package com.luxsoft.siipap.pos.ui.consultas.almacen;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.Action;

import ca.odell.glazedlists.CollectionList;
import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.CollectionList.Model;
import ca.odell.glazedlists.gui.TableFormat;

import com.luxsoft.siipap.model.Sucursal;
import com.luxsoft.siipap.pos.ui.utils.ReportUtils2;
import com.luxsoft.siipap.pos.ui.venta.forms.DevolucionController;
import com.luxsoft.siipap.pos.ui.venta.forms.DevolucionForm;
import com.luxsoft.siipap.swing.browser.AbstractMasterDatailFilteredBrowserPanel;
import com.luxsoft.siipap.swing.utils.CommandUtils;
import com.luxsoft.siipap.ventas.model.Devolucion;
import com.luxsoft.siipap.ventas.model.DevolucionDeVenta;
import com.luxsoft.sw3.services.Services;

public class DevolucionesPanel extends AbstractMasterDatailFilteredBrowserPanel<Devolucion, DevolucionDeVenta>{

	private Sucursal sucursal;
	
	public DevolucionesPanel() {
		super(Devolucion.class);
	}
	
	protected void agregarMasterProperties(){
		//sucursal=Services.getInstance().getConfiguracion().getSucursal();
		addProperty("numero","fecha","venta.documento","venta.origen","venta.fecha","comentario");
		addLabels("Documento","Fecha","Venta","Tipo","Fecha (F)","Comentario");
		manejarPeriodo();
	}

	@Override
	protected TableFormat createDetailTableFormat() {
		String[] props={"documento","fecha","clave","descripcion","ventaDet.cantidad","cantidad"};
		String[] labels={"Docto","Fecha","Prod","Desc","Vendido","Devuelto"};
		return GlazedLists.tableFormat(DevolucionDeVenta.class, props,labels);
	}
	
	@Override
	public Action[] getActions() {
		if(actions==null)
			actions=new Action[]{
				getLoadAction()
				,getInsertAction()
				//,getDeleteAction()
				,CommandUtils.createPrintAction(this, "imprimir")
				//,addAction("", "imprimir", "Imprimir")
				};
		return actions;
	}

	@Override
	protected Model<Devolucion, DevolucionDeVenta> createPartidasModel() {
		return new CollectionList.Model<Devolucion, DevolucionDeVenta>(){
			public List<DevolucionDeVenta> getChildren(Devolucion parent) {
				String hql="from DevolucionDeVenta d left join fetch d.ventaDet vd where d.devolucion.id=?";
				return Services.getInstance().getHibernateTemplate().find(hql,parent.getId());
			}
		};
	}

	@Override
	protected List<Devolucion> findData() {
		if(sucursal==null)
			sucursal=Services.getInstance().getConfiguracion().getSucursal();
		String hql="from Devolucion d left join fetch d.venta v " +
				" where v.sucursal.id=? and d.fecha between ? and ?" ;
		return Services.getInstance().getHibernateTemplate()
		.find(hql, new Object[]{sucursal.getId(),periodo.getFechaInicial(),periodo.getFechaFinal()});
	}

	@Override
	protected Devolucion doInsert() {
		DevolucionController controller=new DevolucionController();
		controller.setSucursal(sucursal);
		DevolucionForm form=new DevolucionForm(controller);
		form.open();
		if(!form.hasBeenCanceled()){
			Devolucion res= controller.persist();
			
			List<Devolucion> data=Services.getInstance()
				.getHibernateTemplate()
				.find("from Devolucion d left join fetch d.venta v where d.id=?",res.getId());
			return data.get(0);
		}
		return null;
	}

	public void imprimir(){
		Devolucion d=(Devolucion)getSelectedObject();
		if(d!=null){
			Sucursal suc=Services.getInstance().getConfiguracion().getSucursal();
			final Map parameters=new HashMap();
			parameters.put("DEVOLUCION", d.getId());
			parameters.put("SUCURSAL", String.valueOf(suc.getId()));
			ReportUtils2.runReport("invent/Devoluciones.jasper", parameters);
		}
	}
	

}
