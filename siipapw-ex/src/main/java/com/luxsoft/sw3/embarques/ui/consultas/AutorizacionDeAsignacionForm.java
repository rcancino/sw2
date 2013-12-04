package com.luxsoft.sw3.embarques.ui.consultas;

import java.awt.BorderLayout;
import java.text.MessageFormat;
import java.util.Date;
import java.util.List;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JComponent;
import javax.swing.JPanel;

import org.apache.commons.lang.builder.ToStringBuilder;

import com.jgoodies.binding.adapter.BasicComponentFactory;
import com.jgoodies.binding.list.SelectionInList;
import com.jgoodies.binding.value.ValueHolder;
import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;
import com.luxsoft.siipap.cxc.model.Cargo;
import com.luxsoft.siipap.model.Sucursal;
import com.luxsoft.siipap.service.ServiceLocator2;
import com.luxsoft.siipap.swing.binding.Binder;
import com.luxsoft.siipap.swing.controls.SXAbstractDialog;
import com.luxsoft.siipap.swing.utils.MessageUtils;
import com.luxsoft.siipap.util.DateUtil;
import com.luxsoft.siipap.ventas.model.AsignacionVentaCE;
import com.luxsoft.siipap.ventas.model.Venta;

public class AutorizacionDeAsignacionForm extends SXAbstractDialog{

	private ValueHolder documentoHolder=new ValueHolder(null);
	private ValueHolder fechaHolder=new ValueHolder(null);
	private ValueHolder sucursalHolder=new ValueHolder(null);
	private ValueHolder comentarioHolder=new ValueHolder(null);
	private ValueHolder solicitoHolder=new ValueHolder(null);
	
	public AutorizacionDeAsignacionForm() {
		super("Factura");
	}	

		@Override
	protected JComponent buildContent() {
		JPanel content=new JPanel(new BorderLayout());
		content.add(buildForm(),BorderLayout.CENTER);
		content.add(buildButtonBarWithOKCancel(),BorderLayout.SOUTH);
		return content;
	}
		
	private JComponent buildForm(){
			
		FormLayout layout=new  FormLayout("p,3dlu,150dlu:g");			
		DefaultFormBuilder builder=new DefaultFormBuilder(layout);
		builder.append("Documento",getControl("documento"));
		builder.append("Fecha",getControl("fecha"));
		builder.append("Sucursal",getControl("sucursal"));
		builder.append("Solicitó",getControl("solicito"));
		builder.append("Comentario",getControl("comentario"));
		
		
		return builder.getPanel();
	}
		
	private JComponent getControl(String property){
		if("documento".equals(property)){
			return BasicComponentFactory.createLongField(buffer(documentoHolder),0);
		}else if("fecha".equals(property)){
			return Binder.createDateComponent(fechaHolder);
		}else if("sucursal".equals(property)){
			 List<Sucursal> sucursales=ServiceLocator2.getLookupManager().getSucursales();
			 sucursales.add(null);
			 SelectionInList sl=new SelectionInList(sucursales,buffer(sucursalHolder));
			 return BasicComponentFactory.createComboBox(sl,new SucursalRenderer());
		}else if("comentario".equals(property)){
			return BasicComponentFactory.createTextField(buffer(comentarioHolder));
		}else if("solicito".equals(property)){
			return BasicComponentFactory.createTextField(buffer(solicitoHolder));
		}else 
			return null;
	}
		
	public Venta commit(){
		Venta v=new Venta();
		v.setDocumento((Long)documentoHolder.getValue());
		v.setSucursal((Sucursal)sucursalHolder.getValue());
		v.setFecha((Date)fechaHolder.getValue());
		v.setComentario((String)comentarioHolder.getValue());
		v.setComentario2((String)solicitoHolder.getValue());
		return v;
	}
		
		
	private class SucursalRenderer extends DefaultListCellRenderer{
			
		public void setText(String t){
			if(t.isEmpty())
				super.setText("");
			else
				super.setText(t);
		}
	}
	
	public static void autorizar(){
		AutorizacionDeAsignacionForm app=new AutorizacionDeAsignacionForm();
		app.open();
		if(!app.hasBeenCanceled()){
			Venta res=app.commit();
			String hql="from AsignacionVentaCE a where a.venta.documento=? and a.venta.fecha=? and a.venta.sucursal.id=?";
			List found=ServiceLocator2.getHibernateTemplate().find(hql, new Object[]{res.getDocumento(),res.getFecha(),res.getSucursal().getId()});
			if( found!=null && !(found.isEmpty()) ){
				AsignacionVentaCE aa=(AsignacionVentaCE)found.get(0);
				Date dia=new Date();
				Date asignacion=DateUtil.calcularDiaHabilAnterior(dia, 2);
				aa.setAsignacion(asignacion);
				aa.setComentario(res.getComentario());
				aa.setSolicito(res.getComentario2());
				aa=(AsignacionVentaCE)ServiceLocator2.getHibernateTemplate().merge(aa);
				
				String msg=MessageFormat.format("Venta {0} ({1}) sucursal:{2} ({3}) autorizada para su asignación "
						, res.getDocumento(),aa.getVenta().getNombre(),aa.getVenta().getSucursal().getNombre(),aa.getVenta().getFecha());
				MessageUtils.showMessage(msg, "Autorización de asignación");
				//System.out.println(" Localizo asignacion: "+ToStringBuilder.reflectionToString(aa));
				
			}else{
				String msg=MessageFormat.format("Venta {0} no encontrada verificar fecha y sucursal ", res.getDocumento());
				MessageUtils.showMessage(msg, "Autorización de asignación");
			}
		}
	}
	
	public static void main(String[] args) {
		autorizar();
	}

}
