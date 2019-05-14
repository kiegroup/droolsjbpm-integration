import React, { Component } from "react";
import classNames from "classnames";
import axios from "axios";

import {
  TabContainer,
  Nav,
  NavItem,
  TabPane,
  TabContent
} from "patternfly-react";
import { DropdownButton, MenuItem } from "patternfly-react";

import MigrationPlans from "./tabMigrationPlan/MigrationPlans";
import MigrationDefinitions from "./tabMigration/MigrationDefinitions";
import { BACKEND_URL } from "./common/PimConstants";

export default class MainPageWithPfTab extends Component {
  constructor(props) {
    super(props);

    this.state = {
      kieServerIds: "",
      title: "KIE Server Name",
      menuItems: []
    };
  }

  componentDidMount() {
    const servicesUrl = BACKEND_URL + "/kieserver";
    axios
      .get(servicesUrl, {})
      .then(res => this.populateKieServers(res.data, this));
  }

  populateKieServers(kieServerIds, self) {
    let menuItems = self.state.menuItems;
    kieServerIds.map((id, i) => {
      if (i == 0) {
        const newTitle = "KIE Server Name:" + id;
        self.setState({ title: newTitle });
        self.setState({ kieServerIds: id });
      }
      menuItems.push(
        <MenuItem key={i} eventKey={id} onSelect={self.handleChange}>
          {id}
        </MenuItem>
      );
    });
    self.setState({ menuItems });
  }

  handleChange = option => {
    const newTitle = "KIE Server Name:" + option;
    this.setState({ title: newTitle });
    this.setState({ kieServerIds: option });
  };

  render() {
    const bsClass = classNames("nav nav-tabs nav-tabs-pf", {
      "nav-justified": false
    });

    return (
      <div className="">
        <div className="row">
          <div className="col-xs-9">
            <h1>Process Instance Migration</h1>
          </div>
          <div className="col-xs-3">
            <br />
            <div className="pull-right">
              <DropdownButton id={"kieDropDown"} title={this.state.title}>
                {this.state.menuItems}
              </DropdownButton>
            </div>
          </div>
        </div>
        <TabContainer id="tabs-with-dropdown-pf" defaultActiveKey="first">
          <div>
            <Nav bsClass={bsClass}>
              <NavItem eventKey="first">Migration Plans</NavItem>
              <NavItem eventKey="second">Migrations</NavItem>
            </Nav>

            <TabContent animation>
              <TabPane eventKey="first">
                <MigrationPlans kieServerIds={this.state.kieServerIds} />
              </TabPane>
              <TabPane eventKey="second">
                <MigrationDefinitions />
              </TabPane>
            </TabContent>
          </div>
        </TabContainer>
      </div>
    );
  }
}
