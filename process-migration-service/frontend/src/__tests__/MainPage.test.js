import React from "react";
import renderer from "react-test-renderer";

import MainPage from "../component/MainPage";

describe("Main Page", () => {
  it("renders without crashing", () => {
    const tree = renderer.create(<MainPage />).toJSON();
    expect(tree).toMatchSnapshot();
  });
});
