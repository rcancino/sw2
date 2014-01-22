package com.luxsoft.sw3.cxc.consultas;

import java.text.MessageFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.SwingWorker;

import org.apache.commons.lang.StringUtils;
import org.springframework.jdbc.core.BeanPropertyRowMapper;

import ca.odell.glazedlists.event.ListEvent;
import ca.odell.glazedlists.event.ListEventListener;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;
import com.luxsoft.siipap.cxc.CXCRoles;
import com.luxsoft.siipap.cxc.model.Cargo;
import com.luxsoft.siipap.cxc.model.CargoPorTesoreria;
import com.luxsoft.siipap.cxc.model.NotaDeCargo;
import com.luxsoft.siipap.cxc.model.OrigenDeOperacion;
import com.luxsoft.siipap.cxc.service.CXCManager;
import com.luxsoft.siipap.cxc.ui.CXCUIServiceFacade;
import com.luxsoft.siipap.cxc.ui.consultas.CargoView;
import com.luxsoft.siipap.cxc.ui.consultas.FacturaForm;
import com.luxsoft.siipap.cxc.ui.selectores.SelectorDeClientes;
import com.luxsoft.siipap.cxc.ui.selectores.SelectorDeDisponibles;
import com.luxsoft.siipap.model.CantidadMonetaria;
import com.luxsoft.siipap.model.core.Cliente;
import com.luxsoft.siipap.reports.CobranzaCamioneta;
import com.luxsoft.siipap.reports.CobranzaPorTesoreria;
import com.luxsoft.siipap.reports.FacturasPendientesCamioneta;
import com.luxsoft.siipap.reports.RmdCobranzaContadoReportForm;
import com.luxsoft.siipap.service.ServiceLocator2;
import com.luxsoft.siipap.swing.browser.FilteredBrowserPanel;
import com.luxsoft.siipap.swing.controls.AbstractControl;
import com.luxsoft.siipap.swing.utils.MessageUtils;
import com.luxsoft.siipap.swing.utils.TaskUtils;
import com.luxsoft.siipap.util.SQLUtils;
import com.luxsoft.siipap.ventas.model.Venta;
import com.luxsoft.sw3.cxc.forms.CargoPorTesoreriaForm;

public class CuentasPorCobrarContadoPanel extends FilteredBrowserPanel<CuentaPorCobrar>{
	
	private CuentasPorCobrarContadoController controller;
	
	public CuentasPorCobrarContadoPanel() {
		super(CuentaPorCobrar.class);
		controller=new CuentasPorCobrarContadoController();
	}

	protected void init(){
		String[] props=new String[]{
				"tipo"
				,"sucursal"
				,"documento"	
				,"fecha"
				,"clave"
				,"nombre"
				,"total"
				,"moneda"
				,"devoluciones"
				,"bonificaciones"
				,"pagos"
				,"saldo"
				,"atraso"
				};
		String[] names=new String[]{
				"Tipo"
				,"sucursal"
				,"documento"	
				,"fecha"
				,"clave"
				,"nombre"
				,"total"
				,"Mon"
				,"devoluciones"
				,"bonificaciones"
				,"pagos"
				,"saldo"
				,"atraso"
				};
		addProperty(props);
		addLabels(names);
		installTextComponentMatcherEditor("Sucursal", "sucursal");
		installTextComponentMatcherEditor("Documento", "documento");
		installTextComponentMatcherEditor("Cliente", "nombre","clave");
		installTextComponentMatcherEditor("Total", "total");
	}
	
	@Override
	protected List<CuentaPorCobrar> findData() {
		return controller.buscarCuentas();
	}

	protected void executeLoadWorker(final SwingWorker worker){		
		TaskUtils.executeSwingWorker(worker);
	}
	
	@Override
	protected void afterLoad() {
		grid.packAll();
	}

	@Override
	public Action[] getActions() {
		if(actions==null)
			actions=new Action[]{
				getLoadAction()
				,getViewAction()
				,addAction(CXCRoles.COBRANZA_CONTADO.name(),"aplicarPago", "Aplicar abono")
				,addAction(CXCRoles.COBRANZA_CONTADO.name(), "generarNotaDevolucionCam", "Devolución (CAM)" )
				,addAction(CXCRoles.COBRANZA_CONTADO.name(), "generarCfdiMostrador", "CFDI Devolución (MOS)" )
				,addAction(CXCRoles.COBRANZA_CONTADO.name(), "generarBonificacion", "Bonificacion" )
				,addAction(CXCRoles.COBRANZA_CONTADO.name(), "generarNotaCargo", "Generar N.Cargo" )
				,addAction(CXCRoles.COBRANZA_CONTADO.name(), "pagarDiferencias", "Pago de Diferencias")
				,addAction(CXCRoles.COBRANZA_CONTADO.name(), "pagoEnEspecie", "Pago en Especie")
				,addAction(CXCRoles.COBRANZA_CONTADO.name(), "cargoPorTesoreria", "Cargo por Tes")
				,addAction(CXCRoles.COBRANZA_CONTADO.name(), "disponibles", "Disponibles")
				,addAction(CXCRoles.COBRANZA_CONTADO.name(), "mandarJuridico", "Mandar a Jurídico")				
				,addAction(null, "refreshSelection", "Refrescar selección ")
				};
		return actions;
	}
	
	@Override
	protected List<Action> createProccessActions() {
		List<Action> procesos=super.createProccessActions();
		procesos.add(addAction(CXCRoles.COBRANZA_CONTADO.name(),"reporteCobranzaCamioneta", "Reporte de cobranza (CAM)"));
		procesos.add(addAction(CXCRoles.COBRANZA_CONTADO.name(),"reporteCobranzaTesoreria", "Reporte de cobranza (TES)"));
		procesos.add(addAction(CXCRoles.COBRANZA_CONTADO.name(),"reporteFacturasPendientes", "Reporte Facutras pend"));
		procesos.add(addAction(CXCRoles.COBRANZA_CONTADO.name(),"reporteRmds", "Reporte de Rmd's"));
		return procesos;
 		
	}
	
	public void aplicarPago(){
		CXCUIServiceFacade.aplicarPago();
	}
	
	public void pagarDiferencias(){
		CuentaPorCobrar row=(CuentaPorCobrar)getSelectedObject();
		if(MessageUtils.showConfirmationMessage("Aplicar pago automático por: "+row.getSaldo(), "Pago de diferencias")){
			Cargo cargo=getManager().getCargo(row.getId());
			CXCUIServiceFacade.aplicarPagoDiferencias(cargo);
			refreshSelection();
		}
	}
	
	public void pagoEnEspecie(){
		CuentaPorCobrar row=(CuentaPorCobrar)getSelectedObject();
		if(MessageUtils.showConfirmationMessage("Aplicar en especie por: "+row.getSaldo(), "Pago en especie")){
			String comentario=JOptionPane.showInputDialog(getControl(),"Comentario ", "Pago en especie", JOptionPane.QUESTION_MESSAGE);
			CXCUIServiceFacade.aplicarPagoEnEspecie(getManager().getCargo(row.getId()), comentario);
			refreshSelection();
		}
	}
	
	public void generarBonificacion(){
		CXCUIServiceFacade.generarNotaDeBonificacion(OrigenDeOperacion.CAM);
	}
	

	public void mandarJuridico(){
		CuentaPorCobrar row=(CuentaPorCobrar)getSelectedObject();
		if(row!=null){
			Cargo c=getManager().getCargo(row.getId());
			if(c!=null){
				c=CXCUIServiceFacade.generarJuridico(c);
				refreshSelection();			
			}
		}
		
	}
	
	public void reporteCobranzaCamioneta(){
		CobranzaCamioneta.run(ServiceLocator2.getJdbcTemplate());
	}
	
	public void reporteRmds(){
		RmdCobranzaContadoReportForm.run();
	}
	
	public void reporteCobranzaTesoreria(){
		CobranzaPorTesoreria.run();
	}
	public void reporteFacturasPendientes(){
		FacturasPendientesCamioneta.run();
	}
	
	public void generarNotaDevolucionCam(){
		controller.generarNotasDeDevolucion();
		
	}
	
	public void generarCfdiMostrador(){
		controller.generarCfdiMostrador();
	}
	
	public void cargoPorTesoreria(){
		CargoPorTesoreria tes=CargoPorTesoreriaForm.generarCargo();
		if(tes!=null){
			CuentaPorCobrar row=new CuentaPorCobrar();
			row.setId(tes.getId());
			row=refresh(row);
			source.add(row);
		}
		
		
	}
	
	public void generarNotaCargo(){
		if(getSelected().isEmpty())
			CXCUIServiceFacade.generarNotaDeCargo();
		else{
			List<Cargo> cargos=new ArrayList<Cargo>();
			cargos.addAll(getSelected());
			for(Cargo c:cargos){
				if(c instanceof NotaDeCargo)
					CXCUIServiceFacade.generarNotaDeCargo();
			}
			CXCUIServiceFacade.generarNotaDeCargo(cargos);
		}
	}
	
	public void disponibles(){
		Cliente selected=SelectorDeClientes.seleccionar();
		if(selected!=null)
			SelectorDeDisponibles.buscar(selected);
	}
	
	private Cargo getCargo(CuentaPorCobrar c){
		return getManager().getCargo(c.getId());
	}
	
	private CuentaPorCobrar refresh(CuentaPorCobrar row){		
		String sql=SQLUtils.loadSQLQueryFromResource("sql/cuentas_x_cobrar_contado_row.sql");
		Object[] params=new Object[]{row.getId()};
		List<CuentaPorCobrar> target=ServiceLocator2.getJdbcTemplate()
			.query(sql,params, new BeanPropertyRowMapper(CuentaPorCobrar.class));
		return target.isEmpty()?row:target.get(0);
	}
	
	public void refreshSelection(){
		for(Object row:getSelected()){
			CuentaPorCobrar old=(CuentaPorCobrar)row;
			int index=source.indexOf(row);
			CuentaPorCobrar fresh=refresh(old);
			if(index!=-1){
				logger.info("Cargo refrescada:"+fresh);
				source.set(index,fresh);
			}
		}
	}
	
	@Override
	protected void doSelect(Object o) {
		CuentaPorCobrar row=(CuentaPorCobrar)o;
		Cargo bean=getManager().getCargo(row.getId());
		if(bean!=null && (bean instanceof Venta) ){
			Venta v=(Venta)bean;
			FacturaForm.show(v.getId());			
		}else if(bean!=null && (bean instanceof NotaDeCargo)){
			NotaDeCargo cargo=(NotaDeCargo)bean;
			CargoView.show(cargo.getId());
		}
	}	
	
	public CXCManager getManager(){
		return ServiceLocator2.getCXCManager();
	}
	
	private TotalesPanel totalPanel;
	
	
	public JPanel getTotalesPanel(){
		if(totalPanel==null){
			totalPanel=new TotalesPanel();
		}
		return (JPanel)totalPanel.getControl();
	}
	
	
	private class TotalesPanel extends AbstractControl implements ListEventListener{
		
		private JLabel saldoTotal=new JLabel();
		

		@Override
		protected JComponent buildContent() {
			final FormLayout layout=new FormLayout("p,2dlu,f:p:g","");
			DefaultFormBuilder builder=new DefaultFormBuilder(layout);
			saldoTotal.setHorizontalAlignment(SwingConstants.RIGHT);
			
			builder.append("Saldo",saldoTotal);
			
			builder.getPanel().setOpaque(false);
			getFilteredSource().addListEventListener(this);
			updateTotales();
			return builder.getPanel();
		}
		
		public void listChanged(ListEvent listChanges) {
			if(listChanges.next()){
				
			}
			updateTotales();
		}
		
		public void updateTotales(){
			CantidadMonetaria saldo=calcularSaldoPesos(getFilteredSource());			
			String pattern="{0}  ({1})";
			saldoTotal.setText(MessageFormat.format(pattern, saldo.amount(),part(saldo,saldo)));
		}
		
		private NumberFormat nf=NumberFormat.getPercentInstance();
		
		private String part(final CantidadMonetaria total,final CantidadMonetaria part){
			
			double res=0;
			if(total.amount().doubleValue()>0){
				res=part.divide(total.amount()).amount().doubleValue();
			}
			return StringUtils.leftPad(nf.format(res),5);
		}
		
	}
	
	public static CantidadMonetaria calcularSaldoPesos(final List<CuentaPorCobrar> cuentas){
		CantidadMonetaria saldo=CantidadMonetaria.pesos(0);
		for(CuentaPorCobrar c:cuentas){
			saldo=saldo.add(c.getSaldoMN());
		}
		return saldo;
	}

}
