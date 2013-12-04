package com.luxsoft.siipap.swing;

import org.springframework.context.ApplicationEvent;
import org.springframework.util.Assert;



public class SwingApplicationEvent extends ApplicationEvent{
	
	
	
	private EventType type;

	public SwingApplicationEvent(final Object source,final EventType type) {
		super(source);
		Assert.notNull(type,"Must specify an event type");
		this.type=type;
	}

	public EventType getType() {
		return type;
	}

	public void setType(EventType type) {
		this.type = type;
	}
	
	/**
	 * Factory method pattern to create events 
	 * 
	 * @param source
	 * @param type
	 * @return
	 */
	public static ApplicationEvent getEvent(final Object source,EventType type){
		return new SwingApplicationEvent(source,type);
	}
	

}
