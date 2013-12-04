package com.luxsoft.siipap.swing.impl;

import javax.swing.JComponent;
import javax.swing.JPanel;

import org.jmock.Mock;
import org.jmock.MockObjectTestCase;

import com.luxsoft.siipap.swing.AbstractView;
import com.luxsoft.siipap.swing.Page;
import com.luxsoft.siipap.swing.View;

public class InfoNodeTabbedPageTest extends MockObjectTestCase{
	
	private Page page;
	
	public void setUp(){
		page=new InfoNodeTabbedPage();
	}

	/**	 
	 *	Prueba que se agregen correctamente las vistas al page y que 
	 *	el metodo open y focusGanined se detonen en cada una de ellas
	 *
	 */
	public void testAddView() {
		for(int i=0;i<0;i++){
			Mock mockView=mock(View.class);
			
			//Simulados (Stub)
			mockView.stubs().method("getId").will(returnValue("id_"+i));
			mockView.stubs().method("getVisualSupport").will(returnValue(null));
			
			//Oblicados a ser llamados
			mockView.expects(once()).method("open").withNoArguments();			
			mockView.expects(once()).method("focusGained").withNoArguments();
			mockView.expects(once()).method("getContent").withNoArguments().will(returnValue(new JPanel()));
			page.addView((View)mockView.proxy());
		}
	}

	/**
	 * Probar que al cerrar el objeto page se detona el metodo close en todas y cada una de las vistas 
	 *
	 */
	public void testClose() {
		for(int i=0;i<10;i++){
			Mock mockView=mock(View.class);
			
			// Simulados (Stub)
			mockView.stubs().method("getId").will(returnValue("id_"+i));
			mockView.stubs().method("getVisualSupport").will(returnValue(null));
			mockView.stubs().method("getContent").will(returnValue(new JPanel()));
			mockView.stubs().method("open");
			mockView.stubs().method("focusGained");
			
			//mockView.stubs().method("close");
			//mockView.stubs().method("focusLost");
			
			//Oblicados a ser llamados
			mockView.expects(once()).method("close").withNoArguments();			
			mockView.expects(once()).method("focusLost").withNoArguments();
			
			page.addView((View)mockView.proxy());
		}
		page.close();
		
	}

	/**
	 * Prueba la existencia del contenedor
	 *
	 */
	public void  testGetContainer() {
		assertNotNull(page.getContainer());
	}

	/**
	 * Prueba que la implementacion proporcione una lista confiable de
	 * las vistas hospedadas
	 *
	 */
	public void testGetViews() {
		for(int i=0;i<10;i++){
			Mock mockView=mock(View.class);
			
			// Simulados (Stub)
			mockView.stubs().method("getId").will(returnValue("id_"+i));
			mockView.stubs().method("getVisualSupport").will(returnValue(null));
			mockView.stubs().method("getContent").will(returnValue(new JPanel()));
			mockView.stubs().method("open");
			mockView.stubs().method("focusGained");
			
			mockView.stubs().method("close");
			mockView.stubs().method("focusLost");
			page.addView((View)mockView.proxy());
		}
		assertEquals(10,page.getViews().size());
		
	}
	
	/**
	 * 
	 * Prueba que solo se agrega una vista     
	 * 
	 */
	public void testSingleView() {
		/*
		for(int i=0;i<10;i++){
			Mock mockView=mock(View.class);
			
			
			//mockView.expects(atLeastOnce()).method("getId").will(returnValue("sameView"));
			
			mockView.expects(atLeastOnce()).method("equals").withAnyArguments().will(returnValue(true));
			
			//		 Simulados (Stub)
			mockView.stubs().method("getId").will(returnValue("sameView"));
			mockView.stubs().method("getVisualSupport").will(returnValue(null));
			mockView.stubs().method("getContent").will(returnValue(new JPanel()));
			mockView.stubs().method("open");
			mockView.stubs().method("focusGained");
			
			mockView.stubs().method("close");
			mockView.stubs().method("focusLost");
			page.addView((View)mockView.proxy());
		}
		*/
		AbstractView v1=new AbstractView("testView"){

			@Override
			protected JComponent buildContent() {
				return new JPanel();
			}
			
		};
		page.addView(v1);
		page.addView(v1);
		
		assertEquals(1,page.getViews().size());
		
	}

}
