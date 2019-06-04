import React, { Component } from "react";
import axios from "axios";

import { Button } from "patternfly-react";

import PageDefinitionSearchTable from "./PageDefinitionSearchTable";
import { BACKEND_URL } from "../../common/PimConstants";

export default class PageDefinition extends Component {
  constructor(props) {
    super(props);
  }

  copySourceToTarget = () => {
    this.props.onChangeTargetContainerId(this.props.sourceContainerId);
    this.props.onChangeTargetProcessId(this.props.sourceProcessId);
  };

  getDefinitions = () => {
    const servicesUrl = BACKEND_URL + "/kieserver/definitions";
    axios
      .get(servicesUrl, {
        params: {
          sourceProcessId: this.props.sourceProcessId,
          sourceContainerId: this.props.sourceContainerId,
          targetProcessId: this.props.targetProcessId,
          targetContainerId: this.props.targetContainerId,
          kieServerId: this.props.kieServerIds
        }
      })
      .then(res => {
        this.props.setInfo(res.data.sourceInfo, res.data.targetInfo);
      });
  };

  render() {
    return (
      <div className="form-horizontal">
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

        <Button onClick={this.copySourceToTarget}>
          {" "}
          Copy Source To Target
        </Button>
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
