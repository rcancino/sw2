package com.luxsoft.sw3.contabilidad.ui.consultas;

import java.util.List;

import javax.swing.Action;
import javax.swing.SwingWorker;

import com.luxsoft.siipap.service.ServiceLocator2;
import com.luxsoft.siipap.swing.browser.FilteredBrowserPanel;
import com.luxsoft.siipap.swing.utils.TaskUtils;
import com.luxsoft.sw3.contabilidad.model.CuentaContable;
import com.luxsoft.sw3.contabilidad.ui.form.CuentaContableController;

/**
 * Mantenimiento al catalogo de cuentas contables
 * 
 * @author Ruben Cancino Ramos
 *
 */
public class CuentasContablesPanel extends FilteredBrowserPanel<CuentaContable>{

	public CuentasContablesPanel() {
		super(CuentaContable.class);
	}
	
	protected void init(){
		addProperty(
				"id"
				,"clave"
				,"tipo"
				,"subTipo"
				,"descripcion"
				,"naturaleza"
				,"detalle"
				,"deResultado"				
				,"presentacionContable"
				,"presentacionFiscal"
				,"presentacionFinanciera"
				,"presentacionPresupuestal"
				,"descripcion2"
				);
		addLabels(
				"Id"
				,"Clave"
				,"Tipo"
				,"Sub Tipo"
				,"Descripcion"
				,"Nat"
				,"D"
				,"Resultados"				
				,"P. Contable"
				,"P. Fiscal"
				,"P. Financiera"
				,"P. Presupuestal"
				,"Descripcion 2"
				);
	}

	@Override
	protected List<CuentaContable> findData() {
		return ServiceLocator2.getHibernateTemplate().find("from CuentaContable c where c.padre is null");
	}

	@Override
	protected CuentaContable doInsert() {
		return CuentaContableController.generarCuenta();
	}

	@Override
	protected void doSelect(Object bean) {
		CuentaContable cuenta=(CuentaContable)bean;
		CuentaContableController.consultarCuenta(cuenta);
	}

	@Override
	protected CuentaContable doEdit(CuentaContable bean) {
		return CuentaContableController.editarCuenta(bean);
	}

	@Override
	protected void executeLoadWorker(SwingWorker worker) {
		TaskUtils.executeSwingWorker(worker);
	}

	@Override
	public Action[] getActions() {
		if(actions==null)
			actions=new Action[]{
				getLoadAction()
				,getInsertAction()
				,getEditAction()
				,getViewAction()
				};
		return actions;
	}
	
	
	

}
