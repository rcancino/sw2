package com.luxsoft.siipap.ventas.ui;

import javax.swing.JComponent;

import org.hibernate.validator.Length;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;
import com.luxsoft.luxor.utils.Bean;
import com.luxsoft.siipap.swing.form2.AbstractForm;
import com.luxsoft.siipap.swing.form2.DefaultFormModel;
import com.luxsoft.siipap.swing.form2.IFormModel;

public class PaginaForm  extends AbstractForm{
	

	public PaginaForm(IFormModel model) {
		super(model);	
		setTitle("Configuración para lista impresa");
	}

	@Override
	protected JComponent buildFormPanel() {
		FormLayout layout=new FormLayout("p,3dlu,100dlu","");
		DefaultFormBuilder builder=new DefaultFormBuilder(layout);
		builder.append("Pagina",getControl("pagina"));
		builder.append("Columna",getControl("columna"));
		builder.append("Grupo",getControl("grupo"));
		builder.append("Presentación",getControl("presentacion"));
		return builder.getPanel();
	}
	
	public static class PaginaBean{
		
		private int pagina;
		
		private int columna;
		
		private int grupo;
		
		@Length(max=255)
		private String presentacion;

		public int getPagina() {
			return pagina;
		}

		public void setPagina(int pagina) {
			this.pagina = pagina;
		}

		public int getColumna() {
			return columna;
		}

		public void setColumna(int columna) {
			this.columna = columna;
		}

		public int getGrupo() {
			return grupo;
		}

		public void setGrupo(int grupo) {
			this.grupo = grupo;
		}

		public String getPresentacion() {
			return presentacion;
		}

		public void setPresentacion(String presentacion) {
			this.presentacion = presentacion;
		}
		
		
		
	}
	
	public static PaginaBean showForm(){
		DefaultFormModel model=new DefaultFormModel(Bean.proxy(PaginaBean.class));
		final PaginaForm form=new PaginaForm(model);
		form.open();
		if(!form.hasBeenCanceled()){
			return (PaginaBean)model.getBaseBean();
		}
		return null;
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
				showObject(showForm());
				System.exit(0);
			}

		});
	}

}
