package com.luxsoft.sw3.contabilidad.ui.consultas;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingWorker;

import org.jdesktop.swingx.JXTable;

import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.event.ListEvent;
import ca.odell.glazedlists.event.ListEventListener;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;
import com.luxsoft.siipap.model.Periodo;
import com.luxsoft.siipap.service.ServiceLocator2;
import com.luxsoft.siipap.swing.browser.FilteredBrowserPanel;
import com.luxsoft.siipap.swing.browser.JRBrowserReportForm;
import com.luxsoft.siipap.swing.controls.AbstractControl;
import com.luxsoft.siipap.swing.matchers.FechaSelector;
import com.luxsoft.siipap.swing.utils.CommandUtils;
import com.luxsoft.siipap.swing.utils.TaskUtils;
import com.luxsoft.siipap.util.DBUtils;
import com.luxsoft.siipap.util.DateUtil;
import com.luxsoft.sw3.contabilidad.model.CuentaContable;
import com.luxsoft.sw3.contabilidad.model.Poliza;
import com.luxsoft.sw3.contabilidad.model.PolizaDet;
import com.luxsoft.sw3.contabilidad.model.SaldoDeCuenta;
import com.luxsoft.sw3.contabilidad.ui.form.PolizaForm;
import com.luxsoft.sw3.contabilidad.ui.form.PolizaFormModel;

/**
 * 
 * 
 * @author Ruben Cancino Ramos
 *
 */
public class AnalisisDeBalanzaPorCuentaPanel extends FilteredBrowserPanel<PolizaDet> {
	
	private  int year;
	private  int mes;
	private  String cuenta;
	
	
	
	public AnalisisDeBalanzaPorCuentaPanel(int year,int mes,String cuenta) {
		super(PolizaDet.class);
		setTitle("Analisis");
		this.year=year;
		this.mes=mes;
		this.periodo=Periodo.getPeriodoEnUnMes(mes-1, year);
		this.cuenta=cuenta;
	}
	
	public void init(){
		addProperty(
				"mes"
				,"id"
				,"poliza.folio"
				,"poliza.clase"
				,"poliza.tipo.nombre"
				,"poliza.fecha"
				,"cuenta.clave"
				,"concepto.descripcion"
				,"descripcion2"				
				,"debe"
				,"haber"
				,"acumulado"
				,"referencia"
				,"referencia2"
				,"asiento"
				,"tipo");
		addLabels(
				"Mes"
				,"Id"
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
		
		
		FechaSelector fechaSelector=new FechaSelector("poliza.fecha");
		installTextComponentMatcherEditor("Poliza", "poliza.folio");
		installTextComponentMatcherEditor("Clase", "poliza.clase");		
		installTextComponentMatcherEditor("Asiento", "asiento");
		installTextComponentMatcherEditor("Tipo", "tipo");
		installTextComponentMatcherEditor("Fecha", fechaSelector, fechaSelector.getInputField());
		
		installTextComponentMatcherEditor("Sucursal", "referencia2");
		installTextComponentMatcherEditor("Concepto", "concepto");
		installTextComponentMatcherEditor("Origen", "referencia");
		installTextComponentMatcherEditor("Descripción", "descripcion2");
		
		Comparator c1=GlazedLists.beanPropertyComparator(PolizaDet.class, "poliza.fecha","poliza.folio");
		setDefaultComparator(c1);
		//manejarPeriodo();
	}
	
	@Override
	protected void manejarPeriodo() {
		//periodo=Periodo.getPeriodoDelMesActual();
	}
	
	@Override
	protected void adjustMainGrid(JXTable grid) {		
		grid.getColumnExt("Id").setVisible(false);
		grid.getColumnExt("Cuenta").setVisible(false);
		
	}
	
	protected void nuevoPeriodo(Periodo p){
		this.mes=Periodo.obtenerMes(p.getFechaInicial())+1;
		this.year=Periodo.obtenerYear(p.getFechaInicial());
		load();
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
		//CommandUtils.createPrintAction(this, "runReporte");
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
		int mesAnterior=mes-1;
		int yearAnterior=year;
		if(mes==1){
			yearAnterior=year-1;
			mesAnterior=12;
		}
		List<SaldoDeCuenta> res=ServiceLocator2.getHibernateTemplate()
				.find("from SaldoDeCuenta s where s.cuenta.clave=? and s.year=? and s.mes=?"
						, new Object[]{cuenta,yearAnterior,mesAnterior}
						);
		
		if(!res.isEmpty()){
			saldoInicial=res.get(0).getSaldoFinal();
			//saldoFinal=res.get(0).getSaldoFinal();
		}
		if(mes==13){
			String hql="from PolizaDet det where year(det.poliza.fecha)=? " +
					" and det.poliza.clase=? " +
					" and det.cuenta.clave=?";
			return ServiceLocator2.getHibernateTemplate().find(hql
					,new Object[]{
						year,"CIERRE_ANUAL",cuenta
						}
			);
		}else{
			String hql="from PolizaDet det where year(det.poliza.fecha)=? " +
					" and month(det.poliza.fecha)=? " +
					" and det.cuenta.clave=? and det.poliza.clase!=?";
			return ServiceLocator2.getHibernateTemplate().find(hql
					,new Object[]{
						year,mes,cuenta,"CIERRE_ANUAL"
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
	
	@Override
	protected void afterLoad() {		
		super.afterLoad();
		grid.packAll();
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
			JButton act=new JButton("Actualizar saldo inicial");
			act.addActionListener(new ActionListener() {				
				public void actionPerformed(ActionEvent e) {
					BigDecimal saldo=actualizarSaldoInicial();
					inifialField.setText(nf.format(saldoInicial.doubleValue()));					
					updateTotales();
					actualizarSaldoo();
				}
			});
			builder.append(act);
			builder.nextLine();
			builder.append(getPeriodoLabel());
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
			    map.put("CUENTA", cuenta);
				
			}
		};
		reportForm.setReportPath("contabilidad/ReporteDeAuxiliares2.jasper");
		reportForm.open();
	}
	
	public BigDecimal actualizarSaldoInicial(){
		JTextField sucursalField=(JTextField)this.textEditors.get("Sucursal");
		JTextField origenField=(JTextField)this.textEditors.get("Origen");
		JTextField descripcionlField=(JTextField)this.textEditors.get("Descripción");
		JTextField conceptoField=(JTextField)this.textEditors.get("Concepto");
		
		List<CuentaContable> cta=ServiceLocator2.getHibernateTemplate()
				.find("from CuentaContable c where c.clave=? "
						, new Object[]{cuenta}
						);
	
		
		String sql="select ifnull(sum(x.debe-x.haber),0) from sx_polizasdet x " +
				" join sx_polizas a on(a.poliza_id=x.poliza_id)	" +
				" join sx_cuentas_contables z on(z.cuenta_id=x.cuenta_id)	" +
				" join sx_conceptos_contables y on(x.concepto_id=y.concepto_id) " +
				" where  z.clave=? " +
				//" and z.de_resultado is true and  date(a.fecha) >= '2013/01/01' "+
				"  @deResultado " +
				" and  date(a.fecha) <? " +
				" and ifnull(x.REFERENCIA2,'') like ? " +
				" and y.DESCRIPCION like ? " +
				" and ifnull(x.REFERENCIA,'')  like ? " +
				" and ifnull(x.DESCRIPCION2,'') like ?" ;
		
		
		
		String referencia2="%"+sucursalField.getText()+"%";
		String concepto="%"+conceptoField.getText()+"%";
		String referencia1="%"+origenField.getText()+"%";
		String descripcion="%"+descripcionlField.getText()+"%";
		
		int mesnew=0;
		int yearnew=0;
		if(mes==1){
			mesnew=13;
			yearnew=year-1;
		}
		else{
			mesnew=mes;
		    yearnew=year;
		}
			
		String deResultado="";
		if(cta.get(0).isDeResultado()){
			
			deResultado=" and z.de_resultado is true and  date(a.fecha) >= '"+yearnew+"/01/01' ";
		}
		sql=sql.replaceAll("@deResultado",deResultado);
			
		Object params[] ={cuenta,periodo.getFechaInicial(),referencia2,concepto,referencia1,descripcion};		
		Number res=(Number)ServiceLocator2.getJdbcTemplate().queryForObject(sql, params, Number.class);
		saldoInicial=BigDecimal.valueOf(res.doubleValue());		

		for(Object para:params){
			System.err.println("  ----  "+ para);
		}
		
		return saldoInicial;
		

	}
	
	public static void show(final int year,int mes,String cuenta){
		final AnalisisDeBalanzaPorCuentaPanel browser=new AnalisisDeBalanzaPorCuentaPanel(year,mes,cuenta);		
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
				show(2013,3, "600");
				//System.exit(0);
			}

		});
	}

}
