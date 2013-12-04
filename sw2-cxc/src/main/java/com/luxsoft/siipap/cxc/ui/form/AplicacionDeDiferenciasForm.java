package com.luxsoft.siipap.cxc.ui.form;

import java.awt.BorderLayout;

import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.jdesktop.swingx.JXDatePicker;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;
import com.luxsoft.siipap.cxc.model.Abono;
import com.luxsoft.siipap.cxc.model.CargoPorDiferencia;
import com.luxsoft.siipap.swing.controls.Header;
import com.luxsoft.siipap.swing.controls.SXAbstractDialog;

/**
 * Genera el reporte de Estado de cuenta de un cliente
 * 
 * @author Ruben Cancino
 * 
 */
public class AplicacionDeDiferenciasForm extends SXAbstractDialog{
	
	private final Abono abono;

	public AplicacionDeDiferenciasForm(Abono abono) {
		super("Aplicación de diferencias");
		this.abono=abono;
	}	

	public JXDatePicker fechaInicial;
	public JComboBox tipo;
	public JTextField disponibleField;
	

	private void initComponents() {
		fechaInicial = new JXDatePicker();
		fechaInicial.setFormats("dd/MM/yyyy");
		tipo=new JComboBox(CargoPorDiferencia.TipoDiferencia.values());
		disponibleField=new JTextField(abono.getDisponible().toString());
		disponibleField.setEditable(false);
		
	}

	private JComponent buildForm() {
		initComponents();
		final FormLayout layout = new FormLayout("p,3dlu,f:60dlu:g", "");
		DefaultFormBuilder builder = new DefaultFormBuilder(layout);
		builder.append("Fecha ", fechaInicial);
		builder.append("Concepto", tipo);
		builder.append("Disponible: ",disponibleField);
		return builder.getPanel();
	}

	protected JComponent buildHeader() {
		return new Header("Aplicacion por diferencia", "Saldando abono: "+abono.getFolio()).getHeader();
	}
	

	@Override
	protected JComponent buildContent() {
		JPanel panel = new JPanel(new BorderLayout());
		panel.add(buildForm(), BorderLayout.CENTER);
		panel.add(buildButtonBarWithOKCancel(), BorderLayout.SOUTH);

		return panel;
	}

	

	public Abono getAbono() {
		return abono;
	}

	
	

	
}
