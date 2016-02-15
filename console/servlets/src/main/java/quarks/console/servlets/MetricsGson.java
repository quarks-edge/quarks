/*
# Licensed Materials - Property of IBM
# Copyright IBM Corp. 2015, 2016 
*/
package quarks.console.servlets;

import java.util.ArrayList;
import java.util.Iterator;


public class MetricsGson {

	public String jobId = null;
	public ArrayList<Operator> ops = new ArrayList<Operator>();
	
	class Operator {
		String opId = null;
		ArrayList<OpMetric> metrics = null;
	}
	
	class OpMetric {
		// the primitive type of the metric
		String type = null;
		String name = null;
		String value;
	}
	
	public String getJobId () {
		return this.jobId;
	}
	
	public void setJobId(String id) {
		this.jobId = id;
	}
	
	public void addOp(Operator theOp) {
		this.ops.add(theOp);
	}
	
	public Operator getOp(String opId) {
		if (this.ops.size() == 0) {
			return null;
		}
		
		Iterator<Operator> opsIterator = this.ops.iterator();
		while (opsIterator.hasNext()) {
			Operator op = (Operator) opsIterator.next();
			if (getOpId(op).equals(opId)) {
				return op;
			}
		}
		return null;
	}
	
	public ArrayList<Operator> getOps() {
		return this.ops;
	}
	
	public boolean isOpInJob(String opId){
		if (this.ops.size() == 0) {
			return false;
		}
		
		Iterator<Operator> opsIterator = this.ops.iterator();
		while (opsIterator.hasNext()) {
			Operator op = (Operator) opsIterator.next();
			if (getOpId(op).equals(opId)) {
				return true;
			}
		}
		return false;
	}
	
	public String getOpId(Operator anOperator) {
		return anOperator.opId;
	}
	
	public void setOps(ArrayList<Operator> operators){
		this.ops = operators;
	}
	
	public ArrayList<OpMetric> getOpMetrics(Operator theOp) {
		return theOp.metrics;
	}
	
	public void setOpMetrics(Operator theOp, ArrayList<OpMetric> theMetrics) {
		theOp.metrics = theMetrics;
	}
	
}
