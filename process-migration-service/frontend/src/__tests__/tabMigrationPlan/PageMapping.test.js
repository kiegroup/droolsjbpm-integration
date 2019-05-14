import renderer from "react-test-renderer";
import React from "react";
import PageMapping from "../../component/tabMigrationPlan/wizardAddPlan/PageMapping";
import definitions from "../../../mock_data/definitions.json";

test("PageMapping renders correctly using snapshot", () => {
  const tree = renderer
    .create(
      <PageMapping
        sourceInfo={definitions.sourceInfo}
        targetInfo={definitions.targetInfo}
        mappings={""}
      />
    )
    .toJSON();
  expect(tree).toMatchSnapshot();
});
