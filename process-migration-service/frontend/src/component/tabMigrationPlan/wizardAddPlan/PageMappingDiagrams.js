import React from "react";
import PropTypes from "prop-types";
import { Button } from "patternfly-react";

import PageMappingDiagramSvgPan from "./PageMappingDiagramSvgPan";
import { ControlLabel } from "patternfly-react/dist/js/components/Form";
import Col from "patternfly-react/dist/js/components/Grid/Col";

export default class PageMappingDiagrams extends React.Component {
  constructor(props) {
    super(props);
    this.state = {
      showSourceDiagram: false,
      showTargetDiagram: false
    };
  }

  toggleShowSourceDiagram = () => {
    this.setState({
      showSourceDiagram: !this.state.showSourceDiagram
    });
  };

  toggleShowTargetDiagram = () => {
    this.setState({
      showTargetDiagram: !this.state.showTargetDiagram
    });
  };

  render() {
    return (
      <React.Fragment>
        <Col sm={4} className="text-right">
          <Button onClick={this.toggleShowSourceDiagram}>
            {this.state.showSourceDiagram ? "Hide" : "Show"} Source Diagram
          </Button>
        </Col>
        <Col sm={4} className="text-right">
          <Button onClick={this.toggleShowTargetDiagram}>
            {this.state.showTargetDiagram ? "Hide" : "Show"} Target Diagram
          </Button>
        </Col>
        {this.state.showSourceDiagram && (
          <div className="col-sm-12">
            <Col componentClass={ControlLabel}>Source diagram</Col>
            <PageMappingDiagramSvgPan
              svg={this.props.sourceProcess.svgFile}
              previous={this.props.sourcePreviousNode}
              current={this.props.sourceCurrentNode}
            />
          </div>
        )}
        {this.state.showTargetDiagram && (
          <div className="col-sm-12">
            <Col componentClass={ControlLabel}>Target diagram</Col>
            <PageMappingDiagramSvgPan
              svg={this.props.targetProcess.svgFile}
              previous={this.props.targetPreviousNode}
              current={this.props.targetCurrentNode}
            />
          </div>
        )}
      </React.Fragment>
    );
  }
}
PageMappingDiagrams.defaultProps = {
  sourcePreviousNode: "",
  sourceCurrentNode: "",
  targetPreviousNode: "",
  targetCurrentNode: ""
};

PageMappingDiagrams.propTypes = {
  sourceProcess: PropTypes.object.isRequired,
  targetProcess: PropTypes.object.isRequired,
  sourcePreviousNode: PropTypes.string,
  sourceCurrentNode: PropTypes.string,
  targetPreviousNode: PropTypes.string,
  targetCurrentNode: PropTypes.string
};
