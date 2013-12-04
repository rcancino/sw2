package com.luxsoft.siipap.reportes;

import java.awt.BorderLayout;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JPanel;

import org.jdesktop.swingx.JXDatePicker;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;
import com.luxsoft.siipap.swing.controls.SXAbstractDialog;
import com.luxsoft.siipap.swing.reports.ReportForm;
import com.luxsoft.siipap.swing.reports.ReportUtils;
import com.luxsoft.siipap.swing.utils.SWExtUIManager;

public class VentasPorTipoForm extends ReportForm{

	private JXDatePicker fecha_ini;
	private JComboBox suc;
	private JComboBox tipo;
	private Map<String, Object> param=new HashMap<String, Object>();
	
	
	public VentasPorTipoForm() {
		super("Ventas Por Tipo");
	}
	
	
	
	private void initComponents(){
		fecha_ini=new JXDatePicker();
		fecha_ini.setFormats(new String[]{"dd/MM/yyyy"});
		suc=new JComboBox(Sucursales.values());
		tipo=new JComboBox();
		tipo.addItem("CREDITO");
		tipo.addItem("CAMIONETA");
		tipo.addItem("MOSTRADOR");

	}

	@Override
	protected JComponent buildContent() {
		initComponents();
		JPanel panel=new JPanel(new BorderLayout());
		panel.add(buildForm(),BorderLayout.CENTER);
		panel.add(buildButtonBarWithOKCancel(),BorderLayout.SOUTH);
		return panel;
	}

	private JComponent buildForm(){
		FormLayout layout=new FormLayout(
				"40dlu,4dlu,40dlu,4dlu,40dlu,4dlu,40dlu","");
		DefaultFormBuilder builder=new DefaultFormBuilder(layout);
		builder.append("Fecha Inicial ",fecha_ini,4);
		builder.nextLine();
		builder.append("Sucursal",suc,5);
		builder.nextLine();
		builder.append("Tipo Venta",tipo,5);
		return builder.getPanel();
	}
	
	@Override
	public void doApply() {
		SimpleDateFormat formato=new SimpleDateFormat("dd/MM/yyyy");
		String ini=formato.format(fecha_ini.getDate());
		
		param.put("FECHA_INI",ini);
		
		Sucursales s=(Sucursales)suc.getSelectedItem();
		param.put("SUCURSAL",String.valueOf(s.getNumero()));
		if(tipo.getSelectedItem().equals("CREDITO")){
			param.put("TIPO","E");
		}
		else if(tipo.getSelectedItem().equals("CAMIONETA")){
			param.put("TIPO","C");
		}
		else if(tipo.getSelectedItem().equals("MOSTRADOR")){
			param.put("TIPO","A");
		}
		param.put("NOMSUC",s.toString());
		String path=ReportUtils.toReportesPathVentas("VentasPorTipo.jasper");
		ReportUtils.viewReport(path,getParam());
		
		
	}
	
	
	public static void main(String[] args) {
		SWExtUIManager.setup();
		VentasPorTipoForm c=new VentasPorTipoForm();
		c.open();
	}
	
	
	public Map<String, Object> getParam() {
		return param;
	}


	public enum Sucursales {
		
		
		ANDRADE("ANDRADE",3),
		BOLIVAR("BOLIVAR",5),
		CALLE4("CALLE 4",10),
		ERMITA("ERMITA",11),
		TACUBA("TACUBA",12),	
		;
		
		private final String descripcion;
		private final int numero;
		
		private Sucursales(final String descripcion, final int numero) {
			this.descripcion = descripcion;
			this.numero = numero;
		}
		
		public String toString(){
			return descripcion;
		}
		
		public int getNumero(){		
			return numero;
		}
		
		public Integer[] todos(){
			return new Integer[]{1,4,6,7,8};
		}
		
		public static List<Sucursales> getSucursales(){
			ArrayList<Sucursales> l=new ArrayList<Sucursales>();
			for(Sucursales c:values()){			
				l.add(c);
			}
			return l;
		}
		
		public static Sucursales getSucursal(int id){
			for(Sucursales c:values()){
				if(c.getNumero()==id)
					return c;
			}
			return null;
		}
		
		public static int fixSucursalNumero(Sucursales s){
			if(s.equals(Sucursales.CALLE4))
				return 6;
			else if(s.equals(Sucursales.ANDRADE))
				return 10;
			else
				return s.getNumero();
		}

		

	}

	
	
}
