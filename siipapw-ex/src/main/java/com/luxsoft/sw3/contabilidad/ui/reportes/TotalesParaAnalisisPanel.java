package com.luxsoft.sw3.contabilidad.ui.reportes;

import java.text.NumberFormat;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.SwingConstants;

import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.event.ListEvent;
import ca.odell.glazedlists.event.ListEventListener;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;
import com.luxsoft.siipap.swing.controls.AbstractControl;
import com.luxsoft.sw3.contabilidad.model.PolizaDetRow;

public class TotalesParaAnalisisPanel extends AbstractControl implements ListEventListener{
	
	private JLabel inifialField=new JLabel();
	private JLabel cargosField=new JLabel();
	private JLabel abonosField=new JLabel();
	private JLabel finalField=new JLabel();
	private NumberFormat nf=NumberFormat.getNumberInstance();

	
	private double cargos=0;
	private double abonos=0;
	
	private final EventList source;
	
	public TotalesParaAnalisisPanel(EventList eventList){
		this.source=eventList;
	}

	@Override
	protected JComponent buildContent() {
		final FormLayout layout=new FormLayout("p,2dlu,f:p:g","");
		DefaultFormBuilder builder=new DefaultFormBuilder(layout);
		cargosField.setHorizontalAlignment(SwingConstants.RIGHT);
		abonosField.setHorizontalAlignment(SwingConstants.RIGHT);
		inifialField.setHorizontalAlignment(SwingConstants.RIGHT);
		finalField.setHorizontalAlignment(SwingConstants.RIGHT);
		builder.appendSeparator("Resumen ");
		//builder.append("Inicial",inifialField);
		builder.append("Debe",cargosField);
		builder.append("Haber",abonosField);
		//builder.append("Final ",finalField);
		
		builder.getPanel().setOpaque(false);
		source.addListEventListener(this);
		updateTotales();
		return builder.getPanel();
	}
	
	public void listChanged(ListEvent listChanges) {
		if(listChanges.next()){
		}
		updateTotales();
	}
	
	public void updateTotales(){
		
		for(Object obj:source){
			PolizaDetRow a=(PolizaDetRow)obj;
			cargos+=a.getDebe().doubleValue();
			abonos+=a.getHaber().doubleValue();			
		}
		//saldoFinal=BigDecimal.valueOf(saldoInicial.doubleValue()+cargos-abonos);
		//inifialField.setText(nf.format(saldoInicial.doubleValue()));
		cargosField.setText(nf.format(cargos));
		abonosField.setText(nf.format(abonos));
		//finalField.setText(nf.format(saldoFinal.doubleValue()));
		//total3.setText(nf.format(toneladasPorPedir));
	}

	public double getCargos() {
		return cargos;
	}

	public double getAbonos() {
		return abonos;
	}
	
}
