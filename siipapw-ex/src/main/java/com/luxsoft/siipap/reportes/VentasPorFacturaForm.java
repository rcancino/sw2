package com.luxsoft.siipap.reportes;

import java.awt.BorderLayout;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JTextField;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;
import com.luxsoft.siipap.reportes.VentasPorTipoForm.Sucursales;
import com.luxsoft.siipap.swing.reports.ReportForm;
import com.luxsoft.siipap.swing.reports.ReportUtils;
import com.luxsoft.siipap.swing.utils.SWExtUIManager;

public class VentasPorFacturaForm extends ReportForm{

	private JTextField factura;
	private JComboBox suc;
	private JComboBox tipo;
	private Map<String, Object> param=new HashMap<String, Object>();
	
	
	public VentasPorFacturaForm() {
		super("Ventas Por Tipo");
	}
	
	
	
	private void initComponents(){
		factura=new JTextField();
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
		builder.append("CXPFactura",factura,4);
		builder.nextLine();
		builder.append("Sucursal",suc,5);
		builder.nextLine();
		builder.append("Tipo Venta",tipo,5);
		return builder.getPanel();
	}
	
	@Override
	public void doApply() {
		
		Sucursales s=(Sucursales)suc.getSelectedItem();
		param.put("SUCURSAL",new BigDecimal(String.valueOf(s.getNumero())));	
		if(tipo.getSelectedItem().equals("CREDITO")){
			param.put("SERIE","E");
		}
		else if(tipo.getSelectedItem().equals("CAMIONETA")){
			param.put("SERIE","C");
		}
		else if(tipo.getSelectedItem().equals("MOSTRADOR")){
			param.put("SERIE","A");
		}
		param.put("FACTURA",new BigDecimal(factura.getText().toString()));
		param.put("NOMSUC",s.toString());
		String path=ReportUtils.toReportesPathVentas("VentasPorFactura.jasper");
		ReportUtils.viewReport(path,getParam());
		
	}
	
	public Map<String, Object> getParam() {
		return param;
	}
	
	
	public static void main(String[] args) {
		SWExtUIManager.setup();
		VentasPorFacturaForm f=new VentasPorFacturaForm();
		f.open();
	}

}
