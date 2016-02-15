/*
# Licensed Materials - Property of IBM
# Copyright IBM Corp. 2015,2016 
*/
package quarks.test.svt;

public class MyClass1 {
    private String s1, s2;
    private Double d1;

    MyClass1(String str1, String str2, Double d1) {
        this.s1 = str1; this.s2=str2; this.d1=d1; 
    }

    String getS1() {
        return s1; 
    }

    String getS2() {
        return s2; 
    }

    Double getD1() {
        return d1; 
    }

    public void setS1(String s) { 
        s1 = s; 
    }
    public void setS2(String s) {
        s2 = s; 
    }
    public void setD1(Double d) {
        d1 = d; 
    }

    public String toString() {
        return "s1: "+s1+" s2: "+s2 + " d1: "+d1; 
    }

}
