import renderer from "react-test-renderer";
import React from "react";
import PagePlanName from "../../component/tabMigrationPlan/wizardAddPlan/PagePlanName";

test("PagePlanName renders correctly", () => {
  const plan = {};
  const mockFn = jest.fn();
  const tree = renderer
    .create(
      <PagePlanName plan={plan} onFieldChanged={mockFn} onIsValid={mockFn} />
    )
    .toJSON();
  expect(tree).toMatchSnapshot();
});

test("PagePlanName renders correctly using existing data", () => {
  const plan = {
    name: "plan name",
    description: "description"
  };
  const mockFn = jest.fn();
  const tree = renderer
    .create(
      <PagePlanName plan={plan} onFieldChanged={mockFn} onIsValid={mockFn} />
    )
    .toJSON();
  expect(tree).toMatchSnapshot();
});