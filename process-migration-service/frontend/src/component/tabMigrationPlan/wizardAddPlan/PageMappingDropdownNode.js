import React from "react";

import { DropdownButton, MenuItem } from "patternfly-react";

export default class PageMappingDropdownNode extends React.Component {
  constructor(props) {
    super(props);
    this.state = {
      title: this.props.title
    };
    this.handleChange = this.handleChange.bind(this);
  }

  handleChange(option) {
    const newTitle = this.props.title + ":" + option;
    this.setState({ title: newTitle });
    this.props.onDropdownChange(option);
  }

  createMenuItems(options) {
    let menuItems = [];
    for (var i = 0; i < options.length; i++) {
      const value = options[i].value;
      const label = options[i].label;
      menuItems.push(
        <MenuItem key={i} eventKey={value} onSelect={this.handleChange}>
          {label}
        </MenuItem>
      );
    }
    return menuItems;
  }

  render() {
    //const defaultOption = this.state.selected;
    //const placeHolderValue = typeof this.state.selected === 'string' ? this.state.selected : this.state.selected.value

    //const title = 'Primary';

    return (
      <div>
        <DropdownButton title={this.state.title} id="PageMappingDropdownButton">
          {this.createMenuItems(this.props.options)}
        </DropdownButton>
      </div>
    );
  }
}
