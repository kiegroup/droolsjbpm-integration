import React from "react";
import ReactDOM from "react-dom";
import "patternfly/dist/css/patternfly.min.css";
import "patternfly/dist/css/patternfly-additions.min.css";
import "react-datetime/css/react-datetime.css";
import "./css/pim-main.css";

import MainPageWithPfTab from "./component/MainPageWithPfTab";

document.addEventListener("DOMContentLoaded", function() {
  ReactDOM.render(
    React.createElement(MainPageWithPfTab),
    document.getElementById("mount")
  );
});
