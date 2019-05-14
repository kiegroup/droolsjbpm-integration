import React, { Component } from "react";

import MigrationLog from "./MigrationLog";

export default class PageViewMigrationLogs extends Component {
  render() {
    function DisplayMessage(props) {
      if (props.migrationLogs != null && props.migrationLogs != "") {
        return (
          <div>
            {props.migrationLogs.map(log => (
              <MigrationLog log={log} key={log.id} />
            ))}
          </div>
        );
      } else {
        return <div> No detail migration logs for this status</div>;
      }
    }

    return (
      <div>
        <DisplayMessage migrationLogs={this.props.migrationLogs} />
      </div>
    );
  }
}
