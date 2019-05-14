import React, { Component } from "react";
import { Button } from "patternfly-react";

export default class PageReview extends Component {
  onDownloadClick = () => {
    var dataStr =
      "data:text/json;charset=utf-8," +
      encodeURIComponent(this.props.inputJsonStr);
    var dlAnchorElem = document.getElementById("downloadAnchorElem");
    dlAnchorElem.setAttribute("href", dataStr);
    dlAnchorElem.setAttribute("download", "migrationPlan.json");
    dlAnchorElem.click();
  };

  render() {
    return (
      <div>
        <div className="form-group">
          <Button onClick={this.onDownloadClick}>Export</Button>
          <pre>{this.props.inputJsonStr}</pre>
          <a id="downloadAnchorElem" style={{ display: "none" }} />
        </div>
      </div>
    );
  }
}
