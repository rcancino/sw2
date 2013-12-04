package com.luxsoft.siipap.ventas;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import org.springframework.util.Assert;

import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.event.ListEvent;
import ca.odell.glazedlists.event.ListEventListener;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;
import com.luxsoft.siipap.model.CantidadMonetaria;
import com.luxsoft.siipap.swing.controls.ViewControl;
import com.luxsoft.siipap.ventas.model.VentaRow;

/**
 * La responsabilidad de esta clase es implementar un panel con los totales
 * de un grupo de ventas. El grupo de ventas esta sostenido en un {@link EventList}
 * que es pasado en su constructor
 * 
 * @author Ruben Cancino
 *
 */
public class TotalesPanel implements ListEventListener<VentaRow>,ViewControl{
	
	private final EventList<VentaRow> source;
	
	private JLabel totalVenta=new JLabel();
	private JPanel totalPanel;
	
	
	public TotalesPanel(final EventList<VentaRow> ventas){
		Assert.notNull(ventas,"La fuente de datos no puede ser nula");
		source=ventas;
		source.addListEventListener(this);
	}
	
	public JComponent getControl(){
		return getTotalesPanel();
	}
	
	@SuppressWarnings("unchecked")
	private JPanel getTotalesPanel(){
		if(totalPanel==null){
			final FormLayout layout=new FormLayout("p,2dlu,f:max(100dlu;p):g","");
			DefaultFormBuilder builder=new DefaultFormBuilder(layout);
			totalVenta.setHorizontalAlignment(SwingConstants.RIGHT);
			builder.appendSeparator("Totales");
			builder.append("Venta General",totalVenta);
			totalPanel=builder.getPanel();
			totalPanel.setOpaque(false);
		}
		return totalPanel;
	}
	
	protected void updateTotales(){
		CantidadMonetaria tot=CantidadMonetaria.pesos(0);
		
		for(Object  r:source){
			VentaRow c=(VentaRow)r;
			//tot=tot.add(c.getTotalMN());
		}
		totalVenta.setText(tot.toString());
	}

	public void listChanged(ListEvent<VentaRow> listChanges) {
		if(listChanges.next()){
			updateTotales();
		}		
	}

}
