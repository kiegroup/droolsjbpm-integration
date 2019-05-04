import renderer from "react-test-renderer";
import React from "react";
import { Mockup_processMapping_Info } from "../../component/common/MockupData";
import PageDefinition from "../../component/tabMigrationPlan/wizardAddPlan/PageDefinition";

test("PageDefinition renders correctly using snapshot", () => {
  const mockData = Mockup_processMapping_Info;
  const myMock = jest.fn();
  const tree = renderer
    .create(
      <PageDefinition
        sourceInfo={mockData.sourceInfo}
        targetInfo={mockData.targetInfo}
        setInfo={myMock}
      />
    )
    .toJSON();
  expect(tree).toMatchSnapshot();
});
