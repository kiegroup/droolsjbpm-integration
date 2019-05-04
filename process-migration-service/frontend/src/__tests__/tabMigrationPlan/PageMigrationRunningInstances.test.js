import renderer from "react-test-renderer";
import React from "react";
import PageMigrationRunningInstances from "../../component/tabMigrationPlan/wizardExecuteMigration/PageMigrationRunningInstances";
import { MockupData_runningInstances } from "../../component/common/MockupData";

test("PageMigrationRunningInstances renders correctly using snapshot", () => {
  const instances = MockupData_runningInstances;
  const myMock = jest.fn();
  const tree = renderer
    .create(
      <PageMigrationRunningInstances
        runningInstances={instances}
        setRunngingInstancesIds={myMock}
      />
    )
    .toJSON();
  expect(tree).toMatchSnapshot();
});
