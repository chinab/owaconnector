package com.owaconnector.client;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Component;
import org.springframework.webflow.execution.FlowExecutionOutcome;
import org.springframework.webflow.mvc.servlet.AbstractFlowHandler;

//@Component(value = "configuration/overview")
public class ConfigurationFlowHandler extends AbstractFlowHandler {
	public String handleExecutionOutcome(FlowExecutionOutcome outcome,
			HttpServletRequest request, HttpServletResponse response) {
		if (outcome.getId().equals("bookingConfirmed")) {
			return "/booking/show?bookingId="
					+ outcome.getOutput().get("bookingId");
		} else {
			return "/hotels/index";
		}
	}

}
