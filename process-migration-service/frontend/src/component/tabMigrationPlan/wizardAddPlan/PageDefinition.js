import React, { Component } from "react";

import { Button } from "patternfly-react";
import { ALERT_TYPE_ERROR } from "patternfly-react/dist/js/components/Alert/AlertConstants";

import PageDefinitionSearchTable from "./PageDefinitionSearchTable";
import { BACKEND_URL } from "../../common/PimConstants";
import Notification from "../../Notification";

export default class PageDefinition extends Component {
  constructor(props) {
    super(props);
    this.state = {
      errorMsg: ""
    };
  }

  copySourceToTarget = () => {
    this.props.onChangeTargetContainerId(this.props.sourceContainerId);
    this.props.onChangeTargetProcessId(this.props.sourceProcessId);
  };

  getDefinition = (containerId, processId, callbackFn) => {
    const defURL = `${BACKEND_URL}/kieserver/${
      this.props.kieServerId
    }/definitions/${containerId}/${processId}`;
    fetch(defURL, {
      headers: {
        "Content-Type": "application/json"
      },
      credentials: "same-origin"
    })
      .then(res => {
        if (!res.ok) {
          throw res;
        }
        return res.json();
      })
      .then(res => {
        callbackFn(res);
      })
      .catch(err => {
        const status = err.status;
        let errorMsg;
        if (status === 404) {
          if (containerId !== "" || processId !== "") {
            errorMsg = `Process not found: ${containerId}/${processId}`;
          } else {
            errorMsg = "Provide a valid containerId and processId";
          }
          this.setState({ errorMsg });
          callbackFn("");
        } else {
          err.json().then(json => {
            this.setState({
              errorMsg: `${containerId}/${processId}: ${json.message.string}`
            });
            callbackFn("");
          });
        }
      });
  };

  getDefinitions = () => {
    this.getDefinition(
      this.props.sourceContainerId,
      this.props.sourceProcessId,
      this.props.setSourceDefinition
    );
    this.getDefinition(
      this.props.targetContainerId,
      this.props.targetProcessId,
      this.props.setTargetDefinition
    );
  };

  render() {
    const notification = (
      <Notification
        type={ALERT_TYPE_ERROR}
        message={this.state.errorMsg}
        onDismiss={() => this.setState({ errorMsg: "" })}
      />
    );
    return (
      <div className="form-horizontal">
        {this.state.errorMsg && notification}
        <p />
        <PageDefinitionSearchTable
          tableHeader="Source "
          processId={this.props.sourceProcessId}
          containerId={this.props.sourceContainerId}
          handleProcessIdChange={this.props.onChangeSourceProcessId}
          handleContainerIdChange={this.props.onChangeSourceContainerId}
          initContainerId={this.props.initSourceContainerId}
          initProcessId={this.props.initSourceProcessId}
        />

        <Button onClick={this.copySourceToTarget}>Copy Source To Target</Button>
        <p />
        <PageDefinitionSearchTable
          tableHeader="Target "
          processId={this.props.targetProcessId}
          containerId={this.props.targetContainerId}
          handleProcessIdChange={this.props.onChangeTargetProcessId}
          handleContainerIdChange={this.props.onChangeTargetContainerId}
          initContainerId={this.props.initTargetContainerId}
          initProcessId={this.props.initProcessId}
        />

        <Button bsStyle="default" onClick={() => this.getDefinitions()}>
          Retrieve definitions from backend
        </Button>

        <div className="form-group">
          <label className="col-sm-2 control-label">
            {this.props.sourceInfo.containerId}
          </label>
          <label className="col-sm-2 control-label">
            {this.props.targetInfo.containerId}
          </label>
        </div>
      </div>
    );
  }
}
