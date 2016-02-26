/*
# Licensed Materials - Property of IBM
# Copyright IBM Corp. 2016 
*/
package quarks.runtime.jsoncontrol;

/**
 * Control service that takes a Json message and invokes
 * an action on a control service MBean.
 * 
 * <H3>Operations</H3>
 * A JSON object passed to {@link quarks.runtime.jsoncontrol.JsonControlService#controlRequest(com.google.gson.JsonObject) controlRequest} with these name/value pairs is
 * handled as an operation resulting in a method call to a
 * void method on a control service MBean interface.
 * <UL>
 * <LI>{@code type=}<em>type</em></LI>
 * <LI>{@code alias=}<em>alias</em></LI>
 * <LI>{@code op=}<em>name</em></LI>
 * <LI>{@code arguments=}<em>optional list of arguments</em></LI>
 * </UL>
 */