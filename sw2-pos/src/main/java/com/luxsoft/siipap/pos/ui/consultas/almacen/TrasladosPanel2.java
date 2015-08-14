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
import com.luxsoft.siipap.pos.ui.reports.RelacionTPSTimbrarReportForm;
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
public class TrasladosPanel2 extends AbstractMasterDatailFilteredBrowserPanel<Traslado, TrasladoDet>{

	public TrasladosPanel2() {
		super(Traslado.class);
		
	}
	
	protected void init(){		
		super.init();
		addProperty("Sucursal","tipo","documento","fecha","solicitud.documento","solicitud.sucursal","chofer","porInventario","comentario","solicitud.referencia","cfdi","clasificacion");
		addLabels("sucursal.nombre","Tipo","Docto","Fecha","Sol","Sucursal (SOL)","Chofer","Por Inv","Comentario","Ref (Sol)","CFDI","Clasificacion");
		installTextComponentMatcherEditor("Solicitante", new String[]{"solicitud.sucursal.nombre"});
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
				,addRoleBasedContextAction(null,POSRoles.CONTROLADOR_DE_INVENTARIOS.name(), this, "atender", "Atender Sol.")
				//,addRoleBasedContextAction(null,POSRoles.CONTROLADOR_DE_INVENTARIOS.name(), this, "generarCfdi", "Generar CFDI")
				
				//,getViewAction()
				,CommandUtils.createPrintAction(this, "print")
				,addAction(null, "imprimirTPE","Imprimir TPE")
			//	,addAction(null, "imprimirRelacionTPS", "Imprimir Relacion TPS")
				,addAction(null, "imprimirRelacionTPS","Imprimir Relacin TPS")
				,addAction(null, "imprimirRelacionTPSPendientes","TPS Sin Timbrar")
				//,addRoleBasedContextAction(null,POSRoles.CONTROLADOR_DE_INVENTARIOS.name(), this, "timbrar", "Timbrar CFDI")
				,addAction(null, "printCfdi", "Imprimir CFDI")
				//,getEditAction()
				};
		return actions;
	}	

	
	
	@Override
	protected List<Action> createProccessActions(){
		List<Action> actions=new ArrayList<Action>();
		actions.add(addAction("","generarCfdi", "Generar CFDI"));
		actions.add(addAction("",  "timbrar", "Timbrar CFDI"));
		return actions;
	}
	
	
	@Override
	protected List<Traslado> findData() {
		String hql="from Traslado s where " +
				" s.fecha between ? and ? and s.sucursal.id = ? and  s.tipo='TPS' ";
		Long suc=Services.getInstance().getConfiguracion().getSucursalLocalId();
		return Services.getInstance().getHibernateTemplate().find(hql
				,new Object[]{
				periodo.getFechaInicial()
				,periodo.getFechaFinal(),suc
				});
	}
	
	@Override
	protected Traslado doEdit(Traslado bean) {
		
		if(getSelectedObject()!=null){
			Traslado target=(Traslado)getSelectedObject();
			//target=(Traslado)Services.getInstance().getHibernateTemplate().get(Traslado.class, target.getId());
			TrasladoAutomaticoController controller=new TrasladoAutomaticoController(target);
			TrasladoAutomaticoForm form=new TrasladoAutomaticoForm(controller);
			form.open();
			if(!form.hasBeenCanceled()){
				return controller.persistir();
			}
			return bean;
		}
			return bean;
		
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
						if(t.getTipo().equals("TPS")){
							try {
								timbrar(t);
								source.add(cargar(t));
							} catch (Exception e) {
								MessageUtils.showMessage("Error timbrando CFDI\n "+ExceptionUtils.getRootCauseMessage(e), "CFDI");
								e.printStackTrace();
								load();
							}
							
							
						}
						else
							source.add(t);
					}
					if(MessageUtils.showConfirmationMessage("Imprimir salida"	, "Solicitud de traslado")){
						print();
					}
				}
			}
		}
	}
	
	public void generarCfdi(){
		if(getSelectedObject()!=null){
			Traslado m=(Traslado)getSelectedObject();
			if(m.getTipo().equals("TPS") && (m.getCfdi()==null)){
				
					
				if(m.getClasificacion().equals(ClasificacionVale.ENVIA_SUCURSAL.toString()) || m.getClasificacion().equals(ClasificacionVale.RECOGE_CLIENTE.toString())){
					
					
					TrasladoAutomaticoController controller=new TrasladoAutomaticoController(m);
					TrasladoAutomaticoForm form=new TrasladoAutomaticoForm(controller);
					form.open();
					if(!form.hasBeenCanceled()){
						 m=controller. persistir();
					}
					
				}
				int index=source.indexOf(m);
				
				/**Marca el error de origen id duplicado al generar el vale**/
				//CFDI cfdi=Services.getCFDITraslado().generar(m);
				//timbrar(m);
				if(index!=-1){
					m=(Traslado)Services.getInstance().getHibernateTemplate().get(Traslado.class, m.getId());
					source.set(index, m);
				}
				MessageUtils.showMessage("CFDI generado","CFDI");
				
			}else{
				MessageUtils.showMessage("CFDI solo se puede generar para TPS ", "CFDI ");
			}
		}
	}

	
	
	public void print(){
		if(getSelectedObject()!=null){
			Traslado m=(Traslado)getSelectedObject();
			Map params=new HashMap();
			params.put("TRALADO_ID", m.getId());
			ReportUtils2.runReport("invent/SalidaDeTraslado.jasper", params);
		}
	}
	

	public void imprimirTPE(){
		if(getSelectedObject()!=null){
			Traslado m=(Traslado)getSelectedObject();
			Map params=new HashMap();
			params.put("SOL_ID", m.getSolicitud().getId());
			ReportUtils2.runReport("invent/EntradaTraslado.jasper", params);
		}
	}
	
	
	
	
	public void printCfdi(){
		if(getSelectedObject()!=null){
			Traslado m=(Traslado)getSelectedObject();
			if(m.getTipo().equals("TPS") && (m.getCfdi()!=null)){
				
				CFDI cfdi=Services.getCFDIManager().getCFDI(m.getCfdi());
				if(cfdi.getTimbrado()==null){
					MessageUtils.showMessage("El cfdi no se ha timbrado", "CFDI");
					return;
				}else{
					CFDIPrintUI.impripirComprobante(m,cfdi, true);
				}
				
			}
		}
	}
	
	public void printCfdi(Traslado tps,CFDI cfdi){
		if(cfdi.getTimbrado()==null){
			MessageUtils.showMessage("El cfdi no se ha timbrado", "CFDI");
			return;
		}else{
			CFDIPrintUI.impripirComprobante(cargar(tps),cfdi, true);
		}
	}
	
	public void timbrar(){
		
		if(getSelectedObject()!=null){
			Traslado m=(Traslado)getSelectedObject();
			timbrar(m);
		}	
	}
	
	public void timbrar(Traslado m){
		if(m.getTipo().equals("TPS") && (m.getCfdi()!=null)){
			CFDI cfdi=Services.getCFDIManager().getCFDI(m.getCfdi());
			if(cfdi!=null){
				if(cfdi.getUUID()!=null){
					MessageUtils.showMessage("CFDI ya timbrado: "+cfdi.getTimbreFiscal().getUUID(), "CFDI");
					return;
				}
				int index=source.indexOf(m);
				try {
					logger.info("Timbrando CFDI: "+cfdi);
					cfdi=Services.getCFDIManager().timbrar(cfdi);
					if(index!=-1){
						source.set(index, m);
					}
					printCfdi(m,cfdi);
				} catch (Exception e) {
					e.printStackTrace();
					MessageUtils.showMessage(ExceptionUtils.getRootCauseMessage(e), "Timbrado de CFDI");
					return;
				}
			}
		}	
	}
	
	private Traslado cargar(Traslado t){
		List<Traslado> found=Services.getInstance().getHibernateTemplate().find("from Traslado t left join fetch t.partidas where t.id=?"
				,t.getId());
		if(!found.isEmpty())
			return found.iterator().next();
		return null;
			
	}
	
	public void imprimirRelacionTPS(){
		RelacionDeTPSReportForm report=new RelacionDeTPSReportForm();
		report.run();
		
	}
	
	public void imprimirRelacionTPSPendientes(){
		 RelacionTPSTimbrarReportForm report=new RelacionTPSTimbrarReportForm();
		report.run();
		
	}
	
	public static void main(String[] args) {
	/*	CFDI cfdi=Services.getCFDIManager().getCFDI("8a8a8161-44034e57-0144-0350cbc2-0009");
		List<Traslado> found=Services.getInstance().getHibernateTemplate().find("from Traslado t left join fetch t.partidas where t.id=?"
				,"8a8a8161-44034e57-0144-035088b9-0005");
		for(Traslado tps :found){
			CFDIPrintUI.impripirComprobante(tps,cfdi);
		}
	*/
		RelacionDeTPSReportForm report=new RelacionDeTPSReportForm();
		report.run();
		
	}
		
		

}
