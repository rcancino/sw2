package com.luxsoft.siipap.compras.ui.consultas;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.DateFormat;
import java.text.MessageFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Collection;
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

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
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
import com.luxsoft.siipap.compras.model.Compra2;
import com.luxsoft.siipap.compras.model.CompraUnitaria;
import com.luxsoft.siipap.compras.ui.form.CompraCentralizadaForm;
import com.luxsoft.siipap.compras.ui.form.CompraCentralizadaFormModel;
import com.luxsoft.siipap.model.Periodo;
import com.luxsoft.siipap.model.Sucursal;
import com.luxsoft.siipap.model.core.Producto;
import com.luxsoft.siipap.model.core.Proveedor;
import com.luxsoft.siipap.service.ServiceLocator2;
import com.luxsoft.siipap.swing.browser.FilteredBrowserPanel;
import com.luxsoft.siipap.swing.controls.AbstractControl;
import com.luxsoft.siipap.swing.controls.SXAbstractDialog;
import com.luxsoft.siipap.swing.matchers.CheckBoxMatcher;
import com.luxsoft.siipap.swing.matchers.RangoMatcherEditor;
import com.luxsoft.siipap.swing.utils.MessageUtils;
import com.luxsoft.siipap.swing.utils.Renderers;
import com.luxsoft.siipap.swing.utils.TaskUtils;
import com.luxsoft.siipap.util.SQLUtils;
import com.luxsoft.sw3.alcances.Alcance;



public class AlcancesPanel extends FilteredBrowserPanel<Alcance>{
	
	

	public AlcancesPanel() {
		super(Alcance.class);
		setTitle("Alcance Nacional");
	}
	
	private CheckBoxMatcher<Alcance> deLineaMatcher;
	
	private RangoMatcherEditor<Alcance> mayorAEditor;
	private RangoMatcherEditor<Alcance> menorAEditor;
	
	
	public void init(){
		//Definir columnas del browser
		addProperty(
				"LINEA"
				,"CLASE"
				,"MARCA"
				,"CLAVE"
				,"DESCRIPCION"
				,"KILOS"
				,"DELINEA"
				,"EXISTENCIA"
				,"VENTAS"
				,"PROMEDIO_VTA"
				,"ALCANCE_TOTAL"
				,"PEDIDOS_PENDIENTES"
				,"alcanceProyectado"
				,"porPedir"
				,"PROVEEDOR"
				);
		
		addLabels(
				"Linea"
				,"Clase"
				,"Marca"
				,"Clave"
				,"Descripciòn"
				,"Kg"
				,"De Linea"
				,"Exis"
				,"Vts"
				,"Prom(Vta)"
				,"Alcance"
				,"Ped.Pend"
				,"Alc(Inv+Ped)"
				,"Por Pedir"
				,"Proveedor"
				);
		
		installTextComponentMatcherEditor("Clave", "CLAVE","DESCRIPCION");
		installTextComponentMatcherEditor("Linea", "LINEA");
		installTextComponentMatcherEditor("Clase", "CLASE");
		installTextComponentMatcherEditor("Marca", "MARCA");
		installTextComponentMatcherEditor("Proveedor", "PROVEEDOR");
		
		deLineaMatcher=new CheckBoxMatcher<Alcance>(false) {			
			protected Matcher<Alcance> getSelectMatcher(Object... obj) {				
				return new Matcher<Alcance>() {					
					public boolean matches(Alcance item) {
						return item.isDELINEA();
					}					
				};
			}
		};
		installCustomMatcherEditor("De Línea", deLineaMatcher.getBox(), deLineaMatcher);
		
		NumberFormat format=NumberFormat.getNumberInstance();
		NumberFormatter formatter=new NumberFormatter(format);
		formatter.setValueClass(Double.class);
		formatter.setCommitsOnValidEdit(true);
		formatter.setAllowsInvalid(true);
		
		//alcanceMayor=new JFormattedTextField(formatter);
		
		mayorAEditor=new RangoMatcherEditor<Alcance>(){
			public boolean evaluar(Alcance item) {
				return item.getALCANCE_TOTAL()>=getDoubleValue();
			}
		};
		installCustomMatcherEditor("Alcance Tot>= a", mayorAEditor.getField(), mayorAEditor);
		
		menorAEditor=new RangoMatcherEditor<Alcance>(){
			public boolean evaluar(Alcance item) {
				return item.getALCANCE_TOTAL()<=getDoubleValue();
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
		grid.getColumnExt("Vts").setVisible(false);
		grid.getColumnExt("De Linea").setVisible(false);
		grid.getColumnExt("Alcance").setCellRenderer(Renderers.buildBoldDecimalRenderer(1));
		grid.getColumnExt("Alc(Inv+Ped)").setCellRenderer(Renderers.buildBoldDecimalRenderer(1));
		
	}
	
	
	
	@Override
	public Action[] getActions() {
		if(actions==null)
			actions=new Action[]{
				addAction("", "generar", "Generar")			
				,addAction("", "generarOrdenDeCompra", "Generar O.Compra")
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
	protected List<Alcance> findData() {
		String sql=SQLUtils.loadSQLQueryFromResource("sql/alcances_v1.sql");
		DateFormat df=new SimpleDateFormat("yyyy-MM-dd");
		String f1="\'"+df.format(periodo.getFechaInicial())+"\'";
		String f2="\'"+df.format(periodo.getFechaFinal())+"\'";
		sql=sql.replaceAll("@FECHA_INI", f1);
		sql=sql.replaceAll("@FECHA_FIN", f2);
		String suc=sucursal!=null?sucursal.getId().toString():"\'%\'";
		sql=sql.replaceAll("@SUCURSAL_ID", suc);
		
		if(suc.equals("6")){
			sql=sql.replaceAll("@EXI_SUCURSAL_ID", " X.SUCURSAL_ID  in(6,11)");
		}else if(suc.equals("9")){
			sql=sql.replaceAll("@EXI_SUCURSAL_ID", " X.SUCURSAL_ID  in(9,14)");
		} else{
			sql=sql.replaceAll("@EXI_SUCURSAL_ID", " X.SUCURSAL_ID LIKE "+suc);
		}
		
		System.out.println(sql);
		List<Alcance> res=ServiceLocator2.getJdbcTemplate().query(sql,new BeanPropertyRowMapper(Alcance.class));
		for(Alcance a:res){
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
	


	public double getMeses() {
		return meses;
	}
	
	public void setMeses(double meses) {
		this.meses = meses;
		for(int index=0;index<source.size();index++){
			Alcance a=(Alcance)source.get(index);
			a.setMeses(meses);
			source.set(index, a);
		}
		updateHeader();
	}
	
	public void generarOrdenDeCompra(){
		if(!getSelected().isEmpty()){
			if(sucursal!=null){
				Alcance selected=(Alcance)CollectionUtils.find(getSelected(), new org.apache.commons.collections.Predicate(){
					public boolean evaluate(Object object) {
						Alcance alc=(Alcance)object;
						return StringUtils.isNotBlank(alc.getPROVEEDOR());
					}
				});
				if(selected!=null){
					final String provNombre=selected.getPROVEEDOR();
					final Collection<Alcance> result=CollectionUtils.select(getSelected(), new org.apache.commons.collections.Predicate(){
						public boolean evaluate(Object object) {
							Alcance alc=(Alcance)object;
							return alc.getPROVEEDOR().equals(provNombre);
						}						
					});
					Proveedor prov=ServiceLocator2.getProveedorManager().buscarPorNombre(provNombre);
					if(prov!=null){
						Compra2 compra=new Compra2();
						compra.setComentario("Compra automatica alcance: "+periodo.toString()+ "Suc: "+sucursal.getClave()+" Meses: "+meses);
						compra.setFecha(new Date());
						compra.setProveedor(prov);
						compra.setSucursal(sucursal);
						for(Alcance a:result){
							CompraUnitaria cu=new CompraUnitaria();
							Producto prod=ServiceLocator2.getProductoManager().buscarPorClave(a.getCLAVE());
							if(!prod.isActivoCompras())
								continue;
							cu.setProducto(prod);
							cu.setComentario("ALC");
							double porPedir=a.getPorPedir();
							porPedir*=prod.getUnidad().getFactor();
							cu.setSolicitado(porPedir);
							cu.setSucursal(sucursal);
							compra.agregarPartida(cu);
							ServiceLocator2.getComprasManager().asignarPrecioDescuento(cu);
							
						}
						CompraCentralizadaFormModel controller=new CompraCentralizadaFormModel(compra);
						CompraCentralizadaForm form=new CompraCentralizadaForm(controller);
						form.setProveedorFijo(true);
						form.open();
						if(!form.hasBeenCanceled()){
							compra=controller.getCompra();
							
							for(CompraUnitaria det:compra.getPartidas()){
								det.actualizar();
							}
							
							compra=ServiceLocator2.getComprasManager().save(compra);
							MessageUtils.showMessage("Compra generada: "+compra.getFolio(), "Compras");
							ComprasCentralizadasController.imprimir(compra);
						}
					}else{
						MessageUtils.showMessage("No localizo al proveedor: "+provNombre, "Orden automática");
					}
				}
				
			}else{
				MessageUtils.showMessage("Genere un alcance por sucursal", "Orden automática");
			}
		}
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
				
				AlcancesPanel alcancePanel=new AlcancesPanel();
				
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
			Date ini=DateUtils.addMonths(new Date(), -2);
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
			meses.setValue(new Double(2.0));
			
			todasLasSucursales=new JCheckBox("",false);
			todasLasSucursales.addActionListener(new ActionListener(){
				public void actionPerformed(ActionEvent e) {
					sucursalControl.setEnabled(!todasLasSucursales.isSelected());
				}
			});
			
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
		

		@Override
		protected JComponent buildContent() {
			final FormLayout layout=new FormLayout("p,2dlu,f:p:g","");
			DefaultFormBuilder builder=new DefaultFormBuilder(layout);
			total1.setHorizontalAlignment(SwingConstants.RIGHT);
			total2.setHorizontalAlignment(SwingConstants.RIGHT);
			total3.setHorizontalAlignment(SwingConstants.RIGHT);
			builder.appendSeparator("Toneladas ");
			builder.append("Existencias",total1);
			builder.append("Ventas (Prom)",total2);
			builder.append("Por Pedir",total3);
			
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
			
			for(Object obj:getFilteredSource()){
				Alcance a=(Alcance)obj;
				toneladasExis+=a.getToneladasExis();
				toneladasVentas+=a.getToneladasPromVenta();
				toneladasPorPedir+=a.getToneladasPorPedir();
			}
			total1.setText(nf.format(toneladasExis));
			total2.setText(nf.format(toneladasVentas));
			total3.setText(nf.format(toneladasPorPedir));
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
