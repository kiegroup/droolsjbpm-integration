import React from "react";
import { Button } from "patternfly-react";
import PropTypes from "prop-types";

export default class PageReview extends React.Component {
  constructor(props) {
    super(props);
  }

  doExport = () => {
    const toExport = Object.assign({}, this.props.object);
    delete toExport.id;
    const atag = document.createElement("a");
    const exportableJSON = JSON.stringify(toExport, null, 2);
    const file = new Blob([exportableJSON], { type: "application/json" });
    atag.href = URL.createObjectURL(file);
    atag.download = this.props.exportedFileName + ".json";
    atag.click();
  };

  render() {
    return (
      <React.Fragment>
        <Button onClick={this.doExport}>Export</Button>
        <pre>{JSON.stringify(this.props.object, null, 2)}</pre>
      </React.Fragment>
    );
  }
}

PageReview.defaultProps = {
  exportedFileName: "exported"
};

PageReview.propTypes = {
  object: PropTypes.object.isRequired,
  exportedFileName: PropTypes.string
};
