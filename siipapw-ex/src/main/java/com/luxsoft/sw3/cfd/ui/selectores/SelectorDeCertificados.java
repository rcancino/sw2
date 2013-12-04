package com.luxsoft.sw3.cfd.ui.selectores;

import java.util.List;

import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.ListSelection;
import ca.odell.glazedlists.gui.TableFormat;

import com.luxsoft.siipap.service.ServiceLocator2;
import com.luxsoft.siipap.swing.dialog.AbstractSelector;
import com.luxsoft.sw3.cfd.model.CertificadoDeSelloDigital;

public class SelectorDeCertificados extends AbstractSelector<CertificadoDeSelloDigital>{

	public SelectorDeCertificados() {
		super(CertificadoDeSelloDigital.class, "Selector de certificados de sellos digitales");
	}
	
	@Override
	protected TableFormat<CertificadoDeSelloDigital> getTableFormat() {
		final String[] cols={"numeroDeCertificado","expedicion","vencimiento","url","comentario","replicado"};
		final String[] names={"No Serie","Expedición","Vto","URL","Comentario","Replicado"};
		return GlazedLists.tableFormat(CertificadoDeSelloDigital.class, cols,names);
	}

	@Override
	protected List<CertificadoDeSelloDigital> getData() {
		return ServiceLocator2.getCertificadoDeSelloDigitalDao().getAll();
	}
	
	public static CertificadoDeSelloDigital seleccionar(){
		SelectorDeCertificados selector=new SelectorDeCertificados();
		selector.setSelectionMode(ListSelection.SINGLE_SELECTION);
		selector.open();
		if(selector.hasBeenCanceled()){
			return selector.getSelected();
		}else
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
				CertificadoDeSelloDigital selected=seleccionar();
				System.out.println(selected);
				System.exit(0);
			}

		});
	}

}
