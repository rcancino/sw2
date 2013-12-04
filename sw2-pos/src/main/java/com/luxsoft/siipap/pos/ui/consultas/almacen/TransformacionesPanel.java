package com.luxsoft.siipap.pos.ui.consultas.almacen;


import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.Action;

import ca.odell.glazedlists.CollectionList;
import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.CollectionList.Model;
import ca.odell.glazedlists.gui.TableFormat;

import com.luxsoft.siipap.inventarios.model.Transformacion;
import com.luxsoft.siipap.inventarios.model.TransformacionDet;
import com.luxsoft.siipap.model.Sucursal;
import com.luxsoft.siipap.pos.POSRoles;
import com.luxsoft.siipap.pos.ui.utils.ReportUtils2;
import com.luxsoft.siipap.swing.browser.AbstractMasterDatailFilteredBrowserPanel;
import com.luxsoft.siipap.swing.utils.CommandUtils;
import com.luxsoft.sw3.services.Services;
import com.luxsoft.sw3.ui.forms.TransformacionesForm;
import com.luxsoft.sw3.ui.services.KernellUtils;

/**
 * Panel para el mantenimiento de traslados
 * 
 * @author Ruben Cancino
 *
 */
public class TransformacionesPanel extends AbstractMasterDatailFilteredBrowserPanel<Transformacion, TransformacionDet>{

	public TransformacionesPanel() {
		super(Transformacion.class);
		
	}
	
	protected void init(){		
		addProperty("clase","fecha","documento","porInventario","comentario");
		addLabels("Clase","Fecha","Docto","Por Inv","Comentario");
		installTextComponentMatcherEditor("Documento", new String[]{"documento"});
		manejarPeriodo();
		setActions(
				new Action[]{getLoadAction()
						,getInsertAction()
						,getDeleteAction()
						,CommandUtils.createPrintAction(this, "imprimir")
						}
				);
	}
	
	protected Comparator getDefaultDetailComparator(){
		return GlazedLists.beanPropertyComparator(TransformacionDet.class, "renglon");
	}

	@Override
	protected TableFormat createDetailTableFormat() {
		String[] props={"documento","fecha","transformacion.clase","renglon","clave","descripcion","producto.linea.nombre","unidad.nombre","kilos","cantidad","comentario"};
		String[] names={"documento","Fecha","Tipo","Rngl","Clave","Desc","Línea","U","Kg","Cant","Comentario"};		
		return GlazedLists.tableFormat(TransformacionDet.class, props,names);
	}

	@Override
	protected Model<Transformacion, TransformacionDet> createPartidasModel() {
		return new CollectionList.Model<Transformacion, TransformacionDet>(){
			public List<TransformacionDet> getChildren(Transformacion parent) {
				String hql="from TransformacionDet d where d.transformacion.id=?";
				return Services.getInstance().getHibernateTemplate().find(hql,parent.getId());
			}
			
		};
	}

	@Override
	protected Transformacion doInsert() {
		Transformacion t=TransformacionesForm.showForm();
		return t;
	}

	@Override
	public boolean doDelete(Transformacion bean) {
		if(KernellUtils.validarAcceso(POSRoles.GERENTE_DE_INVENTARIOS.name())){
			Services.getInstance().getTransfomracionesManager().remove(bean.getId());
			
		}
		
		return true;
	}

	@Override
	protected List<Transformacion> findData() {
		String hql="from Transformacion t where t.fecha between ? and ?";
		return Services.getInstance().getHibernateTemplate().find(hql,new Object[]{periodo.getFechaInicial(),periodo.getFechaFinal()});
	}

	
	public void imprimir(){
		Transformacion d=(Transformacion)getSelectedObject();
		if(d!=null){
			Sucursal suc=Services.getInstance().getConfiguracion().getSucursal();
			final Map parameters=new HashMap();
			parameters.put("TRANSFORMACION", d.getId());
			parameters.put("SUCURSAL", String.valueOf(suc.getId()));
			ReportUtils2.runReport("invent/Transformacion.jasper", parameters);
		}
	}

	
	
	
}
