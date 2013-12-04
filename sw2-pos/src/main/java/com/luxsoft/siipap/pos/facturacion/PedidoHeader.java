package com.luxsoft.siipap.pos.facturacion;

import java.awt.BorderLayout;
import java.math.BigDecimal;
import java.text.Format;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;

import org.apache.commons.lang.exception.ExceptionUtils;

import com.jgoodies.binding.PresentationModel;
import com.jgoodies.binding.adapter.Bindings;
import com.jgoodies.binding.value.ConverterFactory;
import com.jgoodies.binding.value.ValueModel;
import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.Sizes;
import com.jgoodies.uifextras.panel.GradientBackgroundPanel;
import com.jgoodies.uifextras.panel.HeaderPanel;
import com.jgoodies.uifextras.util.ActionLabel;
import com.luxsoft.siipap.pos.ui.utils.UIUtils;
import com.luxsoft.siipap.swing.controls.AbstractControl;
import com.luxsoft.siipap.swing.controls.SXAbstractDialog;
import com.luxsoft.siipap.swing.utils.ComponentUtils;
import com.luxsoft.sw3.ventas.Pedido;
import com.luxsoft.swing.common.DropDownComponent;

/**
 * Panel para mostrar los datos principales del pedido en un 
 * 
 * @author Ruben Cancino Ramos
 *
 */
public class PedidoHeader extends AbstractControl{
	
	private final PresentationModel model;
	
	private Map<String,JComponent > components;	
	
	public PedidoHeader(final Object pedido){
		this(new PresentationModel(pedido));
	}

	public PedidoHeader(PresentationModel model) {
		this.model = model;
		components=new HashMap<String, JComponent>();
	}
	
	private DropDownComponent dropComponent;

	@Override
	protected JComponent buildContent() {
		
		JPanel panel=new GradientBackgroundPanel(true);
		
		final FormLayout layout=new FormLayout(
				"max(p;50dlu),2dlu,max(p;90dlu):g(.5), 2dlu," +
				"max(p;50dlu),2dlu,max(p;90dlu):g(.5)"
				,"");
		
		final DefaultFormBuilder builder=new DefaultFormBuilder(layout,panel);
		builder.setLineGapSize(Sizes.DLUX4);
		updateTitleLabel(builder.append("Cliente",getControl("nombre"),5));
		builder.nextLine();
		ActionLabel pedidoLabel=new ActionLabel("Pedido [F5]");
		//pedidoLabel.addActionListener(EventHandler.create(ActionListener.class, this, "mostrarPedido"));
		updateTitleLabel(pedidoLabel);
		dropComponent=new DropDownComponent(
				pedidoLabel,new HeaderPanel("Datos Generales","Detalle del pedido"),true);
		pedidoLabel.addActionListener(dropComponent);
		builder.append(dropComponent, getControl("folio"));
		
		updateTitleLabel(builder.append("Fecha",getControl("fecha",new SimpleDateFormat("dd/MM/yyyy"))));
		builder.nextLine();
		updateTitleLabel(builder.append("Tipo",getControl("origen")));
		updateTitleLabel(builder.append("F.P.",getControl("formaDePago")));
		builder.nextLine();
		//builder.appendSeparator();		
		updateTitleLabel(builder.append("Cargos",getControl("comisionTarjetaImporte",moneyFormat)));
		//updateTitleLabel(builder.append("Flete",getControl("flete",moneyFormat)));
		updateTitleLabel(builder.append("Total",getControl("total",moneyFormat)));
		
		//builder.nextColumn(6);
		/*builder.append("");
		builder.nextColumn(2);
		updateTitleLabel(builder.append("Total",getControl("total",moneyFormat)));
		*/
		builder.getPanel().setOpaque(false);
		builder.setDefaultDialogBorder();
		
		JPanel content=new JPanel(new BorderLayout());
		content.add(builder.getPanel(),BorderLayout.CENTER);
		content.add(new JSeparator(),BorderLayout.SOUTH);
		return content;
	}
	
	public void mostrarPedido(){
		dropComponent.showPopupWindow();
	}
	
	private void updateTitleLabel(JLabel l){
		ComponentUtils.toTitleLabel(l, 5f);
	}
	private JLabel createLabel(){
		return  ComponentUtils.createBoldLabel(5f,JLabel.RIGHT);
	}
	
	protected JComponent getControl(String property){
		return getControl(property, defaultFormat);
	}
	
	private Format defaultFormat=UIUtils.buildToStringFormat();
	private Format moneyFormat=NumberFormat.getCurrencyInstance();
	
	protected JComponent getControl(String property,Format format){
		JComponent c=components.get(property) ;
		if(c==null){
			JLabel label=createLabel();
			try {
				ValueModel valueModel=ConverterFactory.createStringConverter(model.getModel(property), format);
				Bindings.bind(label, valueModel);
			} catch (Exception e) {
				String msg=ExceptionUtils.getRootCauseMessage(e);				
				label.setToolTipText("Binding error Property: "+property+"  "+msg);
				System.out.println("Binding error Property: "+property+"  "+msg);
			}
			components.put(property, label);
			return label;
		}else
			return c;
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
				//Font f=UIManager.getFont("Label.font");
				//f=f.deriveFont(f.getSize2D()+5.0f);
				//UIManager.put("Label.font", f);
				SXAbstractDialog dialog=new SXAbstractDialog(""){
					protected JComponent buildContent() {
						Pedido pedido=new Pedido();
						pedido.setTotal(BigDecimal.valueOf(50000));
						pedido.setNombre("IMPRESORA DEL NORTE DE MEXICO");
						PresentationModel model=new PresentationModel(pedido);
						PedidoHeader header=new PedidoHeader(model);
						return header.getControl();
					}					
				};
				dialog.open();
				
				System.exit(0);
			}

		});
	}

}
