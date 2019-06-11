import React, { Component } from "react";

import { Button } from "patternfly-react";
import { ALERT_TYPE_ERROR } from "patternfly-react/dist/js/components/Alert/AlertConstants";

import Notification from "../../Notification";
import KieServerClient from "../../../clients/kieServerClient";
import ProcessSelector from "./ProcessSelector";

export default class PageDefinition extends Component {
  constructor(props) {
    super(props);
    this.state = {
      errorMsg: "",
      containers: {}
    };
    this.loadProcessIds();
  }

  loadProcessIds = () => {
    KieServerClient.getDefinitions(this.props.kieServerId).then(containers => {
      this.setState({
        containers
      });
    });
  };

  copySourceToTarget = () => {
    this.props.onChangeTarget(
      this.props.source.containerId,
      this.props.source.processId
    );
  };

  getDefinition = (definition, callbackFn) => {
    KieServerClient.getDefinition(
      this.props.kieServerId,
      definition.containerId,
      definition.processId
    )
      .then(res => {
        callbackFn(res);
      })
      .catch(err => {
        const status = err.status;
        let errorMsg;
        if (status === 404) {
          if (definition.containerId !== "" || definition.processId !== "") {
            errorMsg = `Process not found: ${definition.containerId}/${definition.processId}`;
          } else {
            errorMsg = "Provide a valid containerId and processId";
          }
          this.setState({ errorMsg });
          callbackFn("");
        } else {
          err.json().then(json => {
            this.setState({
              errorMsg: `${definition.containerId}/${definition.processId}: ${json.message.string}`
            });
            callbackFn("");
          });
        }
      });
  };

  getDefinitions = () => {
    this.getDefinition(this.props.source, this.props.setSourceDefinition);
    this.getDefinition(this.props.target, this.props.setTargetDefinition);
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
        <ProcessSelector
          type="Source"
          options={this.state.containers}
          containerId={this.props.source.containerId}
          processId={this.props.source.processId}
          onChange={this.props.onChangeSource}
        />
        <Button onClick={this.copySourceToTarget}>Copy Source To Target</Button>
        <p />
        <ProcessSelector
          type="Target"
          options={this.state.containers}
          containerId={this.props.target.containerId}
          processId={this.props.target.processId}
          onChange={this.props.onChangeTarget}
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
