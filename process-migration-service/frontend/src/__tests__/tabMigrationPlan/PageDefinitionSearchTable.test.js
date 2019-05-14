import renderer from "react-test-renderer";
import React from "react";
import PageDefinitionSearchTable from "../../component/tabMigrationPlan/wizardAddPlan/PageDefinitionSearchTable";

test("PageDefinitionSearchTable renders correctly using snapshot", () => {
  const myMock = jest.fn();
  const tree = renderer
    .create(
      <PageDefinitionSearchTable
        tableHeader="Source"
        processId="testProcessId"
        groupId="testGroupId"
        artifactId="testArtifactId"
        version="testVersion"
        handleProcessIdChange={myMock}
        handleGroupIdChange={myMock}
        handleArtifactIdChange={myMock}
        handleVersionChange={myMock}
      />
    )
    .toJSON();
  expect(tree).toMatchSnapshot();
});
