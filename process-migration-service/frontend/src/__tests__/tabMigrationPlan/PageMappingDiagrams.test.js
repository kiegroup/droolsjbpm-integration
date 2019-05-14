import renderer from "react-test-renderer";
import React from "react";
import PageMappingDiagrams from "../../component/tabMigrationPlan/wizardAddPlan/PageMappingDiagrams";
import definitions from "../../../mock_data/definitions.json";

test("PageMappingDiagrams renders correctly using snapshot", () => {
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
        sourceInfo={definitions.sourceInfo}
        targetInfo={definitions.targetInfo}
      />
    )
    .toJSON();
  expect(tree).toMatchSnapshot();
});
