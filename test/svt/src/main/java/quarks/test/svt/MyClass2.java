/*
# Licensed Materials - Property of IBM
# Copyright IBM Corp. 2015,2016 
*/
package quarks.test.svt;

public class MyClass2 {
	private MyClass1 mc1, mc2;
	private Double d1;
	private String s1;

	MyClass2(MyClass1 mc1, MyClass1 mc2, Double d, String s) {
		this.mc1 = mc1; 
		this.mc2=mc2; 
		this.d1=d; 
		this.s1=s; 
	}

	MyClass1 getMc1() {
		return mc1; 
	}

	MyClass1 getMc2() {
		return mc2; 
	}

	Double getD1() {
		return d1; 
	}

	String getS1() {
		return s1; 
	}
	public void setMc1(MyClass1 mc) { 
		mc1 = mc; 
	}
	public void setMc2(MyClass1 mc) {
		mc2 = mc; 
	}
	public void setD1(Double d) {
		d1 = d; 
	}

	public void setS1(String s) {
		s1 = s; 
	}
	public String toString() {
		return "mc1: "+mc1.toString() + " mc2: " + mc2.toString() + " d1: "+ d1 + " s1: " + s1; 
	}
}


