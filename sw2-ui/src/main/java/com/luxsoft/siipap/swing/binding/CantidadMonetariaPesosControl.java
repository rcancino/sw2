package com.luxsoft.siipap.swing.binding;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.MessageFormat;
import java.text.NumberFormat;
import java.util.Arrays;
import java.util.Currency;
import java.util.Locale;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFormattedTextField;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.text.NumberFormatter;

import com.jgoodies.binding.adapter.BasicComponentFactory;
import com.jgoodies.binding.list.SelectionInList;
import com.jgoodies.binding.value.ValueHolder;
import com.jgoodies.binding.value.ValueModel;
import com.luxsoft.siipap.model.CantidadMonetaria;
import com.luxsoft.siipap.swing.controls.SXAbstractDialog;
import com.luxsoft.siipap.util.MonedasUtils;

/**
 * Binding control para {@link CantidadMonetaria}
 * 
 * @author Ruben Cancino
 *
 */
public class CantidadMonetariaPesosControl extends JPanel implements PropertyChangeListener{
	
	private JComboBox monedaBox;
	private JFormattedTextField inputField;
	private Currency[] values={MonedasUtils.PESOS
			,MonedasUtils.DOLARES
			,MonedasUtils.EUROS};
	
	private ValueModel valorHolder;
	private ValueModel monedaHolder;
	private ValueModel targetModel;
	
	public CantidadMonetariaPesosControl(final ValueModel model) {
		this.targetModel=model;
		model.addValueChangeListener(new ModelHandler());
		init();
	}
	
	private void init(){
		
		valorHolder=new ValueHolder(BigDecimal.ZERO);
		monedaHolder=new ValueHolder(MonedasUtils.PESOS);
		
		if(targetModel!=null){
			CantidadMonetaria val=(CantidadMonetaria)targetModel.getValue();
			if(val!=null){
				valorHolder.setValue(val.getAmount());
				monedaHolder.setValue(val.getCurrency());
			}
		}
		
		valorHolder.addValueChangeListener(this);		
		monedaHolder.addValueChangeListener(this);
		
		setLayout(new BorderLayout());
		SelectionInList list=new SelectionInList(Arrays.asList(values),monedaHolder);
		monedaBox=BasicComponentFactory.createComboBox(list);		
		
		
		NumberFormat format=NumberFormat.getNumberInstance(Locale.US);
		format.setGroupingUsed(true);
		format.setMaximumFractionDigits(2);
		format.setMinimumFractionDigits(2);
		format.setRoundingMode(RoundingMode.HALF_EVEN);			
		
		NumberFormatter formatter=new NumberFormatter(format);
		formatter.setValueClass(BigDecimal.class);
		formatter.setAllowsInvalid(false);
		
		inputField=BasicComponentFactory.createFormattedTextField(valorHolder, formatter);		
		inputField.setColumns(10);
		inputField.setHorizontalAlignment(SwingConstants.RIGHT);
				
		add(inputField,BorderLayout.CENTER);
		//add(monedaBox,BorderLayout.EAST);
	}
	
	public void propertyChange(PropertyChangeEvent evt) {
		if("value".equals(evt.getPropertyName())){
			updateModel();
		}		
	}
	
	/**
	 * Actualiza el target {@link ValueModel} para reflejar los
	 * cambios en la UI
	 *
	 */
	protected void updateModel(){
		final BigDecimal importe=(BigDecimal)valorHolder.getValue();
		final Currency moneda=(Currency)monedaHolder.getValue();
		CantidadMonetaria newVal=new CantidadMonetaria(importe.doubleValue(),moneda);		
		targetModel.setValue(newVal);
	}
	
	
	
	/**
	 * Detecta cambios en el target {@link ValueModel} y refleja lso cambios
	 * en la UI si estos no son originados por el mismo componente
	 * 
	 * @author Ruben Cancino
	 *
	 */
	protected class ModelHandler implements PropertyChangeListener{
		public void propertyChange(PropertyChangeEvent evt) {
			CantidadMonetaria newVal=(CantidadMonetaria)evt.getNewValue();
			
			BigDecimal amount=(BigDecimal)valorHolder.getValue();
			Currency moneda=(Currency)monedaHolder.getValue();
			CantidadMonetaria current=new CantidadMonetaria(amount.doubleValue(),moneda);
			if(!newVal.equals(current)){				
				valorHolder.setValue(newVal.getAmount());
				monedaHolder.setValue(newVal.getCurrency());
			}
		}
	}
	
	public void setEnabled(boolean enabled){
		monedaBox.setEnabled(enabled);
		inputField.setEnabled(enabled);
	}
	
	public void enableMonedaSelector(boolean enabled){
		monedaBox.setEnabled(enabled);
	}
	
	public JComboBox getMonedaBox() {
		return monedaBox;
	}
	
	

	public JFormattedTextField getInputField() {
		return inputField;
	}

	public String toString(){
		String pattern="{0} ({1})";
		return MessageFormat.format(pattern, valorHolder.getValue(),monedaHolder.getValue());
	}

	public static void main(String[] args) {
		final CantidadMonetaria val=CantidadMonetaria.pesos(465.50);
		final ValueModel model=new ValueHolder(val);
		final CantidadMonetariaPesosControl control=new CantidadMonetariaPesosControl(model);
		SXAbstractDialog dialog=new SXAbstractDialog("TEST"){

			@Override
			protected JComponent buildContent() {
				JPanel p=new JPanel(new BorderLayout());
				p.add(control,BorderLayout.CENTER);
				final JButton btn=new JButton("UPDATE MODEL");
				btn.addActionListener(new ActionListener(){
					public void actionPerformed(ActionEvent e) {
						model.setValue(CantidadMonetaria.pesos(4658.56));
					}					
				});
				p.add(btn,BorderLayout.SOUTH);
				return p;
			}
			
		};
		dialog.open();
		System.exit(0);
	}

	
	

}
