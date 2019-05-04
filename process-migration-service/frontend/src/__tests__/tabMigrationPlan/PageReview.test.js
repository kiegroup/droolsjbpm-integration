import renderer from "react-test-renderer";
import React from "react";
import PageReview from "../../component/tabMigrationPlan/PageReview";
import { MockupData_PIM_response } from "../../component/common/MockupData";

test("PageReview renders correctly using snapshot", () => {
  const mockData = MockupData_PIM_response;
  const tree = renderer
    .create(<PageReview inputJsonStr={JSON.stringify(mockData)} />)
    .toJSON();
  expect(tree).toMatchSnapshot();
});
