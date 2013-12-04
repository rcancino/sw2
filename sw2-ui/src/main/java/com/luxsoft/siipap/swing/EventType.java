/**
 * 
 */
package com.luxsoft.siipap.swing;

public enum EventType {
	
	/**
     * The event type for an application that is starting.
     */
	APPLICATION_STARTING("ApplicationStarting"),
	
	/**
     * The event type for an application that is started.
     */
	APPLICATION_STARTED("ApplicationStarted"),
	
	/**
     * The event type for an application that is closing.
     */
	APPLICATION_CLOSING("ApplicationClosing"),
	/**
     * The event type for an application that is closed.
     */
	APPLICATION_CLOSED("ApplicationClosed");
	
	private String id="";
	
	private EventType(final String id){
		this.id=id;
	}
	
	public String toString(){
		return id;
	}
}