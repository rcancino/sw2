package com.luxsoft.sw3.cxc.forms;

import java.awt.BorderLayout;
import java.text.NumberFormat;
import java.util.List;

import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFormattedTextField;
import javax.swing.JPanel;
import javax.swing.text.NumberFormatter;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;
import com.luxsoft.siipap.model.Sucursal;
import com.luxsoft.siipap.service.ServiceLocator2;
import com.luxsoft.siipap.swing.controls.SXAbstractDialog;
import com.luxsoft.siipap.ventas.model.Devolucion;

public class LocalizadorDeRMDForm  extends SXAbstractDialog{
	
	private JComboBox sucursalBox;
	private JFormattedTextField documentoField;

	public LocalizadorDeRMDForm() {
		super("Buscar RMD");
	}
	
	private void initComponents(){
		List data=ServiceLocator2.getLookupManager().getSucursalesOperativas();
		sucursalBox=new JComboBox(data.toArray(new Object[data.size()]));
		NumberFormat format=NumberFormat.getIntegerInstance();
		NumberFormatter formatter=new NumberFormatter(format);
		formatter.setValueClass(Long.class);
		documentoField=new JFormattedTextField(formatter);
	}

	@Override
	protected JComponent buildContent() {
		initComponents();
		JPanel content=new JPanel(new BorderLayout());
		FormLayout layout=new FormLayout("p,2dlu,p:g","");
		DefaultFormBuilder builder=new DefaultFormBuilder(layout);
		builder.append("Sucursal",sucursalBox);
		builder.append("Documento",documentoField);
		content.add(builder.getPanel(),BorderLayout.CENTER);
		content.add(buildButtonBarWithOKCancel(),BorderLayout.SOUTH);
		return content;
	}
	
	public Sucursal getSucursal(){
		return (Sucursal)sucursalBox.getSelectedItem();
	}
	public Long getDocumento(){
		return (Long)documentoField.getValue();
	}
	
	public static Devolucion buscar(){
		LocalizadorDeRMDForm form=new LocalizadorDeRMDForm();
		form.open();
		if(!form.hasBeenCanceled()){
			Long numero=form.getDocumento();
			Long id=form.getSucursal().getId();
			String hql="from Devolucion d where d.venta.sucursal.id=? and d.numero=?";
			List<Devolucion> res=ServiceLocator2.getHibernateTemplate()
				.find(hql,new Object[]{id,numero});
			return res.isEmpty()?null:res.get(0);
		}
		return null;
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
				System.out.println(buscar());
				System.exit(0);
			}

		});
	}

}
