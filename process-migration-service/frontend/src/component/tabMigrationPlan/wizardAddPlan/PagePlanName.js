import React, { Component } from "react";

export default class PagePlanName extends Component {
  componentDidMount() {
    if (document.getElementById("id_PagePlanName_name") != null) {
      document.getElementById("id_PagePlanName_name").value = this.props.name;
      document.getElementById(
        "id_PagePlanName_description"
      ).value = this.props.description;
    }
  }

  render() {
    return (
      <div className="form-horizontal">
        <div className="form-group required">
          <label className="col-sm-2 control-label">Name</label>
          <div className="col-sm-10">
            <input
              type="text"
              name="name"
              className="form-control"
              id="id_PagePlanName_name"
            />
          </div>
        </div>
        <div className="form-group">
          <label className="col-sm-2 control-label">Description</label>
          <div className="col-sm-10">
            <textarea
              name="description"
              rows="2"
              className="form-control"
              id="id_PagePlanName_description"
            />
          </div>
        </div>
      </div>
    );
  }
}
