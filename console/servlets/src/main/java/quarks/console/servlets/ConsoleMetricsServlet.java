/*
# Licensed Materials - Property of IBM
# Copyright IBM Corp. 2015, 2016 
*/
package quarks.console.servlets;

import java.io.IOException;
import java.util.Iterator;
import java.util.Map;

import javax.management.ObjectInstance;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.gson.Gson;

public class ConsoleMetricsServlet extends HttpServlet {

	/*
	 * This servlet can accept requests for all jobs or a single job, but will
	 * most likely be rewritten to only accept a single job as a parameter
	 */
	private static final long serialVersionUID = -1548438576311809996L;
	
	public void doGet(HttpServletRequest request, HttpServletResponse response) 
		throws ServletException, IOException {
		// get the parameters for the job and operator (?) to fetch the metrics for
		
		Map<String,String[]> parameterMap = request.getParameterMap();
		String[] jobIds;
		String jobId = "";
		String[] metricNames;
		String metricName = "";
		boolean availableMetrics = false;
		for(Map.Entry<String,String[]> entry : parameterMap.entrySet()) {
			if (entry.getKey().equals("job")) {
				jobIds = entry.getValue();
				if (jobIds.length == 1) {
					jobId = jobIds[0];
				}
			} else if (entry.getKey().equals("metric")) {
				metricNames = entry.getValue();
				if (metricNames.length == 1) {
					metricName = metricNames[0];
				}
			} else if (entry.getKey().equals("availableMetrics")) {
				String[] getMetrics = entry.getValue();
				if (getMetrics.length == 1) {
					availableMetrics = true;
				}
			}
		}

		if (!jobId.equals("")) {
			Iterator<ObjectInstance> meterIterator = MetricsUtil.getMeterObjectIterator(jobId);
			Iterator<ObjectInstance> counterIterator = MetricsUtil.getCounterObjectIterator(jobId);
			if (availableMetrics) {
				MetricsGson gsonJob = MetricsUtil.getAvailableMetricsForJob(jobId, meterIterator, counterIterator);
				Gson gson = new Gson();
		    	response.setContentType("application/json");
		    	response.setCharacterEncoding("UTF-8");
		    	response.getWriter().write(gson.toJson(gsonJob));
				return;
			}
			
			MetricsGson mGson = MetricsUtil.getMetric(jobId, metricName, meterIterator, counterIterator);
			String jsonString = "";

			Gson gson = new Gson();
			jsonString = gson.toJson(mGson);
	    	response.setContentType("application/json");
	    	response.setCharacterEncoding("UTF-8");
	    	
	    	response.getWriter().write(jsonString);
			

		}
	}

}
