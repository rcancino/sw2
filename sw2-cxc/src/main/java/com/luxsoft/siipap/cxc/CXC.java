package com.luxsoft.siipap.cxc;




import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.luxsoft.siipap.swing.AbstractApplicationStarter;
import com.luxsoft.siipap.swing.utils.SWExtUIManager;


public class CXC extends AbstractApplicationStarter{
	
	
		

	@Override
	protected String[] getContextPaths() {
		
		return new String[]{				
				"swx-cxc-ctx.xml"
				,"swx-cxc-actions-ctx.xml"
				,"com/luxsoft/siipap/cxc/ui/swx-ui-context.xml"
		};
	}
	
	@Override
	protected ApplicationContext createApplicationContext() {
		return new ClassPathXmlApplicationContext(
				getContextPaths()
				//,ServiceLocator2.instance().getContext()
				);
		
	}
	
	
	

	public static void main(String[] args) {
		try {
			SWExtUIManager.setup();
			new CXC().start();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	
}
