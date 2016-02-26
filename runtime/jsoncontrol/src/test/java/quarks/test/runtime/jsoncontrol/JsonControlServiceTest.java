/*
# Licensed Materials - Property of IBM
# Copyright IBM Corp. 2015, 2016 
*/
package quarks.test.runtime.jsoncontrol;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.google.gson.JsonObject;

import quarks.runtime.jsoncontrol.JsonControlService;

public class JsonControlServiceTest {
	
	public static interface MyBean {
		void doIt();
	}
	
	public static class MyBeanImpl implements MyBean {
		
		private boolean doneIt;

		@Override
		public synchronized void doIt() {
			doneIt = true;	
		}
		
		synchronized boolean isDoneIt() {
			return doneIt;
		}
	}
	
	@Test
	public void testNoArg() throws Exception {
		JsonControlService control = new JsonControlService();
		
		MyBeanImpl cb1 = new MyBeanImpl();
		MyBeanImpl cb2 = new MyBeanImpl();
		
		assertFalse(cb1.isDoneIt());
		assertFalse(cb2.isDoneIt());
				
		control.registerControl("myb", "1", null, MyBean.class, cb1);
		control.registerControl("myb", "2", null, MyBean.class, cb2);
		
		assertFalse(cb1.isDoneIt());
		assertFalse(cb2.isDoneIt());
		
		JsonObject req =  new JsonObject();		
		req.addProperty(JsonControlService.TYPE_KEY, "myb");
		req.addProperty(JsonControlService.ALIAS_KEY, "1");
		req.addProperty(JsonControlService.OP_KEY, "doIt");		
		control.controlRequest(req);

		assertTrue(cb1.isDoneIt());
		assertFalse(cb2.isDoneIt());
		
		req =  new JsonObject();		
		req.addProperty(JsonControlService.TYPE_KEY, "myb");
		req.addProperty(JsonControlService.ALIAS_KEY, "2");
		req.addProperty(JsonControlService.OP_KEY, "doIt");		
		control.controlRequest(req);
		
		assertTrue(cb1.isDoneIt());
		assertTrue(cb2.isDoneIt());
	}
    
}
