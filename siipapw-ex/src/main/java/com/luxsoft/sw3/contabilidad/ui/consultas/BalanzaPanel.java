package com.luxsoft.sw3.contabilidad.ui.consultas;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.EventHandler;
import java.io.IOException;
import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.MessageFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.SwingWorker;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.data.JRTableModelDataSource;
import net.sf.jasperreports.view.JRViewer;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.jdesktop.swingx.JXTable;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.CollectionList.Model;
import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.FilterList;
import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.event.ListEvent;
import ca.odell.glazedlists.event.ListEventListener;
import ca.odell.glazedlists.gui.TableFormat;
import ca.odell.glazedlists.matchers.AbstractMatcherEditor;
import ca.odell.glazedlists.matchers.CompositeMatcherEditor;
import ca.odell.glazedlists.matchers.Matcher;
import ca.odell.glazedlists.matchers.MatcherEditor;
import ca.odell.glazedlists.swing.TextComponentMatcherEditor;

import com.jgoodies.binding.value.ValueHolder;
import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.uif.AbstractDialog;
import com.jgoodies.uifextras.util.ActionLabel;
import com.luxsoft.siipap.model.Periodo;

import com.luxsoft.siipap.service.ServiceLocator2;
import com.luxsoft.siipap.swing.binding.Binder;
import com.luxsoft.siipap.swing.browser.AbstractMasterDatailFilteredBrowserPanel;
import com.luxsoft.siipap.swing.controls.AbstractControl;
import com.luxsoft.siipap.swing.controls.SXAbstractDialog;
import com.luxsoft.siipap.swing.reports.ReportUtils;
import com.luxsoft.siipap.swing.utils.CommandUtils;
import com.luxsoft.siipap.swing.utils.MessageUtils;
import com.luxsoft.siipap.swing.utils.TaskUtils;
import com.luxsoft.siipap.util.DateUtil;
import com.luxsoft.sw3.contabilidad.model.CuentaContable;
import com.luxsoft.sw3.contabilidad.model.SaldoDeCuenta;
import com.luxsoft.sw3.contabilidad.model.SaldoDeCuentaPorConcepto;
import com.luxsoft.sw3.contabilidad.ui.tasks.ReclasificacionPorCuentaBrowser;

public class BalanzaPanel extends AbstractMasterDatailFilteredBrowserPanel<SaldoDeCuenta,SaldoDeCuentaPorConcepto>{

	public BalanzaPanel() {
		super(SaldoDeCuenta.class);
		setTitle("Balanza");
	}
	
	@Override
	protected void agregarMasterProperties() {
		addProperty(
				"cuenta.cuentaSat.codigo"
				,"cuenta.clave"
				,"cuenta.descripcion"
				,"saldoInicial"
				,"debe"
				,"haber"
				,"saldoFinal"
				,"year"
				,"mes"
				);
		addLabels(
				"Codigo Sat"
				,"Cuenta"
				,"Descripcion"
				,"S. Inicial"
				,"debe"
				,"haber"
				,"S. Final"
				,"Año"
				,"Mes"
				);
		CuentasMatcherEditor cuentaEditor=new CuentasMatcherEditor();
		installCustomMatcherEditor("Cuenta", cuentaEditor.getField(),cuentaEditor);
		installTextComponentMatcherEditor("Cuenta", "cuenta.clave");
		installTextComponentMatcherEditor("Descripcion", "cuenta.descripcion");
		manejarPeriodo();
	}
	protected void manejarPeriodo(){
		periodo=Periodo.getPeriodoDelMesActual(new Date());
	}

	@Override
	protected TableFormat createDetailTableFormat() {
		String[] props={"concepto.cuentaSat.codigo","concepto.subcuenta","concepto.descripcion","saldoInicial","debe","haber","saldoFinal","year","mes"};
		String[] names={"Codigo sat","Cuenta","Concepto","Inicial","Debe","Haber","Final","year","mes"};
		return GlazedLists.tableFormat(SaldoDeCuentaPorConcepto.class, props,names);
	}
	

	@Override
	protected Model<SaldoDeCuenta, SaldoDeCuentaPorConcepto> createPartidasModel() {
		return new Model<SaldoDeCuenta,SaldoDeCuentaPorConcepto>(){
			public List<SaldoDeCuentaPorConcepto> getChildren(SaldoDeCuenta parent) {
				String hql="from SaldoDeCuentaPorConcepto s where s.concepto.cuenta.id=? " +
						"	and  s.year=? and s.mes=?";
				
				Object[] params=new Object[]{
						parent.getCuenta().getId()
						,Periodo.obtenerYear(periodo.getFechaInicial())
						,Periodo.obtenerMes(periodo.getFechaFinal())+1
						};
				return ServiceLocator2.getHibernateTemplate().find(hql,params);
			}
		};
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
	protected void adjustDetailGrid(final JXTable grid){
		grid.addMouseListener(new MouseAdapter(){			
			public void mouseClicked(MouseEvent e) {
				if(e.getClickCount()==2)
					selectIntoDetail();
			}			
		});
	}
	
	@Override
	public void select() {
		if(getSelectedObject()!=null){
			SaldoDeCuenta saldo=(SaldoDeCuenta)getSelectedObject();
			AnalisisDeBalanzaPorCuentaPanel.show(saldo.getYear(), saldo.getMes(), saldo.getCuenta().getClave());
		}
	}
	
	public void selectIntoDetail(){
		if(!detailSelectionModel.getSelected().isEmpty()){
			drill((SaldoDeCuentaPorConcepto)detailSelectionModel.getSelected().get(0));
		}
	}
	
	
	
	
	/**
	 * TemplateMethod para personalizar en las sub-clases el comportamiento del taladreo
	 * 
	 * @param det
	 */
	public void drill(SaldoDeCuentaPorConcepto det){
		String titulo="Cuenta: {0} {1} Concepto: {2} ({3})";
		titulo=MessageFormat.format(titulo
				, det.getConcepto().getCuenta().getClave()
				, det.getConcepto().getCuenta().getDescripcion()
				, det.getConcepto().getDescripcion()
				, det.getConcepto().getClave()
				);
		String cuenta=det.getConcepto().getCuenta().getClave()+ " "+det.getConcepto().getDescripcion();
		String concepto=det.getConcepto().toString();
		AnalisisDeBalanzaDetPanel.show(
				det.getYear()
				, det.getMes()
				, det.getConcepto().getId()
				,concepto
				,cuenta);
	}
	

	@Override
	protected void executeLoadWorker(SwingWorker worker) {
		TaskUtils.executeSwingWorker(worker);
	}

	@Override
	protected List<SaldoDeCuenta> findData() {
		String hql="from SaldoDeCuenta s where s.year=? and s.mes=?";
		Object[] values={Periodo.obtenerYear(periodo.getFechaFinal()),Periodo.obtenerMes(periodo.getFechaFinal())+1};
		return ServiceLocator2.getHibernateTemplate().find(hql, values);
	}
	
	@Override
	protected void afterLoad() {
		super.afterLoad();
		totalPanel.updateTotales();
	}
	@Override
	public Action[] getActions() {
		if(actions==null){
			Action printAction=CommandUtils.createPrintAction(this, "imprimirBalanza");
			printAction.putValue(Action.SHORT_DESCRIPTION, "Balanza de comprobación");
			actions=new Action[]{
				getLoadAction()
				,printAction
				,addAction(null, "recalcularSaldos", "Recalcular Saldos")
				,addAction(null,"reporteDeBalanzaMayor","Imprimir Balanza")
				,addAction(null,"reporteEstadoDeResutlados","Estado de resultados")
				,addAction(null,"reporteBalanceGeneral","Balance General")
				,addAction(null,"reporteBalanzaDeComprobacion","Balanza De Comprobación")
				,addAction(null,"reclasificacion","Reclasificación")
				//,addAction(null,"cierreAnual","Cierre Anual")
				//,getDeleteAction()
				//,getEditAction()
				//,getViewAction()
				};
		}
		return actions;
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
	
	private JTextField concepto=new JTextField(5);
	private JTextField cuenta=new JTextField(5);
	
	
	
	
	protected void installDetailFilterComponents(DefaultFormBuilder builder){
		builder.appendSeparator("Detalle");
		builder.append("Concepto ",concepto);
		builder.append("Cuenta ",cuenta);
		
	}
	
	protected EventList<SaldoDeCuentaPorConcepto> partidasFiltered;
	protected EventList<SaldoDeCuentaPorConcepto> partidasSource;
	
	protected EventList decorateDetailList( EventList data){
		partidasSource=data;
		EventList<MatcherEditor> editors=new BasicEventList<MatcherEditor>();
		editors.add(new TextComponentMatcherEditor(concepto,GlazedLists.textFilterator("concepto.descripcion")));
		editors.add(new TextComponentMatcherEditor(cuenta,GlazedLists.textFilterator("concepto.cuenta.clave")));
		CompositeMatcherEditor matcherEditor=new CompositeMatcherEditor(editors);
		FilterList detailFilter=new FilterList(data,matcherEditor);
		partidasFiltered=detailFilter;
		return detailFilter;
	}
	
	public void reporteEstadoDeResutlados(){
		final ValueHolder yearModel=new ValueHolder(getYear());
		final ValueHolder mesModel=new ValueHolder(getMes());
		AbstractDialog dialog=Binder.createSelectorMesYear(yearModel, mesModel);
		dialog.open();
		if(!dialog.hasBeenCanceled()){
			//Periodo per=Periodo.getPeriodoEnUnMes(mesModel.intValue()-1, yearModel.intValue());
			Map map=new HashMap();
			map.put("YEAR", yearModel.intValue());
			map.put("MES", mesModel.intValue());
			String path=ReportUtils.toReportesPath("contabilidad/EstadoDeResultados.jasper");
			if(ReportUtils.existe(path))
				ReportUtils.viewReport(path, map);
			else
				JOptionPane.showMessageDialog(this.getControl()
						,MessageFormat.format("El reporte:\n {0} no existe",path),"Reportes",JOptionPane.ERROR_MESSAGE);
	        
	        
		}
	}
	
	public void reporteBalanceGeneral(){
		final ValueHolder yearModel=new ValueHolder(getYear());
		final ValueHolder mesModel=new ValueHolder(getMes());
		AbstractDialog dialog=Binder.createSelectorMesYear(yearModel, mesModel);
		dialog.open();
		if(!dialog.hasBeenCanceled()){
			//Periodo per=Periodo.getPeriodoEnUnMes(mesModel.intValue()-1, yearModel.intValue());
			Map map=new HashMap();
			map.put("YEAR", yearModel.intValue());
			map.put("MES", mesModel.intValue());
			String path=ReportUtils.toReportesPath("contabilidad/BalanceGeneral.jasper");
			if(ReportUtils.existe(path))
				ReportUtils.viewReport(path, map);
			else
				JOptionPane.showMessageDialog(this.getControl()
						,MessageFormat.format("El reporte:\n {0} no existe",path),"Reportes",JOptionPane.ERROR_MESSAGE);
	        
		}
	}
	
	
	public void reporteBalanzaDeComprobacion(){
		final ValueHolder yearModel=new ValueHolder(getYear());
		final ValueHolder mesModel=new ValueHolder(getMes());
		AbstractDialog dialog=Binder.createSelectorMesYearContable(yearModel, mesModel);
		dialog.open();
		if(!dialog.hasBeenCanceled()){
			//Periodo per=Periodo.getPeriodoEnUnMes(mesModel.intValue()-1, yearModel.intValue());
			
			Map map=new HashMap();
			map.put("YEAR", yearModel.intValue());
			map.put("MES", mesModel.intValue());
			String path=ReportUtils.toReportesPath("contabilidad/BalanzaDeComprobacion.jasper");
			if(ReportUtils.existe(path))
				ReportUtils.viewReport(path, map);
			else
				JOptionPane.showMessageDialog(this.getControl()
						,MessageFormat.format("El reporte:\n {0} no existe",path),"Reportes",JOptionPane.ERROR_MESSAGE);
	        
		}
	}
	
	private TotalesPanel totalPanel;
	
	public JPanel getTotalesPanel(){
		if(totalPanel==null){
			totalPanel=new TotalesPanel();
			partidasFiltered.addListEventListener(totalPanel);
			this.detailSortedList.addListEventListener(totalPanel);
		}
		return (JPanel)totalPanel.getControl();
	}
	
	
	protected class TotalesPanel extends AbstractControl implements ListEventListener{
	
		private JLabel saldoInicial;
		private JLabel totalDebe;
		private JLabel totalHaber;
		private JLabel saldoFinal;
		private JLabel cuadre;
		NumberFormat nf=NumberFormat.getCurrencyInstance();
		

		@Override
		protected JComponent buildContent() {
			
			totalDebe=new JLabel();
			totalDebe.setHorizontalAlignment(JLabel.RIGHT);
			
			totalHaber=new JLabel();
			totalHaber.setHorizontalAlignment(JLabel.RIGHT);
			cuadre=new JLabel();
			cuadre.setHorizontalAlignment(JLabel.RIGHT);
			
			saldoInicial=new JLabel();
			saldoInicial.setHorizontalAlignment(JLabel.RIGHT);
			
			saldoFinal=new JLabel();
			saldoFinal.setHorizontalAlignment(JLabel.RIGHT);
			
			FormLayout layout=new FormLayout("p,2dlu,f:50dlu:g","");
			final DefaultFormBuilder builder=new DefaultFormBuilder(layout);
			//porPoliza.setEnabled(false);
			
			builder.append("Saldo Inicial:",saldoInicial);
			builder.append("Debe:",totalDebe);
			builder.append("Haber: ",totalHaber);			
			builder.append("Saldo Final:",saldoFinal);
			builder.append("Cuadre: ",cuadre);
			builder.getPanel().setOpaque(false);			
			return builder.getPanel();
		}
		
		BigDecimal debe=BigDecimal.ZERO;
		BigDecimal haber=BigDecimal.ZERO;
		BigDecimal saldoIni=BigDecimal.ZERO;
		BigDecimal saldoFin=BigDecimal.ZERO;
		
		private void updateTotales(){
			debe=BigDecimal.ZERO;
			haber=BigDecimal.ZERO;
			saldoIni=BigDecimal.ZERO;
			saldoFin=BigDecimal.ZERO;
			
			for(Object o:sortedSource){
				SaldoDeCuenta a=(SaldoDeCuenta)o;
				
				debe=debe.add(a.getDebe());
				haber=haber.add(a.getHaber());
				saldoIni=saldoIni.add(a.getSaldoInicial());
				saldoFin=saldoFin.add(a.getSaldoFinal());
			}
			
			totalDebe.setText(nf.format(debe));
			totalHaber.setText(nf.format(haber));
			cuadre.setText( nf.format(debe.subtract(haber)) );
			saldoInicial.setText (nf.format(saldoIni));
			saldoFinal.setText( nf.format(saldoFin));
		}
		
		
		public void listChanged(ListEvent listChanges) {
			if(listChanges.hasNext()){
				updateTotales();
			}
		}
	}
	
	private List<CuentaContable> getSelectedCuentas(){
		List<CuentaContable> cuentas=new ArrayList<CuentaContable>();
		for(Object o:getSelected()){
			SaldoDeCuenta s=(SaldoDeCuenta)o;
			cuentas.add(s.getCuenta());
		}
		return cuentas;
	}
	
	public void recalcularSaldos(){
		recalcularTodasLasCuentas();
	}
	
	public void reclasificacion(){
		List<CuentaContable> cuentas=getSelectedCuentas();
		if(!cuentas.isEmpty()){
			Date fecha=periodo.getFechaFinal();
			int mes=Periodo.obtenerMes(fecha)+1;
			int year=Periodo.obtenerYear(fecha);
			for(CuentaContable cuenta:cuentas){
				final ReclasificacionPorCuentaBrowser browser=new ReclasificacionPorCuentaBrowser(year,mes,cuenta.getClave());
				final FilterBrowserDialog dialog=new FilterBrowserDialog(browser);
				dialog.setTitle(MessageFormat.format("Movimientos   Cuenta: {0}  Periodo: {1}/{2}",cuenta,mes,year));
				dialog.open();
				recalcularTodasLasCuentas();
			}
		}
	}
	
	public void cierreAnual(){
		Date fecha=periodo.getFechaFinal();
		int mes=Periodo.obtenerMes(fecha)+1;
		int year=Periodo.obtenerYear(fecha);
		if(mes!=12){
			MessageUtils.showMessage("El cierre anual solo aplica para el último mes del ejercicio", "Cierre anual");
			return;
		}else{
			System.out.println("Procesando cierre anual...");
			ServiceLocator2.getCierreAnualManager().recalcularSaldos(year);
			
			
		}
	}
	
	public void reporteDeBalanzaMayor(){
		ReporteDeBalanzaMayor r=new ReporteDeBalanzaMayor();
		r.open();
	}
	
	private class ReporteDeBalanzaMayor extends SXAbstractDialog{
		
		public  ReporteDeBalanzaMayor() {
			super("Reporte...");
			
		}

		public JComponent displayReport(){
			Map<String, Object>parametros=new HashMap<String, Object>();
		      parametros.put("YEAR",Integer.valueOf(getYear()));
		      parametros.put("MES",DateUtil.getMesAsString(getMes()));
			  parametros.put("FECHA_INI",periodo.getFechaInicial());
			  parametros.put("FECHA_FIN",periodo.getFechaFinal());
			  parametros.put("INICIAL",totalPanel.saldoIni);
			  parametros.put("CARGOS",totalPanel.debe);
			  parametros.put("ABONOS",totalPanel.haber);
			  parametros.put("SALDO",totalPanel.saldoFin);
                net.sf.jasperreports.engine.JasperPrint jasperPrint = null;
                DefaultResourceLoader loader = new DefaultResourceLoader();
                Resource res = loader.getResource(ReportUtils.toReportesPath("contabilidad/BalanzaMayor.jasper"));
                try
                {
                    java.io.InputStream io = res.getInputStream();
                    try
                    {
                    	//JTable table=getGrid();
                        jasperPrint = JasperFillManager.fillReport(io, parametros, new JRTableModelDataSource(grid.getModel()));
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
	
	public void recalcularTodasLasCuentas(){
		final SwingWorker worker=new SwingWorker(){
			protected Object doInBackground() throws Exception {
				Date fecha=periodo.getFechaFinal();
				int mes=Periodo.obtenerMes(fecha)+1;
				int year=Periodo.obtenerYear(fecha);
				Object[] params=new Object[]{year,mes};
				ServiceLocator2.getJdbcTemplate().update("DELETE FROM SX_CONTABILIDAD_SALDOSDET WHERE YEAR=? AND  MES=?",params);
				ServiceLocator2.getJdbcTemplate().update("DELETE FROM SX_CONTABILIDAD_SALDOS WHERE YEAR=? AND  MES=?",params);
				ServiceLocator2.getSaldoDeCuentaManager().recalcularSaldos(year, mes);
				return "OK";
			}

			@Override
			protected void done() {
				try {
					get();
					load();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			
			
		};
		executeLoadWorker(worker);
	}
	
	
	public static class CuentasMatcherEditor extends AbstractMatcherEditor implements DocumentListener,ActionListener{
		
		private final JTextField textField;
		
		public CuentasMatcherEditor(){
			textField=new JTextField(10);
			textField.addActionListener(this);
			textField.getDocument().addDocumentListener(this);
		}
		
		public JTextField getField(){
			return textField;
		}

		public void changedUpdate(DocumentEvent e) {filter();}
		public void insertUpdate(DocumentEvent e) {filter();}
		public void removeUpdate(DocumentEvent e) {filter();}
		public void actionPerformed(ActionEvent e) {filter();}
		
		protected void filter(){
			if(StringUtils.isBlank(textField.getText()))
				fireMatchAll();
			else{
				fireChanged(new EXMatcher(textField.getText()));
			}
			
		}
		
		private class  EXMatcher implements Matcher<SaldoDeCuenta>{
			
			private final String[] cuentas;
			
			public EXMatcher(String cuentasString){
				
				this.cuentas=cuentasString.split(",");
			}
			
			public boolean matches(SaldoDeCuenta item) {
				
				return ArrayUtils.contains(cuentas,item.getCuenta().getClave());
			}
			
		}
		
		
	}

}
