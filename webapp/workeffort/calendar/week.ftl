<#--
Licensed to the Apache Software Foundation (ASF) under one
or more contributor license agreements.  See the NOTICE file
distributed with this work for additional information
regarding copyright ownership.  The ASF licenses this file
to you under the Apache License, Version 2.0 (the
"License"); you may not use this file except in compliance
with the License.  You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing,
software distributed under the License is distributed on an
"AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
KIND, either express or implied.  See the License for the
specific language governing permissions and limitations
under the License.
-->
<#if periods?has_content>
  <#if (maxConcurrentEntries < 2)>
    <#assign entryWidth = 100>
  <#else>
    <#assign entryWidth = (100 / (maxConcurrentEntries))>
  </#if>
<table cellspacing="0" class="basic-table calendar">
  <tr class="header-row">
    <td>${uiLabelMap.CommonTime}</td>
    <td colspan=${maxConcurrentEntries}>${uiLabelMap.WorkEffortCalendarEntries}</td>
  </tr>
  <#list periods as period>
    <#assign currentPeriod = false/>
    <#if (nowTimestamp >= period.start) && (nowTimestamp <= period.end)><#assign currentPeriod = true/></#if>
  <tr<#if currentPeriod> class="current-period"<#else><#if (period.calendarEntries?size > 0)> class="active-period"</#if></#if>>
    <td class="centered" width="1%">
      <a href="<@ofbizUrl>${parameters._LAST_VIEW_NAME_}?period=day&start=${period.start.time?string("#")}<#if eventsParam?has_content>&${eventsParam}</#if>${addlParam?if_exists}</@ofbizUrl>">${period.start?date?string("EEEE")?cap_first}&nbsp;${period.start?date?string.short}</a><br/>
      <a href="<@ofbizUrl>${parameters._LAST_VIEW_NAME_}?period=week&form=edit&start=${parameters.start?if_exists}&workEffortTypeId=EVENT&currentStatusId=CAL_TENTATIVE&estimatedStartDate=${period.start?string("yyyy-MM-dd HH:mm:ss")}&estimatedCompletionDate=${period.end?string("yyyy-MM-dd HH:mm:ss")}${addlParam?if_exists}</@ofbizUrl>">${uiLabelMap.CommonAddNew}</a>
    </td>
    <#list period.calendarEntries as calEntry>
        <#if calEntry.workEffort.actualStartDate?exists>
            <#assign startDate = calEntry.workEffort.actualStartDate>
          <#else>
            <#assign startDate = calEntry.workEffort.estimatedStartDate>
        </#if>

        <#if calEntry.workEffort.actualCompletionDate?exists>
            <#assign completionDate = calEntry.workEffort.actualCompletionDate>
          <#else>
            <#assign completionDate = calEntry.workEffort.estimatedCompletionDate>
        </#if>

        <#if !completionDate?exists>
            <#assign completionDate =  calEntry.workEffort.actualStartDate + calEntry.workEffort.actualMilliSeconds>
        </#if>    
        <#if !completionDate?exists>
            <#assign completionDate =  calEntry.workEffort.estimatedStartDate + calEntry.workEffort.estimatedMilliSeconds>
        </#if>    
    
    <#if calEntry.startOfPeriod>
    <td<#if (calEntry.periodSpan > 1)> rowspan="${calEntry.periodSpan}"</#if> width="${entryWidth?string("#")}%">
    <#if (startDate.compareTo(start)  <= 0 && completionDate.compareTo(next) >= 0)>
      ${uiLabelMap.CommonAllWeek}
    <#elseif (startDate.compareTo(period.start)  = 0 && completionDate.compareTo(period.end) = 0)>
      ${uiLabelMap.CommonAllDay}
    <#elseif startDate.before(start)>
      ${uiLabelMap.CommonUntil} ${completionDate?datetime?string.short}
    <#elseif completionDate.after(next)>
      ${uiLabelMap.CommonFrom} ${startDate?time?string.short}
    <#else>
      ${startDate?time?string.short}-${completionDate?time?string.short}
    </#if>
      <br/><a href="<@ofbizUrl>${parameters._LAST_VIEW_NAME_}?form=show&period=week&start=${parameters.start?if_exists}&workEffortId=${calEntry.workEffort.workEffortId}${addlParam?if_exists}</@ofbizUrl>" class="event">${calEntry.workEffort.workEffortName?default("Undefined")}</a>&nbsp;</td>
    </#if>
    </#list>
    <#if (period.calendarEntries?size < maxConcurrentEntries)>
      <#assign emptySlots = (maxConcurrentEntries - period.calendarEntries?size)>
        <td<#if (emptySlots > 1)> colspan="${emptySlots}"</#if>>&nbsp;</td>
    </#if>
    <#if maxConcurrentEntries = 0>
      <td width="${entryWidth?string("#")}%">&nbsp;</td>
    </#if>
  </tr>
  </#list>
</table>
<#else>
  <div class="screenlet-body">${uiLabelMap.WorkEffortFailedCalendarEntries}!</div>
</#if>
