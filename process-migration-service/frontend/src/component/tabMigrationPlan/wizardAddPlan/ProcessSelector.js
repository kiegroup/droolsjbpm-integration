import React from "react";
import PropTypes from "prop-types";
import { DropdownButton, MenuItem } from "patternfly-react";

export default class ProcessSelector extends React.Component {
  constructor(props) {
    super(props);
  }

  onChange = (containerId, processId) => {
    if (this.props.containerId != containerId) {
      this.props.onChange(containerId, "");
    } else {
      this.props.onChange(containerId, processId);
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
      <div>
        <div className="form-group required">
          <label className="col-sm-2 control-label">
            {this.props.type} Container ID
          </label>
          <div className="col-sm-10">
            <DropdownButton
              title={containerIdTitle}
              id={this.props.type + "-ContainerDropdown"}
            >
              {containerOptions}
            </DropdownButton>
          </div>
        </div>

        <div className="form-group required">
          <label className="col-sm-2 control-label">
            {this.props.type} Process ID
          </label>
          <div className="col-sm-10">
            <DropdownButton
              title={processIdTitle}
              id={this.props.type + "-ProcessDropdown"}
            >
              {processOptions}
            </DropdownButton>
          </div>
        </div>
      </div>
    );
  }
}

ProcessSelector.defaultProps = {
  containerId: "",
  processId: ""
};

ProcessSelector.propTypes = {
  type: PropTypes.string.isRequired,
  options: PropTypes.object.isRequired,
  onChange: PropTypes.func.isRequired,
  containerId: PropTypes.string,
  processId: PropTypes.string
};
