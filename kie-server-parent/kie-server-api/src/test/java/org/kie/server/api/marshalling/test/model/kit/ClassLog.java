/*
 * Copyright 2024 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.kie.server.api.marshalling.test.model.kit;

import java.io.Serializable;
import java.time.format.DateTimeFormatter;

public class ClassLog implements Serializable {
    private static final long serialVersionUID = -4489972701712981564L;
    public static final DateTimeFormatter SOURCE_DATE_FORMAT = DateTimeFormatter.ofPattern("uuuu.MM.dd");
    public static final DateTimeFormatter TARGET_DATE_FORMAT = DateTimeFormatter.ofPattern("MM/dd/uuuu");
    private String actionId;
    private String source = "";
    private String dateReceived;
    private String actionPerformed = "";
    private String dateOfAction;
    private String metadataRecordStatus;
    private String ingestedFileStatus;
    private String metadataRecordErrorDesc;
    private String ingestedFileErrorDesc;
    private String inputDirectory;
    private String metaDataRecordPDFFileName;
    private String ingestedPDFFileName;
    private String correspondenceType;
    private String applicationUniqueID;
    private String requestID;
    private String accountNumber;
    private boolean eDeliveryIndicator;

    public String getActionId() {
        return actionId;
    }

    public void setActionId(String actionId) {
        this.actionId = actionId;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getDateReceived() {
        return dateReceived;
    }

    public void setDateReceived(String dateReceived) {
        this.dateReceived = dateReceived;
    }

    public String getActionPerformed() {
        return actionPerformed;
    }

    public void setActionPerformed(String actionPerformed) {
        this.actionPerformed = actionPerformed;
    }

    public String getDateOfAction() {
        return dateOfAction;
    }

    public void setDateOfAction(String dateOfAction) {
        this.dateOfAction = dateOfAction;
    }

    public String getMetadataRecordStatus() {
        return metadataRecordStatus;
    }

    public void setMetadataRecordStatus(String metadataRecordStatus) {
        this.metadataRecordStatus = metadataRecordStatus;
    }

    public String getIngestedFileStatus() {
        return ingestedFileStatus;
    }

    public void setIngestedFileStatus(String ingestedFileStatus) {
        this.ingestedFileStatus = ingestedFileStatus;
    }

    public String getMetadataRecordErrorDesc() {
        return metadataRecordErrorDesc;
    }

    public void setMetadataRecordErrorDesc(String metadataRecordErrorDesc) {
        this.metadataRecordErrorDesc = metadataRecordErrorDesc;
    }

    public String getIngestedFileErrorDesc() {
        return ingestedFileErrorDesc;
    }

    public void setIngestedFileErrorDesc(String ingestedFileErrorDesc) {
        this.ingestedFileErrorDesc = ingestedFileErrorDesc;
    }

    public String getInputDirectory() {
        return inputDirectory;
    }

    public void setInputDirectory(String inputDirectory) {
        this.inputDirectory = inputDirectory;
    }

    public String getMetaDataRecordPDFFileName() {
        return metaDataRecordPDFFileName;
    }

    public void setMetaDataRecordPDFFileName(String metaDataRecordPDFFileName) {
        this.metaDataRecordPDFFileName = metaDataRecordPDFFileName;
    }

    public String getIngestedPDFFileName() {
        return ingestedPDFFileName;
    }

    public void setIngestedPDFFileName(String ingestedPDFFileName) {
        this.ingestedPDFFileName = ingestedPDFFileName;
    }

    public String getCorrespondenceType() {
        return correspondenceType;
    }

    public void setCorrespondenceType(String correspondenceType) {
        this.correspondenceType = correspondenceType;
    }

    public String getApplicationUniqueID() {
        return applicationUniqueID;
    }

    public void setApplicationUniqueID(String applicationUniqueID) {
        this.applicationUniqueID = applicationUniqueID;
    }

    public String getRequestID() {
        return requestID;
    }

    public void setRequestID(String requestID) {
        this.requestID = requestID;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }

    public boolean iseDeliveryIndicator() {
        return eDeliveryIndicator;
    }

    public void seteDeliveryIndicator(boolean eDeliveryIndicator) {
        this.eDeliveryIndicator = eDeliveryIndicator;
    }

}
