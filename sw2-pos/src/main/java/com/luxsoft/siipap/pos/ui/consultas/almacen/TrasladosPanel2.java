package com.luxsoft.siipap.pos.ui.consultas.almacen;


import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.Action;

import ca.odell.glazedlists.CollectionList;
import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.CollectionList.Model;
import ca.odell.glazedlists.gui.TableFormat;

import com.luxsoft.siipap.inventarios.model.SolicitudDeTraslado;
import com.luxsoft.siipap.inventarios.model.Traslado;
import com.luxsoft.siipap.inventarios.model.TrasladoDet;
import com.luxsoft.siipap.pos.POSRoles;
import com.luxsoft.siipap.pos.ui.selectores.SelectorDeSolicitudesPendietes;
import com.luxsoft.siipap.pos.ui.utils.ReportUtils2;
import com.luxsoft.siipap.swing.browser.AbstractMasterDatailFilteredBrowserPanel;
import com.luxsoft.siipap.swing.utils.CommandUtils;
import com.luxsoft.siipap.swing.utils.MessageUtils;
import com.luxsoft.sw3.services.Services;
import com.luxsoft.sw3.ui.forms.TrasladoController;
import com.luxsoft.sw3.ui.forms.TrasladoForm;

/**
 * Panel para la atención y mantenimiento de traslados
 * 
 * @author Ruben Cancino
 *
 */
public class TrasladosPanel2 extends AbstractMasterDatailFilteredBrowserPanel<Traslado, TrasladoDet>{

	public TrasladosPanel2() {
		super(Traslado.class);
		
	}
	
	protected void init(){		
		super.init();
		addProperty("Sucursal","tipo","documento","fecha","solicitud.documento","solicitud.sucursal","chofer","porInventario","comentario","solicitud.referencia");
		addLabels("sucursal.nombre","Tipo","Docto","Fecha","Sol","Sucursal (SOL)","Chofer","Por Inv","Comentario","Ref (Sol)");
		installTextComponentMatcherEditor("Solicitante", new String[]{"sucursal.nombre"});
		installTextComponentMatcherEditor("Tipo", new String[]{"tipo"});
		installTextComponentMatcherEditor("Documento", new String[]{"documento"});
		installTextComponentMatcherEditor("Solicitud", new String[]{"solicitud.documento"});
		installTextComponentMatcherEditor("Chofer", new String[]{"chofer"});
		installTextComponentMatcherEditor("Comentario", new String[]{"comentario"});
		manejarPeriodo();
	}

	@Override
	protected TableFormat createDetailTableFormat() {
		String[] props={"traslado.documento","producto.clave","producto.descripcion","tipo","solicitado","cantidad","comentario"};
		String[] labels={"Traslado","Producto","Descripción","Tipo","Solicitado","Cantidad","Comentario"};
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
	
	public void atender(){
		List<SolicitudDeTraslado> sols=SelectorDeSolicitudesPendietes.seleccionar();
		if(!sols.isEmpty()){
			SolicitudDeTraslado sol=sols.get(0);
			TrasladoController controller=new TrasladoController(sol);
			TrasladoForm form=new TrasladoForm(controller);
			form.open();
			if(!form.hasBeenCanceled() && (!controller.isReadOnly())){
				Traslado[] res=controller.persistir();
				if(res!=null){
					for(Traslado t:res){
						source.add(t);
					}
					if(MessageUtils.showConfirmationMessage("Imprimir salida"	, "Solicitud de traslado")){
						print();
					}
				}
			}
		}
	}

	@Override
	public Action[] getActions() {
		if(actions==null)
			actions=new Action[]{
				getLoadAction()
				,addRoleBasedContextAction(null,POSRoles.CONTROLADOR_DE_INVENTARIOS.name(), this, "atender", "Atender Sol.")
				//,getViewAction()
				,CommandUtils.createPrintAction(this, "print")
				};
		return actions;
	}	

	@Override
	protected List<Traslado> findData() {
		String hql="from Traslado s where " +
				" s.fecha between ? and ? ";
		//Sucursal suc=Services.getInstance().getConfiguracion().getSucursal();
		return Services.getInstance().getHibernateTemplate().find(hql
				,new Object[]{
				periodo.getFechaInicial()
				,periodo.getFechaFinal()
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
	
	

}
