package com.luxsoft.siipap.compras.ui.consultas;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.DateFormat;
import java.text.MessageFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import javax.swing.Action;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingWorker;
import javax.swing.text.NumberFormatter;

import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.lang.time.DateUtils;
import org.jdesktop.swingx.JXDatePicker;
import org.jdesktop.swingx.JXTable;
import org.springframework.jdbc.core.BeanPropertyRowMapper;

import ca.odell.glazedlists.event.ListEvent;
import ca.odell.glazedlists.event.ListEventListener;
import ca.odell.glazedlists.matchers.Matcher;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.uif.builder.ToolBarBuilder;
import com.jgoodies.uifextras.panel.HeaderPanel;

import com.luxsoft.siipap.model.Periodo;
import com.luxsoft.siipap.model.Sucursal;
import com.luxsoft.siipap.service.ServiceLocator2;
import com.luxsoft.siipap.swing.browser.FilteredBrowserPanel;
import com.luxsoft.siipap.swing.controls.AbstractControl;
import com.luxsoft.siipap.swing.controls.SXAbstractDialog;
import com.luxsoft.siipap.swing.matchers.CheckBoxMatcher;
import com.luxsoft.siipap.swing.matchers.RangoMatcherEditor;
import com.luxsoft.siipap.swing.utils.Renderers;
import com.luxsoft.siipap.swing.utils.TaskUtils;
import com.luxsoft.siipap.util.SQLUtils;


import com.luxsoft.sw3.alcances.AlcanceDeImportacion;

public class AlcanceImportacionPanel extends FilteredBrowserPanel<AlcanceDeImportacion>{
	
	

	public AlcanceImportacionPanel() {
		super(AlcanceDeImportacion.class);
		setTitle("Alcance de Importación");
	}
	
	private CheckBoxMatcher<AlcanceDeImportacion> deLineaMatcher;
	private RangoMatcherEditor<AlcanceDeImportacion> mayorAEditor;
	private RangoMatcherEditor<AlcanceDeImportacion> menorAEditor;
	
	public void init(){
		//Definir columnas del browser
		addProperty(
				"PROVEEDOR"
				,"LINEA"
				,"CLASE"
				,"MARCA"
				,"CLAVE"
				,"DESCRIPCION"				
				,"ANCHO"
				,"LARGO"
				,"GRAMOS"
				,"KILOS"
				,"DELINEA"
				,"EXISTENCIA"
				,"EXISTENCIA_TON"
				,"VENTAS"
				,"PROMEDIO_VTA"
				,"PROMEDIO_VTA_TON"
				,"ALCANCE_INV"				
				,"PEDIDOS_SOLICITADOS"
				,"DEPURADOS"
				,"ADUANA"
				,"ENTRADA"				
				,"PEDIDOS_PENDIENTES"				
				,"PEDIDOS_PEND_TON"
				,"ALCANCE_PED"
				,"alcanceProyectado"
				,"porPedir"
				,"porPedirTon"
				);
		
		addLabels(
				"Proveedor"
				,"Linea"
				,"Clase"
				,"Marca"
				,"Clave"
				,"Descripciòn"				
				,"Ancho"
				,"Largo"
				,"Grs"				
				,"Kg"
				,"De Linea"
				,"Exis"
				,"Ex.Ton"
				,"Vts"
				,"Prom(Vta)"
				,"P.Vta.Ton"
				,"Alc.Inv"				
				,"Ped.Sol"
				,"Depurado"
				,"Aduana"
				,"Entrada"				
				,"Ped.Pend"				
				,"Ped.P.Ton"
				,"Alc.Ped"				
				,"Alc(Inv+Ped)"
				,"Por Pedir"
				,"Ped.Ton"
				);
		
		installTextComponentMatcherEditor("Clave", "CLAVE","DESCRIPCION");
		installTextComponentMatcherEditor("Linea", "LINEA");
		installTextComponentMatcherEditor("Clase", "CLASE");
		installTextComponentMatcherEditor("Marca", "MARCA");
		installTextComponentMatcherEditor("Proveedor", "PROVEEDOR");
		
		deLineaMatcher=new CheckBoxMatcher<AlcanceDeImportacion>(false) {			
			protected Matcher<AlcanceDeImportacion> getSelectMatcher(Object... obj) {				
				return new Matcher<AlcanceDeImportacion>() {					
					public boolean matches(AlcanceDeImportacion item) {
						return item.isDELINEA();
					}					
				};
			}
		};
		installCustomMatcherEditor("De Línea", deLineaMatcher.getBox(), deLineaMatcher);
		mayorAEditor=new RangoMatcherEditor<AlcanceDeImportacion>(){
			public boolean evaluar(AlcanceDeImportacion item) {
				return item.getALCANCE_INV()>=getDoubleValue();
			}
		};
		installCustomMatcherEditor("Alcance Tot>= a", mayorAEditor.getField(), mayorAEditor);
		
		menorAEditor=new RangoMatcherEditor<AlcanceDeImportacion>(){
			public boolean evaluar(AlcanceDeImportacion item) {
				return item.getALCANCE_INV()<=getDoubleValue();
			}
		};
		installCustomMatcherEditor("Alcance Tot<= a", menorAEditor.getField(), menorAEditor);
		
	}
	
	protected JComponent buildHeader(){
		return getHeader();
	}
	
	private HeaderPanel header;
	
	public HeaderPanel getHeader(){
		if(header==null){
			header=new HeaderPanel("Reporte General de Alcances","");
		}
		return header;
	}
	
	public void updateHeader(){
		String pattern="Periodo: {0,date,short} - {1,date,short} Sucursal: {2}  Meses: {3}";
		String suc=sucursal!=null?sucursal.getNombre():"TODAS";
		getHeader().setDescription(MessageFormat.format(pattern, periodo.getFechaInicial(),periodo.getFechaFinal(),suc,meses));
	}
	
	private JTextField mesesField;
	
	protected void installCustomComponentsInFilterPanel(DefaultFormBuilder builder){
		mesesField=new JTextField(7);
		mesesField.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				double res=NumberUtils.toDouble(mesesField.getText());
				setMeses(res);
			}
		});
		builder.append("Meses",mesesField);
	}
	
	protected void adjustMainGrid(final JXTable grid){
		grid.getColumnExt("Linea").setVisible(false);
		grid.getColumnExt("Clase").setVisible(false);
		grid.getColumnExt("Marca").setVisible(false);
		grid.getColumnExt("Descripciòn").setVisible(false);
		grid.getColumnExt("Proveedor").setVisible(false);
		grid.getColumnExt("Vts").setVisible(false);
		grid.getColumnExt("De Linea").setVisible(false);
		grid.getColumnExt("Ancho").setVisible(false);
		grid.getColumnExt("Largo").setVisible(false);
		grid.getColumnExt("Ped.Sol").setVisible(false);
		grid.getColumnExt("Depurado").setVisible(false);
		grid.getColumnExt("Aduana").setVisible(false);
		grid.getColumnExt("Entrada").setVisible(false);
		
		grid.getColumnExt("Alc.Inv").setCellRenderer(Renderers.buildBoldDecimalRenderer(1));
		grid.getColumnExt("Alc(Inv+Ped)").setCellRenderer(Renderers.buildBoldDecimalRenderer(1));
		grid.getColumnExt("Alc.Ped").setCellRenderer(Renderers.buildBoldDecimalRenderer(1));
		
		
	}
	
	@Override
	public Action[] getActions() {
		if(actions==null)
			actions=new Action[]{
				addAction("", "generar", "Generar")
				};
		return actions;
	}
	
	private Sucursal sucursal;
	private double meses;
	
	
	public void generar(){
		AlcanceForm form=new AlcanceForm();
		form.open();
		if(!form.hasBeenCanceled()){
			periodo=form.getPeriodo();
			sucursal=form.getSucursal();
			meses=form.getMeses();
			load();
		}
	}
	
	 

	@Override
	protected List<AlcanceDeImportacion> findData() {
		String sql=SQLUtils.loadSQLQueryFromResource("sql/alcances_v2.sql");
		DateFormat df=new SimpleDateFormat("yyyy-MM-dd");
		String f1="\'"+df.format(periodo.getFechaInicial());
		String f2="\'"+df.format(periodo.getFechaFinal());
		sql=sql.replaceAll("@FECHA_INI", f1+" 00:00:00\'");
		sql=sql.replaceAll("@FECHA_FIN", f2+" 23:00:00\'");
		String suc=sucursal!=null?sucursal.getId().toString():"\'%\'";
		sql=sql.replaceAll("@SUCURSAL_ID", suc);
		//System.out.println(sql);
		List<AlcanceDeImportacion> res=ServiceLocator2.getJdbcTemplate().query(sql,new BeanPropertyRowMapper(AlcanceDeImportacion.class));
		for(AlcanceDeImportacion a:res){
			a.setSUCURSAL(sucursal!=null?sucursal.getNombre():"TODAS");
			a.setMeses(meses);
		}
		
		System.out.println(sql);
		
		return res;
	}

	protected void executeLoadWorker(final SwingWorker worker){
		TaskUtils.executeSwingWorker(worker);
	}
	
	@Override
	protected void afterLoad() {
		super.afterLoad();
		updateHeader();		
	}
	
	public void setMeses(double meses) {
		this.meses = meses;
		for(int index=0;index<source.size();index++){
			AlcanceDeImportacion a=(AlcanceDeImportacion)source.get(index);
			a.setMeses(meses);
			source.set(index, a);
		}
		updateHeader();
	}

	/**
	 * Metodo para practicas q permite ver como se presenta el browser sin arrancar la 
	 * aplicacion
	 * 
	 */
	public static void showInDialog(){
		SXAbstractDialog dialog=new SXAbstractDialog(""){

			@Override
			protected JComponent buildContent() {
				
				JPanel content=new JPanel(new BorderLayout());
				
				AlcanceImportacionPanel alcancePanel=new AlcanceImportacionPanel();
				
				ToolBarBuilder builder=new ToolBarBuilder();
				for(Action a:alcancePanel.getActions()){
					builder.add(a);
				}
				content.add(builder.getToolBar(),BorderLayout.NORTH);
				
				content.add(alcancePanel.getControl(),BorderLayout.CENTER);
				
				return content;
			}
			
		};
		
		dialog.open();
	}
	
	public static class AlcanceForm extends SXAbstractDialog{
		
		private JXDatePicker fechaInicial;
		private JXDatePicker fechaFinal;
		private JComboBox sucursalControl;
		//private JComboBox lineaControl;
		private JFormattedTextField meses;
		private JCheckBox todasLasSucursales;
		
		public AlcanceForm() {
			super("");
		}
		
		private void init(){
			fechaInicial=new JXDatePicker();
			Date ini=DateUtils.addMonths(new Date(), -6);
			fechaInicial.setDate(ini);
			fechaInicial.setFormats("dd/MM/yyyy");
			fechaFinal=new JXDatePicker();
			fechaFinal.setFormats("dd/MM/yyyy");
			sucursalControl=createSucursalControl();
			//lineaControl=buildLineaControl();
			NumberFormatter formatter=new NumberFormatter(NumberFormat.getNumberInstance());
			formatter.setValueClass(Double.class);
			//formatter.setMaximum(new Integer(0));
			meses=new JFormattedTextField(formatter);
			meses.setValue(new Double(6.0));
			
			todasLasSucursales=new JCheckBox("",false);
			todasLasSucursales.addActionListener(new ActionListener(){
				public void actionPerformed(ActionEvent e) {
					sucursalControl.setEnabled(!todasLasSucursales.isSelected());
				}
			});
			todasLasSucursales.setSelected(true);
			sucursalControl.setEnabled(false);
			
		}
		
		@Override
		protected JComponent buildContent() {
			init();
			JPanel panel=new JPanel(new BorderLayout());
			
			final FormLayout layout=new FormLayout(
					"p,2dlu,70dlu,3dlu," +
					"p,2dlu,70dlu:g,3dlu," +
					"p,2dlu,p",
					"");
			DefaultFormBuilder builder=new DefaultFormBuilder(layout);
			builder.append("Fecha Inicial",fechaInicial);
			builder.append("Fecha Final",fechaFinal);
			builder.nextLine();
			
			builder.append("Sucursal",sucursalControl,5);			
			builder.append("Todas",todasLasSucursales);
			
			builder.nextLine();
			//builder.append("Linea",lineaControl,5);
			//builder.nextLine();
			builder.append("Meses de alcance",meses);
			
			
			panel.add(builder.getPanel(),BorderLayout.CENTER);
			panel.add(buildButtonBarWithOKCancel(),BorderLayout.SOUTH);
			
			return panel;
		}
		
		private JComboBox createSucursalControl() {			
			final JComboBox box = new JComboBox(ServiceLocator2.getLookupManager().getSucursalesOperativas().toArray());
			Sucursal local=ServiceLocator2.getConfiguracion().getSucursal();
			for(int index=0;index<box.getModel().getSize();index++){
				Sucursal s=(Sucursal)box.getModel().getElementAt(index);
				if(s.equals(local)){
					box.setSelectedIndex(index);
					break;
				}
			}
			return box;
		}
		
		public Periodo getPeriodo(){
			return new Periodo(fechaInicial.getDate(),fechaFinal.getDate());
		}
		
		public Sucursal getSucursal(){
			Sucursal selected=(Sucursal)sucursalControl.getSelectedItem();
			if(todasLasSucursales.isSelected())
				return null;
			return selected;
		}
		
		public double getMeses(){
			return (Double)meses.getValue();
		}
		
		
	}
	
	private TotalesPanel totalPanel;
	
	public JPanel getTotalesPanel(){
		if(totalPanel==null){
			totalPanel=new TotalesPanel();
		}
		return (JPanel)totalPanel.getControl();
	}
	
	private class TotalesPanel extends AbstractControl implements ListEventListener{
		
		
		private JLabel total1=new JLabel();
		private JLabel total2=new JLabel();
		private JLabel total3=new JLabel();
		private JLabel total4=new JLabel();
		
		

		@Override
		protected JComponent buildContent() {
			final FormLayout layout=new FormLayout("p,2dlu,f:p:g","");
			DefaultFormBuilder builder=new DefaultFormBuilder(layout);
			total1.setHorizontalAlignment(SwingConstants.RIGHT);
			total2.setHorizontalAlignment(SwingConstants.RIGHT);
			total3.setHorizontalAlignment(SwingConstants.RIGHT);
			total4.setHorizontalAlignment(SwingConstants.RIGHT);
			
			builder.appendSeparator("Toneladas ");
			builder.append("Existencias",total1);
			builder.append("Ventas (Prom)",total2);
			builder.append("Pedidos (Pend)",total3);
			builder.append("Por Pedir",total4);
			
			
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
			
			double toneladasExis=0;
			double toneladasVentas=0;
			double toneladasPorPedir=0;
			double pedidosPendientes=0;
			
			for(Object obj:getFilteredSource()){
				AlcanceDeImportacion a=(AlcanceDeImportacion)obj;
				toneladasExis+=a.getToneladasExis();
				toneladasVentas+=a.getToneladasPromVenta();
				toneladasPorPedir+=a.getToneladasPorPedir();
				pedidosPendientes+=a.getPEDIDOS_PEND_TON();
			}
			total1.setText(nf.format(toneladasExis));
			total2.setText(nf.format(toneladasVentas));
			total3.setText(nf.format(pedidosPendientes));
			total4.setText(nf.format(toneladasPorPedir));
		}
		
		private NumberFormat nf=NumberFormat.getNumberInstance();
		
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
				com.luxsoft.siipap.swing.utils.SWExtUIManager.setup();
				showInDialog();
				//SXAbstractDialog dialog=new AlcanceForm();
				//dialog.open();
				System.exit(0);
			}

		});
	}
	
	
	
}
