package com.luxsoft.siipap.pos.ui.venta.forms;

import java.awt.BorderLayout;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.text.MessageFormat;

import javax.swing.JPanel;

import org.apache.commons.lang.StringUtils;

import com.luxsoft.siipap.model.Direccion;
import com.luxsoft.siipap.model.core.Cliente;
import com.luxsoft.siipap.swing.controls.Header;
import com.luxsoft.sw3.ventas.Pedido;

/**
 * Panel para presentar en forma elegante los datos generales y representativos de un cliente
 * 
 * TODO Implementar mediante HTML y FreeMarker y/o Velocity
 * 
 * @author Ruben Cancino Ramos
 *
 */
public class PedidoHeader extends JPanel implements PropertyChangeListener{

	private Header header;

	public PedidoHeader() {
		setLayout(new BorderLayout());
		header = new Header("Seleccione un producto", "");
		add(header.getHeader());
	}	

	public void updateHeader() {
		Cliente c = getCliente();		
		if (c != null) {
			if(c.getClave().equals("1")){
				header.setTitulo("Venta Mostrador");
				header.setDescripcion(c.getNombreRazon());
				return;
			}
			header.setTitulo(c.getNombreRazon()+ " ( "+c.getClave()+" )"+ "  RFC: "+c.getRfc());
			if (c.getDireccionFiscal() != null) {
				String pattern = "" +
						"Calle  : {0}  #       {1} Int  : {8} 		Crédito{2} \n"+
						"Col    : {3} CP:      {4} \n" +
						"Del/Mpo: {5} Entidad: {6} 	Tel(s):{9}  {10}	Kilos: {7}";
				Direccion df = c.getDireccionFiscal();
				String msg = MessageFormat.format(pattern, 
						df.getCalle(), 
						df.getNumero(),
						c.isDeCredito() ? (c.getCredito().isSuspendido() ? "NO": "SI") : "NO",
						df.getColonia(),
						df.getCp(), 
						df.getMunicipio(),
						df.getEstado(),  						
						pedido.getKilos()
						,StringUtils.trimToEmpty(df.getNumeroInterior())
						,StringUtils.trimToEmpty(c.getTelefono1())
						,c.getTelefono2()
							);
				header.setDescripcion(msg);
			}

		} else {
			header.setTitulo("Seleccione un cliente");
			header.setDescripcion("");
		}
	}
	
	
	private Pedido pedido;

	public Cliente getCliente() {
		return pedido.getCliente();
	}

	public Pedido getPedido() {
		return pedido;
	}

	public void setPedido(Pedido pedido) {
		this.pedido = pedido;
	}

	public void propertyChange(PropertyChangeEvent evt) {
		updateHeader();
		
	}

	

}
