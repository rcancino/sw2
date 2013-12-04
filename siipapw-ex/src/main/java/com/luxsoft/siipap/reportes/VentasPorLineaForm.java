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
import com.luxsoft.siipap.reportes.VentaPorKilosForm.Meses;
import com.luxsoft.siipap.swing.reports.ReportForm;
import com.luxsoft.siipap.swing.reports.ReportUtils;
import com.luxsoft.siipap.swing.utils.SWExtUIManager;

public class VentasPorLineaForm extends ReportForm{

	private JXDatePicker fecha_ini;
	private JXDatePicker fecha_fin;
	private JComboBox linea;
	
	private Map<String, Object> param=new HashMap<String, Object>();
	
	public VentasPorLineaForm() {
		super("");
	}

	private void initComponents(){
		fecha_ini=new JXDatePicker();
		fecha_ini.setFormats(new String[]{"dd/MM/yyyy"});
		fecha_fin=new JXDatePicker();
		fecha_fin.setFormats(new String[]{"dd/MM/yyyy"});
		linea=new JComboBox(Lineas.values());
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
		builder.append("Fecha Inicial",fecha_ini,3);
		builder.nextLine();
		builder.append("Fecha Final",fecha_fin,3);
		builder.nextLine();
		builder.append("Linea",linea,3);
		return builder.getPanel();
	}
	
	@Override
	public void doApply() {
		SimpleDateFormat formato=new SimpleDateFormat("dd/MM/yyyy");
		String ini=formato.format(fecha_ini.getDate());
		String fin=formato.format(fecha_fin.getDate());
		
		param.put("FECHA_INI",ini);
		param.put("FECHA_FIN",fin);
		param.put("LINEA",linea.getSelectedItem().toString());
		String path=ReportUtils.toReportesPathVentas("VentasPorLinea.jasper");
		ReportUtils.viewReport(path,getParam());
		
	}
	
	
	
	
	public static void main(String[] args) {
		SWExtUIManager.setup();
		VentasPorLineaForm f=new VentasPorLineaForm();
		f.open();
	}
	
	
	public enum Lineas {

		
		ADHESIVOS("ADHESIVOS",1),
		AUTOCOPIANTE("AUTOCOPIANTE",2),
		BOND("BOND",3),
		BRISTOL("BRISTOL",4),
		CAPLE("CAPLE",5),
		CLASICOS("CLASICOS",6),
		CONTABLE("CONTABLE",7),
		CUBIERTOS("CUBIERTOS",8),
		EUROKOTE("EUROKOTE",9),
		GALGOS("GALGOS",10),
		MANILA("MANILA",11),
		MATERIALDE("MATERIAL DE",12),
		METALIZADOS("METALIZADOS",13),
		OPALINA("OPALINA",14),
		POLYPAP("POLYPAP",15),
		SBSSULFATA("SBS SULFATA",16),
		SOBRESYFO("SOBRES Y FO",17),
		TYVEK("TYVEK",18),
		VARIOS("VARIOS",19),
		;
		
		private final String descripcion;
		private final int numero;
		
		private Lineas(final String descripcion, final int numero) {
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
		
		public static List<Lineas> getSucursales(){
			ArrayList<Lineas> l=new ArrayList<Lineas>();
			for(Lineas c:values()){			
				l.add(c);
			}
			return l;
		}
		
		public static Lineas getMes(int id){
			for(Lineas c:values()){
				if(c.getNumero()==id)
					return c;
			}
			return null;
		}
		
		public static int fixMesNumero(Lineas s){
			if(s.equals(Lineas.ADHESIVOS))
				return 6;
			else if(s.equals(Lineas.AUTOCOPIANTE))
				return 10;
			else
				return s.getNumero();
		}

		

	}


	public Map<String, Object> getParam() {
		return param;
	}
	
	
	
	

}
