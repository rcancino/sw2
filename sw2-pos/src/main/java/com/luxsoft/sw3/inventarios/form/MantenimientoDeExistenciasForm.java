package com.luxsoft.sw3.inventarios.form;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.MessageFormat;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;

import org.apache.commons.lang.StringUtils;

import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.TextFilterator;
import ca.odell.glazedlists.matchers.TextMatcherEditor;
import ca.odell.glazedlists.swing.AutoCompleteSupport;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;

import com.luxsoft.siipap.inventarios.model.Existencia;
import com.luxsoft.siipap.model.Sucursal;
import com.luxsoft.siipap.model.core.Producto;
import com.luxsoft.siipap.swing.controls.Header;
import com.luxsoft.siipap.swing.form2.AbstractForm;
import com.luxsoft.siipap.swing.form2.IFormModel;
import com.luxsoft.sw3.services.Services;


/**
 * Forma par el mantenimiento de existencias en las sucursales
 * particularmente lo relacionado con el recorte
 * 
 * @author Ruben Cancino
 *
 */
public class MantenimientoDeExistenciasForm extends AbstractForm{

	public MantenimientoDeExistenciasForm(IFormModel model) {
		super(model);
		
	}

	@Override
	protected JComponent buildFormPanel() {
		FormLayout layout=new FormLayout("p,3dlu,70dlu,4dlu,p,3dlu,70dlu","");
		DefaultFormBuilder builder=new DefaultFormBuilder(layout);		
		builder.append("Recorte",getControl("recorte"),true);
		builder.append("Comentario",getControl("recorteComentario"),5);
		return builder.getPanel();
	}
	
	private Header header;
	
	@Override
	protected JComponent buildHeader() {
		if(header==null){
			header=new Header("","");
			updateHeader();
		}
		return header.getHeader();
	}
	
	public void updateHeader() {
		if(header!=null){
			Producto p=(Producto)model.getValue("producto");
			if(p!=null){
				header.setTitulo(MessageFormat.format("{0} ({1})",p.getDescripcion(),p.getClave()));
				String pattern="Uni:{0}\t Ancho:{1}\tLargo:{2}\t Calibre:{3}" +
						"\nAcabado:{4}\t Caras:{5}\tPrecio:{6}" +
						"\nCrédito: {7,number,currency}\tContado: {8,number,currency} " +
						"\n\nExistencias:  {9,number,#,###,###} "
						;
				String desc=MessageFormat.format(pattern
						,p.getUnidad().getNombre()						
						,p.getAncho()
						,p.getLargo()
						,p.getCalibre()
						,p.getAcabado()
						,p.getCaras()
						,p.getModoDeVenta()!=null?(p.getModoDeVenta().equals("B")?"Bruto":"Neto"):""
						,p.getPrecioCredito()
						,p.getPrecioContado()
						,getExistencia().getCantidad()						
						);
				
				header.setDescripcion(desc);
			}
			else{
				header.setTitulo("Seleccione un producto");
				header.setDescripcion("");
			}
		}
	}
	
	private Existencia getExistencia(){
		Existencia exis=(Existencia)model.getBaseBean();
		return exis;
	}
	
	

}
