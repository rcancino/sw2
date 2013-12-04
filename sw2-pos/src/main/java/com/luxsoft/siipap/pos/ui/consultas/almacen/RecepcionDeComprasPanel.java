package com.luxsoft.siipap.pos.ui.consultas.almacen;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.Action;

import ca.odell.glazedlists.CollectionList;
import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.CollectionList.Model;
import ca.odell.glazedlists.gui.TableFormat;

import com.luxsoft.siipap.compras.model.EntradaPorCompra;
import com.luxsoft.siipap.compras.model.RecepcionDeCompra;
import com.luxsoft.siipap.model.Sucursal;
import com.luxsoft.siipap.pos.ui.utils.ReportUtils2;
import com.luxsoft.siipap.swing.browser.AbstractMasterDatailFilteredBrowserPanel;
import com.luxsoft.siipap.swing.dialog.SelectorDeFecha;
import com.luxsoft.siipap.swing.utils.CommandUtils;
import com.luxsoft.siipap.swing.utils.MessageUtils;
import com.luxsoft.sw3.services.Services;


public class RecepcionDeComprasPanel extends AbstractMasterDatailFilteredBrowserPanel<RecepcionDeCompra, EntradaPorCompra>{

	public RecepcionDeComprasPanel() {
		super(RecepcionDeCompra.class);
	}
	
	
	@Override
	protected void agregarMasterProperties(){
		addProperty("documento","fecha","compra.proveedor.nombre","Compra.folio","compra.fecha");
		addLabels("Docto","Fecha","Proveedor","Compra","Fecha C");
		installTextComponentMatcherEditor("Com", "documento");
		installTextComponentMatcherEditor("Proveedor", "compra.proveedor.nombre");
	}



	@Override
	protected TableFormat createDetailTableFormat() {
		return GlazedLists.tableFormat(
				EntradaPorCompra.class
				,new String[]{"sucursal.nombre","documento","fecha","clave","descripcion","compraDet.solicitado","cantidad","compraDet.sucursalNombre"}
				,new String[]{"Sucursal","Docto","Fecha","Prod","Desc","Solicitado","Recibido","Suc (Compra)"}
				);
		
	}

	@Override
	protected Model<RecepcionDeCompra, EntradaPorCompra> createPartidasModel() {
		return new CollectionList.Model<RecepcionDeCompra, EntradaPorCompra>(){
			public List<EntradaPorCompra> getChildren(RecepcionDeCompra parent) {
				String hql="from EntradaPorCompra e where e.recepcion.id=?";
				return Services.getInstance().getHibernateTemplate().find(hql, parent.getId());
			}
			
		};
	}


	@Override
	protected List<RecepcionDeCompra> findData() {
		String hql="from RecepcionDeCompra r " +
				" left join fetch r.compra c" +
				" left join fetch c.proveedor p";
		return Services.getInstance().getHibernateTemplate().find(hql);
	}


	@Override
	public Action[] getActions() {
		if(actions==null)
			actions=new Action[]{
				getLoadAction()
				//,getInsertAction()
				//,getDeleteAction()
				//,getEditAction()
				,getViewAction()
				,CommandUtils.createPrintAction(this, "imprimir")
				,addAction("", "entradasPorDia", "Entradas por día")
				};
		return actions;
	}

	
	public void imprimir(){
		RecepcionDeCompra rec=(RecepcionDeCompra)getSelectedObject();
		if(rec!=null){
			Sucursal suc=Services.getInstance().getConfiguracion().getSucursal();
			final Map parameters=new HashMap();
			parameters.put("ENTRADA", rec.getId());
			parameters.put("SUCURSAL", String.valueOf(suc.getId()));
			ReportUtils2.runReport("compras/EntradaPorCompra.jasper", parameters);
		}
	}
	
	public void entradasPorDia(){
		Sucursal suc=Services.getInstance().getConfiguracion().getSucursal();
		final Map parameters=new HashMap();
		Date fecha=SelectorDeFecha.seleccionar();
		parameters.put("FECHA_ENT", fecha);
		parameters.put("SUCURSAL", String.valueOf(suc.getId()));
		ReportUtils2.runReport("compras/RecepDeMercancia.jasper", parameters);
	}
	

}
