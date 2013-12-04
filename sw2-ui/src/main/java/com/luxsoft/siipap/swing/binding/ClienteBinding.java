package com.luxsoft.siipap.swing.binding;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.FocusManager;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.KeyStroke;

import org.apache.log4j.Logger;

import com.jgoodies.binding.value.ComponentValueModel;
import com.jgoodies.binding.value.ValueModel;
import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.uif.component.ToolBarButton;
import com.jgoodies.uif.util.ComponentUtils;
import com.luxsoft.siipap.dao.core.ClienteDao;
import com.luxsoft.siipap.model.core.Cliente;
import com.luxsoft.siipap.service.ServiceLocator2;
import com.luxsoft.siipap.swing.controls.AbstractControl;
import com.luxsoft.siipap.swing.controls.UpperCaseField;
import com.luxsoft.siipap.swing.selectores.SelectorDeClientes;

public class ClienteBinding extends AbstractControl{
	
	private JTextField tfClave;
	private JTextField tfNombre;
	private JButton btnF2;
	private Action lookupAction;
	private ValueModel cliente;
	
	private Logger logger=Logger.getLogger(getClass());
	
	public ClienteBinding(final ValueModel valueModel){		
		cliente=valueModel;
		cliente.addValueChangeListener(clienteHandler);
		
		if(valueModel instanceof ComponentValueModel){
			ComponentValueModel cm=(ComponentValueModel)cliente;
			cm.addPropertyChangeListener(new PropertyChangeListener(){
				public void propertyChange(PropertyChangeEvent evt) {
					if(ComponentValueModel.PROPERTYNAME_ENABLED.equals(evt.getPropertyName())){
						boolean val=(Boolean)evt.getNewValue();
						setEnabled(val);
					}
				}				
			});
			//setEnabled(cm.isEnabled());
		}
	}
	
	

	@Override
	protected JComponent buildContent() {
		init();
		FormLayout layout=new FormLayout("f:p,2dlu,f:p:g,2dlu,f:20dlu","p");
		JPanel cp=new JPanel(){

			@Override
			public void setBackground(Color bg) {				
				super.setBackground(bg);
				tfClave.setBackground(bg);
				tfNombre.setBackground(bg);
			}
			
		};
		PanelBuilder builder=new PanelBuilder(layout,cp);
		CellConstraints cc=new CellConstraints();		
		builder.nextLine();
		builder.add(tfClave,cc.xy(1, 1));
		builder.nextLine();
		builder.add(tfNombre,cc.xy(3, 1));
		builder.nextLine();
		builder.add(btnF2,cc.xy(5, 1));
		updateFields();
		JPanel panel= builder.getPanel();
		return panel;
	}
	
	private void init(){
		lookupAction=new AbstractAction("lookup"){
			public void actionPerformed(ActionEvent e) {
				lookup();				
			}			
		};
		lookupAction.putValue(Action.SMALL_ICON, getIconFromResource("images/misc/tsearch_obj.gif"));
		tfClave=new UpperCaseField(7);
		tfClave.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				getCliente();
			}			
		});
		tfNombre=new UpperCaseField(40);
		tfClave.addActionListener(new ClienteHandler());
		tfNombre.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				if(cliente.getValue()!=null){
					tfNombre.transferFocus();
				}
			}			
		});
		btnF2=new ToolBarButton(lookupAction);
		btnF2.setText("...");
		btnF2.setFocusable(false);
		
		ComponentUtils.addAction(tfClave, lookupAction, KeyStroke.getKeyStroke("F2"));
		ComponentUtils.addAction(tfNombre, lookupAction, KeyStroke.getKeyStroke("F2"));
		
	}
	
	private void lookup(){
		JTextField tf=tfClave.hasFocus()?tfClave:tfNombre;
		if(tf.getText().length()==0){
			JOptionPane.showMessageDialog(this.getControl()
					, "Digite una clave o nombre para localizar al cliente desado"
					,"Selección de Cliente"
					,JOptionPane.WARNING_MESSAGE);
			return;
		}else{
			SelectorDeClientes selector=new SelectorDeClientes();
			selector.setLocationRelativeTo(getControl());
			if(tfClave.hasFocus())
				selector.cargarPorClave(tfClave.getText());
			else
				selector.cargarPorNombre(tfNombre.getText());
			selector.open();
			if(!selector.hasBeenCanceled()){
				cliente.setValue(selector.getSelection());
				updateFields();
			}
			
		}
		
	}
	
	private void getCliente(){
		if(cliente.getValue()!=null)
			transferFocus();
		if(tfClave.getText().length()==7){
			if(logger.isDebugEnabled()){
				logger.debug("Solicitando cliente: "+tfClave.getText());				
			}
		}else
			lookup();
		
	}
	
	private void transferFocus(){
		FocusManager.getCurrentKeyboardFocusManager().focusNextComponent();
	}
	
	private void updateFields(){
		Cliente c=(Cliente)cliente.getValue();
		if(c!=null){			
			tfClave.setText(c.getClave());
			tfNombre.setText(c.getNombre());
		}else{
			tfClave.setText("");
			tfNombre.setText("");
		}
	}
	
	public void setEnabled(boolean val){
		this.tfClave.setEditable(val);
		this.tfNombre.setEditable(val);
		this.lookupAction.setEnabled(val);
		
	}
	
	/**
	 * Observa los cambios en el modelo y actualiza los componentes
	 */
	private PropertyChangeListener clienteHandler=new PropertyChangeListener(){

		public void propertyChange(PropertyChangeEvent evt) {			
			updateFields();
		}
		
	};
	
	private class ClienteHandler implements ActionListener{

		public void actionPerformed(ActionEvent e) {
			ClienteDao dao=(ClienteDao)ServiceLocator2.getClienteManager().getClienteDao();
			Cliente c=dao.buscarPorClave(tfClave.getText());
			if(c!=null){
				cliente.setValue(c);
			}
			
		}
		
	}
	
	

}
