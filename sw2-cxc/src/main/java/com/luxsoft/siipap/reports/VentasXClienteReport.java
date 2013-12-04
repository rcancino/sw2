package com.luxsoft.siipap.reports;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.jdesktop.swingx.JXDatePicker;

import com.jgoodies.binding.value.ValueHolder;
import com.jgoodies.binding.value.ValueModel;
import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.uifextras.panel.HeaderPanel;
import com.luxsoft.siipap.model.Sucursal;
import com.luxsoft.siipap.model.Sucursales;
import com.luxsoft.siipap.model.core.Cliente;
import com.luxsoft.siipap.service.ServiceLocator2;
import com.luxsoft.siipap.swing.actions.SWXAction;
import com.luxsoft.siipap.swing.binding.Binder;
import com.luxsoft.siipap.swing.controls.SXAbstractDialog;
import com.luxsoft.siipap.swing.reports.ReportUtils;
import com.luxsoft.siipap.swing.utils.SWExtUIManager;
import com.luxsoft.siipap.util.DBUtils;


@SuppressWarnings("serial")
public class VentasXClienteReport extends SWXAction{
	
	@Override
	protected void execute() {
		
		final ReportForm form=new ReportForm();
		form.open();
		if(!form.hasBeenCanceled()){
			if(logger.isDebugEnabled()){
				logger.debug("Parametros enviados: "+form.getParametros());
			}
			if(form.getCk().isSelected()==true){
				ReportUtils.viewReport(ReportUtils.toReportesPath("ventas/VentasClienteResumen.jasper"), form.getParametros());
			}			
            if(form.getCk().isSelected()==false){
            	ReportUtils.viewReport(ReportUtils
            			.toReportesPath("ventas/VentasCliente.jasper"), form.getParametros());	
			}
			
			
		}
		form.dispose();
	}
	
	
	

	public class ReportForm extends SXAbstractDialog{
		public  Map<String, Object>parametros;
		private JXDatePicker ini;
		private JXDatePicker fin;
		private JComponent cliente;
		private JComboBox sucursales;
		private JComboBox tipo_vta;
		private JComboBox orden1;
		private JComboBox orden2;
		private JCheckBox ck;
		private JCheckBox todasLasSucursales;
		ValueModel clienteModel;

		public ReportForm() {
			super("Ventas Netas");
			parametros=new HashMap<String, Object>();
		}
		
		
		@Override
		protected void setResizable() {
			setResizable(true);
		}


		private void initComponents(){
			clienteModel=new ValueHolder();
			cliente=Binder.createClientesBinding(clienteModel);
			ini=new JXDatePicker();
			ini.setFormats(new String[]{"dd/MM/yyyy"});
			fin=new JXDatePicker();
			fin.setFormats(new String[]{"dd/MM/yyyy"});
			sucursales=createSucursalControl();
			tipo_vta=new JComboBox(Order2.values());
			orden1=new JComboBox(Order1Det.values());
			orden2=new JComboBox();
			orden2.addItem("ASC");
			orden2.addItem("DESC");
			ck=new JCheckBox();
			ck.setSelected(false);
			todasLasSucursales=new JCheckBox("Todas",true);
			todasLasSucursales.addActionListener(new ActionListener(){
				public void actionPerformed(ActionEvent e) {
					sucursales.setEnabled(!todasLasSucursales.isSelected());
				}
			});
			sucursales.setEnabled(!todasLasSucursales.isSelected());
			
		}

		@Override
		protected JComponent buildContent() {
			initComponents();
			JPanel panel =new JPanel(new BorderLayout());
			panel.add(buildForma(),BorderLayout.CENTER);
			panel.add(buildButtonBarWithOKCancel(),BorderLayout.SOUTH);
			return panel;
		}

		private JComponent buildForma() {
			FormLayout layout=new FormLayout(
					"50dlu,4dlu,50dlu,40dlu,50dlu,50dlu,4dlu,50dlu,40dlu,2dlu,40dlu",
					"pref,20dlu,pref,20dlu,pref,20dlu,pref");
			PanelBuilder builder=new PanelBuilder(layout);
			CellConstraints cc=new CellConstraints();
			builder.add(new JLabel("Clave"),cc.xyw(1, 2, 2));
			builder.add(cliente,cc.xyw(3, 2, 7));
			builder.add(new JLabel("Inicial"),cc.xyw(1,3,2));
			builder.add(ini,cc.xyw(3, 3, 2));
			builder.add(new JLabel("final"),cc.xyw(6, 3, 2));
			builder.add(fin,cc.xyw(7, 3, 3));
			builder.add(new JLabel("Tipo"),cc.xyw(1, 4, 2));
			builder.add(tipo_vta,cc.xyw(3, 4, 2));
			builder.add(new JLabel("Sucursal"),cc.xyw(6, 4, 2));
			builder.add(todasLasSucursales,cc.xyw(11, 4, 1));
			builder.add(sucursales,cc.xyw(7, 4, 3));
			//builder.add(new JLabel("Orden 1"),cc.xyw(1, 5, 2));
			//builder.add(orden1,cc.xyw(3, 5, 2));
			//builder.add(new JLabel("Orden 2"),cc.xyw(6, 5, 2));
			//builder.add(orden2,cc.xyw(7, 5, 2));
			builder.add(new JLabel("Resumen"),cc.xyw(6, 6, 2));
			builder.add(ck,cc.xyw(7, 6, 2));
			
			return builder.getPanel();
		}
		
		public Map<String, Object> getParametros() {
			return parametros;
		}
		
		
		public void doApply(){
			
			Order2 om=(Order2)tipo_vta.getSelectedItem();
			parametros.put("FECHA_INI",ini.getDate());
			parametros.put("FECHA_FIN",fin.getDate());
			if(todasLasSucursales.isSelected())
				parametros.put("SUCURSAL","%");
			else
				parametros.put("SUCURSAL",String.valueOf(getSucursal()));
			parametros.put("ORIGEN",om.getValor().toString());
			Cliente a=(Cliente)clienteModel.getValue();
			if(a!=null){
				parametros.put("CLAVE",a.getClave());
			}
			
			
			Order1Det o=(Order1Det)orden1.getSelectedItem();
			parametros.put("ORDER1",String.valueOf(o.getNumero()));
			parametros.put("ORDER2", orden2.getSelectedItem());
			System.out.println("Parametros: "+parametros);
		}
		
		
		private JComboBox createSucursalControl() {			
			final JComboBox box = new JComboBox(ServiceLocator2
					.getLookupManager().getSucursalesOperativas().toArray());
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
		
		private Long getSucursal(){
			Sucursal selected=(Sucursal)sucursales.getSelectedItem();
			return selected.getId();
		}
	

		@Override
		protected JComponent buildHeader() {
			return new HeaderPanel("Ventas Por Cliente"
					,"Reporte por Cliente Detalle y  Resumen");
		}


		public JCheckBox getCk() {
			return ck;
		}
	
		
		
		
	}
	
	
	public static void main(String[] args) {
		SWExtUIManager.setup();
		DBUtils.whereWeAre();
		VentasCreditoContadoReport c=new VentasCreditoContadoReport();
		c.execute();
		
	}
	
	private enum Order1Det{
		
		clave("ClaveCliente","1"),
		nombre("NombreCliente","2"),
		Venta("VentaNeta","7"),
		TC("Venta Total Cliente","8")
		;
		
		private final String descripcion;
		private final String numero;
		
		private Order1Det(final String descripcion, final String numero) {
			this.descripcion = descripcion;
			this.numero = numero;
		}
		
		public String toString(){
			return descripcion;
		}
		
		public String getNumero(){
			return numero;
		}
		
		public Integer[] todos(){
			return new Integer[]{1,4,6,7,8};
		}
		
		public static List<Order1Det> getOrder1(){
			ArrayList<Order1Det> l=new ArrayList<Order1Det>();
			for(Order1Det c:values()){			
				l.add(c);
			}
			return l;
		}
		
		public static Order1Det getOrder1(String id){
			for(Order1Det c:values()){
				if(c.getNumero()==id)
					return c;
			}
			return null;
		}
		}

	


	


}
