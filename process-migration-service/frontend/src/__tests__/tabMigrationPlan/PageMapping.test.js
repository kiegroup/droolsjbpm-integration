import renderer from "react-test-renderer";
import React from "react";
import { Mockup_processMapping_Info } from "../../component/common/MockupData";
import PageMapping from "../../component/tabMigrationPlan/wizardAddPlan/PageMapping";

test("PageMapping renders correctly using snapshot", () => {
  const mockData = Mockup_processMapping_Info;
  const tree = renderer
    .create(
      <PageMapping
        sourceInfo={mockData.sourceInfo}
        targetInfo={mockData.targetInfo}
        mappings={""}
      />
    )
    .toJSON();
  expect(tree).toMatchSnapshot();
});
