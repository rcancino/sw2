package com.luxsoft.siipap.pos.ui.consultas.almacen;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.Action;

import ca.odell.glazedlists.CollectionList;
import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.CollectionList.Model;
import ca.odell.glazedlists.gui.TableFormat;

import com.luxsoft.siipap.inventarios.model.SolicitudDeTraslado;
import com.luxsoft.siipap.inventarios.model.SolicitudDeTrasladoDet;
import com.luxsoft.siipap.inventarios.model.Traslado;
import com.luxsoft.siipap.model.Sucursal;
import com.luxsoft.siipap.model.User;
import com.luxsoft.siipap.pos.POSRoles;
import com.luxsoft.siipap.pos.ui.reports.KardexReportForm;
import com.luxsoft.siipap.pos.ui.reports.SolicitudesPendientesReportForm;
import com.luxsoft.siipap.pos.ui.utils.ReportUtils2;
import com.luxsoft.siipap.security.SeleccionDeUsuario;
import com.luxsoft.siipap.swing.browser.AbstractMasterDatailFilteredBrowserPanel;
import com.luxsoft.siipap.swing.utils.CommandUtils;
import com.luxsoft.siipap.swing.utils.MessageUtils;
import com.luxsoft.sw3.services.Services;
import com.luxsoft.sw3.ui.forms.SolicitudDeTrasladoController;
import com.luxsoft.sw3.ui.forms.SolicitudDeTrasladoForm;

/**
 * Panel para el mantenimiento de traslados
 * 
 * @author Ruben Cancino
 *
 */
public class SolicitudDeTrasladosPanel extends AbstractMasterDatailFilteredBrowserPanel<SolicitudDeTraslado, SolicitudDeTrasladoDet>{

	public SolicitudDeTrasladosPanel() {
		super(SolicitudDeTraslado.class);
		
	}
	
	protected void init(){		
		super.init();
		addProperty("documento","fecha","sucursal.nombre","atendido","comentario","referencia");
		addLabels("Docto","Fecha","Origen","Atendido","Comentario","Referencia");
		installTextComponentMatcherEditor("Origen", new String[]{"sucursal.nombre"});
		installTextComponentMatcherEditor("Documento", new String[]{"documento"});
		installTextComponentMatcherEditor("Atendido", new String[]{"atendido"});
		installTextComponentMatcherEditor("Referencia", new String[]{"referencia"});
		setDefaultComparator(GlazedLists.beanPropertyComparator(SolicitudDeTraslado.class, "documento"));
		manejarPeriodo();
	}

	@Override
	protected TableFormat createDetailTableFormat() {
		String[] props={"solicitud.documento","producto.clave","producto.descripcion","solicitado","recibido","comentario"};
		String[] labels={"Sol","Producto","Descripcin","Solicitado","Recibido","Comentario"};
		return GlazedLists.tableFormat(SolicitudDeTrasladoDet.class, props,labels);
	}

	@Override
	protected Model<SolicitudDeTraslado, SolicitudDeTrasladoDet> createPartidasModel() {
		return new CollectionList.Model<SolicitudDeTraslado, SolicitudDeTrasladoDet>(){
			public List<SolicitudDeTrasladoDet> getChildren(SolicitudDeTraslado parent) {
				//String hql="select s.from SolicitudDeTrasladoDet det where det.solicitud.id=?";
				//Services.getInstance().getHibernateTemplate().initialize(parent.getPartidas());
				return parent.getPartidas();
			}
		};
	}

	@Override
	protected SolicitudDeTraslado doInsert() {
		SolicitudDeTrasladoController controller=new SolicitudDeTrasladoController();
		SolicitudDeTrasladoForm form=new SolicitudDeTrasladoForm(controller);
		form.open();		
		if(!form.hasBeenCanceled()){
			SolicitudDeTraslado res= controller.persist();
			return res;
		}
		return null;
	}
	
	@Override
	protected void afterInsert(SolicitudDeTraslado bean) {
		if(MessageUtils.showConfirmationMessage("Imprimir la solicitud"
				, "Solicitud de traslado")){
			print(bean);
		}
	}

	
	

	@Override
	public Action[] getActions() {
		if(actions==null)
			actions=new Action[]{
				getLoadAction()
				,getInsertAction()
				,getDeleteAction()
				//,getEditAction()
				,getViewAction()
				,CommandUtils.createPrintAction(this, "print")
				};
		return actions;
	}	

	protected List<Action> createProccessActions(){
		List<Action> actions=new ArrayList<Action>();
		actions.add(addAction("", "reporteDeSolicitudesPendientes", "Solicitudes Pendientes"));
		actions.add(addAction("", "marcarNoAtender", "Marcar No Atender"));
		return actions;
	}
	
	public void reporteDeSolicitudesPendientes(){
		SolicitudesPendientesReportForm.run();
	}
	
	@Override
	protected List<SolicitudDeTraslado> findData() {
		String hql="from SolicitudDeTraslado s where " +
				" s.sucursal.id=? " +
				" and s.fecha between ? and ?   ";
		Sucursal suc=Services.getInstance().getConfiguracion().getSucursal();
		return Services.getInstance().getHibernateTemplate().find(hql
				,new Object[]{suc.getId()
				,periodo.getFechaInicial()
				,periodo.getFechaFinal()
				});
	}
	
	public void print(){
		if(getSelectedObject()!=null){
			SolicitudDeTraslado m=(SolicitudDeTraslado)getSelectedObject();
			Map params=new HashMap();
			params.put("SOL_ID", m.getId());
			ReportUtils2.runReport("invent/SolicitudDeTraslado.jasper", params);
		}
	}
	
	public void print(SolicitudDeTraslado sol){
		Map params=new HashMap();
		params.put("SOL_ID", sol.getId());
		ReportUtils2.runReport("invent/SolicitudDeTraslado.jasper", params);
	}
	
	public void marcarNoAtender(){
		if(getSelectedObject()!=null ){
			SolicitudDeTraslado m=(SolicitudDeTraslado)getSelectedObject();
			List<Traslado> t=Services.getInstance().getHibernateTemplate().find("from Traslado t where t.solicitud.id=? ",new Object[]{m.getId()});
			if(t.isEmpty()){
				if(!m.isnoAtender()){
					User user=SeleccionDeUsuario.findUser(Services.getInstance().getHibernateTemplate());
					if((user!=null) && user.hasRole(POSRoles.VENDEDOR.name())){
						
							m.setnoAtender(true);
							m.setUsrCancelacion(user.getUsername());
							//m.getLog().setUpdateUser(user.getFirstName());
							m=(SolicitudDeTraslado)Services.getInstance().getHibernateTemplate().merge(m);
							MessageUtils.showMessage("Se cancelo la Solicitud"+m.getDocumento(),"Cancelacin de Vale");
						
								
					}else{
						MessageUtils.showMessage("No tiene los derechos apropiados", "Ventas");
					}
				}else{
					MessageUtils.showMessage("La solicitud ya ha sido cancelada", "Ventas");
				}
				
			}else {
				MessageUtils.showMessage("El traslado ya ha sido atendido", "Ventas");
			}
			
		}
	}
	
	
	

}
