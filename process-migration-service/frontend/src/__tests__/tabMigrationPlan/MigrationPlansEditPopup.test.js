import renderer from "react-test-renderer";
import React from "react";
import MigrationPlansEditPopup from "../../component/tabMigrationPlan/MigrationPlansEditPopup";

test("MigrationPlansEditPopup renders correctly using snapshot", () => {
  const myMock = jest.fn();
  const tree = renderer
    .create(
      <MigrationPlansEditPopup
        title="Import Migration Plan"
        actionName="Import Plan"
        retrieveAllPlans={myMock}
        updatePlan={myMock}
      />
    )
    .toJSON();
  expect(tree).toMatchSnapshot();
});
