package com.luxsoft.siipap.swing.binding;

import java.awt.event.ActionListener;
import java.beans.EventHandler;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;

import com.jgoodies.binding.value.ComponentValueModel;
import com.jgoodies.binding.value.ValueHolder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;
import com.luxsoft.siipap.model.Direccion;

public class DireccionControl extends JPanel{
	
	private JTextArea area;
	private JButton cambiarBtn;
	private ComponentValueModel model;
	
	public DireccionControl(ComponentValueModel model){
		super();
		this.model=model;
		init();
		if(model.getValue()!=null){
			area.setText(model.getValue().toString());
		}
	}
	
	public void setEnabled(boolean enabled){
		super.setEnabled(enabled);
		cambiarBtn.setEnabled(enabled);
	}
	
	private void init(){
		area=new JTextArea(4,20);
		JScrollPane sp=new JScrollPane(area);
		area.setEnabled(false);
		cambiarBtn=new JButton("Modificar");
		cambiarBtn.addActionListener(EventHandler.create(ActionListener.class, this, "showForm"));
		cambiarBtn.setHorizontalAlignment(SwingConstants.LEFT);
		sp.setColumnHeaderView(cambiarBtn);
		final FormLayout layout=new FormLayout(
				"f:p:g"
				,"c:p:g");
		setLayout(layout);
		final CellConstraints cc=new CellConstraints();
		add(sp,cc.xy(1,1));
	}
	
	

	public void showForm(){	
		Direccion source=(Direccion)model.getValue();
		if(source!=null){
			Direccion res=DireccionPanel.showForm(source);
			if(res!=null){			
				area.setText(res.toString());
				model.setValue(res);
			}
		}else{
			Direccion res=DireccionPanel.showForm();
			if(res!=null){			
				area.setText(res.toString());
				model.setValue(res);
			}
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
				DireccionControl control=new DireccionControl(new ComponentValueModel(new ValueHolder(new Direccion())));
				control.showForm();
				System.exit(0);
			}

		});
	}
	
	
	

}
