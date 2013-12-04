package com.luxsoft.siipap.cxp.ui.consultas;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;

import javax.swing.Action;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingWorker;

import org.apache.commons.lang.StringUtils;

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.FilterList;
import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.TextFilterator;
import ca.odell.glazedlists.CollectionList.Model;
import ca.odell.glazedlists.event.ListEvent;
import ca.odell.glazedlists.event.ListEventListener;
import ca.odell.glazedlists.gui.TableFormat;
import ca.odell.glazedlists.matchers.CompositeMatcherEditor;
import ca.odell.glazedlists.matchers.MatcherEditor;
import ca.odell.glazedlists.swing.TextComponentMatcherEditor;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;
import com.luxsoft.siipap.model.CantidadMonetaria;
import com.luxsoft.siipap.model.contabilidad.AsientoContable;
import com.luxsoft.siipap.model.contabilidad.Poliza;
import com.luxsoft.siipap.reportes.ReporteDeAlcancesImpoForm;
import com.luxsoft.siipap.service.contabilidad.ExportadorGenericoDePolizas;
import com.luxsoft.siipap.swing.browser.AbstractMasterDatailFilteredBrowserPanel;
import com.luxsoft.siipap.swing.controls.AbstractControl;
import com.luxsoft.siipap.swing.utils.TaskUtils;


public class PolizaAlmacenPanel extends AbstractMasterDatailFilteredBrowserPanel<Poliza, AsientoContable> {
	
	protected PolizaDeAlmacenModel2 model;
	
	
	public PolizaAlmacenPanel() {
		super(Poliza.class);
		
		
	}
	
	protected void init(){
		model=new PolizaDeAlmacenModel2();
		addProperty(new String[]{"fecha","tipo","concepto","sucursalNombre","debe","haber","cuadre","year","mes"});
		addLabels(  new String[]{"Fecha","Tipo","Concepto","Sucursal","Debe","Haber","Cuadre","year","mes"});
		
		manejarPeriodo();
	}
	
	@Override
	protected TableFormat createDetailTableFormat() {
		final String[] cols={"cuenta","concepto","descripcion","descripcion2","descripcion3","debe","haber","tipo","agrupador"};
		final String[] names={"Cuenta","Concepto","Descripción","Desc2","Desc3","Debe","Haber","Tipo","Agrupador"};
		boolean edits[]={
				true//"cuenta"
				,true//"concepto"
				,true//"descripcion"
				,true//"descripcion2"
				,true//"descripcion3"
				,false//"debe"
				,false//"haber"
				,false//"tipo"
				,true //"agrupador"
				};
		final TableFormat<AsientoContable> tf=GlazedLists.tableFormat(AsientoContable.class, cols,names,edits);
		return tf;
	}

	@Override
	protected Model<Poliza, AsientoContable> createPartidasModel() {
		return new Model<Poliza, AsientoContable>(){
			public List<AsientoContable> getChildren(Poliza parent) {
				return parent.getRegistros();
			}
		};
	}
	
	private JTextField cuentaField=new JTextField(5);
	private JTextField concepto=new JTextField(5);
	private JTextField desc1=new JTextField(5);
	private JTextField desc2=new JTextField(5);
	private JTextField desc3=new JTextField(5);
	private JTextField agrupador=new JTextField(5);
	private JTextField tipo=new JTextField(5);
	
	protected void installDetailFilterComponents(DefaultFormBuilder builder){
		builder.appendSeparator("Detalle");
		builder.append("Cuenta",cuentaField);
		builder.append("Concepto ",concepto);
		builder.append("Descripción ",desc1);
		builder.append("Descripción 2",desc2);
		builder.append("Descripción 3",desc3);
		builder.append("Agrupador",agrupador);
		builder.append("Tipo",tipo);
	}
	
	protected EventList<AsientoContable> partidasFiltered;
	protected EventList<AsientoContable> partidasSource;
	
	@Override
	protected EventList decorateDetailList( EventList data){
		partidasSource=data;
		EventList<MatcherEditor> editors=new BasicEventList<MatcherEditor>();
		
		
		TextFilterator docFilterator=GlazedLists.textFilterator("cuenta");
		TextComponentMatcherEditor docEditor=new TextComponentMatcherEditor(cuentaField,docFilterator);
		editors.add(docEditor);
		
		TextFilterator conceptoFilter=GlazedLists.textFilterator("concepto");
		TextComponentMatcherEditor conceptoEditor=new TextComponentMatcherEditor(concepto,conceptoFilter);
		editors.add(conceptoEditor);
		
		TextFilterator agrupadorFilter=GlazedLists.textFilterator("agrupador");
		TextComponentMatcherEditor agrupadorEditor=new TextComponentMatcherEditor(agrupador,agrupadorFilter);
		editors.add(agrupadorEditor);
		
		TextFilterator tipoFilter=GlazedLists.textFilterator("tipo");
		TextComponentMatcherEditor tipoEditor=new TextComponentMatcherEditor(tipo,tipoFilter);
		editors.add(tipoEditor);
		
		TextFilterator descFilter=GlazedLists.textFilterator("descripcion");
		TextComponentMatcherEditor descEditor=new TextComponentMatcherEditor(desc1,descFilter);
		editors.add(descEditor);
		
		TextFilterator desc2Filter=GlazedLists.textFilterator("descripcion2");
		TextComponentMatcherEditor desc2Editor=new TextComponentMatcherEditor(desc2,desc2Filter);
		editors.add(desc2Editor);
		
		TextFilterator desc3Filter=GlazedLists.textFilterator("descripcion3");
		TextComponentMatcherEditor desc3Editor=new TextComponentMatcherEditor(desc3,desc3Filter);
		editors.add(desc3Editor);
		
		CompositeMatcherEditor matcherEditor=new CompositeMatcherEditor(editors);
		FilterList detailFilter=new FilterList(data,matcherEditor);
		partidasFiltered=detailFilter;
		return detailFilter;
	}
	
	public List<Poliza> findData(){
		return model.generarPoliza(periodo);
	}
	
	@Override
	public Action[] getActions() {
		if(actions==null)
			actions=new Action[]{
				getLoadAction()
				,addAction("generarPoliza", "generarPoliza", "Generar Póliza")
				,addDetailContextAction(new EliminarRegistroPredicate(),"eliminarAsiento", "eliminareAsiento", "Eliminar registro")
				};
		return actions;
	}
	
	@Override
	protected List<Action> createProccessActions() {		
		List<Action> procesos=super.createProccessActions();
		procesos.add(addAction("","alcanceImportacion", "Alcance Importaciones"));
		return procesos;
	}
	
	public void alcanceImportacion(){
		ReporteDeAlcancesImpoForm.run();
	}
	
	
	private DateFormat df=new SimpleDateFormat("ddMM");
	
	public void generarPoliza(){
		if(!getSelected().isEmpty()){
			for(Object o:getSelected()){
				Poliza poliza=(Poliza)o;
				String suc=StringUtils.leftPad(String.valueOf(poliza.getSucursalId()), 2,'0');
				poliza.setExportName("C"+suc+df.format(poliza.getFecha())+".POL");
				poliza.ordenarPartidas("descripcion2", "descripcion3");
				File res=ExportadorGenericoDePolizas.exportarACoi(poliza, null,"META-INF/templates/Poliza_Compras.ftl");
				JOptionPane.showMessageDialog(getControl(), "Poliza generada en: \n"+res.getPath(),"Exportador a COI",JOptionPane.INFORMATION_MESSAGE);
			}
		}
	}

	@Override
	protected void afterLoad() {
		if(this.totalPanel!=null)
			totalPanel.updateTotales();
	}

	@Override
	protected void executeLoadWorker(SwingWorker worker) {
		TaskUtils.executeSwingWorker(worker);
	}
	
	public void eliminareAsiento(){
		if(!detailSelectionModel.getSelected().isEmpty()){
			Object selected=detailSelectionModel.getSelected().get(0);
			int index=partidasSource.indexOf(selected);
			if(index!=-1){
				AsientoContable as=(AsientoContable)selected;
				Poliza pol=as.getPoliza();
				pol.removerAsiento(as);
				partidasSource.remove(index);
			}
			
		}
	}

	private TotalesPanel totalPanel;
	
	public JPanel getTotalesPanel(){
		if(totalPanel==null){
			totalPanel=new TotalesPanel();
			getFilteredSource().addListEventListener(totalPanel);
			partidasFiltered.addListEventListener(totalPanel);
		}
		return (JPanel)totalPanel.getControl();
	}
	
	
	protected class TotalesPanel extends AbstractControl implements ListEventListener{
	
		private JLabel totalDebe;
		private JLabel totalHaber;
		private JLabel cuadre;
		private JCheckBox porPoliza=new JCheckBox(" Totales por plóliza",true);
		

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
					for(AsientoContable a:pol.getRegistros()){
						debe=debe.add(a.getDebe());
						haber=haber.add(a.getHaber());
					}
				}
			}else{
				for(AsientoContable a:partidasFiltered){
					debe=debe.add(a.getDebe());
					haber=haber.add(a.getHaber());
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
	
	protected class EliminarRegistroPredicate implements Predicate{

		public boolean evaluate(Object bean) {
			return bean!=null;
		}
		
	}
	
}
