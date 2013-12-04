package com.luxsoft.siipap.cxc.ui.command;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JPanel;

import com.jgoodies.binding.adapter.BasicComponentFactory;
import com.jgoodies.binding.value.ValueHolder;
import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;
import com.luxsoft.siipap.cxc.model.NotaDeCredito;
import com.luxsoft.siipap.cxc.model.OrigenDeOperacion;
import com.luxsoft.siipap.cxc.ui.selectores.SelectorDeNotasDeCredito;
import com.luxsoft.siipap.service.ServiceLocator2;
import com.luxsoft.siipap.swing.controls.SXAbstractDialog;
import com.luxsoft.sw3.cxc.forms.NotaGenericaForm;

public class BuscadorDeNotasDeCredito extends AbstractAction{

	public void actionPerformed(ActionEvent e) {
		final NotaForm form=new NotaForm();
		form.open();
		if(!form.hasBeenCanceled()){
			final Integer numero=form.getNumero();
			if(numero!=0l){
				final OrigenDeOperacion origen=form.getOrigen();
				SelectorDeNotasDeCredito selector=new SelectorDeNotasDeCredito(){
					@Override
					protected List<NotaDeCredito> getData() {
						String hql="from NotaDeCredito n left join fetch n.aplicaciones a" +
						" where n.folio=? " +
						"  and n.origen="+"\'"+origen.name()+"\'" ;
						List<NotaDeCredito > notas=ServiceLocator2
						.getHibernateTemplate().find(hql,numero);
						return notas;
					}
					
				};
				selector.open();
				NotaDeCredito selected=selector.getSelected();
				if(selected!=null){
					NotaGenericaForm.show(selected);
				}
			}	
		}
	}
	
	public static class NotaForm extends SXAbstractDialog{
		
		private ValueHolder numeroHolder=new ValueHolder(null);
		private JComboBox origenBox;

		public NotaForm() {
			super("Nota de crédito");
		}

		@Override
		protected JComponent buildContent() {
			JPanel content=new JPanel(new BorderLayout());
			content.add(buildForm(),BorderLayout.CENTER);
			content.add(buildButtonBarWithOKCancel(),BorderLayout.SOUTH);
			return content;
		}
		
		private JComponent buildForm(){			
			FormLayout layout=new  FormLayout("p,3dlu,70dlu");			
			DefaultFormBuilder builder=new DefaultFormBuilder(layout);
			
			builder.append("Número",getControl("numero"));
			
			Object[] origenes=new Object[]{OrigenDeOperacion.CRE,OrigenDeOperacion.CAM,OrigenDeOperacion.MOS,OrigenDeOperacion.JUR,OrigenDeOperacion.CHE};
			origenBox=new JComboBox(origenes);
			builder.append("Número",origenBox);
			
			return builder.getPanel();
		}
		
		private JComponent getControl(String property){
			if("numero".equals(property)){
				return BasicComponentFactory.createIntegerField(buffer(numeroHolder),0);
			}else
				return null;
		}
		
		public Integer getNumero(){
			Integer res=(Integer)numeroHolder.getValue();
			return res==null?0:res;
		}
		
		public OrigenDeOperacion getOrigen(){
			return (OrigenDeOperacion)origenBox.getSelectedItem();
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
				new BuscadorDeNotasDeCredito().actionPerformed(null);
				System.exit(0);
			}

		});
	}
}
