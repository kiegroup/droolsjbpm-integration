import React, { Component } from "react";
import PropTypes from "prop-types";
import { Button } from "patternfly-react";

import PageMappingDiagrams from "./PageMappingDiagrams";
import PageMappingDropdownNode from "./PageMappingDropdownNode";
import Notification from "../../Notification";
import { ALERT_TYPE_ERROR } from "patternfly-react/dist/js/components/Alert/AlertConstants";
import { Form } from "patternfly-react/dist/js/components/Form";
import Col from "patternfly-react/dist/js/components/Grid/Col";
import FormGroup from "patternfly-react/dist/js/components/Form/FormGroup";
import ControlLabel from "patternfly-react/dist/js/components/Form/ControlLabel";
import Label from "patternfly-react/dist/js/components/Label/Label";

export default class PageMapping extends Component {
  constructor(props) {
    super(props);
    this.state = {
      errorMsg: ""
    };
    // The mappings page doesn't require any validation
    this.props.onIsValid(true);
  }

  handleSourceDropdownChange = option => {
    this.setState({
      sourceNodeTitle: `${option.name} (${option.type})`,
      sourceNode: option,
      sourcePreviousNode: this.state.sourceCurrentNode,
      sourceCurrentNode: option.id
    });
  };

  handleTargetDropdownChange = option => {
    this.setState({
      targetNodeTitle: `${option.name} (${option.type})`,
      targetNode: option,
      targetPreviousNode: this.state.targetCurrentNode,
      targetCurrentNode: option.id
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
      let mappings = this.props.plan.mappings;
      if (mappings === undefined) {
        mappings = {};
      }
      if (mappings[this.state.sourceNode.id]) {
        this.setState({
          errorMsg: "You cannot map the same source node multiple times"
        });
        return;
      }
      mappings[this.state.sourceNode.id] = this.state.targetNode.id;
      this.props.onMappingsChange(mappings);
    }
  };

  deleteMapping = id => {
    const mappings = this.props.plan.mappings;
    delete mappings[id];
    this.props.onMappingsChange(mappings);
  };

  buildMappings = () => {
    const mappingValues = [];
    const mappings = this.props.plan.mappings;
    if (mappings === undefined || mappings === {}) {
      return mappingValues;
    }
    Object.keys(mappings).forEach(key => {
      const filteredSource = this.props.sourceProcess.nodes.filter(
        node => node.id === key
      );
      let sourceNode;
      if (filteredSource.length === 1) {
        sourceNode = filteredSource[0];
      }
      const filteredTarget = this.props.targetProcess.nodes.filter(
        node => node.id === mappings[key]
      );
      let targetNode;
      if (filteredTarget.length === 1) {
        targetNode = filteredTarget[0];
      }
      if (sourceNode && targetNode) {
        mappingValues.push({
          id: sourceNode.id,
          label: sourceNode.name + ">" + targetNode.name
        });
      }
    });
    return mappingValues;
  };

  buildMappingLabels = () =>
    this.buildMappings().map(mapping => (
      <Label
        key={mapping.id}
        style={{ margin: "5px 5px 0 0", display: "inline-block" }}
        onRemoveClick={() => this.deleteMapping(mapping.id)}
      >
        {mapping.label}
      </Label>
    ));

  render() {
    const notification = (
      <Notification
        type={ALERT_TYPE_ERROR}
        message={this.state.errorMsg}
        onDismiss={() => this.setState({ errorMsg: "" })}
      />
    );
    if (
      this.props.sourceProcess.processId !== undefined &&
      this.props.targetProcess.processId !== undefined
    ) {
      return (
        <Form horizontal>
          {this.state.errorMsg && notification}
          <FormGroup controlId="mappingHeader">
            <Col sm={4} componentClass={ControlLabel}>
              Source: {this.props.sourceProcess.containerId} {" / "}
              {this.props.sourceProcess.processId}
            </Col>
            <Col sm={4} componentClass={ControlLabel}>
              Target: {this.props.targetProcess.containerId} {" / "}
              {this.props.targetProcess.processId}
            </Col>
            <Col sm={4} />
          </FormGroup>
          <FormGroup controlId="mappingDropdowns">
            <Col sm={4} className="text-right">
              <PageMappingDropdownNode
                options={this.props.sourceProcess.nodes}
                title={
                  this.state.sourceNodeTitle
                    ? this.state.sourceNodeTitle
                    : "Source Nodes"
                }
                onDropdownChange={this.handleSourceDropdownChange}
              />
            </Col>
            <Col sm={4} className="text-right">
              <PageMappingDropdownNode
                options={this.props.targetProcess.nodes}
                title={
                  this.state.targetNodeTitle
                    ? this.state.targetNodeTitle
                    : "Target Nodes"
                }
                onDropdownChange={this.handleTargetDropdownChange}
              />
            </Col>
            <Col sm={4}>
              <Button bsStyle="primary" onClick={this.handleMapButtonClick}>
                Map these two nodes
              </Button>
            </Col>
          </FormGroup>
          <FormGroup controlId="mappingLabels">
            <Col sm={4} componentClass={ControlLabel}>
              Mappings:
            </Col>
            <Col sm={8}>{this.buildMappingLabels()}</Col>
          </FormGroup>
          <FormGroup controlId="mappingDiagrams">
            <PageMappingDiagrams
              sourceCurrentNode={this.state.sourceCurrentNode}
              sourcePreviousNode={this.state.sourcePreviousNode}
              targetCurrentNode={this.state.targetCurrentNode}
              targetPreviousNode={this.state.targetPreviousNode}
              sourceProcess={this.props.sourceProcess}
              targetProcess={this.props.targetProcess}
            />
          </FormGroup>
        </Form>
      );
    } else {
      //no process info retrieved from backend yet, just display an empty tag to avoid error.
      return <div />;
    }
  }
}

PageMapping.propTypes = {
  plan: PropTypes.object.isRequired,
  sourceProcess: PropTypes.object.isRequired,
  targetProcess: PropTypes.object.isRequired,
  onMappingsChange: PropTypes.func.isRequired,
  onIsValid: PropTypes.func.isRequired
};
