package com.luxsoft.siipap.pos.ui.reports;

import java.awt.BorderLayout;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JPanel;

import org.springframework.jdbc.core.RowMapper;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;
import com.luxsoft.siipap.model.Sucursal;
import com.luxsoft.siipap.model.core.Clase;
import com.luxsoft.siipap.model.core.Linea;
import com.luxsoft.siipap.pos.ui.reports.ConteoSelectivoDeInventarioForm.Familia;
import com.luxsoft.siipap.pos.ui.utils.ReportUtils2;
import com.luxsoft.siipap.replica.reader.DBFException;
import com.luxsoft.siipap.replica.reader.DBFMapConverter;
import com.luxsoft.siipap.swing.actions.SWXAction;
import com.luxsoft.siipap.swing.controls.SXAbstractDialog;
import com.luxsoft.sw3.services.Services;


/**
 * Forma para el conteo selectivo de inventarios
 * 
 * @author Ruben Cancino Ramos
 *
 */
public class MaterialEnRecorteReportForm extends SWXAction{
	
	
	public static void run(){
		new MaterialEnRecorteReportForm().execute();
	}
	
	@Override
	protected void execute() {
		ReportForm form=new ReportForm();
		form.open();
		if(!form.hasBeenCanceled()){
			if(form.detalleBox.isSelected()){
				Map params=form.getParameters();
			ReportUtils2.runReport("invent/RecorteXDetalle.jasper",params);
			}
			else{
			Map params=form.getParameters();
			ReportUtils2.runReport("invent/RecorteXSuc.jasper",params);
			}
		}
	}
	
	private class ReportForm extends SXAbstractDialog{
		
		private JComboBox filtrosBox;
		private JComboBox estadoBox;
		private JComboBox tipoBox;
		//private JComboBox ordenBox;
		private JCheckBox detalleBox;
		private JComboBox sucursalBox;
		private JComboBox lineaBox;		
		private JComboBox claseBox;
		private JComboBox familiaBox;
		private JComboBox familia2Box;
		

		public ReportForm() {
			super("Existencias en inventario");
			
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
					new ParamLabelValue("TODOS","")
					});
			tipoBox=new JComboBox(new Object[]{
					new ParamLabelValue("DE LINEA","AND DELINEA IS TRUE"),
					new ParamLabelValue("ESPECIALES","AND DELINEA IS FALSE"),
					new ParamLabelValue("TODOS","")
					});
				//ordenBox=new JComboBox(new Object[]{
			//	new ParamLabelValue("CLAVE","9"),
				//	new ParamLabelValue("NOMBRE","10")
				//	});
			detalleBox=new JCheckBox("",false);
			sucursalBox=ReportControls.createSucursalesBox();
			//lineaBox=ReportControls.createLineasBox();		
			//claseBox=ReportControls.createClaseBox();
			familiaBox=ReportControls.createFamiliasBox();
			familia2Box=ReportControls.createFamiliasBox();
		}

		

		@Override
		protected JComponent buildContent() {
			init();
			FormLayout layout=new FormLayout("p,2dlu,f:max(150dlu;p):g, 3dlu, p,2dlu,f:max(150dlu;p)","");
			DefaultFormBuilder builder=new DefaultFormBuilder(layout);
			
			builder.append("Filtrar",filtrosBox);
			builder.append("Estado",estadoBox);
			builder.append("Tipo",tipoBox);
			builder.append("Sucursal",sucursalBox);
			builder.append("General",detalleBox,true);
			//builder.append("Orden",ordenBox);
			//builder.append("Línea",lineaBox);
			//builder.append("Clase",claseBox);
			builder.appendSeparator("Familias");
			builder.append("De",familiaBox,true);
			builder.append("A",familia2Box,true);
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
			//map.put("ORDENADOR", getParam(ordenBox).intValue());
			if (detalleBox.isSelected())
				map.put("SUC", "%");
			else
				map.put("SUC", String.valueOf(getSucursal().getClave()));
			//map.put("IMP_EXIS", exstenciaBox.isSelected()?"SI":"NO");
			//Linea linea=(Linea)lineaBox.getSelectedItem();
			//Clase clase=(Clase)claseBox.getSelectedItem();
			Familia fam=(Familia)familiaBox.getSelectedItem();
			//map.put("LINEA", linea!=null?linea.getNombre():"%");
			//map.put("CLASE", clase!=null?clase.getNombre():"%");
			map.put("FAMILIA", fam!=null?fam.getClave():"%");
			map.put("FAMILIA_NAME", fam!=null?fam.getNombre():"%");
			
			Familia fam2=(Familia)familia2Box.getSelectedItem();
			map.put("FAMILIA2", fam2!=null?fam2.getClave():"%");
			map.put("FAMILIA2_NAME", fam!=null?fam2.getNombre():"%");
			
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
				MaterialEnRecorteReportForm form=new MaterialEnRecorteReportForm();
				form.execute();
				//System.exit(0);
			}

		});
	}

}
