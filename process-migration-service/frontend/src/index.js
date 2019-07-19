import React from "react";
import ReactDOM from "react-dom";
import "patternfly/dist/css/patternfly.min.css";
import "patternfly/dist/css/patternfly-additions.min.css";
import "react-datetime/css/react-datetime.css";
import "./css/pim-main.css";

import MainPage from "./component/MainPage";

document.addEventListener("DOMContentLoaded", function() {
  ReactDOM.render(
    React.createElement(MainPage),
    document.getElementById("mount")
  );
});
