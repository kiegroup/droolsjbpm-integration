import renderer from "react-test-renderer";
import React from "react";
import PageMigrationRunningInstances from "../../component/tabMigrationPlan/wizardExecuteMigration/PageMigrationRunningInstances";
import running_instances from "../../../mock_data/running_instances.json";

test("PageMigrationRunningInstances renders correctly using snapshot", () => {
  const myMock = jest.fn();
  const tree = renderer
    .create(
      <PageMigrationRunningInstances
        runningInstances={running_instances}
        setRunningInstancesIds={myMock}
        onIsValid={myMock}
      />
    )
    .toJSON();
  expect(tree).toMatchSnapshot();
});
