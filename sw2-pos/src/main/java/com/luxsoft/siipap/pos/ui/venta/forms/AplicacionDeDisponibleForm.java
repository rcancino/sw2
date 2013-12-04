package com.luxsoft.siipap.pos.ui.venta.forms;

import java.math.BigDecimal;
import java.text.MessageFormat;

import javax.swing.JComponent;

import org.hibernate.validator.AssertFalse;
import org.hibernate.validator.AssertTrue;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;
import com.luxsoft.luxor.utils.Bean;
import com.luxsoft.siipap.swing.controls.Header;
import com.luxsoft.siipap.swing.form2.AbstractForm;
import com.luxsoft.siipap.swing.form2.DefaultFormModel;
import com.luxsoft.siipap.ventas.model.Venta;
import com.luxsoft.sw3.services.POSDBUtils;
import com.luxsoft.sw3.services.Services;

public class AplicacionDeDisponibleForm extends AbstractForm{
	
	

	public AplicacionDeDisponibleForm(Venta anticipo) {
		this(anticipo,BigDecimal.ZERO);
	}
	
	public AplicacionDeDisponibleForm(Venta anticipo,BigDecimal importe) {
		super(new DefaultFormModel(Bean.proxy(AnticipoModel.class)));
		getModel().setValue("anticipo", anticipo);		
		getModel().setValue("importe", importe);
	}

	@Override
	protected JComponent buildFormPanel() {
		FormLayout layout=new FormLayout(
				"p,3dlu,120dlu"
				,""
				);
		DefaultFormBuilder builder=new DefaultFormBuilder(layout);
		builder.append("Importe por aplicar",getControl("importe"));
		return builder.getPanel();
	}

	@Override
	protected JComponent buildHeader() {
		Venta a=(Venta)getModel().getValue("anticipo");
		//Header header=new Header("","");
		
		Header header=new Header(
				MessageFormat.format("Anticipo de: {0}", a.getCliente().getNombreRazon())
				,MessageFormat.format("Sucursal:{0} " +
						"Fecha: {1,date,short}  " +
						"Docto: {2}  " +
						"\nTotal Ant:{3} " +
						"\nDisponible:{4}"
						,a.getSucursal(),a.getFecha(),a.getDocumento()
						,a.getTotal()
						,a.getDisponibleDeAnticipo())
				);
		return header.getHeader();
	}
	
	public static class AnticipoModel {
		private Venta anticipo;
		private BigDecimal importe=BigDecimal.ZERO;
		public Venta getAnticipo() {
			return anticipo;
		}
		public void setAnticipo(Venta anticipo) {
			this.anticipo = anticipo;
		}
		public BigDecimal getImporte() {
			return importe;
		}
		public void setImporte(BigDecimal importe) {
			this.importe = importe;
		}
		
		@AssertTrue(message="El importe es mayor que el disponible del anticipo")
		public boolean validarImporte(){
			if(getAnticipo()!=null)
				return getAnticipo().getDisponibleDeAnticipo().doubleValue()>=getImporte().doubleValue();
			return true;
		}		
	}
	
	public static BigDecimal getImporteAplicado(Venta venta){
		return getImporteAplicado(venta,BigDecimal.ZERO);
	}
	
	public static BigDecimal getImporteAplicado(Venta venta,BigDecimal aplicado){
		AplicacionDeDisponibleForm form=new AplicacionDeDisponibleForm(venta,aplicado);
		form.open();
		if(!form.hasBeenCanceled()){
			return (BigDecimal)form.getModel().getValue("importe");
		}
		return BigDecimal.ZERO;
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
				POSDBUtils.whereWeAre();
				Venta a=Services.getInstance().getFacturasManager().buscarVentaInicializada("8a8a81c7-2da4e003-012d-a4e323e9-0003");
				BigDecimal res=getImporteAplicado(a);
				System.out.println(res);
				System.exit(0);
			}

		});
	}

}
