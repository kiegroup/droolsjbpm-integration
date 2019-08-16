import React from "react";
import PropTypes from "prop-types";

import { DropdownButton, MenuItem } from "patternfly-react";

export default class PageMappingDropdownNode extends React.Component {
  constructor(props) {
    super(props);
  }

  createMenuItems(options) {
    let menuItems = [];
    if (options !== undefined && options !== null) {
      options = options.sort(
        (a, b) =>
          a.name !== undefined &&
          a.name !== null &&
          a.name.localeCompare(b.name)
      );
      for (var i = 0; i < options.length; i++) {
        const name = options[i].name;
        const type = options[i].type;
        if (name !== undefined && name !== null && name.trim().length > 0) {
          menuItems.push(
            <MenuItem
              key={i}
              eventKey={options[i]}
              onSelect={this.props.onDropdownChange}
            >
              {name} ({type})
            </MenuItem>
          );
        }
      }
    }
    return menuItems;
  }

  render() {
    return (
      <DropdownButton title={this.props.title} id="nodeMappingDropdown">
        {this.createMenuItems(this.props.options)}
      </DropdownButton>
    );
  }
}

PageMappingDropdownNode.propTypes = {
  title: PropTypes.string.isRequired,
  options: PropTypes.array.isRequired,
  onDropdownChange: PropTypes.func.isRequired
};
