import React, { Component } from "react";

export default class PageDefinitionSearchTable extends Component {
  handleProcessIdChange(event) {
    this.props.handleProcessIdChange(event.target.value);
  }

  handleContainerIdChange(event) {
    this.props.handleContainerIdChange(event.target.value);
  }

  render() {
    return (
      <div>
        <div className="form-group required">
          <label className="col-sm-2 control-label">
            {this.props.tableHeader} Container ID
          </label>
          <div className="col-sm-10">
            <input
              className="form-control"
              type="text"
              name="ContainerId"
              value={this.props.containerId}
              onChange={e => this.handleContainerIdChange(e)}
            />
          </div>
        </div>

        <div className="form-group required">
          <label className="col-sm-2 control-label">
            {this.props.tableHeader} Process ID
          </label>
          <div className="col-sm-10">
            <input
              className="form-control"
              type="text"
              name="processId"
              value={this.props.processId}
              onChange={e => this.handleProcessIdChange(e)}
            />
          </div>
        </div>
      </div>
    );
  }
}
