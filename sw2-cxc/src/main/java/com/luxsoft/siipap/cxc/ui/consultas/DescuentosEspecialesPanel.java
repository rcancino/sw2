package com.luxsoft.siipap.cxc.ui.consultas;

import java.util.List;

import javax.swing.Action;

import com.luxsoft.siipap.cxc.CXCActions;
import com.luxsoft.siipap.service.ServiceLocator2;
import com.luxsoft.siipap.swing.browser.FilteredBrowserPanel;
import com.luxsoft.siipap.ventas.model.DescuentoEspecial;

/**
 * Panel para el mantenimiento de descuentos especiales
 * 
 * @author Ruben Cancino Ramos
 *
 */
public class DescuentosEspecialesPanel extends FilteredBrowserPanel<DescuentoEspecial>{

	public DescuentosEspecialesPanel() {
		super(DescuentoEspecial.class);
	}
	
	protected void init(){
		String[] props=new String[]{
				"cargo.tipoDocto"
				,"cargo.documento"
				,"cargo.precioNeto"
				,"cargo.fecha"
				,"cargo.sucursal"
				,"cargo.nombre"
				,"cargo.total"
				,"cargo.devoluciones"
				,"cargo.bonificaciones"
				,"cargo.saldoCalculado"
				,"cargo.descuentoNota"
				,"descuento.descuento"
				,"descuento.descripcion"
				};
		String[] names=new String[]{
				"Tipo"
				,"Docto"
				,"PN"
				,"Fecha"
				,"Suc"
				,"Nombre"
				,"Total"
				,"Devs"
				,"Bonific"
				,"Saldo"
				,"Desc Nota"
				,"Descuento"
				,"Comentario"
				};
		addProperty(props);
		addLabels(names);
		
		
	}
	
	@Override
	public Action[] getActions() {
		if(actions==null){
			this.actions=new Action[]{
					getLoadAction()
					,addAction(CXCActions.CancelarDescuentoEspecial.getId(), "cancelarDescuento", "Cancelar")
			};
		}
		return actions;
	}
	
	public void cancelarDescuento(){
		delete();
	}

	@Override
	public boolean doDelete(DescuentoEspecial bean) {
		ServiceLocator2.getDescuentosManager().cancelarDescuentoEspecial(bean.getId());
		return true;
	}

	@Override
	protected List<DescuentoEspecial> findData() {
		return ServiceLocator2.getHibernateTemplate().find("from DescuentoEspecial de left join fetch de.cargo c");
	}
	
	 
	

}
