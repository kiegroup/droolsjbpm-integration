import renderer from "react-test-renderer";
import React from "react";
import PagePlanName from "../../component/tabMigrationPlan/wizardAddPlan/PagePlanName";

test("PagePlanName renders correctly using snapshot", () => {
  const tree = renderer
    .create(<PagePlanName name={"testName"} description={"testDescription"} />)
    .toJSON();
  expect(tree).toMatchSnapshot();
});
