/*
* Copyright 2014-2019 Logo Business Solutions
* (a.k.a. LOGO YAZILIM SAN. VE TIC. A.S)
*
* Licensed under the Apache License, Version 2.0 (the "License"); you may not
* use this file except in compliance with the License. You may obtain a copy of
* the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
* WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
* License for the specific language governing permissions and limitations under
* the License.
*/

package com.lbs.tedam.bsh;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.w3c.dom.Element;

import com.lbs.message.JLbsMessageUtil;
import com.lbs.tedam.bsh.utils.ScriptService;
import com.lbs.tedam.exception.CreateNewFileException;
import com.lbs.tedam.model.SnapshotValue;
import com.lbs.tedam.model.TestReport;
import com.lbs.tedam.model.DTO.GridCell;
import com.lbs.tedam.model.DTO.LookupParameter;
import com.lbs.tedam.model.DTO.MessageDialog;
import com.lbs.tedam.model.DTO.TabbedPaneAndPageParent;
import com.lbs.tedam.util.Constants;
import com.lbs.tedam.util.Enums.FilePath;
import com.lbs.tedam.util.Enums.FormOpenTypes;
import com.lbs.tedam.util.Enums.Regex;
import com.lbs.tedam.util.Enums.ScriptParameters;
import com.lbs.tedam.util.Enums.StatusMessages;
import com.lbs.tedam.util.Enums.TedamLogLevel;
import com.lbs.tedam.util.PropUtils;
import com.lbs.tedam.util.TedamReportUtils;
import com.lbs.test.error.JLbsTestErrorList;
import com.lbs.test.scripting.JLbsScriptButton;
import com.lbs.test.scripting.JLbsScriptCheckBox;
import com.lbs.test.scripting.JLbsScriptCheckBoxGroup;
import com.lbs.test.scripting.JLbsScriptComboBox;
import com.lbs.test.scripting.JLbsScriptComboEdit;
import com.lbs.test.scripting.JLbsScriptComponent;
import com.lbs.test.scripting.JLbsScriptContainer;
import com.lbs.test.scripting.JLbsScriptDateEdit;
import com.lbs.test.scripting.JLbsScriptFilterGrid;
import com.lbs.test.scripting.JLbsScriptGrid;
import com.lbs.test.scripting.JLbsScriptGridCell;
import com.lbs.test.scripting.JLbsScriptMenuButton;
import com.lbs.test.scripting.JLbsScriptMessageDialog;
import com.lbs.test.scripting.JLbsScriptNumEditWithCalc;
import com.lbs.test.scripting.JLbsScriptPopUpMenu;
import com.lbs.test.scripting.JLbsScriptRadioButtonGroup;
import com.lbs.test.scripting.JLbsScriptSyntaxEdit;
import com.lbs.test.scripting.JLbsScriptTabbedPane;
import com.lbs.test.scripting.JLbsScriptTextArea;
import com.lbs.test.scripting.JLbsScriptTextEdit;
import com.lbs.test.scripting.JLbsScriptTreeGrid;
import com.lbs.test.scripting.JLbsTestPlayerWrapper;
import com.lbs.util.JLbsOpenWindowListing;

/**
 * @author Ahmet.Izgi
 * 
 */
public class CommonBsh {

	/**/
	JLbsTestPlayerWrapper TPW;
	/**/
	private String version;
	// private String headerBase = "BASE.BSH";
	// private String headerGetSnapshot = "GETSNAPSHOT.BSH";
	private ScriptService su;
	private TedamLogLevel logLevelInfo;
	private TedamLogLevel logLevelWarn;
	private TedamLogLevel logLevelError;
	private Boolean printLog;
	private ArrayList messageList;
	private String formName;
	private boolean isLookup;
	private Element formElement;
	private boolean passCond;
	private boolean sleepCond;
	private long start;
	private boolean isTestStep;
	private int messageDialogSleepTime;
	private boolean continueOnError;
	private String sourceOperation;
	private List reportList;
	private String testCaseId = "2310";
	private int expandedRootConfig = 0;
	private int synchronizeFormsWaitTime;

	private String projectFile = "";
	private long buttonExistWaitMillis = Long.valueOf(PropUtils.getProperty("bsh.buttonClick.buttonExistWaitMillis").trim()).longValue();
	private long buttonExistSleepMillis = Long.valueOf(PropUtils.getProperty("bsh.buttonClick.buttonExistSleepMillis").trim()).longValue();
	private long waitForPendingFormWaitTime = Long.valueOf(PropUtils.getProperty("bsh.waitforpendingform.waitmillis").trim()).longValue();
	private long snapshotWaitMillis = Long.valueOf(PropUtils.getProperty("bsh.verify.snapshotwaitmillis").trim()).longValue();
	private long snapshotSleepMillis = Long.valueOf(PropUtils.getProperty("bsh.verify.snapshotsleepmillis").trim()).longValue();
	private String tysMachine = String.valueOf(PropUtils.getProperty("tysMachine").trim());
	// BaseBsh generator path

	public void buttonClick(JLbsScriptContainer form, Integer buttonTag, Integer menuButtonItemNo, List mdList) {
		String headerButtonClick = "BUTTONCLICKNG.BSH";
		// The console will start printing step warning
		su.log(headerButtonClick, "STEP STARTED", logLevelInfo, printLog);
		su.log(headerButtonClick, "form :" + form + " buttonTag :" + " menuButtonItemNo:" + menuButtonItemNo + " mdList :" + mdList, logLevelInfo, printLog);
		su.log(headerButtonClick, Constants.BSH_MESSAGE_DASHSIGN + Constants.OPERATION_BUTTONCLICK_BSH + Constants.BSH_MESSAGE_DASHSIGN, logLevelInfo, printLog);
		su.log(headerButtonClick, "isLookup :" + isLookup, logLevelInfo, printLog);
		// Step report description
		TestReport bshtr = new TestReport(Constants.OPERATION_BUTTONCLICK_BSH, formName, isLookup);
		su.log(headerButtonClick, "Step report defined.", logLevelInfo, printLog);
		// In the next step Dialog the REPORT object is defined here
		TestReport messageDialogbshtr = new TestReport(Constants.OPERATION_MESSAGEDIALOG_BSH, formName, isLookup);
		su.log(headerButtonClick, "The step dialog is the REPORT object defined here", logLevelInfo, printLog);
		messageList = new ArrayList();
		passCond = true;
		try {
			// control buttonTag in formElement control

			boolean isTimeOver = false; // time limit control
			boolean isButtonExist = false; // matching control of values
			long startTime = System.currentTimeMillis(); // start time
			int methodCallCount = 0;
			su.log(headerButtonClick, buttonExistWaitMillis + " during milliseconds, " + buttonExistSleepMillis
					+ " a snapshot is taken in milliseconds and the su.buttonExists () procedure is called. .", logLevelInfo, printLog);

			while (!(isButtonExist || isTimeOver)) {
				/* $I(../FunctionalScripts/GetSnapshot.bsh); */
				methodCallCount++;
				su.log(headerButtonClick, methodCallCount + ". Snapshot was received.", logLevelInfo, printLog);

				isTimeOver = (System.currentTimeMillis() - startTime) > buttonExistWaitMillis;
				isButtonExist = su.buttonExists(formElement, buttonTag);
				su.log(headerButtonClick, methodCallCount + ". times the Su.ButtonExists () procedure was invoked.", logLevelInfo, printLog);

				// added control to prevent waiting in the last positive step
				if (!(isButtonExist || isTimeOver)) {
					Thread.sleep(buttonExistSleepMillis);
					su.log(headerButtonClick, buttonExistSleepMillis + " waited for milliseconds...", logLevelInfo, printLog);
				}

			}
			if (!isButtonExist) {
				su.log(headerButtonClick, buttonTag + " snapshot not found in the tag button.!!!", logLevelError, printLog);
				throw new Exception();
			} else {
				su.log(headerButtonClick, buttonTag + " tag button found in snapshot...", logLevelInfo, printLog);
			}

			su.log(headerButtonClick, "Is a MenuButton or a standardButton...", logLevelInfo, printLog);
			if (menuButtonItemNo != null) {
				su.log(headerButtonClick, "Button type menu button. MenuButtonItemNo arrived", logLevelInfo, printLog);
				// If Button type is menuButton.
				JLbsScriptMenuButton menuButton = (JLbsScriptMenuButton) form.getScriptMenuButtonByTag(buttonTag.intValue());
				menuButton.showItems();
				menuButton.clickItem(menuButtonItemNo.intValue());
				su.log(headerButtonClick, "MenuButtonItemNo output.", logLevelInfo, printLog);
			} else {
				// If Button type is standart button.
				su.log(headerButtonClick, "MenuButtonItemNo has not arrived, standard button type", logLevelInfo, printLog);
				JLbsScriptButton button = (JLbsScriptButton) form.getScriptButtonByTag(buttonTag.intValue());
				button.select();
				button.click();
			}
			su.log(headerButtonClick, "button after exit", logLevelInfo, printLog);

			sleepCond = true;
			start = System.currentTimeMillis();
			isTestStep = false;
			if (mdList != null) {
				// If mdList is coming, then there is dialog after ButtonClick, so isTestStep is true.
				su.log(headerButtonClick, "mDList is coming !!! ButtonClick has post dialog", logLevelInfo, printLog);
				isTestStep = true;
				su.log(headerButtonClick, "isteststep becomes true.", logLevelInfo, printLog);
			}
			// Used to capture messageDialog and red message.
			while (sleepCond) {
				su.log(headerButtonClick, "sleepCond true", logLevelInfo, printLog);
				if (messageDialogSleepTime < 2000) {
					messageDialogSleepTime = 2000;
				}
				// The parameter messageDialogbshtr is given here, it is filled in the method, ButtonClick is added after the button step after
				// the report step is completed
				// When the while loop is rotating more than once, mdList = null is made in the second conversion even if mdList is full at first. (in messageDialog)
				// Except the first return we will do click and pass ? Reason ?
				su.log(headerButtonClick, "messageDialog(mdList, messageDialogbshtr, isTestStep) yordami cagiriliyor.", logLevelInfo, printLog);
				messageDialog(mdList, messageDialogbshtr, isTestStep);
				// messageDialogSleepTime = 1000; // TODO: mikyas. Why is it being repeated again for 1 second? Why do we get a config when
				// we change it?
				isTestStep = false;
				if (!continueOnError) {
					// If continueOnError is FALSE, we will enter here and red pop up if a red message is encountered.
					su.log(headerButtonClick, "continueOnError false", logLevelInfo, printLog);
					su.log(headerButtonClick, "Snapshot will be taken for 4 seconds...", logLevelInfo, printLog);
					if (System.currentTimeMillis() - start > 4000) {// TODO: mikyas. Why 4 seconds? It will enter here anytime and anywhere
						su.log(headerButtonClick, "4 saniye gectigi icin snapshot aliniyor...", logLevelInfo, printLog);
						// After 4 seconds snapshot of the form is checked for a message shown
						/* $I(../FunctionalScripts/GetSnapshot.bsh); */
						messageList = (ArrayList) su.getErrorMessages(formElement);
						su.log(headerButtonClick, "messageList size :" + messageList.size(), logLevelInfo, printLog);
						su.log(headerButtonClick, "messageList :" + messageList, logLevelInfo, printLog);
						if (messageList.size() > 0) {
							passCond = false;
							su.log(headerButtonClick, "messageList.size() is greater than zero. passCond = false set. ", logLevelInfo, printLog);
							throw new Exception();
						} else {
							sleepCond = false;
							su.log(headerButtonClick, "continueOnError = FALSE... sleepCond false", logLevelInfo, printLog);
						}
					}
				} else {
					// If continueOnError is TRUE, it will enter here and continue successfully, whether or not it is a red message
					sleepCond = false;
					su.log(headerButtonClick, "continueOnError = TRUE...sleepCond false", logLevelInfo, printLog);
				}
			}
			if (!passCond) {
				su.log(headerButtonClick, "passCond  false !!!  messageDialog got an error.", logLevelInfo, printLog);
				// If messageDialog gets an error, it will enter here. In this case, the button is added to the report before the buttonClick successful work,
				// and then the messageDialog is added.
				if (menuButtonItemNo != null) {
					su.log(headerButtonClick, "passCond  false !!!  menuButtonItemNo notnull", logLevelInfo, printLog);
					bshtr.setMessage(Constants.OPERATION_BUTTONCLICK_BSH + " " + Constants.BSH_MESSAGE_SUCCESS + " " + String.valueOf(buttonTag) + "/" + menuButtonItemNo);
				} else {
					su.log(headerButtonClick, "passCond  false !!!  menuButtonItemNo null", logLevelInfo, printLog);
					bshtr.setMessage(Constants.OPERATION_BUTTONCLICK_BSH + " " + Constants.BSH_MESSAGE_SUCCESS + " " + String.valueOf(buttonTag));
				}
				bshtr.setStatusMsg(StatusMessages.SUCCEEDED.getStatus());
				// ButtonClick is added to the report as succeeded.
				su.log(headerButtonClick, "ButtonClick is added to the report as succeeded.", logLevelInfo, printLog);
				reportList.add(bshtr);
				reportList.add(messageDialogbshtr);
			}
		} catch (Exception e) {
			su.log(headerButtonClick, "!!! Exception !!!", logLevelError, printLog);
			passCond = false;
			if (messageList.size() > 0) {
				String messageLine = "";
				for (int i = 0; i < messageList.size(); i++) {
					messageLine += messageList.get(i) + "/";
				}
				// If the message is captured, it is printed in the report.
				bshtr.addMessage(messageLine);
				su.log(headerButtonClick, " messageList.size ()> 0 messages are being printed on the captured report.", logLevelInfo, printLog);
			} else {
				// If the message is not caught
				su.log(headerButtonClick, " Message could not be caught.", logLevelInfo, printLog);
				if (menuButtonItemNo != null) {
					bshtr.addMessage(Constants.BSH_MESSAGE_BUTTONPARAMETER + String.valueOf(buttonTag) + "/" + menuButtonItemNo);
				} else {
					bshtr.addMessage(Constants.BSH_MESSAGE_BUTTONPARAMETER + String.valueOf(buttonTag));
				}
			}
			bshtr.setStatusMsg(StatusMessages.FAILED.getStatus());
			// We are adding buttonClick if the report is completely independent of the messageDialog. No need to add anything about messageDialog!
			reportList.add(bshtr);
		}
		if (passCond) {
			// buttonClick and if the messageDialog is successful
			su.log(headerButtonClick, " button click and - if any - message Dialog succeeded. ", logLevelInfo, printLog);
			if (menuButtonItemNo != null) {
				bshtr.setMessage(Constants.OPERATION_BUTTONCLICK_BSH + " " + Constants.BSH_MESSAGE_SUCCESS + " " + String.valueOf(buttonTag) + "/" + menuButtonItemNo);
			} else {
				bshtr.setMessage(Constants.OPERATION_BUTTONCLICK_BSH + " " + Constants.BSH_MESSAGE_SUCCESS + " " + String.valueOf(buttonTag));
			}
			bshtr.setStatusMsg(StatusMessages.SUCCEEDED.getStatus());
			// ButtonClick is added to the report as succeeded.
			reportList.add(bshtr);
			if (mdList != null) {
				// messageDialog is succeeded.
				su.log(headerButtonClick, " mdList not null - messageDialog is succeeded. ", logLevelInfo, printLog);
				reportList.add(messageDialogbshtr);
			}
		}

		// The console is going to print the stepped out warning.
		su.log(headerButtonClick, " STEP IS OVER ", logLevelInfo, printLog);
		if (menuButtonItemNo != null) {
			su.log(headerButtonClick, Constants.BSH_MESSAGE_DASHSIGN + Constants.OPERATION_BUTTONCLICK_BSH + Constants.BSH_MESSAGE_ENDS + Constants.BSH_MESSAGE_DASHSIGN
					+ Constants.BSH_MESSAGE_PARAMETER + String.valueOf(buttonTag) + "/" + String.valueOf(menuButtonItemNo), logLevelInfo, printLog);
		} else {
			su.log(headerButtonClick, Constants.BSH_MESSAGE_DASHSIGN + Constants.OPERATION_BUTTONCLICK_BSH + Constants.BSH_MESSAGE_ENDS + Constants.BSH_MESSAGE_DASHSIGN
					+ Constants.BSH_MESSAGE_PARAMETER + String.valueOf(buttonTag), logLevelInfo, printLog);
		}
	}

	public void filterFill(JLbsScriptContainer form, List ssvFilterFillList, Integer snapshotID) {
		String headerFilterFill = "FILTERFILLNG.BSH";
		// The console will start printing step warning.
		su.log(headerFilterFill, "STEP STARTED", logLevelInfo, printLog);
		su.log(headerFilterFill, Constants.BSH_MESSAGE_DASHSIGN + Constants.OPERATION_FILTERFILL_BSH + Constants.BSH_MESSAGE_DASHSIGN, logLevelInfo, printLog);
		su.log(headerFilterFill, "isLookup :" + isLookup, logLevelInfo, printLog);
		passCond = true;
		// Step report definition.
		TestReport bshtr = new TestReport(Constants.OPERATION_FILTERFILL_BSH, formName, isLookup);
		su.log(headerFilterFill, "Step report defined.", logLevelInfo, printLog);
		try {
			// If given snapshotDefinitionId cannot be found in SNAPSHOT_DEFINITION table
			su.log(headerFilterFill, "try start.", logLevelInfo, printLog);
			if (ssvFilterFillList == null) {
				passCond = false;
				su.log(headerFilterFill, "ssvFilterFillList is null snapshotDefinitionId not found", logLevelError, printLog);
				throw new Exception();
			}
			// su.log(headerFilterFill, "ssvFilterFillList : " + ssvFilterFillList, logLevelInfo, printLog);
			// su.log(headerFilterFill, "filterFillssValue is there", logLevelInfo, printLog);
			// SnapshotValue filterFillssValue = (SnapshotValue) ssvFilterFillList.get(0);
			// su.log(headerFilterFill, "filterFillssValue is there :" + filterFillssValue, logLevelInfo, printLog);
			// su.log(headerFilterFill, "ssValueParentTag is there", logLevelInfo, printLog);
			// int ssValueParentTag = Integer.parseInt(filterFillssValue.getParentTag());
			// su.log(headerFilterFill, "ssValueParentTag is there :" + ssValueParentTag, logLevelInfo, printLog);
			// su.log(headerFilterFill, "flgrid is there", logLevelInfo, printLog);
			// JLbsScriptFilterGrid filterGrid = (JLbsScriptFilterGrid) form.getScriptFilterGridByTag(ssValueParentTag);
			// su.log(headerFilterFill, "flgrid is there :" + filterGrid, logLevelInfo, printLog);
			Thread.sleep(1000);
			su.log(headerFilterFill, "ssvFilterFillList.size() : " + ssvFilterFillList.size(), logLevelInfo, printLog);
			for (int i = 0; i < ssvFilterFillList.size() && passCond; i++) {

				su.log(headerFilterFill, "ssvFilterFillList : " + ssvFilterFillList, logLevelInfo, printLog);
				su.log(headerFilterFill, "filterFillssValue is there", logLevelInfo, printLog);
				SnapshotValue filterFillssValue = (SnapshotValue) ssvFilterFillList.get(i);
				su.log(headerFilterFill, "filterFillssValue is there : " + filterFillssValue, logLevelInfo, printLog);
				su.log(headerFilterFill, "ssValueParentTag is there", logLevelInfo, printLog);
				int ssValueParentTag = Integer.parseInt(filterFillssValue.getParentTag());
				su.log(headerFilterFill, "ssValueParentTag is there :" + ssValueParentTag, logLevelInfo, printLog);
				su.log(headerFilterFill, "flgrid is there", logLevelInfo, printLog);
				JLbsScriptFilterGrid filterGrid = (JLbsScriptFilterGrid) form.getScriptFilterGridByTag(ssValueParentTag);
				su.log(headerFilterFill, "flgrid is there :" + filterGrid, logLevelInfo, printLog);
				if (i == 0) {
					su.log(headerFilterFill, "filterGrid.resetFilterValues () is called before the first element is filled...", logLevelInfo, printLog);
					filterGrid.resetFilterValues();
					su.log(headerFilterFill, "filterGrid.resetFilterValues () was called.", logLevelInfo, printLog);
				}
				/* $I(../FunctionalScripts/GetSnapshot.bsh); */
				List tabList = su.getFilterTabProperties(formElement, Integer.parseInt(((SnapshotValue) ssvFilterFillList.get(0)).getParentTag()));
				su.log(headerFilterFill, "tabList, su.getFilterTabProperties prosedürüyle dolduruldu.", logLevelInfo, printLog);
				for (int j = tabList.size() - 1; j >= 0; j--) {
					// I go to the related tabs until I find the filter.
					JLbsScriptTabbedPane tabbedPane = (JLbsScriptTabbedPane) form.getScriptTabbedPaneByTag(((TabbedPaneAndPageParent) tabList.get(j)).getTag());
					tabbedPane.setSelectedIndex(((TabbedPaneAndPageParent) tabList.get(j)).getValue());
				}
				int tag = Integer.parseInt(((SnapshotValue) ssvFilterFillList.get(i)).getTag());
				String value = String.valueOf(((SnapshotValue) ssvFilterFillList.get(i)).getValue());
				su.log(headerFilterFill, i + ". value :" + value, logLevelInfo, printLog);
				String type = String.valueOf(((SnapshotValue) ssvFilterFillList.get(i)).getType());
				su.log(headerFilterFill, i + ". type :" + type, logLevelInfo, printLog);
				List valueList;
				JLbsScriptGridCell gridCell;
				if (type.equals(Constants.BSH_MESSAGE_TIME) || type.equals(Constants.BSH_MESSAGE_DATE) || type.equals(Constants.BSH_MESSAGE_STRING)
						|| type.equals(Constants.BSH_MESSAGE_NUMERIC)) {
					try {
						su.log(headerFilterFill, Constants.BSH_MESSAGE_TIME + " && " + Constants.BSH_MESSAGE_DATERANGE + " && " + Constants.BSH_MESSAGE_STRING + " && "
								+ Constants.BSH_MESSAGE_NUMERIC, logLevelInfo, printLog);
						valueList = su.decomposeNonRangeValues(value);
						if (valueList.get(0) != null) {
							// especially if there is a value we are looking for.
							su.log(headerFilterFill, "filterGrid.getCellByFilterId(0, 1, " + tag + ")", logLevelInfo, printLog);
							gridCell = (JLbsScriptGridCell) filterGrid.getCellByFilterId(0, 1, tag);
							gridCell.select();
							su.log(headerFilterFill, "valueList.get(0) :" + valueList.get(0) + " -> is set....", logLevelInfo, printLog);
							gridCell.setValue(valueList.get(0));
							su.log(headerFilterFill, "valueList.get(0) :" + valueList.get(0) + " -> was set.", logLevelInfo, printLog);
						}
						if (valueList.get(1) != null) {
							// If the value to exclude is set, it is set.
							su.log(headerFilterFill, "filterGrid.getCellByFilterId(0, 3, " + tag + ")", logLevelInfo, printLog);
							gridCell = (JLbsScriptGridCell) filterGrid.getCellByFilterId(0, 3, tag);
							gridCell.select();
							su.log(headerFilterFill, "valueList.get(1) :" + valueList.get(1) + " -> is set...", logLevelInfo, printLog);
							gridCell.setValue(valueList.get(1));
							su.log(headerFilterFill, "valueList.get(1) :" + valueList.get(1) + " -> was set.", logLevelInfo, printLog);
							su.log(headerFilterFill, "If the value to exclude is set, it is set.", logLevelInfo, printLog);
						}
					} catch (Exception e) {
						bshtr.addMessage(Constants.BSH_MESSAGE_TAG + tag + Constants.BSH_MESSAGE_NONRANGEFILTERCELLS + value + Constants.BSH_MESSAGE_ASSIGNERROR);
						su.log(headerFilterFill, Constants.BSH_MESSAGE_TAG + tag + Constants.BSH_MESSAGE_NONRANGEFILTERCELLS + value + Constants.BSH_MESSAGE_ASSIGNERROR,
								logLevelError, printLog);
						bshtr.setStatusMsg(StatusMessages.FAILED.getStatus());
						reportList.add(bshtr);
						passCond = false;
						continue;
					}
				} else if (type.equals(Constants.BSH_MESSAGE_TIMERANGE) || type.equals(Constants.BSH_MESSAGE_DATERANGE) || type.equals(Constants.BSH_MESSAGE_STRINGRANGE)
						|| type.equals(Constants.BSH_MESSAGE_NUMERICRANGE)) {
					try {
						su.log(headerFilterFill, Constants.BSH_MESSAGE_TIMERANGE + " && " + Constants.BSH_MESSAGE_DATERANGE + " && " + Constants.BSH_MESSAGE_DATERANGE + " && "
								+ Constants.BSH_MESSAGE_NUMERICRANGE, logLevelInfo, printLog);
						valueList = su.decomposeRangeValues(value);
						// Index 0 of valueList: Groupped Value,
						// Index 1 of valueList: Low Value,
						// Index 2 of valueList: High Value,
						// Index 3 of valueList: Excluded Value.
						// If value is groupped, high and low cannot be entered. Its same in opposite way.
						// Excluded value can be entered independently.
						if (valueList.get(0) != null) {
							filterGrid.toggleSubGroup(0, tag);
							su.log(headerFilterFill, "filterGrid.toggleSubGroup(0, tag) - filterGrid.getCellByFilterId(0, 1, " + tag + ")", logLevelInfo, printLog);
							gridCell = (JLbsScriptGridCell) filterGrid.getCellByFilterId(0, 1, tag);
							gridCell.select();
							su.log(headerFilterFill, "valueList.get(0) :" + valueList.get(0) + " -> is set...", logLevelInfo, printLog);
							gridCell.setValue(valueList.get(0));
							su.log(headerFilterFill, "valueList.get(0) :" + valueList.get(0) + " -> was set.", logLevelInfo, printLog);
						} else {
							if (valueList.get(1) != null) {
								su.log(headerFilterFill, "filterGrid.getCellByFilterId(0, 1, " + tag + ")", logLevelInfo, printLog);
								gridCell = (JLbsScriptGridCell) filterGrid.getCellByFilterId(0, 1, tag);
								gridCell.select();
								su.log(headerFilterFill, "valueList.get(1) :" + valueList.get(1) + " -> is set...", logLevelInfo, printLog);
								gridCell.setValue(valueList.get(1));
								su.log(headerFilterFill, "valueList.get(1) :" + valueList.get(1) + " -> was set.", logLevelInfo, printLog);
							}
							if (valueList.get(2) != null) {
								su.log(headerFilterFill, "filterGrid.getCellByFilterId(0, 2, " + tag + ")", logLevelInfo, printLog);
								gridCell = (JLbsScriptGridCell) filterGrid.getCellByFilterId(0, 2, tag);
								gridCell.select();
								su.log(headerFilterFill, "valueList.get(2) :" + valueList.get(2) + " -> is set...", logLevelInfo, printLog);
								gridCell.setValue(valueList.get(2));
								su.log(headerFilterFill, "valueList.get(2) :" + valueList.get(2) + " -> was set.", logLevelInfo, printLog);
							}
						}
						if (valueList.get(3) != null) {
							su.log(headerFilterFill, "filterGrid.getCellByFilterId(0, 3, " + tag + ")", logLevelInfo, printLog);
							gridCell = (JLbsScriptGridCell) filterGrid.getCellByFilterId(0, 3, tag);
							gridCell.select();
							su.log(headerFilterFill, "valueList.get(3) :" + valueList.get(3) + " -> is set...", logLevelInfo, printLog);
							gridCell.setValue(valueList.get(3));
							su.log(headerFilterFill, "valueList.get(3) :" + valueList.get(3) + " -> was set.", logLevelInfo, printLog);
						}
					} catch (Exception e) {
						bshtr.addMessage(Constants.BSH_MESSAGE_TAG + tag + Constants.BSH_MESSAGE_RANGEFILTERCELLS + value + Constants.BSH_MESSAGE_ASSIGNERROR);
						su.log(headerFilterFill, Constants.BSH_MESSAGE_TAG + tag + Constants.BSH_MESSAGE_RANGEFILTERCELLS + value + Constants.BSH_MESSAGE_ASSIGNERROR,
								logLevelError, printLog);
						bshtr.setStatusMsg(StatusMessages.FAILED.getStatus());
						reportList.add(bshtr);
						passCond = false;
						continue;
					}
				} else if (type.equals(Constants.BSH_MESSAGE_GROUP) || type.equals(Constants.BSH_MESSAGE_SELECTION)) {
					su.log(headerFilterFill, "value :" + value, logLevelInfo, printLog);
					valueList = su.decomposeGroupAndSelectionValues(value);
					su.log(headerFilterFill, "valueList.size() :" + valueList.size(), logLevelInfo, printLog);
					for (int j = 0; j < valueList.size() && !value.isEmpty(); j++) {
						// ! value.isEmpty () was added because value may be empty. 8136 There is a value in the testcase for filterfill and
						// the value is emptyString. Only resetFilterValues will work on this count.
						int itemValue = Integer.parseInt(String.valueOf(valueList.get(j)));
						su.log(headerFilterFill, j + " . valueListelemani :" + valueList.get(j), logLevelInfo, printLog);
						su.log(headerFilterFill, "tag :" + tag, logLevelInfo, printLog);
						su.log(headerFilterFill, "filterGrid.toggleEntry(0, " + tag + " , " + itemValue + ") yapiliyor... ", logLevelInfo, printLog);
						filterGrid.toggleEntry(0, tag, itemValue);
						su.log(headerFilterFill, "filterGrid.toggleEntry set.", logLevelInfo, printLog);
					}
				}
			}
		} catch (Exception e) {
			su.log(headerFilterFill, e + "", logLevelError, printLog);
			bshtr.addMessage(Constants.BSH_MESSAGE_FILTERFILLPARAMETERERROR + String.valueOf(snapshotID));
			su.log(headerFilterFill, Constants.BSH_MESSAGE_FILTERFILLPARAMETERERROR + String.valueOf(snapshotID), logLevelError, printLog);
			bshtr.setStatusMsg(StatusMessages.FAILED.getStatus());
			reportList.add(bshtr);
			passCond = false;
		}
		if (passCond) {
			bshtr.addMessage(Constants.BSH_MESSAGE_FILTERFILLSUCCESS + String.valueOf(snapshotID));
			su.log(headerFilterFill, Constants.BSH_MESSAGE_FILTERFILLSUCCESS + String.valueOf(snapshotID), logLevelInfo, printLog);
			bshtr.setStatusMsg(StatusMessages.SUCCEEDED.getStatus());
			reportList.add(bshtr);
		}

		su.log(headerFilterFill, " STEP IS OVER ", logLevelInfo, printLog);
		su.log(headerFilterFill, Constants.BSH_MESSAGE_DASHSIGN + Constants.OPERATION_FILTERFILL_BSH + Constants.BSH_MESSAGE_ENDS + Constants.BSH_MESSAGE_DASHSIGN
				+ Constants.BSH_MESSAGE_PARAMETER + String.valueOf(snapshotID), logLevelInfo, printLog);
	}

	public void formfill(JLbsScriptContainer form, List ssvFormFillList, Integer snapshotID) throws Exception {
		String headerFormFill = "FORMFILLNG.BSH";
		// The console starts stepping to print.
		su.log(headerFormFill, "STEP BASLADI", logLevelInfo, printLog);
		su.log(headerFormFill, Constants.BSH_MESSAGE_DASHSIGN + Constants.OPERATION_FORMFILL_BSH + Constants.BSH_MESSAGE_DASHSIGN, logLevelInfo, printLog);
		su.log(headerFormFill, "isLookup :" + isLookup, logLevelInfo, printLog);
		int prevSyntaxEditTag = 0;
		int tag;
		String value;
		// Step report definition.
		TestReport bshtr = new TestReport(Constants.OPERATION_FORMFILL_BSH, formName, isLookup);
		passCond = true;

		try {
			// If given snapshotDefinitionId cannot be found in SNAPSHOT_DEFINITION table
			if (ssvFormFillList == null) {
				passCond = false;
				su.log(headerFormFill, formName + " The Value List is null.", logLevelError, printLog);
				throw new Exception();
			}
			// If No snapshotValue found with given snapshotDefinitionId
			if (ssvFormFillList.size() == 0) {
				su.log(headerFormFill, formName + " The Value List is null.", logLevelWarn, printLog);
			}
			su.log(headerFormFill, formName + " The Value List is full. List size : " + ssvFormFillList.size(), logLevelInfo, printLog);
			for (int i = 0; i < ssvFormFillList.size() && passCond; i++) {
				SnapshotValue currentSnapshotValue = (SnapshotValue) ssvFormFillList.get(i);
				su.log(headerFormFill, " currentSnapshotValue.getId : " + currentSnapshotValue.getId(), logLevelInfo, printLog);
				tag = Integer.parseInt(currentSnapshotValue.getTag());
				su.log(headerFormFill, " currentSnapshotValue.tag : " + tag, logLevelInfo, printLog);
				value = currentSnapshotValue.getValue();
				su.log(headerFormFill, " currentSnapshotValue.value : " + value, logLevelInfo, printLog);
				boolean isGrid = su.isComponentGridCell(currentSnapshotValue);
				su.log(headerFormFill, " currentSnapshotValue.isGrid : " + isGrid, logLevelInfo, printLog);
				su.log(headerFormFill, " currentSnapshotValue.isLookup : " + currentSnapshotValue.isLookUp(), logLevelInfo, printLog);

				List tabList;// TODO:The tablist is repeatedly calculated in the same parameters according to the isGrid state....
				JLbsScriptTabbedPane tabbedPane;
				List lookupParamPropList;
				if (isGrid) {
					// If the value is in the grid, the necessary parameters are calculated
					su.log(headerFormFill, "value in the grid. The necessary parameters are being calculated.", logLevelInfo, printLog);
					Integer gridTag = Integer.valueOf(currentSnapshotValue.getParentTag());
					su.log(headerFormFill, "gridTag :" + gridTag, logLevelInfo, printLog);
					JLbsScriptGrid grid = (JLbsScriptGrid) form.getScriptGridByTag(gridTag.intValue());
					su.log(headerFormFill, "grid :" + grid, logLevelInfo, printLog);
					int currentRowCount = grid.getRowCount();
					su.log(headerFormFill, "currentRowCount :" + currentRowCount, logLevelInfo, printLog);
					int columnTag = Integer.parseInt(currentSnapshotValue.getTag());
					su.log(headerFormFill, "columnTag :" + columnTag, logLevelInfo, printLog);
					int columnIndex = grid.findColumnIndex(columnTag);
					su.log(headerFormFill, "columnIndex :" + columnIndex, logLevelInfo, printLog);
					int rowIndex = currentSnapshotValue.getRowIndex();
					su.log(headerFormFill, "rowIndex :" + rowIndex, logLevelInfo, printLog);
					String gridCellValue = currentSnapshotValue.getValue();
					su.log(headerFormFill, "gridCellValue :" + gridCellValue, logLevelInfo, printLog);
					JLbsScriptGridCell gridCell = (JLbsScriptGridCell) grid.getCell(rowIndex, columnIndex);
					su.log(headerFormFill, "gridCell :" + gridCell, logLevelInfo, printLog);
					su.log(headerFormFill, "su.getGridSplitterIndex(" + currentSnapshotValue.getSnapshotDefinitionId() + ", " + String.valueOf(gridTag) + ") cagiriliyor.",
							logLevelInfo, printLog);
					int splitterIndex = su.getGridSplitterIndex(currentSnapshotValue.getSnapshotDefinitionId().intValue(), String.valueOf(gridTag));
					su.log(headerFormFill, "splitterIndex :" + splitterIndex, logLevelInfo, printLog);
					// If SnapshotValue.getType is 1, false otherwise. To be used to remove points from the split slice
					boolean isNumericCell = currentSnapshotValue.getType().equalsIgnoreCase("1");
					boolean isTimeCell = currentSnapshotValue.getType().equalsIgnoreCase("3");
					su.log(headerFormFill, "currentSnapshotValue.getType() : " + currentSnapshotValue.getType() + "  isNumericCell :" + isNumericCell, logLevelInfo, printLog);
					su.log(headerFormFill, "currentSnapshotValue.getType() : " + currentSnapshotValue.getType() + "  isTimeCell :" + isTimeCell, logLevelInfo, printLog);
					/* $I(../FunctionalScripts/GetSnapshot.bsh); */
					su.log(headerFormFill, "tabList is being calculated.", logLevelInfo, printLog);
					tabList = su.getGridTabProperties(formElement, gridTag);
					su.log(headerFormFill, "tabList calculated.", logLevelInfo, printLog);
					for (int j = tabList.size() - 1; j >= 0; j--) {
						// We find the tab with the grid and go inside.
						su.log(headerFormFill, "The tab with the grid is found and entered.", logLevelInfo, printLog);
						su.log(headerFormFill, "TabbedPane is calculated.", logLevelInfo, printLog);
						tabbedPane = (JLbsScriptTabbedPane) form.getScriptTabbedPaneByTag(((TabbedPaneAndPageParent) tabList.get(j)).getTag());
						su.log(headerFormFill, "TabbedPane was calculated.", logLevelInfo, printLog);
						tabbedPane.setSelectedIndex(((TabbedPaneAndPageParent) tabList.get(j)).getValue());
					}
					if (splitterIndex > -1) {
						su.log(headerFormFill, "The dataGrid has a splitter.", logLevelInfo, printLog);
						// If you have a snapshot of the splitter grid, we have included the grid currentSplinterIndex in the document.
						su.log(headerFormFill, "currentSplinterIndex is being calculated. formElement :" + formElement + "gridTag :" + String.valueOf(gridTag), logLevelInfo,
								printLog);
						int currentSplinterIndex = su.getCurrentGridSplitter(formElement, String.valueOf(gridTag));
						if (currentSplinterIndex == Constants.VALUE_NULL_INTEGER) { //
							throw new Exception("currentSplitterIndex is null");
						}
						su.log(headerFormFill, "currentSplinterIndex was calculated.", logLevelInfo, printLog);
						boolean up;
						if (rowIndex <= splitterIndex) {
							// if our value rowIndex is less than the splitterIndex in the property loaded into database
							su.log(headerFormFill, "rowIndex is not greater than the splitterIndex in the property loaded into the database.", logLevelInfo, printLog);
							up = true;
						} else {
							// if the value of rowIndex is not less than the splitterIndex in the property loaded into the database
							su.log(headerFormFill, "rowIndex is greater than the splitterIndex in the property loaded into the database.", logLevelInfo, printLog);
							up = false;
						}
						if (rowIndex != 0 && up) {
							// The reason for doing rowIndex! = 0 is to check if the value is on top of the splitter
							// and if the row is not 0.
							su.log(headerFormFill, "the value is at the top of the splitter and the line is not 0.", logLevelInfo, printLog);
							su.log(headerFormFill, "The splitterIndex will be checked to add new rows on top of it.", logLevelInfo, printLog);
							if (currentSplinterIndex < rowIndex) {
								// we add a new line that can be joined with the top IF && If our currentSplinterIndex is less than rowIndex,
								// we add a line to the splitter.
								su.log(headerFormFill, "The currentSplinterIndex is added to the top of the splitter smaller than rowIndex.", logLevelInfo, printLog);
								grid.insertRow(currentSplinterIndex, false);
								su.log(headerFormFill, "Our currentSplinterIndex was added to the row splitter above rowIndex.", logLevelInfo, printLog);
							}
						} else if (rowIndex != 0 && !up) {
							// If rowIndex is different from 0 and is not up, we need to add a row below the splitterIndex.
							su.log(headerFormFill, "the value is not at the top of the splitter and the line is not 0.", logLevelInfo, printLog);
							su.log(headerFormFill, "New splitter insertion control under splitterIndex.", logLevelInfo, printLog);
							if (rowIndex - currentSplinterIndex != 1 && currentRowCount == rowIndex) {
								su.log(headerFormFill, "A new row is added below the splitterIndex.", logLevelInfo, printLog);
								// in case we want to add line
								grid.insertRow(currentRowCount - 1, false);
								su.log(headerFormFill, "A new row has been added below the splitterIndex.", logLevelInfo, printLog);
							}
						}
					} else {
						su.log(headerFormFill, "No dataGrid splitter.", logLevelInfo, printLog);
						// If there is no splitter on the current DataGrid
						if (currentRowCount == 0) {
							su.log(headerFormFill, "If there are no rows, rows are added on the grid.", logLevelInfo, printLog);
							// If there are no rows, we add rows in the grids
							grid.insertRow(0, true);
							su.log(headerFormFill, "If there are no rows, a row is added to the grid.", logLevelInfo, printLog);
						} else if (rowIndex >= currentRowCount) {
							su.log(headerFormFill, "the line is added up to the first line in the sequence.", logLevelInfo, printLog);
							// For example, if the eighth line is initially entered for the order, eight lines will be added
							while (currentRowCount - 1 != rowIndex) {
								grid.insertRow(currentRowCount - 1, false);
								currentRowCount = grid.getRowCount();
								su.log(headerFormFill, "currentRowCount :" + currentRowCount, logLevelInfo, printLog);
							}
							su.log(headerFormFill, "The first line was added to the line that was entered.", logLevelInfo, printLog);
						}
					}
					// The cell object to be filled is being handled.
					su.log(headerFormFill, "The cell object to be filled is being handled.", logLevelInfo, printLog);
					gridCell = (JLbsScriptGridCell) grid.getCellByColId(rowIndex, columnIndex, columnTag);
					if (currentSnapshotValue.isLookUp()
							&& (currentSnapshotValue.getLookUpParameter() != null && !currentSnapshotValue.getLookUpParameter().equals(Constants.VALUE_NULL))) {
						// If lookUp is there it goes here.
						su.log(headerFormFill, "there is lookUp. LookUp is opening.", logLevelInfo, printLog);
						gridCell.select();
						// It opens lookUp with this command.
						gridCell.lookup();
						su.log(headerFormFill, "there is lookUp. LookUp is opening.", logLevelInfo, printLog);
						su.log(headerFormFill,
								"su.getLookupParamProp(currentSnapshotValue.getLookUpParameter() :" + currentSnapshotValue.getLookUpParameter() + ") the procedure is called.",
								logLevelInfo, printLog);
						lookupParamPropList = su.getLookupParamProp(currentSnapshotValue.getLookUpParameter());
						su.log(headerFormFill, "su.getLookupParamProp(currentSnapshotValue.getLookUpParameter() :" + currentSnapshotValue.getLookUpParameter()
								+ ") the procedure is called. lookupParamPropList :" + lookupParamPropList, logLevelInfo, printLog);
						su.log(headerFormFill, "tedamLookUp the procedure is called..", logLevelInfo, printLog);
						tedamLookUp(lookupParamPropList);
						// TODO: The tedamLookup () routine should be done as a boolean. If it is False, then FormFill must also be terminated.
						if (!passCond) { // If passCond is false, the procedure must be terminated.
							return;
						}
						su.log(headerFormFill, "The tedamLookUp procedure was called. It's over.", logLevelInfo, printLog);
						form.activate();
					} else {
						// If there is no LookUp command is given in snapshotValue, value
						su.log(headerFormFill, "No lookUp.", logLevelInfo, printLog);
						gridCell.select();
						// if you remove numeric separator in numericTextEdit.
						if (isTimeCell) {
							su.log(headerFormFill, "su.getFormattedDateStringAsString(gridCellValue = " + gridCellValue + " ) procedure is called.", logLevelInfo, printLog);
							String timeValue = su.getFormattedDateStringAsString(gridCellValue);
							su.log(headerFormFill, "su.getFormattedDateStringAsString(gridCellValue) procedure was called. timeValue = " + timeValue, logLevelInfo, printLog);
							su.log(headerFormFill, "The timeValue is set to gridCell.", logLevelInfo, printLog);
							gridCell.setValue(timeValue);
						} else if (isNumericCell) {
							su.log(headerFormFill, "Since gridCell is numeric, replace is done and set.", logLevelInfo, printLog);
							gridCell.setValue(gridCellValue.replace(".", ""));
						} else {
							su.log(headerFormFill, "gridCell is set to the standard gridCellValue.", logLevelInfo, printLog);
							gridCell.setValue(gridCellValue);
						}
					}
					// When gridCell is set, deselect is done to lose focus.
					su.log(headerFormFill, "gridCell set edildi. focus'u kaybetmek icin deselect yapiliyor.", logLevelInfo, printLog);
					grid.deselect();
				} else {
					su.log(headerFormFill, "value was not found in the grid. Normal non-grid controls.", logLevelInfo, printLog);
					// Normal non-grid controls.
					/* $I(../FunctionalScripts/GetSnapshot.bsh); */
					boolean isControlEnabled = false;
					isControlEnabled = su.isControlEnabled(formElement, tag);
					su.log(headerFormFill, "isControlEnabled :" + isControlEnabled, logLevelInfo, printLog);
					if (!isControlEnabled) {
						continue;
					}
					// The tabs in our control area are clicked from the outside to the inside.
					su.log(headerFormFill, "The tabs in our control area are clicked from the outside to the inside.", logLevelInfo, printLog);
					su.log(headerFormFill, "tabList su.getControlTabProperties is filled in by the procedure. formElement :" + formElement + " tag :" + tag, logLevelInfo,
							printLog);
					tabList = su.getControlTabProperties(formElement, tag);
					su.log(headerFormFill, "tabList su.getControlTabProperties is filled in by the procedure.", logLevelInfo, printLog);
					for (int j = tabList.size() - 1; j > -1; j--) {
						// Opens the tab given value
						tabbedPane = (JLbsScriptTabbedPane) form.getScriptTabbedPaneByTag(((TabbedPaneAndPageParent) tabList.get(j)).getTag());
						tabbedPane.setSelectedIndex(((TabbedPaneAndPageParent) tabList.get(j)).getValue());
					}
					su.log(headerFormFill, "TabbedPane was opened with the given value.", logLevelInfo, printLog);
					su.log(headerFormFill, "Getting type of ssvFormFillListin.", logLevelInfo, printLog);

					// LookUp with textEdit or TextEdit
					if (currentSnapshotValue.getType().equalsIgnoreCase("158") || currentSnapshotValue.getType().equalsIgnoreCase("102")) {
						su.log(headerFormFill, "ssvFormFillListin type TextEdit or TextEdit with LookUp.", logLevelInfo, printLog);
						if (!currentSnapshotValue.isLookUp()) {
							su.log(headerFormFill, "If it's not LookUp.", logLevelInfo, printLog);
							// If it's not LookUp.
							try {
								su.log(headerFormFill, "textEdit is calculating.", logLevelInfo, printLog);
								JLbsScriptTextEdit textEdit = (JLbsScriptTextEdit) form.getScriptTextEditByTag(tag);
								su.log(headerFormFill, "textEdit is calculated.", logLevelInfo, printLog);
								textEdit.select();
								textEdit.setValue(value);
								textEdit.deselect();
								su.log(headerFormFill, "textEdit deselect was done.", logLevelInfo, printLog);
							} catch (Exception e) {
								su.log(headerFormFill, e + "", logLevelError, printLog);
								bshtr.addMessage(Constants.BSH_MESSAGE_TAG + tag + Constants.BSH_MESSAGE_TEXTEDITFORMFILL + value + Constants.BSH_MESSAGE_ASSIGNERROR);
								su.log(headerFormFill, Constants.BSH_MESSAGE_TAG + tag + Constants.BSH_MESSAGE_TEXTEDITFORMFILL + value + Constants.BSH_MESSAGE_ASSIGNERROR,
										logLevelError, printLog);
								bshtr.setStatusMsg(StatusMessages.FAILED.getStatus());
								reportList.add(bshtr);
								continue;
							}
						} else if (currentSnapshotValue.isLookUp()
								&& (currentSnapshotValue.getLookUpParameter() == null || currentSnapshotValue.getLookUpParameter().equals(Constants.VALUE_NULL))) {
							su.log(headerFormFill, "If it is LookUp but the data is entered directly (LookUp parameter is empty).", logLevelInfo, printLog);
							// If it is LookUp but the data is entered directly (LookUp parameter is empty).
							try {
								su.log(headerFormFill, "ComboEdit is being calculated.", logLevelInfo, printLog);
								JLbsScriptComboEdit comboEdit = (JLbsScriptComboEdit) form.getScriptComboEditByTag(tag);
								su.log(headerFormFill, "ComboEdit was calculated.", logLevelInfo, printLog);
								comboEdit.select();
								comboEdit.setValue(value);
								comboEdit.deselect();
								su.log(headerFormFill, "comboEdit deselect was done.", logLevelInfo, printLog);
							} catch (Exception e) {
								su.log(headerFormFill, e + "", logLevelError, printLog);
								bshtr.addMessage(Constants.BSH_MESSAGE_TAG + tag + Constants.BSH_MESSAGE_COMBOEDITFORMFILL + value + Constants.BSH_MESSAGE_ASSIGNERROR);
								su.log(headerFormFill, Constants.BSH_MESSAGE_TAG + tag + Constants.BSH_MESSAGE_COMBOEDITFORMFILL + value + Constants.BSH_MESSAGE_ASSIGNERROR,
										logLevelError, printLog);
								bshtr.setStatusMsg(StatusMessages.FAILED.getStatus());
								reportList.add(bshtr);
								continue;
							}
						} else if (currentSnapshotValue.isLookUp()
								&& (currentSnapshotValue.getLookUpParameter() != null && !currentSnapshotValue.getLookUpParameter().equals(Constants.VALUE_NULL))) {
							su.log(headerFormFill, "If LookUp is and LookUpParameter is full", logLevelInfo, printLog);
							// If LookUp is and LookUpParameter is full
							try {
								su.log(headerFormFill, "comboEdit is being calculated..", logLevelInfo, printLog);
								JLbsScriptComboEdit comboEdit = (JLbsScriptComboEdit) form.getScriptComboEditByTag(tag);
								su.log(headerFormFill, "ComboEdit was calculated.", logLevelInfo, printLog);
								comboEdit.select();
								comboEdit.lookup();
								su.log(headerFormFill, "currentSnapshotValue.getLookUpParameter() :" + currentSnapshotValue.getLookUpParameter(), logLevelInfo, printLog);
								lookupParamPropList = su.getLookupParamProp(currentSnapshotValue.getLookUpParameter());
								su.log(headerFormFill,
										"The lookupParamPropList was calculated using the su.getLookupParamProp procedure. lookupParamPropList :" + lookupParamPropList,
										logLevelInfo, printLog);
								su.log(headerFormFill, "tedamLookup procedure is called.", logLevelInfo, printLog);
								tedamLookUp(lookupParamPropList);
								// TODO: The tedamLookup () routine should be done as a boolean. If it is False, then FormFill must also be terminated.
								if (!passCond) { // If passCond is false, the procedure must end
									return;
								}
								su.log(headerFormFill, "tedamLookUp procedure was called.", logLevelInfo, printLog);
								form.activate();
							} catch (Exception e) {
								su.log(headerFormFill, e + "", logLevelError, printLog);
								bshtr.addMessage(Constants.BSH_MESSAGE_TAG + tag + Constants.BSH_MESSAGE_LOOKUPCOMBOEDITFORMFILL + Constants.BSH_MESSAGE_ASSIGNERROR);
								su.log(headerFormFill, Constants.BSH_MESSAGE_TAG + tag + Constants.BSH_MESSAGE_LOOKUPCOMBOEDITFORMFILL + Constants.BSH_MESSAGE_ASSIGNERROR,
										logLevelError, printLog);
								bshtr.setStatusMsg(StatusMessages.FAILED.getStatus());
								reportList.add(bshtr);
								continue;
							}
						}
					}
					// DateEdit // the second is dateEdit in the system settings.
					else if (currentSnapshotValue.getType().equalsIgnoreCase("116") || currentSnapshotValue.getType().equalsIgnoreCase("104")) {
						su.log(headerFormFill, "Type of ssvFormFillList DateEdit", logLevelInfo, printLog);
						try {
							su.log(headerFormFill, "DateEdit is calculating.", logLevelInfo, printLog);
							JLbsScriptDateEdit dateEdit = (JLbsScriptDateEdit) form.getScriptDateEditByTag(tag);
							su.log(headerFormFill, "DateEdit was calculated.", logLevelInfo, printLog);
							dateEdit.select();
							// dateEdit.setDate(value);
							dateEdit.setValue(value);
							dateEdit.deselect();
							su.log(headerFormFill, "DateEdit deselect was done.", logLevelInfo, printLog);
						} catch (Exception e) {
							su.log(headerFormFill, e + "", logLevelError, printLog);
							bshtr.addMessage(Constants.BSH_MESSAGE_TAG + tag + Constants.BSH_MESSAGE_DATEEDITFORMFILL + value + Constants.BSH_MESSAGE_ASSIGNERROR);
							su.log(headerFormFill, Constants.BSH_MESSAGE_TAG + tag + Constants.BSH_MESSAGE_DATEEDITFORMFILL + value + Constants.BSH_MESSAGE_ASSIGNERROR,
									logLevelError, printLog);
							bshtr.setStatusMsg(StatusMessages.FAILED.getStatus());
							reportList.add(bshtr);
							continue;
						}

					}
					// ComboBox
					else if (currentSnapshotValue.getType().equalsIgnoreCase("106")) {
						su.log(headerFormFill, "Type of ssvFormFillList ComboBox", logLevelInfo, printLog);
						try {
							su.log(headerFormFill, "ComboBox is calculating.", logLevelInfo, printLog);
							JLbsScriptComboBox comboBox = (JLbsScriptComboBox) form.getScriptComboBoxByTag(tag);
							su.log(headerFormFill, "ComboBox was calculated.", logLevelInfo, printLog);
							comboBox.setString(value);
							comboBox.deselect();
							su.log(headerFormFill, "ComboBox deselect was done.", logLevelInfo, printLog);
						} catch (Exception e) {
							su.log(headerFormFill, e + "", logLevelError, printLog);
							bshtr.addMessage(Constants.BSH_MESSAGE_TAG + tag + Constants.BSH_MESSAGE_COMBOBOXFORMFILL + value + Constants.BSH_MESSAGE_ASSIGNERROR);
							su.log(headerFormFill, Constants.BSH_MESSAGE_TAG + tag + Constants.BSH_MESSAGE_COMBOBOXFORMFILL + value + Constants.BSH_MESSAGE_ASSIGNERROR,
									logLevelError, printLog);
							bshtr.setStatusMsg(StatusMessages.FAILED.getStatus());
							reportList.add(bshtr);
							continue;
						}

					}
					// CheckBox
					else if (currentSnapshotValue.getType().equalsIgnoreCase("107")) {
						su.log(headerFormFill, "Type of ssvFormFillList CheckBox", logLevelInfo, printLog);
						try {
							value = su.getCorrectValueForCheckBox(value);
							su.log(headerFormFill, "CheckBox is calculating.", logLevelInfo, printLog);
							JLbsScriptCheckBox checkBox = (JLbsScriptCheckBox) form.getScriptCheckBoxByTag(tag);
							su.log(headerFormFill, "ComboBox was calculated.", logLevelInfo, printLog);
							checkBox.select();
							checkBox.setSelected(value);
							checkBox.deselect();
							su.log(headerFormFill, "ComboBox deselect was done.", logLevelInfo, printLog);
						} catch (Exception e) {
							su.log(headerFormFill, e + "", logLevelError, printLog);
							bshtr.addMessage(Constants.BSH_MESSAGE_TAG + tag + Constants.BSH_MESSAGE_CHECKBOXFORMFILL + value + Constants.BSH_MESSAGE_ASSIGNERROR);
							su.log(headerFormFill, Constants.BSH_MESSAGE_TAG + tag + Constants.BSH_MESSAGE_CHECKBOXFORMFILL + value + Constants.BSH_MESSAGE_ASSIGNERROR,
									logLevelError, printLog);
							bshtr.setStatusMsg(StatusMessages.FAILED.getStatus());
							reportList.add(bshtr);
							continue;
						}

					}
					// TextArea
					else if (currentSnapshotValue.getType().equalsIgnoreCase("113")) {
						su.log(headerFormFill, "Type of ssvFormFillList TextArea", logLevelInfo, printLog);
						try {
							su.log(headerFormFill, "textArea is calculating.", logLevelInfo, printLog);
							JLbsScriptTextArea textArea = (JLbsScriptTextArea) form.getScriptTextAreaByTag(tag);
							su.log(headerFormFill, "textArea was calculated.", logLevelInfo, printLog);
							textArea.select();
							textArea.setText(value);
							textArea.deselect();
							su.log(headerFormFill, "textArea deselect was done.", logLevelInfo, printLog);
						} catch (Exception e) {
							su.log(headerFormFill, e + "", logLevelError, printLog);
							bshtr.addMessage(Constants.BSH_MESSAGE_TAG + tag + Constants.BSH_MESSAGE_TEXTAREAFORMFILL + value + Constants.BSH_MESSAGE_ASSIGNERROR);
							su.log(headerFormFill, Constants.BSH_MESSAGE_TAG + tag + Constants.BSH_MESSAGE_TEXTAREAFORMFILL + value + Constants.BSH_MESSAGE_ASSIGNERROR,
									logLevelError, printLog);
							bshtr.setStatusMsg(StatusMessages.FAILED.getStatus());
							reportList.add(bshtr);
							continue;
						}

					}
					// NumericEditWithCalculator
					else if (currentSnapshotValue.getType().equalsIgnoreCase("117")) {
						if (currentSnapshotValue.isLookUp()
								&& (currentSnapshotValue.getLookUpParameter() != null && !currentSnapshotValue.getLookUpParameter().equals(Constants.VALUE_NULL))) {
							su.log(headerFormFill, "If LookUp is and LookUpParameter is full", logLevelInfo, printLog);
							// If LookUp is and LookUpParameter is full
							try {
								su.log(headerFormFill, "comboEdit is being calculated..", logLevelInfo, printLog);
								// TODO: tag's component type is JLbsScriptNumEditWithCalc (117) but we are getting this tag with JLbsScriptComboEdit type.
								JLbsScriptComboEdit comboEdit = (JLbsScriptComboEdit) form.getScriptComboEditByTag(tag);
								su.log(headerFormFill, "ComboEdit was calculated.", logLevelInfo, printLog);
								comboEdit.select();
								comboEdit.lookup();
								su.log(headerFormFill, "currentSnapshotValue.getLookUpParameter() :" + currentSnapshotValue.getLookUpParameter(), logLevelInfo, printLog);
								lookupParamPropList = su.getLookupParamProp(currentSnapshotValue.getLookUpParameter());
								su.log(headerFormFill,
										"The lookupParamPropList was calculated using the su.getLookupParamProp procedure. lookupParamPropList :" + lookupParamPropList,
										logLevelInfo, printLog);
								su.log(headerFormFill, "tedamLookup procedure is called.", logLevelInfo, printLog);
								tedamLookUp(lookupParamPropList);
								// TODO: The tedamLookup () routine should be done as a boolean. If it is False, then FormFill must also be terminated.
								if (!passCond) { // If passCond is false, the procedure must end
									return;
								}
								su.log(headerFormFill, "tedamLookUp procedure was called.", logLevelInfo, printLog);
								form.activate();
							} catch (Exception e) {
								su.log(headerFormFill, e + "", logLevelError, printLog);
								bshtr.addMessage(Constants.BSH_MESSAGE_TAG + tag + Constants.BSH_MESSAGE_LOOKUPCOMBOEDITFORMFILL + Constants.BSH_MESSAGE_ASSIGNERROR);
								su.log(headerFormFill, Constants.BSH_MESSAGE_TAG + tag + Constants.BSH_MESSAGE_LOOKUPCOMBOEDITFORMFILL + Constants.BSH_MESSAGE_ASSIGNERROR,
										logLevelError, printLog);
								bshtr.setStatusMsg(StatusMessages.FAILED.getStatus());
								reportList.add(bshtr);
								continue;
							}
						} else {
							su.log(headerFormFill, "Type of ssvFormFillList NumericEditWithCalculator", logLevelInfo, printLog);
							try {
								su.log(headerFormFill, "NumericEditWithCalculator is calculating.", logLevelInfo, printLog);
								JLbsScriptNumEditWithCalc numEditWithCalc = (JLbsScriptNumEditWithCalc) form.getScriptNumEditWithCalcByTag(tag);
								su.log(headerFormFill, "NumericEditWithCalculator was calculated.", logLevelInfo, printLog);
								numEditWithCalc.select();
								numEditWithCalc.setNumber(value);
								numEditWithCalc.deselect();
								su.log(headerFormFill, "NumericEditWithCalculator deselect  was done.", logLevelInfo, printLog);
							} catch (Exception e) {
								su.log(headerFormFill, e + "", logLevelError, printLog);
								bshtr.addMessage(Constants.BSH_MESSAGE_TAG + tag + Constants.BSH_MESSAGE_NUMERICEDITCALCFORMFILL + value + Constants.BSH_MESSAGE_ASSIGNERROR);
								su.log(headerFormFill, Constants.BSH_MESSAGE_TAG + tag + Constants.BSH_MESSAGE_NUMERICEDITCALCFORMFILL + value + Constants.BSH_MESSAGE_ASSIGNERROR,
										logLevelError, printLog);
								bshtr.setStatusMsg(StatusMessages.FAILED.getStatus());
								reportList.add(bshtr);
								continue;
							}
						}
					}
					// Checkboxgroup
					else if (currentSnapshotValue.getType().equalsIgnoreCase("151")) {

						su.log(headerFormFill, "Type of ssvFormFillList Checkboxgroup", logLevelInfo, printLog);
						try {
							su.log(headerFormFill, "Checkboxgroup is calculating.", logLevelInfo, printLog);
							JLbsScriptCheckBoxGroup checkBoxGroup = (JLbsScriptCheckBoxGroup) form.getScriptCheckBoxGroupByTag(tag);
							su.log(headerFormFill, "Checkboxgroup was calculated.", logLevelInfo, printLog);
							checkBoxGroup.select();
							checkBoxGroup.uncheckedAll();
							int[] selectedList = su.getMultiSelectionList(value);
							su.log(headerFormFill, "selectedList is computed with the su.getMultiSelectionList (value) procedure.", logLevelInfo, printLog);
							for (int k = 0; k < selectedList.length; k++) {
								checkBoxGroup.selectByTag(selectedList[k], "1");
							}
						} catch (Exception e) {
							su.log(headerFormFill, e + "", logLevelError, printLog);
							bshtr.addMessage(Constants.BSH_MESSAGE_TAG + tag + Constants.BSH_MESSAGE_CHECKBOCGROUPFORMFILL + value + Constants.BSH_MESSAGE_ASSIGNERROR);
							su.log(headerFormFill, Constants.BSH_MESSAGE_TAG + tag + Constants.BSH_MESSAGE_CHECKBOCGROUPFORMFILL + value + Constants.BSH_MESSAGE_ASSIGNERROR,
									logLevelError, printLog);
							bshtr.setStatusMsg(StatusMessages.FAILED.getStatus());
							reportList.add(bshtr);
							continue;
						}

					}
					// RadioButtonGroup
					else if (currentSnapshotValue.getType().equalsIgnoreCase("152")) {
						su.log(headerFormFill, "Type of ssvFormFillList RadioButtonGroup", logLevelInfo, printLog);
						try {
							su.log(headerFormFill, "RadioButtonGroup is calculating.", logLevelInfo, printLog);
							JLbsScriptRadioButtonGroup radioButtonGroup = (JLbsScriptRadioButtonGroup) form.getScriptRadioButtonGroupByTag(tag);
							su.log(headerFormFill, "RadioButtonGroup was calculated.", logLevelInfo, printLog);
							radioButtonGroup.select();
							radioButtonGroup.selectByTag(Integer.parseInt(value), "1");
							radioButtonGroup.deselect();
							su.log(headerFormFill, "RadioButtonGroup deselect was done.", logLevelInfo, printLog);
						} catch (Exception e) {
							su.log(headerFormFill, e + "", logLevelError, printLog);
							bshtr.addMessage(Constants.BSH_MESSAGE_TAG + tag + Constants.BSH_MESSAGE_RADIOBUTTONGROUPFORMFILL + value + Constants.BSH_MESSAGE_ASSIGNERROR);
							su.log(headerFormFill, Constants.BSH_MESSAGE_TAG + tag + Constants.BSH_MESSAGE_RADIOBUTTONGROUPFORMFILL + value + Constants.BSH_MESSAGE_ASSIGNERROR,
									logLevelError, printLog);
							bshtr.setStatusMsg(StatusMessages.FAILED.getStatus());
							reportList.add(bshtr);
							continue;
						}

					}
					// SyntaxEdit
					else if (currentSnapshotValue.getType().equalsIgnoreCase("162")) {
						su.log(headerFormFill, "Type of ssvFormFillList SyntaxEdit", logLevelInfo, printLog);
						try {
							if (prevSyntaxEditTag != tag) {
								su.log(headerFormFill, "If prevSyntaxEditTag is not the same as tag.", logLevelInfo, printLog);
								su.log(headerFormFill, "syntaxEdit is calculating.", logLevelInfo, printLog);
								JLbsScriptSyntaxEdit syntaxEdit = (JLbsScriptSyntaxEdit) form.getScriptSyntaxEditByTag(tag);
								su.log(headerFormFill, "syntaxEdit was calculated.", logLevelInfo, printLog);
								syntaxEdit.setFormula(value);
								prevSyntaxEditTag = tag;
								su.log(headerFormFill, "The same as prevSyntaxEditTag tag.", logLevelInfo, printLog);
							} else {
								su.log(headerFormFill, "If prevSyntaxEditTag is the same as tag.", logLevelInfo, printLog);
								su.log(headerFormFill, "syntaxEdit is calculating.", logLevelInfo, printLog);
								JLbsScriptSyntaxEdit syntaxEdit = (JLbsScriptSyntaxEdit) form.getScriptSyntaxEditByTag(tag);
								su.log(headerFormFill, "syntaxEdit was calculated.", logLevelInfo, printLog);
								syntaxEdit.setFormula(value);
							}
							// syntaxEdit.deselect();
						} catch (Exception e) {
							su.log(headerFormFill, e + "", logLevelError, printLog);
							bshtr.addMessage(Constants.BSH_MESSAGE_TAG + tag + Constants.BSH_MESSAGE_SYNTAXEDITFORMFILL + value + Constants.BSH_MESSAGE_ASSIGNERROR);
							su.log(headerFormFill, Constants.BSH_MESSAGE_TAG + tag + Constants.BSH_MESSAGE_SYNTAXEDITFORMFILL + value + Constants.BSH_MESSAGE_ASSIGNERROR,
									logLevelError, printLog);
							bshtr.setStatusMsg(StatusMessages.FAILED.getStatus());
							reportList.add(bshtr);
							continue;
						}

					}
					// TimeEdit
					else if (currentSnapshotValue.getType().equalsIgnoreCase("105")) {
						su.log(headerFormFill, "Type of ssvFormFillList TimeEdit", logLevelInfo, printLog);
						try {
							su.log(headerFormFill, "timeEdit is calculating.", logLevelInfo, printLog);
							JLbsScriptComponent timeEdit = (JLbsScriptComponent) form.getScriptComponentByTag(tag, false);
							su.log(headerFormFill, "timeEdit  was calculated.", logLevelInfo, printLog);
							timeEdit.select();
							timeEdit.setValue(value);
							timeEdit.deselect();
							su.log(headerFormFill, "timeEdit deselect was done.", logLevelInfo, printLog);
						} catch (Exception e) {
							su.log(headerFormFill, e + "", logLevelError, printLog);
							bshtr.addMessage(Constants.BSH_MESSAGE_TAG + tag + Constants.BSH_MESSAGE_TIMEEDITFORMFILL + value + Constants.BSH_MESSAGE_ASSIGNERROR);
							su.log(headerFormFill, Constants.BSH_MESSAGE_TAG + tag + Constants.BSH_MESSAGE_TIMEEDITFORMFILL + value + Constants.BSH_MESSAGE_ASSIGNERROR,
									logLevelError, printLog);
							bshtr.setStatusMsg(StatusMessages.FAILED.getStatus());
							reportList.add(bshtr);
							continue;
						}

					}

				}
				// value is filled, if the dialog is later printed, it will be handled, looking for mdList.
				su.log(headerFormFill, "value is filled, if the dialog is later printed, it will be handled, looking for mdList.", logLevelInfo, printLog);
				su.log(headerFormFill, "mdList is populated with the su.messageDialogParameterParser () routine.", logLevelInfo, printLog);
				List mdList = su.messageDialogParameterParser(currentSnapshotValue.getDialogParameter());
				su.log(headerFormFill, "mdList su.messageDialogParameterParser () was populated with the procedure.", logLevelInfo, printLog);
				sourceOperation = Constants.OPERATION_FORMFILL_BSH;
				isTestStep = false;
				if (mdList != null) {
					isTestStep = true;
				}
				// if mdList is empty press-late
				su.log(headerFormFill, "if mdList is empty press-late", logLevelInfo, printLog);
				messageDialog(mdList, bshtr, isTestStep);
				isTestStep = false;
			}
		} catch (Exception e) {
			su.log(headerFormFill, e + "", logLevelError, printLog);
			bshtr.addMessage(Constants.BSH_MESSAGE_FORMFILLPARAMETERERROR + String.valueOf(snapshotID) + " - " + e.getMessage());
			su.log(headerFormFill, Constants.BSH_MESSAGE_FORMFILLPARAMETERERROR + String.valueOf(snapshotID), logLevelError, printLog);
			bshtr.setStatusMsg(StatusMessages.FAILED.getStatus());
			reportList.add(bshtr);
			passCond = false;
			su.log(headerFormFill, "FormFill exception block passCond=false", logLevelError, printLog);
			throw new Exception();
		}
		if (passCond) {
			// the report is completed successfully.
			su.log(headerFormFill, "the report is completed successfully.", logLevelInfo, printLog);
			bshtr.addMessage(Constants.OPERATION_FORMFILL_BSH + Constants.BSH_MESSAGE_SUCCESS + String.valueOf(snapshotID));
			su.log(headerFormFill, Constants.OPERATION_FORMFILL_BSH + Constants.BSH_MESSAGE_SUCCESS + String.valueOf(snapshotID), logLevelInfo, printLog);
			bshtr.setStatusMsg(StatusMessages.SUCCEEDED.getStatus());
			reportList.add(bshtr);
		}
		su.log(headerFormFill, "STEP IS OVER", logLevelInfo, printLog);
		su.log(headerFormFill, Constants.BSH_MESSAGE_DASHSIGN + Constants.OPERATION_FORMFILL_BSH + Constants.BSH_MESSAGE_ENDS + Constants.BSH_MESSAGE_DASHSIGN
				+ Constants.BSH_MESSAGE_PARAMETER + String.valueOf(snapshotID), logLevelInfo, printLog);
	}

	public void tedamLookUp(List lookupParamPropList) throws Exception {
		String formFilltedamLookUp = "FORMFILLNG.BSH - lookUp() : ";
		// If it can not find the lookup screen that needs to be opened for 10 seconds, it pulls passCond to false and triggers the end of the testCase
		// and leaves the method.
		su.log(formFilltedamLookUp, formFilltedamLookUp + " starting..." + formName, logLevelInfo, printLog);
		su.log(formFilltedamLookUp, "FORMNAME :" + formName, logLevelInfo, printLog);
		// A parameter in the BSHTR object when the report is being printed. For example, if you enter buttonClick, after this step, the report object is created
		// by setting isLookUp to true in ButtonClick.
		isLookup = true;
		su.log(formFilltedamLookUp, " isLookup was set to TRUE when printing the report. isLookup :" + isLookup, logLevelInfo, printLog);
		su.log(formFilltedamLookUp, " lookupParamPropList :" + lookupParamPropList, logLevelInfo, printLog);
		su.log(formFilltedamLookUp, " lookupParamPropList.size() :" + lookupParamPropList.size(), logLevelInfo, printLog);
		if (!((LookupParameter) lookupParamPropList.get(0)).getLookupMethod().equals((Constants.OPERATION_POPUP_BSH)) && !synchronizeForms(synchronizeFormsWaitTime)) {
			su.log(formFilltedamLookUp, " the first lookup step is not PopUp." + isLookup, logLevelInfo, printLog);
			su.log(formFilltedamLookUp,
					synchronizeFormsWaitTime
							+ " if it can not find the lookup screen that needs to be opened for milliseconds, it pulls passCond to false and triggers the end of the testCase and leaves the method.",
					logLevelInfo, printLog);
			su.log(formFilltedamLookUp, "could not synchronize." + formName, logLevelInfo, printLog);
			passCond = false;
			su.log(formFilltedamLookUp, "passCond set to false. formName :" + formName, logLevelError, printLog);
			return;
		}
		su.log(formFilltedamLookUp, "Navigating on the lookupParamPropList." + formName, logLevelInfo, printLog);
		for (int k = 0; k < lookupParamPropList.size(); k++) {
			LookupParameter currentLookUpParameter = (LookupParameter) lookupParamPropList.get(k);
			su.log(formFilltedamLookUp, (k + 1) + ". currentLookUpParameter :" + currentLookUpParameter, logLevelInfo, printLog);
			// dialog lookUp our screen.
			String lookupMethod = currentLookUpParameter.getLookupMethod();
			su.log(formFilltedamLookUp, "lookupMethod :" + lookupMethod, logLevelInfo, printLog);
			Map lookupParamMap = currentLookUpParameter.getParameters();
			su.log(formFilltedamLookUp, "lookupParamPropList " + (k + 1) + ". for element", logLevelInfo, printLog);

			JLbsScriptContainer dialog = TPW.getContainer(formName + Constants.FILE_EXTENSION_JFM, true);
			su.log(formFilltedamLookUp, "dialog.formName :" + dialog.getFormName(), logLevelInfo, printLog);
			// the first step is to popup the form that already exists. so the dialog.activate () routine will not be called.
			if (!(k == 0 && lookupMethod.equals(Constants.OPERATION_POPUP_BSH))) {
				su.log(formFilltedamLookUp, "The dialog.activate () routine is called.", logLevelInfo, printLog);
				dialog.activate();
			}
			try {
				Integer fofSSID;
				List ssvFormFillList;
				Integer ffSSID;
				List ssvFilterFillList;
				String lookupParam;
				Integer gridTag;
				HashMap gridSelectParamMap;
				HashMap paramMap;
				Integer buttonTag;
				Integer menuButtonItemNo;
				String popUpItemName;
				Integer columnTag;
				Integer rowIndex;
				if (lookupMethod.equals(Constants.OPERATION_FORMFILL_BSH)) {
					su.log(formFilltedamLookUp, " case :" + Constants.OPERATION_FORMFILL_BSH, logLevelInfo, printLog);
					su.log(formFilltedamLookUp, " fofSSID is being calculated...", logLevelInfo, printLog);
					fofSSID = lookupParamMap.get(ScriptParameters.UPLOADED_SNAPSHOT_ID) == null ? null
							: Integer.valueOf(lookupParamMap.get(ScriptParameters.UPLOADED_SNAPSHOT_ID).toString());
					su.log(formFilltedamLookUp, " fofSSID :" + fofSSID, logLevelInfo, printLog);
					ssvFormFillList = su.getSnapshotFormFillValueBOList(version, fofSSID.intValue());
					su.log(formFilltedamLookUp, " ssvFormFillList :" + ssvFormFillList, logLevelInfo, printLog);
					su.log(formFilltedamLookUp, " Calling the formfill procedure.( dialog :" + dialog + ", ssvFormFillList :" + ssvFormFillList + ", fofSSID :" + fofSSID,
							logLevelInfo, printLog);
					formfill(dialog, ssvFormFillList, fofSSID);
					su.log(formFilltedamLookUp, " The formFill () procedure has been called.", logLevelInfo, printLog);
				} else if (lookupMethod.equals(Constants.OPERATION_FILTERFILL_BSH)) {
					su.log(formFilltedamLookUp, " case :" + Constants.OPERATION_FILTERFILL_BSH, logLevelInfo, printLog);
					su.log(formFilltedamLookUp, " ffSSID is calculating...", logLevelInfo, printLog);
					ffSSID = lookupParamMap.get(ScriptParameters.UPLOADED_SNAPSHOT_ID) == null ? null
							: Integer.valueOf(lookupParamMap.get(ScriptParameters.UPLOADED_SNAPSHOT_ID).toString()); //
					su.log(formFilltedamLookUp, " ffSSID :" + ffSSID, logLevelInfo, printLog);
					ssvFilterFillList = su.getSnapshotFilterFillValueBOList(version, ffSSID.intValue());
					su.log(formFilltedamLookUp, " ssvFilterFillList :" + ssvFilterFillList, logLevelInfo, printLog);
					su.log(formFilltedamLookUp, " Calling the filterFill procedure.( dialog :" + dialog + ", ssvFilterFillList :" + ssvFilterFillList + ", ffSSID :" + ffSSID,
							logLevelInfo, printLog);
					filterFill(dialog, ssvFilterFillList, ffSSID);
					su.log(formFilltedamLookUp, " Calling the filterFill() procedure.", logLevelInfo, printLog);
				} else if (lookupMethod.equals(Constants.OPERATION_GRIDSEARCH_BSH)) {
					su.log(formFilltedamLookUp, " case :" + Constants.OPERATION_GRIDSEARCH_BSH, logLevelInfo, printLog);
					lookupParam = lookupParamMap.get(ScriptParameters.SEARCH_DETAILS) == null ? null : lookupParamMap.get(ScriptParameters.SEARCH_DETAILS).toString();
					su.log(formFilltedamLookUp, " lookupParam :" + lookupParam, logLevelInfo, printLog);
					gridTag = lookupParamMap.get(ScriptParameters.GRID_TAG) == null ? null : Integer.valueOf(lookupParamMap.get(ScriptParameters.GRID_TAG).toString());
					su.log(formFilltedamLookUp, " gridTag :" + gridTag, logLevelInfo, printLog);
					if (gridTag == null) {
						/* $I(../FunctionalScripts/GetSnapshot.bsh); */
						gridTag = su.getDataGridTag(formElement);
					}
					su.log(formFilltedamLookUp, "calling procedure. gridSearchdialog(dialog :" + dialog + ", gridTag :" + gridTag + ", lookupParam :" + lookupParam + ")",
							logLevelInfo, printLog);
					gridSearch(dialog, gridTag, lookupParam);
					su.log(formFilltedamLookUp, "The gridSearch() procedure has been called. ", logLevelInfo, printLog);
				} else if (lookupMethod.equals(Constants.OPERATION_GRIDCELLSELECT_BSH)) {
					su.log(formFilltedamLookUp, " case :" + Constants.OPERATION_GRIDCELLSELECT_BSH, logLevelInfo, printLog);
					gridSelectParamMap = new HashMap();
					gridSelectParamMap.put("gridTag", lookupParamMap.get(ScriptParameters.GRID_TAG));
					gridSelectParamMap.put("columnTag", lookupParamMap.get(ScriptParameters.COLUMN_TAG));
					gridSelectParamMap.put("rowIndex", lookupParamMap.get(ScriptParameters.ROW_INDEX));
					su.log(formFilltedamLookUp, " Calling the gridSelect procedure.( dialog :" + dialog + ", gridSelectParamMap :" + gridSelectParamMap, logLevelInfo, printLog);
					gridSelect(dialog, gridSelectParamMap);
					su.log(formFilltedamLookUp, "gridSelect() procedure has been called. ", logLevelInfo, printLog);
				} else if (lookupMethod.equals(Constants.OPERATION_DOUBLECLICK_BSH)) {
					su.log(formFilltedamLookUp, " case :" + Constants.OPERATION_DOUBLECLICK_BSH, logLevelInfo, printLog);
					gridTag = lookupParamMap.get(ScriptParameters.GRID_TAG) == null ? null : Integer.valueOf(lookupParamMap.get(ScriptParameters.GRID_TAG).toString());
					rowIndex = lookupParamMap.get(ScriptParameters.ROW_INDEX) == null ? null : Integer.valueOf(lookupParamMap.get(ScriptParameters.ROW_INDEX).toString());
					su.log(formFilltedamLookUp, "gridSearchdialog(dialog :" + dialog + ", gridTag :" + gridTag + ", rowIndex :" + rowIndex + ") procedure is called..",
							logLevelInfo, printLog);
					doubleClick(dialog, gridTag, rowIndex);
					su.log(formFilltedamLookUp, "gridSelect() procedure has been called. ", logLevelInfo, printLog);
				} else if (lookupMethod.equals(Constants.OPERATION_GRIDROWSELECT_BSH)) {
					su.log(formFilltedamLookUp, " case :" + Constants.OPERATION_GRIDROWSELECT_BSH, logLevelInfo, printLog);
					gridSelectParamMap = new HashMap();
					gridSelectParamMap.put("gridTag", lookupParamMap.get(ScriptParameters.GRID_TAG));
					gridSelectParamMap.put("rowIndexList", su
							.getRowIndexList(lookupParamMap.get(ScriptParameters.ROW_INDEX_LIST) == null ? null : lookupParamMap.get(ScriptParameters.ROW_INDEX_LIST).toString()));
					su.log(formFilltedamLookUp, " Calling the gridSelect procedure.( dialog :" + dialog + ", gridSelectParamMap :" + gridSelectParamMap, logLevelInfo, printLog);
					gridSelect(dialog, gridSelectParamMap);
					su.log(formFilltedamLookUp, "The gridSelect () procedure was called.. ", logLevelInfo, printLog);
				} else if (lookupMethod.equals(Constants.OPERATION_ROWCOUNTVERIFY_BSH)) {
					su.log(formFilltedamLookUp, " case :" + Constants.OPERATION_ROWCOUNTVERIFY_BSH, logLevelInfo, printLog);
					paramMap = new HashMap();
					paramMap.put("gridTag", lookupParamMap.get(ScriptParameters.GRID_TAG));
					paramMap.put("rowCount", lookupParamMap.get(ScriptParameters.ROW_COUNT));
					su.log(formFilltedamLookUp, " Calling the verify procedure.( dialog :" + dialog + ", paramMap :" + paramMap + "lookupMethod :" + lookupMethod, logLevelInfo,
							printLog);
					verify(dialog, paramMap, lookupMethod, false); // the last parameter is only used for OPERATION_FIELDVALUEVERIFY.
					su.log(formFilltedamLookUp, "verify() procedure was called. ", logLevelInfo, printLog);
				} else if (lookupMethod.equals(Constants.OPERATION_MESSAGEVERIFY_BSH)) {
					su.log(formFilltedamLookUp, " case :" + Constants.OPERATION_MESSAGEVERIFY_BSH, logLevelInfo, printLog);
					paramMap = new HashMap();
					paramMap.put("message", lookupParamMap.get(ScriptParameters.MESSAGE_VERIFY_PARAMETER));
					su.log(formFilltedamLookUp, " calling the verify procedure.( dialog :" + dialog + ", paramMap :" + paramMap + "lookupMethod :" + lookupMethod, logLevelInfo,
							printLog);
					verify(dialog, paramMap, lookupMethod, false); // the last parameter is only used for OPERATION_FIELDVALUEVERIFY.
					su.log(formFilltedamLookUp, "verify() procedure was called. ", logLevelInfo, printLog);
				} else if (lookupMethod.equals(Constants.OPERATION_FIELDVALUEVERIFY_BSH)) {
					su.log(formFilltedamLookUp, " case :" + Constants.OPERATION_FIELDVALUEVERIFY_BSH, logLevelInfo, printLog);
					paramMap = new HashMap();
					paramMap.put("snapshotDefinitionId", lookupParamMap.get(ScriptParameters.UPLOADED_SNAPSHOT_ID));
					String isIgnoreRowIndex = (String) lookupParamMap.get(ScriptParameters.IS_IGNORE_ROW_INDEX);
					su.log(formFilltedamLookUp, " Calling the verify procedure.( dialog :" + dialog + ", paramMap :" + paramMap + "lookupMethod :" + lookupMethod, logLevelInfo,
							printLog);
					verify(dialog, paramMap, lookupMethod, isIgnoreRowIndex.equalsIgnoreCase("0") ? false : true);
					su.log(formFilltedamLookUp, "verify() procedure was called. ", logLevelInfo, printLog);
				} else if (lookupMethod.equals(Constants.OPERATION_BUTTONCLICK_BSH)) {
					su.log(formFilltedamLookUp, " case :" + Constants.OPERATION_BUTTONCLICK_BSH, logLevelInfo, printLog);
					Object lookupParamMapGetObject = lookupParamMap.get(ScriptParameters.BUTTON_TAG);
					su.log(formFilltedamLookUp, " Constants.OPERATION_BUTTONCLICK lookupParamMapGetObject :" + lookupParamMapGetObject, logLevelInfo, printLog);
					su.log(formFilltedamLookUp, " buttonTag is being calculated...", logLevelInfo, printLog);
					buttonTag = lookupParamMap.get(ScriptParameters.BUTTON_TAG) == null ? null : Integer.valueOf(lookupParamMap.get(ScriptParameters.BUTTON_TAG).toString());
					su.log(formFilltedamLookUp, " buttonTag :" + buttonTag, logLevelInfo, printLog);
					su.log(formFilltedamLookUp, " menuButtonItemNo is being calculated....", logLevelInfo, printLog);
					menuButtonItemNo = lookupParamMap.get(ScriptParameters.MENU_BUTTON_RESOURCE_TAG) == null ? null
							: Integer.valueOf(lookupParamMap.get(ScriptParameters.MENU_BUTTON_RESOURCE_TAG).toString());
					su.log(formFilltedamLookUp, " menuButtonItemNo :" + menuButtonItemNo, logLevelInfo, printLog);
					su.log(formFilltedamLookUp, " Calling the buttonClick procedure.( dialog :" + dialog + ", buttonTag :" + buttonTag + "menuButtonItemNo :" + menuButtonItemNo,
							logLevelInfo, printLog);
					buttonClick(dialog, buttonTag, menuButtonItemNo, null);
					su.log(formFilltedamLookUp, "buttonClick() procedure was called. ", logLevelInfo, printLog);
				} else if (lookupMethod.equals(Constants.OPERATION_POPUP_BSH)) {
					su.log(formFilltedamLookUp, " case :" + Constants.OPERATION_POPUP_BSH, logLevelInfo, printLog);
					popUpItemName = lookupParamMap.get(ScriptParameters.POP_UP_TAG) == null ? null : lookupParamMap.get(ScriptParameters.POP_UP_TAG).toString();
					popUpItemName = popUpItemName.replace(Regex.SPACE.getRegex(), " ");
					su.log(formFilltedamLookUp, " popUpItemName(popUpTag) :" + popUpItemName, logLevelInfo, printLog);
					gridTag = lookupParamMap.get(ScriptParameters.GRID_TAG) == null ? null : Integer.valueOf(lookupParamMap.get(ScriptParameters.GRID_TAG).toString());
					su.log(formFilltedamLookUp, " gridTag :" + gridTag, logLevelInfo, printLog);

					su.log(formFilltedamLookUp, " columnTag is being calculated....", logLevelInfo, printLog);
					columnTag = lookupParamMap.get(ScriptParameters.COLUMN_TAG) == null ? null : Integer.valueOf(lookupParamMap.get(ScriptParameters.COLUMN_TAG).toString());
					su.log(formFilltedamLookUp, " columnTag :" + columnTag, logLevelInfo, printLog);
					su.log(formFilltedamLookUp, " rowIndex is being calculated...", logLevelInfo, printLog);
					rowIndex = lookupParamMap.get(ScriptParameters.ROW_INDEX) == null ? null : Integer.valueOf(lookupParamMap.get(ScriptParameters.ROW_INDEX).toString());
					su.log(formFilltedamLookUp, " rowIndex :" + rowIndex, logLevelInfo, printLog);
					su.log(formFilltedamLookUp, " Calling the popUp procedure.( dialog :" + dialog + ", popupParameter(popUpTag) :" + popUpItemName + "gridTag :" + gridTag
							+ "columnTag :" + columnTag + "rowIndex :" + rowIndex, logLevelInfo, printLog);

					if (k == 0) {
						su.log(formFilltedamLookUp, " the first lookup step popup...." + rowIndex, logLevelInfo, printLog);
						popUp(dialog, popUpItemName, gridTag, columnTag, rowIndex, null, true); // we call the procedure for a popup that has been opened.
																								// isAlreadyOpen = true
						// If PopUp is the first step of the lookup, it should be done at the end of the Synchronized PopUp process
						if (!synchronizeForms(synchronizeFormsWaitTime)) {
							su.log(formFilltedamLookUp,
									synchronizeFormsWaitTime
											+ " if it can not find the lookup screen that needs to be opened for milliseconds, it pulls passCond to false, triggering the end of the testCase and leaving the method.",
									logLevelInfo, printLog);
							su.log(formFilltedamLookUp, "could not synchronize." + formName, logLevelInfo, printLog);
							passCond = false;
							su.log(formFilltedamLookUp, "passCond set to false. formName :" + formName, logLevelError, printLog);
							return;
						}
					} else {
						su.log(formFilltedamLookUp, " the first lookup step is not popup!" + rowIndex, logLevelInfo, printLog);
						popUp(dialog, popUpItemName, gridTag, columnTag, rowIndex, null, false); // we call the procedure for an unopened popup.
																									// isAlreadyOpen = false

					}
					su.log(formFilltedamLookUp, "popUp() routine was called. ", logLevelInfo, printLog);
				}
				su.log(formFilltedamLookUp, "OLD FORMNAME: " + formName, logLevelInfo, printLog);
				su.log(formFilltedamLookUp, "k : " + k, logLevelInfo, printLog);
				// if (k != lookupParamPropList.size() - 1) {
				// // we do not want the last lookup to enter here
				// synchronizeForms(synchronizeFormsWaitTime);
				// }
				// su.log(formFilltedamLookUp, "AFTER FORMNAME: " + formName, logLevelInfo, printLog);
			} catch (Exception e) {
				TestReport bshtr = new TestReport(lookupMethod, lookupMethod, true);
				su.log(formFilltedamLookUp, e + "", logLevelError, printLog);
				bshtr.addMessage("LookupType: " + lookupMethod + ", lookupOrder: " + (k + 1) + ". There was an error during lookup operation.");
				su.log(formFilltedamLookUp, "LookupType: " + lookupMethod + ", lookupOrder: " + (k + 1) + ". There was an error during lookup operation.", logLevelError, printLog);
				bshtr.setStatusMsg(StatusMessages.FAILED.getStatus());
				reportList.add(bshtr);
				passCond = false;
				return;
			}
		}

		su.log(formFilltedamLookUp, "It was visited on the lookupParamPropList. FOR cycle is over..." + formName, logLevelInfo, printLog);
		su.log(formFilltedamLookUp, "Calling the synchronizeForms () procedure..." + formName, logLevelInfo, printLog);
		// If it can not find the lookup screen that needs to be opened for 10 seconds, it pulls passCond to false, triggers
		// the end of the testCase and leaves the method.
		if (!synchronizeForms(synchronizeFormsWaitTime)) {
			su.log(formFilltedamLookUp, synchronizeFormsWaitTime + " The lookup screen did not come up within milliseconds... formName :" + formName, logLevelInfo, printLog);
			passCond = false;
			su.log(formFilltedamLookUp, "passCond = false set.", logLevelInfo, printLog);
			return;
		} else {
			su.log(formFilltedamLookUp, "FOR cycle is over.The screen needed to be turned on." + formName, logLevelInfo, printLog);
		}
		su.log(formFilltedamLookUp, "AFTER FINAL FORMNAME: " + formName, logLevelInfo, printLog);
		su.log(formFilltedamLookUp, "The procedure tedamLookup () ended..." + formName, logLevelInfo, printLog);
		isLookup = false;
		su.log(formFilltedamLookUp, " At the end of the tedamLookup routine, isLookup is set to FALSE." + formName, logLevelInfo, printLog);

	}

	public void gridSearch(JLbsScriptContainer form, Integer gridStrTag, String gridSearchParameter) throws Exception {
		String headerGridSearch = "GRIDSEARCHNG.BSH";
		// The console starts stepping to print.
		su.log(headerGridSearch, "STEP STARTED.", logLevelInfo, printLog);
		su.log(headerGridSearch, Constants.BSH_MESSAGE_DASHSIGN + Constants.OPERATION_GRIDSEARCH_BSH + Constants.BSH_MESSAGE_DASHSIGN, logLevelInfo, printLog);
		su.log(headerGridSearch, "isLookup :" + isLookup, logLevelInfo, printLog);
		sourceOperation = Constants.OPERATION_GRIDSEARCH_BSH;
		su.log(headerGridSearch, "su.getGridSearchParameter(gridSearchParameter :" + gridSearchParameter + ") procedure is called..", logLevelInfo, printLog);
		List gsList = su.getGridSearchParameterList(gridSearchParameter);
		passCond = true;
		// Step report definition.
		su.log(headerGridSearch, "Step report definition.", logLevelInfo, printLog);
		TestReport bshtr = new TestReport(Constants.OPERATION_GRIDSEARCH_BSH, formName, isLookup);
		try {
			JLbsScriptGrid grid = null;
			if (gsList == null) {
				su.log(headerGridSearch, "gsList null !!!", logLevelWarn, printLog);
				su.log(headerGridSearch, "terminating gridSearch.", logLevelWarn, printLog);
				throw new Exception("gsList null...");
			}
			su.log(headerGridSearch, "gsList size :" + gsList.size(), logLevelInfo, printLog);
			for (int i = 0; i < gsList.size(); i++) {
				int tag = Integer.valueOf(((GridCell) gsList.get(i)).getTag()).intValue();
				String value = ((GridCell) gsList.get(i)).getValue();
				su.log(headerGridSearch, "gsList : " + i + ". for  -tag :" + tag + " -value :" + value, logLevelInfo, printLog);
				Integer gridTag;
				// If there is no given gridTag, returns the first apperance of DataGrid element's tag from snapshot
				if (gridStrTag == null) {
					su.log(headerGridSearch, "If gridStrTag is null, we take a snapshot and get the first gridin tag we are comparing.", logLevelInfo, printLog);
					// We take a snapshot and get the tag of the first grid we are comparing.
					/* $I(../FunctionalScripts/GetSnapshot.bsh); */
					su.log(headerGridSearch, "Calling su.getDataGridTag (formElement, tag) for gridTag.", logLevelInfo, printLog);
					gridTag = su.getDataGridTag(formElement);
				} else {
					su.log(headerGridSearch, "gridTag gridStrTag is getting the integer value.", logLevelInfo, printLog);
					gridTag = gridStrTag;
				}
				su.log(headerGridSearch, "getScriptGridByTag(gridTag :" + gridTag + ")", logLevelInfo, printLog);
				grid = (JLbsScriptGrid) form.getScriptGridByTag(gridTag.intValue());
				grid.select();
				// f4 The command to open the menu.
				su.log(headerGridSearch, "f4 The command to open the menu.", logLevelInfo, printLog);
				grid.showSearchRow();
				// f4 The command to open the menu.
				grid.keyPressed(115);
				// at this point we find the index of the column we will search.
				su.log(headerGridSearch, "at this point we find the index of the column we will search.", logLevelInfo, printLog);
				int columnIndex = grid.findColumnIndex(tag);
				// we find cell to cell precisely.
				JLbsScriptGridCell gridCell = (JLbsScriptGridCell) grid.getCellByColId(-2, columnIndex, tag);
				// value is set.
				gridCell.setValue(value);
			}
			// is done to lose focus when all the columns are filled. " if " is unnecessary, but do not lift it.
			if (gsList.size() > 0) {
				grid.applyHeaderFilters();
			}
			// the first line is selected in the case of listed entries.
			int primaryKey = grid.getPrimaryKey(0);
			if (primaryKey != -1 && primaryKey != 0) {
				grid.selectRowByKey(primaryKey, false, true);
			}
		} catch (Exception e) {
			su.log(headerGridSearch, e + "", logLevelError, printLog);
			su.log(headerGridSearch, Constants.BSH_MESSAGE_GRIDSEARCHPARAMETERERROR + gridSearchParameter, logLevelError, printLog);
			bshtr.setMessage(Constants.BSH_MESSAGE_GRIDSEARCHPARAMETERERROR + gridSearchParameter);
			bshtr.setStatusMsg(StatusMessages.FAILED.getStatus());
			reportList.add(bshtr);
			passCond = false;
		}
		if (passCond) {
			su.log(headerGridSearch, Constants.BSH_MESSAGE_GRIDSEARCHSUCCESS + gridSearchParameter, logLevelInfo, printLog);
			bshtr.addMessage(Constants.BSH_MESSAGE_GRIDSEARCHSUCCESS + gridSearchParameter);
			bshtr.setStatusMsg(StatusMessages.SUCCEEDED.getStatus());
			reportList.add(bshtr);
		}
		// The console is going to print the stepped out warning.
		su.log(headerGridSearch, "STEP FINISHED.", logLevelInfo, printLog);
		su.log(headerGridSearch, Constants.BSH_MESSAGE_DASHSIGN + Constants.OPERATION_GRIDSEARCH_BSH + Constants.BSH_MESSAGE_ENDS + Constants.BSH_MESSAGE_DASHSIGN
				+ Constants.BSH_MESSAGE_PARAMETER + gridSearchParameter, logLevelInfo, printLog);
	}

	public void gridSelect(JLbsScriptContainer form, Map gridSelectParamMap) {
		String headerGridSelect = "GRIDSELECTNG.BSH";
		// The console will start printing step warning.
		su.log(headerGridSelect, "STEP STARTED.", logLevelInfo, printLog);
		su.log(headerGridSelect, Constants.BSH_MESSAGE_DASHSIGN + sourceOperation + Constants.BSH_MESSAGE_DASHSIGN, logLevelInfo, printLog);
		su.log(headerGridSelect, "isLookup :" + isLookup, logLevelInfo, printLog);
		// Step report definition.
		TestReport bshtr = new TestReport(sourceOperation, formName, isLookup);
		su.log(headerGridSelect, "Step report defined.", logLevelInfo, printLog);
		su.log(headerGridSelect, " Calculating gridTag...", logLevelInfo, printLog);
		Integer gridTag = Integer.valueOf(gridSelectParamMap.get("gridTag").toString());
		su.log(headerGridSelect, " gridTag :" + gridTag, logLevelInfo, printLog);
		JLbsScriptGrid grid = (JLbsScriptGrid) form.getScriptGridByTag(gridTag.intValue());
		Integer rowIndex = null;
		List rowIndexList = null;
		Integer columnTag = null;
		int columnIndex = 0;
		if (gridSelectParamMap.get("columnTag") == null) {
			su.log(headerGridSelect, "GridRowSelect is being done.", logLevelInfo, printLog);
			su.log(headerGridSelect, "grid :" + grid, logLevelInfo, printLog);
			// GridRowSelect
			try {
				rowIndexList = (List) gridSelectParamMap.get("rowIndexList");
				// Row select returns all row indexes.
				su.log(headerGridSelect, "rowIndexList :" + rowIndexList, logLevelInfo, printLog);
				for (int i = 0; i < rowIndexList.size(); i++) {
					rowIndex = Integer.valueOf(rowIndexList.get(i).toString());
					su.log(headerGridSelect, i + ".rowIndex :" + rowIndex, logLevelInfo, printLog);
					// the record with the rowIndex at the beginning is taken from the primaryKey of J'guar.
					int primaryKey = grid.getPrimaryKey(rowIndex.intValue());
					su.log(headerGridSelect, i + ".rowIndexin  primaryKey :" + primaryKey, logLevelInfo, printLog);
					// The row is selected using the primary key.
					su.log(headerGridSelect, i + ".primaryKey is selected using line.", logLevelInfo, printLog);
					su.log(headerGridSelect, i + " Calling procedure grid.selectRowByKey(primaryKey, false, true) for .primaryKey.", logLevelInfo, printLog);
					grid.selectRowByKey(primaryKey, false, true);
					su.log(headerGridSelect, i + "Called the grid.selectRowByKey(primaryKey, false, true) routine for .primaryKey.", logLevelInfo, printLog);

				}
				su.log(headerGridSelect, "rowIndexList :" + rowIndexList, logLevelInfo, printLog);
			} catch (Exception e) {
				su.log(headerGridSelect, e + "", logLevelError, printLog);
				su.log(headerGridSelect, Constants.BSH_MESSAGE_GRIDCELLSELECTPARAMETERERROR + " gridTag: " + gridTag + Constants.BSH_MESSAGE_ROWINDEX + rowIndex, logLevelError,
						printLog);
				bshtr.setMessage(Constants.BSH_MESSAGE_GRIDCELLSELECTPARAMETERERROR + " gridTag: " + gridTag + Constants.BSH_MESSAGE_ROWINDEX + rowIndex);
				bshtr.setStatusMsg(StatusMessages.FAILED.getStatus());
				reportList.add(bshtr);
				passCond = false;
			}
		} else {
			su.log(headerGridSelect, "GridCellSelect is being done.", logLevelInfo, printLog);
			// GridCellSelect
			try {
				su.log(headerGridSelect, " RowIndex is being calculated....", logLevelInfo, printLog);
				rowIndex = Integer.valueOf(gridSelectParamMap.get("rowIndex").toString());
				su.log(headerGridSelect, " rowIndex :" + rowIndex, logLevelInfo, printLog);
				su.log(headerGridSelect, " columnTag is being calculated....", logLevelInfo, printLog);
				columnTag = Integer.valueOf(gridSelectParamMap.get("columnTag").toString());
				su.log(headerGridSelect, " columnTag :" + columnTag, logLevelInfo, printLog);
				columnIndex = grid.findColumnIndex(columnTag.intValue());
				JLbsScriptGridCell gridCell = (JLbsScriptGridCell) grid.getCellByColId(rowIndex.intValue(), columnIndex, columnTag.intValue());
				gridCell.select();
			} catch (Exception e) {
				su.log(headerGridSelect, e + "", logLevelError, printLog);
				su.log(headerGridSelect,
						Constants.BSH_MESSAGE_GRIDCELLSELECTPARAMETERERROR + " gridTag: " + gridTag + "columnTag: " + columnTag + Constants.BSH_MESSAGE_ROWINDEX + rowIndex,
						logLevelError, printLog);
				bshtr.setMessage(
						Constants.BSH_MESSAGE_GRIDCELLSELECTPARAMETERERROR + " gridTag: " + gridTag + "columnTag: " + columnTag + Constants.BSH_MESSAGE_ROWINDEX + rowIndex);
				bshtr.setStatusMsg(StatusMessages.FAILED.getStatus());
				reportList.add(bshtr);
				passCond = false;
			}
		}
		if (passCond) {
			su.log(headerGridSelect, "GridSelect was successful.", logLevelInfo, printLog);
			// If GridSelect succeeds
			if (gridSelectParamMap.get("columnTag") == null) {
				su.log(headerGridSelect, Constants.BSH_MESSAGE_GRIDROWSELECTSUCCESS + "gridTag: " + gridTag + Constants.BSH_MESSAGE_COLUMNINDEX + columnIndex, logLevelInfo,
						printLog);
				bshtr.setMessage(Constants.BSH_MESSAGE_GRIDROWSELECTSUCCESS + "gridTag: " + gridTag + Constants.BSH_MESSAGE_COLUMNINDEX + columnIndex);
			} else {
				su.log(headerGridSelect,
						Constants.BSH_MESSAGE_GRIDCELLSELECTSUCCESS + "gridTag: " + gridTag + " columnTag: " + columnTag + Constants.BSH_MESSAGE_COLUMNINDEX + columnIndex,
						logLevelInfo, printLog);
				bshtr.setMessage(
						Constants.BSH_MESSAGE_GRIDCELLSELECTSUCCESS + "gridTag: " + gridTag + " columnTag: " + columnTag + Constants.BSH_MESSAGE_COLUMNINDEX + columnIndex);
			}
			bshtr.setStatusMsg(StatusMessages.SUCCEEDED.getStatus());
			reportList.add(bshtr);
		}
		// The console is going to print the stepped out warning.
		su.log(headerGridSelect, "STEP FINISHED.", logLevelInfo, printLog);
		su.log(headerGridSelect, Constants.BSH_MESSAGE_DASHSIGN + sourceOperation + Constants.BSH_MESSAGE_ENDS + Constants.BSH_MESSAGE_DASHSIGN + Constants.BSH_MESSAGE_PARAMETER
				+ "   gridTag :" + gridTag + " / rowIndexList :" + rowIndexList + " / columnTag :" + columnTag, logLevelInfo, printLog);
	}

	public void verify(JLbsScriptContainer form, Map paramMap, String verifyType, boolean isIgnoreRowIndex) {
		String headerVerify = "VERIFYNG.BSH";
		// The console will start printing step warning.
		su.log(headerVerify, "STEP STARTED.", logLevelInfo, printLog);
		su.log(headerVerify, Constants.BSH_MESSAGE_DASHSIGN + Constants.OPERATION_VERIFY_BSH + Constants.BSH_MESSAGE_DASHSIGN, logLevelInfo, printLog);
		su.log(headerVerify, "isLookup :" + isLookup, logLevelInfo, printLog);

		// //////////////////////////////////////
		// @verified types:
		// 0: Failed
		// 1: Succeeded
		// 2: Caution (Continued with error)
		// //////////////////////////////////////
		int verified = 1;
		passCond = true;
		// Step report definition.
		TestReport bshtr = new TestReport(Constants.OPERATION_VERIFY_BSH, formName, isLookup);
		su.log(headerVerify, "Step report defined.", logLevelInfo, printLog);
		su.log(headerVerify, "verifyType :" + verifyType, logLevelInfo, printLog);

		try {
			/* $I(../FunctionalScripts/GetSnapshot.bsh); */
			Integer gridTag = null;
			JLbsScriptGrid grid = null;
			boolean isTimeOver = false; // time limit control.
			boolean isValuesMatches = false; // matching control of values.
			int snapshotCount = 1; // Snapshot is the step parameter.
			if (verifyType.equals(Constants.OPERATION_ROWCOUNTVERIFY_BSH)) {
				su.log(headerVerify, "case :" + Constants.OPERATION_ROWCOUNTVERIFY_BSH, logLevelInfo, printLog);
				// Grid row count verification
				long startTime = System.currentTimeMillis(); // start time.
				su.log(headerVerify, "The snapshot retrieval for rowCountVerify has begun.", logLevelInfo, printLog);
				while (!(isValuesMatches || isTimeOver)) {
					isTimeOver = (System.currentTimeMillis() - startTime) > snapshotWaitMillis;
					/* $I(../FunctionalScripts/GetSnapshot.bsh); */
					// Is gridTag information given in the parameter?
					if (paramMap.get("gridTag") == null) {
						// the first encountered GridTag on the form is retrieved.
						gridTag = su.getDataGridTag(formElement);
					} else {
						gridTag = Integer.valueOf(paramMap.get("gridTag").toString());
					}
					grid = (JLbsScriptGrid) form.getScriptGridByTag(gridTag.intValue());
					su.log(headerVerify, snapshotCount + ". Snapshot taken once. gridTag :" + gridTag + " grid : ## " + grid + " ## grid.getRowCount():" + grid.getRowCount(),
							logLevelInfo, printLog);

					// do the values match?
					isValuesMatches = grid.getRowCount() == (Integer.valueOf(paramMap.get("rowCount").toString())).intValue();
					// added control to prevent waiting in the last positive step.

					if (!(isValuesMatches || isTimeOver)) {
						su.log(headerVerify, "Sonraki snapshot alimi icin 1 sn beklenecek", logLevelInfo, printLog);
						Thread.sleep(snapshotSleepMillis);
					}
					snapshotCount++;
				}
				su.log(headerVerify, "last gridTag value :" + gridTag, logLevelInfo, printLog);
				su.log(headerVerify, "last grid value :" + grid, logLevelInfo, printLog);
				su.log(headerVerify, "last grid.getRowCount() value :" + grid.getRowCount(), logLevelInfo, printLog);
				su.log(headerVerify, "paramMap.get(rowCount) :" + paramMap.get("rowCount").toString(), logLevelInfo, printLog);

				if (!isValuesMatches && continueOnError) {
					// Given row count does not fit with row count on form and continueOnError is true
					su.log(headerVerify, "the number of rows supplied does not match the number of rows in the form and the continueOnError parameter true", logLevelInfo,
							printLog);
					verified = 2;
					bshtr.setStatusMsg(StatusMessages.CAUTION.getStatus());
					su.log(headerVerify, Constants.BSH_MESSAGE_GRIDROWVERIFYNOTEQUAL + verifyType + Constants.BSH_MESSAGE_PARAMETER + paramMap.get("rowCount"), logLevelInfo,
							printLog);
					bshtr.addMessage(Constants.BSH_MESSAGE_GRIDROWVERIFYNOTEQUAL + verifyType + Constants.BSH_MESSAGE_PARAMETER + paramMap.get("rowCount"));
				} else if (!isValuesMatches && !continueOnError) {
					// Given row count does not fit with row count on form and continueOnError is false
					su.log(headerVerify, "the number of rows supplied does not match the number of rows in the form and the continueOnError parameter false", logLevelInfo,
							printLog);
					verified = 0;
					bshtr.setStatusMsg(StatusMessages.FAILED.getStatus());
					su.log(headerVerify, Constants.BSH_MESSAGE_GRIDROWVERIFYNOTEQUAL + verifyType + Constants.BSH_MESSAGE_PARAMETER + paramMap.get("rowCount"), logLevelInfo,
							printLog);
					bshtr.addMessage(Constants.BSH_MESSAGE_GRIDROWVERIFYNOTEQUAL + verifyType + Constants.BSH_MESSAGE_PARAMETER + paramMap.get("rowCount"));
				} else if (isValuesMatches) {
					// Given row count fits with row count on form
					su.log(headerVerify, "the number of rows supplied matches the number of rows in the form.", logLevelInfo, printLog);
					verified = 1;
					bshtr.setStatusMsg(StatusMessages.SUCCEEDED.getStatus());
					su.log(headerVerify, Constants.BSH_MESSAGE_VERIFYSUCCESS + verifyType + Constants.BSH_MESSAGE_PARAMETER + paramMap.get("rowCount"), logLevelInfo, printLog);
					bshtr.addMessage(Constants.BSH_MESSAGE_VERIFYSUCCESS + verifyType + Constants.BSH_MESSAGE_PARAMETER + paramMap.get("rowCount"));
				}
			} else if (verifyType.equals(Constants.OPERATION_MESSAGEVERIFY_BSH)) {
				su.log(headerVerify, "case :" + Constants.OPERATION_MESSAGEVERIFY_BSH, logLevelInfo, printLog);
				// Message verification
				su.log(headerVerify,
						"su.doMessageExist(formElement, (String) paramMap.get(\"message\") :" + (String) paramMap.get("message") + ", continueOnError)  procedure is called.",
						logLevelInfo, printLog);

				long startTime = System.currentTimeMillis(); // start time
				su.log(headerVerify, "rowCountVerify icin snapshot alimi basladi.", logLevelInfo, printLog);
				while (!(isValuesMatches || isTimeOver)) {
					isTimeOver = (System.currentTimeMillis() - startTime) > snapshotWaitMillis;
					/* $I(../FunctionalScripts/GetSnapshot.bsh); */
					verified = su.doMessageExist(formElement, (String) paramMap.get("message"), continueOnError);
					su.log(headerVerify, snapshotCount + ". Snapshot taken once. The su.doMessageExist procedure was called. verified value :" + verified, logLevelInfo, printLog);

					// do the values match?
					isValuesMatches = verified == StatusMessages.SUCCEEDED.getStatusId();
					// added control to prevent waiting in the last positive step.

					if (!(isValuesMatches || isTimeOver)) {
						su.log(headerVerify, "The next snapshot will be waited for 1 second for reception.", logLevelInfo, printLog);
						Thread.sleep(snapshotSleepMillis);
					}
					snapshotCount++;
				}
				if (verified == StatusMessages.FAILED.getStatusId()) {
					// If message does not fit message on form and continueOnError is false
					su.log(headerVerify, "the message does not match the message on the form and the continueOnError parameter is false.", logLevelInfo, printLog);
					su.log(headerVerify, Constants.BSH_MESSAGE_VERIFYINCOMPATIBLE + verifyType + Constants.BSH_MESSAGE_PARAMETER + paramMap.get("message"), logLevelInfo, printLog);
					bshtr.addMessage(Constants.BSH_MESSAGE_VERIFYINCOMPATIBLE + verifyType + Constants.BSH_MESSAGE_PARAMETER + paramMap.get("message"));
					bshtr.setStatusMsg(StatusMessages.FAILED.getStatus());
				} else if (verified == StatusMessages.SUCCEEDED.getStatusId()) {
					// If message fits message on form
					su.log(headerVerify, "the message matches the message on the form.", logLevelInfo, printLog);
					su.log(headerVerify, Constants.BSH_MESSAGE_VERIFYSUCCESS + verifyType + Constants.BSH_MESSAGE_PARAMETER + paramMap.get("message"), logLevelInfo, printLog);
					bshtr.addMessage(Constants.BSH_MESSAGE_VERIFYSUCCESS + verifyType + Constants.BSH_MESSAGE_PARAMETER + paramMap.get("message"));
					bshtr.setStatusMsg(StatusMessages.SUCCEEDED.getStatus());
				} else if (verified == StatusMessages.CAUTION.getStatusId()) {
					// If message does not fit message on form and continueOnError is true
					su.log(headerVerify, "the message does not match the message on the form and the continueOnError parameter is true.", logLevelInfo, printLog);
					su.log(headerVerify, Constants.BSH_MESSAGE_VERIFYINCOMPATIBLE + verifyType + Constants.BSH_MESSAGE_PARAMETER + paramMap.get("message"), logLevelInfo, printLog);
					bshtr.addMessage(Constants.BSH_MESSAGE_VERIFYINCOMPATIBLE + verifyType + Constants.BSH_MESSAGE_PARAMETER + paramMap.get("message"));
					bshtr.setStatusMsg(StatusMessages.CAUTION.getStatus());
				}
			} else if (verifyType.equals(Constants.OPERATION_FIELDVALUEVERIFY_BSH)) {
				su.log(headerVerify, "case :" + Constants.OPERATION_FIELDVALUEVERIFY_BSH, logLevelInfo, printLog);
				// Field Value Verification
				su.log(headerVerify, "-------formName: # " + formName + " # ----------paramMap.get(\"snapshotDefinitionId\"): # " + paramMap.get("snapshotDefinitionId")
						+ " formElement : #" + formElement + "#" + " # -------continueOnError: # " + continueOnError + " # ------", logLevelInfo, printLog);
				// The values of the control and gridcells in the snapshot taken with the databased fields are compared
				// and the result is obtained as BSHTestReport. //

				su.log(headerVerify, "su.getFillerFieldsList () routine is called. ", logLevelInfo, printLog);
				// Go to the database once. check for returned list.
				List fillerFieldsList = su.getFillerFieldsList(Integer.valueOf(paramMap.get("snapshotDefinitionId").toString()));
				su.log(headerVerify, "su.getFillerFieldsList() routine is called. fillerFieldsList :" + fillerFieldsList, logLevelInfo, printLog);

				long startTime = System.currentTimeMillis(); // start time.
				su.log(headerVerify, "The snapshot retrieval for fieldValueVerify has begun.", logLevelInfo, printLog);
				while (!(isValuesMatches || isTimeOver)) {
					isTimeOver = (System.currentTimeMillis() - startTime) > snapshotWaitMillis;
					/* $I(../FunctionalScripts/GetSnapshot.bsh); */
					bshtr = su.validateSavedData(formElement, fillerFieldsList, formName, isLookup, isIgnoreRowIndex); // ignoreRowIndex will be added after the enhancements have
																														// been made.
					su.log(headerVerify, snapshotCount + ". once the validateSavedData routine has been called. bshtr :" + bshtr + " fields was calculated.", logLevelInfo,
							printLog);
					isValuesMatches = bshtr.getStatusMsg().equals(StatusMessages.SUCCEEDED.getStatus());
					if (!(isValuesMatches || isTimeOver)) { // added control to prevent waiting in the last positive step.
						su.log(headerVerify, "The next snapshot will wait 1 second for the reception.", logLevelInfo, printLog);
						Thread.sleep(snapshotSleepMillis);
					}
					snapshotCount++;
				}
				su.log(headerVerify, "last bshtr value: " + bshtr, logLevelInfo, printLog);
				if (bshtr.getStatusMsg().equals(StatusMessages.SUCCEEDED.getStatus())) {
					// If all snapshotValues fits with value of fields on form
					su.log(headerVerify, "all snapshotValues values match the fields on the form.", logLevelInfo, printLog);
					verified = 1;
					su.log(headerVerify, Constants.BSH_MESSAGE_VERIFYSUCCESS + verifyType + Constants.BSH_MESSAGE_PARAMETER + paramMap.get("snapshotDefinitionId"), logLevelInfo,
							printLog);
					bshtr.addMessage(Constants.BSH_MESSAGE_VERIFYSUCCESS + verifyType + Constants.BSH_MESSAGE_PARAMETER + paramMap.get("snapshotDefinitionId"));
				} else if (bshtr.getStatusMsg().equals(StatusMessages.FAILED.getStatus())) {
					// If some snapshotValues does not fit with value of fields on form and their continueOnError is false
					su.log(headerVerify, "I have a snapshotValues value that does not match the fields in the form, and their continueOnError parameter is false.", logLevelInfo,
							printLog);
					verified = 0;
					su.log(headerVerify, Constants.BSH_MESSAGE_VERIFYSUCCESS + verifyType + Constants.BSH_MESSAGE_PARAMETER + paramMap.get("snapshotDefinitionId"), logLevelInfo,
							printLog);
					bshtr.addMessage(Constants.BSH_MESSAGE_VERIFYSUCCESS + verifyType + Constants.BSH_MESSAGE_PARAMETER + paramMap.get("snapshotDefinitionId"));
				} else if (bshtr.getStatusMsg().equals(StatusMessages.CAUTION.getStatus())) {
					// If some snapshotValues does not fit with value of fields on form and their continueOnError is true
					su.log(headerVerify, "The snapshotValues values do not match the fields in the form, and their continueOnError parameter is true.", logLevelInfo, printLog);
					verified = 2;
					su.log(headerVerify, Constants.BSH_MESSAGE_VERIFYSUCCESS + verifyType + Constants.BSH_MESSAGE_PARAMETER + paramMap.get("snapshotDefinitionId"), logLevelInfo,
							printLog);
					bshtr.addMessage(Constants.BSH_MESSAGE_VERIFYSUCCESS + verifyType + Constants.BSH_MESSAGE_PARAMETER + paramMap.get("snapshotDefinitionId"));
				}
			}
		} catch (Exception e) {
			su.log(headerVerify, e + "", logLevelInfo, printLog);
			su.log(headerVerify, Constants.BSH_MESSAGE_VERIFYERROR + verifyType + Constants.BSH_MESSAGE_PARAMETER + paramMap, logLevelInfo, printLog);
			bshtr.setMessage(Constants.BSH_MESSAGE_VERIFYERROR + verifyType + Constants.BSH_MESSAGE_PARAMETER + paramMap);
			bshtr.setStatusMsg(StatusMessages.FAILED.getStatus());
			reportList.add(bshtr);
			passCond = false;
		}

		if (passCond) {
			// If the step is successful. Verify may have failed, but verify operation succeeded.
			reportList.add(bshtr);
			if (verified == 0) {
				// In the case of a failure, the passCond is pulled to false for the test case termination.
				passCond = false;
			}
		}
		// The console is going to print the stepped out warning.
		su.log(headerVerify, "STEP IS OVER.", logLevelInfo, printLog);
		su.log(headerVerify, Constants.BSH_MESSAGE_DASHSIGN + Constants.OPERATION_VERIFY_BSH + Constants.BSH_MESSAGE_ENDS + Constants.BSH_MESSAGE_DASHSIGN
				+ Constants.BSH_MESSAGE_VERIFYTYPE + verifyType + Constants.BSH_MESSAGE_PARAMETER + paramMap, logLevelInfo, printLog);
	}

	/**
	 * Not: If the popup is on the grid within a tab that is not open on the screen then we have to open it. Otherwise popup will not open.
	 */
	public void popUp(JLbsScriptContainer form, String popUpItemName, Integer gridTag, Integer columnTag, Integer rowIndex, List mdList, boolean isAlreadyOpened) {
		String headerPopUp = "POPUPNG.BSH";
		su.log(headerPopUp, "STEP STARTED.", logLevelInfo, printLog);
		su.log(headerPopUp, "form :" + form + " mdList :" + mdList, logLevelInfo, printLog);
		su.log(headerPopUp, " popUpItemName :" + popUpItemName, logLevelInfo, printLog);
		su.log(headerPopUp, " gridTag :" + gridTag + " columnTag :" + columnTag + " rowIndex :" + rowIndex, logLevelInfo, printLog);
		passCond = true;
		// Step report definition.
		TestReport bshtr = new TestReport(Constants.OPERATION_POPUP_BSH, formName, isLookup);
		// If there is a consecutive messageDialog, its report is defined.
		TestReport messageDialogbshtr = new TestReport(Constants.OPERATION_MESSAGEDIALOG_BSH, formName, isLookup);
		JLbsScriptPopUpMenu popUp = null;
		try {
			su.log(headerPopUp, "GETSNAPSHOT.BSH will be called.", logLevelInfo, printLog);
			/* $I(../FunctionalScripts/GetSnapshot.bsh); */
			su.log(headerPopUp, "su.getPopUpMenuTag(formElement :" + formElement + ") is being called...", logLevelInfo, printLog);
			int popupTag = su.getPopUpMenuTag(formElement);
			su.log(headerPopUp, "popupTag :" + popupTag, logLevelInfo, printLog);
			popUp = (JLbsScriptPopUpMenu) form.getScriptPopUpMenuByTag(popupTag);
			su.log(headerPopUp, "JLbsScriptContainer.getScriptPopUpMenuByTag() was called.", logLevelInfo, printLog);
			su.log(headerPopUp, " popUp.getItems() : " + popUp.getItems(), logLevelInfo, printLog);
			if (gridTag != null) {
				// If popUp is opened via grid or gridcell, we find the corresponding tabList that enters here.
				su.log(headerPopUp, "gridTag not null", logLevelWarn, printLog);
				List tabList = su.getGridTabProperties(formElement, gridTag);
				for (int j = tabList.size() - 1; j >= 0; j--) {
					// Click in the tabList to open the tablings, we are going to where the grid is.
					JLbsScriptTabbedPane tabbedPane = (JLbsScriptTabbedPane) form.getScriptTabbedPaneByTag(((TabbedPaneAndPageParent) tabList.get(j)).getTag());
					tabbedPane.setSelectedIndex(((TabbedPaneAndPageParent) tabList.get(j)).getValue());
					su.log(headerPopUp, "Clicking in the tabList opens the tabs, we go to where the grid is.", logLevelInfo, printLog);
				}
				su.log(headerPopUp, " If there gridTag: popUp.getItems() : " + popUp.getItems(), logLevelInfo, printLog);
				if (!isAlreadyOpened) {
					su.log(headerPopUp, "  popUp.show () is called for a popup that has not been opened.", logLevelInfo, printLog);
					if (columnTag != null && rowIndex != null) {
						su.log(headerPopUp, "gridTag notnull && rowIndex notnull", logLevelWarn, printLog);
						// If columnTag and rowIndex are given, the corresponding popUp.show () method is called.
						popUp.show(gridTag.intValue(), rowIndex.intValue(), columnTag.intValue());
						su.log(headerPopUp, "If columnTag and rowIndex are given, the corresponding popUp.show () method is called.", logLevelInfo, printLog);
					} else {
						su.log(headerPopUp, "columnTag and rowIndex not given !!!", logLevelWarn, printLog);
						// popUp.show () works with focus on the grid.
						popUp.show(gridTag.intValue());
						su.log(headerPopUp, "popUp.show () worked, focused on the grid.", logLevelInfo, printLog);
					}
				}
			} else {
				su.log(headerPopUp, "gridTag null", logLevelWarn, printLog);
				// If gridTag does not exist it opens for any location.
				if (!isAlreadyOpened) {
					su.log(headerPopUp, " The popUp.show () routine is called for an unopened popup.", logLevelInfo, printLog);
					popUp.show();
				}
				su.log(headerPopUp, "GridTag was turned open for any location.", logLevelInfo, printLog);
			}
		} catch (Exception e) {
			su.log(headerPopUp, e + "", logLevelError, printLog);
			bshtr.addMessage(Constants.BSH_MESSAGE_POPUPOPENERROR);
			su.log(headerPopUp, Constants.BSH_MESSAGE_POPUPOPENERROR, logLevelError, printLog);
			bshtr.setStatusMsg(StatusMessages.FAILED.getStatus());
			reportList.add(bshtr);
			passCond = false;
			su.log(headerPopUp, "passCond set to false. ", logLevelError, printLog);
		}
		if (passCond) {
			try {
				// hand in the popup.
				su.log(headerPopUp, "su.getPopUpMenuItemIndex(popUp.getItems().getList() :" + popUp.getItems().getList() + ", popUpItemName :" + popUpItemName, logLevelInfo,
						printLog);
				int popUpItemIndex = su.getPopUpMenuItemIndex(popUp.getItems().getList(), popUpItemName);
				su.log(headerPopUp, "popUpItemIndex :" + popUpItemIndex, logLevelInfo, printLog);
				if (popUpItemIndex == Constants.VALUE_NULL_INTEGER) {
					su.log(headerPopUp, "popUpItemIndex :" + Constants.VALUE_NULL_INTEGER, logLevelError, printLog);
					su.log(headerPopUp, popUpItemIndex + " The popTag field was not found on the list.", logLevelError, printLog);
					throw new Exception();
				}
				if (isAlreadyOpened) {
					su.log(headerPopUp, " the popUp.show () routine is called for a popup that has already been opened...", logLevelInfo, printLog);
					popUp.show();
					su.log(headerPopUp, " the popUp.show () routine was called for a popup that has already been opened.", logLevelInfo, printLog);
				}
				popUp.selectItem(popUpItemIndex);
				su.log(headerPopUp, "popUp.selectItem () was called.", logLevelInfo, printLog);

				// the messageDialog will work, if isTestStep is true, the next step in the testcase is messageDialog, if isTestStep is false.
				su.log(headerPopUp,
						"The messageDialog will work, if isTestStep is true, the next step in the testcase will be messageDialog, and if isTestStep is false it will be push-late.",
						logLevelInfo, printLog);
				isTestStep = false;
				if (mdList != null) {
					su.log(headerPopUp, "mdList notnull", logLevelWarn, printLog);
					isTestStep = true;
				}
				messageDialog(mdList, messageDialogbshtr, isTestStep);
				isTestStep = false;
				if (!passCond) {
					// If the messageDialog pops up, we add popUp successfully first.
					bshtr.addMessage(Constants.OPERATION_POPUP_BSH + " " + Constants.BSH_MESSAGE_SUCCESS + popUpItemName);
					su.log(headerPopUp, "PopUp has completed successfully...", logLevelInfo, printLog);
					su.log(headerPopUp, popUpItemName + "", logLevelInfo, printLog);
					bshtr.setStatusMsg(StatusMessages.SUCCEEDED.getStatus());
					reportList.add(bshtr);
					// We also add the report of the messageDialog.
					reportList.add(messageDialogbshtr);
				}
			} catch (Exception e) {
				bshtr.addMessage(Constants.BSH_MESSAGE_POPUPSELECTERROR + popUpItemName);
				su.log(headerPopUp, "The problem was encountered when selecting the target popup item....", logLevelError, printLog);
				su.log(headerPopUp, popUpItemName + "", logLevelError, printLog);
				bshtr.setStatusMsg(StatusMessages.FAILED.getStatus());
				reportList.add(bshtr);
				passCond = false;
			}
		}
		if (passCond) {
			bshtr.addMessage(Constants.OPERATION_POPUP_BSH + " " + Constants.BSH_MESSAGE_SUCCESS + popUpItemName);
			su.log(headerPopUp, "PopUp has completed successfully. Parameter :", logLevelInfo, printLog);
			bshtr.setStatusMsg(StatusMessages.SUCCEEDED.getStatus());
			reportList.add(bshtr);
			if (mdList != null) {
				// The next step MessageDialog is added after the report popup step.
				reportList.add(messageDialogbshtr);
			}
		}

		// The console is going to print the stepped out warning.
		su.log(headerPopUp, " STEP IS OVER ", logLevelInfo, printLog);
		su.log(headerPopUp, Constants.BSH_MESSAGE_DASHSIGN + Constants.OPERATION_POPUP_BSH + Constants.BSH_MESSAGE_ENDS + Constants.BSH_MESSAGE_DASHSIGN
				+ Constants.BSH_MESSAGE_PARAMETER + popUpItemName, logLevelInfo, printLog);

	}

	public void formOpen(String formOpenType, String menuPath) {
		String headerFormOpen = "FORMOPENNG.BSH";
		// The console will start printing step by step warning.
		su.log(headerFormOpen, Constants.BSH_MESSAGE_DASHSIGN + Constants.OPERATION_FORMOPEN_BSH + Constants.BSH_MESSAGE_DASHSIGN, logLevelInfo, printLog);
		// Step report definition.
		TestReport bshtr = new TestReport(Constants.OPERATION_FORMOPEN_BSH, "---");
		JLbsScriptTreeGrid mainMenu = (JLbsScriptTreeGrid) TPW.getMainMenuTreeGrid();

		try {
			// All open forms are closed by pressing the release key.
			su.log(headerFormOpen, "The open, all forms are closed by pressing the release key.", logLevelInfo, printLog);
			if (JLbsOpenWindowListing.getOpenDialogs().size() != 0) {
				closeAllForms();
			}
		} catch (Exception e) {
			su.log(headerFormOpen, e + "", logLevelError, printLog);
			su.log(headerFormOpen, Constants.BSH_MESSAGE_CLOSEALLFORMSERROR, logLevelError, printLog);
			bshtr.setMessage(Constants.BSH_MESSAGE_CLOSEALLFORMSERROR);
			bshtr.setStatusMsg(Constants.BSH_MESSAGE_STATUSMESSAGE);
			reportList.add(bshtr);
			passCond = false;
		}

		if (passCond) {
			try {
				if (expandedRootConfig != 0) {
					// If the module is left open in the tree, click on it to close it.
					su.log(headerFormOpen, "If the module is left open in the tree, it is closed by clicking on it.", logLevelInfo, printLog);
					mainMenu.leftClickNode(expandedRootConfig);
				}
				if (formOpenType.equals(FormOpenTypes.EXCHANGE_RATES_FORM_OPEN.getType())) {
					// Works for daily exchange rates.
					su.log(headerFormOpen, "Works for Daily Exchange Rates.", logLevelInfo, printLog);
					TPW.doExchangeRates();
					// Indicates that there are no open modules in the product tree.
					su.log(headerFormOpen, "Indicates that there are no open modules in the product tree.", logLevelInfo, printLog);
					expandedRootConfig = 0;
				} else if (formOpenType.equals(FormOpenTypes.SET_WORK_DATES_FORM_OPEN.getType())) {
					// He's working for the Working Days.
					su.log(headerFormOpen, "He's working for the Working Days.", logLevelInfo, printLog);
					TPW.doSetWorkDate();
					// Indicates that there are no open modules in the product tree.
					su.log(headerFormOpen, "Indicates that there are no open modules in the product tree.", logLevelInfo, printLog);
					expandedRootConfig = 0;
				} else {
					// I open the menuPath variable by split and open it by loop.
					su.log(headerFormOpen, "The menuPath variable is opened by split and it is opened individually by loop.", logLevelInfo, printLog);
					String formPathWithoutBrackets = menuPath.substring(1, menuPath.length() - 1);
					String[] pathList = formPathWithoutBrackets.split(",");
					for (int i = 0; i < pathList.length; i++) {
						mainMenu.leftClickNode(Integer.parseInt(pathList[i]));
					}
					// We assign the module that is open in the product tree to the global expandedRootConfig variable.
					expandedRootConfig = Integer.parseInt(pathList[0]);
					su.log(headerFormOpen,
							"We assign the module that is open in the product tree to the global expandedRootConfig variable. expandedRootConfig :" + expandedRootConfig,
							logLevelInfo, printLog);
				}
			} catch (Exception e) {
				su.log(headerFormOpen, e + "", logLevelError, printLog);
				if (menuPath == null) {
					// For formOpen steeps that do not use the product tree.
					su.log(headerFormOpen, formOpenType + " failed.", logLevelError, printLog);
					bshtr.setMessage(formOpenType + " failed.");
				} else {
					su.log(headerFormOpen, Constants.BSH_MESSAGE_FORMOPENPARAMETERERROR + menuPath, logLevelError, printLog);
					bshtr.setMessage(Constants.BSH_MESSAGE_FORMOPENPARAMETERERROR + menuPath);
				}
				bshtr.setStatusMsg(StatusMessages.FAILED.getStatus());
				reportList.add(bshtr);
				passCond = false;
			}
		}
		if (passCond) {
			// If successful, the report is successful.
			su.log(headerFormOpen, "successful! The report is successful.", logLevelInfo, printLog);
			if (menuPath == null) {
				// For formOpen steeps that do not use the product tree.
				su.log(headerFormOpen, formOpenType + " successfully opened.", logLevelInfo, printLog);
				bshtr.setMessage(formOpenType + " successfully opened.");
			} else {
				// For formOpen steps that use the product tree.
				su.log(headerFormOpen, Constants.BSH_MESSAGE_FORMOPENSUCCESS + menuPath, logLevelInfo, printLog);
				bshtr.setMessage(Constants.BSH_MESSAGE_FORMOPENSUCCESS + menuPath);
			}
			bshtr.setStatusMsg(StatusMessages.SUCCEEDED.getStatus());
			reportList.add(bshtr);
		}
		// The console is going to print the stepped out warning.
		su.log(headerFormOpen, "STEP BITTI", logLevelInfo, printLog);
		su.log(headerFormOpen, Constants.BSH_MESSAGE_DASHSIGN + Constants.OPERATION_FORMOPEN_BSH + Constants.BSH_MESSAGE_ENDS + Constants.BSH_MESSAGE_DASHSIGN, logLevelInfo,
				printLog);
	}

	/*
	 * This method will wait until a screen with the expectedFormName given as parameter is opened. If it does not open within the specified timeout period, it will return false
	 * value.
	 */
	public boolean waitForPendingForm(String expectedFormName) {
		String headerPendingForm = "waitForPendingForm() : ";
		su.log(headerPendingForm, "The waitForPendingForm procedure was entered. expectedFormName :" + expectedFormName, logLevelInfo, printLog);
		try {
			sleepCond = true;
			start = System.currentTimeMillis();
			su.log(headerPendingForm, "The while loop will begin.", logLevelInfo, printLog);
			while (sleepCond) {
				if (TPW.getCurrentContainerFormName().equals(expectedFormName)) {
					// If the screen on which FormName is available is opened
					su.log(headerPendingForm,
							"waitForPendingForm : The screen with the formName selected is opened. TPW.getCurrentContainerFormName() :" + TPW.getCurrentContainerFormName(),
							logLevelInfo, printLog);
					return true;
				} else if (System.currentTimeMillis() - start > waitForPendingFormWaitTime) {
					// If the screen you want for 30 seconds has not been turned on. NOTE: 30 seconds, parametric due to the slowness of the product.
					su.log(headerPendingForm, "waitForPendingForm : " + waitForPendingFormWaitTime
							+ " The screen that we wanted for second did not open. TPW.getCurrentContainerFormName() :" + TPW.getCurrentContainerFormName(), logLevelError,
							printLog);
					throw new Exception();
				}
			}
			su.log(headerPendingForm, "while loop is over.", logLevelInfo, printLog);
		} catch (Exception e) {
			// If the display did not turn on for 30 seconds or if a problem is encountered while opening, false is returned and the report is printed.
			// (Where the returned value is equal to passCond is called)
			su.log(headerPendingForm, expectedFormName + "(expectedFormName) form was not reached.", logLevelError, printLog);
			((TestReport) reportList.get(reportList.size() - 1)).setMessage(expectedFormName + "(expectedFormName) form was not reached.");
			((TestReport) reportList.get(reportList.size() - 1)).setStatusMsg(StatusMessages.FAILED.getStatus());
			return false;
		}
		return false;
	}

	/*
	 * This method will only close all open screens by pressing the release button until the opening screen remains.
	 */
	public void closeAllForms() throws InterruptedException {
		start = System.currentTimeMillis();
		String headerCloseAllForms = "closeAllForms() : ";
		sleepCond = true;
		formName = "";
		// It will return until the not open screen remains.
		su.log(headerCloseAllForms, " procedure started...Start time :" + start, logLevelInfo, printLog);
		su.log(headerCloseAllForms, "It will return until the not open screen remains.", logLevelInfo, printLog);
		su.log(headerCloseAllForms, "JLbsOpenWindowListing.getOpenDialogs().size() : " + JLbsOpenWindowListing.getOpenDialogs().size(), logLevelInfo, printLog);
		int cafCount = 1; // closeAllForms while Count

		while (sleepCond && JLbsOpenWindowListing.getOpenDialogs().size() != 0) {

			su.log(headerCloseAllForms, " We call messageDialog () if there is a dialog on the screen... closeAllForms while Count: " + cafCount, logLevelInfo, printLog);
			messageDialog(null, null, false);
			su.log(headerCloseAllForms, " We called messageDialog () if there was a dialog on the screen.", logLevelInfo, printLog);

			su.log(headerCloseAllForms, " calling GetSnapshot.bsh in the procedure.", logLevelInfo, printLog);
			/* $I(GetSnapshot.bsh); */
			su.log(headerCloseAllForms, " In the procedure we called GetSnapshot.bsh.", logLevelInfo, printLog);

			su.log(headerCloseAllForms, "su.getCloseButton(formElement:" + formElement + ") calling.", logLevelInfo, printLog);
			int buttonTag = su.getCloseButton(formElement);
			su.log(headerCloseAllForms, "su.getCloseButton(formElement :" + formElement + ") We called. buttonTag :" + buttonTag, logLevelInfo, printLog);

			if (buttonTag != 0 && !formName.equals(TPW.getCurrentContainerFormName())) {
				su.log(headerCloseAllForms, "buttonTag is not null and TPW.getCurrentContainerFormName() with formName is not the same.", logLevelInfo, printLog);
				// If the previous screen was closed and there was no problem finding the cancellation key.
				su.log(headerCloseAllForms, "If the previous screen was closed and there was no problem finding the cancellation key", logLevelInfo, printLog);
				formName = TPW.getCurrentContainerFormName();
				su.log(headerCloseAllForms, "formName :" + formName, logLevelInfo, printLog);
				su.log(headerCloseAllForms, "focus on the form.", logLevelInfo, printLog);
				// focus on the form.

				JLbsScriptContainer form = TPW.getContainer(formName + Constants.FILE_EXTENSION_JFM);
				su.log(headerCloseAllForms, "form :" + form, logLevelInfo, printLog);

				su.log(headerCloseAllForms, "Calling the form.activate () routine.", logLevelInfo, printLog);
				form.activate();
				su.log(headerCloseAllForms, "form.activate () routine was called.", logLevelInfo, printLog);

				// Press release button
				su.log(headerCloseAllForms, "Press release button", logLevelInfo, printLog);

				su.log(headerCloseAllForms, " The button is retrieved with the form.getScriptButtonByTag (buttonTag) procedure.", logLevelInfo, printLog);
				JLbsScriptButton button = (JLbsScriptButton) form.getScriptButtonByTag(buttonTag);
				su.log(headerCloseAllForms, "button :" + button, logLevelInfo, printLog);

				su.log(headerCloseAllForms, " Calling the button.select() routine.", logLevelInfo, printLog);
				button.select();
				su.log(headerCloseAllForms, " The button.select() routine was called.", logLevelInfo, printLog);

				su.log(headerCloseAllForms, " Calling the button.click() routine.", logLevelInfo, printLog);
				button.click();
				su.log(headerCloseAllForms, "The button.click() routine was called.", logLevelInfo, printLog);

			} else if (System.currentTimeMillis() - start > 5000) {

				// If there is no new screen for 5 seconds
				su.log(headerCloseAllForms, "If there is no new screen for 5 seconds", logLevelInfo, printLog);

				sleepCond = false;
			}

			Thread.sleep(500);

			cafCount++;
		}
		su.log(headerCloseAllForms, "all the screens are closed but we are doing push back again if the messageDialog is still displayed when the press-last screen is closed. ",
				logLevelInfo, printLog);

		// all the screens are closed but we are doing push back again if the messageDialog is still displayed when the press-last screen is closed.
		messageDialog(null, null, false);
	}

	/*
	 * This method controls the opening of the new form during the timeout period. Returns true if the new screen is opened, otherwise returns false.
	 */
	public boolean synchronizeForms(long timeOut) throws InterruptedException {
		start = System.currentTimeMillis();
		Thread.sleep(500);
		su.log("SynchronizeForms", "formName :" + formName, logLevelInfo, printLog);
		su.log("SynchronizeForms", "TPW.getCurrentContainerFormName() :" + TPW.getCurrentContainerFormName(), logLevelInfo, printLog);
		su.log("SynchronizeForms", "timeOut :" + timeOut, logLevelInfo, printLog);
		// BSHTestReport bshtr = new BSHTestReport("SynchronizeForms", formName, isLookup);
		// String oldFormName = formName;
		while (true) {
			if (!formName.equals(TPW.getCurrentContainerFormName())) {
				// If a different screen is found, it updates formName and returns true.
				su.log("synchronizeForms", "There was a different screen than before.", logLevelInfo, printLog);
				su.log("synchronizeForms", "TPW.getCurrentContainerFormName () is being called.", logLevelInfo, printLog);
				formName = TPW.getCurrentContainerFormName();
				su.log("synchronizeForms", "TPW.getCurrentContainerFormName() was called. formName :" + formName, logLevelInfo, printLog);
				// bshtr.addMessage("The new form has been reached within the specified time.");
				// bshtr.setStatusMsg(StatusMessages.SUCCEEDED.getStatus());
				// reportList.add(bshtr);
				return true;
			} else if (System.currentTimeMillis() - start > timeOut) {
				// Returns false if no new screen is found during timeOut.
				su.log("synchronizeForms", "No new screen was found during timeOut. timeOut :" + timeOut, logLevelInfo, printLog);
				// su.log("synchronizeForms", "The new form could not be reached within the specified time. Unclosed form Name :" + oldFormName,
				// logLevelInfo, printLog);
				// bshtr.addMessage("The new form could not be reached within the specified time. Unclosed form Name :" + oldFormName);
				// bshtr.setStatusMsg(StatusMessages.FAILED.getStatus());
				// reportList.add(bshtr);
				return false;
			}
		}
	}

	/*
	 * It opens with 4 methods; 1- The drop-downs in the DIALOG column in the Snapshot_value in FormFill. 2- Button Click or PopUp come after test Steps, button Click and popUp
	 * integration. 3- ButtonClick or PopUp not followed by any test step 4- Automatically prints OK when captured by TEDAM, not controlled by the user
	 * @mdlist incoming message list, if null, dialogue presse - late mode.
	 * @messageDialogbshtr, the object to be used for the report
	 * @isTestStep is TRUE if it comes from a dialog or FormFill linked to a step in the Testcase, and FALSE if something happens instantaneously.
	 */
	public TestReport messageDialog(List mdList, TestReport messageDialogbshtr, boolean isTestStep) {
		String headerMessageDialog = "MESSAGEDIALOGNG.BSH";
		// The console will start printing step warning.
		su.log(headerMessageDialog, "isTestStep: " + isTestStep + " mdList : " + mdList, logLevelInfo, printLog);
		// If the message is entered, this variable is discarded once the correctness is checked.
		su.log(headerMessageDialog, "If the message is entered, this variable is discarded once the correctness is checked.", logLevelInfo, printLog);
		try {
			if (messageDialogSleepTime > 0) {
				// messageDialogSleepTime Base.bsh is being retrieved from config while it is being created.
				su.log(headerMessageDialog, "messageDialogSleepTime =" + messageDialogSleepTime + " Wait until ms...", logLevelInfo, printLog);
				Thread.sleep(messageDialogSleepTime);
			}
			int index = 0;
			// index of mdList
			su.log(headerMessageDialog, "-1 means no dialogue !!!! JLbsScriptMessageDialog._getCurrentDialogId() :  " + JLbsScriptMessageDialog._getCurrentDialogId(), logLevelInfo,
					printLog);
			while (JLbsScriptMessageDialog._getCurrentDialogId() != -1) {
				su.log(headerMessageDialog, "LOOPED IN   " + JLbsScriptMessageDialog._getCurrentDialogId(), logLevelInfo, printLog);
				// If a MessageDialog is encountered, the loop enters, -1 means no dialog.
				JLbsScriptMessageDialog messageDialog = TPW.getCurrentMessageDialog();
				if (isTestStep && mdList != null && passCond) {
					su.log(headerMessageDialog, "isTestStep && mdList != null " + isTestStep + " - " + mdList, logLevelInfo, printLog);
					if (((MessageDialog) mdList.get(index)).getSelection()) {
						su.log(headerMessageDialog, "mdList.get(index).getSelection():  " + ((MessageDialog) mdList.get(index)).getSelection(), logLevelInfo, printLog);
						// If the given dialog parameter is TRUE then click OK or YES
						su.log(headerMessageDialog, "If the given dialog parameter is TRUE then click OK or YES.", logLevelInfo, printLog);
						messageDialog.actionPerformed(JLbsMessageUtil.BUT_OK);
						messageDialog.actionPerformed(JLbsMessageUtil.BUT_YES);
						su.log(headerMessageDialog, "If the given dialog parameter is TRUE then he clicked OK or YES.", logLevelInfo, printLog);
					} else {
						// If the given dialog parameter is FALSE
						su.log(headerMessageDialog, "If the given dialog parameter is FALSE, click on CANCEL.", logLevelInfo, printLog);
						messageDialog.actionPerformed(JLbsMessageUtil.BUT_CANCEL);
						su.log(headerMessageDialog, "If the dialogue parameter is FALSE, CANCEL is clicked.", logLevelInfo, printLog);
					}
				} else {
					// Press - late is working in this situation.
					su.log(headerMessageDialog, "Push late state...", logLevelInfo, printLog);
					messageDialog.actionPerformed(JLbsMessageUtil.BUT_OK);
					messageDialog.actionPerformed(JLbsMessageUtil.BUT_YES);
				}
				// if (messageDialogSleepTime > 0) {
				// If there is a second dialogue, we wait for a while.
				su.log(headerMessageDialog, "We wait 2 seconds for a second dialog.", logLevelInfo, printLog);
				Thread.sleep(2000);
				su.log(headerMessageDialog, "I waited 2 seconds for a second dialog.", logLevelInfo, printLog);
				// }
				index++;
			}
			su.log(headerMessageDialog, "after the while loop", logLevelInfo, printLog);
			if (mdList != null && mdList.size() > index) {
				su.log(headerMessageDialog, "mDList is not null and size of mdList is greater than index; any of the dialogs that came as parameters did not open.", logLevelError,
						printLog);
				// If mdList is not null and the size of the mDList is greater than the index; if any of the dialogs that come as parameters do not open, we explode.
				throw new Exception();
			}
			su.log(headerMessageDialog, "We are making mdList null.", logLevelInfo, printLog);
			mdList = null;
		} catch (Exception e) {
			su.log(headerMessageDialog, e + "", logLevelError, printLog);
			if (sourceOperation.equals(Constants.OPERATION_FORMFILL_BSH)) {
				su.log(headerMessageDialog, "OPERATION_FORMFILL Parametre : " + mdList, logLevelError, printLog);
				messageDialogbshtr.setStatusMsg(StatusMessages.FAILED.getStatus());
				messageDialogbshtr.addMessage("The MessageDialog parameter was entered incorrectly. Parameter: " + mdList);
				reportList.add(messageDialogbshtr);
			} else if (isTestStep) {
				su.log(headerMessageDialog, " else if(isTestStep) Parametre : " + mdList, logLevelError, printLog);
				messageDialogbshtr.setStatusMsg(StatusMessages.FAILED.getStatus());
				messageDialogbshtr.addMessage("The MessageDialog parameter was entered incorrectly. Parameter: " + mdList);
			}
			passCond = false;
			mdList = null;
			su.log(headerMessageDialog, "mdList is null, passCond is false", logLevelInfo, printLog);
		}
		su.log(headerMessageDialog, "AFTER EXCEPTION", logLevelInfo, printLog);
		if (passCond) {
			su.log(headerMessageDialog, " if(passCond)", logLevelInfo, printLog);
			// If the process is successful
			if (isTestStep && !sourceOperation.equals(Constants.OPERATION_FORMFILL_BSH)) {
				su.log(headerMessageDialog, " isTestStep && !sourceOperation.equals(Constants.OPERATION_FORMFILL) ", logLevelInfo, printLog);
				/*
				 * When the result is successful, if isTestStep outside of FormFill is true, the report is updated as successful. The messageDialogs called from within FormFill
				 * have no effect on the report part of the success, if the formFill is successful, the formfill is entered as a success.
				 */
				messageDialogbshtr.setStatusMsg(StatusMessages.SUCCEEDED.getStatus());
				messageDialogbshtr.addMessage("MessageDialog başarıyla tamamlandı. Parameter: " + mdList);
			}
		}
		su.log(headerMessageDialog, " ---MESSAGEDIALOG ENDS--- Parameter : " + mdList, logLevelInfo, printLog);
		return messageDialogbshtr;
	}

	public void report(String reportFileName, boolean continueOnErrorReport, boolean isWriteFilters, Long reportWaitSleepMillis) throws InterruptedException {
		String headerReportTest = "REPORTNG.BSH";
		su.log(headerReportTest, " ---Report Starteds--- ", logLevelInfo, printLog);
		su.log(headerReportTest, " reportFileName : " + reportFileName, logLevelInfo, printLog);
		su.log(headerReportTest, "reportWaitSleepMillis : " + reportWaitSleepMillis, logLevelInfo, printLog);
		String sourceReportFilePathString = tysMachine + testCaseId + Constants.FILE_SEPARATOR + reportFileName;
		su.log(headerReportTest, "sourceReportFilePathString : " + sourceReportFilePathString, logLevelInfo, printLog);
		String targetTemporaryReportFilePathString = projectFile + Constants.FILE_SEPARATOR + FilePath.BSH_MODULAR_SCRIPTS.getPath() + Constants.FILE_SEPARATOR + reportFileName;
		su.log(headerReportTest, "targetTemporaryReportFilePathString : " + targetTemporaryReportFilePathString, logLevelInfo, printLog);
		String targetReportFilePathString = projectFile + Constants.FILE_SEPARATOR + FilePath.REPORTS.getPath() + Constants.FILE_SEPARATOR + Constants.TC + testCaseId
				+ Constants.FILE_SEPARATOR + reportFileName + "_" + new java.util.Date().getTime();
		su.log(headerReportTest, "targetReportFilePathString : " + targetReportFilePathString, logLevelInfo, printLog);
		String reportName = "";
		JLbsTestErrorList errorList = null;
		boolean isErrorExist = false; // is there an error?
		String errorContent = ""; // error content
		try {
			su.log(headerReportTest, "The copySourceToDestinationWithFormat operations begin.", logLevelInfo, printLog);
			boolean isReportFltCopied = su.copySourceToDestinationWithFormat(sourceReportFilePathString, targetTemporaryReportFilePathString, Constants.FILE_EXTENSION_FLT, false);
			su.log(headerReportTest, "isReportFltCopied :" + isReportFltCopied, logLevelInfo, printLog);
			if (!isReportFltCopied) {
				su.log(headerReportTest, "Report files could not be copied. isReportFltCopied : " + isReportFltCopied, logLevelInfo, printLog);
				throw new CreateNewFileException("Report files could not be copied.");
			}
			su.log(headerReportTest, "getReportNameFromFile starts the process.", logLevelInfo, printLog);
			reportName = su.getReportNameFromFile(sourceReportFilePathString);
			su.log(headerReportTest, "reportName : " + reportName, logLevelInfo, printLog);
			su.log(headerReportTest, " The TPW.executePDFReport () routine is being called.... Report name : " + reportName, logLevelInfo, printLog);
			su.log(headerReportTest, " printFilter : " + isWriteFilters, logLevelInfo, printLog);
			TPW.executePDFReport(reportName, null, 0, 0, reportFileName + Constants.FILE_EXTENSION_FLT, isWriteFilters);
			su.log(headerReportTest, reportWaitSleepMillis + " Wait for seconds", logLevelInfo, printLog);
			Thread.sleep(reportWaitSleepMillis.longValue());
			if (!new File(targetTemporaryReportFilePathString + Constants.FILE_EXTENSION_PDF).exists()) {
				su.log(headerReportTest,
						"targetReportFilePathString : " + targetTemporaryReportFilePathString
								+ Constants.FILE_EXTENSION_PDF
								+ " report file not found. Try to increase reportWaitSleepMillis",
						logLevelInfo, printLog);
				throw new CreateNewFileException(
						"Report file not found in 'targetReportFilePathString'. Increasing report wait time may be the solution!");
			}
			else {
				su.log(headerReportTest, "targetReportFilePathString : " + targetTemporaryReportFilePathString
						+ Constants.FILE_EXTENSION_PDF + " report file created. OK!", logLevelInfo, printLog);
			}
			errorList = TPW.getCurrentErrorList();
			su.log(headerReportTest, " ErrorList is reached.", logLevelInfo, printLog);
			su.log(headerReportTest, " errorList.size()=" + errorList.size(), logLevelInfo, printLog);
			if (errorList.size() > 0) {
				su.log(headerReportTest, " errorList is full. Error is being thrown.", logLevelInfo, printLog);
				throw new Exception("Error calling TPW.executePDFReport () routine !!! ");
			} else {
				su.log(headerReportTest, " errorList BOS!", logLevelInfo, printLog);
				su.log(headerReportTest, " The TPW.executePDFReport () procedure was called. Report name : " + reportName, logLevelInfo, printLog);
			}
		} catch (CreateNewFileException e) {
			su.log(headerReportTest, " copying could not be done on tys machine. :" + e, TedamLogLevel.ERROR, printLog);
			isErrorExist = true;
			errorContent = e.getMessage();
		} catch (Exception e) {
			isErrorExist = true;
			errorContent = e.getMessage();
		}
		String errorMessage = "";
		su.log(headerReportTest, "Error content: " + errorContent, logLevelInfo, printLog);
		if (errorContent != null) {
			errorMessage += errorContent;
		}
		if (isErrorExist && !continueOnErrorReport) {
			// error and the continueOnErrorReport parameter is false. The test scenario will be terminated by writing
			// Report Fail and passCond False.//
			passCond = false;
			TestReport bshtr = new TestReport(Constants.OPERATION_REPORT_BSH, formName);
			su.log(headerReportTest, "isErrorExist: " + isErrorExist + " continueOnErrorReport : " + continueOnErrorReport + " passCond = it is doing false.", logLevelInfo,
					printLog);
			if (errorList != null && errorList.size() > 0) {
				for (int i = 0; i < errorList.size(); i++) {
					errorMessage += " \n" + (i + 1) + ". " + errorContent + errorList.getErrorItem(i).getErrorMessage() + " !!!";
				}
			}
			su.copySourceToDestinationWithFormat(sourceReportFilePathString, targetReportFilePathString + "_old",
					Constants.FILE_EXTENSION_PDF, false);
			su.log(headerReportTest, "PDF file copied. Step 1", logLevelInfo, printLog);
			su.copySourceToDestinationWithFormat(targetTemporaryReportFilePathString, targetReportFilePathString,
					Constants.FILE_EXTENSION_PDF, true);
			su.log(headerReportTest, "PDF file copied. Step 2", logLevelInfo, printLog);
			su.log(headerReportTest, "errorMessage: " + errorMessage, logLevelInfo, printLog);
			bshtr.addMessage(errorMessage);
			bshtr.setStatusMsg(StatusMessages.FAILED.getStatus());
			reportList.add(bshtr);
		} else if (isErrorExist && continueOnErrorReport) {
			// error and the continueOnErrorReport parameter is true. The report will be written as Caution and
			// passCond will continue to be true test scenario.
			passCond = true;
			TestReport bshtr = new TestReport(Constants.OPERATION_REPORT_BSH, formName);
			su.log(headerReportTest, "isErrorExist: " + isErrorExist + " continueOnErrorReport : " + continueOnErrorReport + " passCond = it is doing true.", logLevelInfo,
					printLog);
			if (errorList != null && errorList.size() > 0) {
				for (int i = 0; i < errorList.size(); i++) {
					su.log(headerReportTest, (i + 1) + " ." + errorContent + errorList.getErrorItem(i).getErrorMessage() + " !!!", logLevelError, printLog);
					errorMessage += " \n" + (i + 1) + ". " + errorContent + errorList.getErrorItem(i).getErrorMessage() + " !!!";
				}
			}
			bshtr.addMessage(errorContent + errorMessage);
			bshtr.setStatusMsg(StatusMessages.CAUTION.getStatus());
			reportList.add(bshtr);
		} else {
			su.log(headerReportTest, "The copySourceToDestinationWithFormat operations begin. sourceReportFilePathString : " + sourceReportFilePathString, logLevelInfo, printLog);
			su.copySourceToDestinationWithFormat(sourceReportFilePathString, targetReportFilePathString + "_old", Constants.FILE_EXTENSION_PDF, false);
			su.log(headerReportTest, "The copySourceToDestinationWithFormat operations begin. targetTemporaryReportFilePathString : " + targetTemporaryReportFilePathString,
					logLevelInfo, printLog);
			su.copySourceToDestinationWithFormat(targetTemporaryReportFilePathString, targetReportFilePathString, Constants.FILE_EXTENSION_PDF, true);
			TedamReportUtils reportUtil = new TedamReportUtils();
			TestReport bshtr = reportUtil.compareTwoPDFFile(new File(targetReportFilePathString + "_old" + Constants.FILE_EXTENSION_PDF),
					new File(targetReportFilePathString + Constants.FILE_EXTENSION_PDF));
			passCond = true;
			reportList.add(bshtr);
		}
		su.deleteFile(targetTemporaryReportFilePathString + Constants.FILE_EXTENSION_FLT);
		// The console is going to print the stepped out warning.
		su.log(headerReportTest, " STEP IS OVER ", logLevelInfo, printLog);
		su.log(headerReportTest, " ---REPORTNG END--- reportFileName : " + reportName, logLevelInfo, printLog);
	}

	public void rowDelete(JLbsScriptContainer form, Integer gridTag, Integer rowIndex) {
		String headerRowDelete = "ROWDELETENG.BSH";
		// The console will start printing step warning.
		su.log(headerRowDelete, "STEP STARTED", logLevelInfo, printLog);
		su.log(headerRowDelete, Constants.BSH_MESSAGE_DASHSIGN + Constants.OPERATION_ROWDELETE_BSH + Constants.BSH_MESSAGE_DASHSIGN, logLevelInfo, printLog);
		// Step report definition.
		TestReport bshtr = new TestReport(Constants.OPERATION_ROWDELETE_BSH, formName);
		su.log(headerRowDelete, "Step report defined.", logLevelInfo, printLog);
		try {
			JLbsScriptGrid grid = (JLbsScriptGrid) form.getScriptGridByTag(gridTag.intValue());
			grid.select();
			grid.deleteRow(rowIndex.intValue());
			messageDialogSleepTime = 1000;
			// After deletion, messageDialog is pressed in message dialogs.
			su.log(headerRowDelete, "After deletion, messageDialog is pressed in message dialogs.", logLevelInfo, printLog);
			messageDialog(null, null, false);
		} catch (Exception e) {
			su.log(headerRowDelete, e + "", logLevelError, printLog);
			su.log(headerRowDelete, Constants.BSH_MESSAGE_ROWDELETEERROR + String.valueOf(rowIndex) + Constants.BSH_MESSAGE_GRIDTAG + String.valueOf(gridTag), logLevelError,
					printLog);
			bshtr.addMessage(Constants.BSH_MESSAGE_ROWDELETEERROR + String.valueOf(rowIndex) + Constants.BSH_MESSAGE_GRIDTAG + String.valueOf(gridTag));
			bshtr.setStatusMsg(StatusMessages.FAILED.getStatus());
			reportList.add(bshtr);
			passCond = false;
		}
		if (passCond) {
			bshtr.addMessage(Constants.BSH_MESSAGE_ROWDELETESUCCESS + String.valueOf(rowIndex) + Constants.BSH_MESSAGE_GRIDTAG + String.valueOf(gridTag));
			bshtr.setStatusMsg(StatusMessages.SUCCEEDED.getStatus());
			reportList.add(bshtr);
		}

		// The console is going to print the stepped out warning.
		su.log(headerRowDelete, " STEP BITTI ", logLevelInfo, printLog);
		su.log(headerRowDelete, Constants.BSH_MESSAGE_DASHSIGN + Constants.OPERATION_ROWDELETE_BSH + Constants.BSH_MESSAGE_ENDS + Constants.BSH_MESSAGE_DASHSIGN
				+ Constants.BSH_MESSAGE_PARAMETER + String.valueOf(rowIndex) + Constants.BSH_MESSAGE_GRIDTAG + String.valueOf(gridTag), logLevelInfo, printLog);
	}

	public void doubleClick(JLbsScriptContainer form, Integer gridTag, Integer rowIndex) {
		String headerDoubleClick = "DOUBLECLICKNG.BSH";
		su.log(headerDoubleClick, "form :" + form, logLevelInfo, printLog);
		su.log(headerDoubleClick, Constants.BSH_MESSAGE_DASHSIGN + Constants.OPERATION_DOUBLECLICK_BSH + Constants.BSH_MESSAGE_DASHSIGN, logLevelInfo, printLog);
		su.log(headerDoubleClick, "isLookup :" + isLookup, logLevelInfo, printLog);
		// Step report definition.
		TestReport bshtr = new TestReport(Constants.OPERATION_BUTTONCLICK_BSH, formName, isLookup);
		su.log(headerDoubleClick, "Step report defined.", logLevelInfo, printLog);

		try {
			JLbsScriptGrid grid = (JLbsScriptGrid) form.getScriptGridByTag(gridTag.intValue());
			su.log(headerDoubleClick, "grid.Select () routine is called.", logLevelInfo, printLog);
			grid.select();
			su.log(headerDoubleClick, "grid.Select () routine was called.", logLevelInfo, printLog);
			su.log(headerDoubleClick, "grid.doubleClick() routine is called.", logLevelInfo, printLog);
			grid.doubleClick();
			su.log(headerDoubleClick, "grid.doubleClick() routine was called.", logLevelInfo, printLog);
		} catch (Exception e) {
			su.log(headerDoubleClick, e + "", logLevelError, printLog);
			su.log(headerDoubleClick, Constants.BSH_MESSAGE_DOUBLECLICKERROR + String.valueOf(rowIndex) + Constants.BSH_MESSAGE_GRIDTAG + String.valueOf(gridTag), logLevelError,
					printLog);
			bshtr.addMessage(Constants.BSH_MESSAGE_DOUBLECLICKERROR + String.valueOf(rowIndex) + Constants.BSH_MESSAGE_GRIDTAG + String.valueOf(gridTag));
			bshtr.setStatusMsg(StatusMessages.FAILED.getStatus());
			reportList.add(bshtr);
			passCond = false;
		}
		if (passCond) {
			su.log(headerDoubleClick, Constants.BSH_MESSAGE_DOUBLECLICKSUCCESS + rowIndex, logLevelInfo, printLog);
			bshtr.addMessage(Constants.BSH_MESSAGE_DOUBLECLICKSUCCESS + rowIndex);
			bshtr.setStatusMsg(StatusMessages.SUCCEEDED.getStatus());
			reportList.add(bshtr);
		}
		// The console is going to print the stepped out warning.
		su.log(headerDoubleClick, "STEP IS OVER", logLevelInfo, printLog);
		su.log(headerDoubleClick, Constants.BSH_MESSAGE_DASHSIGN + Constants.OPERATION_DOUBLECLICK_BSH + Constants.BSH_MESSAGE_ENDS + Constants.BSH_MESSAGE_DASHSIGN
				+ Constants.BSH_MESSAGE_PARAMETER + " - rowIndex :" + rowIndex, logLevelInfo, printLog);

	}
}
