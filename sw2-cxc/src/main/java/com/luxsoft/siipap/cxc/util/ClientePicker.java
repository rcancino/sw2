/**
 * 
 */
package com.luxsoft.siipap.cxc.util;

import java.awt.BorderLayout;
import java.util.Date;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

import com.jgoodies.binding.PresentationModel;
import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;
import com.luxsoft.siipap.model.BaseBean;
import com.luxsoft.siipap.model.Periodo;
import com.luxsoft.siipap.model.core.Cliente;
import com.luxsoft.siipap.swing.binding.Binder;
import com.luxsoft.siipap.swing.controls.SXAbstractDialog;
import com.luxsoft.siipap.swing.utils.SWExtUIManager;

public class ClientePicker extends BaseBean{
	
	private Cliente cliente;
	
	private Date fechaInicial;
	
	private Date fechaFinal;
	
	public ClientePicker(){
		Periodo per=Periodo.getPeriodoDelMesActual();
		fechaInicial=per.getFechaInicial();
		fechaFinal=per.getFechaFinal();
	}
	

	public Cliente getCliente() {
		return cliente;
	}

	public void setCliente(Cliente cliente) {
		Object old=this.cliente;
		this.cliente = cliente;
		firePropertyChange("cliente", old, cliente);
	}


	public Date getFechaInicial() {
		return fechaInicial;
	}
	public void setFechaInicial(Date fechaInicial) {
		Object old=this.fechaInicial;
		this.fechaInicial = fechaInicial;
		firePropertyChange("fechaInicial", old, fechaInicial);
	}
	
	public Date getFechaFinal() {
		return fechaFinal;
	}
	public void setFechaFinal(Date fechaFinal) {
		Object old=this.fechaFinal;
		this.fechaFinal = fechaFinal;
		firePropertyChange("fechaFinal", old, fechaFinal);
	}

	
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = prime;
		result = prime * result + ((cliente == null) ? 0 : cliente.hashCode());
		return result;
	}

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this,ToStringStyle.MULTI_LINE_STYLE);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (getClass() != obj.getClass())
			return false;
		ClientePicker other = (ClientePicker) obj;
		if (cliente == null) {
			if (other.cliente != null)
				return false;
		} else if (!cliente.equals(other.cliente))
			return false;
		return true;
	}


	public static ClientePicker execute(){
		ClientePicker picker=new ClientePicker();
		final PickerForm form=new PickerForm(picker);
		form.open();
		if(form.hasBeenCanceled()){
			return picker;
		}
		return null;
	}
	
	
	
	public  static class PickerForm extends SXAbstractDialog{
		
		private Cliente cliente;
		private Date fechaInicial;
		private Date fechaFinal;
		
		private final PresentationModel model;
		
		private JComponent jCliente;
		private JComponent jFechaIni;
		private JComponent jFechaFin;
		
		

		public PickerForm(ClientePicker picker) {
			super("Estado de mivimientos");
			model=new PresentationModel(picker);
		}
		
		private void initComponents(){
			jCliente=Binder.createClientesBinding(buffer(model.getModel("cliente")));
			jFechaIni=Binder.createDateComponent(buffer(model.getModel("fechaInicial")));
			jFechaFin=Binder.createDateComponent(buffer(model.getModel("fechaFinal")));
		}

		@Override
		protected JComponent buildContent() {
			initComponents();
			JPanel panel=new JPanel(new BorderLayout());
			final FormLayout layout=new FormLayout(
					"l:40dlu,30dlu,60dlu, 3dlu, " +
					"l:40dlu,30dlu,p:g " +
					"");
			DefaultFormBuilder builder=new DefaultFormBuilder(layout);
			builder.append("Cliente",jCliente,5);
			builder.nextLine();
			builder.append("Fecha corte ",jFechaIni,true);
			builder.append("Fecha Final",jFechaFin,true);
			panel.add(builder.getPanel(),BorderLayout.CENTER);
			panel.add(buildButtonBarWithOKCancel(),BorderLayout.SOUTH);
			
			return panel;
		}

		public Cliente getCliente() {
			return cliente;
		}
		public void setCliente(Cliente cliente) {
			Object oldValue=this.cliente;
			this.cliente = cliente;
			firePropertyChange("cliente", oldValue, cliente);
		}

		public Date getFechaFinal() {
			return fechaFinal;
		}
		public void setFechaFinal(Date fechaFinal) {
			Object oldValue=this.fechaFinal;
			this.fechaFinal = fechaFinal;
			firePropertyChange("fechaFinal", oldValue, fechaFinal);
		}

		public Date getFechaInicial() {
			return fechaInicial;
		}
		public void setFechaInicial(Date fechaInicial) {
			Object oldValue=this.fechaInicial;
			this.fechaInicial = fechaInicial;
			firePropertyChange("fechaInicial", oldValue, fechaInicial);
		}		
	
	}
	
	public static void main(String[] args) {
		SwingUtilities.invokeLater(new Runnable(){
			public void run() {
				SWExtUIManager.setup();
				execute();
			}
			
		});
	}


	
}