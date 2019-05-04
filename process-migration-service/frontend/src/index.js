import React from "react";
import ReactDOM from "react-dom";

import MainPageWithPfTab from "./component/MainPageWithPfTab";

document.addEventListener("DOMContentLoaded", function() {
  ReactDOM.render(
    React.createElement(MainPageWithPfTab),
    document.getElementById("mount")
  );
});
