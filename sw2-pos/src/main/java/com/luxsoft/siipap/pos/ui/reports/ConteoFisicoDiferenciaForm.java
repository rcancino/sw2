package com.luxsoft.siipap.pos.ui.reports;

import java.awt.BorderLayout;
import java.text.NumberFormat;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFormattedTextField;
import javax.swing.JPanel;
import javax.swing.text.NumberFormatter;

import org.jdesktop.swingx.JXDatePicker;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;
import com.luxsoft.siipap.model.Configuracion;
import com.luxsoft.siipap.model.Sucursal;
import com.luxsoft.siipap.pos.ui.reports.ConteoSelectivoDeInventarioForm.Familia;
import com.luxsoft.siipap.pos.ui.reports.ConteoSelectivoDeInventarioForm.ParamLabelValue;
import com.luxsoft.siipap.pos.ui.utils.ReportUtils2;
import com.luxsoft.siipap.swing.actions.SWXAction;
import com.luxsoft.siipap.swing.controls.SXAbstractDialog;

/**
 * Forma para ejecutar el reporte de validación de conteo de inventario
 * 
 * @author Ruben Cancino Ramos
 *
 */
public class ConteoFisicoDiferenciaForm extends SWXAction{
	
	
	public static void run(){
		new ConteoFisicoDiferenciaForm().execute();
	}
	
	@Override
	protected void execute() {
		ReportForm form=new ReportForm();
		form.open();
		if(!form.hasBeenCanceled()){
			Map params=form.getParameters();
			ReportUtils2.runReport("invent/DiferenciasEnConteo.jasper",params);
		}
	}
	
	private class ReportForm extends SXAbstractDialog{
		
		
		
		private JFormattedTextField sectorInicial;
		private JFormattedTextField sectorFinal;
		private JXDatePicker datePicker;
		private JComboBox sucursalBox;
		private JComboBox filtrosBox;
		private JComboBox estadoBox;
		private JComboBox tipoBox;
		private JComboBox ordenBox;
		
		private JFormattedTextField difMayor;
		private JFormattedTextField difMenor;

		public ReportForm() {
			super("Validación de captura");
			
		}
		
		private void init(){
			
			sucursalBox=ReportControls.createSucursalesBox();
			NumberFormatter formatter=new NumberFormatter(NumberFormat.getIntegerInstance());
			formatter.setValueClass(Integer.class);
			sectorInicial=new JFormattedTextField(formatter);
			sectorFinal=new JFormattedTextField(formatter);
			datePicker=new JXDatePicker();
			datePicker.setFormats("dd/MM/yyyy");
			
			filtrosBox=new JComboBox(new Object[]{
					new ParamLabelValue("TODOS"," LIKE \'%\'"),
					new ParamLabelValue("SIN DIFERENCIA","=0"),
					new ParamLabelValue("CON DIFERENCIA","<>0")
					});
			estadoBox=new JComboBox(new Object[]{
					new ParamLabelValue("TODOS"," "),
					new ParamLabelValue("ACTIVOS",	"AND ACTIVO IS TRUE"),
					new ParamLabelValue("INACTIVOS","AND ACTIVO IS FALSE")
					});
			tipoBox=new JComboBox(new Object[]{
					new ParamLabelValue("TODOS"," "),
					new ParamLabelValue("DE LINEA","AND DELINEA IS TRUE"),
					new ParamLabelValue("ESPECIALES","AND DELINEA IS FALSE")
					});
			ordenBox=new JComboBox(new Object[]{
					new ParamLabelValue("CLAVE","L.NOMBRE,CL.NOMBRE,D.CLAVE"),
					new ParamLabelValue("NOMBRE","L.NOMBRE,CL.NOMBRE,D.DESCRIPCION"),
					new ParamLabelValue("DIF ASC","11 ASC"),
					new ParamLabelValue("DIF DESC","11 DESC")
					});
			
			sucursalBox=ReportControls.createSucursalesBox(5L);
			//lineaBox=ReportControls.createLineasBox();		
			//claseBox=ReportControls.createClaseBox();
			
			
			NumberFormat format=NumberFormat.getNumberInstance();
			format.setGroupingUsed(false);
			NumberFormatter formatter2=new NumberFormatter(format);
			formatter2.setValueClass(Double.class);
			
			difMayor=new JFormattedTextField(formatter2);
			difMenor=new JFormattedTextField(formatter2);
			
		}

		@Override
		protected JComponent buildContent() {
			init();
			FormLayout layout=new FormLayout("p,2dlu,p, 3dlu, p,2dlu,p","");
			DefaultFormBuilder builder=new DefaultFormBuilder(layout);
			builder.append("Sector inicial",sectorInicial);
			builder.append("Sector final",sectorFinal);
			builder.append("Sucursal",sucursalBox);
			builder.append("Fecha",datePicker);
			builder.appendSeparator();
			builder.append("Filtrar",filtrosBox);
			builder.append("Estado",estadoBox);
			builder.append("Tipo",tipoBox);
			
			builder.append("Orden",ordenBox,true);
			builder.appendSeparator("Rango de Diferencias");
			builder.append("De",difMayor);
			builder.append("A",difMenor,true);
			
			
			JPanel conten=new JPanel(new BorderLayout());
			conten.add(builder.getPanel(),BorderLayout.CENTER);
			conten.add(buildButtonBarWithOKCancel(),BorderLayout.SOUTH);
			return conten;
		}		
		
		
		
		private Sucursal getSucursal(){
			return (Sucursal)sucursalBox.getSelectedItem();
		}
		
		public Map getParameters(){
			Map map=new HashMap();
			map.put("SECTOR_INICIAL", sectorInicial.getValue());
			map.put("SECTOR_FINAL", sectorFinal.getValue());
			map.put("SUC", getSucursal().getId());
			map.put("FECHA", datePicker.getDate());
			
			map.put("FILTRO", getParam(filtrosBox).stringValue());
			map.put("ACTIVO", getParam(estadoBox).stringValue());
			map.put("DELINEA", getParam(tipoBox).stringValue());
			map.put("ORDENADOR", getParam(ordenBox).stringValue());
			
			Double difIni=(Double)difMayor.getValue();
			Double difFin=(Double)difMenor.getValue();
			if(difIni==null ){
				difIni=new Double(-10000000.00);
			}
			if(difFin==null){
				difFin=new Double(10000000.00);
			}
			map.put("DIF_DE",difIni);
			map.put("DIF_A",difFin);
			
			return map;
		}
		
		private ParamLabelValue getParam(JComboBox box){
			return (ParamLabelValue)box.getSelectedItem();
		}
		
		
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
				ConteoFisicoDiferenciaForm form=new ConteoFisicoDiferenciaForm();
				form.execute();
				//System.exit(0);
			}

		});
	}

}
