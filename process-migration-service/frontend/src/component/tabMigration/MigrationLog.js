import React, { Component } from "react";

export default class MigrationLog extends Component {
  render() {
    let flag = "" + this.props.log.successful;
    let logs = this.props.log.logs.map((log, i) => <li key={i}>{log}</li>);
    return (
      <div className="form-horizontal">
        <div className="form-group required">
          <label className="col-sm-2 control-label">ID</label>
          <div className="col-sm-10">{this.props.log.id}</div>
        </div>
        <div className="form-group">
          <label className="col-sm-2 control-label">Start Date</label>
          <div className="col-sm-10">{this.props.log.startDate}</div>
        </div>
        <div className="form-group">
          <label className="col-sm-2 control-label">End Date</label>
          <div className="col-sm-10">{this.props.log.endDate}</div>
        </div>
        <div className="form-group">
          <label className="col-sm-2 control-label">Successful</label>
          <div className="col-sm-10">{flag}</div>
        </div>
        <div className="form-group">
          <label className="col-sm-2 control-label">Logs</label>
          <div className="col-sm-10">{logs}</div>
        </div>
      </div>
    );
  }
}
