package com.luxsoft.sw3.impap.ui.form;

import java.awt.BorderLayout;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.NumberFormat;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.ToStringBuilder;

import com.jgoodies.binding.PresentationModel;
import com.jgoodies.binding.adapter.BasicComponentFactory;
import com.jgoodies.binding.value.ValueModel;
import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;
import com.luxsoft.siipap.compras.model.EntradaPorCompra;
import com.luxsoft.siipap.cxp.model.CXPAnalisisDet;
import com.luxsoft.siipap.swing.binding.Binder;
import com.luxsoft.siipap.swing.binding.Bindings;
import com.luxsoft.siipap.swing.controls.SXAbstractDialog;
import com.luxsoft.siipap.swing.utils.MessageUtils;
import com.luxsoft.siipap.swing.utils.SWExtUIManager;
import com.luxsoft.siipap.util.DateUtil;

public class AnalisisDeEntradaDetForm extends SXAbstractDialog{

	private final CXPAnalisisDet analisis;
	private final PresentationModel model;
	
	private JTextField cantidad;
	private JTextField precio;
	private JLabel precioLista;
	private JTextField desc1;
	private JTextField desc2;
	private JTextField desc3;
	private JTextField desc4;
	private JTextField desc5;
	private JTextField desc6;
	private JTextField costo;
	private JTextField importe;
	
	
	public AnalisisDeEntradaDetForm(final CXPAnalisisDet analisis) {
		super("Analisis de entrada");
		this.analisis=analisis;
		analisis.setPrecioOriginal(BigDecimal.valueOf(analisis.getPrecio().doubleValue()));
		model=new PresentationModel(analisis);
		model.addBeanPropertyChangeListener(new Handler());
		initComponents();
		updateEntrada();
		
	}
	
	private ValueModel cantidadModel;
	
	private void initComponents(){
		cantidadModel=buffer(model.getModel("cantidadEnFactor"));
		cantidad=Bindings.createDoubleBinding(cantidadModel,3,3);
		precio=Bindings.createBigDecimalBinding(buffer(model.getModel("precio")),4,6);
		precioLista=BasicComponentFactory.createLabel(model.getModel("precioOriginal"), NumberFormat.getInstance());
		precioLista.setEnabled(false);
		desc1=Bindings.createDescuentoEstandarBinding(buffer(model.getModel("desc1")));
		desc2=Bindings.createDescuentoEstandarBinding(buffer(model.getModel("desc2")));
		desc3=Bindings.createDescuentoEstandarBinding(buffer(model.getModel("desc3")));
		desc4=Bindings.createDescuentoEstandarBinding(buffer(model.getModel("desc4")));
		desc5=Bindings.createDescuentoEstandarBinding(buffer(model.getModel("desc5")));
		desc6=Bindings.createDescuentoEstandarBinding(buffer(model.getModel("desc6")));
		costo=Binder.createBigDecimalForMonyBinding(model.getModel("costo"));
		costo.setEnabled(false);
		importe=Binder.createBigDecimalForMonyBinding(model.getModel("importe"));
		importe.setEnabled(false);
		
	}
	
	private JLabel compra=new JLabel("");
	private JLabel fechaCompra=new JLabel("");
	private JLabel remision=new JLabel("");
	private JLabel fecharRemision=new JLabel("");
	private JLabel entrada=new JLabel("");
	private JLabel fechaEntrada=new JLabel("");
	
	private JLabel producto=new JLabel("");
	private JLabel descripcion=new JLabel("");
	private JLabel unidad=new JLabel("");
	private JLabel sucursal=new JLabel("");
	private JLabel recibido=new JLabel("");
	private JLabel analizado=new JLabel("");
	private JLabel pendiente=new JLabel("");
	
	private NumberFormat nf=new DecimalFormat("#,##,###,####.###");
	
	private void updateEntrada(){
		EntradaPorCompra entrada=analisis.getEntrada();
		if(entrada!=null){
			compra.setText(String.valueOf(entrada.getCompra()));
			fechaCompra.setText(DateUtil.convertDateToString(entrada.getFechaCompra()));
			remision.setText(StringUtils.trim(entrada.getRemision()));
			fecharRemision.setText(DateUtil.convertDateToString(entrada.getFechaRemision()));
			if(entrada.getDocumento()!=null)
				this.entrada.setText(entrada.getDocumento().toString());
			
			fechaEntrada.setText(DateUtil.convertDateToString(entrada.getFecha()));
			producto.setText(entrada.getClave());
			descripcion.setText(entrada.getDescripcion());
			if(entrada.getUnidad()!=null)
				unidad.setText(entrada.getUnidad().getNombre());
			if(entrada.getSucursal()!=null)
				sucursal.setText(entrada.getSucursal().getNombre());
			recibido.setText(nf.format(entrada.getCantidad()));
			analizado.setText(nf.format(entrada.getAnalizado()));
			String pendienteDeAnalisis=nf.format(entrada.getPendienteDeAnalisis());
			/*if(analisis.getId()!=null){
				//Ya se ha persistido
				pendienteDeAnalisis+=analisis.getCantidad();
			}*/
			pendiente.setText(pendienteDeAnalisis);
			
				
		}
	}
	
	public JComponent buildContent() {
        JPanel content = new JPanel(new BorderLayout());
        content.add(buildMainPanel(),             BorderLayout.CENTER);
        content.add(buildButtonBarWithOKCancel(), BorderLayout.SOUTH);
        return content;
    }	
	
	protected JComponent buildMainPanel() {
		FormLayout layout=new FormLayout(
				"50dlu,2dlu,90dlu, 3dlu," +
				"50dlu,2dlu,90dlu","");
		DefaultFormBuilder builder=new DefaultFormBuilder(layout);
		
		builder.appendSeparator("Entrada");
		builder.append("Compra",compra);
		builder.append("F.Compra",fechaCompra);
		builder.append("Remisión",remision);
		builder.append("F.Remisión",fecharRemision);
		builder.append("COM",entrada);
		builder.append("F.COM",fechaEntrada);
		
		builder.append("Producto",producto,true);
		builder.append("Descripción",descripcion,5);
		builder.append("Unidad",unidad);
		builder.append("Sucursal",sucursal);
		builder.append("Recibido",recibido);
		builder.append("Analizado",analizado);
		builder.append("Pendiente",pendiente);
		builder.nextLine();
		builder.appendSeparator("Precio / Descuentos");
		builder.append("Cantidad",cantidad,true);
		builder.append("Precio",precio);
		builder.append("Precio L.",precioLista);
		
		
		builder.append("Desc 1",desc1);
		builder.append("Desc 2",desc2);
		builder.append("Desc 3",desc3);
		builder.append("Desc 4",desc4);
		builder.append("Desc 5",desc5);
		builder.append("Desc 6",desc6);
		
		builder.append("Costo U",costo,5);
		builder.append("Importe",importe,5);
		
		return builder.getPanel();
	}
	
	public void commit(){
		
	}
	
	
	
	@Override
	public void doApply() {
		Double ct=(Double)cantidadModel.getValue();
		if(ct==null)
			ct=0d;
		BigDecimal cantidad=BigDecimal.valueOf(ct);
		
		BigDecimal pendiente=BigDecimal.valueOf(analisis.getEntrada().getPendienteDeAnalisis()).setScale(3,RoundingMode.HALF_EVEN);
		if(analisis.getId()!=null){
			//Ya se ha persistido
			pendiente=pendiente.add(BigDecimal.valueOf(analisis.getCantidad()));
		}
		cantidad=cantidad.multiply(BigDecimal.valueOf(analisis.getEntrada().getFactor())).setScale(3,RoundingMode.HALF_EVEN);
		pendiente=pendiente.setScale(3,RoundingMode.HALF_EVEN);
		if(cantidad.doubleValue()<=pendiente.doubleValue()){
				super.doApply();
				return;
		}
		else{
			MessageUtils.showMessage("La cantidad analizada no es correcta", "Analisis");
			doCancel();
		}
	}



	private class Handler implements PropertyChangeListener{
		public void propertyChange(PropertyChangeEvent evt) {
			if("precio".equals(evt.getPropertyName()))
				analisis.calcularImporte();
			else if("cantidad".equals(evt.getPropertyName()))
				analisis.calcularImporte();
			else if(evt.getPropertyName().startsWith("desc"))
				analisis.calcularImporte();
		}
	}

	public static void main(String[] args) {
		SwingUtilities.invokeLater(new Runnable(){
			public void run() {
				SWExtUIManager.setup();
				EntradaPorCompra e=new EntradaPorCompra();
				e.setFactor(1000d);
				System.out.println(e);
				CXPAnalisisDet det=new CXPAnalisisDet();
				det.setPrecio(BigDecimal.valueOf(1600));
				det.setEntrada(e);
				AnalisisDeEntradaDetForm form=new AnalisisDeEntradaDetForm(det);
				form.open();
				if(!form.hasBeenCanceled()){
					System.out.println(ToStringBuilder.reflectionToString(det));
				}
			}			
		});
	}
}
