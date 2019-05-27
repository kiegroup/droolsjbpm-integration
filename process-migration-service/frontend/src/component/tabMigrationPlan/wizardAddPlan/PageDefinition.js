import React, { Component } from "react";
import axios from "axios";

import { Button } from "patternfly-react";

import PageDefinitionSearchTable from "./PageDefinitionSearchTable";
import { BACKEND_URL } from "../../common/PimConstants";

export default class PageDefinition extends Component {
  constructor(props) {
    super(props);

    this.state = {
      sourceProcessId: "",
      sourceContainerId: "",
      targetProcessId: "",
      targetContainerId: ""
    };
  }

  //This is used in add plan wizard (for edit plan) to load the inital data to form fields
  componentDidMount() {
    if (this.props.initSourceContainerId != null) {
      this.setState({
        sourceProcessId: this.props.initProcessId,
        sourceContainerId: this.props.initSourceContainerId,
        targetProcessId: this.props.initProcessId,
        targetContainerId: this.props.initTargetContainerId
      });
    }
  }

  //this function is for helping test only
  fillWithTestRecord = () => {
    this.setState({
      sourceProcessId: "evaluation",
      sourceContainerId: "evaluation_1.0.0-SNAPSHOT",
      targetProcessId: "Mortgage_Process.MortgageApprovalProcess",
      targetContainerId: "mortgage-process_1.0.0-SNAPSHOT"
    });
  };

  copySourceToTarget = () => {
    this.setState({
      targetProcessId: this.state.sourceProcessId,
      targetContainerId: this.state.sourceContainerId
    });
  };

  handleSourceProcessIdChange = value => {
    this.setState({ sourceProcessId: value });
  };

  handleSourceContainerIdChange = value => {
    this.setState({ sourceContainerId: value });
  };

  handleTargetProcessIdChange = value => {
    this.setState({ targetProcessId: value });
  };

  handleTargetContainerIdChange = value => {
    this.setState({ targetContainerId: value });
  };

  getDefinitions = () => {
    const servicesUrl = BACKEND_URL + "/kieserver/definitions";
    axios
      .get(servicesUrl, {
        params: {
          sourceProcessId: this.state.sourceProcessId,
          sourceContainerId: this.state.sourceContainerId,
          targetProcessId: this.state.targetProcessId,
          targetContainerId: this.state.targetContainerId,
          kieServerId: this.props.kieServerIds
        }
      })
      .then(res => {
        this.props.setInfo(res.data.sourceInfo, res.data.targetInfo);

        var input = document.getElementById("hiddenField_sourceContainerId");
        var containerId = this.state.sourceContainerId;
        var nativeInputValueSetter = Object.getOwnPropertyDescriptor(
          window.HTMLInputElement.prototype,
          "value"
        ).set;
        nativeInputValueSetter.call(input, containerId);
        //once fired the event, this currentInputValue will be saved in the wizard form's values
        var ev = new Event("input", { bubbles: true });
        input.dispatchEvent(ev);

        input = document.getElementById("hiddenField_targetContainerId");
        containerId = this.state.targetContainerId;
        nativeInputValueSetter = Object.getOwnPropertyDescriptor(
          window.HTMLInputElement.prototype,
          "value"
        ).set;
        nativeInputValueSetter.call(input, containerId);
        //once fired the event, this currentInputValue will be saved in the wizard form's values
        ev = new Event("input", { bubbles: true });
        input.dispatchEvent(ev);

        input = document.getElementById("hiddenField_targetProcessId");
        var processId = this.state.targetProcessId;
        nativeInputValueSetter = Object.getOwnPropertyDescriptor(
          window.HTMLInputElement.prototype,
          "value"
        ).set;
        nativeInputValueSetter.call(input, processId);
        //once fired the event, this currentInputValue will be saved in the wizard form's values
        ev = new Event("input", { bubbles: true });
        input.dispatchEvent(ev);
      })
      .catch(() => {
        this.props.setInfo("", "");
      });
  };

  render() {
    return (
      <div className="form-horizontal">
        {/*
        <Button onClick={this.fillWithTestRecord}>
          quick fill for testing
        </Button>
         */}
        <p />
        <PageDefinitionSearchTable
          tableHeader="Source "
          processId={this.state.sourceProcessId}
          containerId={this.state.sourceContainerId}
          handleProcessIdChange={this.handleSourceProcessIdChange}
          handleContainerIdChange={this.handleSourceContainerIdChange}
          initContainerId={this.props.initSourceContainerId}
          initProcessId={this.props.initProcessId}
        />

        <Button onClick={this.copySourceToTarget}>
          {" "}
          Copy Source To Target
        </Button>
        <p />
        <PageDefinitionSearchTable
          tableHeader="Target "
          processId={this.state.targetProcessId}
          containerId={this.state.targetContainerId}
          handleProcessIdChange={this.handleTargetProcessIdChange}
          handleContainerIdChange={this.handleTargetContainerIdChange}
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
        <div style={{ display: "none" }}>
          <input
            type="text"
            className="form-control"
            name="sourceContainerId"
            id="hiddenField_sourceContainerId"
          />
          <input
            type="text"
            className="form-control"
            name="targetContainerId"
            id="hiddenField_targetContainerId"
          />
          <input
            type="text"
            className="form-control"
            name="targetProcessId"
            id="hiddenField_targetProcessId"
          />
        </div>
      </div>
    );
  }
}
