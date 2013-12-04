package com.luxsoft.siipap.pos.ui.reports;

import java.awt.BorderLayout;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JPanel;

import org.jdesktop.swingx.JXDatePicker;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;
import com.luxsoft.siipap.model.Sucursal;
import com.luxsoft.siipap.model.core.Clase;
import com.luxsoft.siipap.model.core.Linea;
import com.luxsoft.siipap.pos.ui.utils.ReportUtils2;
import com.luxsoft.siipap.swing.actions.SWXAction;
import com.luxsoft.siipap.swing.controls.SXAbstractDialog;

/**
 * Forma para ejecutar el reporte de analisis de diferencia en conteo fisico
 * 
 * @author Ruben Cancino Ramos
 *
 */
public class ConteoFisicoAnalisisDiferenciasForm extends SWXAction{
	
	
	public static void run(){
		new ConteoFisicoAnalisisDiferenciasForm().execute();
	}
	
	@Override
	protected void execute() {
		ReportForm form=new ReportForm();
		form.open();
		if(!form.hasBeenCanceled()){
			Map params=form.getParameters();
			ReportUtils2.runReport("invent/AnalisisDeDiferenciaCF.jasper",params);
		}
	}
	
	private class ReportForm extends SXAbstractDialog{
		
		private JComboBox filtrosBox;
		private JComboBox estadoBox;
		private JComboBox tipoBox;
		private JComboBox ordenBox;
		private JCheckBox exstenciaBox;
		private JComboBox sucursalBox;
		private JComboBox lineaBox;		
		private JComboBox claseBox;
		private JXDatePicker fecha;
		

		public ReportForm() {
			super("Análisis de diferencias");
			
		}
		
		private void init(){
			filtrosBox=new JComboBox(new Object[]{
					new ParamLabelValue("POSITIVOS"		,"AND (I.CANTIDAD/I.FACTORU)>0"),
					new ParamLabelValue("SIN EXISTENCIA","AND (I.CANTIDAD/I.FACTORU)=0"),
					new ParamLabelValue("NEGATIVOS"		,"AND (I.CANTIDAD/I.FACTORU)<0"),
					new ParamLabelValue("POSITIVOS/NEGATIVOS","AND (I.CANTIDAD/I.FACTORU)<>0"),
					new ParamLabelValue("TODOS","")
					});
			estadoBox=new JComboBox(new Object[]{
					new ParamLabelValue("ACTIVOS",	"AND ACTIVO IS TRUE"),
					new ParamLabelValue("INACTIVOS","AND ACTIVO IS FALSE"),
					new ParamLabelValue("TODOS","TODOS")
					});
			tipoBox=new JComboBox(new Object[]{
					new ParamLabelValue("DE LINEA","AND DELINEA IS TRUE"),
					new ParamLabelValue("ESPECIALES","AND DELINEA IS FALSE"),
					new ParamLabelValue("TODOS","TODOS")
					});
			ordenBox=new JComboBox(new Object[]{
					new ParamLabelValue("LINEA","7"),
					new ParamLabelValue("CLASE","8"),
					new ParamLabelValue("CLAVE","9"),
					new ParamLabelValue("NOMBRE","10"),
					new ParamLabelValue("DIFERENCIA","16")
					
					});
			exstenciaBox=new JCheckBox("existencia",true);
			sucursalBox=ReportControls.createSucursalesBox();
			lineaBox=ReportControls.createLineasBox();		
			claseBox=ReportControls.createClaseBox();
			fecha=new JXDatePicker();
			fecha.setFormats("dd/MM/yyyy");
		}

		@Override
		protected JComponent buildContent() {
			init();
			FormLayout layout=new FormLayout("p,2dlu,max(150;p), 3dlu, p,2dlu,max(150;p)","");
			DefaultFormBuilder builder=new DefaultFormBuilder(layout);
			
			builder.append("Fecha",fecha);
			builder.append("Sucursal",sucursalBox);
			
			builder.append("Filtrar",filtrosBox);
			builder.append("Estado",estadoBox);
			
			builder.append("Tipo",tipoBox);			
			builder.append("Orden",ordenBox);			
			
			builder.append("Línea",lineaBox);
			builder.append("Clase",claseBox,true);
			
			builder.append("Con existencias",exstenciaBox,true);
			
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
			map.put("FILTRO", getParam(filtrosBox).stringValue());
			map.put("ACTIVO", getParam(estadoBox).stringValue());
			map.put("DELINEA", getParam(tipoBox).stringValue());
			map.put("ORDENADOR", getParam(ordenBox).intValue());
			map.put("SUC", String.valueOf(getSucursal().getClave()));
			map.put("IMP_EXIS", exstenciaBox.isSelected()?"SI":"NO");
			Linea linea=(Linea)lineaBox.getSelectedItem();
			Clase clase=(Clase)claseBox.getSelectedItem();
			map.put("LINEA", linea!=null?linea.getNombre():"%");
			map.put("CLASE", clase!=null?clase.getNombre():"%");
			return map;
		}
		
		private ParamLabelValue getParam(JComboBox box){
			return (ParamLabelValue)box.getSelectedItem();
		}
		
	}
	
	public static class ParamLabelValue{
		
		private final String name;
		private final Object value;
		
		public ParamLabelValue(String name, Object value) {			
			this.name = name;
			this.value = value;
		}

		public String getName() {
			return name;
		}

		public Object getValue() {
			return value;
		}
		
		public String toString(){
			return name;
		}
		
		public String stringValue(){
			return value.toString();
		}
		
		public Integer intValue(){
			return Integer.valueOf(stringValue());
		}
		
		public Number numberValue(){
			return (Number)value;
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
				ConteoFisicoAnalisisDiferenciasForm form=new ConteoFisicoAnalisisDiferenciasForm();
				form.execute();
				//System.exit(0);
			}

		});
	}

}
