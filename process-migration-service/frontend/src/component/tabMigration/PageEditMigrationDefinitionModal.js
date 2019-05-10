import React from "react";
import axios from "axios";

import { Button } from "patternfly-react";
import { Modal } from "patternfly-react";
import { Icon } from "patternfly-react";
import { OverlayTrigger } from "patternfly-react";
import { Tooltip } from "patternfly-react";

import { BACKEND_URL } from "../common/PimConstants";
import PageMigrationScheduler from "../tabMigrationPlan/wizardExecuteMigration/PageMigrationScheduler";

export default class PageEditMigrationDefinitionModal extends React.Component {
  constructor(props) {
    super(props);
    this.state = {
      id: this.props.rowData.id,
      planId: this.props.rowData.definition.planId,
      processInstanceIds: this.props.rowData.definition.processInstanceIds,
      kieserverId: this.props.rowData.definition.kieserverId,
      scheduledStartTime: this.props.rowData.definition.execution
        .scheduledStartTime,
      callbackUrl: this.props.rowData.definition.execution.callbackUrl,
      showEditDialog: false
    };
  }

  convertFormDataToJson() {
    const execution = {
      type: "ASYNC"
    };

    if (
      this.state.scheduledStartTime !== null &&
      this.state.scheduledStartTime !== ""
    ) {
      execution.scheduledStartTime = this.state.scheduledStartTime;
    }
    if (this.state.callbackUrl !== null && this.state.callbackUrl !== "") {
      execution.callbackUrl = this.state.callbackUrl;
    }

    const formData = {
      planId: this.state.planId,
      kieserverId: this.state.kieserverId,
      processInstanceIds: "[" + this.state.processInstanceIds + "]",
      execution: execution
    };

    var jsonStr = JSON.stringify(formData, null, 2);

    if (jsonStr !== null && jsonStr !== "") {
      //replace "[ to [
      jsonStr = jsonStr.replace('"[', "[");

      //replace ]" to ]
      jsonStr = jsonStr.replace(']"', "]");
    }

    return jsonStr;
  }

  setCallbackUrl = url => {
    this.setState({
      callbackUrl: url
    });
  };

  setScheduleStartTime = startTime => {
    this.setState({
      scheduledStartTime: startTime
    });
  };

  hideEditDialog = () => {
    this.setState({ showEditDialog: false });
  };

  openEditMigration = () => {
    this.setState({
      showEditDialog: true
    });
  };

  submit = () => {
    const serviceUrl = BACKEND_URL + "/migrations/" + this.state.id;
    const migrationDefinitionJsonStr = this.convertFormDataToJson();
    axios
      .put(serviceUrl, migrationDefinitionJsonStr, {
        headers: {
          "Content-Type": "application/json"
        }
      })
      .then(() => {
        this.hideEditDialog();
      });
  };

  render() {
    const tooltipEdit = (
      <Tooltip id="tooltip">
        <div>Edit</div>
      </Tooltip>
    );

    const defaultBody = (
      <div>
        <div className="form-horizontal">
          <div className="form-group">
            <label className="col-md-4 control-label"> Plan ID</label>
            <div className="col-md-8">{this.state.planId}</div>
          </div>
          <div className="form-group">
            <label className="col-md-4 control-label">
              {" "}
              Process Instances ID
            </label>
            <div className="col-md-8">
              {JSON.stringify(this.state.processInstanceIds)}
            </div>
          </div>
          <div className="form-group">
            <label className="col-md-4 control-label"> KIE Server ID</label>
            <div className="col-md-8">{this.state.kieserverId}</div>
          </div>
        </div>

        <PageMigrationScheduler
          setCallbackUrl={this.setCallbackUrl}
          setScheduleStartTime={this.setScheduleStartTime}
        />
      </div>
    );

    return (
      <div>
        <OverlayTrigger overlay={tooltipEdit} placement={"bottom"}>
          <Button bsStyle="link" onClick={this.openEditMigration}>
            <Icon type="fa" name="edit" />
          </Button>
        </OverlayTrigger>
        <Modal
          show={this.state.showEditDialog}
          onHide={this.hideEditDialog}
          size="lg"
        >
          <Modal.Header>
            <Modal.CloseButton onClick={this.hideEditDialog} />
            <Modal.Title>Edit Migration Definition</Modal.Title>
          </Modal.Header>
          <Modal.Body>{defaultBody}</Modal.Body>
          <Modal.Footer>
            <Button
              bsStyle="default"
              className="btn-cancel"
              onClick={this.hideEditDialog}
            >
              Cancel
            </Button>
            <Button bsStyle="primary" onClick={this.submit}>
              Submit
            </Button>
          </Modal.Footer>
        </Modal>
      </div>
    );
  }
}
