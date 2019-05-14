import React from "react";
import ReactDOM from "react-dom";
import MigrationPlans from "../../component/tabMigrationPlan/MigrationPlans";

describe("Main Page for Migration Plan", () => {
  it("renders without crashing", () => {
    const div = document.createElement("div");
    ReactDOM.render(<MigrationPlans />, div);
  });
});
