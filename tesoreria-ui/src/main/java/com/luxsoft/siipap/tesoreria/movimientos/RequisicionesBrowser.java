/**
 * 
 */
package com.luxsoft.siipap.tesoreria.movimientos;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.JTable;

import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.data.JRTableModelDataSource;
import net.sf.jasperreports.view.JRViewer;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.jdesktop.swingx.JXTable;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;

import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.FilterList;
import ca.odell.glazedlists.matchers.Matcher;

import com.luxsoft.siipap.dao.cxp.ImpotadorDeRequisiciones;
import com.luxsoft.siipap.model.Autorizacion;
import com.luxsoft.siipap.model.tesoreria.Concepto;
import com.luxsoft.siipap.model.tesoreria.FormaDePago;
import com.luxsoft.siipap.model.tesoreria.Requisicion;
import com.luxsoft.siipap.model.tesoreria.Requisicion.Estado;
import com.luxsoft.siipap.service.KernellSecurity;
import com.luxsoft.siipap.service.ServiceLocator2;
import com.luxsoft.siipap.swing.browser.FilteredBrowserPanel;
import com.luxsoft.siipap.swing.controls.SXAbstractDialog;
import com.luxsoft.siipap.swing.matchers.FechaMayorAMatcher;
import com.luxsoft.siipap.swing.matchers.FechaMenorAMatcher;
import com.luxsoft.siipap.swing.matchers.FechaSelector;
import com.luxsoft.siipap.swing.reports.ReportUtils;
import com.luxsoft.siipap.swing.utils.CommandUtils;
import com.luxsoft.siipap.swing.utils.MessageUtils;
import com.luxsoft.siipap.swing.utils.Renderers;
import com.luxsoft.siipap.tesoreria.TesoreriaActions;
import com.luxsoft.siipap.tesoreria.procesos.PagoDeRequisicionForm;

@SuppressWarnings("unchecked")
public class RequisicionesBrowser extends FilteredBrowserPanel<Requisicion>{
	
	final static String[] props={"id","afavor","fecha","moneda","tipoDeCambio","total.amount","porPagar"
		,"estado.name","origen","pago.id","pago.fecha","pago.importe","pago.referencia","pago.cuenta"
		,"concepto.descripcion"
		,"pago.impreso"
		};
	final static String[] labels={"Id","A Favor","Fecha","Mon","T.C.","Total","Por Pagar","estado","Origen"
		,"PagoId","Fecha (P)","Total (P)","Referencia","Cuenta"
		,"Concepto"
		,"Impreso"
		};
	
	private FechaSelector selector;
	Map<String, Object>parametros=new HashMap<String, Object>();
	private Action pagarAction;
	private Action autorizarAction;
	private Action cancelarAutorizacion;
	private Action cancelarPago;
	private Action imprimirPoliza;
	private Action importarReqCompras;
	public FechaMayorAMatcher fechaInicialSelector;
	public FechaMenorAMatcher fechaFinalSelector;
	
	public RequisicionesBrowser() {
		super(Requisicion.class);
		setProperties(props);
		setLabels(labels);
		fechaInicialSelector=new FechaMayorAMatcher();
		fechaInicialSelector.setDateField("fecha");
		fechaFinalSelector=new FechaMenorAMatcher();
		fechaFinalSelector.setDateField("fecha");
		installTextComponentMatcherEditor("Id", "id");
		installTextComponentMatcherEditor("Origen", "origen");
		installTextComponentMatcherEditor("A Favor", "afavor");
		installTextComponentMatcherEditor("Estado", "estado");
//		selector=new FechaSelector("fecha");			
//		installTextComponentMatcherEditor("Fecha", selector, selector.getInputField());
		installCustomMatcherEditor("F Inicial", fechaInicialSelector.getFechaField(), fechaInicialSelector);
		installCustomMatcherEditor("F Final", fechaFinalSelector.getFechaField(), fechaFinalSelector);
	
		manejarPeriodo();
	}
	
	@Override
	protected void adjustMainGrid(JXTable grid) {
		grid.getColumnExt("T.C.").setCellRenderer(Renderers.getTipoDeCambioRenderer());
		
		
		
	}
	

	public boolean doDelete(Requisicion bean) {
		try {
			String origen=bean.getOrigen();
			if(origen.equals(Requisicion.GASTOS) ){
				MessageUtils.showMessage("La requisición solo se puede eliminar desde el modulo de: "+bean.getOrigen(), "Eliminación de requisición");
				return false;
			}else{
				ServiceLocator2.getRequisiciionesManager().remove(bean.getId());
				return true;
			}
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}
	
	@Override
	protected Requisicion doInsert() {
		Requisicion req=RequisicionForm.showForm();
		if(req!=null){			
			Requisicion res= ServiceLocator2.getRequisiciionesManager().save(req);
			System.out.println("Req nueva:"+res.getId());
			return res;
		}
		return null;
	}
	
	

	@Override
	protected void doSelect(Object obj) {
		Requisicion bean=(Requisicion)obj;
		Requisicion target=ServiceLocator2.getRequisiciionesManager().get(bean.getId());
		target=RequisicionForm.showForm(target,true);
	}

	@Override
	protected Requisicion doEdit(Requisicion bean) {
		
		Requisicion target=ServiceLocator2.getRequisiciionesManager().get(bean.getId());
		boolean readOnly=target.getEstado().equals(Estado.PAGADA);
		target=RequisicionForm.showForm(target,readOnly);
		if(target!=null){
			Requisicion res= ServiceLocator2.getRequisiciionesManager().save(target);
			return res;
		}
		return null;
	}
	
	private void pagarRequisicion(){
		if(!getSelected().isEmpty()){
			Requisicion bean=(Requisicion)getSelected().get(0);
			/*if(bean.getAutorizacion()==null){
				MessageUtils.showMessage("La requisición no esta autorizada", "Autorización");
				return;
			}*/
			if(bean.getEstado().equals(Estado.PAGADA)){
				MessageUtils.showMessage("Esta requisición ya esta pagada", "Pago");
				return;
			}
			Requisicion clone=ServiceLocator2.getRequisiciionesManager().get(bean.getId());
			
			clone=PagoDeRequisicionForm.showForm(clone);
			if(clone!=null){
				int index=source.indexOf(bean);
				clone=ServiceLocator2.getRequisiciionesManager().registrarPago(clone);
				if(clone!=null)
					source.set(index,clone);
				if(clone.getFormaDePago().equals(FormaDePago.CHEQUE)){
					clone=ControladorDeImpresion.imprimierCheque(clone);
				}
				
			}
			
		}
	}
	
	private void autorizarRequisicion(){
//		if(!getSelected().isEmpty()){
//			final List<Requisicion> reqs=new ArrayList<Requisicion>();
//			reqs.addAll(getSelected());
//			
//			final Autorizacion aut=AutorizacionController.autorizar("Autorización automatica");
//			if(aut!=null){
//				for(int i=0;i<reqs.size();i++){
//					Requisicion rr=reqs.get(i);
//					int index=source.indexOf(rr);
//					rr=ServiceLocator2.getRequisiciionesManager().get(rr.getId());
//					
//					boolean res=rr.autorizar(aut);
//					if(res){
//						rr=ServiceLocator2.getRequisiciionesManager().save(rr);
//						source.set(index, rr);
//					}
//				}
//			}	
//			
//		}
	}
	
	private void cancelarAutorizacion(){
		/*if(!getSelected().isEmpty()){
			final List<Requisicion> reqs=new ArrayList<Requisicion>();
			reqs.addAll(getSelected());
			
			for(int i=0;i<reqs.size();i++){
				Requisicion rr=reqs.get(i);
				int index=source.indexOf(rr);
				rr=ServiceLocator2.getRequisiciionesManager().get(rr.getId());
				Autorizacion a=rr.cancelarAutorizacion();
				if(a!=null){
					rr=ServiceLocator2.getRequisiciionesManager().save(rr);
					//AutorizacionManager.getInstance().cancelarAutorizacion(a);
					source.set(index, rr);
				}
			}
		}*/
	}
	
	@Override
	public Action[] getActions() {
		return super.getActions();
	}
	
	@Override
	public List<Action> getProccessActions() {		
		List<Action> procesos=new ArrayList<Action>();
		procesos.add(getPagarAction());
		procesos.add(getCancelarPagoAction());
		procesos.add(getImprimirPolizaAction());
		procesos.add(createActionReqDet());
		procesos.add(createActionReqGral());
		procesos.add(addAction(null, "cambiarConcepto", "Cambiar concepto"));
		return procesos;
	}
	
	private void cancelarPago(){
		if(!getSelected().isEmpty()){
			Requisicion bean=(Requisicion)getSelected().get(0);
			Requisicion clone=ServiceLocator2.getRequisiciionesManager().cancelarPago(bean);
			int index=source.indexOf(bean);
			source.set(index, clone);
		}
	}
	
	public void cambiarConcepto(){
		if(!getSelected().isEmpty()){
			Requisicion bean=(Requisicion)getSelected().get(0);
			if(bean.getOrigen().equals("TESORERIA") ){
				int index=source.indexOf(bean);
				Requisicion clone=ServiceLocator2.getRequisiciionesManager().get(bean.getId());
				if(index!=-1){
					final List<Concepto> data=ServiceLocator2.getHibernateTemplate().find("from Concepto c where c.clase='TESORERIA2'");
					Object res=JOptionPane.showInputDialog(this.getControl(), "Concepto: ", "Requisición", JOptionPane.QUESTION_MESSAGE,null
							, data.toArray(new Object[0])
							, null);
					if(res!=null){
						clone.setConcepto((Concepto)res);
						clone=(Requisicion)ServiceLocator2.getUniversalDao().save(clone);
						source.set(index, clone);
					}
				}
			}
		}
	}
	
private void impPoliza(){
		
		if(!getSelected().isEmpty()){
			Requisicion bean=(Requisicion)getSelected().get(0);
			if(bean.getEstado().toString()=="PAGADA"){
			SimpleDateFormat fecha=new SimpleDateFormat("dd-MMMMM-yyyy");
			DecimalFormat myFormatter = new DecimalFormat("###,##0.00");
			BigDecimal impl=bean.getPago().getImporte().abs();
			n2t funcion=new n2t();
			String let=funcion.convertirLetras(impl.intValue());
			int t=new Integer(bean.getPago().getImporte().intValue());
			BigDecimal h=bean.getPago().getImporte();
			BigDecimal g=new BigDecimal(t);
			BigDecimal l=h.subtract(g);
			DecimalFormat df=new DecimalFormat("###,##0.00");
			String stramount=df.format(l);
			    String importe = myFormatter.format(bean.getPago().getImporte().multiply(new BigDecimal(-1)));
			    String date=fecha.format(bean.getPago().getFecha());
			    MessageUtils.showMessage("Imprimiendo poliza para el cheque: "+bean.getPago().getReferencia(),"Impresión de poliza");
				Map<String, Object>parametros=new HashMap<String, Object>();
				parametros.put("PROVEEDOR", bean.getPago().getAFavor());
				parametros.put("FECHA",StringUtils.upperCase(date));
				String centavos=stramount.substring(3);
				centavos=StringUtils.leftPad(centavos, 2,'0');
				parametros.put("IMP_LETRA","("+let+" "+"PESOS"+" "+centavos+"/100 MN"+")");
				parametros.put("IMPORTE",importe);
				parametros.put("BANCO",bean.getPago().getCuenta().getDescripcion().toString());
				parametros.put("NUM",bean.getPago().getReferencia());
				parametros.put("HECHO_POR",KernellSecurity.instance().getCurrentUserName());
				parametros.put("POLIZA",bean.getPago().getCuenta().getClave().toString());
				parametros.put("COMENTARIO",bean.getPago().getComentario());
				ReportUtils.viewReport(ReportUtils.toReportesPath("tesoreria/PolizaChekeOR.jasper"),parametros);
			}
			if(!(bean.getEstado().toString()=="PAGADA")){
				MessageUtils.showMessage("La Requisicion debe estar Pagada Para Poder Imprimir Poliza","Message");
			}
			
		}

	}


	
		
	
	
		@Override
	protected List<Requisicion> findData() {
		String hql="from Requisicion r where r.fecha between ? and ?";
		return ServiceLocator2.getHibernateTemplate().find(hql,new Object[]{periodo.getFechaInicial(),periodo.getFechaFinal()});
	}

	public Action getPagarAction(){
		if(pagarAction==null){
			pagarAction=new AbstractAction("pagarRequisicion"){
				public void actionPerformed(ActionEvent e) {
					pagarRequisicion();
				}
			};
			CommandUtils.configAction(pagarAction, TesoreriaActions.PagarRequisicion.getId(), null);
		}
		return pagarAction;
	}
	
	public Action getAutorizarAction(){
		if(autorizarAction==null){
			autorizarAction=new AbstractAction("autorizarPago"){
				public void actionPerformed(ActionEvent e) {
					autorizarRequisicion();
				}				
			};
			CommandUtils.configAction(autorizarAction, TesoreriaActions.AutorizarRequisicion.getId(), null);
		}
		
		return autorizarAction;
	}
	
	public Action getCancelraAutorizacion(){
		if(cancelarAutorizacion==null){
			cancelarAutorizacion=new AbstractAction("cancelarAutorizacion"){
				public void actionPerformed(ActionEvent e) {
					cancelarAutorizacion();
				}
				
			};
			CommandUtils.configAction(cancelarAutorizacion, TesoreriaActions.CancelarAutorizacion.getId(), null);
		}
		
		return cancelarAutorizacion;
	}
	
	public Action getCancelarPagoAction(){
		if(cancelarPago==null){
			cancelarPago=new AbstractAction("cancelarPago"){
				public void actionPerformed(ActionEvent e) {
					cancelarPago();
				}
				
			};
			CommandUtils.configAction(cancelarPago, TesoreriaActions.CancelarPagoDeRequisicion.getId(), null);
		}
		return cancelarPago;
	}
	
	public Action getImprimirPolizaAction(){
		if(imprimirPoliza==null){
			imprimirPoliza=new AbstractAction("Imprimir Poliza"){
				public void actionPerformed(ActionEvent arg0) {
					impPoliza();
				}
			};
		}
		return imprimirPoliza;
	}
	

	public Action getImportarReqCompras(){
		if(importarReqCompras==null){
			importarReqCompras=new AbstractAction("Importar Req Compras"){
				public void actionPerformed(ActionEvent e) {
					importarReqCompras();
				}
			};
		}		
		return importarReqCompras;
	}
	
	private void importarReqCompras(){
		/*String res=JOptionPane.showInputDialog(getControl(), "Numero :","Requisición de Compras",JOptionPane.INFORMATION_MESSAGE);
		if(res!=null){
			if(NumberUtils.isNumber(res)){
				ImpotadorDeRequisiciones imp=new ImpotadorDeRequisiciones();
				imp.setJdbcTemplate(ServiceLocator2.getJdbcTemplate());
				imp.setRequisicionManager(ServiceLocator2.getRequisiciionesManager());
				imp.setLookupManager(ServiceLocator2.getLookupManager());
				Long id=Long.valueOf(res);
				Requisicion r=imp.importarRequisicion(id);
				if(res!=null){
					MessageUtils.showMessage("Req importada: "+res.toString(), "Compras");
					source.add(r);
				}
			}
		}
		else{
			JOptionPane.showMessageDialog(grid, "El numero de requisición debe ser numérico");
		}*/
		
	}
	
	
	protected EventList getFilteredList(EventList list) {		
		return new FilterList<Requisicion>(super.getFilteredList(list),new RevisadasMatcher());
	}



	private class RevisadasMatcher implements Matcher<Requisicion>{

		public boolean matches(Requisicion item) {
			return !item.getEstado().equals(Requisicion.Estado.SOLICITADA);
		}
		
	}





	public Map<String, Object> getParametros() {
		return parametros;
	}
	
	
	public Action createActionReqDet(){
		AbstractAction a=new AbstractAction(){

			public void actionPerformed(ActionEvent arg0) {
				Map<String, Object>param=new HashMap<String, Object>();
				if(!getSelected().isEmpty()){
					Requisicion req=(Requisicion)getSelected().get(0);
					param.put("ID",req.getId());
					System.out.println("Valor "+req.getId());
					//String path=ReportUtils.toReportesPath("");
					
					ReportUtils.viewReport(ReportUtils.toReportesPath("tesoreria/Requisicion.jasper"), param);
				  }
				}
		};
		a.putValue(Action.NAME, "Imprime Req Detalle");
		
		return a;
	}
	
	public Action createActionReqGral(){
		AbstractAction a=new AbstractAction(){

			public void actionPerformed(ActionEvent e) {
				
				if(fechaInicialSelector.getFechaField().getText().isEmpty() && fechaFinalSelector.getFechaField().getText().isEmpty()){
					MessageUtils.showMessage("Los Campos de fecha no deben estar vacios", "Message..");
				}
				if(!fechaInicialSelector.getFechaField().getText().isEmpty() && !fechaFinalSelector.getFechaField().getText().isEmpty()){
					showReportReqGral c=new showReportReqGral();
					c.open();
				}
				
			}
			
		};
		a.putValue(Action.NAME, "imprime Req Gral");
		
		return a;
	}
	
	private class showReportReqGral extends SXAbstractDialog{
		
		public showReportReqGral() {
			super("Reporte...");
		}

		
		public JComponent displayReport() {
			Map<String, Object>parametros=new HashMap<String, Object>();
				SimpleDateFormat format =new SimpleDateFormat("dd/MM/yyyy");
				
					
					try {
						
						Date fecha_ini = format.parse(fechaInicialSelector.getFechaField().getText().toString());
						Date fecha_fin=format.parse(fechaFinalSelector.getFechaField().getText().toString());
						parametros.put("FECHA_INI",fecha_ini);
		                parametros.put("FECHA_FIN",fecha_fin );
					} catch (ParseException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
					

			
                net.sf.jasperreports.engine.JasperPrint jasperPrint = null;
                DefaultResourceLoader loader = new DefaultResourceLoader();
                Resource res = loader.getResource(ReportUtils.toReportesPath("tesoreria/RequicisionGral.jasper"));
                try
                {
                    java.io.InputStream io = res.getInputStream();
                    try
                    {
                    	JTable table=getGrid();
                        jasperPrint = JasperFillManager.fillReport(io, parametros, new JRTableModelDataSource(table.getModel()));
                    }
                    catch(JRException e)
                    {
                        e.printStackTrace();
                    }
                }
                catch(IOException ioe)
                {
                    ioe.printStackTrace();
                }
                JRViewer jasperViewer = new JRViewer(jasperPrint);
                jasperViewer.setPreferredSize(new Dimension(1000, 600));
                return jasperViewer;

			}

		@Override
		protected JComponent buildContent() {
			return displayReport();
		}

		@Override
		protected void setResizable() {
		setResizable(true);
		}
		
	}


	
	
}