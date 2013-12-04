package com.luxsoft.siipap.gastos.consultas;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingWorker;
import javax.swing.table.TableModel;

import org.apache.commons.lang.StringUtils;
import org.jdesktop.swingx.JXTable;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.util.Assert;

import ca.odell.glazedlists.event.ListEvent;
import ca.odell.glazedlists.event.ListEventListener;
import ca.odell.glazedlists.matchers.AbstractMatcherEditor;
import ca.odell.glazedlists.matchers.Matcher;
import ca.odell.glazedlists.matchers.Matchers;
import ca.odell.glazedlists.matchers.TextMatcherEditor;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;



import com.luxsoft.siipap.service.ServiceLocator2;
import com.luxsoft.siipap.swing.browser.FilteredBrowserPanel;
import com.luxsoft.siipap.swing.reports.ReportUtils;
import com.luxsoft.siipap.swing.utils.CommandUtils;
import com.luxsoft.siipap.swing.utils.TaskUtils;
import com.luxsoft.siipap.util.SQLUtils;

/**
 * Consulta de analisis de gastos utilizando SQL directo
 * 
 * @author Ruben Cancino
 *
 */
public class AnalisisDeGastos2Panel extends FilteredBrowserPanel<Analisis2>{
	
	private FacturasMatcherEditor facturasMatcher;
	private OpcionesMatcherEditor opcionesMatcherEditor;

	public AnalisisDeGastos2Panel() {
		super(Analisis2.class);
		addProperty("CLASE_ID","REF_CONTABLE","DESCRIP_RUBRO","SUCURSAL","NOMBRE","COMPRA_ID","F_COMPRA","GCOMPRADET_ID","DESCRIPCION"
				,"IMPORTE","IMPUESTO_IMP","RET1_IMPP","RET2_IMP","TOTAL","TOT_COMP","IETU","INVERSION"
				,"DOCUMENTO","F_DOCTO","TOT_FACT","ORIGEN","REQUISICION_ID","F_REQ","TOT_REQ","F_PAGO"
				,"FORMADP","REFERENCIA","TOT_PAG","BANCO");
		
		facturasMatcher=new FacturasMatcherEditor();
		installCustomMatcherEditor("Selección", facturasMatcher.getSelector(), facturasMatcher);
		
		opcionesMatcherEditor=new OpcionesMatcherEditor();
		installCustomMatcherEditor("Opciones", opcionesMatcherEditor.getSelector(), opcionesMatcherEditor);
		installTextComponentMatcherEditor("Cuenta", "REF_CONTABLE").setMode(TextMatcherEditor.STARTS_WITH);
		installTextComponentMatcherEditor("Rubro", "DESCRIP_RUBRO");
		installTextComponentMatcherEditor("Sucursal", "SUCURSAL");
		installTextComponentMatcherEditor("Proveedor", "NOMBRE");
		installTextComponentMatcherEditor("Producto", "DESCRIPCION");
		installTextComponentMatcherEditor("Compra", "COMPRA_ID");
		installTextComponentMatcherEditor("Documento", "DOCUMENTO");
		installTextComponentMatcherEditor("Origen", "ORIGEN");
		installTextComponentMatcherEditor("Forma Pago", "FORMADP");
		installTextComponentMatcherEditor("Cheque", "REFERENCIA");
		installTextComponentMatcherEditor("Banco", "BANCO");
		installTextComponentMatcherEditor("Mes", "mes");
	}
	
	
	protected void adjustMainGrid(final JXTable grid){
	}

	

	@Override
	protected void executeLoadWorker(SwingWorker worker) {
		TaskUtils.executeSwingWorker(worker);
	}


	@Override
	protected JComponent buildContent() {
		JSplitPane sp=new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		sp.setTopComponent(super.buildContent());
		sp.setBottomComponent(buildDetailComponent());
		sp.setResizeWeight(.65);
		sp.setOneTouchExpandable(true);
		return sp;
	}
	
	private JTabbedPane detailPanel;
	
	private JComponent buildDetailComponent(){
		detailPanel=new JTabbedPane();
		
		return detailPanel;
	}
	
	

	@Override
	public Action[] getActions() {
		if(actions==null)
			actions=new Action[]{getLoadAction()};
		return actions;
	}


	@Override
	protected List<Analisis2> findData() {
		String sql=SQLUtils.loadSQLQueryFromResource("sql/gastos/analisisGaostos2.sql");
		BeanPropertyRowMapper mapper=new BeanPropertyRowMapper(Analisis2.class){

			@Override
			public Object mapRow(ResultSet rs, int rowNumber)throws SQLException {
				Analisis2 res=(Analisis2) super.mapRow(rs, rowNumber);
				res.setF_DOCTO(rs.getDate("F_DOCTO"));
				res.setF_COMPRA(rs.getDate("F_COMPRA"));
				res.setF_REQ(rs.getDate("F_REQ"));
				res.setF_PAGO(rs.getDate("F_PAGO"));
				return res;
			}
			
		};
		if(periodo==null){
			manejarPeriodo();
		}
		return ServiceLocator2.getJdbcTemplate().query(sql,new Object[]{periodo.getFechaInicial(),periodo.getFechaFinal()}, mapper);
	}
	
	private JPanel totalPanel;
	private JTextField importe;
	private JTextField impuesto;
	private JTextField ret1;
	private JTextField ret2;
	private JTextField total;
	
	@SuppressWarnings("unchecked")
	public JPanel getTotalesPanel(){
		if(totalPanel==null){
			final FormLayout layout=new FormLayout("p,2dlu,f:max(100dlu;p):g","");
			DefaultFormBuilder builder=new DefaultFormBuilder(layout);
			importe=createSumaryField();
			impuesto=createSumaryField();
			ret1=createSumaryField();
			ret2=createSumaryField();
			total=createSumaryField();
			
			builder.append("Importe",importe);
			builder.append("Impuesto",impuesto);
			builder.append("Ret 1",ret1);
			builder.append("Ret 2",ret2);
			builder.append("Total",total);
			
			totalPanel=builder.getPanel();
			totalPanel.setOpaque(false);
			getFilteredSource().addListEventListener(new TotalesHandler());
			
		}
		return totalPanel;
	}
	
	protected JTextField createSumaryField(){
		JTextField t=new JTextField();
		t.setHorizontalAlignment(SwingConstants.RIGHT);
		return t;
	}
	
	/******** Report framework ***/
	
	public TableModel getTableModel(){
		return grid.getModel();
	}

	List<Action> reportActions;
	
	public List<Action> getReportActions(){
		if(reportActions==null){
			reportActions=new ArrayList<Action>();
			addCustomReports(reportActions);
		}
		return reportActions;
	}
	
	protected void addCustomReports(List<Action> actions){
		actions.add(createReportAction("reporte1", "Gastos globales", "PorGasto", getTableModel()));
	}
	
	protected Action createReportAction(String id,String name,final String reportName,final TableModel tm){
		Action action=new AbstractAction(name){
			public void actionPerformed(ActionEvent e) {
				ejecutarReporte(reportName,tm);
			}			
		};
		CommandUtils.configAction(action, id, null);
		if(name!=null)
			action.putValue(Action.NAME, name);
		return action;
		
	}
	
	protected void ejecutarReporte(String reportId,final TableModel tm){
		String reportName=reportId+".jasper";
		Map params=getParametros();
		Assert.notEmpty(params);
		Assert.notNull(tm);
		ReportUtils.viewReport(reportName, getParametros(), tm);
	}
	
	protected Map getParametros(){
		Map params=new HashMap();
		for(Map.Entry<String, JComponent> entry:textEditors.entrySet()){
			JComponent c=(JComponent)entry.getValue();
			if(c instanceof JTextField){
				params.put(entry.getKey().toUpperCase(), ((JTextField)c).getText());
			}
			
		}
		if(periodo!=null){
			params.put("FECHA_INI", periodo.getFechaInicial());
			params.put("FECHA_FIN", periodo.getFechaFinal());
		}
		return params;
	}

	/*** En report framework ***/
	
	private NumberFormat nf=NumberFormat.getCurrencyInstance(Locale.US);
	
	private void updateTotales(){
		BigDecimal importe=BigDecimal.ZERO;
		BigDecimal impuesto=BigDecimal.ZERO;
		BigDecimal total=BigDecimal.ZERO;
		
		BigDecimal ret1=BigDecimal.ZERO;
		BigDecimal ret2=BigDecimal.ZERO;
		
		for(Object  r:getFilteredSource()){
			Analisis2 c=(Analisis2)r;
			
			BigDecimal im1=c.getIMPORTE()!=null?c.getIMPORTE():BigDecimal.ZERO;
			importe=importe.add(im1);
			
			BigDecimal impuestot=c.getIMPUESTO_IMP()!=null?c.getIMPUESTO_IMP():BigDecimal.ZERO;
			impuesto=impuesto.add(impuestot);
			
			BigDecimal rett1=c.getRET1_IMPP()!=null?c.getRET1_IMPP():BigDecimal.ZERO;
			ret1=ret1.add(rett1);
			
			BigDecimal rett2=c.getRET2_IMP()!=null?c.getRET2_IMP():BigDecimal.ZERO;
			ret2=ret2.add(rett2);
		}
		total=importe.add(impuesto).add(ret1).add(ret2);
		this.importe.setText(nf.format(importe.doubleValue()));
		this.impuesto.setText(nf.format(impuesto.doubleValue()));
		this.ret1.setText(nf.format(ret1.doubleValue()));
		this.ret2.setText(nf.format(ret2.doubleValue()));
		this.total.setText(nf.format(total.doubleValue()));
		
	}
	

	private class TotalesHandler implements ListEventListener{
		public void listChanged(ListEvent listChanges) {
			if(listChanges.next()){
				updateTotales();
			}
		}
		
	}
	
	private class FacturasMatcherEditor extends AbstractMatcherEditor<Analisis2> implements ActionListener{
		
		private JComboBox selector;
		
		public FacturasMatcherEditor(){
			String[] vals={"Todos","Con CXPFactura","Sin CXPFactura","Con Pago","Sin Pago"};
			selector=new JComboBox(vals);
			selector.addActionListener(this);
		}
		
		public JComboBox getSelector(){
			return selector;
		}

		public void actionPerformed(ActionEvent e) {
			String val=selector.getSelectedItem().toString();
			if("Todos".equals(val)){
				fireMatchAll();
			}else if("Con CXPFactura".equalsIgnoreCase(val)){
				fireChanged(new ConFacturaMatcher());
						
			}else if("Sin CXPFactura".equalsIgnoreCase(val)){
				fireChanged(Matchers.invert(new ConFacturaMatcher()));
			}else if("Con Pago".equalsIgnoreCase(val)){
				fireChanged(new ConPagoMatcher());
			}else if("Sin Pago".equalsIgnoreCase(val)){
				fireChanged(Matchers.invert(new ConPagoMatcher()));
			}
			else
				fireMatchAll();
				
			
		}
		
		class ConFacturaMatcher implements Matcher<Analisis2> {
			public boolean matches(Analisis2 item) {								
				return !StringUtils.isBlank(item.getDOCUMENTO());
			}
		};
		
		class ConPagoMatcher implements Matcher<Analisis2> {
			public boolean matches(Analisis2 item) {								
				return !StringUtils.isBlank(item.getREFERENCIA());
			}
		};
		
				
		
	}
	
	
	private class OpcionesMatcherEditor extends AbstractMatcherEditor<Analisis2> implements ActionListener{
		
		private JComboBox selector;
		
		public OpcionesMatcherEditor(){
			String[] vals={"Todos","Gastos","Inversion","Anticipos","IETU"};
			selector=new JComboBox(vals);
			selector.addActionListener(this);
		}
		
		public JComboBox getSelector(){
			return selector;
		}

		public void actionPerformed(ActionEvent e) {
			String val=selector.getSelectedItem().toString();
			if("Todos".equals(val)){
				fireMatchAll();
			}else if("Gastos".equalsIgnoreCase(val)){
				fireChanged(new GastosMatcher());						
			}else if("Inversion".equalsIgnoreCase(val)){
				fireChanged(new InversionMatcher());
			}else if("Anticipos".equalsIgnoreCase(val)){
				fireChanged(new AnticiposMatcher());
			}else if("IETU".equalsIgnoreCase(val)){
				fireChanged(new IetuMatcher());
			}
			else
				fireMatchAll();
			
		}
		
		class GastosMatcher implements Matcher<Analisis2> {
			public boolean matches(Analisis2 item) {
				if(item.getORIGEN().equalsIgnoreCase("GASTOS")){
					if(item.getINVERSION()==null)
						return true;
					return (item.getINVERSION().doubleValue()==0);
				}
				return false;
			}
		};
		
		class InversionMatcher implements Matcher<Analisis2> {
			public boolean matches(Analisis2 item) {
				if(item.getORIGEN().equalsIgnoreCase("GASTOS")){
					if(item.getINVERSION()==null)
						return false;
					return (item.getINVERSION().doubleValue()==1);
				}
				return false;
			}
		};
		
		class IetuMatcher implements Matcher<Analisis2> {
			public boolean matches(Analisis2 item) {
				if(item.getORIGEN().equalsIgnoreCase("GASTOS")){
					return (item.getIETU().doubleValue()>0);					
				}
				return false;
			}
		};
		
		class AnticiposMatcher implements Matcher<Analisis2> {
			public boolean matches(Analisis2 item) {
				if(item.getORIGEN().equalsIgnoreCase("GASTOS")){
					return (item.getCOMPRA_ID()==null);
				}
				return false;
				
			}
		};
				
		
	}
	
	public static void main(String[] args) {
		String sql=SQLUtils.loadSQLQueryFromResource("sql/gastos/analisisGaostos2.sql");
		System.out.println(sql);
		//SQLUtils.printBeanClasFromSQL(sql);
		SQLUtils.printColumnNames(sql);
		//List data=ServiceLocator2.getJdbcTemplate().queryForList(sql);
		//System.out.println("Data: "+data.size());
		
		
	}
	

}
