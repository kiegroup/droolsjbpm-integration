import React from "react";
import PropTypes from "prop-types";

import { ReactSVGPanZoom } from "react-svg-pan-zoom";
import { SvgLoader, SvgProxy } from "react-svgmt";

export default class PageMappingDiagramSvgPan extends React.Component {
  constructor(props) {
    super(props);
    this.Viewer = null;
  }

  parseSvgSize = () => {
    const svgDoc = new DOMParser()
      .parseFromString(this.props.svg, "text/xml")
      .getElementsByTagName("svg")[0];
    const width = svgDoc.hasAttribute("width")
      ? parseInt(svgDoc.getAttribute("width"))
      : 2000;
    const height = svgDoc.hasAttribute("height")
      ? parseInt(svgDoc.getAttribute("height"))
      : 1400;

    return { width, height };
  };

  componentDidMount = () => {
    this.Viewer.zoom(0, 0, 1);
  };

  getSelector = id => {
    return "#" + id + "_shapeType_BACKGROUND";
  };

  render() {
    const { width, height } = this.parseSvgSize();
    return (
      <div>
        <ReactSVGPanZoom
          style={{ border: "1px solid black" }}
          width={800}
          height={400}
          ref={Viewer => (this.Viewer = Viewer)}
          detectWheel={false}
        >
          <svg width={width} height={height}>
            <SvgLoader svgXML={this.props.svg}>
              <SvgProxy
                selector={this.getSelector(this.props.previous)}
                fill="white"
              />
              <SvgProxy
                selector={this.getSelector(this.props.current)}
                fill="yellow"
              />
            </SvgLoader>
          </svg>
        </ReactSVGPanZoom>
      </div>
    );
  }
}

PageMappingDiagramSvgPan.propTypes = {
  svg: PropTypes.string.isRequired,
  previous: PropTypes.string.isRequired,
  current: PropTypes.string.isRequired
};
