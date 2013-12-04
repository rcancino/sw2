package com.luxsoft.siipap.pos.ui.forms;

import java.text.SimpleDateFormat;

import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JFormattedTextField;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.jdesktop.swingx.VerticalLayout;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;
import com.luxsoft.siipap.swing.actions.DispatchingAction;
import com.luxsoft.siipap.swing.form2.AbstractForm;
import com.luxsoft.siipap.swing.form2.DefaultFormModel;
import com.luxsoft.siipap.swing.form2.IFormModel;
import com.luxsoft.siipap.swing.utils.ResourcesUtils;
import com.luxsoft.siipap.ventas.model.Venta;
import com.luxsoft.sw3.embarque.SolicitudDeEmbarque;

/**
 * Forma para agregar entregas a una 
 * 
 * @author Ruben Cancino
 *
 */
public class SolicitudDeEmbarqueForm extends AbstractForm{

	public SolicitudDeEmbarqueForm(IFormModel model) {
		super(model);
		setTitle("Solicitud de embarque");
		
	}
	
	private JFormattedTextField solicitudField;
	private JTextField documentoField;
	private JTextField fiscalField;
	private JTextField origenField;
	private JFormattedTextField fechaField;
	private JTextField nombreField;
	
	
	private void initComponents(){
		//solicitudField=new JFormattedTextField();
		//solicitudField.setEditable(false);
		documentoField=new JTextField();
		documentoField.setEditable(false);
		fiscalField=new JTextField(10);
		fiscalField.setEditable(false);
		origenField=new JTextField(10);
		origenField.setEditable(false);
		fechaField=new JFormattedTextField(new SimpleDateFormat("dd/MM/yyyy"));
		fechaField.setEditable(false);
		nombreField=new JTextField(20);
		nombreField.setEditable(false);
		
	}

	@Override
	protected JComponent buildFormPanel() {
		initComponents();
		final JPanel panel=new JPanel(new VerticalLayout());
		
		final FormLayout layout=new FormLayout(
				"p,2dlu,70dlu,3dlu," +
				"p,2dlu,70dlu"
				,"");
		final DefaultFormBuilder builder=new DefaultFormBuilder(layout);
		//solicitudField=new JFormattedTextField();
		//builder.append("Solicitud",solicitudField,new JButton(getLookupAction()));
		//builder.nextLine();	
		builder.appendSeparator("Factura");		
			
		builder.append("Documento",documentoField);	
		builder.append("Fiscal",fiscalField);		
		builder.append("Fecha",fechaField);
		//builder.nextLine();	
		builder.append("Origen",origenField);
		builder.append("Cliente",nombreField,5);
		//builder.nextLine();
		builder.appendSeparator("Instrucción");
		builder.append("Comentario",getControl("comentario"),5);
		
		builder.append("Dirección",getControl("direccion"),5);
		panel.add(builder.getPanel());
		
		return panel;
	}
	@Override
	protected void onWindowOpened() {
		updateFactura();
		getControl("comentario").requestFocusInWindow();
		super.onWindowOpened();
	}
	
	private void updateFactura(){
		if(model.getValue("factura")!=null){
			Venta fac=(Venta)model.getValue("factura");
			//solicitudField.setText(fac.)
			documentoField.setText(fac.getDocumento().toString());
			fiscalField.setText(fac.getNumeroFiscal().toString());
			origenField.setText(fac.getOrigen().name());
			fechaField.setValue(fac.getFecha());
			nombreField.setText(fac.getNombre());
		}
	}
	
	private Action lookupAction;
	
	public Action getLookupAction(){
		if(lookupAction==null){
			lookupAction=new DispatchingAction(this,"buscarFactura");
			lookupAction.putValue(Action.SMALL_ICON, ResourcesUtils.getIconFromResource("images/misc2/page_find.png"));
			lookupAction.putValue(Action.NAME, "F2");
		}
		return lookupAction;
	}
	
	public static void main(String[] args) {
		javax.swing.SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				com.luxsoft.siipap.swing.utils.SWExtUIManager.setup();
				final DefaultFormModel model=new DefaultFormModel(new SolicitudDeEmbarque());
				final SolicitudDeEmbarqueForm form =new SolicitudDeEmbarqueForm(model);
				form.open();
				System.exit(0);
			}

		});
	}

}
