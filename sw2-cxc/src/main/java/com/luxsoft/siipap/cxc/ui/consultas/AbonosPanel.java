package com.luxsoft.siipap.cxc.ui.consultas;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.swing.Action;
import javax.swing.JOptionPane;
import javax.swing.JTextField;


import org.apache.commons.lang.exception.ExceptionUtils;

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.FilterList;
import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.TextFilterator;
import ca.odell.glazedlists.CollectionList.Model;
import ca.odell.glazedlists.gui.TableFormat;
import ca.odell.glazedlists.matchers.CompositeMatcherEditor;
import ca.odell.glazedlists.matchers.Matcher;
import ca.odell.glazedlists.matchers.MatcherEditor;
import ca.odell.glazedlists.swing.TextComponentMatcherEditor;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.luxsoft.siipap.cxc.CXCActions;
import com.luxsoft.siipap.cxc.CXCRoles;
import com.luxsoft.siipap.cxc.model.Abono;
import com.luxsoft.siipap.cxc.model.Aplicacion;
import com.luxsoft.siipap.cxc.model.OrigenDeOperacion;
import com.luxsoft.siipap.cxc.model.Pago;
import com.luxsoft.siipap.cxc.service.CXCManager;
import com.luxsoft.siipap.cxc.ui.CXCUIServiceFacade;
import com.luxsoft.siipap.service.ServiceLocator2;
import com.luxsoft.siipap.swing.browser.AbstractMasterDatailFilteredBrowserPanel;
import com.luxsoft.siipap.swing.matchers.CheckBoxMatcher;
import com.luxsoft.siipap.swing.utils.MessageUtils;
import com.luxsoft.sw3.cxc.forms.SolicitudDeDepositoForm;
import com.luxsoft.sw3.cxc.selectores.SelectorDeSolicitudDeDepositos;
import com.luxsoft.sw3.tesoreria.model.SolicitudDeDeposito;


/**
 * Browser para el mantenimiento, control y consulta de
 * los pagos aplicados 
 * 
 * @author Ruben Cancino 
 *
 */
public class AbonosPanel extends AbstractMasterDatailFilteredBrowserPanel<Abono, Aplicacion>{
	
	
	private OrigenDeOperacion origen=OrigenDeOperacion.CRE;

	public AbonosPanel() {
		super(Abono.class);		
	}
	
	protected void init(){
		
		setDefaultComparator(GlazedLists.beanPropertyComparator(Abono.class, "fecha"));
		
		addProperty("nombre","fecha","sucursal.nombre","origen","tipo","folio"
				,"total","aplicado","diferencia","disponibleCalculado","liberado","info","comentario");
		addLabels("Cliente","Fecha","Sucursal","Origen","Tipo","Folio"
				,"Total","Aplicado","Otros Prod","Disponible","Liberado","Info","Comentario");
		installTextComponentMatcherEditor("Cliente", "nombre","clave");
		installTextComponentMatcherEditor("Sucursal","sucursal.nombre");
		installTextComponentMatcherEditor("Folio","folio");
		installTextComponentMatcherEditor("Tipo","tipo");
		installTextComponentMatcherEditor("Desc Tipo","tipo");
		CheckBoxMatcher<Abono> canceladosMatcher=new CheckBoxMatcher<Abono>(){
			protected Matcher<Abono> getSelectMatcher(Object... obj) {				
				return new Matcher<Abono>(){
					public boolean matches(Abono item) {
						return (item.getTotal().doubleValue()!=0);
					}
					
				};
			}
		};
		installCustomMatcherEditor("Canceladas", canceladosMatcher.getBox(), canceladosMatcher);
		
		manejarPeriodo();
		setDetailTitle("Aplicaciones");
	}
	
	@Override
	protected TableFormat createDetailTableFormat() {
		String[] props=new String[]{"detalle.formaDePago","detalle.folio","fecha","detalle.documento","cargo.numeroFiscal","cargo.total"
				,"importe","detalle.origen","cargo.precioNeto"
				,"detalle.postFechado","detalle.sucursal","detalle.nombre"
				,"detalle.banco"};
		String[] labels=new String[]{"F.P","Abono F","Fecha","Docto","Fiscal","Total","Importe(Apl)","Origen","P.N"
				,"PostFech","Sucursal","Cliente"
				,"Banco"};
		return GlazedLists.tableFormat(Aplicacion.class,props,labels);
	}

	@Override
	public Action[] getActions() {
		if(this.actions==null){
			actions=new Action[]{
			getLoadAction(),
			addAction("refrescarSeleccion", "refrescar", "Refrescar Selección")			
			,addAction(null,"registrarDeposito","Registrar Deposito")
			,addAction(null,"consultarSolicitudes","Consultar Solicitudes")
			,addAction(CXCActions.AplicarPago.getId(), "aplicarPago", "Aplicar abono")
			,addAction(CXCActions.SolicitudDeCancelacion.getId(), "solicitarCancelacion", "Solicitar cancelacion")
			,addAction(CXCActions.SolicitudDeAutorizacionParaAbono.getId(),"solicitarAutorizacion","Solicitar autorización")
			,addAction(CXCActions.CancelarAbono.getId(),"cancelarAbono","Cancelar Abono")
			,addAction(CXCActions.CancelarAplicacion.getId(), "cancelarAplicacion", "Cancelar Aplicación")	
			,addRoleBasedContextAction(null,CXCRoles.ADMINISTRADOR_COBRANZA_CREDITO.getId(),this,"otrosProductos","Otros Productos")
			
			};
		}
		return actions;
	}
	
	@Override
	protected Model<Abono, Aplicacion> createPartidasModel() {		
		return new Model<Abono, Aplicacion>(){
			public List<Aplicacion> getChildren(Abono parent) {				
				return getManager().buscarAplicaciones(parent);
			}			
		};
	}
	
	private JTextField documentField=new JTextField(5);
	private JTextField fiscalField=new JTextField(5);
	
	protected void installDetailFilterComponents(DefaultFormBuilder builder){
		builder.appendSeparator("Detalle");
		builder.append("Documento",documentField);
		builder.append("Fiscal",fiscalField);
	}
	
	@Override
	protected EventList decorateDetailList( EventList data){
		EventList<MatcherEditor> editors=new BasicEventList<MatcherEditor>();
		
		
		TextFilterator docFilterator=GlazedLists.textFilterator("detalle.documento");
		TextComponentMatcherEditor docEditor=new TextComponentMatcherEditor(documentField,docFilterator);
		editors.add(docEditor);
		
		TextFilterator fiscalFilterator=GlazedLists.textFilterator("cargo.numeroFiscal");
		TextComponentMatcherEditor fiscalEditor=new TextComponentMatcherEditor(fiscalField,fiscalFilterator);
		editors.add(fiscalEditor);
		
		CompositeMatcherEditor matcherEditor=new CompositeMatcherEditor(editors);
		FilterList detailFilter=new FilterList(data,matcherEditor);
		return detailFilter;
	}
	
	
	
	protected void beforeLoad(){
		super.beforeLoad();
		Object res=JOptionPane.showInputDialog(getControl(), "Origen", "Origen de operación", JOptionPane.QUESTION_MESSAGE, null, OrigenDeOperacion.values(), OrigenDeOperacion.CRE);
		setOrigen((OrigenDeOperacion)res);
	}

	@Override
	protected List<Abono> findData() {
		return getManager().buscarAbonos(periodo, getOrigen());
	}

	public void aplicarPago(){
		CXCUIServiceFacade.aplicarPago();
	}	

	public void cancelarAbono(){
		if(getSelectedObject()!=null){			
			Abono a=(Abono)getSelectedObject();
			if(a.isCancelado()) return;
			if(MessageUtils.showConfirmationMessage("Cancelar :\n"+a.toString(), "Cancelación de documentos")){
				if(a instanceof Pago){
					getManager().cancelarPago(a.getId(), null);
				}else{
					getManager().cancelarNota(a.getId(), null);
				}
				refrescarAbono(a.getId());
			}
		}	
	}	
	
	
	public void cancelarAplicacion(){
		if(!this.detailSelectionModel.isSelectionEmpty()){
			List<Aplicacion> aplicaciones=new ArrayList<Aplicacion>();
			aplicaciones.addAll(this.detailSelectionModel.getSelected());
			if(MessageUtils.showConfirmationMessage("Cancelar :"+aplicaciones.size()+" aplicaciones?", "Cancelación de documentos")){
				for(Aplicacion aplicacion:aplicaciones){
					getManager().cancelarAplicacion(aplicacion.getId());
				}
				refrescar();
			}
		}		
	}
	
	public void refrescar(){
		if(getSelectedObject()!=null){
			Abono a=(Abono)getSelectedObject();
			refrescarAbono(a.getId());
		}
	}
	
	/**
	 * Refresca desde la base de datos el abono indicado
	 * 
	 * @param id
	 */
	public void refrescarAbono(String id){
		
		Abono target=getManager().getAbono(id);
		if(target!=null){
			for(int index=0;index<source.size();index++){
				Abono a=(Abono)source.get(index);
				if(a.getId().equals(id)){
					source.set(index, target);
					return;
				}	
			}
			source.add(target);
		}
				
	}
	
	public void otrosProductos(){
		if(!getSelected().isEmpty()){
			if(MessageUtils.showConfirmationMessage("Mandar a otros productos "+getSelected().size()+ " registros?", "Traspaso a otros productos")){
				for(Object o:getSelected()){
					Abono a=(Abono)o;
					otrosProductos(a);
				}
				refrescar();
			}
		}
		
	}
	
	private void otrosProductos(Abono abono){
		Abono a=getManager().getAbono(abono.getId());
		if(a.getDiferencia().doubleValue()==0){
			if(a.getDisponibleCalculado().doubleValue()<=10){
				try {
					a.setDiferencia(a.getDisponibleCalculado());
					a.setDirefenciaFecha(new Date());
					a=(Abono)ServiceLocator2.getHibernateTemplate().merge(a);
				} catch (Exception e) {
					MessageUtils.showMessage("Error transfiriendo disponible de abono a otros productos" +
							"\n"+ExceptionUtils.getRootCauseMessage(e)
							, "Traspaso a otros productos");
					logger.error(e);
				}
			}
		}
	}
	
	public void registrarDeposito(){
		SolicitudDeDeposito sol=SolicitudDeDepositoForm.generar(OrigenDeOperacion.CRE);
		if(sol!=null){
			sol=ServiceLocator2.getSolicitudDeDepositosManager().save(sol);
			MessageUtils.showMessage("Solicitud generada: "+sol.getDocumento(), "Solicitud de depositos");
		}
	}
	
	public void consultarSolicitudes(){
		SelectorDeSolicitudDeDepositos.buscar(OrigenDeOperacion.CRE);
	}

	public OrigenDeOperacion getOrigen() {
		return origen;
	}

	public void setOrigen(OrigenDeOperacion origen) {
		this.origen = origen;
	}
	
	public CXCManager getManager(){
		return ServiceLocator2.getCXCManager();
	}

}
