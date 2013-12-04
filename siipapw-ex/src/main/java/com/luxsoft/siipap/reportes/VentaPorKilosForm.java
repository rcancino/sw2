package com.luxsoft.siipap.reportes;

import java.awt.BorderLayout;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JPanel;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;
import com.luxsoft.siipap.swing.reports.ReportForm;
import com.luxsoft.siipap.swing.reports.ReportUtils;
import com.luxsoft.siipap.swing.utils.SWExtUIManager;

public class VentaPorKilosForm extends ReportForm{

	private JComboBox puno;
	private JComboBox pdos;
	
	private Map<String, Object> param=new HashMap<String, Object>();
	
	public VentaPorKilosForm() {
		super("Ventas Por Linea");
	}
	
	private void initComponents(){
		puno=new JComboBox(Meses.values());
		pdos=new JComboBox(Meses.values());
	}

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
		builder.append("Periodo Uno",puno,3);
		builder.nextLine();
		builder.append("Periodo Dos",pdos,3);
		return builder.getPanel();
	}
	
	@Override
	public void doApply() {
		Meses s=(Meses)puno.getSelectedItem();
		param.put("MES1",String.valueOf(s.getNumero()));
		Meses v=(Meses)pdos.getSelectedItem();
		param.put("MES2",String.valueOf(v.getNumero()));
		String path=ReportUtils.toReportesPathVentas("VentaPorKilos.jasper");
		ReportUtils.viewReport(path,getParam());
	}
	
	
	
	public static void main(String[] args) {
		SWExtUIManager.setup();
		VentaPorKilosForm v=new VentaPorKilosForm();
		v.open();
	}
	
	public Map<String, Object> getParam() {
		return param;
	}

	public enum Meses {
		ENERO("Enero",1),
		FEBRERO("Febrero",2),
		MARZO("Marzo",3),
		ABRIL("Abril",4),
		MAYO("Mayo",5),
		JUNIO("Junio",6),
		JJULIO("Julio",7),
		AGOSTO("Agosto",8),
		SEPTIEMBRE("Septiembre",9),
		OCTUBRE("Octubre",10),
		NOVIEMBRE("Noviembre",11),
		DICIEMBRE("Diciembre",12)
		;
		
		private final String descripcion;
		private final int numero;
		
		private Meses(final String descripcion, final int numero) {
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
			return new Integer[]{1,2,3,4,5,6,7,8,9,10,11,12};
		}
		
		public static List<Meses> getSucursales(){
			ArrayList<Meses> l=new ArrayList<Meses>();
			for(Meses c:values()){			
				l.add(c);
			}
			return l;
		}
		
		public static Meses getMes(int id){
			for(Meses c:values()){
				if(c.getNumero()==id)
					return c;
			}
			return null;
		}
		
		public static int fixMesNumero(Meses s){
			if(s.equals(Meses.ENERO))
				return 6;
			else if(s.equals(Meses.FEBRERO))
				return 10;
			else
				return s.getNumero();
		}

		

	}
	

	
	
}
