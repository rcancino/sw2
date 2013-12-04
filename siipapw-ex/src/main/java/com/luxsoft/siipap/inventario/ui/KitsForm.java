package com.luxsoft.siipap.inventario.ui;

import java.awt.event.ActionListener;
import java.beans.EventHandler;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingWorker;

import org.jdesktop.swingx.JXBusyLabel;

import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.gui.TableFormat;

import com.jgoodies.binding.adapter.BasicComponentFactory;
import com.jgoodies.binding.list.SelectionInList;
import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.validation.ValidationResult;
import com.luxsoft.siipap.inventarios.model.Kit;
import com.luxsoft.siipap.inventarios.model.KitDet;
import com.luxsoft.siipap.model.Sucursal;
import com.luxsoft.siipap.model.core.ConfiguracionKit;
import com.luxsoft.siipap.model.core.Producto;
import com.luxsoft.siipap.model.core.Unidad;
import com.luxsoft.siipap.swing.form2.AbstractMasterDetailForm;
import com.luxsoft.siipap.swing.form2.MasterDetailFormModel;

public class KitsForm extends AbstractMasterDetailForm{

	private JTextField sucursalField;
	
	public KitsForm(MasterDetailFormModel model) {
		super(model);
	}
	
	private KitFormModel getKitModel(){
		return (KitFormModel)model;
	}
	
	@Override
	protected JComponent buildMasterForm() {
		FormLayout layout=new FormLayout(
				"50dlu,2dlu,f:90dlu:g(.5),3dlu," +
				"50dlu,2dlu,f:90dlu:g(.5)"
				,"");
		DefaultFormBuilder builder=new DefaultFormBuilder(layout);
		
		builder.append("Fecha",getControl("fecha"));
		sucursalField=new JTextField();
		sucursalField.setText(model.getValue("sucursal").toString());
		sucursalField.setEditable(false);
		builder.append("Sucursal",sucursalField);
		builder.append("Producto (Kit):",getControl("config"),5);
		builder.append("Cantidad",getControl("cantidad"));
		builder.append("Costo U",addReadOnly("costoUnitario"),true);
		builder.append("Comentario",getControl("comentario"),5);
		if(model.getValue("id")==null){
			builder.append(createProcesarBtn());
			builder.append(label);
		}
		return builder.getPanel();
	}
	
	protected void installToolbar(final JPanel panel){
		
	}
	
	private JButton createProcesarBtn(){
		JButton btn=new JButton("Procesar");
		btn.addActionListener(EventHandler.create(ActionListener.class, this, "doProcess"));
		return btn;
	}
	
	JXBusyLabel label=new JXBusyLabel();
	
	SwingWorker<String, String> worker;
	
	public void doProcess(){
		if(model.getValue("config")==null){
			JOptionPane.showMessageDialog(getContentPane(), "Debe seleccionar un producto kit","Error",JOptionPane.ERROR_MESSAGE);
			return;
		}
		if(getKitModel().getKit().getCantidad()<=0){
			JOptionPane.showMessageDialog(getContentPane(), "Debe indicar la cantidad a producir","Error",JOptionPane.ERROR_MESSAGE);
			return;
		}
		getKitModel().procesa();
		getOKAction().setEnabled(false);
		label.setVisible(true);
		label.setBusy(true);
		worker=new SwingWorker<String, String>(){

			@Override
			protected String doInBackground() throws Exception {
				Thread.sleep(1000);
				publish("Verificando existencias");
				getKitModel().validateExistencias();
				return "OK";
			}

			@Override
			protected void done() {
				label.setBusy(false);				
				try {
					publish("");
					get();			
				} catch (InterruptedException e) {
					e.printStackTrace();
				} catch (ExecutionException e) {
					JOptionPane.showMessageDialog(getContentPane(), e.getCause().getMessage(),"Error",JOptionPane.ERROR_MESSAGE);
				}				
			}

			@Override
			protected void process(List<String> chunks) {
				for(String s:chunks){
					label.setText(s);
				}
			}			
		};
		worker.execute();
	}

	@Override
	protected TableFormat getTableFormat() {
		String props[] = new String[]{"producto.clave","producto.descripcion","cantidad","unidad"};
		String cols[] = new String[]{"Producto","Descripción","Cantidad","Unidad"};
		return GlazedLists.tableFormat(KitDet.class, props, cols);
	}
	
	@Override
	protected JComponent createCustomComponent(String property) {
		if("config".equals(property)){
			SelectionInList sl=new SelectionInList(getKitModel().buscarKits(),model.getModel("config"));
			JComboBox box=BasicComponentFactory.createComboBox(sl);
			box.setEnabled(!model.isReadOnly());
			return box;
		}else
			return null;
	}

	public static Kit showForm(final Sucursal suc){
		Kit kit=new Kit();
		kit.setSucursal(suc);
		final KitFormModel model=new KitFormModel(kit);
		final KitsForm form=new KitsForm(model);
		form.open();
		if(!form.hasBeenCanceled()){
			return model.getKit();
		}else
			return null;
	}
	
	protected static class KitFormModel  extends MasterDetailFormModel{		
		
		public KitFormModel(Object bean) {
			super(bean);
		}

		public KitFormModel(Object bean, boolean readOnly) {
			super(bean, readOnly);
		}
		
		public Kit getKit(){
			return (Kit)getBaseBean();
		}

		@Override
		public boolean manejaTotalesEstandares() {
			return false;
		}
		
		public void procesa(){
			logger.info("Procesando kit......");			
			getKit().procesar();
			source.clear();
			for(KitDet det:getKit().getSalidas()){
				source.add(det);
			}			
		}
		
		public void validateExistencias(){
			String[] errors=new String[]{"No existe material suficiente"};
			if(errors.length>0){
				for(int i=0;i<errors.length;i++){
					ValidationResult res=new ValidationResult();
					res.addError(errors[i]);
					getValidationModel().setResult(res);
				}
				throw new RuntimeException("Error en las existencias");
			}
		}
		
		
		/**
		 * Util para inhabilitar el boton de aceptar
		 */
		public void invalidState(String msg){
			ValidationResult res=new ValidationResult();
			res.addError(msg);
			getValidationModel().setResult(res);
		}
		
		public List<ConfiguracionKit> buscarKits(){
			return testConfig();
		}
		
	}
	
	public static void main(String[] args) {	
		Sucursal s=new Sucursal(1,"Oficinas");
		showForm(s);
	}
	
	public static List<ConfiguracionKit> testConfig(){
		Unidad u=new Unidad("MIL",1000);
		Producto target=new Producto("Kit1 ","Kit de prueba1");
		target.setUnidad(u);
		Producto s1=new Producto("Kit source1 ","Source de kit 1");
		s1.setUnidad(u);
		Producto s2=new Producto("Kit source2 ","Source de kit 2");
		s2.setUnidad(u);
		
		ConfiguracionKit config=new ConfiguracionKit();
		config.setDestino(target);
		config.getPartes().add(new ConfiguracionKit.Elemento(s1,1d));
		config.getPartes().add(new ConfiguracionKit.Elemento(s2,1d));
		List<ConfiguracionKit> res=new ArrayList<ConfiguracionKit>();
		res.add(config);
		return res;
	}

}
