import React, { Component } from "react";
import { Button } from "patternfly-react";

import PageMappingDiagrams from "./PageMappingDiagrams";
import PageMappingDropdownNode from "./PageMappingDropdownNode";
import Notification from "../../Notification";
import { ALERT_TYPE_ERROR } from "patternfly-react/dist/js/components/Alert/AlertConstants";

export default class PageMapping extends Component {
  //need to provide some dummy data for the selectors which are used in svg pan, because
  //it's required and can't be empty, and need to start with "_"
  //can't do the initial in the sub components like PageMappingDiagramsSvgPan because they
  //are passed in through props
  constructor(props) {
    super(props);
    this.state = {
      errorMsg: "",
      showSourceDiagram: false,
      showTargetDiagram: false,
      sourceCurrentSelector: "_Dummy123",
      sourcePreviousSelector: "_Dummy123",
      targetCurrentSelector: "_Dummy123",
      targetPreviousSelector: "_Dummy123"
    };
  }

  handleSourceDiagramButtonClick = () => {
    this.setState({
      showSourceDiagram: !this.state.showSourceDiagram
    });
  };

  handleTargetDiagramButtonClick = () => {
    this.setState({
      showTargetDiagram: !this.state.showTargetDiagram
    });
  };

  handleSourceDropdownChange = option => {
    this.setState({
      sourceNodeTitle: `${option.name} (${option.type})`,
      sourceNode: option,
      sourcePreviousSelector: this.state.sourceCurrentSelector,
      sourceCurrentSelector: "#" + option.id + "_shapeType_BACKGROUND",
      showSourceDiagram: true,
      showTargetDiagram: false
    });
  };

  handleTargetDropdownChange = option => {
    this.setState({
      targetNodeTitle: `${option.name} (${option.type})`,
      targetNode: option,
      targetPreviousSelector: this.state.targetCurrentSelector,
      targetCurrentSelector: "#" + option.id + "_shapeType_BACKGROUND",
      showSourceDiagram: false,
      showTargetDiagram: true
    });
  };

  handleMapButtonClick = () => {
    if (
      this.state.sourceNode !== undefined &&
      this.state.targetNode !== undefined
    ) {
      if (this.state.sourceNode.type !== this.state.targetNode.type) {
        this.setState({
          errorMsg: "Source and Target nodes must be of the same type"
        });
        return;
      }

      var currentNodeMapping =
        '"' +
        this.state.sourceNode.id +
        '"' +
        ":" +
        '"' +
        this.state.targetNode.id +
        '"';

      var currentInputValue = this.props.mappings;
      //remove {} before add new node mapping values
      currentInputValue = currentInputValue.replace(/{/g, "");
      currentInputValue = currentInputValue.replace(/}/g, "");
      if (currentInputValue.length > 0) {
        currentInputValue = currentInputValue + "," + currentNodeMapping;
      } else {
        currentInputValue = currentNodeMapping;
      }
      currentInputValue = "{" + currentInputValue + "}";
      this.props.onMappingsChange(currentInputValue);
    }
  };

  mappingButton = () => {
    return (
      <Button bsStyle="primary" onClick={this.handleMapButtonClick}>
        Map these two nodes
      </Button>
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
    if (
      this.props.sourceInfo !== null &&
      this.props.sourceInfo !== "" &&
      this.props.targetInfo !== null &&
      this.props.targetInfo !== ""
    ) {
      return (
        <div className="form-horizontal">
          {this.state.errorMsg && notification}
          <div className="form-group">
            <label>Source: {this.props.sourceInfo.processId}</label>
            <PageMappingDropdownNode
              options={this.props.sourceInfo.nodes}
              title={
                this.state.sourceNodeTitle
                  ? this.state.sourceNodeTitle
                  : "Source Nodes"
              }
              onDropdownChange={this.handleSourceDropdownChange}
            />
            <br />
            <label>Target: {this.props.targetInfo.processId}</label>
            <PageMappingDropdownNode
              options={this.props.targetInfo.nodes}
              title={
                this.state.targetNodeTitle
                  ? this.state.targetNodeTitle
                  : "Target Nodes"
              }
              onDropdownChange={this.handleTargetDropdownChange}
            />
            <br />
            {this.mappingButton()}
          </div>

          <div className="form-group">
            <label>
              Use the text field below to update mappings directly (e.g. to
              delete an incorrect mapping)
            </label>

            <textarea
              className="form-control"
              name="mappings"
              id="nodeMappingField"
              rows="2"
              value={this.props.mappings}
              onChange={this.props.onMappingsChange}
            />
          </div>

          <PageMappingDiagrams
            sourceCurrentSelector={this.state.sourceCurrentSelector}
            sourcePreviousSelector={this.state.sourcePreviousSelector}
            targetCurrentSelector={this.state.targetCurrentSelector}
            targetPreviousSelector={this.state.targetPreviousSelector}
            sourceDiagramButtonClick={this.handleSourceDiagramButtonClick}
            targetDiagramButtonClick={this.handleTargetDiagramButtonClick}
            showSourceDiagram={this.state.showSourceDiagram}
            showTargetDiagram={this.state.showTargetDiagram}
            sourceInfo={this.props.sourceInfo}
            targetInfo={this.props.targetInfo}
          />
        </div>
      );
    } else {
      //no process info retrieved from backend yet, just display an empty tag to avoid error.
      return <div />;
    }
  }
}
