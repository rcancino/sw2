package com.luxsoft.sw3.contabilidad.ui.reportes;

import java.awt.BorderLayout;
import java.awt.event.ActionListener;
import java.beans.EventHandler;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.SwingWorker;

import org.apache.poi.hssf.record.formula.functions.Round;
import org.jdesktop.swingx.JXTable;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.util.ClassUtils;

import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.matchers.Matcher;
import ca.odell.glazedlists.matchers.Matchers;

import com.jgoodies.binding.value.ValueHolder;
import com.jgoodies.uif.AbstractDialog;
import com.jgoodies.uifextras.util.ActionLabel;
import com.luxsoft.siipap.model.Periodo;
import com.luxsoft.siipap.service.ServiceLocator2;
import com.luxsoft.siipap.swing.binding.Binder;
import com.luxsoft.siipap.swing.browser.FilteredBrowserPanel;
import com.luxsoft.siipap.swing.browser.JRBrowserReportForm;
import com.luxsoft.siipap.swing.matchers.FechaSelector;
import com.luxsoft.siipap.swing.utils.CommandUtils;
import com.luxsoft.siipap.swing.utils.TaskUtils;
import com.luxsoft.siipap.util.MonedasUtils;
import com.luxsoft.siipap.util.SQLUtils;
import com.luxsoft.sw3.contabilidad.polizas.PolizaUtils;
import com.luxsoft.sw3.contabilidad.ui.consultas.FilterBrowserDialog;

/**
 * 
 * 
 * @author Ruben Cancino Ramos
 *
 */
public class AnalisisDeDIOTPanel extends FilteredBrowserPanel<AnalisisDeDIOTPanel.AnalisisDIOT> {
	
	
	private DIOTPanel diotPanel;
	
	public AnalisisDeDIOTPanel() {
		super(AnalisisDeDIOTPanel.AnalisisDIOT.class);
		
	}
	
	public AnalisisDeDIOTPanel(int year,int mes) {
		super(AnalisisDeDIOTPanel.AnalisisDIOT.class);
		setTitle("Depositos y transferencias autorizadas");
		
	}
	
	public void init(){
		
		addProperty("ORIGEN","PROVEEDOR","IVA_NOTA","IVA_ACRED","IVA_RET","IVA_ANT","BASE_CALCULADA","IETU","IETU_ANT","EXENTO");
		
		installTextComponentMatcherEditor("Origen", "ORIGEN");
		installTextComponentMatcherEditor("Proveedor", "PROVEEDOR");
		installTextComponentMatcherEditor("Iva Acred", "IVA_ACRED");
		setDefaultComparator(GlazedLists.beanPropertyComparator(AnalisisDIOT.class,"BASE_CALCULADA"));
		manejarPeriodo();
	}
	@Override
	protected void installEditors(EventList editors) {		
		super.installEditors(editors);
		//Matcher m=Matchers.beanPropertyMatcher(AnalisisDIOT.class, "tipo", "NA");
		//m=Matchers.invert(m);
		//editors.add(GlazedLists.fixedMatcherEditor(m));
	}
	
	
	
	@Override
	protected void adjustMainGrid(JXTable grid) {		
		//grid.getColumnExt("Descripción").setVisible(true);
		
	}
	
	@Override
	protected JComponent buildContent() {
		
		JTabbedPane tabPanel=new JTabbedPane();
		
		final JPanel panel=new JPanel(new BorderLayout());
		panel.add(buildGridPanel(buildGrid()),BorderLayout.CENTER);
		afterGridCreated();
		tabPanel.addTab("IVA", panel);
		
		diotPanel=new DIOTPanel();
		tabPanel.addTab("DIOT",diotPanel.getControl());
		
		return tabPanel;
	}
	@Override
	protected void afterLoad() {
		super.afterLoad();
		diotPanel.actualizar(getFilteredSource());
	}
	
	protected void manejarPeriodo(){
		periodo=Periodo.getPeriodoDelMesActual(new Date());
	}
	
	@Override
	public void cambiarPeriodo() {
		final ValueHolder yearModel=new ValueHolder(getYear());
		final ValueHolder mesModel=new ValueHolder(getMes());
		AbstractDialog dialog=Binder.createSelectorMesYear(yearModel, mesModel);
		dialog.open();
		if(!dialog.hasBeenCanceled()){
			periodo=Periodo.getPeriodoEnUnMes(mesModel.intValue()-1, yearModel.intValue());
			nuevoPeriodo(periodo);
			updatePeriodoLabel();
		}
	}
	final DateFormat df=new SimpleDateFormat("MMMM - yyyy");
	
	protected void updatePeriodoLabel(){
		periodoLabel.setText(df.format(periodo.getFechaFinal()));
	}
	public ActionLabel getPeriodoLabel(){
		if(periodoLabel==null){
			manejarPeriodo();
			periodoLabel=new ActionLabel(df.format(periodo.getFechaFinal()));
			periodoLabel.addActionListener(EventHandler.create(ActionListener.class, this, "cambiarPeriodo"));
		}
		return periodoLabel;
	}
	
	
	@Override
	protected void doSelect(Object bean) {
		AnalisisDeDIOTPanel.AnalisisDIOT det=(AnalisisDeDIOTPanel.AnalisisDIOT)bean;
		/*
		final Poliza source=ServiceLocator2.getPolizasManager().getPolizaDao().get(det.getPolizaid());		
		final PolizaFormModel model=new PolizaFormModel(source);
		model.setReadOnly(true);
		final PolizaForm form=new PolizaForm(model);			
		form.open();*/
		drill(det);
	}
	
	public void drill(AnalisisDeDIOTPanel.AnalisisDIOT row){
		getView().getViewProperties().getViewTitleBarProperties().setVisible(false);
	}

	@Override
	public Action[] getActions() {
		if(actions==null){			
			actions=new Action[]{
					getLoadAction()
					,addAction(null, "cargaBatch", "Generar carga batch"),
					CommandUtils.createPrintAction(this, "runReporte")
				};
		}
		return actions;
	}
	
	public void cargaBatch(){
		this.diotPanel.generarCargaBatch();
	}
	
	private BigDecimal saldoInicial=BigDecimal.ZERO;
	private BigDecimal saldoFinal=BigDecimal.ZERO;

	@Override
	protected List<AnalisisDeDIOTPanel.AnalisisDIOT> findData() {
		String path=ClassUtils.addResourcePathToPackagePath(getClass(), "AnalisisDeDIOT.sql");
		String sql=SQLUtils.loadSQLQueryFromResource(path);
		List<AnalisisDIOT> res=ServiceLocator2.getJdbcTemplate()
				.query(sql,new Object[]{getYear(),getMes()},new BeanPropertyRowMapper(AnalisisDeDIOTPanel.AnalisisDIOT.class));
		
		return res;
	}

	public void open(){		
		load();		
	}
	
	@Override
	protected void executeLoadWorker(SwingWorker worker) {
		TaskUtils.executeSwingWorker(worker);
	}
	
	
	public int getYear(){
		return Periodo.obtenerYear(periodo.getFechaFinal());
	}
	public int getMes(){
		return Periodo.obtenerMes(periodo.getFechaFinal())+1;
	}

	/*private TotalesParaAnalisisPanel totalPanel;
	
	public JPanel getTotalesPanel(){
		if(totalPanel==null){
			totalPanel=new TotalesParaAnalisisPanel(getFilteredSource());
		}
		return (JPanel)totalPanel.getControl();
	}
	*/
	
	
	public void runReporte(String reportPath,String title){
		JRBrowserReportForm reportForm=new JRBrowserReportForm(title, this){
			public void agregarParametros(Map<String, Object> map) {
				map.put("SALDO_INICIAL", saldoInicial);
				map.put("SALDO_FIANL", saldoFinal);
				//map.put("CARGOS", totalPanel.getCargos());
				//map.put("ABONOS", totalPanel.getAbonos());
				
			}
		};
		reportForm.setReportPath("contabilidad/DetalleDeBalanza.jasper");
		reportForm.open();
	}
	
	public static void show(final int year,int mes){
		final AnalisisDeDIOTPanel browser=new AnalisisDeDIOTPanel(year,mes);
		final FilterBrowserDialog dialog=new FilterBrowserDialog(browser);
		dialog.setTitle("");
		dialog.setModal(false);
		dialog.open();
	}
	
	
	
	
	public static class AnalisisDIOT {
		private String ORIGEN;
		private String PROVEEDOR;
		private BigDecimal IVA_NOTA;
		private BigDecimal IVA_ACRED;
		private BigDecimal IVA_RET;
		private BigDecimal IVA_ANT;
		private BigDecimal BASE_CALCULADA;
		private BigDecimal IETU;
		private BigDecimal IETU_ANT;
		private BigDecimal EXENTO;
		public String getORIGEN() {
			return ORIGEN;
		}
		public void setORIGEN(String oRIGEN) {
			ORIGEN = oRIGEN;
		}
		public String getPROVEEDOR() {
			return PROVEEDOR;
		}
		public void setPROVEEDOR(String pROVEEDOR) {
			PROVEEDOR = pROVEEDOR;
		}
		public BigDecimal getIVA_NOTA() {
			if(IVA_NOTA==null)
				IVA_NOTA=BigDecimal.ZERO;
			return IVA_NOTA;
		}
		public void setIVA_NOTA(BigDecimal iVA_NOTA) {
			IVA_NOTA = iVA_NOTA;
		}
		public BigDecimal getIVA_ACRED() {
			if(IVA_ACRED==null)
				IVA_ACRED=BigDecimal.ZERO;
			return IVA_ACRED;
		}
		public void setIVA_ACRED(BigDecimal iVA_ACRED) {
			IVA_ACRED = iVA_ACRED;
		}
		public BigDecimal getIVA_RET() {
			if(IVA_RET==null)
				IVA_RET=BigDecimal.ZERO;
			return IVA_RET;
		}
		public void setIVA_RET(BigDecimal iVA_RET) {
			IVA_RET = iVA_RET;
		}
		public BigDecimal getIVA_ANT() {
			if(IVA_ANT==null)
				IVA_ANT=BigDecimal.ZERO;
			return IVA_ANT;
		}
		public void setIVA_ANT(BigDecimal iVA_ANT) {
			IVA_ANT = iVA_ANT;
		}
		public BigDecimal getBASE_CALCULADA() {
			if(BASE_CALCULADA==null)
				BASE_CALCULADA=BigDecimal.ZERO;
			return BASE_CALCULADA;
		}
		public void setBASE_CALCULADA(BigDecimal bASE_CALCULADA) {
			BASE_CALCULADA = bASE_CALCULADA;
		}
		public BigDecimal getIETU() {
			if(IETU==null)
				IETU=BigDecimal.ZERO;
			return IETU;
		}
		public void setIETU(BigDecimal iETU) {
			IETU = iETU;
		}
		public BigDecimal getIETU_ANT() {
			if(IETU_ANT==null)
				IETU_ANT=BigDecimal.ZERO;
			return IETU_ANT;
		}
		public void setIETU_ANT(BigDecimal iETU_ANT) {
			IETU_ANT = iETU_ANT;
		}
		public BigDecimal getEXENTO() {
			if(EXENTO==null)
				EXENTO=BigDecimal.ZERO;
			return EXENTO;
		}
		public void setEXENTO(BigDecimal eXENTO) {
			EXENTO = eXENTO;
		}
		
		
	}
	
	/**
	 * Prueba local en el EDT
	 * 
	 * @param args
	 * 
	 */
	public static void main(String[] args) {
		String path=ClassUtils.addResourcePathToPackagePath(DetalleDeIvaAcreditablePanel.class, "AnalisisDeDIOT.sql");
		String sql=SQLUtils.loadSQLQueryFromResource(path);
		SQLUtils.printBeanClasFromSQL(sql, false);
		SQLUtils.printColumnNames(sql);
		/*
		javax.swing.SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				//DBUtils.whereWeAre();
				show(2012,1);
				//System.exit(0);
				 
				
			}

		});*/
	}
	
	

}
