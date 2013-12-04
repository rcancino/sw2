package com.luxsoft.siipap.pos.ui.forms.caja;

import java.awt.KeyEventPostProcessor;
import java.awt.event.KeyEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.KeyStroke;

import com.luxsoft.siipap.swing.controls.Header;
import com.luxsoft.siipap.swing.form2.AbstractForm;
import com.luxsoft.siipap.swing.utils.ResourcesUtils;

/**
 * Forma para registrar el pago de un pedido y/o factura
 * 
 * 
 * @author Ruben Cancino Ramos
 *
 */
public class PagoForm extends AbstractForm implements PropertyChangeListener{
	
	private Header header;
	private boolean selectionDeCliente=true;
	private boolean persistirAlAplicar=true;
	private boolean formaDePagoModificable=true;
	
	protected java.awt.Font DEFAULT_FONT= new java.awt.Font("SansSerif", 0, 15);
	
	public PagoForm(PagoFormModel model) {
		super(model);
		setTitle("Registro de pago            "+model.getValue("sucursal"));
	}
	
	public PagoFormModel getPagoFormModel(){
		return (PagoFormModel)getModel();
	}
	
	public JComponent buildHeader(){
		if(header==null){
			header=new Header("Registro de pago","");
			header.getHeader().setIcon(ResourcesUtils.getIconFromResource("images/siipapwin/ventas64.gif"));
			updateHeader();
		}
		return header.getHeader();
	}
	
	KeyHandler keyHandler;
	
	public void open(){
		model.getModel("cliente").addValueChangeListener(this);
		//keyHandler=new KeyHandler();
		//KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventPostProcessor(keyHandler);
		super.open();
		
	}
	public void close(){
		model.getModel("cliente").addValueChangeListener(this);
		//KeyboardFocusManager.getCurrentKeyboardFocusManager().removeKeyEventPostProcessor(keyHandler);
		super.close();
		
	}
	
	private void initActions(){
		getOKAction().putValue(Action.NAME, "Aplicar [F10]");
		getOKAction().putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke("F10"));
	}
	
	
	@Override
	protected JComponent buildFormPanel() {
		initActions();
		PagoPanel pagoPanel=new PagoPanel(model.getMainModel(),selectionDeCliente);	
		pagoPanel.setFormaDePagoModificable(isFormaDePagoModificable());
		//ComponentUtils.decorateSpecialFocusTraversal(pagoPanel);
		pagoPanel.setFormasDePago(getPagoFormModel().getFormasDePago());
		return pagoPanel;
	}
	
	 @Override
	public void doAccept() {
		 if(getOKAction().isEnabled()){
			 getPagoFormModel().applicar();
			 if(isPersistirAlAplicar())
				 getPagoFormModel().persistir();
			 else
				 super.doAccept();
		 }
	}		
	
	public void propertyChange(PropertyChangeEvent evt) {
		updateHeader();
	}

	private void updateHeader() {
		if(getPagoFormModel().getPago().getCliente()!=null){
			header.setDescripcion(getPagoFormModel().getPago().getCliente().getNombreRazon());
		}else{
			header.setDescripcion("Seleccione un cliente");
		}
	}
	
	
	
	public boolean isPersistirAlAplicar() {
		return persistirAlAplicar;
	}

	public void setPersistirAlAplicar(boolean persistirAlAplicar) {
		this.persistirAlAplicar = persistirAlAplicar;
	}

	public boolean isSelectionDeCliente() {
		return selectionDeCliente;
	}

	public void setSelectionDeCliente(boolean selectionDeCliente) {
		this.selectionDeCliente = selectionDeCliente;
	}
	
	public boolean isFormaDePagoModificable() {
		return formaDePagoModificable;
	}

	public void setFormaDePagoModificable(boolean formaDePagoModificable) {
		this.formaDePagoModificable = formaDePagoModificable;
	}



	private class KeyHandler implements KeyEventPostProcessor{
		/**
		 * Implementacion de {@link KeyEventPostProcessor} para los accesos de teclado rápido
		 * 
		 */
		public boolean postProcessKeyEvent(final  KeyEvent e) {
			if(KeyEvent.KEY_PRESSED==e.getID()){
				if(KeyStroke.getKeyStroke("F10").getKeyCode()==e.getKeyCode()){
					if(isFocused()){
						e.consume();
						if(getOKAction().isEnabled())
							getOKAction().actionPerformed(null);
					}
				}
			}
			return false;
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
				PagoFormModel model=new PagoFormModel();
				PagoForm form=new PagoForm(model);
				form.setFormaDePagoModificable(false);
				form.open();
				System.exit(0);
			}

		});
	}

}
