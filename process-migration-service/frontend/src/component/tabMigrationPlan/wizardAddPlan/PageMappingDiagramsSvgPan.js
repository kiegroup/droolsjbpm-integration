import React, { Component } from "react";

import { ReactSVGPanZoom } from "react-svg-pan-zoom";
import { SvgLoader, SvgProxy } from "react-svgmt";

export default class PageMappingDiagramsSvgPan extends Component {
  constructor(props, context) {
    super(props, context);
    this.Viewer = null;
  }

  componentDidMount() {
    this.Viewer.fitToViewer();
  }

  render() {
    return (
      <div>
        <ReactSVGPanZoom
          style={{ border: "1px solid black" }}
          width={800}
          height={400}
          ref={Viewer => (this.Viewer = Viewer)}
        >
          <svg width={2000} height={1000}>
            <SvgLoader svgXML={this.props.svgcontents}>
              <SvgProxy selector={this.props.previousSelector} fill="white" />
              <SvgProxy selector={this.props.currentSelector} fill="yellow" />
            </SvgLoader>
          </svg>
        </ReactSVGPanZoom>
      </div>
    );
  }
}
