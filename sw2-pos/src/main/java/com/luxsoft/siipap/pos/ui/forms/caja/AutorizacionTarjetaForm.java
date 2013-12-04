package com.luxsoft.siipap.pos.ui.forms.caja;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.EventHandler;
import java.math.BigDecimal;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.SwingWorker;

import org.apache.commons.lang.StringUtils;
import org.hibernate.validator.AssertTrue;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.factories.Borders;
import com.jgoodies.forms.factories.ButtonBarFactory;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.uif.component.UIFButton;
import com.jgoodies.uif.util.SystemUtils;
import com.jgoodies.uifextras.panel.HeaderPanel;
import com.luxsoft.luxor.utils.Bean;
import com.luxsoft.siipap.cxc.model.Tarjeta;
import com.luxsoft.siipap.swing.form2.AbstractForm;
import com.luxsoft.siipap.swing.form2.DefaultFormModel;
import com.luxsoft.siipap.swing.form2.IFormModel;
import com.luxsoft.siipap.swing.utils.ComponentUtils;
import com.luxsoft.siipap.swing.utils.MessageUtils;
import com.luxsoft.siipap.swing.utils.TaskUtils;

/**
 * Forma para la autorizacion de pagos con tarjeta
 * 
 * @author Ruben Cancino Ramos
 *
 */
public class AutorizacionTarjetaForm extends AbstractForm{

	public AutorizacionTarjetaForm(IFormModel model) {
		super(model);
		setTitle("Pago con tarjeta");
	}
	
	private HeaderPanel header;
	
	@Override
	protected  JComponent buildContent() {
		final JPanel panel=new JPanel(new BorderLayout());
		panel.add(buildMainPanel(),BorderLayout.CENTER);
		if(model.isReadOnly()){
			panel.add(buildButtonBarWithClose(),BorderLayout.SOUTH);			
		}else
			panel.add(buildButtonBarWithOKCancelApply(),BorderLayout.SOUTH);
		return panel;
	}
	
	 protected JComponent buildButtonBarWithOKCancelApply() {
	        JPanel bar = ButtonBarFactory.buildOKCancelApplyBar(
	            createOKButton(true),
	            createCancelButton(),
	            createSolicitudButton());
	        bar.setBorder(Borders.BUTTON_BAR_GAP_BORDER);
	        return bar;
	 }
	
	private JButton createSolicitudButton() {
		 Action action=new AbstractAction("Solicitar Aut. [F7]"){
			public void actionPerformed(ActionEvent e) {
				solicitarAutorizacionEnLinea();
			}
		 };
		 UIFButton button = new UIFButton(action);
		 button.addActionListener(EventHandler.create(ActionListener.class, "this", "solicitarAutorizacionEnLinea"));
		 ComponentUtils.addAction(button, action, KeyStroke.getKeyStroke("F7"),JComponent.WHEN_IN_FOCUSED_WINDOW);
	     button.setVerifyInputWhenFocusTarget(false);
	     return button;
	}

	@Override
	protected JComponent buildHeader() {
		header=new HeaderPanel("Solicitud de autorización"
				,"Registre el número o deslice la tarjeta");
		return header;
	}
	
	public void solicitarAutorizacionEnLinea(){
		final SwingWorker worker=new SwingWorker(){
			protected Object doInBackground() throws Exception {
				SystemUtils.sleep(5000);
				throw new UnsupportedOperationException("Servicio de autorizaciones en línea no disponible");
			}
			
			protected void done() {
				try {
					model.setValue("autorizacion", get());
				} catch (Exception e) {
					e.printStackTrace();
					MessageUtils.showSimpleWrappedErrorMessage(e, AutorizacionTarjetaForm.this);
				}
			}
		};
		TaskUtils.executeSwingWorker(worker);
	}


	@Override
	protected JComponent buildFormPanel() {
		FormLayout layout=new FormLayout("p,2dlu,p:g","");
		DefaultFormBuilder builder=new DefaultFormBuilder(layout);
		builder.append("Número",getControl("numeroTarjeta"));
		builder.append("Importe",addReadOnly("importe"));
		builder.append("Autorización",getControl("autorizacion"));
		return builder.getPanel();
	}


	public static class TarjetaModel {
		//Tarjeta
		private BigDecimal importe;
		private Tarjeta tarjeta;
		private String autorizacion;
		private String numeroTarjeta;
		
		
		
		public BigDecimal getImporte() {
			return importe;
		}
		public void setImporte(BigDecimal importe) {
			this.importe = importe;
		}
		public Tarjeta getTarjeta() {
			return tarjeta;
		}
		public void setTarjeta(Tarjeta tarjeta) {
			this.tarjeta = tarjeta;
		}
		public String getAutorizacion() {
			return autorizacion;
		}
		public void setAutorizacion(String autorizacion) {
			this.autorizacion = autorizacion;
		}
		public String getNumeroTarjeta() {
			return numeroTarjeta;
		}
		public void setNumeroTarjeta(String numeroTarjeta) {
			this.numeroTarjeta = numeroTarjeta;
		}
		
		@AssertTrue(message="Número de tarjeta invalida")
		public boolean validarNumeroTarjeta(){
			return StringUtils.isNumeric(numeroTarjeta);
		}
		
		@AssertTrue(message="Número de tarjeta invalida")
		public boolean validarAutorizacionTarjeta(){
			return !StringUtils.isBlank(autorizacion);
		}
		
	}
	
	
	public static boolean autorizar(PagoModel pago){
		TarjetaModel tarjeta=(TarjetaModel)Bean.proxy(TarjetaModel.class);
		tarjeta.setImporte(pago.getEfectivo());
		tarjeta.setTarjeta(pago.getTarjeta());
		final DefaultFormModel model=new DefaultFormModel(tarjeta);
		
		final AutorizacionTarjetaForm form=new AutorizacionTarjetaForm(model);
		form.open();
		if(!form.hasBeenCanceled()){
			pago.setAutorizacion(tarjeta.getAutorizacion());
			pago.setTarjeta(tarjeta.getTarjeta());
			pago.setNumeroTarjeta(tarjeta.getNumeroTarjeta());
			
			return true;
		}
		return false;
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
				PagoModel pago=(PagoModel)Bean.proxy(PagoModel.class);
				pago.setEfectivo(BigDecimal.valueOf(50000));
				autorizar(pago);
				System.exit(0);
			}

		});
	}
	
}
