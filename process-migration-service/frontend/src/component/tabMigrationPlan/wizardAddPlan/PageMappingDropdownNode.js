import React from "react";

import { DropdownButton, MenuItem } from "patternfly-react";

export default class PageMappingDropdownNode extends React.Component {
  constructor(props) {
    super(props);
  }

  createMenuItems(options) {
    let menuItems = [];
    for (var i = 0; i < options.length; i++) {
      const name = options[i].name;
      const type = options[i].type;
      if (name !== undefined && name.trim().length > 0) {
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
    return menuItems;
  }

  render() {
    return (
      <div>
        <DropdownButton title={this.props.title} id="PageMappingDropdownButton">
          {this.createMenuItems(this.props.options)}
        </DropdownButton>
      </div>
    );
  }
}
