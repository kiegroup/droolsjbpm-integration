import React from "react";
import PropTypes from "prop-types";
import { DropdownButton, MenuItem } from "patternfly-react";
import { FormGroup } from "patternfly-react/dist/js/components/Form";
import ControlLabel from "patternfly-react/dist/js/components/Form/ControlLabel";
import { Col } from "patternfly-react/dist/js/components/Grid";
import HelpBlock from "patternfly-react/dist/js/components/Form/HelpBlock";

export default class ProcessSelector extends React.Component {
  constructor(props) {
    super(props);
  }

  onChange = (containerId, processId) => {
    if (this.props.containerId != containerId) {
      this.props.onChange({ containerId });
    } else {
      this.props.onChange({ containerId, processId });
    }
  };

  createContainerOptions = () => {
    const containerIds = Object.keys(this.props.options);
    const menuItems = [];
    containerIds.forEach((id, i) => {
      menuItems.push(
        <MenuItem
          key={i}
          eventKey={id}
          onSelect={value => this.onChange(value, this.props.processId)}
        >
          {id}
        </MenuItem>
      );
    });
    return menuItems;
  };

  createProcessOptions = () => {
    const processes = this.props.options[this.props.containerId];
    if (processes === undefined) {
      return [];
    }
    const menuItems = [];
    processes.forEach((id, i) => {
      menuItems.push(
        <MenuItem
          key={i}
          eventKey={id}
          onSelect={value => this.onChange(this.props.containerId, value)}
        >
          {id}
        </MenuItem>
      );
    });
    return menuItems;
  };

  render() {
    const containerOptions = this.createContainerOptions();
    const containerIdTitle = this.props.containerId
      ? this.props.containerId
      : "Select a container";
    const processOptions = this.createProcessOptions();
    const processIdTitle = this.props.processId
      ? this.props.processId
      : "Select a process";
    return (
      <React.Fragment>
        <Col sm={12}>
          <h3>{this.props.type}</h3>
        </Col>
        <FormGroup
          controlId={this.props.type + "dropdown_containerid"}
          className="required"
        >
          <Col componentClass={ControlLabel} sm={3}>
            Container:
          </Col>
          <Col sm={9}>
            <DropdownButton
              title={containerIdTitle}
              id={this.props.type + "-ContainerDropdown"}
            >
              {containerOptions}
            </DropdownButton>
          </Col>
        </FormGroup>
        <FormGroup
          controlId={this.props.type + "dropdown_processid"}
          className="required"
          validationState={this.props.validationError === "" ? null : "error"}
        >
          <Col componentClass={ControlLabel} sm={3}>
            Process:
          </Col>
          <Col sm={9}>
            <DropdownButton
              title={processIdTitle}
              id={this.props.type + "-ProcessDropdown"}
            >
              {processOptions}
            </DropdownButton>
            {this.props.validationError && (
              <HelpBlock>{this.props.validationError}</HelpBlock>
            )}
          </Col>
        </FormGroup>
      </React.Fragment>
    );
  }
}

ProcessSelector.defaultProps = {
  containerId: "",
  processId: "",
  validationError: ""
};

ProcessSelector.propTypes = {
  type: PropTypes.string.isRequired,
  options: PropTypes.object.isRequired,
  onChange: PropTypes.func.isRequired,
  containerId: PropTypes.string,
  processId: PropTypes.string,
  validationError: PropTypes.string
};
