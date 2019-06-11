import React from "react";
import renderer from "react-test-renderer";
import Notification from "./";

test("Notification tests", () => {
  const mock = jest.fn();
  const tree = renderer
    .create(
      <Notification type="error" message="Test message" onDismiss={mock} />
    )
    .toJSON();
  expect(tree).toMatchSnapshot();
});
