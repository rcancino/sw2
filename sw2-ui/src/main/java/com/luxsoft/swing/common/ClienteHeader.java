package com.luxsoft.swing.common;

import java.awt.BorderLayout;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.text.MessageFormat;

import javax.swing.JPanel;

import org.apache.commons.lang.StringUtils;

import com.jgoodies.binding.value.ValueModel;
import com.luxsoft.siipap.model.Direccion;
import com.luxsoft.siipap.model.core.Cliente;
import com.luxsoft.siipap.swing.controls.Header;


/**
 * Panel para presentar en forma elegante los datos generales y representativos de un cliente
 * 
 * TODO Implementar mediante HTML y FreeMarker y/o Velocity
 * 
 * @author Ruben Cancino Ramos
 *
 */
public class ClienteHeader extends JPanel implements PropertyChangeListener{

	private Header header;
	private final ValueModel model;

	public ClienteHeader(final ValueModel vm) {
		setLayout(new BorderLayout());
		this.model=vm;
		this.model.addValueChangeListener(this);
		header = new Header("Seleccione un cliente", "");
		add(header.getHeader());
	}	

	public void updateHeader() {
		Cliente c = (Cliente)model.getValue();		
		if (c != null) {
			if(c.getClave().equals("1")){
				header.setTitulo("Venta Mostrador");
				header.setDescripcion(c.getNombreRazon());
				return;
			}
			header.setTitulo(c.getNombreRazon()+ " ( "+c.getClave()+" )");
			if (c.getDireccionFiscal() != null) {
				String pattern = "" +
						"Calle  : {0}  #       {1} Int  : {7} 		Crédito{2} \n"+
						"Col    : {3} CP:      {4} \n" +
						"Del/Mpo: {5} Entidad: {6} \nTel(s):{8} ";
				Direccion df = c.getDireccionFiscal();
				String msg = MessageFormat.format(pattern, 
						df.getCalle(), 
						df.getNumero(),
						c.isDeCredito() ? (c.getCredito().isSuspendido() ? " NO": " SI") : " NO",
						df.getColonia(),
						df.getCp(), 
						df.getMunicipio(),
						df.getEstado(),
						StringUtils.trimToEmpty(df.getNumeroInterior())
						,c.getTelefonosRow()
							);
				header.setDescripcion(msg);
			}

		} else {
			header.setTitulo("Seleccione un cliente");
			header.setDescripcion("");
		}
	}
	
	
	

	public void propertyChange(PropertyChangeEvent evt) {
		updateHeader();
		
	}

	

}
