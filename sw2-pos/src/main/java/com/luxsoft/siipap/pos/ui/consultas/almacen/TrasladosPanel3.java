package com.luxsoft.siipap.pos.ui.consultas.almacen;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.Action;

import org.apache.commons.lang.exception.ExceptionUtils;

import ca.odell.glazedlists.CollectionList;
import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.CollectionList.Model;
import ca.odell.glazedlists.gui.TableFormat;

import com.luxsoft.cfdi.CFDIPrintUI;
import com.luxsoft.siipap.inventarios.model.Movimiento;
import com.luxsoft.siipap.inventarios.model.SolicitudDeTraslado;
import com.luxsoft.siipap.inventarios.model.Traslado;
import com.luxsoft.siipap.inventarios.model.TrasladoDet;
import com.luxsoft.siipap.model.Sucursal;
import com.luxsoft.siipap.pos.POSRoles;
import com.luxsoft.siipap.pos.ui.reports.RelacionDeTPSReportForm;
import com.luxsoft.siipap.pos.ui.selectores.SelectorDeSolicitudesPendietes;
import com.luxsoft.siipap.pos.ui.utils.ReportUtils2;

import com.luxsoft.siipap.swing.browser.AbstractMasterDatailFilteredBrowserPanel;
import com.luxsoft.siipap.swing.utils.CommandUtils;
import com.luxsoft.siipap.swing.utils.MessageUtils;
import com.luxsoft.sw3.cfdi.model.CFDI;
import com.luxsoft.sw3.services.Services;
import com.luxsoft.sw3.ui.forms.MovimientoController;
import com.luxsoft.sw3.ui.forms.MovimientoDeInventarioForm;
import com.luxsoft.sw3.ui.forms.TrasladoAutomaticoController;
import com.luxsoft.sw3.ui.forms.TrasladoAutomaticoForm;
import com.luxsoft.sw3.ui.forms.TrasladoController;
import com.luxsoft.sw3.ui.forms.TrasladoForm;
import com.luxsoft.sw3.ui.services.KernellUtils;
import com.luxsoft.sw3.ventas.Pedido.ClasificacionVale;

/**
 * Panel para la atencin y mantenimiento de traslados
 * 
 * @author Ruben Cancino
 *
 */
public class TrasladosPanel3 extends AbstractMasterDatailFilteredBrowserPanel<Traslado, TrasladoDet>{

	public TrasladosPanel3() {
		super(Traslado.class);
		
	}
	
	protected void init(){		
		super.init();
		addProperty("Sucursal","tipo","documento","fecha","solicitud.documento","solicitud.origen","chofer","porInventario","comentario","solicitud.referencia","clasificacion");
		addLabels("Sucursal","Tipo","Docto","Fecha","Sol","Sucursal (Ate.)","Chofer","Por Inv","Comentario","Ref (Sol)","Clasificacion");
		installTextComponentMatcherEditor("Atendido", new String[]{"solicitud.origen.nombre"});
		installTextComponentMatcherEditor("Documento", new String[]{"documento"});
		installTextComponentMatcherEditor("Solicitud", new String[]{"solicitud.documento"});
		installTextComponentMatcherEditor("Chofer", new String[]{"chofer"});
		installTextComponentMatcherEditor("Comentario", new String[]{"comentario"});
		manejarPeriodo();
	}

	@Override
	protected TableFormat createDetailTableFormat() {
		String[] props={"traslado.documento","producto.clave","producto.descripcion","tipo","solicitado","cantidad","comentario"};
		String[] labels={"Traslado","Producto","Descripcin","Tipo","Solicitado","Cantidad","Comentario"};
		return GlazedLists.tableFormat(TrasladoDet.class, props,labels);
	}

	@Override
	protected Model<Traslado, TrasladoDet> createPartidasModel() {
		return new CollectionList.Model<Traslado, TrasladoDet>(){
			public List<TrasladoDet> getChildren(Traslado parent) {
				String hql="from TrasladoDet det where det.traslado.id=?";
				return Services.getInstance()
					.getHibernateTemplate()
					.find(hql,parent.getId());
			}
		};
	}
	
	@Override
	public Action[] getActions() {
		if(actions==null)
			actions=new Action[]{
				getLoadAction()
				,CommandUtils.createPrintAction(this, "print")
			
				};
		return actions;
	}	

	
	
	@Override
	protected List<Action> createProccessActions(){
		List<Action> actions=new ArrayList<Action>();

		return actions;
	}
	
	
	@Override
	protected List<Traslado> findData() {
		String hql="from Traslado s where " +
				" s.fecha between ? and ? and s.sucursal.id = ? and  s.tipo='TPE' ";
		Long suc=Services.getInstance().getConfiguracion().getSucursalLocalId();
		return Services.getInstance().getHibernateTemplate().find(hql
				,new Object[]{
				periodo.getFechaInicial()
				,periodo.getFechaFinal(),suc
				});
	}

	

	
	
	public void print(){
		if(getSelectedObject()!=null){
			Traslado m=(Traslado)getSelectedObject();
			Map params=new HashMap();
			params.put("TRALADO_ID", m.getId());
			ReportUtils2.runReport("invent/SalidaDeTraslado.jasper", params);
		}
	}
	


	private Traslado cargar(Traslado t){
		List<Traslado> found=Services.getInstance().getHibernateTemplate().find("from Traslado t left join fetch t.partidas where t.id=?"
				,t.getId());
		if(!found.isEmpty())
			return found.iterator().next();
		return null;
			
	}
	

	public static void main(String[] args) {

	
		
	}
		
		

}
