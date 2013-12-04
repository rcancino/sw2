package com.luxsoft.sw3.contabilidad.ui.form;

import org.springframework.beans.BeanUtils;

import com.luxsoft.luxor.utils.Bean;
import com.luxsoft.siipap.service.ServiceLocator2;
import com.luxsoft.siipap.swing.form2.AbstractForm;
import com.luxsoft.sw3.contabilidad.model.CuentaContable;
import com.luxsoft.sw3.contabilidad.services.CuentasContablesManager;

public class CuentaContableController {
	
	
	public static CuentaContable generarCuenta(){
		CuentaContable proxy=(CuentaContable)Bean.proxy(CuentaContable.class);
		final CuentaContableFormModel model=new CuentaContableFormModel(proxy);
		final CuentaContableForm form=new CuentaContableForm(model);
		form.setTitle("Alta de Cuentas (ACUMULATIVAS)");
		form.open();
		if(!form.hasBeenCanceled()){
			CuentaContable target=new CuentaContable();
			Bean.normalizar(proxy, target, null);
			AbstractForm.showObject(target);
			target=getManager().salvar(target);
			
			return target;
		}
		return null;
	}
	
	public static void consultarCuenta(CuentaContable cuenta){
		CuentaContable proxy=(CuentaContable)Bean.proxy(CuentaContable.class);
		BeanUtils.copyProperties(cuenta, proxy);
		final CuentaContableFormModel model=new CuentaContableFormModel(proxy);
		model.setReadOnly(true);
		final CuentaContableForm form=new CuentaContableForm(model);
		form.setTitle("Consulta de Cuentas (ACUMULATIVAS)");
		form.open();
		
	}
	
	public static CuentaContable editarCuenta(CuentaContable source){
		//source=getManager().getCuentaContableDao().get(source.getId());
		CuentaContable proxy=(CuentaContable)Bean.proxy(CuentaContable.class);
		BeanUtils.copyProperties(source, proxy,new String[]{"subCuentas","padre"});
		final CuentaContableFormModel model=new CuentaContableFormModel(proxy);
		final CuentaContableForm form=new CuentaContableForm(model);
		form.setTitle("Edición de Cuentas (ACUMULATIVAS)");
		form.open();
		if(!form.hasBeenCanceled()){			
			BeanUtils.copyProperties(proxy, source
					, new String[]{"subCuentas","padre","version","id","log","addresLog"}
			);
			source=getManager().salvar(source);
			return source;
		}
		return null;
	}
	
	private static CuentasContablesManager getManager(){
		return ServiceLocator2.getCuentasContablesManager();
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
				generarCuenta();
				System.exit(0);
			}

		});
	}

}
