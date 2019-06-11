import React, { Component } from "react";
import classNames from "classnames";
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
import { Button } from "patternfly-react/dist/js/components/Button";
import { Icon } from "patternfly-react/dist/js/components/Icon";

export default class MainPageWithPfTab extends Component {
  constructor(props) {
    super(props);

    this.state = {
      kieServerId: "",
      title: "KIE Server Name",
      menuItems: []
    };
  }

  componentDidMount() {
    this.loadKieServers();
  }

  loadKieServers = () => {
    fetch(`${BACKEND_URL}/kieserver`, {
      headers: {
        "Content-Type": "application/json"
      },
      credentials: "same-origin"
    })
      .then(res => {
        if (!res.ok) {
          throw res;
        }
        return res.json();
      })
      .then(res => this.populateKieServers(res))
      .catch(() => {
        this.populateKieServers([]);
      });
  };

  populateKieServers = kieServers => {
    let menuItems = [];
    if (kieServers.size === 0) {
      menuItems.push(
        <MenuItem key={0} eventKey="no-kieservers" disabled>
          No configured KieServers
        </MenuItem>
      );
    } else {
      kieServers.map((kieServer, i) => {
        const kieServerId =
          kieServer.id === null ? "unknown-" + i : kieServer.id;
        if (this.state.kieServerId === "") {
          this.setState({ kieServerId });
        }
        menuItems.push(
          <MenuItem
            key={i}
            eventKey={kieServerId}
            onSelect={this.handleChange}
            disabled={kieServer.status !== "SUCCESS"}
          >
            {kieServerId}
          </MenuItem>
        );
      });
    }
    this.setState({ menuItems });
  };

  handleChange = option => {
    this.setState({ kieServerId: option });
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
              <span>KIE Server: </span>
              <DropdownButton id={"kieDropDown"} title={this.state.kieServerId}>
                {this.state.menuItems}
              </DropdownButton>
              <Button onClick={this.loadKieServers}>
                <Icon type="fa" name="refresh" />
              </Button>
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
                <MigrationPlans kieServerId={this.state.kieServerId} />
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
