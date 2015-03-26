package com.luxsoft.sw3.contabilidad.ui.consultas2;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.DateFormat;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.Action;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.springframework.orm.hibernate3.HibernateTemplate;

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.CollectionList.Model;
import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.FilterList;
import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.TextFilterator;
import ca.odell.glazedlists.event.ListEvent;
import ca.odell.glazedlists.event.ListEventListener;
import ca.odell.glazedlists.gui.TableFormat;
import ca.odell.glazedlists.matchers.CompositeMatcherEditor;
import ca.odell.glazedlists.matchers.MatcherEditor;
import ca.odell.glazedlists.swing.TextComponentMatcherEditor;

import com.jgoodies.binding.value.ValueHolder;
import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.uif.AbstractDialog;
import com.luxsoft.siipap.model.CantidadMonetaria;
import com.luxsoft.siipap.model.Periodo;
import com.luxsoft.siipap.service.ServiceLocator2;
import com.luxsoft.siipap.swing.binding.Binder;
import com.luxsoft.siipap.swing.binding.PeriodoPicker;
import com.luxsoft.siipap.swing.browser.AbstractMasterDatailFilteredBrowserPanel;
import com.luxsoft.siipap.swing.controls.AbstractControl;
import com.luxsoft.siipap.swing.matchers.FechaMatcherEditor;
import com.luxsoft.siipap.swing.reports.ReportUtils;
import com.luxsoft.siipap.swing.utils.CommandUtils;
import com.luxsoft.sw3.contabilidad.model.Poliza;
import com.luxsoft.sw3.contabilidad.model.PolizaDet;
import com.luxsoft.sw3.contabilidad.ui.consultas.SelectorDeSucursal;
import com.luxsoft.sw3.contabilidad.ui.consultas.PanelGenericoDePolizasMultiples.AsientoMatcherEditor;
import com.luxsoft.sw3.contabilidad.ui.consultas.PanelGenericoDePolizasMultiples.CuentasMatcherEditor;
import com.luxsoft.sw3.contabilidad.ui.form.PolizaForm;
import com.luxsoft.sw3.contabilidad.ui.form.PolizaFormModel;


public class ConsultaGeneralDePolizas extends AbstractMasterDatailFilteredBrowserPanel<Poliza, PolizaDet>{

	public ConsultaGeneralDePolizas() {
		super(Poliza.class);
	}
	
	@Override
	protected void agregarMasterProperties() {
		addProperty("id","clase","folio","tipo","descripcion","fecha","referencia","debe","haber","cuadre");
		addLabels("Id","Clase","Folio","Tipo","Descripción","Fecha","Referencia","Debe","Haber","Cuadre");
		installTextComponentMatcherEditor("Clase", "clase");
		installTextComponentMatcherEditor("Tipo", "tipo");
		installTextComponentMatcherEditor("Descripción", "descripcion");
		installTextComponentMatcherEditor("Referencia", "referencia");
		
		final JTextField tf=new JTextField(10);
		TextFilterator<Poliza> filterator=new TextFilterator<Poliza>() {
			DateFormat df=new SimpleDateFormat("dd/MM/yyyy");
			public void getFilterStrings(List<String> baseList, Poliza element) {
				baseList.add(df.format(element.getFecha()));
			}
		};
		installTextComponentMatcherEditor("Día", filterator, tf);
		installTextComponentMatcherEditor("Folio", "folio");
		manejarPeriodo();
	}

	@Override
	protected TableFormat createDetailTableFormat() {
		String[] props={"poliza.folio","concepto.subcuenta","concepto","descripcion2","referencia","referencia2","debe","haber","asiento","tipo"};
		
		return GlazedLists.tableFormat(PolizaDet.class, props,getDetalleNames());
	}
	
	protected String[] getDetalleNames(){
		String[] names={
				"Poliza"
				,"Cuenta"
				,"Concepto"
				,"Descripción"
				,"Origen"
				,"Sucursal"
				,"Debe"
				,"Haber"
				,"Asiento"
				,"Tipo"
				};
		return names;
	}

	@Override
	protected Model<Poliza, PolizaDet> createPartidasModel() {
		return new Model<Poliza,PolizaDet>(){
			public List<PolizaDet> getChildren(Poliza parent) {
				if(parent.getId()==null)
					return parent.getPartidas();
				return getHibernateTemplate().find("from PolizaDet d where d.poliza.id=?",parent.getId());
			}
			
		};
	}
	
	private int year;
	
	private int mes;
	
	public int getYear(){
		return year;
	}
	public int getMes(){
		return mes;
	}
	
	@Override
	protected void doSelect(Object bean) {
		Poliza poliza=(Poliza)bean;
		final Poliza source=ServiceLocator2.getPolizasManager().getPolizaDao().get(poliza.getId());		
		final PolizaFormModel model=new PolizaFormModel(source);
		model.setReadOnly(true);
		final PolizaForm form=new PolizaForm(model);			
		form.open();
		
	}
	
	@Override
	protected void manejarPeriodo() {
		periodo=Periodo.getPeriodoDelMesActual(new Date());
		year=Periodo.obtenerYear(periodo.getFechaFinal());
		mes=Periodo.obtenerMes(periodo.getFechaFinal());
	}
	
	public void cambiarPeriodo(){
		ValueHolder yearHolder=new ValueHolder(Periodo.obtenerYear(periodo.getFechaInicial()));
		ValueHolder mesHolder=new ValueHolder(Periodo.obtenerMes(periodo.getFechaInicial()));
		
		AbstractDialog dialog=Binder.createSelectorMesYearContable(yearHolder, mesHolder);
		dialog.open();
		if(!dialog.hasBeenCanceled()){
			mes=(Integer)mesHolder.getValue();
			year=(Integer)yearHolder.getValue();
			periodo=Periodo.getPeriodoEnUnMes(mes-1, year);
			nuevoPeriodo(periodo);
			updatePeriodoLabel();
			load();
		}
	}
	
	protected void updatePeriodoLabel(){
		periodoLabel.setText("Per:" +year+" - "+mes);
	}
	
	@Override
	public void open() {
		load();
	}
	
	@Override
	public Action[] getActions() {
		if(actions==null)
			actions=new Action[]{
				getLoadAction()
				,getSelectAction()
				,CommandUtils.createPrintAction(this, "imprimir")
				,addAction("", "imprimirPolizaPorSucursal", "Imprimir por sucursal")
			};
		return actions;
	}
	
	public void imprimir(){
		if(getSelectedObject()!=null){
			imprimirPoliza((Poliza)getSelectedObject());
		}
	}
	
	
	public void imprimirPoliza(Poliza bean){
		Map params=new HashMap();
		params.put("ID", bean.getId());
		params.put("ORDEN", "ORDER BY 13,3");
		String path=ReportUtils.toReportesPath("contabilidad/Poliza.jasper");
		ReportUtils.viewReport(path, params);
	}
	
	public void imprimirPolizaPorSucursal(){
		if(getSelectedObject()!=null){
			imprimirPolizaSucursal((Poliza)getSelectedObject());
		}
	}
	
	public void imprimirPolizaSucursal(Poliza bean){
		if(bean.getClase().equals("VENTAS")){
			String sucursal=SelectorDeSucursal.seleccionar();
			Map params=new HashMap();
			params.put("ID", bean.getId());
			params.put("SUCURSAL",sucursal);
			String path=ReportUtils.toReportesPath("contabilidad/PolizaxSucursal.jasper");
			ReportUtils.viewReport(path, params);
		}
	}
	
		private CuentasMatcherEditor cuentaEditor=new CuentasMatcherEditor();
		private JTextField concepto=new JTextField(5);
		private JTextField referencia1=new JTextField(5);
		private JTextField referencia2=new JTextField(5);
		private AsientoMatcherEditor asiento=new AsientoMatcherEditor();
		//private JTextField asiento=new JTextField(5);
		private JTextField tipo=new JTextField(5);
		private JTextField descripcion=new JTextField(5);
		
		
		protected void installDetailFilterComponents(DefaultFormBuilder builder){
			builder.appendSeparator("Detalle");
			builder.append("Sucursal",referencia2);
			builder.append("Asiento",asiento.getField());
			builder.append("Cuenta",cuentaEditor.getField());
			builder.append("Tipo",tipo);
			builder.append("Origen ",referencia1);
			builder.append("Concepto ",concepto);
			builder.append("Descripción ",descripcion);
		}
		
		protected EventList<PolizaDet> partidasFiltered;
		protected EventList<PolizaDet> partidasSource;
		
		@Override
		protected EventList decorateDetailList( EventList data){
			partidasSource=data;
			EventList<MatcherEditor> editors=new BasicEventList<MatcherEditor>();
			editors.add(new TextComponentMatcherEditor(referencia2,GlazedLists.textFilterator("referencia2")));
			editors.add(asiento);
			editors.add(cuentaEditor);
			editors.add(new TextComponentMatcherEditor(tipo,GlazedLists.textFilterator("tipo")));
			editors.add(new TextComponentMatcherEditor(referencia1,GlazedLists.textFilterator("referencia")));
			editors.add(new TextComponentMatcherEditor(concepto,GlazedLists.textFilterator("descripcion")));
			editors.add(new TextComponentMatcherEditor(descripcion,GlazedLists.textFilterator("descripcion2")));
			CompositeMatcherEditor matcherEditor=new CompositeMatcherEditor(editors);
			FilterList detailFilter=new FilterList(data,matcherEditor);
			partidasFiltered=detailFilter;
			return detailFilter;
		}
		

		@Override
		protected List<Poliza> findData() {
			
			if(mes==13){
				
				String hql="from Poliza p " +
						" where year(p.fecha)=? and p.clase=?";
				Object[] params={year,"CIERRE_ANUAL"};
				return ServiceLocator2
					.getHibernateTemplate()
					.find(hql,params);
			}else{
				String hql="from Poliza p " +
						" where year(p.fecha)=? and month(p.fecha)=?";
				Object[] params={year,mes};
				return ServiceLocator2
					.getHibernateTemplate()
					.find(hql,params);
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
		
			private JLabel totalDebe;
			private JLabel totalHaber;
			private JLabel cuadre;
			private JCheckBox porPoliza=new JCheckBox(" Totales por plóliza",false);
			

			@Override
			protected JComponent buildContent() {
				porPoliza.setOpaque(false);
				porPoliza.addActionListener(new ActionListener(){
					public void actionPerformed(ActionEvent e) {
						updateTotales();
					}				
				});
				totalDebe=new JLabel();
				totalDebe.setHorizontalAlignment(JLabel.RIGHT);
				totalHaber=new JLabel();
				totalHaber.setHorizontalAlignment(JLabel.RIGHT);
				cuadre=new JLabel();
				cuadre.setHorizontalAlignment(JLabel.RIGHT);
				FormLayout layout=new FormLayout("p,2dlu,f:50dlu:g","");
				final DefaultFormBuilder builder=new DefaultFormBuilder(layout);
				//porPoliza.setEnabled(false);
				builder.append("General",porPoliza);
				builder.append("Debe:",totalDebe);
				builder.append("Haber: ",totalHaber);			
				builder.append("Cuadre: ",cuadre);
				builder.getPanel().setOpaque(false);			
				return builder.getPanel();
			}
			
			private void updateTotales(){
				CantidadMonetaria debe=CantidadMonetaria.pesos(0);
				CantidadMonetaria haber=CantidadMonetaria.pesos(0);
				if(porPoliza.isSelected()){
					for(Object o:getFilteredSource()){
						Poliza pol=(Poliza)o;
						debe=debe.add(CantidadMonetaria.pesos(pol.getDebe()));
						haber=haber.add(CantidadMonetaria.pesos(pol.getHaber()));
					}
				}else{
					for(PolizaDet a:partidasFiltered){
						debe=debe.add(a.getDebeCM());
						haber=haber.add(a.getHaberCM());
					}
				}
				
				totalDebe.setText(debe.toString());
				totalHaber.setText(haber.toString());
				cuadre.setText(debe.subtract(haber).toString());
			}
			
			
			public void listChanged(ListEvent listChanges) {
				if(listChanges.hasNext()){
					updateTotales();
				}
			}
			
			

		}


		protected HibernateTemplate getHibernateTemplate(){
			return ServiceLocator2.getHibernateTemplate();
		}
}
