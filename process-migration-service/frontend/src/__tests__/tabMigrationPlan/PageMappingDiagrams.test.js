import renderer from "react-test-renderer";
import React from "react";
import { Mockup_processMapping_Info } from "../../component/common/MockupData";
import PageMappingDiagrams from "../../component/tabMigrationPlan/wizardAddPlan/PageMappingDiagrams";

test("PageMappingDiagrams renders correctly using snapshot", () => {
  const mockData = Mockup_processMapping_Info;
  const myMock = jest.fn();
  const tree = renderer
    .create(
      <PageMappingDiagrams
        sourceCurrentSelector=""
        sourcePreviousSelector=""
        targetCurrentSelector=""
        targetPreviousSelector=""
        sourceDiagramButtonClick={myMock}
        targetDiagramButtonClick={myMock}
        sourceDiagramShown="false"
        targetDiagramShown="false"
        sourceInfo={mockData.sourceInfo}
        targetInfo={mockData.targetInfo}
      />
    )
    .toJSON();
  expect(tree).toMatchSnapshot();
});
