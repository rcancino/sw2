package com.luxsoft.siipap.pos.ui.consultas.almacen;


import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.Action;

import ca.odell.glazedlists.CollectionList;
import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.CollectionList.Model;
import ca.odell.glazedlists.gui.TableFormat;

import com.luxsoft.siipap.compras.model.DevolucionDeCompra;
import com.luxsoft.siipap.compras.model.DevolucionDeCompraDet;
import com.luxsoft.siipap.model.Configuracion;
import com.luxsoft.siipap.model.Periodo;
import com.luxsoft.siipap.model.User;
import com.luxsoft.siipap.pos.POSRoles;
import com.luxsoft.siipap.pos.ui.utils.ReportUtils2;
import com.luxsoft.siipap.swing.browser.AbstractMasterDatailFilteredBrowserPanel;
import com.luxsoft.siipap.swing.utils.CommandUtils;
import com.luxsoft.siipap.swing.utils.MessageUtils;
import com.luxsoft.sw3.services.Services;
import com.luxsoft.sw3.ui.forms.DevolucionDeCompraController;
import com.luxsoft.sw3.ui.forms.DevolucionDeCompraForm;
import com.luxsoft.sw3.ui.services.KernellUtils;

/**
 * Panel para la atención y mantenimiento de devoluciones de compras
 * 
 * @author Ruben Cancino
 *
 */
public class DevolucionesDeComprasPanel extends AbstractMasterDatailFilteredBrowserPanel<DevolucionDeCompra, DevolucionDeCompraDet>{
	
	

	public DevolucionesDeComprasPanel() {
		super(DevolucionDeCompra.class);
		
	}
	
	protected void init(){		
		super.init();
		addProperty("sucursal.nombre","documento","fecha","nombre","referencia","comentario");
		addLabels("Sucursal","Docto","Fecha","Proveedor","Referencia","Comentario");
		installTextComponentMatcherEditor("Documento", "documento");
		installTextComponentMatcherEditor("Proveedor", "nombre");
		manejarPeriodo();
		periodo=Periodo.getPeriodoDelYear(Periodo.obtenerYear(new Date()));
	}

	@Override
	protected TableFormat createDetailTableFormat() {
		String[] props={"devolucion.documento","clave","descripcion","cantidad"};
		String[] labels={"Documento","Producto","Descripción","cantidad"};
		return GlazedLists.tableFormat(DevolucionDeCompraDet.class, props,labels);
	}

	@Override
	protected Model<DevolucionDeCompra, DevolucionDeCompraDet> createPartidasModel() {
		return new CollectionList.Model<DevolucionDeCompra, DevolucionDeCompraDet>(){
			public List<DevolucionDeCompraDet> getChildren(DevolucionDeCompra parent) {
				return new ArrayList<DevolucionDeCompraDet>(parent.getPartidas());
			}
		};
	}
	
	

	@Override
	public Action[] getActions() {
		if(actions==null)
			actions=new Action[]{
				getLoadAction()
				,getInsertAction()
				,getViewAction()
				,CommandUtils.createDeleteAction(this, "cancelar")
				,CommandUtils.createPrintAction(this, "print")
				};
		return actions;
	}	

	@Override
	protected List<DevolucionDeCompra> findData() {
		String hql="from DevolucionDeCompra s where " +
				" s.sucursal.id=?" +
				" and s.fecha between ? and ? ";
		return Services.getInstance().getHibernateTemplate().find(hql
				,new Object[]{
				Configuracion.getSucursalLocalId()
				,periodo.getFechaInicial()
				,periodo.getFechaFinal()
				});
	}
	
	@Override
	protected DevolucionDeCompra doInsert() {
		final DevolucionDeCompraController controller=new DevolucionDeCompraController();
		final DevolucionDeCompraForm form=new DevolucionDeCompraForm(controller);
		form.open();
		if(!form.hasBeenCanceled()){
			DevolucionDeCompra res=controller.persistir();
			controller.dispose();
			return res;
		}
		controller.dispose();
		return null;
	}
	
	
	
	@Override
	protected void afterInsert(DevolucionDeCompra bean) {
		super.afterInsert(bean);
		if(MessageUtils.showConfirmationMessage("Imprimir DEC?", "Devolución de Compras"))
			print(bean);
		int index=source.indexOf(bean);
		if(index!=-1)
			selectionModel.setSelectionInterval(index, index);
	}

	public void cancelar() {
		DevolucionDeCompra dec=(DevolucionDeCompra)getSelectedObject();
		if(dec!=null){
			int index=source.indexOf(dec);
			if(index!=-1){
				User user=KernellUtils.buscarUsuario();
				if(user.hasRole(POSRoles.GERENTE_DE_INVENTARIOS.name())){
					if(MessageUtils.showConfirmationMessage("Seguro q desea cancelar el DEC: "+dec.getDocumento(), "Cancelación de DEC")){
						dec=Services.getInstance().getInventariosManager().cancelarDevolucionDeCompra(dec);
						source.set(index, dec);
					}
				}
				else{
					MessageUtils.showMessage("Derechos insuficientes \n Role requerido:  "+POSRoles.GERENTE_DE_INVENTARIOS
							, "Operación denegada");
				}
				
			}
			
		}
	}

	public void print(){
		for(Object o:getSelected()){
			DevolucionDeCompra dec=(DevolucionDeCompra)o;
			print(dec);	
		}
	}
	
	public void print(DevolucionDeCompra dec){
		Map params=new HashMap();
		params.put("DEVOLUCION_ID", dec.getId());
		ReportUtils2.runReport("invent/DevolucionDeCompra.jasper", params);
	}

}
