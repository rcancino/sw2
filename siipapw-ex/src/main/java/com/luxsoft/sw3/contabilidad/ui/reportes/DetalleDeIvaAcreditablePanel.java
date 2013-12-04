package com.luxsoft.sw3.contabilidad.ui.reportes;

import java.awt.event.ActionListener;
import java.beans.EventHandler;
import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingWorker;

import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.util.ClassUtils;

import ca.odell.glazedlists.event.ListEvent;
import ca.odell.glazedlists.event.ListEventListener;

import com.jgoodies.binding.value.ValueHolder;
import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.uif.AbstractDialog;
import com.jgoodies.uifextras.util.ActionLabel;
import com.luxsoft.siipap.model.Periodo;
import com.luxsoft.siipap.service.ServiceLocator2;
import com.luxsoft.siipap.swing.binding.Binder;
import com.luxsoft.siipap.swing.browser.FilteredBrowserPanel;
import com.luxsoft.siipap.swing.controls.AbstractControl;
import com.luxsoft.siipap.swing.utils.CommandUtils;
import com.luxsoft.siipap.swing.utils.TaskUtils;
import com.luxsoft.siipap.util.SQLUtils;





public class DetalleDeIvaAcreditablePanel extends FilteredBrowserPanel<DetalleDeIvaAcreditablePanel.IvaAcreditable> {

	public DetalleDeIvaAcreditablePanel() {
		super(DetalleDeIvaAcreditablePanel.IvaAcreditable.class);
	}

	@Override
	protected void init() {
		addProperty("PROVEEDOR","ORIGEN","TIPO","POLIZA","FECHA","DEBE","HABER","BASE_CALCULADA","CLASE","ASIENTO","CTA_DESCRIPCION","CONC_DESCRIPCION","CUENTA","CONCEPTO_ID","CONCEPTO","DESCRIPCION2","REFERENCIA2","POLIZADET_ID","POLIZA_ID");
		installTextComponentMatcherEditor("Proveedor", "PROVEEDOR");
		installTextComponentMatcherEditor("Origen", "ORIGEN");
		installTextComponentMatcherEditor("Poliza", "POLIZA");
		
		installTextComponentMatcherEditor("Clase", "CLASE");
		installTextComponentMatcherEditor("Asiento", "ASIENTO");
		installTextComponentMatcherEditor("Cuenta", "CTA_DESCRIPCION","CUENTA");
		installTextComponentMatcherEditor("Concepto", "CONC_DESCRIPCION","CONCEPTO");
		manejarPeriodo();
	}
	
	@Override
	protected List<IvaAcreditable> findData() {
		String path=ClassUtils.addResourcePathToPackagePath(DetalleDeIvaAcreditablePanel.class, "DetalleIvaAcreditable_IETU.sql");
		String sql=SQLUtils.loadSQLQueryFromResource(path);
		Object[] params=new Object[]{
				getYear(),getMes()
		};
		List<IvaAcreditable> res= ServiceLocator2.getJdbcTemplate().query(sql, params,new BeanPropertyRowMapper(IvaAcreditable.class));
		for(IvaAcreditable a:res){
			asignarOrigen(a);
		}
		return res;
	}
	
	protected void manejarPeriodo(){
		periodo=Periodo.getPeriodoDelMesActual(new Date());
	}
	public int getYear(){
		return Periodo.obtenerYear(periodo.getFechaFinal());
	}
	public int getMes(){
		return Periodo.obtenerMes(periodo.getFechaFinal())+1;
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
	protected void executeLoadWorker(SwingWorker worker) {
		TaskUtils.executeSwingWorker(worker);
	}
	@Override
	public Action[] getActions() {
		if(actions==null){
			Action printAction=CommandUtils.createPrintAction(this, "imprimirBalanza");
			printAction.putValue(Action.SHORT_DESCRIPTION, "Balanza de comprobación");
			actions=new Action[]{
				getLoadAction()
				//,printAction
				
				};
		}
		return actions;
	}
	
	public void asignarOrigen(IvaAcreditable a) {
		String proveedor=a.getPROVEEDOR();
		if("RETENIDO".equals(a.getORIGEN())){
			List<String> res = ServiceLocator2.getHibernateTemplate()
						.find("select p.id from Proveedor p where p.nombre like ?",proveedor);
			if (!res.isEmpty()) {
				a.setORIGEN("COMPRAS");
				return;
			}else{
				List<String> rgastos = ServiceLocator2.getHibernateTemplate()
						.find("select p.id from GProveedor p where p.nombre like ?",proveedor);
				if (!rgastos.isEmpty() && (rgastos.get(0)!=null)) {
					a.setORIGEN("GASTOS");
				}
			}
		}
	}
	
	
	private TotalesPanel totalPanel;
	
	public JPanel getTotalesPanel(){
		if(totalPanel==null){
			totalPanel=new TotalesPanel();
			getFilteredSource().addListEventListener(totalPanel);
			//this.detailSortedList.addListEventListener(totalPanel);
		}
		return (JPanel)totalPanel.getControl();
	}
	
	protected class TotalesPanel extends AbstractControl implements ListEventListener{
		
		private JLabel totalIvaLabel;
		private JLabel baseCalculadaLabel;
		private JLabel totalIetuLabel;
		private JLabel exentoLabel;
		
		NumberFormat nf=NumberFormat.getCurrencyInstance();
		
		@Override
		protected JComponent buildContent() {
			
			totalIvaLabel=new JLabel();
			totalIvaLabel.setHorizontalAlignment(JLabel.RIGHT);			
			baseCalculadaLabel=new JLabel();
			baseCalculadaLabel.setHorizontalAlignment(JLabel.RIGHT);			
			totalIetuLabel=new JLabel();
			totalIetuLabel.setHorizontalAlignment(JLabel.RIGHT);			
			exentoLabel=new JLabel();
			exentoLabel.setHorizontalAlignment(JLabel.RIGHT);
			
			
			FormLayout layout=new FormLayout("p,2dlu,f:50dlu:g","");
			final DefaultFormBuilder builder=new DefaultFormBuilder(layout);
			//porPoliza.setEnabled(false);
			
			builder.append("IVA:",totalIvaLabel);
			builder.append("Base (Calc):",baseCalculadaLabel);
			builder.append("IETU: ",totalIetuLabel);			
			builder.append("Exento:",exentoLabel);
			
			builder.getPanel().setOpaque(false);			
			return builder.getPanel();
		}
		
		BigDecimal totalIva=BigDecimal.ZERO;
		BigDecimal baseCalculada=BigDecimal.ZERO;
		BigDecimal totalIetu=BigDecimal.ZERO;
		BigDecimal exento=BigDecimal.ZERO;
		
		private void updateTotales(){
			totalIva=BigDecimal.ZERO;
			baseCalculada=BigDecimal.ZERO;
			totalIetu=BigDecimal.ZERO;
			exento=BigDecimal.ZERO;
			
			for(Object o:sortedSource){
				IvaAcreditable a=(IvaAcreditable)o;
				
				totalIva=totalIva.add(a.getIVA());
				baseCalculada=baseCalculada.add(a.getBase());
				totalIetu=totalIetu.add(a.getIetu());
				exento=exento.add(a.getExento());
			}
			
			totalIvaLabel.setText(nf.format(totalIva));
			baseCalculadaLabel.setText(nf.format(baseCalculada));
			totalIetuLabel.setText (nf.format(totalIetu));
			exentoLabel.setText( nf.format(exento));
		}
		
		
		public void listChanged(ListEvent listChanges) {
			if(listChanges.hasNext()){
				updateTotales();
			}
		}
	}
	
	public static class IvaAcreditable{
		private String PROVEEDOR;
		private String ORIGEN;
		private String TIPO;
		private long POLIZA;
		private Date FECHA;
		private BigDecimal DEBE;
		private BigDecimal HABER;
		private String CLASE;
		private String ASIENTO;
		private String CTA_DESCRIPCION;
		private String CONC_DESCRIPCION;
		private String CUENTA;
		private long CONCEPTO_ID;
		private String CONCEPTO;
		private String DESCRIPCION2;
		private String REFERENCIA2;
		private long POLIZADET_ID;
		private long POLIZA_ID;
		private BigDecimal BASE_CALCULADA;
		public String getPROVEEDOR() {
			return PROVEEDOR;
		}
		public void setPROVEEDOR(String pROVEEDOR) {
			PROVEEDOR = pROVEEDOR;
		}
		public String getORIGEN() {
			return ORIGEN;
		}
		public void setORIGEN(String oRIGEN) {
			ORIGEN = oRIGEN;
		}
		public String getTIPO() {
			return TIPO;
		}
		public void setTIPO(String tIPO) {
			TIPO = tIPO;
		}
		public long getPOLIZA() {
			return POLIZA;
		}
		public void setPOLIZA(long pOLIZA) {
			POLIZA = pOLIZA;
		}
		public Date getFECHA() {
			return FECHA;
		}
		public void setFECHA(Date fECHA) {
			FECHA = fECHA;
		}
		public BigDecimal getDEBE() {
			if(DEBE==null)
				DEBE=BigDecimal.ZERO;
			return DEBE;
		}
		public void setDEBE(BigDecimal dEBE) {
			DEBE = dEBE;
		}
		public BigDecimal getHABER() {
			if(HABER==null){
				HABER=BigDecimal.ZERO;
			}
			return HABER;
		}
		public void setHABER(BigDecimal hABER) {
			HABER = hABER;
		}
		public String getCLASE() {
			return CLASE;
		}
		public void setCLASE(String cLASE) {
			CLASE = cLASE;
		}
		public String getASIENTO() {
			return ASIENTO;
		}
		public void setASIENTO(String aSIENTO) {
			ASIENTO = aSIENTO;
		}
		public String getCTA_DESCRIPCION() {
			return CTA_DESCRIPCION;
		}
		public void setCTA_DESCRIPCION(String cTA_DESCRIPCION) {
			CTA_DESCRIPCION = cTA_DESCRIPCION;
		}
		public String getCONC_DESCRIPCION() {
			return CONC_DESCRIPCION;
		}
		public void setCONC_DESCRIPCION(String cONC_DESCRIPCION) {
			CONC_DESCRIPCION = cONC_DESCRIPCION;
		}
		public String getCUENTA() {
			return CUENTA;
		}
		public void setCUENTA(String cUENTA) {
			CUENTA = cUENTA;
		}
		public long getCONCEPTO_ID() {
			return CONCEPTO_ID;
		}
		public void setCONCEPTO_ID(long cONCEPTO_ID) {
			CONCEPTO_ID = cONCEPTO_ID;
		}
		public String getCONCEPTO() {
			return CONCEPTO;
		}
		public void setCONCEPTO(String cONCEPTO) {
			CONCEPTO = cONCEPTO;
		}
		public String getDESCRIPCION2() {
			return DESCRIPCION2;
		}
		public void setDESCRIPCION2(String dESCRIPCION2) {
			DESCRIPCION2 = dESCRIPCION2;
		}
		public String getREFERENCIA2() {
			return REFERENCIA2;
		}
		public void setREFERENCIA2(String rEFERENCIA2) {
			REFERENCIA2 = rEFERENCIA2;
		}
		public long getPOLIZADET_ID() {
			return POLIZADET_ID;
		}
		public void setPOLIZADET_ID(long pOLIZADET_ID) {
			POLIZADET_ID = pOLIZADET_ID;
		}
		public long getPOLIZA_ID() {
			return POLIZA_ID;
		}
		public void setPOLIZA_ID(long pOLIZA_ID) {
			POLIZA_ID = pOLIZA_ID;
		}
		public BigDecimal getBASE_CALCULADA() {
			return BASE_CALCULADA;
		}
		public void setBASE_CALCULADA(BigDecimal bASE_CALCULADA) {
			BASE_CALCULADA = bASE_CALCULADA;
		}
		
		
		public BigDecimal getIetu(){
			if(getCUENTA().equals("900"))
				return getDEBE().subtract(getHABER());
			return BigDecimal.ZERO;
		}
		
		public BigDecimal getIVA(){
			if(getCUENTA().equals("117"))
				return getDEBE();
			return BigDecimal.ZERO;
		}
		
		public BigDecimal getBase(){
			if(getCUENTA().equals("117"))
				return getBASE_CALCULADA();
			return BigDecimal.ZERO;
		}
		
		public BigDecimal getExento(){
			BigDecimal exento=BigDecimal.ZERO;
			BigDecimal ietu=BigDecimal.ZERO;
			if(getCUENTA().equals("900") && getCONCEPTO_ID()!=423L){
				ietu= getDEBE().subtract(getHABER());
			}
			exento=ietu.subtract(getBase());
			return exento;
		}
		
	}

	public static void main(String[] args) {
		String path=ClassUtils.addResourcePathToPackagePath(DetalleDeIvaAcreditablePanel.class, "DetalleIvaAcreditable_IETU.sql");
		String sql=SQLUtils.loadSQLQueryFromResource(path);
		SQLUtils.printColumnNames(sql);
		
	}

}
