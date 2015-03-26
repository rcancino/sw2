package com.luxsoft.sw3.contabilidad.ui.consultas;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.SwingWorker;

import org.jdesktop.swingx.JXTable;

import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.event.ListEvent;
import ca.odell.glazedlists.event.ListEventListener;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;
import com.luxsoft.siipap.model.CantidadMonetaria;
import com.luxsoft.siipap.service.ServiceLocator2;
import com.luxsoft.siipap.swing.browser.FilteredBrowserPanel;
import com.luxsoft.siipap.swing.browser.JRBrowserReportForm;
import com.luxsoft.siipap.swing.controls.AbstractControl;
import com.luxsoft.siipap.swing.matchers.FechaSelector;
import com.luxsoft.siipap.swing.utils.CommandUtils;
import com.luxsoft.siipap.swing.utils.TaskUtils;
import com.luxsoft.siipap.util.DBUtils;
import com.luxsoft.siipap.util.DateUtil;
import com.luxsoft.sw3.contabilidad.model.Poliza;
import com.luxsoft.sw3.contabilidad.model.PolizaDet;
import com.luxsoft.sw3.contabilidad.model.SaldoDeCuentaPorConcepto;
import com.luxsoft.sw3.contabilidad.ui.form.PolizaForm;
import com.luxsoft.sw3.contabilidad.ui.form.PolizaFormModel;

/**
 * 
 * 
 * @author Ruben Cancino Ramos
 *
 */
public class AnalisisDeBalanzaDetPanel extends FilteredBrowserPanel<PolizaDet> {
	
	private final int year;
	private final int mes;
	private final Long conceptoId;
	private final String concepto;
	private final String cuenta;
	
	
	
	public AnalisisDeBalanzaDetPanel(int year,int mes,Long conceptoId,String concepto,String cuenta) {
		super(PolizaDet.class);
		setTitle("Analisis");
		this.year=year;
		this.mes=mes;
		this.conceptoId=conceptoId;
		this.concepto=concepto;
		this.cuenta=cuenta;
	}
	
	public void init(){
		addProperty(
				"id"
				,"poliza.folio"
				,"poliza.clase"
				,"poliza.tipo.nombre"
				,"poliza.fecha"
				,"cuenta.clave"
				,"concepto.id"
				,"descripcion2"				
				,"debe"
				,"haber"
				,"acumulado"
				,"referencia"
				,"referencia2"
				,"asiento"
				,"tipo");
		addLabels(
				"Id"
			,"Poliza"
				,"Clase"
				,"Tipo"
				,"Fecha"
			,"Cuenta"
			,"Concepto"
			,"Descripcion"			
			,"Debe"
			,"Haber"
			,"Acumulado"
			,"Origen"
			,"Sucursal"
			,"Asiento"
			,"T"
			);
		/*builder.appendSeparator("Detalle");
		builder.append("Sucursal",referencia2);
		builder.append("Asiento",asiento.getField());
		builder.append("Cuenta",cuentaEditor.getField());
		builder.append("Tipo",tipo);
		builder.append("Origen ",referencia1);
		builder.append("Concepto ",concepto);
		builder.append("Descripción ",descripcion);*/
		
		FechaSelector fechaSelector=new FechaSelector("poliza.fecha");
		installTextComponentMatcherEditor("Poliza", "poliza.folio");
		installTextComponentMatcherEditor("Clase", "poliza.clase");
		installTextComponentMatcherEditor("Sucursal", "referencia2");
		installTextComponentMatcherEditor("Asiento", "asiento");
		installTextComponentMatcherEditor("Tipo", "tipo");
		installTextComponentMatcherEditor("Fecha", fechaSelector, fechaSelector.getInputField());
		installTextComponentMatcherEditor("Origen", "referencia");
		installTextComponentMatcherEditor("Descripción", "descripcion2");
		Comparator c1=GlazedLists.beanPropertyComparator(PolizaDet.class, "poliza.fecha","poliza.folio");
		setDefaultComparator(c1);
	}
	
	
	@Override
	protected void adjustMainGrid(JXTable grid) {		
		grid.getColumnExt("Concepto").setVisible(false);
		grid.getColumnExt("Cuenta").setVisible(false);
		
	}
	
	@Override
	protected void doSelect(Object bean) {
		PolizaDet det=(PolizaDet)bean;
		final Poliza source=ServiceLocator2.getPolizasManager().getPolizaDao().get(det.getPoliza().getId());		
		final PolizaFormModel model=new PolizaFormModel(source);
		model.setReadOnly(true);
		final PolizaForm form=new PolizaForm(model);			
		form.open();
	}
	
	

	@Override
	public Action[] getActions() {
		CommandUtils.createPrintAction(this, "runReporte");
		if(actions==null){			
			actions=new Action[]{
					CommandUtils.createPrintAction(this, "runReporte")
				};
		}
		return actions;
	}
	
	private BigDecimal saldoInicial=BigDecimal.ZERO;
	private BigDecimal saldoFinal=BigDecimal.ZERO;

	@Override
	protected List<PolizaDet> findData() {
		System.out.println("Taladrando: "+mes+"  "+year);
		int mesAnterior=mes-1;
		int yearAnterior=year;
		if(mes==1){
			yearAnterior=year-1;
			mesAnterior=12;
		}
		List<SaldoDeCuentaPorConcepto> res=ServiceLocator2.getHibernateTemplate()
				.find("from SaldoDeCuentaPorConcepto s where s.concepto.id=? and s.year=? and s.mes=?"
						, new Object[]{conceptoId,yearAnterior,mesAnterior}
						);
		
		if(!res.isEmpty()){
			saldoInicial=res.get(0).getSaldoFinal();
			//saldoFinal=res.get(0).getSaldoFinal();
		}
		
		if(mes==13){
			String hql="from PolizaDet det where year(det.poliza.fecha)=? " +
					" and det.poliza.clase=? " +
					" and det.concepto.id=?";
			return ServiceLocator2.getHibernateTemplate().find(hql
					,new Object[]{
						year,"CIERRE_ANUAL",conceptoId
						}
			);
		}else{
			String hql="from PolizaDet det where year(det.poliza.fecha)=? " +
					" and month(det.poliza.fecha)=? " +
					" and det.concepto.id=?";
			return ServiceLocator2.getHibernateTemplate().find(hql
					,new Object[]{
						year,mes,conceptoId
						}
			);
		}
		
	}
	
	
	
	

	@Override
	protected void installEditors(EventList editors) {
		super.installEditors(editors);
	}


	public void open(){
		load();
	}
	@Override
	protected void executeLoadWorker(SwingWorker worker) {
		TaskUtils.executeSwingWorker(worker);
	}

	private TotalesPanel totalPanel;
	
	public JPanel getTotalesPanel(){
		if(totalPanel==null){
			totalPanel=new TotalesPanel();
		}
		return (JPanel)totalPanel.getControl();
	}
	
	private class TotalesPanel extends AbstractControl implements ListEventListener{
		
		private JLabel inifialField=new JLabel();
		private JLabel total1=new JLabel();
		private JLabel total2=new JLabel();
		private JLabel finalField=new JLabel();
		
		

		@Override
		protected JComponent buildContent() {
			final FormLayout layout=new FormLayout("p,2dlu,f:p:g","");
			DefaultFormBuilder builder=new DefaultFormBuilder(layout);
			total1.setHorizontalAlignment(SwingConstants.RIGHT);
			total2.setHorizontalAlignment(SwingConstants.RIGHT);
			inifialField.setHorizontalAlignment(SwingConstants.RIGHT);
			finalField.setHorizontalAlignment(SwingConstants.RIGHT);
			builder.appendSeparator("Resumen ");
			builder.append("Inicial",inifialField);
			builder.append("Debe",total1);
			builder.append("Haber",total2);
			builder.append("Final ",finalField);
			//builder.append("Ventas (Prom)",total2);
			//builder.append("Por Pedir",total3);
			
			builder.getPanel().setOpaque(false);
			getFilteredSource().addListEventListener(this);
			updateTotales();
			return builder.getPanel();
		}
		
		public void listChanged(ListEvent listChanges) {
			if(listChanges.next()){
				System.out.println("Cambio inicial");
			}else{
				System.out.println("Cambio final");
			}
			updateTotales();
			actualizarSaldoo();
		}
		
		double cargos=0;
		double abonos=0;
		
		
		public void updateTotales(){
			
			cargos=0;
			abonos=0;
			
			
			for(Object obj:getFilteredSource()){
				PolizaDet a=(PolizaDet)obj;
				cargos+=a.getDebe().doubleValue();
				abonos+=a.getHaber().doubleValue();
							
			}
			saldoFinal=BigDecimal.valueOf(saldoInicial.doubleValue()+cargos-abonos);
			inifialField.setText(nf.format(saldoInicial.doubleValue()));
			total1.setText(nf.format(cargos));
			total2.setText(nf.format(abonos));
			finalField.setText(nf.format(saldoFinal.doubleValue()));
			//total3.setText(nf.format(toneladasPorPedir));
		}
		
		private void actualizarSaldoo(){
			System.out.println("Actualizando el saldo....");
			int last=getFilteredSource().size();
			double saldo=saldoInicial.doubleValue();
			for(int index=0;index<last;index++){
				PolizaDet det=(PolizaDet)getFilteredSource().get(index);
				double cargos=det.getDebe().doubleValue();
				double abonos=det.getHaber().doubleValue();
				saldo+=cargos-abonos;
				det.setAcumulado(BigDecimal.valueOf(saldo));
				
			}
		}
		
		private NumberFormat nf=NumberFormat.getNumberInstance();
		
	}
	
	public void runReporte(){
		JRBrowserReportForm reportForm=new JRBrowserReportForm("Analisis de Balanza", this){
			public void agregarParametros(Map<String, Object> map) {
				map.put("INICIAL", saldoInicial);
				map.put("SALDO", saldoFinal);
				map.put("CARGOS", BigDecimal.valueOf(totalPanel.cargos));
				map.put("ABONOS", BigDecimal.valueOf(totalPanel.abonos));
				map.put("YEAR",year);
			    map.put("MES",DateUtil.getMesAsString(mes));
			    map.put("CONCEPTO", concepto);
			    map.put("CUENTA", cuenta);
				
			}
		};
		reportForm.setReportPath("contabilidad/ReporteDeAuxiliares.jasper");
		reportForm.open();
	}
	
	public static void show(final int year,int mes,Long conceptoId,String concepto,String cuenta){
		final AnalisisDeBalanzaDetPanel browser=new AnalisisDeBalanzaDetPanel(year,mes,conceptoId,concepto,cuenta);
		
		final FilterBrowserDialog dialog=new FilterBrowserDialog(browser);
		dialog.setTitle("Cuenta: "+cuenta);
		dialog.setModal(false);
		dialog.open();
	}
	
	/**
	 * Prueba local en el EDT
	 * 
	 * @param args
	 * 
	 */
	public static void main(String[] args) {
		javax.swing.SwingUtilities.invokeLater(new Runnable() {

			public void run() {
				DBUtils.whereWeAre();
				com.luxsoft.siipap.swing.utils.SWExtUIManager.setup();
				show(2012,1, new Long(439),"TEST","");
				//System.exit(0);
			}

		});
	}

}
