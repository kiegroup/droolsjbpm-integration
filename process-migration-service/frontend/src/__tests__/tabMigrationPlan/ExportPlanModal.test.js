import renderer from "react-test-renderer";
import React from "react";
import ExportPlanModal from "../../component/tabMigrationPlan/ExportPlanModal";
import plan from "../../../mock_data/plan.json";

test("ExportPlanModal renders correctly using snapshot", () => {
  const tree = renderer.create(<ExportPlanModal plan={plan} />).toJSON();
  expect(tree).toMatchSnapshot();
});
