package com.luxsoft.sw3.maquila.ui.forms;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.math.BigDecimal;
import java.text.MessageFormat;
import java.text.NumberFormat;

import javax.swing.JComponent;
import javax.swing.JFormattedTextField;
import javax.swing.JTextField;
import javax.swing.text.NumberFormatter;

import com.jgoodies.binding.adapter.BasicComponentFactory;
import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;
import com.luxsoft.siipap.model.core.Producto;
import com.luxsoft.siipap.service.ServiceLocator2;
import com.luxsoft.siipap.swing.binding.Binder;
import com.luxsoft.siipap.swing.binding.Bindings;
import com.luxsoft.siipap.swing.controls.Header;
import com.luxsoft.siipap.swing.form2.AbstractForm;
import com.luxsoft.siipap.swing.form2.DefaultFormModel;
import com.luxsoft.siipap.swing.form2.IFormModel;
import com.luxsoft.sw3.maquila.model.OrdenDeCorteDet;
import com.luxsoft.sw3.maquila.model.RecepcionDeCorteDet;






/**
 * Forma para el mantenimiento unitario de recepciones de corte
 * 
 * @author Ruben Cancino
 *
 */
public class RecepcionDeCorteDetForm extends AbstractForm{
	
 	
	public RecepcionDeCorteDetForm(IFormModel model) {
		super(model);
		setTitle("Recepción de corte unitaria");
		model.addBeanPropertyChangeListener(new Handler());
	}	

	@Override
	protected JComponent buildFormPanel() {
		FormLayout layout=new FormLayout(
				" p,2dlu,90dlu:g(.5),2dlu" +
				",p,2dlu,90dlu:g(.5)" 
			,	"");
		final DefaultFormBuilder builder=new DefaultFormBuilder(layout);
		
		builder.appendSeparator("Hojeo");
		builder.append("Entrada",getControl("entrada"));
		builder.append("Teórico",addReadOnly("hojeoTeorico"));		
		builder.nextLine();
		
		builder.append("Kilos",getControl("kilos"));
		builder.append("M2",getControl("metros2"));
		builder.nextLine();
		builder.append("M2 por Millar",getControl("metros2PorMillar"));
		builder.nextLine();
		
		builder.appendSeparator("Merma");
		builder.append("M2",addReadOnly("merma"));
		builder.append("   %",addReadOnly("mermaPor"));		
		actualizarCalculos();
		builder.nextLine();		
		
		
		
		builder.append("Comentario",getControl("comentario"),5);
		
		return builder.getPanel();
	}	

	private Header header;	
	
	@Override
	protected JComponent buildHeader() {
		if(header==null){
			header=new Header("","");
			updateHeader();
		}
		return header.getHeader();
	}
	
	public void updateHeader() {
		if(header!=null){
			OrdenDeCorteDet corte=getRecepcionDet().getCorte();
			if(corte!=null){
				Producto p=corte.getDestino();
				header.setTitulo(MessageFormat.format("{0} ({1})"						
						,p.getDescripcion()
						,p.getClave())
						);
				String pattern=
						  "Instrucción de corte: Kilos: {0}     M2: {1}"; 
				String desc=MessageFormat.format(pattern
						,corte.getKilos()
						,corte.getMetros2()
						);
				header.setDescripcion(desc);
			}
			else{
				header.setTitulo("El corte unitario es mandatorio");
				header.setDescripcion("");
			}
		}
	}
	
	private JFormattedTextField kilosField;
	private JFormattedTextField metrosField;
	
	private JTextField hojeoTeorico=new JTextField();
	private JTextField merma=new JTextField();
	private JTextField mermaPorcentual=new JTextField();
	
	
	@Override
	protected JComponent createCustomComponent(String property) {
		if("kilos".equals(property)){
			NumberFormat format=NumberFormat.getInstance();
			format.setGroupingUsed(true);			
			format.setMaximumFractionDigits(4);
			format.setMinimumFractionDigits(2);
			format.setParseIntegerOnly(false);
			NumberFormatter formatter=new NumberFormatter(format);
			formatter.setAllowsInvalid(true);			
			formatter.setCommitsOnValidEdit(false);
			formatter.setValueClass(BigDecimal.class);			
			kilosField=new JFormattedTextField(formatter);
			kilosField.setEnabled(!model.isReadOnly());
			kilosField.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					BigDecimal val=(BigDecimal)kilosField.getValue();
					if(val==null)
						val=BigDecimal.ZERO;
					if(getRecepcionDet()!=null){
						getRecepcionDet().setKilos(val);
						getRecepcionDet().recalcularMetros();
						metrosField.setValue(getRecepcionDet().getMetros2());
						getControl("comentario").requestFocusInWindow();
					}
				}
			});
			kilosField.setValue(getRecepcionDet().getKilos());
			return kilosField;
		}else if("metros2".equals(property)){
			NumberFormat format=NumberFormat.getInstance();
			format.setGroupingUsed(true);			
			format.setMaximumFractionDigits(4);
			format.setMinimumFractionDigits(2);
			format.setParseIntegerOnly(false);
			NumberFormatter formatter=new NumberFormatter(format);
			formatter.setAllowsInvalid(true);			
			formatter.setCommitsOnValidEdit(false);
			formatter.setValueClass(BigDecimal.class);			
			metrosField=new JFormattedTextField(formatter);
			metrosField.setEnabled(!model.isReadOnly());
			metrosField.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					BigDecimal val=(BigDecimal)metrosField.getValue();
					if(val==null)
						val=BigDecimal.ZERO;
					if(getRecepcionDet()!=null){
						getRecepcionDet().setMetros2(val);
						getRecepcionDet().recalcularKilos();
						kilosField.setValue(getRecepcionDet().getKilos());
						getControl("comentario").requestFocusInWindow();
					}
				}
			});
			metrosField.setValue(getRecepcionDet().getMetros2());
			return metrosField;
		}else if("metros2PorMillar".equals(property)){
			return Bindings.createDoubleBinding(model.getModel(property), 5, 3);
		}else if("entrada".equals(property) ){	
			NumberFormat format=NumberFormat.getInstance();
			format.setMaximumFractionDigits(0);
			format.setMinimumFractionDigits(0);
			NumberFormatter formatter=new NumberFormatter(format);			
			JFormattedTextField tf=BasicComponentFactory.createFormattedTextField(model.getModel(property), formatter);
			//return Binder.createNumberBinding(model.getModel(property), 0);
			return tf;
		}else if("hojeoTeorico".equals(property)){
			return hojeoTeorico;
		}else if("merma".equals(property)){
			return merma;
		}else if("mermaPor".equals(property)){
			return mermaPorcentual;
		}
		return super.createCustomComponent(property);
	}

	private RecepcionDeCorteDet getRecepcionDet(){
		RecepcionDeCorteDet det=(RecepcionDeCorteDet)model.getBaseBean();
		return det;
	}
	
	private NumberFormat hojasFormat;
	private NumberFormat mermaFormat;
	
	public void actualizarCalculos(){
		if(hojasFormat==null){
			hojasFormat=NumberFormat.getIntegerInstance();
		}
		if(mermaFormat==null){
			mermaFormat=NumberFormat.getNumberInstance();
			mermaFormat.setMaximumFractionDigits(3);
			mermaFormat.setMinimumFractionDigits(2);
		}
		double teorico=getRecepcionDet().getHojeoTeorico();
		this.hojeoTeorico.setText(hojasFormat.format(teorico));
		this.merma.setText(mermaFormat.format(getRecepcionDet().getMerma()));
		this.mermaPorcentual.setText(mermaFormat.format(getRecepcionDet().getMermaPor()));
	}
	
	private class Handler implements PropertyChangeListener{
		public void propertyChange(PropertyChangeEvent evt) {
			actualizarCalculos();
		}
	}

	
	public static void main(String[] args) {
		javax.swing.SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				com.luxsoft.siipap.swing.utils.SWExtUIManager.setup();
				OrdenDeCorteDet corte=(OrdenDeCorteDet)ServiceLocator2.getHibernateTemplate().get(OrdenDeCorteDet.class, new Long(786));
				RecepcionDeCorteDet rec=new RecepcionDeCorteDet(corte);
				rec.setCorte(corte);
				DefaultFormModel model=new DefaultFormModel(rec);
				RecepcionDeCorteDetForm form=new RecepcionDeCorteDetForm(model);
				form.open();
				System.exit(0);
			}

		});
	}

}
