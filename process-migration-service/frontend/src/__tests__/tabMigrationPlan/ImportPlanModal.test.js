import renderer from "react-test-renderer";
import React from "react";
import ImportPlanModal from "../../component/tabMigrationPlan/ImportPlanModal";

test("ImportPlanModal renders correctly using snapshot", () => {
  const mockFn = jest.fn();
  const tree = renderer.create(<ImportPlanModal onImport={mockFn} />).toJSON();
  expect(tree).toMatchSnapshot();
});
