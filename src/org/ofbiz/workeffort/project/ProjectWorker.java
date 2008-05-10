/*******************************************************************************
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *******************************************************************************/

package org.ofbiz.workeffort.project;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import javax.servlet.jsp.PageContext;

import org.ofbiz.base.util.Debug;
import org.ofbiz.base.util.UtilMisc;
import org.ofbiz.entity.GenericDelegator;
import org.ofbiz.entity.GenericEntityException;
import org.ofbiz.entity.GenericValue;
import org.ofbiz.entity.condition.EntityCondition;
import org.ofbiz.entity.condition.EntityConditionList;
import org.ofbiz.entity.condition.EntityExpr;
import org.ofbiz.entity.condition.EntityOperator;

/**
 * WorkEffortWorker - Worker class to reduce code in JSPs & make it more reusable
 */
public class ProjectWorker {
    
    public static final String module = ProjectWorker.class.getName();

    public static void getAssignedProjects(PageContext pageContext, String projectsAttrName) {
        GenericDelegator delegator = (GenericDelegator) pageContext.getRequest().getAttribute("delegator");
        GenericValue userLogin = (GenericValue) pageContext.getSession().getAttribute("userLogin");

        Collection validWorkEfforts = null;

        if (userLogin != null && userLogin.get("partyId") != null) {
            try {
                EntityConditionList<EntityExpr> ecl = new EntityConditionList<EntityExpr>(UtilMisc.toList(
                        new EntityExpr("partyId", EntityOperator.EQUALS, userLogin.get("partyId")),
                        new EntityExpr("currentStatusId", EntityOperator.NOT_EQUAL, "WF_COMPLETED"),
                        new EntityExpr("currentStatusId", EntityOperator.NOT_EQUAL, "WF_TERMINATED"),
                        new EntityExpr("currentStatusId", EntityOperator.NOT_EQUAL, "WF_ABORTED"),
                        new EntityExpr("workEffortTypeId", EntityOperator.EQUALS, "TASK"),
                        new EntityExpr("workEffortPurposeTypeId", EntityOperator.EQUALS, "WEPT_PROJECT")),
                        EntityOperator.AND);
                validWorkEfforts = delegator.findList("WorkEffortAndPartyAssign", ecl, null, UtilMisc.toList("priority"), null, false);
            } catch (GenericEntityException e) {
                Debug.logWarning(e, module);
            }
        }
        if (validWorkEfforts == null || validWorkEfforts.size() <= 0)
            return;

        pageContext.setAttribute(projectsAttrName, validWorkEfforts);
    }

    public static void getAllAssignedProjects(PageContext pageContext, String projectsAttrName) {
        GenericDelegator delegator = (GenericDelegator) pageContext.getRequest().getAttribute("delegator");
        GenericValue userLogin = (GenericValue) pageContext.getSession().getAttribute("userLogin");

        Collection validWorkEfforts = null;

        if (userLogin != null && userLogin.get("partyId") != null) {
            try {
                EntityConditionList<EntityExpr> ecl = new EntityConditionList<EntityExpr>(UtilMisc.toList(
                        new EntityExpr("partyId", EntityOperator.EQUALS, userLogin.get("partyId")),
                        new EntityExpr("workEffortTypeId", EntityOperator.EQUALS, "TASK"),
                        new EntityExpr("workEffortPurposeTypeId", EntityOperator.EQUALS, "WEPT_PROJECT")), 
                        EntityOperator.AND);
                validWorkEfforts = delegator.findList("WorkEffortAndPartyAssign", ecl, null, UtilMisc.toList("priority"), null, false);
            } catch (GenericEntityException e) {
                Debug.logWarning(e, module);
            }
        }
        if (validWorkEfforts == null || validWorkEfforts.size() <= 0)
            return;

        pageContext.setAttribute(projectsAttrName, validWorkEfforts);
    }

    public static void getAllProjectPhases(PageContext pageContext, String phasesAttrName) {
        GenericDelegator delegator = (GenericDelegator) pageContext.getRequest().getAttribute("delegator");
        GenericValue userLogin = (GenericValue) pageContext.getSession().getAttribute("userLogin");

        String projectWorkEffortId = pageContext.getRequest().getParameter("projectWorkEffortId");

        // if there was no parameter, check the request attribute, this may be a newly created entity
        if (projectWorkEffortId == null)
            projectWorkEffortId = (String) pageContext.getRequest().getAttribute("projectWorkEffortId");

        Collection relatedWorkEfforts = null;

        if (userLogin != null && userLogin.get("partyId") != null) {
            try {
                EntityConditionList<EntityExpr> ecl = new EntityConditionList<EntityExpr>(UtilMisc.toList(
                        new EntityExpr("workEffortIdFrom", EntityOperator.EQUALS, projectWorkEffortId),
                        new EntityExpr("workEffortAssocTypeId", EntityOperator.EQUALS, "WORK_EFF_BREAKDOWN")),
                        EntityOperator.AND);
                relatedWorkEfforts = delegator.findList("WorkEffortAssoc", ecl, null, null, null, false);
            } catch (GenericEntityException e) {
                Debug.logWarning(e, module);
            }
        }

        Collection validWorkEfforts = new ArrayList();

        if (relatedWorkEfforts != null) {
            Iterator relatedWorkEffortsIter = relatedWorkEfforts.iterator();

            try {
                while (relatedWorkEffortsIter.hasNext()) {
                    GenericValue workEffortAssoc = (GenericValue) relatedWorkEffortsIter.next();
                    GenericValue workEffort = workEffortAssoc.getRelatedOne("ToWorkEffort");

                    // only get phases
                    if ("TASK".equals(workEffort.getString("workEffortTypeId")) &&
                        ("WEPT_PHASE".equals(workEffort.getString("workEffortPurposeTypeId")))) {
                        validWorkEfforts.add(workEffort);
                    }
                }
            } catch (GenericEntityException e) {
                Debug.logWarning(e, module);
            }
        }
        if (validWorkEfforts == null || validWorkEfforts.size() <= 0)
            return;

        pageContext.setAttribute(phasesAttrName, validWorkEfforts);
    }

    public static void getAllPhaseTasks(PageContext pageContext, String tasksAttrName) {
        GenericDelegator delegator = (GenericDelegator) pageContext.getRequest().getAttribute("delegator");
        GenericValue userLogin = (GenericValue) pageContext.getSession().getAttribute("userLogin");

        String phaseWorkEffortId = pageContext.getRequest().getParameter("phaseWorkEffortId");

        // if there was no parameter, check the request attribute, this may be a newly created entity
        if (phaseWorkEffortId == null)
            phaseWorkEffortId = (String) pageContext.getRequest().getAttribute("phaseWorkEffortId");

        Collection relatedWorkEfforts = null;

        if (userLogin != null && userLogin.get("partyId") != null) {
            try {
                EntityConditionList<EntityExpr> ecl = new EntityConditionList<EntityExpr>(UtilMisc.toList(
                        new EntityExpr("workEffortIdFrom", EntityOperator.EQUALS, phaseWorkEffortId),
                        new EntityExpr("workEffortAssocTypeId", EntityOperator.EQUALS, "WORK_EFF_BREAKDOWN")),
                        EntityOperator.AND);
                relatedWorkEfforts = delegator.findList("WorkEffortAssoc", ecl, null, null, null, false);
            } catch (GenericEntityException e) {
                Debug.logWarning(e, module);
            }
        }

        Collection validWorkEfforts = new ArrayList();

        if (relatedWorkEfforts != null) {
            Iterator relatedWorkEffortsIter = relatedWorkEfforts.iterator();

            try {
                while (relatedWorkEffortsIter.hasNext()) {
                    GenericValue workEffortAssoc = (GenericValue) relatedWorkEffortsIter.next();
                    GenericValue workEffort = workEffortAssoc.getRelatedOne("ToWorkEffort");

                    // only get phases
                    if ("TASK".equals(workEffort.getString("workEffortTypeId"))) {
                        validWorkEfforts.add(workEffort);
                    }
                }
            } catch (GenericEntityException e) {
                Debug.logWarning(e, module);
            }
        }
        if (validWorkEfforts == null || validWorkEfforts.size() <= 0)
            return;

        pageContext.setAttribute(tasksAttrName, validWorkEfforts);
    }

    public static void getTaskNotes(PageContext pageContext, String notesAttrName) {
        GenericDelegator delegator = (GenericDelegator) pageContext.getRequest().getAttribute("delegator");
        GenericValue userLogin = (GenericValue) pageContext.getSession().getAttribute("userLogin");

        String workEffortId = pageContext.getRequest().getParameter("workEffortId");

        // if there was no parameter, check the request attribute, this may be a newly created entity
        if (workEffortId == null)
            workEffortId = (String) pageContext.getRequest().getAttribute("workEffortId");

        Collection notes = null;

        if (userLogin != null && userLogin.get("partyId") != null) {
            try {
                EntityExpr ee = new EntityExpr("workEffortId", EntityOperator.EQUALS, workEffortId);
                notes = delegator.findList("WorkEffortNoteAndData", ee, null, UtilMisc.toList("noteDateTime"), null, false);
            } catch (GenericEntityException e) {
                Debug.logWarning(e, module);
            }
        }
        if (notes == null || notes.size() <= 0)
            return;

        pageContext.setAttribute(notesAttrName, notes);
    }

}
