package com.luxsoft.siipap.swx.binding;

import java.awt.BorderLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JTextField;

import com.jgoodies.binding.value.ValueHolder;
import com.jgoodies.binding.value.ValueModel;
import com.luxsoft.siipap.dao.core.ClienteDao;
import com.luxsoft.siipap.model.core.ClienteRow;
import com.luxsoft.siipap.service.ServiceLocator2;
import com.luxsoft.siipap.swing.controls.SXAbstractDialog;

/**
 * Control que permite un Binding entre una lista de clientes y un ValuModel
 * Permite seleccionar uno y solo un proveedor
 * 
 * @author Ruben Cancino Ramos
 *
 */
public class ClienteLookup extends JPanel implements ActionListener{
	
	private JButton lookupBtn;
	private JTextField inputField;
	private ValueHolder beanChannel;
	
	//protected WeakReference<ValueModel> vmRef;
	
	private ClienteLookup(final ValueModel vm){
		//vmRef=new WeakReference<ValueModel>(holder);
		beanChannel=new ValueHolder(vm,true);
		initComponents();
	}
	
	
	
	protected void initComponents(){
		setLayout(new BorderLayout());
		
		lookupBtn=new JButton("...");
		lookupBtn.addActionListener(this);
		
		//lookupBtn.setBorderPainted(false);
		lookupBtn.setRolloverEnabled(false);
		lookupBtn.setMargin(new Insets(0, 3, 0, 3));
		lookupBtn.setFocusable(false);
		
		inputField=new JTextField(20);
		inputField.addActionListener(this);
		add(inputField,BorderLayout.CENTER);
		add(lookupBtn,BorderLayout.EAST);
		
		
	}

	public void actionPerformed(ActionEvent e) {
		if(e.getSource()==lookupBtn){
			lookup();
		}else if(e.getSource()==inputField){
			search();
		}		
	}
	
	
	protected void setSelection(final Object o){
		Object res=transform(o);
		beanChannel.setValue(res);
		inputField.setText(format(res));
		
	}
	
	protected String format(Object o){
		if(o==null)
			return "";
		return o.toString();
	}
	
	/**
	 * Permite hacer una transformacion con la seleccion antes de instalarla en el valueModel 
	 * @param source
	 * @return
	 */
	protected Object transform(Object source){
		return source;
	}
	
	LookupDialog dialog;
	
	/**
	 * Muestra la pantalla para seleccionar un elemento
	 */
	protected void lookup(){
		if(dialog==null){
			dialog=new LookupDialog();
		}
		dialog.open();
		
	}
	
	protected void search(){
		System.out.println("Searching...");
		
	}

	
	class ChannelHandler implements PropertyChangeListener{

		public void propertyChange(PropertyChangeEvent evt) {
			setSelection(evt.getNewValue());
			
		}
		
	}
	
	
	class LookupDialog extends SXAbstractDialog{
		
		final BrowserLookup<ClienteRow> browser;

		public LookupDialog() {
			super("Clientes");
			browser=new BrowserLookup<ClienteRow>(ClienteRow.class){
				@Override
				protected void doSelect(ClienteRow bean) {
					setSelection(bean);
					doAccept();
				}

				@Override
				protected List<ClienteRow> findData() {
					List<ClienteRow> clientes=new ArrayList<ClienteRow>();
					((ClienteDao)ServiceLocator2.instance().getContext().getBean("clienteDao")).buscarClientes(clientes);
					return clientes;
				}
				
				
			};
			browser.addProperty("id","clave","nombre");
			browser.addLables("Id","Clave","Nombre");
		}

		@Override
		protected JComponent buildContent() {
			JPanel p=new JPanel(new BorderLayout());
			p.add(browser.getControl(),BorderLayout.CENTER);
			p.add(browser.buildFilterPanel(),BorderLayout.NORTH);
			return p;
		}

		@Override
		public void doClose() {
			browser.getSource().clear();
			super.doClose();
		}

		@Override
		protected void onWindowOpened() {
			browser.load();
		}
		
		
		
		
	}
	
	public static void main(String[] args) {
		final ValueHolder vm=new ValueHolder(null,true);
		SXAbstractDialog dialog=new SXAbstractDialog("TEST"){
			@Override
			protected JComponent buildContent() {
				
				return new ClienteLookup(vm);
			}
			
		};
		dialog.open();
		System.out.println(vm.getValue());
		System.exit(0);
	}

}
