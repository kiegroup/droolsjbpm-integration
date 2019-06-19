import React, { Component } from "react";
import { Button } from "patternfly-react";

import PageMappingDiagrams from "./PageMappingDiagrams";
import PageMappingDropdownNode from "./PageMappingDropdownNode";

export default class PageMapping extends Component {
  //need to provide some dummy data for the selectors which are used in svg pan, because
  //it's required and can't be empty, and need to start with "_"
  //can't do the initial in the sub components like PageMappingDiagramsSvgPan because they
  //are passed in through props
  constructor(props) {
    super(props);
    this.state = {
      sourceNodeStr: "",
      targetNodeStr: "",
      sourceDiagramShown: false,
      targetDiagramShown: false,
      sourceCurrentSelector: "_Dummy123",
      sourcePreviousSelector: "_Dummy123",
      targetCurrentSelector: "_Dummy123",
      targetPreviousSelector: "_Dummy123"
    };
  }

  componentDidUpdate() {
    var mappingField = document.getElementById("nodeMappingField");
    if (mappingField != null) {
      if (this.props.mappings !== null && this.props.mappings != "") {
        if (mappingField.value === null || mappingField.value === "") {
          mappingField.value = JSON.stringify(this.props.mappings);
        }
      }
    }
  }

  handleSourceDiagramButtonClick = () => {
    this.setState({
      sourceDiagramShown: !this.state.sourceDiagramShown
    });
  };

  handleTargetDiagramButtonClick = () => {
    this.setState({
      targetDiagramShown: !this.state.targetDiagramShown
    });
  };

  handleSourceDropdownChange = option => {
    let tmpPreviousSelector = this.state.sourceCurrentSelector;
    let tmpCurrentSelector = "#" + option + "_shapeType_BACKGROUND";
    this.setState({
      sourceNodeStr: option,
      sourcePreviousSelector: tmpPreviousSelector,
      sourceCurrentSelector: tmpCurrentSelector,
      sourceDiagramShown: true,
      targetDiagramShown: false
    });
  };

  handleTargetDropdownChange = option => {
    let tmpPreviousSelector = this.state.targetCurrentSelector;
    let tmpCurrentSelector = "#" + option + "_shapeType_BACKGROUND";

    this.setState({
      targetNodeStr: option,
      targetPreviousSelector: tmpPreviousSelector,
      targetCurrentSelector: tmpCurrentSelector,
      sourceDiagramShown: false,
      targetDiagramShown: true
    });
  };

  handleMapButtonClick = () => {
    if (
      this.state.sourceNodeStr.length > 0 &&
      this.state.targetNodeStr.length > 0
    ) {
      var currentNodeMapping =
        '"' +
        this.state.sourceNodeStr +
        '"' +
        ":" +
        '"' +
        this.state.targetNodeStr +
        '"';

      var input = document.getElementById("nodeMappingField");
      var currentInputValue = input.value;
      //remove {} before add new node mapping values
      currentInputValue = currentInputValue.replace(/{/g, "");
      currentInputValue = currentInputValue.replace(/}/g, "");
      if (currentInputValue.length > 0) {
        currentInputValue = currentInputValue + "," + currentNodeMapping;
      } else {
        currentInputValue = currentNodeMapping;
      }

      currentInputValue = "{" + currentInputValue + "}";

      var nativeInputValueSetter = Object.getOwnPropertyDescriptor(
        window.HTMLTextAreaElement.prototype,
        "value"
      ).set;
      nativeInputValueSetter.call(input, currentInputValue);

      //once fired the event, this currentInputValue will be saved in the wizard form's values
      var ev2 = new Event("input", { bubbles: true });
      input.dispatchEvent(ev2);
    }
  };

  MappingButton() {
    return (
      <Button bsStyle="primary" onClick={this.handleMapButtonClick}>
        Map these two nodes
      </Button>
    );
  }

  render() {
    const sourceValues = this.props.sourceInfo.values;
    const sourceLabels = this.props.sourceInfo.labels;
    const sourceNode = [];
    if (
      this.props.sourceInfo !== null &&
      this.props.sourceInfo !== "" &&
      this.props.targetInfo !== null &&
      this.props.targetInfo !== ""
    ) {
      for (var i = 0; i < sourceValues.length; i++) {
        sourceNode.push({ value: sourceValues[i], label: sourceLabels[i] });
      }

      const targetValues = this.props.targetInfo.values;
      const targetLabels = this.props.targetInfo.labels;
      const targetNode = [];
      for (var j = 0; j < targetValues.length; j++) {
        targetNode.push({ value: targetValues[j], label: targetLabels[j] });
      }

      return (
        <div className="form-horizontal">
          <div className="form-group">
            <label>Source: {this.props.sourceInfo.processId}</label>
            <PageMappingDropdownNode
              options={sourceNode}
              title="Source Nodes "
              onDropdownChange={this.handleSourceDropdownChange}
            />
            <br />
            <label>Target: {this.props.targetInfo.processId}</label>
            <PageMappingDropdownNode
              options={targetNode}
              title="Target Nodes "
              onDropdownChange={this.handleTargetDropdownChange}
            />
            <br />
            {this.MappingButton()}
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
            />
          </div>

          <PageMappingDiagrams
            sourceCurrentSelector={this.state.sourceCurrentSelector}
            sourcePreviousSelector={this.state.sourcePreviousSelector}
            targetCurrentSelector={this.state.targetCurrentSelector}
            targetPreviousSelector={this.state.targetPreviousSelector}
            sourceDiagramButtonClick={this.handleSourceDiagramButtonClick}
            targetDiagramButtonClick={this.handleTargetDiagramButtonClick}
            sourceDiagramShown={this.state.sourceDiagramShown}
            targetDiagramShown={this.state.targetDiagramShown}
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
