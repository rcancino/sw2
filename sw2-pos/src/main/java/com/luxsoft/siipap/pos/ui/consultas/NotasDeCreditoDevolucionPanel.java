package com.luxsoft.siipap.pos.ui.consultas;

import java.util.List;

import javax.swing.Action;

import ca.odell.glazedlists.CollectionList;
import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.CollectionList.Model;
import ca.odell.glazedlists.gui.TableFormat;

import com.luxsoft.siipap.cxc.model.NotaDeCreditoDevolucion;
import com.luxsoft.siipap.pos.POSRoles;
import com.luxsoft.siipap.pos.ui.utils.ImpresionUtils;
import com.luxsoft.siipap.swing.browser.AbstractMasterDatailFilteredBrowserPanel;
import com.luxsoft.siipap.swing.utils.MessageUtils;
import com.luxsoft.siipap.ventas.model.DevolucionDeVenta;
import com.luxsoft.sw3.services.Services;

public class NotasDeCreditoDevolucionPanel extends AbstractMasterDatailFilteredBrowserPanel<NotaDeCreditoDevolucion, DevolucionDeVenta>{

	public NotasDeCreditoDevolucionPanel() {
		super(NotaDeCreditoDevolucion.class);
	}
	

	@Override
	protected void agregarMasterProperties() {
		addProperty("fecha","sucursal.nombre","folio","devolucion.numero","devolucion.venta.origen","nombre","total","comentario","tipoDeDevolucion","impreso","aplicable");
		addLabels("Fecha","Sucursal","Folio","RMD","Origen","Cliente","Total","Comentario","Tipo","Impreso","Aplicable");
		manejarPeriodo();
		installTextComponentMatcherEditor("Sucursal","sucursal.nombre");
		installTextComponentMatcherEditor("Cliente","nombre");
		installTextComponentMatcherEditor("Folio","folio");
	}

	@Override
	protected TableFormat createDetailTableFormat() {
		String props[]={"nota.folio","devolucion.fecha","devolucion.numero","clave","descripcion","cantidad","importeNeto"};
		String labels[]={"Nota","Fecha","Número","Producto","Descripcion","Devolución","Importe"};
		return GlazedLists.tableFormat(DevolucionDeVenta.class, props,labels);
	}

	@Override
	protected Model<NotaDeCreditoDevolucion, DevolucionDeVenta> createPartidasModel() {
		return new CollectionList.Model<NotaDeCreditoDevolucion, DevolucionDeVenta>(){
			public List<DevolucionDeVenta> getChildren(NotaDeCreditoDevolucion parent) {
				String hql="from DevolucionDeVenta d where d.nota.id=?";
				return Services.getInstance().getHibernateTemplate().find(hql, parent.getId());
			}
			
		};
	}
	
	@Override
	protected List<NotaDeCreditoDevolucion> findData() {
		//String hql="from NotaDeCreditoDevolucion n where n.sucursal.id=? and n.fecha between ? and ?";
		//Object params[]={Services.getInstance().getConfiguracion().getSucursal().getId(),periodo.getFechaInicial(),periodo.getFechaFinal()};
		String hql="from NotaDeCreditoDevolucion n where  n.fecha between ? and ? and n.devolucion.venta.origen!=\'CRE\'";
		Object params[]={periodo.getFechaInicial(),periodo.getFechaFinal()};		
		return Services.getInstance().getHibernateTemplate().find(hql, params);
	}


	@Override
	public Action[] getActions() {
		if(actions==null)
			actions=new Action[]{
				getLoadAction()
				//,getInsertAction()
				,addRoleBasedContextAction(null, POSRoles.NOTAS_POR_DEVOLUCION.name(), this, "cancelar", "Cancelar")
				,addRoleBasedContextAction(null, POSRoles.NOTAS_POR_DEVOLUCION.name(), this, "imprimir", "Imprimir")
				//,getEditAction()
				//,getViewAction()
				};
		return actions;
	}
	
	public void imprimir(){
		NotaDeCreditoDevolucion nota=(NotaDeCreditoDevolucion)getSelectedObject();
		if(nota!=null && (nota.getImpreso()==null)){
			int index=source.indexOf(nota);
			if(index!=-1){
				if(MessageUtils.showConfirmationMessage("Generar nota de crédito a RMD: "+nota.getDevolucion().getNumero()
						, "Notas de crédito")){
					ImpresionUtils.imprimirNotaDevolucion(nota.getId());
					NotaDeCreditoDevolucion fresh=(NotaDeCreditoDevolucion)Services.getInstance().getUniversalDao().get(NotaDeCreditoDevolucion.class, nota.getId());
					source.set(index,fresh);
				}
				
			}
			
			
		}
	}

}
