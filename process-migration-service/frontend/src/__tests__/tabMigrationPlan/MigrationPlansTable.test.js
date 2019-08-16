import renderer from "react-test-renderer";
import React from "react";
import plans from "../../../mock_data/plans.json";
import MigrationPlansTable from "../../component/tabMigrationPlan/MigrationPlansTable";

test("MigrationPlansTable renders correctly using snapshot", () => {
  const myMock = jest.fn();
  const tree = renderer
    .create(
      <MigrationPlansTable
        plans={plans}
        kieServerId="kieServer1"
        onEditPlan={myMock}
        onDeletePlan={myMock}
      />
    )
    .toJSON();
  expect(tree).toMatchSnapshot();
});
