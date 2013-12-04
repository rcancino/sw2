package com.luxsoft.siipap.pos.ui.selectores;

import java.awt.BorderLayout;
import java.text.NumberFormat;
import java.util.Date;

import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFormattedTextField;
import javax.swing.JPanel;
import javax.swing.text.NumberFormatter;

import org.jdesktop.swingx.JXDatePicker;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;
import com.luxsoft.siipap.cxc.model.OrigenDeOperacion;
import com.luxsoft.siipap.model.Sucursal;
import com.luxsoft.siipap.swing.controls.SXAbstractDialog;
import com.luxsoft.siipap.ventas.model.Venta;
import com.luxsoft.sw3.services.Services;

public class BuscadorSelectivoVentas extends SXAbstractDialog{
	
	
	
	public JComboBox sucursalBox;
	public JComboBox origenBox;
	public JXDatePicker dateField;
	public JFormattedTextField documentoField;
	
	private boolean porFecha=true;

	public BuscadorSelectivoVentas() {
		super("Buscar documento");
	}
	
	private void init(){
		sucursalBox=new JComboBox(getSucursales());
		sucursalBox.setEnabled(isSucursalHabilitada());
		origenBox=new JComboBox(getOrigenes());
		dateField=new JXDatePicker();
		dateField.setFormats("dd/MM/yyyy");
		NumberFormat format=NumberFormat.getNumberInstance();
		format.setGroupingUsed(false);
		NumberFormatter formatter=new NumberFormatter(format);
		formatter.setValueClass(Long.class);
		documentoField=new JFormattedTextField(formatter);
	}

	

	@Override
	protected JComponent buildContent() {
		init();
		
		JPanel panel=new JPanel(new BorderLayout());
		
		FormLayout layout=new FormLayout("p,3dlu,p:g","");
		DefaultFormBuilder builder=new DefaultFormBuilder(layout);
		builder.append("Sucursal",sucursalBox);
		builder.append("Origen",origenBox);
		builder.append("Folio",documentoField);
		if(porFecha)
			builder.append("Fecha",dateField);
		
		panel.add(BorderLayout.CENTER,builder.getPanel());
		panel.add(BorderLayout.SOUTH,buildButtonBarWithOKCancel());
		
		return panel;
	}

	public OrigenDeOperacion getOrigen() {
		return (OrigenDeOperacion)origenBox.getSelectedItem();
	}

	

	public boolean isPorFecha() {
		return porFecha;
	}

	public void setPorFecha(boolean porFecha) {
		this.porFecha = porFecha;
	}

	public Long getDocumento() {
		return (Long)documentoField.getValue();
	}

	public Sucursal getSucursal() {
		return (Sucursal)sucursalBox.getSelectedItem();
	}
	

	public Date getFecha() {
		return dateField.getDate();
	}
	
	private OrigenDeOperacion origenes[];

	public void setOrigenes(OrigenDeOperacion...com){
		origenes=com;
	}
	
	public OrigenDeOperacion[] getOrigenes() {
		if(origenes==null){
			origenes=new OrigenDeOperacion[]{OrigenDeOperacion.CRE,OrigenDeOperacion.CAM,OrigenDeOperacion.MOS};
		}
		return origenes;
	}
	
	private Sucursal[] sucursales;
	
	

	public Sucursal[] getSucursales() {
		if(sucursales==null){
			sucursales=Services.getInstance().getSucursalesOperativas().toArray(new Sucursal[0]);
		}
		return sucursales;
	}

	public void setSucursales(Sucursal... sucursales) {
		this.sucursales = sucursales;
	}
	
	private boolean sucursalHabilitada=false;
	
	
	
	
	public boolean isSucursalHabilitada() {
		return sucursalHabilitada;
	}

	public void setSucursalHabilitada(boolean sucursalHabilitada) {
		this.sucursalHabilitada = sucursalHabilitada;
	}

	public static Venta buscarFacturaDeContado(){
		BuscadorSelectivoVentas form=new BuscadorSelectivoVentas();
		form.setOrigenes(OrigenDeOperacion.MOS,OrigenDeOperacion.CAM);
		form.setSucursales(Services.getInstance().getConfiguracion().getSucursal());
		form.setPorFecha(false);
		form.open();
		if(!form.hasBeenCanceled()){
			Sucursal suc=form.getSucursal();
			Long docto=form.getDocumento();
			OrigenDeOperacion origen=form.getOrigen();
			Venta v=Services.getInstance().getFacturasManager().getVentaDao().buscarVenta(suc.getId(), docto, origen);
			return v;			
		}
		return null;
	}
	
	public static Venta buscarFacturaDeCredito(){
		BuscadorSelectivoVentas form=new BuscadorSelectivoVentas();
		form.setOrigenes(OrigenDeOperacion.CRE);
		form.setSucursales(Services.getInstance().getConfiguracion().getSucursal());
		form.setPorFecha(false);
		form.open();
		if(!form.hasBeenCanceled()){
			Sucursal suc=form.getSucursal();
			Long docto=form.getDocumento();
			OrigenDeOperacion origen=form.getOrigen();
			Venta v=Services.getInstance().getFacturasManager().getVentaDao().buscarVenta(suc.getId(), docto, origen);
			return v;			
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
				BuscadorSelectivoVentas b=new BuscadorSelectivoVentas();
				b.open();
				if(b.hasBeenCanceled()){
					
				}
				System.exit(0);
			}

		});
	}

}
