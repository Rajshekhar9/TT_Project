/*******************************************************************************
 * Licensed Materials - Property of IBM (c) Copyright IBM Corporation 2005-20014.
 * All Rights Reserved.
 * 
 * Note to U.S. Government Users Restricted Rights: Use, duplication or
 * disclosure restricted by GSA ADP Schedule Contract with IBM Corp.
 ******************************************************************************/
package com.ibm.js.team.workitem.extension.advisor.statechange.service;

import org.eclipse.core.runtime.IProgressMonitor;

import com.ibm.team.process.common.IProcessConfigurationElement;
import com.ibm.team.process.common.advice.AdvisableOperation;
import com.ibm.team.process.common.advice.IAdvisorInfo;
import com.ibm.team.process.common.advice.IAdvisorInfoCollector;
import com.ibm.team.process.common.advice.runtime.IOperationAdvisor;
import com.ibm.team.repository.common.IAuditable;
import com.ibm.team.repository.common.IContributorHandle;
import com.ibm.team.repository.common.TeamRepositoryException;
import com.ibm.team.repository.service.AbstractService;
import com.ibm.team.workitem.common.ISaveParameter;
import com.ibm.team.workitem.common.model.IWorkItem;
import com.ibm.team.workitem.common.workflow.IWorkflowInfo;
import com.ibm.team.workitem.service.IWorkItemServer;

public class RestrictClosingToOwner extends AbstractService implements
		IOperationAdvisor {

	@Override
	public void run(AdvisableOperation operation,
			IProcessConfigurationElement advisorConfiguration,
			IAdvisorInfoCollector collector, IProgressMonitor monitor)
			throws TeamRepositoryException {
		Object data = operation.getOperationData();
		if (data instanceof ISaveParameter) {
			IAuditable auditable = ((ISaveParameter) data).getNewState();
			if (auditable instanceof IWorkItem) {
				IWorkItem workItem = (IWorkItem) auditable;
				
			

				// // If this needs to be limited to a special type
				// if (workItem.getWorkItemType() != "Enter Type ID Here")
				// return;

				// We want to allow saving the work item, if there is no state
				// change happening.
				String action = ((ISaveParameter) data).getWorkflowAction();
				if (action == null)
					return;

				// Get the workflow info and check if the new state is in the
				// closed group.
				IWorkItemServer iWorkItemServer = getService(IWorkItemServer.class);
				IWorkflowInfo workflowInfo = iWorkItemServer.findWorkflowInfo(
						workItem, monitor);
				if (!(workflowInfo.getStateGroup(workItem.getState2()) == IWorkflowInfo.CLOSED_STATES)) {
					return; // nothing to check if the new state is not closed.
				}

				// work item is going to a state in the closed group.
				// Check if the current user is owner of the work item.
				IContributorHandle loggedIn = this
						.getAuthenticatedContributor();
				
				IContributorHandle owner = workItem.getOwner();
						
				if ((owner != null && owner.getItemId().equals(
						loggedIn.getItemId())))
					return;

				IAdvisorInfo info = collector
						.createProblemInfo(
								"The work item can only closed by its owner!",
								"The work item can only closed by its owner! If the owner is unassigned and it can also not be closed.",
								"error");
				collector.addInfo(info);
			}
		}
	}
}
