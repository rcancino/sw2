package com.luxsoft.siipap.inventario.ui.consultas;

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
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingWorker;

import org.apache.commons.lang.StringUtils;
import org.jdesktop.swingx.JXTable;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.jdbc.core.BeanPropertyRowMapper;

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.CollectionList.Model;
import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.FilterList;
import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.ListSelection;
import ca.odell.glazedlists.TextFilterator;
import ca.odell.glazedlists.event.ListEvent;
import ca.odell.glazedlists.event.ListEventListener;
import ca.odell.glazedlists.gui.TableFormat;
import ca.odell.glazedlists.matchers.CompositeMatcherEditor;
import ca.odell.glazedlists.matchers.Matcher;
import ca.odell.glazedlists.matchers.MatcherEditor;
import ca.odell.glazedlists.matchers.Matchers;
import ca.odell.glazedlists.swing.TextComponentMatcherEditor;

import com.jgoodies.binding.value.ValueHolder;
import com.jgoodies.binding.value.ValueModel;
import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.uif.AbstractDialog;
import com.jgoodies.uifextras.panel.HeaderPanel;
import com.luxsoft.siipap.inventario.InventariosActions;
import com.luxsoft.siipap.inventario.ui.reports.AnalisisMovimientosCosteadosReportForm;
import com.luxsoft.siipap.inventario.ui.reports.FacturasPorProveedorForm;
import com.luxsoft.siipap.inventario.ui.reports.KardexReportForm;
import com.luxsoft.siipap.inventario.ui.reports.Maquila_InventariosCosteadosForm;
import com.luxsoft.siipap.inventario.ui.reports.Maquila_MovimientosCosteadosReportForm;
import com.luxsoft.siipap.inventario.ui.reports.MovimientosCosteadosReportForm;
import com.luxsoft.siipap.inventario.ui.reports.ReportePorDocumentoForm;
import com.luxsoft.siipap.model.CantidadMonetaria;
import com.luxsoft.siipap.model.Periodo;
import com.luxsoft.siipap.reportes.VentasSinCosto;
import com.luxsoft.siipap.reports.VentasXSucReportForm;
import com.luxsoft.siipap.service.ServiceLocator2;
import com.luxsoft.siipap.swing.binding.Binder;
import com.luxsoft.siipap.swing.browser.AbstractMasterDatailFilteredBrowserPanel;
import com.luxsoft.siipap.swing.controls.AbstractControl;
import com.luxsoft.siipap.swing.matchers.CheckBoxMatcher;
import com.luxsoft.siipap.swing.utils.Renderers;
import com.luxsoft.siipap.swing.utils.TaskUtils;
import com.luxsoft.siipap.util.SQLUtils;


public class MovimientosCosteadosPanel extends AbstractMasterDatailFilteredBrowserPanel<MovimientoCosteadoRow,MovimientoCosteadoRowDet>{

	CheckBoxMatcher<MovimientoCosteadoRow> negativosMatcher;
	
	public MovimientosCosteadosPanel() {
		super(MovimientoCosteadoRow.class);		
	}
	
	private ValueModel yearModel;
	
	private ValueModel mesModel;

	
	public Integer getMes(){
		return (Integer)mesModel.getValue();
	}
	public Integer getYear(){
		return (Integer)yearModel.getValue();
	}
	
	private HeaderPanel header;
	
	
	
	@Override
	protected JComponent buildHeader(){
		if(header==null){
			header=new HeaderPanel("Analisis de Movimientos costeados","Año:  Mes:");
			
		}
		return header;
	}
	
	
	
		@Override
	protected void init() {
		Date fecha=new Date();
		yearModel=new ValueHolder(Periodo.obtenerYear(fecha));
		mesModel=new ValueHolder(Periodo.obtenerMes(fecha));
		addProperty(
				"clave"				
				,"costoIni"
				,"saldoInicial"				
				,"SALDO"
				,"UniFin"
				,"difUni"
				,"COSTOP"
				,"calCostoP"
				,"difCostoP"
				,"COSTO"
				,"costoFin"
				,"dif"
				,"descripcion"
				,"comsSinAUni"
				,"comsSinA"
				,"comsUni"
				,"comsCosto"
				,"maqUni"
				,"maqCosto"
				,"servicioCosto"
				,"movsUni"
				,"movsCosto"
				,"decUni"
				,"decCosto"
				,"trasladosUni"
				,"trasladosCosto"
				,"trsSalUni"
				,"trsSalCosto"
				,"trsEntUni"
				,"trsEntCosto"
				,"devUni"
				,"devCosto"
				,"vtaUni"
				,"vtaCosto"
				);
		
		addLabels(
				"Clave"					
				,"Inicial ($)"
				,"Inicial (U)"
				,"Final  (U)"
				,"Inicial+Movs(U)"
				,"Dif (U)"
				,"Costo p/U"
				,"Costo (calc)"
				,"Dif (Costo)"
				,"Costo (inv)"
				,"Costo cal(inv)"
				,"Dif (Costo inv)"
				,"Producto"			
				,"comsSinAUni"
				,"comsSinA"
				,"comsUni"
				,"comsCosto"
				,"maqUni"
				,"maqCosto"
				,"servicioCosto"
				,"movsUni"
				,"movsCosto"
				,"decUni"
				,"decCosto"
				,"trasladosUni"
				,"trasladosCosto"
				,"trsSalUni"
				,"trsSalCosto"
				,"trsEntUni"
				,"trsEntCosto"
				,"devUni"
				,"devCosto"
				,"vtaUni"
				,"vtaCosto"
				);
		installTextComponentMatcherEditor("Producto", "clave","descripcion");
		installTextComponentMatcherEditor("Linea", "linea");
		installTextComponentMatcherEditor("Clase", "clase");
		installTextComponentMatcherEditor("Marca", "marca");
		
		negativosMatcher=new CheckBoxMatcher<MovimientoCosteadoRow>(false) {			
			protected Matcher<MovimientoCosteadoRow> getSelectMatcher(Object... obj) {				
				return new Matcher<MovimientoCosteadoRow>() {					
					public boolean matches(MovimientoCosteadoRow item) {
						return item.getSaldoInicial().doubleValue()<0;
					}					
				};
			}
		};
		installCustomMatcherEditor("Inicial negativo", negativosMatcher.getBox(), negativosMatcher);
		setDetailSelectionMode(ListSelection.SINGLE_SELECTION);
	}
		
	@Override
	protected TableFormat createDetailTableFormat() {
		String[] cols={"GRUPO","TIPO","CLAVE","FECHA","DOCTO","RENGL","NOMBRE","UNIDAD","CANTIDAD","KILOS","COSTO","COSTOP","IMPORTECOSTO","YEAR","MES","COMENTARIO"};
		return GlazedLists.tableFormat(MovimientoCosteadoRowDet.class, cols, cols);
	}
		
	
	@Override
	protected void adjustMainGrid(JXTable grid) {
		grid.getColumnExt("Dif (U)").setCellRenderer(Renderers.buildBoldDecimalRenderer(3));
		grid.getColumnExt("Dif (Costo)").setCellRenderer(Renderers.buildBoldDecimalRenderer(2));
		grid.getColumnExt("Dif (Costo inv)").setCellRenderer(Renderers.buildBoldDecimalRenderer(2));
	}
	@Override
	protected Model<MovimientoCosteadoRow, MovimientoCosteadoRowDet> createPartidasModel() {
		return new Model<MovimientoCosteadoRow, MovimientoCosteadoRowDet>(){
			public List<MovimientoCosteadoRowDet> getChildren(MovimientoCosteadoRow parent) {
				return findDetailData(parent);
			}
		};
	}
	
	@Override
	protected void executeLoadWorker(SwingWorker worker) {
		TaskUtils.executeSwingWorker(worker);
	}
	@Override
	protected void installEditors(EventList editors) {
		final Matcher matcher=Matchers.beanPropertyMatcher(MovimientoCosteadoRow.class, "visible", Boolean.TRUE);		
		editors.add(GlazedLists.fixedMatcherEditor(matcher));
		super.installEditors(editors);
	}
	public void load(){
		AbstractDialog dialog=Binder.createSelectorMesYear(yearModel, mesModel);
		dialog.open();
		if(!dialog.hasBeenCanceled()){
			super.load();
		}
		return;
	}
	
	@Override
	protected void beforeLoad(){
		if(getYear()!=null && getYear()>0)
			header.setDescription("Periodo: "+getYear()+"/"+getMes());
	}
	
	@Override
	protected List<MovimientoCosteadoRow> findData() {
		String sql=SQLUtils.loadSQLQueryFromResource("sql/inventarios/movimientosCosteados_v2.sql");
		int mes=getMes();
		int year=getYear();
		int mes_ini=mes-1;
		int year_ini=year;
		if(mes==1){
			mes_ini=12;
			year_ini=year-1;
		}
		sql=sql.replaceAll("@INI_YEAR", String.valueOf(year_ini));
		sql=sql.replaceAll("@INI_MES", String.valueOf(mes_ini));
		sql=sql.replaceAll("@YEAR", String.valueOf(getYear()));
		sql=sql.replaceAll("@MES", String.valueOf(getMes()));
		
		Periodo periodo=Periodo.getPeriodoEnUnMes(mes-1,getYear());
		DateFormat df=new SimpleDateFormat("yyyy-MM-dd 00:00:00");
		DateFormat df2=new SimpleDateFormat("yyyy-MM-dd 23:00:00");
		
		sql=sql.replaceAll("@FECHA_INI", df.format(periodo.getFechaInicial()));
		sql=sql.replaceAll("@FECHA_FIN", df2.format(periodo.getFechaFinal()));
		
		System.out.println(sql);
		List<MovimientoCosteadoRow> res=ServiceLocator2.getJdbcTemplate().query(sql, new BeanPropertyRowMapper(MovimientoCosteadoRow.class));
		System.out.println("Res: "+res.size());
		return res;
	}
	
	private List<MovimientoCosteadoRowDet> findDetailData(MovimientoCosteadoRow row){
		
		String sql=SQLUtils.loadSQLQueryFromResource("sql/inventarios/movimientosCosteadosDet.sql");
		int mes=getMes();
		int year=getYear();
		int mes_ini=mes-1;
		int year_ini=year;
		if(mes==1){
			mes_ini=12;
			year_ini=year-1;
		}
		sql=sql.replaceAll("@INI_YEAR", String.valueOf(year_ini));
		sql=sql.replaceAll("@INI_MES", String.valueOf(mes_ini));
		sql=sql.replaceAll("@YEAR", String.valueOf(getYear()));
		sql=sql.replaceAll("@MES", String.valueOf(getMes()));
		
		Periodo periodo=Periodo.getPeriodoEnUnMes(mes-1,getYear());
		DateFormat df=new SimpleDateFormat("yyyy-MM-dd 00:00:00");
		DateFormat df2=new SimpleDateFormat("yyyy-MM-dd 23:00:00");
		
		sql=sql.replaceAll("@FECHA_INI", df.format(periodo.getFechaInicial()));
		sql=sql.replaceAll("@FECHA_FIN", df2.format(periodo.getFechaFinal()));
		sql=sql.replaceAll("@CLAVE", "\'"+row.getClave()+"\'");
		System.out.println(sql);
		return ServiceLocator2.getJdbcTemplate().query(sql,  new BeanPropertyRowMapper(MovimientoCosteadoRowDet.class));
	}
	
	@Override
	protected List<Action> createProccessActions() {
		List<Action> procesos=super.createProccessActions();		
		procesos.add(addAction(InventariosActions.CalculoDeCostos.getId(), "costearMovimientos", "Costear Movimientos"));
		return procesos;
	}

	@Override
	public Action[] getActions() {
		if(actions==null)
			actions=new Action[]{
				getLoadAction()				
				,addAction(InventariosActions.ConsultaDeCostosDeInventario.getId(), "reporteDeKardex", "Kardex")
				,addAction(InventariosActions.ConsultaDeCostosDeInventario.getId(), "reporteMovimientosCosteados", "Rep Movs Costeados")
				,addAction(InventariosActions.ConsultaDeCostosDeInventario.getId(), "reporteDeInventarioMaquila", "Rep Inventario Maq")
				,addAction(InventariosActions.ConsultaDeCostosDeInventario.getId(), "reporteDeMovimientosMaquila", "Rep Movs Costeados Maq")
				,addAction(InventariosActions.ConsultaDeCostosDeInventario.getId(), "reporteDeAnalisisDeMovimientos", "Rep Análisis de Movs")
				,addAction(InventariosActions.ConsultaDeCostosDeInventario.getId(), "facturasAnalizadas", "Facturas Analizadas")
				,addAction(InventariosActions.ConsultaDeCostosDeInventario.getId(), "reporteDeMovimiento", "Imprimir Dcto")
				,addAction(InventariosActions.ConsultaDeCostosDeInventario.getId(), "reporteComSinAnalizar", "Coms Sin Analizar")
				,addAction("", "reporteVentasMensuales", "Ventas Mensuales")
				,addAction("", "reporteDeVentasSinCosto", "Ventas Sin Costo Promedio")
				
				};
		return actions;
	}
	
	private JTextField documentField=new JTextField(5);
	private JTextField tipoField=new JTextField(5);
	
	protected void installDetailFilterComponents(DefaultFormBuilder builder){
		builder.appendSeparator("Detalle");
		builder.append("Tipo",tipoField);
		builder.append("Documento",documentField);
		
	}
	
	@Override
	protected EventList decorateDetailList( EventList data){
		EventList<MatcherEditor> editors=new BasicEventList<MatcherEditor>();
		
		
		TextFilterator docFilterator=GlazedLists.textFilterator("DOCTO");
		TextComponentMatcherEditor docEditor=new TextComponentMatcherEditor(documentField,docFilterator);
		editors.add(docEditor);
		
		TextFilterator tipoFilterator=GlazedLists.textFilterator("TIPO");
		TextComponentMatcherEditor tipoEditor=new TextComponentMatcherEditor(tipoField,tipoFilterator);
		editors.add(tipoEditor);
		
		CompositeMatcherEditor matcherEditor=new CompositeMatcherEditor(editors);
		FilterList detailFilter=new FilterList(data,matcherEditor);
		return detailFilter;
	}
	
	
	public void reporteMovimientosCosteados(){
		MovimientosCosteadosReportForm.run();
	}
	
	public void reporteComSinAnalizar(){
		com.luxsoft.siipap.reportes.ComsSinAnalizarReportForm.run();
	}
	
	public void reporteVentasMensuales(){
		VentasXSucReportForm.run();
	}
	
	
	
	public void reporteDeInventarioMaquila(){
		Maquila_InventariosCosteadosForm.run();		
	}
	
	public void reporteDeVentasSinCosto(){
		VentasSinCosto.run();
	}
	
	public void reporteDeMovimientosMaquila(){
		Maquila_MovimientosCosteadosReportForm.run();
	}
	
	public void reporteDeKardex(){
		KardexReportForm.run();
	}
	
	public void reporteDeAnalisisDeMovimientos(){
		AnalisisMovimientosCosteadosReportForm.run();
	}
	public void reporteDeMovimiento(){
		ReportePorDocumentoForm.run();
	}
	
	public void facturasAnalizadas(){
		FacturasPorProveedorForm.run();
	}
	
	private TotalesPanel totalPanel;
	
	public JPanel getTotalesPanel(){
		if(totalPanel==null){
			totalPanel=new TotalesPanel();
		}
		return (JPanel)totalPanel.getControl();
	}
	
	private class TotalesPanel extends AbstractControl implements ListEventListener{
		
		private JLabel costoInicial=new JLabel();
		private JLabel costoFinal=new JLabel();
		private JLabel saldoInicial=new JLabel();
		private JLabel saldoFinal=new JLabel();
		private JLabel diferencias=new JLabel();

		public TotalesPanel() {
			super();
			sortedSource.addListEventListener(this);
		}

		@Override
		protected JComponent buildContent() {
			
			costoInicial.setHorizontalAlignment(JLabel.RIGHT);
			costoFinal.setHorizontalAlignment(JLabel.RIGHT);
			saldoInicial.setHorizontalAlignment(JLabel.RIGHT);
			saldoFinal.setHorizontalAlignment(JLabel.RIGHT);
			diferencias.setHorizontalAlignment(JLabel.RIGHT);
			
			final FormLayout layout=new FormLayout("p,2dlu,f:p:g","");
			DefaultFormBuilder builder=new DefaultFormBuilder(layout);
			costoInicial.setHorizontalAlignment(SwingConstants.RIGHT);
			
			builder.append("Saldo Ini",saldoInicial);
			builder.append("Costo Ini",costoInicial);
			builder.append("Saldo Fin",saldoFinal);
			builder.append("Costo Fin",costoFinal);
			builder.append("Dif:",diferencias);
			
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
		
		NumberFormat nf1=NumberFormat.getCurrencyInstance();
		NumberFormat nf2=NumberFormat.getNumberInstance();
		
		public void updateTotales(){
			//Cantidad monetaria para redondear
			
			CantidadMonetaria costoIni=CantidadMonetaria.pesos(sumarizar("costoIni"));
			CantidadMonetaria costoFin=CantidadMonetaria.pesos(sumarizar("COSTO"));
			BigDecimal saldoIni=sumarizar("saldoInicial");
			BigDecimal saldoFin=sumarizar("SALDO");
			BigDecimal dif=sumarizar("dif");
			costoInicial.setText(nf1.format(costoIni.amount().doubleValue()));
			costoFinal.setText(nf1.format(costoFin.amount().doubleValue()));
			saldoInicial.setText(nf2.format(saldoIni.doubleValue()));
			saldoFinal.setText(nf2.format(saldoFin.doubleValue()));
			diferencias.setText(nf2.format(dif.doubleValue()));
		}
		
		private BigDecimal sumarizar(String property){
			BigDecimal res=BigDecimal.ZERO;
			for(Object bean:sortedSource){
				res=res.add(getValor(bean, property));
			}
			return res;
		}
		
		private BigDecimal getValor(Object bean,String property){
			BigDecimal res=BigDecimal.ZERO;
			BeanWrapperImpl wrapper=new BeanWrapperImpl(bean);
			res=(BigDecimal)wrapper.getPropertyValue(property);
			return res;
		}
		
		private NumberFormat nf=NumberFormat.getPercentInstance();
		
		protected String part(final CantidadMonetaria total,final CantidadMonetaria part){
			
			double res=0;
			if(total.amount().doubleValue()>0){
				res=part.divide(total.amount()).amount().doubleValue();
			}
			return StringUtils.leftPad(nf.format(res),5);
		}
		
	}
	
	

}


